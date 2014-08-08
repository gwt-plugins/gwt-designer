package test.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * General file utilities using during the testing process
 */
public class FileUtil
{
	private static final CopyFileFilter ALL_FILES_FILTER = new CopyFileFilter() {
		public boolean shouldCopyFile(File file) {
			return true;
		}
	};
	
	/**
	 * Copy a single file or a directory of files recursively.
	 * 
	 * @param src the source file or directory
	 * @param dst the destination file or directory
	 * @return the destination
	 */
	public static File copyFiles(File src, File dst) throws IOException {
		return copyFiles(src, dst, ALL_FILES_FILTER);
	}
	
	/**
	 * Copy a single file or a directory of files recursively.
	 * 
	 * @param src the source file or directory
	 * @param dst the destination file or directory
	 * @return the destination
	 */
	public static File copyFiles(File src, File dst, CopyFileFilter filter) throws IOException {
		if (src.getName().equals(".svn"))
			return null;
		if (src.isDirectory()) {
			for (String name : new TreeSet<String>(Arrays.asList(src.list())))
				copyFiles(new File(src, name), new File(dst, name), filter);
		}
		else {
			if (dst.exists())
				dst.delete();
			if (!filter.shouldCopyFile(src))
				return dst;
			dst.getParentFile().mkdirs();
			byte[] buf = new byte[4096];
			InputStream input = new BufferedInputStream(new FileInputStream(src));
			try {
				OutputStream output = new BufferedOutputStream(new FileOutputStream(dst));
				try {
					while (true) {
						int count = input.read(buf);
						if (count == -1)
							break;
						output.write(buf, 0, count);
					}
				}
				finally {
					output.close();
				}
			}
			finally {
				input.close();
			}
		}
		return dst;
	}

	/**
	 * Delete an entire file tree. Validate that this is safe by requiring "test" in the
	 * path of the files to be deleted.
	 */
	public static void deleteFiles(File file) {
		if (file.getPath().indexOf("test") == -1)
			throw new RuntimeException("To prevent inadvertantly deleting important files, 'test' must be in the path of the file to be deleted");
		if (file.isDirectory())
			for (File child : file.listFiles())
				deleteFiles(child);
		file.delete();
	}

	/**
	 * Read the content of the specified file
	 * 
	 * @param file the file to be read
	 * @return the file's content
	 */
	public static String readFile(File file) throws IOException {
		StringWriter stringWriter = new StringWriter(4096);
		FileReader fileReader = new FileReader(file);
		try {
			PrintWriter writer = new PrintWriter(stringWriter);
			LineNumberReader reader = new LineNumberReader(new BufferedReader(fileReader));
			while (true) {
				String line = reader.readLine();
				if (line == null)
					break;
				writer.println(line);
			}
		}
		finally {
			fileReader.close();
		}
		return stringWriter.toString();
	}

}
