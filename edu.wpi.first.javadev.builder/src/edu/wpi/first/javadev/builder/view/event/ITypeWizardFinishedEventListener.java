package edu.wpi.first.javadev.builder.view.event;

/** Listener for the finishing of this plug-ins wizards */
public interface ITypeWizardFinishedEventListener {
	
	/** Handles the triggered event */
	public abstract void receiveEvent(ITypeWizardFinishEvent event);
}
