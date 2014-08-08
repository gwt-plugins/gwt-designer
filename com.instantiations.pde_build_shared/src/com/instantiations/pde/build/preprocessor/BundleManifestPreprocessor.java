package com.instantiations.pde.build.preprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import com.instantiations.pde.build.util.Version;

/**
 * A preprocessor for META-INF/MANIFEST.MF files
 */
public abstract class BundleManifestPreprocessor extends LineBasedPreprocessor
{
	// Execution Environment detection
	private static final Pattern EXECUTION_ENVIRONMENT_PATTERN = Pattern
		.compile("Bundle\\-RequiredExecutionEnvironment:\\s*\\w");

	// The detected execution environment (e.g. "J2SE-1.5") or null if not defined
	private String executionEnvironment = null;

	// Flag indicating whether Fragment-Host is detected
	private boolean isFragment = false;

	/**
	 * Construct a new instance.
	 * 
	 * @param eclipseTargetVersion the version of Eclipse against which the source will be
	 *            compiled (e.g. "3.3")
	 */
	public BundleManifestPreprocessor(Version eclipseTargetVersion) {
		super(eclipseTargetVersion);
	}

	/**
	 * Answer the detected execution environment (e.g. "J2SE-1.5") or null if not defined
	 */
	public String getExecutionEnvironment() {
		return executionEnvironment;
	}

	public class DetectExecutionEnvironment
		implements LineProcessor
	{
		public DetectExecutionEnvironment() {
		}

		public void reset() {
		}

		public String process(String line) {
			if (EXECUTION_ENVIRONMENT_PATTERN.matcher(line).find()) {
				int start = line.indexOf(':');
				int end = line.indexOf(',', start);
				if (end == -1)
					end = line.length();
				executionEnvironment = line.substring(start + 1, end).trim();
			}
			return line;
		}
	}

	/**
	 * Answer true if the manifest specifies a fragment
	 */
	public boolean isFragment() {
		return isFragment;
	}

	public class DetectFragmentHost
		implements LineProcessor
	{
		public DetectFragmentHost() {
		}

		public void reset() {
		}

		public String process(String line) {
			if (line.startsWith("Fragment-Host:"))
				isFragment = true;
			return line;
		}
	}

	/**
	 * Modify the Reqire-Bundle entry as follows:
	 * <ul>
	 * <li>Eclipse 3.0 - replace org.eclipse.ui.* with org.eclipse.ui</li>
	 * <li>Eclipse 2.1 - add org.apache.xerces</li>
	 * </ul>
	 * Current implementation of this class ASSUMES one identifer per line
	 */
	public class RequireBundleModifier
		implements LineProcessor
	{
		private static final String REQUIRE_BUNDLE = "Require-Bundle:";

		/**
		 * A collection of rules for processing required bundles
		 */
		private final Collection<IBundleRule> rules = new ArrayList<IBundleRule>();

		/**
		 * The cached required bundles declaration
		 */
		private String declaration;

		/**
		 * The current processing state 0 - initial state 1 - in Require-Bundle
		 * declaration 2 - finished processing Require-Bundle declaration
		 */
		int state;

		/**
		 * Add the specified rule to the receiver's list of rules
		 * 
		 * @param rule the rule to be added (not <code>null</code>)
		 */
		public void addRule(IBundleRule rule) {
			rules.add(rule);
		}

		public void reset() {
			declaration = null;
			state = 0;
			for (IBundleRule rule : rules)
				rule.reset();
		}

		public String process(String line) {

			// Find the first line in the declaration
			if (declaration == null) {
				if (!line.startsWith(REQUIRE_BUNDLE))
					return line;
				declaration = "";
			}

			// Concatenate and cache each line in the declaration
			declaration += line.trim();
			if (declaration.endsWith(","))
				return null;

			// Process the entire declaration
			StringBuffer buf = null;
			for (String idAndOptions : declaration.substring(REQUIRE_BUNDLE.length()).split(",")) {
				idAndOptions = idAndOptions.trim();
				for (IBundleRule rule : rules) {
					idAndOptions = rule.processId(idAndOptions);
					if (idAndOptions == null)
						break;
				}
				if (idAndOptions != null) {
					if (buf == null) {
						buf = new StringBuffer(300);
						buf.append(REQUIRE_BUNDLE);
						buf.append(" ");
					}
					else {
						buf.append(",\r\n ");
					}
					buf.append(idAndOptions);
				}
			}
			declaration = null;

			// Append additional bundle identifiers
			for (IBundleRule rule : rules) {
				for (String id : rule.additionalIds()) {
					if (buf == null) {
						buf = new StringBuffer(300);
						buf.append(REQUIRE_BUNDLE);
						buf.append(" ");
					}
					else {
						buf.append(",\r\n ");
					}
					buf.append(id);
				}
			}

			return buf.toString();
		}
	}

	protected static final String[] NO_IDS = new String[]{};

	/**
	 * Abstract class for defining "Required-Bundles" rules
	 */
	private interface IBundleRule
	{
		public void reset();

		/**
		 * Process the specified required bundle identifier
		 * 
		 * @param idAndOptions the bundle identifier and options
		 * @return the bundle identifier and options or <code>null</code> if the bundle
		 *         should be removed
		 */
		public String processId(String idAndOptions);

		/**
		 * Answer any additional identifiers that should be added (not <code>null</code>)
		 * This is called after processing all identifiers
		 */
		public String[] additionalIds();
	}

	/**
	 * Add the specified bundle identifier if not already added
	 */
	public class AddBundleRule
		implements IBundleRule
	{
		private final String idToAdd;
		private boolean found;

		public AddBundleRule(String idToAdd) {
			this.idToAdd = idToAdd;
		}

		public void reset() {
			found = false;
		}

		public String processId(String idAndOptions) {
			String id = idAndOptions;
			int index = id.indexOf(';');
			if (index > 0)
				id = id.substring(0, index).trim();
			if (id.equals(idToAdd))
				found = true;
			return idAndOptions;
		}

		public String[] additionalIds() {
			return found ? NO_IDS : new String[]{
				idToAdd
			};
		}
	}

	/**
	 * Replace all of the specified bundle identifiers with a single other identifier
	 */
	public class ReplaceBundleRule
		implements IBundleRule
	{
		private final String[] idsToReplace;
		private final String replacementId;
		private boolean replaced;

		public ReplaceBundleRule(String[] idsToReplace, String replacementId) {
			this.idsToReplace = idsToReplace;
			this.replacementId = replacementId;
		}

		public void reset() {
			replaced = false;
		}

		public String processId(String idAndOptions) {
			String id = idAndOptions;
			int index = id.indexOf(';');
			if (index > 0)
				id = id.substring(0, index).trim();
			for (String oldId : idsToReplace) {
				if (oldId.equals(id)) {
					if (replaced)
						return null;
					replaced = true;
					return replacementId;
				}
			}
			if (id.equals(replacementId)) {
				if (replaced)
					return null;
				replaced = true;
				return idAndOptions;
			}
			return idAndOptions;
		}

		public String[] additionalIds() {
			return NO_IDS;
		}
	}

	/**
	 * Remove the specified bundle identifier
	 */
	public class RemoveBundleRule
		implements IBundleRule
	{
		private final String removeId;

		public RemoveBundleRule(String id) {
			this.removeId = id;
		}

		public void reset() {
		}

		public String processId(String idAndOptions) {
			String id = idAndOptions;
			int index = id.indexOf(';');
			if (index > 0)
				id = id.substring(0, index).trim();
			return id.equals(removeId) ? null : idAndOptions;
		}

		public String[] additionalIds() {
			return NO_IDS;
		}
	}
}
