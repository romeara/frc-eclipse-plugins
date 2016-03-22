package edu.wpi.first.javadev.builder.view.dnd;

import edu.wpi.first.javadev.builder.workspace.model.robot.FRCRState;

public class StateTransferType {

	public FRCRState	state;
	public int		offsetX;
	public int		offsetY;

	public StateTransferType() {}

	public StateTransferType(FRCRState state) {
		this.state = state;
	}

	public StateTransferType(FRCRState state, int offsetX, int offsetY) {
		this.state = state;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
}
