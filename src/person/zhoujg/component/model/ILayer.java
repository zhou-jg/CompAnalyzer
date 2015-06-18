package person.zhoujg.component.model;

/**
 * A Layer is an architectural concept for layered architecture style.
 * A layer has a propery to indicate its levels. Normally, a layer in
 * a lower level cannot access a layer in a upper level. <br><br>
 * In a layered architecture, a <code>strictLayer</code> propery can be 
 * set on the architecture component to indicate whether a upper layer 
 * can access all layers below it or just the layer directly below it. 
 * see {@link IComponent#isStrictLayer(boolean)}. 
 * 
 * @author zhoujg
 * @date 2014年1月20日
 *
 */
public interface ILayer extends IComponent {
	
	/**
	 * @return the <code>level</code> attribute of the layer.
	 */
	int getLevel();
	
	void setLevel(int level);
}
