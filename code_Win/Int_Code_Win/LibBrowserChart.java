import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.*;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

/**
 * This class creates a panel on which to draw family tree charts in a
 * LibBrowser.
 *
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class LibBrowserChart extends JPanel implements Scrollable {

    Context currentContext = null;
    int nameLabel, kinTermLabel;
    LibBrowser browser;
    boolean writeLtrLegend = true;  // controls printing of legend
    String textContent = "";
    String chartID = "A";
    static Font boldFont = new Font("Dialog", Font.BOLD, 12);  // Default font
    static Font regFont = new Font("Dialog", Font.PLAIN, 12);  // Default font
    int width = 1200, height = 600, offsetX = 0;
    boolean debug = false;
    boolean noRefresh = false;
            
    public LibBrowserChart(LibBrowser lb2) {
        browser = lb2;
    }

    /**Call super's paint method, to be safe; then set all fields and flags
     * for the <code>paintTree</code> method.
     * 
     * @param g the Graphics object on which to paint
     */
    public void paint(Graphics g) {
        super.paint(g);
        g.setFont(boldFont);
        if (currentContext != null && !noRefresh) {
            // Store prior values
            Context oldCtxt = Context.getCurrent();
            int oldNameLabel = Person.nameLabel;
            int oldKinTermLabel = Person.kinTermLabel;
            Context.setCurrent(currentContext);
            Person.nameLabel = nameLabel;
            Person.kinTermLabel = kinTermLabel;
            // temporarily set values needed for this chart
            paintTree(g);
            //  restore to prior state
            Context.setCurrent(oldCtxt);
            Person.nameLabel = oldNameLabel;
            Person.kinTermLabel = oldKinTermLabel;            
        }
    }
    
    /**For every Individual, Family, and Link whose home chart is the current
     * chartID, call their drawing methods. Put their symbol in the location
     * indicated on their 'loation' field. Draw Ego in red, Alter in blue, and
     * all linking kinsmen in orange.
     * 
     * @param g the Graphics object on which to paint 
     */
    public void paintTree(Graphics g) {
        int originX = 0, originY = 0, maxY = 0;
        int GAP = 5;
        g.setFont(regFont);
        Color originalColor = g.getColor();
        g.translate(originX, originY);
        Rectangle myRect = getBounds();
        myRect.setLocation(-originX, -originY);
        
        if (debug) {
        System.out.println("Bounds = " + myRect.x + ", " + myRect.x 
                        + ", " + myRect.width + ", " + myRect.height);
        }
        
        for (Link lk : currentContext.linkCensus) {
            if (!lk.deleted && lk.homeChart.equals(chartID)) {
                Individual p = lk.personPointedTo;
                if (p.name.equals("Ego")) {
                    lk.drawSymbol(g, myRect, Color.red);
                } else {
                    lk.drawSymbol(g, myRect, Link.defaultColor);
                }
                maxY = Math.max(maxY, lk.getLocationY());
                
                if (debug) {
                    System.out.println("\tLink to: I-" + p.serialNmbr + " at " + lk.location.x
                    + ", " + lk.location.y);
                }
            }
        }  //  end of loop thru Links, w/ default color = green
        for (Individual p : currentContext.individualCensus) {
            if (p != null && !p.deleted && p.homeChart.equals(chartID)) {
                if (p.name.equals("Ego")) {
                    // I want P's ID# to print on chart as 'name'    
                    p.name = "I-" + p.serialNmbr;
                    p.drawSymbol(g, myRect, Color.red);
                    p.name = "Ego";
                } else {
                    p.drawSymbol(g, myRect);
                }
                maxY = Math.max(maxY, p.getLocationY());
                
                if (debug) {
                    System.out.println("\tI-" + p.serialNmbr + " at " + p.location.x
                    + ", " + p.location.y);
                }
            }
        }  //  end of loop thru Individuals with defailt color = black
        for (Family m : currentContext.familyCensus) {
            if (m != null && m.homeChart.equals(chartID)) {
                m.drawSymbol(g, myRect, Color.black);
                m.drawLines(g, myRect);
                maxY = Math.max(maxY, m.getLocationY());
                
                 if (debug) {
                    System.out.println("\tF-" + m.serialNmbr + " at " + m.location.x
                    + ", " + m.location.y);
                }
            }
          }  //  enf of loop thru families, who draw lines to their members
        if (currentContext.specialRelationships != null
                && currentContext.specialRelationships.get(chartID) != null) {
            for (Context.SpecRelTriple sr : currentContext.specialRelationships.get(chartID)) {
                Rectangle pr = sr.parent.bounds(), kr = sr.child.bounds();
                int parentMidX = pr.x + (pr.width / 2),
                        kidMidX = kr.x + (kr.width / 2),
                        parBotY = pr.y + pr.height + GAP,
                        kidTopY = kr.y;
                Color clr = ((UserDefinedProperty) Context.getCurrent().userDefinedProperties.get(sr.udpName)).chartColor;
                g.setColor(clr);
                g.drawLine(parentMidX, parBotY, kidMidX, kidTopY);
            }
        }
        if (browser.letterLegend != null) {
            writeLegend(g, maxY + 15 + Library.gridY);
        }
        g.setColor(originalColor);
    }
    
    /**Write a legend on the bottom of each page of a diagram that uses letters
     * instead of kin terms. Begin writing at 20, maxY.
     * 
     * @param g     the Graphics object on which to paint
     * @param maxY  the vertical height 
     */
    public void writeLegend(Graphics g, int maxY) {
        if (writeLtrLegend) {
            if (browser.letterLegend.size() > 10) {
                writeLegend2Col(g, maxY);
                return;
            }
            for (String line : browser.letterLegend) {
                g.drawString(line, offsetX + 20, maxY);
                maxY += 15;
            }
        }
    }

    /**If the legend to be written has more than 10 lines, then this alternative
     * method will be called to write a 2-column legend.
     * 
     * @param g     the Graphics object on which to paint
     * @param maxY  the vertical height 
     */
    public void writeLegend2Col(Graphics g, int maxY) {
        ArrayList<String> leftCol = new ArrayList<>(),
                          rightCol = new ArrayList<>();
        int length = 0, titleLength, j;
        String title = browser.letterLegend.get(0);
        titleLength = title.length() * LibBrowser.factor;
        for (int i = 1; i < browser.letterLegend.size(); i++) {
            String str = browser.letterLegend.get(i);
            if (i % 2 == 1) {
                leftCol.add(str);
            } else {
                rightCol.add(str);
            }
            length = Math.max(length, str.length());
        }  //  left col size always is >= right size
        length = (length * LibBrowser.factor);
        int leftSize = leftCol.size(),
            titleStart = 10 + length - (titleLength / 2),
            rightStart = 20 + length,
            rightSize = rightCol.size();
        g.drawString(title, offsetX + titleStart, maxY);
        maxY += 15;
        for (j = 0; j < rightSize; j++) {
            g.drawString(leftCol.get(j), offsetX + 10, maxY);
            g.drawString(rightCol.get(j), offsetX + rightStart, maxY);
            maxY += 15;
        }
        if (j < leftSize) {  //  possible single leftover
            g.drawString(leftCol.get(j), offsetX + 10, maxY);
        }
    }

    public Dimension getPreferredSize() {
        Double d1 = (width + Library.gridX / 2) * 1.3,
               d2 = (height + Library.gridY / 2) * 1.3;
        int p1 = d1.intValue(),
            p2 = d2.intValue();
        return new Dimension(p1, p2);
    }

    public int[] chartSize(Rectangle viewArea) {
        int[] results = new int[4];
        int minX = 1000000, minY = 1000000, maxX = -1, maxY = -1;
        int extraX = Library.gridX, extraY = Library.gridY;
        Rectangle printArea = new Rectangle(0, 0, 100000, 100000);
        if (viewArea != null) {
            printArea = viewArea;
        }
        for (Family f : Context.getCurrent().familyCensus) {
            if (!f.deleted && f.homeChart.equals(chartID)
                    && printArea.contains(f.location)) {
                if (f.location.x < minX) {
                    minX = f.location.x;
                }
                if (f.location.y < minY) {
                    minY = f.location.y;
                }
                if (f.location.x + extraX > maxX) {
                    maxX = f.location.x + extraX;
                }
                if (f.location.y + extraY > maxY) {
                    maxY = f.location.y + extraY;
                }
            }
        }
        for (Individual ind : Context.getCurrent().individualCensus) {
            if (!ind.deleted && ind.homeChart.equals(chartID)
                    && printArea.contains(ind.location)) {
                if (ind.location.x < minX) {
                    minX = ind.location.x;
                }
                if (ind.location.y < minY) {
                    minY = ind.location.y;
                }
                if (ind.location.x + extraX > maxX) {
                    maxX = ind.location.x + extraX;
                }
                if (ind.location.y + extraY > maxY) {
                    maxY = ind.location.y + extraY;
                }
            }
        }
        for (Link lk : Context.getCurrent().linkCensus) {
            if (!lk.deleted && lk.homeChart.equals(chartID)
                    && printArea.contains(lk.location)) {
                if (lk.location.x < minX) {
                    minX = lk.location.x;
                }
                if (lk.location.y < minY) {
                    minY = lk.location.y;
                }
                if (lk.location.x + extraX > maxX) {
                    maxX = lk.location.x + extraX;
                }
                if (lk.location.y + extraY > maxY) {
                    maxY = lk.location.y + extraY;
                }
            }
        }
        if (browser.letterLegend != null && !browser.letterLegend.isEmpty()) {
            maxY += browser.letterLegend.size() * 20;
        }  //  adding length of legend at bottom of chart

        if (maxY == -1) {  //  never found a valid Y
            results[0] = 0;
            results[1] = 0;
        } else {
            results[0] = maxX - minX + extraX;
            results[1] = maxY - minY + extraY;
            results[2] = minX - extraX;
            results[3] = minY - extraY;
        }
        return results;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }

    public int getScrollableUnitIncrement(Rectangle visibleRect,
            int orientation, int direction) {
        return 20;
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,
            int orientation,
            int direction) {
        return 20;
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

}
