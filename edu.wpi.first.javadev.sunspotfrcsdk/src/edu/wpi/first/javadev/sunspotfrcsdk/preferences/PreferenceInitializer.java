package edu.wpi.first.javadev.sunspotfrcsdk.preferences;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import edu.wpi.first.javadev.sunspotfrcsdk.SDKPlugin;
import edu.wpi.first.javadev.sunspotfrcsdk.filesystem.SDKFileLocations;

/**
 * Initializes all preferences' default values 
 * 
 * @author Ryan O'Meara
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer{

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = SDKPlugin.getDefault().getPreferenceStore();
		
		store.setDefault(PreferenceConstants.P_ALT_SDK, false);
		store.setDefault(PreferenceConstants.P_ALT_WPILIB, false);
		store.setDefault(PreferenceConstants.P_SDK_DIRECTORY, 
				SDKFileLocations.getDefaultExtractLocation());
		store.setDefault(PreferenceConstants.P_TEAM_NUMBER, 0);
		store.setDefault(PreferenceConstants.P_WPILIBJ_DIRECTORY, 
				SDKFileLocations.getDefaultExtractLocation() + File.separator 
				+ "lib" + File.separator + "WPILibJ");
		store.setDefault(PreferenceConstants.P_UPDATE, false);
		store.setDefault(PreferenceConstants.P_CRIO_IMAGE, true);
	}

}
