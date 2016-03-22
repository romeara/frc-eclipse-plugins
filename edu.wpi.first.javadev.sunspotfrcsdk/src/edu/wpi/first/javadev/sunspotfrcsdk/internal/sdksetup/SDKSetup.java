package edu.wpi.first.javadev.sunspotfrcsdk.internal.sdksetup;

import java.io.File;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;

import edu.wpi.first.codedev.output.FRCConsole;
import edu.wpi.first.javadev.sunspotfrcsdk.SDKPlugin;
import edu.wpi.first.javadev.sunspotfrcsdk.ant.AntLauncher;
import edu.wpi.first.javadev.sunspotfrcsdk.filesystem.ArchiveOperations;
import edu.wpi.first.javadev.sunspotfrcsdk.filesystem.FileOperations;
import edu.wpi.first.javadev.sunspotfrcsdk.filesystem.PropertiesData;
import edu.wpi.first.javadev.sunspotfrcsdk.filesystem.SDKFileLocations;
import edu.wpi.first.javadev.sunspotfrcsdk.preferences.PreferenceConstants;

/**
 * Contains functions to setup the SDK, including determining if setup is 
 * necessary, extracting, and running the ant install script included with
 * the SDK
 * 
 * @author Ryan O'Meara
 */
public class SDKSetup {
	
	 /** The argument that tells the install file which remote address to use */
	private static final String REMOTE_ADDRESS_ARGUMENT = "Dremoteaddress";
	
	/** Name of the ant install file to use */
	private static final String INSTALL_FILE = "install.xml";

	/**
	 * Determines if the SDK is already correctly installed on the system
	 * @return true if the SDK is currently correctly installed, false otherwise
	 */
	public static boolean needsUpdate(){
		return needsExtraction() || needsInstall();
	}
	
	/**
	 * Determines if the SDK has been extracted to the designated location 
	 * successfully on this system
	 * @return true if the SDK is already extracted, false otherwise
	 */
	public static boolean needsExtraction(){
		//Check if SDK is not present somewhere
		if(PropertiesData.getSunspotHome() == null){
			return true;
		}
		
		File extractLoc = new File(SDKFileLocations.getExtractLocation());
		
		//Check that the user wishes update and whether the location is correct	
		if((!SDKPlugin.getDefault().getPreferenceStore().getBoolean(
				PreferenceConstants.P_UPDATE))
				&&((!FileOperations.sameFileLocation(
						PropertiesData.getSunspotHome(), 
						SDKFileLocations.getExtractLocation()))||
				(!extractLoc.exists()))){
			return true;
		}
		
		//Check that user wishes update and whether the SDK is using the most 
		//current version (indicated by date stamp)
		if((!SDKPlugin.getDefault().getPreferenceStore().getBoolean(
				PreferenceConstants.P_UPDATE))&&
				((PropertiesData.getInstalledDateStamp() == null)||
				(PropertiesData.getArchiveDateStamp() == null)||
				(!PropertiesData.getInstalledDateStamp().equalsIgnoreCase(
						PropertiesData.getArchiveDateStamp())))){
			return true;
		}
		
		//Check if cRIO Images need copying (assuming option is selected)
		if((SDKPlugin.getDefault().getPreferenceStore().getBoolean(
				PreferenceConstants.P_CRIO_IMAGE))&&(!(new File(System.getProperty("user.home") + File.separator + "sunspotfrcsdk" + File.separator + "cRIO_Images")).exists())){
			return true;
		}
		
		return false;
		
	}
	
