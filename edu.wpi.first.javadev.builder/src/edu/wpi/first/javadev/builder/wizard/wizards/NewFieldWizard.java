package edu.wpi.first.javadev.builder.wizard.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import edu.wpi.first.javadev.builder.view.event.FieldWizardFinishEvent;
import edu.wpi.first.javadev.builder.view.event.FieldWizardFinishedEventListener;
import edu.wpi.first.javadev.builder.wizard.pages.NewFieldPage;

public class NewFieldWizard extends Wizard implements INewWizard
{
	// Wizard page - to add new capability
	NewFieldPage fieldPage;
	
	protected IStructuredSelection selection;
	protected IWorkbench workbench;
	
	private String defaultName;
	private String elementName;
	
	//Listener to trigger further action on wizard completion
	FieldWizardFinishedEventListener listener;

	protected NewFieldWizard(String elemName, String defName) {
		super();
		listener = null;
		defaultName = defName;
		elementName = elemName;
	}
	
	public NewFieldWizard(FieldWizardFinishedEventListener inputListener, String elemName, String defName){
		this(elemName, defName);
		listener = inputListener;
	}
	
	@Override
	public void addPages()
	{
		fieldPage = new NewFieldPage(workbench, selection, elementName, defaultName);
		addPage(fieldPage);
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
		return fieldPage.complete();
	}
	
	@Override
	public boolean performFinish() {
		//If a listener is present, notify it of wizard completion
		if(listener != null){listener.receiveEvent(new FieldWizardFinishEvent(
				true,
				fieldPage.getVisibility(),
				fieldPage.getFieldName()));}
		return true;
	}
	
	@Override
	public boolean performCancel(){
		//If a listener is present, notify it of wizard completion
		if(listener != null){listener.receiveEvent(new FieldWizardFinishEvent(false, null, null));}
		return super.performCancel();
	}
}
