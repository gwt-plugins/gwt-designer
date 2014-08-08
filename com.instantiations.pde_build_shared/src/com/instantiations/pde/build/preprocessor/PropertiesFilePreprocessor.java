package com.instantiations.pde.build.preprocessor;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.OemVersion;
import com.instantiations.pde.build.util.Version;
import com.objfac.prebop.Expression;
import com.objfac.prebop.PreprocessorError;

/**
 * A preprocessor for *.properties files such as feature.properties and plugin.properties.
 */
public class PropertiesFilePreprocessor extends VariableReplacementPreprocessor
{
	private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\s*#\\s*\\$\\s*(if|elseif|else|endif)\\s+");

	private final HashMap<String, String> vars;

	protected PropertiesFilePreprocessor(OemVersion targetVersion, BuildProperties prop) {
		super(targetVersion.getVersion(), prop);
		this.vars = new HashMap<String, String>();
		this.vars.put("eclipse.version", targetVersion.getVersion().toString());
		this.vars.put("oem.name", targetVersion.getOemName() != null ? targetVersion.getOemName() : "none");
		lineProcessors.add(new ExpressionProcessor());
	}
	
	protected void endProcess(PrintWriter writer) {

		// Append the "build" property
		writer.println();
		writer.println("build = " + prop.getBuildNum());
		
		super.endProcess(writer);
	}

	private enum ExpressionState {
		NORMAL, // processing outside any expression
		INCLUDE, // in expression that evaluates true
		INCLUDE_ELSE, // in ELSE expression that should be uncommented
		EXCLUDE, // in expression but have not found a true expression yet 
		EXCLUDE_ELSE, // in ELSE expression that should be commented
		EXCLUDE_REMAINING, // in expression but after block that evaluated true
	};
	private enum ExpressionType {
		IF, ELSEIF, ELSE, ENDIF
	}

