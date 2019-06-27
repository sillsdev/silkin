import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

/**This class defines the window in which Help Files are displayed.
 *
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 *
 * Created on Sept 16, 2011
 */
public class HelpFrame extends JFrame implements HyperlinkListener {

    static HelpFrame window = null;
    static HelpFrame.HMenu hMenu;
    static MyResBundle se = Library.screenElements;
    static final int START = 0, CHART = 1, SUGGEST = 2, PREFS = 3, 
            THEORY_EDIT = 4, CONTEXT = 5, NON_GEN = 6, PRED = 7;
    static String[] filenames = {"Start", "Chart", "Suggs", "Prefs", 
        "HornClause", "ContextEdit", "NonGen", "PredicateTable"};    
    static String[] titles = new String[8];    
    String[] pathNames = new String[8];
    ArrayList<HyperlinkEvent> stack = new ArrayList<HyperlinkEvent>();
    ArrayList<String> titleStack = new ArrayList<String>();
    int stackPtr = 0;
    boolean updateStack = true;
    
    void loadTitles() {
        titles[0] = se.getString("gettingStarted");
        titles[1] = se.getString("drawingCharts");
        titles[2] = se.getString("suggestions");
        titles[3] = se.getString("preferences");
        titles[4] = se.getString("editingDefinitions");
        titles[5] = se.getString("editingContext");
        titles[6] = se.getString("nonGenFactors");
        titles[7] = se.getString("predicateTable");        
    }
    
