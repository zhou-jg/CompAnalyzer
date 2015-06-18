package person.zhoujg.component.model.problem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jdepend.framework.JavaClass;
import person.zhoujg.component.model.IComponent;

/**
 * All depencecy problems are among components that violates accessing rules. 
 * These includes:<br> 
 * <li> {@link #IN_BLACKLIST}: Component A is in the black list of Component B, 
 * 		but A accesses B.
 * <li> {@link #NOT_IN_WHITELIST}: Component B is invisble to Component A due to 
 * 		nested components or layer mechanisms, but A accesses B. 
 *      component A. 
 * <li> {@link #NON_INTERFACE}: Component A accesses non interface part of Component B 
 * 		though B is visible to A. 
 * <li> {@link #NON_DETERMINISTIC}: Component B is accessed by Component A, but it cannot
 * 		determine whether this access is allowed or not. This is mainly caused by that A is
 * 		not in B's black list, neither nor B's white list.
 * 
 * @author zhoujg
 * @date 2014年2月20日
 *
 */
public class AccessProblem extends DependencyProblem {
	
	public static final int IN_BLACKLIST 		= 1;
	public static final int NOT_IN_WHITELIST 	= 2;
	public static final int NON_INTERFACE 		= 3;
	// a type of warning.
	public static final int NON_DETERMINISTIC	= 4;
	
	private IComponent dependee;
	private IComponent dependant;
	
	private Map<JavaClass, Set<JavaClass>> cause = new HashMap<JavaClass, Set<JavaClass>>();
	
	private String desc;

	public AccessProblem(IComponent dependee, IComponent dependant, int type, String desc){
		super(type);
		this.dependee = dependee;
		this.dependant = dependant;
		this.desc = desc;
	}
	
	public Map<JavaClass, Set<JavaClass>> getProblemElements() {
		return cause;
	}

	public void addProblemSubElement(JavaClass dependee, JavaClass dependant) {
		Set<JavaClass> set = cause.get(dependee);
		if (set == null){
			set = new HashSet<JavaClass>();
			cause.put(dependee, set);
		}
		set.add(dependant);
	}
	
	public void addProblemElement(JavaClass dependee,Set<JavaClass> dependants){
		Set<JavaClass> set = cause.get(dependee);
		if (set == null){
			set = dependants;
			cause.put(dependee, set);
		}else{
			set.addAll(dependants);
		}
	}
	
	public void setProblemElements(Map<JavaClass, Set<JavaClass>> map){
		this.cause = map;
	}
	
	public IComponent getDependee(){
		return dependee;
	}
	
	public IComponent getDependant(){
		return dependant;
	}
	
	public String getDescription(){
		return desc;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		switch (type){
		case AccessProblem.IN_BLACKLIST : {
			sb.append("[Component]" + dependant.getName() + " is in the black list of [Component]" + dependee.getName() + ".\n");
			break;
		}
		case AccessProblem.NON_INTERFACE : {
			sb.append("[Component]" + dependant.getName() + " accesses non interface part of [Component]" + dependee.getName() + ".\n");
			break;
		}
		case AccessProblem.NOT_IN_WHITELIST : {
			sb.append("[Component]" + dependee.getName() + " should be invisible to [Component]" + dependant.getName() + ".\n");
			break;
		}
		case NON_DETERMINISTIC : {
			sb.append("Cannot judge whether [Component]" + dependee.getName() + " should be accessed by [Component]" + dependant.getName() + ".\n");
			break;
		}
		};
		for (JavaClass clazz : cause.keySet()){
			sb.append("  [" + clazz.getName() + "] is accessed by\n");
			for (JavaClass dep : cause.get(clazz)){
				sb.append("    " + dep.getName() + "\n");
			}
		}
		return sb.toString();
	}

}
