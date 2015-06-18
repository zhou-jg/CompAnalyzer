package jdepend.framework;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import person.zhoujg.component.model.IDependableEntity;

/**
 * The <code>JavaClass</code> class represents a Java 
 * class or interface.
 * 
 * Informaiton on class level includes:
 * <li>owning package</li>
 * <li>owning container</li>
 * <li>whether abstract</li>
 * <li>whether an interface</li>
 * <li>superclass</li>
 * <li>implemented interfaces</li>
 * <li>referenced classes</li>
 * 
 * @author <b>Mike Clark</b>
 * @author Clarkware Consulting, Inc.
 * 
 * modified by zhoujg
 */

public class JavaClass implements IDependableEntity {

    private String className;
    private String packageName;
    private boolean isAbstract;
    private HashMap<String, JavaPackage> imports; //adds generic by zhoujg
    private String sourceFile;


    public JavaClass(String name) {
        className = name;
        packageName = "default";
        isAbstract = false;
        isInterface = false;
        imports = new HashMap<String, JavaPackage>();
        references = new HashMap<String, JavaClass>();
        sourceFile = "Unknown";
        container = "Unknown";
        interfaces = new HashMap<String, JavaClass>();
        superClazz = null;
    }

    public void setName(String name) {
        className = name;
    }

    public String getName() {
        return className;
    }

    public void setPackageName(String name) {
        packageName = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setSourceFile(String name) {
        sourceFile = name;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public Collection<JavaPackage> getImportedPackages() {
        return imports.values();
    }

    public void addImportedPackage(JavaPackage jPackage) {
        if (!jPackage.getName().equals(getPackageName())) {
            imports.put(jPackage.getName(), jPackage);
        }
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void isAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public boolean equals(Object other) {

        if (other instanceof JavaClass) {
            JavaClass otherClass = (JavaClass) other;
            return otherClass.getName().equals(getName());
        }

        return false;
    }

    public int hashCode() {
        return getName().hashCode();
    }

    public static class ClassComparator implements Comparator<JavaClass> {

        public int compare(JavaClass a, JavaClass b) {
            return a.getName().compareTo(b.getName());
        }
    }
    
    //--------------------added by zhoujg to support dependency analysis----------------------------//
    private HashMap<String, JavaClass> references;
    private JavaClass superClazz;
    
	private HashMap<String, JavaClass> interfaces;
    private String container;    
	private boolean isInterface;
	
	public JavaClass getSuperClazz() {
		return superClazz;
	}

	public void setSuperClazz(JavaClass superClazz) {
		this.superClazz = superClazz;
	}

	public void addInterface(JavaClass inter){
		interfaces.put(inter.getName(), inter);
	}
	
	public Collection<JavaClass> getInterfaces(){
		return interfaces.values();
	}
    
	public String getContainer() {
		return container;
	}

	public void setContainer(String container) {
		this.container = container;
	}

    public boolean isInterface() {
		return isInterface;
	}

	public void isInterface(boolean isInterface) {
		this.isInterface = isInterface;
	}

	public void addReferencedClass(JavaClass clazz){
    	references.put(clazz.getName(), clazz);
    }

	@Override
	public boolean isDependency(IDependableEntity obj) {
		if (obj instanceof JavaClass){
			if (references.keySet().contains(obj.getName())){
				return true;
			}
		}
		return false;
	}

	@Override
	public Collection<JavaClass> getAfferents() {
		return Collections.emptySet();
	}

	@Override
	public Collection<JavaClass> getEfferents() {
		return references.values();
	}
	
	
}
