package edu.wpi.first.javadev.builder.wizard.wizards;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.ui.wizards.NewClassCreationWizard;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import edu.wpi.first.javadev.builder.CodeViewerPlugin;
import edu.wpi.first.javadev.builder.view.event.ITypeWizardFinishedEventListener;
import edu.wpi.first.javadev.builder.view.event.ITypeWizardFinishEvent;

/**
 * Hijacks the Java new class wizard to add some functionality, like adding instances
 * of the new class to existing code
 * 
 * @author Ryan O'Meara
 */
@SuppressWarnings("restriction")
public class NewFRCRElementWizard extends NewClassCreationWizard{
	ITypeWizardFinishedEventListener listener;
	private String interfaceQualName;
	
	public NewFRCRElementWizard(String interfaceQualifiedName, ITypeWizardFinishedEventListener inListen){
		super(null, true);
		listener = inListen;
		interfaceQualName = interfaceQualifiedName;
	}
	
	@Override 
	 public void addPages(){
		 super.addPages();
		 for(IWizardPage current : getPages()){
				if(current instanceof NewClassWizardPage){
					((NewClassWizardPage)current).addSuperInterface(interfaceQualName);
					try {
						((NewClassWizardPage)current).setPackageFragmentRoot(CodeViewerPlugin.getDefault().getActiveProject().getPackageFragmentRoots()[0], true);
						((NewClassWizardPage)current).setPackageFragment(CodeViewerPlugin.getFRCModel().getActiveProject().getRobot().getRobotPackageFragment(), true);
					} catch (Exception e) {e.printStackTrace();}
				}
			}
	 }
	
	@Override
	public boolean performFinish(){
		boolean ret = super.performFinish();
		if(listener != null){listener.receiveEvent(new ITypeWizardFinishEvent(true, getCreated()));}
		return ret;
	}
	
	@Override
	public boolean performCancel(){
		boolean ret = super.performCancel();
		if(listener != null){listener.receiveEvent(new ITypeWizardFinishEvent(false, getCreated()));}
		return ret;
	}
	
	/** Returns the created IType */
	protected IType getCreated(){
		IType created = null;
		for(IWizardPage current : getPages()){
			if(current instanceof NewClassWizardPage){
				created = ((NewClassWizardPage)current).getCreatedType();
			}
		}
		
		return created;
	}
	
	@Override
	protected void selectAndReveal(IResource newResource) {
		//Does not call super, as the double call will cause an eclipse exception, and the wizard will not finish properly
		try{BasicNewResourceWizard.selectAndReveal(newResource, CodeViewerPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow());}catch(Exception e){}
	}
}
