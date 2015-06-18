package person.zhoujg.component.model;

import java.util.Collection;
import java.util.Set;

/**
 * This class represents a component that is used in software modeling.
 * A component provides modular design by offering interface and hiding
 * internal implementation. <br>
 * We allow a component to set white list or black list of other components 
 * to specify which components can allow to access it or not. In addition, 
 * a component can be hierarchical, i.e., containing nested child components 
 * and allow to upgrade interfaces of its children as its own interface. 
 * An advanced feature of the component  interface is that the interface 
 * support different views for different components, which allows fine tuned 
 * for dependency issues. The interfaces in different views of a component 
 * are disjoint, i.e., they are not allowed to overlap, to avoid the problem 
 * like, e.g., interface A in both view 1 and view 2, but view 1 is not allowed 
 * to access by component C while view 2 does, so what happens if component C 
 * access interface A. A solution for such situation is to extract interface A 
 * into a separate view and set access rule for component C. If an interface is 
 * not set related view, it belongs to a <code>default</code> view (with view id
 * of "default", thus we recommend users not to use it as an explicit view id).<br>
 * Besides the common concepts support for a component, we also provide
 * <b>layer</b> (an important architectural concept) support for a component, 
 * i.e., a component can adds a {@link ILayer} as its child.
 *
 * @see ILayer
 * 
 * @author zhoujg
 * @date 2014/3/14
 *
 */
public interface IComponent extends IPackageContainer, IDependableEntity {		
	
	/**
	 * @return all interface views name
	 */
	Set<String> getInterfaceViews();
	
	/**
	 * Get belonging view of the specific interface.
	 * @param api the specified interface.
	 * @return view id. <code>null</code> if not found.
	 */
	String getView(String api);
	
	/**
	 * Check whether the specified component can see the current component or not.
	 * @param comp the specified component.
	 * @return
	 */
	boolean isVisibleTo(IComponent comp);
	
	/**
	 * Allow the specified component to access the current component on
	 * specified interfaces<br>
	 * <b>Note</b>, this policy only applies to sibling components.
	 * @param comp specified component
	 * @param views specified interface groups (or views), <code>default</code> for all views
	 * @return the current component
	 * @see #addsToBlackList(IComponent)
	 * @see #isInWhiteList(IComponent)
	 * @see #getWhiteList()
	 * @see #removeFromWhiteList(IComponent)
	 */
	IComponent addsToWhiteList(IComponent comp, String... views);
	
	/**
	 * Remove the specified component from the white list for specific interfaces.
	 * @param comp the specified component
	 * @param views specified interfaces denoted by group (or view) id (name).
	 * @return the current component
	 * @see #addsToInterface(Collection)
	 * @see #isInWhiteList(IComponent)
	 */
	IComponent removeFromWhiteList(IComponent comp, String... views);
	
	/**
	 * Clear white list on the specified views
	 * @param views
	 * @return current component.
	 */
	IComponent clearWhiteList(String... views);
	
	/**
	 * Clear black list on the specified views
	 * @param views
	 * @return current component
	 */
	IComponent clearBlackList(String... views);
	
	/**
	 * Clear interface declarations on the specified views (<code>null</code> 
	 * for all views).<br>
	 * <b>Note</b>, the operation may cause black/white list change.
	 * @param views
	 * @return current component
	 */
	IComponent clearInterface(String... views);
	
	/**
	 * Judge whether specified component is in the white list of the current component
	 * @param comp specified component
	 * @return <b>true</b> if the specified component is in the white list, <b>false</b> otherwise.
	 * @see #addsToWhiteList(IComponent)
	 * @see #getWhiteList()
	 * @see #isInBlackList(IComponent)
	 */
	boolean isInWhiteList(IComponent comp, String... viewId);
	
	/**
	 * @param views interface views. <code>Default</code> for all.
	 * @return all components that are allowed to access the component on the
	 * specified interface views. 
	 * @see #getBlackList()
	 */
	Set<IComponent> getWhiteList(String... views);
	
