package edu.wpi.first.javadev.builder.editor.graphics.statemachine;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;

import edu.wpi.first.javadev.builder.editor.graphics.Focus;
import edu.wpi.first.javadev.builder.editor.graphics.cbswt.Paintable;
import edu.wpi.first.javadev.builder.editor.graphics.data.Line;
import edu.wpi.first.javadev.builder.editor.graphics.data.Point2;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRState;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRTransition;

/**
 * This class paints a single transition.
 * 
 * @author Joe Grinstead
 */
public class TransitionPainter implements Paintable {
	/**
	 * The margin (in pixels) between the highlight outlines and the actual
	 * arrow
	 */
	public static final int					HALO_MARGIN	= 3;

	/** The model this painter represents */
	protected FRCRTransition					model;

	protected StateMachineContentControl	composite;
	protected StateControl					beginningControl;
	protected StateControl					endingControl;
	protected Focus							focus;

	public TransitionPainter(StateMachineContentControl composite, FRCRTransition transition) {
		this.composite = composite;
		this.model = transition;

		composite.addCanvasPainter(StateMachineContentControl.SELECTION_BOX_DEPTH, this);

		focus = new Focus(composite.getFocusManager()) {
			@Override
			public void focusLost() {
				redraw();
			}

			@Override
			public void focusGained() {
				redraw();
			}
		};
	}

	public double getDistance(Point2 p) {
		Line line = getLine();
		if (line == null) return Double.MAX_VALUE;
		return line.getDistance(p);
	}

	public boolean intersects(Rectangle r) {
		Line line = getLine();
		if (line == null) return false;
		return line.intersects(r);
	}

	public FRCRState getOrigin() {
		return model.getOriginState();
	}

	public FRCRState getDestination() {
		return model.getDestinationState();
	}

	public void registerToControls() {
		for (StateControl control : composite.controls) {
			if (control.getState().sameName(getOrigin())) {
				if (control != beginningControl) {
					if (beginningControl != null) beginningControl.removeTransition(this);
					beginningControl = control;
					beginningControl.addTransition(this);
				}
			}
			if (control.getState().sameName(getDestination())) {
				if (control != endingControl) {
					if (endingControl != null) endingControl.removeTransition(this);
					endingControl = control;
					endingControl.addTransition(this);
				}
			}
		}
		if (beginningControl == null || endingControl == null) {
			System.out.println(this + " was unsuccessful");
		}
	}

	public Line getLine() {
		// Make sure the line can be gotten
		if (beginningControl == null || endingControl == null) return null;
		if (beginningControl.isDisposed() || endingControl.isDisposed()) return null;

		// Get the line
		Line line = new Line(beginningControl.getPoint(this), endingControl.getPoint(this));

		// Only return the line if both points exist
		return (line.a == null || line.b == null ? null : line);
	}

	@Override
	public void paintAbsolute(GC gc, int x, int y) {
		Rectangle area = getArea();
		if (area == null) return;
		paintRelative(gc, -area.x, -area.y);
	}

	public void paintRelative(GC g, int x, int y) {
		// Get the line
		Line line = getLine();

		// Watch out for errors
		if (line == null) return;

		// Translate
		line.a.x += x;
		line.a.y += y;
		line.b.x += x;
		line.b.y += y;

		// Set the colors
		g.setForeground(composite.standardStroke);
		g.setBackground(composite.standardStroke);
		int oldInterpolation = g.getInterpolation();
		g.setInterpolation(SWT.LOW);

		// Draw the line
		drawLine(g, line);

		// Drawing the Arrow Head
		double direction = Math.PI + line.getDirection();
		double quarterTurn = Math.PI * .5;
		Point2 p0 = new Point2(line.b).translate(8.66, direction);
		Point2 p1 = new Point2(p0).translate(5, direction + quarterTurn);
		Point2 p2 = new Point2(p0).translate(5, direction - quarterTurn);
		g.fillPolygon(new int[] { line.b.x, line.b.y, p1.x, p1.y, p2.x, p2.y });

		// Draw the highlights (if necessary)
		if (isHighlighted()) {
			g.setForeground(composite.highlightStroke);
			Point2 p3 = new Point2(p0).translate(HALO_MARGIN, direction + quarterTurn);
			Point2 p4 = new Point2(p0).translate(HALO_MARGIN, direction - quarterTurn);
			Point2 p01 = new Point2(line.a).translate(-8.66, direction);
			Point2 p5 = new Point2(p01).translate(HALO_MARGIN, direction + quarterTurn);
			Point2 p6 = new Point2(p01).translate(HALO_MARGIN, direction - quarterTurn);

			g.drawPolyline(new int[] { p3.x, p3.y, p5.x, p5.y, line.a.x, line.a.y, p6.x, p6.y, p4.x, p4.y });
		}

		// Reset the interpolation
		g.setInterpolation(oldInterpolation);
	}

	public void drawLine(GC g, Line line) {
		g.drawLine(line.a.x, line.a.y, line.b.x, line.b.y);
	}

	public boolean isHighlighted() {
		return focus.isFocus() || composite.selectedTransitions.contains(this);
	}

	public StateControl otherControl(StateControl stateCBControl) {
		return (stateCBControl == endingControl ? beginningControl : endingControl);
	}

	public void dispose() {
		focus.dispose();
		if (beginningControl != null) {
			beginningControl.removeTransition(this);
			beginningControl = null;
		}
		if (endingControl != null) {
			endingControl.removeTransition(this);
			endingControl = null;
		}
		composite.removeCanvasPainter(this);
		composite = null;
	}

	public void redraw() {
		Rectangle area = getArea();
		if (area == null) return;
		composite.redraw(area.x - HALO_MARGIN - 1, area.y - HALO_MARGIN - 1, area.width + HALO_MARGIN + 3, area.height
				+ HALO_MARGIN + 3, false);
	}

	public Rectangle getArea() {
		Line line = getLine();
		return line == null ? null : line.getArea();
	}

	@Override
	public String toString() {
		return "[TPainter from:" + getOrigin() + " to:" + getDestination() + "]";
	}
}