package edu.wpi.first.javadev.builder.editor.graphics;

import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Control;

/**
 * This class is just like a normal paint listener, except it also has a depth
 * value.  Note that a normal {@link PaintListener} will be treated as a {@link DepthPaintListener} with depth 0.
 * Any {@link CBLinkedControl} or {@link Control} placed on the foreground will act as though it has -inf depth.
 * 
 * @author Joe Grinstead
 */
public interface DepthPaintListener extends PaintListener {

	/**
	 * The depth value, things with a lesser depth value will be displayed above
	 * those with a lower value. Two listeners with the same value will be
	 * arbitrarily placed.
	 * 
	 * @return the depth for this listener
	 */
	public int getDepth();
}
