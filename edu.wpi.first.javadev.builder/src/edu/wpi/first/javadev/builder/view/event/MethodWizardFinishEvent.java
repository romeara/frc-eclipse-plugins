package edu.wpi.first.javadev.builder.view.event;


public class MethodWizardFinishEvent {
	private boolean finished;
	protected String visibility, returnType, name, parameters;
	
	
	public MethodWizardFinishEvent(boolean didFinish, String vis, String rT, String mname, String para){
		finished = didFinish;
		visibility = vis;
		returnType = rT;
		name = mname;
		parameters = para;
	}
	
	public boolean getFinished(){
		return finished;
	}
	
	public String getVisibility(){
		return visibility;
	}
	
	public String getReturnType(){
		return returnType;
	}
	
	public String getMethodName(){
		return name;
	}
	
	public String getParameters(){
		return parameters;
	}
	
	
}
