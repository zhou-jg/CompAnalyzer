package person.zhoujg.component.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdepend.framework.JavaClass;
import jdepend.framework.JavaPackage;
import person.zhoujg.component.Project;
import person.zhoujg.component.model.DeployableContainer;
import person.zhoujg.component.model.IComponent;
import person.zhoujg.component.model.problem.AccessProblem;
import person.zhoujg.component.model.problem.SplitPackageProblem;
import person.zhoujg.util.CollectionUtil;

public class ModelCalculator {
	
	/**
	 * Get a sibling pair of components that contains the specified components 
	 * respectively.
	 * @param comp1
	 * @param comp2
	 * @return a {@link CompPair} object of which the <code>left</code> is the 
	 * 			ancestor of the {@literal comp1} or itself; and the <code>right</code>
	 * 			is the ancestor of the {@literal comp2} or itself.
	 */
	public static CompPair getSiblingPair(IComponent comp1, IComponent comp2){
		if (comp1.isSiblingWith(comp2)) {
			return new CompPair(comp1, comp2);
		}else if (comp1.isAncestor(comp2)){
			return new CompPair(comp1, comp2);
		}else if (comp1.isDescendant(comp2)){
			return new CompPair(comp2, comp2);
		}else if (getDepth(comp1) == getDepth(comp2) && !(comp1.getParent() == null)){
			return getSiblingPair((IComponent)comp1.getParent(), (IComponent)comp2.getParent());
		}else{
			IComponent newLeft 	= comp1;
			IComponent newRight = comp2;
			int lDep = getDepth(comp1);
			int rDep = getDepth(comp2);
			if (lDep > rDep){				
				for (int i=lDep; i>rDep; i--){
					if (!(newLeft.getParent() == null)){
						newLeft = (IComponent)newLeft.getParent();
					}
				}
				
			}else{
				for (int i=rDep; i>lDep; i--){
					if (!(newRight.getParent() == null)){
						newRight = (IComponent) newRight.getParent();
					}
				}
			}
			return getSiblingPair(newLeft, newRight);
		}
	}
	
	/**
	 * Get depth for the specified component from the root.
	 * @param comp
	 * @return
	 */
	public static int getDepth(IComponent comp){
		int depth = 1;
		IComponent parent = comp.getParent();
		while (parent != null){
			depth++;
			parent = ((IComponent)parent).getParent();
		}
		return depth;
	}
	/**
	 * Get height for the specified project, which equals the heightest component.
	 * @param project the project
	 * @return project height; 0 for none components.
	 */
	public static int getHeight(Project project){
		int height = 0;
		for (IComponent child : project.getComponents()){
			int childHeight = getHeight(child);
			if (childHeight > height){
				height = childHeight;
			}
		}
		return height;
	}
	/**
	 * Get height for the specified component
	 * @param comp the component
	 * @return component height; 1 for leaf component.
	 */
	public static int getHeight(IComponent comp){
		if (comp.getComponents().size() == 0){
			return 1;
		}else {
			return heightestChild(comp) + 1;
		}
	}
	
	private static int heightestChild(IComponent parent){
		int height = 0;
		for (IComponent child : parent.getComponents()){
			int childHeight = getHeight(child);
			if (childHeight > height){
				height = childHeight;
			}
		}
		return height;
	}
	
	/**
	 * Judge whether a specified class is an API class on the specified component level.
	 * @param c specified <code>JavaClass</code> object.
	 * @param comp specified <code>IComponent</code> object
	 * @return <code>true</code> indicates the class is an API of the specified component, 
	 * 			<code>false</code> otherwise.
	 */
	public static boolean isAPIClass(JavaClass c, IComponent comp){
		return CollectionUtil.containsOne(comp.getInterface(), c.getPackageName(), c.getName());
	}

	/**
	 * Get all {@link JavaPackage} objects under the scope of the specified component.
	 * @param comp specified component.
	 * @return a collection of {@link JavaPackage} objects.
	 */
	public static Collection<JavaPackage> getAllJavaPackagesUnder(IComponent comp){
		HashSet<JavaPackage> packs = new HashSet<JavaPackage>();
		packs.addAll(comp.getJavaPackages());
		for (IComponent child : comp.getComponents()){
			packs.addAll(getAllJavaPackagesUnder(child));
		}
		return packs;
	}
	
