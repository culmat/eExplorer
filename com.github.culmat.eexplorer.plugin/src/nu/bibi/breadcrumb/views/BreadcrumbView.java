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
package nu.bibi.breadcrumb.views;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import nu.bibi.breadcrumb.IMenuSelectionListener;
import nu.bibi.breadcrumb.MenuSelectionEvent;
import nu.bibi.breadcrumb.files.Computer;
import nu.bibi.breadcrumb.files.DirectoryFilter;
import nu.bibi.breadcrumb.files.FileBreadcrumbViewer;
import nu.bibi.breadcrumb.internal.Messages;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * View with a FileBreadcrumbViewer.
 * 
 * @author Laurent Muller
 * @version 1.0
 */
public class BreadcrumbView extends ViewPart {

	/*
	 * The selected file property name
	 */
	private final static String P_FILE = "file"; //$NON-NLS-1$

	/*
	 * The display root property name
	 */
	private final static String P_ROOT = "root"; //$NON-NLS-1$

	/*
	 * the display directory only property name
	 */
	private final static String P_DIRECTORY = "directory"; //$NON-NLS-1$

	/*
	 * the initial input file
	 */
	private File inputFile;

	/*
	 * the root visible flag
	 */
	private boolean rootVisible;

	/*
	 * the display directory only flag
	 */
	private boolean directoryOnly;

	/*
	 * the viewer
	 */
	private FileBreadcrumbViewer viewer;

	/*
	 * the button to hide/show directories only
	 */
	private Button btnDirectory;

	/*
	 * the button to hide/show root element
	 */
	private Button btnRoot;

	/**
	 * Create a new instance of this class with default values.
	 */
	public BreadcrumbView() {
		super();
		rootVisible = false;
		directoryOnly = true;
		inputFile = getDefaultFile();

		final InputStream s = BreadcrumbView.class
				.getResourceAsStream("/icons/computer.png");
		if (s != null) {
			try {
				s.close();
			} catch (final IOException e) {
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(final Composite parent) {
		final GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		parent.setLayout(layout);

		viewer = new FileBreadcrumbViewer(parent, SWT.NONE);
		viewer.getControl().setLayoutData(
				new GridData(GridData.FILL_HORIZONTAL));

		viewer.addMenuSelectionListener(new IMenuSelectionListener() {
			@Override
			public void menuSelect(final MenuSelectionEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				if (selection.isEmpty()) {
					viewer.setFocus();
					return;
				}
				viewer.setInput(selection.getFirstElement());
				viewer.setSelection(selection, true);
				viewer.setFocus();
			}
		});

		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(final DoubleClickEvent event) {
				// get selection
				Object element = viewer.getSelection().getFirstElement();
				if (element == null) {
					return;
				}

				// get parent
				if (element instanceof File) {
					element = ((File) element).getParentFile();
				}
				if (element == null) {
					element = Computer.getInstance();
				}

				// open
				viewer.openDropDownMenu(element);
			}
		});

		viewer.setRootVisible(rootVisible);
		if (directoryOnly) {
			viewer.addFilter(DirectoryFilter.INSTANCE);
		}

		btnRoot = new Button(parent, SWT.CHECK);
		btnRoot.setText(Messages.BreadcrumbView_Root_Text);
		btnRoot.setSelection(rootVisible);
		final GridData gdRoot = new GridData();
		gdRoot.verticalIndent = 10;
		gdRoot.horizontalIndent = 5;
		btnRoot.setLayoutData(gdRoot);
		btnRoot.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				viewer.setRootVisible(btnRoot.getSelection());
			}
		});

		btnDirectory = new Button(parent, SWT.CHECK);
		btnDirectory.setText(Messages.BreadcrumbView_Directory_Text);
		btnDirectory.setSelection(directoryOnly);
		final GridData gdDir = new GridData();
		gdDir.horizontalIndent = 5;
		btnDirectory.setLayoutData(gdDir);
		btnDirectory.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				Object input = viewer.getInput();
				if (btnDirectory.getSelection()) {
					viewer.addFilter(DirectoryFilter.INSTANCE);
					if (input instanceof File) {
						final File file = (File) input;
						if (file.isFile()) {
							input = file.getParentFile();
						}
					}
				} else {
					viewer.removeFilter(DirectoryFilter.INSTANCE);
				}
				viewer.setInput(input);

			}
		});

		viewer.setInput(inputFile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	@Override
	public void init(final IViewSite site, final IMemento memento)
			throws PartInitException {
		super.init(site, memento);

		if (memento == null) {
			return;
		}

		final Boolean directory = memento.getBoolean(P_DIRECTORY);
		if (directory != null) {
			directoryOnly = directory;
		}
		final Boolean root = memento.getBoolean(P_DIRECTORY);
		if (root != null) {
			rootVisible = root;
		}

		// get file
		final String fileName = memento.getString(P_FILE);
		if (fileName != null) {
			final File file = new File(fileName);
			if (file.exists()) {
				inputFile = file;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.ViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	@Override
	public void saveState(final IMemento memento) {
		// save
		memento.putBoolean(P_DIRECTORY, btnDirectory.getSelection());
		memento.putBoolean(P_ROOT, btnRoot.getSelection());

		// Save the selected file
		final Object input = viewer.getInput();
		if (input == null) {
			return;
		}
		if (input instanceof File) {
			inputFile = (File) input;
			memento.putString(P_FILE, inputFile.getAbsolutePath());
		}
		super.saveState(memento);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	/*
	 * Gets the default file
	 */
	private File getDefaultFile() {
		// user home
		final String fileName = System.getProperty("user.home"); //$NON-NLS-1$
		if (fileName != null) {
			return new File(fileName);
		}
		// first root file
		return File.listRoots()[0];
	}
}