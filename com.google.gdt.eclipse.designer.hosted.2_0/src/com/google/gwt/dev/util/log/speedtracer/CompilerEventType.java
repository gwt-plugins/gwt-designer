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
package com.google.gwt.dev.util.log.speedtracer;

import com.google.gwt.dev.util.log.speedtracer.SpeedTracerLogger.EventType;

/**
 * Represents a type of event whose performance is tracked while running the
 * {@link com.google.gwt.dev.Compiler}
 */
public enum CompilerEventType implements EventType {
  CODE_SPLITTER("CodeSplitter", "Yellow"), //
  COMPILE("Compiler", "DarkBlue"), //
  COMPILE_PERMUTATIONS("CompilePermutations", "BurlyWood"), //
  JJS_COMPILE_PERMUTATION("JjsCompilePermutation", "Moccasin"), //
  DRAFT_OPTIMIZE("DraftOptimizer", "Blue"), //
  GENERATOR("Generator", "Red"), //
  JDT_COMPILER("JdtCompiler", "Tomato"), //
  LINK("Link", "LawnGreen"), //
  MAKE_SOYC_ARTIFACTS("MakeSoycArtifacts", "Chartreuse"), //
  MODULE_DEF("ModuleDef", "BlueViolet"), //
  OPTIMIZE("Optimize", "LightSlateGray"), //
  PRECOMPILE("Precompile", "CornflowerBlue"), //
  RESOURCE_ORACLE("ResourceOracle", "GoldenRod"), //
  TYPE_ORACLE_MEDIATOR("TypeOracleMediator", "LightSteelBlue"), //
  PRECOMPILE_CORE("CoreCompiler", "Crimson"), //
  WRITE_OBJECT_AS_FILE("WriteObjectAsFile", "Magenta"), //
  BUILD_AST("BuildAST", "DarkGoldenRod"), //
  FUNCTION_CLUSTER("JsFunctionClusterer","Cornflower"), //
  BLOCKED("Blocked", "FloralWhite"), //
  PERMUTATION_WORKER("PermuationWorker", "SpringGreen"), //
  GET_ALL_REBINDS("GetAllPossibleRebindAnswers", "Brown"), //
  FIND_ENTRY_POINTS("FindEntryPoints", "SlateGray"), //
  VALIDATE("Validate", "YellowGreen"), //
  NORMALIZER("Normalizer", "Peru"), //
  RECORD_REBINDS("RecordRebinds", "Chocolate"), //
  BUILD_TYPE_MAP("BuildTypeMap", "LightSeaGreen"), //
  GET_COMPILATION_UNITS("GetCompilationUnitDeclarations", "Gainsboro"), //
  GENERATE_JAVA_AST("GenerateJavaAST", "DodgerBlue"),
  ARTIFICIAL_RESCUE_REORDER("ArtificialRescueReorder", "LightCyan"), //
  FIX_ASSIGNMENT_TO_UNBOX("FixAssignmentToUnbox", "Orange"), //
  ASSERTION_NORMALIZER("AssertionNormalizer","LightPink"), //
  ASSERTION_REMOVER("AssertionRemover","LightSalmon"), //
  REPLACE_REBINDS("ReplaceRebinds", "Snow"), //
  CREATE_UNIFIED_AST("CreateUnifiedAST", "BlueViolet"), //
  CHECK_FOR_ERRORS("CheckForErrors", "DimGrey"), //
  GRAPHICS_INIT("Graphics2D.createGraphics()", "Blue"); //

  final String cssColor;
  final String name;

  CompilerEventType(String name, String cssColor) {
    this.name = name;
    this.cssColor = cssColor;
  }

  public String getColor() {
    return cssColor;
  }

  public String getName() {
    return name;
  }
}
