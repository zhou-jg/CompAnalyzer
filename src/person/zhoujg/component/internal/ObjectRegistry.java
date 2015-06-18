package person.zhoujg.component.internal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jdepend.framework.ClassFileParser;
import jdepend.framework.FileManager;
import jdepend.framework.JavaClass;
import jdepend.framework.JavaClassBuilder;
import jdepend.framework.JavaPackage;
import jdepend.framework.PackageFilter;
import person.zhoujg.component.Project;
import person.zhoujg.component.model.Component;
import person.zhoujg.component.model.DeployableContainer;
import person.zhoujg.component.model.IComponent;
import person.zhoujg.component.model.ModelException;
import person.zhoujg.component.model.validation.BlackListConsistencyChecker;
import person.zhoujg.component.model.validation.InterfaceConsistencyChecker;
import person.zhoujg.component.model.validation.ModelValidator;
import person.zhoujg.component.model.validation.PackageConsistencyChecker;

/**
 * A worker class of {@link Project} used as a registry for all analysed classes, 
 * packages, components, and containers, as well as their dependency informations.
 * 
 * @author zhoujg
 * @date 2014年2月26日
 *
 */
public class ObjectRegistry {

	//base components info
	private Map<String, IComponent> projectComponents = new HashMap<String, IComponent>();
	//classes that need analyse
	private Map<String, JavaClass> focusedClasses = new HashMap<String, JavaClass>();
	//classes that do not need analyse
	private Map<String, JavaClass> ignoredClasses = new HashMap<String, JavaClass>();
	private Map<String, JavaPackage> focusedPackages = new HashMap<String, JavaPackage>();
	private Map<String, JavaPackage> ignoredPackages = new HashMap<String, JavaPackage>();
	private Map<String, DeployableContainer> containers = new HashMap<String, DeployableContainer>();
	private UnknownContainer ignoredContianer = null;
	
	private ClassFileParser parser = null;
	private JavaClassBuilder builder = null;
	private FileManager fileManager = null;
	private PackageFilter pakcageFilter = null;
	
	//validators to assure consistency among model objects.
	private ModelValidator validator = null;
	
	public ObjectRegistry() {
		this.fileManager = new FileManager();		
		initValidators();
	}
	
	public void addClassPath(String... paths) throws IOException {
		for (String path : paths){
			fileManager.addDirectory(path);
		}
	}
	
	public Collection<String> getClassPath(){
		List<String> paths = new ArrayList<String>();
		for (File path : fileManager.getDirectories()){
			paths.add(path.getAbsolutePath());
		}
		return paths;
	}
	
	public void setPackageFilter(PackageFilter filter){
		if (pakcageFilter == filter) {
			return;
		}

		pakcageFilter = filter;
	}
	
	public PackageFilter getPackageFilter(){
		return pakcageFilter;
	}
	
	public Collection<String> getFilteredPackages(){
		if (pakcageFilter != null){
			return pakcageFilter.getFilters();
		}else{
			return Collections.emptySet();
		}
	}

	public void addComponent(IComponent comp) {
		if (!projectComponents.containsKey(comp)){
			removeComponent(comp);
			projectComponents.put(comp.getName(), comp);
		}
	}
	
	public void removeComponent(IComponent comp) {
		if (comp.getParent() != null){
			if (comp.getParent() instanceof Project){
				projectComponents.remove(comp.getName());
			}else{
				comp.getParent().removeComponent(comp);
			}
		}
	}
	
	public Collection<IComponent> getProjectComponents() {
		return projectComponents.values();
	}
	
	public Collection<IComponent> getAllComponents() {
		Set<IComponent> set = new HashSet<IComponent>();
		for (IComponent comp : projectComponents.values()) {
			set.add(comp);
			set.addAll(comp.getAllDescendants());
		}
		return set;
	}
	
	public IComponent getComponent(String name){
		for (IComponent comp : getAllComponents()){
			if (comp.getName().equals(name)) {
				return comp;
			}
		}
		return null;
	}
	
	public Collection<DeployableContainer> getContainers() {
		return containers.values();
	}

	public Set<JavaPackage> getProjectJavaPackages(){
		return new HashSet<JavaPackage>(focusedPackages.values());
	}

	public JavaPackage getProjectJavaPackage(String name){
		return focusedPackages.get(name);
	}
	
	public Set<JavaClass> getProjectJavaClasses(){
		return new HashSet<JavaClass>(focusedClasses.values());
	}
	
	public JavaClass getProjectJavaClass(String name){
		return focusedClasses.get(name);
	}
	
	
	public Collection<JavaClass> getDependants(JavaClass clazz){
		Collection<JavaClass> temp = new HashSet<JavaClass>();
		for (JavaClass c : focusedClasses.values()){
			if (c.isDependency(clazz)){
				temp.add(c);
			}
		}
		return temp;
	}
	
	public void analyze() {		
		boolean result = validator.validate();
		if (!result){
			throw new ModelException(validator.getErrorInfo());
		}

		reset();
		initWorkers();
	
		analyseClasses();
		parseContainers();				
		parseComponents();
	}
	
	//*************************************************************************
	//              private methods for computation processing                *
	//*************************************************************************

