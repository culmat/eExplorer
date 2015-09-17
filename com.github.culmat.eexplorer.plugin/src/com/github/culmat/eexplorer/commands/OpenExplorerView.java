package com.github.culmat.eexplorer.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.github.culmat.eexplorer.LogUtil;


public class OpenExplorerView extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		try {
	        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	        String viewId = "com.github.culmat.eexplore.views.ExplorerView";
	        IViewPart view = activePage.showView(viewId);
	        activePage.activate(view);
		} catch (PartInitException e) {
			showError(e);
	    } catch (Exception e) {
	    	showError(e);
	    }		
		return null;
	}

	private void showError(Exception e) {
		String title = "Exception while opening GitHub Flavored Markdown View";
		String message = title+" (com.github.culmat.eexplore.views.ExplorerView)"
				+"\nCheck Error Log View and continue at https://github.com/culmat/eExplorer";
		LogUtil.error(message, e);
		MessageDialog.openError(Display.getDefault().getActiveShell(), title , message);
	}
}