	/**
	 * Determines if the SDK has been correctly installed on the system
	 * @return true if the SDK was correctly installed, false otherwise
	 */
	private static boolean needsInstall(){
		//Check if install has been performed at all
		if(PropertiesData.getSunspotHome() == null){
			return true;
		}	
		
		String preverifyLoc = PropertiesData.getSunspotHome();
		preverifyLoc += File.separator + "bin";
		preverifyLoc = preverifyLoc.replace('\\', File.separatorChar);
		preverifyLoc = preverifyLoc.replace('/', File.separatorChar);
		
		File preverifyLocFile = new File(preverifyLoc);
		
		boolean found = false;
		if(preverifyLocFile.exists()){
			File[] files = preverifyLocFile.listFiles();
			//If preverify is not found, install
			if(files != null){
				for(File current : files){
					if(current.toString().indexOf("preverify") != -1){
						found = true;
					}
				}
				
			}
		}
		
		if(!found){return true;}
		
		//Check for wpilibj and sqwuak libraries being present
		if(!(new File(SDKFileLocations.getSDKClasspath())).exists()){return true;}
		
		if(!(new File(SDKFileLocations.getWPILibJClasspath())).exists()){
			return true;
		}
		
		//Check if install was was location desired by user
		if(!FileOperations.sameFileLocation(PropertiesData.getSunspotHome(), 
				SDKFileLocations.getExtractLocation())){
			return true;
		}
		
		return false;
	}
	
	/**
	 * Deletes old SDK (if any) and extracts to the currently selected location
	 * @return true if the SDK was extracted successfully, false otherwise
	 */
	public static boolean extractSDK(){
		File extractLocation = new File(SDKFileLocations.getExtractLocation());
		
		//Make sure directory structure exists
		if(!extractLocation.exists()){extractLocation.mkdirs();}
		
		//Delete old SDK (which could be in a user-defined or default location,
		//not necessarily where it is currently being installed to
		FRCConsole.writeToConsole("Cleaning old SDK");
		FileOperations.deleteFile(new File(SDKFileLocations.getExtractLocation()));
		FileOperations.deleteFile(new File(SDKFileLocations.getDefaultExtractLocation()));
		
		FRCConsole.writeToConsole("Beginning Extraction");
		boolean extracted = ArchiveOperations.extractArchive(
				new File(SDKFileLocations.getSDKArchivePath()), extractLocation);
		
		//Copy cRIO Images to user directory for imaging too
		String userHome = System.getProperty("user.home");
				
		if((SDKPlugin.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_CRIO_IMAGE))
				&&extracted
				&&(extractLocation.toString().indexOf("userHome") == -1)){
			File sunspotuserLoc = new File(userHome + File.separator + "sunspotfrcsdk");
			
			if(sunspotuserLoc.exists()){
				FileOperations.deleteFile(sunspotuserLoc);
			}
			
			sunspotuserLoc = new File(SDKFileLocations.getCRIOImagesCopyPath());
			
			sunspotuserLoc.mkdirs();
			
			extracted = extracted & FileOperations.copyDirectory(new File(SDKFileLocations.getcRIOImagesSourcePath()), sunspotuserLoc);
		}
			
			
		return extracted;
	}
	
	/**
	 * Installs the SDK using the install.xml file included with it
	 * @return true if installation was successful, false otherwise
	 */
	public static ILaunch installSDK(){
		//Run the ant install
		ILaunch retLaunch = AntLauncher.runAntFile(
				new File(SDKFileLocations.getExtractLocation() + File.separator 
						+ INSTALL_FILE), 
						null, 
						getRemoteAddressArgument(), 
						ILaunchManager.RUN_MODE);
		
		//Replace properties entry if necessary
		if(SDKPlugin.getDefault().getPreferenceStore().getBoolean(
				PreferenceConstants.P_ALT_WPILIB)){
			PropertiesData.setWPILibJHome(
					SDKFileLocations.getActiveWPILibJLocation());
		}
		
		PropertiesData.update();
		
		return retLaunch;
	}
	
	/**
	 * Constructs the remote address argument to install the SDK with based
	 * on the entered team number
	 * @return {@link String} representation of the argument to run the ant 
	 * file with
	 */
	private static String getRemoteAddressArgument(){
		int teamNumber = SDKPlugin.getDefault().getPreferenceStore().getInt(
				PreferenceConstants.P_TEAM_NUMBER);
		
		return "-" + REMOTE_ADDRESS_ARGUMENT + "=10." + (int)(teamNumber/100) 
		+ "." + (int)(teamNumber % 100) + ".2";
	}
}
