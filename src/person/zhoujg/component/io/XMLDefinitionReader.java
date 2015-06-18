package person.zhoujg.component.io;

import static person.zhoujg.util.XMLUtil.getAttribute;
import static person.zhoujg.util.XMLUtil.getAttributeAsBoolean;
import static person.zhoujg.util.XMLUtil.getAttributeAsInt;
import static person.zhoujg.util.XMLUtil.getDocumentBuilder;
import static person.zhoujg.util.XMLUtil.getElementValue;
import static person.zhoujg.util.XMLUtil.getFirstSubElementByName;
import static person.zhoujg.util.XMLUtil.getFirstSubElementByNames;
import static person.zhoujg.util.XMLUtil.getSubElementsByName;
import static person.zhoujg.util.XMLUtil.getSubElementsByNames;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import jdepend.framework.PackageFilter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import person.zhoujg.component.Project;
import person.zhoujg.component.model.Component;
import person.zhoujg.component.model.IComponent;
import person.zhoujg.component.model.Layer;

public class XMLDefinitionReader {

	private Project pro = null; 
	private String modelFile = null;
	private String reportFile = null;
	private HashMap<String, IComponent> compRegistry = new HashMap<String, IComponent>();
	private HashMap<String, ComponentHelper> tempMem = new HashMap<String, ComponentHelper>();
	
	public Project read(String fileName){
		return read(new File(fileName));
	}
	
	public Project read(File file){
		try {
			return read(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new DepException(DepException.FILE_WRONG);
		}
	}
	
	public Project read(InputStream is){
		try {
			return read(getDocumentBuilder().parse(is));
		} catch (Exception e) {
			e.printStackTrace();
			throw new DepException(DepException.FILE_WRONG);
		} 
	}
	
	private Project read(Document doc) {
		Element root = doc.getDocumentElement();
		pro = new Project(getAttribute(root, "name"));
		pro.isStrictLayer(getAttributeAsBoolean(root, "strictLayer", false));
		setClassPath(root);
		setPackageFilter(root);
		setComponentModel(root);
		reportFile = getReportFile(root);
		return pro;
	}

	private void setComponentModel(Element root) {
		Element model = getFirstSubElementByName(root, "model");
		Collection<IComponent> comps = new ArrayList<IComponent>();
		if (model!= null){
			modelFile = getElementValue(model);
//			comps = UMLModelParser.parse(modelFile);
			
		}else {
			model = getFirstSubElementByNames(root, "components", "layers");
			if (model != null){
				List<Element> compList = getSubElementsByNames(model, "component", "layer");
				for (Element ele : compList){
					comps.add(parseComponent(ele));
				}
				for (IComponent comp : compRegistry.values()){
					ComponentHelper helper = tempMem.get(comp.getName());
					if (helper != null){
						for (String view : helper.getWhiteList().keySet()){
							for (String wComp : helper.getWhiteList().get(view)){
								comp.addsToWhiteList(compRegistry.get(wComp), view);
							}
						}
						for (String view : helper.getBlackList().keySet()){
							for (String bComp : helper.getBlackList().get(view))
							comp.addsToBlackList(compRegistry.get(bComp), view);
						}
					}
				}
			}
		}
		for (IComponent comp : comps){
			pro.addComponent(comp);
		}
	}

	private IComponent parseComponent(Element ele) {
		String type = ele.getNodeName();
		String name = getAttribute(ele, "name");

		IComponent comp = null;
		if (type.equals("component")){
			comp = new Component(name);
		}else{
			int level = getAttributeAsInt(ele, "level");
			comp = new Layer(name, level);
		}
		comp.isStrictLayer(getAttributeAsBoolean(ele, "strictLayer", false));
		comp.isSealed(getAttributeAsBoolean(ele, "sealed", false));
		compRegistry.put(name, comp);

		for (Element child : getSubElementsByName(ele, "package")){
			comp.addPackage(getElementValue(child));
		}
		
		ComponentHelper helper = new ComponentHelper();
		Element list = getFirstSubElementByName(ele, "interfaces");
		if (list != null){			
			List<Element> items = getSubElementsByName(list, "interface");
			for (Element item : items){
				comp.addsToInterface(getElementValue(item));
			}
			
			{//TODO exclusive with the above code for interface configuration.
			items = getSubElementsByName(list, "view");
			for (Element view : items){				
				List<Element> apiElements = getSubElementsByName(view, "interface");
				if (apiElements.size() > 0){
					String viewId = getAttribute(view, "name");
					List<String> apis = new ArrayList<String>();
					for (Element api : apiElements){
						apis.add(getElementValue(api));
					}
					comp.addsToInterface(viewId, apis.toArray(new String[apis.size()]));
					
					list = getFirstSubElementByName(view, "whiteList");
					if (list != null){
						List<Element> comps = getSubElementsByName(list, "component");
						for (Element compEle : comps){
							helper.addToWhiteList(getElementValue(compEle), viewId);
						}
					}
					list = getFirstSubElementByName(view, "blackList");
					if (list != null){
						List<Element> comps = getSubElementsByName(list, "component");
						for (Element compEle : comps){
							helper.addToBlackList(getElementValue(compEle), viewId);
						}
					}
				}
			}
			}
		}
		
		list = getFirstSubElementByName(ele, "whiteList");
		if (list != null){
			List<Element> items = getSubElementsByName(list, "component");
			for (Element item : items){
				helper.addToWhiteList(getElementValue(item));
			}
		}
		list = getFirstSubElementByName(ele, "blackList");
		if (list != null){
			List<Element> items = getSubElementsByName(list, "component");
			for (Element item : items){
				helper.addToBlackList(getElementValue(item));
			}
		}
		if (helper.isUseful()){
			tempMem.put(name, helper);
		}
		
		list = getFirstSubElementByName(ele, "children");
		if (list != null){
			List<Element> items = getSubElementsByName(list, "component");
			for (Element item : items){
				comp.addComponent(parseComponent(item));
			}
		}
		return comp;
	}

	private void setPackageFilter(Element root) {
		Element packageFilter = getFirstSubElementByName(root, "packageFilter");
		if (packageFilter != null){
			PackageFilter filter = new PackageFilter();
			List<Element> packs = getSubElementsByName(packageFilter, "package");
			for (Element pack : packs){
				try {
					filter.addPackage(getElementValue(pack));
				} catch (Exception e) {
					e.printStackTrace();
					throw new DepException(DepException.PACK_FILTER_WRONG);
				}
			}			
			pro.setPackageFilter(filter);
		}
	}

	private void setClassPath(Element root) {
		Element classPath = getFirstSubElementByName(root, "classPath");
		if (classPath != null){
			List<Element> paths= getSubElementsByName(classPath, "path");
			for (Element path : paths){
				try {
					pro.addClassPath(getElementValue(path));
				} catch (IOException e) {
					e.printStackTrace();
					throw new DepException(DepException.PATH_WRONG);
				}
			}
		}
	}

	public String getModelFileName(){
		return modelFile;
	}
	
	public String getReportFileName(){
		return reportFile;
	}
	
	private String getReportFile(Element doc) {
		return getAttribute(doc, "report");
	}	
	
}
