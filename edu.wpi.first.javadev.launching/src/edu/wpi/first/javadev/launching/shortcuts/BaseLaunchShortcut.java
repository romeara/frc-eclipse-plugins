package edu.wpi.first.javadev.launching.shortcuts;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Vector;

import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

import edu.wpi.first.javadev.sunspotfrcsdk.SDKPlugin;
import edu.wpi.first.javadev.sunspotfrcsdk.ant.AntLauncher;


/**
 * Launch shortcut base functionality, common to both build and deploy/debug
 * Retrieves the project the operation is being called on, and runs the correct
 * ant targets based on polymorphically determined data values
 * @author Ryan O'Meara
 */
@SuppressWarnings("restriction")
public abstract class BaseLaunchShortcut implements ILaunchShortcut {
	//Class constants - used to delineate types for launch shortcuts
	public static final String BUILD_TYPE = "edu.wpi.first.javadev.build";
	public static final String DEPLOY_TYPE = "edu.wpi.first.javadev.deploy";
	private static final String ANT_SERVER_THREAD_NAME = "Ant Build Server Connection";
	
	private static ILaunch lastDeploy = null;
	
	/**
	 * Returns the launch type of the shortcut that was used, one of the constants
	 * defined in BaseLaunchShortcut
	 * @return Launch shortcut type
	 */
	public abstract String getLaunchType();
	
	@Override
	public void launch(ISelection selection, String mode) {
		//Extract resource from selection
		StructuredSelection sel = (StructuredSelection)selection;
		IProject activeProject = null;
		//NOTE:  This caused issues earlier, as the sel return was treated as a workspace, instead of a project
		//When it is a valid FIRST project, the selection is always a JavaProject
		if(sel.getFirstElement() instanceof IJavaProject){
			activeProject = ((IJavaProject)sel.getFirstElement()).getProject();
		}else if(sel.getFirstElement() instanceof IJavaElement){
			activeProject = ((IJavaElement)sel.getFirstElement()).getJavaProject().getProject();
		}else{
			return;
		}
        
        //Run config using project found in extracted resource, with indicated mode
        runConfig(activeProject, mode);
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		//Extract resource from editor
		if(editor  != null){
		    IFileEditorInput input = (IFileEditorInput)editor.getEditorInput();
		    IFile file = input.getFile();
		    IProject activeProject = file.getProject();
		    
		    //If editor existed, run config using extracted resource in indicated mode
		    runConfig(activeProject, mode);
		}else{
			System.err.println("editor was null");
		}

	}
	
	/**
	 * Runs the ant script using the correct target for the indicated mode (deploy to cRIO or just compile)
	 * @param activeProj The project that the script will be run on/from
	 * @param mode The mode it will be run in (ILaunchManager.RUN_MODE or ILaunchManager.DEBUG_MODE)
	 */
	public void runConfig(IProject activeProj, String mode){
		SDKPlugin.getDefault().updateSDK(true, false);
		
		String targets = "jar-app";
		    
		if(mode.equals(ILaunchManager.RUN_MODE)){
			if(getLaunchType().equals(DEPLOY_TYPE)){
				targets = "deploy,run";
			}else if(getLaunchType().equals(BUILD_TYPE)){
				targets = "jar-app";
			}
		//Debug mode options
		}else if((mode.equals(ILaunchManager.DEBUG_MODE))&&(getLaunchType().equals(DEPLOY_TYPE))){
			targets = "deploy,debug-run";
			try{
				PlatformUI.getWorkbench().showPerspective(IDebugUIConstants.ID_DEBUG_PERSPECTIVE, 
					PlatformUI.getWorkbench().getActiveWorkbenchWindow());
			}catch(Exception e){}
		}
		
		if((lastDeploy != null)&&(!lastDeploy.isTerminated())){
			System.out.println("Last deploy running");
			//Find the server connection thread and kill it
			Vector<ThreadGroup> threadGroups = new Vector<ThreadGroup>();
	        ThreadGroup root = Thread.currentThread().getThreadGroup().getParent();
	        while (root.getParent() != null) {root = root.getParent();}
	        threadGroups.add(root);
	        ThreadGroup threadGroup = threadGroups.remove(0);
	        int numThreads = threadGroup.activeCount();
	        Thread[] threads = new Thread[numThreads*100];
            numThreads = threadGroup.enumerate(threads, true);
            
            for(Thread current: threads){
            	if(current != null){
            		if(current.getName().equals(ANT_SERVER_THREAD_NAME)){
            			try{
            				//Manually end thread and then try terminating launch
            				Method stopMethod = current.getClass().getMethod("stop");
            				stopMethod.invoke(current);
            				lastDeploy.terminate();
            				break;
            			}catch(Exception e){e.printStackTrace();}
            		}
            	}
            }
            
            System.out.println("Waiting");
            try{wait(1000);}catch(Exception e){}
               
		}
		
		System.out.println("Running ant file: " + activeProj.getLocation().toOSString() + File.separator + "build.xml");
		System.out.println("Targets: " + targets + ", Mode: " + mode);
		lastDeploy = AntLauncher.runAntFile(new File (activeProj.getLocation().toOSString() + File.separator + "build.xml"), targets, null, mode);
		
		if((mode.equals(ILaunchManager.DEBUG_MODE))&&(getLaunchType().equals(DEPLOY_TYPE))){
			//start the debug proxy
			try{wait(1000);}catch(Exception e){}
			AntLauncher.runAntFile(new File (activeProj.getLocation().toOSString() + File.separator + "build.xml"), "debug-proxy", null, mode, false);
		}
		
		try {
			activeProj.refreshLocal(Resource.DEPTH_INFINITE, null);
		} catch (Exception e) {}
	}
}

