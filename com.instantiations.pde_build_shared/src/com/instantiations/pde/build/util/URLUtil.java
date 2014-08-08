package com.instantiations.pde.build.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLUtil
{
	/**
	 * Answer a collection of child names for the specified URL
	 * @param url the URL (not <code>null</code>)
	 * @return a collection of child names (not <code>null</code>)
	 */
	public static Collection<String> getChildNames(URL url) {
		return getChildNames(url, Pattern.compile("href=\"[\\.\\w]+[/]?\""));
	}

	/**
	 * Answer a collection of child file names for the specified URL
	 * @param url the URL (not <code>null</code>)
	 * @return a collection of child file names (not <code>null</code>)
	 */
	public static Collection<String> getChildFileNames(URL url) {
		return getChildNames(url, Pattern.compile("href=\"[\\-\\.\\w]+\""));
	}

	private static Collection<String> getChildNames(URL url, Pattern pattern) {
		Collection<String> result = new HashSet<String>();
		InputStream in;
		try {
			in = url.openStream();
		}
		catch (IOException e) {
			e.printStackTrace();
			return result;
		}
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				Matcher matcher = pattern.matcher(line);
				if (!matcher.find())
					continue;
				String name = line.substring(matcher.start() + 6, matcher.end() - 1);
				result.add(name);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				in.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
	
	//=====================================================================
	// TEST

//	public static void main(String[] args) throws Exception {
//		printChildURLs(new URL("http://download.instantiations.com/RCPDeveloper/"));
//		printChildURLs(new URL("http://download.instantiations.com/RCPDeveloper/continuous/"));
//		printChildURLs(new URL("http://download.instantiations.com/RCPDeveloper/continuous/latest/"));
//	}
//
//	private static void printChildURLs(URL url) {
//		System.out.println("======================================================");
//		System.out.println(url);
//		Collection<String> result = URLUtil.getChildNames(url);
//		for (String childName : result)
//			System.out.println("   " + childName);
//	}
}
