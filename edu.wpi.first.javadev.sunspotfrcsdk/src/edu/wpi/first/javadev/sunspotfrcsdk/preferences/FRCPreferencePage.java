package edu.wpi.first.javadev.sunspotfrcsdk.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.wpi.first.javadev.sunspotfrcsdk.SDKPlugin;
import edu.wpi.first.javadev.sunspotfrcsdk.internal.sdksetup.SDKSetup;

/**
 * Base preference page for the FRC java development plug-ins, with team 
 * number option 
 * 
 * @author Ryan O'Meara
 */
public class FRCPreferencePage extends FieldEditorPreferencePage 
implements IWorkbenchPreferencePage{
	
	private static IntegerFieldEditor teamNumberEditor;
	
	private boolean doSave;
	
	public FRCPreferencePage(){
		super(GRID);
		setPreferenceStore(SDKPlugin.getDefault().getPreferenceStore());
		setDescription("Options for the Java Development Plug-ins for the " +
				"FIRST Robotics Competition");
	}

	@Override
	public void init(IWorkbench workbench) {
		doSave = false;
	}

	@Override
	protected void createFieldEditors() {
		teamNumberEditor = new IntegerFieldEditor(PreferenceConstants.P_TEAM_NUMBER,
				"&Team Number:", getFieldEditorParent());
		
		addField(teamNumberEditor);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event){
		doSave = true;
		
		super.propertyChange(event);
	}
	
	@Override
	public boolean okToLeave(){
		if(doSave){
			this.setErrorMessage("Must apply changes before changing pages");
			return false;
		}
		
		
		
		return super.okToLeave();
	}
	
	@Override
	public boolean performOk(){
		boolean retVal = super.performOk();
		
		if(doSave){
			this.setErrorMessage(null);
			SDKSetup.installSDK();
		}
		
		doSave = false;
		
		return retVal;
	}

}
