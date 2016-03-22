package edu.wpi.first.cppdev.ui.wizard.elements;

import java.io.File;


public class CommandWizard extends BaseFRCCppElementWizard {

	@Override
	protected String getTemplateHeaderFilePath() {
		String templateDirectory = getTemplateDirectory();
		
		if(templateDirectory == null){return null;}
		
		return templateDirectory + File.separator + "ExampleCommand.h";
	}

	@Override
	protected String getTemplateSourceFilePath() {
		String templateDirectory = getTemplateDirectory();
		
		if(templateDirectory == null){return null;}
		
		return templateDirectory + File.separator + "ExampleCommand.cpp";
	}

	@Override
	protected String getDestinationFolderName() {
		return "Commands";
	}

	@Override
	protected String getWizardDisplayName() {
		return "Command";
	}

	@Override
	protected String getTemplateClassName() {
		return "ExampleCommand";
	}

	@Override
	protected String getTemplateHeaderDef() {
		return "EXAMPLE_COMMAND_H";
	}

	@Override
	protected String getDefaultNewFileName() {
		return "NewCommand";
	}

	
}
