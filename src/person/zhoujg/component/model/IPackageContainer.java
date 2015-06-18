package person.zhoujg.component.model;

import java.util.Set;

import jdepend.framework.JavaPackage;

/**
 * Basic interface for a package container which can contain multiple 
 * packages, so that we can define a base virtual component in softare 
 * architecture design, since most component models are in this level. 
 * 
 * @author zhoujg
 * @date 
 */
public interface IPackageContainer extends INamedObject{
	/**
	 * Get a specified {@link JavaPackage} object in terms of its name.
	 * @param name the specified {@link JavaPackage} name
	 * @return the specified {@link JavaPackage} object
	 */
	JavaPackage getJavaPackage (String name);
	
	/**
	 * Get all {@link #JavaPackage}s contained in the current container.
	 * @return a collection of {@link #JavaPackage} objects.
	 */
	Set<JavaPackage> getJavaPackages();
	
	/**
	 * Add a package with specified name to the current package container.
	 * @param packName package name support wildchar '*'.
	 * @return the current <code>IPackageContainer</code> object.
	 */
	IPackageContainer addPackage(String packName);
	
	/**
	 * @return all package names the current container has.
	 */
	Set<String> getPackages();
	
	/**
	 * Remove a specified package from the current package container.
	 * @param packName name of the package to be removed, supporting '*'.
	 * @return the current <code>IPackageContainer</code> object.
	 */
	IPackageContainer removePackage(String packName);
	
	/**
	 * @return the name of the current package container.
	 */
	String getName();
	
}
