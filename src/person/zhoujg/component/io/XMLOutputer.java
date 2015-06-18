package person.zhoujg.component.io;

import static person.zhoujg.util.StringUtil.tab;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import jdepend.framework.JavaClass;
import jdepend.framework.JavaPackage;
import person.zhoujg.component.Project;
import person.zhoujg.component.model.DeployableContainer;
import person.zhoujg.component.model.IComponent;
import person.zhoujg.component.model.ILayer;
import person.zhoujg.component.model.ModelException;
import person.zhoujg.component.model.problem.AccessProblem;
import person.zhoujg.component.model.problem.SplitPackageProblem;

public class XMLOutputer {
	protected Project project;
	protected int tabNum = 1;
	
	public XMLOutputer(Project pro){
		this.project = pro;
	}
	public XMLOutputer(){}
	
	public XMLOutputer setProject(Project pro){
		this.project = pro;
		return this;
	}
	
	public void toXML(String fileName) throws FileNotFoundException{
		if (project == null){
			throw new ModelException(ModelException.PROJECT_NOT_VALID);
		}
		PrintWriter writer = new PrintWriter(fileName);
		StringBuffer sb = new StringBuffer();
		writeHeader(sb);
		writeContainers(sb);
		writeComponent(sb);
		writeAccessProblems(sb);
		writeSplitPackages(sb);
		writeFooter(sb);
		writer.write(sb.toString());
		writer.flush();
		writer.close();
	}
	
	protected void writeFooter(StringBuffer sb) {
		sb.append("</project>");
	}
	
	protected void writeSplitPackages(StringBuffer sb){
		if (project.getSplitPackageProblems().size() > 0) {
			sb.append(tab());
			sb.append("<splitPackages>\n");
			for (SplitPackageProblem problem : project.getSplitPackageProblems()){
				sb.append(tab(2));
				sb.append("<package name=\"" + problem.getJavaPackage().getName() + "\">\n");
				for (DeployableContainer con : problem.getInvolvedContainers()){
					sb.append(tab(3));
					sb.append("<container>" + con.getName() + "</container>\n");
				}
				sb.append(tab(2));
				sb.append("</package>\n");
			}
			sb.append(tab());
			sb.append("</splitPackages>\n");
		}
	}
	
	protected void writeAccessProblems(StringBuffer sb) {
		sb.append(tab());
		sb.append("<accessProblems inBlackList=\"" + project.getInBlackListProblems().size() +
					"\" nonInWhiteList=\"" + project.getInvisibleProblems().size() +
					"\" nonInterface=\"" + project.getNonInterfaceProblems().size() + "\">\n");
		Collection<AccessProblem> problems = project.getInBlackListProblems();
		if (problems.size() > 0){
			writeAccessProblems(sb, problems, AccessProblem.IN_BLACKLIST);
		}
		problems = project.getInvisibleProblems();
		if (problems.size() > 0){
			writeAccessProblems(sb, problems, AccessProblem.NOT_IN_WHITELIST);
		}
		problems = project.getNonInterfaceProblems();
		if (problems.size() > 0){
			writeAccessProblems(sb, problems, AccessProblem.NON_INTERFACE);
		}
		
		sb.append(tab());
		sb.append("</accessProblems>\n");
	}
	protected void writeAccessProblems(StringBuffer sb, Collection<AccessProblem> problems, int type){
		
		String typeStr = "";
		switch (type){
		case AccessProblem.IN_BLACKLIST 	:	typeStr = "inBlackList"; break;
		case AccessProblem.NOT_IN_WHITELIST :	typeStr = "notInWhiteList"; break;
		case AccessProblem.NON_INTERFACE 	:	typeStr = "nonInterface"; break;
		}
		
		sb.append(tab(2));
		sb.append("<" + typeStr + ">\n");
		
		for (AccessProblem problem : problems){
			sb.append(tab(3));
			sb.append("<compPair dependee=\"" + problem.getDependee().getName() +
						"\" dependant=\"" + problem.getDependant().getName() + "\">\n");
			Map<JavaClass, Set<JavaClass>> elements = problem.getProblemElements();
			for (JavaClass c : elements.keySet()){
				sb.append(tab(4));
				sb.append("<class name=\"" + c.getName() + "\">\n");
				sb.append(tab(5));
				sb.append("<accessedBy>\n");
				for (JavaClass c1 : elements.get(c)){
					sb.append(tab(6));
					sb.append("<class>" + c1.getName() + "</class>\n");
				}
				sb.append(tab(5));
				sb.append("</accessedBy>\n");
				sb.append(tab(4));
				sb.append("</class>\n");
			}
			sb.append(tab(3));
			sb.append("</compPair>\n");
		}
		
		sb.append(tab(2));
		sb.append("</" + typeStr + ">\n");
	}	
	
	protected void writeComponent(StringBuffer sb) {		
		sb.append(tab());
		String compType;
		if (isLayers(project.getComponents())) {
			compType = "layers";
		}else{
			compType = "components";
		}
		sb.append("<"+ compType + ">\n");
		for (IComponent comp : project.getComponents()){
			writeComponent(comp, sb);
		}
		sb.append(tab());
		sb.append("</" + compType + ">\n");
		tabNum = 1;
	}
	
