package person.zhoujg.component.model.util;

import person.zhoujg.component.model.IComponent;

public class CompPair{
	private IComponent comp1;
	private IComponent comp2;
	public CompPair(IComponent comp1, IComponent comp2){
		this.comp1 = comp1;
		this.comp2 = comp2;
	}
	public IComponent getLeft(){
		return comp1;
	}
	
	public IComponent getRight(){
		return comp2;
	}
}

