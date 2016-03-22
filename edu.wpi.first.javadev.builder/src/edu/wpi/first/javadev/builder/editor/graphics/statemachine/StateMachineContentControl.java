package edu.wpi.first.javadev.builder.editor.graphics.statemachine;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import edu.wpi.first.javadev.builder.editor.graphics.AbsoluteData;
import edu.wpi.first.javadev.builder.editor.graphics.AbsoluteLayout;
import edu.wpi.first.javadev.builder.editor.graphics.Focus;
import edu.wpi.first.javadev.builder.editor.graphics.FocusManager;
import edu.wpi.first.javadev.builder.editor.graphics.MenuSmartMouseListener;
import edu.wpi.first.javadev.builder.editor.graphics.cbswt.CBFrame;
import edu.wpi.first.javadev.builder.editor.graphics.data.LinkedCollection;
import edu.wpi.first.javadev.builder.editor.graphics.data.Point2;
import edu.wpi.first.javadev.builder.view.dnd.StateTransfer;
import edu.wpi.first.javadev.builder.view.dnd.StateTransferType;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.event.IFRCModelEventListener;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRState;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRStateMachine;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRTransition;

/**
 * This class defines the panel where the diagram can be seen.
 * 
 * @author Joe Grinstead
 */
public class StateMachineContentControl extends CBFrame {

	// The qualifying name for the properties
	public static final String						QUALIFIER			= "StatePositions";

	// Depths
	public static final int							SELECTION_BOX_DEPTH	= 0;
	public static final int							BADGE_DEPTH			= 1;
	public static final int							STATE_DEPTH			= 2;
	public static final int							TRANSITION_DEPTH	= 3;
	public static final int							HIGHLIGHT_DEPTH		= 4;

	// Colors
	protected Color									highlightStroke;
	protected Color									standardStroke;
	protected Color									stateBackground;
	protected Color									badgeBackground;

	// Drag and drop
	protected DropTarget							target;

	// Model
	protected FRCRStateMachine						model;
	protected IFRCModelEventListener				listener;
	protected IFile									file;

	// Children
	protected ArrayList<StateControl>				controls;
	protected ArrayList<TransitionPainter>			painters;
	protected BadgeControl							badge;
	protected StateControl							defaultControl;

	// Selections
	protected LinkedCollection<StateControl>		selectedControls;
	protected LinkedCollection<TransitionPainter>	selectedTransitions;
	protected FocusManager							focusManager;
	protected Focus									focus;
	protected SelectionBox							selectionBox;

	// Right click menu
	protected Menu									menu;
	protected LinkedCollection<MenuItem>			menuItems;

	/**
	 * This will instantiate a StateMachineControl. A StateMachineControl is the
	 * main control displayed in the view, it is where all the states and
	 * transitions are drawn.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	public StateMachineContentControl(Composite parent) {
		super(parent, SWT.NO_REDRAW_RESIZE);

		// Create the colors
		highlightStroke = getDisplay().getSystemColor(SWT.COLOR_RED);
		standardStroke = getDisplay().getSystemColor(SWT.COLOR_BLACK);
		stateBackground = new Color(getDisplay(), 0, 220, 220);
		badgeBackground = new Color(getDisplay(), 244, 164, 96);

		// Set the layout
		setLayout(new AbsoluteLayout());
		
		// Focus
		focusManager = new FocusManager();
		focus = new Focus(getFocusManager()) {
			@Override
			public void focusLost() {
				if (selectedTransitions.size() > 0) redraw();
				selectedTransitions.clear();
				selectedControls.clear();
			}
		};

		// Create the badge
		badge = new BadgeControl(this);

		// Default to nothing
		controls = new ArrayList<StateControl>();
		painters = new ArrayList<TransitionPainter>();
		selectedControls = new SelectedControls();
		selectedTransitions = new SelectedTransitions();

		MenuSmartMouseListener mouseListener = new MenuSmartMouseListener() {
			@Override
			public void mouseDown(MouseEvent e, boolean isMenuAction) {
				// Don't do anything if this is actually a menu action
				if (isMenuAction) return;

				// Erase the current selections
				selectedControls.clear();
				selectedTransitions.clear();

				/* Determine if the user selected a transition */

				// Get the mouse's position
				Point2 point = new Point2(e.x, e.y);

				// Create some placeholder variableds
				double minDistance = 0;
				TransitionPainter closestPainter = null;

