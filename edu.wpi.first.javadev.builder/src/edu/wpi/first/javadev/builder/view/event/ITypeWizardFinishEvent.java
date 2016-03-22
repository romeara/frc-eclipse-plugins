package edu.wpi.first.javadev.builder.view.event;

import org.eclipse.jdt.core.IType;

/**
 * Event to notify listeners that the given wizard has finished
 * 
 * @author Ryan O'Meara
 */
public class ITypeWizardFinishEvent {
	private boolean finished;
	IType createdType;
	
	public ITypeWizardFinishEvent(boolean didFinish, IType creT){
		finished = didFinish;
		createdType = creT;
	}
	
	public boolean getFinished(){
		return finished;
	}
	
	public IType getCreatedType(){
		return createdType;
	}
}
