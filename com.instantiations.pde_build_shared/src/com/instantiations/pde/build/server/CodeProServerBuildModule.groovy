package com.instantiations.pde.build.server;

import groovy.xml.MarkupBuilder;

import java.io.File;

import com.instantiations.pde.build.util.BuildProperties;

/**
 * Instances of the class <code>BuildModule</code> represent a group of projects
 * that are built and analyzed together within the build system.
 * <p>
 * Copyright (c) 2010, Instantiations, Inc.<br>
 * All Rights Reserved
 *
 * @author Bryan Shepherd
 */
public class CodeProServerBuildModule {
	/**
	 * The name of the build module.
	 */
	private String name;
	
	/**
	 * A description of information common across all builds.
	 */
	private CodeProServerBuildSpecification buildSpecification;
	
	/**
	 * An array containing all of the build modules containing projects that are
	 * referenced by the projects in this build module.
	 */
	private CodeProServerBuildModule[] referencedModules;
	
	/**
	 * The absolute path of the directory containing the projects that are part
	 * of this build.
	 */
	private String projectDirectory;
	
	/**
	 * An array containing the names of the projects that are part of this
	 * build.
	 */
	private String[] projectNames;
	
	/**
	 * build properties used for creating the CodeProServer input file
	 */
	BuildProperties prop;
	
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Initialize a newly created build module.
	 *
	 * @param name the name of the build module
	 * @param buildSpecification a description of information common across all
	 *        builds
	 * @param referencedModules the build modules containing projects that are
	 *        referenced by the projects in this build module
	 * @param projectDirectory the directory containing the projects that are
	 *        part of this build
	 * @param projectNames the names of the projects that are part of this build
	 */
	public CodeProServerBuildModule(String name, CodeProServerBuildSpecification buildSpecification, 
									CodeProServerBuildModule[] referencedModules, String projectDirectory, 
									String[] projectNames, BuildProperties prop) {
		this.name = name;
		this.buildSpecification = buildSpecification;
		this.referencedModules = referencedModules;
		this.projectDirectory = projectDirectory;
		this.projectNames = projectNames;
		this.prop = prop;
	}
	
	////////////////////////////////////////////////////////////////////////////
	//
	// Accessing
	//
	////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Return the name of this build module.
	 *
	 * @return the name of this build module
	 */
	public String getName()	{
		return name;
	}
	
	/**
	 * Return a description of information common across all builds.
	 *
	 * @return a description of information common across all builds
	 */
	public CodeProServerBuildSpecification getBuildSpecification() {
		return buildSpecification;
	}
	
	////////////////////////////////////////////////////////////////////////////
	//
	// Externalizing
	//
	////////////////////////////////////////////////////////////////////////////
	
