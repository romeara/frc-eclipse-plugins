package edu.wpi.first.cppdev.ui.wizard.page;

import org.eclipse.cdt.core.CConventions;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

/**
 * Page that accepts a name for the new element to be created, and validates 
 * that entry before allowing the wizard to continue
 * 
 * @author Ryan O'Meara
 */
public class FRCTemplateFileWizardPage extends WizardPage {

	private Text desiredNameText;
	private String defName;
	private String displayName;
	
	/**
	 * Listener for name modification to alert user to invalid names and prevent 
	 * continuing if invalid input exists
	 */
	private Listener nameModifyListener = new Listener() {
   	 	public void handleEvent(Event e) {
   	 		boolean valid = validatePage();
   	 		setPageComplete(valid);
   	 	}
    };
    
    public FRCTemplateFileWizardPage(String pageName, String dispName, String i_defPack){
    	super(pageName);
    	displayName = dispName;
    	setTitle("FRC C++ New " + displayName + " Wizard");
    	setDescription("Enter new " + displayName.toLowerCase() + " specifications");
     	defName = i_defPack;
     	setPageComplete(false);
    }
    
    @Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
     	GridLayout layout = new GridLayout();
     	GridData gd = new GridData(GridData.FILL_HORIZONTAL);
     	layout.numColumns = 2;
     	composite.setLayout(layout);
     	setControl(composite);
     	new Label(composite,SWT.NONE).setText("Desired " + displayName + " Name");
     	desiredNameText = new Text(composite,SWT.BORDER);
     	desiredNameText.setText(defName);
     	desiredNameText.setLayoutData(gd);
     	desiredNameText.addListener(SWT.Modify, nameModifyListener);
     	setErrorMessage(null);
     	setMessage(null);
     	setPageComplete(validatePage());
	}
		
	/**
	 * Checks if page input is valid
	 * @return Whether all input is valid, including validation of language conventions
	 */
	protected boolean validatePage(){
		IStatus classStat = null, fileStat = null;
		
		//Validate entered name
		if(desiredNameText.getText().equals("") 
   	 			|| (!(classStat = CConventions.validateClassName(desiredNameText.getText())).isOK())
   	 			|| (!(fileStat = CConventions.validateFileName(desiredNameText.getText())).isOK())){
   	 		setErrorMessage(null);
   		 
   	 		if(desiredNameText.getText().equals("")){
   	 			setMessage("Desired name is required");
   	 		}else if(!classStat.isOK()){
   	 			setMessage("Name cannot be used as a class name:\n" + classStat.getMessage());
   	 		}else{
   	 			setMessage("Name cannot be used as a file name:\n" + fileStat.getMessage());
   	 		}
   		 
   	 		return false;
   	 	}
   	 
   	 	setMessage(null);
   	 
   	 	return true;
	}
	
	@Override
	public boolean canFlipToNextPage(){
	 	return false;
	}
 
	@Override
	public boolean isPageComplete(){
		return ((getErrorMessage() == null)&&validatePage());
	}
 
	/** @return The desired name that has been input */
	public String getDesiredName(){
		return desiredNameText.getText();
	}


}
