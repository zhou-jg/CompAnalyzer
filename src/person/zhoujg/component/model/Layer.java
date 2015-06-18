package person.zhoujg.component.model;

import java.util.HashSet;
import java.util.Set;


/**
 * Default implementation of an {@link ILayer} interface.
 * 
 * @author zhoujg
 * @date 2014年1月20日
 *
 */
public class Layer extends Component implements ILayer {

	private int level = 0;	

	public Layer (String name, int level) {
		super(name);
		this.level = level;
	}
	
	/**{@inheritDoc} */
	@Override
	public int getLevel() {
		return level;
	}
	

	/**
	 * Layer adds layer encapsulation kind of black list components beyond a 
	 * noraml component. 
	 * @see IComponent#getBlackList()
	 */
	@Override
	public Set<IComponent> getBlackList(String... views) {
		Set<IComponent> list = super.getBlackList(views);
		list.addAll(computeBlackListForLayer());
		return list;
	}
	
	
//	type should be invisible than blacklist.
	private Set<IComponent> computeBlackListForLayer(){
		Set<IComponent> set = new HashSet<IComponent>();
		if (getParent() != null){
			for (IComponent sibling : getSiblings()){
				if (sibling instanceof ILayer){
					int siblingLevel = ((ILayer)sibling).getLevel();
					int dis = siblingLevel - getLevel();
					if (dis > 0){
						set.add(sibling);
						set.addAll(sibling.getAllDescendants());
					}else if (dis < -1 && getParent().isStrictLayer()){
						set.add(sibling);
						set.addAll(sibling.getAllDescendants());
					}												
				}
			}
		}		
		
		return set;
	}

	/**{@inheritDoc}*/
	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	/**{@inheritDoc}*/
	@Override
	public IComponent addsToWhiteList(IComponent comp, String... views) {
		if (isSiblingWith(comp)){
			if (comp instanceof ILayer){
				//no-op;
				return this;
			}
		}
		return super.addsToWhiteList(comp, views);
	}

}
