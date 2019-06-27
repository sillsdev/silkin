
import javax.swing.*;

/**This is the original principal class of the academic SILKin program. It was 
 * designed to learn kinship terminology for a new culture from a collection 
 * of {@link Dyad} objects. Learn mode is preserved in case any future experiments
 * are conducted re: the Machine Learning features.
 * 
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 * 
 * Dates:  Begun in 2002. 
 * Dissertation based on this system accepted 2008.
 * User Interface, based on KAES code, begun 2010.
 */
public class SILKin extends JFrame {

    /** This class variable holds the pointer to the main SILKin window
     *  containing all the menus etc.
     */ 
    public static MainPane mainPane;
    static String runMode = "SIL_Edit";

    /**  The main method of SILKin runs the code in one of 2 modes: 
     * - SIL_Edit mode is normal operation for users
     * - learn mode was used during development to test the machine learning routines
     * - KAES mode is no longer used. It was originally intended to preserve 
     *   the option of running the original KAES system.
     * 
     * @param args 
     */
    public static void main(String[] args) {   // System Initialization Method
        //  runMode = "KAES";
        //  runMode = "learn";
        String os = System.getProperty("os.name");
//        if (os.equals("Mac OS X")) {
//            com.apple.eawt.Application app = com.apple.eawt.Application.getApplication();
//            app.setAboutHandler(new AboutBox());
//        }
        try {
            //Schedule a job for the event-dispatching thread: creating and showing a GUI.
            if (runMode.equals("learn")) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        createAndShowGUI();
                    }
                });
            } else if (runMode.equals("SIL_Edit")) {
                javax.swing.SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        SIL_Edit.main(null);
                    }
                });
            } else {
                throw new KSInternalErrorException("No run mode selected.");
            }
        } catch (Exception exc) {
            System.out.println("Error: " + exc);
        }//
    }  //  end of static method main

    /**  Create the Kinship system's main GUI and show it.  For thread safety,
     *  this method is invoked from the event-dispatching thread by 'main()'.  
     *  
     *  This method is only used by System Admins, and therefore defaults to 
     *  English as the menu language. But if perchance this method were called
     *  from the Internationalized version, it will support other languages.
     */
    static void createAndShowGUI() {
        try {
            String title1 = "SILKin: Kinship Analysis",
                   title2 = "Only 3 Modes of Operation have been implemented";                
            if (Library.screenElements != null) {
                title1 = Library.screenElements.getString("sILKinKinshipAnalysis");
                title2 = Library.screenElements.getString("only3ModesImplemented");
                }
            mainPane = new MainPane(title1);            
            Predicate.loadPrimitiveNames();
            if (Library.stubs.isEmpty()) {
                Library.readStubFile();
            }
            if (Library.currentActivity == Library.BROWSING) {
                LibBrowser browser = new LibBrowser();
                mainPane.addWindow(browser);
            } else if (Library.currentActivity == Library.DATA_GATHERING) {
                mainPane.editCUC();
            } else if (Library.currentActivity == Library.ADMIN) {
                //  Present blank MainPane
            } else {
                System.out.println(title2);
            }
            mainPane.setVisible(true);
        } catch (Exception e) {
            System.out.println(e);
        }
    }	// end of static method createAndShowGUI
}	//  end of class SILKin
