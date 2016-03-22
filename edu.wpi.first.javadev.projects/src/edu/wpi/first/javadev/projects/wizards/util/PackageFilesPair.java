package edu.wpi.first.javadev.projects.wizards.util;

/**
 * Represents a discreet package and the files it contians. Used with project
 * creation wizards for easy copying and modification to packages during
 * project creation.
 * 
 * @author Ryan O'Meara
 */
public class PackageFilesPair {
	protected String packageName;
	protected String newPackageName;
	protected String[] files;
	
	/**
	 * @param i_packageName The name of the package which contains the files
	 * @param i_files The names of all the files (excluding ".java" extension) 
	 * in the package
	 */
	public PackageFilesPair(String i_packageName, String[] i_files){
		packageName = i_packageName;
		newPackageName = i_packageName;
		files = i_files;
	}
	
	/**
	 * @param i_packageName The name of the package which contains the files
	 * @param i_outputPackageName The name the package name should be changed 
	 * to during project creation
	 * @param i_files The names of all the files (excluding ".java" extension) 
	 * in the package
	 */
	public PackageFilesPair(String i_packageName, String i_outputPackageName, String[] i_files){
		packageName = i_packageName;
		newPackageName = i_outputPackageName;
		files = i_files;
	}
	
	/**
	 * @return The name of the package the this object is representing
	 */
	public String getPackageName(){return packageName;}
	
	/**
	 * @param outputPackage The name this package should be added to the new project as
	 */
	public void setOutputPackageName(String outputPackage){newPackageName = outputPackage;}
	
	/**
	 * @return The name this package should be added to the new project as
	 */
	public String getOutputPackageName(){return newPackageName;}
	
	/**
	 * @return The names of the files contained in the package represented by this object
	 */
	public String[] getFiles(){return files;}
}
