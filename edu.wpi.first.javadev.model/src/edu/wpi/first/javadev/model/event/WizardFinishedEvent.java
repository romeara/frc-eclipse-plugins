package edu.wpi.first.javadev.model.event;

import org.eclipse.jdt.core.IJavaElement;

public class WizardFinishedEvent {
	private boolean finished;
	private String wizardName;
	private IJavaElement element;
	
	public WizardFinishedEvent(boolean i_finished, String i_wizardName, IJavaElement i_element){
		finished = i_finished;
		wizardName = i_wizardName;
		element = i_element;
	}
	
	public boolean getFinished(){return finished;}
	
	public String getWizardName(){return wizardName;}
	
	public IJavaElement getElement(){return element;}
}
