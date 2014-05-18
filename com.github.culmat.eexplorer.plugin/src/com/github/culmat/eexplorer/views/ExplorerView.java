package com.github.culmat.eexplorer.views;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;

import com.github.culmat.eexplorer.Activator;
import com.github.culmat.eexplorer.ExplorerClientSite;
import com.github.culmat.eexplorer.views.SyncWithDirectorySelectionListener.FileSelectionListener;

public class ExplorerView extends ViewPart implements FileSelectionListener, IShowInTarget {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.github.culmat.eexplorer.views.ExplorerView";

	private ExplorerClientSite site;

	private SyncWithDirectorySelectionListener selectionListener;

	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		selectionListener = new SyncWithDirectorySelectionListener(site.getWorkbenchWindow(), this);
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
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
			site = new ExplorerClientSite(frame);
			site.navigate(new File("c:\\"));
		} catch (SWTError e) {
			System.out.println("Unable to open activeX control");
			return;
		}
		registerActions(createPopOutAction(), createSyncAction());
	}

	private void registerActions(Action... actions) {
		IToolBarManager toolBar = getViewSite().getActionBars().getToolBarManager();
		for (Action action : actions) {
			toolBar.add(action);
		}
	}

	private Action createSyncAction() {
		Action syncAction = new Action("Link with Package Explorer", SWT.TOGGLE) {
			{
				try {
					String imgDisabled = "platform:/plugin/org.eclipse.ui.browser/icons/dlcl16/synced.gif";
					setDisabledImageDescriptor(ImageDescriptor.createFromURL(new URL(imgDisabled)));
					String imgEnabled = "platform:/plugin/org.eclipse.ui.browser/icons/elcl16/synced.gif";
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

	private Action createPopOutAction() {
		Action ret = new Action("Open native explorer window") {
			{
				setImageDescriptor(Activator.getImageDescriptor("icons/Windows_Explorer_Icon_16x16.png"));
			}

			@Override
			public void run() {
				Program.launch(site.getLocationURL());
			}
		};
		return ret;
	}

	@Override
	public void setFocus() {
		site.setFocus();
	}

	@Override
	public void select(File selection) {
		site.navigate(selection);

	}

	@Override
	public boolean show(ShowInContext context) {
		ISelection sel = context.getSelection();
		if (sel instanceof IStructuredSelection) {
			return selectionListener.show((IStructuredSelection) sel);
		}
		return false;
	}

}