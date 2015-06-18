package person.zhoujg.component.io;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ComponentHelper{
	
	//key is viewid, value is component list
	private Map<String, Set<String>> whiteList = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> blackList = new HashMap<String, Set<String>>();
	
	public void addToWhiteList(String comp, String... views){
		internalAddToList(whiteList, comp, views);
	}
	
	public void addToBlackList(String comp, String... views){
		internalAddToList(blackList, comp, views);
	}
	
	public Map<String, Set<String>> getWhiteList(){
		return whiteList;
	}
	
	public Map<String, Set<String>> getBlackList(){
		return blackList;
	}
	
	public boolean isUseful(){
		return whiteList.size() > 0 || blackList.size() > 0;
	}
	
	
	private void internalAddToList(Map<String, Set<String>> map, String comp, String... views){
		if (views == null || views.length == 0){
			for (Set<String> set : map.values()){
				set.add(comp);
			}
		}else {
			for (String view : views){
				Set<String> set = map.get(view);
				if (set == null){
					set = new HashSet<String>();
				}
				set.add(comp);
			}
		}
	}
}

