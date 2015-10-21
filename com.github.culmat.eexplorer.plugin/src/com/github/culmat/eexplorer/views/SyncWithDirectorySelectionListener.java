package com.github.culmat.eexplorer.views;

import static com.github.culmat.eexplorer.views.FileDetector.detect;

import java.io.File;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

public class SyncWithDirectorySelectionListener implements ISelectionListener {
	private static final String PACKAGE_EXPLORER_ID = "org.eclipse.jdt.ui.PackageExplorer";
	private static final String PROJECT_EXPLORER_ID = "org.eclipse.ui.navigator.ProjectExplorer";
	private static final String GIT_REPOSITORIES_VIEW_ID = "org.eclipse.egit.ui.RepositoriesView";
	private static final String[] IDS = new String[] { PROJECT_EXPLORER_ID, PACKAGE_EXPLORER_ID, GIT_REPOSITORIES_VIEW_ID };
	private boolean enabled;
	private final ISelectionService selectionService;
	private final FileSelectionListener listener;
	private File lastSelection;
	private boolean fileMode = false;
	private File lastNotify;

	public File getLastSelection() {
		return lastSelection;
	}

	public void setLastSelection(File lastSelection) {
		this.lastSelection = lastSelection;
	}

	public static interface FileSelectionListener {
		void select(File selection);
	}

	SyncWithDirectorySelectionListener(IWorkbenchWindow workbenchWindow, FileSelectionListener listener) {
		this.listener = listener;
		selectionService = workbenchWindow.getSelectionService();
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (!enabled)
			return;
		if (selection instanceof IStructuredSelection) {
			show((IStructuredSelection) selection);
		}
	}

	boolean show(IStructuredSelection sel) {
		if (sel == null)
			return false;
		@SuppressWarnings("rawtypes")
		Iterator iterator = sel.iterator();
		while (iterator.hasNext()) {
			Object item = iterator.next();
			if (item instanceof IAdaptable) {
				IResource resource = (IResource) ((IAdaptable) item).getAdapter(IResource.class);
				if (resource != null) {
					IPath location = resource.getLocation();
					if (location != null) {
						File file = location.toFile();
						notifyListener(file);
						return true;
					}
				}
				File fuzzy = detect(item.toString());
				if(fuzzy != null) {
					notifyListener(fuzzy);
					return true;
				}
			}
		}
		return false;
	}

	public boolean notifyListener(File file) {
		if (file == null)
			return false;
		lastNotify = file;
		if (!file.isDirectory() && !fileMode)
			file = file.getParentFile();
		if (file.equals(lastSelection))
			return false;
		if (!file.exists())
			return false;
		lastSelection = file;
		listener.select(file);
		return true;
	}

	public void setEnabled(boolean enabled) {
		if (enabled == this.enabled)
			return;
		this.enabled = enabled;
		if (enabled) {
			for (String id : IDS) {
				selectionService.addPostSelectionListener(id, this);
			}
			for (String id : IDS) {
				ISelection selection = selectionService.getSelection(id);
				if (selection != null) {
					selectionChanged(null, selection);
					break;
				}
			}
		} else {
			for (String id : IDS) {
				selectionService.removePostSelectionListener(id, this);
			}
		}
	}

	public void setFileMode(boolean fileMode) {
		this.fileMode = fileMode;
		if(lastNotify != null){
			notifyListener(lastNotify);
		}
	}
}