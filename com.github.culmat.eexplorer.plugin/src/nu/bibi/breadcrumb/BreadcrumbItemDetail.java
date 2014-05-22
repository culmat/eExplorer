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

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * The icon and label parts of the breadcrumb item.
 * 
 * @since 3.4
 */
class BreadcrumbItemDetail {

	private final Composite contentComposite;
	private final Composite imageComposite;
	private final Composite textComposite;
	private final Label imageElement;
	private final Label textElement;

	private final BreadcrumbItem parentItem;

	private boolean textVisible;
	private boolean selected;
	private boolean hasFocus;

	/**
	 * Create a new instance of this class with the given item parent and the
	 * composite parent.
	 * 
	 * @param parent
	 *            the parent item.
	 * @param parentContainer
	 *            the parent composite.
	 */
	public BreadcrumbItemDetail(final BreadcrumbItem parent,
			final Composite parentContainer) {
		this.parentItem = parent;
		textVisible = true;

		contentComposite = new Composite(parentContainer, SWT.NONE);
		contentComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 0;
		contentComposite.setLayout(layout);
		addElementListeners(contentComposite);

		imageComposite = new Composite(contentComposite, SWT.NONE);
		imageComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));
		layout = new GridLayout(1, false);
		layout.marginWidth = 2;
		layout.marginHeight = 1;
		imageComposite.setLayout(layout);
		imageComposite.addListener(SWT.Paint, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				if (hasFocus && !isTextVisible()) {
					e.gc.drawFocus(e.x, e.y, e.width, e.height);
				}
			}
		});

		installFocusComposite(imageComposite);
		addElementListeners(imageComposite);

		imageElement = new Label(imageComposite, SWT.NONE);
		GridData layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false,
				false);
		imageElement.setLayoutData(layoutData);
		addElementListeners(imageElement);

		textComposite = new Composite(contentComposite, SWT.NONE);
		textComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER,
				false, false));
		layout = new GridLayout(1, false);
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		textComposite.setLayout(layout);
		addElementListeners(textComposite);
		textComposite.addListener(SWT.Paint, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				if (hasFocus && isTextVisible()) {
					e.gc.drawFocus(e.x, e.y, e.width, e.height);
				}
			}
		});

		installFocusComposite(textComposite);
		addElementListeners(textComposite);

		textElement = new Label(textComposite, SWT.NONE);
		layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		textElement.setLayoutData(layoutData);
		addElementListeners(textElement);

		textComposite.getAccessible().addAccessibleListener(
				new AccessibleAdapter() {
					@Override
					public void getName(final AccessibleEvent e) {
						e.result = getText();
					}
				});
		imageComposite.getAccessible().addAccessibleListener(
				new AccessibleAdapter() {
					@Override
					public void getName(final AccessibleEvent e) {
						e.result = getText();
					}
				});

		contentComposite.setTabList(new Control[] { textComposite });
	}

	/**
	 * Gets the current item width.
	 * 
	 * @return the current item width.
	 */
	public int getCurrentWidth() {
		int result = 0;
		if (getImage() != null) {
			result += 4; // margins
			result += imageElement.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		}
		if (textVisible && getText().length() > 0) {
			result += 4; // margins
			result += textElement.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		}
		return result;
	}

	/**
	 * Returns the receiver's image if it has one, or <code>null</code> if it
	 * does not.
	 * 
	 * @return the receiver's image.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Image getImage() {
		return imageElement.getImage();
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
		if (getImage() == null) {
			return null;
		}
		return imageElement.getBounds();
	}

	/**
	 * Returns the receiver's text, which will be an empty string if it has
	 * never been set or if the receiver is a <code>SEPARATOR</code> label.
	 * 
	 * @return the receiver's text.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public String getText() {
		return textElement.getText();
	}

	/**
	 * Returns a rectangle describing the size and location relative to its
	 * parent of the text.
	 * 
	 * @return the receiver's bounding text rectangle or <code>null</code> if
	 *         the text is <code>null</code> or not visible.
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
		if (textVisible) {
			return textElement.getBounds();
		}
		return null;
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
		if (isTextVisible()) {
			return textElement.getToolTipText();
		}
		return imageElement.getToolTipText();
	}

	/**
	 * Gets a value indicating if this element has the keyboard focus.
	 * 
	 * @return <code>true</code> if this element has the keyboard focus.
	 */
	public boolean hasFocus() {
		return hasFocus;
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
		return contentComposite.isFocusControl()
				|| imageComposite.isFocusControl()
				|| textComposite.isFocusControl()
				|| imageElement.isFocusControl()
				|| textElement.isFocusControl();
	}

	/**
	 * Gets a value indicating if this item show a text or only an image?
	 * 
	 * @return <code>true</code> if it shows a text and an image,
	 *         <code>false</code> if it only shows the image.
	 */
	public boolean isTextVisible() {
		return textVisible;
	}

	/**
	 * Causes the receiver to have the keyboard focus, such that all keyboard
	 * events will be delivered to it.
	 * 
	 * @param hasFocus
	 *            <code>true</code> if this element has the keyboard focus,
	 *            <code>false</code> otherwise.
	 */
	public void setHasFocus(final boolean hasFocus) {
		if (hasFocus) {
			if (isTextVisible()) {
				textComposite.setFocus();
			} else {
				imageComposite.setFocus();
			}
		}
		if (this.hasFocus != hasFocus) {
			this.hasFocus = hasFocus;
			updateSelection();
		}
	}

	/**
	 * Sets the receiver's image to the argument, which may be null indicating
	 * that no image should be displayed.
	 * 
	 * @param image
	 *            the image to display on the receiver (may be null)
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the image has been
	 *                disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setImage(final Image image) {
		imageElement.setImage(image);
	}

	/**
	 * Sets a value indicating if this element is selected.
	 * 
	 * @param selected
	 *            <code>true</code> if this element is selected,
	 *            <code>false</code> otherwise.
	 */
	public void setSelected(final boolean selected) {
		if (this.selected == selected) {
			return;
		}

		this.selected = selected;
		if (!this.selected) {
			hasFocus = false;
		}

		updateSelection();
	}

	/**
	 * Sets the receiver's text.
	 * 
	 * @param string
	 *            the new text.
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setText(final String text) {
		if (text != null) {
			textElement.setText(text);
		} else {
			textElement.setText(""); //$NON-NLS-1$
		}
	}

	/**
	 * Marks the text as visible if the argument is <code>true</code>, and marks
	 * it invisible otherwise.
	 * 
	 * @param textVisible
	 *            the new visibility state.
	 */
	public void setTextVisible(final boolean textVisible) {
		if (this.textVisible == textVisible) {
			return;
		}

		this.textVisible = textVisible;

		final GridData data = (GridData) textComposite.getLayoutData();
		data.exclude = !textVisible;
		textComposite.setVisible(textVisible);

		if (this.textVisible) {
			contentComposite.setTabList(new Control[] { textComposite });
		} else {
			contentComposite.setTabList(new Control[] { imageComposite });
		}

		if (hasFocus) {
			if (isTextVisible()) {
				textComposite.setFocus();
			} else {
				imageComposite.setFocus();
			}
		}
		updateSelection();
	}

	/**
	 * Sets the receiver's tool tip text to the argument.
	 * 
	 * @param toolTipText
	 *            the new tool tip text (or null).
	 */
	public void setToolTipText(final String toolTipText) {
		if (isTextVisible()) {
			textElement.getParent().setToolTipText(toolTipText);
			textElement.setToolTipText(toolTipText);
			imageElement.setToolTipText(toolTipText);
		} else {
			textElement.getParent().setToolTipText(null);
			textElement.setToolTipText(null);
			imageElement.setToolTipText(toolTipText);
		}
	}

	/**
	 * Sets whether details should be shown
	 * 
	 * @param visible
	 *            true if details should be shown
	 */
	public void setVisible(final boolean visible) {
		contentComposite.setVisible(visible);
		final GridData data = (GridData) contentComposite.getLayoutData();
		data.exclude = !visible;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return textElement.getText();
	}

	/**
	 * Add mouse listeners to the given control.
	 * 
	 * @param control
	 *            the control to which may be clicked.
	 */
	private void addElementListeners(final Control control) {
		control.addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				final BreadcrumbViewer viewer = getViewer();
				viewer.selectItem(parentItem);
				if (event.button == 1 && event.stateMask == 0) {
					viewer.fireDoubleClick();
				}
			}
		});
		control.addListener(SWT.MenuDetect, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				event.widget = parentItem;
				final BreadcrumbViewer viewer = getViewer();
				viewer.selectItem(parentItem);
				viewer.fireMenuDetect(event);
			}
		});
		control.addListener(SWT.FocusIn, new Listener() {
			public void handleEvent(final Event event) {
				setHasFocus(true);
			}
		});
		control.addListener(SWT.FocusOut, new Listener() {
			public void handleEvent(final Event event) {
				setHasFocus(false);
			}
		});
	}

	/*
	 * Returns the viewer
	 */
	private BreadcrumbViewer getViewer() {
		return parentItem.getViewer();
	}

	/**
	 * Install focus and key listeners to the given composite.
	 * 
	 * @param composite
	 *            the composite which may get focus.
	 */
	private void installFocusComposite(final Composite composite) {
		composite.addListener(SWT.Traverse, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				final BreadcrumbViewer viewer = getViewer();
				int index = viewer.indexOf(parentItem);
				switch (event.detail) {
				case SWT.TRAVERSE_TAB_NEXT:
					index++;
					break;
				case SWT.TRAVERSE_TAB_PREVIOUS:
					index--;
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

		composite.addListener(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				final BreadcrumbViewer viewer = getViewer();

				// convert character
				switch (event.character) {
				case ' ':
					event.keyCode = SWT.KEYPAD_ADD;
					break;
				case SWT.CR:
					event.keyCode = SWT.CR;
					break;
				}

				switch (event.keyCode) {
				case SWT.ARROW_RIGHT:
					if (selected) {
						viewer.doTraverse(true);
					} else {
						viewer.selectItem(parentItem);
					}
					event.doit = false;
					break;
				case SWT.ARROW_LEFT:
					if (selected) {
						viewer.doTraverse(false);
					} else {
						viewer.selectItem(parentItem);
					}
					event.doit = false;
					break;
				case SWT.ARROW_DOWN:
				case SWT.KEYPAD_ADD:
					if (!selected) {
						viewer.selectItem(parentItem);
					}
					openDropDown();
					event.doit = false;
					break;
				case SWT.CR:
					if (!selected) {
						viewer.selectItem(parentItem);
					}
					viewer.fireOpen();
					event.doit = false;
					break;
				case SWT.HOME:
					final int index = viewer.isRootVisible() ? 0 : 1;
					viewer.selectItem(index);
					event.doit = false;
					break;
				case SWT.END:
					viewer.selectItem(viewer.getItemCount() - 1);
					event.doit = false;
					break;
				}
			}

			private void openDropDown() {
				final BreadcrumbViewer viewer = getViewer();
				final int index = viewer.indexOf(parentItem);
				final BreadcrumbItem parent = viewer.getItem(Math.max(0,
						index - 1));

				final Window window = parent.getDropDownWindow();
				if (window == null) {
					parent.openDropDownMenu();
				}
			}
		});
	}

	/**
	 * Updates the foreground and background colors.
	 */
	private void updateSelection() {
		Color background = null;
		Color foreground = null;

		if (selected) {
			if (hasFocus) {
				background = Display.getDefault().getSystemColor(
						SWT.COLOR_LIST_SELECTION);
				foreground = Display.getDefault().getSystemColor(
						SWT.COLOR_LIST_SELECTION_TEXT);
			} else {
				background = Display.getDefault().getSystemColor(
						SWT.COLOR_WIDGET_LIGHT_SHADOW);
				foreground = Display.getDefault().getSystemColor(
						SWT.COLOR_WIDGET_FOREGROUND);
			}
		}

		if (isTextVisible()) {
			textComposite.setBackground(background);
			textElement.setBackground(background);
			textElement.setForeground(foreground);
			imageComposite.setBackground(null);
			imageElement.setBackground((Color) null);
		} else {
			imageComposite.setBackground(background);
			imageElement.setBackground(background);
			textComposite.setBackground(null);
			textElement.setBackground((Color) null);
			textElement.setForeground(null);
		}

		textComposite.redraw();
		imageComposite.redraw();
	}
}
