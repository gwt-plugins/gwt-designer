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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CategorizedProblem;

import com.google.gwt.dev.jjs.SourceInfo;
import com.google.gwt.dev.jjs.SourceOrigin;
import com.google.gwt.dev.js.ast.JsFunction;
import com.google.gwt.dev.js.ast.JsProgram;

/**
 * A compilation unit loaded from cache.
 * 
 * XXX Instantiations
 * 
 * @author mitin_aa
 */
public class CachedCompilationUnit extends CompilationUnit {
	private String m_displayLocation;
	private String m_typeName;
	private ContentId m_contentId;
	private long m_lastModified;
	private Collection<CompiledClass> m_compiledClasses;
	private HashSet<ContentId> m_dependencies;
	private boolean m_isSuperSource;
	private ArrayList<JsniMethod> m_jsniMethods;
	private MethodArgNamesLookup m_methodArgs;
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	private CachedCompilationUnit() {
		// cannot be instantiated directly
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Instantiate
	//
	////////////////////////////////////////////////////////////////////////////
	public static CachedCompilationUnit load(InputStream inputStream, JsProgram jsProgram) throws Exception {
		DataInputStream dis = new DataInputStream(new BufferedInputStream(inputStream));
		try {
			CachedCompilationUnit compilationUnit = new CachedCompilationUnit();
			// version
			long version = dis.readLong();
			if (version != CompilationUnitDiskCache.CACHE_VERSION) {
				return null;
			}
			// some simple stuff :)
			compilationUnit.m_lastModified = dis.readLong();
			compilationUnit.m_displayLocation = dis.readUTF();
			compilationUnit.m_typeName = dis.readUTF();
			compilationUnit.m_contentId = new ContentId(dis.readUTF());
			compilationUnit.m_isSuperSource = dis.readBoolean();
			// compiled classes
			{
				int size = dis.readInt();
				compilationUnit.m_compiledClasses = new ArrayList<CompiledClass>(size);
				for (int i = 0; i < size; ++i) {
					// internal name
					String internalName = dis.readUTF();
					// is local
					boolean isLocal = dis.readBoolean();
					// bytes
					int byteSize = dis.readInt();
					byte[] bytes = new byte[byteSize];
					dis.readFully(bytes);
					// enclosing class
					CompiledClass enclosingClass = null;
					String enclosingClassName = dis.readUTF();
					if (!StringUtils.isEmpty(enclosingClassName)) {
						for (CompiledClass cc : compilationUnit.m_compiledClasses) {
							if (enclosingClassName.equals(cc.getInternalName())) {
								enclosingClass = cc;
								break;
							}
						}
					}
					// some assertion
					if (!StringUtils.isEmpty(enclosingClassName) && enclosingClass == null) {
						throw new IllegalStateException("Can't find the enclosing class \""
							+ enclosingClassName
							+ "\" for \""
							+ internalName
							+ "\"");
					}
					// init unit
					CompiledClass cc = new CompiledClass(internalName, bytes, isLocal, enclosingClass);
					cc.initUnit(compilationUnit);
					compilationUnit.m_compiledClasses.add(cc);
				}
			}
			// dependencies
			{
				compilationUnit.m_dependencies = new HashSet<ContentId>();
				int size = dis.readInt();
				if (size > 0) {
					for (int i = 0; i < size; i++) {
						compilationUnit.m_dependencies.add(new ContentId(dis.readUTF()));
					}
				}
			}
			// JSNI methods
			{
				compilationUnit.m_jsniMethods = new ArrayList<JsniMethod>();
				int size = dis.readInt();
				if (size > 0) {
					for (int i = 0; i < size; i++) {
						String name = dis.readUTF();
						int startPos = dis.readInt();
						int endPos = dis.readInt();
						int startLine = dis.readInt();
						String source = dis.readUTF();
						String fileName = compilationUnit.m_displayLocation;
						SourceInfo jsInfo = SourceOrigin.create(startPos, endPos, startLine, fileName);
						compilationUnit.m_jsniMethods.add(JsniCollector.restoreJsniMethod(name,
							source,
							jsInfo,
							jsProgram));
					}
				}
			}
			// Method lookup
			{
				compilationUnit.m_methodArgs = MethodArgNamesLookup.load(dis);
			}
			return compilationUnit;
		} finally {
			IOUtils.closeQuietly(dis);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Save
	//
	////////////////////////////////////////////////////////////////////////////
	public static void save(SourceFileCompilationUnit unit, OutputStream outputStream) throws Exception {
		DataOutputStream dos = null;
		try {
			dos = new DataOutputStream(new BufferedOutputStream(outputStream));
			// version
			dos.writeLong(CompilationUnitDiskCache.CACHE_VERSION);
			// simple stuff
			dos.writeLong(unit.getLastModified());
			dos.writeUTF(unit.getDisplayLocation());
			dos.writeUTF(unit.getTypeName());
			dos.writeUTF(unit.getContentId().get());
			dos.writeBoolean(unit.isSuperSource());
			// compiled classes
			{
				Collection<CompiledClass> compiledClasses = unit.getCompiledClasses();
				int size = compiledClasses.size();
				dos.writeInt(size);
				if (size > 0) {
					// sort in enclosing order to be able to restore enclosing classes by name
					CompiledClass[] compiledClassesArray =
							compiledClasses.toArray(new CompiledClass[compiledClasses.size()]);
					Arrays.sort(compiledClassesArray, new Comparator<CompiledClass>() {
						public int compare(CompiledClass o1, CompiledClass o2) {
							int o1count = countMatches(o1.getInternalName(), Signature.C_DOLLAR);
							int o2count = countMatches(o2.getInternalName(), Signature.C_DOLLAR);
							return o1count - o2count;
						}
					});
					// store
					for (CompiledClass compiledClass : compiledClassesArray) {
						// internal name
						dos.writeUTF(compiledClass.getInternalName());
						// is local
						dos.writeBoolean(compiledClass.isLocal());
						// bytes
						byte[] bytes = compiledClass.getBytes();
						dos.writeInt(bytes.length);
						dos.write(bytes);
						// enclosing class, write the name only
						CompiledClass enclosingClass = compiledClass.getEnclosingClass();
						String enclosingClassName =
								enclosingClass != null ? enclosingClass.getInternalName() : "";
						dos.writeUTF(enclosingClassName);
					}
				}
			}
			// dependencies
			{
				Set<ContentId> dependencies = unit.getDependencies();
				int size = dependencies.size();
				dos.writeInt(size);
				if (size > 0) {
					for (ContentId contentId : dependencies) {
						dos.writeUTF(contentId.get());
					}
				}
			}
			// JSNI methods
			{
				List<JsniMethod> jsniMethods = unit.getJsniMethods();
				int size = jsniMethods.size();
				dos.writeInt(size);
				if (size > 0) {
					for (JsniMethod jsniMethod : jsniMethods) {
						dos.writeUTF(jsniMethod.name());
						JsFunction function = jsniMethod.function();
						SourceInfo sourceInfo = function.getSourceInfo();
						dos.writeInt(sourceInfo.getStartPos());
						dos.writeInt(sourceInfo.getEndPos());
						dos.writeInt(sourceInfo.getStartLine());
						dos.writeUTF(function.toSource());
					}
				}
			}
			// Method lookup
			{
				MethodArgNamesLookup methodArgs = unit.getMethodArgs();
				MethodArgNamesLookup.save(methodArgs, dos);
			}
		} finally {
			IOUtils.closeQuietly(dos);
		}
	}
	/**
	 * a bit faster then in StringUtils because searches for a char.
	 */
	private static int countMatches(String str, char sub) {
		if (StringUtils.isEmpty(str)) {
			return 0;
		}
		int count = 0;
		int idx = 0;
		while ((idx = str.indexOf(sub, idx)) != -1) {
			count++;
			idx += 1;
		}
		return count;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// CompilationUnit
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	Collection<CompiledClass> getCompiledClasses() {
		return m_compiledClasses;
	}
	@Override
	public long getLastModified() {
		return m_lastModified;
	}
	@Override
	ContentId getContentId() {
		return m_contentId;
	}
	@Override
	public String getTypeName() {
		return m_typeName;
	}
	@Override
	Set<ContentId> getDependencies() {
		return m_dependencies;
	}
	@Override
	public String getDisplayLocation() {
		return m_displayLocation;
	}
	@Override
	public List<JsniMethod> getJsniMethods() {
		return m_jsniMethods;
	}
	@Override
	CategorizedProblem[] getProblems() {
		return null;
	}
	@Override
	public String getSource() {
		return null;
	}
	@Override
	public boolean isGenerated() {
		return false;
	}
	@Override
	public boolean isSuperSource() {
		return m_isSuperSource;
	}
	@Override
	public MethodArgNamesLookup getMethodArgs() {
		return m_methodArgs;
	}
}
