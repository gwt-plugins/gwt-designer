package com.instantiations.pde.build.util;

/**
 * A combination of OEM name (e.g. "OEM-NAME" or null for none) and version (e.g. "3.3",
 * "3.4", ...). The string representation of this is oemName/eclipseTargetVersion (e.g.
 * "OEM-NAME/3.3") or just eclipseTargetVersion if oemName is null (e.g. "3.4").
 */
public class OemVersion
{
	//eclipse versions
	
	public static final	OemVersion V_2_1 = new OemVersion(null, Version.V_2_1);
	public static final OemVersion V_3_0 = new OemVersion(null, Version.V_3_0);
	public static final OemVersion V_3_1 = new OemVersion(null, Version.V_3_1);
	public static final OemVersion V_3_2 = new OemVersion(null, Version.V_3_2);
	public static final OemVersion V_3_3 = new OemVersion(null, Version.V_3_3);
	public static final OemVersion V_3_4 = new OemVersion(null, Version.V_3_4);
	public static final OemVersion V_3_5 = new OemVersion(null, Version.V_3_5);
	public static final OemVersion V_3_6 = new OemVersion(null, Version.V_3_6);
	
	private final String oemName; // "OEM-NAME", or null
	private final Version version; // the Eclipse target version

	public OemVersion(String oemNameAndVersion) {
		int index = oemNameAndVersion.indexOf('/');
		oemName = index > 0 ? oemNameAndVersion.substring(0, index) : null;
		version = new Version(index > 0 ? oemNameAndVersion.substring(index + 1) : oemNameAndVersion);
	}
	
	public OemVersion(String oemName, Version version) {
		this.oemName = oemName;
		this.version = version;
	}

	public String getOemName() {
		return oemName;
	}

	public Version getVersion() {
		return version;
	}

	public boolean equals(Object object) {
		if (object == this) // quicktest
			return true;
		if (!(object instanceof OemVersion))
			return false;
		OemVersion other = (OemVersion) object;
		return (oemName != null ? oemName.equals(other.oemName) : other.oemName == null)
			&& version.equals(other.version);
	}

	public int hashCode() {
		return oemName != null ? oemName.hashCode() + version.hashCode() : version.hashCode();
	}
	
	/**
	 * Append the specified string to the receiver's string representation and return
	 * the result
	 * 
	 * @param string the string to be appended
	 * @return the receiver as a string combined with the specified string
	 */
	public String plus(String string) {
		return toString() + string;
	}

	public String toString() {
		if (oemName == null)
			return version.toString();
		return oemName + "/" + version;
	}
}
