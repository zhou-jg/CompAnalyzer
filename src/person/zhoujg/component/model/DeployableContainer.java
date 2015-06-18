package person.zhoujg.component.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jdepend.framework.JavaClass;

public class DeployableContainer implements IDependableEntity{
	public static final int CLASS_DIR = 1;
	public static final int JAR_FILE = 0;
	public static final int UNKNOWN = -1;
	
	protected String name;
	protected int type = 0;
	protected String path;
	
	protected Set<JavaClass> classes = new HashSet<JavaClass>();
	
	private Set<DeployableContainer> dependencies = new HashSet<DeployableContainer>();
	private Set<DeployableContainer> dependants	 = new HashSet<DeployableContainer>();
	
	protected DeployableContainer(String name, int type) {
//		super(name);
		this.name = name;
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public String getPath() {
		return path;
	}
	
	public void addClass(JavaClass clazz) {
		classes.add(clazz);
	}
	
	public Collection<JavaClass> getJavaClasses() {
		return classes;
	}

	public Set<String> getPackages() {
//		return javaPackages.keySet();
		Set<String> packs = new HashSet<String>();
		for (JavaClass c : getJavaClasses()){
			packs.add(c.getPackageName());
		}
		return packs;
	}

	@Override
	public boolean isDependency(IDependableEntity obj) {
		if (obj instanceof DeployableContainer) {
			return dependencies.contains(obj);
		}
		return false;
	}

	@Override
	public String getName() {
		return name;
	}

	public void addDependency(DeployableContainer refCon) {
		dependencies.add(refCon);
	}

	public void addDependant(DeployableContainer con) {
		dependants.add(con);
	}

	@Override
	public Collection<DeployableContainer> getAfferents() {
		return dependants;
	}

	@Override
	public Collection<DeployableContainer> getEfferents() {
		return dependencies;
	}

	@Override
	public void setName(String name) {
		//
	}
}
