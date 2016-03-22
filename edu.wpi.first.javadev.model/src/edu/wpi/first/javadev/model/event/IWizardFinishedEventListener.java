package edu.wpi.first.javadev.model.event;

/**
 * Listener for the completion of wizards in edu.wpi.first.javadev.model
 * 
 * @author Ryan O'Meara
 */
public interface IWizardFinishedEventListener {
	
	/**
	 * @param event The finished event to handle
	 */
	public abstract void receiveEvent(WizardFinishedEvent event);
}
