package edu.wpi.first.javadev.projects.wizards.samples;

import java.io.File;

import edu.wpi.first.javadev.projects.wizards.util.PackageFilesPair;

/**
 * Wizard to construct a Default code sample project
 * @author Ryan O'Meara
 */
public class DefaultCodeWizard extends BaseSampleProjectWizard {

	@Override
	protected String getSourceDirectoryName() {
		return getRootPathFile() + File.separator + "BuiltInDefaultCode";
	}

	@Override
	protected PackageFilesPair[] getStartingPackageFilePairs() {
		return new PackageFilesPair[]{new PackageFilesPair("edu.wpi.first.wpilibj.defaultCode",
				new String[]{"DefaultRobot"})};
	}

}
