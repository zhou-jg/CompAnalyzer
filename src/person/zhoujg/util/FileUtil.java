package person.zhoujg.util;

import java.io.File;
import java.util.Collection;

public class FileUtil {
	
	public static String getQualifiedName(String str) {
		return str.indexOf(".*")>0 ? str.substring(0, str.indexOf(".*")) : str;
	}
	
	public static String getDirectory (Collection<File> dirs, File file) {
		for (File f : dirs) {
			if (file.getPath().startsWith(f.getPath())) {
				return f.getPath();
			}
		}
		return "unknown";
	}
	
	public static int getSegmentNumberForPackage(String pack){
		return pack.split(".").length;
	}
	
	public static String getLastSegment(String qualifedName){
		if (qualifedName.indexOf('.') > -1){
			return qualifedName.substring(qualifedName.lastIndexOf('.')+1);
		}
		return qualifedName;
	}
}
