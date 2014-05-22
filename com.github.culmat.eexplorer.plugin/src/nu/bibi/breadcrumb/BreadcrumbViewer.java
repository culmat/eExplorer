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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.FormColors;

/**
 * A breadcrumb viewer shows a the parent chain of its input element in a list.
 * Each breadcrumb item of that list can be expanded and a sibling of the
 * element presented by the breadcrumb item can be selected.
 * <p>
 * Content providers for breadcrumb viewers must implement the
 * <code>ITreeContentProvider</code> interface.
 * </p>
 * <p>
 * Label providers for breadcrumb viewers must implement the
 * <code>ILabelProvider</code> interface.
 * </p>
 */
public abstract class BreadcrumbViewer extends StructuredViewer {

	private static final boolean IS_GTK = "gtk".equals(SWT.getPlatform()); //$NON-NLS-1$

	private final Composite container;
	private final ArrayList<BreadcrumbItem> items;
	private final ArrayList<MenuDetectListener> menuDetectListeners;
	private final ArrayList<IMenuSelectionListener> menuSelectionListeners;

	private Image backgroundImage;
	private BreadcrumbItem selectedItem;
	private ILabelProvider toolTipLabelProvider;
	private boolean rootVisible = false;

	/**
	 * Creates a breadcrumb viewer under the given parent. The control is
	 * created using the SWT style bits <code>HORIZONTAL</code>. The viewer has
	 * no input, no content provider, and no label provider.
	 * 
	 * @param parent
	 *            the parent control.
	 */
	public BreadcrumbViewer(final Composite parent) {
		this(parent, SWT.HORIZONTAL);
	}

