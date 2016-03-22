package edu.wpi.first.javadev.builder.editor.graphics;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * 
 * 
 * @author Joe Grinstead
 */
public class AbsoluteLayout extends Layout {

	public int				marginWidth		= 0;
	public int				marginHeight	= 0;
	private AbsoluteData	defaultData		= new AbsoluteData();

	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		Point size = new Point(0, 0);

		for (Control child : composite.getChildren()) {
			Rectangle bounds = getPreferredBounds(child, flushCache);
			if (bounds == null) continue;
			size.x = Math.max(size.x, bounds.x + bounds.width);
			size.y = Math.max(size.y, bounds.y + bounds.height);
		}

		// Add in the margins
		size.x += 2 * marginWidth;
		size.y += 2 * marginHeight;

		return size;
	}

	@Override
	protected void layout(Composite composite, boolean flushCache) {
		for (Control child : composite.getChildren()) {
			Rectangle bounds = getPreferredBounds(child, flushCache);
			if (bounds == null) continue;
			child.setBounds(bounds.x + marginWidth, bounds.y + marginHeight, bounds.width, bounds.height);
		}
	}

	protected Rectangle getPreferredBounds(Control child, boolean flushCache) {
		// Get the layout data (or use default data if it doesn't match the
		// format)
		Object childData = child.getLayoutData();
		AbsoluteData layoutData = null;
		if (childData instanceof IgnoreData) {
			return null;
		} else if (childData == null || !(childData instanceof AbsoluteData)) {
			layoutData = defaultData;
		} else {
			layoutData = (AbsoluteData) childData;
		}

		// Get the location and size
		Point location = layoutData.getLocation();
		Point size = layoutData.getSize(child);

		// Return the rectangle
		return new Rectangle(location.x, location.y, size.x, size.y);
	}
}
