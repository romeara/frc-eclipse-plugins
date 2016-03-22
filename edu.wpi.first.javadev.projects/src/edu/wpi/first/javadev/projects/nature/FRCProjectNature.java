package edu.wpi.first.javadev.projects.nature;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import edu.wpi.first.javadev.projects.ProjectsPlugin;
import edu.wpi.first.javadev.projects.builder.AntIncrementalBuilder;
import edu.wpi.first.javadev.sunspotfrcsdk.filesystem.SDKFileLocations;

/**
 * Project nature which configures any project which is given it to be an 
 * FRC Project, and designates it so FRC options will be enabled to act on
 * it
 * 
 * @author Ryan O'Meara
 */
public class FRCProjectNature implements IProjectNature{
	
	public static final String FRC_PROJECT_NATURE = 
		"edu.wpi.first.javadev.projects.nature.FRCProjectNature";
	
	
	private IProject internalProject;
	
	/**
	 * IStatus representing a failed configuration attempt
	 * 
	 * @author Ryan O'Meara
	 */
	private class FRCProjectFailedStatus implements IStatus{
		String message;
		
		public FRCProjectFailedStatus(String message){
			this.message = message;
		}
		
		@Override
		public IStatus[] getChildren() {return null;}

		@Override
		public int getCode() {return 0;}

		@Override
		public Throwable getException() {return null;}

		@Override
		public String getMessage() {return message;}

		@Override
		public String getPlugin() {return ProjectsPlugin.PLUGIN_ID;}

		@Override
		public int getSeverity() {return ERROR;}

		@Override
		public boolean isMultiStatus() {return false;}

		@Override
		public boolean isOK() {return false;}

		@Override
		public boolean matches(int severityMask) {
			if((severityMask & ERROR) == ERROR){return true;}
			return false;
		}	
	}
	
	public FRCProjectNature(){
		internalProject = null;
	}
	
	@Override
	public void configure() throws CoreException {
		if(internalProject == null){
			throw new CoreException(
					new FRCProjectFailedStatus("No project set"));
		}
		
		//Add java nature to project
		IProjectDescription description = internalProject.getDescription();
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = JavaCore.NATURE_ID;
		description.setNatureIds(newNatures);
		internalProject.setDescription(description, null);
		
		//Create input output folders
		internalProject.getFolder("src").create(false, false, null);
		internalProject.getFolder("build").create(IResource.DERIVED, false, null);
		
		IJavaProject javaProject = JavaCore.create(internalProject);
		javaProject.setRawClasspath(new IClasspathEntry[]{
				JavaCore.newSourceEntry(new Path(
				internalProject.getFolder("src")
				.getFullPath().toString())),
				JavaCore.newLibraryEntry(
						new Path(SDKFileLocations.getWPILibJClasspath()), 
						null, 
						null),
				JavaCore.newLibraryEntry(new Path(SDKFileLocations.getSDKClasspath()), 
						null, 
						null)
		}
		, null);
		javaProject.setOutputLocation(
				new Path(internalProject.getFolder("build")
						.getFullPath().toString()), 
				null);
		addBuilder(internalProject, AntIncrementalBuilder.FRC_ANT_BUILDER_ID);
	}

	@Override
	public void deconfigure() throws CoreException {
		if(internalProject == null){
			throw new CoreException(
					new FRCProjectFailedStatus("No project set"));
		}
	}

	@Override
	public IProject getProject() {
		return internalProject;
	}

	@Override
	public void setProject(IProject project) {
		internalProject = project;
	}
	
	private void addBuilder(IProject project, String id) {
		try{
	      IProjectDescription desc = project.getDescription();
	      ICommand[] commands = desc.getBuildSpec();
	      for (int i = 0; i < commands.length; ++i)
	         if (commands[i].getBuilderName().equals(id))
	            return;
	      //add builder to project
	      ICommand command = desc.newCommand();
	      command.setBuilderName(id);
	      ICommand[] nc = new ICommand[commands.length + 1];
	      // Add it before other builders.
	      System.arraycopy(commands, 0, nc, 1, commands.length);
	      nc[0] = command;
	      desc.setBuildSpec(nc);
	      project.setDescription(desc, null);
		}catch(Exception e){}
	   }

}
