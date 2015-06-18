package test;

import person.zhoujg.component.Project;
import person.zhoujg.component.io.XMLDefinitionReader;
import person.zhoujg.component.io.XMLOutputer;

public class Test {
	private Project pro;
	String report;
	
	public void testXMLConfig(String fileName) throws Exception{
		XMLDefinitionReader reader = new XMLDefinitionReader();
		if (fileName == null){
			fileName = "config.xml";
		}
		pro = reader.read(Test.class.getResourceAsStream(fileName));
		report = reader.getReportFileName();
	}
	
	public void testOutput() throws Exception{
		pro.analyze();
		XMLOutputer out = new XMLOutputer(pro);
		if (report == null){
			report = "G:/testConfig.xml";
		}
		out.toXML(report);
	}
	
	public static void main(String args[]) throws Exception{
		Test t = new Test();
		t.testXMLConfig(null);
		t.testOutput();
	}
}
