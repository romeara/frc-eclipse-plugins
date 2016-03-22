package edu.wpi.first.javadev.builder.view.event;


public class FieldWizardFinishEvent {
	private boolean finished;
	protected String name;
	protected String visibility;
	
	
	public FieldWizardFinishEvent(boolean didFinish, String vis, String fname){
		finished = didFinish;
		visibility = vis;
		name = fname;
	}
	
	public boolean getFinished(){
		return finished;
	}
	
	public String getFieldName(){
		return name;
	}
	
	public String getVisibility(){
		return visibility;
	}
}
