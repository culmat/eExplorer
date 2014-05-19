package com.github.culmat.eexplorer.views;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;

public class UIBrowserAction extends Action {
	public static enum Icon {
		nav_backward, 
		nav_forward, 
		nva_go, 
		nav_home, 
		nav_print, 
		nav_refresh, 
		nav_stop, 
		synced;
	}
	public UIBrowserAction(String text, Icon icon) {
		this(text, SWT.PUSH, icon);
	}
	public UIBrowserAction(String text, int style, Icon icon) {
		super(text,style);
		try {
			String imgDisabled = "platform:/plugin/org.eclipse.ui.browser/icons/dlcl16/"+icon+".gif";
			setDisabledImageDescriptor(ImageDescriptor.createFromURL(new URL(imgDisabled)));
			String imgEnabled = "platform:/plugin/org.eclipse.ui.browser/icons/elcl16/"+icon+".gif";
			setImageDescriptor(ImageDescriptor.createFromURL(new URL(imgEnabled)));
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
	}
}
