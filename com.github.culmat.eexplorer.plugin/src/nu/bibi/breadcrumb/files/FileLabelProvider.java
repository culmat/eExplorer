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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * File label provider
 * 
 * @author Laurent Muller
 * @version 1.0
 */
public class FileLabelProvider extends ColumnLabelProvider {

	/*
	 * the image file registry
	 */
	private final ImageFileRegistry registry;

	/**
	 * Create a new instance of this class with the given image file registry.
	 * 
	 * @param registry
	 *            the image registry used to display images.
	 */
	protected FileLabelProvider(final ImageFileRegistry registry) {
		this.registry = registry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang.Object)
	 */
	@Override
	public Image getImage(final Object element) {
		if (registry == null) {
			return null;
		}

		if (element instanceof Computer) {
			return registry.get(ImageFileRegistry.KEY_COMPUTER);
		}

		if (element instanceof File) {
			return registry.get((File) element);
		}
		return super.getImage(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(final Object element) {
		if (element instanceof Computer) {
			return ((Computer) element).getName();
		}

		if (element instanceof File) {
			final File file = (File) element;
			if (FileUtils.isDriveFile(file)) {
				return FileUtils.getDriveName(file);
			}
			return file.getName();
		}
		return super.getText(element);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
	 */
	@Override
	public String getToolTipText(final Object element) {
		if (element instanceof File) {
			final File file = (File) element;
			return file.getAbsolutePath();
		}
		return getText(element);
	}
}
