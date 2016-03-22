package edu.wpi.first.javadev.builder.editor.graphics.statemachine;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import edu.wpi.first.javadev.builder.editor.graphics.AbsoluteData;
import edu.wpi.first.javadev.builder.editor.graphics.AbsoluteLayout;
import edu.wpi.first.javadev.builder.editor.graphics.IgnoreData;
import edu.wpi.first.javadev.builder.editor.graphics.cbswt.Paintable;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.event.IFRCModelEventListener;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRState;

/**
 * This class defines the text (even when editing) on each {@link StateControl}
 * class.
 * 
 * @author Joe Grinstead
 */
public class StateControlText extends Canvas implements Paintable {

	/** The {@link Label} displaying the name */
	protected Label				label;
	protected Label				hiddenLabel;
	/** The {@link Text} that appears when text editing is wanted */
	protected Text				text;
	/**
	 * This will listen to the {@link RtState} and change it's name if necessary
	 */
	protected IFRCModelEventListener	listener;
	/**
	 * This is true if the name should become editable after a single click.
	 * When the user clicks the parent {@link StateControl}, this field is set
	 * to true and is only set to false if the user eventually double-clicks or
	 * if the mouse leaves the parent control.
	 */
	protected boolean			shouldBecomeEditable;

	StateControlText(StateControl parent) {
		super(parent, SWT.NONE);

		setBackgroundMode(SWT.INHERIT_DEFAULT);

		// Pay attention to the model
		registerToModel();

		// Ignore mouse events for this
		setEnabled(false);

		// Layout
		setLayout(new AbsoluteLayout());

		// Text
		createText();

		// Label
		createLabel();

		// Mouse Events
		getParent().addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// A double click cancels the naming action
				shouldBecomeEditable = false;
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if(isEnabled()) {
					// To pass this if statement means that the text is
					// editable, but the user has pressed somewhere else on the
					// parent StateControl, which should therefore cancel the
					// naming process
					setEditable(false);
				} else if(getParent().focus.isFocus()) {
					/*
					 * Start waiting to see if the user has single clicked the
					 * control
					 */

					// Set this to true (it will be set to false if the user
					// double-clicks or moves the cursor out of the parent
					// StateControl).
					shouldBecomeEditable = true;

					// Allow the user to modify the name only after all
					// possibility of double click becomes non-existant
					Display display = getDisplay();
					display.timerExec(display.getDoubleClickTime(), new Runnable() {

						public void run() {
							// If the parent is no longer the one with focus,
							// then cancel the action
							if(shouldBecomeEditable && getParent().focus.isFocus()) {
								// Become editable
								setEditable(true);
							}
						}
					});
				}
			}
		});

		// If the mouse leaves the parent StateControl, then don't switch to
		// text editing
		getParent().addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseExit(MouseEvent e) {
				shouldBecomeEditable = false;
			}
		});
	}

	/**
	 * Register this {@link StateControlText} to the model. If the name changes,
	 * the name displayed will also change.
	 */
	private void registerToModel() {
		listener = new IFRCModelEventListener() {

			@Override
			public void receiveEvent(FRCModelEvent event) {
				if(!isDisposed()) label.setText(getModel().getDisplayName());
			}
		};
		getModel().addListener(listener);
	}

	/**
	 * Creates the label.
	 */
	private void createLabel() {
		label = new Label(this, SWT.SINGLE | SWT.LEFT);
		label.setText(getModel().getDisplayName());
		label.setEnabled(true);
		label.setLayoutData(new AbsoluteData());
	}

	/**
	 * Creates the text.
	 */
	private void createText() {
		// Create the text and make it invisible
		text = new Text(this, SWT.SINGLE | SWT.LEAD);
		text.setVisible(false);
		text.setText("a");
		text.setLayoutData(new AbsoluteData());
		text.setToolTipText("The name of this state");

		// If the text is modified, live update the screen
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				getMachineControl().layout();
				getMachineControl().redraw();
			}
		});

		// If the user hits enter in the text, then apply the changes
		text.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				getModel().renameClass(getShell(), text.getText());
			}
		});

		// If keyboard focus is lost, then hide the text and reverse the changes
		text.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setEditable(false);
			}
		});
	}

	/**
	 * Sets whether the name becomes editable or not. If the argument is true,
	 * the {@link StateControlText#text} will become visible and will have the
	 * entire name highlighted. If the argument is false, the name will be reset
	 * to what it was previously and the {@link StateControlText#text} will be
	 * hidden.
	 * 
	 * @param enabled
	 */
	public void setEditable(boolean enabled) {
		if(enabled && !isEnabled()) {
			// Enable this control
			setEnabled(true);

			// Give the text some default values
			text.setText(getModel().getDisplayName());
			text.setSelection(0, getModel().getDisplayName().length());

			// Make the text visible
			text.setVisible(true);

			// Give focus to the text
			text.setFocus();
		} else if(!enabled && isEnabled()) {
			// Disable the control
			setEnabled(false);

			// Hide the text
			text.setVisible(false);

			// Return to the original name
			text.setText("a");
			label.setText(getModel().getDisplayName());
		}

		// Draw the changes
		getMachineControl().layout();
		getMachineControl().redraw();
	}

	@Override
	public void paintAbsolute(GC gc, int x, int y) {
		if(x == 0 && y == 0) {
			print(gc);
		} else {}
	}

	@Override
	public void paintRelative(GC gc, int x, int y) {
		paintAbsolute(gc, x + getLocation().x, y + getLocation().y);
	}

	public void setPrinting(boolean printing) {
		if(hiddenLabel == null) {
			hiddenLabel = new Label(this, label.getStyle());
			hiddenLabel.setBackground(getMachineControl().stateBackground);
			hiddenLabel.moveAbove(null);
			hiddenLabel.setLayoutData(new IgnoreData());
		}
		if(printing) {
			if(!(hiddenLabel.getText().equals(label.getText())))
				hiddenLabel.setText(label.getText());
			hiddenLabel.setBounds(label.getBounds());
			hiddenLabel.setVisible(true);
		} else {
			hiddenLabel.setVisible(false);
			hiddenLabel.setText("");
		}
	}

	/**
	 * Returns the {@link StateControl} this draws within.
	 * 
	 * @return The {@link StateControl} this draws within.
	 */
	@Override
	public StateControl getParent() {
		return (StateControl)super.getParent();
	}

	/**
	 * Returns the {@link StateMachineContentControl} this draws within.
	 * 
	 * @return The {@link StateMachineContentControl} this draws within.
	 */
	public StateMachineContentControl getMachineControl() {
		return getParent().getParent();
	}

	/**
	 * Returns the model that this control displays the name of.
	 * 
	 * @return The model that this control displays the name of.
	 */
	public FRCRState getModel() {
		return getParent().model;
	}

	@Override
	public void dispose() {
		getModel().removeListener(listener);
		super.dispose();
	}
}
