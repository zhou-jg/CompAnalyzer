package person.zhoujg.component.internal;

import static person.zhoujg.component.model.util.ModelCalculator.getDependantClassesBetween;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdepend.framework.JavaClass;
import jdepend.framework.JavaPackage;
import person.zhoujg.component.model.DeployableContainer;
import person.zhoujg.component.model.IComponent;
import person.zhoujg.component.model.ILayer;
import person.zhoujg.component.model.problem.AccessProblem;
import person.zhoujg.component.model.problem.DependencyProblem;
import person.zhoujg.component.model.problem.SplitPackageProblem;
import person.zhoujg.component.model.util.ModelCalculator;

/**
 * TODO add warnings for 
 * <li> components that not in both white list and black list but accesses some 
 * 			other components, And 
 * <li> package declaration conflict with analyzed packages, i.e., not found 
 * 		declared packages in the packages analyzed.
 *   
 * @author zhoujg
 * @date 2014/02/26
 *
 */
public class ProblemCollector{
	private ObjectRegistry objectRegistry = null;
	//computed information
	private Set<AccessProblem> inBlackList = new HashSet<AccessProblem>();
	private Set<AccessProblem> notInWhiteList = new HashSet<AccessProblem>();
	private Set<AccessProblem> nonInterface = new HashSet<AccessProblem>();
	private Set<AccessProblem> nonInBlackAndWhite = new HashSet<AccessProblem>();
	private Set<SplitPackageProblem> splitPacks = new HashSet<SplitPackageProblem>();
	
	//store layer type black list for layers to process black list problems in a uniform way.
	private Map<IComponent, Set<IComponent>> blackListForLayers = new HashMap<IComponent, Set<IComponent>>();
	
	private int circleFilter = 0;
	private boolean strictLayer = false;
	private boolean needToRefresh = true;
	
	public ProblemCollector(ObjectRegistry objectRegistry){
		this.objectRegistry = objectRegistry;
	}
	
	public void setCircleFilter(int filter) {
		this.circleFilter = filter;
	}
	
	public int getCircleFilter(){
		return circleFilter;
	}
	
	public boolean isStrictLayer(){
		return strictLayer;
	}
	
	public void isStrictLayer(boolean strictLayer){
		this.strictLayer = strictLayer;
	}
	
	public ObjectRegistry getObjectRegistry(){
		return objectRegistry;
	}

	@SuppressWarnings(value={"unchecked", "rawtypes"})
	private void addSubSetElementInMap(Map map, Object key, Object value){
		Set set = (Set)map.get(key);
		if (set == null){
			set = new HashSet();
			map.put(key, set);
		}
		set.add(value);		
	}

	private void collectProblems() {
		reset();
		
		computeAccessProblems();
		computeSplitPackageProblems();
		
		needToRefresh(false);
	}

	private void reset() {
		inBlackList.clear();
		notInWhiteList.clear();
		nonInterface.clear();
		nonInBlackAndWhite.clear();
		splitPacks.clear();
		blackListForLayers.clear();
		
		buildBlackListForLayers();
	}

	
	private void buildBlackListForLayers() {
		buildBlackListForLayers(getObjectRegistry().getProjectComponents(), isStrictLayer());
	}
	
	private void buildBlackListForLayers(Collection<IComponent> components, boolean strictLayer) {
		int count = components.size();
		if (count == 0){
			return;
		}
		IComponent[] comps = new IComponent[count];
		components.toArray(comps);
		
		for (int i = 0; i < count-1; i++){
			IComponent comp = comps[i];
			if (comp instanceof ILayer){
				int compLevel = ((ILayer)comp).getLevel();
				for (int j = i+1; j < count; j++){
					IComponent other = comps[j];
					if (other instanceof ILayer){
						int otherLevel = ((ILayer)other).getLevel();
						int dis = compLevel - otherLevel;
						if (dis > 0){
							addSubSetElementInMap(blackListForLayers, other, comp);								
						}else if (dis < -1 && strictLayer){
							addSubSetElementInMap(blackListForLayers, comp, other);
						}							
					}
				}
			}
		}
		for (IComponent comp : components){
			buildBlackListForLayers(comp.getComponents(), comp.isStrictLayer());
		}
	}

	
	public Collection<DependencyProblem> getDependencyProblems() {
		preprocessing();
		List<DependencyProblem> dependencyProblems = new ArrayList<DependencyProblem>();
		dependencyProblems.addAll(inBlackList);
		dependencyProblems.addAll(notInWhiteList);
		dependencyProblems.addAll(nonInterface);
		dependencyProblems.addAll(nonInBlackAndWhite);
		dependencyProblems.addAll(splitPacks);
		
		return dependencyProblems;
	}
	
