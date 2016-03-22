package edu.wpi.first.javadev.builder.editor.graphics.statemachine;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;

import edu.wpi.first.javadev.builder.editor.graphics.LayoutListener;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRState;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRStateMachine;
import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRTransition;

/**
 * This class is the most outward control and represents the public face of
 * {@link StateMachineContentControl}. For now, it contains scroll bars, and has
 * no other function. However, this state is where a toolbar would be added or
 * anything outside the representation of the diagram.
 * 
 * @author Joe Grinstead
 */
public class StateMachineControl extends ScrolledComposite {
	/** The control containing the diagram */
	StateMachineContentControl	content;

	public StateMachineControl(Composite parent) {
		// Enable horizontal and vertical scrolling
		super(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		setExpandHorizontal(true);
		setExpandVertical(true);

		// Scroll over to whatever control gets the Keyboard focus
		setShowFocusedControl(true);

		// Create the content
		content = new StateMachineContentControl(this);

		// Register the content as the control that needs to be scrolled
		setContent(content);
		
		// Resize the scroll bars to match the size of the diagram
		content.addLayoutListener(new LayoutListener() {
			
			@Override
			public void laidout() {
				setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			}
		});
	}

	/**
	 * Computes the position of everything within the diagram. Also repaints the
	 * control to match the situation.
	 */
	public void compute() {
		content.compute();
	}

	/**
	 * Sets the model which the receiver displays.
	 * 
	 * @param model
	 *            the {@link RtStateMachine} which the receiver represents.
	 */
	public void setModel(FRCRStateMachine model) {
		content.setModel(model);
	}

	/**
	 * Sets the file which represents the model. This contains metadata about
	 * where the states were previously. It is alright if this is null, the
	 * states will be randomly placed on the screen.
	 * 
	 * @param file
	 *            the file the model comes from
	 */
	public void setFile(IFile file) {
		content.setFile(file);
	}

	/**
	 * Puts the focus on the given state (or does nothing if the state does not
	 * exist within the diagram). This will highlight and scroll to the given
	 * state.
	 * 
	 * @param state
	 *            the state to put the focus on
	 */
	public void focusOn(FRCRState state) {
		for (StateControl control : content.controls) {
			if (control.model.equals(state)) {
				control.focus.setFocus();
				return;
			}
		}
	}

	/**
	 * Puts the focus on the given transition (or does nothing if the transition
	 * does not exist within the diagram). This will highlight the given
	 * transition.
	 * 
	 * @param transition
	 *            the transition to put the focus on
	 */
	public void focusOn(FRCRTransition transition) {
		for (TransitionPainter painter : content.painters) {
			if (painter.model.equals(transition)) {
				painter.focus.setFocus();
				return;
			}
		}
	}
}
