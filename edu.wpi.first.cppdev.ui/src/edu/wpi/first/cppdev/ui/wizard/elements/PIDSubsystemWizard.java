package edu.wpi.first.cppdev.ui.wizard.elements;

import java.io.File;


public class PIDSubsystemWizard extends BaseFRCCppElementWizard{

	@Override
	protected String getTemplateHeaderFilePath() {
		String templateDirectory = getTemplateDirectory();
		
		if(templateDirectory == null){return null;}
		
		return templateDirectory + File.separator + "ExamplePIDSubsystem.h";
	}

	@Override
	protected String getTemplateSourceFilePath() {
		String templateDirectory = getTemplateDirectory();
		
		if(templateDirectory == null){return null;}
		
		return templateDirectory + File.separator + "ExamplePIDSubsystem.cpp";
	}

	@Override
	protected String getDestinationFolderName() {
		return "Subsystems";
	}

	@Override
	protected String getWizardDisplayName() {
		return "PID Subsystem";
	}

	@Override
	protected String getTemplateClassName() {
		return "ExamplePIDSubsystem";
	}

	@Override
	protected String getTemplateHeaderDef() {
		return "EXAMPLE_PID_SUBSYSTEM_H";
	}

	@Override
	protected String getDefaultNewFileName() {
		return "NewPIDSubsystem";
	}


}
