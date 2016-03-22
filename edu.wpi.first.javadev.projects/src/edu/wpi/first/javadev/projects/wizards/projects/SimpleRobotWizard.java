package edu.wpi.first.javadev.projects.wizards.projects;

import java.io.File;

import edu.wpi.first.javadev.projects.wizards.util.PackageFilesPair;

/**
 * Wizard to create a project using the SimpleRobot template
 * 
 * @author Ryan O'Meara
 */
public class SimpleRobotWizard extends BaseProjectTemplateWizard {

	@Override
	protected String getDefaultClassName() {
		return "ASimpleJavaBot";
	}

	@Override
	protected String getSourceDirectoryName() {
		return getRootPathFile() + File.separator + "SimpleRobotTemplate";
	}

	@Override
	protected String getMainClassName() {
		return "SimpleRobotTemplate";
	}

	@Override
	protected String getMainPackageName() {
		return "edu.wpi.first.wpilibj.templates";
	}

	@Override
	protected PackageFilesPair[] getStartingPackageFilePairs() {
		return new PackageFilesPair[]{new PackageFilesPair("edu.wpi.first.wpilibj.templates",
				new String[]{"SimpleRobotTemplate"})};
	}

}
