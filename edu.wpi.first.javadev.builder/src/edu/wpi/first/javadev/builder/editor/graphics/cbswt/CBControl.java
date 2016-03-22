package edu.wpi.first.javadev.builder.editor.graphics.cbswt;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import edu.wpi.first.javadev.builder.editor.graphics.LayoutListener;
import edu.wpi.first.javadev.builder.editor.graphics.data.DepthSortedSet;

/**
 * 
 * 
 * @author Joe Grinstead
 */
abstract class CBControl extends Composite implements Paintable {

	DepthSortedSet<Paintable>	painters;
	ArrayList<LayoutListener>	layouts;

	/**
	 * @param parent
	 * @param style
	 */
	public CBControl(Composite parent, int style) {
		super(parent, style);

		setBackgroundMode(SWT.INHERIT_DEFAULT);
	}

	public abstract CBFrame getFrame();

	@Override
	public void paintAbsolute(GC gc, int x, int y) {
		if (painters == null) return;
		for (Paintable painter : painters) {
			painter.paintRelative(gc, x, y);
		}
	}

	@Override
	public void paintRelative(GC gc, int x, int y) {
		paintAbsolute(gc, x + getLocation().x, y + getLocation().y);
	}

	public void addCanvasPainter(int depth, Paintable paintable) {
		if (painters == null) painters = new DepthSortedSet<Paintable>();
		painters.add(depth, paintable);
	}

	public void removeCanvasPainter(Paintable paintable) {
		if (painters != null) painters.remove(paintable);
	}

	public void addLayoutListener(LayoutListener listener) {
		if (layouts == null) layouts = new ArrayList<LayoutListener>();
		layouts.add(listener);
	}
	
	public void removeLayoutListener(Object listener) {
		if (layouts == null) return;
		layouts.remove(listener);
	}

	protected void notifyLayoutListeners() {
		if (layouts == null) return;
		for (LayoutListener listener : layouts) {
			listener.laidout();
		}
	}
	
	@Override
	public void layout(boolean changed, boolean all) {
		super.layout(changed, all);
		notifyLayoutListeners();
	}
	
	@Override
	public void layout(Control[] changed) {
		super.layout(changed);
		notifyLayoutListeners();
	}
	
	@Override
	public void dispose() {
		if (painters != null) painters.clear();
		if (layouts != null) layouts.clear();
		super.dispose();
	}
}