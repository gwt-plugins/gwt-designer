/*******************************************************************************
 * Copyright 2011 Google Inc. All Rights Reserved.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.google.gwt.dev.shell.designtime;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.AbstractReferenceMap;
import org.apache.commons.collections.map.ReferenceIdentityMap;
import org.apache.commons.collections.map.ReferenceMap;

/**
 * Cache for JavaScriptObjects per ClassLoader.
 * 
 * @author mitin_aa
 */
public final class WrappersCache {
	private static final Map<ClassLoader, Map<Object, Object>> m_javaWrapperCache =
			new HashMap<ClassLoader, Map<Object, Object>>();
	private static final Map<ClassLoader, Map<Long, Object>> m_jsoCache =
			new HashMap<ClassLoader, Map<Long, Object>>();
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////	
	private WrappersCache() {
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Weakly caches a given JSO by unique id. A cached JSO can be looked up by unique id until it is garbage
	 * collected.
	 * 
	 * Instantiations: changed to long.
	 * 
	 * @param uniqueId
	 *            a unique id associated with the JSO
	 * @param jso
	 *            the value to cache
	 */
	@SuppressWarnings("unchecked")
	public static void putCachedJso(ClassLoader cl, long uniqueId, Object jso) {
		Map<Long, Object> cache = m_jsoCache.get(cl);
		if (cache == null) {
			cache = new ReferenceMap(AbstractReferenceMap.HARD, AbstractReferenceMap.WEAK);
			m_jsoCache.put(cl, cache);
		}
		cache.put(uniqueId, jso);
	}
	/**
	 * Retrieves the mapped JSO for a given unique id, provided the id was previously cached and the JSO has
	 * not been garbage collected.
	 * 
	 * @param uniqueId
	 *            the previously stored unique id
	 * @return the mapped JSO, or <code>null</code> if the id was not previously mapped or if the JSO has been
	 *         garbage collected
	 */
	public static Object getCachedJso(ClassLoader cl, long uniqueId) {
		Map<Long, Object> cache = m_jsoCache.get(cl);
		if (cache != null) {
			return cache.get(uniqueId);
		}
		return null;
	}
	/**
	 * Retrieves the mapped wrapper for a given Java Object, provided the wrapper was previously cached and
	 * has not been garbage collected.
	 * 
	 * @param javaObject
	 *            the Object being wrapped
	 * @return the mapped wrapper, or <code>null</code> if the Java object mapped or if the wrapper has been
	 *         garbage collected
	 */
	public static Object getWrapperForObject(ClassLoader cl, Object javaObject) {
		Map<Object, Object> cache = m_javaWrapperCache.get(cl);
		if (cache != null) {
			return cache.get(javaObject);
		}
		return null;
	}
	/**
	 * Weakly caches a wrapper for a given Java Object.
	 * 
	 * @param javaObject
	 *            the Object being wrapped
	 * @param wrapper
	 *            the mapped wrapper
	 */
	@SuppressWarnings("unchecked")
	public static void putWrapperForObject(ClassLoader cl, Object javaObject, Object wrapper) {
		Map<Object, Object> cache = m_javaWrapperCache.get(cl);
		if (cache == null) {
			cache = new ReferenceIdentityMap(AbstractReferenceMap.WEAK, AbstractReferenceMap.WEAK);
			m_javaWrapperCache.put(cl, cache);
		}
		cache.put(javaObject, wrapper);
	}
	/**
	 * Clears the cache for given ClassLoader key.
	 */
	public static void clear(ClassLoader cl) {
		m_jsoCache.remove(cl);
		m_javaWrapperCache.remove(cl);
	}
}
