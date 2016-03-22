package edu.wpi.first.javadev.launching.shortcuts;

/**
 * Launch shortcut that compiles and downloads to the cRIO
 * @author Ryan O'Meara
 */
public class LaunchShortcutDeploy extends BaseLaunchShortcut {
	@Override
	public String getLaunchType() {
		return BaseLaunchShortcut.DEPLOY_TYPE;
	}
}
