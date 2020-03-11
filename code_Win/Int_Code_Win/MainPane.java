import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.filechooser.*;
import java.beans.*;

/** The MainPane class was the top-level window for this application during the 
 * academic phase of this project, holding all other windows in its contentPane 
 * (which is a JDesktopPane).  There should be only one instance of MainPane.  
 * The static fields <code>activity</code> and <code>openBrowsers</code> hold, 
 * respectively, the system-wide Error Log and a list of all open LibBrowser 
 * windows (for good housekeeping purposes).<p>
 * This class creates the MenuBar and is the ActionListener which implements 
 * menu choices.  Menu choices which lead to exceptions trigger the instantiation 
 * of the Error Log and posting to it.  The Error Log is intended to record all
 * problems encountered and recommended solutions.  Therefore, all methods that 
 * post to it must provide enough information about the problem for the User to 
 * know what specific action to take much later when the log is reviewed.
 * </p>
 * 
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class MainPane extends JFrame implements ActionListener {

    public static KSJInternalFrame activity;  //  single activity log for all windows
    static ArrayList<Object> openBrowsers = new ArrayList<Object>(), 
            openPersonEditors = new ArrayList<Object>(),
            openFamilyEditors = new ArrayList<Object>();
    public static ContextEditor curr_CUC_Editor;
    public static JDesktopPane desktop;
    public static MainPane topPane;
    public static int testSerialNmbr;
    public static int buildNmbr = 511;
    public static int NUMBER_OF_EGOS = 4;
    public static boolean fill_In_Flag = false;
    public static boolean emergencyExit = false;
    static MyResBundle se = Library.screenElements,
                     msgs = Library.messages;
    private JMenuBar menuBar = new JMenuBar();
    KSJInternalFrame currentFrame;
    Runtime topDog = Runtime.getRuntime();
    JFileChooser fc = null;
    File currDomTheoryDir = null, currGEDCOMDir = null;
    int numWindows = 0;

    public MainPane(String title) {
        super(title);
        createMenu();
        labelMenuItems();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        desktop = new JDesktopPane();
        topPane = this;
        setContentPane(desktop);
        Toolkit tKit = getToolkit();
        createActivityLog(desktop, menuView);
        Dimension wndSize = tKit.getScreenSize();
        int maxHi = 768, maxWide = 1024;
        setBounds(40, 0,
                Math.min(maxWide, (int) (wndSize.width / 1.1)),
                Math.min(maxHi, (int) (wndSize.height / 1.1)));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Listen for window closing, so Library.stubs file can be saved before exit.
        topDog.addShutdownHook(new CleanUpThread());
        setVisible(false);
    }  //  end of constructor

    public static void displayError(String msg, String title, int typ) {
        if (activity == null) {
            createActivityLog(desktop, menuView);
        }
        JFrame topWindow = topPane;
        if (topWindow == null || !topWindow.isVisible()) {
            topWindow = SIL_Edit.edWin;
        }
        JOptionPane.showMessageDialog(topWindow, msg, title, typ);
        MainPane.activity.log.append(msg + "\n\n");
    }

    /**This utility method returns a "deep copy" with new elements replacing 
     * any subordinate TreeMaps or ALists as VALUES in the model. 
     * KEYS are not deep-copied.        
     * 
     * @param model the TreeMap to be copied
     * @return      an exact replica, with all elements copies of the original elements.
     */ 
    public static TreeMap treeMapDeepCopy(TreeMap model) {
        TreeMap newTM = new TreeMap();
        Iterator topIter = model.entrySet().iterator();
        while (topIter.hasNext()) {
            Map.Entry entry = (Map.Entry) topIter.next();
            Object key = entry.getKey(),
                    value = entry.getValue(),
                    newValue;
            if (value instanceof TreeMap) {
                newValue = treeMapDeepCopy((TreeMap) value);
            } else if (value instanceof ArrayList) {
                newValue = new ArrayList<Object>((ArrayList<Object>) value);
            } else {
                newValue = value;
            }
            newTM.put(key, newValue);
        }  // end of loop thru top level of model TMap
        return newTM;
    }  //  end of method treeMapDeepCopy

    /** Add a window to the desktop, and make it the current focus. 
     * 
    @param wind  A window to be added to the system's desktop & displayed. */
    public void addWindow(KSJInternalFrame wind) {
        currentFrame = wind;
        numWindows++;
        wind.desktop = desktop;
        wind.miViewMe = menuView.add(wind.windowNum);
        wind.miViewMe.addActionListener(wind);
        wind.menuView = menuView;
        desktop.add(wind);
        wind.show();
    }
    
    public void updateViewMenu(String oldTxt, String newTxt) {
        for (int i=0; i < menuView.getItemCount(); i++) {
            JMenuItem item = menuView.getItem(i);
            if (item.getText().equals(oldTxt)) {
                item.setText(newTxt);
                break;
            }
        }
    }
    
    // MENU-BUILDING VARIABLES
    JMenu menuFile = new JMenu(),
          menuEdit = new JMenu(),
          menuFileAdd2Library = new JMenu(),
          menuFileExport = new JMenu(),
          menuAdmin = new JMenu();
    static JMenu menuView = new JMenu();
    JMenu menuHelp = new HelpFrame.HMenu();
    JMenuItem miFileClose = new JMenuItem(),
            miFileAdd2LibraryBatch = new JMenuItem(),
            miFileAdd2LibraryCen = new JMenuItem(),
            miFileAdd2LibraryGEDCOM = new JMenuItem(),
            miFileDelete = new JMenuItem(), 
            miFileAdd2LibraryDT = new JMenuItem(),
            miFileNewBrowser = new JMenuItem(), 
            miFileSaveContextAs = new JMenuItem(),
            miFileExportDT = new JMenuItem(),
            miFileExportCen = new JMenuItem(),
            miFileExportGEDCOM = new JMenuItem(),
            miFileExit = new JMenuItem(),
            miEditUnDo = new JMenuItem(),
            miEditReDo = new JMenuItem(),
            miEditCut = new JMenuItem(new DefaultEditorKit.CutAction()),
            miEditCopy = new JMenuItem(new DefaultEditorKit.CopyAction()),
            miEditPaste = new JMenuItem(new DefaultEditorKit.PasteAction()),
            miEditPrefs = new JMenuItem(),
            miEditUserContext = new JMenuItem(),
            miTest1 = new JMenuItem(),
            miTest4 = new JMenuItem(),
            miTest6 = new JMenuItem(),
            miTest7 = new JMenuItem(),
            miTest8 = new JMenuItem(),
            miFileSaveContext = new JMenuItem();
    
    
           

    public void createMenu() {
        // menu bar
        setJMenuBar(menuBar);

        menuFile.setMnemonic(KeyEvent.VK_F);
        menuEdit.setMnemonic(KeyEvent.VK_E);        
        menuView.setMnemonic(KeyEvent.VK_V);
        menuHelp.setMnemonic(KeyEvent.VK_H);
        HelpFrame.window = new HelpFrame();
        
        menuFile.add(miFileNewBrowser);
        miFileNewBrowser.setActionCommand("new browser");
        miFileNewBrowser.addActionListener(this);

        menuFile.add(miFileClose);
        miFileClose.setActionCommand("close window");
        miFileClose.addActionListener(this);

        menuFileAdd2Library.add(miFileAdd2LibraryDT);
        miFileAdd2LibraryDT.setActionCommand("add domain theory");
        miFileAdd2LibraryDT.addActionListener(this);
        menuFileAdd2Library.add(miFileAdd2LibraryBatch);
        miFileAdd2LibraryBatch.setActionCommand("add batch of domain theories");
        miFileAdd2LibraryBatch.addActionListener(this);
        menuFileAdd2Library.add(miFileAdd2LibraryCen);
        miFileAdd2LibraryCen.setEnabled(false);
        menuFileAdd2Library.add(miFileAdd2LibraryGEDCOM);
        miFileAdd2LibraryGEDCOM.setEnabled(false);
        menuFile.add(menuFileAdd2Library);

        menuFile.add(miFileDelete);
        miFileDelete.setActionCommand("delete from library");
        miFileDelete.addActionListener(this);

        menuFile.addSeparator();

        menuFile.add(miFileSaveContext);
        miFileSaveContext.setActionCommand("save context");
        miFileSaveContext.addActionListener(this);
        menuFile.add(miFileSaveContextAs);
        miFileSaveContextAs.setActionCommand("save context as");
        miFileSaveContextAs.addActionListener(this);
        
        menuFileExport.add(miFileExportDT);
        miFileExportDT.setActionCommand("export domain theory");
        miFileExportDT.addActionListener(this);
        menuFileExport.add(miFileExportCen);
        miFileExportCen.setEnabled(true);
        menuFileExport.add(miFileExportGEDCOM);
        miFileExportGEDCOM.setActionCommand("export GEDCOM");
        miFileExportGEDCOM.addActionListener(this);
        menuFile.add(menuFileExport);

        menuFile.addSeparator();
        
        menuFile.add(miFileExit);
        miFileExit.setActionCommand("exit");
        miFileExit.addActionListener(this);

        menuEdit.add(miEditUnDo);
        miEditUnDo.setEnabled(false);
        menuEdit.add(miEditReDo);
        miEditReDo.setEnabled(false);
        
        menuEdit.addSeparator();

        miEditCut.setMnemonic(KeyEvent.VK_T);
        menuEdit.add(miEditCut);
        miEditCopy.setMnemonic(KeyEvent.VK_C);
        menuEdit.add(miEditCopy);
        miEditPaste.setMnemonic(KeyEvent.VK_P);
        menuEdit.add(miEditPaste);

        menuEdit.addSeparator();

        menuEdit.add(miEditPrefs);
        miEditPrefs.setActionCommand("edit prefs");
        miEditPrefs.addActionListener(this);
        menuEdit.add(miEditUserContext);
        miEditUserContext.setActionCommand("edit user context");
        miEditUserContext.addActionListener(this);

        menuAdmin.add(miTest1);
        miTest1.setActionCommand("Compute Similarity Matrix");
        miTest1.addActionListener(this);
        menuAdmin.add(miTest4);
        miTest4.setActionCommand("Cluster the Feature Vectors");
        miTest4.addActionListener(this);
        menuAdmin.add(miTest6);
        miTest6.setActionCommand("Gen Indexes");
        miTest6.addActionListener(this);
        menuAdmin.add(miTest7);
        miTest7.setActionCommand("Merge Indexes");
        miTest7.addActionListener(this);
        menuAdmin.add(miTest8); 
        miTest8.setActionCommand("Index & Merge");
        miTest8.addActionListener(this);
        
        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuView);
        menuBar.add(menuAdmin);
        menuBar.add(menuHelp);
        menuAdmin.setEnabled(Library.currentActivity == Library.ADMIN);
        menuBar.validate();
    }  //  end of method createMenu
    
    public void labelMenuItems() {
        MyResBundle labels = Library.menuItems;        
        menuFile.setText(labels.getString("file"));
        menuEdit.setText(labels.getString("edit"));
        menuAdmin.setText(labels.getString("admin"));
        menuHelp.setText(labels.getString("help"));
        menuView.setText(labels.getString("view"));
        miFileNewBrowser.setText(labels.getString("newLiBrowser"));
        miFileClose.setText(labels.getString("closeWindow"));
        menuFileAdd2Library.setText(labels.getString("add2Lib"));
        miFileAdd2LibraryDT.setText(labels.getString("domTh"));
        miFileAdd2LibraryBatch.setText(labels.getString("batchAdd"));
        miFileAdd2LibraryCen.setText(labels.getString("peopleData"));
        miFileAdd2LibraryGEDCOM.setText(labels.getString("gedcomFile"));
        miFileDelete.setText(labels.getString("deleteFromLib"));
        miFileSaveContext.setText(labels.getString("saveCtxt"));
        miFileSaveContextAs.setText(labels.getString("saveCtxtAs"));
        menuFileExport.setText(labels.getString("exportFile"));
        miFileExportDT.setText(labels.getString("domTheory"));
        miFileExportCen.setText(labels.getString("peopleData"));
        miFileExportGEDCOM.setText(labels.getString("gedcomFile"));
        miFileExit.setText(labels.getString("exit"));
        miEditUnDo.setText(labels.getString("undo"));
        miEditReDo.setText(labels.getString("redo"));
        miEditCut.setText(labels.getString("cut"));
        miEditCopy.setText(labels.getString("copy"));
        miEditPaste.setText(labels.getString("paste"));
        miEditPrefs.setText(labels.getString("editPrefs"));
        miEditUserContext.setText(labels.getString("editUserCtxt"));
        miTest1.setText(labels.getString("compSim"));
        miTest4.setText(labels.getString("clusterFVects"));
        miTest6.setText(labels.getString("genIndexes"));
        miTest7.setText(labels.getString("mergeIndexes"));
        miTest8.setText(labels.getString("merge1"));
    }

    
    public void enableAdvancedMenuItems(boolean bool) {
        menuFileAdd2Library.setEnabled(bool);
        miFileDelete.setEnabled(bool);
        menuAdmin.setEnabled(bool);
        
    }
    
    /**This is the Listener that handles all menu choices. There are four main
     * menu choices:
     * <ol>
     * <li>When e='add domain theory' we ask User to point to the file, then load
     * it from disk, create a FeatureVector, postDistributions & write them to 
     * disk. Update any open Library Browsers to reflect this new domain theory
     * that may be viewed.
     * </li><li>When e='delete from Library' we ask User to point to the domain 
     * theory to be deleted. We offer the option to delete all, which is useful 
     * when you wish to re-build the Library and its indexes etc. from scratch.
     * </li><li>When e='export domain theory' we ask User to point to the theory
     * to be exported, then write it out. We want to export a file that looks 
     * like a manually-built domain theory, so we must wipe out all the 
     * expandedDefs. When this file is added to the SILKin Library (by an 
     * administrator) the expansions will be calculated and re-added.
     * </li><li>When e='new browser' we simply call <code>browseLibrary()</code>.
     * </li>
     * </ol>
     * 
     * @param e the GUI event that triggered this call.
     */
    public void actionPerformed(ActionEvent e) {
        if(msgs == null || se == null) {
            if(Library.messages == null || Library.screenElements == null) {
            Library.setMenuLanguage();
            }
            msgs = Library.messages;
            se = Library.screenElements;
        }
        if (e.getActionCommand().equals("new browser")) {
            browseLibrary();
        } //  end of action-is-new-browser
        else if (e.getActionCommand().equals("Match dyadsUndefined")) {
            try {
                DomainTheory.current.matchDyads();
            } catch (Exception ex) {
                activity.log.append("While matching dyadsUndefined, " + ex + "\n\n");
            }
        } //  end of action-is-Match-dyadsUndefined
        else if (e.getActionCommand().equals("close window")) {
            KSJInternalFrame frame = (KSJInternalFrame) desktop.getSelectedFrame();
            if (frame != null) {
                frame.doDefaultCloseAction();
            } else {
                setVisible(false);
            }
        } //  end of action-is-close-window
        else if (e.getActionCommand().equals("exit")) {
            if (SIL_Edit.edWin != null
                    && SIL_Edit.edWin.chart.dirty) {
                SIL_Edit.edWin.chart.doWantToSave();
            }
            System.exit(0);
        } else if (e.getActionCommand().equals("delete from library")) {
            //  First, let User choose what to language to delete.
            String[] langs = Library.genCtxtMenu("All");
            String victim = (String) JOptionPane.showInputDialog(desktop,
                    msgs.getString("1)ForWhatLanguage?"),
                    msgs.getString("whatToDelete"),
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    langs,
                    langs[0]);
            //Check for cancellation.
            if (victim == null) {
                return;
            }
            if (victim.equals("All")) {
                String vict;
                Library.ContextStub cs1;
                File aboutToDi;
                for (int k = 1; k < langs.length; k++) {
                    vict = langs[k];
                    cs1 = Library.retrieveOrCreateStub(vict);
                    Library.removeContextStub(cs1);
                    aboutToDi = new File(Library.libraryCtxtDirectory + vict + ".ctxt");
                    try {
                        aboutToDi.delete();
                    } catch (Exception exc) {
                        //  Exception means already deleted. That's fine.
                    }  //  end of catch block
                }
                Library.cbIndex = null;
                try {
                    Library.writeStubFile();
                } catch (JavaSystemException exc) {
                    activity.log.append("While writing Library stub file:\n" + exc + "\n\n");
                }
            } //  end of delete-All
            else {
                Library.ContextStub cs = Library.retrieveOrCreateStub(victim);
                Context actxt = (Context) Library.activeContexts.get(victim);
                String[] choices;
                if (cs.adrThyExists && cs.refThyExists) {  //  a context with 2 domain theories
                    choices = new String[4];
                    choices[3] = msgs.getString("bothDomainTheories");
                    choices[1] = victim + " " + msgs.getString("domainTheory");
                    choices[2] = victim + " " + msgs.getString("adrDomainTheory");
                    choices[0] = msgs.getString("theEntire") + " " 
                            + victim + " " + msgs.getString("context+2");
                } else if (cs.refThyExists) {
                    choices = new String[2];
                    choices[1] = victim + " " + msgs.getString("domainTheory");
                    choices[0] = msgs.getString("theEntire") + " "  + victim 
                            + " " + msgs.getString("contextAndDT");
                } else if (cs.adrThyExists) {
                    choices = new String[2];
                    choices[1] = victim + " " + msgs.getString("adrDomainTheory");
                    choices[0] = msgs.getString("theEntire") + " "  + victim 
                            + " " + msgs.getString("contextAndDT");
                } else {
                    choices = new String[1];
                    choices[0] = msgs.getString("theEntire") + " " + victim 
                            + " " + msgs.getString("contextNoDT");
                }
                String choice = (String) JOptionPane.showInputDialog(desktop,
                        msgs.getString("2)WhichElements"),
                        msgs.getString("whatToDelete"),
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        choices,
                        choices[0]);
                if (choice == null) {
                    return;
                }
                String[] onlyAlso = {msgs.getString("cardONLY"), 
                                     msgs.getString("diskALSO") };
                String eraser = (String) JOptionPane.showInputDialog(desktop,
                        msgs.getString("3)Delete"),
                        msgs.getString("whatToDelete"),
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        onlyAlso,
                        msgs.getString("diskALSO"));
                if (eraser == null) {
                    return;
                }
                String filePath = Library.libraryCtxtDirectory + victim;
                File aboutToDie;
                try {
                    if (choice.startsWith(msgs.getString("theEntire"))) {  //  kill the whole context
                        Library.removeContextStub(cs);
                        Library.writeStubFile();
                        if (eraser.startsWith(msgs.getString("diskALSO"))) {
                            aboutToDie = new File(filePath + ".ctxt");
                            aboutToDie.delete();
                            filePath = Library.thyDirectory + victim;
                            aboutToDie = new File(filePath + ".thy");
                            if (aboutToDie.exists()) {
                                aboutToDie.delete();
                            }
                            aboutToDie = new File(filePath + "(Adr).thy");
                            if (aboutToDie.exists()) {
                                aboutToDie.delete();
                            }
                        }  //  end of erase-disk-also
                    } else if (choice.startsWith(msgs.getString("bothDomainTheories"))) {  //  choice-equals-both-theories
                        cs.refThyExists = false;
                        cs.adrThyExists = false;
                        if (actxt == null) {
                            actxt = Library.readContextFromDisk(filePath + ".ctxt");
                        }  //  end of this-context-was-not-active
                        actxt.domTheoryAdrNullify();
                        actxt.domTheoryRefNullify();
                        if (eraser.startsWith(msgs.getString("diskALSO"))) { //  erase disk = over-write .ctxt file
                            Library.saveContextToDisk(actxt);
                            filePath = Library.thyDirectory + victim;
                            aboutToDie = new File(filePath + ".thy");
                            if (aboutToDie.exists()) {
                                aboutToDie.delete();
                            }
                            aboutToDie = new File(filePath + "(Adr).thy");
                            if (aboutToDie.exists()) {
                                aboutToDie.delete();
                            }
                        }
                    } else if (choice.contains(msgs.getString("adrDomainTheory"))) {
                        cs.adrThyExists = false;
                        if (actxt == null) {
                            actxt = Library.readContextFromDisk(filePath + ".ctxt");
                        }  //  end of this-context-was-not-active
                        actxt.domTheoryAdrNullify();
                        if (eraser.startsWith(msgs.getString("diskALSO"))) {	//  erase disk = over-write .ctxt file
                            Library.saveContextToDisk(actxt);
                            filePath = Library.thyDirectory + victim;
                            aboutToDie = new File(filePath + "(Adr).thy");
                            if (aboutToDie.exists()) {
                                aboutToDie.delete();
                            }
                        }
                    } else {
                        cs.refThyExists = false;
                        if (actxt == null) {
                            actxt = Library.readContextFromDisk(filePath + ".ctxt");
                        }  //  end of this-context-was-not-active
                        actxt.domTheoryRefNullify();
                        if (eraser.startsWith(msgs.getString("diskALSO"))) {	//  erase disk = over-write .ctxt file
                            Library.saveContextToDisk(actxt);
                            filePath = Library.thyDirectory + victim;
                            aboutToDie = new File(filePath + ".thy");
                            if (aboutToDie.exists()) {
                                aboutToDie.delete();
                            }
                        }
                    }
                } catch (Exception exc) {
                    activity.log.append("While deleting Library files:\n" + exc + "\n\n");
                }
            }  //  end of don't-delete-all
            for (int i = 0; i < MainPane.openBrowsers.size(); i++) {
                Object o = MainPane.openBrowsers.get(i);
                        if (o instanceof LibBrowser) {
                            LibBrowser lb = (LibBrowser) o;
                            lb.refreshLangMenu();
                        }
            }  //  end of loop thru MainPane.openBrowsers
            JOptionPane.showMessageDialog(desktop,
                    msgs.getString("deletionsCompleted"),
                    msgs.getString("taskCompleted"),
                    JOptionPane.PLAIN_MESSAGE);
        } //  end of action-command-is-delete
        else if (e.getActionCommand().equals("add domain theory")) {
            if (fc == null) {
                fc = new JFileChooser();
            }
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setDialogTitle(msgs.getString("fileHoldingDT"));
            if (currDomTheoryDir != null) {
                fc.setCurrentDirectory(currDomTheoryDir);
            }
            //  Display File-Open dialog and get User's selection
            int returnVal = fc.showOpenDialog(desktop);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                try {
                    Library.loadNewDomTh(file);
                    DomainTheory dt = DomainTheory.current;
                    Context.simulation = false;
                    dt.ctxt.simDataGen = false;
                    String lang = dt.languageName, filePath;
                    for (int i = 0; i < MainPane.openBrowsers.size(); i++) {
                        Object o = MainPane.openBrowsers.get(i);
                        if (o instanceof LibBrowser) {
                            LibBrowser lb = (LibBrowser) o;
                            lb.refreshLangMenu();
                        }
                    }  //  end of loop thru MainPane.openBrowsers
                    //  Create a FeatureVector, postDistributions & write to disk
                    ArrayList<Object> egoList = new ArrayList<Object>();
                    if (FeatureVectorObj.el_1_distn == null) {
                        Library.readDistributionsFromFile();
                    }
                    FeatureVectorObj fv1 = dt.computeFeatureVector(egoList);
                    Library.postDistributions(fv1);
                    filePath = Library.libraryDirectory + "Feature Vectors/" + fv1.languageName + ".fvect";
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
                    fv1.toDisk(out);
                    dt.findOverlappingTerms(egoList);
                    dt.findHiddenNeuterEgos();
                    currDomTheoryDir = fc.getCurrentDirectory();
                    filePath = Library.libraryDirectory + "Domain Theory Files/" + lang + ".thy";
                    out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
                    dt.toThyFile(out);
                    System.out.println("Wrote: " + filePath);
                    Library.saveContextToDisk(dt.ctxt);
                    JOptionPane.showMessageDialog(desktop,
                            lang + " " + msgs.getString("successfulAdd"),
                            msgs.getString("taskCompleted"),
                            JOptionPane.PLAIN_MESSAGE);
                } catch (KSConstraintInconsistency exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("pROBLEM:") + " "
                            + exc.toString().substring(where + 1)
                            + msgs.getString("rECOMMENDATION1");
                    displayError(msg, msgs.getString("hornClauseError"), JOptionPane.ERROR_MESSAGE);
                } catch (KSBadHornClauseException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("pROBLEM:") + " "
                            + exc.toString().substring(where + 1)
                            + msgs.getString("rECOMMENDATION2");
                    displayError(msg, msgs.getString("hornClauseError"), JOptionPane.ERROR_MESSAGE);
                } catch (KSParsingErrorException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("pROBLEM:") + " "
                            + exc.toString().substring(where + 1)
                            + msgs.getString("rECOMMENDATION3");
                    JOptionPane.showMessageDialog(desktop, msg, msgs.getString("parsingError"),
                            JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (ClassNotFoundException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("pROBLEM:") + " "
                            + exc.toString().substring(where + 1)
                            + msgs.getString("rECOMMENDATION4");
                    JOptionPane.showMessageDialog(desktop, msg, msgs.getString("missingClass"),
                            JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (KSInternalErrorException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("pROBLEM:") + " "
                            + exc.toString().substring(where + 1)
                            + msgs.getString("rECOMMENDATION5");
                    JOptionPane.showMessageDialog(desktop, msg, msgs.getString("duplicateDomainTheory"),
                            JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (NotSerializableException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("internalError") + ": " + exc
                            + msgs.getString("rECOMMENDATION6");
                    JOptionPane.showMessageDialog(desktop, msg, msgs.getString("internalError"),
                            JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (IOException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("pROBLEM:") + " "
                            + msgs.getString("writing2Library") + " "
                            + exc.toString().substring(where + 1)
                            + msgs.getString("rECOMMENDATION7");
                    JOptionPane.showMessageDialog(desktop, msg, msgs.getString("fileSystemError"),
                            JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (JavaSystemException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("pROBLEM:") + " "
                            + msgs.getString("writing2Library") + " "
                            + exc.toString().substring(where + 1)
                            + msgs.getString("rECOMMENDATION7");
                    JOptionPane.showMessageDialog(desktop, msg, msgs.getString("fileSystemError"),
                            JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                }  //  end of catch blocks
            }  //  end of if-returnVal=Approve
        } //  end of action-is-add-dom-thy
        else if (e.getActionCommand().equals("add batch of domain theories")) {
            if (fc == null) {
                fc = new JFileChooser();
            }
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setDialogTitle(msgs.getString("dirHoldingDT"));
            if (currDomTheoryDir != null) {
                fc.setCurrentDirectory(currDomTheoryDir.getParentFile());
            }
            //  Display File-Open dialog and get User's selection
            String lang = "";
            DomainTheory dt = null;
            int returnVal = fc.showOpenDialog(desktop);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                    File dir = fc.getSelectedFile();
                    if (!dir.isDirectory()) {
                        throw new JavaSystemException(msgs.getString("notADirectory"));
                    }
                    Object[] btns = {se.getString("yes"), se.getString("no")};
                    int doFVs = JOptionPane.showOptionDialog(this,
                            msgs.getString("computeFVs"),
                            null,
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null, btns, btns[0]);
                    int keep = JOptionPane.showOptionDialog(this,
                            msgs.getString("keepContextsActive"),
                            null,
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null, btns, btns[0]);
                    int overWrite = JOptionPane.showOptionDialog(this,
                            msgs.getString("allowOverWrite"),
                            null,
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null, btns, btns[0]);
                    Library.allowOverWrites = (overWrite == JOptionPane.YES_OPTION);
                    currDomTheoryDir = dir;
                    if (doFVs == JOptionPane.YES_OPTION && FeatureVectorObj.el_1_distn == null) {
                        Library.resetDistributions();
                    }
                    File[] batch = dir.listFiles();
                    for (int i = 0; i < batch.length; i++) {
                        File file = batch[i];
                        String fName = file.getName(), filePath, mod;
                        if (fName.indexOf(".") != 0) {
                            Library.loadNewDomTh(file);
                            dt = DomainTheory.current;
                            Context.current = dt.ctxt;
                            Context.simulation = false;
                            dt.ctxt.simDataGen = false;
                            if (keep == JOptionPane.NO_OPTION) {
                                lang = dt.languageName;
                                int end = lang.indexOf("(");
                                if (end > -1) {
                                    lang = lang.substring(0, end);
                                }
                                Library.activeContexts.remove(lang);
                            }
                            PrintWriter out;
                            ArrayList<Object> egoList = new ArrayList<Object>();
                            if (doFVs == JOptionPane.YES_OPTION) {
                                //  Create a FeatureVector & write to disk
                                FeatureVectorObj fv1 = dt.computeFeatureVector(egoList);
                                filePath = Library.libraryDirectory + "Feature Vectors/" + dt.languageName + ".fvect";
                                out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
                                fv1.toDisk(out);
                            } else {
                                //  Just asssure examples generated
                                ClauseBody.priorPred = "";  //   Needed for Clause Counts
                                ClauseBody.seqTotal = 0;
                                ClauseBody.dupTotal = 0;
                                Iterator ktdIter = dt.theory.values().iterator();
                                egoList = dt.maleAndFemaleCreatedHeThem();
                                while (ktdIter.hasNext()) {
                                    KinTermDef ktd = (KinTermDef) ktdIter.next();
                                    if (dt.printableTerm(ktd)) {
                                        ktd.assureExamplesGenerated(egoList);
                                        if (ktd.eqcSigExact == null) {
                                            ktd.makeSigStrings();
                                        }
                                    }
                                }
                                //  Print out the Clause Counts, etc.
                                String pad = "";
                                if (++ClauseBody.seq < 10) {
                                    pad = "  ";
                                } else if (ClauseBody.seq < 100) {
                                    pad = " ";
                                }
                                System.out.println(ClauseBody.priorPred + ": " + pad + ClauseBody.seq + " - " + ClauseBody.dups
                                        + " = " + (ClauseBody.seq - ClauseBody.dups));
                                ClauseBody.seqTotal += ClauseBody.seq;
                                System.out.println("\nTotal clauses for " + dt.languageName + " is " + ClauseBody.seqTotal
                                        + " - " + ClauseBody.dupTotal + " = " + (ClauseBody.seqTotal - ClauseBody.dupTotal) + "\n\n");
                            }
                            dt.findOverlappingTerms(egoList);
                            dt.findHiddenNeuterEgos();
                            filePath = Library.libraryDirectory + "Domain Theory Files/" + dt.languageName + ".thy";
                            out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
                            dt.toThyFile(out);
                            System.out.println("Wrote: " + filePath);
                            Library.saveContextToDisk(Context.current);
                        }  //  end of it's not an invisible file (e.g. .DSxxxx)
                    }  //  end of loop thru DT files
                    for (int i = 0; i < MainPane.openBrowsers.size(); i++) {
                        Object o = MainPane.openBrowsers.get(i);
                        if (o instanceof LibBrowser) {
                            LibBrowser lb = (LibBrowser) o;
                            lb.refreshLangMenu();
                        }
                    }  //  end of loop thru MainPane.openBrowsers
                    int num = batch.length - 1;
                    JOptionPane.showMessageDialog(desktop,
                            num + " DomTheories Added to Library.",
                            "Task Completed",
                            JOptionPane.PLAIN_MESSAGE);
                } catch (KSConstraintInconsistency exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("pROBLEM") + " " + dt.languageName + ": " 
                            + exc.toString().substring(where + 1)
                            + msgs.getString("rECOMMENDATION2");
                    JOptionPane.showMessageDialog(desktop, msg, msgs.getString("hornClauseError"),
                            JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (KSBadHornClauseException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("pROBLEM") + " " + dt.languageName + ": " 
                            + exc.toString().substring(where + 1)
                            + msgs.getString("rECOMMENDATION2");
                    JOptionPane.showMessageDialog(desktop, msg, msgs.getString("hornClauseError"),
                            JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (KSParsingErrorException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("pROBLEM") + " " 
                            + exc.toString().substring(where + 1)
                            + msgs.getString("rECOMMENDATION3");
                    JOptionPane.showMessageDialog(desktop, msg, msgs.getString("parsingError"),
                            JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (ClassNotFoundException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("pROBLEM:") + " "  
                            + exc.toString().substring(where + 1)
                            + msgs.getString("rECOMMENDATION4");
                    JOptionPane.showMessageDialog(desktop, msg, msgs.getString("missingClass"),
                            JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (KSInternalErrorException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("pROBLEM:") + " "  
                            + exc.toString().substring(where + 1)
                            + msgs.getString("rECOMMENDATION5");
                    JOptionPane.showMessageDialog(desktop, msg, msgs.getString("duplicateDomainTheory"),
                            JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (NotSerializableException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("internalError") + ": " + exc
                            + msgs.getString("rECOMMENDATION6");
                    JOptionPane.showMessageDialog(desktop, msg, msgs.getString("internalError"),
                            JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (IOException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("pROBLEM:") + " " + 
                            msgs.getString("writing2Library") + " "  
                            + exc.toString().substring(where + 1)
                            + msgs.getString("rECOMMENDATION7");
                    JOptionPane.showMessageDialog(desktop, msg, msgs.getString("fileSystemError"),
                            JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                } catch (JavaSystemException exc) {
                    int where = exc.toString().indexOf(":");
                    String msg = msgs.getString("pROBLEM:") + " " + 
                            msgs.getString("writing2Library") + " "  
                            + exc.toString().substring(where + 1)
                            + msgs.getString("rECOMMENDATION7");
                    JOptionPane.showMessageDialog(desktop, msg, msgs.getString("fileSystemError"),
                            JOptionPane.ERROR_MESSAGE);
                    activity.log.append(msg + "\n\n");
                }  //  end of catch blocks
            }  //  end of if-returnVal=Approve
        } //  end of action-is-add-batch-of-dom-thys
        else if (e.getActionCommand().equals("save context")) {
            if (SIL_Edit.edWin != null) {
                SIL_Edit.edWin.chart.saveSILKFile();
            }else try {
                Library.saveUserContext(currentFrame, false);
            } catch (Exception ex) {
                activity.log.append(msgs.getString("savingCtxt") + ", " + ex + "\n\n");
            }
            changeActivity(Library.DATA_GATHERING);
        } //  end of action-is-save-context
        else if (e.getActionCommand().equals("save context as")) {
            if (SIL_Edit.edWin != null) {
                SIL_Edit.edWin.chart.saveAsFile();
            }else try {
                Library.saveUserContext(currentFrame, true);
            } catch (Exception ex) {
                    activity.log.append(msgs.getString("savingCtxt") + ", " + ex + "\n\n");
                }
            changeActivity(Library.DATA_GATHERING);
        } //  end of action-is-save-context-as
        else if (e.getActionCommand().equals("export GEDCOM")) {
            exportGEDCOM();
        } else if (e.getActionCommand().equals("export domain theory")) {
            // To export a file that looks like a manually-built domain theory,
            // we must wipe out all the expandedDefs. When this file is added to
            // the SILKin Library (by administrator) the expansions will be
            // calculated and re-added.
            String msg;
            try {
                DomainTheory dt = Library.contextUnderConstruction.domTheoryRef();
                eraseExpansions(dt);
                Library.writeThyFile(dt);
                if (Library.contextUnderConstruction.domTheoryAdrExists()) {
                    msg = se.getString("secondDTWritten");
                    Object[] btns = {se.getString("yes"), se.getString("no")};
                    int ch = JOptionPane.showOptionDialog(desktop, msg, null,
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null, btns, btns[0]);
                    // User may have canceled out of writing Ref, and want to bypass writing Adr.
                    if (ch == JOptionPane.YES_OPTION) {
                        dt = Library.contextUnderConstruction.domTheoryAdr();
                        eraseExpansions(dt);
                        Library.writeThyFile(dt);
                    }
                }
            } catch (Exception exc) {
                msg = "Error Encountered While Writing Domain Theory:\n"
                        + exc.getMessage();
                JOptionPane.showMessageDialog(desktop, msg, "Report This Bug!",
                        JOptionPane.ERROR_MESSAGE);
            }
        } //  end of action-is-export-domain-theory
        else if (e.getActionCommand().equals("edit prefs")) {
            if (SIL_Edit.edWin != null) {
                SIL_Edit.edWin.editPrefsItemActionPerformed(null);
            }else {
                String msg = msgs.getString("noCtxt");
                JOptionPane.showMessageDialog(desktop, msg, msgs.getString("invalidCommand"),
                        JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
        } //  end of action-is-edit-prefs
        else if (e.getActionCommand().equals("edit user context")) {
            editCUC();
        } //  end of action-is-edit-context
        else if (e.getActionCommand().equals("Compute Similarity Matrix")) {
            // This command used during development only. Therefore no translation
            try {
                Library.generateSimMatrix();
            } catch (Exception exc) {
                String msg = "PROBLEM: During FV generation, " + prettify(exc.toString())
                        + "\nRECOMMENDATION: DeBug!";
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error",
                        JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
        } //  end of action-is-Compute-Similarity-Matrix
        else if (e.getActionCommand().equals("Cluster the Feature Vectors")) {
            int minK, maxK;
            float penalty;
            // This command used during development only. Therefore no translation
            try {
                String minKStr = JOptionPane.showInputDialog(this, "Enter Smallest Number of Clusters"),
                        maxKStr = JOptionPane.showInputDialog(this, "Enter Largest Number of Clusters"),
                        penaltyStr = JOptionPane.showInputDialog(this, "Enter Penalty Percentage (a whole number)");
                minK = Integer.parseInt(minKStr);
                maxK = Integer.parseInt(maxKStr);
                penalty = Float.parseFloat(penaltyStr) / 100f;
                Library.clusterFVs(minK, maxK, penalty);
            } catch (Exception exc) {
                String msg = "PROBLEM: During FV clustering, " + prettify(exc.toString())
                        + "\nRECOMMENDATION: DeBug!";
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error",
                        JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
        } //  end of action-is-Cluster-the-Feature-Vectors
        else if (e.getActionCommand().equals("Make Curr Lang dyadsUndefined")) {
            // This command used during development only. Therefore no translation
            try {
                if (Context.current != null) {
                    System.out.println("Generating dyadsUndefined for " + Context.current.languageName);
                    DomainTheory.current.dyadsUndefined = new DyadTMap();
//                currentContext.cleanNodeFields();
                    Context.current.addDyads((Individual) Context.current.individualCensus.get(0));
                }
                //     ((JTextArea)diagramArea).setText(censusString() + dtm.summaryString());
            } catch (Exception exc) {
                String msg = "PROBLEM: During Dyad generation, " + prettify(exc.toString())
                        + "\nRECOMMENDATION: DeBug!";
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error",
                        JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
        } //  end of action-is-Make-dyadsUndefined
        else if (e.getActionCommand().equals("Gen Indexes")) {
            try {
                String startStr = JOptionPane.showInputDialog(this, msgs.getString("startNumber")),
                        endStr = JOptionPane.showInputDialog(this, msgs.getString("endNumber")),
                        tag = JOptionPane.showInputDialog(this, msgs.getString("suffix"));
                int start = Integer.parseInt(startStr),
                        end = Integer.parseInt(endStr);
                if (Library.predEncodings == null) {
                    Library.predEncodings = new TreeMap();
                }
                if (Library.predDecodings == null) {
                    Library.predDecodings = new TreeMap();
                }
                Library.parseClauseCounterOn = false;
                Library.generateAllIndexes(start, end, tag);
                String[] langs = Library.genCtxtMenu();
                end = Math.min(end, langs.length);
                String msg = "Indexes have been generated for\nlanguages " + 
                        startStr + " through " + end;
                JOptionPane.showMessageDialog(this, msg);
            } catch (RuntimeException rte) {
                throw new RuntimeException(rte);
            } catch (Exception exc) {
                String msg = msgs.getString("pROBLEM:") + " " + prettify(exc.toString())
                        + msgs.getString("rECOMMENDATION6");
                JOptionPane.showMessageDialog(desktop, msg, msgs.getString("pROBLEM:"),
                        JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
        } //  end of action-is-Gen-Indexes
        else if (e.getActionCommand().equals("Merge Indexes")) {
            String[] suffixes = new String[10];
            int index = 0;
            String suffix = JOptionPane.showInputDialog(this, msgs.getString("1stSuffix"));
            if (suffix.equals("")) {
                return;
            }
            String firstSuffix = suffix;
            suffixes[index++] = suffix;
            while (!suffix.equals("")) {
                suffix = JOptionPane.showInputDialog(this, msgs.getString("lastSuffix"));
                suffixes[index++] = suffix;
            }  // end of suffix-gathering loop
            index--;
            String lastSuffix = suffixes[index -1];
            ArrayList<Object> cbIfiles = new ArrayList<Object>(),
                    bcbIfiles = new ArrayList<Object>(),
                    ktFiles = new ArrayList<Object>(),
                    cbCountFiles = new ArrayList<Object>(),
                    predEncodeFiles = new ArrayList<Object>(),
                    predDecodeFiles = new ArrayList<Object>();
            String cbIbase = Library.libraryDirectory + "Index Segments/ClauseIndex",
                    bcbIbase = Library.libraryDirectory + "Index Segments/BaseCBIndex",
                    ktBase = Library.libraryDirectory + "Index Segments/KinTermSigTree",
                    cbCountBase = Library.libraryDirectory + "Index Segments/ClauseCounts",
                    predEncodeBase = Library.libraryDirectory + "Index Segments/PredEncodings",
                    predDecodeBase = Library.libraryDirectory + "Index Segments/PredDecodings";
            for (int i = 0; i < index; i++) {
                cbIfiles.add(cbIbase + suffixes[i]);
                bcbIfiles.add(bcbIbase + suffixes[i]);
                ktFiles.add(ktBase + suffixes[i]);
                cbCountFiles.add(cbCountBase + suffixes[i]);
                predEncodeFiles.add(predEncodeBase + suffixes[i]);
                predDecodeFiles.add(predDecodeBase + suffixes[i]);
            }
            try {
                if (Library.predEncodings == null) {
                    Library.readPredEncodings();
                }
                if (Library.predDecodings == null) {
                    Library.readPredDecodings();
                }
                Library.mergeIndexes(cbIfiles, bcbIfiles, ktFiles, cbCountFiles, predEncodeFiles, predDecodeFiles);
                String msg = "Segments " + firstSuffix  + " through " + 
                        lastSuffix + " were merged.";
                JOptionPane.showMessageDialog(this, msg);
            } catch (RuntimeException rte) {
                throw new RuntimeException(rte);
            } catch (Exception exc) {
                String msg = msgs.getString("pROBLEM:") + "\n" + prettify(exc.toString())
                        + msgs.getString("rECOMMENDATION6");
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error",
                        JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
            return;
        } //  end of action-is-Merge-Indexes
        else if (e.getActionCommand().equals("Index & Merge")) {
        // 1    Identify the context to be added
            String[] langs = Library.genCtxtMenu(null);
            String chosen = (String) JOptionPane.showInputDialog(desktop,
                    msgs.getString("whatLangMerge"),
                    msgs.getString("pickALang"),
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    langs,
                    langs[0]);
            //Check for cancellation.
            if (chosen == null) {
                return;
            }
            int start = Arrays.binarySearch(langs, chosen);
       // 2    Generate index segments for that DT into the Segments directory, marked "B"
            if (Library.predEncodings == null) {
                Library.predEncodings = new TreeMap();
            }
            if (Library.predDecodings == null) {
                Library.predDecodings = new TreeMap();
            }
            Library.parseClauseCounterOn = false;
            try {
                Library.generateAllIndexes(start, start, "-B");
            } catch (Exception exc) {
                String msg = "Problem loading the language '" + chosen + "'\n" + exc;
                JOptionPane.showMessageDialog(desktop, msg, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        // 3    Move the current cum indexes to the Segments directory, marked "A"
            String  oldcbIbase = Library.libraryDirectory + "ClauseIndex",
                    oldbcbIbase = Library.libraryDirectory + "BaseCBIndex",
                    oldktBase = Library.libraryDirectory + "KinTermSigTree",
                    oldktCompressedBase =  Library.libraryDirectory + "KTSigCompressed",
                    oldcbCountBase = Library.libraryDirectory + "ClauseCounts",
                    oldpredEncodeBase = Library.libraryDirectory + "PredEncodings",
                    oldpredDecodeBase = Library.libraryDirectory + "PredDecodings",
                    newcbIbase = Library.libraryDirectory + "Index Segments/ClauseIndex",
                    newbcbIbase = Library.libraryDirectory + "Index Segments/BaseCBIndex",
                    newktBase = Library.libraryDirectory + "Index Segments/KinTermSigTree",
                    newktCompressedBase =  Library.libraryDirectory + "Index Segments/KTSigCompressed",
                    newcbCountBase = Library.libraryDirectory + "Index Segments/ClauseCounts",
                    newpredEncodeBase = Library.libraryDirectory + "Index Segments/PredEncodings",
                    newpredDecodeBase = Library.libraryDirectory + "Index Segments/PredDecodings";            
            try {
                Library.copyFile(oldcbIbase, newcbIbase + "-A");
                Library.copyFile(oldbcbIbase, newbcbIbase + "-A");
                Library.copyFile(oldktBase, newktBase + "-A");
                Library.copyFile(oldktCompressedBase, newktCompressedBase + "-A");
                Library.copyFile(oldcbCountBase, newcbCountBase + "-A");
                Library.copyFile(oldpredEncodeBase, newpredEncodeBase + "-A");
                Library.copyFile(oldpredDecodeBase, newpredDecodeBase + "-A");
            } catch (Exception iee) {
                String msg = msgs.getString("problemCopyingOld") + iee;
                msg += msgs.getString("possibleLibraryCorruption");
                JOptionPane.showMessageDialog(desktop, msg, 
                        msgs.getString("libraryError"), JOptionPane.ERROR_MESSAGE);
                return;
            }            
        // 4    Call 'Merge Indexes' with A & B.
            ArrayList<Object> cbIfiles = new ArrayList<Object>(),
                    bcbIfiles = new ArrayList<Object>(),
                    ktFiles = new ArrayList<Object>(),
                    cbCountFiles = new ArrayList<Object>(),
                    predEncodeFiles = new ArrayList<Object>(),
                    predDecodeFiles = new ArrayList<Object>();
            String[] suffixes = {"-A", "-B"};
            for (String s : suffixes) {
                cbIfiles.add(newcbIbase + s);
                bcbIfiles.add(newbcbIbase + s);
                ktFiles.add(newktBase + s);
                cbCountFiles.add(newcbCountBase + s);
                predEncodeFiles.add(newpredEncodeBase + s);
                predDecodeFiles.add(newpredDecodeBase + s);
            }
            try {
            Library.mergeIndexes(cbIfiles, bcbIfiles, ktFiles, cbCountFiles, predEncodeFiles, predDecodeFiles);
            } catch(Exception exc) {
                String msg = msgs.getString("problemMergingIndexes") + exc;
                msg += msgs.getString("likelyFileSystem");
                JOptionPane.showMessageDialog(desktop, msg, 
                        msgs.getString("seriousError"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }  // end of index-and-merge
        else if (e.getActionCommand().equals("Leave 1 Instant Replay")) {
            // Development Only. Not translated.
            File file = null;
            try {
                if (fc == null) {
                    fc = new JFileChooser();
                }
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fc.setDialogTitle("Choose the test (.case file) to replay");
                //  Display File-Open dialog and get User's selection
                int returnVal = fc.showOpenDialog(desktop);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                }
                if (file == null) {
                    return;
                }
                int nmbrOfRounds, maxNoise, ignorable, minDyadsPerPCStr, roundNmbr, start, where;
                BufferedReader inFile = new BufferedReader(new FileReader(file));
                String line,
                        dtFileName = inFile.readLine();  //  the LearnedDT's object file
                ObjectInputStream str = new ObjectInputStream(new FileInputStream(dtFileName));
                Learned_DT learner = (Learned_DT) str.readObject();
                DomainTheory sourceDT = (learner.languageName.contains("(Adr)")
                        ? learner.ctxt.domTheoryAdr()
                        : learner.ctxt.domTheoryRef());
                Context.current = learner.ctxt;
                nmbrOfRounds = Integer.parseInt(inFile.readLine());
                Library.baseCB_Wait = Integer.parseInt(inFile.readLine());
                Library.inductionWait = Integer.parseInt(inFile.readLine());
                int[] distanceLimits = new int[nmbrOfRounds + 1];
                line = inFile.readLine();
                start = 1;
                for (int i = 1; i <= nmbrOfRounds; i++) {
                    where = line.indexOf(",", start);
                    if (where == -1) {
                        where = line.length() - 1;
                    }
                    distanceLimits[i] = Integer.parseInt(line.substring(start, where));
                    start = where + 2;
                }
                int[] minHitPercents = new int[nmbrOfRounds + 1];
                line = inFile.readLine();
                start = 1;
                for (int i = 1; i <= nmbrOfRounds; i++) {
                    where = line.indexOf(",", start);
                    if (where == -1) {
                        where = line.length() - 1;
                    }
                    minHitPercents[i] = Integer.parseInt(line.substring(start, where));
                    start = where + 2;
                }
                maxNoise = Integer.parseInt(inFile.readLine());
                ignorable = Integer.parseInt(inFile.readLine());
                Context.spellingNoise = Float.parseFloat(inFile.readLine());
                Context.classNoise = Float.parseFloat(inFile.readLine());
                Oracle.spellingNoise = Float.parseFloat(inFile.readLine());
                Oracle.classNoise = Float.parseFloat(inFile.readLine());
                Library.ClusterState.wtVectChoice = Integer.parseInt(inFile.readLine());
                minDyadsPerPCStr = Integer.parseInt(inFile.readLine());
                roundNmbr = Integer.parseInt(inFile.readLine());
                float priorMinHitPercent = Library.minHitPerCent;
                Context.simulation = true;
                if (FeatureVectorObj.el_1_distn == null) {
                    Library.readDistributionsFromFile();
                }
                String fileName = Library.libraryDirectory + "Test Cases/-Logs & Charts/Test-" + ++testSerialNmbr + ".log",
                        tabFileName = Library.libraryDirectory + "Test Cases/-Logs & Charts/Test-" + testSerialNmbr + ".txt";
                PrintWriter logFile = new PrintWriter(new BufferedWriter(new FileWriter(fileName))),
                        tabFile = new PrintWriter(new BufferedWriter(new FileWriter(tabFileName)));
                Library.parseClauseCounterOn = false;
                leaveOneOut(learner, sourceDT, logFile, tabFile, nmbrOfRounds, distanceLimits, minHitPercents,
                        maxNoise, ignorable, minDyadsPerPCStr, true, roundNmbr);
                logFile.flush();
                logFile.close();
                tabFile.flush();
                tabFile.close();
                Library.parseClauseCounterOn = true;
                Library.minHitPerCent = priorMinHitPercent;
                Library.contextUnderConstruction = null;
                return;
            } catch (RuntimeException rte) {
                throw rte;  //  Pass it thru to the Console.
            } catch (Exception exc) {
                String msg = "PROBLEM: During Leave-1-Instant-Replay,\n" + prettify(exc.toString())
                        + "\nRECOMMENDATION: DeBug!";
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error", JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
            return;
        } //  end of action-is-Leave-1-Instant-Replay
        else if (e.getActionCommand().equals("Leave One Out")) {
            // Development Only. Not translated.
            File file = null;
            try {
                if (fc == null) {
                    fc = new JFileChooser();
                }
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fc.setDialogTitle("Choose the theory file to parse.");
                int returnVal = fc.showOpenDialog(desktop);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                }
                if (file == null) {
                    return;
                }
                String fileName = file.getAbsolutePath();
                Library.readThyFile(fileName);
            }catch(Exception exc) { }
        } //  end of action-is-Leave-One-Out
        else if (e.getActionCommand().equals("Simulate User Data")) {
            // Development Only. Not translated.
            try {
                String[] langs = Library.genCtxtMenu();
                String msg = "What context to use.", langChoice = null;
                boolean goodChoice = false;
                while (!goodChoice) {
                    langChoice = JOptionPane.showInputDialog(msg);
                    if (langChoice == null || langChoice.equals("")) {
                        return;
                    }
                    for (int i = 0; i < langs.length; i++) {
                        if (langChoice.equals(langs[i])) {
                            goodChoice = true;
                        }
                    }
                    if (!goodChoice) {
                        msg = "Sorry -- " + langChoice + " does not match any context name in the Library.";
                    }
                }  //  end of while not goodChoice
                Context ctxt = (Context) Library.activeContexts.get(langChoice);
                if (ctxt == null) { //  need to load from disk
                    String fileName = Library.libraryCtxtDirectory + langChoice + ".ctxt";
                    ctxt = Library.readContextFromDisk(fileName);
                }  //  end of need to load from disk
                if (ctxt.individualCensus.size() > 0) {
                    msg = "There are " + ctxt.individualCensus.size() + " people already present.  Delete & start fresh?";
                    int killer = JOptionPane.showConfirmDialog(null, msg);
                    if (killer == JOptionPane.YES_OPTION) {
                        ctxt.resetTo(0, 0);
                        if (ctxt.domTheoryRefExists()) {
                            ctxt.domTheoryRef().dyadsUndefined = new DyadTMap();
                        }
                        if (ctxt.domTheoryAdrExists()) {
                            ctxt.domTheoryAdr().dyadsUndefined = new DyadTMap();
                        }
                        ctxt.ktm = new KinTermMatrix();
                    }  //  end of kill 'em
                } else {
                    ctxt.ktm = new KinTermMatrix();
                    if (ctxt.domTheoryRefExists()) {
                        ctxt.domTheoryRef().dyadsUndefined = new DyadTMap();
                    }
                    if (ctxt.domTheoryAdrExists()) {
                        ctxt.domTheoryAdr().dyadsUndefined = new DyadTMap();
                    }
                }
                String nmbrOf = JOptionPane.showInputDialog("Number of Egos to use? (enter a number)"),
                        badSp = JOptionPane.showInputDialog("Percent Spelling Noise? (% entered as whole number)"),
                        badCl = JOptionPane.showInputDialog("Percent Class Name Noise? (% entered as whole number)");
                NUMBER_OF_EGOS = Integer.parseInt(nmbrOf);
                Context.spellingNoise = Integer.parseInt(badSp) / 100f;
                Context.classNoise = Integer.parseInt(badCl) / 100f;
                Context.simulation = true;
                ArrayList<Object> egoBag = null;
                if (ctxt.domTheoryRefExists()) {
                    DomainTheory dt = ctxt.domTheoryRef();
                    Learned_DT learner = new Learned_DT(dt);
                    egoBag = dt.maleAndFemaleCreatedHeThem();
                    generateSimulatedData(ctxt, dt, 9, egoBag, learner);  //  9 = virtually no distance limit
                }
                if (ctxt.domTheoryAdrExists()) {
                    DomainTheory dt = ctxt.domTheoryAdr();
                    Learned_DT learner = new Learned_DT(dt);
                    if (egoBag == null) {
                        egoBag = dt.maleAndFemaleCreatedHeThem();
                    }
                    generateSimulatedData(ctxt, dt, 9, egoBag, learner);  //  9 = virtually no distance limit
                }
                if (!ctxt.domTheoryRefExists() && !ctxt.domTheoryAdrExists()) {
                    JOptionPane.showMessageDialog(null,
                            "Could not simulate data -- there are no domain theories in this context.",
                            "Serious Error",
                            JOptionPane.ERROR_MESSAGE);
                } else {
                    Library.contextUnderConstruction = ctxt;
                }
            } catch (RuntimeException rte) {
                throw new RuntimeException(rte);
            } catch (Exception exc) {
                String msg = "PROBLEM: During User Simulation,\n" + prettify(exc.toString())
                        + "\nRECOMMENDATION: DeBug!";
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error",
                        JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
            return;
        } //  end of action-is-Simulate-User-Data
        else if (e.getActionCommand().equals("Gen CUC Population")) {
            // Development Only. Not translated.
            try {
                Context cuc = Library.contextUnderConstruction;
                Context.current = cuc;
                DomainTheory.current = cuc.domTheoryRef();
                Individual indie;
                String gender;
                for (int j = 0; j < 20; j++) {
                    if (j % 2 == 0) {
                        gender = "F";
                    } else {
                        gender = "M";
                    }
                    indie = new Individual("Person#" + j, gender);
                }
            } catch (Exception exc) {
                String msg = "PROBLEM: During generation of a fake population, " + prettify(exc.toString())
                        + "\nRECOMMENDATION: DeBug!";
                JOptionPane.showMessageDialog(desktop, msg, "Testing Error",
                        JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }
        }  //  end of action-is-Gen-CUC-Population

    }  //  end of listener actionPerformed
    
    /** Erase all expandedDefs in the specified domain theory.
     * 
     * @param dt the target domain theory
     */
    public void eraseExpansions(DomainTheory dt){
        Iterator ktIter = dt.theory.values().iterator();
        while(ktIter.hasNext()) {
            KinTermDef ktd = (KinTermDef)ktIter.next();
            ktd.expandedDefs.clear();
        }
    }
    
    public void browseCurrentContext() {
        // Strategy: Make sure the current context is listed in the Library's
        // stub file, and that the .ctxt file has been created or updated.
        MyResBundle rb = Library.messages;
        try {
            Library.saveUserContext();
        } catch (Exception exc) {
            String msg = rb.getString("fileErrorDuringSaveCtxt")
                    + prettify(exc.toString())
                    + rb.getString("sendErrorLog");
            JOptionPane.showMessageDialog(desktop, msg, rb.getString("seriousError"),
                    JOptionPane.ERROR_MESSAGE);
            activity.log.append(msg + "\n\n");
            return;
        }
        String lang = Context.current.languageName;
        Library.ContextStub newStub = Library.retrieveOrCreateStub(lang);
        newStub.refThyExists = Context.current.domTheoryRefExists();
        newStub.adrThyExists = Context.current.domTheoryAdrExists();
        Library.currentLanguage = lang;
        browseLibrary();
    }

    /**Construct a new {@link LibBrowser}, make it the 'currentFrame' and add
     * it to the 'View' menu. 
     */
    public void browseLibrary() {
        currentFrame = new LibBrowser();
        currentFrame.desktop = desktop;
        currentFrame.setLocation(25 * numWindows, 25 * numWindows++);
        desktop.add(currentFrame);
        currentFrame.show();
        currentFrame.moveToFront();
        try {
            currentFrame.setSelected(true);
        } catch (PropertyVetoException pv) {
        }
        currentFrame.miViewMe = menuView.add(currentFrame.windowNum);
        currentFrame.miViewMe.addActionListener(currentFrame);
        currentFrame.menuView = menuView;
    }

    public void editCUC() {
        String rbm  = Library.messages.getString("whileEdCtxt");
        if (Library.contextUnderConstruction == null) {
            try {
                Library.chooseUserContext(currentFrame); //  usually creates a contextUnderConstruction
            } catch (Exception ex) {
                String msg = rbm + " " + ex;
                activity.log.append(msg + "\n\n");
            }
        }
        if (curr_CUC_Editor != null) {  //  duplicate ContextEditors not allowed
            curr_CUC_Editor.buildPopulationBox();
            currentFrame = curr_CUC_Editor;
            currentFrame.moveToFront();
            try {
                currentFrame.setSelected(true);
            } catch (PropertyVetoException pv) {
            }
        } else {  // create-new-Editor
            try {
                currentFrame = Library.contextUnderConstruction.editFields();  //  returns a ContextEditor
                curr_CUC_Editor = (ContextEditor) currentFrame;
                currentFrame.desktop = desktop;
                currentFrame.setLocation(25 * numWindows, 25 * numWindows++);
                desktop.add(currentFrame);
                currentFrame.show();
                currentFrame.moveToFront();
                try {
                    currentFrame.setSelected(true);
                } catch (PropertyVetoException pv) {
                }
                currentFrame.miViewMe = menuView.add(currentFrame.windowNum);
                currentFrame.miViewMe.addActionListener(currentFrame);
                currentFrame.menuView = menuView;
            } catch (Exception ex) {
                activity.log.append(rbm + " " + ex + "\n\n");
            }
        }  //  end of create-new-Editor
        changeActivity(Library.DATA_GATHERING);
    }
    
    /** Ask User for the target file name and a few options for the type of export.
     * Then call this context's <code>exportFGEDCOM</code> method.
     */
    public void exportGEDCOM() {
        String msg;
        boolean realData;
        if (Context.current == null) {
            msg = msgs.getString("chooseThenExport");
            JOptionPane.showMessageDialog(desktop, msg,
                    msgs.getString("nothingToExport"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int population = Context.current.indSerNumGen + Context.current.famSerNumGen;
        if (population < 2) {
            JOptionPane.showMessageDialog(desktop,
                    msgs.getString("exportingOnly") + " " + population 
                            + msgs.getString("exportingOnly2") + " ",
                    msgs.getString("nothingToExport"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        } //  end of too-small-population
        msg = msgs.getString("exportOptions");
        Object[] options = {msgs.getString("actualData"), 
                            msgs.getString("kinTerms")};
        int choice = JOptionPane.showOptionDialog(desktop, msg,
                msgs.getString("typeGEDCOMExport"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, //don't use a custom Icon
                options, //the titles of buttons
                options[0]); //default is actual data
        realData = (choice == 0);
        msg = msgs.getString("exportWhere");
        String destination = JOptionPane.showInputDialog(desktop, msg);
        if (fc == null) {
            fc = new JFileChooser();
        }
        File file = null;
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setDialogTitle(msgs.getString("gEDCOMLocation"));
        if (currGEDCOMDir != null) {
            fc.setCurrentDirectory(currGEDCOMDir);
        }
        //  Display File-Open dialog and get User's selection
        int returnVal = fc.showSaveDialog(desktop);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            file = fc.getSelectedFile();
            currGEDCOMDir = fc.getCurrentDirectory();
            String fName = file.getName(), fPath = file.getPath();
            int extLoc = fName.lastIndexOf(".");
            String ext = (extLoc == -1 ? "" : fName.substring(extLoc));
            if (! ext.equalsIgnoreCase(".GED")) {
                msg = msgs.getString("mustEnd.ged");
                JOptionPane.showMessageDialog(desktop, msg, 
                        msgs.getString("fileNameError"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!realData) {
                options[0] = msgs.getString("include");
                options[1] = msgs.getString("don'tInclude");
                choice = JOptionPane.showOptionDialog(desktop,
                        msgs.getString("namesHaveFlags"),
                        msgs.getString("includeTags"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, //don't use a custom Icon
                        options, //the titles of buttons
                        options[1]); //default is "Don't Include"
            }
            try {
                PrintWriter outFile = new PrintWriter(fPath, "UTF-8");
                Context.current.exportGEDCOM(outFile, fName, realData, destination, (String) options[choice]);
                outFile.flush();
                outFile.close();
            } catch (IOException exc) {
                msg = msgs.getString("whileExportingGEDCOM") + " " + prettify(exc.toString())
                        + msgs.getString("rECOMMENDATION7");
                JOptionPane.showMessageDialog(desktop, msg, 
                        msgs.getString("seriousError"),
                        JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            } catch (Exception exc) {
                msg = msgs.getString("whileExportingGEDCOM") + " " + prettify(exc.toString())
                        + msgs.getString("rECOMMENDATION6");
                JOptionPane.showMessageDialog(desktop, msg, 
                        msgs.getString("seriousError"),
                        JOptionPane.ERROR_MESSAGE);
                activity.log.append(msg + "\n\n");
            }  //  end of catch bodies
        }  //  end of if-APPROVE_OPTION
    }
    
    /** Ask User for the GEDCOM file name, create a Tokenizer and a parser, 
     * and execute the parse.
     */
    public void importGEDCOM() {
        File ged = null;
        MyResBundle rb = Library.messages;
        try {
            DFA gdfa = Library.gedcomDFA();
            if (fc == null) {
                fc = new JFileChooser();
            }
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setDialogTitle(rb.getString("gEDCOMFile"));
            int returnVal = fc.showOpenDialog(desktop);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                ged = fc.getSelectedFile();
            } else {
                return;
            }
            TokenizerGEDCOM tok = new TokenizerGEDCOM(gdfa, new Linus(ged, "UTF8"));
            ParserGEDCOM parser = new ParserGEDCOM(tok);
            parser.parseFile(ged);
        } catch (Exception exc) {
            String msg = rb.getString("whileExportingGEDCOM") + " " + exc + "\n" + exc;
            StackTraceElement[] bad = exc.getStackTrace();
            for (int i = 0; i < 5; i++) {
                msg += "\n" + bad[i];
            }
            displayError(msg, rb.getString("gEDCOMImportFailed"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void changeActivity(int activityCode) {
        Library.currentActivity = activityCode;
    }

    // prettify inserts line breaks into error messages coming from the JVM
    String prettify(String ugly) {
        int start = 0, stop = 100, length = ugly.length(),
                period, comma, leftSquare, rightSquare, rightSquareComma, max;
        String lessUgly = null;
        while (stop < length) {
            period = ugly.substring(start, stop - 10).lastIndexOf(".") + 1 + start;
            comma = ugly.substring(start, stop - 10).lastIndexOf(",") + 1 + start;
            rightSquare = ugly.substring(start, stop).lastIndexOf("]") + 1 + start;
            rightSquareComma = ugly.substring(start, stop).lastIndexOf("],") + 2 + start;
            leftSquare = ugly.substring(start, stop).lastIndexOf("[") + start;
            max = Math.max(comma, period);
            max = Math.max(max, leftSquare);
            max = Math.max(max, rightSquare);
            max = Math.max(max, rightSquareComma);
            if (max > (start + 60)) {
                stop = max;
            }
            lessUgly += ugly.substring(start, stop) + "\n";
            start = stop;
            stop += 100;
        }  //  end of while stop<length
        lessUgly += ugly.substring(start);
        return lessUgly;
    }  //  end of method prettify

    public void doLv1ActiveLearning(Learned_DT learner, int maxNoise, int ignorable, int maxConf, int minDyadsPerPCStr, DomainTheory sourceDT)
            throws KSInternalErrorException, KSParsingErrorException, KSBadHornClauseException, KSConstraintInconsistency,
            ClassNotFoundException, JavaSystemException, FileNotFoundException, IOException {
        //  NOTE:  When simulations have ended and real User is employed, I must assure that the entries
        //		   in DyadsUndefined for erroneous terms (e.g. sistex) are removed from the tree once the
        //		   activity dyads have been corrected.  that is currently done in DyadTMap.summaryString()
        if (learner == null) {
            return;
        }
        if (Library.predEncodings == null) {
            Library.readPredEncodings();
        }
        if (Library.predDecodings == null) {
            Library.readPredDecodings();
        }
        learner.ctxt.simDataGen = true;
        Context.current = learner.ctxt;
        DomainTheory.current = learner;
        if (Library.synUmbDetectOn) {
            learner.resolveSynonymsInDyads();
        }
        boolean nonTermsFound = learner.dyadsUndefined.containsKey("no__term");
        Iterator dyadIter = learner.theory.keySet().iterator();
        while (dyadIter.hasNext()) {
            String kterm = (String) dyadIter.next();
            if (nonTermsFound || learner.dyadsUndefined.containsKey(kterm)) {
                if (Library.errorCorrectionOn) {
                    ArrayList<Object> validationIssues = learner.validateNewDyads(kterm, maxNoise);
                    learner.postAnomaliesForUser(kterm, validationIssues);
                }
            }
        }  //  end of 1st loop thru kinTerms in theory
        dyadIter = learner.dyadsUndefined.keySet().iterator();
        while (dyadIter.hasNext()) {
            String kterm = (String) dyadIter.next();
            if (learner.dyadsUndefined.avgDyadsPerPCStr(kterm) >= minDyadsPerPCStr) {
                if (DomainTheory.LGG_ONLY) {
                    learner.learnKinTermLGG(kterm, maxNoise, ignorable);
                } else {
                    learner.learnKinTerm(kterm, maxNoise, ignorable, maxConf, sourceDT);
                }
            }
        }  //  end of 2nd loop thru kinTerms in DyadTMap
        Library.CB_Ptr.clearCache();
        Library.KTD_Ptr.clearCacheDTs();
        System.gc();
        if (learner.dyadsDefined != null) {  //  Now loop thru definedTerm dyads
            dyadIter = learner.dyadsDefined.entrySet().iterator();
            while (dyadIter.hasNext()) {
                Map.Entry entry = (Map.Entry) dyadIter.next();
                String kterm = (String) entry.getKey();
                TreeMap pos = (TreeMap) entry.getValue(),
                        neg = learner.makeNEG(kterm, pos);
                if (Library.synUmbDetectOn) {
                    learner.detectSynonymsAndUmbrellas(kterm, pos, neg, maxNoise);
                }
            }  //  end of loop thru definedTerm dyads
        }
        // Now analyze all potentialUmbrellas & synonyms found in this loop
        if (learner.potUmbrellas != null) {
            learner.proposeUmbrellas();
        }
        if (Library.synUmbDetectOn) {
            learner.analyzeSynonymsAndUmbrellas();
            learner.validateSynonymsAndUmbrellas();
        }
        learner.ctxt.simDataGen = false;
    } //  end of method doLv1ActiveLearning

    public void leaveOneOut(Learned_DT learner, DomainTheory sourceDT, PrintWriter logFile, PrintWriter tabFile, int nmbrOfRounds, int[] distanceLimits, int[] minHitPercents,
            int maxNoise, int ignorable, int minDyadsPerPCStr, boolean replay, int roundNmbr)
            throws KSParsingErrorException, KSConstraintInconsistency, KSBadHornClauseException, FileNotFoundException, IOException,
            KSInternalErrorException, KinshipSystemException, ClassNotFoundException, JavaSystemException {
        // Development code only. No translation.
        
        //  learner is a Learned_DT in which to record what we can learn (& how quickly).
        //  For up to nmbrOfRounds, or until all terms have been learned (i.e. all dyads are in dyadsDefined), in each round:
        //	1.	Generate 4 Egos, and make 1 dyad for each Ego of each expandedCB of each KTD in sourceFile.
        //		so long as the absolute value of the level of a CB is <= the distanceLimit for this round.
        //	2.	ResolveSynonyms in the dyads, then validateNewDyads for already-learned terms
        //	3.	Call learnKinTerm on each kinTerm in DyadsUndefined
        //	4.	If any Suggestions are generated, make Learned_DT process them.
        //	5.	When all dyads are for terms with accepted definitions, we're done.  No more rounds.
        //  When rounds are completed, write results to logFile & return.
        //  NOTE:  When simulations have ended and real User is employed, I must assure that the entries
        //		   in DyadsUndefined for erroneous terms (e.g. sistex) are removed from the tree once the
        //		   activity dyads have been corrected.  that is currently done in DyadTMap.summaryString()
        Library.contextUnderConstruction = learner.ctxt;
        if (FeatureVectorObj.el_1_distn == null) {
            Library.readDistributionsFromFile();
        }
        learner.ctxt.featuresHaveChanged = true;
        int testSerial = testSerialNmbr,
                noiseCntr = 0;
        System.out.println("*************Beginning Leave-1-Out for " + learner.languageName + "-" + testSerial);
        boolean done = false;
        while (roundNmbr <= nmbrOfRounds && !done) {
            if (learner.ctxt.featuresHaveChanged) {
                FeatureVectorObj fv = learner.computeFeatureVector(new ArrayList<Object>());
                String filePath = Library.libraryDirectory + "Feature Vectors/" + fv.languageName + ".fvect";
                fv.toDisk(new PrintWriter(new BufferedWriter(new FileWriter(filePath))));
                learner.ctxt.featuresHaveChanged = false;
            }
            System.out.println("*** Round " + roundNmbr + ".0");
            Library.minHitPerCent = minHitPercents[roundNmbr] / 100f;
            if (!replay) {  //  start from scratch; generate initial dyads
                ArrayList<Object> egoBag = sourceDT.maleAndFemaleCreatedHeThem();
                generateSimulatedData(sourceDT.ctxt, sourceDT, distanceLimits[roundNmbr], egoBag, learner);
                learner.dyadsUndefined.assimlate(sourceDT.dyadsUndefined);
            } else {
                replay = false;  //  replay with existing dyads this round
            }
            System.out.println(learner.dyadsUndefined.summaryString());
            noiseCntr = learner.postNoiseCounts(roundNmbr, noiseCntr);
            if (roundNmbr > Library.baseCB_Wait) {
                Learned_DT.doBaseCBs = true;
            }
            if (roundNmbr > Library.inductionWait) {
                Learned_DT.doInduction = true;
            }
            int maxConf = ignorable / 2;
            doLv1ActiveLearning(learner, maxNoise, ignorable, maxConf, minDyadsPerPCStr, sourceDT);
            learner.printSuggestions("" + roundNmbr + ".0");
            learner.processSuggestions(roundNmbr, 0, sourceDT, maxNoise, ignorable, minDyadsPerPCStr);
            done = learner.allDone(minDyadsPerPCStr, sourceDT);
            learner.dumpRemNoise();
            ArrayList<Object> learnRec = new ArrayList<Object>();
            learnRec.add("Total Dyads thru Round ");
            learnRec.add(roundNmbr);
            learnRec.add(" = ");
            Integer dyTotal = new Integer(learner.dyadsDefined.total() + learner.dyadsUndefined.total());
            learnRec.add(dyTotal);
            learner.postLrnHist("" + roundNmbr, learnRec);
            roundNmbr++;
        }  //  end of rounds
        learner.writeTestResults(logFile, tabFile, sourceDT);
        System.out.println("\n\n\t\tDefinitions Learned");
        Iterator iter = learner.theory.keySet().iterator();
        while (iter.hasNext()) {
            String term = (String) iter.next();
            if (!learner.nonTerms.contains(term) && term.indexOf("aux>") == -1) {
                System.out.println(term);
            }
        }
    }  //  end of method LeaveOneOut

    public void generateSimulatedData(Context ctxt, DomainTheory dt, int distLimit,
            ArrayList<Object> originalEgoBag, Learned_DT learner)
            throws KSBadHornClauseException, KSInternalErrorException, KinshipSystemException, ClassNotFoundException {
        // Development code only. No translation.        
        
        //  For 'nmbrOfEgos' iterations, pick an ego and generate complete set of examples for them of dt's
        //  kinTerms.  Add to the KinTermMatrix & DyadTMap as we go.
        Oracle orca = learner.wiseGuy;
        Context.current = ctxt;
        ctxt.simDataGen = true;
        boolean oldSimVal = Context.simulation;
        Context.simulation = true;
        ClauseBody.distanceLimit = distLimit;
        DomainTheory.addrTerms = dt.addressTerms;
        DomainTheory.current = dt;
        ArrayList<Object> egoBag = new ArrayList<Object>(originalEgoBag);  // original may be re-used
        Individual ego = (Individual) egoBag.remove(0), potUsedEgo;
        dt.dyadsUndefined = new DyadTMap();
        KinTermDef ktd;
        for (int i = 0; i < NUMBER_OF_EGOS; i++) {
            ctxt.changeEgoTo(ego);
            egoBag.add(0, ego);
            //  generate examples for all kinterms with this ego
            ArrayList<Object> round2 = new ArrayList<Object>();
            Iterator iter = dt.theory.values().iterator();  //  each value is a KinTermDef
            while (iter.hasNext()) {
                ktd = (KinTermDef) iter.next();
                //  Only generate examples of un-learned terms or learned ComposedDefs (perhaps too broad)
                if (ktd.hasDelayFlags()) {
                    round2.add(ktd);
                } else if (dt.printableBaseTerm(ktd)) {
                    ktd.generateExamples(ctxt, egoBag, orca, (KinTermDef) learner.theory.get(ktd.kinTerm));
                }
            }  // end of while hasNext()
            fill_In_Flag = true;
            for (int j = 0; j < round2.size(); j++) {  //  in Round 2, process delayed-ID terms
                ktd = (KinTermDef) round2.get(j);
                if (dt.printableBaseTerm(ktd)) {
                    ktd.generateExamples(ctxt, egoBag, orca, (KinTermDef) learner.theory.get(ktd.kinTerm));
                }
            }
            fill_In_Flag = false;
            //  now pick a new ego
            ego = randomEgo(ctxt, egoBag, ego.gender);
            //  Make dyads
            Iterator egoIter = egoBag.iterator();
            while (egoIter.hasNext()) {
                potUsedEgo = (Individual) egoIter.next();
                if (potUsedEgo.birthFamily != null || potUsedEgo.marriages.size() > 0) {  //  it was used
                    ctxt.currentEgo = potUsedEgo;
                    ctxt.addDyads(potUsedEgo);
                    egoIter.remove();
                }
            }
        }  //  end of loop for i < NUMBER_OF_EGOS
        ctxt.changeEgoTo(null);
        ctxt.simDataGen = false;
        Context.simulation = oldSimVal;
    }  //  end of method generateSimulatedData

    public Individual randomEgo(Context ctxt, ArrayList<Object> priorEgos, String priorSex) throws KinshipSystemException {
        // Development code only. No translation.        
        
        //  Pick 2 new individuals to be used as Ego:  1 with gender = priorSex to go in the front of the egoBag,
        //  and another with gender != priorSex to return to the caller.  That way, when making examples of a CB
        //  that specifies Ego's gender, we won't fall back on the previous Ego -- and potentially make a duplicate dyad.
        if (priorSex.equals("?")) {
            priorSex = "M";
        }
        int which = (int) Math.floor((Math.random() * ctxt.individualCensus.size())), reps = 0;
        Individual newEgo = (Individual) ctxt.individualCensus.get(which);
        while (priorEgos.contains(newEgo) || !newEgo.gender.equals(priorSex)) {
            which = (int) Math.floor((Math.random() * ctxt.individualCensus.size()));
            newEgo = (Individual) ctxt.individualCensus.get(which);
            if (reps++ > 100) {
                throw new KinshipSystemException("No new Egos available in Simulation");
            }
        }
        priorEgos.add(0, newEgo);  //  a person of priorSex
        reps = 0;
        which = (int) Math.floor((Math.random() * ctxt.individualCensus.size()));
        newEgo = (Individual) ctxt.individualCensus.get(which);
        while (priorEgos.contains(newEgo) || newEgo.gender.equals(priorSex)) {
            which = (int) Math.floor((Math.random() * ctxt.individualCensus.size()));
            newEgo = (Individual) ctxt.individualCensus.get(which);
            if (reps++ > 100) {
                throw new KinshipSystemException("No new Egos available in Simulation");
            }
        }
        return newEgo;
    }  //  end of method randomEgo

    public boolean neutersIn(ClauseBody cb, ClauseBody.StringObj details) {
        //  Conflicting gender constraints have already been weeded out.
        //  I'm only looking for variables that SHOULD be constrained, but aren't.
        TreeMap genderConstraints = new TreeMap(), //  personVarName => CB.StringObj (M/F)
                genderVars = new TreeMap();			//  personVarName => genderVarName AND vice versa
        ArrayList<Object> opposites = new ArrayList<Object>();		//  pairs of varNames or personVarNames
//  if (cb.ktd.kinTerm.equals("mama") && cb.seqNmbr == 87) Context.breakpoint();
        postConstraints(cb.body, genderConstraints, genderVars, opposites);
        boolean changesMade = true;
        Iterator iter;
        while (changesMade) {
            boolean allOK = true;
            iter = genderConstraints.entrySet().iterator();
            while (allOK && iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                ClauseBody.StringObj gender = (ClauseBody.StringObj) entry.getValue();
                String person = (String) entry.getKey();
                if (gender.value.equals("") && !person.equals("Ego")) {
                    allOK = false;
                }
            }
            if (allOK) {
                return false;  // Done!
            }
            changesMade = false;
            iter = genderConstraints.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                ClauseBody.StringObj gender = (ClauseBody.StringObj) entry.getValue();
                String person = (String) entry.getKey();
                if (gender.value.equals("") && !person.equals("Ego")) {  //  we need a gender
                    ArrayList<Object> oppPers = getOppo(person, opposites);
                    if (!oppPers.isEmpty()) {
                        for (int i = 0; i < oppPers.size(); i++) {
                            String oppPer = (String) oppPers.get(i);
                            String oppSexGndr = ((ClauseBody.StringObj) genderConstraints.get(oppPer)).value;
                            if (oppSexGndr.equals("M")) {
                                gender.value = "F";
                            } else if (oppSexGndr.equals("F")) {
                                gender.value = "M";
                            } else if (oppSexGndr.equals("") && oppPer.equals("Ego")) {
                                gender.value = "oppEgo";
                            } else if (oppSexGndr.equals("oppEgo")) {
                                gender.value = "samEgo";
                            } else if (oppSexGndr.equals("samEgo")) {
                                gender.value = "oppEgo";
                            }
                            if (!gender.value.equals("")) {
                                changesMade = true;  //  found one!
                            }
                        }  //  end of infer directly from opposites
                    }
                    if (gender.value.equals("") && genderVars.get(person) != null) {
                        String genVar = (String) genderVars.get(person);
                        ArrayList<Object> grp = (ArrayList<Object>) genderVars.get(genVar);  // everyone with this genderVar
                        if (grp != null) {
                            for (int i = 0; i < grp.size(); i++) {
                                String coSex = (String) grp.get(i),
                                        coSexGndr = ((ClauseBody.StringObj) genderConstraints.get(coSex)).value;
                                if (!coSexGndr.equals("")) {
                                    gender.value = coSexGndr;
                                    changesMade = true;  //  found one!
                                    i = grp.size();
                                } else if (coSex.equals("Ego")) {
                                    gender.value = "samEgo";
                                    changesMade = true;  //  found one!
                                    i = grp.size();
                                }
                            }  //  end of loop thru grp
                        }
                        ArrayList<Object> oppoVars = getOppo(genVar, opposites);
                        if (gender.value.equals("") && !oppoVars.isEmpty()) {
                            for (int i = 0; i < oppoVars.size(); i++) {
                                String oppoVar = (String) oppoVars.get(i);
                                grp = (ArrayList<Object>) genderVars.get(oppoVar);  //  people with opp gender
                                if (grp != null) {
                                    for (int j = 0; j < grp.size(); j++) {
                                        String oppSex = (String) grp.get(j),
                                                oppSexGndr = ((ClauseBody.StringObj) genderConstraints.get(oppSex)).value;
                                        if (oppSexGndr.equals("M")) {
                                            gender.value = "F";
                                        } else if (oppSexGndr.equals("F")) {
                                            gender.value = "M";
                                        } else if (oppSexGndr.equals("") && oppSex.equals("Ego")) {
                                            gender.value = "oppEgo";
                                        } else if (oppSexGndr.equals("oppEgo")) {
                                            gender.value = "samEgo";
                                        } else if (oppSexGndr.equals("samEgo")) {
                                            gender.value = "oppEgo";
                                        }
                                        if (!gender.value.equals("")) {
                                            changesMade = true;  //  found one!
                                            j = grp.size();
                                        }
                                    }  //  end of loop thru grp
                                }
                            }  //  end of infer from opposite gernderVar
                        }
                    }  //  end of infer from genderVar value
                }  //  end of we need a gender
            }  //  end of loop thru genderConstraints
        }  //  end of big while-changes-made loop
        iter = genderConstraints.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String gender = ((ClauseBody.StringObj) entry.getValue()).value,
                    person = (String) entry.getKey();
            if (gender.equals("") && !person.equals("Ego")) {
                details.value += person + ", ";
            }
        }
        details.value = details.value.substring(0, details.value.lastIndexOf(", "));
        return true;  //  Could not eliminate all neuters
    }  //  end of method neutersIn

    public ArrayList<Object> getOppo(String key, ArrayList<Object> pairs) {
        ArrayList<Object> result = new ArrayList<Object>();
        for (int i = 0; i < pairs.size(); i++) {
            ArrayList<Object> pair = (ArrayList<Object>) pairs.get(i);
            if (key.equals((String) pair.get(0))) {
                result.add(pair.get(1));
            }
            if (key.equals((String) pair.get(1))) {
                result.add(pair.get(0));
            }
        }
        return result;
    }  //  end of method getOppo

    public void postConstraints(ArrayList<Object> body, TreeMap genderConstraints, TreeMap genderVars, ArrayList<Object> opposites) {
        ArrayList<Object> unequals = new ArrayList<Object>(), genXers = new ArrayList<Object>();
        TreeMap parentsOf = new TreeMap();
        for (int i = 0; i < body.size(); i++) {
            Literal lit = (Literal) body.get(i);
            String predName = lit.predicate.name;
            ClauseBody.StringObj arg0gender = null, arg1gender = null;
            if (predName.equals("not")) { //  special treatment for 'not'
                Literal subLit = (Literal) lit.args.get(0);
                if (subLit.predicate.name.equals("equal") && lit.args.size() == 1) { //  ignore general negation
                    ArrayList<Object> pair = new ArrayList<Object>();
                    pair.add(((Argument) subLit.args.get(0)).argName);
                    pair.add(((Argument) subLit.args.get(1)).argName);
                    unequals.add(pair);
                }
            } else if (predName.equals("lessThan") || predName.equals("greaterThan") || predName.equals("lessOrEql")
                    || predName.equals("greaterOrEql") || predName.equals("contains") || predName.equals("allowCreation")
                    || predName.substring(0, 1).equals("*")) {
                //  do nothing
            } else {  //  regular predicates
                Argument arg0 = (Argument) lit.args.get(0),
                        arg1 = null;
                if (genderConstraints.get(arg0.argName) == null) {
                    arg0gender = new ClauseBody.StringObj("");
                    genderConstraints.put(arg0.argName, arg0gender);
                } else {
                    arg0gender = (ClauseBody.StringObj) genderConstraints.get(arg0.argName);
                }
                if (lit.args.size() > 1) {
                    arg1 = (Argument) lit.args.get(1);
                    if (genderConstraints.get(arg1.argName) == null) {
                        arg1gender = new ClauseBody.StringObj("");
                        genderConstraints.put(arg1.argName, arg1gender);
                    } else {
                        arg1gender = (ClauseBody.StringObj) genderConstraints.get(arg1.argName);
                    }
                }
                if (predName.equals("father") || predName.equals("son") || predName.equals("male")) {
                    arg0gender.value = "M";
                } else if (predName.equals("mother") || predName.equals("daughter") || predName.equals("female")) {
                    arg0gender.value = "F";
                } else if (predName.equals("husband")) {
                    arg0gender.value = "M";
                    arg1gender.value = "F";
                } else if (predName.equals("wife")) {
                    arg0gender.value = "F";
                    arg1gender.value = "M";
                } else if (predName.equals("spouse")) {
                    ArrayList<Object> pair = new ArrayList<Object>();
                    pair.add(arg0.argName);
                    pair.add(arg1.argName);
                    opposites.add(pair);
                } else if (predName.equals("gender")) {
                    genderVars.put(arg1.argName, arg0.argName);
                    genderConstraints.remove(arg0.argName);
                    genXers.add(arg0.argName);
                    if (genderVars.get(arg0.argName) == null) {
                        genderVars.put(arg0.argName, new ArrayList<Object>());
                    }
                    ArrayList<Object> people = (ArrayList<Object>) genderVars.get(arg0.argName);
                    people.add(arg1.argName);
                }
                //  Now identify all edWin pairs
                if (predName.equals("father") || predName.equals("son") || predName.equals("mother")
                        || predName.equals("daughter") || predName.equals("parent") || predName.equals("child")) {
                    boolean parental = (predName.equals("father") || predName.equals("parent") || predName.equals("mother"));
                    String parArgName = (parental ? arg0.argName : arg1.argName),
                            kidArgName = (!parental ? arg0.argName : arg1.argName),
                            otherParent = (String) parentsOf.get(kidArgName);  // other already known?
                    if (otherParent == null) {
                        parentsOf.put(kidArgName, parArgName);
                    } else if (!otherParent.equals(parArgName)) {
                        ArrayList<Object> pair = new ArrayList<Object>();
                        pair.add(parArgName);
                        pair.add(otherParent);
                        opposites.add(pair);
                    }  //  end of found-a-pair
                }  //  end of its-a-PC-predicate
            }  //  end of regular predicates
        }  //  end of loop thru literals
        for (int j = 0; j < unequals.size(); j++) {  //  only add gender variables, not people variables
            ArrayList<Object> pair = (ArrayList<Object>) unequals.get(j);
            if (genXers.containsAll(pair)) {
                opposites.add(pair);
            }
        }
    }  //  end of method postConstraints

    public static void createActivityLog(JDesktopPane desktop, JMenu menuView) {
        activity = new KSJInternalFrame("Error Log");
        activity.desktop = desktop;
        activity.windowNum = "Error Log";
        activity.miViewMe = menuView.add(activity.windowNum);
        activity.miViewMe.addActionListener(activity);
        activity.menuView = menuView;

        JTextArea log = new JTextArea(45, 300);
        activity.log = log;
        String logDate = UDate.today();
        log.append("########### Log of activity, errors, and warnings encountered on " + logDate + ".\n");
        log.append("########### Version 2.3, build # " + buildNmbr + ".\n");
        JScrollPane logroll = new JScrollPane(log);
        logroll.setMinimumSize(new Dimension(250, 45));
        logroll.setMaximumSize(new Dimension(850, 500));
        logroll.setPreferredSize(new Dimension(250, 45));
        activity.getContentPane().add(logroll);
        activity.setDefaultCloseOperation(HIDE_ON_CLOSE);
        desktop.add(activity);
        activity.setSize(900, 235);
        activity.setVisible(true);
        activity.moveToBack();
    }

    public void writeActivityLog() {
        if (activity == null) {
            return;
        }
        String fileName = Library.libraryDirectory + "ActivityLog";
        try {
            PrintWriter outFile = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
            outFile.println(activity.log.getText());
            outFile.flush();
            outFile.close();
            System.out.println("Wrote: " + fileName);
        } catch (IOException e) {
            System.out.println(msgs.getString("logWriteFailed") + e + "\n\n");
        } //  end of catch block

    } //  end of method writeActivityLog

    /** ContextFilter makes filters that only allow *.ctxt files and directories. 
     */
    public static class ContextFilter extends javax.swing.filechooser.FileFilter {

        //Accept all directories and all ctxt files.
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');
            if (i > 0 && i < s.length() - 1) {
                extension = s.substring(i + 1).toLowerCase();
            }
            if ((extension != null) && (extension.equals("ctxt"))) {
                return true;
            } else {
                return false;
            }
        }  //  end of method accept

        //The description of this filter
        public String getDescription() {
            return "Only Context files";
        }
    }  //  end of inner class ContextFilter

    /** 
    The inner class CleanUpThread is used to tidy up and save state before the Kinship System exits.
    It first checks all active contexts to see if any need to be saved to disk, & saves them.  It then
    writes out the Library's StubFile which serves as a directory to all files available in the Library.
     */
    public class CleanUpThread extends Thread {

        public void run() {
            if (SIL_Edit.edWin != null
                    && SIL_Edit.edWin.chart.saveFile != null) {
                try {
                    if (SIL_Edit.editingCtxt != null) {
                        Context.current = SIL_Edit.editingCtxt;
                    }
                    if (!emergencyExit) {
                        SIL_Edit.edWin.chart.saveSILKFile();
                        System.out.println("Wrote: " + SIL_Edit.edWin.chart.saveFile);
                    }
                } catch (Exception ex) {
                    System.out.println(msgs.getString("cleanUpException"));
                }
            }
            try {
                Library.writeStubFile();
                if (curr_CUC_Editor != null) {
                    curr_CUC_Editor.setClosed(true);
                }
                writeActivityLog();
            } catch (Exception goof) {
                System.out.println(msgs.getString("stubsError") + " " + goof + "\n\n");
            } //  end of catch block
        }  //  end of run method
    }  //  end of inner class CleanUpThread

    
}  //  end of class MainPane

