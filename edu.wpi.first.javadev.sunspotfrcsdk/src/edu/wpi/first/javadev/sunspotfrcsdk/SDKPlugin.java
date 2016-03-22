package edu.wpi.first.javadev.sunspotfrcsdk;

import java.util.ArrayList;

import org.eclipse.core.internal.resources.Resource;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.ui.actions.RefreshAction;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.wpi.first.codedev.output.FRCConsole;
import edu.wpi.first.javadev.sunspotfrcsdk.filesystem.SDKFileLocations;
import edu.wpi.first.javadev.sunspotfrcsdk.internal.sdksetup.SDKSetup;
import edu.wpi.first.javadev.sunspotfrcsdk.listener.ISDKInstallListener;

/**
 * The activator class controls the plug-in life cycle.  On startup, checks if 
 * the archived SDK is newer than the installed
 */
@SuppressWarnings("restriction")
public class SDKPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.wpi.first.javadev.sunspotfrcsdk"; //$NON-NLS-1$

	public static final String CONSOLE_NAME = "FRC Output Console";
	
	// The shared instance
	private static SDKPlugin plugin;
	
	private static Thread setupSDKThread;
	
	private ArrayList<ISDKInstallListener> listeners;
	
	public SDKPlugin() {
		listeners = new ArrayList<ISDKInstallListener>();
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		setupSDKThread = null;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * @return the shared instance
	 */
	public static SDKPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Thread to extract and install SDK
	 * 
	 * @author Ryan O'Meara
	 */
	protected class SetupSDKThread extends Thread{
		public void run(){
			setupSDK();
		}
	}
	
	private void setupSDK(){
		if(SDKSetup.needsExtraction()){
			FRCConsole.writeToConsole("Extracting SDK");
			SDKSetup.extractSDK();
			FRCConsole.writeToConsole("SDK Extracted");
		}
			
		FRCConsole.writeToConsole("Installing SDK");
		ILaunch launch = SDKSetup.installSDK();
		FRCConsole.writeToConsole("SDK Installed");
		
		while(!launch.isTerminated()){}
		
		FRCConsole.writeToConsole("Finishing Install");
		finishInstall();
		FRCConsole.writeToConsole("Process Finished");
	}
	
	/**
	 * Updates and installs the SDK.  If the SDK has not been extracted, it is
	 * extracted, and the an install is run.  If the SDK is already up to date,
	 * no action is taken
	 * @param waitComplete true if the caller wishes to wait for the operation
	 * to complete before continuing
	 */
	public void updateSDK(boolean waitComplete, boolean seperateThread){
		System.out.println("Input: " + waitComplete + ", " + seperateThread);
		if(seperateThread&&((setupSDKThread == null)||(setupSDKThread.getState() == Thread.State.TERMINATED))){
			System.out.println("New thread");
			setupSDKThread = new SetupSDKThread();
		}
		
		if(((setupSDKThread == null)||(!setupSDKThread.isAlive()))&&SDKSetup.needsUpdate()){
			FRCConsole.writeToConsole("Updating SDK");
			if(seperateThread){
				System.out.println("Thread");
				setupSDKThread.start();
			}else{
				System.out.println("Standard");
				setupSDK();
			}
			
			if(waitComplete&&seperateThread){Thread.yield();}
			
		}else if((setupSDKThread != null)&&setupSDKThread.isAlive()&&waitComplete){
			System.out.println("Waiting");
			while(setupSDKThread.isAlive()){
				try{
					Thread.yield();
					setupSDKThread.join();
				}catch(Exception e){e.printStackTrace();}
			}
			
			System.out.println("Done Waiting");
			System.out.println("Calling again");
			updateSDK(waitComplete,seperateThread);
		}
	}
	
	public void addInstallListener(ISDKInstallListener add){
		listeners.add(add);
	}
	
	public void removeInstallListener(ISDKInstallListener remove){
		listeners.remove(remove);
	}
	
	private void finishInstall(){
		//Update classpath
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		
		for(IProject proj : projects){
			IJavaProject jProj = JavaCore.create(proj);
			
			if(jProj != null){
				try{
					IClasspathEntry[] entries = jProj.getRawClasspath();
					ArrayList<IClasspathEntry> dynamicEntries = new ArrayList<IClasspathEntry>();
					for(IClasspathEntry entry : entries){
						if(entry.getPath().toString()
								.indexOf("edu.wpi.first.javadev.sunspotfrcsdk") == -1){
							dynamicEntries.add(entry);
						}
					}
					
					dynamicEntries.add(JavaCore.newLibraryEntry(
							new Path(SDKFileLocations.getWPILibJClasspath()), 
							null, 
							null));
					
					dynamicEntries.add(JavaCore.newLibraryEntry(new Path(SDKFileLocations.getSDKClasspath()), 
							null, 
							null));
					
					jProj.setRawClasspath(dynamicEntries.toArray(new IClasspathEntry[dynamicEntries.size()]), null);
				}catch(Exception e){}
			}
		}
		
		//Refresh workspace
		for(IProject proj : projects){
			try{
				proj.build(IncrementalProjectBuilder.FULL_BUILD, null);
				proj.clearHistory(null);
				proj.refreshLocal(IResource.DEPTH_INFINITE, null);
			}catch(Exception e){}
		}
		
		try{
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(Resource.DEPTH_INFINITE, null);
		}catch(Exception e){}
		
		Display.getDefault().syncExec(new Runnable(){
			public void run(){
				RefreshAction refAction = new RefreshAction(PackageExplorerPart.getFromActivePerspective().getSite());
				refAction.run();
			}
		});
		
		Display.getDefault().asyncExec(new Runnable(){
			public void run(){
				//Notify Listeners
				for(ISDKInstallListener current : listeners){
					current.installComplete();
				}
			}
		});
	}
}
