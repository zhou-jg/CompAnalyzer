package person.zhoujg.component.model.util;

import person.zhoujg.component.Project;
import person.zhoujg.component.model.IComponent;
import person.zhoujg.component.model.ILayer;
import person.zhoujg.util.StringUtil;

public class DotGenerator {
	private int tab = 0; 
	/**
	 * Generate a dot string content, which can be rendered by a dot engine.
	 * @param project
	 * @return
	 */
	public String toDOT(Project project){
		StringBuffer sb = new StringBuffer();
		sb.append("digraph ");
		sb.append(qualifiedName(project.getName()));
		sb.append("{\n");
		tab++;
		//write nodes
		for (IComponent comp : project.getComponents()){			
			generateDOTNode(sb, comp);			
		}
		sb.append("\n");
		//write edges
		IComponent comps[] = new IComponent[project.getAllComponents().size()];
		project.getAllComponents().toArray(comps);
		for (int i = 0; i < comps.length - 1; i++){
			for (int j = i+1; j < comps.length; j++){
				if (comps[i].isDependency(comps[j])){
					sb.append(StringUtil.tab(tab));
					sb.append(comps[i].getName());
					sb.append(" -> ");
					sb.append(comps[j].getName());
					if (comps[j].isDependency(comps[i])){
						sb.append(" [dir=\"both\"]");
					}
					sb.append(";\n");
				}else if (comps[j].isDependency(comps[i])){
					sb.append(StringUtil.tab(tab));
					sb.append(comps[j].getName());
					sb.append(" -> ");
					sb.append(comps[i].getName());
					sb.append(";\n");
				}									
			}
		}
		tab--;
		sb.append(StringUtil.tab(tab));
		sb.append("}\n");
		
		return sb.toString();
	}
	
	private void generateDOTNode(StringBuffer sb, IComponent comp) {
		sb.append(StringUtil.tab(tab));
		if (comp.getComponents().size() > 0){
			sb.append("subgraph cluster_");
			sb.append(comp.getName());
			sb.append("{\n");
			sb.append(StringUtil.tab(++tab));
			if (comp instanceof ILayer){
				sb.append("style=filled");
				sb.append("label = \"<<Layer>>");
			}else {
				sb.append("label = \"");
			}
			
			sb.append(comp.getName());
			sb.append("\";\n");
			sb.append(StringUtil.tab(tab));
			sb.append(comp.getName());
			sb.append(";\n");
			for (IComponent child : comp.getComponents()){
				generateDOTNode(sb, child);
			}
			sb.append(StringUtil.tab(--tab));
			sb.append("}\n");
		}else {
			sb.append(comp.getName());
			sb.append(" [shape=rect];\n");
		}
	}

	private static String qualifiedName(String name) {
		return name.replaceAll(" ", "_");
	}
}
