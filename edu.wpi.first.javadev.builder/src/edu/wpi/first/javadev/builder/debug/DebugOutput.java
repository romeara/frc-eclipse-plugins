package edu.wpi.first.javadev.builder.debug;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;

import edu.wpi.first.javadev.builder.workspace.model.IFRCElementParent;

/**
 * Provides debug printing which can be turned on/off from the class
 * utilizing this object.  Contains support for tab levels.  i.e:
 * 
 * Tab level zero output:
 * "hi"
 * 
 * Tab level one output:
 * "\thi" or "	hi"
 * 
 * Tab level two output:
 * "\t\thi" or "		hi"
 * 
 * and so on.
 * 
 * @author Ryan O'Meara
 */
public class DebugOutput {
	protected static boolean enabled;
	protected static int tabLevel;
	
	/**
	 * Initializes the debugger to not print and have a tab level of zero
	 */
	public static void initialize(){
		enabled = false;
		tabLevel = 0;
	}
	
	/**
	 * Outputs the given string if debugging is turned on
	 * @param output The String to output
	 */
	public static void printDebug(String output){
		if(enabled){
			tabCorrect();
			System.out.println(output);
		}
	}
	
	/**
	 * Turns on or off debug prints
	 * @param enable true if printing is desired, false otherwise
	 */
	public static void setEnabled(boolean enable){
		enabled = enable;
	}
	
	/**
	 * Adds the correct number of tabs to the print line
	 */
	protected static void tabCorrect(){
		for(int i = -1; i < tabLevel; i++){
			System.out.print("-");
		}
	}
	
	/**
	 * Adds one to the tab level
	 */
	public static void incrementTab(){
		tabLevel++;
	}
	
	/**
	 * Removes one form the tab level
	 */
	public static void decrementTab(){
		tabLevel--;
	}
	
	/**
	 * Puts the tab level to the specified value
	 * @param tab
	 */
	public static void setTab(int tab){
		tabLevel = tab;
	}
	
	/**
	 * Sets the tab level to zero
	 */
	public static void resetTab(){
		setTab(0);
	}
	
	/** Prints a IModelParent and its children */
	public static void printTree(IFRCElementParent<?> root){
		resetTab();
		System.out.println(root);
		printChildren(root);
	}
	
	/** Loops through and prints the children of an IModelParent */
	private static void printChildren(IFRCElementParent<?> root){
		incrementTab();
		for(Object current : root.getChildren()){
			tabCorrect();
			System.out.println(current);
			if(current instanceof IFRCElementParent<?>){
				printChildren((IFRCElementParent<?>)current);
			}
		}
		decrementTab();
	}
	
	public static void printDeltaNested(IJavaElementDelta source, int tabLevel){
		if(source == null){return;}
		for(int i = 0; i < tabLevel; i++){System.out.print("\t");}
		
		System.out.println(getTypeString(source) + ", E: " + source.getElement().getElementName() + ", R: " + getStringReason(source));
		
		if(source.getAffectedChildren() != null){
			for(IJavaElementDelta current : source.getAffectedChildren()){
				printDeltaNested(current, tabLevel+1);
			}
		}
	}
	
