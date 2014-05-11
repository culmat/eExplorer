package com.github.culmat.eexplorer.views;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleClientSite;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import com.github.culmat.eexplorer.views.SyncWithDirectorySelectionListener.FileSelectionListener;

public class ExplorerView extends ViewPart implements FileSelectionListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.github.culmat.eexplorer.views.ExplorerView";

	private OleClientSite site;

	private SyncWithDirectorySelectionListener selectionListener;

	private OleAutomation auto;

	static final int Navigate = 0x68;

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		selectionListener = new SyncWithDirectorySelectionListener(site.getWorkbenchWindow(), this);
		Display.getDefault().asyncExec(new Runnable()
		{
		  @Override
		  public void run()
		  {
			  selectionListener.setEnabled(true);
		  }
		});
	}
	
	@Override
	public void dispose() {
		super.dispose();
		selectionListener.setEnabled(false);
	}
	

	@Override
	public void createPartControl(Composite parent) {
		try {
			OleFrame frame = new OleFrame(parent, SWT.NONE);
			site = new OleClientSite(frame, SWT.NONE, "Shell.Explorer.1");
			site.doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
			auto = new OleAutomation(site);
			auto.invoke(Navigate, new Variant[] { new Variant("c:\\") });
		} catch (SWTError e) {
			System.out.println("Unable to open activeX control");
			return;
		}
		registerActions(createSyncAction());
	}

	private void registerActions(Action ... actions) {
		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager dropDownMenu = actionBars.getMenuManager();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		for (Action action : actions) {
			dropDownMenu.add(action);
			toolBar.add(action);
		}
	}

	private Action createSyncAction() {
		Action syncAction = new Action("Link with Package Explorer", SWT.TOGGLE) {
			{
				try {
					String imgDisabled = "platform:/plugin/org.eclipse.ui.browser/icons/dlcl16/synced.gif";
					setDisabledImageDescriptor(ImageDescriptor.createFromURL(new URL(imgDisabled)));
					String imgEnabled= "platform:/plugin/org.eclipse.ui.browser/icons/elcl16/synced.gif";
					setImageDescriptor(ImageDescriptor.createFromURL(new URL(imgEnabled)));
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
			}
			
			@Override
			public void setChecked(boolean checked) {
				super.setChecked(checked);
				selectionListener.setEnabled(checked);
			}
		};
		syncAction.setChecked(true);
		return syncAction;
	}

	@Override
	public void setFocus() {
		site.setFocus();
	}

	@Override
	public void select(File selection) {
		try {
			auto.invoke(Navigate, new Variant[] { new Variant(selection.getCanonicalPath()) });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}