package edu.wpi.first.javadev.projects.wizards.projects;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PartInitException;

import edu.wpi.first.javadev.projects.util.ProjectFileLocations;
import edu.wpi.first.javadev.projects.util.StringUtil;
import edu.wpi.first.javadev.projects.wizards.BaseFRCProjectWizard;
import edu.wpi.first.javadev.projects.wizards.pages.FRCBotProjectWizardPage;
import edu.wpi.first.javadev.projects.wizards.util.PackageFilesPair;
import edu.wpi.first.javadev.sunspotfrcsdk.SDKPlugin;
import edu.wpi.first.javadev.sunspotfrcsdk.preferences.PreferenceConstants;

/**
 * The Base Wizard for WPILibJ projects, such as simple and iterative robot
 * 
 * @author Ryan O'Meara
 */
public abstract class BaseProjectTemplateWizard extends BaseFRCProjectWizard {
	private FRCBotProjectWizardPage	dataPage;
	PackageFilesPair[] pairs;

	public BaseProjectTemplateWizard() {
		super();
		dataPage = new FRCBotProjectWizardPage("Robot", defaultPackageName(), getDefaultClassName());	
	}

	@Override
	protected abstract String getSourceDirectoryName();

	/**
	 * Returns the name to offer as the default for the source file in the new
	 * project
	 * 
	 * @return The name of the class to offer as default when creating a project
	 *         of this type
	 */
	protected abstract String getDefaultClassName();
	
	/**
	 * @return A String representing the name of the main robot class for the template
	 */
	protected abstract String getMainClassName();
	
	/**
	 * @return A String representing the name of the package the main robot class for 
	 * the template is in
	 */
	protected abstract String getMainPackageName();
	

	@Override
	protected void performSourceOperations() {
		configureTemplate();
		configureManifest();
		copyRemainingSourceFiles();
	}

	@Override
	protected File getRootPathFile() {
		return new File(ProjectFileLocations.getWizardDirectory());
	}
	
	@Override
	protected void preFinishOperationProcessing(){
		for(PackageFilesPair currentPackage : getPackageFilePairs()){
			currentPackage.setOutputPackageName(StringUtil.replaceDelimitedString(currentPackage.getOutputPackageName(), getMainPackageName(), dataPage.getPackageName()));
		}
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(dataPage);
	}

	/**
	 * Returns the name to offer as default for the src package when creating a
	 * project of this type
	 * 
	 * @return The name to use for the package by default
	 */
	protected String defaultPackageName() {
		IPreferenceStore store = SDKPlugin.getDefault().getPreferenceStore();
		return "org.usfirst.frc" + store.getInt(PreferenceConstants.P_TEAM_NUMBER);
	}

	/**
	 * Returns the full path of the the template's java file
	 * 
	 * @return The full source path to copy the template from
	 */
	protected String getSourceFilePath(String givenName, String packageName) {
		return getSourceDirectoryName() + File.separator + "src" + File.separator
				+ StringUtil.convertPackageToDirectory(packageName) + File.separator + givenName
				+ ".java";
	}

