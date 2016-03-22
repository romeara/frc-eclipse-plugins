package edu.wpi.first.javadev.builder.editor.graphics.statemachine;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.FillLayout;

import edu.wpi.first.javadev.builder.editor.graphics.DepthPaintListener;
import edu.wpi.first.javadev.builder.editor.graphics.Focus;
import edu.wpi.first.javadev.builder.editor.graphics.cbswt.CBComposite;
import edu.wpi.first.javadev.builder.editor.graphics.cbswt.Paintable;
import edu.wpi.first.javadev.builder.editor.graphics.data.Line;
import edu.wpi.first.javadev.builder.editor.graphics.data.Point2;
import edu.wpi.first.javadev.builder.editor.graphics.data.Zone;
import edu.wpi.first.javadev.builder.view.dnd.BadgeTransfer;
import edu.wpi.first.javadev.builder.view.dnd.StateTransfer;
import edu.wpi.first.javadev.builder.view.dnd.StateTransferType;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRState;

/**
 * This is the control which displays the state and it's information
 * 
 * @author Joe Grinstead
 */
public class StateControl extends CBComposite {

	/** The number of pixels between the Edges and the text */
	public static final int					TEXT_MARGIN	= 6;
	/** The number of pixels between the Edges and the halo */
	public static final int					HALO_MARGIN	= 2;

	/** The model this control represents */
	protected FRCRState						model;
	/** The control which displays all the text */
	protected StateControlText				label;

	// Drag and Drop
	/**
	 * The type that will be given as data to drag and drop events. This is only
	 * created when dragging starts and released immediately when dragging ends.
	 * It is therefore legitimate to check if the state being dragged is this
	 * one by checking if this variable is not null
	 */
	protected StateTransferType				stateType;
	/** The DragSource for this control */
	protected DragSource					source;
	/** The DropTarget for this control */
	protected DropTarget					target;

	// Transition Stuff
	/** The edges surrounding this control */
	protected Edge[]						edges;
	/** The painters that are connected to this control */
	protected ArrayList<TransitionPainter>	painters;

	// Parent Stuff (let go when disposed)
	/**
	 * The {@link DepthPaintListener} that will paint the halo around the
	 * control
	 */
	protected Paintable						haloPainter;

	// Focus
	/** The {@link Focus} for this control */
	protected Focus							focus;

