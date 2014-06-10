package com.github.culmat.eexplorer.views;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;

public abstract class BrowserLocationListener implements LocationListener {
	
	@Override
	public void changing(LocationEvent event) {
	}

	@Override
	public void changed(LocationEvent event) {
		if (!event.top)
			return;
		try {
			File file = new File(new URI(event.location));
			IStructuredSelection selection = new StructuredSelection(file);
			changed(file, selection);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	public abstract void changed(File file, IStructuredSelection selection);

}
