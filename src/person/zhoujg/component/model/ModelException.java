package person.zhoujg.component.model;

public class ModelException extends RuntimeException{
	public ModelException(String msg) {
		super(msg);
	}
	
	private static final long serialVersionUID = 1L;
	
	public static final String NOT_ALLOWED = "";
	public static final String PACKAGE_OR_CLASS_NOT_FOUND = "The specified package or class is not found in the current component.";
	public static final String SPLIT_PACKAGE = "A package cannot be splited into two components!";
	public static final String PROJECT_NOT_VALID = "Not a valid project object for output.";
	public static final String PARENT_IN_BLACKLIST ="Parent component {0} cannot be in the black list of its child {1}.";
	public static final String INTERFACE_WRONG = "Cannot find declared interface(s) in component(s).";
}
