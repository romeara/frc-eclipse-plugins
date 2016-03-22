package edu.wpi.first.javadev.builder.wizard.pages;

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


public class NewFieldPage extends WizardPage implements Listener{
	IWorkbench workbench;
	IStructuredSelection selection;
	
	Text nameText;
	
	Button publicButton;
	Button protectedButton;
	Button privateButton;
	
	IStatus nameStatus;
	private String defName;
	private String elemName;

	
	/**
	 * Constructor for HolidayMainPage.
	 */
	public NewFieldPage(IWorkbench workbench, IStructuredSelection selection, String elementType, String defaultName) {
		super("Element Wizard");
		setTitle("Element Wizard");
		if(elementType == null){elemName = "element";}else{elemName = elementType;}
		this.workbench = workbench;
		this.selection = selection;
		nameStatus = new Status(IStatus.OK, "not_used", 0, "", null);
		defName = defaultName;
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
		
		//Instructions
		Label instruct = new Label (composite, SWT.NONE);
		instruct.setText("Set the name of the " + elemName 
				+ " to be added.  This will be the name of the member variable \n" + 
				"for the instance of the " + elemName + " in the parent element selected.");				
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol;
		instruct.setLayoutData(gd);
		
		createLine(composite, ncol);
		
		// Choice of visibility - public, protected, private
		new Label (composite, SWT.NONE).setText("Visibility:");
		
		publicButton = new Button(composite, SWT.RADIO);
		publicButton.setText("Public");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol-1;
		publicButton.setLayoutData(gd);
		
		protectedButton = new Button(composite, SWT.RADIO);
		protectedButton.setText("Protected");
		gd.horizontalSpan = ncol-2;
		protectedButton.setLayoutData(gd);
		protectedButton.setSelection(true);
		
		privateButton = new Button(composite, SWT.RADIO);
		privateButton.setText("Private");
		gd.horizontalSpan = ncol-3;
		privateButton.setLayoutData(gd);
		
		// field name
		String capEN = elemName.substring(1);
		capEN = elemName.toUpperCase().charAt(0) + capEN;
		new Label (composite, SWT.NONE).setText(capEN + " Name:");				
		nameText = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol - 1;
		nameText.setLayoutData(gd);
		nameText.setText(defName);
		
	    //Set the composite as the control for this page, displaying wizard elements
		setControl(composite);		
		addListeners();
	}
	
	/** Creates a divider line in the given composite */
	private void createLine(Composite parent, int ncol) 
	{
		Label line = new Label(parent, SWT.SEPARATOR|SWT.HORIZONTAL|SWT.BOLD);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = ncol;
		line.setLayoutData(gridData);
	}
	
	/**Adds listeners to components for status updates*/
	private void addListeners(){
		publicButton.addListener(SWT.Selection, this);
		protectedButton.addListener(SWT.Selection, this);
		privateButton.addListener(SWT.Selection, this);
		nameText.addListener(SWT.KeyUp, this);
	}

	
	@Override
	public void handleEvent(Event event) {
		// Show the most serious error
		nameStatus = JavaConventions.validateFieldName(nameText.getText(), null, null);
	    applyToStatusLine(nameStatus);
		getWizard().getContainer().updateButtons();
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
		return isTextNonEmpty(nameText)
		&&((publicButton.getSelection() 
				|| protectedButton.getSelection() 
				|| privateButton.getSelection()));
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

	/** Checks if a given text field is empty */
	private static boolean isTextNonEmpty(Text t)
	{
		String s = t.getText();
		if ((s!=null) && (s.trim().length() >0)) return true;
		return false;
	}	
	
	/** Returns the entered capability name */
	public String getFieldName(){
		return nameText.getText().trim();
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
}

