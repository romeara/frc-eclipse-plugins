package edu.wpi.first.javadev.builder.view.event;

public interface FieldWizardFinishedEventListener {
	/** Handles the triggered event */
	public abstract void receiveEvent(FieldWizardFinishEvent event);
}
