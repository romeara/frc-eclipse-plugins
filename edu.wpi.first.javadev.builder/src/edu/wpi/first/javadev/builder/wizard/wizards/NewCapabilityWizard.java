package edu.wpi.first.javadev.builder.wizard.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import edu.wpi.first.javadev.builder.view.event.MethodWizardFinishEvent;
import edu.wpi.first.javadev.builder.view.event.MethodWizardFinishedEventListener;
import edu.wpi.first.javadev.builder.wizard.pages.NewCapabilityPage;

public class NewCapabilityWizard extends Wizard implements INewWizard
{
	// Wizard page - to add new capability
	NewCapabilityPage capPage;
	
	protected IStructuredSelection selection;
	protected IWorkbench workbench;
	
	//Listener to trigger further action on wizard completion
	MethodWizardFinishedEventListener listener;

	protected NewCapabilityWizard() {
		super();
		listener = null;
	}
	
	public NewCapabilityWizard(MethodWizardFinishedEventListener inputListener){
		this();
		listener = inputListener;
	}
	
	@Override
	public void addPages()
	{
		capPage = new NewCapabilityPage(workbench, selection);
		addPage(capPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) 
	{
		this.workbench = workbench;
		this.selection = selection;
		
	}

	@Override
	public boolean canFinish()
	{
		return capPage.complete();
	}
	
	@Override
	public boolean performFinish() {
		//If a listener is present, notify it of wizard completion
		if(listener != null){listener.receiveEvent(new MethodWizardFinishEvent(
				true, 
				capPage.getVisibility(), 
				capPage.getReturnType(), 
				capPage.getMethodName(), 
				capPage.getParameters()));}
		return true;
	}
	
	@Override
	public boolean performCancel(){
		//If a listener is present, notify it of wizard completion
		if(listener != null){listener.receiveEvent(new MethodWizardFinishEvent(false, null, null, null, null));}
		return super.performCancel();
	}
}
