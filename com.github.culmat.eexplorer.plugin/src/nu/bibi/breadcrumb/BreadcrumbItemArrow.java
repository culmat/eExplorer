/*******************************************************************************
 * Copyright (c) 2008 Laurent Muller.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Laurent Muller - initial API and implementation
 *******************************************************************************/
package nu.bibi.breadcrumb;

import nu.bibi.breadcrumb.files.ImageFileRegistry;
import nu.bibi.breadcrumb.internal.Messages;

import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * The drop-down part of the breadcrumb item.
 * 
 * @since 3.4
 */
class BreadcrumbItemArrow {

	/**
	 * A popup window used to display the drop down menu.
	 * 
	 * @author Laurent Muller
	 * @version 1.0
	 */
	private class DropDownWindow extends Window {

		private CLabel lblMessage;
		private SizeGrip grip;

		public DropDownWindow(final Shell shell) {
			super(shell);
			setShellStyle(SWT.NO_TRIM | SWT.TOOL | SWT.ON_TOP
					| SWT.DOUBLE_BUFFERED);
		}

		@Override
		public boolean close() {
			menuIsShown = false;
			dropDownViewer = null;
			dropDownWindow = null;
			imageElement.setImage(getArrowImage(false));
			return super.close();
		}

		@Override
		public int open() {
			// create shell
			Shell shell = getShell();
			if (shell == null || shell.isDisposed()) {
				shell = null;
				create();
				shell = getShell();
			}
			setShellBounds(shell);

			// select top index
			final int index = getViewer().indexOf(parentItem);
			if (index < getViewer().getItemCount() - 1) {
				final BreadcrumbItem childItem = getViewer().getItem(index + 1);
				final Object child = childItem.element;
				dropDownViewer.setSelection(new StructuredSelection(child),
						true);
				final Tree tree = dropDownViewer.getTree();
				final TreeItem[] selection = tree.getSelection();
				if (selection.length > 0) {
					tree.setTopItem(selection[0]);
					tree.notifyListeners(SWT.Selection, new Event());
				}
			}

			// open
			shell.open();
			return OK;
		}

		@Override
		protected void configureShell(final Shell newShell) {
			super.configureShell(newShell);
			newShell.addListener(SWT.Deactivate, new Listener() {
				@Override
				public void handleEvent(final Event event) {
					close();
				}
			});
		}

