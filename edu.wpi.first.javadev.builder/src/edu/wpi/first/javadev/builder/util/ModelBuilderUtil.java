package edu.wpi.first.javadev.builder.util;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import edu.wpi.first.javadev.builder.workspace.model.FRCModel;

/**
 * Utility classes used to build various model elements
 * 
 * @author Ryan O'Meara
 */
public class ModelBuilderUtil {
	/**
	 * Creates/retrieves the IType for a given IField, if it exists in the
	 * project class path
	 * 
	 * @param element
	 *            The IField to create the IType for
	 * @return The IType, or null if it does not exist
	 */
	public static IType createIFieldType(IField element) {
		IType resType;

		if (element.getClassFile() == null) {
			resType = element.getCompilationUnit().findPrimaryType();
		} else {
			resType = element.getClassFile().getType();
		}
		
		//Get types in the class and check to make sure it is not an internal class
		try{
			IType[] internal = resType.getTypes();
			String elementQual = Signature.getSignatureQualifier(element.getTypeSignature())+"."+Signature.getSignatureSimpleName(element.getTypeSignature()); 
			
			if(internal != null){
				for(IType current : internal){
					if(elementQual.equalsIgnoreCase(current.getFullyQualifiedName())){
						return current;
					}
				}
			}
		}catch(Exception e){/*Just continue on with alternate method*/}

		try {
			return getJavaElement(resType, element.getTypeSignature());
		} catch (JavaModelException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Returns the Java element referenced with the given signature in the given
	 * class
	 * 
	 * @param type
	 *            the compilation unit the signature was found in
	 * @param signature
	 *            the signature to resolve
	 * @return The IType, or null if it does not exist
	 */
	public static IType getJavaElement(IType type, String signature) {
		if (signature == null) return null;
		try {
			if (type != null) {
				String[][] matches = type.resolveType(Signature.getSignatureSimpleName(signature));
				if (matches != null) {
					IType elementType = type.getJavaProject().findType(matches[0][0], matches[0][1],
							(IProgressMonitor) null);
					return elementType;
				}
			}
		} catch (Exception e) {}

		return null;
	}

	/**
	 * Returns a correctly constructed code fragment for the given method, 
	 * whether it is a constructor, static
	 * @param method
	 * @return
	 */
	public static String createMethodCodeFragment(IMethod method){
		if(method == null){return "";}
		
		String codeFragment = "";
		
		//Determine if constructor or standard method type
		try{
			
			
			if(method.isConstructor()){
				//Method beginning will be "<class name> <instance name place holder> = new "
				//(class name = method name in the case of a constructor)
				codeFragment = method.getElementName() + " " + FRCModel.INSERT_INSTANCE_NAME + " = new ";
			}else if(Flags.isPublic(method.getFlags())&&Flags.isStatic(method.getFlags())){
				//Method beginning will be "<class name>."
				//(class name = method.getDeclaringType().getElementName() in this case)
				codeFragment = method.getDeclaringType().getElementName() + ".";
			}else if(Flags.isPrivate(method.getFlags())){
				//private methods, even static ones, will only be called from within
				codeFragment = "";
			}else{
				//Method beginning will be "<instance name place holder>."
				codeFragment = FRCModel.INSERT_INSTANCE_NAME + ".";
			}
			
			//In all cases, end of code fragment is "<method name>(<parameters (formatted)>);"
			codeFragment += method.getElementName() + "(" + formatMethodParameters(method) + ");";
			
		}catch(Exception e){
			//If an exception occurs, assume no code frag for this method
			codeFragment = "";
		}
		
		return codeFragment;
	}
	
	private static String formatMethodParameters(IMethod method){
		if(method.getNumberOfParameters() <= 0){return "";}
		
		String[] params = new String[method.getNumberOfParameters()];
		
		try {
			
			String[] params2 = method.getParameterNames();
			
			int i;
			//Loops through given parameter names
			for (i = 0; i < params2.length; i++) {params[i] = params2[i];}

			//Fill in any gaps left by the retrieved parameter names
			if (i < params.length) {
				int temp = i;
				for (i = temp; i < params.length; i++) {
					params[i] = "param" + (i + 1);
				}
			}
			
		} catch (Exception e) {
			//If retrieving actual names fails, then use place holder names
			for (int i = 0; i < params.length; i++) {
				params[i] = "params" + (i + 1);
			}
		}
		
		//Format the string for return
		String retString = "";

		for (int i = 0; i < params.length; i++) {
			retString += params[i];

			if (i != (params.length - 1)) {
				retString += ", ";
			}
		}
		
		return retString;
	}

	/**
	 * Returns a display name for the given method
	 * 
	 * @param element
	 *            The element to create a display name for
	 * @return display name String
	 */
	public static String createDisplayName(IMethod element) {
		String sig = null;
		String retSig = null;
		String visibility = null;
		int flags;

		try {
			sig = element.getSignature();
			retSig = element.getReturnType();
			flags = element.getFlags();
			
			if(Flags.isPublic(flags)){visibility = "public";}
			else if(Flags.isProtected(flags)){visibility = "protected";}
			else{visibility = "private";}
		} catch (Exception e) {return "";}

		boolean isConstruct = false;

		try {
			isConstruct = element.isConstructor();
		} catch (Exception e) { return "";}

		if (isConstruct) {
			retSig = " : " + visibility;
		} else {
			retSig = " : " + visibility + " " + Signature.getSignatureSimpleName(retSig);
		}

		if ((sig != null) && (retSig != null)) {
			return Signature.toString(sig, element.getElementName(), null, false, false) + retSig;
		}

		return element.getElementName();
	}

	/** Finds and returns the compilation units of higher level elements */
	public static ICompilationUnit[] findCompilationUnits(IJavaElement element) {
		if (element instanceof ICompilationUnit) {
			return new ICompilationUnit[] { (ICompilationUnit) element };
		}

		ArrayList<ICompilationUnit> cus = new ArrayList<ICompilationUnit>();

		if ((element instanceof IJavaProject) || (element instanceof IPackageFragmentRoot)
				|| (element instanceof IPackageFragment)) {
			try {
				for (IJavaElement current : ((IParent) element).getChildren()) {
					ICompilationUnit[] temp = findCompilationUnits(current);

					if (temp != null) {
						for (ICompilationUnit cu : temp) {
							cus.add(cu);
						}
					}
				}

				return cus.toArray(new ICompilationUnit[cus.size()]);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		return null;

	}

	/** Finds and returns the compilation units of higher level elements */
	public static IClassFile[] findClassFiles(IJavaElement element) {
		if (element instanceof IClassFile) {
			return new IClassFile[] { (IClassFile) element };
		}

		ArrayList<IClassFile> cus = new ArrayList<IClassFile>();

		if ((element instanceof IJavaProject) || (element instanceof IPackageFragmentRoot)
				|| (element instanceof IPackageFragment)) {
			try {
				for (IJavaElement current : ((IParent) element).getChildren()) {
					IClassFile[] temp = findClassFiles(current);

					if (temp != null) {
						for (IClassFile cu : temp) {
							cus.add(cu);
						}
					}
				}

				return cus.toArray(new IClassFile[cus.size()]);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		return null;

	}

	/** Returns true if the given type implements the given interface, an interface
	 * that extends it, or is an interface that extends it */
	public static boolean isInterface(IType testInterface, String qualifiedNameBase) {
		if (testInterface == null) {
			return false;
		}
		if (testInterface.getFullyQualifiedName().equalsIgnoreCase(qualifiedNameBase)) {
			return true;
		}

		try {
			IType resType;

			if (testInterface.getClassFile() == null) {
				resType = testInterface.getCompilationUnit().findPrimaryType();
			} else {
				resType = testInterface.getClassFile().getType();
			}

			String[] typeSigs = testInterface.getSuperInterfaceTypeSignatures();

			for (String current : typeSigs) {
				if (isInterface(getJavaElement(resType, current), qualifiedNameBase)) {
					return true;
				}
			}
		} catch (Exception e) {}
		return false;
	}

	/** Returns true if this is or is a child class of the given named class */
	public static boolean isClass(IType testClass, String qualifiedNameBase) {
		if (testClass == null) {
			return false;
		}
		if (testClass.getFullyQualifiedName().equalsIgnoreCase(qualifiedNameBase)) {
			return true;
		}

		try {
			IType resType;

			if (testClass.getClassFile() == null) {
				resType = testClass.getCompilationUnit().findPrimaryType();
			} else {
				resType = testClass.getClassFile().getType();
			}

			String typeSig = testClass.getSuperclassTypeSignature();

			if (isInterface(getJavaElement(resType, typeSig), qualifiedNameBase)) {
				return true;
			}

		} catch (Exception e) {}
		return false;
	}
	
	/** Returns true if the class is or extends any of the named classes provided */
	public static boolean isClass(IType testClass, String[] qualifiedNameBases) {
		for(String current : qualifiedNameBases){
			if(isClass(testClass, current)){
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean inSamePackage(ICompilationUnit cu1, ICompilationUnit cu2){
		if((cu1 == null)||(cu2 == null)){return false;}
		try{
			if((cu1.getPackageDeclarations().length != 1)||(cu2.getPackageDeclarations().length != 1)){
				return false;
			}
			
			return (cu1.getPackageDeclarations()[0].getElementName().equalsIgnoreCase(
					cu2.getPackageDeclarations()[0].getElementName()));
		}catch(Exception e){
			return false;
		}
	}
	
	/**
	 * Compares to compilation units, ignoring working copy status, and determines if they
	 * are the same.  Both being equal to null does not constitute equality.  If either is
	 * null, they are considered unequal
	 * @param comp1 The first compilation unit to compare
	 * @param comp2 The second compilation unit to compare
	 * @return true if the two units represent the same file, false otherwise
	 */
	public static boolean isSameCompilationUnit(ICompilationUnit comp1, ICompilationUnit comp2){
		if((comp1 == null)||(comp2 == null)){return false;}
		
		if(comp1.equals(comp2)){return true;}
		
		
		try{
			IPackageDeclaration[] decs1 = comp1.getPackageDeclarations();
			IPackageDeclaration[] decs2 = comp2.getPackageDeclarations();
			
			ArrayList<IPackageDeclaration> d2 = new ArrayList<IPackageDeclaration>();
			
			for(IPackageDeclaration dec : decs2){d2.add(dec);}
			
			if(decs1.length == decs2.length){
				for(IPackageDeclaration dec : decs1){
					if(!d2.contains(dec)){return false;}
				}
				
				if(comp1.getElementName().equals(comp2.getElementName())){
					return true;
				}
			}
		}catch(Exception e){return false;}
		
		return false;
	}
}
