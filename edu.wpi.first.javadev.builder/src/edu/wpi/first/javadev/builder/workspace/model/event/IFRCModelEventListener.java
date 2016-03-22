package edu.wpi.first.javadev.builder.workspace.model.event;

/**
 * Interface implemented by clients who wish to listener for FRC model changes
 * 
 * @author Ryan O'Meara
 */
public interface IFRCModelEventListener {

	/**
	 * Called when an event occurs
	 * @param event The event which triggered the call
	 */
	public abstract void receiveEvent(FRCModelEvent event);

}
