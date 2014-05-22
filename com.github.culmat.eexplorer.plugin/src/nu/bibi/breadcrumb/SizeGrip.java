/*******************************************************************************
 * Copyright (c) 2008 Laurent Muller and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Laurent Muller - updated API
 *******************************************************************************/
package nu.bibi.breadcrumb;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Laurent Muller
 * @version 1.0
 */
class SizeGrip extends Canvas {

	/*
	 * The default width
	 */
	private static final int WIDTH = 13;

	/*
	 * The default height
	 */
	private static final int HEIGHT = 13;

	/*
	 * the right to left flag
	 */
	private final boolean rightToLeft;

	/*
	 * The resize cursor
	 */
	private final Cursor cursor;

	/*
	 * The mouse down flag
	 */
	private boolean down;

	/*
	 * The mouse down location
	 */
	private Point mouseDownOffset;

	/*
	 * The minimum shell size
	 */
	private Point minimumSize;

	/*
	 * The maximum size
	 */
	private Point maximumSize;

	/**
	 * Constructs a new instance of this class given its parent and a style
	 * value describing its behavior and appearance.
	 * 
	 * @param parent
	 *            a composite control which will be the parent of the new
	 *            instance (cannot be null)
	 * @param style
	 *            the style of control to construct
	 */
	public SizeGrip(final Composite parent, final int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);

		rightToLeft = (style & SWT.RIGHT_TO_LEFT) != 0;
		final int cursorStyle = rightToLeft ? SWT.CURSOR_SIZESW
				: SWT.CURSOR_SIZESE;
		cursor = new Cursor(parent.getDisplay(), cursorStyle);
		setCursor(cursor);
		final Shell shell = parent.getShell();

		addListener(SWT.Paint, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				if (!shell.getMaximized()) {
					onPaint(event);
				}
			}
		});
		addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				if (cursor != null && !cursor.isDisposed()) {
					cursor.dispose();
				}
			}
		});
		addListener(SWT.MouseDown, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				if (event.button == 1 && !shell.getMaximized()) {
					down = true;
					mouseDownOffset = new Point(event.x, event.y);
				}
			}
		});
		addListener(SWT.MouseUp, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				down = false;
			}
		});
		addListener(SWT.MouseMove, new Listener() {
			@Override
			public void handleEvent(final Event event) {
				if (!down) {
					return;
				}

				final Point size = shell.getSize();
				int newX = size.x + event.x - mouseDownOffset.x;
				int newY = size.y + event.y - mouseDownOffset.y;
				if (minimumSize != null) {
					newX = Math.max(newX, minimumSize.x);
					newY = Math.max(newY, minimumSize.y);
				}
				if (maximumSize != null) {
					newX = Math.min(newX, maximumSize.x);
					newY = Math.min(newY, maximumSize.y);
				}
				if (newX != size.x || newY != size.y) {
					if (rightToLeft) {
						final Rectangle bounds = shell.getBounds();
						final int dx = newX - size.x;
						final int dy = newY - size.y;
						bounds.x -= dx;
						bounds.width += dx;
						bounds.height += dy;
						shell.setBounds(bounds);
					} else {
						shell.setSize(newX, newY);
					}
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#computeSize(int, int, boolean)
	 */
	@Override
	public Point computeSize(int wHint, int hHint, final boolean changed) {
		checkWidget();
		if (wHint == SWT.DEFAULT) {
			wHint = WIDTH;
		}
		if (hHint == SWT.DEFAULT) {
			hHint = HEIGHT;
		}
		return new Point(wHint, hHint);
	}

	/**
	 * Returns the maximum shell size allowed.
	 * 
	 * @return the maximum shell size allowed.
	 */
	public Point getMaximumSize() {
		return maximumSize;
	}

	/**
	 * Returns the minimum shell size allowed.
	 * 
	 * @return the minimum shell size allowed.
	 */
	public Point getMinimumSize() {
		return minimumSize;
	}

	/**
	 * Sets the maximum shell size allowed.
	 * 
	 * @param maximumSize
	 *            the maximum shell size allowed to set.
	 */
	public void setMaximumSize(final Point maximumSize) {
		this.maximumSize = maximumSize;
	}

	/**
	 * Sets the minimum shell size allowed.
	 * 
	 * @param minimumSize
	 *            the minimum shell size allowed to set.
	 */
	public void setMinimumSize(final Point minimumSize) {
		this.minimumSize = minimumSize;
	}

	/**
	 * Handles the paint event.
	 * 
	 * @param event
	 *            the event containing data for the paint event.
	 */
	protected void onPaint(final Event event) {
		final Rectangle r = getClientArea();
		if (r.width <= 0 || r.height <= 0) {
			return;
		}

		final GC gc = event.gc;
		final Display display = event.display;
		final Color shadow = display
				.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		final Color highlight = display
				.getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW);

		final int right = r.x + r.width;
		final int bottom = r.y + r.height;

		gc.setBackground(highlight);
		gc.fillRectangle(right - 3, bottom - 3, 2, 2);
		gc.fillRectangle(right - 7, bottom - 3, 2, 2);
		gc.fillRectangle(right - 11, bottom - 3, 2, 2);
		gc.fillRectangle(right - 3, bottom - 7, 2, 2);
		gc.fillRectangle(right - 7, bottom - 7, 2, 2);
		gc.fillRectangle(right - 3, bottom - 11, 2, 2);

		gc.setBackground(shadow);
		gc.fillRectangle(right - 4, bottom - 4, 2, 2);
		gc.fillRectangle(right - 8, bottom - 4, 2, 2);
		gc.fillRectangle(right - 12, bottom - 4, 2, 2);
		gc.fillRectangle(right - 4, bottom - 8, 2, 2);
		gc.fillRectangle(right - 8, bottom - 8, 2, 2);
		gc.fillRectangle(right - 4, bottom - 12, 2, 2);
	}
}
