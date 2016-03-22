package edu.wpi.first.javadev.builder.editor.graphics.statemachine;

import java.util.ArrayList;

import edu.wpi.first.javadev.builder.editor.graphics.data.Line;
import edu.wpi.first.javadev.builder.editor.graphics.data.Point2;
import edu.wpi.first.javadev.builder.editor.graphics.data.Zone;

/**
 * This class is a helper class for {@link StateControl}. It represents a side
 * of the state's displayed rectangle. The purpose of it is to hold on to the
 * information about what transitions should emanate from this edge and exactly
 * where they should come from.
 * 
 * @author Joe Grinstead
 */
public class Edge {
	/** The control this Edge is associated with */
	protected StateControl					control;
	/** The side (one of the {@link DIR} constants) this Edge represents */
	protected Zone							side;
	/** A list of {@link TransitionPainter} that emanate from this Edge */
	protected ArrayList<TransitionPainter>	painters;
	/**
	 * A list of Edge that represent the edges the {@link TransitionPainter}
	 * objects in {@link Edge#painters} that they connect to
	 */
	protected ArrayList<Edge>				otherEdges;

	/**
	 * @param control
	 *            the control to be associated with
	 * @param side
	 *            the side of the control the new Edge represents
	 */
	public Edge(StateControl control, Zone side) {
		this.control = control;
		this.side = side;
		painters = new ArrayList<TransitionPainter>();
		otherEdges = new ArrayList<Edge>();
	}

	/**
	 * Gets the point that the given {@link TransitionPainter} should draw
	 * from/to.
	 * 
	 * @param painter
	 *            the {@link TransitionPainter}
	 * @return the point (or null if the painter is not registered to this
	 *         Edge).
	 */
	public Point2 getPoint(TransitionPainter painter) {
		int index = painters.indexOf(painter);
		if (index < 0) return null;
		double denominator = painters.size() + 1;
		double numerator = denominator - (index + 1);
		return getLine().getPoint(numerator / denominator);
	}

	/**
	 * Returns the point in the center of the edge.
	 * 
	 * @return the point in the center of the edge.
	 */
	public Point2 getCenterPoint() {
		return control.getPoint(side);
	}

	/**
	 * Returns the line from the center point of the receiver to the center
	 * point of the alternate edge.
	 * 
	 * @param edge
	 *            the edge to connect to
	 * @return the line connecting the center points
	 */
	public Line getLineTo(Edge edge) {
		return new Line(getCenterPoint(), edge.getCenterPoint());
	}

	/**
	 * Returns the line that this edge represents
	 * 
	 * @return the line that this edge represents
	 */
	public Line getLine() {
		return control.getLine(side);
	}

	/**
	 * Registers the given painter to the list of painters to be notified. This
	 * is the point at which the transitions are sorted.
	 * 
	 * @param painter
	 *            the {@link TransitionPainter} to register
	 * @param edge
	 *            the other edge to which the painter is expected to go
	 */
	public void addPainter(TransitionPainter painter, Edge edge) {
		// Before sorting it into position, check and handle if the
		// transition
		// goes to/from the same state
		// as another one
		for (int i = 0; i < painters.size(); i++) {
			Edge otherEdge = otherEdges.get(i);
			if (edge.equals(otherEdge)) {
				Zone zone = getCenterPoint().getZone(edge.getCenterPoint());
				if (zone.x > 0 || zone == Zone.NORTH) {
					for (; i < painters.size() && otherEdge.equals(otherEdges.get(i)); i++) {}
				}

				painters.add(i, painter);
				otherEdges.add(i, edge);
				return;
			} else {
				for (; i < painters.size() && otherEdge.equals(otherEdges.get(i)); i++) {}
				i--;
				continue;
			}
		}

		// Sorting
		Line line = getLine();
		double angle = line.getAbsoluteAngleBetween(getLineTo(edge));
		for (int i = 0; i < painters.size(); i++) {
			if (angle < line.getAbsoluteAngleBetween(getLineTo(otherEdges.get(i)))) {
				painters.add(i, painter);
				otherEdges.add(i, edge);
				return;
			}
		}
		painters.add(painter);
		otherEdges.add(edge);
	}

	/**
	 * Clears out everything that is registered to this Edge.
	 */
	public void clear() {
		painters.clear();
		otherEdges.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[Edge state:" + control.getStateName() + " side:" + side + "]";
	}
}