	public void createInputFile(File inputFile) throws IOException {
		FileWriter fileWriter = null;
		PrintWriter printWriter = null;
		
		try {
			fileWriter = new FileWriter(inputFile);
			printWriter = new PrintWriter(new BufferedWriter(fileWriter));
			writeInputFile(printWriter);
		} finally {
			if (printWriter != null) {
				printWriter.flush();
			}
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
	}
	
	public void createBatchFile(File batchFile) throws IOException {
		FileWriter fileWriter = null;
		PrintWriter printWriter = null;
		
		try {
			fileWriter = new FileWriter(batchFile);
			printWriter = new PrintWriter(new BufferedWriter(fileWriter));
			writeBatchFile(batchFile.getParentFile(), printWriter);
		} finally {
			if (printWriter != null) {
				printWriter.flush();
			}
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
	}
	
	public void writeDashboardGroupPage(PrintWriter writer) {
		//		writer.println();
		//		writer.println("	<!-- Dashboard Creation -->");
		//		writer.println();
		//		writer.println("	<dashboard");
		//		writer.println("			reportDirectory=\"C:/ManualTest/Server/CodePro/results/dashboard\"");
		//		writer.println("			timeSpans=\"2w\">");
		//		writer.println("		<clientData name=\"Instantiations, Inc.\"/>");
		//		writer.println("		<dataStoreRef id=\"dataStore\"/>");
		writer.println("			<projectGroupPage");
		writer.print("					name=\"");
		writer.print(name);
		writer.println("\"");
		writer.print("					buildNumber=\"");
		writer.print(buildSpecification.getBuildNumber());
		writer.println("\"");
		writer.print("					buildDate=\"");
		writer.print(buildSpecification.getBuildDate());
		writer.println("\">");
		for (int i = 0; i < projectNames.length; i++) {
			writer.print("				<projectPage name=\"");
			writer.print(projectNames[i]);
			writer.println("\"/>");
		}
		writer.println("			</projectGroupPage>");
		//		writer.println("	</dashboard>");
	}
	
	////////////////////////////////////////////////////////////////////////////
	//
	// Externalizing - internal
	//
	////////////////////////////////////////////////////////////////////////////
	
	private void writeInputFile(PrintWriter writer)	{
		writer.println("<serverApplication version=\"1.0\">");
		writeWorkspaceDefinition(writer);
		buildSpecification.writeDataStoreDefinition(writer);
		writeSelections(writer);
		writeAudits(writer);
		writeDeadCode(writer);
		writeDependency(writer);
		writeMetrics(writer);
		writeSimilarCode(writer);
		writer.println("</serverApplication>");
	}
	
	private void writeWorkspaceDefinition(PrintWriter writer) {
		String targetPlatformDirectory;
		
		targetPlatformDirectory = buildSpecification.getTargetPlatformDirectory();
		writer.println();
		writer.println("	<!-- Set up the workspace -->");
		writer.println();
		writer.println("	<workspace>");
		if (targetPlatformDirectory != null) {
			Collection<?> keys = prop.getKeys();
			
			writer.print("		<targetPlatform directory=\"");
			writer.print(targetPlatformDirectory);
			writer.print('"');
			if (keys.contains('build.analysis.target.os')) {
				writer.print(' os="');
				writer.print(prop.get('build.analysis.target.os'));
				writer.print('"')
			}
			if (keys.contains('build.analysis.target.ws')) {
				writer.print(' ws="');
				writer.print(prop.get('build.analysis.target.ws'));
				writer.print('"')
			}
			writer.println("/>");
		}
		writeProjectsInWorkspace(writer);
		writer.println("	</workspace>");
	}
	
	private void writeProjectsInWorkspace(PrintWriter writer) {
		for (int i = 0; i < referencedModules.length; i++) {
			referencedModules[i].writeProjectsInWorkspace(writer);
		}
		Writer xmlWriter = new StringWriter();
		def builder = new MarkupBuilder(xmlWriter);
		def projects = builder.projects {
			for (int i = 0; i < projectNames.length; i++) {
				project(directory: "${projectDirectory}${File.separator}${projectNames[i]}");
			}
		}
		String result = xmlWriter.toString();
//		println("projects: $result");
		writer.println(result);
	}
	
	private void writeSelections(PrintWriter writer) {
		writer.println();
		writer.println("	<!-- Define selections for each of the projects -->");
		writer.println();
		for (int i = 0; i < projectNames.length; i++) {
			writer.print("	<selection id=\"");
			writer.print(projectNames[i]);
			writer.println("\">");
			writer.print("		<resource path=\"");
			writer.print(projectNames[i]);
			writer.println("\"/>");
			writer.println("	</selection>");
		}
	}
	
	private void writeAudits(PrintWriter writer) {
		String auditRuleSetFile;
		
		auditRuleSetFile = buildSpecification.getAuditRuleSetFile();
		
		writer.println();
		writer.println("	<!-- Code Audit -->");
		writer.println();
		for (int i = 0; i < projectNames.length; i++) {
			writer.print("	<audit name=\"");
			writer.print(projectNames[i]);
			writer.println("\">");
			writer.print("		<selectionRef id=\"");
			writer.print(projectNames[i]);
			writer.println("\"/>");
			if (auditRuleSetFile != null) {
				writer.print("		<auditRuleSet file=\"");
				writer.print(auditRuleSetFile);
				writer.println("\"/>");
			}
			writer.println("		<dataStoreRef id=\"dataStore\"/>");
			writer.println("	</audit>");
		}
	}
	
	private void writeDeadCode(PrintWriter writer) {
		writer.println();
		writer.println("	<!-- Dead Code Analysis -->");
		writer.println();
		for (int i = 0; i < projectNames.length; i++) {
			writer.println("	<deadCode>");
			writer.print("		<selectionRef id=\"");
			writer.print(projectNames[i]);
			writer.println("\"/>");
			writer.println("		<dataStoreRef id=\"dataStore\"/>");
			writer.println("	</deadCode>");
		}
	}
	
	private void writeDependency(PrintWriter writer) {
		writer.println();
		writer.println("	<!-- Dependency Analysis -->");
		writer.println();
		for (int i = 0; i < projectNames.length; i++) {
			writer.println("	<dependency>");
			writer.print("		<selectionRef id=\"");
			writer.print(projectNames[i]);
			writer.println("\"/>");
			writer.println("		<dataStoreRef id=\"dataStore\"/>");
			writer.println("	</dependency>");
		}
	}
	
	private void writeMetrics(PrintWriter writer) {
		String metricSetFile;
		
		metricSetFile = buildSpecification.getMetricSetFile();
		
		writer.println();
		writer.println("	<!-- Metrics Analysis -->");
		writer.println();
		for (int i = 0; i < projectNames.length; i++) {
			writer.print("	<metrics name=\"");
			writer.print(projectNames[i]);
			writer.println("\">");
			writer.print("		<selectionRef id=\"");
			writer.print(projectNames[i]);
			writer.println("\"/>");
			if (metricSetFile != null) {
				writer.print("		<metricSet file=\"");
				writer.print(metricSetFile);
				writer.println("\"/>");
			}
			writer.println("		<dataStoreRef id=\"dataStore\"/>");
			writer.println("	</metrics>");
		}
	}
	
	private void writeSimilarCode(PrintWriter writer) {
		writer.println();
		writer.println("	<!-- Similar Code Analysis -->");
		writer.println();
		for (int i = 0; i < projectNames.length; i++) {
			writer.println("	<similarCode>");
			writer.print("		<selectionRef id=\"");
			writer.print(projectNames[i]);
			writer.println("\"/>");
			writer.println("		<dataStoreRef id=\"dataStore\"/>");
			writer.println("	</similarCode>");
		}
	}
	
	private void writeBatchFile(File directory, PrintWriter writer) {
		String directoryPath, workspacePath, logFilePath, javaPath, jarPath;
		
		directoryPath = directory.getAbsolutePath();
		workspacePath = "C:\\ManualTest\\Server\\CodePro\\temp\\workspace";
		logFilePath = workspacePath + "\\.metadata\\.log";
		javaPath = "C:\\Program Files\\Java\\jdk1.6.0_12\\bin\\java.exe";
		jarPath = "C:\\CodeProServer\\eclipse\\plugins\\org.eclipse.equinox.launcher_1.0.1.R33x_v20070828.jar";
		
		writer.println("@echo off");
		writer.println();
		writer.println("echo -----------------------------------------------------------");
		writer.println("echo CodePro - " + name);
		writer.println("echo -----------------------------------------------------------");
		writer.println();
		writer.println("setlocal");
		writer.println("title CodePro - " + name);
		writer.println();
		writer.println("if exist " + name + "-console.txt del " + name + "-console.txt");
		writer.println("if exist " + name + "-output.xml del " + name + "-output.xml");
		writer.println("if exist " + logFilePath + " del " + logFilePath);
		writer.println();
		writer.println("\"" + javaPath + "\" -Xms512M -Xmx1024M"
				+ " -classpath " + jarPath
				+ " org.eclipse.core.launcher.Main"
				+ " -debug"
				+ " -consolelog"
				+ " -os win32"
				+ " -ws win32"
				+ " -data " + workspacePath
				+ " -application com.instantiations.eclipse.server.app"
				+ " -in " + directoryPath + "\\" + name + "-input.xml"
				+ " -out " + directoryPath + "\\" + name + "-output.xml"
				+ " > " + name + "-console.txt");
		writer.println();
		writer.println("endlocal");
	}
}
