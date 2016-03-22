package edu.wpi.first.javadev.builder.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportContainer;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

import edu.wpi.first.codedev.output.FRCDialog;
import edu.wpi.first.javadev.builder.CodeViewerPlugin;

/**
 * Functions to modify source code and make additions to it
 * 
 * @author Ryan O'Meara
 */
public class SourceModification {
	/** Adds a protected field of type add to the type addto */
	public static IField addField(IType addTo, IType add, String visibility){
		return addField(addTo, add, visibility, add.getElementName().toLowerCase(), -1);
	}
	
	/** Adds a protected field of type add to the type addto, with the given name */
	public static IField addField(IType addTo, IType add, String visibility, String name){
		return addField(addTo, add, visibility, name, -1);
	}
	
	/** Adds a protected field of type add to the type addto, with the given name, 
	 * and the given name qualifier */
	private static IField addField(IType addTo, IType add, String visibility, String name, int addToSize){
		IField createdField = null;
		if(visibility == null){visibility = "protected";}
		visibility = visibility.toLowerCase();
		
		if(!((visibility.equals("public"))
				||(visibility.equals("protected"))
				||(visibility.equals("private")))){
			visibility = "protected";
		}
		
		if((name == null)||(name.length() == 0)){name = add.getElementName().toLowerCase();}
		try {
			String contents = visibility + " ";
			contents += add.getElementName() + " ";
			contents += name;
			if(addToSize != -1){
				contents += addToSize;
			}
			
			contents += ";";
			
			createdField = addTo.createField(contents, null, false, null);
		} catch (JavaModelException e) {
			if(e.getJavaModelStatus().getCode() == IJavaModelStatusConstants.NAME_COLLISION){
				addField(addTo, add, visibility, name, addToSize+1);
			}
		}
		
		//Add import if not already there
		ICompilationUnit cu = addTo.getCompilationUnit();
		
		if(cu != null){
			IImportContainer ic = cu.getImportContainer();
			
			if(!ic.getImport(add.getFullyQualifiedName()).exists()){
				try {
					cu.createImport(add.getFullyQualifiedName(), null, null);
				} catch (Exception e) {e.printStackTrace();}
			}
		}
		
		return createdField;
	}
	
	/** Adds a method with the given properties to teh given type */
	public static IMethod addMethod(IType addTo, String visibility, String returnType, String name, String parameters){
		if(name == null){return null;}
		
		String contents = "";
		
		String retStatement = "";
		
		if(!isVisibility(visibility)){visibility = "public";}
		
		if(returnType == null){returnType = "void";}
		
		if(parameters == null){parameters = "";}
		
		if((retStatement = getReturnStatement(returnType)) == null){retStatement = "";}
		
		contents += visibility + " " + returnType + " " + name + " (" 
		+ parameters + "){" + retStatement + "}";

		return addMethod(addTo, contents);
	}
	
	/** Adds a method with the given source to the given type */
	public static IMethod addMethod(IType addTo, String methodContents){
		if((methodContents == null)||(addTo == null)){return null;}
		
		IMethod retMeth = null;
		
		try{
			retMeth = addTo.createMethod(methodContents, null, false, null);	
		}catch(JavaModelException e){
			if(((JavaModelException)e).getJavaModelStatus().getCode() 
					== IJavaModelStatusConstants.NAME_COLLISION){
				FRCDialog.createErrorDialog("Method with given name already exists", 
						new Status(IStatus.WARNING,
								CodeViewerPlugin.PLUGIN_ID,
								"Given method name already exists"));
			}else{
				FRCDialog.createErrorDialog("Method encountered error during creation", 
						new Status(IStatus.WARNING,
								CodeViewerPlugin.PLUGIN_ID,
								"Method could not be created"));
			}
			
			return null;
		}
		
		return retMeth;
	}
	
	/** Determines if the given string is a visibility keyword */
	public static boolean isVisibility(String candidate){
		if(candidate.equalsIgnoreCase("public")||candidate.equalsIgnoreCase("protected")||candidate.equalsIgnoreCase("private")){
			return true;
		}
		
		return false;
	}
	
	/** Determines the default return statement, if any, for the given return type */
	public static String getReturnStatement(String retType){
		if((retType == null)||retType.equalsIgnoreCase("void")){
			return null;
		}else if(retType.equalsIgnoreCase("boolean")){
			return "return false;";
		}else if(retType.equalsIgnoreCase("int")||retType.equalsIgnoreCase("double")||retType.equalsIgnoreCase("byte")
			||retType.equalsIgnoreCase("short")||retType.equalsIgnoreCase("long")||retType.equalsIgnoreCase("float")
			||retType.equalsIgnoreCase("char")){
			return "return 0;";
		}
		
		return "return null;";
	}
}
