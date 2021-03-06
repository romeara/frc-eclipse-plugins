package edu.wpi.first.javadev.model.wizard.elements;

import edu.wpi.first.javadev.model.wizard.NewFRCClassWizard;

public class CommandWizard extends NewFRCClassWizard{

	@Override
	protected String[] getInterfaceQualifiedNames() {
		return new String[]{};
	}

	@Override
	protected String getSuperClassQualifiedName() {
		return "edu.wpi.first.wpilibj.command.Command";
	}

	@Override
	protected String getWizardName() {
		return "edu.wpi.first.javadev.model.wizard.elements.CommandWizard";
	}

	@Override
	protected String[] getPreferredPackageEndings() {
		return new String[]{"command", "commands"};
	}

	@Override
	protected void postProcessing() {}

}
