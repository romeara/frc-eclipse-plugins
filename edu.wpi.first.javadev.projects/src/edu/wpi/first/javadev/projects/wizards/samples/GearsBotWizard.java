package edu.wpi.first.javadev.projects.wizards.samples;

import java.io.File;

import edu.wpi.first.javadev.projects.wizards.util.PackageFilesPair;

/**
 * Wizard to create the gears bot sample project
 * @author Ryan O'Meara
 */
public class GearsBotWizard extends BaseSampleProjectWizard {

	@Override
	protected String getSourceDirectoryName() {
		return getRootPathFile() + File.separator + "GearsBot";
	}

	@Override
	protected PackageFilesPair[] getStartingPackageFilePairs() {
		PackageFilesPair pairs[] = new PackageFilesPair[3];
		
		pairs[0] = new PackageFilesPair("edu.wpi.first.wpilibj.templates",
				new String[]{"GearsBot","OI","RobotMap"});
		pairs[1] = new PackageFilesPair("edu.wpi.first.wpilibj.templates.commands",
				new String[]{"ClawDoNothing", "CloseClaw", "CommandBase", "DriveToDistance", "DriveWithJoysticks", 
				"Grab", "OpenClaw", "PlaceSoda", "PrepareToGrab", "SetElevatorSetpoint", "SetWristSetpoint",
				"SodaDelivery", "Stow"});
		pairs[2] = new PackageFilesPair("edu.wpi.first.wpilibj.templates.subsystems",
				new String[]{"Claw", "DriveTrain", "Elevator", "Wrist"});
		
		return pairs;
	}

}
