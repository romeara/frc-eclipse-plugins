package edu.wpi.first.javadev.builder.editor.graphics.data;

import org.eclipse.swt.graphics.Point;

public class Point2 {

	public int	x;
	public int	y;

	/**
	 * Constructs a new point with the given x and y coordinates.
	 * 
	 * @param x
	 *            the x coordinate of the new point
	 * @param y
	 *            the y coordinate of the new point
	 */
	public Point2(final int x, final int y) {
		this.x = x;
		this.y = y;
	}

	public Point2(Point2 point) {
		this(point.x, point.y);
	}

	public Point2(Point point) {
		this(point.x, point.y);
	}

	public Point2 translate(double magnitude, double direction) {
		x += (int) (magnitude * Math.cos(direction));
		y -= (int) (magnitude * Math.sin(direction));
		return this;
	}

	/**
	 * Compares the argument to the receiver, and returns true if they represent
	 * the <em>same</em> object using a class specific comparison.
	 * 
	 * @param object
	 *            the object to compare with this object
	 * @return <code>true</code> if the object is the same as this object and
	 *         <code>false</code> otherwise
	 * 
	 * @see #hashCode()
	 */
	@Override
	public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof Point)) {
			return false;
		}
		Point p = (Point) object;
		return p.x == this.x && p.y == this.y;
	}

	/**
	 * Returns an integer hash code for the receiver. Any two objects that
	 * return <code>true</code> when passed to <code>equals</code> must return
	 * the same value for this method.
	 * 
	 * @return the receiver's hash
	 * 
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {
		return x ^ y;
	}

	/**
	 * Returns a string containing a concise, human-readable description of the
	 * receiver.
	 * 
	 * @return a string representation of the point
	 */
	@Override
	public String toString() {
		return "[Point x:" + x + " y:" + y + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Determines what zone the other point is relative to the receiver. For
	 * example, giving (20, 20) to point (0, 0) would return
	 * {@link DIR#SOUTHEAST}.
	 * 
	 * @param point
	 *            the point
	 * @return the zone
	 */
	public Zone getZone(Point2 point) {
		return Zone.get(point.x - x, point.y - y);
	}

	public Point getPoint() {
		return new Point(x, y);
	}

	/**
	 * Return the direction (in radians) that a line would be between the two
	 * points. Returns -0 if the the points are the exact same.
	 * 
	 * @param point
	 *            the point to go to
	 * @return the direction
	 */
	public double getDirection(Point2 point) {
		Zone zone = getZone(point);

		// If the point is directly above or below (or the exact same)
		if (zone == Zone.CENTER) {
			return -0;
		} else if (zone == Zone.NORTH) {
			return Math.PI * .5;
		} else if (zone == Zone.SOUTH) {
			return Math.PI * 1.5;
		}

		// this is the unaugmented arctan
		double atan = Math.atan(((double) (y - point.y)) / ((double) (point.x - x)));

		// If the point is to the right
		if (zone.x > 0) return zone.y > 0 ? 2.0 * Math.PI + atan : atan;

		// If the point is to the left
		return Math.PI + atan;
	}

	public double getDistanceTo(Point2 point) {
		return Math.sqrt((x - point.x) * (x - point.x) + (y - point.y) * (y - point.y));
	}
}
