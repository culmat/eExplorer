package com.github.culmat.eexplorer.views;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.part.ShowInContext;
import org.eclipse.ui.part.ViewPart;

import com.github.culmat.eexplorer.Activator;
import com.github.culmat.eexplorer.actions.CopyAction;
import com.github.culmat.eexplorer.actions.PasteAction;
import com.github.culmat.eexplorer.views.SyncWithDirectorySelectionListener.FileSelectionListener;
import com.github.culmat.eexplorer.views.UIBrowserAction.Icon;

import nu.bibi.breadcrumb.IMenuSelectionListener;
import nu.bibi.breadcrumb.MenuSelectionEvent;
import nu.bibi.breadcrumb.files.FileBreadcrumbViewer;
import nu.bibi.breadcrumb.files.ImageFileRegistry;

public class ExplorerView extends ViewPart implements FileSelectionListener, IShowInTarget, IPartListener2 {

	private File defaultFile;

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.github.culmat.eexplorer.views.ExplorerView";

	private static final String CONTEXT_ID = "com.github.culmat.eexplorer.context";

	private Browser browser;

	private SyncWithDirectorySelectionListener selectionListener;

	private FileBreadcrumbViewer breadcrumb;
	
	private PasteAction pasteAction;
	private CopyAction  copyAction;
	private boolean syncing = true;

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
		pasteAction = new PasteAction(Display.getDefault(),selectionListener,site.getWorkbenchWindow());
		registerKey(pasteAction);
		copyAction  = new CopyAction(Display.getDefault(),site.getWorkbenchWindow());
		registerKey(copyAction);
		IPartService iPartService = site.getWorkbenchWindow().getService(IPartService.class);
		iPartService.addPartListener(this);
	}
	
	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		if(!ID.equals(partRef.getId())) {
			return;
		}
		selectionListener.setEnabled(syncing);
	}
	
	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		if(!ID.equals(partRef.getId())) {
			return;
		}
		selectionListener.setEnabled(false);
	}

	@Override
	public void dispose() {	
		super.dispose();
		selectionListener.setEnabled(false);
		browser.dispose();
		pasteAction.dispose();
		copyAction.dispose();
	}

	@Override
	public void createPartControl(Composite parent) {
		IActionBars actionBars= getViewSite().getActionBars();
		actionBars.setGlobalActionHandler(
		      ActionFactory.COPY.getId(),
		      copyAction
		      );
		actionBars.setGlobalActionHandler(
				ActionFactory.PASTE.getId(),
				pasteAction
				);
		
		defaultFile =  ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		// --- Register context to separate keybindings form standard eclipse
		// (see plugin.xml)
		IContextService contextService = (IContextService) getSite().getService(IContextService.class);
		contextService.activateContext(CONTEXT_ID);

		final GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		parent.setLayout(layout);
		createBreadcrumb(parent);
		try {
			browser = new Browser(parent, SWT.NONE);
			browser.setUrl(defaultFile.toURI().toString());
		} catch (SWTError e) {
			System.out.println("Unable to open activeX control");
			return;
		}
		browser.setLayoutData(new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL));
		final IAction forwardAction = createForwardAction();
		final IAction backAction = createBackWardAction();
		browser.addLocationListener(new BrowserLocationListener() {
			@Override
			public void changed(File file, IStructuredSelection selection) {
				forwardAction.setEnabled(browser.isForwardEnabled());
				backAction.setEnabled(browser.isBackEnabled());
				setStatus();
				breadcrumb.setInput(file);
				breadcrumb.setSelection(selection, false);
			}
		});

	
		// TODO focus view when browser gets focus
		final IAction upAction = createUpAction();
		browser.addLocationListener(new BrowserLocationListener() {
			@Override
			public void changed(File file, IStructuredSelection selection) {
				upAction.setEnabled(file.getParentFile() != null);
			}
		});
		
		browser.addLocationListener(new BrowserLocationListener() {
			@Override
			public void changed(File file, IStructuredSelection selection) {
				copyAction.setClipboard(file);
				selectionListener.setLastSelection(file);
			}
		});
		
		registerActions(copyAction, pasteAction, backAction, upAction, forwardAction, createPopOutAction(), createCommandPromptAction(), createSyncAction(), createFileModeAction());
	}

	private void createBreadcrumb(Composite parent) {
		breadcrumb = new FileBreadcrumbViewer(parent, SWT.NONE);
		breadcrumb.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		breadcrumb.addMenuSelectionListener(new IMenuSelectionListener() {
			@Override
			public void menuSelect(final MenuSelectionEvent event) {
				final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				if (selection.isEmpty()) {
					return;
				}
				Object firstElement = selection.getFirstElement();
				breadcrumb.setInput(firstElement);
				breadcrumb.setSelection(selection, false);
				if (firstElement instanceof File) {
					select((File) firstElement);
				}
			}
		});

		breadcrumb.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(final DoubleClickEvent event) {
				Object element = breadcrumb.getSelection().getFirstElement();
				if (element == null) {
					return;
				}
				if (element instanceof File) {
					select((File) element);
				}

			}
		});

		breadcrumb.setRootVisible(false);
		breadcrumb.setInput(defaultFile);
	}

	private void registerActions(IAction... actions) {
		IToolBarManager toolBar = getViewSite().getActionBars().getToolBarManager();
		for (IAction action : actions) {
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
				syncing = checked;
			}
		};
	}
	
	private Action createFileModeAction() {
		return new Action("Toggle file mode", SWT.TOGGLE) {
			{
				try {
				URL url = new URL("platform:/plugin/org.eclipse.ui.ide/icons/full/obj16/welcome_editor.png");
				ImageDescriptor folder = ImageDescriptor.createFromURL(url );
				setImageDescriptor(folder);
				
				} catch (Exception e){}
			}

			@Override
			public void setChecked(boolean checked) {
				super.setChecked(checked);
				selectionListener.setFileMode(checked);
			}
		};
	}

	private IAction createBackWardAction() {
		return registerKey(new UIBrowserAction("Back", Icon.nav_backward) {
			{
				setEnabled(browser.isBackEnabled());
			}

			@Override
			public void run() {
				browser.back();
			}

		});
	}

	private IAction createUpAction() {
		return registerKey(new Action("Up") {
			{
				setImageDescriptor(Activator.getImageDescriptor("icons/nav_up.gif"));
				setDisabledImageDescriptor(Activator.getImageDescriptor("icons/nav_up_dis.gif"));
			}

			@Override
			public void run() {
				select(getBrowserLocation().getParentFile());
			}
		});
	}

	private File getBrowserLocation() {
		String url = browser.getUrl();
		try {
			return new File(new URI(url));
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private <T extends IAction> T registerKey(T action) {
		action.setActionDefinitionId("com.github.culmat.eexplorer.cmd." + action.getText().toLowerCase().replace(' ', '_'));
		IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
		handlerService.activateHandler(action.getActionDefinitionId(), new ActionHandler(action));
		return action;

	}

	private IAction createForwardAction() {
		return registerKey(new UIBrowserAction("Forward", Icon.nav_forward) {
			{
				setEnabled(browser.isForwardEnabled());
			}

			@Override
			public void run() {
				browser.forward();
			}
		});
	}

	private IAction createPopOutAction() {
		return registerKey(new Action("Open native explorer window") {
			{
				setImageDescriptor(ImageFileRegistry.getResource(ImageFileRegistry.KEY_FOLDER_DEFAULT));
			}
			
			LastRun lastRun = new LastRun();
			@Override
			public void run() {
				if(!lastRun.check()) return;
				Program.launch(browser.getUrl());
			}
		});
	}
	
	/**
	 * This is a hack 
	 */
	@Deprecated
	private class LastRun{
		long lastrun;
		private boolean check() {
			try {
				return System.currentTimeMillis()-lastrun > 100;
			} finally {
				lastrun = System.currentTimeMillis();
			}
		}
	}
	
	private IAction createCommandPromptAction() {
		return registerKey(new Action("Open command prompt") {
			{
				setImageDescriptor(Activator.getImageDescriptor("icons/command_prompt.gif"));
			}
			LastRun lastRun = new LastRun();
			@Override
			public void run() {
				if(!lastRun.check()) return;
				try {
					File file = new File(new URI(browser.getUrl()));
					if(file.isFile()) file = file.getParentFile();
					Runtime.getRuntime().exec(String.format("cmd /C start /D \"%s\" cmd.exe /K", file));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void setFocus() {
		browser.setFocus();
	}

	@Override
	public void select(File selection) {
		browser.setUrl(selection.toURI().toString());
		copyAction.setClipboard(selection);
	}

	@Override
	public boolean show(ShowInContext context) {
		ISelection sel = context.getSelection();
		if (sel instanceof IStructuredSelection) {
			return selectionListener.show((IStructuredSelection) sel);
		}
		if (sel instanceof ITextSelection) {
			ITextSelection textSel = (ITextSelection) sel;
			File detected = FileDetector.detect(textSel.getText());
			if(detected !=null) {
				return selectionListener.notifyListener(detected);
			}
		}
		return false;
	}

	private void setStatus() {
		IStatusLineManager statusLine = ExplorerView.this.getViewSite().getActionBars().getStatusLineManager();
		String absolutePath = getBrowserLocation().getAbsolutePath();
		statusLine.setMessage(URLDecoder.decode(absolutePath));
	}

}