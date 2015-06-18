package person.zhoujg.component.model;

import java.util.Collection;

public interface IDependableEntity extends INamedObject{
	/**
	 * Judge whether current object depends on <code>obj</obj>
	 * @param obj another object
	 * @return <b>true</b> if current boject depends on <code>obj</code>, otherwise <b>false</b>.
	 */
	boolean isDependency(IDependableEntity obj);
	
	/**
	 * @return dependants of the current object.
	 */
	Collection getAfferents();
	
	/**
	 * @return objects the current object depends on.
	 */
	Collection getEfferents();
}
