package edu.wpi.first.javadev.model;

import java.util.ArrayList;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.wpi.first.javadev.model.event.IWizardFinishedEventListener;
import edu.wpi.first.javadev.model.event.WizardFinishedEvent;

/**
 * The activator class controls the plug-in life cycle
 */
public class ModelPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "edu.wpi.first.javadev.model"; //$NON-NLS-1$

	// The shared instance
	private static ModelPlugin plugin;
	
	private ArrayList<IWizardFinishedEventListener> listeners;
	
	/**
	 * The constructor
	 */
	public ModelPlugin() {
		listeners = new ArrayList<IWizardFinishedEventListener>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	public boolean addWizardListener(IWizardFinishedEventListener listener){
		return listeners.add(listener);
	}
	
	public boolean removeWizardListener(IWizardFinishedEventListener listener){
		return listeners.remove(listener);
	}
	
	public void notifyListeners(WizardFinishedEvent event){
		for(IWizardFinishedEventListener current : listeners){current.receiveEvent(event);}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static ModelPlugin getDefault() {
		return plugin;
	}

}
