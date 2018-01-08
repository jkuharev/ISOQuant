package isoquant.interfaces.dependency;

/**
 * <h3>iDependency</h3>
 * dependency of plugin 
 * @author Joerg Kuharev
 * @version 30.12.2010 11:46:51
 *
 */
public interface iDependency
{
	/**
	 * <h3>State</h3>
	 * dependency states
	 * @author Joerg Kuharev
	 * @version 30.12.2010 11:46:29
	 */
	public enum State
	{
		/** original state before anithing happens */
		unknown,
		/** dependency state is beeng checked */
		requested, 
		/** dependency is fullfiled */
		fullfilled,
		/** dependency fullfillment is scheduled */
		queued,
		/** dependency failed */
		failed;
		
		/** 
		 * copare States
		 * @param s
		 * @return true if States are equal
		 */
		public boolean equals(State s){return s==this;}
		
		/**
		 * check if State is checked and will be ok until execution
		 * @return true if queued or fullfilled
		 */
		public boolean ok(){return (this==State.queued || this==State.fullfilled);}
	}
	
	public State getState();
	public boolean ok();
	public boolean equals(iDependency otherDependency);
}
