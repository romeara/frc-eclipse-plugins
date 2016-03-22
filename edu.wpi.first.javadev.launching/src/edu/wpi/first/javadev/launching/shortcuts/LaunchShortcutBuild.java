package edu.wpi.first.javadev.launching.shortcuts;

/**
 * Shortcut that compiles the project without deploying it to the cRIO
 * @author Ryan O'Meara
 */
public class LaunchShortcutBuild extends BaseLaunchShortcut {
	@Override
	public String getLaunchType() {
		return BaseLaunchShortcut.BUILD_TYPE;
	}
}
