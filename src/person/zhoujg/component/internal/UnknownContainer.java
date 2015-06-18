package person.zhoujg.component.internal;

import java.util.Collection;

import jdepend.framework.JavaClass;
import person.zhoujg.component.model.DeployableContainer;

public class UnknownContainer extends DeployableContainer {

	UnknownContainer(String name) {
		super(name, DeployableContainer.UNKNOWN);
	}
	
	void addJavaClasses(Collection<JavaClass> c){
		classes.addAll(c);
	}
	
//	void addJavaPackages(Collection<JavaPackage> packages){
//		for (JavaPackage p : packages){
//			addJavaPackage(p);
//		}
//	}

}
