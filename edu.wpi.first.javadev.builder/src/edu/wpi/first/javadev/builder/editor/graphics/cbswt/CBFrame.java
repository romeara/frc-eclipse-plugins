package edu.wpi.first.javadev.builder.editor.graphics.cbswt;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

/**
 * 
 * 
 * @author Joe Grinstead
 */
public class CBFrame extends CBControl {

	Image	image;
	Image	alternate;

	public CBFrame(Composite parent, final int style) {
		super(parent, style);
	}

	@Override
	public void redraw(int x, int y, int width, int height, boolean all) {
		Rectangle bounds = getBounds();
		bounds.x = 0;
		bounds.y = 0;

		{
			Image temp = image;
			image = alternate;
			alternate = temp;
		}

		if (image == null || image.isDisposed() || !image.getBounds().equals(bounds)) {
			if (image != null) image.dispose();
			image = new Image(getDisplay(), bounds);
		}

		GC gc = new GC(image);

		gc.setBackground(getBackground());
		gc.fillRectangle(bounds);

		paintAbsolute(gc, 0, 0);

		setBackgroundImage(image);

		gc.dispose();
		super.redraw(x, y, width, height, false);
	};

	@Override
	public void redraw() {
		Rectangle bounds = getBounds();
		redraw(0, 0, bounds.width, bounds.height, false);
	}

	@Override
	public CBFrame getFrame() {
		return this;
	}

	public Image getImage() {
		return image;
	}

	@Override
	public void dispose() {
		if (image != null) image.dispose();
		if (alternate != null) alternate.dispose();
		super.dispose();
	}
}
