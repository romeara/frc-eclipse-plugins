package edu.wpi.first.cppdev.ui.wizard.elements;

import java.io.File;


public class SubsystemWizard extends BaseFRCCppElementWizard{

	@Override
	protected String getTemplateHeaderFilePath() {
		String templateDirectory = getTemplateDirectory();
		
		if(templateDirectory == null){return null;}
		
		return templateDirectory + File.separator + "ExampleSubsystem.h";
	}

	@Override
	protected String getTemplateSourceFilePath() {
		String templateDirectory = getTemplateDirectory();
		
		if(templateDirectory == null){return null;}
		
		return templateDirectory + File.separator + "ExampleSubsystem.cpp";
	}

	@Override
	protected String getDestinationFolderName() {
		return "Subsystems";
	}

	@Override
	protected String getWizardDisplayName() {
		return "Subsystem";
	}

	@Override
	protected String getTemplateClassName() {
		return "ExampleSubsystem";
	}

	@Override
	protected String getTemplateHeaderDef() {
		return "EXAMPLE_SUBSYSTEM_H";
	}

	@Override
	protected String getDefaultNewFileName() {
		return "NewSubsystem";
	}

}
