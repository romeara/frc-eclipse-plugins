package edu.wpi.first.javadev.builder.editor.graphics.cbswt;

import org.eclipse.swt.graphics.GC;

/**
 * 
 * 
 * @author Joe Grinstead
 */
public interface Paintable {

	/**
	 * Paints the receiver at the given point.
	 * 
	 * @param gc
	 *            the graphics context to paint on
	 * @param x
	 *            the x-value of the graphics
	 * @param y
	 *            the y-value of the graphics
	 */
	public void paintAbsolute(GC gc, int x, int y);

	/**
	 * Paints the receiver at the receiver's chosen point. If the painter is a
	 * control, then this should be equivalent to calling paintAbsolute(gc, x +
	 * getLocation().x, y + getLocation().y);
	 * 
	 * @param gc
	 *            the graphics context to paint on
	 * @param x
	 *            the x-value of the graphics
	 * @param y
	 *            the y-value of the graphics
	 */
	public void paintRelative(GC gc, int x, int y);
}
