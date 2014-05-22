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
package nu.bibi.breadcrumb;

import java.util.EventObject;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;

/**
 * Event object describing a menu drop down selection which may be generated
 * from a menu drop down selection event. The source of these events is a
 * breadcrumb viewer.
 * 
 * @see IMenuSelectionListener
 */
public class MenuSelectionEvent extends EventObject {

	/**
	 * Generated serial version UID for this class.
	 */
	private static final long serialVersionUID = 8003421105282099099L;

	/**
	 * The selection.
	 */
	protected ISelection selection;

	/**
	 * Creates a new event for the given source and selection.
	 * 
	 * @param source
	 *            the viewer.
	 * @param selection
	 *            the selection.
	 */
	public MenuSelectionEvent(final Viewer source, final ISelection selection) {
		super(source);
		Assert.isNotNull(selection);
		this.selection = selection;
	}

	/**
	 * Returns the selection.
	 * 
	 * @return the selection.
	 */
	public ISelection getSelection() {
		return selection;
	}

	/**
	 * Returns the viewer that is the source of this event.
	 * 
	 * @return the originating viewer.
	 */
	public Viewer getViewer() {
		return (Viewer) getSource();
	}
}
