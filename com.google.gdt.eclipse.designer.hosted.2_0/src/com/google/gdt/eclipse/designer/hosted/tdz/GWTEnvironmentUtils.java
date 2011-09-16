/*
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.gdt.eclipse.designer.hosted.tdz;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Helper for environment state access.
 * 
 * @author scheglov_ke
 * @coverage core
 */
public final class GWTEnvironmentUtils extends AbstractUIPlugin {
	////////////////////////////////////////////////////////////////////////////
	//
	// Host
	//
	////////////////////////////////////////////////////////////////////////////
	public static final String HOST_NAME = getHostName();
	public static final boolean DEVELOPERS_HOST;
	static {
		String host = HOST_NAME.toUpperCase(Locale.ENGLISH);
		DEVELOPERS_HOST =
				"SCHEGLOV-WIN".equals(host) || "SABLIN-AA".equals(host) || "FLANKER-WINDOWS".equals(host);
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// System utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static String getHostName() {
		String hostName = "";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
			String[] names = StringUtils.split(hostName, '.');
			for (String name : names) {
				if (StringUtils.isNumeric(name)) {
					// getHostName() returned in a IP-address form
					return hostName;
				}
			}
			hostName = names[0];
		} catch (UnknownHostException e) {
		}
		return hostName;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Development
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String WBP_TESTING_TIME = "wbp.testing.time";
	public static boolean isTestingTime() {
		return "true".equals(System.getProperty(WBP_TESTING_TIME));
	}
	public static void setTestingTime(boolean value) {
		System.setProperty(WBP_TESTING_TIME, value ? "true" : "false");
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Cache directory
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the path for GWT cache.
	 */
	public static File getCacheDirectory() {
		String path = "C:/Work/GWT/.dev-cache";
		// attempt to use separate cache location for different GWT test suites, which we run in parallel
		{
			String suffix = System.getProperty("wbp.testing.gwtCacheSuffix");
			if (suffix != null) {
				path += "/" + suffix;
			} else {
				path += "/Main";
			}
		}
		// done
		{
			File file = new File(path);
			file.mkdirs();
			return file;
		}
	}
}
