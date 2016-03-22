package edu.wpi.first.javadev.builder.workspace.model.robot;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.ide.IDE;

import edu.wpi.first.javadev.builder.util.ModelBuilderUtil;
import edu.wpi.first.javadev.builder.workspace.model.FRCElement;
import edu.wpi.first.javadev.builder.workspace.model.ModelElementType;
import edu.wpi.first.javadev.builder.workspace.model.ParseConstants;
import edu.wpi.first.javadev.builder.workspace.model.event.FRCModelEvent;
import edu.wpi.first.javadev.builder.workspace.model.view.FRCVParent;

/**
 * Represents a state machine in the robot model.  Can contain transitions and
 * states
 * 
 * @author Ryan O'Meara and Joe Grinstead
 */
public class FRCRStateMachine extends FRCRParent {
	private static final QualifiedName	DefaultEditorProperty	= new QualifiedName("FIRST", "NotifiedOfDefault");
	
	protected IType stateMachineType;
	protected IField stateMachineField;
	protected ASTParser stateMachineParser;
	protected int defaultState;
	
	protected FRCRStateMachine(IField inputField, ASTNode root){
		super();
		defaultState = -1;
		stateMachineParser = null;
		stateMachineField = inputField;
		
		stateMachineType = ModelBuilderUtil.createIFieldType(stateMachineField);
		
		disableEventPassing();
		
		// Set the default editor for the underlying file to be the code and
		// diagram editor
		try {
			IFile file = (IFile) stateMachineType.getCompilationUnit().getCorrespondingResource();
			String value = file.getPersistentProperty(DefaultEditorProperty);
			if (value == null) {
				file.setPersistentProperty(DefaultEditorProperty, "true");
				IDE.setDefaultEditor(file, "edu.wpi.first.javadev.builder.StateMachineCodeAndDiagramEditor");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Find the states
		try {
			IJavaElement[] children = stateMachineType.getChildren();

			for (IJavaElement current : children) {
				FRCRElement el;
				
				if((el = FRCRobot.createFRCRElement(current))!= null){
					if(el instanceof FRCRState){add(el);}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Find the default state and transitions
		if (root == null) {
			getParser().createAST(null).accept(new StateMachineASTReader());
		} else {
			root.accept(new StateMachineASTReader());
		}
		
		enableEventPassing();
	}
	
	/**
	 * @return The states in this state machine
	 */
	public FRCRState[] getStates(){
		ArrayList<FRCRState> states = new ArrayList<FRCRState>();
		
		for(FRCRElement child : children){
			if(child instanceof FRCRState){
				states.add((FRCRState)child);
			}
		}
		
		return states.toArray(new FRCRState[states.size()]);
	}
	
	/**
	 * @return All the transitions contained within this state machine
	 */
	public FRCRTransition[] getTransitions(){
		ArrayList<FRCRTransition> trans = new ArrayList<FRCRTransition>();
		
		for(FRCRElement child : children){
			if(child instanceof FRCRTransition){
				trans.add((FRCRTransition)child);
			}
		}
		
		return trans.toArray(new FRCRTransition[trans.size()]);
	}
	
	/**
	 * @return The ASTParser for this state machine
	 */
	protected ASTParser getParser() {
		if (stateMachineParser == null) {
			stateMachineParser = ASTParser.newParser(AST.JLS3);
			stateMachineParser.setKind(ASTParser.K_COMPILATION_UNIT);
		}
		stateMachineParser.setSource(stateMachineType.getCompilationUnit()
				.getPrimary());
		return stateMachineParser;
	}
	
	/**
	 * @return the index of the default state
	 */
	public int getDefaultStateIndex() {
		return defaultState;
	}
	
	/** Returns the default starting state of this state machine */
	public void setDefaultState(int index) {
		applyCode(index, getStates(), getTransitions());
	}

	/** Adds a state to the code */
	public void addState(String newStateClassName) {
		// TODO FRCRStateMachine: addState: Method Stub
		//TODO make this turn off and on outside updates if it doesn't use
		//java updates to apply changes
	}
	
	/** Renames an existing state */
	public boolean renameState(Shell shell, FRCRState state, String newName) {
		return state.renameClass(shell, newName);
	}

	/** Adds a transition to the code */
	public void addTransition(FRCRTransition transition) {
		//TODO make this turn off and on outside updates if it doesn't use
		//java updates to apply changes
		FRCRTransition[] currentTransitions = getTransitions();
		FRCRTransition[] transitions = new FRCRTransition[currentTransitions.length + 1];
		System.arraycopy(currentTransitions, 0, transitions, 0, currentTransitions.length);
		transitions[transitions.length - 1] = transition;
		applyCode(getDefaultStateIndex(), getStates(), transitions);
	}

	/** Removes a set of transitions from the code */
	public void removeTransitions(FRCRTransition[] transitions) {
		FRCRTransition[] currentTransitions = getTransitions();
		for (FRCRTransition removeTransition : transitions) {
			for (int i = 0; i < currentTransitions.length; i++) {
				if (removeTransition.equals(currentTransitions[i])) currentTransitions[i] = null;
			}
		}
		applyCode(getDefaultStateIndex(), getStates(), currentTransitions);
	}

	/** Physically writes code to a method body */
	protected void applyCode(int defaultStateIndex, FRCRState[] states, FRCRTransition[] transitions) {
		try {
			// Generate some base information
			String source = stateMachineType.getCompilationUnit().getSource();
			Document document = new Document(source);

			// Modify the node
			CompilationUnit root = (CompilationUnit) getParser().createAST(null);
			root.recordModifications();
			root.accept(new Writer(defaultStateIndex, states, transitions));

			// Get the edits made to the AST
			TextEdit edits = root.rewrite(document, stateMachineType.getJavaProject().getOptions(true));

			// Set the unit to the new source code
			stateMachineType.getCompilationUnit().applyTextEdit(edits, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void removeFromCode(){
		try{
			if(stateMachineField != null){
				IJavaModel jm = stateMachineField.getJavaModel();
				jm.delete(new IJavaElement[]{stateMachineField}, false, null);
			}
		}catch(Exception e){}
		
		dispose();
	}
	
	@Override
	public String getCapabilityCodeFragment(FRCRCapability cap){
		//States have no capabilities, and so this should allow the
		//code to short circuit and run faster
		return null;
	}
	
	@Override
	public boolean isInstanceOfSame(FRCRParent cadidate){
		if(cadidate instanceof FRCRStateMachine){
			FRCRStateMachine smCand = (FRCRStateMachine)cadidate;
			
			if((stateMachineType == null)||(smCand.stateMachineType == null)){
				return false;
			}
			
			return stateMachineType.equals(smCand.stateMachineType);
		}
		
		return false;
	}

	
	@Override
	public boolean isVisibleFrom(ICompilationUnit compUnit, boolean parentEnabled){
		//Visible if:  unit is its or its type's compilation unit, field is public
		//and its parent is enabled, or field is public and static
		if(compUnit != null){
			if((stateMachineField != null)&&(stateMachineField.getCompilationUnit() != null)){
				if(ModelBuilderUtil.isSameCompilationUnit(compUnit,
						stateMachineField.getCompilationUnit())){
					return true;
				}
			}
			
			if((stateMachineType != null)&&(stateMachineType.getCompilationUnit() != null)){
				if(ModelBuilderUtil.isSameCompilationUnit(compUnit,
						stateMachineType.getCompilationUnit())){
					return true;
				}
			}
		}
		
		if(stateMachineField != null){
			try{
				int flags = stateMachineField.getFlags();
				
				if(Flags.isPublic(flags)){
					return parentEnabled || Flags.isStatic(flags);
				}
			}catch(Exception e){return false;}
		}
		
		return false;
	}
	
	@Override
	public String getElementName(){
		String name = "{SM:";
		
		if(stateMachineType != null){
			name += stateMachineType.getElementName() + ":";
		}else{
			name += "NULLSMTYPE:";
		}
		
		if(stateMachineField != null){
			name += stateMachineField.getElementName() + "}";
		}else{
			name += "NULLSMFIELD}";
		}
		
		return name;
	}
	
	@Override
	public String getDisplayName(){
		String name = "";
		
		if(stateMachineField != null){
			name += stateMachineField.getElementName() + " : ";
		}else{
			name += "NULLSMFIELD : ";
		}
		
		if(stateMachineType != null){
			name += stateMachineType.getElementName();
		}else{
			name += "NULLSMTYPE";
		}
		
		return name;
	}
	
	@Override
	public boolean canModify(){
		if((stateMachineField != null)&&(stateMachineField.getCompilationUnit() != null)){
			return true;
		}
		
		return false;
	}
	
	@Override
	public ModelElementType getElementType(){
		return ModelElementType.FRCRSTATEMACHINE;
	}
	
	@Override
	public void rebuild(){
		disableEventPassing();
		enableEventLogging();
		clearEventLog();
		
		((FRCElement)getParent()).rebuild();
		
		disableEventLogging();
		enableEventPassing();
		
		notifyListeners(new FRCModelEvent(
				this,
				FRCModelEvent.FT_DATA_CHANGE,
				FRCModelEvent.FK_REBUILT,
				"Rebuilt " + getElementName(),
				loggedEvents.toArray(new FRCModelEvent[loggedEvents.size()])));
		
		clearEventLog();
	}
	
	@Override
	public boolean definedByElement(IJavaElement element){
		if((stateMachineType == null)&&(stateMachineField == null)){return false;}
		
		if(stateMachineType != null){
			if(element.equals(stateMachineType)){return true;}
		}
		
		if(stateMachineField != null){
			return element.equals(stateMachineField);
		}
		
		return false;
	}
	
	@Override
	protected void runUpdate(IJavaElement element, ASTNode node){
		//Only need to check against types comp unit, since fields are handled
		//by parent
		if((stateMachineType == null)||(!stateMachineType.exists())){
			rebuild();
			return;
		}
		
		if(element != null){
			if(element instanceof ICompilationUnit){
				ICompilationUnit unit = (ICompilationUnit)element;
				if((stateMachineType != null)&&(stateMachineType.getCompilationUnit() != null)){
					if(ModelBuilderUtil.isSameCompilationUnit(unit,
							stateMachineType.getCompilationUnit())){
						if(stateMachineField != null){
							//Use field, since children are still processed, 
							//and keeps proper associations
							FRCRStateMachine recSM = 
								new FRCRStateMachine(stateMachineField, node);
							reconcile(recSM);
							return;
						}
					}
				}
			}
			
			super.runUpdate(element, node);
		}
	}
	
	@Override
	protected boolean runReconcile(FRCElement updateTo){
		if(!equals(updateTo)){
			FRCRStateMachine sm = (FRCRStateMachine)updateTo;
			IField fieldHolder = stateMachineField;
			IType typeHolder = stateMachineType;
			stateMachineField = null;
			stateMachineType = null;
			stateMachineField = sm.stateMachineField;
			stateMachineType = sm.stateMachineType;
			
			if(!super.runReconcile(updateTo)){
				stateMachineField = null;
				stateMachineType = null;
				stateMachineField = fieldHolder;
				stateMachineType = typeHolder;
				return false;
			}
			
			defaultState = sm.defaultState;
			
			//Resets the parser to link to the new, correct IType
			stateMachineParser = null;
			getParser();
			
			notifyListeners(new FRCModelEvent(this, 
					FRCModelEvent.FT_DATA_CHANGE,
					FRCModelEvent.FK_RECONCILE,
					getElementName() + " reconciled"));
		}
		
		return true;
	}
	
	@Override
	public boolean openInEditor(){
		if((stateMachineType != null)
				&&(stateMachineType.getClassFile() == null)){
			try {
				JavaUI.openInEditor(stateMachineType);
				return true;
			} catch (Exception e) {return false;}
		}
		
		return false;
	}
	
	@Override
	public String getCodeFragment(){return null;}
	
	@Override
	public FRCVParent getViewModel(){
		FRCVParent smDisplay = new FRCVParent(this);
		
		FRCRState[] states = getStates();
		FRCRTransition[] trans = getTransitions();
		
		if(states.length > 0){
			FRCVParent stateRoot = 
				new FRCVParent("States", ModelElementType.FRCRSTATE);
			
			for(FRCRState currentState : states){
				stateRoot.add(currentState.getViewModel());
			}
			
			smDisplay.add(stateRoot);
		}
		
		if(trans.length > 0){
			FRCVParent tranRoot = 
				new FRCVParent("Transitions", ModelElementType.FRCRTRANSITION);
			
			for(FRCRTransition currentTran : trans){
				tranRoot.add(currentTran.getViewModel());
			}
			
			smDisplay.add(tranRoot);
		}
		
		return smDisplay;
	}
	
	@Override
	public void dispose(){
		super.dispose();
		stateMachineType = null;
		stateMachineField = null;
		stateMachineParser = null;
	}
	
	@Override
	public boolean equals(Object obj){
		if((super.equals(obj))&&(obj instanceof FRCRStateMachine)){
			FRCRStateMachine sMach = (FRCRStateMachine)obj;
			boolean retValF = false;
			boolean retValT = false;
			
			if(sMach.defaultState != defaultState){return false;}
			
			if((sMach.stateMachineField == null)&&(stateMachineField == null)){
				retValF = true;
			}else if((sMach.stateMachineField != null)&&(stateMachineField != null)){
				if(sMach.stateMachineField.equals(stateMachineField)){
					retValF = true;
				}
			}
			
			
			if((sMach.stateMachineType == null)&&(stateMachineType == null)){
				retValT = true;
			}else if((sMach.stateMachineType != null)&&(stateMachineType != null)){
				if(sMach.stateMachineType.equals(stateMachineType)){
					retValT = true;
				}
			}
			
			return retValT && retValF;
		}
		
		return false;
	}
	
	@Override
	public int hashCode(){
		int hash = defaultState;
		
		if(stateMachineType != null){hash += stateMachineType.hashCode();}
		
		if(stateMachineField != null){hash += stateMachineField.hashCode();}
		
		return super.hashCode() + hash;
	}
	
	/**
	 * Determines if the given object can be used to construct a
	 * FRCRStateMachine safely
	 * @param candidate The object to test against construction requirements
	 * @return true if the object can be used to create an FRCRStateMachine
	 */
	public static boolean meetsRequirements(Object candidate){
		if(candidate == null){return false;}
		
		if(candidate instanceof IField){
			IType smType = ModelBuilderUtil.createIFieldType((IField)candidate);
			
			if(smType != null){
				try {
					String superClass = smType.getSuperclassName();
					for (String current : ParseConstants.STATEMACHINE_ID) {
						if (current.equals(superClass)) return true;
					}
				} catch (Exception e) {return false;}	
			}
		}
		
		return false;
	}
	
	
	
	
	
	
	/**
	 * This class will set the value of the enclosing {@link RtStateMachine}'s
	 * transitions according to what it found within any {@link ASTNode} it
	 * visits. Also sets the default state.
	 * 
	 * @author Joe Grinstead
	 */
	protected class StateMachineASTReader extends ASTVisitor {
		protected FRCRState[]					states;
		protected ArrayList<FRCRTransition>	transitions				= new ArrayList<FRCRTransition>();
		protected FRCRState					beginState;
		protected boolean					withinTransitionArray	= false;

		/**
		 * Tries to find the state that goes with the given variable.
		 * 
		 * @param object
		 *            should be of type {@link SimpleName}, if not null is
		 *            returned
		 * @param states
		 *            the array of states to choose from
		 * @return the state (or null if it couldn't be found).
		 */
		protected FRCRState getState(Object object) {
			// Can't continue if not given a variable
			if (!(object instanceof SimpleName)) return null;

			// Find the state
			String variableName = ((SimpleName) object).getIdentifier();
			for (FRCRState state : states) {
				if (variableName.equals(state.getSimpleName())) {
					return state;
				}
			}

			// If it couldn't be found, default to null
			return null;
		}

		/**
		 * This method should always receive as an input the state machine
		 * class.
		 */
		@SuppressWarnings("rawtypes")
		@Override
		public boolean visit(TypeDeclaration node) {
			try {
				// Get the states from the enclosing RtStateMachine
				states = getStates();

				// Manually visit the children
				List body = node.bodyDeclarations();
				for (Object statement : body) {
					if (statement instanceof MethodDeclaration) {
						// Only concerned with method declarations, fields
						// aren't
						// necessary
						((MethodDeclaration) statement).accept(this);
					}
				}

				// Set the values (the default state is set while visiting the
				// "beginWithState" method invocation).
				disableEventPassing();
				add(transitions.toArray(new FRCRTransition[transitions.size()]));
				enableEventPassing();
			} catch (Exception e) {}

			// Don't drill down twice
			return false;
		}

		/**
		 * The default state is set when the user calls it within the
		 * "beginWithState" method
		 */
		@Override
		public boolean visit(MethodInvocation node) {
			// If this is a call to setTransitions then mark what the beginning
			// state is and dig down into the other argument
			if (node.getName().toString().equals("setTransitions")) {
				// Default to a null beginning state
				beginState = getState(node.arguments().get(0));

				// If the state couldn't be found, then don't continue
				if (beginState == null) return false;

				// Drill down into the transitions argument
				((ASTNode) node.arguments().get(1)).accept(this);

				// Unmark the state
				beginState = null;

				// Stop drilling down
				return false;
			}

			// Mark the default state if this is the beginWithState method
			if (node.getName().toString().equals("beginWithState")) {
				// Make sure the first argument is a variable
				Object argument = node.arguments().get(0);
				if (!(argument instanceof SimpleName)) return false;

				// Find the state that matches the given variable
				String variableName = ((SimpleName) argument).getIdentifier();
				for (int i = 0; i < states.length; i++) {
					if (variableName.equals(states[i].getSimpleName())) {
						// Mark it and stop searching
						defaultState = i;
						return false;
					}
				}

				// Stop drilling down
				return false;
			}

			// Otherwise we keep drilling down.
			return true;
		}

		/**
		 * Mark that the visitor is within an array.
		 */
		@Override
		public boolean visit(ArrayInitializer node) {
			try {
				withinTransitionArray = true;
				for (Object expression : node.expressions()) {
					((ASTNode) expression).accept(this);
				}
			} finally {
				withinTransitionArray = false;
			}
			return false;
		}

		/**
		 * React to the creation of a transition
		 */
		public boolean visit(ClassInstanceCreation node) {
			// Only do something if the beginning state was marked, you're
			// within an array, and there's no complicated markings
			if (beginState != null && withinTransitionArray && node.getType() instanceof SimpleType) {
				// Get the ending state
				FRCRState endState = getState(node.arguments().get(0));

				// Don't continue if it wasn't found
				if (endState == null) return false;

				// Create and add the transition
				final int offset = node.getStartPosition();
				final int length = node.getLength();
				ISourceRange sourceRange = new ISourceRange() {

					@Override
					public int getOffset() {
						return offset;
					}

					@Override
					public int getLength() {
						return length;
					}
				};
				transitions.add(new FRCRTransition(beginState, endState, stateMachineType, sourceRange));

				// Don't bother drilling down
				return false;
			}

			// Never bother going inside an instantiated class
			return false;
		}
	}

	/**
	 * This class writes the given state machine to the given ASTVisitor. It
	 * destroys the given state machine, so it should never be depended on.
	 * 
	 * @author Joe Grinstead
	 */
	@SuppressWarnings("unchecked")
	protected class Writer extends ASTVisitor {
		protected ArrayList<State>		states;
		protected State					beginState;
		protected ArrayList<ASTNode>	toRemove;
		protected int					defaultStateIndex;

		protected class State {
			/** The wrapped state */
			FRCRState					state;
			/** Transitions from the state */
			ArrayList<FRCRTransition>	transitions	= new ArrayList<FRCRTransition>();
			/** Whether the initialize method has been found for this state */
			boolean					initialized	= false;
		}

		/**
		 * Tries to find the state that goes with the given variable.
		 * 
		 * @param object
		 *            should be of type {@link SimpleName}, if not null is
		 *            returned
		 * @param states
		 *            the arraylist of states to check through
		 * @return the state (or null if it couldn't be found).
		 * @throws IllegalArgumentException
		 *             if the argument is not a simple name
		 * @throws ArrayIndexOutOfBoundsException
		 *             if the argument is a state, but the state is not in the
		 *             list of states
		 */
		protected State getState(Object object) throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
			// Can't continue if not given a variable
			if (!(object instanceof SimpleName)) throw new IllegalArgumentException();

			// Find the state
			String variableName = ((SimpleName) object).getIdentifier();
			for (State state : states) {
				if (variableName.equals(state.state.getSimpleName())) {
					return state;
				}
			}

			// If it couldn't be found, throw an exception
			throw new ArrayIndexOutOfBoundsException();
		}

		protected Writer(int defaultStateIndex, FRCRState[] states, FRCRTransition[] transitions) {
			// Set the default state
			this.defaultStateIndex = defaultStateIndex;

			// Get the states
			this.states = new ArrayList<State>();
			for (FRCRState rtState : states) {
				State state = new State();
				state.state = rtState;
				this.states.add(state);
			}

			// Get the transitions
			for (FRCRTransition transition : transitions) {
				if (transition == null) continue;
				for (State state : this.states) {
					if (state.state.equals(transition.getOriginState())) {
						state.transitions.add(transition);
						break;
					}
				}
			}

			// This will be populated with nodes to remove
			toRemove = new ArrayList<ASTNode>();
		}

		/**
		 * This method should always receive as an input the state machine
		 * class.
		 */
		@Override
		public boolean visit(TypeDeclaration node) {
			// Go through the body of the class
			for (Object child : node.bodyDeclarations()) {
				((ASTNode) child).accept(this);
			}

			// Remove the things which were marked to be removed
			for (ASTNode victim : toRemove) {
				victim.delete();
			}

			// Already visited
			return false;
		}

		/**
		 * Only concerned with the begin method
		 */
		@Override
		public boolean visit(MethodDeclaration node) {
			if (node.getName().getIdentifier().equals("begin")) {
				// Run through
				node.getBody().accept(this);

				// Create the beginWithState call
				AST ast = node.getAST();
				MethodInvocation beginWithState = ast.newMethodInvocation();
				beginWithState.setName(ast.newSimpleName("beginWithState"));
				beginWithState.arguments()
						.add(ast.newSimpleName(states.get(defaultStateIndex)
								.state.getSimpleName()));

				// Add the call
				node.getBody().statements().add(ast.newExpressionStatement(beginWithState));
			}

			// Don't go into any methods
			return false;
		}

		/**
		 * The two methods that the writer cares about are setTransitions,
		 * beginWithState, and initialize
		 */
		@Override
		public boolean visit(MethodInvocation node) {
			if (node.getName().toString().equals("beginWithState")) {
				// Delete this call, it will be added to the end of the code
				// later
				toRemove.add(node.getParent());
				return false;
			}

			if (node.getName().toString().equals("setTransitions")) {
				try {
					beginState = getState(node.arguments().get(0));

					if (beginState.transitions.size() > 0) {
						// Process what transitions are apart of this
						((ASTNode) node.arguments().get(1)).accept(this);
					} else {
						// There are no transitions for this state, so remove
						// the call to setTransitions
						toRemove.add(node.getParent());
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					// This setTransitions needs to be deleted because it's
					// state no longer exists
					toRemove.add(node);
				} catch (IllegalArgumentException e) {
					// The first argument was not a variable, so it had to be
					// user created and should be left alone
				}

				// Unmark the state and don't continue down the tree
				beginState = null;
				return false;
			}

			// Mark that a state has been initialized
			if (node.getName().toString().equals("initialize")) {
				// Make sure it's referencing this statemachine
				if (!(node.arguments().get(0) instanceof ThisExpression)) return false;

				try {
					// Find out what the name of the referenced state is
					State foundState = getState(node);

					// Remove the reference if it's already been done
					if (foundState.initialized) {
						toRemove.add(node);
					} else {
						foundState.initialized = true;
					}
				} catch (ArrayIndexOutOfBoundsException e) {
					// Remove it, it is a reference to a state no longer in the
					// machine
					toRemove.add(node);
				} catch (IllegalArgumentException e) {
					// The state argument was not a variable, so it had to be
					// user created and should be left alone
				}

				// Who cares to keep going down
				return false;
			}

			// Default to keep drilling
			return true;
		}

		protected boolean	withinTransitionArray	= false;

		@Override
		public boolean visit(ArrayInitializer node) {
			// Check if the search is on for transitions
			if (beginState == null) return false;

			withinTransitionArray = true;

			// Delete anything that's not necessary, and mark what's been
			// found
			for (Object expression : node.expressions()) {
				if (expression instanceof ClassInstanceCreation) {
					((ClassInstanceCreation) expression).accept(this);
				}
			}

			// Add what's needed
			for (FRCRTransition transition : beginState.transitions) {
				AST ast = node.getAST();
				ClassInstanceCreation constructor = ast.newClassInstanceCreation();
				constructor.setType(ast.newSimpleType(ast.newSimpleName("Transition")));
				constructor.arguments().add(ast.newSimpleName(
						transition.getDestinationState().getSimpleName()));
				constructor.arguments().add(ast.newNullLiteral());
				node.expressions().add(constructor);
			}

			withinTransitionArray = false;

			return false;
		}

		public boolean visit(ClassInstanceCreation node) {
			// Make sure this is a transition within an array and going to a
			// state

			if (beginState != null && withinTransitionArray && node.getType() instanceof SimpleType) {
				try {
					State endState = getState(node.arguments().get(0));

					// Find the transition and remove it from the list of those
					// that need to be found
					ArrayList<FRCRTransition> transitions = 
						(ArrayList<FRCRTransition>) beginState.transitions.clone();
					for (FRCRTransition transition : transitions) {
						if (transition.getDestinationState().equals(endState.state)){
							beginState.transitions.remove(transition);
							return false;
						}
					}

					// The transition was not in the list, so delete it
					toRemove.add(node);
				} catch (ArrayIndexOutOfBoundsException e) {
					// The end state is no longer a state, so delete it
					toRemove.add(node);
				} catch (IllegalArgumentException e) {}

				return false;
			}

			return true;
		}
	}
	
}
