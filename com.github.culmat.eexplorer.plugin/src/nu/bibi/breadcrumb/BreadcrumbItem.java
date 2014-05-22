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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;

/**
 * An item in a breadcrumb viewer.
 * <p>
 * The item shows a label and an image. It also has the ability to expand, that
 * is to open a drop down menu.
 * </p>
 * <p>
 * The drop down allows to select any child of the items input element. The item
 * shows the label and icon of its data element, if any.
 * </p>
 * 
 * @since 3.4
 */
public class BreadcrumbItem extends Item {

	private ILabelProvider labelProvider;
	private ITreeContentProvider contentProvider;

	private final BreadcrumbViewer parent;
	private final Composite container;

	private final BreadcrumbItemDetail itemDetail;
	private final BreadcrumbItemArrow itemArrow;
	private ILabelProvider toolTipLabelProvider;
	private boolean isLastItem;

	/* package */
	Object element;
	
	/**
	 * Create a new breadcrumb item with the given viewer and the given parent
	 * container.
	 * 
	 * @param viewer
	 *            the parent viewer.
	 * @param parent
	 *            the parent container.
	 */
	public BreadcrumbItem(final BreadcrumbViewer viewer, final Composite parent) {
		super(parent, SWT.NONE);

		this.parent = viewer;

		container = new Composite(parent, SWT.NONE);
		container
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		final GridLayout layout = new GridLayout(2, false);
		layout.marginTop = 1;
		layout.marginBottom = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		container.setLayout(layout);

		itemDetail = new BreadcrumbItemDetail(this, container);
		itemArrow = new BreadcrumbItemArrow(this, container);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	@Override
	public void dispose() {
		container.dispose();
		super.dispose();
	}

	/**
	 * Returns a rectangle describing the receiver's size and location relative
	 * to its parent.
	 * 
	 * @return the receiver's bounding rectangle.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Rectangle getBounds() {
		checkWidget();
		return container.getBounds();
	}

	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Item#getImage()
	 */
	@Override
	public Image getImage() {
		checkWidget();
		final Image image = itemDetail.getImage();
		if (image == null) {
			return super.getImage();
		}
		return image;
	}

	/**
	 * Returns a rectangle describing the size and location relative to its
	 * parent of the image.
	 * 
	 * @return the receiver's bounding image rectangle or <code>null</code> if
	 *         no image is set.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Rectangle getImageBounds() {
		checkWidget();
		return itemDetail.getImageBounds();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Item#getText()
	 */
	@Override
	public String getText() {
		checkWidget();
		final String text = itemDetail.getText();
		if (text == null) {
			return super.getText();
		}
		return text;
	}

	/**
	 * Returns a rectangle describing the size and location relative to its
	 * parent of the text.
	 * 
	 * @return the receiver's bounding text rectangle or <code>null</code> if
	 *         the text is not visible.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 * @see #isTextVisible()
	 */
	public Rectangle getTextBounds() {
		checkWidget();
		return itemDetail.getTextBounds();
	}

	/**
	 * Returns the receiver's tool tip text, or null if it has not been set.
	 * 
	 * @return the receiver's tool tip text.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public String getToolTipText() {
		return itemDetail.getToolTipText();
	}

	/**
	 * Gets the viewer parent.
	 * 
	 * @return the viewer parent.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public BreadcrumbViewer getViewer() {
		checkWidget();
		return parent;
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
		checkWidget();
		return itemDetail.isFocusControl() || itemArrow.isFocusControl();
	}

	/**
	 * Gets a value indicating if this item show a text or only an image?
	 * 
	 * @return <code>true</code> if it shows a text and an image,
	 *         <code>false</code> if it only shows the image.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public boolean isTextVisible() {
		checkWidget();
		return itemDetail.isTextVisible();
	}

	/**
	 * Sets the content provider.
	 * 
	 * @param contentProvider
	 *            the content provider to set.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setContentProvider(final ITreeContentProvider contentProvider) {
		checkWidget();
		this.contentProvider = contentProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Item#setImage(org.eclipse.swt.graphics.Image)
	 */
	@Override
	public void setImage(final Image image) {
		super.setImage(image);
		itemDetail.setImage(image);
	}

	/**
	 * Sets the label provider.
	 * 
	 * @param labelProvider
	 *            the label provider to set.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setLabelProvider(final ILabelProvider labelProvider) {
		checkWidget();
		this.labelProvider = labelProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Item#setText(java.lang.String)
	 */
	@Override
	public void setText(final String string) {
		super.setText(string);
		itemDetail.setText(string);

		// more or less space might be required for the label
		if (isLastItem) {
			container.layout(true, true);
		}
	}

	/**
	 * Sets the tooltip label provider.
	 * 
	 * @param toolTipLabelProvider
	 *            the tooltip label provider to set.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setToolTipLabelProvider(
			final ILabelProvider toolTipLabelProvider) {
		checkWidget();
		this.toolTipLabelProvider = toolTipLabelProvider;
	}

	/**
	 * Set the tool tip of the item to the given text.
	 * 
	 * @param toolTipText
	 *            the tool tip to set.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setToolTipText(final String toolTipText) {
		checkWidget();
		itemDetail.setToolTipText(toolTipText);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Widget#toString()
	 */
	@Override
	public String toString() {
		return getText();
	}

	/**
	 * Gets the current item width.
	 * 
	 * @return the current item width.
	 */
	int getCurrentWidth() {
		return itemDetail.getCurrentWidth() + itemArrow.getCurrentWidth();
	}

	/**
	 * Gets the drop down selection provider.
	 * 
	 * @return the selection provider of the drop down or <code>null</code>.
	 */
	ISelectionProvider getDropDownSelectionProvider() {
		return itemArrow.getDropDownSelectionProvider();
	}

	/**
	 * Gets the drop down window.
	 * 
	 * @return the drop down window if shown, <code>null</code> otherwise.
	 */
	Window getDropDownWindow() {
		return itemArrow.getDropDownWindow();
	}

	/**
	 * Gets the item detail.
	 * 
	 * @return the item detail.
	 */
	BreadcrumbItemDetail getItemDetail() {
		return itemDetail;
	}

	/**
	 * Gets the item drop-down.
	 * 
	 * @return the item drop-down.
	 */
	BreadcrumbItemArrow getItemDropDown() {
		return itemArrow;
	}

	/**
	 * Gets a value indicating if this element has the keyboard focus.
	 * 
	 * @return <code>true</code> if this element has the keyboard focus.
	 */
	boolean hasFocus() {
		return itemDetail.hasFocus();
	}

	/**
	 * Gets a value indicating if this element is expanded.
	 * 
	 * @return <code>true</code> if this item is expanded, <code>false</code>
	 *         otherwise.
	 */
	boolean isMenuShown() {
		return itemArrow.isMenuShown();
	}

	/**
	 * Gets a value indicating if this item show a text or only an image? Does
	 * this item show a text label?
	 * 
	 * @return <code>true</code> if it shows a text and an image,
	 *         <code>false</code> if it only shows the image.
	 */
	boolean isShowText() {
		return itemDetail.isTextVisible();
	}

	/**
	 * Expand this item and shows the drop down menu.
	 */
	void openDropDownMenu() {
		itemArrow.openDropDownMenu();
	}

	/**
	 * Redraw this item, retrieves new labels from its label provider.
	 */
	void refresh() {
		final String text = labelProvider.getText(element);
		final Image image = labelProvider.getImage(element);
		String toolTip = text;
		if (toolTipLabelProvider instanceof CellLabelProvider) {
			final CellLabelProvider provider = (CellLabelProvider) toolTipLabelProvider;
			toolTip = provider.getToolTipText(element);
		}
		itemDetail.setText(text);
		itemDetail.setImage(image);
		itemDetail.setToolTipText(toolTip);
		refreshArrow();
	}

	/**
	 * Refresh the arrows visibility.
	 */
	void refreshArrow() {
		final boolean hasChildren = contentProvider.hasChildren(element);
		itemArrow.setEnabled(hasChildren);
	}

	/**
	 * Sets whether or not the this item should show the details (name and
	 * label).
	 * 
	 * @param visible
	 *            <code>true</code> if the item shows details.
	 */
	void setDetailsVisible(final boolean visible) {
		itemDetail.setVisible(visible);
	}

	/**
	 * Causes the receiver to have the keyboard focus, such that all keyboard
	 * events will be delivered to it.
	 * 
	 * @param hasFocus
	 *            <code>true</code> if this element has the keyboard focus,
	 *            <code>false</code> otherwise.
	 */
	void setHasFocus(final boolean hasFocus) {
		itemDetail.setHasFocus(hasFocus);
	}

	/**
	 * Sets whether this is the last item in the breadcrumb item chain or not.
	 * 
	 * @param isLastItem
	 *            <code>true</code> if this is the last item, <code>false</code>
	 *            otherwise.
	 */
	void setIsLastItem(final boolean isLastItem) {
		this.isLastItem = isLastItem;
		final GridData data = (GridData) container.getLayoutData();
		data.grabExcessHorizontalSpace = isLastItem;
	}

	/**
	 * Sets a value indicating if this element is selected.
	 * 
	 * @param selected
	 *            <code>true</code> if this element is selected,
	 *            <code>false</code> otherwise.
	 */
	void setSelected(final boolean selected) {
		itemDetail.setSelected(selected);
	}

	/**
	 * Marks the text as visible if the argument is <code>true</code>, and marks
	 * it invisible otherwise.
	 * 
	 * @param visible
	 *            the new visibility state.
	 */
	void setTextVisible(final boolean textVisible) {
		itemDetail.setTextVisible(textVisible);
	}
}