		@Override
		protected Control createContents(final Composite parent) {
			final Composite content = new Composite(parent, SWT.BORDER);
			content.setLayoutData(new GridData(GridData.FILL_BOTH));
			final GridLayout layout = new GridLayout();
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			layout.horizontalSpacing = 0;
			layout.verticalSpacing = 0;
			content.setLayout(layout);

			int style = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL;
			if (!isLTR()) {
				style |= SWT.RIGHT_TO_LEFT;
			}
			dropDownViewer = new TreeViewer(content, style);
			dropDownViewer.setUseHashlookup(true);

			final Tree tree = dropDownViewer.getTree();
			tree.setLayoutData(new GridData(GridData.FILL_BOTH));

			// separator
			final Label sep = new Label(content, SWT.SEPARATOR | SWT.HORIZONTAL);
			sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			// status line
			final Composite statusLine = new Composite(content, SWT.NONE);
			statusLine.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			final GridLayout slLayout = new GridLayout(2, false);
			slLayout.marginWidth = slLayout.marginHeight = 0;
			statusLine.setLayout(slLayout);

			if (isLTR()) {
				lblMessage = new CLabel(statusLine, SWT.NONE);
				lblMessage
						.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

				grip = new SizeGrip(statusLine, SWT.NONE);
				grip
						.setLayoutData(new GridData(SWT.END, SWT.END, false,
								false));
			} else {
				grip = new SizeGrip(statusLine, SWT.RIGHT_TO_LEFT);
				grip
						.setLayoutData(new GridData(SWT.END, SWT.END, false,
								false));

				lblMessage = new CLabel(statusLine, SWT.RIGHT_TO_LEFT);
				lblMessage
						.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			}

			final Object input = parentItem.element;
			getViewer().configureDropDownViewer(dropDownViewer, input);
			dropDownViewer.setInput(input);

			dropDownViewer.addOpenListener(new IOpenListener() {
				public void open(final OpenEvent event) {
					final ISelection selection = event.getSelection();
					if (!(selection instanceof IStructuredSelection)) {
						return;
					}

					final Object element = ((IStructuredSelection) selection)
							.getFirstElement();
					if (element == null) {
						return;
					}

					getViewer().fireMenuSelection(element);
					if (isShellDisposed()) {
						return;
					}

					if (dropDownViewer.getExpandedState(element)) {
						dropDownViewer.collapseToLevel(element, 1);
					} else {
						tree.setRedraw(false);
						try {
							dropDownViewer.expandToLevel(element, 1);
							resizeShell(getShell());
						} finally {
							tree.setRedraw(true);
						}
					}
				}
			});

			tree.addListener(SWT.MouseUp, new Listener() {
				@Override
				public void handleEvent(final Event event) {
					if (event.button != 1) {
						return;
					}

					final Item item = tree.getItem(new Point(event.x, event.y));
					if (item == null) {
						return;
					}

					final Object data = item.getData();
					if (data == null) {
						return;
					}

					getViewer().fireMenuSelection(data);
					if (isShellDisposed()) {
						return;
					}

					if (dropDownViewer.getExpandedState(data)) {
						dropDownViewer.collapseToLevel(data, 1);
					} else {
						tree.setRedraw(false);
						try {
							dropDownViewer.expandToLevel(data, 1);
							resizeShell(getShell());
						} finally {
							tree.setRedraw(true);
						}
					}
				}
			});

			tree.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(final Event event) {
					if (tree.getSelectionCount() == 0) {
						lblMessage.setText(null);
						lblMessage.setImage(null);
					} else {
						final TreeItem item = tree.getSelection()[0];
						lblMessage.setText(item.getText());
						lblMessage.setImage(item.getImage());
					}
				}
			});

			tree.addListener(SWT.MouseMove, new Listener() {
				TreeItem fLastItem = null;

				@Override
				public void handleEvent(final Event e) {
					if (!tree.equals(e.widget)) {
						return;
					}

					final TreeItem currentItem = tree.getItem(new Point(e.x,
							e.y));
					if (currentItem == null) {
						return;
					}

					if (!currentItem.equals(fLastItem)) {
						updateSelection(currentItem, e);
					} else if (e.y < tree.getItemHeight() / 4) {
						// scroll up
						if (currentItem.getParentItem() == null) {
							final int index = tree.indexOf(currentItem);
							if (index < 1) {
								return;
							}
							updateSelection(tree.getItem(index - 1), e);
						} else {
							final Point p = tree.toDisplay(e.x, e.y);
							final Item item = dropDownViewer.scrollUp(p.x, p.y);
							if (item != null && item instanceof TreeItem) {
								updateSelection((TreeItem) item, e);
							}
						}
					} else if (e.y > tree.getBounds().height
							- tree.getItemHeight() / 4) {
						// scroll down
						if (currentItem.getParentItem() == null) {
							final int index = tree.indexOf(currentItem);
							if (index >= tree.getItemCount() - 1) {
								return;
							}
							updateSelection(tree.getItem(index + 1), e);
						} else {
							final Point p = tree.toDisplay(e.x, e.y);
							final Item item = dropDownViewer.scrollDown(p.x,
									p.y);
							if (item != null && item instanceof TreeItem) {
								updateSelection((TreeItem) item, e);
							}
						}
					}
				}

				private void updateSelection(final TreeItem item, final Event e) {
					fLastItem = item;
					e.type = SWT.Selection;
					final TreeItem[] selection = new TreeItem[] { item };
					tree.setSelection(selection);
					tree.notifyListeners(SWT.Selection, e);
				}
			});

