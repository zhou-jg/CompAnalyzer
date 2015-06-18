package person.zhoujg.component.model.validation;

import person.zhoujg.component.internal.ObjectRegistry;

public abstract class ModelValidator {
	protected ObjectRegistry host = null;
	private ModelValidator successor = null;
	private static String errorInfo = null;
	
	protected ModelValidator(ObjectRegistry host) {
		this.host = host;
	}
	
	public ObjectRegistry getHost(){
		return host;
	}
	
	public abstract boolean validate();
	
	public String getErrorInfo(){
		return errorInfo;
	}
	
	protected void setErrorInfo(String info){
		errorInfo = info;
	}
	
	public ModelValidator getSuccessor(){
		return successor;
	}
	
	public void setSuccessor(ModelValidator successor){
		this.successor = successor;
	}
}
