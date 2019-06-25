/**This class represents exceptions that are thrown to explain that SILKin
 * has encountered a Horn Clause that violates the syntax.
 * 
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class KSBadHornClauseException extends KinshipSystemException  {
	
	/**Construct an Exception which passes <code>msg</code> up the call stack.
         * This class handles Kinship System internal Exceptions which have 
         * detected an illegally-constructed Horn Clause.
         * 
	 * @param	msg	a String describing the exact error found.
	*/		
	KSBadHornClauseException(String msg)  {
		super(msg);
		}  //  end of constructor
	}  //  end of class KSBadHornClauseException
