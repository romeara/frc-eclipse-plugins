package edu.wpi.first.javadev.model.action;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import edu.wpi.first.javadev.model.ModelPlugin;
import edu.wpi.first.javadev.model.wizard.NewFRCClassWizard;
import edu.wpi.first.javadev.model.wizard.elements.CommandGroupWizard;
import edu.wpi.first.javadev.model.wizard.elements.CommandWizard;
import edu.wpi.first.javadev.model.wizard.elements.PIDSubsystemWizard;
import edu.wpi.first.javadev.model.wizard.elements.SubsystemWizard;

public class FRCElementActionDelegate implements IObjectActionDelegate {
	/* Functions called on menu click (in order):
	 * setActivePart
	 * selectionChanged (also called alone when selection changes, or eclipse closes (toString = <empty selection>)
	 * run
	 */
	
	protected final String COMMAND_ACTION_ID = "edu.wpi.first.javadev.model.commandAction";
	protected final String COMMAND_GROUP_ACTION_ID = "edu.wpi.first.javadev.model.commandGroupAction";
	protected final String SUBSYSTEM_ACTION_ID = "edu.wpi.first.javadev.model.subsystemAction";
	protected final String PID_SUBSYSTEM_ACTION_ID = "edu.wpi.first.javadev.model.pidSubsystemAction";
	
	protected final String PACKAGE_EXPLORER_COMPARE = "Package Explorer";
	
	protected Object selectedObjects;
	protected boolean packageExplorer;

	public FRCElementActionDelegate() {
		selectedObjects = null;
		packageExplorer = false;
	}

	@Override
	public void run(IAction action) {
		NewFRCClassWizard wizard = null;
		
		if(selectedObjects != null){
			StructuredSelection selection = new StructuredSelection(selectedObjects);
			
			if(action.getId().equals(COMMAND_ACTION_ID)){
				wizard = new CommandWizard();
			}else if(action.getId().equals(COMMAND_GROUP_ACTION_ID)){
				wizard = new CommandGroupWizard();
			}else if(action.getId().equals(SUBSYSTEM_ACTION_ID)){
				wizard = new SubsystemWizard();
			}else if(action.getId().equals(PID_SUBSYSTEM_ACTION_ID)){
				wizard = new PIDSubsystemWizard();
			}
			
			if(wizard != null){
				wizard.init(ModelPlugin.getDefault().getWorkbench(), selection);
			    WizardDialog dialog = new WizardDialog(ModelPlugin.getDefault().getWorkbench().getDisplay().getActiveShell(), wizard);
			    dialog.create();
			    dialog.open();
			}
		}
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		if((packageExplorer)&&isFRCAction(action)&&(!selection.toString().equals("<empty selection>"))){
			String selectionString = selection.toString();
			selectionString = selectionString.trim();
			selectionString = selectionString.substring(3);
			selectionString = selectionString.substring(0,selectionString.length() - 1);
			
			selectedObjects = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot().findMember(selectionString));
			
			if(selectedObjects == null){
				selectedObjects = ResourcesPlugin.getWorkspace().getRoot().findMember(selectionString);
			}
		}else{
			selectedObjects = null;
		}
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		if(isFRCAction(action)){packageExplorer = targetPart.getTitle().equals(PACKAGE_EXPLORER_COMPARE);}
	}
	
	/**
	 * Determines if the action is one the actions defined by the 
	 * edu.wpi.first.javadev.model plug-in 
	 * @param action The action to analyze
	 * @return True if the action is defined by the plug-in, and is handled by 
	 * this delegate, false otherwise
	 */
	protected boolean isFRCAction(IAction action){
		String id = action.getId();
		
		return ((id.equals(COMMAND_ACTION_ID))
				||(id.equals(COMMAND_GROUP_ACTION_ID))
				||(id.equals(SUBSYSTEM_ACTION_ID))
				||(id.equals(PID_SUBSYSTEM_ACTION_ID)));
	}

}
