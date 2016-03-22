package edu.wpi.first.javadev.projects.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import edu.wpi.first.javadev.projects.nature.FRCProjectNature;
import edu.wpi.first.javadev.projects.util.ConfigurationElement;
import edu.wpi.first.javadev.projects.wizards.util.PackageFilesPair;


/**
 * Base wizard which is extended by any FRC wizard, sample, base project, or
 * otherwise.  Contains method framework and common operations such as nature
 * application
 * 
 * @author Ryan O'Meara
 */
@SuppressWarnings("restriction")
public abstract class BaseFRCProjectWizard extends BasicNewProjectResourceWizard 
implements INewWizard{
	protected PackageFilesPair[] pairs;
	
	public BaseFRCProjectWizard(){
		super();
		pairs = getStartingPackageFilePairs();
	}
	
	/**
	 * Returns the full path of the source template's resources
	 * 
	 * @return The Absolute path of the project template being used
	 */
	protected abstract String getSourceDirectoryName();

	/**
	 * Returns a file where all the template files of the given wizard type are
	 * 
	 * @return File leading to root folder for this wizard type
	 */
	protected abstract File getRootPathFile();

	/**
	 * Performs wizard type-specific operations on source files, such as
	 * manifest file modification/copying, and source file copying. Extending
	 * classes should call all file modifications specific to them from here, as
	 * this function is called after the Finish button is pressed
	 */
	protected abstract void performSourceOperations();

	/**
	 * Returns the sets of files and packages in the template to be copied. 
	 * Each element of the array represents the files in a single, discreet
	 * package
	 * @return An array of package-file pairs to be processed
	 */
	protected abstract PackageFilesPair[] getStartingPackageFilePairs();
	
	/**
	 * Performs any processing necessary before dealing with template files,
	 * such as changing the output name of packages based on wizard input
	 */
	protected abstract void preFinishOperationProcessing();

	
	@Override
	public boolean canFinish() {
		IWizardPage[] allPages = super.getPages();

		for (int i = 0; i < allPages.length; i++) {
			if (!(allPages[i].isPageComplete())) {
				return false;
			}
		}

		return true;
	}
	
	/**
	 * Override this method so that if anyone uses the associatedperspective
	 * extension point, then the final perspective will be that one as opposed
	 * to the default java perspective.
	 * 
	 * @author Joe Grinstead
	 */
	@Override
	public void setInitializationData(final IConfigurationElement cfig, String propertyName, Object data) {
		IExtensionRegistry reg = Platform.getExtensionRegistry();
		final IConfigurationElement[] extensions = reg
				.getConfigurationElementsFor("edu.wpi.first.javadev.projects.associatedperspective");

		IConfigurationElement element = new ConfigurationElement(cfig) {
			@Override
			public String getAttribute(String name) throws InvalidRegistryObjectException {
				if (name.equals("finalPerspective")) {
					return extensions.length == 0 ? super.getAttribute(name) : extensions[0].getAttribute(name);
				} else if (name.equals("preferredPerspectives")) {
					String string = null;
					for (IConfigurationElement extension : extensions) {
						if (string == null) {
							string = extension.getAttribute(name);
						} else {
							string += "," + extension.getAttribute(name);
						}
					}
					return string == null ? super.getAttribute(name) : string;
				} else {
					return super.getAttribute(name);
				}
			}
		};

		super.setInitializationData(element, propertyName, data);
	}

	@Override
	public boolean performFinish(){
		boolean retVal = super.performFinish();
		
		if(retVal){
			
			try {
				preFinishOperationProcessing();
				
				//Add FRC project nature, which includes java nature
				IProjectDescription description = getNewProject().getDescription();
				String[] natures = description.getNatureIds();
				String[] newNatures = new String[natures.length + 1];
				System.arraycopy(natures, 0, newNatures, 0, natures.length);
				newNatures[natures.length] = FRCProjectNature.FRC_PROJECT_NATURE;
				description.setNatureIds(newNatures);
				getNewProject().setDescription(description, null);
				
				//Copy files into project
				copyDirectory(new File(getSourceDirectoryName()), 
						getNewProject().getLocation().toFile());
				
				// Refresh local workspace to prevent errors
				getNewProject().refreshLocal(Resource.DEPTH_INFINITE, null);
				
				//Create reference to java project
				IJavaProject newProj = JavaCore.create(getNewProject());
				
				// Create package in src(source) folder
				IFolder folder = getNewProject().getFolder("src");
				IPackageFragmentRoot srcFolder = newProj.getPackageFragmentRoot(folder);
				
				for(PackageFilesPair current : getPackageFilePairs()){
					srcFolder.createPackageFragment(current.getOutputPackageName(), true, null);
				}
				
				// Transfer java and manifest files to package with new
				// name/package values
				performSourceOperations();

				// Refresh local workspace view for correct display of project
				getNewProject().refreshLocal(Resource.DEPTH_INFINITE, null);
			} catch (Exception e) {e.printStackTrace();}
			
		}else{
			System.err.println("Error creating FRC Project");
		}
		
		return retVal;
	}
	
	/**
	 * Returns the package name of the created project
	 * 
	 * @return package name for the new project
	 */
	protected final PackageFilesPair[] getPackageFilePairs(){
		return pairs;
	}
	
	/**
	 * Copies directory at srcPath to dstPath, creating the directory structure
	 * if need be
	 * @param srcPath Location of directory to copy
	 * @param dstPath Location to copy to
	 * @throws IOException FileInput/OutputStream
	 */
	protected void copyDirectory(File srcPath, File dstPath) throws IOException {
		if (srcPath.isDirectory()) {
			if (srcPath.getAbsolutePath().indexOf("src") == -1) {
				if (!dstPath.exists()) {
					dstPath.mkdir();
				}

				String files[] = srcPath.list();

				for (int i = 0; i < files.length; i++) {
					copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]));
				}
			}
		} else {
			if (!srcPath.exists()) {
				System.err.println("Path not found\n");
			} else if ((srcPath.getAbsolutePath().indexOf(".classpath") == -1)
					&& (srcPath.getAbsolutePath().indexOf(".svn") == -1)
					&& (srcPath.getAbsolutePath().indexOf("src") == -1)
					&& (srcPath.getAbsolutePath().indexOf(".MF") == -1)) {
				InputStream in = new FileInputStream(srcPath);
				OutputStream out = new FileOutputStream(dstPath);
				// Transfer bytes from in to out
				byte[] buf = new byte[1024];
				int len;

				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}

				in.close();
				out.close();
			}
		}

	}
}
