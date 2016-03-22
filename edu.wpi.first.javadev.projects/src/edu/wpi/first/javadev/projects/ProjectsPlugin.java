package edu.wpi.first.javadev.projects;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.wpi.first.javadev.sunspotfrcsdk.SDKPlugin;

/**
 * The activator class controls the plug-in life cycle
 */
public class ProjectsPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.wpi.first.javadev.projects"; //$NON-NLS-1$

	// The shared instance
	private static ProjectsPlugin plugin;
	
	/**
	 * The constructor
	 */
	public ProjectsPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		SDKPlugin.getDefault().updateSDK(false, true);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ProjectsPlugin getDefault() {
		return plugin;
	}

}
