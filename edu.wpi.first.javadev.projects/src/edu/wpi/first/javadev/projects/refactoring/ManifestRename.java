package edu.wpi.first.javadev.projects.refactoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.eclipse.core.internal.resources.Resource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;

import edu.wpi.first.javadev.projects.util.StringUtil;

/**
 * Deals with renaming operations in the manifest
 * 
 * @author Ryan O'Meara
 */
@SuppressWarnings("restriction")
public class ManifestRename {
	
	/**
	 * Changes the name in the manifest for the robot or robot package.  Which is being
	 * renamed is determined by the working element
	 * @param element The Java element associated with the thing being renamed
	 * @param oldName The name to replace
	 * @param newName The name to replace the old name with
	 */
	public static void renameInManifest(IJavaElement element, String oldName, String newName){
		if((element == null)||(element.getJavaProject() == null)){return;}
		String locat = element.getJavaProject().getProject()
		.getLocation().toFile().getAbsolutePath() + File.separator 
		+ "resources" + File.separator + "META-INF" + File.separator 
		+ "MANIFEST.MF";
		
		String finalfile="";
		String temp="";
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(locat));
			while((temp = in.readLine()) != null){
				
				if((element instanceof IType)
						&&((temp.indexOf("MIDlet-1") != -1)
								||(temp.indexOf("MIDlet-Name") != -1))){
					temp = StringUtil.replaceDelimitedString(
							temp, oldName, newName);
				}else if((element instanceof IPackageFragment)
						&&(temp.indexOf("MIDlet-1") != -1)){
					temp = StringUtil.replaceDelimitedString(
							temp, oldName, newName);
				}
				
				finalfile += temp + "\n";
			}
			in.close();
		} catch (Exception e) {e.printStackTrace();} 

		File manifest = new File(element.getJavaProject()
				.getProject().getLocation().toFile().getAbsolutePath() 
				+ File.separator + "resources" + File.separator 
				+ "META-INF" + File.separator + "MANIFEST.MF");
		
		try {
			PrintWriter out = new PrintWriter(new FileWriter(manifest));
			out.println(finalfile);
			out.close();
		} catch (Exception e) {e.printStackTrace();}
		
		try{
			element.getJavaProject().getProject()
			.refreshLocal(Resource.DEPTH_INFINITE,null);
		}catch(Exception e){}
	}
}