				// Find the transitions
				for (TransitionPainter painter : painters) {
					double distance = painter.getDistance(point);

					// The transition has to be less than 5 pixels from the
					// mouse and closer than any other one.
					if (distance < 5 && (closestPainter == null || distance < minDistance)) {
						minDistance = distance;
						selectedTransitions.remove(closestPainter);
						closestPainter = painter;
					}
				}

				// If a transitions was found, then give it focus
				if (closestPainter != null) {
					closestPainter.focus.setFocus();
				} else {
					focus.setFocus();
				}
			}
		};
		addMouseListener(mouseListener);
		addMenuDetectListener(mouseListener);

		// Selection box
		selectionBox = new SelectionBox(this);

		// Drop target
		target = new DropTarget(this, DND.DROP_MOVE | DND.DROP_COPY);
		target.setTransfer(new Transfer[] { StateTransfer.getInstance() });
		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				StateMachineContentControl.this.drop(event);
			}
		});

		// Right click menu
		menu = new Menu(this);
		menuItems = new LinkedCollection<MenuItem>() {
			@Override
			public boolean remove(Object element) {
				if (!super.remove(element)) return false;
				if (element != null) ((MenuItem) element).dispose();
				return true;
			}
		};
		menu.addMenuListener(new MenuListener() {

			@Override
			public void menuShown(MenuEvent e) {
				// Delete the old actions
				menuItems.clear();

				// Generate the menu actions
				if (!selectedControls.isEmpty()) {
					MenuItem item = new MenuItem(menu, SWT.CASCADE);
					item.setText("Delete State" + (selectedControls.size() > 1 ? "s" : ""));
					item.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							System.out.println("Delete Called");
						}
					});
					menuItems.add(item);
				}
				if (!selectedTransitions.isEmpty()) {
					MenuItem item = new MenuItem(menu, SWT.CASCADE);
					item.setText("Delete Transition" + (selectedTransitions.size() > 1 ? "s" : ""));
					item.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							// Get the transitions to remove
							int count = 0;
							FRCRTransition[] removals = new FRCRTransition[selectedTransitions.size()];
							for (TransitionPainter painter : selectedTransitions) {
								removals[count] = painter.model;
								count++;
							}
							model.removeTransitions(removals);
						};
					});
					menuItems.add(item);
				}
			}

			@Override
			public void menuHidden(MenuEvent e) {}
		});
		setMenu(menu);
	}

	/**
	 * Returns if placing the control at a given condition would result in an
	 * intersection of two states.
	 * 
	 * @param control
	 *            the control to test
	 * @param x
	 *            the x-coordinate
	 * @param y
	 *            the y-coordinate
	 * @return true if the position is clear of other states.
	 */
	protected boolean positionClear(StateControl control, int x, int y) {
		Point size = ((AbsoluteData) control.getLayoutData()).getSize(control);
		Rectangle newBounds = new Rectangle(x, y, size.x, size.y);
		for (StateControl control2 : controls) {
			// It doesn't matter if it collides with itself
			if (control2 == control) {
				continue;
			}

			// If they intersect, then return false
			Point otherPosition = ((AbsoluteData) control2.getLayoutData()).getLocation();
			Point otherSize = control2.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Rectangle otherBounds = new Rectangle(otherPosition.x, otherPosition.y, otherSize.x, otherSize.y);
			if (otherBounds.intersects(newBounds)) {
				return false;
			}
		}

		// There were no collisions
		return true;
	}

	/**
	 * @return the focusManager
	 */
	public FocusManager getFocusManager() {
		return focusManager;
	}

	/**
	 * This sets the states within the receiver, but does not redraw.
	 * 
	 * @param modelStates
	 *            the states to be displayed within the state machine.
	 */
	public void setStates(FRCRState[] states) {
		// Clear out the old states
		for (StateControl control : controls) {
			control.dispose();
		}
		controls.clear();

		// Create the new ones
		for (int i = 0; i < states.length; i++) {
			FRCRState state = states[i];

			// Default values in case the properties file doesn't contain them.
			int x = 10, y = 20 + 50 * i;

			// Find the location of the state in the properties
			if (file != null) {
				try {
					QualifiedName namex = new QualifiedName(QUALIFIER, state.getDisplayName() + "x");
					QualifiedName namey = new QualifiedName(QUALIFIER, state.getDisplayName() + "y");
					String property = file.getPersistentProperty(namex);
					if (property != null) x = Integer.parseInt(property);
					property = file.getPersistentProperty(namey);
					if (property != null) y = Integer.parseInt(property);
				} catch (CoreException e) {
					e.printStackTrace();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}

			// Add the state
			if (model.getDefaultStateIndex() == i) {
				_setDefaultControl(addState(state, x, y));
			} else {
				addState(state, x, y);
			}
		}
	}

	/**
	 * This will set the transitions that are displayed. It will not redraw.
	 * 
	 * @param transitions
	 *            the model's transitions, or null if all transitions should be
	 *            erased
	 */
	public void setTransitions(FRCRTransition[] transitions) {
		// Clear out the old painters
		for (TransitionPainter painter : painters) {
			painter.dispose();
		}
		painters.clear();

		// Don't do anything if given nothing
		if (transitions == null) return;

		// Add the transitions
		for (FRCRTransition transition : transitions) {
			addTransition(transition);
		}
	}

	private Runnable	finishComputing;
	private int			computeCount	= 0;

	/**
	 * Recomputes and repaints everything to match the model.
	 */
	public void compute() {
		System.out.println("Computing " + computeCount);
		computeCount++;
		setStates(model.getStates());
		setTransitions(null);
		layout();
		if (finishComputing == null) {
			finishComputing = new Runnable() {

				@Override
				public void run() {
					if (!isDisposed()) {
						setTransitions(model.getTransitions());
						for (StateControl control : controls) {
							control.compute();
						}
						redraw();
						badge.redraw();
						finishComputing = null;
					}
				}
			};
			getDisplay().asyncExec(finishComputing);
		}
	}

	/**
	 * Sets the model. The diagram will automatically link itself to the models
	 * changing events.
	 * 
	 * @param model
	 *            the model to display
	 */
	public void setModel(FRCRStateMachine model) {
		System.out.println("CBFrame received model: " + model);
		if (this.model == model) return;

		// Create the listener, if necessary
		if (listener == null) {
			listener = new IFRCModelEventListener() {

				Runnable	runnable	= new Runnable() {

											@Override
											public void run() {
												if (!isDisposed()) compute();
											}
										};

				@Override
				public void receiveEvent(FRCModelEvent event) {
					if (!isDisposed()) getDisplay().asyncExec(runnable);
				}
			};
		}

		// Stop paying attention to the old model
		if (this.model != null) {
			this.model.removeListener(listener);
		}

		// Set the model and start listening to it
		this.model = model;
		this.model.addListener(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wpi.first.graphics.cbswt.CBFrame#dispose()
	 */
	@Override
	public void dispose() {
		target.dispose();
		stateBackground.dispose();
		badgeBackground.dispose();
		if (model != null) model.removeListener(listener);
		super.dispose();
	}

	/**
	 * This will set the default control. Note: it will layout and repaint the
	 * canvas.
	 * 
	 * @param defaultControl
	 *            the defaultControl to set
	 */
	public void setDefaultControl(StateControl defaultControl) {
		FRCRState[] states = model.getStates();
		for (int i = 0; i < states.length; i++) {
			if (states[i].equals(defaultControl.getState())) {
				model.setDefaultState(i);
				break;
			}
		}
		_setDefaultControl(defaultControl);
		layout();
		redraw();
	}

	/**
	 * This will set the default control. Note: it will NOT layout or repaint
	 * the canvas.
	 * 
	 * @param defaultControl
	 *            the defaultControl to set
	 */
	private void _setDefaultControl(StateControl defaultControl) {
		this.defaultControl = defaultControl;
	}

	/**
	 * This will create a {@link StateControl} for the given state and set it's
	 * layout data to the given location. Note:
	 * {@link StateMachineContentControl#layout()} has to be called for the
	 * control to be in the proper location. Note: the creation will be canceled
	 * if there is a collision at the location
	 * 
	 * @param state
	 *            the state to add
	 * @return the control that was generated or null if the location was bad.
	 */
	public StateControl addState(FRCRState state, int x, int y) {
		StateControl control = new StateControl(this, state);
		control.setLayoutData(new AbsoluteData(x, y));
		if (positionClear(control, x, y)) {
			controls.add(control);
			return control;
		} else {
			control.dispose();
			return null;
		}
	}

	/**
	 * This will create a {@link TransitionPainter} of the given transition.
	 * 
	 * @param transition
	 *            the transition to display
	 * @return the generated painter
	 */
	public TransitionPainter addTransition(FRCRTransition transition) {
		TransitionPainter painter = new TransitionPainter(this, transition);
		painters.add(painter);
		painter.registerToControls();
		return painter;
	}

	/**
	 * This will move the {@link StatePanel} to the proper given location. This
	 * will not repaint the screen. Note: if the movement would cause a
	 * collision, it is canceled
	 * 
	 * @param control
	 *            the control to be moved.
	 * @param x
	 *            the x-coordinate of the location.
	 * @param y
	 *            the y-coordinate of the location.
	 * @return whether the move was succesful.
	 */
	public boolean moveStatePanel(StateControl control, int x, int y) {
		if (!positionClear(control, x, y)) return false;

		// Move everything if the object would be off-screen
		translate(-Math.min(x, 0), -Math.min(y, 0));

		// actually place the StatePanel
		control.setLayoutData(new AbsoluteData(Math.max(x, 0), Math.max(y, 0)));

		if (file != null) {
			try {
				QualifiedName namex = new QualifiedName(QUALIFIER, control.getStateName() + "x");
				QualifiedName namey = new QualifiedName(QUALIFIER, control.getStateName() + "y");
				file.setPersistentProperty(namex, Integer.toString(x));
				file.setPersistentProperty(namey, Integer.toString(y));
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}

		// indicate the movement happened
		return true;
	}

	/**
	 * This will translate all the inner workings of this
	 * {@link StateMachinePanel}. It will not redraw anything.
	 * 
	 * @param dx
	 *            the x difference
	 * @param dy
	 *            the y difference
	 */
	public void translate(int dx, int dy) {
		if (dx == 0 && dy == 0) {
			return;
		}
		for (StateControl control : controls) {
			((AbsoluteData) control.getLayoutData()).translate(dx, dy);
		}
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(IFile file) {
		this.file = file;
	}

	/**
	 * Handles the drop event. This method can process both
	 * {@link DND#DROP_COPY} and {@link DND#DROP_MOVE}.
	 * 
	 * @param event
	 *            the event
	 */
	@SuppressWarnings("unused")
	public void drop(DropTargetEvent event) {
		StateControl control = null;
		try {
			// Get information about the event
			Point mouse = toControl(event.x, event.y);
			StateTransferType stateType = (StateTransferType) event.data;

			// Get the x and y values
			int x = mouse.x - stateType.offsetX;
			int y = mouse.y - stateType.offsetY;

			if (event.detail == DND.DROP_COPY) {
				//TODO make this use the correct state constructor
				//control = addState(new FRCRState("CopyOf" + stateType.state.getName()), x, y);
				// ^ Note that the constructor doesn't easily exist, and it will be added when state machines
				// are going to be officially supported.
				if (control == null) {
					event.detail = DND.DROP_NONE;
				} else {
					event.detail = DND.DROP_COPY;
				}
				return;
			}

			// Find out which StatePanel is represented by the dragged
			// in object
			for (StateControl containedControl : controls) {
				if (containedControl.getState().sameName(stateType.state)) {
					control = containedControl;
					break;
				}
			}

			// If it is a new control, then add it
			if (control == null) {
				control = addState(stateType.state, x, y);
				if (control == null) {
					event.detail = DND.DROP_NONE;
				} else {
					event.detail = DND.DROP_MOVE;
				}
				return;
			}

			// Move it
			if (moveStatePanel(control, x, y)) {
				event.detail = DND.DROP_MOVE;
			} else {
				event.detail = DND.DROP_NONE;
			}

		} finally {
			// Check if the new control was alright
			layout();
			redraw();
		}
	}

	/**
	 * @return the selectedControls. This set is intrinsically linked to the
	 *         receiver, changes to the set will take effect.
	 */
	public LinkedCollection<StateControl> getSelectedControls() {
		return selectedControls;
	}

	/**
	 * This contains the live ordered list of selected controls. Adding and
	 * removing from the set, modifies things. This will act like a set in that
	 * an element will not be added if it already is in the collection.
	 * 
	 * @author Joe Grinstead
	 */
	private class SelectedControls extends LinkedCollection<StateControl> {
		@Override
		public boolean add(StateControl element) {
			if (contains(element)) return false;
			element.redrawHighlightedArea();
			return super.add(element);
		}

		@Override
		public boolean remove(Object o) {
			if (!super.remove(o)) return false;
			((StateControl) o).redrawHighlightedArea();
			return true;
		}
	}

	/**
	 * This contains the live ordered list of selected transitions. Adding and
	 * removing from the set, modifies things. This will act like a set in that
	 * an element will not be added if it already is in the collection.
	 * 
	 * @author Joe Grinstead
	 */
	private class SelectedTransitions extends LinkedCollection<TransitionPainter> {
		@Override
		public boolean add(TransitionPainter element) {
			if (contains(element)) return false;
			element.redraw();
			return super.add(element);
		}

		@Override
		public boolean remove(Object o) {
			if (!super.remove(o)) return false;
			((TransitionPainter) o).redraw();
			return true;
		}
	}
}
