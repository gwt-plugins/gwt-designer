package com.instantiations.pde.build.server;

import com.instantiations.pde.build.util.BuildProperties 
import com.instantiations.pde.build.util.BuildUtil;

import java.io.File;
import java.util.Iterator;
import java.util.List;

/**
 * Instances of the Groovy class <code>Build</code> represent a group of related build
 * modules.
 * <p>
 * Copyright (c) 2010, Instantiations, Inc.<br>
 * All Rights Reserved
 * 
 * @author Bryan Shepherd
 */
public class CodeProServerBuild {
	/**
	 * The name of the product represented by this build.
	 */
	private String name;
	
	/**
	 * An array containing the modules that are part of this build.
	 */
	private CodeProServerBuildModule[] modules;

	private static String buildTempLocation;			//temp directory for build
	private static String productTempLocation;			//Product's temporary directory
	private static String eclipseLocation;				//Target eclipse environment
	private static String auditRulesetLocation;			//RuleSet file used
	private static String metricsLocation;				//Metrics file used
	private static String workspaceLocation;			//Workspace that contains the plugin projects
	
	private static List dontBuildList = [];
	
	// //////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// //////////////////////////////////////////////////////////////////////////
	
	/**
	 * Initialize a newly created build.
	 */
	public CodeProServerBuild(String name, CodeProServerBuildModule[] modules) {
		this.name = name;
		this.modules = modules;
	}
	
	// //////////////////////////////////////////////////////////////////////////
	//
	// Externalizing
	//
	// //////////////////////////////////////////////////////////////////////////
	
	/**
	 * Create input files for each of the modules in this build, writing them to
	 * the given directory.
	 * 
	 * @param directory
	 *            the directory to which the input files are to be written
	 */
	public void createAllFilesIn(File directory) {
		createInputFilesIn(directory);
	}
	
	/**
	 * Create input files for each of the modules in this build, writing them to
	 * the given directory.
	 * 
	 * @param directory
	 *            the directory to which the input files are to be written
	 */
	public void createInputFilesIn(File directory) {
		CodeProServerBuildModule module;
		File inputFile;
		
		for (int i = 0; i < modules.length; i++) {
			module = modules[i];
			inputFile = new File(directory, module.getName() + "-input.xml");
			try {
				module.createInputFile(inputFile);
			} catch (IOException exception) {
				System.out.println("Could not create input file for "
						+ module.getName());
				exception.printStackTrace();
			}
		}
		createDashboardInputIn(directory);
	}
	
	/**
	 * Create input files for each of the modules in this build, writing them to
	 * the given directory.
	 * 
	 * @param directory
	 *            the directory to which the input files are to be written
	 */
	public void createDashboardInputIn(File directory) {
		CodeProServerBuildSpecification buildSpecification;
		File batchFile;
		FileWriter fileWriter = null;
		PrintWriter printWriter = null;
		
		if (modules.length == 0) {
			return;
		}
		buildSpecification = modules[0].getBuildSpecification();
		batchFile = new File(directory, modules[0].getName() + "-DashboardCreation-input.xml");
		try {
			fileWriter = new FileWriter(batchFile);
			printWriter = new PrintWriter(new BufferedWriter(fileWriter));
			writeDashboardGroupPage(printWriter, buildSpecification
					.getDashboardDirectory());
		} catch (IOException exception) {
			System.out
					.println("Could not create batch file for dashboard creation");
			exception.printStackTrace();
		} finally {
			if (printWriter != null) {
				printWriter.flush();
			}
			if (fileWriter != null) {
				try {
					fileWriter.close();
				} catch (IOException exception) {
					// Ignored
				}
			}
		}
	}
	
	// //////////////////////////////////////////////////////////////////////////
	//
	// Externalizing - internal
	//
	// //////////////////////////////////////////////////////////////////////////
	
