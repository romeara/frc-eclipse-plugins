package edu.wpi.first.javadev.builder.view.event;

public interface MethodWizardFinishedEventListener {
	/** Handles the triggered event */
	public abstract void receiveEvent(MethodWizardFinishEvent event);
}