    public HelpFrame() {
        try {
            loadPaths();
        } catch (Exception ex) {
            MainPane.displayError(ex.toString(), 
                Library.messages.getString("seriousError"), 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        initComponents();
        labelMenus();
        window = this;
    }

    void loadPaths() throws KSInternalErrorException {
        String dirPath = Library.helpDirectory;
        String ml = Library.menuLanguage;
        String suffix = (ml.isEmpty() ? ".html" : "_" + ml + ".html");
        int ndx = 0;
        for (String fName : filenames) {
            File f = new File(dirPath + fName + suffix);
            if (!f.exists()) {
                String msg = Library.messages.getString("noHelpFile") + " '" + fName + "'.\n";
                msg += Library.messages.getString("seriousError");
                throw new KSInternalErrorException(msg);
            }
            pathNames[ndx++] = f.getAbsolutePath();
        }
    }
    
    /**Convert the page and section parameters into a proper URL, and then fire
     * a HyperLinkEvent to display that URL in the helpTextPane. Also update 
     * the 'back' button and 'forward' button
     * 
     * @param page      the Help page (file) we want to display
     * @param section   the section (invisible anchor) to display
     */
    public void displayPage(int page, String section) {
        String src = pathNames[page];
        HyperlinkEvent event =  null;
        try {
            URL newLoc = new URL("file", "", -1, src + "#" + section);
            event = new HyperlinkEvent("Request for " + src + " #" + section,
                    HyperlinkEvent.EventType.ACTIVATED, newLoc, "#" + section);
        } catch (Exception ex) {
            String msg = Library.messages.getString("errorDisplaying") + " " 
                    + src + "#" + section;
            msg += "\n" + Library.messages.getString("reason") + ": " + ex;
            MainPane.displayError(msg, Library.messages.getString("malformedURL"), 
                    JOptionPane.ERROR_MESSAGE);
        }
        updateStack = false;
        helpTextPane.fireHyperlinkUpdate(event);
        topTitle.setText(titles[page]);
        titleStack.add(0, titles[page]);
        window.setVisible(true);
        stack.add(0, event);
        backBtn.setEnabled(backable());
        fwdBtn.setEnabled(false);
        stackPtr = 0;
    }
    
    private void backBtnAction() {
        if (stackPtr >= stack.size() - 1) {
            backBtn.setEnabled(false);
            return;
        }
        updateStack = false;
        HyperlinkEvent evt = stack.get(++stackPtr);
        helpTextPane.fireHyperlinkUpdate(evt);
        topTitle.setText(titleStack.get(stackPtr)); 
        if (stackPtr >= stack.size() - 1) {
            backBtn.setEnabled(false);
        }
        fwdBtn.setEnabled(true);
    }
    
    private void fwdBtnAction() {
        if (stackPtr == 0) {
            fwdBtn.setEnabled(false);
            return;
        }
        updateStack = false;
        HyperlinkEvent evt = stack.get(--stackPtr);
        helpTextPane.fireHyperlinkUpdate(evt);
        topTitle.setText(titleStack.get(stackPtr));
        if (stackPtr == 0) {
            fwdBtn.setEnabled(false);
        }
        backBtn.setEnabled(true);
    }
    
    boolean backable() {
        return (stack.size() > 1);
    }
    
    /* This method is needed only for Windows 7 (& Vista?) users.
     * Win7 delivers only the relative address (instead of complete pathname)
     * when the User clicks on a hyperlink with href="a relative address."
     * A Mac delivers the absolute pathname no matter how the HTML href is
     * written. So this methods looks for simple relative addresses (e.g.
     * '/Chart.html') and makes them absolute. All other pathnames are
     * unchanged.
     */
    HyperlinkEvent purify(HyperlinkEvent oldEvent) {
        String path = oldEvent.getURL().getFile();
        if (path.startsWith("/")) {
            String fileName = path.substring(1), 
                   absolutePath = "";
            for (int i=0; i < filenames.length; i++) {
                if (filenames[i].equals(fileName)) {
                    absolutePath = pathNames[i];
                    break;
                }
            }
            if (!absolutePath.isEmpty()) {
                String section = oldEvent.getURL().getRef();
                HyperlinkEvent newEvent = null;
                try {
                    URL newLoc = new URL("file", "", -1, absolutePath + "#" + section);
                    newEvent = new HyperlinkEvent("Request for " + absolutePath + " #" + section,
                            HyperlinkEvent.EventType.ACTIVATED, newLoc, "#" + section);
                } catch (Exception ex) {
                    String msg = Library.messages.getString("errorDisplaying") + " " + absolutePath + "#" + section;
                    msg += "\n" + Library.messages.getString("reason") + ": " + ex;
                    MainPane.displayError(msg, Library.messages.getString("MalformedURL"), 
                            JOptionPane.ERROR_MESSAGE);
                }
                return newEvent;
            }
        }
        return oldEvent;
    }
      
    //  Method implementing HyperLinkListener. Runs when hyperlink clicked.
    public void hyperlinkUpdate(HyperlinkEvent event) {
        if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                HyperlinkEvent pureEvent = purify(event);                
                helpTextPane.setPage(pureEvent.getURL());
                if (updateStack) {
                    stack.add(0, pureEvent);
                    backBtn.setEnabled(true);
                    fwdBtn.setEnabled(false);
                    stackPtr = 0;
                    String pth = pureEvent.getURL().getFile(),
                           ttl = findTitle(pth);
                    topTitle.setText(ttl);
                    titleStack.add(0, ttl);
                }else {
                    updateStack = true;
                }
            } catch (Exception ex) {
                if (MainPane.activity == null) {
                    MainPane.createActivityLog(MainPane.desktop, MainPane.menuView);
                }
                String msg = Library.messages.getString("errorDisplaying") + " "
                        + (event.getURL().toExternalForm());
                MainPane.displayError(msg, Library.messages.getString("seriousError"), JOptionPane.ERROR_MESSAGE);
            }            
        }
    }
    
    String findTitle(String path) {
        for (int i=0; i < pathNames.length; i++) {
            if (pathNames[i].equals(path)) {
                return titles[i];
            }
        }
        return "             ";
    }   
    