	public void writeDashboardGroupPage(PrintWriter writer,
	String reportDirectory) {
		CodeProServerBuildSpecification buildSpecification;
		
		if (modules.length == 0) {
			return;
		}
		buildSpecification = modules[0].getBuildSpecification();
		
		writer.println("<serverApplication version=\"1.0\">");
		buildSpecification.writeDataStoreDefinition(writer);
		writer.println();
		writer.println("	<!-- Dashboard Creation -->");
		writer.println();
		writer.println("	<dashboard");
		writer.print("			reportDirectory=\"");
		writer.print(reportDirectory);
		writer.println("\"");
		writer.println("			timeSpans=\"2w\">");
		writer.println("		<clientData name=\"Instantiations, Inc.\"/>");
		writer.println("		<dataStoreRef id=\"dataStore\"/>");
		writer.println("		<projectGroupPage");
		writer.print("				name=\"");
		writer.print(name);
		writer.println("\"");
		writer.print("				buildNumber=\"");
		writer.print(buildSpecification.getBuildNumber());
		writer.println("\"");
		writer.print("				buildDate=\"");
		writer.print(buildSpecification.getBuildDate());
		writer.println("\">");
		for (int i = 0; i < modules.length; i++) {
			modules[i].writeDashboardGroupPage(writer);
		}
		writer.println("		</projectGroupPage>");
		writer.println("	</dashboard>");
		writer.println("</serverApplication>");
	}
	
	// //////////////////////////////////////////////////////////////////////////
	//
	// Instance Creation
	//
	// //////////////////////////////////////////////////////////////////////////
	
	/**
	 * Return a build representing the CodePro code base.
	 */
	public static CodeProServerBuild codeProServerBuildFor(BuildProperties prop) {
		
		CodeProServerBuildSpecification buildSpecification = new CodeProServerBuildSpecification(eclipseLocation, prop.get('build.analysis.datastore'), prop.get('build.analysis.dashboard'), auditRulesetLocation, metricsLocation);
		
		//Analysis directory in the build temp product directory
		File workspaceDir = new File(workspaceLocation);
		File [] fileArray = workspaceDir.listFiles();
		Iterator<File> iter = fileArray.iterator();
		List files = new ArrayList();
		while(iter.hasNext()) {
		    File dir = (File)iter.next();
		    if(!dontBuildList.contains(dir.getName()))
			files.add(dir.getName());
		}
				
		String [] sarray = files.toArray();
		CodeProServerBuildModule [] barray = [];
		CodeProServerBuildModule cpsbm = new CodeProServerBuildModule(prop.get('product.name'), buildSpecification, barray, workspaceLocation, sarray, prop);
		
		barray = [cpsbm];
		return new CodeProServerBuild(prop.get('product.name'), barray);
	}
	
	// //////////////////////////////////////////////////////////////////////////
	//
	// Run the build
	//
	// //////////////////////////////////////////////////////////////////////////
	
	public static void createInput(BuildProperties prop) {
		File analysisDir = new File(prop.get('build.analysis'));
		analysisDir.mkdirs();
			
		//Product's temporary directory
		File productTmpDir = new File(prop.productTemp, prop.get('build.analysis.target')).getCanonicalFile(); 
		productTempLocation = productTmpDir.getCanonicalPath();
		//Workspace that contains the plugin projects
		File eclipseDir = new File(productTempLocation,  'target/eclipse').getCanonicalFile();
		File workspaceDir = new File(analysisDir, 'workspace').getCanonicalFile();
		workspaceLocation = workspaceDir.getCanonicalPath();
		//Target eclipse environment
		eclipseLocation =  eclipseDir.getCanonicalPath();
		
		System.out.println("productTempLocation: "+productTempLocation);
		System.out.println("workspaceLocation: "+workspaceLocation);
		System.out.println("eclipseLocation: "+eclipseLocation);
		
		//RuleSet file used
		File auditPrefFile = new File(prop.getBuildCommonData(), 'analysis/InstantiationsStandards.pref');
		auditRulesetLocation = auditPrefFile.getCanonicalPath();
		
		//Metrics file used
		File metricPrefFile = new File(prop.getBuildCommonData(), 'analysis/metrics.pref');
		metricsLocation = metricPrefFile.getCanonicalPath();
		
		codeProServerBuildFor(prop).createAllFilesIn(analysisDir);
	}
}
