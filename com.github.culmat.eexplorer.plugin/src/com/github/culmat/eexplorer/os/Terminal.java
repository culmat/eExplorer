package com.github.culmat.eexplorer.os;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Terminal {
	private Terminal() {}
	
	private static String lazyLinuxTerminal;
	
	private static String getLinuxTerminal() throws IOException {
		if(lazyLinuxTerminal == null) {
			lazyLinuxTerminal = detectLinuxTerminal();
		}
		return lazyLinuxTerminal;
	}

	private static synchronized String detectLinuxTerminal() throws IOException {
		// thanks Anndre for https://github.com/anb0s/EasyShell/blob/master/plugin/src/de/anbos/eclipse/easyshell/plugin/preferences/CommandDataDefaultCollectionLinux.java
		for (String candidate : asList(
				"gnome-terminal --working-directory=%s",
				"konsole --workdir %s",
				"xfce4-terminal --working-directory=%s",
				"mate-terminal --working-directory=%s",
				"lxterminal --working-directory=%s",
				"sakura --working-directory=%s",
				"roxterm --title=${easyshell:project_name} --directory=%s",
				"pantheon-terminal --working-directory=%s",
				"guake --show --execute-command=\"cd '%s'\"",
				"terminology --current-directory=%s"
				)) {
			Process process = Runtime.getRuntime().exec("which "+ candidate.split("\\s", 2)[0]);
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = reader.readLine();
			reader.close();
			if(line != null) return candidate;
		}
		throw new IllegalStateException("linux terminal detection failed");
	}
	
	public static void launch(File file) throws IOException {
		file = file.getCanonicalFile();
		final File dir = file.isDirectory() ? file : file.getParentFile();
		switch (OperatingSystem.CURRENT) {
		case WINDOWS:
			Runtime.getRuntime().exec(format("cmd /C start /D \"%s\" cmd.exe /K", dir), null, dir);
			break;
		case LINUX:
			Runtime.getRuntime().exec(format(getLinuxTerminal(), dir), null, dir);
			break;
		case MAC:
			Runtime.getRuntime().exec(format("open -a Terminal %s", dir), null, dir);
			break;
		default:
			throw new IllegalStateException("operating system detection failed");
		}
	}
}
