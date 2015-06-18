package person.zhoujg.component.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import jdepend.framework.JavaPackage;
import person.zhoujg.util.FileUtil;

/**
 * A default implementation of {@link IPackageContainer}.
 * 
 * @author zhoujg
 * @date 2014年1月20日
 *
 */
public class PackageSet implements IPackageContainer {
	
	/* name of the packageSet */
	protected String name;
	protected Set<String> packageNames;
	protected HashMap<String, JavaPackage> javaPackages;
	
	protected PackageSet(String name) {
		this.name = name;
		packageNames = new HashSet<String>();
		javaPackages = new HashMap<String, JavaPackage>();		
	}
	
	public void setName(String name){
		this.name = name;
	}

	/** {@inheritDoc}*/
	@Override
	public JavaPackage getJavaPackage(String name) {
		return javaPackages.get(name);
	}

	/** {@inheritDoc}*/
	@Override
	public Set<JavaPackage> getJavaPackages() {
		return new HashSet<JavaPackage>(javaPackages.values());
	}

	/** {@inheritDoc}*/
	@Override
	public IPackageContainer addPackage(String packName) {
		packageNames.add(FileUtil.getQualifiedName(packName));
		
		return this;
	}

	/**{@inheritDoc} */
	@Override
	public IPackageContainer removePackage(String packName) {
		packageNames.remove(FileUtil.getQualifiedName(packName));
		
		return this;
	}

	/**{@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/**{@inheritDoc}*/
	@Override
	public Set<String> getPackages() {
		return packageNames;
	}

	/**
	 * This method should not be invoked by user.
	 * @param pack
	 */
	public void addJavaPackage(JavaPackage pack) {
		javaPackages.put(pack.getName(), pack);
	}
	
}
