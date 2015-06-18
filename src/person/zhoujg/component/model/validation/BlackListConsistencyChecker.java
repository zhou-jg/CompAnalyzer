package person.zhoujg.component.model.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import person.zhoujg.component.internal.ObjectRegistry;
import person.zhoujg.component.model.IComponent;
import person.zhoujg.component.model.util.CompPair;

import static person.zhoujg.util.StringUtil.tab; 

/**
 * Check black and white list consistency
 * @author zhoujg
 * @date 2014年2月25日
 *
 */

public class BlackListConsistencyChecker extends ModelValidator{

	private Collection<CompPair> pairs = new HashSet<CompPair>();
	
	public BlackListConsistencyChecker (ObjectRegistry host) {
		super(host);
	}
	
	public boolean validate(){
		pairs.clear();
		for (IComponent comp : getHost().getProjectComponents()){
			traverse(comp);
		}
		if (pairs.size() == 0){
			if (getSuccessor() != null) {
				return getSuccessor().validate();
			}else{
				return true;
			}
		}
		initErrorInfo();
		return false;
	}

	private String initErrorInfo() {
		StringBuffer sb = new StringBuffer();
		if (pairs.size() > 0) {
			sb.append("The former component (left) should not be in the black/white list of the latter component (right).\n");
			for (CompPair pair : pairs){
				sb.append(tab());
				sb.append("<pair>" + pair.getLeft().getName() + ", " + pair.getRight().getName() + "</pair>\n");
			}
		}		
		
		return sb.toString();
	}
	
	private void traverse(IComponent node){		
		Set<IComponent> bList = node.getBlackList();
		for (IComponent comp : bList){
			if (!comp.isSiblingWith(node)){
				pairs.add(new CompPair(comp, node));
			}
		}
		Set<IComponent> wList = node.getWhiteList();
		for (IComponent comp : wList){
			if (!comp.isSiblingWith(node)){
				pairs.add(new CompPair(comp, node));
			}
		}
		
		//check node's children recursively
		for (IComponent child : node.getComponents()){
			traverse(child);
		}
	}
}
