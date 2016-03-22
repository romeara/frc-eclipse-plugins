package edu.wpi.first.javadev.builder.wizard.pages;

import java.util.StringTokenizer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

import edu.wpi.first.javadev.builder.CodeViewerPlugin;
import edu.wpi.first.javadev.builder.util.ProjectClasses;
import edu.wpi.first.javadev.builder.workspace.model.FRCProject;


public class NewCapabilityPage extends WizardPage implements Listener
{
	
	public static final String DEFAULT_RETURN_TYPE = "void";
	public static final String DEFAULT_CAPABILITY_NAME = "newCapability";

	IWorkbench workbench;
	IStructuredSelection selection;
	
	Label instructionLabel;
	Text returnText;
	Text nameText;
	Text parameterText;
	Button publicButton;
	Button protectedButton;
	Button privateButton;
	
	IStatus methodNameStatus;
	IStatus parameterStatus;
	IStatus returnTypeStatus;

	
	/**
	 * Constructor for HolidayMainPage.
	 */
	public NewCapabilityPage(IWorkbench workbench, IStructuredSelection selection) {
		super("Capability Wizard");
		setTitle("Capability Wizard");
		setDescription("Select the properties of the capability to be added");
		this.workbench = workbench;
		this.selection = selection;
		parameterStatus = new Status(IStatus.OK, "not_used", 0, "", null);
		methodNameStatus = new Status(IStatus.OK, "not_used", 0, "", null);
		returnTypeStatus = new Status(IStatus.OK, "not_used", 0, "", null);	
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		
		//Create the composite to hold wizard elements
		GridData gd;
		Composite composite =  new Composite(parent, SWT.NULL);

	    // create the desired layout for this wizard page - four columns and otherwise standard layout
		GridLayout gl = new GridLayout();
		int ncol = 4;
		gl.numColumns = ncol;
		composite.setLayout(gl);
		
		//Instruction label
		instructionLabel = new Label(composite, SWT.NONE);
		instructionLabel.setText("- The return type is void, any Java primitive or a Java class\n" +
				"- The capability name will be the name of a method in the element the capability is being added to\n" +
				"- Parameters are a set of type and name.  The type is the class or primitive type of data the parameter\n" + 
				"is, while the name is the label for that data.  Each set is separated by a comma");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol;
		instructionLabel.setLayoutData(gd);
		
		//Create divider line
		createLine(composite, ncol);
		
		// Choice of visibility - public, protected, private
		new Label (composite, SWT.NONE).setText("Visibility:");
		
		publicButton = new Button(composite, SWT.RADIO);
		publicButton.setText("Public");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol-1;
		publicButton.setLayoutData(gd);
		publicButton.setSelection(true);
		
		protectedButton = new Button(composite, SWT.RADIO);
		protectedButton.setText("Protected");
		gd.horizontalSpan = ncol-2;
		protectedButton.setLayoutData(gd);
		
		privateButton = new Button(composite, SWT.RADIO);
		privateButton.setText("Private");
		gd.horizontalSpan = ncol-3;
		privateButton.setLayoutData(gd);
		
		//Create divider line
		createLine(composite, ncol);

		// method return type				
		new Label (composite, SWT.NONE).setText("Return Type:");				
		returnText = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol - 1;
		returnText.setLayoutData(gd);
		returnText.setText(DEFAULT_RETURN_TYPE);
		
		// method name
		new Label (composite, SWT.NONE).setText("Capability Name:");				
		nameText = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol - 1;
		nameText.setLayoutData(gd);
		nameText.setText(DEFAULT_CAPABILITY_NAME);
		
		// method parameters
		new Label (composite, SWT.NONE).setText("Parameters:");				
		parameterText = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol - 1;
		parameterText.setLayoutData(gd);
		
	    //Set the composite as the control for this page, displaying wizard elements
		setControl(composite);		
		addListeners();
	}
	
