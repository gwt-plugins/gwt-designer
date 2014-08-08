package com.instantiations.pde.build.preprocessor;

import java.util.HashMap;
import java.util.Map;

import com.instantiations.pde.build.util.BuildProperties;
import com.instantiations.pde.build.util.Version;

/**
 * A preprocessor for site.xml files replacing "0.0.0" and "0.0.0.qualifier"
 */
public class SiteXmlPreprocessor extends VariableReplacementPreprocessor
{
	private Map<String, String> versionMap = null;

	/**
	 * Construct a new instance.
	 * 
	 * @param prop the build properties
	 */
	public SiteXmlPreprocessor(Version eclipseTargetVersion, BuildProperties prop) {
		super(eclipseTargetVersion, prop);
	}

	/**
	 * Set the version to be used for the specified feature
	 * 
	 * @param featureId the identifier of the feature to be modified
	 * @param fullVersion the version and qualifier of the feature
	 */
	public void setVersion(String featureId, String fullVersion) {
		if (versionMap == null) {
			versionMap = new HashMap<String, String>();
			lineProcessors.add(new SiteXmlFeatureVersionReplacement());
		}
		versionMap.put(featureId, fullVersion);
	}

	/**
	 * Replace the version in both the url and version attributes
	 * for any features specified in the versionMap
	 */
	private class SiteXmlFeatureVersionReplacement
		implements LineProcessor
	{
		public void reset() {
		}

		public String process(String line) {
			XmlElement elem = new XmlElement();
			if (!elem.parse(line) || !elem.getTag().equals("feature"))
				return line;
			String url = elem.getAttribute("url");
			if (url == null || !url.startsWith("features/"))
				return line;
			String id = elem.getAttribute("id");
			if (id == null)
				return line;
			String oldVersion = elem.getAttribute("version");
			if (oldVersion == null)
				return line;
			String newVersion = versionMap.get(id);
			if (newVersion == null || newVersion.equals(oldVersion))
				return line;
			elem.setAttribute("url", "features/" + id + "_" + newVersion + ".jar");
			elem.setAttribute("version", newVersion);
			return elem.getLine();
		}
	}

	private class XmlElement
	{
		/**
		 * The line containing the XML element
		 */
		String line;

		/**
		 * The XML element's tag
		 */
		String tag;

		/**
		 * A mapping of attribute key to the index where their value start in the line
		 */
		Map<String, Integer> attributes;

		/**
		 * Answer the tag for the XML element. Must call {@link #parse(String)} first.
		 */
		public String getTag() {
			return tag;
		}

		/**
		 * Answer the value for the specified attribute
		 * @param key the attribute key
		 * @return the attribute's value or null if the attribute is undefined
		 */
		public String getAttribute(String key) {
			Integer start = attributes.get(key);
			if (start == null)
				return null;
			int end = skipToEndOfValue(start);
			return line.substring(start, end);
		}

		/**
		 * Set the value for the specified attribute
		 * @param key the attribute key
		 * @param value the new value for the attribute
		 */
		public void setAttribute(String key, String value) {
			Integer start = attributes.get(key);
			if (start == null)
				throw new IllegalStateException("Not implemented yet... Cannot add new attributes");
			int end = skipToEndOfValue(start);
			line = line.substring(0, start) + value + line.substring(end);
			// Adjust offsets of any trailing attributes
			int delta = value.length() - (end - start);
			for (Map.Entry<String, Integer> entry : attributes.entrySet())
				if (entry.getValue() > start)
					entry.setValue(entry.getValue() + delta);
		}

		/**
		 * Answer the line containing the XML element. Must call {@link #parse(String)} first.
		 */
		public String getLine() {
			return line;
		}

		/**
		 * Parse the XML element from the specified line
		 * 
		 * @param line the line to be parsed
		 * @return true if the XML element information was parsed, else false
		 */
		public boolean parse(final String line) {
			this.line = line;
			final int length = line.length();

			// Skip leading characters
			int index = skipWhitespace(0);
			if (index == length || line.charAt(index) != '<')
				return false;
			index++;

			// Read the XML element's tag
			index = skipWhitespace(index);
			int tokenStart = index;
			while (index < length && Character.isLetterOrDigit(line.charAt(index)))
				index++;
			if (index == tokenStart)
				return false;
			tag = line.substring(tokenStart, index);

			// Read attributes
			attributes = new HashMap<String, Integer>();
			while (true) {
				index = skipWhitespace(index);
				if (index == length)
					return false;
				if (line.charAt(index) == '/' || line.charAt(index) == '>')
					break;

				// Read the attribute key
				if (!Character.isLetter(line.charAt(index)))
					return false;
				tokenStart = index;
				while (index < length && Character.isLetterOrDigit(line.charAt(index)))
					index++;
				String key = line.substring(tokenStart, index);

				// Skip the equals sign
				index = skipWhitespace(index);
				if (index == length || line.charAt(index) != '=')
					return false;
				index++;
				index = skipWhitespace(index);

				// Read the attribute value
				if (index == length || line.charAt(index) != '"')
					return false;
				index++;
				attributes.put(key, index);
				index = skipToEndOfValue(index);
				if (index == length)
					return false;
				index++;
			}

			// Capture trailing characters
			if (line.charAt(index) == '/') {
				if (index == length || line.charAt(index + 1) != '>')
					return false;
			}
			return true;
		}

		/**
		 * Return the index of the next double quote character ('"') denoting the end of
		 * the attribute value
		 * 
		 * @param start the starting index
		 * @return the index of the double quote or end of line
		 */
		private int skipToEndOfValue(int start) {
			int index = start;
			int length = line.length();
			while (index < length && line.charAt(index) != '"')
				index++;
			return index;
		}

		/**
		 * Return the index of the character beyond the last whitespace character
		 * 
		 * @param start the starting index
		 * @return the index after skipping whitespace which may be the end of the line
		 */
		private int skipWhitespace(int start) {
			int index = start;
			int length = line.length();
			while (index < length && Character.isWhitespace(line.charAt(index)))
				index++;
			return index;
		}
	}
}
