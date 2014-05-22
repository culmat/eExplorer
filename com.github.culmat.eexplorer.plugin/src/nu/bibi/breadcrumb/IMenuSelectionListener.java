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

/**
 * A listener which is notified of menu drop down selection events on breadcrumb
 * viewers.
 */
public interface IMenuSelectionListener {

	/**
	 * Notifies of a menu drop down selection event.
	 * 
	 * @param event
	 *            event object describing the menu drop down selection event.
	 */
	public void menuSelect(MenuSelectionEvent event);
}