	/**
	 * @param source The delta to find the reason for
	 * @return The reason for the given delta
	 */
	@SuppressWarnings("deprecation")
	private static String getStringReason(IJavaElementDelta source){
		if(source == null){return "NULL";}
		
		String returnStr = "";
		
		switch(source.getKind()){
		case IJavaElementDelta.ADDED:
			returnStr += "ADDED:";
			break;
		case IJavaElementDelta.REMOVED:
			returnStr += "REMOVED:";
			break;
		case IJavaElementDelta.CHANGED:
			returnStr += "CHANGED:";
			break;
		default:
			returnStr += "OTHER:";
			break;
		}
		
		
		if((source.getFlags() & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0){
			returnStr += ":ADDED_TO_CLASSPATH";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_ANNOTATIONS) != 0){
			returnStr += ":ANNOTATIONS";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED) != 0){
			returnStr += ":ARCHIVE_CONTENT_CHANGED";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_AST_AFFECTED) != 0){
			returnStr += ":AST_AFFECTED";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_CATEGORIES) != 0){
			returnStr += ":CATEGORIES";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_CHILDREN) != 0){
			returnStr += ":CHILDREN";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0){
			returnStr += ":CLASSPATH_CHANGED";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_CLASSPATH_REORDER) != 0){
			returnStr += ":CLASSPATH_REORDER";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_CLOSED) != 0){
			returnStr += ":CLOSED";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_CONTENT) != 0){
			returnStr += ":CONTENT";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_FINE_GRAINED) != 0){
			returnStr += ":FINE_GRAINED";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_MODIFIERS) != 0){
			returnStr += ":MODIFIERS";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_MOVED_FROM) != 0){
			returnStr += ":MOVED_FROM";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_MOVED_TO) != 0){
			returnStr += ":MOVED_TO";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_OPENED) != 0){
			returnStr += ":OPENED";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_PRIMARY_RESOURCE) != 0){
			returnStr += ":PRIMARY_RESOURCE";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_PRIMARY_WORKING_COPY) != 0){
			returnStr += ":PRIMARY_WORKING_COPY";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0){
			returnStr += ":REMOVED_FROM_CLASSPATH";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_REORDER) != 0){
			returnStr += ":REORDER";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0){
			returnStr += ":RESOLVED_CLASSPATH_CHANGED";
		}
		
		if((source.getFlags() & IJavaElementDelta.F_SOURCEATTACHED) != 0){
			returnStr += ":SOURCEATTACHED";
		}

		if((source.getFlags() & IJavaElementDelta.F_SOURCEDETACHED) != 0){
			returnStr += ":SOURCEDTACHED";
		}
	
		if((source.getFlags() & IJavaElementDelta.F_SUPER_TYPES) != 0){
			returnStr += ":SUPER_TYPES";
		}
	
		return returnStr;
	}
	
	/**
	 * @param source IJavaElementDelta to determine the type of
	 * @return String representing the type of the delta
	 */
	private static String getTypeString(IJavaElementDelta source){
		if(source == null){return "NULL";}
		
		String returnStr = "";
		
		switch(source.getElement().getElementType()){
		case IJavaElement.ANNOTATION:
			returnStr += "ANNOTATION";
			break;
		case IJavaElement.CLASS_FILE:
			returnStr += "CLASS_FILE";
			break;
		case IJavaElement.COMPILATION_UNIT:
			returnStr += "COMPILATION_UNIT";
			break;
		case IJavaElement.FIELD:
			returnStr += "FIELD";
			break;
		case IJavaElement.IMPORT_CONTAINER:
			returnStr += "IMPORT_CONTAINER";
			break;
		case IJavaElement.IMPORT_DECLARATION:
			returnStr += "IMPORT_DECLARATION";
			break;
		case IJavaElement.INITIALIZER:
			returnStr += "INITIALIZER";
			break;
		case IJavaElement.JAVA_MODEL:
			returnStr += "JAVA_MODEL";
			break;
		case IJavaElement.JAVA_PROJECT:
			returnStr += "JAVA_PROJECT";
			break;
		case IJavaElement.LOCAL_VARIABLE:
			returnStr += "LOCAL_VARIABLE";
			break;
		case IJavaElement.METHOD:
			returnStr += "METHOD";
			break;
		case IJavaElement.PACKAGE_DECLARATION:
			returnStr += "PACKAGE_DECLARATION";
			break;
		case IJavaElement.PACKAGE_FRAGMENT:
			returnStr += "PACKAGE_FRAGMENT";
			break;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			returnStr += "PACKAGE_FRAGMENT_ROOT";
			break;
		case IJavaElement.TYPE:
			returnStr += "TYPE";
			break;
		case IJavaElement.TYPE_PARAMETER:
			returnStr += "TYPE_PARAMETER";
			break;
		default:
			returnStr += "OTHER";
			break;
		}
		
		
		return returnStr;
	}
}
