package edu.wpi.first.javadev.model.wizard.elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.core.IJavaElement;

import edu.wpi.first.javadev.model.ModelPlugin;
import edu.wpi.first.javadev.model.wizard.NewFRCClassWizard;

public class PIDSubsystemWizard extends NewFRCClassWizard{

	@Override
	protected String[] getInterfaceQualifiedNames() {
		return new String[]{};
	}

	@Override
	protected String getSuperClassQualifiedName() {
		return "edu.wpi.first.wpilibj.command.PIDSubsystem";
	}

	@Override
	protected String getWizardName() {
		return "edu.wpi.first.javadev.model.wizard.elements.PIDSubsystemWizard";
	}

	@Override
	protected String[] getPreferredPackageEndings() {
		return new String[]{"subsystem", "subsystems"};
	}

	@Override
	protected void postProcessing() {
		IJavaElement created = getCreated();
		File tFile = created.getPath().toFile();
		
		try {
			String tempFile = FileLocator.getBundleFile(ModelPlugin.getDefault().getBundle()).getAbsolutePath() + File.separator + "temp.txt";
			
			System.out.println("tfile: " + tFile.toString());
			System.out.println("Project: " + created.getJavaProject().getProject().getLocation().toFile().toString() + File.separator);
			
			File typeFile = new File(created.getJavaProject().getProject().getLocation().toFile().toString() + File.separator);
			typeFile = typeFile.getParentFile();
			typeFile = new File(typeFile.toString() + tFile.toString());
			
			System.out.println("typefile: " + typeFile.toString());
			
			BufferedReader in = new BufferedReader(new FileReader(typeFile));
			PrintWriter out = new PrintWriter(new FileWriter(tempFile));
			String temp;
			while (in.ready()) {
				temp = in.readLine();
				if (temp.indexOf("public class") != -1) {
					out.println(temp);
					
					//Insert constructor and associated variables
					out.println("");
					out.println("\tprivate static final double Kp = 0.0;");
					out.println("\tprivate static final double Ki = 0.0;");
					out.println("\tprivate static final double Kd = 0.0;");
					out.println();
					out.println("\tprivate " + created.getElementName() + "() {");
					out.println("\t\tsuper(\"newPIDSubsystem\", Kp, Ki, Kd);");
					out.println();
					out.println("\t\t// TODO Auto-generated constructor stub");
					out.println("\t\t// Use these to get going:");
					out.println("\t\t// setSetpoint() -  Sets where the PID controller should move the system to");
					out.println("\t\t// enable() - Enables the PID controller.");
					out.println("\t}");
				}else{
					out.println(temp);
				}
			}
			
			out.close();
			in.close();
			
			typeFile.delete();
			
			in = new BufferedReader(new FileReader(tempFile));
			out = new PrintWriter(new FileWriter(typeFile.getAbsolutePath()));
			while (in.ready()) {
				out.println(in.readLine());
			}
			
			
			out.close();
			in.close();
			(new File(tempFile)).delete();
			created.getUnderlyingResource().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
