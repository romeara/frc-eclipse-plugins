package edu.wpi.first.javadev.projects.wizards.projects;

import java.io.File;

import edu.wpi.first.javadev.projects.wizards.util.PackageFilesPair;

/**
 * Wizard for the Iterative robot template
 * 
 * @author Ryan O'Meara
 */
public class IterativeRobotWizard extends BaseProjectTemplateWizard {

	@Override
	protected String getDefaultClassName() {
		return "AnInterativeJavaBot";
	}

	@Override
	protected String getSourceDirectoryName() {
		return getRootPathFile() + File.separator + "IterativeRobotTemplate";
	}

	@Override
	protected String getMainClassName() {
		return "IterativeRobotTemplate";
	}

	@Override
	protected String getMainPackageName() {
		return "edu.wpi.first.wpilibj.templates";
	}

	@Override
	protected PackageFilesPair[] getStartingPackageFilePairs() {
		return new PackageFilesPair[]{new PackageFilesPair("edu.wpi.first.wpilibj.templates",
				new String[]{"IterativeRobotTemplate"})};
	}

}
