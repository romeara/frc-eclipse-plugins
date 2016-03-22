package edu.wpi.first.javadev.sunspotfrcsdk.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.wpi.first.javadev.sunspotfrcsdk.SDKPlugin;

/**
 * Preference page which provides options for the sunspotsdk extraction
 * location and the wpi library to use
 * 
 * @author Ryan O'Meara
 */
public class SDKPreferencePage extends FieldEditorPreferencePage 
implements IWorkbenchPreferencePage{
	
	private static BooleanFieldEditor updateSDK;
	
	private static BooleanFieldEditor altSDKDirectory;
	private static DirectoryFieldEditor sdkDirectory;
	
	private static BooleanFieldEditor altLibDirectory;
	private static DirectoryFieldEditor libDirectory;
	
	private static BooleanFieldEditor cRIOImageCopy;
	
	private boolean doSave;
	
	public SDKPreferencePage(){
		super(GRID);
		setPreferenceStore(SDKPlugin.getDefault().getPreferenceStore());
		setDescription("FRC Java SunspotSDK deployment options");
	}

	@Override
	public void init(IWorkbench workbench) {
		doSave = false;
	}

	@Override
	protected void createFieldEditors() {
		updateSDK = new BooleanFieldEditor(PreferenceConstants.P_UPDATE, 
				"&Do not update SDK when new version is available", 
				getFieldEditorParent());
		altSDKDirectory = new BooleanFieldEditor(PreferenceConstants.P_ALT_SDK, 
				"&Install SDK to alternate directory", getFieldEditorParent());
		sdkDirectory = new DirectoryFieldEditor(PreferenceConstants.P_SDK_DIRECTORY, 
				"&Alternate SDK Directory:", getFieldEditorParent());
		
		altLibDirectory = new BooleanFieldEditor(PreferenceConstants.P_ALT_WPILIB, 
				"&Use alternate WPILibJ Library", getFieldEditorParent());
		libDirectory = new DirectoryFieldEditor(PreferenceConstants.P_WPILIBJ_DIRECTORY,
				"&Location of alternate WPILibJ Library", getFieldEditorParent());
		
		cRIOImageCopy = new BooleanFieldEditor(PreferenceConstants.P_CRIO_IMAGE,
				"&Copy cRIO Images to User Directory", getFieldEditorParent());
		
		IPreferenceStore store = SDKPlugin.getDefault().getPreferenceStore();
		sdkDirectory.setEnabled(store.getBoolean(PreferenceConstants.P_ALT_SDK), getFieldEditorParent());
		libDirectory.setEnabled(store.getBoolean(PreferenceConstants.P_ALT_WPILIB), getFieldEditorParent());
		
		addField(updateSDK);
		
		addField(altSDKDirectory);
		addField(sdkDirectory);
		
		addField(altLibDirectory);
		addField(libDirectory);
		
		addField(cRIOImageCopy);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent event){
		doSave = true;
		
		if(FieldEditor.VALUE.equals(event.getProperty())){
			sdkDirectory.setEnabled(altSDKDirectory.getBooleanValue(), getFieldEditorParent());
			libDirectory.setEnabled(altLibDirectory.getBooleanValue(), getFieldEditorParent());
		}
		
		super.propertyChange(event);	
	}
	
	@Override
	public boolean performOk(){
		boolean retVal = super.performOk();
		
		if(doSave){
			System.out.println("SDKPreferencePage.performOK");
			SDKPlugin.getDefault().updateSDK(false, true);
		}
		
		doSave = false;
		
		return retVal;
	}

}