	/**
	 * Creates the {@link StateControl}. This will set up painting, register a
	 * focus, and set up mouse listening.
	 * 
	 * @param parent
	 *            the {@link StateMachineContentControl} that the new
	 *            {@link StateControl} will be a part of.
	 * @param model
	 *            the {@link RtState} this control will represent
	 */
	public StateControl(StateMachineContentControl parent, FRCRState model) {
		super(parent, SWT.NONE, StateMachineContentControl.STATE_DEPTH);

		// Layout
		FillLayout layout = new FillLayout();
		layout.marginHeight = TEXT_MARGIN;
		layout.marginWidth = TEXT_MARGIN;
		setLayout(layout);

		// Focus
		focus = new Focus(parent.getFocusManager()) {
			@Override
			public void focusLost() {
				label.setEditable(false);
				if (!isHighlighted()) redrawHighlightedArea();
			}

			@Override
			public void focusGained() {
				redrawHighlightedArea();
				setFocus();
			}
		};

		// Edges and the painters that register to this
		edges = new Edge[] { new Edge(this, Zone.NORTH), new Edge(this, Zone.WEST), new Edge(this, Zone.SOUTH),
				new Edge(this, Zone.EAST) };
		painters = new ArrayList<TransitionPainter>();

		// Halo Drawing
		haloPainter = new Paintable() {

			@Override
			public void paintRelative(GC gc, int x, int y) {
				Rectangle bounds = getBounds();
				paintAbsolute(gc, x + bounds.x, y + bounds.y);
			}

			@Override
			public void paintAbsolute(GC gc, int x, int y) {
				if (isHighlighted()) {
					Rectangle bounds = getBounds();
					x -= HALO_MARGIN;
					y -= HALO_MARGIN;
					bounds.width += 2 * HALO_MARGIN - 1;
					bounds.height += 2 * HALO_MARGIN - 1;
					gc.setForeground(getParent().highlightStroke);
					gc.drawRoundRectangle(x, y, bounds.width, bounds.height, 8, 8);
				}
			}
		};

		getFrame().addCanvasPainter(StateMachineContentControl.HIGHLIGHT_DEPTH, haloPainter);

		// Model
		this.model = model;

		// Label and Text
		label = new StateControlText(this);

		// Recompute the arrows if this gets moved
		addControlListener(new ControlListener() {

			/** Whether or not this is the first time it was moved */
			boolean	firstRun	= true;

			@Override
			public void controlResized(ControlEvent e) {
				if (!firstRun) controlMoved(e);
			}

			@Override
			public void controlMoved(ControlEvent e) {
				// Don't compute if this is the first movement because not all
				// controls are in their proper place
				if (!firstRun && !isDisposed()) {
					ArrayList<StateControl> controls = new ArrayList<StateControl>(painters.size());

					compute();
					for (TransitionPainter painter : painters) {
						StateControl control = painter.otherControl(StateControl.this);
						if (!control.isDisposed() && !controls.contains(painter.otherControl(StateControl.this))) {
							control.compute();
							controls.add(control);
						}
					}
				} else {
					firstRun = false;
				}
			}
		});

		// Mouse listener (for single and double click events)
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				// Give the focus to this StateControl
				focus.setFocus();
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// When double clicked, open the State this represents in code
				if (StateControl.this.model.openInEditor()) {
					// Give focus to the StateMachine
					getParent().focus.setFocus();
				}
			}
		});

		// Drag Source
		source = new DragSource(this, DND.DROP_MOVE | DND.DROP_LINK | DND.DROP_COPY);
		source.setTransfer(new Transfer[] { StateTransfer.getInstance() });
		source.addDragListener(new DragSourceAdapter() {
			Rectangle	labelBounds;
			Region		region	= new Region(getDisplay());

			@Override
			public void dragStart(DragSourceEvent event) {
				// Give focus to the state machine
				getParent().focus.setFocus();

				// Mark this as the state to be transfered
				if (stateType == null) stateType = new StateTransferType();

				stateType.state = getState();

				/* Create the Image */

				// Important values
				Rectangle bounds = getBounds();
				Rectangle labelBounds = label.getBounds();
				int dx = 0;
				int dy = 0;

				// Extra work if the badge is attached
				if (isDefault()) {
					Rectangle badgeBounds = getParent().badge.getBounds();
					dx = bounds.x - badgeBounds.x;
					dy = bounds.y - badgeBounds.y;
				}

				// Create the image
				Image image = new Image(getDisplay(), bounds.width + dx, bounds.height + dy);
				GC gc = new GC(image);

				// Paint the text
				print(gc);
				if (dx > 0 || dy > 0) gc.copyArea(0, 0, bounds.width, bounds.height, dx, dy);

				// Set the region so everyone has to paint around
				if (!labelBounds.equals(this.labelBounds)) {
					this.labelBounds = labelBounds;
					region.add(0, 0, bounds.width + dx, bounds.height + dy);
					region.subtract(labelBounds.x + dx, labelBounds.y + dy, labelBounds.width, labelBounds.height);
				}
				gc.setClipping(region);

				// Paint The Background
				gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
				gc.fillRectangle(image.getBounds());
				paintAbsolute(gc, dx, dy);

				// Paint the Badge
				if (isDefault()) getParent().badge.paintAbsolute(gc, 0, 0);

				// Set the image under the mouse, and dispose the painter
				event.image = image;
				gc.dispose();

				// Shift the image so that it's properly aligned
				event.offsetX = event.x + dx;
				event.offsetY = event.y + dy;

				// Remember the shift value
				stateType.offsetX = event.x;
				stateType.offsetY = event.y;
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				// Set the data to be transferred
				event.data = stateType;
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
				// Clear out a reference to the stateType
				stateType = null;
			}

			@Override
			protected void finalize() throws Throwable {
				region.dispose();
				super.finalize();
			}
		});

		// Drop Target
		target = new DropTarget(this, DND.DROP_LINK | DND.DROP_MOVE);
		target.setTransfer(new Transfer[] { StateTransfer.getInstance(), BadgeTransfer.getInstance() });
		target.addDropListener(new DropTargetAdapter() {

			@Override
			public void drop(DropTargetEvent event) {
				// Check if the item being dragged is another StateControl
				if (StateTransfer.getInstance().isSupportedType(event.currentDataType)) {

					// Check if the user wants the states to link together
					if (event.detail == DND.DROP_LINK) {
						// Find the state to connect with
						StateTransferType stateType = (StateTransferType) event.data;
						FRCRState state = stateType.state;
						for (FRCRState modelState : getParent().model.getStates()) {
							if (modelState.sameName(state)) {
								state = modelState;
								break;
							}
						}

						// Unselect this control
						setSelected(false);

						// Tell the RtStateMachine to add the transition between
						// the two states
						//TODO make the add transition method take two states and THEN create it so 
						//a source range and declaring type can be used
						//getParent().model.addTransition(new FRCRTransition(state, getState()));
						// ^ Waiting until state machines are supported.
					} else if (event.detail == DND.DROP_MOVE) {
						if (stateType != null) {
							// If the item being dragged is this control, tell
							// the parent about it
							getParent().drop(event);
						} else {
							// Do not allow another control to be placed on top
							// of this control
							event.detail = DND.DROP_NONE;
						}
					} else {
						// Only DROP_MOVE and DROP_LINK are supported
						event.detail = DND.DROP_NONE;
					}
				} else if (BadgeTransfer.getInstance().isSupportedType(event.currentDataType)) {
					// Tell the parent to set this control to be the default
					// control
					getParent().setDefaultControl(StateControl.this);
				}
			}

			@Override
			public void dragLeave(DropTargetEvent event) {
				// Unhighlight if the user leaves the state
				setSelected(false);
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				// Highlight if the conditions are met
				if (StateTransfer.getInstance().isSupportedType(event.currentDataType) && event.detail == DND.DROP_LINK) {
					setSelected(true);
				} else if (BadgeTransfer.getInstance().isSupportedType(event.currentDataType)
						&& event.detail == DND.DROP_MOVE) {
					setSelected(true);
				} else {
					setSelected(false);
				}
			}

			@Override
			public void dragEnter(DropTargetEvent event) {
				if (StateTransfer.getInstance().isSupportedType(event.currentDataType)) {
					// If the dragged item is another state control, then check
					// if the data supports being a DROP_LINK command, and if so
					// set it to that
					if (event.detail == DND.DROP_LINK
							|| (event.detail == DND.DROP_DEFAULT && (event.operations & DND.DROP_LINK) != 0)) {
						// Highlight
						setSelected(true);

						// Mark that it should be a DROP_LINK
						event.detail = DND.DROP_LINK;
					}
				} else if (BadgeTransfer.getInstance().isSupportedType(event.currentDataType)) {
					// If the dragged item is the default state badge, then
					// check
					// if the data supports being a DROP_MOVE command, and if so
					// set it to that
					if (event.detail == DND.DROP_MOVE
							|| (event.detail == DND.DROP_DEFAULT && (event.operations & DND.DROP_MOVE) != 0)) {
						setSelected(true);
						event.detail = DND.DROP_MOVE;
					}
				} else {
					event.detail = DND.DROP_NONE;
				}
			}
		});
	}

	/**
	 * This returns the point of the specified type.
	 * 
	 * @param zone
	 *            the zone to grab the point from
	 * @return a point representing that position
	 */
	public Point2 getPoint(Zone zone) {
		Rectangle bounds = getBounds();

		// Default to the center
		Point2 point = new Point2(bounds.x + bounds.width / 2, bounds.y + bounds.height / 2);

		// North and South
		if (zone.y < 0) {
			point.y = bounds.y;
		} else if (zone.y > 0) {
			point.y = bounds.y + bounds.height;
		}

		// East and West
		if (zone.x < 0) {
			point.x = bounds.x;
		} else if (zone.x > 0) {
			point.x = bounds.x + bounds.width;
		}

		return point;
	}

	/**
	 * This will return the {@link Line} which this edge wraps. It is generated
	 * each time this is called, and changing the line's values do nothing to
	 * this edge.
	 * 
	 * @param side
	 *            the side to get the line for
	 * @return the enclosed line
	 */
	public Line getLine(Zone side) {
		Zone[] zones = side.adjacents();
		return new Line(getPoint(zones[0]), getPoint(zones[1]));
	}

	/**
	 * This will register the given {@link TransitionPainter} to the proper
	 * edge.
	 * 
	 * @param painter
	 *            the {@link TransitionPainter} to register
	 */
	private void _compute(TransitionPainter painter) {
		StateControl otherControl = painter.otherControl(this);
		if (otherControl == null || otherControl.isDisposed()) {
			System.out.println(this + " attempted to connect to " + otherControl);
			return;
		}
		if (!this.equals(otherControl)) {
			Zone zone = getPoint(Zone.CENTER).getZone(otherControl.getPoint(Zone.CENTER));
			zone = zone.intersection(getPoint(zone).getZone(otherControl.getPoint(zone.opposite())));

			Zone[] zones = zone.split();
			if (zones.length == 0) {
				// An error has been hit, most likely, two states overlap
				return;
			} else if (zones.length == 1) {
				// There is only one viable edge to register to
				getEdge(zones[0]).addPainter(painter, otherControl.getEdge(zones[0].opposite()));
			} else {
				// Resolving issues
				Edge edge;
				Edge otherEdge;

				// The lines of the first zone
				Line lineAdjacent = new Line(getPoint(zones[0]), otherControl.getPoint(zones[1].opposite()));
				Line lineOpposite = new Line(getPoint(zones[0]), otherControl.getPoint(zones[0].opposite()));

				// The angles of the first zone
				double angle0 = Math.abs(Math.PI * .5 - getLine(zones[0]).getAngleBetween(lineAdjacent));
				angle0 += Math.abs(Math.PI * .5
						- otherControl.getLine(zones[1].opposite()).getAngleBetween(lineAdjacent));
				double angle1 = Math.abs(Math.PI * .5 - getLine(zones[0]).getAngleBetween(lineOpposite));
				angle1 += Math.abs(Math.PI * .5
						- otherControl.getLine(zones[0].opposite()).getAngleBetween(lineOpposite));
				if (angle0 <= angle1) {
					edge = getEdge(zones[0]);
					otherEdge = otherControl.getEdge(zones[1].opposite());
				} else {
					angle0 = angle1;
					edge = getEdge(zones[0]);
					otherEdge = otherControl.getEdge(zones[0].opposite());
				}

				// The lines of the second zone
				lineAdjacent = new Line(getPoint(zones[1]), otherControl.getPoint(zones[0].opposite()));
				lineOpposite = new Line(getPoint(zones[1]), otherControl.getPoint(zones[1].opposite()));

				// The angles of the second zone
				angle1 = Math.abs(Math.PI * .5 - getLine(zones[1]).getAngleBetween(lineAdjacent));
				angle1 += Math.abs(Math.PI * .5
						- otherControl.getLine(zones[0].opposite()).getAngleBetween(lineAdjacent));
				if (angle1 < angle0) {
					angle0 = angle1;
					edge = getEdge(zones[1]);
					otherEdge = otherControl.getEdge(zones[0].opposite());
				}

				angle1 = Math.abs(Math.PI * .5 - getLine(zones[1]).getAngleBetween(lineOpposite));
				angle1 += Math.abs(Math.PI * .5
						- otherControl.getLine(zones[1].opposite()).getAngleBetween(lineOpposite));
				if (angle1 < angle0) {
					edge = getEdge(zones[1]);
					otherEdge = otherControl.getEdge(zones[1].opposite());
				}

				edge.addPainter(painter, otherEdge);
			}
		}
	}

	/**
	 * Returns the edge that represents the given side.
	 * 
	 * @param side
	 *            the side.
	 * @return the edge that represents the given side.
	 */
	public Edge getEdge(Zone side) {
		for (Edge edge : edges) {
			if (edge.side == side) {
				return edge;
			}
		}
		System.out.println(this + " can't find edge with value: " + side);
		return getEdge(Zone.NORTH);
	}

	/**
	 * Returns the name of the state this represents.
	 * 
	 * @return the name of the state this represents.
	 */
	public String getStateName() {
		return model.getDisplayName();
	}

	@Override
	public void dispose() {
		// Remove the paint listeners
		getFrame().removeCanvasPainter(haloPainter);

		// Clear out anything registered
		painters.clear();
		for (Edge edge : edges) {
			edge.clear();
		}

		// Dispose the drag and drop options
		source.dispose();
		target.dispose();

		// Call to super
		super.dispose();
	}

	/**
	 * Returns the {@link StateMachineContentControl} this draws within.
	 */
	@Override
	public StateMachineContentControl getParent() {
		return (StateMachineContentControl) super.getParent();
	}

	/**
	 * Returns the state represented by the receiver.
	 * 
	 * @return the state represented by the receiver.
	 */
	public FRCRState getState() {
		return model;
	}

	/**
	 * Gets the point that the given {@link TransitionPainter} should draw
	 * from/to.
	 * 
	 * @param painter
	 *            the {@link TransitionPainter}
	 * @return the point (or null if the painter is not registered to this
	 *         control).
	 */
	public Point2 getPoint(TransitionPainter painter) {
		for (Edge edge : edges) {
			Point2 point = edge.getPoint(painter);
			if (point != null) return point;
		}
		return null;
	}

	/**
	 * Registers the given {@link TransitionPainter} to this state. If a painter
	 * is not registered, then {@link StateControl#getPoint(TransitionPainter)}
	 * will return null when given the same painter.
	 * {@link StateControl#compute()} must be called at some point after this in
	 * order that {@link StateControl#getPoint(TransitionPainter)} will function
	 * appropriately.
	 * 
	 * @param painter
	 *            the painter to register.
	 */
	public void addTransition(TransitionPainter painter) {
		painters.add(painter);
	}

	/**
	 * Removes the {@link TransitionPainter} from the list of those registered
	 * to this control.
	 * 
	 * @param painter
	 *            the painter to unregister
	 */
	public void removeTransition(TransitionPainter painter) {
		painters.remove(painter);
	}

	/**
	 * Returns whether the conditions are appropriate for drawing a halo around
	 * this control. The conditions are right if this control has the focus or
	 * if it is a member of {@link StateMachineContentControl#selectedControls}.
	 * 
	 * @return whether the conditions are appropriate for drawing a halo around
	 *         this control.
	 */
	public boolean isHighlighted() {
		return focus.isFocus() || getParent().selectedControls.contains(this);
	}

	/**
	 * This will compute the positions of the transitions on the state
	 */
	public void compute() {
		try {
			// Clear out the old data
			for (Edge edge : edges) {
				edge.clear();
			}

			// Give the transition to the proper edge
			for (TransitionPainter painter : painters) {
				_compute(painter);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns if this control represents the default state.
	 * 
	 * @return if this control represents the default state.
	 */
	public boolean isDefault() {
		return this.equals(getParent().defaultControl);
	}

	/**
	 * This will (depending on the input) add or remove itself from
	 * {@link StateMachineContentControl#selectedControls}.
	 * 
	 * @param selected
	 *            If true, the receiver will be added to the list. If false, the
	 *            receiver will be removed.
	 */
	public void setSelected(boolean selected) {
		if (selected) {
			getParent().getSelectedControls().add(this);
		} else {
			getParent().getSelectedControls().remove(this);
		}
	}

	/**
	 * Tells the {@link StateMachineContentControl} to redraw the area
	 * encompassed by this control and its halo.
	 */
	public void redrawHighlightedArea() {
		Rectangle bounds = getBounds();
		bounds.x -= HALO_MARGIN;
		bounds.y -= HALO_MARGIN;
		bounds.width += 2 * HALO_MARGIN;
		bounds.height += 2 * HALO_MARGIN;
		getFrame().redraw(bounds.x, bounds.y, bounds.width, bounds.height, true);
	}

	@Override
	public void paintAbsolute(GC gc, int x, int y) {
		Point bounds = getSize();
		gc.setBackground(getParent().stateBackground);
		gc.setForeground(getParent().standardStroke);
		gc.fillRoundRectangle(x, y, bounds.x, bounds.y, 8, 8);
		gc.drawRoundRectangle(x, y, bounds.x - 1, bounds.y - 1, 8, 8);

	}

	@Override
	public boolean print(GC gc) {
		label.setPrinting(true);
		boolean worked = super.print(gc);
		label.setPrinting(false);
		return worked;
	}

	@Override
	public String toString() {
		return "[StateCBControl state:" + getStateName() + "]";
	}
}
