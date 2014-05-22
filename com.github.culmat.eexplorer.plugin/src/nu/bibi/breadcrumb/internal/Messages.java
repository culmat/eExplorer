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
package nu.bibi.breadcrumb.internal;

import org.eclipse.osgi.util.NLS;

/**
 * @since 3.4
 */
public class Messages extends NLS {

	private static final String BUNDLE_NAME = "nu.bibi.breadcrumb.internal.messages"; //$NON-NLS-1$

	public static String BreadcrumbItemDropDown_Action_ToolTip;
	public static String BreadcrumbView_Directory_Text;

	public static String BreadcrumbView_Root_Text;

	public static String Computer_Name;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
