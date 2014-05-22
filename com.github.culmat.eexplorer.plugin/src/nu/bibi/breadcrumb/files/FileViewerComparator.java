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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * File Viewer Comparator. This comparator uses the following categories:
 * <ul>
 * <li><code>0 - Computer</code></li>
 * <li><code>1 - Drive (File.getParent() ==  null)</code></li>
 * <li><code>2 - Directory (File.isDirectory())</code></li>
 * <li><code>3 - File</code></li>
 * <li><code>4 - Other objects</code></li>
 * </ul>
 * If the two categories are equals; this comparator uses the file absolute path
 * property (<code>File.getAbsolutePath()</code> to compare values; ignoring
 * case differences.
 * 
 * @author Laurent Muller
 * @version 1.0
 */
public class FileViewerComparator extends ViewerComparator {

	/**
	 * The default file viewer comparator instance.
	 */
	public final static FileViewerComparator DEFAULT = new FileViewerComparator();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
	 */
	@Override
	public int category(final Object element) {
		if (element instanceof Computer) {
			// computer
			return 0;
		}
		if (element instanceof File) {
			final File file = (File) element;
			if (file.getParent() == null) {
				// drive
				return 1;
			}
			if (file.isDirectory()) {
				// directory
				return 2;
			}
			// file
			return 3;
		}

		// other
		return 4;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(final Viewer viewer, final Object e1, final Object e2) {
		// compare categories
		final int cat1 = category(e1);
		final int cat2 = category(e2);
		if (cat1 != cat2) {
			return cat1 - cat2;
		}

		// compare files
		if (e1 instanceof File && e2 instanceof File) {
			final File file1 = (File) e1;
			final File file2 = (File) e2;
			if (file1 == file2) {
				return 0;
			}
			if (file1 == null) {
				return -1;
			}
			if (file2 == null) {
				return 1;
			}
			return file1.compareTo(file2); 
//			.getAbsolutePath().compareToIgnoreCase(file2.getAbsolutePath());
		}

		// default
		return super.compare(viewer, e1, e2);
	}
}
