package test.util;

import java.io.File;

public interface CopyFileFilter
{
	boolean shouldCopyFile(File file);
}
