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
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * Directory viewer filter. When this filter is applied, only drive files (
 * <code>File.getParent() ==  null</code>) and directory files (
 * <code>File.isDirectory()</code>) are displayed.
 * 
 * @author Laurent Muller
 * @version 1.0
 */
public class DirectoryFilter extends ViewerFilter {

	/**
	 * The default directory filter instance.
	 */
	public final static DirectoryFilter INSTANCE = new DirectoryFilter();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(final Viewer viewer, final Object parentElement,
			final Object element) {
		// get file
		if (element instanceof File) {
			final File file = (File) element;
			// drive ?
			if (file.getParent() == null) {
				return true;
			}

			// directory ?
			if (file.isDirectory()) {
				return true;
			}

			// file
			return false;
		}

		// other
		return true;
	}
}
