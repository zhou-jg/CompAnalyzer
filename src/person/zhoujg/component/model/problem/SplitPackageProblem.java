package person.zhoujg.component.model.problem;

import java.util.Collection;
import java.util.HashSet;

import jdepend.framework.JavaPackage;
import person.zhoujg.component.model.DeployableContainer;

/**
 * For a problem that a package belongs to more than one deployable containers.
 * 
 * @author zhoujg
 * @date 2014年2月24日
 *
 */
public class SplitPackageProblem extends DependencyProblem {
	
	public static final int SPLIT_PACKAGE = 5;
	private JavaPackage wrong;
	private Collection<DeployableContainer> set = new HashSet<DeployableContainer>();
	
	public SplitPackageProblem(JavaPackage pack){
		super(SplitPackageProblem.SPLIT_PACKAGE);
		this.wrong = pack;
	}
	
	public JavaPackage getJavaPackage(){
		return wrong;
	}
	
	public Collection<DeployableContainer> getInvolvedContainers(){
		return set;
	}
	
	public boolean addInvolvedContainer(DeployableContainer con){
		return set.add(con);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<package name=\""  + wrong.getName()+ "\">\n");
		for (DeployableContainer con : set){
			sb.append("  ");
			sb.append("<container>" + con.getName() + "</container>\n");
		}
		sb.append("</package>\n");
		
		return sb.toString();
	}
	
}
