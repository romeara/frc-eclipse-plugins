package edu.wpi.first.javadev.projects.wizards.pages;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Page that accepts robot template specific input for project creation,
 * such as package name and robot class name
 * 
 * @author Ryan O'Meara
 */
@SuppressWarnings("restriction")
public class FRCBotProjectWizardPage extends WizardPage {

	private Text robotNameText;
	private Text packageNameText;
	private String defPack, defClass;
	
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
    
    public FRCBotProjectWizardPage(String pageName, String i_defPack, 
    		String i_defClass){
    	super(pageName);
    	setTitle("FRC Java Project Wizard");
    	setDescription("Enter desired robot class and package names");
     	defPack = i_defPack;
     	defClass = i_defClass;
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
     	new Label(composite,SWT.NONE).setText("Package Name");
     	packageNameText = new Text(composite,SWT.BORDER);
     	packageNameText.setText(defPack);
     	packageNameText.setLayoutData(gd);
     	new Label(composite,SWT.NONE).setText("Robot Class Name");
     	robotNameText = new Text(composite,SWT.BORDER);
     	robotNameText.setText(defClass);
     	robotNameText.setLayoutData(gd);
     	packageNameText.addListener(SWT.Modify, nameModifyListener);
     	robotNameText.addListener(SWT.Modify, nameModifyListener);
     	setErrorMessage(null);
     	setMessage(null);
     	setPageComplete(validatePage());
	}
		
	/**
	 * Checks if page input is valid
	 * @return Whether all input is valid
	 */
	protected boolean validatePage(){
   	 	if(packageNameText.getText().equals("") 
   	 			|| robotNameText.getText().equals("") 
   	 			|| (!JavaConventions.validatePackageName(packageNameText.getText(),
   	 					CompilerOptions.VERSION_1_3,
   	 					CompilerOptions.VERSION_1_3).isOK()) 
   	 					|| (!JavaConventions.validateJavaTypeName(
   	 							robotNameText.getText(), 
   	 							CompilerOptions.VERSION_1_3,
   	 							CompilerOptions.VERSION_1_3).isOK())){
   	 		setErrorMessage(null);
   		 
   	 		if(packageNameText.getText().equals("") 
   	 				|| robotNameText.getText().equals("")){
   	 			setMessage("Both fields must have valid entries");
   	 		}else if(!JavaConventions.validatePackageName(
   	 				packageNameText.getText(), 
   	 				CompilerOptions.VERSION_1_3,
   	 				CompilerOptions.VERSION_1_3).isOK()){
   	 			setMessage("Package name is invalid");
   	 		}else{
   	 			setMessage("Robot class name is invalid");
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
 
	/**
	 * Returns the input package name
	 * @return The package name that has been input
	 */
	public String getPackageName(){
		return packageNameText.getText();
	}
 
	/**
	 * Returns the input robot name
	 * @return The robot name that has been input
	 */
	public String getRobotName(){
		return robotNameText.getText();
	}


}
