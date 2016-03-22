package edu.wpi.first.javadev.builder.util;

import java.util.ArrayList;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

public class ProjectClasses {
	private static IJavaProject activeProject = null;
	private static ArrayList<String> projectClasses = null;
	private static boolean initialized = false;
	
	/** Initializes the Arraylist and project holder */
	private static void initialize(){
		activeProject = null;
		projectClasses = new ArrayList<String>();
		initialized = true;
	}
	
	/**
	 * Generates an Arraylist of class names in the project
	 * @param javaProject The java project to generate for
	 */
	private static void generateClassArray(IJavaProject javaProject){
		try {
			if(javaProject == null){
				projectClasses = null;
				return;
			}
			
			for (IPackageFragmentRoot current : javaProject.getPackageFragmentRoots()) {
				//Optimize scanning, skip jar known to not be used
				if(!current.getElementName().equals("squawk_device.jar")){
					ICompilationUnit[] cus = ModelBuilderUtil.findCompilationUnits(current);
					IClassFile[] cfs = ModelBuilderUtil.findClassFiles(current);
					if(cus != null){
						for (ICompilationUnit currentcu : cus) {
								projectClasses.add(currentcu.findPrimaryType().getElementName());
						}
					}
					
					if(cfs != null){
						for (IClassFile currentcf : cfs) {
							//Prevent doubling
							if(currentcf.findPrimaryType() != null){
								if(currentcf.findPrimaryType().getFullyQualifiedName().indexOf("j2meclasses") == -1){
									projectClasses.add(currentcf.findPrimaryType().getElementName());
								}
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {e.printStackTrace();}
	}
	
	/**
	 * Determines if the given java project contains a class with the name className
	 * @param javaProject The project to search
	 * @param className the name to look for
	 * @return true if the project can find a class by the given name, false otherwise
	 */
	public static boolean isClassInProject(IJavaProject javaProject, String className){
		if(javaProject == null){return false;}
		if(!initialized){initialize();}
		
		if((activeProject == null)||(!activeProject.equals(javaProject))){
			generateClassArray(javaProject);
			activeProject = null;
			activeProject = javaProject;
		}
		
		if(projectClasses == null){return false;}
		
		return projectClasses.contains(className);
	}
	
}
