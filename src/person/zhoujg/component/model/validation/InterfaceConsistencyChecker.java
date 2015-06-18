package person.zhoujg.component.model.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import person.zhoujg.component.internal.ObjectRegistry;
import person.zhoujg.component.model.IComponent;
import person.zhoujg.util.FileUtil;
import static person.zhoujg.util.StringUtil.tab;

public class InterfaceConsistencyChecker extends ModelValidator{
	private Map<IComponent, Collection<String>> wrongApis = new HashMap<IComponent, Collection<String>>();
	
	public InterfaceConsistencyChecker(ObjectRegistry host){
		super(host);
	}
	
	public boolean validate() {
		wrongApis.clear();
		for (IComponent comp : getHost().getProjectComponents()) {
			traverse2(comp);
		}
		if (wrongApis.size() == 0){
			if (getSuccessor() != null){
				return getSuccessor().validate();
			}else {
				return true;
			}
		}
		setErrorInfo(initErrorInfo());
		return false;
	}
	
	private String initErrorInfo(){		
		StringBuffer sb = new StringBuffer();
		if (wrongApis.size() > 0){
			sb.append("Declared interfaces are not found in the following components.\n");
			for (IComponent comp : wrongApis.keySet()){
				sb.append(tab());
				sb.append("<component name=\"" + comp.getName() + "\">\n");			
				for (String str : wrongApis.get(comp)){
					sb.append(tab(2));
					sb.append("<interface>" + str + "</interface>\n");
				}
				sb.append(tab());
				sb.append("</component>\n");
			}
		}
		
		return sb.toString();
	}

	private void traverse2(IComponent node) {
		Set<String> apiScope = new HashSet<String>(node.getPackages());
		for (IComponent comp : node.getComponents()){
			apiScope.addAll(comp.getInterface());
		}
		
		Set<String> apis = new HashSet<String>(node.getInterface());
		//直接删除明确接口声明的
		apis.removeAll(apiScope);
		//删除未明确声明的类形式的接口声明
		if (apis.size() > 0){//在当前构件范围内处理类形式的接口声明
			for (Iterator<String> api = apis.iterator(); api.hasNext() && apis.size() > 0;){
				String tempAPI = api.next();				
				String seg = FileUtil.getLastSegment(tempAPI);
				if (seg != null && Character.isUpperCase(seg.charAt(0))){
					String pack = tempAPI.substring(0, tempAPI.lastIndexOf("."));
					if (apiScope.contains(pack)){
						api.remove();
					}
				}
				
			}
		}
		if (apis.size() > 0){
			wrongApis.put(node, apis);
		}
		

		for (IComponent child : node.getComponents()){
			traverse2(child);
		}
	}
}
