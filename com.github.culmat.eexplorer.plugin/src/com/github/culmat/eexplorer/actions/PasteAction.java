package com.github.culmat.eexplorer.actions;

import static com.github.culmat.eexplorer.views.FileDetector.detect;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

import com.github.culmat.eexplorer.views.FileDetector;
import com.github.culmat.eexplorer.views.SyncWithDirectorySelectionListener;

public class PasteAction extends Action {
	final Clipboard cb;
	private final SyncWithDirectorySelectionListener listener;

	public PasteAction(Display display, SyncWithDirectorySelectionListener listener, IWorkbenchWindow window) {
		this.listener = listener;
		cb = new Clipboard(display);
		setText("Paste");
		IWorkbenchAction tmp = ActionFactory.PASTE.create(window);
		setImageDescriptor(tmp.getImageDescriptor());
		setDisabledImageDescriptor(tmp.getDisabledImageDescriptor());
		setHoverImageDescriptor(tmp.getHoverImageDescriptor());
	}

	@Override
	public void run() {
		TextTransfer transfer = TextTransfer.getInstance();
		String data = (String) cb.getContents(transfer);
		if (data != null) {
			listener.notifyListener(detect(data));
		}
	}

	public void dispose() {
		cb.dispose();
	}

}
