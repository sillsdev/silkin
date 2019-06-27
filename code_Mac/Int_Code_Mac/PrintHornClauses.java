import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

/**This class provides printing capability for kin term definitions (Horn
 * Clauses) in a Library Browser. Definitions are taken from LibBrowser's
 * originalText and expandedText areas.
 *
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class PrintHornClauses implements Printable {

    /**
     * The font to use for printing HCs, which will be different from the font
     * used in the ChartPanel.
     */
    public static Font tinyFont = new Font("Dialog", Font.PLAIN, 8);

    /**
     * Stores the page format (landscape or portrait) chosen by User in
     * EditPrefs.
     */
    public static PageFormat pgFormat = null;
    LibBrowserText lbText = null;
    LibBrowser browser;
    MyResBundle se = Library.screenElements;
    boolean ok = false;
    int nmbrOfPages = 0, contentHeight;
    Rectangle[] boundsArray;
    MyResBundle msgs = Library.messages;

    /**
     * Construct a PrintHornClauses object ready for printing
     *
     * @param js the scroll pane containing the lbText to be printed
     * @param ch is the browser lbText to be printed.
     * @param whole do we print the whole pane, or just the visible portion?
     */
    public PrintHornClauses(LibBrowserText ch, LibBrowser lb2) {
        lbText = ch;
        browser = lb2;
    }

    /**Make sure this print request is valid, then print yourself, guided by the
     * settings in the static variables.
     */
    public void printTheHCs() {
        if (browser.getOriginalText().getText().isEmpty()) {
            String msg = msgs.getString("noDef");
            JOptionPane.showMessageDialog(browser, msg,
                    msgs.getString("invalidPrintCommand"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        PrinterJob job = PrinterJob.getPrinterJob();
        if (pgFormat == null) {
            PageFormat defaultPage = job.defaultPage();
            defaultPage.setOrientation(PageFormat.LANDSCAPE);
            pgFormat = job.pageDialog(defaultPage);
        }
        // Using page format selected, calculate how many sheets wide and
        // high the printed output must be for whole lbText.
        int[] chartSize = lbText.pageSize(null);
        int pgHeight = (int) pgFormat.getImageableHeight(),
            pgWidth = (int) pgFormat.getImageableWidth();
        contentHeight = pgHeight - 46;  //  arrived at 46 by trial and error
        // The parameter "46" must be consistent with the parameter on line
        // 90 of LibBrowserText.java (2nd instruction in drawText method).
        int pagesWide = (chartSize[0] / pgWidth) + 1;
        int pagesHigh = (chartSize[1] / pgHeight) + 1;
        nmbrOfPages = pagesWide * pagesHigh;
        //   Now calculate the page bounds for each sheet to be printed
        boundsArray = new Rectangle[nmbrOfPages];
        int pgNmbr = 0, offsetX = chartSize[2], offsetY = chartSize[3];
        for (int w = 0; w < pagesWide; w++) {
            for (int h = 0; h < pagesHigh; h++) {
                int x = (w * pgWidth), y = (h * pgHeight);
                Rectangle pgBounds = new Rectangle(x + offsetX, y + offsetY, pgWidth, pgHeight -45);
                boundsArray[pgNmbr++] = pgBounds;
            }
        }
        job.setPrintable(this, pgFormat);
        ok = job.printDialog();
        if (ok) {
            try {
                job.print();  // This eventualy calls print method below.
            } catch (PrinterException ex) {
                Context.breakpoint();
            }
        }
    }

    /**This method, called by PrinterJob.print(), formats the header (title)
     * line for a page, then prints the requested page if it exists.
     * 
     * @param g     the Graphics object on which to print
     * @param pf    the chosen page format
     * @param page  the page number to be printed (zero-based)
     * @return      a numeric flag indicating the status of this page
     * @throws PrinterException 
     */
    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        Graphics2D g2d = (Graphics2D) g;
        int chartPageNmbr = page + 1;
        String language = browser.langName,
               term = browser.getTermPickSelection(),
               claws =  browser.getClausePickSelection(),
               expansion = browser.getExpandPickSelection(),
               titleEnd;
        if (claws.equals(se.getString("all")) 
                && expansion.equals(se.getString("all"))) {
            titleEnd = msgs.getString("in") + language;
        } else if (! expansion.equals(se.getString("all"))) {
            titleEnd = msgs.getString("expClause#") + expansion + ")" 
                    +  msgs.getString("in") + language;
        } else {
            titleEnd = msgs.getString("baseClause#") + claws + ")" 
                    +  msgs.getString("in") + language;
        }
        String chartTitle = msgs.getString("definitionOf") + term + titleEnd;
            if (page >= nmbrOfPages) {
                return NO_SUCH_PAGE;
            } else {
                //  Print the page called for
                Rectangle segmentBounds = boundsArray[page];
                int offsetX = segmentBounds.x,
                    offsetY = segmentBounds.y;
                g2d.translate(pf.getImageableX() - offsetX,
                        pf.getImageableY() - offsetY);
                // Print page header
                g2d.setFont(tinyFont);
                String header = chartTitle + "\t\t pg " + chartPageNmbr;                 
                g2d.drawString(header, offsetX + 5, offsetY + 15);
                lbText.drawText(g2d, page,
                        (page * contentHeight) + 10,            // height to start printing
                        (chartPageNmbr * contentHeight) + 10);  //  height to stop printing
                return PAGE_EXISTS;
            }
        
    }
}
