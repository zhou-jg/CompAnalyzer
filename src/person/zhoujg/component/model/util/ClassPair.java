package person.zhoujg.component.model.util;

import jdepend.framework.JavaClass;

public class ClassPair {
	private JavaClass left, right;
	public ClassPair(JavaClass left, JavaClass right){
		this.left = left;
		this.right = right;
	}
	
	public JavaClass getLeft(){
		return left;
	}
	
	public JavaClass getRight(){
		return right;
	}
}