	/**
	 * Copies the template file, and replaces the template package and class
	 * name with the one entered by the user
	 */
	protected void configureTemplate() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(getSourceFilePath(getMainClassName(),getMainPackageName())));
			PrintWriter out = new PrintWriter(new FileWriter(getOutputFile(dataPage.getRobotName(), dataPage.getPackageName())));
			String temp;
			while (in.ready()) {
				temp = in.readLine();
				if (temp.indexOf("public class") != -1) {
					temp = StringUtil.replaceDelimitedString(temp, getMainClassName(), dataPage.getRobotName());
				}

				if (temp.indexOf("package ") != -1) {
					temp = StringUtil.replaceDelimitedString(temp, getMainPackageName(), dataPage.getPackageName());
				}else if ( temp.indexOf("import") != -1){
					temp = StringUtil.replaceDelimitedString(temp, getMainPackageName(), dataPage.getPackageName());
				}

				out.println(temp);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Copies source files besides those indicated as the main source file (which implements a robot class)
	 */
	protected void copyRemainingSourceFiles(){
		try {
			for(PackageFilesPair currentPackage : getPackageFilePairs()){
				for(String currentFile : currentPackage.getFiles()){
					if(!((currentFile == getMainClassName())&&(currentPackage.getPackageName() == getMainPackageName()))){
						BufferedReader in = new BufferedReader(new FileReader(getSourceFilePath(currentFile, currentPackage.getPackageName())));
						PrintWriter out = new PrintWriter(new FileWriter(getOutputFile(currentFile, currentPackage.getOutputPackageName())));
						String temp;
						while (in.ready()) {
							temp = in.readLine();
							
							if (temp.indexOf("package ") != -1) {
								temp = StringUtil.replaceDelimitedString(temp, currentPackage.getPackageName(), currentPackage.getOutputPackageName());
							}else if ( temp.indexOf("import") != -1){
								temp = StringUtil.replaceDelimitedString(temp, getMainPackageName(), dataPage.getPackageName());
							}
							
							out.println(temp);
						}
						out.close();
						in.close();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	/**
	 * Copies the manifest file to the new project, and renames the template
	 * class and package references to the names indicated by the user
	 */
	protected void configureManifest() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(getManifestSource()));
			PrintWriter out = new PrintWriter(new FileWriter(getManifestOutputFile()));
			String temp;
			while (in.ready()) {
				temp = in.readLine();

				if (temp.indexOf("MIDlet-1") != -1) {
					temp = StringUtil.replaceDelimitedString(temp,
							(getMainPackageName() + "." + getMainClassName()),
							(dataPage.getPackageName() + "." + dataPage.getRobotName()));
				}

				if ((temp.indexOf("MIDlet-Name") != -1) || ((temp.indexOf("MIDlet-1") != -1))) {
					temp = StringUtil.replaceDelimitedString(temp, getMainClassName(), dataPage.getRobotName());
				}

				out.println(temp);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves the location of the manifest file in the project template
	 * source tree
	 * 
	 * @return
	 */
	protected String getManifestSource() {
		return getSourceDirectoryName() + File.separator + "resources" + File.separator + "META-INF" + File.separator
				+ "MANIFEST.MF";
	}

	/**
	 * Retrieves the location to save the Manifest file too, and make sure that
	 * it exists
	 * 
	 * @return File handle to the Manifest file location
	 */
	protected File getManifestOutputFile() {
		String filePath = getNewProject().getFolder("resources").getLocation().toFile().getAbsolutePath()
				+ File.separator + "META-INF" + File.separator + "MANIFEST.MF";
		File outFile = new File(filePath);
		try {
			if (!(outFile.exists())) {
				outFile.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outFile;
	}

	/**
	 * Retrieves the file location the java file will be saved to, and ensures
	 * that it exists
	 * 
	 * @return File handle to the java file location
	 */
	protected File getOutputFile(String givenName, String packageName) {
		String filePath = getNewProject().getFolder("src").getLocation().toFile().getAbsolutePath() + File.separator
				+ StringUtil.convertPackageToDirectory(packageName) + File.separator
				+ givenName + ".java";
		File outFile = new File(filePath);
		try {
			if (!(outFile.exists())) {
				outFile.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outFile;
	}

	/**
	 * When the user finishes with the wizard, this will automatically put the
	 * Robot file into the editor.
	 * 
	 * @author Joe Grinstead
	 */
	@Override
	public boolean performFinish() {
		if (!super.performFinish()) return false;
		
		IJavaProject javaProject = JavaCore.create(getNewProject());

		// Make sure it is a java project
		if (javaProject == null || !javaProject.exists()) return true;

		try {
			IType type = javaProject.findType(defaultPackageName(), getDefaultClassName());
			if (type == null) return true;
			JavaUI.openInEditor(type, true, false);
		} catch (JavaModelException e) {
			e.printStackTrace();
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		return true;
	}
}
