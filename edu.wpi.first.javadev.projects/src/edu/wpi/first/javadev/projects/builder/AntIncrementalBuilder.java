package edu.wpi.first.javadev.projects.builder;

import java.io.File;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchManager;

import edu.wpi.first.javadev.sunspotfrcsdk.SDKPlugin;
import edu.wpi.first.javadev.sunspotfrcsdk.ant.AntLauncher;

public class AntIncrementalBuilder extends IncrementalProjectBuilder {
	public static final String FRC_ANT_BUILDER_ID = 
		"edu.wpi.first.javadev.projects.antbuilder";
	public AntIncrementalBuilder() {
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		System.out.println("AntIncrementalBuilder.build");
		SDKPlugin.getDefault().updateSDK(true, false);
		
		AntLauncher.runAntFile(
				new File (getProject().getLocation().toOSString() 
						+ File.separator 
						+ "build.xml"), 
						"jar-app", 
						null, 
						ILaunchManager.RUN_MODE);
		
		forgetLastBuiltState();
		return null;
	}

}
