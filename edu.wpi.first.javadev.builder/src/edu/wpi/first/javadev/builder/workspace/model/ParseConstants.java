package edu.wpi.first.javadev.builder.workspace.model;

import java.util.ArrayList;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import edu.wpi.first.javadev.builder.util.ModelBuilderUtil;
import edu.wpi.first.javadev.projects.nature.FRCProjectNature;

/**
 * Provides constants which allow the parsing of source into more user-friendly formats
 * 
 * @author Ryan O'Meara
 */
public class ParseConstants {
	/** Constant representing qualified name of the device interface */
	public static final String DEVICE = "edu.wpi.first.wpilibj.parsing.IDevice";
	
	/** Constant representing qualified name of mechanism interface */
	public static final String MECHANISM = "edu.wpi.first.wpilibj.parsing.IMechanism";
	
	//TODO make this only be qualified name once inserted into wpilibj
	/** Constants representing qualified name of class states extend */
	public static final String[] STATE_ID	= { "State", "edu.wpi.wpilibj.states.State"};
	
	//TODO make this only be the qualified name once inserted into wpilibj
	/** Constants representing qualified name of class state machine extend */
	public static final String[] STATEMACHINE_ID = { "StateMachine", "edu.wpi.wpilibj.states.StateMachine"};
	
	//TODO make this only be qualified name once inserted into wpilibj
	public static final String[] EVENT = {"Event", "edu.wpi.wpilibj.events.Event"};
	
	private static final String ROBOT_BASE = "edu.wpi.first.wpilibj.RobotBase";
	
	private static String[] robots = null;  //necessary to cache robot qualified names, to speed up process
	
	/** Returns the last set of qualified names discovered to extend robot base,
	 * creates if it does not already exist, from the provided project
	 */
	public static String[] getRobotQualifiedNames(IJavaProject project){
		if(robots == null){
			parseRobotNames(project);
		}
		
		return robots;
	}
	
	/** Re-parses the robot names using the current project's instance of wpilibj */
	public static void parseRobotNames(IJavaProject project){
		try{
			if(!project.getProject().hasNature(FRCProjectNature.FRC_PROJECT_NATURE)){
				robots = new String[]{};
				return;
			}
			
			ArrayList<IType> types = new ArrayList<IType>();

			try {
				for (IPackageFragmentRoot current : project.getPackageFragmentRoots()) {
					//Optimize scanning, skip jar known to not be used
					if(current.getElementName().equals("classes.jar")){
						ICompilationUnit[] cus = ModelBuilderUtil.findCompilationUnits(current);
						IClassFile[] cfs = ModelBuilderUtil.findClassFiles(current);

						for (ICompilationUnit currentcu : cus) {
								types.add(currentcu.findPrimaryType());
						}
						
						for (IClassFile currentcf : cfs) {
							//Prevent doubling
							if(currentcf.findPrimaryType().getFullyQualifiedName().indexOf("j2meclasses") == -1){
								types.add(currentcf.findPrimaryType());
							}
						}
					}
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			
			ArrayList<IType> robotClasses = new ArrayList<IType>();
			
			for(IType current : types){
				if((ModelBuilderUtil.isClass(current, ROBOT_BASE))&&(!ROBOT_BASE.equalsIgnoreCase(current.getFullyQualifiedName()))){
					robotClasses.add(current);
				}
			}
			
			ArrayList<String> qual = new ArrayList<String>();
			
			for(IType current : robotClasses){
				qual.add(current.getFullyQualifiedName());
			}
			
			robots = qual.toArray(new String[qual.size()]);
			return;
			
		}catch(Exception e){}
		
		robots = new String[]{};
		return;
	}
}
