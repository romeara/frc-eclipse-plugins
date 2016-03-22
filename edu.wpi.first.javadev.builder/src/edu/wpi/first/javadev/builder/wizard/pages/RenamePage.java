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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

import edu.wpi.first.javadev.builder.CodeViewerPlugin;
import edu.wpi.first.javadev.builder.workspace.model.FRCModel;


public class RenamePage extends WizardPage implements Listener
{
	IWorkbench workbench;
	IStructuredSelection selection;
	
	Label instructionLabel;
	Text nameText;
	
	IStatus nameStatus;
	
	private String origName;
	private int entryType;
	
	/**
	 * Constructor for HolidayMainPage.
	 */
	public RenamePage(IWorkbench workbench, IStructuredSelection selection, String originalName, int nameType) {
		super("Rename Wizard");
		setTitle("Rename Wizard");
		setDescription("Enter the new name");
		this.workbench = workbench;
		this.selection = selection;
		nameStatus = new Status(IStatus.OK, "not_used", 0, "", null);	
		origName = originalName;
		entryType = nameType;
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
		String targetType = "element";
		if(entryType == FRCModel.TYPE_RENAME){targetType = "type";}
		else if(entryType == FRCModel.METHOD_RENAME){targetType = "method";}
		else if(entryType == FRCModel.FIELD_RENAME){targetType = "field";}
		
		instructionLabel.setText("Enter the new name for the " + targetType);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol;
		instructionLabel.setLayoutData(gd);
		
		
		// method name
		new Label (composite, SWT.NONE).setText("New Name:");				
		nameText = new Text(composite, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = ncol - 1;
		nameText.setLayoutData(gd);
		nameText.setText(origName);
		
	    //Set the composite as the control for this page, displaying wizard elements
		setControl(composite);		
		addListeners();
	}
	
	/**Adds listeners to components for status updates*/
	private void addListeners(){
		nameText.addListener(SWT.KeyUp, this);
	}

	
	@Override
	public void handleEvent(Event event) {
		// Show the most serious error
		if(entryType == FRCModel.METHOD_RENAME){
			nameStatus = 
				JavaConventions.validateMethodName(nameText.getText(), null, null);
		}else if(entryType == FRCModel.FIELD_RENAME){
			nameStatus = 
				JavaConventions.validateFieldName(nameText.getText(), null, null);
		}else if(entryType == FRCModel.TYPE_RENAME){
			nameStatus = 
				JavaConventions.validateJavaTypeName(nameText.getText(), null, null);
		}
		
		if(nameStatus.isOK()
				||(nameStatus.getSeverity() == IStatus.WARNING)){
			if(origName.equals(nameText.getText())){
				nameStatus = new Status(IStatus.ERROR, 
						CodeViewerPlugin.PLUGIN_ID, 
						"Cannot Rename to original name");	
			}
		}
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
		//Checks for correct name, return, and parameters done through error message application
		if ((getErrorMessage() != null) ||(!isTextNonEmpty(nameText))){return false;}
		
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

	/** Checks if a given text field is empty */
	private static boolean isTextNonEmpty(Text t)
	{
		String s = t.getText();
		if ((s!=null) && (s.trim().length() >0)) return true;
		return false;
	}	

	/** Returns the entered capability name */
	public String getName(){
		return nameText.getText().trim();
	}

}

