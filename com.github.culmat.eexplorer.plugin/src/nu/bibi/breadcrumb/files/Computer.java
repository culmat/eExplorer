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

import nu.bibi.breadcrumb.internal.Messages;

/**
 * This is the root item for the system files.
 * 
 * @author Laurent Muller
 * @version 1.0
 */
public class Computer {

	/*
	 * the singleton instance
	 */
	private static Computer instance;

	/**
	 * Returns the singleton instance.
	 * 
	 * @return the singleton instance.
	 */
	public static synchronized Computer getInstance() {
		if (instance == null) {
			instance = new Computer();
		}
		return instance;
	}

	/*
	 * Prevents instance creation
	 */
	private Computer() {
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || this.getClass() != obj.getClass()) {
			return false;
		}
		return true;
	}

	/**
	 * List the available file system roots.
	 * 
	 * @return An array of <code>File</code> objects denoting the available file
	 *         system roots, or <code>null</code> if the set of roots could not
	 *         be determined. The array will be empty if there are no file
	 *         system roots.
	 */
	public File[] getDrives() {
		final File[] files = File.listRoots();
		if (files == null) {
			return new File[0];
		}
		return files;
	}

	/**
	 * Gets the computer name.
	 * 
	 * @return the computer name.
	 */
	public String getName() {
		return Messages.Computer_Name;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}
}