	protected void writeComponent(IComponent comp, StringBuffer sb) {
		sb.append(tab(tabNum + 1));
		String compType = "component";
		String level = "";
		if (comp instanceof ILayer) {
			compType = "layer";
			level = " level=\"" + String.valueOf(((ILayer)comp).getLevel() + "\"");
		}
		sb.append("<" + compType + " name=\"" + comp.getName() + 
					"\" strictLayer=\"" + comp.isStrictLayer() + 
					"\"" + level + " packages=\"" + comp.getJavaPackages().size() +"\">\n");
		tabNum++;
		for (JavaPackage pack : comp.getJavaPackages()){
			sb.append(tab(tabNum + 1));
			sb.append("<package>" + pack.getName() + "</package>\n");
		}
		
		Set<IComponent> set = null;
		//write component interfaces
		sb.append(tab(tabNum + 1));
		sb.append("<interfaces>\n");
		for (String view : comp.getInterfaceViews()){
			sb.append(tab(tabNum + 2));
			sb.append("<view name=\"");
			sb.append(view);
			sb.append("\">\n");
			for (String api : comp.getInterface(view)){
				sb.append(tab(tabNum + 3));
				sb.append("<interface>" + api + "</interface>\n");
			}			
			tabNum+=3;
			set = comp.getWhiteList(view);
			writeCompsList(sb, set, "whiteList");
			set = comp.getBlackList(view);
			writeCompsList(sb, set, "blackList");
			tabNum-=3;
			sb.append(tab(tabNum + 2));
			sb.append("</view>\n");
		}
		sb.append(tab(tabNum + 1));
		sb.append("</interfaces>\n");

		if (comp.getComponents().size() > 0){
			sb.append(tab(tabNum + 1));			
			sb.append("<children>\n");		
			tabNum++;
			for (IComponent child : comp.getComponents()){
				writeComponent(child, sb);				
			}
			tabNum--;
			sb.append(tab(tabNum + 1));
			sb.append("</children>\n");			
		}
				
		//write dependency and depandant info.
		tabNum++;
		set = comp.getDependencies();
		writeCompsList(sb, set, "dependencies");
		set = comp.getDependants();
		writeCompsList(sb, set, "dependants");
		tabNum--;
		
		sb.append(tab(tabNum));
		sb.append("</" + compType + ">\n");
		
		tabNum--;
	}
	
	protected void writeCompsList(StringBuffer sb, Set<IComponent> set, String listType){
		sb.append(tab(tabNum));
		sb.append("<");
		sb.append(listType);
		sb.append(">\n");
		for (IComponent comp : set){
			sb.append(tab(tabNum + 1));
			sb.append("<component>");
			sb.append(comp.getName());
			sb.append("</component>\n");
		}
		sb.append(tab(tabNum));
		sb.append("</");
		sb.append(listType);
		sb.append(">\n");
	}
	
	protected void writeContainers(StringBuffer sb) {
		sb.append(tab());
		sb.append("<containers>\n");
		for (DeployableContainer con : project.getContainers()){
			sb.append(tab(2));
			sb.append("<container name=\"" + con.getName().replace('\\', '/') + "\">\n");
			for (String pack : con.getPackages()){
				JavaPackage javaPack = project.getJavaPackage(pack);
				sb.append(tab(3));
				sb.append("<package name=\"" + pack + 
							"\" afferents=\"" + javaPack.getAfferents().size() +
							"\" efferents=\"" + javaPack.getEfferents().size() +
							"\" classCount=\"" + javaPack.getClassCount() +
							"\" concreteClassCount=\"" + javaPack.getConcreteClassCount() + "\">\n");
				
				String classType;
				for (JavaClass clazz : javaPack.getClasses()){
					sb.append(tab(4));
					if (clazz.isInterface()){
						classType = "interface";
					}else if (clazz.isAbstract()){
						classType = "abstract";
					}else{
						classType = "concrete";
					}
					sb.append("<class type=\"" + classType + "\">" + clazz.getName() + "\n");
					if (clazz.getSuperClazz() != null){
						sb.append(tab(5));
						sb.append("<extends>" + clazz.getSuperClazz().getName() + "</extends>\n");
					}
					if (!clazz.getInterfaces().isEmpty()){
						for (JavaClass inter : clazz.getInterfaces()){
							sb.append(tab(5));
							sb.append("<implements>"+ inter.getName() + "</implements>\n");
						}
					}
					sb.append(tab(4));
					sb.append("</class>\n");
				}
				sb.append(tab(3));
				sb.append("</package>\n");
			}
			sb.append(tab(2));
			sb.append("</container>\n");
		}
		sb.append(tab());
		sb.append("</containers>\n");
	}
	
	protected void writeHeader(StringBuffer sb){
		sb.append("<?xml version=\"1.0\"?>\n");
		sb.append("<project ");
		StringBuffer filterStr = new StringBuffer();		
		sb.append("name=\"" + project.getName() + "\" strictLayer=\"" + project.isStrictLayer() + "\"" + filterStr.toString());
		headerDecorator(sb);
	}
	
	protected void headerDecorator(StringBuffer sb){
		sb.append(" containers=\"" + project.getContainers().size() + "\"");
		sb.append(" components=\"" + project.getAllComponents().size() + "\"");
		sb.append(" packages=\"" + project.getJavaPackages().size() + "\"");
		sb.append(" classes=\"" + project.getJavaClasses().size() + "\">\n");
	}
	
	private boolean isLayers(Collection<IComponent> col){
		for (IComponent comp : col){
			if (!(comp instanceof ILayer)) 
				return false;
		}
		return true;
	}

}
