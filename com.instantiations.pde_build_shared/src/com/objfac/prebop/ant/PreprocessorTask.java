/*
 * Copyright (c) 2004 Object Factory Inc. All rights reserved.
 * This file is made available under the Common Public License (CPL) 1.0
 * (see http://www.opensource.org/licenses/cpl.php).
 * Every copy, modified or not, must retain the above copyright
 * and license notices.
 */
package com.objfac.prebop.ant;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import com.objfac.prebop.FileLocator;
import com.objfac.prebop.Preprocessor;
import com.objfac.prebop.PreprocessorError;

/**
	<h1>preprocess Ant Task</h1>
	<h2>preprocess task</h2>
	<p>Each <code>preprocess</code> task copies a single directory 
		(recursively) from input to output, transforming preprocessor comments 
		as it goes.</p>
	<h3>preprocess attributes</h3>
	<table cellspacing="4" cellpadding="4" border="1">
		<tr>
			<th align="left">Name</th>
			<th align="left">Value(s)</th>
			<th align="left">Description</th>
		</tr>
		<tr>
			<td valign="top">
				<code>indir</code>
			</td>
			<td valign="top">File path</td>
			<td valign="top">The input directory to be preprocessed. This 
				directory, all sub-directories (recursively) and files in them 
				are copied to the output directory. Files with ".java" and 
				".xml" extensions are examined for preprocessor comments, 
				which, if found, are transformed as described below.</td>
		</tr>
		<tr>
			<td valign="top">
				<code>outdir</code>
			</td>
			<td valign="top">File path</td>
			<td valign="top">The output target directory. If the value of the 
				<code>out</code> attribute is "create", this directory must not 
				already exist.</td>
		</tr>
		<tr>
			<td valign="top">
				<code>out</code>
			</td>
			<td valign="top">"create", "replace" or "merge"</td>
			<td valign="top">Specify "create" if the output directory does not 
				already exist and is to be created. Specify "replace" to delete 
				the existing output directory, if any, and replace it with the 
				new directory and contents. Specify "merge" to leave the 
				existing output directory, if any, and merge the result of 
				preprocess into it.</td>
		</tr>
		<tr>
			<td valign="top">
				<code>except</code>
			</td>
			<td valign="top">Comma-separated list of names</td>
			<td valign="top">A directory or file in the input directory whose 
				name is in the list is skipped. Names must be simple names, 
				e.g., "bin, FunnyClass.java", and must not contain / 
				characters. Whitespace is trimmed from the begin and end of 
				each name, but left alone in the middle of a name.</td>
		</tr>
	</table>
	<h2>var element</h2>
	<p>The <code>var</code> element sets the value of a preprocessor variable 
		which can be tested in preprocessor comments in the input.</p>
	<h3>var attributes</h3>
	<table cellspacing="4" cellpadding="4" border="1">
		<tr>
			<th align="left">Name</th>
			<th align="left">Value</th>
			<th align="left">Description</th>
		</tr>
		<tr>
			<td valign="top">
				<code>name</code>
			</td>
			<td valign="top">identifier</td>
			<td valign="top">The var name must begin with an ASCII alphabetic 
				character 'a'-'z' or 'A'-'Z'. It may continue with any number 
				of alphabetic characters, numeric digits '0'-'9', and the 
				characters '_' (underscore), '-' (dash) or '.' (dot).</td>
		</tr>
		<tr>
			<td valign="top">
				<code>value</code>
			</td>
			<td valign="top">string</td>
			<td valign="top">The value may be any string, but the following 
				strings are treated specially: "true" and "false" are treated 
				as the boolean values true and false. Any string beginning with 
				a digit '0'-'9' is treated as a hierarchical number, and must 
				match the regular expression "[0-9]+(\.[0-9]*)?(\.[0-9]*)?", 
				for example, 1, 1., 10.2 and 2.1.2 are valid numbers with 1, 1, 
				2, and 3 hierarchical levels, respectively.</td>
		</tr>
	</table>
	<h2>filetype element</h2>
	<p>The <code>filetype</code> element extends the file types
		examined by the preprocessor.</p>
	<h3>filetype attributes</h3>
	<table cellspacing="4" cellpadding="4" border="1">
		<tr>
			<th align="left">Name</th>
			<th align="left">Value</th>
			<th align="left">Description</th>
		</tr>
		<tr>
			<td valign="top">
				<code>name</code>
			</td>
			<td valign="top">identifier</td>
			<td valign="top">Optional human-readable name; used only in 
				error messages.</td>
		</tr>
		<tr>
			<td valign="top">
				<code>extensions</code>
			</td>
			<td valign="top">comma-separated list of file extensions</td>
			<td valign="top">E.g., <code>extensions="xml, xsl, xsd"</code>.</td>
		</tr>
		<tr>
			<td valign="top">
				<code>commentbegin</code>
			</td>
			<td valign="top">comment BEGIN bracket</td>
			<td valign="top">The comment start string, e.g., 
				<code>commentbegin="&amp;lt;!--"</code>.</td>
		</tr>
		<tr>
			<td valign="top">
				<code>commentend</code>
			</td>
			<td valign="top">comment END bracket</td>
			<td valign="top">The comment stop string, e.g., 
				<code>commentend="-->"</code>.</td>
		</tr>
	</table>
	<p>By default, the preprocessor provides the following filetypes:</p>
	<pre>
	&lt;filetype name="Java" extensions="java" commentbegin="/*" commentend="*&#47;"/>
	&lt;filetype name="XML" extensions="xml" commentbegin="&amp;lt;!--" commentend="-->"/>
	</pre>
	<p><code>filetype</code> elements you write extend or override the 
		defaults. (Hint: You can disable processing of Java or XML files by 
		defining a filetype with the <code>"java"</code> or <code>"xml"</code> 
		extensions and defining <code>commentbegin</code> as some character 
		sequence unlikely to occur at the beginning of a line.)</p>
	<h2>How the preprocessor works</h2>
	<p>The preprocess task copies files and folders from the input directory to 
		the output directory, and allows conditional inclusion of source 
		contents for Java (.java extension) and XML (.xml extension) source 
		files.</p>
	<p>The preprocessor works by examining comments within source files. If it 
		finds a comment that begins with the characters "$if" it enters 
		replacement mode. In replacement mode:</p>
	<ul>
		<li>Every comment or non-comment line that begins with the characters 
			"$if", "$elseif", "$else" or "$endif" is interpreted as a 
			preprocessor statement.</li>
		<li>Preprocessor statements must be written entirely on one line with 
			nothing but optional whitespace or opening comment brackets to the 
			left of the statement</li>
		<li>Preprocessor statements use the comment brackets <code>/* *&#47;</code> 
			(for Java) and <code>&lt;!-- --&gt;</code> (for XML). Comments 
			beginning with // are not treated specially.</li>
	</ul>
	<p>The formats of the preprocessor statements are as follows.</p>
	<pre>
	[BEGIN] $if condition $ [END]
	[BEGIN] $elseif condition $ [END]
	[BEGIN] $else $ [END]
	[BEGIN] $endif $ [END]
</pre>
<p>In the above, BEGIN stands for <code>/*</code> in Java or 
	<code>&lt;!--</code> in XML. END stands for <code>*&#47;</code> in Java or 
	<code>--&gt;</code> in XML. [] enclose optional parts. The BEGIN at the 
	start of the first preprocessor <code>$if</code> in a comment group and 
	the END at the end of the last preprocessor <code>$endif</code> in a 
	comment group are required. All others are optional.</p>
<p>Every preprocessor <code>$if</code> must be followed by a balancing 
	<code>$endif</code>. <code>$else</code> must follow all occurrences of 
	<code>$elseif</code> at the same level.</p>
<p>The basic operation of the preprocessor is to add or remove BEGIN and 
	END brackets as needed to honor the conditions. A simple example makes 
	this obvious:</p>
<pre>
	/* $if version >= 3.0.0 $ *&#47;
	import org.eclipse.ide.*;
	/* $endif$ *&#47;
</pre>
	<p>The above would be the result if the value of the <code>version</code> 
		preprocessor variable were, e.g., "3.0.0". On the other hand, if the 
		value of <code>version</code> variable were "2.1.2", the result would 
		be:</p>
	<pre>
	/* $if version >= 3.0.0 $
	import org.eclipse.ide.*;
	$endif$ *&#47;
</pre>
	<p>Because the condition evaluates to false, the contents between the $if 
		and $endif are "commented out" by removing the appropriate BEGIN and 
		END brackets. Note that the $ characters in a preprocessor statement 
		and everything between them are always preserved, as is the leading 
		whitespace on the line.</p>
	<p>Here is a more complicated example, assuming that the variables 
		<code>verson</code> and <code>lite</code> have the values "2.1.2" and 
		"false", respectively.</p>
	<pre>
	/* $if version &lt; 3.0.0 $ *&#47;
	  /* $if lite$
	  foo();
	  $else$ *&#47;
	  bar(); // this line is included
	  /* $endif$ *&#47;
	/* $else$
	  $if lite$
	  foo3();
	  $else$
	  bar3();
	  $endif$
	$endif$ *&#47;
</pre>
	<p>Nested conditionals like this are allowed but are difficult for humans 
		to read. Indentation helps, as above, but tends to confuse the 
		indentation of the code in which the statements appear (and none of the 
		automatic formatters will preserve it.) Simpler statements are 
		encouraged. For example, here is the same example rewritten with 
		compound conditionals:</p>
	<pre>
	/* $if version&lt;3.0.0 &amp;&amp; lite$
	foo();
	$elseif version&lt;3.0.0 &amp;&amp; !lite$ *&#47;
	bar(); // this line is included
	/* $elseif version>=3.0.0 &amp;&amp; lite$
	foo3();
	$else$
	bar3();
	$endif$ *&#47;
</pre>
	<p>Simplicity pays big divendends in maintainability.</p>
	<p>The BEGIN/END brackets in preprocessor statements do not have to be 
		written correctly in the input. The only requirements are:</p>
	<ul>
	<li>The leading <code>$if</code> of each preprocessor statement block must 
		be preceded by BEGIN comment brackets.</li>
	<li>Within a preprocessor statement block, <code>$if</code>, 
		<code>$elseif</code>, <code>$else</code> and <code>$endif</code> 
		statements must be written on a single line preceded by no more than 
		whitespace and an optional BEGIN bracket.</li>
	</ul>
	<p>Note that anything after the second $ in a preprocessor statement is 
		discarded by the preprocessor.</p>
	<h3>Conditions</h3>
	<p>Preprocessor conditions are boolean expressions involving:</p>
	<ul>
		<li>The names of variables declared in a &lt;var> element,</li>
		<li>The boolean constants <code>true</code> and <code>false</code>,</li>
		<li>Non-negative numbers, beginning with a digit 0-9 and containing 
			digits and up to two decimal points ('.'),</li>
		<li>String constants delimited by " or ' characters,</li>
		<li>The comparison operators ==, !=, &lt;, &lt;=, > and >=,</li>
		<li>The boolean binary operators &amp;&amp; and ||, and the unary ! 
			(not) operator, and</li>
		<li>Left and right parenthesis ( ).</li>
	</ul>
	<p>There are three types of primitive values: boolean, numeric and string. 
		Only values with like types can be compared; there is no value 
		promotion or coercion.</p>
	<p>Boolean values are compared by <code>false</code> &lt; 
		<code>true</code>.</p>
	<p>String values are compared using UTF-16 numeric ordering (Java 
		comparison).</p>
	<p>Numeric values are compared by comparing the corresponding numeric 
		values at each hierarchical level for up to three levels. For example, 
		1 &lt; 2 &lt; 2.1 &lt; 2.3 &lt; 2.3.1, etc. 3 == 3.0 == 3.0.0. This 
		allows both standard integer and decimal comparisons while integrating 
		version number comparison in a natural way. Two numbers with no decimal 
		points compare like non-negative integers; two numbers with one decimal 
		point compare like non-negative decimals; and two numbers with two 
		decimal points each compare like, e.g., Eclipse version numbers.</p>
	<p>The complete expression syntax is:</p>
	<pre>
	condition ::= compare (('&amp;&amp;' | '||') compare)*
	compare ::= term (('==' | '!=' | '&lt;' | '&lt;=' | '>' | '>=') term)*
	term ::= '!'? primary
	primary ::= number | string | 'true' | 'false' | '(' condition ')'
	</pre>
	<p>Binary operators associate to the left and there is no precedence order 
		between &amp;&amp; and ||. Thus, <code>a||b&amp;&amp;c</code> equals 
		<code>(a||b)&amp;&amp;c</code> and <code>1&lt;2&lt;true</code> equals 
		<code>(1&lt;2)&lt;true</code> (which is false). Full parenthesization 
		will clarify your intentions and increase maintainability.</p>
	<h3>Acknowledgement</h3>
	<p>The idea for a preprocessor as a comment transformer 
		came from the CodePro Preprocessor from <a 
		href="http://www.instantiations.com/">Instantiations</a>.</p>
 * @author Bob Foster
 */
