/*
 * Copyright (c) 2004 Object Factory Inc. All rights reserved.
 * This file is made available under the Common Public License (CPL) 1.0
 * (see http://www.opensource.org/licenses/cpl.php).
 * Every copy, modified or not, must retain the above copyright
 * and license notices.
 */
package com.objfac.prebop;

import java.util.HashMap;
import java.util.HashSet;

import com.objfac.prebop.ant.Filetype;

/**
 * @author Bob Foster
 */
public class Main {

	public static void main(String[] args) {
		HashMap vars = new HashMap();
		HashSet exclude = new HashSet();
		boolean replace = false;
		boolean merge = false;
		String indir = null, outdir = null;
		StringBuffer buf = new StringBuffer();
		int i = 0;
		while (i < args.length) {
			String arg = args[i++];
			char start = arg.charAt(0);
			if (start == '-') {
				int pos = 1, len = arg.length();
				if (pos == len)
					exit("Empty switch");
				buf.setLength(0);
				for (; pos < len; pos++) {
					char c = arg.charAt(pos);
					if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'))
						buf.append(c);
					else if (i > 0 && ((c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.'))
						buf.append(c);
					else if (c == '=')
						break;
					else
						exit("Invalid variable name "+arg);
				}
				if (buf.length() == 0)
					exit("Empty variable name");
				String name = buf.toString();
				buf.setLength(0);
				if (pos++ == len) {
					// treat -name as boolean true
					if ("replace".equals(name))
						replace = true;
					else if ("merge".equals(name))
						merge = true;
					else
						vars.put(name, "true");
				}
				else if (pos == len) {
					// treat -name= as setting to ""
					vars.put(name, "");
				}
				else {
					char quote = arg.charAt(pos);
					if (quote == '\'' || quote == '"')
						pos++;
					else
						quote = 0;
					for (; pos < len; pos++) {
						char c = arg.charAt(pos);
						if (c == quote) {
							if (pos+1 < len)
								exit("Quote ends string before end of switch");
							break;
						}
						buf.append(c);
					}
					String value = buf.toString();
					if ("excludeFolder".equals(name))
						exclude.add(value);
					else
						vars.put(name, value);
				}
			}
			else {
				indir = arg;
				if (i < args.length)
					outdir = args[i];
				else 
					exit("Missing output dir argument");
				break;
			}
		}
		buf = null;
		
		HashMap typeMap = new HashMap();
		Filetype jtype = new Filetype();
		jtype.setName("Java");
		jtype.setExtensions("java");
		jtype.setCommentbegin("/*");
		jtype.setCommentend("*/");
		typeMap.put("java", jtype);
			
		Filetype xtype = new Filetype();
		xtype.setName("XML");
		xtype.setExtensions("xml");
		xtype.setCommentbegin("<!--");
		xtype.setCommentend("-->");
		typeMap.put("xml", xtype);
		
		Preprocessor prep = new Preprocessor();
		try {
			prep.preprocess(indir, outdir, vars, exclude, typeMap, replace, merge);
			System.out.println("Success");
		} catch (PreprocessorError e) {
			Throwable t = e.getCause();
			if (t != null) {
			}
			else {
				String msg = e.getMessage();
				FileLocator locator = e.getLocator();
				if (locator != null) {
					System.err.println(msg);
					exit("at line "+locator.getLineNumber()+" in file "+locator.getFileName());
				}
				else
					exit(msg);
			}
		}
	}
	
	public static void exit(String msg) {
		System.err.println(msg);
		System.exit(1);
	}
}
