package com.github.culmat.eexplorer.os;
public enum OperatingSystem {
	LINUX, MAC, WINDOWS, OTHER;
	public final static OperatingSystem CURRENT = detect();

	private static OperatingSystem detect() {
		String OS = System.getProperty("os.name").toLowerCase();
		if (OS.indexOf("win") >= 0) return WINDOWS;
		if (OS.indexOf("mac") >= 0) return MAC;
		if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 ) return LINUX;
		return OTHER;
	}
}

