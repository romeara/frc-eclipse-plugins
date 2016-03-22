package edu.wpi.first.javadev.sunspotfrcsdk.filesystem;

import java.io.File;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.preference.IPreferenceStore;

import edu.wpi.first.javadev.sunspotfrcsdk.SDKPlugin;
import edu.wpi.first.javadev.sunspotfrcsdk.preferences.PreferenceConstants;

/**
 * Contains constants and methods representing relevant file system locations 
 * for installing the SDK, such as the zip file location, the default 
 * extraction location, and the user defined extract location
 * 
 * @author Ryan O'Meara
 */
public class SDKFileLocations {
	
	/** Label of SDK zip file and unpack directory */
	private static final String SDK_LABEL = "sunspotfrcsdk";
	
	/** Label of jar with SDK library */
	private static final String SDK_LIBRARY_LABEL = "squawk_device";
	
	/** Label of jar with WPILibJ Library */
	private static final String WPILIBJ_LIBRARY_LABEL = "classes";
	
	/**
	 * Retrieves the location of the "plugins" directory for this Eclipse 
	 * installation, where all plug-in files are kept
	 * @return {@link String} path to the "plugins" directory, null if location
	 * could not be retrieved
	 */
	public static String getEclipsePluginDirectory(){
		try{
			return FileLocator.getBundleFile(SDKPlugin.getDefault().getBundle()).getParent();
		}catch(Exception e){return null;}
	}
	
	/**
	 * Retrieves the location of the SDK archive on the file system 
	 * @return {@link String} representation of the path to the SDK archive, or
	 * null if the location could not be retrieved
	 */
	public static String getSDKArchivePath(){
		String pluginDir = getSDKPluginDirectory();
		
		if(pluginDir != null){
			return pluginDir + File.separator + SDK_LABEL + ".zip";
		}
		
		return null;
	}
	
	/**
	 * Retrieves the expected location of the cRIO Images
	 * @return The directory path to where the extracted cRIO images should be
	 */
	public static String getcRIOImagesSourcePath(){
		return getExtractLocation() + File.separator + "cRIO_Images";
	}
	
	/**
	 * @return The string path to the user directory where the FRC Imaging tool expects the
	 * cRIO images to be
	 */
	public static String getCRIOImagesCopyPath(){
		return System.getProperty("user.home") + File.separator + "sunspotfrcsdk" + File.separator + "cRIO_Images";
	}
	
	/**
	 * Gives the path which the SDK archive should be unpacked into by default
	 * @return {@link String} path to unpack the archive into, or null if the 
	 * location could not be determined
	 */
	public static String getDefaultExtractLocation(){
		return getSDKPluginDirectory() + File.separator + SDK_LABEL;
	}
	
	/**
	 * Retrieves the location to extract the SDK archive too, taking into 
	 * account user preferences
	 * @return {@link String} path to the extraction location
	 */
	public static String getExtractLocation(){
		
		IPreferenceStore prefs = SDKPlugin.getDefault().getPreferenceStore();
		
		if(prefs.getBoolean(PreferenceConstants.P_ALT_SDK)){
			return prefs.getString(PreferenceConstants.P_SDK_DIRECTORY);
		}
			
		return prefs.getDefaultString(PreferenceConstants.P_SDK_DIRECTORY);
	
	}
	
	/**
	 * Retrieves the location of WPILibJ to use, taking into account user 
	 * preferences
	 * @return {@link String} path to the extraction location
	 */
	public static String getActiveWPILibJLocation(){
		IPreferenceStore prefs = SDKPlugin.getDefault().getPreferenceStore();
		
		if(prefs.getBoolean(PreferenceConstants.P_ALT_WPILIB)){
			return prefs.getString(PreferenceConstants.P_WPILIBJ_DIRECTORY);
		}
		
		return prefs.getDefaultString(PreferenceConstants.P_WPILIBJ_DIRECTORY);
	}
	
	/**
	 * Constructs the path to the jar file which contains the WPILibJ classes
	 * for inclusion on FRC project class paths
	 * @return {@link String} representation of the path to the jar file 
	 */
	public static String getWPILibJClasspath(){
		return getActiveWPILibJLocation() + File.separator 
		+ WPILIBJ_LIBRARY_LABEL + ".jar";
	}
	
	/**
	 * Constructs the path to the jar file which contains the SDK classes
	 * for inclusion on FRC project class paths
	 * @return {@link String} representation of the path to the jar file 
	 */
	public static String getSDKClasspath(){
		return getExtractLocation() + File.separator + "lib" + File.separator 
		+ SDK_LIBRARY_LABEL + ".jar";
	}
	
	/**
	 * Retrieves the location of this plug-in's file directory in the Eclipse
	 * "plugins" directory
	 * @return {@link String} path to this plug-in's file directory, null if
	 * location could not be retrieved 
	 */
	public static String getSDKPluginDirectory(){
		try{
			return FileLocator.getBundleFile(SDKPlugin.getDefault().getBundle()).getAbsolutePath();
		}catch(Exception e){return null;}
	}
}