    void labelMenus() {
        MyResBundle labels = Library.menuItems;
        topTitle.setText(labels.getString("silkinHelp"));  
        fileMenu.setText(labels.getString("file"));      
        closeItem.setText(labels.getString("closeHelp"));
        editMenu.setText(labels.getString("edit"));
        topicMenu.setText(labels.getString("topics"));  
        loadTitles();
    }
  
    
    void initComponents() {
        MyResBundle labels = Library.menuItems;
        setTitle(labels.getString("silkinHelp"));
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));
        setBackground(new Color(255, 255, 204));
        topTitle = new JLabel();
        Font temp = topTitle.getFont();
        Font newFont = temp.deriveFont(Font.BOLD, 16.0f);
        topTitle.setFont(newFont);
        topTitle.setAlignmentX(0.5f);
   //  Make Back and Fwd Buttons
        backBtn = new JButton("<");
        backBtn.setFont(newFont);
        backBtn.setEnabled(false);
        backBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    backBtnAction();
                }
            });
        fwdBtn = new JButton(">");
        fwdBtn.setFont(newFont);
        fwdBtn.setEnabled(false);
        fwdBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    fwdBtnAction();
                }
            });
        JPanel topRow = new JPanel();
        topRow.setLayout(new BoxLayout(topRow, BoxLayout.LINE_AXIS));
        topRow.add(backBtn);
        topRow.add(fwdBtn);
        topRow.add(Box.createRigidArea(new Dimension(20, 0)));
        topRow.add(topTitle);
        topRow.add(Box.createHorizontalGlue());
    //  Help on Start-Up
        startUpCkBox = new JCheckBox(se.getString("startHelp"));
        startUpCkBox.setSelected(SIL_Edit.edWin.helpScreenOnStartUp);
        startUpCkBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {                    
                    SIL_Edit.edWin.helpScreenOnStartUp = startUpCkBox.isSelected();                    
                }
            });
    // create menu
        jMenuBar1 = new JMenuBar();
        fileMenu = new JMenu();
        editMenu = new JMenu();
        topicMenu = new HMenu();
        closeItem = new JMenuItem();

