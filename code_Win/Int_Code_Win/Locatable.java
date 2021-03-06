import java.awt.Point;
import java.awt.Rectangle;

/**This interface is implemented by Families, Individuals and Links.
 * Any object that can be represented by a symbol (for its Kind) on a chart in
 * SILKin should implement this interface.
 *
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
*/
public interface Locatable {
    
    public int getSerialNmbr();
    
    public Point getLocation();
    
    public int getLocationX();

    public int getLocationY();

    public void setLocation(Point p);
    
    public void setLocation(int x, int y);
    
    public void setLocationX(int x);
    
    public void setLocationY(int y);
    
    public String getHomeChart();
    
    public Rectangle bounds();
}
