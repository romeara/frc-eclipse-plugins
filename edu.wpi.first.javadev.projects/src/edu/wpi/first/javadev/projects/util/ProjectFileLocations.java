package edu.wpi.first.javadev.projects.util;

import java.io.File;

import org.eclipse.core.runtime.FileLocator;

import edu.wpi.first.javadev.projects.ProjectsPlugin;

public class ProjectFileLocations {
	
	public static String getProjectPluginDirectory(){
		try{
			return FileLocator.getBundleFile(ProjectsPlugin.getDefault().getBundle()).getAbsolutePath();
		}catch(Exception e){return null;}
	}
	
	/**
	 * Retrieves the wizard template root directory in the plug-in's folder
	 * for use with wizards
	 * @return {@link String} representation of the file path to the directory
	 */
	public static String getWizardDirectory(){
		return getProjectPluginDirectory() + File.separator + "WizardTemplates";
	}
	
	/**
	 * Retrieves the directory where files for sample projects are stored
	 * @return {@link String} representation of the path to the directory
	 */
	public static String getSampleWizardsDirectory(){
		return getWizardDirectory() + File.separator + "Samples";
	}
}
