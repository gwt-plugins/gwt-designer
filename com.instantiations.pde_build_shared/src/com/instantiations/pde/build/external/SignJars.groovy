/**
 * 
 */
package com.instantiations.pde.build.external;

import groovy.xml.MarkupBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;

import com.instantiations.pde.build.util.BuildUtil;

/**
 * @author markr
 *
 */
public class SignJars extends BuildUtil {
	
	/**
	 * directory to sign
	 */
	private File dirToSign;
	
	/**
	 * the host to communicate with
	 */
	private String host = "localhost";
	
	/**
	 * the port to communicate on
	 */
	private int port = 8188;

	/**
	 * 
	 */
	public SignJars(File updateDir) {
		this.dirToSign = updateDir;
	}
	/**
	 * 
	 */
	public SignJars(File updateDir, String host, int port) {
		this.dirToSign = updateDir;
		this.host = host;
		this.port = port;
	}
	
	public void signJars(File logFile) {
		println("chmod g+w of $dirToSign, $dirToSign/plugins/*, and $dirToSign/features/*")
		ant.chmod(perm: "g+w") {
			fileset(dir: dirToSign) {
				include(name: 'plugins/*');
				include(name: 'features/*');
			}
			dirset(dir: dirToSign.parent) {
				include(name: dirToSign.name);
				include(name: dirToSign.name + '/**/plugins');
				include(name: dirToSign.name + '/**/features');
			}
		}

    	Socket s = new Socket(host, port);
    	BufferedReader sockIn = null;
    	PrintWriter sockOut = null;
    	try {
    		sockIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
    		sockOut = new PrintWriter(s.getOutputStream(), true /* autoFlush */);
    		
    		String xml = createMarkup(logFile);
    		sockOut.println(xml);
    		sockOut.println(SignJarsConstants.END_OF_TRANSMITION);
	    	println('request sent');
	    	processRequest(sockIn, sockOut);
    	}
    	finally {
    		if (sockIn != null) {
    			sockIn.close();
    		}
    		if (sockOut != null) {
    			sockOut.close();
    		}
    		println("chmod g-w of $dirToSign, $dirToSign/plugins/*, and $dirToSign/features/*")
    		ant.chmod(perm: "g-w") {
    			fileset(dir: dirToSign) {
    				include(name: 'plugins/*');
    				include(name: 'features/*');
    			}
    			dirset(dir: dirToSign.parent) {
    				include(name: dirToSign.name);
    				include(name: dirToSign.name + '/**/plugins');
    				include(name: dirToSign.name + '/**/features');
    			}
    		}
    	}
	}
	
	private String createMarkup(File logFile) {
		String ret = null;
		StringWriter writer = new StringWriter();
		MarkupBuilder builder = new MarkupBuilder(writer);
		def signdata = builder.signdata {
			elements() {
				element(dir: this.dirToSign);
			}
			log(name: logFile.canonicalPath);
		}
		ret = writer.toString();
		printf('Signing jars with XML:%n%s%n',  ret);
		return ret;
	}
	
	private void processRequest(BufferedReader sockIn, PrintWriter sockOut) {
    	String line;
		line = sockIn.readLine();
		if (!(line == null || line.equals(SignJarsConstants.END_OF_TRANSMITION))) {
			switch (line) {
    			case SignJarsConstants.JAR_SIGNING_FAILED:
    				println('Jar Signing Failed');
    				line = sockIn.readLine();
    				File log = new File(line);
    				println("log file is $log");
    				readRest(sockIn);
    				String logFileData = printLog(log);
    				warn("the jars in directory $log could not be signed", logFileData);
    				break;
    				
    			case SignJarsConstants.JARS_SIGNED:
    				line = sockIn.readLine();
    				File log = new File(line);
    				println("log file is $log");
    				readRest(sockIn);
    				printLog(log);
    				break;
    				
    			default:
    				println("*** DEFAULT ****");
	    			println(line);
	    			String restOfData = readRest(sockIn);
    				warn('unknown response came from JarSigning', restOfData)
    				break;
			}
    	}

	}

	private String readRest(BufferedReader sockIn) {
		Boolean more = true;
		StringBuffer sb = new StringBuffer();
		while (more) {
    		def line = sockIn.readLine();
    		if (line == null || line.equals(SignJarsConstants.END_OF_TRANSMITION)) {
    			more = false;
    		}
    		else {
    			sb.append(sprintf("%s", line));
    		}
		}
		println(sb.toString())
		return sb.toString();
	}
	
	private String printLog(File log) {
		Integer counter = 0;
		StringBuffer sb = new StringBuffer();
		log.eachLine { line -> 
			sb.append(sprintf('%5d: %s%n', counter++, line));
		}
		println(sb.toString());
		return sb.toString();
	}
}