	/**Adds listeners to components for status updates*/
	private void addListeners()
	{
		publicButton.addListener(SWT.Selection, this);
		protectedButton.addListener(SWT.Selection, this);
		privateButton.addListener(SWT.Selection, this);
		returnText.addListener(SWT.KeyUp, this);
		nameText.addListener(SWT.KeyUp, this);
		parameterText.addListener(SWT.KeyUp, this);
	}

	
	@Override
	public void handleEvent(Event event) {
		// Show the most serious error
		methodNameStatus = JavaConventions.validateMethodName(nameText.getText(), null, null);
		parameterStatus = validateParameters(parameterText.getText());
		returnTypeStatus = validateReturnType(returnText.getText().trim());
	    applyToStatusLine(findMostSevere());
		getWizard().getContainer().updateButtons();
	}
	
	/**
	 * Checks the given string for validity as a parameter set
	 * "Object o, int i" is a valid parameter set
	 * "Object" is not
	 * @param parameters the set of parameters
	 * @return IStatus corresponding to whether the given parameter set is valid
	 */
	public IStatus validateParameters(String parameters){
		if(parameters != null){
			StringTokenizer tokens = new StringTokenizer(parameters, ",");
			
			//Check for empty string
			if(!parameters.trim().equalsIgnoreCase("")){
				//Check for ending with divider character
				if(parameters.trim().endsWith(",")){
					return new Status(IStatus.ERROR, "not_used", 0, "Parameter entry cannot end with a comma", null);
				}
				
				while(tokens.hasMoreTokens()){
					IStatus retStat;
					
					//Check each set of type and name in the parameter set
					if(!(retStat = checkParameterSet(tokens.nextToken())).isOK()){
						return retStat;
					}
				}
			}
		}
		
		//If not errors, return a status saying all is well
		return new Status(IStatus.OK, "not_used", 0, "", null);
	}
	
	/**
	 * Checks a given string for validity as a return type
	 * "void", "Object", "int" are all valid returns
	 * "", "Obj-ect", and any non-valid Java class names are not valid
	 * @param retType The string to test
	 * @return IStatus representing the validity of the given string
	 */
	public IStatus validateReturnType(String retType){
		//TODO make this return a warning status if a referenced object cannot be found
		if(retType != null){
			if((retType.equals("void"))
					|| (isPrimitiveType(retType))){
				return new Status(IStatus.OK, "not_used", 0, "", null);
			}
			FRCProject frcProject = CodeViewerPlugin.getFRCModel().getActiveProject();
			
			if(frcProject != null){
				if(!((JavaConventions.validateJavaTypeName(retType, null, null).isOK())
						&&(ProjectClasses.isClassInProject(frcProject.getJavaProject(), retType)))){
					return new Status(IStatus.WARNING, "not_used", 0, retType + " is not yet defined in the project", null);
				}else{
					return new Status(IStatus.OK, "not_used", 0, "", null);
				}
			}else{
				return new Status(IStatus.WARNING, "not_used", 0, "Could not determine if " + retType + " has yet been defined in the project", null);
			}
		}
		
		return new Status(IStatus.ERROR, "not_used", 0, retType + " is not a valid return type", null);
	}
	
	/**
	 * Checks if the given set is a valid parameter entry
	 * "int i" is a valid entry
	 * "i" is not a valid entry
	 * First space-separated string must be a valid java primitive or type name
	 * Second entry must be a valid variable name
	 * @param set
	 * @return
	 */
	private IStatus checkParameterSet(String set){
		//TODO make this return a warning status if a referenced object cannot be found
		String type, name;
		StringTokenizer tokens = new StringTokenizer(set);
		
		//Check that both components, a type and name, are present (exception thrown if too few tokens present)
		try{
			type = tokens.nextToken();
			name = tokens.nextToken();
		}catch(Exception e){
			return new Status(IStatus.ERROR, "not_used", 0, set + " is not a valid parameter", null);
		}
		
		//Check that the parameter set is not too long (more than two elements, type and name, is invalid)
		if(tokens.hasMoreTokens()){
			return new Status(IStatus.ERROR, "not_used", 0, set + " is not a valid parameter", null);
		}
		
		//Check that the given name is a valid java name
		if(JavaConventions.validateFieldName(name, null, null).isOK()){
			//Check that the given type is eitehr a primitive or a valid java class name
			if((JavaConventions.validateJavaTypeName(type, null, null).isOK())||(isPrimitiveType(type))){
				FRCProject frcProject;
				if((!isPrimitiveType(type))&&((frcProject = CodeViewerPlugin.getFRCModel().getActiveProject()) != null)){
					if(!ProjectClasses.isClassInProject(frcProject.getJavaProject(), type)){
						return new Status(IStatus.WARNING, "not_used", 0, type + " is not yet a defined type in the project.", null);
					}
				}else if(!isPrimitiveType(type)){
					return new Status(IStatus.WARNING, "not_used", 0, "Could not determine if " + type + " has yet been defined in the project", null);
				}
				
				return new Status(IStatus.OK, "not_used", 0, "", null);
			}
		}
		
		return new Status(IStatus.ERROR, "not_used", 0, set + " is not a valid parameter", null);
	}
	
