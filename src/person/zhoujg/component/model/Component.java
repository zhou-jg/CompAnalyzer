package person.zhoujg.component.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import person.zhoujg.component.Project;
import person.zhoujg.util.FileUtil;

/**
 * A default implementation of {@link IComponent}.<br>
 * In the current implementation, the priority of the black list is higher 
 * than the white list, i.e., if a component is in both the black list and 
 * the white list, it will be considered in the black list for computation.  
 * 
 * @author zhoujg
 * @date 01/20/2014
 */
public class Component extends PackageSet implements IComponent {

	protected HashMap<String, IComponent> children;
	protected IComponent parent;
	//only contains self declared components
	protected HashMap<String, Set<IComponent>> whiteList;
	protected HashMap<String, Set<IComponent>> blackList;
	protected HashMap<String, Set<String>> interfaces;
	
	private HashSet<IComponent> dependencies;
	private HashSet<IComponent> dependants;
	
	protected Boolean strictLayer = null;
	private boolean sealed = false;
	
	public Component(String name) {
		this(null, name);
	}
	
	public Component(IComponent comp, String name) {
		super(name);
		this.parent	= comp;
		children	= new HashMap<String, IComponent>();
		whiteList	= new HashMap<String, Set<IComponent>>();
		blackList	= new HashMap<String, Set<IComponent>>();		
		dependants	= new HashSet<IComponent>();
		dependencies= new HashSet<IComponent>();
		interfaces	= new HashMap<String, Set<String>>();
	}
	
	public void setName(String name){
		this.name = name;
	}
	/**
	 * {@inheritDoc}
	 * <br><b>Note:</b> this operation will change the <code>parent</code>
	 * attribute of the <code>comp</code> object. 
	 */
	@Override
	public IComponent addComponent(IComponent comp) {
		if (comp.getParent() != null){
			comp.getParent().removeComponent(comp);
		}
		children.put(comp.getName(), comp);
		
		((Component)comp).setParent(this);
		
		return this;
	}

	/**{@inheritDoc} */
	@Override
	public IComponent getComponent(String name) {
		return children.get(name);
	}

	/**{@inheritDoc} */
	@Override
	public IComponent getParent() {
		return parent;
	}

	/**{@inheritDoc} 
	 * @see #isStrictLayer(boolean)
	 */
	@Override
	public boolean isStrictLayer() {
		if (strictLayer == null){
			return getParent().isStrictLayer();
		}
		return strictLayer;
	}

	/**{@inheritDoc} */
	@Override
	public void isStrictLayer(boolean strictLayer) {
		this.strictLayer = strictLayer;
	}

	/** 
	 * {@inheritDoc} <br>
	 * <b>Note:</b> if a component is in both <b>white</b> and <b>black</b>
	 * list of another component, it will be determined by the last method 
	 * ({@link #addsToWhiteList(IComponent)} or {@link #addsToBlackList(IComponent)} )
	 * invoked.<br>
	 * Also, we use a less strcit processing way for <code>white</code> or 
	 * <code>black list</code> for the component, i.e., we allow anther component 
	 * which is not in both the black and white lists of the current component to 
	 * access the current component. But, if a component only has white list, then
	 * the component not in the white list is not allowed to access the component. <br>
	 * In addition, a view must be exist to allow such operation, otherwise, no operation
	 * performed on that view.
	 * 
	 * @see #addsToBlackList(IComponent, String...)
	 */
	@Override
	public IComponent addsToWhiteList(IComponent comp, String... views) {		
		if (isSiblingWith(comp)){
			String[] vSet = (views == null || views.length == 0)? 
						interfaces.keySet().toArray(new String[interfaces.keySet().size()]) : views;		
			if (vSet.length == 0){
				vSet = new String[1];
				vSet[0] = "default";
				interfaces.put("default", new HashSet<String>());
			}
			for (String view : vSet){
				if (interfaces.containsKey(view)){
					Set<IComponent> list = whiteList.get(view);
					if (list == null){
						list = new HashSet<IComponent>();
						whiteList.put(view, list);
					}
					list.add(comp);//add to specified white list.
					Set<IComponent> bList = blackList.get(view);
					if (bList != null){		
						bList.remove(comp);	
					}
				}
			}			
		}
		
		return this;
	}

