package com.instantiations.pde.build.external;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Wrappers a {@link ProcessBuilder} and a {@link Process}
 */
public class ProcessRunner
{
	private final String name;
	private ArrayList<String> cmdLine;
	private File workingDir;
	private File logFile;
	private Map<String, String> environment;
	
	private ProcessBuilder builder;
	private Process process;
	private boolean resultsEchoed = false;

	/**
	 * Construct a new instance to launch and monitor a process
	 * @param cmds the command line
	 */
	public ProcessRunner(String name) {
		this.name = name;
	}

	/**
	 * Set the working directory used by the external process
	 */
	public void setWorkingDir(File workingDir) {
		this.workingDir = workingDir;
	}

	/**
	 * Set the command line used to launch the external process
	 */
	public void setCmdLine(ArrayList<String> cmdLine) {
		this.cmdLine = cmdLine;
	}

	/**
	 * Set the log file into which stdout and stderr from the external process will be sent
	 */
	public void setLogFile(File logFile) {
		this.logFile = logFile;
	}
	
	/**
	 * Set an environment variable to be used by the spawned process
	 */
	public void setEnvironmentVar(String key, String value) {
		if (environment == null)
			environment = new HashMap<String, String>(10);
		environment.put(key, value);
	}
	
	/**
	 * Answer the name specified in the constructor
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Answer the log file
	 */
	public File getLogFile() {
		return logFile;
	}

	/**
	 * Launch the external process and return without waiting for it to complete
	 * @see #waitForResult()
	 */
	public void launch() throws IOException {
		if (process != null)
			throw new IllegalStateException("Process has already been launched: " + name);
		builder = new ProcessBuilder(cmdLine);
		builder.directory(workingDir);
		builder.redirectErrorStream(true);
		Map<String, String> env = builder.environment();
		if (environment != null)
			env.putAll(environment);

		// Echo launch information before launching

		System.out.println("Launching " + name);
		System.out.println("  Working directory: " + builder.directory().getCanonicalPath());
		System.out.println("  Environment:");
		for (String key : new TreeSet<String>(env.keySet()))
			System.out.println("    " + key + " : " + env.get(key));
		System.out.println("  Command Line:");
		for (String each : builder.command())
			System.out.println("    " + each);

		// Launch the process and consume the output and error streams

		process = builder.start();
		consumeProcessOutput();
		process.getOutputStream().close();
	}

	private void consumeProcessOutput() {
		new Thread(name + " Output Handler") {
			public void run() {
				byte[] buf = new byte[1024];
				InputStream in = process.getInputStream();
				FileOutputStream out = null;
				try {
					out = new FileOutputStream(logFile);
				}
				catch (FileNotFoundException e) {
					System.err.println("Failed to open log file: " + logFile.getPath());
					e.printStackTrace();
				}
				try {
					while (true) {
						int count;
						try {
							count = in.read(buf);
						}
						catch (IOException e) {
							System.err.println("Failed to read from process stream: " + name);
							e.printStackTrace();
							break;
						}
						if (count == -1)
							break;
						if (out != null)
							try {
								out.write(buf, 0, count);
							}
							catch (IOException e) {
								System.err.println("Failed to write to log file: " + logFile.getPath());
								e.printStackTrace();
								out = null;
							}
					}
				}
				finally {
					try {
						out.close();
					}
					catch (IOException e) {
						System.err.println("Failed to close log file: " + logFile.getPath());
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	/**
	 * Wait for the process to complete and echo the build succeed/fail info
	 * 
	 * @param maxWaitTimeMillis the number of milliseconds to wait before
	 * 		forcibly terminating the external process.
	 * @return <code>true</code> if the process completed successfully
	 */
	public boolean waitForResult(final long maxWaitTimeMillis) {
		final long startTime = System.currentTimeMillis();
		final boolean[] completed = new boolean[1];
		completed[0] = false;
		if (maxWaitTimeMillis > 0) {
			Thread watcher = new Thread(getName() + " Watcher") {
				public void run() {
					while (!completed[0]) {
						if (System.currentTimeMillis() - startTime > maxWaitTimeMillis) {
							process.destroy();
							break;
						}
						try {
							Thread.sleep(1000);
						}
						catch (InterruptedException e) {
							// ignored
						}
					}
				}
			};
			watcher.start();
		}
		boolean result = waitForResult();
		completed[0] = true;
		return result;
	}

	/**
	 * Wait for the process to complete and echo the build succeed/fail info
	 * 
	 * @return <code>true</code> if the process completed successfully
	 */
	public boolean waitForResult() {
		int result;
		while (true) {
			try {
				result = process.waitFor();
				break;
			}
			catch (InterruptedException e) {
				// Fall through and loop to continue waiting
			}
		}
		if (!resultsEchoed) {
			if (result == 0)
				System.out.println(name + " Succeeded");
			else
				System.err.println(name + " Failed: " + result);
			resultsEchoed = true;
		}
		return result == 0;
	}
	
	//============================================================================
	// Test
	
//	public static void main(String[] args) throws Exception {
//		ProcessRunner runner = new ProcessRunner("Test");
//		
//		ArrayList<String> cmdLine = new ArrayList<String>();
//		cmdLine.add("java");
//		runner.setCmdLine(cmdLine);
//		
//		File workingDir = new File(System.getProperty("java.io.tmpdir"), ProcessRunner.class.getName());
//		workingDir.mkdirs();
//		runner.setWorkingDir(workingDir);
//		
//		File logFile = new File(workingDir, "log.txt");
//		logFile.delete();
//		System.out.println("Log File: " + logFile.getCanonicalPath());
//		runner.setLogFile(logFile);
//		
//		runner.launch();
//		runner.waitForResult(10);
//		
//		if (logFile.exists()) {
//			LineNumberReader reader = new LineNumberReader(new BufferedReader(new FileReader(logFile)));
//			try {
//				while (true) {
//					String line = reader.readLine();
//					if (line == null)
//						break;
//					System.out.println(line);
//				}
//			}
//			finally {
//				reader.close();
//			}
//		}
//		System.out.println("Test Complete");
//	}
}