	/**
	 * @param comp specified component
	 * @return a collection of package names that under the scope of the specified component.
	 */
	public static Collection<String> getAllPackagesUnder(IComponent comp){
		HashSet<String> packs = new HashSet<String>();
		packs.addAll(comp.getPackages());
		for (IComponent child : comp.getComponents()){
			packs.addAll(getAllPackagesUnder(child));
		}
		return packs;
	}
	
	/**
	 * Judge whether the specified class is in the scope of the specified component.
	 * @param c specified class
	 * @param comp specified component
	 * @param cascade search in child components if has
	 * @return <code>true</code> for yes; <code>false</code> otherwise. 
	 */
	public static boolean isClassUnderComponent(JavaClass c, IComponent comp, boolean cascade){		
		if (comp.getPackages().contains(c.getPackageName())){
			return true;
		}else if (cascade){
			for (IComponent child : comp.getComponents()){
				if (isClassUnderComponent(c, child, true)){
					return true;
				}
			}			
		}
		return false;
	}

	/**
	 * Get classes that are in the specified component and access the specified class.  
	 * @param dependee specified class.
	 * @param comp specified component
	 * @return a set of <code>JavaClass</code> objects that depend on the specified class.
	 */
	public static Set<JavaClass> getDependant(JavaClass dependee, IComponent comp){
		Set<JavaClass> set = new HashSet<JavaClass>();
		for (JavaClass c : getJavaClasses(comp)){
			if (c.isDependency(dependee)){
				set.add(c);
			}
		}
		return set;
	}
	
	/**
	 * Reture self contained classes of the specified component.
	 * @param comp the specified component.
	 * @return
	 */
	public static Collection<JavaClass> getJavaClasses(IComponent comp) {
		HashSet<JavaClass> clases = new HashSet<JavaClass>();
		for (JavaPackage pack : comp.getJavaPackages()){
			clases.addAll(pack.getClasses());
		}
		return clases;
	}
	
	public static boolean isClassUnderSamePackageHierachy(Collection<JavaClass> elements) {
		HashSet<String> temp = new HashSet<String>();
		for (JavaClass c : elements){
			temp.add(c.getPackageName());
		}

		return isPackageUnderSamePackageHierachy(temp);
	}
	
	public static boolean isPackageUnderSamePackageHierachy(Set<String> elements) {
		List<String> temp = new ArrayList<String>(elements);
		Collections.sort(temp);
		for (int i = temp.size()-1; i > 0; i--){
			if (!temp.get(i).startsWith(temp.get(i-1))) {
				return false;
			}
		}

		return true;
	}
	
	
	public static boolean isPackageUnderSamePackageHierachy(Collection<JavaPackage> elements) {
		HashSet<String> temp = new HashSet<String>();
		for (JavaPackage pack : elements){
			temp.add(pack.getName());
		}
		return isPackageUnderSamePackageHierachy(temp);
	}

	public static boolean isClassInSamePackage(Collection<JavaClass> elements) {
		HashSet<String> temp = new HashSet<String>();
		for (JavaClass c : elements){
			temp.add(c.getPackageName());
			if (temp.size() > 1) {
				return false;
			}
		}
		return true;		
	}
	
	/**
	 * Get the accessing classes in the <code>dependant</code> that access classes (keys in 
	 * the returned Map) in the <code>dependee</code>.
	 * @param dependee
	 * @param dependant
	 * @return Map 
	 */
	public static Map<JavaClass, Set<JavaClass>> getDependantClassesBetween(IComponent dependee, IComponent dependant){
		Map<JavaClass, Set<JavaClass>> map = new HashMap<JavaClass, Set<JavaClass>>();
		for (JavaClass c : getJavaClasses(dependee)){
			Set<JavaClass> set = getDependant(c, dependant);
			if (set.size() > 0){
				map.put(c, set);
			}
		}
		return map;
	}
	
	public static Object getOwner(JavaPackage pack, Project pro){
		for (IComponent comp : pro.getAllComponents()){
			if (comp.getJavaPackages().contains(pack)){
				return comp;
			}
		}
		return pro;
	}
	
	public static boolean isOriginalAPIInComponent(String api, IComponent comp){
		if (comp.getPackages().contains(api)){
			return true;
		}
		char firstLetterOnLastSegment = api.charAt(api.lastIndexOf(".")+1);
		if (Character.isUpperCase(firstLetterOnLastSegment)){
			String packSegment = api.substring(0, api.lastIndexOf("."));
			if (comp.getPackages().contains(packSegment)){
				return true;
			}			
		}
		return false;
	}
	