	/** 
	 * {@inheritDoc} <br>
	 * <b>Note:</b> if a component is in both <b>white</b> and <b>black</b>
	 * list of another component, it will be determined by the last method 
	 * ({@link #addsToWhiteList(IComponent)} or {@link #addsToBlackList(IComponent)} )
	 * invoked.
	 * @see #addsToWhiteList(IComponent)
	 */
	@Override
	public IComponent addsToBlackList(IComponent comp, String... views) {
		if (isSiblingWith(comp)){
			String[] vSet = (views == null || views.length == 0)? 
					interfaces.keySet().toArray(new String[interfaces.keySet().size()]) : views;
			if (vSet.length == 0){
				vSet = new String[1];
				vSet[0] = "default";
				interfaces.put("default", new HashSet<String>());
			}
			for (String view : vSet){
				if (interfaces.containsKey(view)){
					Set<IComponent> set = blackList.get(view);
					if (set == null){
						set = new HashSet<IComponent>();
						blackList.put(view, set);
					}
					set.add(comp);
					Set<IComponent> wSet = whiteList.get(view);
					if (wSet != null){
						wSet.remove(comp);
					}
				}
			}
						
		}
		
		return this;
	}

	/**{@inheritDoc} */
	@Override
	public IComponent addsToInterface(String... paths) {
		//We defer the checking whether a path in the scope of the current component
		//to the analysis phase.
		if (interfaces.keySet().size() == 0){
			interfaces.put("default", new HashSet<String>());
		}
		for (Set<String> set : interfaces.values()){
			set.addAll(Arrays.asList(paths));
		}
		
		return this;
	}
	
	/**{@inheritDoc}*/
	@Override
	public Set<String> getInterfaceViews(){
		return interfaces.keySet();
	}

	/**{@inheritDoc} */
	@Override
	public Set<IComponent> getDependencies() {
		return dependencies;
	}

	/**{@inheritDoc} */
	@Override
	public Set<IComponent> getDependants() {
		return dependants;
	}
	
	void setParent(IComponent comp) {
		this.parent = comp;
	}

	/**{@inheritDoc} */
	@Override
	public Set<String> getInterface(String... views) {
		String[] vSet = views.length == 0? 
				interfaces.keySet().toArray(new String[interfaces.keySet().size()]) : views;
		Set<String> apis = new HashSet<String>();
		
		for (String view : vSet){
			if (interfaces.containsKey(view)){
				apis.addAll(interfaces.get(view));
			}
		}
		return apis;
	}

	/**{@inheritDoc} */
	@Override
	public Set<IComponent> getComponents() {
		return new HashSet<IComponent>(children.values());
	}
	
	/**
	 * Only for internal use.
	 * @param comp
	 */
	public void addDependency(IComponent comp) {
		dependencies.add(comp);
	}
	
	/**
	 * Only for internal use.
	 * @param comp
	 */
	public void addDependant(IComponent comp) {
		dependants.add(comp);
	}
	
	/**{@inheritDoc}*/
	@Override
	public boolean isVisibleTo(IComponent comp){
		if (isSiblingWith(comp)){
			return true;		
		}else {
			IComponent parent = comp.getParent();
			if (parent != null) {
				if (isSiblingWith(parent)){
					return true;
				}else {
					parent = parent.getParent();
				}
			}
			return false;
		}
	}

	/**{@inheritDoc} 
	 */
	@Override
	public Set<IComponent> getWhiteList(String... views) {
		String[] vSet = (views == null || views.length == 0)? 
				interfaces.keySet().toArray(new String[interfaces.keySet().size()]) : views;
		Set<IComponent> list = new HashSet<IComponent>();
		for (String view : vSet){
			Set<IComponent> wList = whiteList.get(view);
			if (wList != null){
				Iterator<IComponent> it = wList.iterator();			
				while (it.hasNext()){
					IComponent comp = it.next();
					//we must esure the comp is a sibling (may change at any time) at the time.
					if (!isSiblingWith(comp)){					
						it.remove();
					}
				}
				
				list.addAll(wList);
			}
		}
		return list;
	}

