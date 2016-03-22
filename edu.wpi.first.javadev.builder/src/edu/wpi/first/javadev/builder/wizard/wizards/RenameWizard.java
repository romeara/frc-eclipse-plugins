package edu.wpi.first.javadev.builder.wizard.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import edu.wpi.first.javadev.builder.view.event.RenameWizardFinishEvent;
import edu.wpi.first.javadev.builder.view.event.RenameWizardFinishedEventListener;
import edu.wpi.first.javadev.builder.wizard.pages.RenamePage;

public class RenameWizard extends Wizard implements INewWizard
{
	// Wizard page - to add new capability
	RenamePage renamePage;
	
	protected IStructuredSelection selection;
	protected IWorkbench workbench;
	
	//Listener to trigger further action on wizard completion
	RenameWizardFinishedEventListener listener;
	
	String originalName;
	int entryType;

	protected RenameWizard() {
		super();
		listener = null;
		entryType = 0;
		originalName = null;
	}
	
	public RenameWizard(RenameWizardFinishedEventListener inputListener, String origName, int nameType){
		this();
		listener = inputListener;
		originalName = origName;
		entryType = nameType;
	}
	
	@Override
	public void addPages()
	{
		renamePage = new RenamePage(workbench, selection, originalName, entryType);
		addPage(renamePage);
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
		return renamePage.complete();
	}
	
	@Override
	public boolean performFinish() {
		//If a listener is present, notify it of wizard completion
		if(listener != null){listener.receiveEvent(new RenameWizardFinishEvent(
				true, 
				renamePage.getName()));}
		return true;
	}
	
	@Override
	public boolean performCancel(){
		//If a listener is present, notify it of wizard completion
		if(listener != null){listener.receiveEvent(new RenameWizardFinishEvent(false, null));}
		return super.performCancel();
	}
}
