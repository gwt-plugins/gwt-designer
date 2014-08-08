/*
 * Copyright (c) 2004 Object Factory Inc. All rights reserved.
 * This file is made available under the Common Public License (CPL) 1.0
 * (see http://www.opensource.org/licenses/cpl.php).
 * Every copy, modified or not, must retain the above copyright
 * and license notices.
 */
package com.objfac.prebop;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;

import com.objfac.prebop.ant.Filetype;

/**
 * @author Bob Foster
 */
public class Preprocessor {

	private HashMap fTypeMap;
	private HashSet fCopied;
	private int fModCount;
	private int fFileCount;
	private boolean fMerge;
	private HashSet fExclude;
	private int fLno;
	private File fFile;
	private InputStream fIn;
	private static final byte[] CR = new byte[] { '\r' };
	private static final byte[] CRLF = new byte[] { '\r','\n' };
	private static final byte[] LF = new byte[] { '\n' };
	private static final String EMPTY = "";
	private int fEnd;
	private int fPos;
	private HashMap fVars;
	private byte[] fBuf;
	private byte[] fEol = CRLF;

	/**
	 * Preprocess the entire indir directory and write the
	 * result to the outdir directory, copying all files
	 * from in to out with special handling for .java and
	 * .xml files.
	 */
	public int preprocess(
		String indir, String outdir, 
		HashMap vars, HashSet exclude, HashMap typeMap,
		boolean replace, boolean merge)
	throws PreprocessorError {
		File indirFile = new File(indir);
		if (!indirFile.exists())
			error("Input directory "+indir+" does not exist");
		if (!indirFile.isDirectory())
			error("Input directory "+indir+" is not a directory");
		File outdirFile = new File(outdir);
		if (outdirFile.exists()) {
			if (replace)
				delete(outdirFile);
			else if (!merge)
				error("Output directory "+outdir+" already exists");
		}
		fVars = vars;
		fExclude = exclude;
		fTypeMap = typeMap;
		fMerge = merge;
		fBuf = new byte[32768];
		fFileCount = 0;
		fModCount = 0;
		fCopied = new HashSet();
		preprocess(indirFile, outdirFile);
		fCopied = null;
		fBuf = null;
		fVars = null;
		fExclude = null;
		return fFileCount;
	}
	
	public int preprocess(
		File indirFile, File outdirFile, 
		HashMap vars, HashSet exclude, HashMap typeMap,
		boolean replace, boolean merge)
	throws PreprocessorError {
		//??? this doesn't catch overlapping dirs, which could lead to infinite loop
		if (indirFile.equals(outdirFile) && !merge)
			error("Input and output directories are the same");
		if (outdirFile.exists()) {
			if (replace)
				delete(outdirFile);
			else if (!merge)
				error("Output directory "+outdirFile.getAbsolutePath()+" already exists");
		}
		fVars = vars;
		fExclude = exclude;
		fTypeMap = typeMap;
		fMerge = merge;
		fBuf = new byte[32768];
		fFileCount = 0;
		fModCount = 0;
		fCopied = new HashSet();
		preprocess(indirFile, outdirFile);
		fCopied = null;
		fBuf = null;
		fVars = null;
		fExclude = null;
		return fFileCount;
	}
	
	public int preprocessFile(
		File inFile, File outFile, 
		HashMap vars, HashSet exclude, HashMap typeMap,
		boolean replace, boolean merge)
	throws PreprocessorError, IOException {
		//??? this doesn't catch overlapping dirs, which could lead to infinite loop
		if (inFile.equals(outFile) && !merge)
			error("Input and output files are the same");
		if (outFile.exists()) {
			if (replace)
				delete(outFile);
			else if (!merge)
				error("Output file "+outFile.getAbsolutePath()+" already exists");
		}
		fVars = vars;
		fExclude = exclude;
		fTypeMap = typeMap;
		fMerge = merge;
		fBuf = new byte[32768];
		fFileCount = 0;
		fModCount = 0;
		fCopied = new HashSet();

		fFileCount++;
		String oldname = inFile.getCanonicalPath();
		String newname = outFile.getCanonicalPath();
		String ext = getExtension(oldname);
		Filetype ft = (Filetype) fTypeMap.get(ext);
		if (ft != null) {
			String outext = ft.getOutextension();
			newname = oldname.substring(0, oldname.length()-ext.length())+outext;
			preprocessFile(inFile, newname, ft);
		}
		if (!oldname.equals(newname))
			copyFile(inFile, oldname);
		
		fCopied = null;
		fBuf = null;
		fVars = null;
		fExclude = null;
		return fFileCount;
	}
	