	/**{@inheritDoc}*/
	@Override
	public Set<IComponent> getBlackList(String... views) {
		String[] vSet = (views == null || views.length == 0)? 
				interfaces.keySet().toArray(new String[interfaces.keySet().size()]) : views;
		Set<IComponent> list = new HashSet<IComponent>();
		for (String view : vSet){
			Set<IComponent> bList = blackList.get(view);
			if (bList != null){
				Iterator<IComponent> it = bList.iterator();
				while (it.hasNext()){
					IComponent comp = it.next();
					//we must esure the comp is a sibling (may change at any time) at the time.
					if (!isSiblingWith(comp)){
						it.remove();
					}
				}
				
				list.addAll(bList);//add sibling in B
			}			
		}
		return list;
	}
	
	/**{@inheritDoc}*/
	@Override
	public boolean isInBlackList(IComponent comp, String... views) {
		String[] vSet = (views == null || views.length == 0)? 
				interfaces.keySet().toArray(new String[interfaces.keySet().size()]) : views;
		for (String view : vSet){
			if (!getBlackList(view).contains(comp)){
				return false;
			}
		}
		return true;
	}

	/**{@inheritDoc}*/
	@Override
	public boolean isInWhiteList(IComponent comp, String... views) {
		String[] vSet = (views == null || views.length == 0)? 
				interfaces.keySet().toArray(new String[interfaces.keySet().size()]) : views;
		for (String view : vSet){
			if (!getWhiteList(view).contains(comp)){
				return false;
			}
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public void isSealed(boolean sealed) {
		this.sealed = sealed;		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Deprecated
	public boolean isSealed() {
		return sealed;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IComponent removeComponent(String compName) {
		IComponent child = children.get(compName);
		if (child != null){
			for (IComponent sibling : child.getSiblings()){
				sibling.removeFromBlackList(child);
				sibling.removeFromWhiteList(child);
				child.removeFromBlackList(sibling);
				child.removeFromWhiteList(sibling);
			}
			((Component)child).setParent(null);
			children.remove(compName);			
		}		
		
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IComponent removeComponent(IComponent comp) {
		return removeComponent(comp.getName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<IComponent> getAllDescendants() {	
		Set<IComponent> set = new HashSet<IComponent>();
		
		set.addAll(getComponents());
		for (IComponent comp : getComponents()){
			set.addAll(comp.getAllDescendants());
		}
		
		return set;
	}

	/**{@inheritDoc}*/
	@Override
	public boolean isAncestor(IComponent comp) {
		IComponent parent = comp.getParent();
		while (parent != null && parent != this && !(parent instanceof Project)){
			parent = ((IComponent)parent).getParent();
		}
		if (parent == null) {
			return false;
		}else{
			return true;
		}
	}

	/**{@inheritDoc}*/
	@Override
	public boolean isDescendant(IComponent comp) {
		return comp.isAncestor(this);
	}

	/**{@inheritDoc}*/
	@Override
	public Set<IComponent> getAllAncestors() {
		Set<IComponent> ancestors = new HashSet<IComponent>();
		IComponent ancestor = getParent();
		while (ancestor != null && !(ancestor instanceof Project)){
			ancestors.add((IComponent)ancestor);
			ancestor = ((IComponent)ancestor).getParent();
		}
		return ancestors;
	}

	/**{@inheritDoc}*/
	@Override
	public boolean isSiblingWith(IComponent comp) {
		if (comp == null){
			return false;
		}else if (getParent() == null){
			return comp.getParent() == null;
		}else {
			return this.getParent() == comp.getParent();
		}		
	}

	/**{@inheritDoc}*/
	@Override
	public Set<IComponent> getSiblings() {
		Set<IComponent> siblings = new HashSet<IComponent>();
		if (getParent() != null){
			siblings = new HashSet<IComponent>(getParent().getComponents());
			siblings.remove(this);
		}
		return siblings;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IComponent removeFromWhiteList(IComponent comp, String... views) {
		String[] vSet = (views == null || views.length == 0)? 
				interfaces.keySet().toArray(new String[interfaces.keySet().size()]) : views;
		for (String view : vSet){
			Set<IComponent> list = whiteList.get(view);
			if (list != null){
				list.remove(comp);
			}
		}			
		
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IComponent removeFromBlackList(IComponent comp, String... views) {
		String[] vSet = (views == null || views.length == 0)? 
				interfaces.keySet().toArray(new String[interfaces.keySet().size()]) : views;
		for (String view : vSet){
			Set<IComponent> list = blackList.get(view);
			if (list != null){
				list.remove(comp);
			}
		}		

		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	//TODO 需要处理在已有的包接口中去掉某个类的情况，即需要额外记录这样的信息,可以用一个类集合记录指定的非接口类。
	public IComponent removeFromInterface(String... apis) {
		for (Set<String> set : interfaces.values()){
			set.removeAll(Arrays.asList(apis));
		}
		
		return this;
	}
	
	/**{@inheritDoc}*/
	@Override
	public IComponent removeFromInterface(String viewId, String[] apis){
		Set<String> list = interfaces.get(viewId);
		if (list != null){
			list.removeAll(Arrays.asList(apis));			
		}
		
		return this;
	}

	/**{@inheritDoc}*/
	@Override
	public Collection<IComponent> getAfferents() {
		return getDependants();
	}

	/**{@inheritDoc}*/
	@Override
	public Collection<IComponent> getEfferents() {
		return getDependencies();
	}

	/**{@inheritDoc}*/
	@Override
	public IComponent addsToInterface(String viewID, String[] paths) {
		Set<String> apis = interfaces.get(viewID);
		if (apis == null){
			apis = new HashSet<String>();
			interfaces.put(viewID, apis);
		}
		apis.addAll(Arrays.asList(paths));
		
		return this;
	}

	/**{@inheritDoc}*/
	@Override
	public String getView(String api) {
		for (String viewId : interfaces.keySet()){
			Set<String> apis = interfaces.get(viewId);
			if (apis.contains(api)){
				return viewId;
			}
		}
		if (Character.isUpperCase(FileUtil.getLastSegment(api).charAt(0))){// api is a class
			if (api.indexOf('.') > 0){
				//get package name of api to re-check
				String pack = api.substring(0, api.lastIndexOf('.'));
				for (String viewId : interfaces.keySet()){
					Set<String> apis = interfaces.get(viewId);
					if (apis.contains(pack)){
						return viewId;
					}
				}
			}
		}else{//api is a package
			//TODO if a package not specified, but all its contained classes are interface
			//thus the package is an interface. This checking only valid after class parsing.
		}
		return null;
	}

	/**{@inheritDoc}*/
	@Override
	public IComponent clearWhiteList(String... views) {
		String[] vSet = (views == null || views.length == 0)? 
				interfaces.keySet().toArray(new String[interfaces.keySet().size()]) : views;
		for (String view : vSet){
			whiteList.remove(view);
		}
		
		
		return this;
	}

	/**{@inheritDoc}*/
	@Override
	public IComponent clearBlackList(String... views) {
		String[] vSet = (views == null || views.length == 0)? 
				interfaces.keySet().toArray(new String[interfaces.keySet().size()]) : views;
		for (String view : vSet){
			blackList.remove(view);
		}
		
		
		return this;
	}

	/**{@inheritDoc}*/
	@Override
	public IComponent clearInterface(String... views) {
		String[] vSet = (views == null || views.length == 0)? 
				interfaces.keySet().toArray(new String[interfaces.keySet().size()]) : views;
		for (String view : vSet){
			interfaces.remove(view);
			blackList.remove(view);
			whiteList.remove(view);
		}
		
		
		return this;
	}

	/**{@inheritDoc} */
	@Override
	public boolean isDependency(IDependableEntity obj) {
		if (obj instanceof IComponent) {
			return dependencies.contains(obj);
		}
		return false;
	}

}