//        closeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK));
//  For some strange reason, the keyboard accelerator doesn't work here.  ??????
        closeItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                setVisible(false);
            }
        });
        fileMenu.add(closeItem);
        jMenuBar1.add(fileMenu);
        jMenuBar1.add(editMenu);
        jMenuBar1.add(topicMenu);
        setJMenuBar(jMenuBar1);
        
    //  Make Editor Pane to display HTML files
        helpTextPane = new JEditorPane();
        helpTextPane.setEditable(false);
        helpTextPane.addHyperlinkListener(this);
        scroller = new JScrollPane(helpTextPane);
        scroller.setAlignmentX(0.5f);
        scroller.setPreferredSize(new Dimension(600, 400));
        scroller.setMinimumSize(new Dimension(300, 200));        
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
        centerPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        centerPanel.add(topRow);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        centerPanel.add(scroller);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(startUpCkBox);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        
        getContentPane().add(Box.createRigidArea(new Dimension(12, 0)));
        getContentPane().add(centerPanel);
        getContentPane().add(Box.createRigidArea(new Dimension(12, 0)));
        pack();
        setVisible(false);
    }
    
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new HelpFrame().setVisible(true);
            }
        });
    }
    
    
    // GUI Variables
    private JButton backBtn;
    private JButton fwdBtn;
    private JLabel topTitle;
    private JEditorPane helpTextPane;
    private JScrollPane scroller;
    private JMenu fileMenu;
    private JMenu editMenu;
    private JMenu topicMenu;
    private JMenuItem closeItem;
    private JMenuBar jMenuBar1;
    private JPanel centerPanel;
    private JCheckBox startUpCkBox;

    
    /**This inner class creates a top-level menu with all the Help topics and
     * subtopics. Because a menu of Help topics must appear in both the Help 
     * menu of SIL_Edit and also the Topics menu of the Help window, and the 
     * two must always agree, we keep this single menu.
     * 
     */
    public static class HMenu extends JMenu {

        public HMenu() {
            initComponents();
            labelMenuItems();
            hMenu = this;
        }
        
        void initComponents() {
        // SubMenus
            startSubMenu = new JMenu();
            chartSubMenu = new JMenu();
            contextSubMenu = new JMenu();
            suggsSubMenu = new JMenu();
            prefsSubMenu = new JMenu();
            theoryEditSubMenu = new JMenu();
            nonGenSubMenu = new JMenu();
//          Start Sub-Menus
            screenItem = new JMenuItem();
            createProjectItem = new JMenuItem();
            addPeopleItem = new JMenuItem();
            detailDisplayItem = new JMenuItem();
            refAdrItem = new JMenuItem();
            dyadsItem = new JMenuItem();
            multiplesItem = new JMenuItem();
            reservedItem = new JMenuItem();
            commentsItem = new JMenuItem();
//          Chart Sub-Menus
            datesItem = new JMenuItem();
            egoAlterItem = new JMenuItem();
            labelItem = new JMenuItem();
            editableItem = new JMenuItem();
            reshapingItem = new JMenuItem();
            movingItem = new JMenuItem();
            deleteRelItem = new JMenuItem();
            deletePeopleItem = new JMenuItem();
            multiChartItem = new JMenuItem();
            linkItem = new JMenuItem();
            deletingChartsItem = new JMenuItem();
            printChartItem = new JMenuItem();
//          Context Editor Sub-Menus
            definitionItem = new JMenuItem();
            editorItem = new JMenuItem();
            deletedRecsItem = new JMenuItem();
            udpsItem = new JMenuItem();
            editTheoryItem = new JMenuItem();
//          Suggestions Sub-Menus
            getSuggsItem = new JMenuItem();
            actSuggsItem = new JMenuItem();
//          Preferences Sub-Menus
            prefIntroItem = new JMenuItem();
            ignorableItem = new JMenuItem();
            maxItem = new JMenuItem();
            subPatternItem = new JMenuItem();
            inductionItem = new JMenuItem();
            undoItem = new JMenuItem();
            polygamyItem = new JMenuItem();
            nameCaptureItem = new JMenuItem();
            snapToGridItem = new JMenuItem();
            adoptionHelpItem = new JMenuItem();
            linkPriorityItem = new JMenuItem();
            chartPrintingItem = new JMenuItem();
//          Theory Edit Sub-Menus
            layoutItem = new JMenuItem();
            tutorialItem = new JMenuItem();
            rulesItem = new JMenuItem();
            predefinedItem = new JMenuItem();
            auxiliariesItem = new JMenuItem();
            bottomBtnsItem = new JMenuItem();
            sampleSessionItem = new JMenuItem();
            variableGenItem = new JMenuItem();
            editCommentsItem = new JMenuItem();
//          Adoption Sub-Menus
            adoptionIntroItem = new JMenuItem();
            adoptionItem = new JMenuItem();
            clansItem = new JMenuItem();
            adoptionRulesItem = new JMenuItem();
            adoptionDeleteItem = new JMenuItem();
            predicateTableItem = new JMenuItem();

            // Getting Started SubMenu
            add(startSubMenu);

            screenItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(START, "screen");
                }
            });
            startSubMenu.add(screenItem);

            createProjectItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(START, "create");
                }
            });
            startSubMenu.add(createProjectItem);

            addPeopleItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(START, "add");
                }
            });
            startSubMenu.add(addPeopleItem);

            detailDisplayItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(START, "detail");
                }
            });
            startSubMenu.add(detailDisplayItem);

            refAdrItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(START, "terms");
                }
            });
            startSubMenu.add(refAdrItem);

            dyadsItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(START, "dyads");
                }
            });
            startSubMenu.add(dyadsItem);
            
            multiplesItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(START, "multiples");
                }
            });
            startSubMenu.add(multiplesItem);
            
            reservedItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(START, "reserved");
                }
            });
            startSubMenu.add(reservedItem);
            
            commentsItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(START, "comments");
                }
            });
            startSubMenu.add(commentsItem);
            
