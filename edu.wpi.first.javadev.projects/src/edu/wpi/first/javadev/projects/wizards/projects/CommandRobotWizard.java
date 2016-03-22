package edu.wpi.first.javadev.projects.wizards.projects;

import java.io.File;

import edu.wpi.first.javadev.projects.wizards.util.PackageFilesPair;

public class CommandRobotWizard extends BaseProjectTemplateWizard {

	@Override
	protected String getSourceDirectoryName() {
		return getRootPathFile() + File.separator + "CommandRobotTemplate";
	}

	@Override
	protected PackageFilesPair[] getStartingPackageFilePairs() {
		PackageFilesPair files[] = new PackageFilesPair[3];
		
		files[0] = new PackageFilesPair("edu.wpi.first.wpilibj.templates", 
				new String[]{"OI","RobotMap","CommandBasedRobotTemplate"});
		files[1] = new PackageFilesPair("edu.wpi.first.wpilibj.templates.commands", 
				new String[]{"CommandBase","ExampleCommand"});
		files[2] = new PackageFilesPair("edu.wpi.first.wpilibj.templates.subsystems", 
				new String[]{"ExampleSubsystem"});
		
		return files;
	}

	@Override
	protected String getDefaultClassName() {
		return "CommandBasedRobot";
	}

	@Override
	protected String getMainClassName() {
		return "CommandBasedRobotTemplate";
	}

	@Override
	protected String getMainPackageName() {
		return "edu.wpi.first.wpilibj.templates";
	}
}
