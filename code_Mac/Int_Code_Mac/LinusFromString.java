/**Each instance of LinusFromString is initialized with a string; it will 
 * provide that string when <code>makeLine</code> is called.  Thereafter, it 
 * responds with null.	<p>	
 * This extension of Linus is intendedto allow parsing from a string.
 * </p>
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
*/
public class LinusFromString extends Linus {
	String source;
	int pos = 0;
	
	public LinusFromString(String theLine)	{
		source = theLine;
		}	// end of constructor

	
	public String makeLine()  {
		if (source == null) return null;
		Character newLine = new Character((char)10);
		int newLn = source.indexOf("\n", pos);
		if (newLn > -1)  {  //  internal newLines in the source string
			lineImage = source.substring(pos, newLn);
			pos = newLn + 1;
			if (pos == source.length()) source = null; //  We're done.
		}else {
			lineImage = source.substring(pos);
			source = null;
			}
		return lineImage + newLine;
		}  //  end of over-riding method makeLine

}  //  end of class LinusFromString
