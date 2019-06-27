import java.io.File;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

/**This class provides printing capability for Family Tree charts in SILKin.
 * Charts (from the SIL_Edit main window) are drawn in the ChartPanel inside 
 * a JScrollPanel.
 *
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class PrintChart implements Printable {

    /** The font to use for printing charts, which will be different from the
     *  font used in the ChartPanel.
     */public static Font printFont;
    public static Font tinyFont = new Font("Dialog", Font.PLAIN, 8);
    /**
     * Stores the page format (landscape or portrait) chosen by User in EditPrefs.
     */public static PageFormat pgFormat = null;
    ChartPanel chart = null;
    JScrollPane pane;
    boolean wholeChart, ok = false;
    int nmbrOfPages = 0;
    Rectangle[] boundsArray;
    MyResBundle msgs = Library.messages;

    /**
     * Construct a PrintChart object ready for printing
     * 
     * @param js the window pane to be printed
     * @param whole do we print the whole pane, or just the visible portion?
     */
    public PrintChart(JScrollPane js, boolean whole) {
        pane = js;
        wholeChart = whole;
        if (printFont == null) {
            printFont = new Font("Dialog", Font.PLAIN, 8);
        }
    }

    /**
     * Make sure this print request is valid, then print yourself, guided by 
     * the settings in the static variables.
     */
    public void printTheChart() {
        chart = SIL_Edit.edWin.chart;
        if (chart == null) {
            String msg = msgs.getString("chartVisibleB4Print");
            JOptionPane.showMessageDialog(SIL_Edit.edWin, msg, 
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
        // high the printed output must be for whole chart.
        int[] chartSize = chart.chartSize(null);
        if (chartSize[0] == 0) {
            String msg = msgs.getString("atLeastOneSymbol");
            JOptionPane.showMessageDialog(SIL_Edit.edWin, msg,
                    msgs.getString("invalidPrintCommand"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (wholeChart) {
            int pgHeight = (int) pgFormat.getImageableHeight() - Library.gridY,
                    pgWidth = (int) pgFormat.getImageableWidth() - Library.gridX;
            int pagesWide = (chartSize[0] / pgWidth) + 1;
            int pagesHigh = (chartSize[1] / pgHeight) + 1;
            nmbrOfPages = pagesWide * pagesHigh;
            //   Now calculate the page bounds for each sheet to be printed
            boundsArray = new Rectangle[nmbrOfPages];
            int pgNmbr = 0, offsetX = chartSize[2], offsetY = chartSize[3];
            for (int w = 0; w < pagesWide; w++) {
                for (int h = 0; h < pagesHigh; h++) {
                    int x = (w * pgWidth), y = (h * pgHeight);
                    Rectangle pgBounds = new Rectangle(x + offsetX, y + offsetY, pgWidth, pgHeight);
                    boundsArray[pgNmbr++] = pgBounds;
                }
            }
        }
        Font oldChartFont = ChartPanel.chartFont;
        ChartPanel.chartFont = printFont;
        job.setPrintable(this, pgFormat);
        ok = job.printDialog();
        if (ok) {
            try {
                job.print();
            } catch (PrinterException ex) {
                Context.breakpoint();
            }
        }
        ChartPanel.chartFont = oldChartFont;
    }  

    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        Graphics2D g2d = (Graphics2D) g;
        Context ctxt = Context.current;
        int chartPageNmbr = page + 1;
        String chartTitle = ctxt.currentChart;
        chartTitle += ": " + ctxt.getChartDescription(ctxt.currentChart);
        if (wholeChart) {
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
                String header = chartTitle + " pg " + chartPageNmbr;
                File saveFile = SIL_Edit.edWin.chart.saveFile;
                if (saveFile != null) {
                    header += "\t\t\t\t\t\t\t\tFrom: " + saveFile.getName();
                }
                g2d.drawString(header, offsetX + 5, offsetY + 15);
                chart.paint0(g2d, segmentBounds, ctxt.currentChart);
                return PAGE_EXISTS;
            }
        } else {  // Print just visible portion: Page 0
            if (page > 0) {
                return NO_SUCH_PAGE;
            }
            Rectangle segmentBounds = pane.getViewport().getViewRect();
            // Adjust bounds to remove white space left and top
            int[] chartSize = SIL_Edit.edWin.chart.chartSize(segmentBounds);
            // chartSize: width, height, offsetX, offsetY
            int offsetX = chartSize[2],
                    offsetY = chartSize[3];
            g2d.translate(pf.getImageableX() - offsetX,
                    pf.getImageableY() - offsetY);
            chart.paint0(g2d, segmentBounds, ctxt.currentChart);
            return PAGE_EXISTS;
        }
    }
}
