package person.zhoujg.component;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import jdepend.framework.JavaClass;
import jdepend.framework.JavaPackage;
import jdepend.framework.PackageFilter;
import person.zhoujg.component.internal.ObjectRegistry;
import person.zhoujg.component.internal.ProblemCollector;
import person.zhoujg.component.model.DeployableContainer;
import person.zhoujg.component.model.IComponent;
import person.zhoujg.component.model.ILayer;
import person.zhoujg.component.model.problem.AccessProblem;
import person.zhoujg.component.model.problem.DependencyProblem;
import person.zhoujg.component.model.problem.SplitPackageProblem;

/**
 * A <code>project</code> stands for the object to be analyzed. A project may 
 * include {@link IComponent} or {@link ILayer} objects. All these objects are 
 * needed to be connected to a <code>project</code> via directly invoking 
 * {@link ICompositeComponent#addComponent(IComponent)} on the <code>project</code> 
 * or other {@link ICompositeComponent} objects that are already or will be 
 * connected to the <code>project</code> later in a similar way, otherwise, 
 * it won't be analyzed. 
 * 
 * @author zhoujg
 * @date 2014年1月21日
 */
public class Project {	
	
	private boolean allowSplitPackage = false;
	
	private ObjectRegistry registry = null;
	private ProblemCollector problemCollector = null;
	
	private String name;
	private boolean strictLayer = false;
	
	public Project(String name){
		this.name = name;	
		
		registry = new ObjectRegistry();
		problemCollector = new ProblemCollector(registry);		
	}
	
	public String getName(){
		return name;
	}

	public void setName(String name){
		this.name = name;
	}

	//********************analyse result********************/
	
	public Collection<DependencyProblem> analyze() {		
		registry.analyze();
		return problemCollector.getDependencyProblems();
	}
	
	public Collection<DependencyProblem> getDependencyblems() {
		return problemCollector.getDependencyProblems();
	}
	
	public Collection<AccessProblem> getInBlackListProblems() {
		return problemCollector.getInBlackListProblems();
	}
	
	public Collection<AccessProblem> getInvisibleProblems(){
		return problemCollector.getInvisbleProblems();
	}
	
	public Collection<AccessProblem> getNonInterfaceProblems(){
		return problemCollector.getNonInterfaceProblems();
	}
	
	public Collection<SplitPackageProblem> getSplitPackageProblems(){
		return problemCollector.getSplitPackageProblems();
	}
	
	public Collection<DeployableContainer> getContainers() {
		return registry.getContainers();
	}
	
	/**
	 * @return <code>null</code> for <code>Project</code> object.
	 */
	public IComponent getParent(){
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IComponent getComponent(String name){
		Collection<IComponent> children = registry.getProjectComponents();
		if (children.contains(registry.getComponent(name))){
			return registry.getComponent(name);
		}
		return null;
	}
	
	public Collection<JavaClass> getJavaClasses(){
		return registry.getProjectJavaClasses();
	}
	
	public JavaClass getJavaClass(String name){
		return registry.getProjectJavaClass(name);
	}
	//*************************end******************************/
	
	//**********************conf segment************************/	
	
	public void setPackageFilter(PackageFilter filter) {
		registry.setPackageFilter(filter);
	}
	
	public PackageFilter getPackageFilter(){
		return registry.getPackageFilter();
	}

	public void addClassPath(String... path) throws IOException {		
		registry.addClassPath(path);
	}
	
	public Collection<String> getClassPath(){
		return registry.getClassPath();
	}
		

	public Project removeComponent(IComponent comp){		
		registry.removeComponent(comp);
		return this;
	}

	public Project removeComponent(String comp){		
		registry.removeComponent(getComponent(comp));
		return this;
	}

	public JavaPackage getJavaPackage(String name) {
		return registry.getProjectJavaPackage(name);
	}

	/**
	 * @return all analysed packages of the project. 
	 */
	public Set<JavaPackage> getJavaPackages() {
		return registry.getProjectJavaPackages();
	}

	public void isStrictLayer(boolean strictLayer) {
		this.strictLayer = strictLayer;
	}

	public boolean isStrictLayer() {
		return strictLayer;
	}

	public Project addComponent(IComponent comp) {
//		((Component)comp).setParent(this);
		registry.addComponent(comp);
		return this;
	}


	public Collection<IComponent> getComponents() {
		return registry.getProjectComponents();
	}

	/**
	 * @return all components under the project.
	 */
	public Collection<IComponent> getAllComponents(){
		return registry.getAllComponents();
	}
	
	public ObjectRegistry getObjectRegistry(){
		return registry;
	}
}
