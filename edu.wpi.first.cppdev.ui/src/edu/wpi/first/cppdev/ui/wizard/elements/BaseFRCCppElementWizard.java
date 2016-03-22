package edu.wpi.first.cppdev.ui.wizard.elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import edu.wpi.first.codedev.output.FRCDialog;
import edu.wpi.first.cppdev.ui.FRCCppUserInterfacePlugin;
import edu.wpi.first.cppdev.ui.util.StringUtil;
import edu.wpi.first.cppdev.ui.wizard.page.FRCTemplateFileWizardPage;

public abstract class BaseFRCCppElementWizard extends Wizard implements INewWizard {
	protected IProject activeProject;
	private FRCTemplateFileWizardPage	dataPage;
	
	/** @return The file path for the header file template associated with the particular class type */
	protected abstract String getTemplateHeaderFilePath();
	
	/** @return The file path for the source file template associated with the particular class type */
	protected abstract String getTemplateSourceFilePath();
	
	/** @return The name of the folder the particular class type is added to */
	protected abstract String getDestinationFolderName();
	
	/** @return The name to display in the wizard when creating the particular class type */
	protected abstract String getWizardDisplayName();
	
	/** @return The class name in the template files to replace with the name input by the user*/
	protected abstract String getTemplateClassName();
	
	/** @return The name to offer as the default for new classes of this type */
	protected abstract String getDefaultNewFileName();
	
	/** @return The string in the header file template which prevents 
	 * multiple definitions, to be replace by user input string*/
	protected abstract String getTemplateHeaderDef();

	public BaseFRCCppElementWizard() {
		activeProject = null;
		dataPage = new FRCTemplateFileWizardPage(getWizardDisplayName(), getWizardDisplayName(), getDefaultNewFileName());
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		//Extract the project reference to add to
		
		//Extract resource from selection
		StructuredSelection sel = (StructuredSelection)selection;
		activeProject = null;
		//NOTE:  This caused issues earlier, as the sel return was treated as a workspace, instead of a project
		//When it is a valid FIRST project, the selection is always a JavaProject
		if(sel.getFirstElement() instanceof IProject){
			activeProject = ((IProject)sel.getFirstElement());
		}else if(sel.getFirstElement() instanceof IResource){
			activeProject = ((IResource)sel.getFirstElement()).getProject();
		}else{
			return;
		}
	}

	@Override
	public boolean performFinish() {
		copyTemplateFiles();
		
		try{activeProject.refreshLocal(IResource.DEPTH_INFINITE, null);}catch(Exception e){}
		
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		addPage(dataPage);
	}
	
	/** @return The desired name input by the user in the wizard*/
	protected String getDesiredName() {
		return dataPage.getDesiredName();
	}
	
	/**
	 * Copies the template files to temporary files, replacing strings in the
	 * process. The temporary files are then opened as input streams to be
	 * written into the workspace
	 */
	protected void copyTemplateFiles(){
		boolean commandsExisted;
		
		
		if(activeProject == null){
			//Print error message that project could not be resolved
			FRCDialog.createErrorDialog("File Creation Error", 
					new Status(Status.ERROR, FRCCppUserInterfacePlugin.PLUGIN_ID, "Target project could not be resolved"));
			return;
		}
		
		//Get reference to commands directory, and create it if it doesn't exist
		IFolder destFolder = activeProject.getFolder(getDestinationFolderName());;
		
		if((commandsExisted = destFolder.exists()) != true){
			try {destFolder.create(false, false, null);} catch (Exception e) {
				//Print error message that Folder could not be created
				FRCDialog.createErrorDialog("File Creation Error", 
						new Status(Status.ERROR, FRCCppUserInterfacePlugin.PLUGIN_ID, "Folder " + getDestinationFolderName() + " could not be created"));
				
				return;
			}
		}
	
		IFile headerFile;
		
		if((headerFile = createProjectFile(destFolder, getTemplateHeaderFilePath(), getDesiredName() + ".h")) == null){
			if(!commandsExisted){
				//Undo parts of process that were started
				try{destFolder.delete(true, null);}catch(Exception e){}
			}
			
			return;	
		}
		
		if(createProjectFile(destFolder, getTemplateSourceFilePath(), getDesiredName() + ".cpp") == null){
			try{headerFile.delete(true, null);}catch(Exception e){}
			
			if(!commandsExisted){
				//Undo parts of process that were started
				try{destFolder.delete(true, null);}catch(Exception e){}
			}
			
			return;	
		}
		
	}
	
	/**
	 * Creates an individual project file from a template
	 * @param destinationFolder The folder the new file will be in
	 * @param templatePath The path to the template to copy from
	 * @param newFileName The name of the file to be created
	 * @return null if the operation failed, or a handle to the successfully created file
	 */
	protected IFile createProjectFile(IFolder destinationFolder, String templatePath, String newFileName){
		try {
			//Create Temp file
			File newFile = new File(templatePath + ".temp");
			
			if(newFile.exists()){newFile.delete();}
			
			newFile.createNewFile();
			
			BufferedReader in = new BufferedReader(new FileReader(templatePath));
			PrintWriter out = new PrintWriter(new FileWriter(newFile));
			
			String temp;
			while (in.ready()) {
				temp = in.readLine();
				//Replace all occurances of class name
				if (temp.indexOf(getTemplateClassName()) != -1) {
					temp = StringUtil.replaceDelimitedString(temp, getTemplateClassName(), dataPage.getDesiredName());
				}

				//Replace all occurances of header file definition
				if (temp.indexOf(getTemplateHeaderDef()) != -1) {
					temp = StringUtil.replaceDelimitedString(temp, getTemplateHeaderDef(), dataPage.getDesiredName().toUpperCase() + "_H");
				}
				
				//Replace all occurances of ExampleAuthor
				if(temp.indexOf("ExampleAuthor") != -1){
					temp = StringUtil.replaceDelimitedString(temp, "ExampleAuthor", System.getenv("username"));
				}

				out.println(temp);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			//print error message
			FRCDialog.createErrorDialog("File Creation Error", 
					new Status(Status.ERROR, FRCCppUserInterfacePlugin.PLUGIN_ID, "Error when copying from template to " + newFileName));
			
			return null;
		}
		
		//Copy into workspace file
		IFile workspaceFile = destinationFolder.getFile(newFileName);
		
		if(workspaceFile.exists()){
			FRCDialog.createErrorDialog("File Creation Error", 
					new Status(Status.ERROR, FRCCppUserInterfacePlugin.PLUGIN_ID, "File " + newFileName + " already exists"));
			
			return null;
		}
		
		
		
		try{
			InputStream input = new FileInputStream(templatePath + ".temp");
			
			workspaceFile.create(input, false, null);
			input.close();
		}catch(Exception e){
			//Output error message
			FRCDialog.createErrorDialog("File Creation Error", 
					new Status(Status.ERROR, FRCCppUserInterfacePlugin.PLUGIN_ID, "Error creating file " + newFileName));
			
			//delete temp file
			File tempFile = new File(templatePath + ".temp");
			if(tempFile.exists()){tempFile.delete();}
			
			return null;
		}
		
		//Delete temp file
		File tempFile = new File(templatePath + ".temp");
		if(tempFile.exists()){tempFile.delete();}
		
		return workspaceFile;
	}
	
	/** @return The path to the root directory where templates are stored */
	protected String getTemplateDirectory(){
		try{
			return FileLocator.getBundleFile(FRCCppUserInterfacePlugin.getDefault().getBundle()).getAbsolutePath() + File.separator + "templates";
		}catch(Exception e){return null;}
	}

}