	/**
	 * Checks if a given string is the identifier for a java primitive 
	 * @param ident The string to check
	 * @return true if the given stirng is a valid primitive identifier
	 */
	private boolean isPrimitiveType(String ident){
		if(ident.equals("boolean")
				|| ident.equals("char")
				|| ident.equals("byte")
				|| ident.equals("short")
				|| ident.equals("int")
				|| ident.equals("long")
				|| ident.equals("float")
				|| ident.equals("double")){
				return true;
		}
		
		return false;
	}

	@Override
	public boolean canFlipToNextPage(){
		return complete() && super.canFlipToNextPage();
	}
	
	/** Returns whether the page is complete.  Page is complete if required fields are filled
	 * (return type and method name), there are no errors on the page, and one of the three 
	 * visibility choice is selected.
	 */
	public boolean complete(){
		//Checks for correct name, return, and parameters done through error message application
		if (((getErrorMessage() != null) 
				||(!(publicButton.getSelection() || protectedButton.getSelection() || privateButton.getSelection()))) 
				||(!(isTextNonEmpty(nameText)&&isTextNonEmpty(returnText)))) return false;
		
		return true;
	}
	
	/**
	 * Applies the status to the status line of a dialog page.
	 */
	private void applyToStatusLine(IStatus status) {
		String message= status.getMessage();
		if (message.length() == 0) message= null;
		switch (status.getSeverity()) {
			case IStatus.OK:
				setErrorMessage(null);
				setMessage(message);
				break;
			case IStatus.WARNING:
				setErrorMessage(null);
				setMessage(message, WizardPage.WARNING);
				break;				
			case IStatus.INFO:
				setErrorMessage(null);
				setMessage(message, WizardPage.INFORMATION);
				break;			
			default:
				setErrorMessage(message);
				setMessage(null);
				break;		
		}
	}	
	
	private IStatus findMostSevere()
	{
		if (methodNameStatus.matches(IStatus.ERROR))
			return methodNameStatus;
		if (parameterStatus.matches(IStatus.ERROR))
			return parameterStatus;
		if (returnTypeStatus.matches(IStatus.ERROR))
			return returnTypeStatus;
		if (methodNameStatus.getSeverity() > parameterStatus.getSeverity()){
			if(methodNameStatus.getSeverity() > returnTypeStatus.getSeverity())
				return methodNameStatus;
			else return returnTypeStatus;
		}
		
		if(parameterStatus.getSeverity() > returnTypeStatus.getSeverity())
			return parameterStatus;	
		else return returnTypeStatus;
	}

	/** Checks if a given text field is empty */
	private static boolean isTextNonEmpty(Text t)
	{
		String s = t.getText();
		if ((s!=null) && (s.trim().length() >0)) return true;
		return false;
	}	

	/** Creates a divider line in the given composite */
	private void createLine(Composite parent, int ncol) 
	{
		Label line = new Label(parent, SWT.SEPARATOR|SWT.HORIZONTAL|SWT.BOLD);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = ncol;
		line.setLayoutData(gridData);
	}
	
	/** Returns the visibility selected */
	public String getVisibility(){
		if(publicButton.getSelection()){
			return "public";
		}else if(protectedButton.getSelection()){
			return "protected";
		}
		
		return "private";
	}
	
	/** Returns the entered return type */
	public String getReturnType(){
		return returnText.getText().trim();
	}

	/** Returns the entered capability name */
	public String getMethodName(){
		return nameText.getText().trim();
	}

	/** Returns the entered parameter name */
	public String getParameters(){
		if(isTextNonEmpty(parameterText)){
			return parameterText.getText().trim();
		}
		return null;
	}

}

