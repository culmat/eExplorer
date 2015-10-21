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
	enum Target {
		
		PACKAGE_EXPLORER_ID("org.eclipse.jdt.ui.PackageExplorer"),
		PROJECT_EXPLORER_ID("org.eclipse.ui.navigator.ProjectExplorer"),
		GIT_REPOSITORIES_VIEW_ID("org.eclipse.egit.ui.RepositoriesView");		
		
		final String id;
		private Target(String id) {
			this.id = id;
		}
	}
	
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
			for (Target target : Target.values()) {
				selectionService.addPostSelectionListener(target.id, this);
			}
			for (Target target : Target.values()) {
				ISelection selection = selectionService.getSelection(target.id);
				if (selection != null) {
					selectionChanged(null, selection);
					break;
				}
			}
		} else {
			for (Target target : Target.values()) {
				selectionService.removePostSelectionListener(target.id, this);
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