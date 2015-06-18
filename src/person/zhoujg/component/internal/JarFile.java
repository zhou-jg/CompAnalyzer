package person.zhoujg.component.internal;

import person.zhoujg.component.model.DeployableContainer;

public class JarFile extends DeployableContainer {
	JarFile (String name) {
		super(name, DeployableContainer.JAR_FILE);
	}
}