			tree.addListener(SWT.KeyDown, new Listener() {
				@Override
				public void handleEvent(final Event event) {
					switch (event.keyCode) {
					case SWT.ARROW_UP:
						handleSelection(false);
						break;
					case SWT.ARROW_LEFT:
						handleSelection(true);
						break;
					}
				}

				private void handleSelection(final boolean leftKey) {
					// get selection
					final TreeItem[] selection = tree.getSelection();
					if (selection.length != 1) {
						return;
					}
					final TreeItem item = selection[0];

					if (leftKey) {
						// expanded ?
						if (item.getItemCount() != 0 && item.getExpanded()) {
							return;
						}

						// root ?
						if (item.getParentItem() == null) {
							close();
						}
					} else {
						// first item ?
						final int selectionIndex = tree.indexOf(item);
						if (selectionIndex == 0) {
							close();
						}
					}
				}
			});

			dropDownViewer.addTreeListener(new ITreeViewerListener() {
				public void treeCollapsed(final TreeExpansionEvent event) {
				}

				public void treeExpanded(final TreeExpansionEvent event) {
					tree.setRedraw(false);
					getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (isShellDisposed()) {
								return;
							}
							try {
								resizeShell(getShell());
							} finally {
								tree.setRedraw(true);
							}
						}
					});
				}
			});

			return content;
		}

		private boolean isShellDisposed() {
			return getShell() == null || getShell().isDisposed();
		}
	}

	/*
	 * The default drop down height
	 */
	private static final int DROP_DOWN_HEIGHT = 300;

	/*
	 * The default drop down width
	 */
	private static final int DROP_DOWN_WIDTH = 500;

	/**
	 * Returns the monitor whose client area contains the given point. If no
	 * monitor contains the point, returns the monitor that is closest to the
	 * point.
	 * <p>
	 * Copied from
	 * <code>org.eclipse.jface.window.Window.getClosestMonitor(Display, Point)</code>
	 * .
	 * </p>
	 * 
	 * @param display
	 *            the display showing the monitors.
	 * @param point
	 *            point to find (display coordinates).
	 * @return the monitor closest to the given point.
	 */
	private static Monitor getClosestMonitor(final Display display,
			final Point point) {
		int closest = Integer.MAX_VALUE;

		final Monitor[] monitors = display.getMonitors();
		Monitor result = monitors[0];

		for (final Monitor current : monitors) {
			final Rectangle clientArea = current.getClientArea();

			if (clientArea.contains(point)) {
				return current;
			}

			final int distance = Geometry.distanceSquared(Geometry
					.centerPoint(clientArea), point);
			if (distance < closest) {
				closest = distance;
				result = current;
			}
		}

		return result;
	}

	private final BreadcrumbItem parentItem;
	private final Composite parentComposite;
	private final Label imageElement;

	private boolean enabled;
	private boolean menuIsShown;

	private TreeViewer dropDownViewer;
	private DropDownWindow dropDownWindow;

	private final Image rightImage;
	private final Image leftImage;
	private final Image downImage;

	private final boolean leftToRight;

	/**
	 * Create a new instance of this class with the given item parent and the
	 * composite parent.
	 * 
	 * @param parent
	 *            the parent item.
	 * @param parentContainer
	 *            the parent composite.
	 */
	public BreadcrumbItemArrow(final BreadcrumbItem parent,
			final Composite parentContainer) {
		this.parentItem = parent;
		enabled = true;
		menuIsShown = false;
		parentComposite = parentContainer;
		leftToRight = (parentContainer.getStyle() & SWT.RIGHT_TO_LEFT) == 0;

		// images
		rightImage = ImageFileRegistry.getResource(
				ImageFileRegistry.KEY_ARROW_RIGHT).createImage();
		leftImage = ImageFileRegistry.getResource(
				ImageFileRegistry.KEY_ARROW_LEFT).createImage();
		downImage = ImageFileRegistry.getResource(
				ImageFileRegistry.KEY_ARROW_DOWN).createImage();

		imageElement = new Label(parentContainer, SWT.NONE);
		imageElement.setImage(getArrowImage(false));
		imageElement
				.setToolTipText(Messages.BreadcrumbItemDropDown_Action_ToolTip);
		imageElement.getAccessible().addAccessibleListener(
				new AccessibleAdapter() {
					@Override
					public void getName(final AccessibleEvent e) {
						e.result = Messages.BreadcrumbItemDropDown_Action_ToolTip;
					}
				});
		imageElement.addListener(SWT.FocusIn, new Listener() {
			public void handleEvent(final Event event) {
				parent.getItemDetail().setHasFocus(true);
			}
		});
		imageElement.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(final Event event) {
				if (event.button == 1) {
					// already open ?
					Window window = parentItem.getDropDownWindow();
					if (window != null) {
						return;
					}

					// other open ?
					window = getViewer().getDropDownWindow();
					if (window != null) {
						window.close();
					}
					openDropDownMenu();
				}
			}
		});
		imageElement.addListener(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				final BreadcrumbViewer viewer = getViewer();
				int index = viewer.indexOf(parentItem);
				switch (event.keyCode) {
				case SWT.ARROW_RIGHT:
					index++;
					break;
				case SWT.ARROW_LEFT:
					index--;
					break;
				case SWT.HOME:
					index = viewer.isRootVisible() ? 0 : 1;
					break;
				case SWT.END:
					index = viewer.getItemCount() - 1;
					break;
				default:
					return;
				}
				if (index >= 0 && index < viewer.getItemCount()) {
					viewer.selectItem(index);
				}
				event.doit = true;
			}
		});
	}

	/**
	 * Gets the current item width.
	 * 
	 * @return the current item width.
	 */
	public int getCurrentWidth() {
		if (!imageElement.getVisible()) {
			return 0;
		}
		return imageElement.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
	}

	/**
	 * Gets the drop down selection provider.
	 * 
	 * @return the selection provider of the drop down or <code>null</code>.
	 */
	public ISelectionProvider getDropDownSelectionProvider() {
		if (!menuIsShown) {
			return null;
		}
		return dropDownViewer;
	}

	/**
	 * Gets the drop down window.
	 * 
	 * @return the drop down window if shown, <code>null</code> otherwise.
	 */
	public Window getDropDownWindow() {
		if (!isMenuShown()) {
			return null;
		}
		return dropDownWindow;
	}

	/**
	 * Returns <code>true</code> if the receiver has the user-interface focus,
	 * and <code>false</code> otherwise.
	 * 
	 * @return the receiver's focus state
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean isFocusControl() {
		return parentComposite.isFocusControl()
				|| imageElement.isFocusControl();
	}

	/**
	 * Gets a value indicating if this element is expanded.
	 * 
	 * @return <code>true</code> if this item is expanded, <code>false</code>
	 *         otherwise.
	 */
	public boolean isMenuShown() {
		return menuIsShown;
	}

	/**
	 * Opens the drop down menu.
	 */
	public void openDropDownMenu() {
		if (!enabled || menuIsShown) {
			return;
		}
		
		menuIsShown = true;
		imageElement.setImage(getArrowImage(true));
		dropDownWindow = new DropDownWindow(imageElement.getShell());
		
		BusyIndicator.showWhile(imageElement.getDisplay(), new Runnable() {			
			@Override
			public void run() {
				dropDownWindow.open();				
			}
		});		
	}

	/**
	 * Set whether the drop down menu is available.
	 * 
	 * @param enabled
	 *            true if available
	 */
	public void setEnabled(final boolean enabled) {
		this.enabled = enabled;
		imageElement.setVisible(enabled);
	}

	private Image getArrowImage(final boolean down) {
		if (down) {
			return downImage;
		}
		if (isLTR()) {
			return rightImage;
		}
		return leftImage;
	}

	/*
	 * Returns the viewer
	 */
	private BreadcrumbViewer getViewer() {
		return parentItem.getViewer();
	}

	/**
	 * Gets a value indicating if the breadcrumb is in left-to-right mode.
	 * 
	 * @return <code>true</code> if the breadcrumb is in left-to-right mode,
	 *         <code>false</code> otherwise.
	 */
	private boolean isLTR() {
		return leftToRight;
	}

	/**
	 * Set the size of the given shell such that more content can be shown. The
	 * shell size does not exceed {@link #DROP_DOWN_HEIGHT} and
	 * {@link #DROP_DOWN_WIDTH}.
	 * 
	 * @param shell
	 *            the shell to resize.
	 */
	private void resizeShell(final Shell shell) {
		final Point size = shell.getSize();
		final int currentWidth = size.x;
		final int currentHeight = size.y;

		if (currentHeight >= DROP_DOWN_HEIGHT
				&& currentWidth >= DROP_DOWN_WIDTH) {
			return;
		}

		final Point preferedSize = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT,
				true);

		int newWidth;
		if (currentWidth >= DROP_DOWN_WIDTH) {
			newWidth = currentWidth;
		} else {
			newWidth = Math.min(Math.max(preferedSize.x, currentWidth),
					DROP_DOWN_WIDTH);
		}
		int newHeight;
		if (currentHeight >= DROP_DOWN_HEIGHT) {
			newHeight = currentHeight;
		} else {
			newHeight = Math.min(Math.max(preferedSize.y, currentHeight),
					DROP_DOWN_HEIGHT);
		}

		if (newHeight != currentHeight || newWidth != currentWidth) {
			shell.setRedraw(false);
			try {
				shell.setSize(newWidth, newHeight);
				if (!isLTR()) {
					final Point location = shell.getLocation();
					shell.setLocation(location.x - (newWidth - currentWidth),
							location.y);
				}
			} finally {
				shell.setRedraw(true);
			}
		}
	}

	/**
	 * Calculates a useful size for the given shell.
	 * 
	 * @param shell
	 *            the shell to calculate the size for.
	 */
	private void setShellBounds(final Shell shell) {
		final Rectangle rect = parentComposite.getBounds();
		final Rectangle toolbarBounds = imageElement.getBounds();

		shell.pack();
		final Point size = shell.getSize();
		final int height = Math.min(size.y, DROP_DOWN_HEIGHT);
		final int width = Math.max(Math.min(size.x, DROP_DOWN_WIDTH), 250);

		int imageBoundsX = 0;
		if (dropDownViewer.getTree().getItemCount() > 0) {
			final TreeItem item = dropDownViewer.getTree().getItem(0);
			imageBoundsX = item.getImageBounds(0).x;
		}

		final Rectangle trim = shell.computeTrim(0, 0, width, height);
		int x = toolbarBounds.x + toolbarBounds.width + 2 + trim.x
				- imageBoundsX;
		if (!isLTR()) {
			x += width;
		}

		Point pt = new Point(x, rect.y + rect.height);
		pt = parentComposite.toDisplay(pt);

		final Rectangle monitor = getClosestMonitor(shell.getDisplay(), pt)
				.getClientArea();
		final int overlapX = pt.x + width - (monitor.x + monitor.width);		
		if (overlapX > 0) {
			pt.x -= overlapX;
		}
		if (pt.x < monitor.x) {
			pt.x = monitor.x;
		}
		final int overlayY = pt.y + height - (monitor.y + monitor.height);
		if (overlayY > 0) {
			pt.y -= height;
			pt.y -= rect.height;			
		}

		shell.setLocation(pt);
		shell.setSize(width, height);
	}
}