	public int getModCount() {
		return fModCount;
	}

	private void delete(File file) throws PreprocessorError {
		boolean isdir = file.isDirectory();
		if (isdir) {
			File[] files = file.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++)
					delete(files[i]);
			}
		}
		if (file.exists() && !file.delete())
			error((isdir ? "Can't delete directory " : "Can't delete file ")+file.getAbsolutePath());
		
	}

	private void preprocess(File indirFile, File outdirFile) throws PreprocessorError {
		if (fExclude.contains(outdirFile.getName()))
			return;
		if (outdirFile.exists() ? !fMerge : !outdirFile.mkdirs())
			error("Cannot create output directory "+outdirFile.getAbsolutePath());
		File[] files = indirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String name = file.getName();
			if (!fExclude.contains(name)) {
				String outdirPath = outdirFile.getAbsolutePath();
				if (!outdirPath.endsWith(File.separator))
					outdirPath += File.separator;
				if (file.isDirectory()) {
					preprocess(file, new File(outdirPath+name));
				}
				else if (!fExclude.contains(name)) {
					fFileCount++;
					String oldname = outdirPath + name;
					String newname = null;
					if (!fCopied.contains(oldname)) {
						fCopied.add(oldname);
						String ext = getExtension(name);
						Filetype ft = (Filetype) fTypeMap.get(ext);
						if (ft != null) {
							String outext = ft.getOutextension();
							newname = outdirPath + name.substring(0, name.length()-ext.length())+outext;
							fCopied.add(newname);
							preprocessFile(file, newname, ft);
						}
						if (!oldname.equals(newname))
							copyFile(file, oldname);
					}
				}
			}
		}
	}

	private void preprocessFile(File file, String name, Filetype ft) throws PreprocessorError {
		String beg = ft.getCommentbegin();
		String end = " " + ft.getCommentend();
		preprocessFile(file, name, beg, beg+' ', end);
	}

	private String getExtension(String name) {
		int pos = name.lastIndexOf('.');
		if (pos >= 0)
			return name.substring(pos+1);
		return "";
	}

	private void copyFile(File file, File outFile) throws PreprocessorError {
		if (file.equals(outFile)) return;
		try {
			if (!outFile.createNewFile())
				error("Output file already exists "+outFile.getAbsolutePath());
			InputStream in = new BufferedInputStream(new FileInputStream(file));
			OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
			int read;
			while ((read = in.read(fBuf)) >= 0) {
				if (read > 0) {
					out.write(fBuf, 0, read);
				}
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			error("File not found "+file.getAbsolutePath());
		} catch (IOException e) {
			error("Error writing "+outFile.getAbsolutePath());
		}
		outFile.setLastModified(file.lastModified());
	}
	
	private static final int INCLUDE = 0;
	private static final int LOOK = 1;
	private static final int SKIP = 2;
	private static final int PUSH_SKIP = 3;

	private boolean preprocessFile(File file, String name, String commentStart, String insertBeg, String insertEnd) throws PreprocessorError {
		InputStream in;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			return error("File not found "+file.getAbsolutePath());
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		fIn = in;
		fFile = file;
		fPos = fEnd = 0;
		fLno = 0;
		boolean mod = false;
		try {
		while (true) {
			byte[] line = getLine();
			if (line == null) break;
			fLno++;
			int pos, indent;
			int indentEnd = skipWs(line, 0);
			if ((pos = match(line, commentStart, indentEnd)) > indentEnd) {
				pos = skipWs(line, pos);
			}
			int exprBeg = pos;
			if ((pos = match(line, "$if", exprBeg)) > exprBeg) {
				mod = true;
				Expression expr = new Expression(line, pos, fVars);
				int exprEnd = expr.getEnd();
				if (expr.eval()) {
					putLine(indentEnd, insertBeg, line, exprBeg, exprEnd, insertEnd, out, name);
					if (!preprocessIfBody(INCLUDE, commentStart, insertBeg, insertEnd, out, name))
						return false;
				}
				else {
					putLine(indentEnd, insertBeg, line, exprBeg, exprEnd, EMPTY, out, name);
					if (!preprocessIfBody(LOOK, commentStart, insertBeg, insertEnd, out, name))
						return false;
				}
			}
			else {
//				System.out.println(new String(line) + "|" + file.getName());
				putLine(line, out, name);
			}
		}
		} catch (PreprocessorError e) {
			if (e.getLocator() == null) {
				e.setLocator(new FileLocator(file.getAbsolutePath(), fLno));
			}
			throw e;
		} catch (Exception e) {
			throw new PreprocessorError(e);
		} finally {
			try {
				out.close();
				in.close();
			} catch (IOException e2) {
				// can't do much about it
			}
		}
		if (mod) {
			fModCount++;
			try {
				OutputStream outStream = new BufferedOutputStream(new FileOutputStream(
					new File(name)));
				byte[] contents = out.toByteArray();
				outStream.write(contents);
				outStream.close();
			} catch (FileNotFoundException e1) {
				return error("Error creating output stream for "+name);
			} catch (IOException e) {
				return error("Error writing output stream for "+name);
			}
		}
		else {
			copyFile(file, name);
		}
		return true;
	}
	
	private void copyFile(File file, String name) throws PreprocessorError {
		copyFile(file, new File(name));
	}

	private boolean preprocessIfBody(int state, String commentStart, String insertBeg, String insertEnd, OutputStream out, String name) throws PreprocessorError {
		int pos = -1;
		byte[] line;
		int indentEnd, exprBeg, exprEnd;
		while (true) {
			line = getLine();
			if (line == null) {
				return error("End of file before $endif", fLno, new byte[0], fFile);
			}
			fLno++;
			indentEnd = skipWs(line, 0);
			if ((pos = match(line, commentStart, indentEnd)) > indentEnd) {
				pos = skipWs(line, pos);
			}
			exprBeg = pos;
			
			if ((pos = match(line, "$if", exprBeg)) > exprBeg) {
				Expression expr = new Expression(line, pos, fVars);
				exprEnd = expr.getEnd();
				if (state == INCLUDE) {
					if (expr.eval()) {
						putLine(indentEnd, insertBeg, line, exprBeg, exprEnd, insertEnd, out, name);
						if (!preprocessIfBody(INCLUDE, commentStart, insertBeg, insertEnd, out, name))
							return false;
					}
					else {
						putLine(indentEnd, insertBeg, line, exprBeg, exprEnd, EMPTY, out, name);
						if (!preprocessIfBody(LOOK, commentStart, insertBeg, insertEnd, out, name))
							return false;
					}
				}
				else if (state == LOOK) {
					putLine(indentEnd, EMPTY, line, exprBeg, exprEnd, EMPTY, out, name);
					if (!preprocessIfBody(PUSH_SKIP, commentStart, insertBeg, insertEnd, out, name))
						return false;
				}
				else if (state == SKIP || state == PUSH_SKIP) {
					putLine(indentEnd, EMPTY, line, exprBeg, exprEnd, EMPTY, out, name);
					if (!preprocessIfBody(PUSH_SKIP, commentStart, insertBeg, insertEnd, out, name))
						return false;
				}
			}
			else if ((pos = match(line, "$elseif", exprBeg)) > exprBeg) {
				Expression expr = new Expression(line, pos, fVars);
				exprEnd = expr.getEnd();
				if (state == INCLUDE) {
					putLine(indentEnd, insertBeg, line, exprBeg, exprEnd, EMPTY, out, name);
					state = SKIP;
				}
				else if (state == LOOK) {
					if (expr.eval()) {
						putLine(indentEnd, EMPTY, line, exprBeg, exprEnd, insertEnd, out, name);
						state = INCLUDE;
					}
					else {
						putLine(indentEnd, EMPTY, line, exprBeg, exprEnd, EMPTY, out, name);
					}
				}
				else if (state == SKIP || state == PUSH_SKIP) {
					putLine(indentEnd, EMPTY, line, exprBeg, exprEnd, EMPTY, out, name);
				}
			}
			else if ((pos = match(line, "$else", exprBeg)) > exprBeg) {
				pos = skipWs(line, pos);
				exprEnd = match(line, "$", pos);
				if (exprEnd < 0) {
					return error("Expect $ after $else", fLno, line, fFile);
				}
				if (state == INCLUDE) {
					putLine(indentEnd, insertBeg, line, exprBeg, exprEnd, EMPTY, out, name);
					state = SKIP;
				}
				else if (state == LOOK) {
					putLine(indentEnd, EMPTY, line, exprBeg, exprEnd, insertEnd, out, name);
					state = INCLUDE;
				}
				else if (state == SKIP || state == PUSH_SKIP) {
					putLine(indentEnd, EMPTY, line, exprBeg, exprEnd, EMPTY, out, name);
				}
			}
			else if ((pos = match(line, "$endif", exprBeg)) > exprBeg) {
				pos = skipWs(line, pos);
				exprEnd = match(line, "$", pos);
				if (exprEnd < 0) {
					return error("Expect $ after $endif", fLno, line, fFile);
				}
				if (state == INCLUDE) {
					putLine(indentEnd, insertBeg, line, exprBeg, exprEnd, insertEnd, out, name);
				}
				else if (state == LOOK || state == SKIP) {
					putLine(indentEnd, EMPTY, line, exprBeg, exprEnd, insertEnd, out, name);
				}
				else if (state == PUSH_SKIP) {
					putLine(indentEnd, EMPTY, line, exprBeg, exprEnd, EMPTY, out, name);
				}
				break;
			}
			else
				putLine(line, out, name);
		}
		return true;
	}
	
	private boolean error(String msg, int lineNumber, byte[] line, File file) throws PreprocessorError {
		throw new PreprocessorError(msg, new FileLocator(file.getAbsolutePath(), lineNumber));
	}

	private boolean error(String msg) throws PreprocessorError {
		throw new PreprocessorError(msg);
	}
	
	private int match(byte[] line, String s, int pos) {
		int llen = line.length, slen = s.length();
		int i = 0, beg = pos;
		while (pos < llen && i < slen && line[pos] == s.charAt(i)) {
			pos++;
			i++;
		}
		return i == slen ? pos : beg;
	}

	private int skipWs(byte[] line, int pos) {
		while (pos < line.length && (line[pos] == ' ' || line[pos] == '\t'))
			pos++;
		return pos;
	}

	/**
	 * Write line in four parts:
	 *  1. line up to indentEnd.
	 *  2. lineBeg.
	 *  3. line between exprBeg and exprEnd.
	 *  4. lineEnd. 
	 */
	private void putLine(int indentEnd, String lineBeg, byte[] line, int exprBeg, int exprEnd, String lineEnd, OutputStream out, String name) throws PreprocessorError {
		try {
			if (indentEnd > 0)
				out.write(line, 0, indentEnd);
			writeString(out, lineBeg);
			out.write(line, exprBeg, exprEnd-exprBeg);
			writeString(out, lineEnd);
			out.write(fEol);
		} catch (IOException e) {
			error("IO error writing "+name);
		}
	}
	
	private void writeString(OutputStream out, String s) throws IOException {
		int len = s.length();
		for (int i = 0, n = s.length(); i < n; i++) {
			out.write(0xFF&s.charAt(i));
		}
	}

	/**
	 * Write line without changes.
	 */
	private void putLine(byte[] line, OutputStream out, String name) throws PreprocessorError {
		try {
			out.write(line);
			out.write(fEol);
		} catch (IOException e) {
			error("IO error writing "+name);
		} catch (OutOfMemoryError e) {
			throw new OutOfMemoryError("out of memory processing file " + name);
		}
	}

	private int find(byte[] line, String target, int pos) {
		int len = line.length;
		if (pos >= len) return -1;
		int c;
		while (pos < len && ((c = line[pos]) == ' ' || c == '\t'))
			pos++;
		if (pos == len) return -1;
		int tpos = 0, tlen = target.length();
		while (pos < len && tpos < tlen && line[pos] == target.charAt(tpos)) {
			pos++;
			tpos++;
		}
		return tpos == tlen ? pos : -1;
	}

	private byte[] getLine() throws PreprocessorError {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		boolean seen = false;
		while (true) {
			int c;
			while ((c = nextChar()) >= 0 && c != '\n' && c != '\r') {
				out.write((byte)c);
				seen = true;
			}
			if (c >= 0) {
				seen = true;
				if (c == '\r') {
					if (nextChar() == '\n')
						fEol = CRLF;
					else {
						fEol = CR;
						// only push the character back on the if
						// it is not the last character in the file
						if (fPos != fEnd) {
							pushChar();
						}
					}
				}
				else
					fEol = LF;
			}
			return seen ? out.toByteArray() : null;
		}
	}
	
	private int nextChar() throws PreprocessorError {
		if (fPos == fEnd) {
			int read = 0;
			try {
				while ((read = fIn.read(fBuf)) == 0)
					continue;
			} catch (IOException e) {
				error("IO error reading "+fFile.getAbsolutePath());
			}
			if (read < 0)
				return -1;
			fEnd = read;
			fPos = 0;
		}
		return ((int) fBuf[fPos++]) & 0xFF;
	}
	
	private void pushChar() {
		fPos--;
	}

	private byte[] copySeg(byte[] seg, int segStart, int segEnd) {
		int newSegLen = segEnd-segStart, oldSegLen = seg == null ? 0 : seg.length;
		if (newSegLen > 0) {
			byte[] tmp = new byte[oldSegLen+newSegLen];
			System.arraycopy(seg,0,tmp,0,oldSegLen);
			seg = tmp;
			System.arraycopy(fBuf,segStart,seg,oldSegLen,newSegLen);
		}
		return seg;
	}

}