	private void reset() {		
		focusedClasses.clear();
		ignoredClasses.clear();
		focusedPackages.clear();
		ignoredPackages.clear();
		containers.clear();
		ignoredContianer = null;
	}


	private void parseContainers() {
		for (JavaClass clazz : focusedClasses.values()) {
			String conName = clazz.getContainer();
			DeployableContainer con = containers.get(conName);
			if (con == null) {
				if (conName.equals("Unknown")) {//should not reach here
					con = new UnknownContainer("Unknown");
				}else if (isZipped(conName)) {
					con = new JarFile(conName);
				}else{
					con = new ClassDir(conName);
				}
				containers.put(conName, con);
			}
			con.addClass(clazz);
		}
		if (!ignoredPackages.isEmpty()){
			ignoredContianer = new UnknownContainer("Unknown");
			ignoredContianer.addJavaClasses(ignoredClasses.values());
		}
		//build reference relation among containers
		for (DeployableContainer con : containers.values()){
			for (JavaClass c : con.getJavaClasses()){
				for (JavaClass ref : c.getEfferents()){
					String refConName = ref.getContainer();
					if (!refConName.equals(con.getName())){
						DeployableContainer refCon = containers.get(refConName);
						if (refCon == null){
							refCon = ignoredContianer;
						}
						con.addDependency(refCon);
						refCon.addDependant(con);
					}
				}
			}
		}
	}

	private boolean isZipped(String name) {
		return name.endsWith(".jar")||name.endsWith(".zip")||name.endsWith(".war");
	}
	
	private void parseComponents() {
		Collection<IComponent> comps = getAllComponents();
		
		for (IComponent comp : comps) {
			// refresh JavaPackage for component
			for (String packName : comp.getPackages()) {
				if (focusedPackages.containsKey(packName)) {
					((Component) comp).addJavaPackage(focusedPackages.get(packName));
				}
			}
		}
		for (IComponent comp : comps) {
			buildDependencyFor(comp);
		}
	}

	private void initWorkers() {
		if (parser == null) {
			parser = new ClassFileParser(pakcageFilter);
			builder = new JavaClassBuilder(parser, fileManager);
		}
	}
	
	
	private void initValidators(){
		validator = new PackageConsistencyChecker(this);
		ModelValidator v2 = new InterfaceConsistencyChecker(this);
		//TODO, this checker is essentially a redundance, since they all 
		//should be assured in component manipulation.
		ModelValidator v3 = new BlackListConsistencyChecker(this);
		validator.setSuccessor(v2);
		v2.setSuccessor(v3);
	}

	private void analyseClasses() {
		//build all classes in specified class path
		Collection<JavaClass> temp = builder.build();
		//register built classes and packages
		for (JavaClass clazz : temp) {
			focusedClasses.put(clazz.getName(), clazz);
			JavaPackage pack = focusedPackages.get(clazz.getPackageName());
			if (pack == null) {
				pack = new JavaPackage(clazz.getPackageName());
				focusedPackages.put(clazz.getPackageName(), pack);
			}
			//fill classes into package
			pack.addClass(clazz);
        }
		//refresh referenced classes for all analysed classes		
		for (JavaClass clazz : focusedClasses.values()){
			for (JavaClass ref : clazz.getEfferents()){
				if (focusedClasses.get(ref.getName())!= null){
					clazz.addReferencedClass(focusedClasses.get(ref.getName()));
				}else{
					JavaClass outRef = ignoredClasses.get(ref.getName());
					if (outRef == null){
						ignoredClasses.put(ref.getName(), ref);
					}else{
						ref = outRef;
					}
				}
			}
		}
		//build reference relations among packages	
		for (JavaClass clazz : focusedClasses.values()) {
			JavaPackage pack = focusedPackages.get(clazz.getPackageName());
			
			for (JavaPackage ref : clazz.getImportedPackages()){
				if (focusedPackages.get(ref.getName()) != null){
					ref = focusedPackages.get(ref.getName());					
				}else{
					if (ignoredPackages.get(ref.getName()) != null){
						ref = ignoredPackages.get(ref.getName());
					}else{
						ignoredPackages.put(ref.getName(), ref);
					}
				}
				pack.dependsUpon(ref);
			}
		}           
		//fill unnalysed classes into unanalysed pcakges
		for (JavaClass clazz : ignoredClasses.values()){
			JavaPackage jp = ignoredPackages.get(clazz.getPackageName());
			if (jp == null){//should not reach here.
				jp = new JavaPackage(getPackageName(clazz.getName()));
				ignoredPackages.put(jp.getName(), jp);
			}
			jp.addClass(clazz);
		}
		
//		System.out.println("done!");
	}
	
	private String getPackageName(String className){
		return className.substring(0, className.lastIndexOf("."));
	}

	private void buildDependencyFor(IComponent comp) {		
		yang_zz:for (IComponent other : getAllComponents()) {
    		if (comp == other) continue;
    		if (comp.getDependencies().contains(other)) continue;
    		
    		for (JavaPackage sp : comp.getJavaPackages()) {
    			for (JavaPackage tp : other.getJavaPackages()) {
    				if (sp.getEfferents().contains(tp)) {
    					((Component)comp).addDependency(other);
    					((Component)other).addDependant(comp);
    					continue yang_zz;    					
    				}
    			}
    		}
		}
	}
		
}
