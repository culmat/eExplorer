package com.github.culmat.eexplorer.actions;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

public class CopyAction extends Action {
	final Clipboard cb;
	private String clipboardtext;
	public CopyAction(Display display, IWorkbenchWindow window) {
		cb = new Clipboard(display);
		setText("Copy");
		IWorkbenchAction tmp = ActionFactory.COPY.create(window);
		setImageDescriptor(tmp.getImageDescriptor());
		setDisabledImageDescriptor(tmp.getDisabledImageDescriptor());
		setHoverImageDescriptor(tmp.getHoverImageDescriptor());
	}
	
	@Override
	public void run() {
		if(clipboardtext == null) return;
		TextTransfer textTransfer = TextTransfer.getInstance();
        cb.setContents(new Object[] { clipboardtext },
            new Transfer[] { textTransfer });
	}
	
	public void setClipboardText(String clipboardtext){
		this.clipboardtext = clipboardtext;
	}

	public void dispose() {
		cb.dispose();
	}

	public void setClipboard(File file) {
		setClipboardText(file.getAbsolutePath());
	}
}
