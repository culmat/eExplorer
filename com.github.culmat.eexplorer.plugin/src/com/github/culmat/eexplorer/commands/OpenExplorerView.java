package com.github.culmat.eexplorer.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.github.culmat.eexplorer.LogUtil;
import com.github.culmat.eexplorer.views.ExplorerView;


public class OpenExplorerView extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		try {
	        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	        String viewId = "com.github.culmat.eexplorer.views.ExplorerView";
	        if(viewId.equals(activePage.getActivePartReference().getId())) {
	        	((ExplorerView) activePage.getActivePart().getSite().getPart()).focusBreadcrump();
	        } else {
	        	activePage.activate(activePage.showView(viewId));
	        }
		} catch (PartInitException e) {
			showError(e);
	    } catch (Exception e) {
	    	showError(e);
	    }		
		return null;
	}

	private void showError(Exception e) {
		String title = "Exception while opening Explorer View";
		String message = title+" (com.github.culmat.eexplorer.views.ExplorerView)"
				+"\nCheck Error Log View and continue at https://github.com/culmat/eExplorer";
		LogUtil.error(message, e);
		MessageDialog.openError(Display.getDefault().getActiveShell(), title , message);
	}
}
