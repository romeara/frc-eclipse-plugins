package edu.wpi.first.javadev.model.wizard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.wizards.NewClassCreationWizard;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import edu.wpi.first.javadev.model.ModelPlugin;
import edu.wpi.first.javadev.model.event.WizardFinishedEvent;

@SuppressWarnings("restriction")
public abstract class NewFRCClassWizard extends NewClassCreationWizard{
	IPackageFragment packageFrag;
	IPackageFragmentRoot packageFragRoot;
	
	public NewFRCClassWizard(){
		super(null, true);
		packageFrag = null;
		packageFragRoot = null;
	}
	
	protected abstract String[] getInterfaceQualifiedNames();
	
	protected abstract String getSuperClassQualifiedName();
	
	protected abstract String getWizardName();
	
	protected abstract String[] getPreferredPackageEndings();
	
	protected abstract void postProcessing();
	
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		super.init(workbench, selection);
		
		//Extract resource from selection
		StructuredSelection sel = (StructuredSelection)selection;
		IJavaElement extracted = null;
		
		if(sel.getFirstElement() == null){return;}
		
		if(sel.getFirstElement() instanceof IJavaElement){
			extracted = (IJavaElement)sel.getFirstElement();
		}else if(sel.getFirstElement() instanceof IResource){
			if(sel.getFirstElement() instanceof IFile){
				extracted = JavaCore.create(
						ResourcesPlugin.getWorkspace().getRoot().findMember(((IFile)sel.getFirstElement()).getParent().getName()));
			}else if(sel.getFirstElement() instanceof IFolder){
				extracted = JavaCore.create(
						ResourcesPlugin.getWorkspace().getRoot().findMember(((IFolder)sel.getFirstElement()).getName()));	
			}
			
			if(sel.getFirstElement() instanceof IProject){
				extracted = JavaCore.create((IResource)sel.getFirstElement());
			}
		}else{
			return;
		}
		
		if(extracted instanceof ICompilationUnit){
			extracted = (IPackageFragment)((ICompilationUnit)extracted).getParent();
		}
		
		if(extracted == null){
			return;
		}else if(extracted instanceof IPackageFragment){
			packageFrag = (IPackageFragment)extracted;
			packageFragRoot = (IPackageFragmentRoot)packageFrag.getParent();
		}else{
			if(extracted instanceof IJavaProject){
				try{extracted = ((IJavaProject)extracted).getAllPackageFragmentRoots()[0];}catch(Exception e){}
			}
			
			if(extracted instanceof IPackageFragmentRoot){
				packageFragRoot = (IPackageFragmentRoot)extracted;
				
				try{
					boolean endsInPreferred = false;
					
					for(IJavaElement current : packageFragRoot.getChildren()){
						for(String currentString : getPreferredPackageEndings()){
							endsInPreferred = endsInPreferred || current.getElementName().endsWith("." + currentString);
						}
						
						if(current instanceof IPackageFragment){
							if(packageFrag == null){
								packageFrag = (IPackageFragment)current;
							}else if(packageFrag.getElementName().length() == 0){
								packageFrag = (IPackageFragment)current;
							}else if((!packageFrag.containsJavaResources()) && ((IPackageFragment)current).containsJavaResources()){
								packageFrag = (IPackageFragment)current;
							}else if(endsInPreferred){
								packageFrag = (IPackageFragment)current;
								break;
							}else if(packageFrag.getElementName().length() < current.getElementName().length()){
								packageFrag = (IPackageFragment)current;
							}
						}
						
						endsInPreferred = false;
					}
				}catch(Exception e){}
			}else{
				return;
			}
		}
	}
	
	@Override 
	 public void addPages(){
		 super.addPages();
		 for(IWizardPage current : getPages()){
				if(current instanceof NewClassWizardPage){
					for(String currentInterface : getInterfaceQualifiedNames()){
						((NewClassWizardPage)current).addSuperInterface(currentInterface);
					}
					
					if(getSuperClassQualifiedName() != null){
						((NewClassWizardPage)current).setSuperClass(getSuperClassQualifiedName(), false);
					}
					
					((NewClassWizardPage)current).setMethodStubSelection(false, false, true, false);
					
					((NewClassWizardPage)current).enableCommentControl(true);
					((NewClassWizardPage)current).setAddComments(false, false);
					
					try {
						if(packageFragRoot != null){((NewClassWizardPage)current).setPackageFragmentRoot(packageFragRoot, true);}
						if(packageFrag != null){((NewClassWizardPage)current).setPackageFragment(packageFrag, true);}
					} catch (Exception e) {e.printStackTrace();}
				}
			}
	 }
	
	@Override
	public boolean performFinish(){
		boolean ret = super.performFinish();
		
		if(ret){
			removeAnnotations();
			postProcessing();	
		}
		
		try{getCreated().getUnderlyingResource().refreshLocal(IResource.DEPTH_INFINITE, null);}catch(Exception e){}
		
		ModelPlugin.getDefault().notifyListeners(new WizardFinishedEvent(ret, getWizardName(),getCreated()));
		return ret;
	}
	
	@Override
	public boolean performCancel(){
		boolean ret = super.performCancel();
		ModelPlugin.getDefault().notifyListeners(new WizardFinishedEvent(false, getWizardName(),getCreated()));
		return ret;
	}
	
	/** Returns the created IJavaElement */
	protected IJavaElement getCreated(){
		IType created = null;
		for(IWizardPage current : getPages()){
			if(current instanceof NewClassWizardPage){
				created = ((NewClassWizardPage)current).getCreatedType();
			}
		}
		
		return created;
	}
	
	@Override
	protected void selectAndReveal(IResource newResource) {
		//Does not call super, as the double call will cause an eclipse exception, and the wizard will not finish properly
		try{BasicNewResourceWizard.selectAndReveal(newResource, ModelPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow());}catch(Exception e){}
	}
	
	/**
	 * Remove Java annotations from the generated file - at time of writing, 
	 * version of Java Squawk used does not support them
	 */
	protected void removeAnnotations(){
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
				if (temp.indexOf("@") == -1) {
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
