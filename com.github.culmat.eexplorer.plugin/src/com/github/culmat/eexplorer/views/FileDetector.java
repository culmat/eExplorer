package com.github.culmat.eexplorer.views;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileDetector {
	static Pattern pattern = Pattern.compile("(?i)([\\w]\\:|\\\\)(\\\\[a-z_\\-\\s0-9\\.]+)+");

	public static File detect(String haystack) {
		Matcher matcher = pattern.matcher(haystack);
		while (matcher.find()) {
			File candidate = new File(matcher.group().trim());
			if (candidate.exists())
				return candidate;
		}
		return null;
	}
}
