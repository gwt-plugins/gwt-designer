package com.instantiations.pde.build.server;

import java.text.SimpleDateFormat 

/**
 * Instances of the class <code>BuildSpecification</code> define the information
 * that is common across all builds.
 * <p>
 * Copyright (c) 2010, Instantiations, Inc.<br>
 * All Rights Reserved
 *
 * @author Bryan Shepherd
 */
class CodeProServerBuildSpecification
{
	/**
	 * The installation directory of the Eclipse to be used as a target
	 * platform, or <code>null</code> if there is no target platform.
	 */
	private String targetPlatformDirectory;

	/**
	 * The absolute path of the data store directory.
	 */
	private String dataStoreDirectory;

	/**
	 * The absolute path of the directory to which the dashboard should be
	 * written.
	 */
	private String dashboardDirectory;

	/**
	 * The date of the build.
	 */
	private String buildDate;

	/**
	 * The build number for the build.
	 */
	private String buildNumber;

	/**
	 * The absolute path of the file containing the audit rule set to use, or
	 * <code>null</code> if the default audit rule set is to be used.
	 */
	private String auditRuleSetFile;

	/**
	 * The absolute path of the file containing the metrics set to use, or
	 * <code>null</code> if the default metrics set is to be used.
	 */
	private String metricSetFile;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Initialize a newly created build specification with the given information.
	 *
	 * @param targetPlatformDirectory the installation directory of the Eclipse
	 *        to be used as a target platform
	 * @param dataStoreDirectory the absolute path of the data store directory
	 * @param dashboardDirectory the absolute path of the directory to which the
	 *        dashboard should be written
	 * @param auditRuleSetFile the absolute path of the file containing the
	 *        audit rule set to use
	 * @param metricSetFile the absolute path of the file containing the metrics
	 *        set to use
	 */
	public CodeProServerBuildSpecification(String targetPlatformDirectory, String dataStoreDirectory, String dashboardDirectory, String auditRuleSetFile, String metricSetFile)
	{
		this.dataStoreDirectory = dataStoreDirectory;
		this.dashboardDirectory = dashboardDirectory;
		Date now = new Date();
		buildDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss ZZZ").format(now);
		buildNumber = new SimpleDateFormat("yyyyMMddHHmm").format(now);
		this.auditRuleSetFile = auditRuleSetFile;
		this.metricSetFile = metricSetFile;
		this.targetPlatformDirectory = targetPlatformDirectory;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Accessing
	//
	////////////////////////////////////////////////////////////////////////////

	/**
	 * Return the installation directory of the Eclipse to be used as a target
	 * platform, or <code>null</code> if there is no target platform.
	 *
	 * @return the installation directory of the Eclipse to be used as a target
	 *         platform
	 */
	public String getTargetPlatformDirectory()
	{
		return targetPlatformDirectory;
	}

	/**
	 * Return the absolute path of the data store directory.
	 *
	 * @return the absolute path of the data store directory
	 */
	public String getDataStoreDirectory()
	{
		return dataStoreDirectory;
	}

	/**
	 * Return the absolute path of the directory to which the dashboard should
	 * be written.
	 *
	 * @return the absolute path of the directory to which the dashboard should
	 *         be written
	 */
	public String getDashboardDirectory()
	{
		return dashboardDirectory;
	}

	/**
	 * Return the date of the build.
	 *
	 * @return the date of the build
	 */
	public String getBuildDate()
	{
		return buildDate;
	}

	/**
	 * Return the build number for the build.
	 *
	 * @return the build number for the build
	 */
	public String getBuildNumber()
	{
		return buildNumber;
	}

	/**
	 * Return the absolute path of the file containing the audit rule set to use, or
	 * <code>null</code> if the default audit rule set is to be used.
	 *
	 * @return the absolute path of the file containing the audit rule set to use
	 */
	public String getAuditRuleSetFile()
	{
		return auditRuleSetFile;
	}

	/**
	 * Return the absolute path of the file containing the metrics set to use, or
	 * <code>null</code> if the default metrics set is to be used.
	 *
	 * @return the absolute path of the file containing the metrics set to use
	 */
	public String getMetricSetFile()
	{
		return metricSetFile;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Externalizing
	//
	////////////////////////////////////////////////////////////////////////////

	public void writeDataStoreDefinition(PrintWriter writer)
	{
		writer.println();
		writer.println("	<!-- Define the data store -->");
		writer.println();
		writer.println("	<dataStore");
		writer.println("			id=\"dataStore\"");
		writer.print("			directory=\"");
		writer.print(dataStoreDirectory);
		writer.println("\"");
		writer.print("			date=\"");
		writer.print(buildDate);
		writer.println("\"");
		writer.print("			buildId=\"");
		writer.print(buildNumber);
		writer.println("\"/>");
	}
}