	/**
	 * Not allow the specified component to access the current component</br>
	 * <b>Note</b>, this policy only applies to sibling components.
	 * @param comp specified component
	 * @return the current component
	 * @see #addsToWhiteList(IComponent)
	 * @see #isInBlackList(IComponent)
	 * @see #getBlackList()
	 */
	IComponent addsToBlackList(IComponent comp, String... views );
	
	/**
	 * Remove a specified component from the black list of specified interface views
	 * @param comp the specified component
	 * @param views the specified interface views, <code>default</code>for all
	 * @return the current component 
	 * @see #addsToBlackList(IComponent)
	 * @see #isInBlackList(IComponent)
	 */
	IComponent removeFromBlackList(IComponent comp, String... views);
	
	/**
	 * @return all components that are denied to access the component. These include 
	 * blacklist defined on the current component and other blacklists defined on e.g., 
	 * its ancestors. 
	 * @see #addsToBlackList(IComponent)
	 * @see #isInBlackList(IComponent)
	 */
	Set<IComponent> getBlackList(String... viewId);
	
	/**
	 * Judge whether specified component is in the black list of the current component.<br>
	 * <b>Note</b>, black/white list policy is only applied to sibling components, therefore,
	 * we only consider sibling components (without their decendants), so, not in black list 
	 * does not mean the specified component can access the current component. In addition, 
	 * the operation is transient since components may be changed their owner during configuration. 
	 * Use 
	 * @param comp specified component
	 * @param views specified interface groups
	 * @return <b>true</b> if the specified component is in the black lists of all the interface
	 * groups specified by the <code>views</code>, <b>false</b> otherwise.
	 * @see #addsToBlackList(IComponent)
	 * @see #getBlackList()
	 */
	boolean isInBlackList(IComponent comp, String... views);
	
	/**
	 * Adds specified packages or classes in a fully qualified format to 
	 * all interface views of the current component.<br>
	 * <b>Note</b>, we allow user to use a package name as short for its
	 * contained classes as interfaces, thus, the name of a package must 
	 * obey conventions like, all characters in the name are lowercase, 
	 * otherwise it will be treated as a class which uses a upcase character 
	 * in the first character of its name.
	 * @param paths a collection of package or class names
	 * @return the current component
	 * @see #addsToInterface(String)
	 */
	IComponent addsToInterface(String... paths);
	
	/**
	 * Adds interfaces specified by <code>paths</code> to a specified group (or view),
	 * which allows fine-grained tunning.<br>
	 * <b>Note</b>, we allow user to use a package name as short for its
	 * contained classes as interfaces, thus, the name of a package must 
	 * obey conventions like, all characters in the name are lowercase, 
	 * otherwise it will be treated as a class which uses a upcase character 
	 * in the first character of its name.
	 * @param viewID
	 * @param paths
	 * @return
	 */
	IComponent addsToInterface(String viewID, String[] paths);
	
	
	
	/**
	 * Remove specified interfaces from all interface groups (views) that contains it.<br>
	 * <b>Note</b>, we allow user to use a package name as short for its
	 * contained classes as interfaces, thus, the name of a package must 
	 * obey conventions like, all characters in the name are lowercase, 
	 * otherwise it will be treated as a class which uses a upcase character 
	 * in the first character of its name.
	 * @param apis the specified interface names
	 * @return the current component
	 */
	IComponent removeFromInterface(String... apis);
	
	/**
	 * Remove specified interfaces from the specified interface view. <br> 
	 * Please note, we don't move an interface to the <code>default</code> group if 
	 * it is moved from all other views.<br>
	 * <b>Note</b>, we allow user to use a package name as short for its
	 * contained classes as interfaces, thus, the name of a package must 
	 * obey conventions like, all characters in the name are lowercase, 
	 * otherwise it will be treated as a class which uses a upcase character 
	 * in the first character of its name.
	 * @param viewId specified interface view
	 * @param apis specified interfaces
	 * @return current component
	 */
	IComponent removeFromInterface(String viewId, String[] apis);
	
