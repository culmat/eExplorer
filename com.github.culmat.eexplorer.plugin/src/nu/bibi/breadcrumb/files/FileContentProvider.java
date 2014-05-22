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
import java.util.Arrays;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * File content provider.
 * 
 * @author Laurent Muller
 * @version 1.0
 */
public class FileContentProvider implements ITreeContentProvider {
//
//	/**
//	 * The default file content provider instance.
//	 */
//	public final static FileContentProvider DEFAULT = new FileContentProvider();

	/*
	 * the viewer
	 */
	private StructuredViewer viewer;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
	public Object[] getChildren(final Object parentElement) {
		// computer ?
		if (parentElement instanceof Computer) {
			return ((Computer) parentElement).getDrives();
		}

		// file ?
		if (parentElement instanceof File) {
			// get children
			final File file = (File) parentElement;
			Object[] elements = file.listFiles();

			// apply filters
			elements = applyFilters(parentElement, elements);

			// sort
			Arrays.sort(elements);

			return elements;
		}

		// no data
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	@Override
	public Object[] getElements(final Object inputElement) {
		return getChildren(inputElement);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
	public Object getParent(final Object element) {
		if (element instanceof File) {
			final File file = (File) element;
			if (FileUtils.isDriveFile(file)) {
				return Computer.getInstance();
			}
			return file.getParentFile();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
	public boolean hasChildren(final Object element) {
		return this.getChildren(element).length > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput,
			final Object newInput) {
		if (viewer != null && viewer instanceof StructuredViewer) {
			this.viewer = (StructuredViewer) viewer;
		} else {
			this.viewer = null;
		}
	}

	/**
	 * Filters the given elements with all the filters, if any, of the current
	 * viewer. This method must never return <code>null</code>.
	 * 
	 * @param parent
	 *            the parent element.
	 * @param elements
	 *            the elements to filter.
	 * @return the filtered elements or an empty array if no one element is
	 *         selected.
	 * @see StructuredViewer#getFilters()
	 * @see ViewerFilter#filter(Viewer, Object, Object[])
	 */
	protected Object[] applyFilters(final Object parentElement,
			Object[] elements) {
		// data ?
		if (elements == null || elements.length == 0) {
			return new Object[0];
		}		
		if (viewer == null) {
			return elements;
		}

		// get filters
		final ViewerFilter[] filters = viewer.getFilters();
		if (filters == null || filters.length == 0) {
			return elements;
		}

		// apply each filter
		for (final ViewerFilter current : filters) {
			elements = current.filter(viewer, parentElement, elements);
			if (elements == null || elements.length == 0) {
				return new Object[0];
			}
		}
		return elements;
	}
}
