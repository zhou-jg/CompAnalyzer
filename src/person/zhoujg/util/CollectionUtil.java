package person.zhoujg.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionUtil {
	/**
	 * Check whether a specified collection containes one of object specified by
	 * the viable arguments. 
	 * @param set
	 * @param t
	 * @return the first found object contained in the collection, <code>null</code> 
	 * 		indicates not found.
	 */
	public static <T> boolean containsOne(Set<T> set, T... t){
		for (T obj : t){
			if (set.contains(obj)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Casting elements of the specified list from a upper type (T1) to a subtype (T2).
	 * @param col
	 * @param dummy
	 * @return
	 */
	@SuppressWarnings(value={"unchecked"})
	public static <T1, T2> List<T2> downCasting(List<T1> col, T2 dummy){
		List<T2> list = new ArrayList<T2>();
		for (T1 obj : col){
			list.add((T2)obj);
		}
		
		return list;
	}
	
    
    public static boolean equals(Set<String> set1, Set<String> set2){
    	if (set1.size() != set2.size()){
    		return false;
    	}else {
    		Set<String> temp = new HashSet<String>(set1);
    		temp.removeAll(set2);
    		if (temp.size() == 0){
    			return true;
    		}else {
    			return false;
    		}    	
    	}
    }
    
    public static boolean equals(List<String> list1, List<String> list2, boolean ordered){
    	if (list1.size() != list2.size()){
    		return false;
    	}else if (ordered){
    		int count = list1.size();
    		int index = -1;
    		boolean exist = false;
    		for (String str : list1){
    			index++;
    			if (str.equals(list2.get(0))){
    				exist = true;
    				break;
    			}
    		}
    		if (exist){
    			for (int i=0;i < count; i++){
    				if (index == count){
    					index = 0;
    				}
    				if (list2.get(i) != list1.get(index++)){
    					return false;
    				}
    			}
    			return true;
    		}else {
    			return false;
    		}
    	}else {
    		return equals(new HashSet<String>(list1), new HashSet<String>(list2));
    	}
    }
    
}
