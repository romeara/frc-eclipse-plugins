package edu.wpi.first.javadev.builder.editor.graphics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;


/**
 * This is the data used by the {@link AbsoluteLayout}
 * 
 * @author Joe Grinstead
 */
public class AbsoluteData {

	protected Point	location;
	protected Point	size;

	public AbsoluteData(Point location, Point size) {
		this.location = location;
		this.size = size;
	}

	public AbsoluteData(Point location) {
		this(location, null);
	}

	public AbsoluteData(int x, int y, int width, int height) {
		this(new Point(x, y), new Point(width, height));
	}

	public AbsoluteData(int x, int y) {
		this(new Point(x, y));
	}

	public AbsoluteData() {
		this(0, 0);
	}

	public void translate(int dx, int dy) {
		location.x += dx;
		location.y += dy;
	}

	/**
	 * @return the location
	 */
	public Point getLocation() {
		return location;
	}

	public Point getSize(Control control) {
		if (size == null && control != null) {
			return control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		} else if (size != null) {
			return size;
		} else {
			size = new Point(0, 0);
			return size;
		}
	}
}
