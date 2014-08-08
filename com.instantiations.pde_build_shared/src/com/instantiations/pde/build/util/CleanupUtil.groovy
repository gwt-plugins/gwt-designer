package com.instantiations.pde.build.util

/**
 * Class for cleaning up temporary directories 
 * and older builds in the continuous and integration directories
 */
public class CleanupUtil extends BuildUtil
{
	public int numberOfBuildsToSave = 5;
	
	/**
	 * Clear out the temp build space
	 */
	public void cleanupTemp() {
		ant.delete(dir: prop.buildTemp);
		prop.buildTemp.mkdirs();
	}
	
	/**
	 * Delete older build directories 
	 * in the prop.productOut and prop.productArchive directories.
	 */
	public void cleanupOldBuilds() {
		
		// Sort directories by build number
		def dirs = new TreeMap();
		prop.productOut.parentFile.eachDir { eachDir ->
			if (eachDir.name.startsWith("v")) {
				int i = eachDir.name.lastIndexOf('_');
				if (i > 0)
					dirs.put(eachDir.name.substring(i+1), eachDir);
			}
		}
		dirs = new ArrayList(dirs.values());
		
		// Delete all but most recent output directories
		def save = new HashSet();
		int count = numberOfBuildsToSave;
		for (int i = dirs.size() - 1; i >= 0; i--) {
			File eachDir = dirs[i];
			if (count == 0 || !new File(eachDir, "build-date.html").exists()) {
				deleteDirOrSymlink(eachDir);
			}
			else {
				save.add(eachDir.name);
				count--;
			}
		}
		
		// Delete all but most recent artifact directories parallel to output directories
		prop.productArtifacts.parentFile.eachDir { eachDir ->
			if (eachDir.name.startsWith("v") 
				&& eachDir.name.lastIndexOf('_') > 0
				&& !save.contains(eachDir.name))
			{
				deleteDirOrSymlink(eachDir);
			}
		}
	}
}