// Draw/Edit Charts SubMenu       
            add(chartSubMenu);

            egoAlterItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CHART, "ego");
                }
            });
            chartSubMenu.add(egoAlterItem);

            datesItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CHART, "dates");
                }
            });
            chartSubMenu.add(datesItem);

            labelItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CHART, "label");
                }
            });
            chartSubMenu.add(labelItem);

            editableItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CHART, "editable");
                }
            });
            chartSubMenu.add(editableItem);

            reshapingItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CHART, "reshape");
                }
            });
            chartSubMenu.add(reshapingItem);

            movingItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CHART, "move");
                }
            });
            chartSubMenu.add(movingItem);

            deleteRelItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CHART, "del-rel");
                }
            });
            chartSubMenu.add(deleteRelItem);

            deletePeopleItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CHART, "del-ppl");
                }
            });
            chartSubMenu.add(deletePeopleItem);
            
            multiChartItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CHART, "multicharts");
                }
            });
            chartSubMenu.add(multiChartItem);
            
            linkItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CHART, "links");
                }
            });
            chartSubMenu.add(linkItem);
            
            deletingChartsItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CHART, "deletingcharts");
                }
            });
            chartSubMenu.add(deletingChartsItem);
            
            printChartItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CHART, "print");
                }
            });
            chartSubMenu.add(printChartItem);
            
            
//  Context Editor SubMenu
            add(contextSubMenu);
            
            definitionItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CONTEXT, "definition");
                }
            });
            contextSubMenu.add(definitionItem);
            
            editorItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CONTEXT, "editor");
                }
            });
            contextSubMenu.add(editorItem);
            
            deletedRecsItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CONTEXT, "deleted-records");
                }
            });
            contextSubMenu.add(deletedRecsItem);
            
            udpsItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CONTEXT, "UDPs");
                }
            });
            contextSubMenu.add(udpsItem);
            
            editTheoryItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(CONTEXT, "EditTheory");
                }
            });
            contextSubMenu.add(editTheoryItem);                      

 // Suggestions SubMenu       
            add(suggsSubMenu);

            getSuggsItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(SUGGEST, "get");
                }
            });
            suggsSubMenu.add(getSuggsItem);

            actSuggsItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(SUGGEST, "act");
                }
            });
            suggsSubMenu.add(actSuggsItem);

            undoItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(SUGGEST, "undo");
                }
            });
            suggsSubMenu.add(undoItem);


// Edit Prefs SubMenu       
            add(prefsSubMenu);

            prefIntroItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(PREFS, "intro");
                }
            });
            prefsSubMenu.add(prefIntroItem);

            ignorableItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(PREFS, "ignore");
                }
            });
            prefsSubMenu.add(ignorableItem);

            maxItem.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(PREFS, "max");
                }
            });
            prefsSubMenu.add(maxItem);

            subPatternItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(PREFS, "sub");
                }
            });
            prefsSubMenu.add(subPatternItem);

            inductionItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(PREFS, "induce");
                }
            });
            prefsSubMenu.add(inductionItem);

            polygamyItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(PREFS, "poly");
                }
            });
            prefsSubMenu.add(polygamyItem);

            nameCaptureItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(PREFS, "capture");
                }
            });
            prefsSubMenu.add(nameCaptureItem);

            snapToGridItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(PREFS, "grid");
                }
            });
            prefsSubMenu.add(snapToGridItem);

            adoptionHelpItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(PREFS, "adoption");
                }
            });
            prefsSubMenu.add(adoptionHelpItem);

            linkPriorityItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(PREFS, "priority");
                }
            });
            prefsSubMenu.add(linkPriorityItem);
            
            chartPrintingItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(PREFS, "printing");
                }
            });
            prefsSubMenu.add(chartPrintingItem);
            