	/**
	 * Get a detailed dependency info.
	 * @param dependant
	 * @param dependee
	 * @param depandantCascade check children of dependant
	 * @param dependeeCascade check children of dependee
	 * @return -1: no dependency from <code>dependant</code> to <code>dependee</code> and its children;
	 * 			0: <code>dependant</code> dependens <code>dependee</code> directly;
	 * 			1: <code>dependant</code> dependens children of <code>dependee</code>, this only valid without 
	 * 			direct dependency; 
	 * 			2: some children of <code>dependant</code> depend <code>dependee</code> directly;
	 * 			3: some children of <code>dependant</code> depend some children of <code>dependee</code>.
	 */
	public static int getDependencyState(IComponent dependant, IComponent dependee, boolean depandantCascade, boolean dependeeCascade){
		if (dependant.getDependencies().contains(dependee)){
			return 0;
		}
	
		if (dependeeCascade){
			for (IComponent child : dependee.getComponents()){
				if (getDependencyState(dependant, child, depandantCascade, true) == 0){
					return 1;
				}
			}
		}
		
		if (depandantCascade){
			for (IComponent child : dependant.getComponents()){
				if (getDependencyState(child, dependee, true, dependeeCascade) == 0){
					return 2;
				}else if (getDependencyState(child, dependee, true, dependeeCascade) == 1){
					return 3;
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * Get class dependency info between two components.
	 * 
	 * @param dependant
	 * @param dependee
	 * @param dependantCascade check for child components
	 * @param dependeeCascade  check for child components
	 * @return a list of class pairs that left depends on right, which belong to dependant
	 * and dependee respectively.
	 */
	public static List<ClassPair> getDependencyInfoBetween(IComponent dependant, 
															IComponent dependee, 
															boolean dependantCascade, 
															boolean dependeeCascade){
		List<ClassPair> list = new ArrayList<ClassPair>();
		for (JavaPackage pack : dependant.getJavaPackages()){
			for (JavaClass clazz : pack.getClasses()){
				for (JavaClass other : clazz.getEfferents()){
					if (isClassUnderComponent(other, dependee, dependeeCascade)){
						list.add(new ClassPair(clazz, other));
					}
				}
			}
		}
		if (dependantCascade){
			for (IComponent child : dependant.getComponents()){
				list.addAll(getDependencyInfoBetween(child, dependee, true, dependeeCascade));
			}
		}
		
		return list;
	}
	
	//for split packages
	public static Collection<DeployableContainer> getInvolvedContainers(Project project, JavaPackage pack){		
		for (SplitPackageProblem pro : project.getSplitPackageProblems()){
			if (pro.getJavaPackage() == pack){
				return pro.getInvolvedContainers();
			}
		}
		return Collections.emptySet();
	}
	
	public static IComponent getComponentInProject(Project project, String name){
		return project.getObjectRegistry().getComponent(name);
	}
	
	public static Map<Integer, Integer> getComponentNumWithDepth(Project pro){
		Map<Integer, Integer> result = new HashMap<Integer, Integer>();
		for (IComponent comp : pro.getAllComponents()){
			int depth = getDepth(comp);
			if (result.get(depth) == null){
				result.put(depth, 1);
			}else {
				result.put(depth, result.get(depth) + 1);
			}
		}
		return result;
	}
	
	
	/**
	 * Return owner project of the specified component, <code>null</code> may be
	 * returned if the comp is not connected to a project.
	 * @param comp
	 * @return
	 */
	public static Project getProject (IComponent comp){
		IComponent parent = comp.getParent();
		while (parent != null && !(parent instanceof Project)){
			parent = ((IComponent)parent).getParent();
		}
		return (Project)parent;
	}
	
	public static IComponent getComponentOwner(JavaClass clazz, IComponent scope){
		if (scope.getPackages().contains(clazz.getPackageName())){
			return scope;
		}else {
			for (IComponent child : scope.getComponents()){
				if (getComponentOwner(clazz, child) != null){
					return child;
				}
			}
		}
		return null;
	}
	
	/**
	 * Check whether a pair of component has access problems in the specified problem collection and 
	 * return the access problem if has, otherwise <code>null</code>
	 * @param col
	 * @param dependant
	 * @param dependee
	 * @return
	 */
	public static AccessProblem contains (Collection<AccessProblem> col, IComponent dependant, IComponent dependee){
		for (AccessProblem pro : col){
			if (pro.getDependant() == dependant && pro.getDependee() == dependee){
				return pro;
			}
		}
		return null;
	}
	
	/**
	 * Get lowest ancestor of component (<code>toSee</code>) in component hierarchy that can be seen
	 * by component <code>seeing</code>.
	 * @param seeing
	 * @param toSee
	 * @return
	 */
	public static IComponent getLowestVisibleAncestor(IComponent seeing, IComponent toSee){
		if (toSee.isVisibleTo(seeing)){
			return toSee;
		}else {
			IComponent parent = toSee.getParent();
			if (parent == null){
				return null;
			}else {
				return getLowestVisibleAncestor(seeing, (IComponent)parent);
			}
		}
	}

	/**
	 * Check whether a client is under the scope of one of server's black list components.
	 * @param client
	 * @param server
	 * @param view
	 * @return
	 */
	public static boolean isInTheScopeOfBlacklist(IComponent client, IComponent server, String view){
		if (client.isInBlackList(server, view)){
			return true;
		}else{
			CompPair pair = getSiblingPair(client, server);
			if (pair.getLeft().isInBlackList(pair.getRight(), view)){
				return true;
			}else {
				return false;
			}
		}
	}
	
	/**
	 * 
	 * @param client
	 * @param server
	 * @param view
	 * @return
	 */
	public static boolean isInTheScopeOfWhitelist(IComponent client, IComponent server, String view){
		if (client.isInWhiteList(server, view)){
			return true;
		}else{
			CompPair pair = getSiblingPair(client, server);
			if (pair.getLeft().isInWhiteList(pair.getRight(), view)){
				return true;
			}else {
				return false;
			}
		}
	}
	
	/**
	 * Get the instability metrix of the specified component using the algorithm:<br>
	 * instability = Ao / (Ao + Ai) where:
	 * <li> <b>Ao</b> the number of classes inside the component use classes outside the component, 
	 * <li> <b>Ai</b> the number of classes outside the component use classes inside the component.
	 * @param comp target component
	 * @param self false (default) including children component if has, true otherwise.
	 * @return the instability metrix of the specified component. -1 means no interaction between 
	 * the component and its environment.
	 */
	public static float getInStability(IComponent comp, boolean self){
		int ao = getDependencyClasses(comp, self).size();
		int ai = getDependantClasses(comp, self).size();
		
		if (ao == 0 && ai ==0){
			return -1;
		}else {
			return (float) ao / (ao + ai);
		}
	}
		
	/**
	 * Get depended classes of the specified component.
	 * @param comp target component
	 * @param self true for self, false including chidren of the component
	 * @return
	 */
	public static Set<JavaClass> getDependencyClasses(IComponent comp, boolean self){
		Set<JavaClass> set = new HashSet<JavaClass>();
		Collection<JavaPackage> packs = self? comp.getJavaPackages() : getAllJavaPackagesUnder(comp);
		for (JavaPackage pack : packs){
			for (JavaClass clazz : pack.getClasses()){
				Collection<JavaClass> depends = clazz.getEfferents();
				for (JavaClass depend : depends){
					if (!isClassUnderComponent(depend, comp, !self)){
						set.add(depend);
					}
				}
			}
		}
		
		return set;
	}
	
	/**
	 * Get classes outside the component use classes inside the component
	 * @param comp target component 
	 * @param self whether to include children components if has.
	 * @return
	 */
	public static Set<JavaClass> getDependantClasses(IComponent comp, boolean self){
		Set<JavaClass> set = new HashSet<JavaClass>();
		Collection<JavaPackage> packs = self? comp.getJavaPackages() : getAllJavaPackagesUnder(comp);
		for (JavaPackage pack : packs){
			for (JavaPackage other : pack.getAfferents()){
				if (!packs.contains(other)){
					for (JavaClass clazz : other.getClasses()){
						Set<JavaPackage> temp = new HashSet<JavaPackage>(packs);
						temp.retainAll(clazz.getImportedPackages());
						if (temp.size() > 0){
							set.add(clazz);
						}
					}
				}
			}
		}
		
		return set;
	}
	
}
