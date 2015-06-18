package person.zhoujg.component.model.validation;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import person.zhoujg.component.internal.ObjectRegistry;
import person.zhoujg.component.model.IComponent;
import static person.zhoujg.util.StringUtil.tab;

/**
 * Check whether one package is added to more than one components.
 * 
 * @author zhoujg
 * @date 2014年2月25日
 *
 */
public class PackageConsistencyChecker extends ModelValidator {
	//packages that declared in a set of component
	private Map<String, Set<IComponent>> dupPacks = new HashMap<String, Set<IComponent>>();
	//packages that declared in components but in filters
	private Map<String, Set<IComponent>> ignoredPacks = new HashMap<String, Set<IComponent>>();

	public PackageConsistencyChecker(ObjectRegistry host) {
		super(host);
	}

	@Override
	public boolean validate() {
		dupPacks.clear();
		ignoredPacks.clear();
		traverse();
		if (dupPacks.size() == 0 && ignoredPacks.size() == 0){
			if (getSuccessor() != null) {
				return getSuccessor().validate();
			}else{
				return true;
			}
		}
		setErrorInfo(initErrorInfo());
		return false;
	}

	private String initErrorInfo() {
		StringBuffer sb = new StringBuffer();
		if (dupPacks.size() > 0){
			sb.append("The following packages are added to more than one components.\n");
			addString(sb, dupPacks);
		}
		if (ignoredPacks.size() > 0){
			sb.append("The following packages declared in components but also in filters.\n");
			addString(sb, ignoredPacks);
		}
		return sb.toString();
	}
	
	private void addString(StringBuffer sb, Map<String, Set<IComponent>> map){
		for (String pack : map.keySet()){
			sb.append(tab());
			sb.append("<package name=\"" + pack + "\"/>\n");
			for (IComponent comp : map.get(pack)){
				sb.append(tab(2));
				sb.append("<component>" + comp.getName() + "</component>\n");
			}
			sb.append(tab());
			sb.append("</package>\n");
		}
	}

	private void traverse() {
		int count = getHost().getAllComponents().size();
		if (count > 0){
			IComponent[] comps = new IComponent[count];
			getHost().getAllComponents().toArray(comps);
			
			//check a package declared in more than one components
			for (int i=0; i<count-1; i++){
				for (int j=i+1; j<count; j++){
					Set<String> set = new HashSet<String>(comps[i].getPackages());
					set.retainAll(comps[j].getPackages());
					for (String pack : set){
						Set<IComponent> packComps = dupPacks.get(pack);
						if (packComps == null){
							packComps = new HashSet<IComponent>();
							dupPacks.put(pack, packComps);
						}
						packComps.add(comps[i]);
						packComps.add(comps[j]);
					}
				}
			}
			//check a filted package whether in a component.
			if (getFilters().size() > 0) {
				for (IComponent comp : comps){
					Set<String> filtered = new HashSet<String>(getFilters());
					filtered.retainAll(comp.getPackages());
					for (String pack : filtered){
						Set<IComponent> owners = ignoredPacks.get(pack);
						if (owners == null){
							owners = new HashSet<IComponent>();
						}
						owners.add(comp);
						ignoredPacks.put(pack, owners);
					}
				}
			}
		}
	}
	
	private Collection<String> getFilters(){
		return getHost().getFilteredPackages();
	}
}
