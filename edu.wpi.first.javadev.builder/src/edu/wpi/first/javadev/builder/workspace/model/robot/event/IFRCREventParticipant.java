package edu.wpi.first.javadev.builder.workspace.model.robot.event;

import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;

public interface IFRCREventParticipant {
	/**
	 * Receives an event set from the robot to the element
	 * @param event The event received
	 */
	public abstract void receiveRobotEvent(FRCModelEvent event);
	
	/**
	 * Notifies the element's robot of an event
	 * @param event The event to notify the robot of
	 */
	public abstract void notifyRobot(FRCModelEvent event);
}
