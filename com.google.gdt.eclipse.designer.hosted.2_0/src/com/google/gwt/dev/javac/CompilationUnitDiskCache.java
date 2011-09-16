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
package com.google.gwt.dev.javac;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jdt.core.compiler.CategorizedProblem;

import com.google.gdt.eclipse.designer.hosted.tdz.Activator;
import com.google.gdt.eclipse.designer.hosted.tdz.GWTEnvironmentUtils;
import com.google.gwt.dev.js.ast.JsProgram;
import com.google.gwt.dev.resource.Resource;

/**
 * 
 * For caching compiled units.
 * 
 * XXX Instantiations
 * 
 * @author mitin_aa
 */
public final class CompilationUnitDiskCache {
	private static final String CACHE_SUFFIX = ".cache";
	private static final String TYPES_CACHE_DIR_NAME = "types-cache";
	private static File STATE_LOCATION =
			GWTEnvironmentUtils.DEVELOPERS_HOST && GWTEnvironmentUtils.isTestingTime()
					? GWTEnvironmentUtils.getCacheDirectory()
					: Activator.getDefault().getStateLocation().toFile();
	public static final long CACHE_VERSION = 2595239285662127395L;
	private final File m_cacheDir;
	private final Map<String, CompilationUnit> m_cache = new ReferenceMap();
	//
	private static ReferenceMap m_caches = new ReferenceMap();
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private CompilationUnitDiskCache(File cacheDir) {
		m_cacheDir = cacheDir;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public static CompilationUnitDiskCache get() {
		File cacheDir = new File(STATE_LOCATION, TYPES_CACHE_DIR_NAME);
		CompilationUnitDiskCache diskCache = (CompilationUnitDiskCache) m_caches.get(cacheDir);
		if (diskCache == null) {
			cacheDir.mkdirs();
			diskCache = new CompilationUnitDiskCache(cacheDir);
			m_caches.put(cacheDir, diskCache);
		}
		return diskCache;
	}
	public CompilationUnit get(Resource key, JsProgram jsProgram) {
		String fileName = getFileName(key);
		CompilationUnit unit = m_cache.get(fileName);
		if (unit != null) {
			// check for modification
			if (key.getLastModified() != unit.getLastModified()) {
				removeFromCache(new File(m_cacheDir, fileName));
				return null;
			}
			return unit;
		}
		File cacheFile = new File(m_cacheDir, fileName);
		if (!cacheFile.exists()) {
			return null;
		}
		// do load CU
		{
			FileInputStream inputStream = null;
			try {
				inputStream = new FileInputStream(cacheFile);
				// try to load
				unit = CachedCompilationUnit.load(inputStream, jsProgram);
				if (unit == null) {
					removeFromCache(cacheFile);
					return null;
				}
				// check for modification
				if (key.getLastModified() != unit.getLastModified()) {
					removeFromCache(cacheFile);
					return null;
				}
				// store in memory
				m_cache.put(fileName, unit);
				return unit;
			} catch (Throwable e) {
				removeFromCache(cacheFile);
			} finally {
				IOUtils.closeQuietly(inputStream);
			}
		}
		return null;
	}
	public void put(SourceFileCompilationUnit unit) {
		// don't mess with troubles ;)
		if (!ArrayUtils.isEmpty(unit.getProblems())) {
			for (CategorizedProblem problem : unit.getProblems()) {
				if (problem.isError()) {
					return;
				}
			}
		}
		// use the type name as key 
		String fileName = getFileName(unit.getSourceFile());
		File cacheFile = new File(m_cacheDir, fileName);
		FileOutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(cacheFile);
			CachedCompilationUnit.save(unit, outputStream);
		} catch (Throwable e) {
			removeFromCache(cacheFile);
		} finally {
			IOUtils.closeQuietly(outputStream);
		}
	}
	private String getFileName(Resource key) {
		return Shared.getTypeName(key) + CACHE_SUFFIX;
	}
	private void removeFromCache(File cacheFile) {
		FileUtils.deleteQuietly(cacheFile);
		m_cache.remove(cacheFile.getName());
	}
}
