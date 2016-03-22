package edu.wpi.first.javadev.projects.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

/**
 * Participant that hooks into the eclipse rename refactoring action and adds
 * an additional operation, which replaces the manifest file with the new name,
 * for either the package or main robot class
 * 
 * @author Ryan O'Meara
 */
public class ManifestRenameParticipant extends RenameParticipant{
	private String m_oldName;
	private String m_newName;
	private IJavaElement m_renameElement;
	
	public ManifestRenameParticipant(){
		m_oldName = null;
		m_newName = null;
		m_renameElement = null;
	}
	
	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm,
			CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException,
			OperationCanceledException {
		return new manifestRenameChange(m_renameElement, m_oldName, m_newName);
	}

	@Override
	public String getName() {
		return "FRC Manifest Rename Participant";
	}

	@Override
	protected boolean initialize(Object element) {
		//Check for changed element being java file or package
		if(element instanceof IPackageFragment){
			m_renameElement = (IPackageFragment)element;
			m_oldName = ((IPackageFragment) element).getElementName();
		}else if(element instanceof IType){
			m_renameElement = (IType)element;
			m_oldName = ((IType)element).getElementName(); 
		}
		
		m_newName = getArguments().getNewName();
		return true;
	}
	
	
	private class manifestRenameChange extends Change{
		private IJavaElement mi_workingElement;
		private String mi_oldName;
		private String mi_newName;
		private RefactoringStatus stat;

		public manifestRenameChange(IJavaElement renameElement, String oldName, String newName){
			super();
			mi_workingElement = renameElement;
			mi_oldName = oldName;
			mi_newName = newName;
			stat = new RefactoringStatus();
		}

		@Override
		public Object getModifiedElement() {return null;}

		@Override
		public String getName() {return "Manifest File Update";}

		@Override
		public void initializeValidationData(IProgressMonitor pm) {}

		@Override
		public RefactoringStatus isValid(IProgressMonitor pm)
				throws CoreException, OperationCanceledException {
			return stat;
		}

		@Override
		public Change perform(IProgressMonitor pm) throws CoreException {
			ManifestRename.renameInManifest(mi_workingElement, 
					mi_oldName, 
					mi_newName);
			
			stat.addFatalError("Refactoring already complete for this " +
					"change instance");
			return null;
		}	
	}

}
