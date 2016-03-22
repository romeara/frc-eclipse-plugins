package edu.wpi.first.javadev.builder.view.event;

public interface RenameWizardFinishedEventListener {
	/** Handles the triggered event */
	public abstract void receiveEvent(RenameWizardFinishEvent event);
}
