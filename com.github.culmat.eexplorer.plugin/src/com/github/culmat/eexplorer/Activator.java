package com.github.culmat.eexplorer;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Scanner;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.github.culmat.eexplorer.os.OperatingSystem;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	private enum Preferences {
		NON_WINDOWS_OK;
	}

	// The plug-in ID
	public static final String PLUGIN_ID = "com.github.culmat.eexplorer.plugin"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public boolean isNonWindowsWarning() {
		return !OperatingSystem.CURRENT.equals(OperatingSystem.WINDOWS)
				&& !getPreferenceStore().getBoolean(Preferences.NON_WINDOWS_OK.name());
	}

	public void setNonWindowsWarning(boolean warn) {
		getPreferenceStore().setValue(Preferences.NON_WINDOWS_OK.name(), !warn);
	}

	public String getResourceAsString(String path) throws IOException {
		return read(getResource(path));
	}
	
	public URL getResource(String path) {
		return FileLocator.find(getBundle(), new Path("resources/"+path));
	}

	private String read(URL url) throws IOException {
		Scanner scanner = new Scanner(url.openStream(), Charset.forName("UTF-8"));
		try {
			return scanner.useDelimiter("\\A").next();
		} finally {
			try {
				scanner.close();
			} catch (Exception ignored) {}
		}
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative
	 * path
	 * 
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
