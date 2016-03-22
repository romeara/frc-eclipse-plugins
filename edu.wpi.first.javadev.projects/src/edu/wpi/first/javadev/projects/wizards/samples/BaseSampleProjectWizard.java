package edu.wpi.first.javadev.projects.wizards.samples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import edu.wpi.first.javadev.projects.util.ProjectFileLocations;
import edu.wpi.first.javadev.projects.util.StringUtil;
import edu.wpi.first.javadev.projects.wizards.BaseFRCProjectWizard;
import edu.wpi.first.javadev.projects.wizards.util.PackageFilesPair;

/**
 * The Base wizard for all wpi sample programs
 * 
 * @author Ryan O'Meara
 */
public abstract class BaseSampleProjectWizard extends BaseFRCProjectWizard {
	
	@Override
	protected abstract String getSourceDirectoryName();

	@Override
	protected File getRootPathFile() {
		return new File(ProjectFileLocations.getSampleWizardsDirectory());
	}

	@Override
	protected void performSourceOperations() {
		copySampleSource();
		copyManifestFile();
	}
	
	@Override
	protected void preFinishOperationProcessing(){}

	/**
	 * Returns the full path of the the template's java file
	 * 
	 * @param The
	 *            file to return the path for
	 * @return The full source path to copy the template from
	 */
	protected String getSourceFilePath(String givenName, String packageName) {
		return getSourceDirectoryName() + File.separator + "src" + File.separator
				+ StringUtil.convertPackageToDirectory(packageName) + File.separator + givenName + ".java";
	}

	/**
	 * Copies the source of the sample from the stored template to the project
	 */
	protected void copySampleSource() {
		String[] files;
		for(PackageFilesPair currentFiles : getPackageFilePairs()){
			try {
				for (int i = 0; i < (files = currentFiles.getFiles()).length; i++) {
					BufferedReader in = new BufferedReader(new FileReader(getSourceFilePath(files[i], currentFiles.getPackageName())));
					PrintWriter out = new PrintWriter(new FileWriter(getTemplateOutputFile(files[i], currentFiles.getOutputPackageName())));
					while (in.ready()) {
						out.println(in.readLine());
					}
					out.close();
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Copies the manifest from the template source to the project
	 */
	protected void copyManifestFile() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(getManifestSource()));
			PrintWriter out = new PrintWriter(new FileWriter(getManifestOutputFile()));
			while (in.ready()) {
				out.println(in.readLine());
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
	 * @param givenName
	 *            The file to designate a path for
	 * @return File handle to the java file location
	 */
	protected File getTemplateOutputFile(String givenName, String packageName) {
		String filePath = getNewProject().getFolder("src").getLocation().toFile().getAbsolutePath() + File.separator
				+ StringUtil.convertPackageToDirectory(packageName) + File.separator + givenName + ".java";
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
}
