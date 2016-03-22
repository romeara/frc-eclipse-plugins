package edu.wpi.first.cppdev.ui.wizard.elements;

import java.io.File;


public class CommandGroupWizard extends BaseFRCCppElementWizard{

	@Override
	protected String getTemplateHeaderFilePath() {
		String templateDirectory = getTemplateDirectory();
		
		if(templateDirectory == null){return null;}
		
		return templateDirectory + File.separator + "ExampleCommandGroup.h";
	}

	@Override
	protected String getTemplateSourceFilePath() {
		String templateDirectory = getTemplateDirectory();
		
		if(templateDirectory == null){return null;}
		
		return templateDirectory + File.separator + "ExampleCommandGroup.cpp";
	}

	@Override
	protected String getDestinationFolderName() {
		return "Commands";
	}

	@Override
	protected String getWizardDisplayName() {
		return "Command Group";
	}

	@Override
	protected String getTemplateClassName() {
		return "ExampleCommandGroup";
	}

	@Override
	protected String getTemplateHeaderDef() {
		return "EXAMPLE_COMMAND_GROUP_H";
	}

	@Override
	protected String getDefaultNewFileName() {
		return "NewCommandGroup";
	}


}
