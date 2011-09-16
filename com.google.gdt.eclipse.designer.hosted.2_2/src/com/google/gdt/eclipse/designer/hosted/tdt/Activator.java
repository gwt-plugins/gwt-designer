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
package com.google.gdt.eclipse.designer.hosted.tdt;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * @coverage gwtHosted
 */
public class Activator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "com.instantiations.designer.gwt.hosted.2_2";
	// The shared instance
	private static Activator m_plugin;
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		m_plugin = this;
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		m_plugin = null;
		super.stop(context);
	}
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return m_plugin;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Images
	//
	////////////////////////////////////////////////////////////////////////////
	private static final Map<String, Image> m_nameToIconMap = new HashMap<String, Image>();
	/**
	 * Open file from plugin directory.
	 */
	public static InputStream getFile(String name) {
		try {
			return m_plugin.getBundle().getEntry(name).openStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * Return array of entry sub-paths in given path.
	 */
	public static String[] getEntriesPaths(String path) {
		List<String> entryPaths;
		{
			Enumeration<?> entryPathsEnumeration = m_plugin.getBundle().getEntryPaths(path);
			entryPaths = new ArrayList<String>();
			CollectionUtils.addAll(entryPaths, entryPathsEnumeration);
		}
		// remove "CVS" files (far case when we use runtime workbench)
		for (Iterator<String> I = entryPaths.iterator(); I.hasNext();) {
			String entryPath = I.next();
			if (entryPath.indexOf("CVS") != -1) {
				I.remove();
			}
		}
		// convert to array
		return entryPaths.toArray(new String[entryPaths.size()]);
	}
	/**
	 * Get image from "icons" directory.
	 */
	public static Image getImage(String name) {
		Image image = m_nameToIconMap.get(name);
		if (image == null) {
			// prepare path
			String path;
			if (name.startsWith("/")) {
				path = name;
			} else {
				path = "icons/" + name;
			}
			//
			InputStream is = getFile(path);
			try {
				image = new Image(Display.getCurrent(), is);
				m_nameToIconMap.put(name, image);
			} finally {
				IOUtils.closeQuietly(is);
			}
		}
		return image;
	}
	/**
	 * Get image descriptor from "icons" directory.
	 */
	public static ImageDescriptor getImageDescriptor(String name) {
		URL url = m_plugin.getBundle().getEntry("icons/" + name);
		return ImageDescriptor.createFromURL(url);
	}
}
