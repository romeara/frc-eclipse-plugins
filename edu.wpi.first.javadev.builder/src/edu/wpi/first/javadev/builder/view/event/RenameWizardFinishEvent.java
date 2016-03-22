package edu.wpi.first.javadev.builder.view.event;


public class RenameWizardFinishEvent {
	private boolean finished;
	protected String name;
	
	
	public RenameWizardFinishEvent(boolean didFinish, String iname){
		finished = didFinish;	
		name = iname;
	}
	
	public boolean getFinished(){
		return finished;
	}
	
	public String getNewName(){
		return name;
	}
}
