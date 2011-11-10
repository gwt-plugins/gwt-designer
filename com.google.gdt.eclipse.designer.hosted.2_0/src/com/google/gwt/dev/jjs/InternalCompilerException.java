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
package com.google.gwt.dev.jjs;

import java.util.ArrayList;
import java.util.List;

/**
 * Indicates the compiler encountered an unexpected and unsupported state of
 * operation.
 */
public class InternalCompilerException extends RuntimeException {

  /**
   * Information regarding a node that was being processed when an
   * InternalCompilerException was thrown.
   */
  public static final class NodeInfo {

    static void preload() {
      // Initialize this class on static invocation.
    }

    private final String className;
    private final String description;
    private final SourceInfo sourceInfo;

    NodeInfo(String className, String description, SourceInfo sourceInfo) {
      this.className = className;
      this.description = description;
      this.sourceInfo = sourceInfo;
    }

    /**
     * Returns the name of the Java class of the node.
     */
    public String getClassName() {
      return className;
    }

    /**
     * Returns a text description of the node; typically toString().
     */
    public String getDescription() {
      return description;
    }

    /**
     * Returns the node's source info, if available; otherwise <code>null</code>.
     */
    public SourceInfo getSourceInfo() {
      return sourceInfo;
    }
  }

  /**
   * Tracks if there's a pending addNode() to avoid recursion sickness.
   */
  private static final ThreadLocal<InternalCompilerException> pendingICE = new ThreadLocal<InternalCompilerException>();

  /**
   * Force this class to be preloaded. If we don't preload this class, we can
   * get into bad behavior if we later try to load this class under out of
   * memory or out of stack conditions.
   */
  public static void preload() {
    // Initialize this class on static invocation.
    NodeInfo.preload();
    pendingICE.set(pendingICE.get());
  }

  private final List<NodeInfo> nodeTrace = new ArrayList<NodeInfo>();

  /**
   * Constructs a new exception with the specified node, message, and cause.
   */
  public InternalCompilerException(HasSourceInfo node, String message,
      Throwable cause) {
    this(message, cause);
    addNode(node);
  }

  /**
   * Constructs a new exception with the specified message.
   */
  public InternalCompilerException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified message and cause.
   */
  public InternalCompilerException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Adds a node to the end of the node trace. This is similar to how a stack
   * trace works.
   */
  public void addNode(HasSourceInfo node) {
    InternalCompilerException other = pendingICE.get();
    if (other != null) {
      // Avoiding recursion sickness: Yet Another ICE must have occurred while
      // generating info for a prior ICE. Just bail!
      return;
    }

    String className = null;
    String description = null;
    SourceInfo sourceInfo = null;
    try {
      pendingICE.set(this);
      className = node.getClass().getName();
      sourceInfo = node.getSourceInfo();
      description = node.toString();
    } catch (Throwable e) {
      // ignore any exceptions
      if (description == null) {
        description = "<source info not available>";
      }
    } finally {
      pendingICE.set(null);
    }
    addNode(className, description, sourceInfo);
  }

  /**
   * Adds information about a a node to the end of the node trace. This is
   * similar to how a stack trace works.
   */
  public void addNode(String className, String description,
      SourceInfo sourceInfo) {
    nodeTrace.add(new NodeInfo(className, description, sourceInfo));
  }

  /**
   * Returns a list of nodes that were being processed when this exception was
   * thrown. The list reflects the parent-child relationships of the AST and is
   * is in order from children to parents. The first element of the returned
   * list is the node that was most specifically being visited when the
   * exception was thrown.
   */
  public List<NodeInfo> getNodeTrace() {
    return nodeTrace;
  }

}
