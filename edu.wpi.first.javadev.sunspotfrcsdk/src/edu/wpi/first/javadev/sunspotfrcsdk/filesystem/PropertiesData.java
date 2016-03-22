package edu.wpi.first.javadev.sunspotfrcsdk.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;

/**
 * Retrieves properties information for the SDK and makes it available to
 * plug-ins
 * 
 * @author Ryan O'Meara
 */
public class PropertiesData {
	
	/** Sunspot properties file name */
	private static final String SUNSPOT_PROPS = ".sunspotfrc.properties";
	
	/** Version properties file name */
	private static final String VERSION_PROPS = "version.properties";
	
	/** Property key for sunspot home */
	private static final String SUNSPOT_HOME_PROP = "sunspot.home";
	
	/** Property key for wpilibj home */
	private static final String WPILIBJ_HOME_PROP = "wpilibj.home";
	
	/** Property key for version date stamp */
	private static final String VERSION_DATESTAMP_PROP = "version.datestamp";
	
	private static boolean initialized = false;
	private static String sunspotHome = null;
	private static String wpilibjHome = null;
	private static String installedDateStamp = null;
	private static String archiveDateStamp = null;
	
	
	/**
	 * Sets up the two return strings if necessary to cut down on file
	 * operations
	 */
	private static void initialize(){
		if(!initialized){
			initialized = true;
			Properties sunprops = new Properties();
			FileReader reader = null;
			
			try{
				reader = new FileReader(System.getProperty("user.home") + File.separator + SUNSPOT_PROPS);
				sunprops.load(reader);
				sunspotHome = sunprops.getProperty(SUNSPOT_HOME_PROP);
				wpilibjHome = sunprops.getProperty(WPILIBJ_HOME_PROP);
			}catch(Exception e){
				sunspotHome = null;
				wpilibjHome = null;
				initialized = false;
			}finally{
				try{reader.close();}catch(Exception e){}
			}
			
			Properties installedprops = new Properties();
			
			try{
				reader = new FileReader(sunspotHome + File.separator + VERSION_PROPS);
				installedprops.load(reader);
				installedDateStamp = installedprops.getProperty(VERSION_DATESTAMP_PROP);
			}catch(Exception e){
				installedDateStamp = null;
				initialized = false;
			}finally{
				try{reader.close();}catch(Exception e){}
			}
			
			File verFile = ArchiveOperations.extractFile(
					new File(SDKFileLocations.getSDKArchivePath()),
					new File(SDKFileLocations.getSDKPluginDirectory()),
					VERSION_PROPS);
			
			Properties archiveprops = new Properties();
			
			try{
				reader = new FileReader(verFile);
				archiveprops.load(reader);
				archiveDateStamp = installedprops.getProperty(VERSION_DATESTAMP_PROP);
			}catch(Exception e){
				archiveDateStamp = null;
				initialized = false;
			}finally{
				try{reader.close();}catch(Exception e){}
				verFile.delete();
			}
			
		}
	}
	
	public static void update(){
		initialized = false;
		initialize();
	}
	
	/**
	 * Reads and gives back the currently installed sdk's home directory from 
	 * the SDK properties file
	 * @return {@link String} representing path to SDK directory
	 */
	public static String getSunspotHome(){
		initialize();
		return sunspotHome;
	}
	
	/**
	 * Reads and gives back the currently installed wpilibj's home directory 
	 * from the SDK properties file
	 * @return {@link String} representing path to WPILibJ directory
	 */
	public static String getWPILibJHome(){
		initialize();
		return wpilibjHome;
	}
	
	/**
	 * Reads and gives back the currently installed SDK's version date stamp
	 * @return {@link String} representation of the date stamp, or null if it 
	 * could not be read 
	 */
	public static String getInstalledDateStamp(){
		initialize();
		return installedDateStamp;
	}
	
	/**
	 * Reads and gives back the archive SDK's version date stamp
	 * @return {@link String} representation of the date stamp, or null if it 
	 * could not be read 
	 */
	public static String getArchiveDateStamp(){
		initialize();
		return archiveDateStamp;
	}
	
	/**
	 * Changes the wpilibj.home property in the SDK property file, in case 
	 * alternate location is desired
	 * @param newWPILibJLocation {@link String}Path to location to set as 
	 * WPILibJ home
	 * @return true if the operation was successful, false otherwise
	 */
	public static boolean setWPILibJHome(String newWPILibJLocation){
		Properties sunprops = new Properties();
		FileReader reader = null;
		FileOutputStream stream = null;
		try{
			reader = new FileReader(System.getProperty("user.home") 
					+ File.separator + ".sunspotfrc.properties");
			stream = new FileOutputStream(System.getProperty("user.home") 
					+ File.separator + SUNSPOT_PROPS);
			sunprops.load(reader);
			sunprops.setProperty("wpilibj.home", newWPILibJLocation);
			
			sunprops.store(stream, "");
			return true;
		}catch(Exception e){
			return false;
		}finally{
			try{reader.close();
			stream.close();}catch(Exception e){}
		}
	}
}
