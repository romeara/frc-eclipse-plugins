package edu.wpi.first.javadev.builder.editor.graphics.data;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class Line {

	/** The first point */
	public Point2	a;
	/** The second point */
	public Point2	b;

	/**
	 * Instantiates a line from a to b
	 * 
	 * @param a
	 *            the start point
	 * @param b
	 *            the end point
	 */
	public Line(Point2 a, Point2 b) {
		this.a = a;
		this.b = b;
	}

	public Point getOrigin() {
		return a.getPoint();
	}

	public Point getDestination() {
		return b.getPoint();
	}

	public double getLength() {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}

	/**
	 * Returns the minimum distance from the given point to this line (which is
	 * really a segment).
	 * 
	 * @param c
	 *            the point
	 * @return the distance between the point and the line
	 */
	public double getDistance(Point2 c) {
		if (getAngleTo(c) >= Math.PI / 2) {
			return a.getDistanceTo(c);
		} else if (getAlteriorAngleTo(c) >= Math.PI / 2) {
			return b.getDistanceTo(c);
		} else {
			return Math.abs(Math.sin(getAngleTo(c)) * a.getDistanceTo(c));
		}
	}

	/**
	 * @return direction in radians
	 */
	public double getDirection() {
		return a.getDirection(b);
	}

	public Point2 getPoint(double percentage) {
		return new Point2(a).translate(getLength() * percentage, getDirection());
	}

	public Point2 getPointWithX(int x) {
		if (a.x == b.x) return (a.x == x ? getPoint(.5) : null);
		double denominator = Math.abs(b.x - a.x);
		if (a.x <= x && x <= b.x) return getPoint(((double) (x - a.x)) / denominator);
		if (a.x >= x && x >= b.x) return getPoint(1.0 - ((double) (x - b.x)) / denominator);
		return null;
	}

	public Point2 getPointWithY(int y) {
		if (a.y == b.y) return (a.y == y ? getPoint(.5) : null);
		double denominator = Math.abs(b.y - a.y);
		if (a.y <= y && y <= b.y) return getPoint(((double) (y - a.y)) / denominator);
		if (a.y >= y && y >= b.y) return getPoint(1.0 - ((double) (y - b.y)) / denominator);
		return null;
	}

	public double getAngleBetween(Line line) {
		return Math.abs(line.getDirection() - getDirection()) % Math.PI;
	}

	public double getAbsoluteAngleBetween(Line line) {
		double toReturn = line.getDirection() - getDirection();
		toReturn %= Math.PI;
		if (toReturn < 0) {
			toReturn += Math.PI;
		}
		return toReturn;
	}

	/**
	 * Returns the interior angle of the vertex BAC in the triangle defined by A
	 * and B of this line and the given point.
	 * 
	 * @param c
	 *            the third point
	 * @return the angle (in radians)
	 */
	public double getAngleTo(Point2 c) {
		double toReturn = Math.abs(a.getDirection(b) - a.getDirection(c));
		return (toReturn > Math.PI ? 2 * Math.PI - toReturn : toReturn);
	}

	/**
	 * Returns the interior angle of the vertex ABC in the triangle defined by A
	 * and B of this line and the given point.
	 * 
	 * @param c
	 *            the third point
	 * @return the angle (in radians)
	 */
	public double getAlteriorAngleTo(Point2 c) {
		return Math.abs(b.getDirection(a) - b.getDirection(c));
	}

	/**
	 * Calculates and returns a line parallel to this one with the given offset
	 * 
	 * @param offset
	 *            the offset (in pixels). A negative value will return a
	 *            parallel line on the opposite side of the equivalent positive
	 *            offset.
	 * @return the parallel line
	 */
	public Line getParallelLine(int offset) {
		double direction = getDirection() + Math.PI / 2;
		Point2 a = new Point2(this.a);
		Point2 b = new Point2(this.b);

		a.translate(offset, direction);
		b.translate(offset, direction);

		return new Line(a, b);
	}

	public void shift(int offset) {
		double direction = getDirection();

		a.translate(offset, direction);
		b.translate(offset, direction);
	}

	public boolean intersectsZone(Point2 point, Zone zone) {
		Zone zoneA = point.getZone(a);
		Zone zoneB = point.getZone(b);

		if (zoneA == Zone.CENTER || zoneB == Zone.CENTER) return true;
		if (zoneA == zone || zoneB == zone) return true;
		if (zoneA == zoneB) return false;
		if (zoneA == zone.opposite() || zoneB == zone.opposite()) return false;

		Point2 pointX = getPointWithX(point.x);
		Point2 pointY = getPointWithY(point.y);

		if (pointX == null || pointY == null) return false;
		if ((pointX.y - point.y) * zone.y < 0) return false;
		if ((pointY.x - point.x) * zone.x < 0) return false;
		return true;
	}

	public boolean intersects(Rectangle r) {
		return intersectsZone(new Point2(r.x, r.y), Zone.SOUTHEAST)
				&& intersectsZone(new Point2(r.x + r.width, r.y + r.height), Zone.NORTHWEST)
				&& intersectsZone(new Point2(r.x, r.y + r.height), Zone.NORTHEAST)
				&& intersectsZone(new Point2(r.x + r.width, r.y), Zone.SOUTHWEST);
	}

	public Rectangle getArea() {
		return new Rectangle(Math.min(a.x, b.x), Math.min(a.y, b.y), Math.abs(a.x - b.x), Math.abs(a.y - b.y));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[Line a:" + a + " b:" + b + "]";
	}
}
