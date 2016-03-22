package edu.wpi.first.javadev.sunspotfrcsdk.listener;

/**
 * Implemented by clients who wish to be notified when an SDK install
 * operation completes
 * 
 * @author Ryan O'Meara
 */
public interface ISDKInstallListener {
	
	/** Indicates an install operation has finished */
	public abstract void installComplete();
}