public class PreprocessorTask extends Task {

	private HashMap fTypeMap;
	private HashSet fExcludeSet;
	private String fExclude;
	private HashMap fVarMap;
	private String fOut;
	private LinkedList fVars = new LinkedList();
	private LinkedList fTypes = new LinkedList();
	private File fOutDirFile;
	private File fInDirFile;
	
	public PreprocessorTask() {
		setTaskName("com.objfac.preprocess");
	}
	
	public void setIndir(File inDirFile) {
		fInDirFile = inDirFile;
	}
	
	public void setOutdir(File outDirFile) {
		fOutDirFile = outDirFile;
	}
	
	public void setOut(String out) {
		fOut = out;
	}
	
	public void setExcept(String string) {
		fExclude = string;
	}

	public void addVar(Var var) {
		fVars.add(var);
	}
	
	public void addFiletype(Filetype type) {
		fTypes.add(type);
	}
	
	/**
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException {
		verifyAttributes();
		String out = fOut;
		Preprocessor preprocessor = new Preprocessor();
		try {
			int processed = preprocessor.preprocess(
				fInDirFile, fOutDirFile, 
				fVarMap, fExcludeSet, fTypeMap,
				"replace".equals(out), "merge".equals(out));
			int modified = preprocessor.getModCount();
			log("modified "+modified+" of "+processed+" files");
		} catch (PreprocessorError e) {
			String msg;
			Throwable t = e.getCause();
			if (t != null) {
				msg = "preprocessor unexpected error: "+t.toString();
			}
			else {
				msg = e.getMessage();
				FileLocator locator = e.getLocator();
				if (locator != null) {
					msg += " at line "+locator.getLineNumber()+" in file "+locator.getFileName();
				}
			}
			throw new BuildException(msg);
		}
	}

	private void verifyAttributes() {
		Project project = getProject();
		if (fInDirFile == null)
			throw new BuildException("indir attribute (input directory) must be specified");
		if (!fInDirFile.exists())
			throw new BuildException("indir directory does not exist");
		if (!fInDirFile.isDirectory())
			throw new BuildException("indir is not a directory");
			
		if (fOut == null) {
			fOut = "create";
		}
		else if (!"create".equals(fOut) && !"replace".equals(fOut) && !"merge".equals(fOut))
			throw new BuildException("out attribute must have value \"create\", \"replace\" or \"merge\"");
		
		if (fOutDirFile == null)
			throw new BuildException("outdir attribute (output directory) must be specified");
		if (fOutDirFile.exists()) {
			if (!fOutDirFile.isDirectory())
				throw new BuildException("outdir is not a directory");
			if ("create".equals(fOut))
				throw new BuildException("cannot create existing directory "+fOutDirFile.getAbsolutePath());
		}
		//??? project is null at this point so can't log - how to fix?
		//if (fVars.size() == 0)
		//	log("Warning - no preprocessor variables specified");
		fVarMap = new HashMap(fVars.size());
		Iterator vit = fVars.iterator();
		while (vit.hasNext()) {
			Var var = (Var) vit.next();
			String name = var.getName();
			String valu = var.getValue();
			if (name == null)
				throw new BuildException("var must specify name attribute, e.g., <var name=\"name\" value=\"value\">");
			if (valu == null)
				throw new BuildException("var must specify value attribute, e.g., <var name=\"name\" value=\"value\">");
			fVarMap.put(name, valu);
		}
		
		fExcludeSet = new HashSet();
		if (fExclude != null) {
			scanCsv(fExclude, fExcludeSet);
		}
		fTypeMap = new HashMap();
		/*
		Filetype jtype = new Filetype();
		jtype.setName("Java");
		jtype.setExtensions("java");
		jtype.setOutextension("java");
		jtype.setCommentbegin("/*");
		jtype.setCommentend("* /");
		fTypeMap.put("java", jtype);
			
		Filetype xtype = new Filetype();
		xtype.setName("XML");
		xtype.setExtensions("xml");
		xtype.setOutextension("xml");
		xtype.setCommentbegin("<!--");
		xtype.setCommentend("-->");
		fTypeMap.put("xml", xtype);
		*/
		if (fTypes.size() > 0) {
			Iterator it = fTypes.iterator();
			while (it.hasNext()) {
				Filetype ft = (Filetype) it.next();
				String name = ft.getName();
				String exts = ft.getExtensions();
				String beg = ft.getCommentbegin();
				String end = ft.getCommentend();
				String outs = ft.getOutextensions();
				String out = ft.getOutextension();
				if (name == null)
					name = "unnamed";
				if (exts == null)
					throw new BuildException(name+" filetype missing extensions attribute");
				if (beg == null)
					throw new BuildException(name+" filetype missing commentbegin attribute");
				if (beg.length() == 0)
					throw new BuildException(name+" filetype commentbegin attribute empty value");
				if (end == null)
					throw new BuildException(name+" filetype missing commentend attribute");
				if (end == null)
					throw new BuildException(name+" filetype commentend attribute empty value");
				if (out != null && outs != null)
					throw new BuildException(name+" filetype outextensions and outextension cannot both be specified");
				else if (out == null && outs == null)
					outs = exts;
				else if (outs == null)
					outs = out;
				
				LinkedList extlist = new LinkedList();
				scanCsv(exts, extlist);
				if (extlist.size() == 0)
					throw new BuildException(name+" filetype extensions attribute empty value");
				LinkedList outlist = new LinkedList();
				scanCsv(outs, outlist);
				if (outlist.size() == 0)
					throw new BuildException(name+" filetype outextension"+(out!=null?"":"s")+" attribute empty value");
				
				Iterator eit = extlist.iterator();
				Iterator oit = outlist.iterator();
				out = null;
				while (eit.hasNext()) {
					String ext = (String) eit.next();
					ext = checkExtension(ext, name);
					if (oit.hasNext()) {
						out = (String) oit.next();
						out = checkExtension(out, name);
						fTypeMap.remove(out);
					}
					fTypeMap.put(ext, ft.copy().setOutextension(out));
				}
			}
		}
		else
			throw new BuildException("No filetype specified");
	}
	
	private String checkExtension(String ext, String name) {
		if (ext.startsWith(".")) {
			if (ext.length() == 1)
				throw new BuildException(name+" filetype extensions attribute . is not a valid extension");
			ext = ext.substring(1);
		}
		else if (ext.startsWith("*.")) {
			if (ext.length() == 2)
				throw new BuildException(name+" filetype extensions attribute *. is not a valid extension");
			ext = ext.substring(2);
		}
		return ext;
	}
	
	private void scanCsv(String csv, Collection collection) {
		StringBuffer buf = new StringBuffer();
		int len = csv.length();
		for (int i = 0; i < len; i++) {
			char c = csv.charAt(i);
			if (c == ',' || c == ';') {
				if (buf.length() > 0) {
					String ex = buf.toString().trim();
					collection.add(ex);
					buf.setLength(0);
				}
			}
			else
				buf.append(c);
		}
		if (buf.length() > 0) {
			String ex = buf.toString().trim();
			collection.add(ex);
		}
	}
}
