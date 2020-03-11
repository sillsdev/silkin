import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.*;

/**
 * This class creates a panel on which to draw kin term Horn Clauses from a
 * LibBrowser. This panel remains invisible, since the HCs are already displayed
 to the User in LibBrowser's split pane. This is just a 'staging area' for
 HCs to be printed.
 *
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class LibBrowserText extends javax.swing.JPanel {
    
    public static final int width = 600, height = 800; 
    LibBrowser browser;
    KinTermDef ktd;
    MyResBundle se = Library.screenElements;
    static Font boldFont = new Font("Dialog", Font.BOLD, 12);  // Default font
    static Font regFont = new Font("Dialog", Font.PLAIN, 10);  // Default font
    int maxX = 0, maxY = 0, charWidth = 5;
    ArrayList<PrintLine> content = new ArrayList<>();
    
    public LibBrowserText(LibBrowser lb2) {
        browser = lb2;
        ktd = browser.currentKTD;
        prepareContent();
    }
    
    public void paint(Graphics g) {
        super.paint(g);
//        drawText(g, 0, 999999);
    }

    public void prepareContent() { 
        int nmbrOfLines = ktd.definitions.size();  //  Nmbr of base clauses
        String plural = "", line;
        if (nmbrOfLines > 1 && 
                browser.getClausePickSelection().equals(se.getString("all"))) {
            plural = "s";
        }  //  Put a header over the base clause(s)
        line = "\tBase Clause" + plural;
        content.add(new PrintLine(30, 20, line, boldFont));
        maxX = Math.max(maxX, line.length());
        // Now add the base clauses
        int newY = 45;
        ArrayList<String> cbList = ktd.toStringAList("original", 
                browser.includePrimary(), 
                browser.includeExtended());
        for (String str : cbList) {
            if (str.contains("Ego")) {
                newY += 5;
            }
            content.add(new PrintLine(10, newY, str, regFont));
            maxX = Math.max(maxX, str.length());
            newY += 15;
        }            
        //  Now make a header over expanded clauses
        nmbrOfLines = ktd.expandedDefs.size();
        if (nmbrOfLines > 1 && 
                browser.getExpandPickSelection().equals(se.getString("all"))) {
            plural = "s";
        } else {
            plural = "";
        }
        line = se.getString("expClause") + plural;
        content.add(new PrintLine(30, newY +10, line, boldFont));
        newY += 25;
        maxX = Math.max(maxX, line.length());
        cbList = ktd.toStringAList("expanded", 
                browser.includePrimary(), 
                browser.includeExtended());
        for (String str : cbList) {
            if (str.contains("Ego")) {
                newY += 5;
            }
            content.add(new PrintLine(10, newY, str, regFont));
            maxX = Math.max(maxX, str.length());
            newY += 15;
        }
        maxY = newY;
        maxX *= charWidth;
    }

    public void drawText(Graphics g, int page, int start, int stop) {
        g.translate(0, 0);
        int offsetY = (page * 46 + 20);        
        for (PrintLine pl : content) {
            if (pl.y >= start && pl.y < stop) {
                g.setFont(pl.font);
                g.drawString(pl.line, pl.x, pl.y + offsetY);
            }
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    public int[] pageSize(Rectangle viewArea) {
        return new int[] {maxX, maxY, 0, 0}; 
    }
    
    /**This inner class is a convenience. It represents one line to be printed.
     * 
     */
    public class PrintLine {
        int x,y;
        String line;
        Font font;
        
        public PrintLine(int theX, int theY, String theLine, Font theFont) {
            x = theX;
            y = theY;
            line = theLine;
            font = theFont;
        }
    }  //  end of inner class PrintLine
    
}
