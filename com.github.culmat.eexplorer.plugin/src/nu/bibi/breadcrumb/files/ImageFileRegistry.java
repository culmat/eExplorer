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
package nu.bibi.breadcrumb.files;

import java.io.File;
import java.io.InputStream;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

/**
 * An image file registry maintains a mapping between a file extension and SWT
 * image descriptor objects.
 * 
 * @author Laurent Muller
 * @version 1.0
 */
public class ImageFileRegistry extends ImageRegistry {

	/**
	 * The key name for a computer.
	 */
	public final static String KEY_COMPUTER = "computer.png"; //$NON-NLS-1$

	/**
	 * The key name for a normal drive.
	 */
	public final static String KEY_DRIVE_DEFAULT = "drive_default.png"; //$NON-NLS-1$

	/**
	 * The key name for a hidden drive.
	 */
	public final static String KEY_DRIVE_HIDDEN = "drive_hidden.png"; //$NON-NLS-1$

	/**
	 * The key name for a normal folder.
	 */
	public final static String KEY_FOLDER_DEFAULT = "folder_default.png"; //$NON-NLS-1$

	/**
	 * The key name for a hidden folder.
	 */
	public final static String KEY_FOLDER_HIDDEN = "folder_hidden.png"; //$NON-NLS-1$

	/**
	 * The key name for a normal file.
	 */
	public final static String KEY_FILE_DEFAULT = "file_default.png"; //$NON-NLS-1$

	/**
	 * The key name for a hidden file.
	 */
	public final static String KEY_FILE_HIDDEN = "file_hidden.png"; //$NON-NLS-1$

	/**
	 * The key name for the right arrow image.
	 */
	public final static String KEY_ARROW_RIGHT = "arrow_right.png"; //$NON-NLS-1$

	/**
	 * The key name for the left arrow image.
	 */
	public final static String KEY_ARROW_LEFT = "arrow_left.png"; //$NON-NLS-1$

	/**
	 * The key name for the down arrow image.
	 */
	public final static String KEY_ARROW_DOWN = "arrow_down.png"; //$NON-NLS-1

	/*
	 * The icons path
	 */
	private static String ICONS_PATH = "icons/";

	/**
	 * Gets a image descriptor from a resource key.
	 * <p>
	 * Images are stored in the <code>nu.bibi.breadcrumb.files.icons</code>
	 * folder.
	 * 
	 * @param keyName
	 *            the resource key name used to load the image. This key must
	 *            contains a valid file name.
	 * @return the image descriptor if success, <code>null</code> if error.
	 */
	public static ImageDescriptor getResource(final String keyName) {
		ImageDescriptor descriptor = null;
		try {
			final InputStream stream = ImageFileRegistry.class
					.getResourceAsStream(ICONS_PATH + keyName);
			if (stream != null) {
				final ImageData data = new ImageData(stream);
				descriptor = ImageDescriptor.createFromImageData(data);
				stream.close();
			}
		} catch (final Exception e) {
		}
		return descriptor;
	}

	/**
	 * Creates an empty image file registry.
	 * <p>
	 * There must be an SWT Display created in the current thread before calling
	 * this method.
	 * </p>
	 */
	public ImageFileRegistry() {
		super();
		initializeImages();
	}

	/**
	 * Creates an empty image file registry.
	 * 
	 * @param display
	 *            this <code>Display</code> must not be <code>null</code> and
	 *            must not be disposed in order to use this registry.
	 */
	public ImageFileRegistry(final Display display) {
		super(display);
		initializeImages();
	}

	/**
	 * Creates an empty image file registry using the given resource manager to
	 * allocate images.
	 * 
	 * @param manager
	 *            the resource manager used to allocate images.
	 */
	public ImageFileRegistry(final ResourceManager manager) {
		super(manager);
		initializeImages();
	}

	/**
	 * Returns a image for the given file.
	 * 
	 * @param file
	 *            a file used to retrieve image.
	 * @return an image for the given file.
	 */
	public Image get(final File file) {
		// file ?
		if (file == null) {
			return null;
		}

		// the hidden flag
		final boolean hidden = file.isHidden();

		// drive
		if (file.getParentFile() == null) {
			return hidden ? get(KEY_DRIVE_HIDDEN) : get(KEY_DRIVE_DEFAULT);
		}

		// folder
		if (file.isDirectory()) {
			return hidden ? get(KEY_FOLDER_HIDDEN) : get(KEY_FOLDER_DEFAULT);
		}

		// get extension
		final Path path = new Path(file.getAbsolutePath());
		final String extension = path.getFileExtension();

		// image already saved ?
		final Image image = get(extension);
		if (image != null) {
			return image;
		}

		// get program image
		if (extension != null) {
			final Program program = Program.findProgram(extension);
			// program ?
			if (program != null) {
				// get image data
				final ImageData imageData = program.getImageData();
				if (imageData != null) {
					// add
					final ImageDescriptor descriptor = ImageDescriptor
							.createFromImageData(imageData);
					this.put(extension, descriptor);
					return get(extension);
				}
			}
		}

		// default image
		return hidden ? get(KEY_FILE_HIDDEN) : get(KEY_FILE_DEFAULT);
	}

	/**
	 * Load the default images.
	 */
	private void initializeImages() {
		// computer image
		putImageByKey(KEY_COMPUTER);

		// drive images
		putImageByKey(KEY_DRIVE_DEFAULT);
		putImageByKey(KEY_DRIVE_HIDDEN);

		// folder images
		putImageByKey(KEY_FOLDER_DEFAULT);
		putImageByKey(KEY_FOLDER_HIDDEN);

		// file images
		putImageByKey(KEY_FILE_DEFAULT);
		putImageByKey(KEY_FILE_HIDDEN);

		// arrow images
		putImageByKey(KEY_ARROW_RIGHT);
		putImageByKey(KEY_ARROW_LEFT);
		putImageByKey(KEY_ARROW_DOWN);
	}

	/**
	 * Adds a image descriptor for the given key.
	 * 
	 * @param key
	 *            the image key.
	 */
	private void putImageByKey(final String key) {
		// get image
		final ImageDescriptor image = getResource(key);
		if (image != null) {
			put(key, image);
		}
	}
}