	/**
	 * Creates a breadcrumb viewer under the given parent. The control is
	 * created using the given style bits. The viewer has no input, no content
	 * provider, and no label provider.
	 * <p>
	 * Allowed styles are one of:
	 * <ul>
	 * <li>SWT.NONE</li>
	 * <li>SWT.VERTICAL</li>
	 * <li>SWT.HORIZONTAL</li>
	 * </ul>
	 * 
	 * @param parent
	 *            the parent control.
	 * @param style
	 *            SWT style bits.
	 */
	public BreadcrumbViewer(final Composite parent, final int style) {
		items = new ArrayList<BreadcrumbItem>();
		menuDetectListeners = new ArrayList<MenuDetectListener>();
		menuSelectionListeners = new ArrayList<IMenuSelectionListener>();

		container = new Composite(parent, style);
		container.setBackgroundMode(SWT.INHERIT_DEFAULT);

		// add listeners
		container.addListener(SWT.Traverse, new Listener() {
			public void handleEvent(final Event event) {
				event.doit = true;
			}
		});
		container.addListener(SWT.Resize, new Listener() {
			public void handleEvent(final Event event) {
				final int height = container.getClientArea().height;

				// update background image
				if (backgroundImage == null
						|| backgroundImage.getBounds().height != height) {
					if (backgroundImage != null) {
						backgroundImage.dispose();
					}
					backgroundImage = createGradientImage(height, event.display);
					container.setBackgroundImage(backgroundImage);
				}
				// update layout
				refresh();
			}
		});
		container.addListener(SWT.Dispose, new Listener() {
			public void handleEvent(final Event event) {
				if (backgroundImage != null) {
					backgroundImage.dispose();
				}
			}
		});
		container.addListener(SWT.FocusIn, new Listener() {
			public void handleEvent(final Event event) {
				if (selectedItem != null) {
					selectedItem.setHasFocus(true);
				}
			}
		});
		container.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(final Event event) {
				if (event.button == 1) {
					setFocus();
				}
			}
		});

		hookControl(container);

		// layout
		int columns = 1000;
		if ((SWT.VERTICAL & style) != 0) {
			columns = 1;
		}
		final GridLayout gridLayout = new GridLayout(columns, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = gridLayout.horizontalSpacing = 0;
		container.setLayout(gridLayout);
	}

	/**
	 * Add the given listener to the set of listeners which will be informed
	 * when a context menu is requested for a breadcrumb item.
	 * 
	 * @param listener
	 *            the listener to add
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 */
	public void addMenuDetectListener(final MenuDetectListener listener) {
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		menuDetectListeners.add(listener);
	}

	/**
	 * Add the given listener to the set of listeners which will be informed
	 * when a selection of the drop down menu occurs.
	 * 
	 * @param listener
	 *            the listener to add
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 */
	public void addMenuSelectionListener(final IMenuSelectionListener listener) {
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		menuSelectionListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ContentViewer#getContentProvider()
	 */
	@Override
	public ITreeContentProvider getContentProvider() {
		return (ITreeContentProvider) super.getContentProvider();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.Viewer#getControl()
	 */
	@Override
	public Control getControl() {
		return container;
	}

	/**
	 * Returns the selection provider which provides the selection of the drop
	 * down currently opened or <code>null</code> if no drop down is open at the
	 * moment.
	 * 
	 * @return the selection provider of the open drop down or <code>null</code>
	 *         .
	 */
	public ISelectionProvider getDropDownSelectionProvider() {
		for (final BreadcrumbItem item : items) {
			if (item.isMenuShown()) {
				return item.getDropDownSelectionProvider();
			}
		}
		return null;
	}

	/**
	 * The Window used for the shown drop down menu or <code>null</code> if no
	 * drop down is shown at the moment.
	 * 
	 * @return the drop downs window or <code>null</code>.
	 */
	public Window getDropDownWindow() {
		for (final BreadcrumbItem item : items) {
			if (item.isMenuShown()) {
				return item.getDropDownWindow();
			}
		}
		return null;
	}

	/**
	 * Returns the item at the given, zero-relative index in the receiver.
	 * Throws an exception if the index is out of range.
	 * 
	 * @param index
	 *            the index of the item to return.
	 * @return the item at the given index.
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (
	 *             <tt>index &lt; 0 || index &gt;= getItemCount()</tt>).
	 */
	public BreadcrumbItem getItem(final int index) {
		return items.get(index);
	}

	/**
	 * Returns the item at the given display-relative coordinates, or
	 * <code>null</code> if there is no item at that location.
	 * 
	 * @param x
	 *            horizontal coordinate
	 * @param y
	 *            vertical coordinate
	 * @return the item, or <code>null</code> if there is no item at the given
	 *         coordinates.
	 */
	@Override
	public BreadcrumbItem getItem(final int x, final int y) {
		return getItem(new Point(x, y));
	}

	/**
	 * Returns the item at the given point in the receiver or null if no such
	 * item exists. The point is in the coordinate system of the receiver.
	 * 
	 * @param point
	 *            the point used to locate the item.
	 * @return the item at the given point, or <code>null</code> if the point is
	 *         not in a selectable item.
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the point is null</li>
	 *                </ul>
	 */
	public BreadcrumbItem getItem(final Point point) {
		if (point == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		for (final BreadcrumbItem item : items) {
			if (item.getBounds().contains(point)) {
				return item;
			}
		}
		return null;
	}

	/**
	 * Returns the number of items contained in the receiver.
	 * 
	 * @return the number of items.
	 */
	public int getItemCount() {
		return items.size();
	}

	/**
	 * Returns a (possibly empty) array of items contained in the receiver.
	 * <p>
	 * Note: This is not the actual structure used by the receiver to maintain
	 * its list of items, so modifying the array will not affect the receiver.
	 * </p>
	 * 
	 * @return the items.
	 */
	public BreadcrumbItem[] getItems() {
		return items.toArray(new BreadcrumbItem[0]);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ContentViewer#getLabelProvider()
	 */
	@Override
	public ILabelProvider getLabelProvider() {
		return (ILabelProvider) super.getLabelProvider();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#getSelection()
	 */
	@Override
	public IStructuredSelection getSelection() {
		return (IStructuredSelection) super.getSelection();
	}

	/**
	 * Returns the index of the first occurrence of the specified element, or -1
	 * if this does not contain the element.
	 * 
	 * @param item
	 *            the item to search for.
	 * @return the index of the first occurrence of the specified element, or -1
	 *         if this does not contain the element.
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the item is null</li>
	 *                </ul>
	 */
	public int indexOf(final BreadcrumbItem item) {
		if (item == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		return items.indexOf(item);
	}

	/**
	 * Gets a value indicating if any of the items in the viewer is expanded.
	 * 
	 * @return <code>true</code> if any of the items in the viewer is expanded.
	 */
	public boolean isDropDownOpen() {
		for (final BreadcrumbItem item : items) {
			if (item.isMenuShown()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the root visible.
	 * 
	 * @return <code>true</code> if the root item is visible, <code>false</code>
	 *         otherwise.
	 */
	public boolean isRootVisible() {
		return rootVisible;
	}

	/**
	 * Display the drop-down menu for the given element
	 * 
	 * @param element
	 *            the element for open the drop-down menu.
	 */
	public void openDropDownMenu(final Object element) {
		final BreadcrumbItem item = (BreadcrumbItem) doFindItem(element);
		if (item != null) {
			item.openDropDownMenu();
		}
	}

	/**
	 * Remove the given listener from the set of menu detect listeners. Does
	 * nothing if the listener is not element of the set.
	 * 
	 * @param listener
	 *            the listener to remove.
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 */
	public void removeMenuDetectListener(final MenuDetectListener listener) {
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		menuDetectListeners.remove(listener);
	}

	/**
	 * Remove the given listener from the set of menu selection listeners. Does
	 * nothing if the listener is not element of the set.
	 * 
	 * @param listener
	 *            the listener to remove.
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 */
	public void removeMenuSelectionListener(
			final IMenuSelectionListener listener) {
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		menuSelectionListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#reveal(java.lang.Object)
	 */
	@Override
	public void reveal(final Object element) {
		// all elements are always visible
	}

	/**
	 * Causes the receiver to have the <em>keyboard focus</em>, such that all
	 * keyboard events will be delivered to it. Focus reassignment will respect
	 * applicable platform constraints.
	 */
	public void setFocus() {
		container.setFocus();

		if (selectedItem != null) {
			selectedItem.setHasFocus(true);
		} else {
			final int size = items.size();
			if (size == 0) {
				return;
			}
			BreadcrumbItem item = items.get(size - 1);
			if (item.element == null) {
				if (size < 2) {
					return;
				}
				item = items.get(size - 2);
			}
			item.setHasFocus(true);
		}
	}

	/**
	 * Sets the root item visibility.
	 * 
	 * @param rootVisible
	 *            <code>true</code> to set the root item visible,
	 *            <code>false</code> to hide.
	 */
	public void setRootVisible(final boolean rootVisible) {
		if (this.rootVisible == rootVisible) {
			return;
		}
		this.rootVisible = rootVisible;
		if (items.size() != 0) {
			final BreadcrumbItem item = items.get(0);
			item.setDetailsVisible(rootVisible);
			container.layout(true, true);
			if (rootVisible || selectedItem == null) {
				return;
			}

			// root selected ?
			if (selectedItem == item || selectedItem.equals(item)) {
				if (items.size() > 1) {
					selectItem(items.get(1));
				}
			}
		}
	}

	/**
	 * The tool tip to use for the tool tip labels. <code>null</code> if the
	 * viewers label provider should be used.
	 * 
	 * @param toolTipLabelProvider
	 *            the label provider for the tool tips or <code>null</code>.
	 */
	public void setToolTipLabelProvider(
			final ILabelProvider toolTipLabelProvider) {
		this.toolTipLabelProvider = toolTipLabelProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#assertContentProviderType(org.eclipse.jface.viewers.IContentProvider)
	 */
	@Override
	protected void assertContentProviderType(final IContentProvider provider) {
		super.assertContentProviderType(provider);
		Assert.isTrue(provider instanceof ITreeContentProvider);
	}

	/**
	 * Configure the given drop down viewer. The given input is used for the
	 * viewers input. Clients must at least set the label providr and the
	 * content provider for the viewer.
	 * 
	 * @param viewer
	 *            the viewer to configure
	 * @param input
	 *            the input for the viewer.
	 */
	protected abstract void configureDropDownViewer(TreeViewer viewer,
			Object input);

	/**
	 * Creates a new instance of a breadcrumb item.
	 * 
	 * @return a new instance of a breadcrumb item.
	 */
	protected BreadcrumbItem createItem() {
		final BreadcrumbItem item = new BreadcrumbItem(this, container);
		item.setLabelProvider(getLabelProvider());
		item.setContentProvider(getContentProvider());		
		if (toolTipLabelProvider != null) {
			item.setToolTipLabelProvider(toolTipLabelProvider);
		} else {
			item.setToolTipLabelProvider(getLabelProvider());
		}
		return item;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#doFindInputItem(java.lang.Object)
	 */
	@Override
	protected Widget doFindInputItem(final Object element) {
		if (element == null) {
			return null;
		}

		if (element == getInput() || element.equals(getInput())) {
			return doFindItem(element);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#doFindItem(java.lang.Object)
	 */
	@Override
	protected Widget doFindItem(final Object element) {
		if (element == null) {
			return null;
		}

		for (final BreadcrumbItem item : items) {
			if (element == item.element
					|| element.equals(item.element)) {
				return item;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#doUpdateItem(org.eclipse.swt.widgets.Widget, java.lang.Object, boolean)
	 */
	@Override
	protected void doUpdateItem(final Widget widget, final Object element,
			final boolean fullMap) {
		if (widget instanceof BreadcrumbItem) {
			final BreadcrumbItem item = (BreadcrumbItem) widget;

			// remember element we are showing
			if (fullMap) {
				associate(element, item);
			} else {
				final Object data = item.element;
				if (data != null) {
					unmapElement(data, item);
				}
				item.element = element;
				mapElement(element, item);
			}
			// update
			item.refresh();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#getRoot()
	 */
	@Override
	protected Object getRoot() {
		if (items.isEmpty()) {
			return null;
		}
		return items.get(0).element;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#getSelectionFromWidget()
	 */
	@Override
	protected List<?> getSelectionFromWidget() {
		if (selectedItem == null
				|| selectedItem.element == null) {
			return Collections.EMPTY_LIST;
		}

		final ArrayList<Object> list = new ArrayList<Object>();
		list.add(selectedItem.element);
		return Collections.unmodifiableList(list);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.Viewer#inputChanged(java.lang.Object,java.lang.Object)
	 */
	@Override
	protected void inputChanged(final Object fInput, final Object oldInput) {
		if (container.isDisposed()) {
			return;
		}

		disableRedraw();
		try {
			if (items.size() > 0) {
				final BreadcrumbItem last = items.get(items.size() - 1);
				last.setIsLastItem(false);
			}

			final int lastIndex = buildItemChain(fInput);

			if (lastIndex > 0) {
				final BreadcrumbItem last = items.get(lastIndex - 1);
				last.setIsLastItem(true);
			}

			while (lastIndex < items.size()) {
				final BreadcrumbItem item = items.remove(items.size() - 1);
				if (item == selectedItem) {
					selectItem(null);
				}
				if (item.element != null) {
					unmapElement(item.element);
				}
				item.dispose();
			}

			updateSize();
			container.layout(true, true);
		} finally {
			enableRedraw();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.StructuredViewer#internalRefresh(java.lang.Object)
	 */
	@Override
	protected void internalRefresh(final Object element) {
		disableRedraw();
		try {
			final BreadcrumbItem item = (BreadcrumbItem) doFindItem(element);
			if (item == null) {
				for (final BreadcrumbItem current : items) {
					current.refresh();
				}
			} else {
				item.refresh();
			}
			if (updateSize()) {
				container.layout(true, true);
			}
		} finally {
			enableRedraw();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.StructuredViewer#setSelectionToWidget(java.util.List, boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void setSelectionToWidget(final List list, final boolean reveal) {
		boolean focused = container.isFocusControl();
		BreadcrumbItem focusItem = null;

		for (final BreadcrumbItem item : items) {
			if (item.hasFocus()) {
				focusItem = item;
			}
			item.setSelected(false);
			focused = focused | item.isFocusControl();
		}

		if (list == null) {
			return;
		}

		selectedItem = null;
		for (final Iterator<?> iter = list.iterator(); iter.hasNext();) {
			final Object element = iter.next();
			final BreadcrumbItem item = (BreadcrumbItem) doFindItem(element);
			if (item != null) {
				selectedItem = item;
				selectedItem.setSelected(true);
				if (item == focusItem) {
					item.setHasFocus(true);
				}
			}
		}
		if ((focused || reveal) && focusItem == null && selectedItem != null) {
			selectedItem.setHasFocus(true);
		}
	}

	/**
	 * Generates the parent chain of the given element.
	 * 
	 * @param element
	 *            element to build the parent chain for
	 * @return the first index of an item in fBreadcrumbItems which is not part
	 *         of the chain
	 */
	private int buildItemChain(final Object element) {
		if (element == null) {
			return 0;
		}

		final ITreeContentProvider contentProvider = getContentProvider();
		final Object parent = contentProvider.getParent(element);

		final int index = buildItemChain(parent);

		BreadcrumbItem item;
		if (index < items.size()) {
			item = items.get(index);
			if (item.element != null) {
				unmapElement(item.element);
			}
		} else {
			item = createItem();
			items.add(item);
		}

		if (equals(element, item.element)) {
			update(element, null);
		} else {
			item.element = element;
			item.refresh();
		}
		if (parent == null) {
			// don't show the models root
			item.setDetailsVisible(rootVisible);
		}

		mapElement(element, item);

		return index + 1;
	}

	private Color createColor(final int color1, final int color2,
			final int ratio, final Display display) {
		final RGB rgb1 = display.getSystemColor(color1).getRGB();
		final RGB rgb2 = display.getSystemColor(color2).getRGB();

		final RGB blend = FormColors.blend(rgb2, rgb1, ratio);

		return new Color(display, blend);
	}

	/**
	 * The image to use for the breadcrumb background as specified in
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=221477
	 * 
	 * @param height
	 *            the height of the image to create
	 * @param display
	 *            the current display
	 * @return the image for the breadcrumb background
	 */
	private Image createGradientImage(final int height, final Display display) {
		final int width = 10;

		final Image image = new Image(display, width, height);

		final GC gc = new GC(image);

		final Color colorC = createColor(SWT.COLOR_WIDGET_BACKGROUND,
				SWT.COLOR_LIST_BACKGROUND, 35, display);
		final Color colorD = createColor(SWT.COLOR_WIDGET_BACKGROUND,
				SWT.COLOR_LIST_BACKGROUND, 45, display);
		final Color colorE = createColor(SWT.COLOR_WIDGET_BACKGROUND,
				SWT.COLOR_LIST_BACKGROUND, 80, display);
		final Color colorF = createColor(SWT.COLOR_WIDGET_BACKGROUND,
				SWT.COLOR_LIST_BACKGROUND, 70, display);
		final Color colorG = createColor(SWT.COLOR_WIDGET_BACKGROUND,
				SWT.COLOR_WHITE, 45, display);
		final Color colorH = createColor(SWT.COLOR_WIDGET_NORMAL_SHADOW,
				SWT.COLOR_LIST_BACKGROUND, 35, display);

		try {
			drawLine(width, 0, colorC, gc);
			drawLine(width, 1, colorC, gc);

			gc.setForeground(colorD);
			gc.setBackground(colorE);
			gc.fillGradientRectangle(0, 2, width, 2 + 8, true);

			gc.setBackground(colorE);
			gc.fillRectangle(0, 2 + 9, width, height - 4);

			drawLine(width, height - 3, colorF, gc);
			drawLine(width, height - 2, colorG, gc);
			drawLine(width, height - 1, colorH, gc);

		} finally {
			gc.dispose();
			colorC.dispose();
			colorD.dispose();
			colorE.dispose();
			colorF.dispose();
			colorG.dispose();
			colorH.dispose();
		}

		return image;
	}

	/**
	 * Disable redrawing of the breadcrumb.
	 * <p>
	 * <strong>A call to this method must be followed by a call to
	 * {@link #enableRedraw()}</strong>
	 * </p>
	 */
	private void disableRedraw() {
		if (IS_GTK) {
			return;
		}

		container.setRedraw(false);
	}

	private void drawLine(final int width, final int position,
			final Color color, final GC gc) {
		gc.setForeground(color);
		gc.drawLine(0, position, width, position);
	}

	/**
	 * Enable redrawing of the breadcrumb.
	 */
	private void enableRedraw() {
		if (IS_GTK) {
			return;
		}

		container.setRedraw(true);
	}

	/**
	 * Returns the current width of all items in the list.
	 * 
	 * @return the width of all items.
	 */
	private int getCurrentWidth() {
		int result = 2;
		for (final BreadcrumbItem item : items) {
			result += item.getCurrentWidth();
		}
		return result;
	}

	/**
	 * Update the size of the items such that all items are visible, if
	 * possible.
	 * 
	 * @return true if any item has changed, false otherwise.
	 */
	private boolean updateSize() {
		boolean requiresLayout = false;
		int currentWidth = getCurrentWidth();
		final int width = container.getClientArea().width;

		if (currentWidth > width) {
			// hide texts
			int index = 0;
			while (currentWidth > width && index < items.size() - 1) {
				final BreadcrumbItem viewer = items.get(index);
				if (viewer.isShowText()) {
					viewer.setTextVisible(false);
					currentWidth = getCurrentWidth();
					requiresLayout = true;
				}

				index++;
			}

		} else if (currentWidth < width) {
			// display texts
			int index = items.size() - 1;

			while (currentWidth < width && index >= 0) {
				final BreadcrumbItem item = items.get(index);
				if (!item.isShowText()) {
					item.setTextVisible(true);
					currentWidth = getCurrentWidth();
					if (currentWidth > width) {
						item.setTextVisible(false);
						index = 0;
					} else {
						requiresLayout = true;
					}
				}
				index--;
			}
		}

		return requiresLayout;
	}

	/**
	 * Set selection, if possible, to the next or previous element.
	 * 
	 * @param next
	 *            <code>true</code> for the next element, <code>false</code> for
	 *            the previous element.
	 */
	void doTraverse(final boolean next) {
		if (selectedItem == null) {
			return;
		}
		final int index = items.indexOf(selectedItem);
		if (next) {
			// last ?
			if (index == getItemCount() - 1) {
				final BreadcrumbItem current = items.get(index);
				final ITreeContentProvider contentProvider = getContentProvider();
				if (!contentProvider.hasChildren(current
						.element)) {
					return;
				}
				current.openDropDownMenu();
			} else {
				// next
				selectItem(items.get(index + 1));
			}
		} else {
			switch (index) {
			case 0:
				items.get(0).openDropDownMenu();
				return;
			case 1:
				if (!rootVisible) {
					items.get(0).openDropDownMenu();
					return;
				}
				break;
			}
			selectItem(items.get(Math.max(0, index - 1)));
		}
	}

	/**
	 * Notify all double click listeners
	 */
	void fireDoubleClick() {
		fireDoubleClick(new DoubleClickEvent(this, getSelection()));
	}

	/**
	 * A context menu has been requested for the selected breadcrumb item.
	 * 
	 * @param event
	 *            the event issued the menu detection
	 */
	void fireMenuDetect(final Event e) {
		final MenuDetectEvent event = new MenuDetectEvent(e);
		for (final MenuDetectListener listener : menuDetectListeners) {
			SafeRunnable.run(new SafeRunnable() {
				public void run() {
					listener.menuDetected(event);
				}
			});
		}
	}

	/**
	 * Fire event for the given element that was selected from a drop down menu.
	 * 
	 * @param element
	 *            the selected element.
	 */
	void fireMenuSelection(final Object element) {
		final MenuSelectionEvent event = new MenuSelectionEvent(this,
				new StructuredSelection(element));
		for (final IMenuSelectionListener listener : menuSelectionListeners) {
			SafeRunnable.run(new SafeRunnable() {
				public void run() {
					listener.menuSelect(event);
				}
			});
		}
		// fireOpen(new OpenEvent(this, new StructuredSelection(element)));
	}

	/**
	 * Notify all open listeners with the current selection.
	 */
	void fireOpen() {
		fireOpen(new OpenEvent(this, getSelection()));
	}

	/**
	 * Set a single selection to the given item, <code>null</code> to deselect
	 * all.
	 * 
	 * @param item
	 *            the item to select or <code>null</code>
	 */
	void selectItem(final BreadcrumbItem item) {
		if (selectedItem != null) {
			selectedItem.setSelected(false);
		}

		selectedItem = item;
		setSelectionToWidget(getSelection(), false);

		if (item != null) {
			setFocus();
		} else {
			for (final BreadcrumbItem current : items) {
				current.setHasFocus(false);
			}
		}

		fireSelectionChanged(new SelectionChangedEvent(this, getSelection()));
	}

	/**
	 * Set a single selection to the given item, <code>-1</code> to deselect
	 * all.
	 * 
	 * @param index
	 *            the item index to select or <code>-1</code>
	 */
	void selectItem(final int index) {
		if (index < 0 || index >= items.size()) {
			selectItem(null);
		} else {
			selectItem(items.get(index));
		}
	}
}
