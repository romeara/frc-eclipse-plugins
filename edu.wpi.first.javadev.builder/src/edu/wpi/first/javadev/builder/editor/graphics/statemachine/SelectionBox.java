package edu.wpi.first.javadev.builder.editor.graphics.statemachine;

import org.eclipse.swt.events.DragDetectEvent;
import org.eclipse.swt.events.DragDetectListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import edu.wpi.first.javadev.builder.editor.graphics.Focus;
import edu.wpi.first.javadev.builder.editor.graphics.cbswt.Paintable;
import edu.wpi.first.javadev.builder.editor.graphics.data.Point2;

/**
 * 
 * 
 * @author Joe Grinstead
 */
class SelectionBox implements Paintable {
	protected Point2							start;
	protected Point2							end;
	protected Focus								focus;
	protected final StateMachineContentControl	composite;

	public SelectionBox(final StateMachineContentControl composite) {
		this.composite = composite;

		focus = new Focus(composite.getFocusManager());

		composite.addCanvasPainter(StateMachineContentControl.SELECTION_BOX_DEPTH, this);

		composite.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseUp(MouseEvent e) {
				if (focus.isFocus()) {
					composite.focus.setFocus();
					composite.redraw();
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
				start = new Point2(e.x, e.y);
				end = new Point2(e.x + 1, e.y + 1);
			}
		});

		composite.addDragDetectListener(new DragDetectListener() {

			@Override
			public void dragDetected(DragDetectEvent e) {
				if (composite.focus.isFocus()) {
					focus.setFocus();
				}
			}
		});

		composite.addMouseMoveListener(new MouseMoveListener() {

			@Override
			public void mouseMove(MouseEvent e) {
				if (focus.isFocus()) {
					redraw();

					end.x = e.x;
					end.y = e.y;

					// Search for collisions
					Rectangle area = getArea();
					for (TransitionPainter painter : composite.painters) {
						if (painter.intersects(area)) {
							composite.selectedTransitions.add(painter);
						} else {
							composite.selectedTransitions.remove(painter);
						}
					}
					for (StateControl control : composite.controls) {
						if (control.getBounds().intersects(area)) {
							composite.selectedControls.add(control);
						} else {
							composite.selectedControls.remove(control);
						}
					}

					// Redraw the box
					redraw();
				}
			}
		});
	}

	@Override
	public void paintAbsolute(GC gc, int x, int y) {
		if (focus.isFocus()) {
			gc.setForeground(composite.standardStroke);
			Rectangle area = getArea();
			gc.drawRectangle(x, y, area.width, area.height);
		}
	}

	@Override
	public void paintRelative(GC gc, int x, int y) {
		if (focus.isFocus()) {
			gc.setForeground(composite.standardStroke);
			Rectangle area = getArea();
			gc.drawRectangle(x + area.x, y + area.y, area.width, area.height);
		}
	}

	public void redraw() {
		Rectangle area = getArea();
		composite.redraw(area.x - 1, area.y - 1, area.width + 2, area.height + 2, true);
	}

	protected Rectangle getArea() {
		int x = Math.min(start.x, end.x);
		int y = Math.min(start.y, end.y);
		int width = Math.abs(end.x - start.x);
		int height = Math.abs(end.y - start.y);
		return new Rectangle(x, y, width, height);
	}
}