	public Collection<AccessProblem> getInBlackListProblems(){
		preprocessing();
		return inBlackList;
	}
	
	public Collection<AccessProblem> getInvisbleProblems() {
		preprocessing();
		return notInWhiteList;
	}
	
	public Collection<AccessProblem> getNonInterfaceProblems(){
		preprocessing();
		return nonInterface;
	}
	
	public Collection<AccessProblem> getNonInBlackAndWhiteProblems(){
		preprocessing();
		return nonInBlackAndWhite;
	}
	
	public Collection<SplitPackageProblem> getSplitPackageProblems(){
		preprocessing();
		return splitPacks;
	}	
	
	public boolean isSplitPackage(JavaPackage pack){
		for (SplitPackageProblem p : splitPacks){
			if (p.getJavaPackage().equals(pack)){
				return true;
			}
		}
		return false;
	}
	
	private void computeSplitPackageProblems() {
		for (JavaPackage p : getObjectRegistry().getProjectJavaPackages()){
			SplitPackageProblem problem = new SplitPackageProblem(p);
			for (DeployableContainer con : getObjectRegistry().getContainers()) {
				if (con.getPackages().contains(p)) {
					problem.addInvolvedContainer(con);
				}
			}
			if (problem.getInvolvedContainers().size() > 1) {
				splitPacks.add(problem);
			}
		}
	}

	/**
	 * TODO a more effiecient algorithm. 
	 */
	private void computeAccessProblems() {
		for (IComponent comp : getObjectRegistry().getAllComponents()){
//			computeAccessProblems(comp);
			computeAccessProblemsNew(comp);
		}
	}
	
	private void computeAccessProblemsNew(IComponent comp){

		AccessProblem problem = null;		
		
		for (IComponent client : comp.getDependants()){	
			IComponent dependee = null;
			if (comp.isVisibleTo(client)){
				dependee = comp;
			}else {
				dependee = ModelCalculator.getLowestVisibleAncestor(client, comp);
			}
						
			Map<JavaClass, Set<JavaClass>> refClasses = getDependantClassesBetween(comp, client);
			
			for (JavaClass clazz : refClasses.keySet()){				
				String viewId = dependee.getView(clazz.getName());
				if (viewId == null){
					String desc = clazz.getName() + " is not an interface on component[" + 
									dependee.getName() + "].";
					problem = new AccessProblem(comp, client, AccessProblem.NON_INTERFACE, desc);
					problem.setProblemElements(refClasses);
					nonInterface.add(problem);
				}else{
					if (dependee.getBlackList(viewId) == null){//no black list
						if (dependee.getWhiteList(viewId) != null){//has white list
							//not in the white list
							if (!ModelCalculator.isInTheScopeOfWhitelist(client, dependee, viewId)){
								String desc = client.getName() + " is not in the white list of component[" +
												dependee.getName() + "].";
								problem = new AccessProblem(client, comp, AccessProblem.NOT_IN_WHITELIST, desc);
								problem.setProblemElements(refClasses);
								notInWhiteList.add(problem);
							}
						}
					}else {//has black list
						if (ModelCalculator.isInTheScopeOfBlacklist(client, dependee, viewId)){
							String desc = client.getName() + " is in the black list of component[" +
										dependee.getName() + "].";
							problem = new AccessProblem(comp, client, AccessProblem.IN_BLACKLIST, desc);
							problem.setProblemElements(refClasses);
							inBlackList.add(problem);
						}else if (dependee.getWhiteList(viewId) != null){//has white list
							if (!ModelCalculator.isInTheScopeOfWhitelist(client, dependee, viewId)){
								String desc = client.getName() + 
										" is neither in the black list, nor the white list of component[" +
										dependee.getName() + "].";
								problem = new AccessProblem(comp, client, AccessProblem.NON_DETERMINISTIC, desc);
								problem.setProblemElements(refClasses);
								nonInBlackAndWhite.add(problem);
							}
						}
					}
				}
			}
		}
	}
	

	private void needToRefresh(boolean flag){
		needToRefresh = flag;
	}

		
	private void preprocessing() {
		if (needToRefresh){
			collectProblems();
		}
	}

}
