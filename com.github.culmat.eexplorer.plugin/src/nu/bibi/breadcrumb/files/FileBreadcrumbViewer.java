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

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import nu.bibi.breadcrumb.BreadcrumbViewer;

/**
 * File Breadcrumb Viewer.
 * <p>
 * This breadcrumb displays the system files with "My Computer" as input. User
 * can set the input with a given {@link File}.
 * </p>
 * The default values for filling, displaying and comparing values are:
 * <ul>
 * <li>Content provider: {@link FileContentProvider#DEFAULT}</li>
 * <li>Label provider: {@link FileLabelProvider}</li>
 * <li>Viewer comparator: {@link FileViewerComparator#DEFAULT}</li>
 * </ul>
 * 
 * @see Computer
 * @see File
 * @author Laurent Muller
 * @version 1.0
 */
public class FileBreadcrumbViewer extends BreadcrumbViewer {

	/*
	 * the image file registry
	 */
	private final ImageFileRegistry registry;

	/**
	 * Create a new <code>FileBreadcrumbViewer</code>.
	 * <p>
	 * Style is one of:
	 * <ul>
	 * <li>SWT.NONE</li>
	 * <li>SWT.VERTICAL</li>
	 * <li>SWT.HORIZONTAL</li>
	 * </ul>
	 * 
	 * @param parent
	 *            the container for the viewer
	 * @param style
	 *            the style flag used for this viewer
	 */
	public FileBreadcrumbViewer(final Composite parent, final int style) {
		super(parent, style);

		registry = new ImageFileRegistry(parent.getDisplay());
		parent.addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				registry.dispose();
			}
		});
		
		setLabelProvider(new FileLabelProvider(registry));
		setContentProvider(new FileContentProvider());
		setComparator(FileViewerComparator.DEFAULT);
		setToolTipLabelProvider(getLabelProvider());
	}

	/**
	 * Gets the image file registry.
	 * 
	 * @return the image file registry.
	 */
	public ImageFileRegistry getImageFileRegistry() {
		return registry;
	}

	/*
	 * (non-Javadoc)
	 * @see nu.bibi.breadcrumb.BreadcrumbViewer#configureDropDownViewer(org.eclipse.jface.viewers.TreeViewer, java.lang.Object)
	 */
	@Override
	protected void configureDropDownViewer(final TreeViewer viewer,
			final Object input) {
		// copy values
		viewer.setContentProvider(getContentProvider());
		viewer.setLabelProvider(getLabelProvider());
		viewer.setComparator(getComparator());
		viewer.setFilters(getFilters());
		viewer.setSelection(getSelection());
		ColumnViewerToolTipSupport.enableFor(viewer);
	}
}
