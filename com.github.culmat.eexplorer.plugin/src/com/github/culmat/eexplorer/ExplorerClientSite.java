package com.github.culmat.eexplorer;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleClientSite;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Composite;

public class ExplorerClientSite extends OleClientSite {
	private OleAutomation auto;
	final int navigate;
	final int locationURL;

	public ExplorerClientSite(Composite parent) {
		super(parent, SWT.NONE, "Shell.Explorer.1");
		doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
		auto = new OleAutomation(this);
		navigate = property(auto, "Navigate");
		locationURL = property(auto, "LocationURL");
	}

	public void navigate(File selection) {
		try {
			auto.invoke(navigate, new Variant[] { new Variant(selection.getCanonicalPath()) });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getLocationURL() {
		return auto.getProperty(locationURL).getString();
	}

	private static int property(OleAutomation auto, String name) {
		return auto.getIDsOfNames(new String[] { name })[0];
	}
}
