/*******************************************************************************
 * Copyright (c) 2008 Laurent Muller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Laurent Muller - initial API and implementation
 *******************************************************************************/
package nu.bibi.breadcrumb.files;

import java.io.File;

import javax.swing.filechooser.FileSystemView;

/**
 * File utility
 * 
 * @author Laurent Muller
 */
public class FileUtils {

	/**
	 * Gets the drive name from the file system view.
	 * 
	 * @param file
	 *            the used to find the drive name.
	 * @return the drive name from the file system view if found, the name only
	 *         otherwise.
	 */
	public static String getDriveName(final File file) {
		// get the display name
		try {
			if (file.exists()) {
				final FileSystemView view = FileSystemView.getFileSystemView();
				final String name = view.getSystemDisplayName(file);
				if (name != null && name.length() != 0) {
					return name;
				}
			}
			// get the 2 first letters (e.g. C:)
			final String pathName = file.getAbsolutePath();
			if (pathName.length() <= 2) {
				return pathName;
			}
			return pathName.substring(0, 2);
		} catch (final Exception e) {
			return file.getName();
		}
	}

	/**
	 * Gets the drive type from the file system view.
	 * 
	 * @param file
	 *            the used to find the drive type.
	 * @return the drive type from the file system view if found,
	 *         <code>null</code> otherwise.
	 */
	public static String getDriveType(final File file) {
		try {
			final FileSystemView view = FileSystemView.getFileSystemView();
			return view.getSystemTypeDescription(file);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Gets a value indicating if the specified file is the drive file.
	 * 
	 * @param file
	 *            the file to be tested.
	 * @return <code>true</code> if the specified file is the drive file,
	 *         <code>false</code> otherwise.
	 */
	public static boolean isDriveFile(final File file) {
		if (file == null || file.getParentFile() != null) {
			return false;
		}
		return true;
	}

	/*
	 * prevent instance creation
	 */
	private FileUtils() {
	}
}
