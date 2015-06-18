package person.zhoujg.component.model.problem;


public class DependencyProblem{
	
	public static final int CIRCLE	= 0;	
	
	protected int type;

	protected DependencyProblem(int type){
		this.type = type;
	}
	
	public int getType(){
		return type;
	}
}
