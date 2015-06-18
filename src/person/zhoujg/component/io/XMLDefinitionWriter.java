package person.zhoujg.component.io;

import static person.zhoujg.util.StringUtil.tab;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Set;

import person.zhoujg.component.model.IComponent;
import person.zhoujg.component.model.ModelException;

public class XMLDefinitionWriter extends XMLOutputer{
	
	private String fileName;
	private String reportName;
	
	public void setReportName(String reportName){
		this.reportName = reportName;
	}

	@Override
	public void toXML(String fileName) throws FileNotFoundException {
		if (project == null){
			throw new ModelException(ModelException.PROJECT_NOT_VALID);
		}
		this.fileName = fileName;		
		PrintWriter writer = new PrintWriter(fileName);
		StringBuffer sb = new StringBuffer();
		writeHeader(sb);
		writeClasspath(sb);
		writeComponent(sb);
		writeFooter(sb);
		writer.write(sb.toString());
		writer.flush();
		writer.close();
	}

	private void writeClasspath(StringBuffer sb) {
		sb.append(tab());
		sb.append("<classPath>\n");
		for (String path : project.getClassPath()){
			sb.append(tab(2));
			sb.append("<path>");
			sb.append(path.replace('\\', '/'));
			sb.append("</path>\n");
		}
		sb.append(tab());
		sb.append("</classPath>\n");
	}	
	
	@Override
	protected void writeCompsList(StringBuffer sb, Set<IComponent> set,
			String listType) {
		if ("whiteList".equals(listType) || "blackList".equals(listType)){
			super.writeCompsList(sb, set, listType);
		}
	}

	protected void headerDecorator(StringBuffer sb){
		sb.append(" report=\"" + reportName + "\">\n");
	}

}
