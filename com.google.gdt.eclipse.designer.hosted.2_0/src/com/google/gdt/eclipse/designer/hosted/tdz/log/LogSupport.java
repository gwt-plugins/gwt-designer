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
package com.google.gdt.eclipse.designer.hosted.tdz.log;

import java.io.File;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.IJavaProject;

import com.google.gdt.eclipse.designer.hosted.ILogSupport;
import com.google.gdt.eclipse.designer.hosted.log.RotatingFileWriter;
import com.google.gdt.eclipse.designer.hosted.tdz.HostedModeSupport;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.util.log.AbstractTreeLogger;
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;

/**
 * Implementation of {@link ILogSupport} for GWT.
 * 
 * @author mitin_aa
 */
public final class LogSupport implements ILogSupport {
	private static final String ERROR_TYPE_LABEL = "[" + TreeLogger.ERROR.getLabel() + "]";
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final TreeLogger.Type[] TREE_LOGGER_TYPES = {
			TreeLogger.ERROR,
			TreeLogger.WARN,
			TreeLogger.INFO,
			TreeLogger.TRACE,
			TreeLogger.DEBUG,
			TreeLogger.SPAM,
			TreeLogger.ALL};
	private final PrintWriter m_writer;
	private final TreeLogger m_logger;
	private String m_errors = "";
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LogSupport(TreeLogger.Type type, IJavaProject javaProject) throws Exception {
		if ("true".equalsIgnoreCase(System.getProperty(WBP_TESTING_TIME))) {
			m_logger = TreeLogger.NULL;
			m_writer = null;
		} else {
			// prepare directory to write log files to
			String logDir = HostedModeSupport.getTemporaryDirectoryName(javaProject);
			// prepare logger
			Writer rotatingWriter =
					RotatingFileWriter.getInstance(logDir + File.separator + ".gwt-log", 10, 3);
			m_writer = new PrintWriter(new FilterWriter(rotatingWriter) {
				private String m_buffer = new String();
				@Override
				public void write(String message, int off, int len) throws IOException {
					super.write(message, off, len);
					int eolIndex = message.indexOf(LINE_SEPARATOR);
					if (eolIndex != -1) {
						m_buffer += message.substring(0, eolIndex);
						if (m_buffer.indexOf(ERROR_TYPE_LABEL) != -1) {
							m_errors += m_buffer + LINE_SEPARATOR;
						}
						m_buffer = message.substring(eolIndex + 1);
					} else {
						m_buffer += message;
					}
				}
			});
			m_logger = new PrintWriterTreeLogger(m_writer);
			((AbstractTreeLogger) m_logger).setMaxDetail(type);
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// ILogSupport
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link TreeLogger} instance.
	 */
	public Object getLogger() {
		return m_logger;
	}
	public void setLogLevel(int logLevel) {
		if (m_writer != null) {
			if (logLevel >= 0 && logLevel < TREE_LOGGER_TYPES.length + 1) {
				((AbstractTreeLogger) m_logger).setMaxDetail(TREE_LOGGER_TYPES[logLevel]);
			}
		}
	}
	public String getErrorMessages() {
		if (StringUtils.isEmpty(m_errors)) {
			return "<none>";
		}
		return m_errors;
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Dispose
	//
	////////////////////////////////////////////////////////////////////////////
	public void dispose() {
		if (m_writer != null) {
			m_writer.close();
		}
	}
}
