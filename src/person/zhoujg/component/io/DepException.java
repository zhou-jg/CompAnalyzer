package person.zhoujg.component.io;

import person.zhoujg.component.model.ModelException;

public class DepException extends ModelException {
	private static final long serialVersionUID = 1L;

	public DepException(String msg) {
		super(msg);
	}

	static final String PATH_WRONG = "class path is not valid.";
	static final String PACK_FILTER_WRONG = "package filter is not valid.";
	static final String FILE_WRONG = "the XML configuraion file is not valid.";
}
