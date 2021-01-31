package com.github.culmat.eexplorer.views;

import static java.util.regex.Pattern.compile;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileDetector { 
	static String pathSeparatorMatcher = "[\\\\/]";
	
	static Pattern[] patterns = new Pattern[] {
			compile("(?i)([\\w]:|%s)?(%s[a-z_\\-0-9\\.\\s]+)+".replace("%s", pathSeparatorMatcher)),
			compile("(?i)([\\w]:|%s)?(%s[a-z_\\-0-9\\.]+)+".replace("%s", pathSeparatorMatcher))
			};
	
	public static File detect(String haystack) {
		for(Pattern pattern : patterns) {
			Matcher matcher = pattern.matcher(haystack);
			while (matcher.find()) {
				File candidate = new File(matcher.group().trim());
				if (candidate.exists())
					return candidate;
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		// run me with -ea or -ea:com.github.culmat... see https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html
		Pattern pattern = patterns[0];
		assert pattern.matcher("c:\\gugus\\bla").find();
		assert pattern.matcher("\\gugus\\bla").find();
		assert pattern.matcher("\\\\gugus\\bla").find();
		assert pattern.matcher("/gugus/bla").find();
		assert pattern.matcher("\\gugus\\bla ha\\ja.txt").find();
		assert pattern.matcher("/gugus/bla ha/ja.txt").find();
//		pattern would need to start with (?i)\s+		
//		assert !pattern.matcher("gugus/bla ha/ja.txt").find();
//		assert !pattern.matcher("https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html").find();
		System.out.println("assertions OK");
	}
	
}
 	