// Theory Editor SubMenu
            add(theoryEditSubMenu);

            layoutItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(THEORY_EDIT, "layout");
                }
            });
            theoryEditSubMenu.add(layoutItem);

            tutorialItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(THEORY_EDIT, "tutorial");
                }
            });
            theoryEditSubMenu.add(tutorialItem);

            rulesItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(THEORY_EDIT, "rules");
                }
            });
            theoryEditSubMenu.add(rulesItem);

            predefinedItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(THEORY_EDIT, "pre-defined-terms");
                }
            });
            theoryEditSubMenu.add(predefinedItem);

            auxiliariesItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(THEORY_EDIT, "auxiliaries");
                }
            });
            theoryEditSubMenu.add(auxiliariesItem);

            bottomBtnsItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(THEORY_EDIT, "bottom-buttons");
                }
            });
            theoryEditSubMenu.add(bottomBtnsItem);

            sampleSessionItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(THEORY_EDIT, "sample-session");
                }
            });
            theoryEditSubMenu.add(sampleSessionItem);

            variableGenItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(THEORY_EDIT, "variable-generation");
                }
            });
            theoryEditSubMenu.add(variableGenItem);

            editCommentsItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(THEORY_EDIT, "edit-comments");
                }
            });
            theoryEditSubMenu.add(editCommentsItem);
            
 // Non-Genealogical SubMenu
            add(nonGenSubMenu);

            adoptionIntroItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(NON_GEN, "intro");
                }
            });
            nonGenSubMenu.add(adoptionIntroItem);

            adoptionItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(NON_GEN, "adoption");
                }
            });
            nonGenSubMenu.add(adoptionItem);

            adoptionRulesItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(NON_GEN, "rules");
                }
            });
            nonGenSubMenu.add(adoptionRulesItem);

            clansItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(NON_GEN, "clans");
                }
            });
            nonGenSubMenu.add(clansItem);

            adoptionDeleteItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(NON_GEN, "delete");
                }
            });
            nonGenSubMenu.add(adoptionDeleteItem);
            
            predicateTableItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    window.displayPage(PRED, "top");
                }
            });
            add(predicateTableItem);
        }
        
        void labelMenuItems() {
            MyResBundle labels = Library.menuItems;
            startSubMenu.setText(labels.getString("getStarted"));
            screenItem.setText(labels.getString("basic"));
            createProjectItem.setText(labels.getString("openCtxt"));
            addPeopleItem.setText(labels.getString("addPeople"));
            detailDisplayItem.setText(labels.getString("detailDisplay"));
            refAdrItem.setText(labels.getString("refAdr"));
            dyadsItem.setText(labels.getString("makingDyads"));
            multiplesItem.setText(labels.getString("multiKin"));
            reservedItem.setText(labels.getString("resWords"));
            commentsItem.setText(labels.getString("restComments"));
            chartSubMenu.setText(labels.getString("chartSub"));
            egoAlterItem.setText(labels.getString("egoAlter"));
            datesItem.setText(labels.getString("dateFormats"));
            labelItem.setText(labels.getString("labelOptions"));
            editableItem.setText(labels.getString("editable"));
            reshapingItem.setText(labels.getString("reshape"));
            movingItem.setText(labels.getString("moving"));
            deleteRelItem.setText(labels.getString("delRel"));
            deletePeopleItem.setText(labels.getString("delPeople"));
            multiChartItem.setText(labels.getString("multiChart"));
            linkItem.setText(labels.getString("link"));
            deletingChartsItem.setText(labels.getString("delChart"));
            printChartItem.setText(labels.getString("printCharts"));
            contextSubMenu.setText(labels.getString("ctxtEd"));
            definitionItem.setText(labels.getString("whatCtxt"));
            editorItem.setText(labels.getString("theCtxtEd"));
            deletedRecsItem.setText(labels.getString("delRecs"));
            udpsItem.setText(labels.getString("udps"));
            editTheoryItem.setText(labels.getString("edDT"));
            suggsSubMenu.setText(labels.getString("suggs"));
            getSuggsItem.setText(labels.getString("gettingSuggs"));
            actSuggsItem.setText(labels.getString("actSuggs"));
            undoItem.setText(labels.getString("undo"));
            prefsSubMenu.setText(labels.getString("edPref"));
            prefIntroItem.setText(labels.getString("prefLearning"));
            ignorableItem.setText(labels.getString("ignorable"));
            maxItem.setText(labels.getString("maxItem"));
            subPatternItem.setText(labels.getString("subPat"));
            inductionItem.setText(labels.getString("induction"));
            polygamyItem.setText(labels.getString("polygamy"));
            nameCaptureItem.setText(labels.getString("nameCap"));
            snapToGridItem.setText(labels.getString("snap"));
            adoptionHelpItem.setText(labels.getString("adoptHelp"));
            linkPriorityItem.setText(labels.getString("linkPri"));
            chartPrintingItem.setText(labels.getString("fonts"));
            theoryEditSubMenu.setText(labels.getString("edDef"));
            layoutItem.setText(labels.getString("layout"));
            tutorialItem.setText(labels.getString("whatHC"));
            rulesItem.setText(labels.getString("rulesHC"));
            predefinedItem.setText(labels.getString("predefs"));
            auxiliariesItem.setText(labels.getString("auxTerms"));
            bottomBtnsItem.setText(labels.getString("validating"));
            sampleSessionItem.setText(labels.getString("sample"));
            variableGenItem.setText(labels.getString("varGen"));
            editCommentsItem.setText(labels.getString("edComments"));
            nonGenSubMenu.setText(labels.getString("nonGen"));
            adoptionIntroItem.setText(labels.getString("adoptIntro"));
            adoptionItem.setText(labels.getString("adoption"));
            adoptionRulesItem.setText(labels.getString("adoptRules"));
            clansItem.setText(labels.getString("clans"));
            adoptionDeleteItem.setText(labels.getString("adoptDelete"));
            predicateTableItem.setText(labels.getString("predTable"));
        }
        
        private JMenu startSubMenu;
        private JMenu chartSubMenu;
        private JMenu contextSubMenu;
        private JMenu suggsSubMenu;
        private JMenu prefsSubMenu;
        private JMenu theoryEditSubMenu;
        private JMenu nonGenSubMenu;
        private JMenuItem screenItem;
        private JMenuItem createProjectItem;
        private JMenuItem addPeopleItem;
        private JMenuItem detailDisplayItem;
        private JMenuItem refAdrItem;
        private JMenuItem dyadsItem;
        private JMenuItem multiplesItem;
        private JMenuItem reservedItem;
        private JMenuItem commentsItem;       
        private JMenuItem datesItem;
        private JMenuItem egoAlterItem;
        private JMenuItem labelItem;
        private JMenuItem editableItem;
        private JMenuItem reshapingItem;
        private JMenuItem movingItem;
        private JMenuItem deleteRelItem;
        private JMenuItem deletePeopleItem;
        private JMenuItem multiChartItem;
        private JMenuItem linkItem;
        private JMenuItem deletingChartsItem;
        private JMenuItem printChartItem;
        private JMenuItem getSuggsItem;
        private JMenuItem actSuggsItem;
        private JMenuItem undoItem;
        private JMenuItem prefIntroItem;
        private JMenuItem ignorableItem;
        private JMenuItem maxItem;
        private JMenuItem subPatternItem;
        private JMenuItem inductionItem;
        private JMenuItem polygamyItem;
        private JMenuItem nameCaptureItem;
        private JMenuItem snapToGridItem;
        private JMenuItem adoptionHelpItem;
        private JMenuItem linkPriorityItem;
        private JMenuItem chartPrintingItem;
        private JMenuItem layoutItem;
        private JMenuItem tutorialItem;
        private JMenuItem rulesItem;
        private JMenuItem predefinedItem;
        private JMenuItem auxiliariesItem;
        private JMenuItem bottomBtnsItem;
        private JMenuItem sampleSessionItem;
        private JMenuItem variableGenItem;
        private JMenuItem editCommentsItem;
        private JMenuItem definitionItem;
        private JMenuItem editorItem;
        private JMenuItem deletedRecsItem;
        private JMenuItem udpsItem;
        private JMenuItem editTheoryItem;
        private JMenuItem adoptionIntroItem;
        private JMenuItem adoptionItem;
        private JMenuItem clansItem;
        private JMenuItem adoptionDeleteItem;
        private JMenuItem adoptionRulesItem;
        private JMenuItem predicateTableItem;
    }
}