	/**
	 * Processor for $if, $elseif, $else, $endif expressions
	 */
	private class ExpressionProcessor
		implements LineProcessor
	{
		ExpressionState state = ExpressionState.NORMAL;

		public void reset() {
			state = ExpressionState.NORMAL;
		}

		/**
		 * Process the specified line
		 */
		public String process(String line) {

			// If this is not a comment containing $if, $elseif, $else, $endif
			// then comment or uncomment the line as appropriate

			if (!EXPRESSION_PATTERN.matcher(line).find()) {
				switch (state) {
					case NORMAL :
						return line;
					case INCLUDE :
					case INCLUDE_ELSE :
						return uncommentLine(line);
					case EXCLUDE :
					case EXCLUDE_ELSE :
					case EXCLUDE_REMAINING :
						return commentLine(line);
					default :
						throw new IllegalStateException("Unexpected state " + state);
				}
			}

			// Determine the type of control statement: $if, $elseif, $else, $endif
			// and adjust the state accordingly
			
			ExpressionType expType = getExpType(line);
			switch (state) {

				case NORMAL :
					switch (expType) {
						case IF :
							state = evaluate(line) ? ExpressionState.INCLUDE : ExpressionState.EXCLUDE;
							return line;
						case ELSEIF :
						case ELSE :
						case ENDIF :
							throw new IllegalStateException("Expected \"if\", but found:\n" + line);
						default :
							throw new IllegalStateException("Unexpected Expression Type: " + expType);
					}

				case INCLUDE :
					switch (expType) {
						case IF :
							throw new IllegalStateException("Expected \"elseif\", \"else\", or \"endif\", but found:\n"
								+ line);
						case ELSEIF :
							state = ExpressionState.EXCLUDE_REMAINING;
							return line;
						case ELSE :
							state = ExpressionState.EXCLUDE_ELSE;
							return line;
						case ENDIF :
							state = ExpressionState.NORMAL;
							return line;
						default :
							throw new IllegalStateException("Unexpected Expression Type: " + expType);
					}

				case EXCLUDE :
					switch (expType) {
						case IF :
							throw new IllegalStateException("Expected \"elseif\", \"else\", or \"endif\", but found:\n"
								+ line);
						case ELSEIF :
							state = (evaluate(line) ? ExpressionState.INCLUDE : ExpressionState.EXCLUDE);
							return line;
						case ELSE :
							state = ExpressionState.INCLUDE_ELSE;
							return line;
						case ENDIF :
							state = ExpressionState.NORMAL;
							return line;
						default :
							throw new IllegalStateException("Unexpected Expression Type: " + expType);
					}

				case INCLUDE_ELSE :
				case EXCLUDE_ELSE :
					switch (expType) {
						case IF :
						case ELSEIF :
						case ELSE :
							throw new IllegalStateException("Expected \"endif\", but found:\n" + line);
						case ENDIF :
							state = ExpressionState.NORMAL;
							return line;
						default :
							throw new IllegalStateException("Unexpected Expression Type: " + expType);
					}

				case EXCLUDE_REMAINING :
					switch (expType) {
						case IF :
							throw new IllegalStateException("Expected \"elseif\", \"else\", or \"endif\", but found:\n"
								+ line);
						case ELSEIF :
							state = ExpressionState.EXCLUDE_REMAINING;
							return line;
						case ELSE :
							state = ExpressionState.EXCLUDE_ELSE;
							return line;
						case ENDIF :
							state = ExpressionState.NORMAL;
							return line;
						default :
							throw new IllegalStateException("Unexpected Expression Type: " + expType);
					}

				default :
					throw new IllegalStateException("Unexpected state " + state);
			}
		}

		/**
		 * Evaluate the expression
		 * 
		 * @param line the line containing the expression
		 * @return <code>true</code> if the expression evaluates <code>true</code>, else
		 *         <code>false</code>
		 */
		private boolean evaluate(String line) {
			Expression exp;
			try {
				exp = new Expression(line.getBytes(), line.indexOf("if") + 2, vars);
			}
			catch (PreprocessorError e) {
				throw new RuntimeException("Failed to contruct expression: " + line, e);
			}
			try {
				return exp.eval();
			}
			catch (PreprocessorError e) {
				throw new RuntimeException("Failed to process expression: " + line, e);
			}
		}

		/**
		 * Answer an uncommented line
		 * 
		 * @param line the original line
		 * @return the original line uncommented
		 */
		private String uncommentLine(String line) {
			if (line == null)
				return null;
			for (int i = 0; i < line.length(); i++) {
				char ch = line.charAt(i);
				if (ch == '#')
					return line.substring(i + 1);
				if (!Character.isWhitespace(ch))
					break;
			}
			return line;
		}

		/**
		 * Answer a commented line
		 * 
		 * @param line the original line
		 * @return the original line commented
		 */
		private String commentLine(String line) {
			if (line == null)
				return null;
			for (int i = 0; i < line.length(); i++) {
				char ch = line.charAt(i);
				if (ch == '#')
					return line;
				if (!Character.isWhitespace(ch))
					break;
			}
			return "# " + line;
		}

		/**
		 * Determine if this is an $if, $elseif, or $endif
		 * 
		 * @param line the line containing the expression
		 * @return IF, ELSEIF, or ENDIF
		 */
		private ExpressionType getExpType(String line) {
			int i = 0;
			while (!Character.isLetter(line.charAt(i)))
				i++;
			if (line.charAt(i) == 'i')
				return ExpressionType.IF;
			if (line.charAt(i) == 'e') {
				if (line.charAt(i + 1) == 'n')
					return ExpressionType.ENDIF;
				if (i + 4 < line.length() && line.charAt(i + 4) == 'i')
					return ExpressionType.ELSEIF;
				return ExpressionType.ELSE;
			}
			throw new IllegalStateException("Expected if, elseif, else, or endif in " + line);
		}
	}
}
