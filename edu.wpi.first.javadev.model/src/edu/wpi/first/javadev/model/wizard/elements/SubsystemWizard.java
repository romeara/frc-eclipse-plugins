package edu.wpi.first.javadev.model.wizard.elements;

import edu.wpi.first.javadev.model.wizard.NewFRCClassWizard;

public class SubsystemWizard extends NewFRCClassWizard{

	@Override
	protected String[] getInterfaceQualifiedNames() {
		return new String[]{};
	}

	@Override
	protected String getSuperClassQualifiedName() {
		return "edu.wpi.first.wpilibj.command.Subsystem";
	}

	@Override
	protected String getWizardName() {
		return "edu.wpi.first.javadev.model.wizard.elements.SubsystemWizard";
	}

	@Override
	protected String[] getPreferredPackageEndings() {
		return new String[]{"subsystem", "subsystems"};
	}

	@Override
	protected void postProcessing() {}
}
