package edu.wpi.first.javadev.builder.editor.graphics.statemachine;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import edu.wpi.first.javadev.builder.editor.graphics.AbsoluteData;
import edu.wpi.first.javadev.builder.editor.graphics.Focus;
import edu.wpi.first.javadev.builder.editor.graphics.cbswt.CBComposite;
import edu.wpi.first.javadev.builder.view.dnd.BadgeTransfer;

/**
 * This class draws the badge on top of the default state.
 * 
 * @author Joe Grinstead
 */
public class BadgeControl extends CBComposite {

	/** The {@link DragSource} for this control */
	protected DragSource	source;
	/** The {@link Focus} for this control */
	protected Focus	focus;

	/**
	 * @param parent
	 *            the {@link StateMachineContentControl} this will link to and
	 *            display within
	 */
	public BadgeControl(StateMachineContentControl parent) {
		super(parent, SWT.NONE, StateMachineContentControl.BADGE_DEPTH);

		// Focus
		focus = new Focus(parent.getFocusManager());

		// The diameter may be a member variable, but the circle will always
		// be this diameter
		int diameter = 10;

		// Set the layout data to be locked to the default controlF
		setLayoutData(new AbsoluteData(new Point(0, 0), new Point(diameter, diameter)) {
			@Override
			public Point getLocation() {
				// If the default control goes bad, default to the top left
				// corner (will be invisible)
				if (getParent().defaultControl == null || getParent().defaultControl.isDisposed()) {
					getParent().defaultControl = null;
					return new Point(0, 0);
				}

				// Actual data
				Point controlLocation = ((AbsoluteData) getParent().defaultControl.getLayoutData()).getLocation();
				int offset = 2;
				return new Point(controlLocation.x - offset, controlLocation.y - offset);
			}
		});

		// Get focus whenever clicked
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				focus.setFocus();
			}
		});

		// Drag Source
		source = new DragSource(this, DND.DROP_MOVE);
		source.setTransfer(new Transfer[] { BadgeTransfer.getInstance() });
		source.addDragListener(new DragSourceAdapter() {

			@Override
			public void dragStart(DragSourceEvent event) {
				// Create the image under the mouse
				Rectangle bounds = getBounds();
				Image image = new Image(getDisplay(), bounds.width, bounds.height);
				GC gc = new GC(image);
				gc.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
				gc.fillRectangle(0, 0, bounds.width, bounds.height);
				paintAbsolute(gc, 0, 0);
				event.image = image;
				gc.dispose();

				// Shift it so that it's properly aligned
				event.offsetX = event.x;
				event.offsetY = event.y;

				// Hide until everything is over with
				getParent().redraw();
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
				event.data = "Badge";
			}
		});
	}

	/**
	 * Paints the badge to the given graphics context.
	 * 
	 * @param gc
	 *            the graphics context to draw on
	 * @param x
	 *            the x coordinate of the top left corner
	 * @param y
	 *            the y coordinate of the top left corner
	 */
	public void paintAbsolute(GC gc, int x, int y) {
		// Set up the colors
		gc.setBackground(getParent().badgeBackground);
		gc.setForeground(getParent().standardStroke);

		// Draw the shapes
		Rectangle bounds = getBounds();
		gc.fillOval(x, y, bounds.width, bounds.height);
		gc.drawOval(x, y, bounds.width - 1, bounds.height - 1);
	}

	/**
	 * Returns the {@link StateMachineContentControl} this draws within.
	 */
	@Override
	public StateMachineContentControl getParent() {
		return (StateMachineContentControl) super.getParent();
	}
}
