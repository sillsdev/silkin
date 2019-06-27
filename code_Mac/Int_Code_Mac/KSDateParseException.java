/**This class represents exceptions that are thrown when SILKin tries to parse
 * a date (usually via {@link UDate} and encounters an invalid format.
 * 
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class KSDateParseException extends KinshipSystemException {

    /** Construct an Exception which passes <code>msg</code> up the call stack.
    This class handles Kinship System internal Exceptions which arise during
    parsing.

    @param	msg	a String describing the exact error found.
     */
    KSDateParseException(String msg) {
        super(msg);
    }  //  end of constructor
}
