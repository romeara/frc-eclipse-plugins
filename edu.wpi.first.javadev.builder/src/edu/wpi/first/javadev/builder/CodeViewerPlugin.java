package edu.wpi.first.javadev.builder;

import java.io.File;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.wpi.first.javadev.builder.debug.DebugOutput;
import edu.wpi.first.javadev.builder.workspace.model.FRCModel;
import edu.wpi.first.javadev.sunspotfrcsdk.SDKPlugin;

/**
 * The main plug-in class that is used by eclipse.  Activates the plug-in for use in the
 * environment, contains resources shared by plug-in elements, and access to the workbench
 * @author Ryan O'Meara
 */
public class CodeViewerPlugin extends AbstractUIPlugin{
	//The shared instance of this plugin
	private static CodeViewerPlugin plugin;
	
	//Workspace model
	private static FRCModel workspaceModel = null;
	
	//The plug-in id, which MUST be changed if the plug-in name changes, otherwise
	//errors will happen
	public static final String PLUGIN_ID = "edu.wpi.first.javadev.builder";
	
	/**
	 * Constructs and initializes plug-in classes
	 */
	public CodeViewerPlugin() {
		DebugOutput.initialize();
	}
	
	/**
	 * Returns the first page of the workbench page set
	 * @return The first page in the array of pages, or throws exception if no pages found
	 * @throws Exception If no pages found - can be caused by workbench not being fully started
	 */
	public IWorkbenchPage getPage() throws Exception{
		IWorkbenchPage retPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		if(retPage != null){return retPage;}
		
		throw new Exception("No workbench page found: CodeViewerPlugin.getPage()");
	}
	
	/**
	 * Gets the java project associated with the active editor file
	 * @return The IJavaProject associated, or null if none
	 */
	public IJavaProject getActiveProject(){
		IWorkingCopyManager mgr = JavaUI.getWorkingCopyManager();
		IEditorInput edIn = null;
		
		try {
			edIn = getPage().getActiveEditor().getEditorInput();

			mgr.connect(edIn);
			ICompilationUnit unit = mgr.getWorkingCopy(edIn);
			
			if (unit != null) {
	            return unit.getJavaProject();
	        }else{
	        	System.out.println("getActiveProject:null");
	        }
		} catch (Exception e) {
			System.out.println("getActiveProject:exception");
			e.printStackTrace();
		} finally {
			mgr.disconnect(edIn);
		}
		
		return null;
	}
	
	/**
	 * Returns the shared instance of the plug-in
	 * @return the shared instance
	 */
	public static CodeViewerPlugin getDefault() {
		return plugin;
	}
	
	public static FRCModel getFRCModel(){
		if(workspaceModel == null){workspaceModel = new FRCModel();}
		return workspaceModel;
	}
	
	/**
	 * Returns an image descriptor for the given file name located under the 
	 * plug-in root/icons directory
	 * @param name Name of the file to find the descriptor for
	 * @return ImageDescritor of the given image file
	 */
	public static ImageDescriptor getImageDescriptor(String name) {
		return imageDescriptorFromPlugin(PLUGIN_ID, "icons" + File.separator + name);
	}
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		SDKPlugin.getDefault().updateSDK(false, true);
		
		if(workspaceModel == null){workspaceModel = new FRCModel();}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		workspaceModel.dispose();
		workspaceModel = null;
		super.stop(context);
	}
}
