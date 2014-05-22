package com.github.culmat.eexplorer.views;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import nu.bibi.breadcrumb.IMenuSelectionListener;
import nu.bibi.breadcrumb.MenuSelectionEvent;
import nu.bibi.breadcrumb.files.FileBreadcrumbViewer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;

import com.github.culmat.eexplorer.Activator;
import com.github.culmat.eexplorer.views.SyncWithDirectorySelectionListener.FileSelectionListener;
import com.github.culmat.eexplorer.views.UIBrowserAction.Icon;

public class ExplorerView extends ViewPart implements FileSelectionListener, IShowInTarget {

	private final File FILE_DEFAULT = new File("c:");

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.github.culmat.eexplorer.views.ExplorerView";

	private Browser browser;

	private SyncWithDirectorySelectionListener selectionListener;

	private FileBreadcrumbViewer breadcrumb;

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
		browser.dispose();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		final GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		parent.setLayout(layout);
		createBreadcrumb(parent);
		try {
			browser = new Browser(parent, SWT.NONE);
			browser.setUrl(FILE_DEFAULT.toURI().toString());
		} catch (SWTError e) {
			System.out.println("Unable to open activeX control");
			return;
		}
		browser.setLayoutData(new GridData(GridData.FILL_VERTICAL|GridData.FILL_HORIZONTAL));
		final Action forwardAction = createForwardAction();
		final Action backAction = createBackWardAction();
		browser.addLocationListener(new LocationListener() {
			@Override
			public void changed(LocationEvent event) {
				if (!event.top)
					return;
				forwardAction.setEnabled(browser.isForwardEnabled());
				backAction.setEnabled(browser.isBackEnabled());
				try {
					File selection = new File(new URI(event.location));
					breadcrumb.setInput(selection);
					IStructuredSelection selection2 = new StructuredSelection(selection);
					breadcrumb.setSelection(selection2, false);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				setStatus();
			}

			@Override
			public void changing(LocationEvent event) {
			}
		});
		//TODO focus view when browser gets focus
		registerActions(backAction, forwardAction, createPopOutAction(), createSyncAction());
	}

	private void createBreadcrumb(Composite parent) {
		breadcrumb = new FileBreadcrumbViewer(parent, SWT.NONE);
		breadcrumb.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		breadcrumb.addMenuSelectionListener(new IMenuSelectionListener() {
			@Override
			public void menuSelect(final MenuSelectionEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				if (selection.isEmpty()) {
					return;
				}
				Object firstElement = selection.getFirstElement();
				breadcrumb.setInput(firstElement);
				breadcrumb.setSelection(selection, false);
				if(firstElement instanceof File) {
					select((File) firstElement);
				}
			}
		});

		breadcrumb.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(final DoubleClickEvent event) {
				// get selection
				Object element = breadcrumb.getSelection().getFirstElement();
				if (element == null) {
					return;
				}
				if (element instanceof File) {
					select((File)element);
				}
				
			}
		});

		breadcrumb.setRootVisible(false);
		breadcrumb.setInput(FILE_DEFAULT);
	}

	private void registerActions(Action... actions) {
		IToolBarManager toolBar = getViewSite().getActionBars().getToolBarManager();
		for (Action action : actions) {
			toolBar.add(action);
		}
	}

	private Action createSyncAction() {
		return new UIBrowserAction("Link with Package Explorer", SWT.TOGGLE, Icon.synced) {
			{
				setChecked(true);
			}
			@Override
			public void setChecked(boolean checked) {
				super.setChecked(checked);
				selectionListener.setEnabled(checked);
			}
		};
	}

	private Action createBackWardAction() {
		return  new UIBrowserAction("Back", Icon.nav_backward) {
			{
				setEnabled(browser.isBackEnabled());
			}

			@Override
			public void run() {
				browser.back();
			}
		};
	}

	private Action createForwardAction() {
		return new UIBrowserAction("Forward", Icon.nav_forward) {
			{
				setEnabled(browser.isForwardEnabled());
			}

			@Override
			public void run() {
				browser.forward();
			}
		};
	}

	private Action createPopOutAction() {
		Action ret = new Action("Open native explorer window") {
			{
				setImageDescriptor(Activator.getImageDescriptor("icons/Windows_Explorer_Icon_16x16.png"));
			}

			@Override
			public void run() {
				Program.launch(browser.getUrl());
			}
		};
		return ret;
	}

	@Override
	public void setFocus() {
		browser.setFocus();
	}

	@Override
	public void select(File selection) {
		browser.setUrl(selection.toURI().toString());
	}

	@Override
	public boolean show(ShowInContext context) {
		ISelection sel = context.getSelection();
		if (sel instanceof IStructuredSelection) {
			return selectionListener.show((IStructuredSelection) sel);
		}
		return false;
	}

	private void setStatus() {
		IStatusLineManager statusLine = ExplorerView.this.getViewSite().getActionBars().getStatusLineManager();
		String absolutePath = new File(browser.getUrl()).getAbsolutePath();
		statusLine.setMessage(URLDecoder.decode(absolutePath));
	}

}