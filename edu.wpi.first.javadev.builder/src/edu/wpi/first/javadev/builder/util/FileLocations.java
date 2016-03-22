package edu.wpi.first.javadev.builder.util;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Stores and provides the location of files in the local file system
 * @author Ryan O'Meara
 */
public class FileLocations {
	
	/**
	 * Returns the string path to the WPILibJ jar file
	 * @return String representing the path to the jar archive
	 */
	private static String wpilibJarLocation(){
		Properties sunprops = new Properties();
		String retValue=null;
		try{
			sunprops.load(new FileReader(System.getProperty("user.home") + File.separator + ".sunspotfrc.properties"));
			retValue = sunprops.getProperty("wpilibj.home");
			retValue += File.separator + "classes.jar";
		}catch(Exception e){e.printStackTrace();}
		
		return retValue;
	}
	
	/**
	 * Return the path to the WPILibJ source files
	 * @return String representing the path to the source files
	 */
	public static String wpilibJarSourceLocation(){
		Properties sunprops = new Properties();
		String retValue=null;
		try{
			sunprops.load(new FileReader(System.getProperty("user.home") + File.separator + ".sunspotfrc.properties"));
			retValue = sunprops.getProperty("wpilibj.home");
			retValue += File.separator + "src";
		}catch(Exception e){e.printStackTrace();}
		
		return retValue;
	}
	
	/**
	 * Returns the string path to the WPILibJ jar file ont he project
	 * @return String representing the path to the jar archive
	 */
	public static String wpilibJarLocation(IJavaProject javaProj){
		if(javaProj == null){return wpilibJarLocation();}
		IClasspathEntry[] entries = null;
		
		try {
			entries  = javaProj.getResolvedClasspath(false);
		} catch (JavaModelException e) {e.printStackTrace();}
		
		if(entries == null){
			return wpilibJarLocation();
		}
		
		for(int i = 0; i < entries.length; i++){
			if(entries[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY){
				if(entries[i].getPath().toFile().getAbsolutePath().endsWith(File.separator + "classes.jar")){
					String retString = entries[i].getPath().toFile().getAbsolutePath();
					return retString;
				}
			}
		}
		
		return wpilibJarLocation();
	}
	
	/**
	 * Return the path to the WPILibJ source files on teh project
	 * @return String representing the path to the source files
	 */
	public static String wpilibJarSourceLocation(IJavaProject javaProj){
		IClasspathEntry[] entries = null;
		
		try {
			entries  = javaProj.getResolvedClasspath(false);
		} catch (JavaModelException e) {e.printStackTrace();}
		
		if(entries == null){
			return wpilibJarSourceLocation();
		}
		
		for(int i = 0; i < entries.length; i++){
			if(entries[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY){
				if(entries[i].getPath().toFile().getAbsolutePath().endsWith(File.separator + "classes.jar")){
					String classes = entries[i].getPath().toFile().getAbsolutePath();
					int endIndex = classes.indexOf(File.separator + "classes.jar");
					classes = classes.substring(0, endIndex);
					classes += File.separator + "src";
				}
			}
		}
		
		return wpilibJarSourceLocation();
	}
}
