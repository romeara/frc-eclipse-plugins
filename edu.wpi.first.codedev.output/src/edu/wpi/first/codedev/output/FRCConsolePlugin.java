package edu.wpi.first.codedev.output;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class FRCConsolePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.wpi.first.codedev.output"; //$NON-NLS-1$

	// The shared instance
	private static FRCConsolePlugin plugin;
	
	public FRCConsolePlugin() {}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
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
	public static FRCConsolePlugin getDefault() {return plugin;}

}
