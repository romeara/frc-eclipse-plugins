package edu.wpi.first.javadev.projects.wizards.samples;

import java.io.File;

import edu.wpi.first.javadev.projects.wizards.util.PackageFilesPair;

/**
 * Wizard to create the encoder drive sample project
 * @author Ryan O'Meara
 */
public class EncoderDriveDemoWizard extends BaseSampleProjectWizard {

	@Override
	protected String getSourceDirectoryName() {
		return getRootPathFile() + File.separator + "EncoderDriveDemo";
	}

	@Override
	protected PackageFilesPair[] getStartingPackageFilePairs() {
		return new PackageFilesPair[]{new PackageFilesPair("edu.wpi.first.wpilibj.templates",
				new String[]{"EncoderDrive"})};
	}

}