	/**
	 * @return interfaces of specified views the current component, <code>default</code> for all .
	 */
	Set<String> getInterface(String... views);
	
	/**
	 * @return a collection of components depended by the current component.
	 * @see #getDependants()
	 */
	Set<IComponent> getDependencies();
	
	/**
	 * @return a collection of components that depend on the current component
	 * @see #getDependencies()
	 */
	Set<IComponent> getDependants();
	
	/**
	 * Set whether non interface contents of the component can be accessed by its parent
	 * if has one.<br>
	 * <b>Note:</b> whether a component is sealed or not, the non interface parts of its
	 * chidren (if has) are always invisble to the outside. 
	 * @param ealed <code>true</code> not allow to access; <code>false</code> otherwise.
	 */
	@Deprecated
	void isSealed(boolean sealed);
	
	/**
	 * @return whether non interface of the component can be accessed by its parent if has one.
	 * @see #isSealed(boolean)
	 */
	@Deprecated
	boolean isSealed();	

	/**
	 * @return all decendant components of the current component.
	 * 			An empty collection indicates no descendant.
	 */
	Set<IComponent> getAllDescendants();
	
	/**
	 * @return all ancestors of the current component. An empty
	 * 			collection indicates no ancestor.
	 */
	Set<IComponent> getAllAncestors();
	
	/**
	 * @return parent of the current container.
	 */
	IComponent getParent();
	
	/**
	 * Check whether the current component is an ancestor of the specified component.
	 * @param comp specified component
	 * @return <code>true</code> indicates yes, <code>false</code> otherwise.
	 */
	boolean isAncestor(IComponent comp);
	
	/**
	 * Check whether the current component is a descendant of the specifed component. 
	 * @param comp specified component.
	 * @return <code>true</code> indicates yes, <code>false</code> otherwise.
	 */
	boolean isDescendant(IComponent comp);
	
	/**
	 * Check whether a sibling relationship between the current component and the 
	 * specified component.
	 * @param comp specified component
	 * @return <code>true</code> for yes; <code>false</code> for no.
	 */
	boolean isSiblingWith(IComponent comp);
	
	/**
	 * @return a set of components that are siblings of the current component. 
	 * 		Empty for none.
	 */
	Set<IComponent> getSiblings();
	
	/**
	 * Adds a child component and changes <code>comp</code>'s parent to the 
	 * current container.
	 * @param comp the child component
	 * @return current container that adds <code>comp</code> as its child.
	 */
	IComponent addComponent(IComponent comp);
	
	/**
	 * Remove a specified component from the current container.<br>
	 * <b>Note:</b>A container is only allowed to remove its direct children.
	 * @param compName name of the component to be removed
	 * @return the current contaienr
	 * @see #removeComponent(IComponent)
	 */
	IComponent removeComponent(String compName);
	
	/**
	 * Remove a specified component from the current container.<br>
	 * <b>Note</b>, remove a component will delete all black and white list declarations
	 * of and upon the component. 
	 * @param comp the specified component to be removed
	 * @return the current contaienr
	 * @see #removeComponent(String)
	 */
	IComponent removeComponent(IComponent comp);
	
	/**
	 * Retrieve a child component in terms of its name.
	 * @param name of the child component
	 * @return the specified child component
	 */
	IComponent getComponent(String name);
	
	/**
	 * Get all child components of the current component if has.
	 * @return a collection of child components if has, otherwise, 
	 * an empty collection will be returned.
	 */
	Collection<IComponent> getComponents();
	

	/**
	 * If not set on the current container, it will delegate to its parent if has.
	 * The default value is <code>false</code>.
	 * @return whether strict layer property is enforced for the component.
	 * @see #isStrictLayer(boolean)
	 */
	boolean isStrictLayer();
	
	/**
	 * @param strictLayer set whether is strict for its nested layers.
	 * @see #isStrictLayer()
	 */
	void isStrictLayer(boolean strictLayer);

}
