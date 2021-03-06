package person.zhoujg.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class StringUtil {
	public static boolean isNullOrEmpty(String str){
		return str==null || str.trim().length()==0;
	}
	
	public static String[] getItems(String str, String sep){
		return str.split(sep);
	}
	
	public static Collection<String> getStringItems(String str, String sep){
		Set<String> res = new HashSet<String>();
		for (String item : getItems(str, sep)){
			res.add(item);
		}
		return res;
	}

	
	public static String tab() {
        return "    ";
    }

    public static String tab(int n) {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < n; i++) {
            s.append(tab());
        }

        return s.toString();
    }

}
