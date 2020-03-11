import javax.swing.*;
import java.io.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;
import java.util.*;

/**This class is part of the SILKin program's GUI. It is modeled after the KAES
 * KinshipEditor by Michael D. Fischer of the Centre for Social Anthropology and
 * Computing, University of Kent, U.K. Those portions taken from his code are
 * copyrighted by him; all rights reserved. See his complete copyright statement
 * and terms of re-use in Marriage.java in this package.
 * <p>
 * SIL_Edit is the main GUI window for the Data-Gathering activity of SILKin. It
 * enables a field user to graphically create a family tree for one or more
 * individuals and to provide the kinship terms used among relatives on that
 * tree.
 * </p><p>
 *  * NOTE: This file originally created via the form editor (GUI Builder) in
 * NetBeans 6.9.1 For unknown reasons the form editor quit working, so I
 * stripped out the comments that seem to guide the editor, deleted the form
 * file, and now use this strictly as a SWING GUI file.
 * </p>
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
*/
public class SIL_Edit extends JFrame {

    static SIL_Edit edWin;
    static DecisionFrame decisionFrame;
    static Context editingCtxt;
    static final int REF = 0, ADR = 1;
    static final int minDyadsPerPCStr = 2;
    /**This field controls whether the Help Window is displayed when a session
     * begins. The default is 'true' but when set to 'false' that choice is
     * stored in the SILK file and persists to subsequent sessions. 
     */
    public static boolean helpScreenOnStartUp = true;
    static TreeMap<String, PropagationMethod> linkMethods = buildLinkMethods();
    RecentActionListener recentAC = new RecentActionListener();
    
    /**Build a TreeMap of methods for propagating from each primitive predicate.
     * 
     * @return a TreeMap with key = PCString symbol for a primitive predicate
     *                        value = the appropriate PropagationMethod
     */
    public static TreeMap<String, PropagationMethod> buildLinkMethods() {
        TreeMap<String, PropagationMethod> lm = new TreeMap<>();
        lm.put("Fa", new ParentMethod());
        lm.put("Mo", new ParentMethod());
        lm.put("P", new ParentMethod());
        lm.put("So", new ChildMethod());
        lm.put("Da", new ChildMethod());
        lm.put("C", new ChildMethod());
        lm.put("Hu", new SpouseMethod());
        lm.put("Wi", new SpouseMethod());
        lm.put("S", new SpouseMethod());
        lm.put("Bro", new SiblingMethod());
        lm.put("Sis", new SiblingMethod());
        lm.put("Sib", new SiblingMethod());
        lm.put("Hbro", new HalfSiblingMethod());
        lm.put("Hsis", new HalfSiblingMethod());
        lm.put("Stfa", new StepParentMethod());
        lm.put("Stmo", new StepParentMethod());
        lm.put("Stbro", new StepSiblingMethod());
        lm.put("Stsis", new StepSiblingMethod());
        lm.put("Stso", new StepChildMethod());
        lm.put("Stda", new StepChildMethod());
        return lm;
    }
    boolean loadingCharts = false;
    Individual infoPerson = null;
    Family infoMarriage = null;
    private int currentEgo = 0;
//    private Runtime topDog = Runtime.getRuntime();
    public KinTermMatrix ktm;
    TreeMap<String, ArrayList<Issue>> suggestionsRef, suggestionsAdr;

    /**
     * Creates new form SIL_Edit
     */
    public SIL_Edit() {
        edWin = this;
        try {
            if (Library.stubs.isEmpty()) {
                Library.readStubFile();
            }
        } catch (Exception e) {
            System.err.println(e.toString());
            e.printStackTrace();
            MainPane.displayError(e.toString(), "Internal Problem", JOptionPane.ERROR_MESSAGE);
        }
        addWindowFocusListener(new FocusListener());
        initComponents();  //  initComponents is NetBeans-generated code
        prepComponents();  //  prepComponents is my own code
        labelMenuItems();        
        // Listen for window closing, so Library.stubs file can be saved before exit.
//        topDog.addShutdownHook(new CleanUpThread());
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        personPanel1 = new PersonPanel();
        familyPanel1 = new FamilyPanel();
        chartScrollPane = new JScrollPane();
        chartHolderPanel = new JPanel();
        chartHolderHorizontal = new JPanel();
        jMenuBar1 = new JMenuBar();
        fileMenu = new JMenu();
        fileNewMenu = new JMenu();
        newContextItem = new JMenuItem();
        newLiBrowserItem = new JMenuItem();
        loadItem = new JMenuItem();
        loadRecent = new JMenu();
        importGEDCOMitem = new JMenuItem();
        gedcomSubMenu = new JMenu();
        exportGEDCOMItem = new JMenuItem();
        exportKAESItem = new JMenuItem();
        exportDefinitionsItem = new JMenuItem();
        jSeparator1 = new JPopupMenu.Separator();
        saveItem = new JMenuItem();
        saveAsItem = new JMenuItem();
        jSeparator3 = new JPopupMenu.Separator();
        deleteAllItem = new JMenuItem();
        jSeparator4 = new JPopupMenu.Separator();
        printMenu = new JMenu();
        printVisible = new JMenuItem();
        printCurrentChart = new JMenuItem();
        printAllCharts = new JMenuItem();
        pageSetupItem = new JMenuItem();
        quitItem = new JMenuItem();
        editMenu = new JMenu();
        cutItem = new JMenuItem();
        copyItem = new JMenuItem();
        pasteItem = new JMenuItem();
        jSeparator2 = new JPopupMenu.Separator();
        editChartDescriptionItem = new JMenuItem();
        deleteCurrentChartItem = new JMenuItem();
        jSeparator5 = new JPopupMenu.Separator();
        editPrefsItem = new JMenuItem();
        chooseLanguageItem = new JMenuItem();
        editContextItem = new JMenuItem();
        adminSILKinItem = new JMenuItem();
        labelMenu = new JMenu();
        nameButtonGroup = new ButtonGroup();
        kinTermButtonGroup = new ButtonGroup();
        noNameBtn = new JRadioButtonMenuItem();
        initialsBtn = new JRadioButtonMenuItem();
        firstNameBtn = new JRadioButtonMenuItem();
        lastNameBtn = new JRadioButtonMenuItem();
        wholeNameBtn = new JRadioButtonMenuItem();
        noKinTermBtn = new JRadioButtonMenuItem();
        kinTmRefBtn = new JRadioButtonMenuItem();
        kinTmAdrBtn = new JRadioButtonMenuItem();
        ltrRefBtn = new JRadioButtonMenuItem();
        ltrAdrBtn = new JRadioButtonMenuItem();
        contextMenu = new JMenu();
        distinctAdrItem = new JCheckBoxMenuItem();
        editableItem = new JCheckBoxMenuItem();
        snapToGrid = new JCheckBoxMenuItem();
        displayGEDCOM = new JCheckBoxMenuItem();
        getSuggestionsItem = new JMenuItem();
        actOnSuggsItem = new JMenuItem();
        actOnSuggsItem.setEnabled(false);
        returnToSuggsItem = new JMenuItem();
        helpMenu = new HelpFrame.HMenu();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        // Initialize window window, but keep invisible
        HelpFrame.window = new HelpFrame();

        newContextItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
        newContextItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newContextActionPerformed(evt);
            }
        });
        fileNewMenu.add(newContextItem);

        newLiBrowserItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                newLiBrowserActionPerformed(evt);
            }
        });
        fileNewMenu.add(newLiBrowserItem);

        fileMenu.add(fileNewMenu);

        loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        loadItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loadItemActionPerformed(evt);
            }
        });
        fileMenu.add(loadItem);
        
        rebuildRecentSubMenu();
        fileMenu.add(loadRecent);
        
        importGEDCOMitem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                importGEDCOMItemActionPerformed(evt);
            }
        });
        gedcomSubMenu.add(importGEDCOMitem);

        exportDefinitionsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exportDefinitionsItemActionPerformed(evt);
            }
        });
        exportGEDCOMItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exportGEDCOMItemActionPerformed(evt);
            }
        });
        exportKAESItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                exportKAESItemActionPerformed(evt);
            }
        });
        exportKAESItem.setVisible(false);
        
//        gedcomSubMenu.add(exportDefinitionsItem);
//        gedcomSubMenu.add(exportKAESItem);
        gedcomSubMenu.add(exportGEDCOMItem);
        fileMenu.add(gedcomSubMenu);

        fileMenu.add(jSeparator1);

        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        saveItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveItem);

        saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK | InputEvent.CTRL_MASK));
        saveAsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveAsItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsItem);
        fileMenu.add(jSeparator3);

        deleteAllItem.setEnabled(true);
        deleteAllItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                deleteAllItemActionPerformed(evt);
            }
        });
        fileMenu.add(deleteAllItem);
        fileMenu.add(jSeparator4);

        printVisible.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                printVisibleItemActionPerformed(evt);
            }
        });
        printMenu.add(printVisible);

        printCurrentChart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                printCurrentChartItemActionPerformed(evt);
            }
        });
        printMenu.add(printCurrentChart);

        printAllCharts.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                printAllChartsItemActionPerformed(evt);
            }
        });
        printMenu.add(printAllCharts);

        fileMenu.add(printMenu);

        pageSetupItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                pageSetupItemActionPerformed(evt);
            }
        });
        fileMenu.add(pageSetupItem);

        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
        quitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                quitItemActionPerformed(evt);
            }
        });
        fileMenu.add(quitItem);

        jMenuBar1.add(fileMenu);

        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0));
        editMenu.add(cutItem);

        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
        editMenu.add(copyItem);

        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0));
        editMenu.add(pasteItem);
        editMenu.add(jSeparator2);

        editChartDescriptionItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editChartDescriptionItemActionPerformed(evt);
            }
        });
        editMenu.add(editChartDescriptionItem);

        deleteCurrentChartItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                deleteCurrentChartItemActionPerformed(evt);
            }
        });
        editMenu.add(deleteCurrentChartItem);
        editMenu.add(jSeparator5);
        
        editPrefsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editPrefsItemActionPerformed(evt);
            }
        });
        editMenu.add(editPrefsItem);

        chooseLanguageItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                chooseLanguageItemActionPerformed(evt);
            }
        });
        editMenu.add(chooseLanguageItem);

        editContextItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editContextItemActionPerformed(evt);
            }
        });
        editMenu.add(editContextItem);

        adminSILKinItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                adminSILKinItemActionPerformed(evt);
            }
        });
        editMenu.add(adminSILKinItem);

        jMenuBar1.add(editMenu);

        nameButtonGroup.add(noNameBtn);
        labelMenu.add(noNameBtn);
        noNameBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                noNameBtnActionPerformed(evt);
            }
        });
        labelMenu.add(noNameBtn);

        nameButtonGroup.add(initialsBtn);
        initialsBtn.setSelected(true);
        initialsBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                initialsBtnActionPerformed(evt);
            }
        });
        labelMenu.add(initialsBtn);

        nameButtonGroup.add(firstNameBtn);
        firstNameBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                firstNameBtnActionPerformed(evt);
            }
        });
        labelMenu.add(firstNameBtn);

        nameButtonGroup.add(lastNameBtn);
        lastNameBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                lastNameBtnActionPerformed(evt);
            }
        });
        labelMenu.add(lastNameBtn);

        nameButtonGroup.add(wholeNameBtn);
        wholeNameBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                wholeNameBtnActionPerformed(evt);
            }
        });
        labelMenu.add(wholeNameBtn);
        labelMenu.add(jSeparator1);

        kinTermButtonGroup.add(noKinTermBtn);
        noKinTermBtn.setSelected(true);
        noKinTermBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                noKinTermBtnActionPerformed(evt);
            }
        });
        labelMenu.add(noKinTermBtn);

        kinTermButtonGroup.add(kinTmRefBtn);
        kinTmRefBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                kinTmRefBtnActionPerformed(evt);
            }
        });
        labelMenu.add(kinTmRefBtn);

        kinTermButtonGroup.add(kinTmAdrBtn);
        kinTmAdrBtn.setEnabled(false);
        kinTmAdrBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                kinTmAdrBtnActionPerformed(evt);
            }
        });
        labelMenu.add(kinTmAdrBtn);

        kinTermButtonGroup.add(ltrRefBtn);
        ltrRefBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ltrRefBtnActionPerformed(evt);
            }
        });
        labelMenu.add(ltrRefBtn);

        kinTermButtonGroup.add(ltrAdrBtn);
        ltrAdrBtn.setEnabled(false);
        ltrAdrBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ltrAdrBtnActionPerformed(evt);
            }
        });
        labelMenu.add(ltrAdrBtn);

        jMenuBar1.add(labelMenu);

        distinctAdrItem.setSelected(false);
        distinctAdrItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                distinctAdrItemActionPerformed(evt);
            }
        });
        contextMenu.add(distinctAdrItem);

        editableItem.setSelected(true);
        editableItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                editableItemActionPerformed(evt);
            }
        });
        contextMenu.add(editableItem);

        setSnap(true);
        snapToGrid.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                snapToGridActionPerformed(evt);
            }
        });
        contextMenu.add(snapToGrid);

        displayGEDCOM.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                displayGEDCOMActionPerformed(evt);
            }
        });
        displayGEDCOM.setVisible(false);
        contextMenu.add(displayGEDCOM);

        contextMenu.add(jSeparator1);

        getSuggestionsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                getSuggestionsItemActionPerformed(evt);
            }
        });
        contextMenu.add(getSuggestionsItem);

        actOnSuggsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                actOnSuggsActionPerformed(evt);
            }
        });
        contextMenu.add(actOnSuggsItem);

        returnToSuggsItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                returnToSuggestionsItemActionPerformed(evt);
            }
        });
        contextMenu.add(returnToSuggsItem);
        returnToSuggsItem.setEnabled(false);

        jMenuBar1.add(contextMenu);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);
        
// Layout the frame. Assemble the Chart Area        
        chart = new ChartPanel();
        chart.init(this);
        chartScrollPane.setPreferredSize(new Dimension(877, 350));
        chartScrollPane.setViewportView(chart);
        chartComboBox = new JComboBox();
        chartComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chartComboBoxActionPerformed(evt);
            }
        });
        chartComboModel = (DefaultComboBoxModel) chartComboBox.getModel();
        rebuildChartCombo();
        chartComboBox.setAlignmentX(0.5f);
        chartComboBox.setPreferredSize(new Dimension(400, 20));
        chartComboBox.setMaximumSize(new Dimension(400, 20));
        chartScrollPane.setAlignmentX(0.5f);
        chartHolderPanel.setLayout(new BoxLayout(chartHolderPanel, BoxLayout.PAGE_AXIS));
        chartHolderPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        chartHolderHorizontal.setLayout(new BoxLayout(chartHolderHorizontal, BoxLayout.LINE_AXIS));
        chartHolderHorizontal.add(Box.createRigidArea(new Dimension(8, 0)));
        chartHolderHorizontal.add(chartScrollPane);
        chartHolderHorizontal.add(Box.createRigidArea(new Dimension(8, 0)));
        chartHolderHorizontal.setBackground(new Color(255, 228, 225));

        chartHolderPanel.add(chartComboBox);
        chartHolderPanel.add(chartHolderHorizontal);
        chartHolderPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        chartHolderPanel.setBorder(BorderFactory.createEtchedBorder(Color.red, Color.blue));
        chartHolderPanel.setBackground(new Color(255, 228, 225));

// Add components to the JFrame's content pane
        MyResBundle labels = Library.screenElements;
        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(chartHolderPanel);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(new JLabel(labels.getString("detailDisplay")));
        add(personPanel1);
        add(familyPanel1);
    }
    
    public void labelMenuItems() {
        MyResBundle labels = Library.menuItems;        
        fileMenu.setText(labels.getString("file"));
        fileNewMenu.setText(labels.getString("new"));
        newContextItem.setText(labels.getString("context"));
        newLiBrowserItem.setText(labels.getString("liBrowser"));        
        loadItem.setText(labels.getString("open"));
        loadRecent.setText(labels.getString("openRecent"));
        gedcomSubMenu.setText(labels.getString("gedcom"));
        importGEDCOMitem.setText(labels.getString("importGedcom"));
        exportDefinitionsItem.setText(labels.getString("definitions"));
        exportDefinitionsItem.setVisible(false);
        exportGEDCOMItem.setText(labels.getString("exportGedcom"));
        exportKAESItem.setText(labels.getString("kaes"));
        saveItem.setText(labels.getString("save"));
        saveAsItem.setText(labels.getString("saveAs"));
        deleteAllItem.setText(labels.getString("clearAll"));
        quitItem.setText(labels.getString("exit"));
        editMenu.setText(labels.getString("edit"));
        cutItem.setText(labels.getString("cut"));
        copyItem.setText(labels.getString("copy"));
        pasteItem.setText(labels.getString("paste"));
        editPrefsItem.setText(labels.getString("editPrefs"));
        chooseLanguageItem.setText(labels.getString("chooseMenuLang"));
        editContextItem.setText(labels.getString("editContext"));
        adminSILKinItem.setText(labels.getString("adminMode"));
        labelMenu.setText(labels.getString("labels")); 
        noNameBtn.setText(labels.getString("noName"));
        initialsBtn.setText(labels.getString("initials"));
        firstNameBtn.setText(labels.getString("firstName"));
        lastNameBtn.setText(labels.getString("lastName"));
        wholeNameBtn.setText(labels.getString("wholeName"));
        noKinTermBtn.setText(labels.getString("noKinTerm"));
        kinTmRefBtn.setText(labels.getString("kinTermRef"));
        kinTmAdrBtn.setText(labels.getString("kinTermAdr"));
        ltrRefBtn.setText(labels.getString("letterRef"));
        ltrAdrBtn.setText(labels.getString("letterAdr"));
        distinctAdrItem.setText(labels.getString("distinctTerms"));
        contextMenu.setText(labels.getString("context"));
        editableItem.setText(labels.getString("contextEditable"));
        snapToGrid.setText(labels.getString("snap"));
        getSuggestionsItem.setText(labels.getString("getSuggs"));
        actOnSuggsItem.setText(labels.getString("actSuggs"));
        returnToSuggsItem.setText(labels.getString("returnSuggs"));
        helpMenu.setText(labels.getString("help"));
        printMenu.setText(labels.getString("print"));
        printVisible.setText(labels.getString("printVisible"));
        printCurrentChart.setText(labels.getString("printCurrent"));
        printAllCharts.setText(labels.getString("printAll"));
        pageSetupItem.setText(labels.getString("pageSetup"));
        editChartDescriptionItem.setText(labels.getString("editDescr"));
        deleteCurrentChartItem.setText(labels.getString("deleteChart"));
        displayGEDCOM.setText(labels.getString("displayGedcom"));        
    }
    
    /**Rebuild the menu of recently-loaded SILK files. This will be called every
     * time User loads a SILK file using the 'Open' menu option. 
     */ 
    public void rebuildRecentSubMenu() {
        loadRecent.removeAll();
        int n = 0;
        for (File f : Library.recentFiles) {
            if (f != null) {
                JMenuItem item = new JMenuItem(f.getName());
                String ac = "n " + n++;
                item.setActionCommand(ac);
                item.setToolTipText(f.getParent());
                item.addActionListener(recentAC);
                loadRecent.add(item);
            }
        }
        try {
            Library.writeStubFile();
        } catch (JavaSystemException jse) {
            MainPane.displayError(jse.toString(), "Error Writing Stub File",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void prepComponents() {
        personPanel1.setVisible(true);
        familyPanel1.setVisible(false);
        // Set Edit Menu items to the DefaultEditorKit methods
        editMenu.remove(copyItem);
        editMenu.remove(cutItem);
        editMenu.remove(pasteItem);
        editMenu.remove(jSeparator2);
        editMenu.remove(editPrefsItem);
        editMenu.remove(editContextItem);
        editMenu.remove(adminSILKinItem);
        cutItem = new JMenuItem(new DefaultEditorKit.CutAction());
//        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0));
        copyItem = new JMenuItem(new DefaultEditorKit.CopyAction());
//        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
        pasteItem = new JMenuItem(new DefaultEditorKit.PasteAction());
//        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0));
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.add(jSeparator2);
        editMenu.add(editPrefsItem);
        editMenu.add(editContextItem);
        editMenu.add(adminSILKinItem);
        setSize(new Dimension(1050, 725));
        newLiBrowserItem.setEnabled(false);
        exportGEDCOMItem.setEnabled(true);
        printAllCharts.setEnabled(true);
        pack();
    }
    
    static String findMutualChart(Individual ind1, Individual ind2) {
        ArrayList<String> charts1 = new ArrayList<String>(),
                charts2 = new ArrayList<String>();
        charts1.add(ind1.homeChart);
        charts2.add(ind2.homeChart);
        if (ind1.links != null) {
            for (Link lk : ind1.links) {
                charts1.add(lk.homeChart);
            }
        }
        if (ind2.links != null) {
            for (Link lk : ind2.links) {
                charts2.add(lk.homeChart);
            }
        }
        charts1.retainAll(charts2);  // Set intersection
        if (charts1.isEmpty()) {
            return null;
        } else {
            return charts1.get(0);
        }
    }   

    /**Rebuild the ComboBox that holds the menu of chart pages. Include all
     * charts for the current context's diagram. Add a choice to 'Create A New
     * Chart.' 
     */ 
    public void rebuildChartCombo() {
        loadingCharts = true;
        chartComboBox.removeAllItems();
        MyResBundle labels = Library.screenElements;
        String newItem = labels.getString("drawBelow");
        if (Context.current != null && Context.current.chartDescriptions != null) {
            int ndx = 0;
            newItem = labels.getString("createNewChart");
            chartComboModel.addElement(newItem);
            for (int i = 0; i < Context.current.chartDescriptions.size(); i++) {
                newItem = Context.current.chartLtrs[i];
                if (newItem.equals(Context.current.currentChart)) {
                    ndx = i + 1;
                }
                newItem += ": " + Context.current.chartDescriptions.get(i);
                if (newItem.length() > 85) {
                    newItem = newItem.substring(0, 84);
                }
                chartComboModel.addElement(newItem);
            }
            if (ndx == 0)  {  //  0 means chart descriptions are empty
                newItem = labels.getString("defaultChart");
                chartComboModel.addElement(newItem);
                Context.current.chartDescriptions.add(newItem);
                ndx = 1;
            }
            chartComboBox.setSelectedIndex(ndx);
            chartComboBox.setEnabled(true);
        } else {
            chartComboModel.addElement(newItem);
            chartComboBox.setSelectedIndex(0);
            chartComboBox.setEnabled(false);
        }        
        loadingCharts = false;
    }

    private void newContextActionPerformed(ActionEvent evt) {
        if (chart.dirty) {
            chart.doWantToSave();
        }
        if (chart.editable) {
            chart.deleteAll();
        }
    }

    /**Post whichever option the User has selected to {@link Library}'s variable.
     * 
     * @param onOff User's choice, made in 'Edit Prefs.'
     */ 
    public void setSnap(boolean onOff) {
        snapToGrid.setSelected(onOff);
    }

    private void newLiBrowserActionPerformed(ActionEvent evt) {
        Library.currentActivity = Library.BROWSING;
        if (Context.current != null) {
            editingCtxt = Context.current;
        }
        SILKin.createAndShowGUI();
        if (Context.current != null) {
            MainPane.topPane.browseCurrentContext();
        } else {
            MainPane.topPane.browseLibrary();
        }
    }

    /**Call the ChartPanel method 'pickSILKFile()' to start the process of
     * loading a SILK file for a Context.
     * 
     * @param evt the event type (not used)
     */ 
    public void loadItemActionPerformed(ActionEvent evt) {
        chart.pickSILKFile();
    }
    
    /**
     * Call for MainPane's GEDCOM import method to parse a file into a new
     * context. If that was successful, add the Standard Macros if needed, record
     * the new context and its elements in all required locations, re-build the
     * displays from the new context data, and set Ego to the lowest numbered 
     * person who is not deleted.
     * @param evt 
     */
    public void importGEDCOMItemActionPerformed(ActionEvent evt) {
        MainPane.topPane.importGEDCOM();
        Context ctxt = Context.current;
        if (ctxt == null) { // action cancelled
            return;
        }
        Library.contextUnderConstruction = ctxt;
        editingCtxt = ctxt;
        ktm = ctxt.ktm;
        try {
            DomainTheory dt = ctxt.domTheoryRef();
            if (!dt.theory.containsKey("step_brother")) {
                Linus macroLineServer = new Linus(Library.libraryDirectory + "Standard_Macros");
                Tokenizer tok = new Tokenizer(Library.getDFA(), macroLineServer);
                ParserDomainTheory parzer = new ParserDomainTheory(tok, tok);
                parzer.parseStandardMacros(dt);
            }
            if (ctxt.kinTypeOrder == null) {
                ctxt.loadDefaultKinTypeStuff();
            }
            ctxt.rebuildLinkMethods();
        } catch(Exception exc) {
            MainPane.displayError(exc.toString(), 
                    Library.messages.getString("errorLoadingStandardMacros"), 
                    JOptionPane.ERROR_MESSAGE);
        }
        rebuildChartCombo();
        rebuildKTMatrixEtc();
        Person.folks = ctxt.individualCensus;
        Marriage.knots = ctxt.familyCensus;
        personPanel1.rebuildEgoBox();
        displayGEDCOM.setVisible(true);
        displayGEDCOM.setSelected(true);
        int n;
        for (n=0; n < ctxt.indSerNumGen; n++) {
            Individual ind = ctxt.individualCensus.get(n);
            if (! ind.deleted) {
                break;
            }
        }
        changeEgo(n);
    }

    /**
     * Call MainPane's export method and we're done.
     * @param evt the mouse event that clicked on this menu item
     */
    public void exportGEDCOMItemActionPerformed(ActionEvent evt) {
        MainPane.topPane.exportGEDCOM();
    }

    private void exportKAESItemActionPerformed(ActionEvent evt) {
        chart.exportKAESFile();
    }

    private void exportDefinitionsItemActionPerformed(ActionEvent evt) {
        // TODO Implement writing a .thy file
    }

    /**Call ChartPanel's's saveSILKFile method and we're done.
     */
    public void saveItemActionPerformed(ActionEvent evt) {
        chart.saveSILKFile();
    }

    /**Call ChartPanel's's saveAsFile method and we're done.
     */
    public void saveAsItemActionPerformed(ActionEvent evt) {
        chart.saveAsFile();
    }

    /**Clear the current context from all displays and memory.
     * 
     * @param evt the mouse event that chose this menu item
     */
    public void deleteAllItemActionPerformed(ActionEvent evt) {
        if (chart.editable) {
            chart.deleteAll();
            setTitle("");
            loadingCharts = true;
            chartComboBox.removeAllItems();
            chartComboModel.addElement(Library.screenElements.getString("drawBelow"));
            chartComboBox.setSelectedIndex(0);
            chartComboBox.setEnabled(false);
            loadingCharts = false;
            
        }
    }

    /**Create a {@link PrintChart} with the 'Whole' parameter = false.
     * This will suppress a chart title and cause only the visible portion
     * of current chart to be printed. Then call the PrintChart's printTheChart
     * method.
     * 
     * @param evt the mouse event that chose this menu item
     */
    public void printVisibleItemActionPerformed(ActionEvent evt) {
        //  No chart title is printed on a partial print.
        PrintChart pc = new PrintChart(chartScrollPane, false);
        pc.printTheChart();
    }

    /**
     * Create a {@link PrintChart} with the 'Whole' parameter = true.
     * This will select the entire current chart. Then call the PrintChart's 
     * printTheChart method.
     * 
     * @param evt the mouse event that chose this menu item
     */
    public void printCurrentChartItemActionPerformed(ActionEvent evt) {
        PrintChart pc = new PrintChart(chartScrollPane, true);
        pc.printTheChart();
    }

    /**
     * Create a {@link PrintChart} with the 'Whole' parameter = true for each
     * chart in the current context. Then call the PrintChart's 
     * printTheChart method on each one.
     * 
     * @param evt the mouse event that chose this menu item
     */
    public void printAllChartsItemActionPerformed(ActionEvent evt) {
        PrintChart pc = new PrintChart(chartScrollPane, true);
        for (int i = 0; i < Context.current.chartDescriptions.size(); i++) {
            Context.current.currentChart = Context.current.getChartLtr(i);
            pc.printTheChart();
        }
    }

    /**Generate a page setup dialog box to capture User's preferences.
     * 
     * @param evt the event type (not used)
     */ 
    public void pageSetupItemActionPerformed(ActionEvent evt) {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat defaultPage = job.defaultPage();
        defaultPage.setOrientation(PageFormat.LANDSCAPE);
        PrintChart.pgFormat = job.pageDialog(defaultPage);
    }

    /**
     * Check for any unsaved changes; if found, ask User if she wants to save.
     * Then call Java exit method. 
     * 
     * @param evt the mouse click that chose this menu item.
     */
    public void quitItemActionPerformed(ActionEvent evt) {
        if (SIL_Edit.edWin.chart.dirty) {
            SIL_Edit.edWin.chart.doWantToSave();
        }
        try {  //  must write no matter what -- for MenuLang
            Library.writeStubFile();
        } catch (Exception e) {
            // None expected. If ecception happens, just proceed.
        }
        System.exit(0);
    }

    /** Solicit from User a new descriptor string for this chart, correct any
     * prohibited characters, and then rebuild the Chart menu.
     * 
     * @param evt the menu choice event
     */
    public void editChartDescriptionItemActionPerformed(ActionEvent evt) {
        String ch = Context.current.currentChart;
        String msg = Library.messages.getString("newChartDescription") + " " + ch + ":\n";
        int ndx = Context.current.getChartIndex(ch);
        msg += Context.current.chartDescriptions.get(ndx);
        String newDescription = JOptionPane.showInputDialog(this, msg);
        if (newDescription != null) {
            Context.current.chartDescriptions.set(ndx, 
                    FamilyPanel.convertBannedCharacters(newDescription));
            rebuildChartCombo();
        }
    }

    public void deleteCurrentChartItemActionPerformed(ActionEvent evt) {
        String chrt = Context.current.currentChart;
        MyResBundle rb = Library.messages;
        String msg = rb.getString("chart") + " " + chrt + " " + rb.getString("contains"), 
               comma = "";
        boolean problems = false;
        ArrayList<Family> fams = new ArrayList<Family>();
        ArrayList<Individual> inds = new ArrayList<Individual>();
        ArrayList<Link> linx = new ArrayList<Link>();
        for (Family fam : Context.current.familyCensus) {
            if (fam.homeChart.equals(chrt)) {
                fams.add(fam);
            }
        }
        for (Individual ind : Context.current.individualCensus) {
            if (ind.homeChart.equals(chrt)) {
                inds.add(ind);
            }
        }
        for (Link lk : Context.current.linkCensus) {
            if (lk.homeChart.equals(chrt)) {
                linx.add(lk);
            }
        }
        int fs = fams.size(), is = inds.size(), ls = linx.size();
        if (fs + is + ls > 0) {
            if (fs > 0) {
                msg += fs + " famil" + (fs == 1 ? "y" : "ies");
                comma = ", ";
            }
            if (is > 0) {
                msg += comma + is + " individual" + (is == 1 ? "" : "s");
                comma = ", ";
            }
            if (ls > 0) {
                msg += comma + ls + " link" + (ls == 1 ? "" : "s");
            }
            msg += ".\n" + (fs + is + ls > 1 ? "These" : "This");
            msg += " will be deleted. Un-deletion is difficult.\nAre you SURE?";
            int choice = JOptionPane.showConfirmDialog(this, msg, "DANGEROUS ACTION",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.NO_OPTION) {
                return;
            }
            msg = "If any person (not link) on this chart is connected to another chart,\n"
                    + "the connection must be broken before you can delete that person\n"
                    + "along with this chart.\nPROCEED?";
            choice = JOptionPane.showConfirmDialog(this, msg, "Rules for Deletions",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.NO_OPTION) {
                return;
            }
            // Proceed with deletion of people, families, and links
            for (Family fam : fams) {
                chart.deleteFam(fam);
            }
            for (Individual ind : inds) {
                try {
                    chart.deleteIndividual(ind);
                } catch (KinshipSystemException exc) {
                    problems = true;
                }
            }
            if (infoPerson.deleted) {
                getPPanel().displayClues();
                repaint();
                infoPerson = null;
            }
            for (Link lk : linx) {
                try {
                    chart.deleteLink(lk);
                } catch (KinshipSystemException exc) {
                    problems = true;
                }                
            }
        }
        // Now delete the chart itself
        if (!problems) {
            deleteChart(chrt);
            chartComboBox.setSelectedIndex(1);
            
        }
    }
    
    void deleteChart(String chrt) {
        Context ctxt = Context.current;
        int ndx = ctxt.getChartIndex(chrt);
        ctxt.chartDescriptions.remove(ndx);
        if (ndx != ctxt.chartDescriptions.size()) {
            // wasn't the last one -- had to remove it from middle
            for (Individual ind : ctxt.individualCensus) {
                if (! ind.deleted && ind.homeChart.compareTo(chrt) > 0) {
                    ndx = ctxt.getChartIndex(ind.homeChart);
                    ind.homeChart = ctxt.getChartLtr(ndx -1);
                }
            }
            for (Family fam : ctxt.familyCensus) {
                if (! fam.deleted && fam.homeChart.compareTo(chrt) > 0) {
                    ndx = ctxt.getChartIndex(fam.homeChart);
                    fam.homeChart = ctxt.getChartLtr(ndx -1);
                }
            }
            for (Link lk : ctxt.linkCensus) {
                if (! lk.deleted && lk.homeChart.compareTo(chrt) > 0) {
                    ndx = ctxt.getChartIndex(lk.homeChart);
                    lk.homeChart = ctxt.getChartLtr(ndx -1);
                }
            }            
        }
        ndx = ctxt.chartDescriptions.size() -1;
        ctxt.currentChart = ctxt.getChartLtr(ndx);
        rebuildChartCombo();
        getPPanel().rebuildEgoBox();
        if (infoPerson != null) {
            showInfo(infoPerson);
        }else if (infoMarriage != null) {
            showInfo(infoMarriage);
        }
    }    

    public void chooseLanguageItemActionPerformed(ActionEvent evt) {
        Library.priorMenuLanguage = Library.menuLanguage;
        Library.menuLanguage = "";
        Library.setMenuLanguage();
        edWin.labelMenuItems();
        try {
            HelpFrame.window.loadPaths();
        } catch(Exception ex) {
            Library.menuLanguage = "";
            try {
                Library.writeStubFile();
            } catch(Exception ex2) {
                // Well, we tried to erase the unfortunate choice.
            }
            MyResBundle rb = Library.messages;
            String msg = rb.getString("menuLanguageMissingFile");
            msg += ex + rb.getString("getTechSupport");
            JOptionPane.showMessageDialog(this, msg, rb.getString("seriousError"), 
                    JOptionPane.WARNING_MESSAGE);
            System.exit(9);
        }
        HelpFrame.hMenu.labelMenuItems();
        HelpFrame.window.labelMenus();
        if (MainPane.topPane != null) {
            MainPane.topPane.labelMenuItems();
        }
        edWin.chart.dirty = true;
    }

    public void editPrefsItemActionPerformed(ActionEvent evt) {
        if (chart.saveFile == null || Context.current == null) {
            MyResBundle rb = Library.messages;
            String msg3 = rb.getString("particularPrefs");
            msg3 += rb.getString("firstOPEN");;
            JOptionPane.showMessageDialog(this, msg3, rb.getString("cannotPerform"), 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        EditPrefsWindow epw = new EditPrefsWindow();
        epw.setVisible(true);
    }

    private void editContextItemActionPerformed(ActionEvent evt) {
        Library.currentActivity = Library.DATA_GATHERING;
        if (SILKin.mainPane == null) {
            SILKin.createAndShowGUI();
            MainPane.topPane.menuAdmin.setEnabled(false);
            MainPane.topPane.editCUC();
        } else {
            MainPane.topPane.editCUC();
            MainPane.topPane.menuAdmin.setEnabled(false);
            MainPane.topPane.enableAdvancedMenuItems(false);
            MainPane.topPane.setVisible(true);
        }       
    }

    private void adminSILKinItemActionPerformed(ActionEvent evt) {
        MyResBundle rb = Library.messages;
        String pwd = JOptionPane.showInputDialog(edWin, rb.getString("enterAdminPassword"));
        if (pwd.equals(Library.PASSWORD)) {
            Library.currentActivity = Library.ADMIN;
            if (SILKin.mainPane == null) {
                SILKin.createAndShowGUI();
            } else {
                MainPane.topPane.setVisible(true);
            }
            MainPane.topPane.menuAdmin.setEnabled(true);
            MainPane.topPane.enableAdvancedMenuItems(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    rb.getString("tryAgain"),
                    rb.getString("invalidPassword"),
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    /** This menu choice ActionPerformed calls a ChartPanel method
     * that records the labeling option. the other 7 menu choice
     * actions do the same thing.
     * 
     * @param evt the menu choice event
     */
    public void noNameBtnActionPerformed(ActionEvent evt) {
        chart.setNameLabel("No Label");
    }

    private void initialsBtnActionPerformed(ActionEvent evt) {
        chart.setNameLabel("Initials");
    }

    private void firstNameBtnActionPerformed(ActionEvent evt) {
        chart.setNameLabel("First");
    }

    private void lastNameBtnActionPerformed(ActionEvent evt) {
        chart.setNameLabel("Last");
    }

    private void wholeNameBtnActionPerformed(ActionEvent evt) {
        chart.setNameLabel("Whole");
    }

    private void noKinTermBtnActionPerformed(ActionEvent evt) {
        chart.setKTmLabel("No KinTerm");
    }

    private void kinTmRefBtnActionPerformed(ActionEvent evt) {
        chart.setKTmLabel("KinTermRef");
    }

    private void kinTmAdrBtnActionPerformed(ActionEvent evt) {
        chart.setKTmLabel("KinTermAdr");
    }

    private void ltrRefBtnActionPerformed(ActionEvent evt) {
        chart.setKTmLabel("LetterRef");
    }

    private void ltrAdrBtnActionPerformed(ActionEvent evt) {
        chart.setKTmLabel("LetterAdr");
    }

    public void setDistinctAdrMenuItemSelected(boolean val) {
        resettingDistinctAdrItem = true;
        distinctAdrItem.setSelected(val);
        resettingDistinctAdrItem = false;
    }
    private boolean resettingDistinctAdrItem = false;

    public void distinctAdrItemActionPerformed(ActionEvent evt) {
        if (resettingDistinctAdrItem) {
            return;
        }
        MyResBundle rb = Library.messages;
        if (distinctAdrItem.isSelected()) {
            String msg = rb.getString("copyRefTerms"),
                    title = rb.getString("initAdr"),
                    btn1 = rb.getString("copyThem"),
                    btn2 = rb.getString("doNotCopy"),
                    btn3 = rb.getString("cancel");
            Object[] btns = {btn1, btn2, btn3};
            int ch = JOptionPane.showOptionDialog(this,
                    msg, title,
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, btns, btns[0]);
            if (ch == JOptionPane.CANCEL_OPTION) {
                distinctAdrItem.setSelected(false);
                return;
            }
            boolean copy = (ch == JOptionPane.YES_OPTION);
            personPanel1.setDistinctAdrTerms(true);
            activateDomThAdr(copy);
        } else { // De-selection            
            String msg = rb.getString("eraseAdr");
            Object[] btns = { rb.getString("eraseEverything"), rb.getString("cancel") };
            int ch = JOptionPane.showOptionDialog(this,
                    msg, rb.getString("erasingAdr"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, btns, btns[1]);
            if (ch == JOptionPane.CANCEL_OPTION) {
                distinctAdrItem.setSelected(true);
                return;
            }
            personPanel1.setDistinctAdrTerms(false);
            deactivateDomThAdr();
        }
    }

    void activateDomThAdr(boolean copy) {
        Context ctxt = Context.current;
        DomainTheory dtRef = null, dtAdr = null;
        try {
            dtRef = ctxt.domTheoryRef();
            dtAdr = new DomainTheory(dtRef);  //  i.e. clone it
            dtAdr.addressTerms = true;
            dtAdr.languageName = ctxt.languageName + "(Adr)";
            if (!copy) {
                dtAdr.theory = new TreeMap();
            }
            ctxt.addDomainTheory(dtAdr);
        } catch (Exception ex) {
            MainPane.displayError(Library.messages.getString("errorAccessingRef"),
                    Library.messages.getString("seriousError"), 
                    JOptionPane.WARNING_MESSAGE);
        }
        ctxt.learningHistoryAdr = new TreeMap<String, ArrayList<Context.HistoryItem>>(ctxt.learningHistoryRef);
        if (copy) {
            dtAdr.dyadsDefined = dtRef.dyadsDefined.convertToAdr();
            dtAdr.dyadsUndefined = dtRef.dyadsUndefined.convertToAdr();
            dtAdr.issuesForUser.putAll(dtRef.issuesForUser);
            ctxt.ktm.addAdrCloneTerms();
            ctxt.autoDefAdr = new TreeMap<String, ArrayList<Context.CB_Ptr>>(ctxt.autoDefRef);
        } else {
            ctxt.autoDefAdr = new TreeMap<String, ArrayList<Context.CB_Ptr>>();
        }
    }

    void deactivateDomThAdr() {
        Context ctxt = Context.current;
        ctxt.domTheoryAdrNullify();
        ctxt.autoDefAdr = null;
        ctxt.learningHistoryAdr = null;
        ctxt.ktm.cleanAdrTerms();
    }

    private void editableItemActionPerformed(ActionEvent evt) {
        if (editableItem.isSelected()) {
            chart.editable = true;
            Context.current.editable = true;
        } else {
            chart.editable = false;
            Context.current.editable = false;
        }
    }

    private void snapToGridActionPerformed(ActionEvent evt) {
        if (snapToGrid.isSelected()) {
            Library.snapToGrid = true;
        } else {
            Library.snapToGrid = false;
        }
    }
    
    // 
    private void displayGEDCOMActionPerformed(ActionEvent evt) {
        Context.current.displayGEDCOM = displayGEDCOM.isSelected();
        personPanel1.repaint();
    }
    
    

    public void setActOnSuggsEnabled(boolean yn) {
        actOnSuggsItem.setEnabled(yn);
    }

    /** After assuring all needed resources are present, save the SILK file, 
     * create and load a learner and then launch active learning. 
     * Store the results here and on the appropriate domain theory. 
     * (Repeat for the DomainTheory(Adr) if needed.) then re-save the SILK
     * file that now contains suggestions.
     * Finally, make an HTML file of the suggestions that can be presented
     * to the User, and activate the menu item 'Act on Suggestions'.
     * 
     * @param evt the menu choice event
     */
    public void getSuggestionsItemActionPerformed(ActionEvent evt) {
        MyResBundle rb = Library.messages;
        try {
            chart.saveSILKFile();
            if (Library.predEncodings == null) {
                Library.readPredEncodings();
            }
            if (Library.predDecodings == null) {
                Library.readPredDecodings();
            }
            Library.synUmbDetectOn = true;
            Context ctxt = Context.current;
            if (ctxt == null) {
                String msg = rb.getString("mustCreateData");
                MainPane.displayError(msg, rb.getString("cannotPerform"), JOptionPane.PLAIN_MESSAGE);
                return;
            }
            int priorIndSerial = ctxt.indSerNumGen,
                    priorFamSerial = ctxt.famSerNumGen;
            DomainTheory dtRef = ctxt.domTheoryRef();
            Learned_DT learner = new Learned_DT(dtRef);
            loadParameters(learner);
            learner.truncateUDPpcStrings(ctxt);
            doActiveLearning(learner);
            // in case artificial people were created during learning
            ctxt.resetTo(priorIndSerial, priorFamSerial);
            suggestionsRef = learner.issuesForUser;
            dtRef.issuesForUser = suggestionsRef;
            if (ctxt.distinctAdrTerms) {
                DomainTheory dtAdr = ctxt.domTheoryAdr();
                learner = new Learned_DT(dtAdr);
                loadParameters(learner);
                doActiveLearning(learner);
                ctxt.resetTo(priorIndSerial, priorFamSerial);
                suggestionsAdr = learner.issuesForUser;
                dtAdr.issuesForUser = suggestionsAdr;
            }
            chart.saveSILKFile();
            File suggsDataFile = chart.saveFile;
            String ml = Library.menuLanguage;
            if (! ml.isEmpty()) {
                ml = "_" + ml;
            }
            String styleFilePath = Library.libraryDirectory + 
                    "Suggestions/silk-status" + ml + ".xsl";
            File stylesheet = new File(styleFilePath);
            if (!stylesheet.exists()) {
                String msg = rb.getString("noStyleFile");
                MainPane.displayError(msg, rb.getString("can'tGetSuggs"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            File outfile = makeHTMLfile(suggsDataFile);
            XMLTransformer tformer = new XMLTransformer();
            tformer.transform(suggsDataFile, stylesheet, outfile);
            ctxt.dateOfLastSuggestion = UDate.today();
        } catch (KinshipSystemException internalExc) {
            String msg = rb.getString("internalError") + " " + internalExc;
            MainPane.displayError(msg, rb.getString("internalError"), JOptionPane.PLAIN_MESSAGE);
        } catch (Exception exc) {
            String msg = rb.getString("systemError") + " " + exc;
            MainPane.displayError(msg, rb.getString("systemError"), JOptionPane.ERROR_MESSAGE);
        }
        actOnSuggsItem.setEnabled(true);
    }

    void loadParameters(Learned_DT learner) {
        learner.doBaseCBs = learner.ctxt.doBaseCBs;
        learner.doInduction = learner.ctxt.doInduction;
    }

    File makeHTMLfile(String lName) {
        String outFilePath = Library.libraryDirectory + "Suggestions/";
        outFilePath += lName + ".html";
        return new File(outFilePath);
    }

    File makeHTMLfile(File s) {
        String outFilePath = Library.libraryDirectory + "Suggestions/";
        String lName = s.getName();
        int dot = lName.lastIndexOf(".");
        lName = lName.substring(0, dot);
        outFilePath += lName + ".html";
        return new File(outFilePath);
    }

    private void actOnSuggsActionPerformed(ActionEvent evt) {
        TreeMap<String, ArrayList<Issue>> suggs = pickSuggs();
        MyResBundle rb = Library.messages;
        if (suggs == null) {
            String msg = rb.getString("noSuggsToAct");
            MainPane.displayError(msg, rb.getString("noSuggs"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        File htmlFile = makeHTMLfile(chart.saveFile);
        if (!htmlFile.exists()) { // 1st tried name of edited file - failed
            try {
            String msg = rb.getString("cannotFindFile") + " '" + htmlFile.getName()
                    + "' " + rb.getString("inSuggsDir");
            String baseName = Context.current.languageName;
            htmlFile = makeHTMLfile(baseName);
            if (!htmlFile.exists()) {  // 2nd tried langName - failed
                msg += "\n" + rb.getString("cannotFindFile") + " " + htmlFile.getName() + ".";
                msg += "\n" + rb.getString("maybeGetSuggs");
                MainPane.displayError(msg, rb.getString("noSuggs"), JOptionPane.ERROR_MESSAGE);
                return;
            } else { // maybe this is it
                msg += "\n" + rb.getString("errorAccessingRef");
                String uz = rb.getString("use") + " " + htmlFile.getName(); 
                Object[] btns = {uz, rb.getString("cancel")};
                int ch = JOptionPane.showOptionDialog(this,
                        msg, rb.getString("looking4Suggs"),
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, btns, btns[0]);
                if (ch == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            }catch(Exception exc) {
            // UTF-8 is not going to cause an exception.    
            }
        }
        decisionFrame = new DecisionFrame(suggs, suggsInFocus, htmlFile);        
        decisionFrame.setVisible(true);
        returnToSuggsItem.setEnabled(true);
            
    }
    
    
    private void returnToSuggestionsItemActionPerformed(ActionEvent evt) {
        try {
            storeInfo();
        } catch (Exception ex) {
        }  //  nothing can go wrong ....
        if (decisionFrame == null || !decisionFrame.isShowing()) {
            actOnSuggsActionPerformed(evt);
        } else {
            decisionFrame.toFront();
        }
    }

    public void chartComboUpdateTitle() {
        loadingCharts = true;
        Context c = Context.current;
        chartComboBox.setSelectedIndex(c.getChartIndex(c.currentChart) + 1);
        loadingCharts = false;
    }
    
    public void chartComboSetEnabled(boolean bool) {
        chartComboBox.setEnabled(bool);
    }
    
    public void goToChart(String ltr) {
        Context c = Context.current;
        chartComboBox.setSelectedIndex(c.getChartIndex(ltr) + 1);
        chartComboBoxActionPerformed(new ActionEvent(this, 0, ltr));
    }

    private void chartComboBoxActionPerformed(ActionEvent evt) {
        if (loadingCharts) {
            return;
        }
        try {
            storeInfo();
        } catch (Exception ex) {
        }  //  nothing can go wrong ....
        if (infoPerson != null) {
            chart.whichFolk = infoPerson.serialNmbr;
        }
        getPPanel().storing = true;
        MyResBundle msgs = Library.messages,
                       items = Library.menuItems;
        String choice = (String) chartComboBox.getSelectedItem(), nxtLtr, msg;
        if (choice.equals(items.getString("createNewChart"))) {
            nxtLtr = Context.current.getNextChartLtr();
            if (nxtLtr == null) { // unlikely - 52 chart letters available
                msg = msgs.getString("52Charts");
                JOptionPane.showInternalMessageDialog(this, msg, 
                        msgs.getString("tooManyCharts"), JOptionPane.ERROR_MESSAGE);
                getPPanel().storing = false;
                return;
            }
            String newDescription = JOptionPane.showInputDialog(this, msgs.getString("shortDescr"));
            if (newDescription == null) {
                chartComboBox.setSelectedIndex(Context.current.getChartIndex(Context.current.currentChart) +1);
                return;
            }
            newDescription = FamilyPanel.convertBannedCharacters(newDescription);
            Context.current.chartDescriptions.add(newDescription);
            Context.current.currentChart = nxtLtr;
            rebuildChartCombo();
        } else {
            int colon = choice.indexOf(":");
            Context.current.currentChart = choice.substring(0, colon);
        }
        chart.checkSizeOfChart(Context.current);
        chart.resizeAndRepaint();
        getPPanel().storing = false;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        //  This is the start point for GitSILKin, the published version.
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Predicate.loadPrimitiveNames();
                    if (Library.stubs.isEmpty()) {
                        Library.readStubFile();
                    }
                    Library.setMenuLanguage();
                    String title = Library.screenElements.getString("kinshipAnalysis");
                    SILKin.mainPane = new MainPane(title);
                } catch (Exception e) {
                    System.err.println(e);
                }
                new SIL_Edit().setVisible(true);
                if (helpScreenOnStartUp) {
                    HelpFrame.window.displayPage(HelpFrame.START, "screen");
                }
            }
        });
    }
    
    static void getUserLanguage() throws JavaSystemException {
        ArrayList<String> langCodes = new ArrayList<String>(), 
                         langDescriptions = new ArrayList<String>();
        String line, choice, filename = Library.resourceDirectory + "ValidLanguages.txt";
        String msg1 = "File not found", msg2 = "Serious Error", 
               msg3 = "Choose the language in which SILKin\nwill display menus and messages.",
               msg4 = "Choose Menu Language";
        int comma;
        BufferedReader file;
        JFrame aBox = new AboutBox();
        Component parent;
        if (edWin != null) {
            parent = edWin;
        } else {
            parent = aBox;
            aBox.setVisible(true);
        }
        MyResBundle msgs = Library.messages;
        if (msgs != null) {
            msg1 = msgs.getString("fileNotFound");
            msg2 = msgs.getString("seriousError");
            msg3 = msgs.getString("chooseLang");
            msg4 = msgs.getString("chooseMenuLang");
        }
        try {
            file = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            Object[] btns = {msgs.getString("oK"), msgs.getString("cancel")};
            JOptionPane.showOptionDialog(parent,
                    "'ValidLanguages' " + msg1,
                    msg2,
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null, btns, btns[0]);
            throw new JavaSystemException("'ValidLanguages' "
                    + msg1 + "\n" + e);
        }
        try {
            line = file.readLine();
            while (line != null) {
                comma = line.indexOf(",");
                langCodes.add(line.substring(0, comma++));
                langDescriptions.add(line.substring(comma));
                line = file.readLine();
            }
            int s = langDescriptions.size();
            Object[] lds = new Object[s];
            for (int i = 0; i < s; i++) {
                lds[i] = langDescriptions.get(i);
            }
            choice = (String) JOptionPane.showInputDialog(
                    parent,
                    msg3,
                    msg4,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    lds,
                    "English(US)");
            if (choice == null) { // User cancelled
                if (! Library.priorMenuLanguage.isEmpty()) {
                    // prior choice continued
                    aBox.dispose();
                    Library.menuLanguage = Library.priorMenuLanguage;
                    return;
                }
                JOptionPane.showMessageDialog(parent, 
                    "Default (English) assumed.", 
                    "Menu Language Required", 
                    JOptionPane.PLAIN_MESSAGE);
                s = 0;
            }else {
                s = langDescriptions.indexOf(choice);
            }            
            Library.menuLanguage = langCodes.get(s);            
        } catch (Exception exc) {
            throw new JavaSystemException("'ValidLanguages' " + 
                    msgs.getString("fileProcessError") + "\n" + exc);
        }
        if (Library.menuLanguage != null && choice != null) {
            Locale userLocale = new Locale(Library.menuLanguage);
            try {
            msgs = MyResBundle.getBundle("Messages", userLocale, null);
            JOptionPane.showMessageDialog(parent, 
                msgs.getString("restart"), 
                msgs.getString("restartTitle"), 
                JOptionPane.PLAIN_MESSAGE);
            }catch(Exception exc) {
                // Don't bother.
            }
        }
        aBox.dispose();  
        Library.langCodes = langCodes;
    }

    /**This case statement is original KAES code.
     * 
     * @param labelName     User's choice for displaying a person's name
     * @param labelKTerm    User's choice for displaying the kin term
     */ 
    public void synchronizeLabelParams(int labelName, int labelKTerm) {
        Person.nameLabel = labelName;
        switch (labelName) {
            case 0: // no name
                noNameBtn.setSelected(true);
                break;
            case 1: // initials
                initialsBtn.setSelected(true);
                break;
            case 2: // first name
                firstNameBtn.setSelected(true);
                break;
            case 3: // last name
                lastNameBtn.setSelected(true);
                break;
            case 4: // whole name
                wholeNameBtn.setSelected(true);
                break;
        }
        Person.kinTermLabel = labelKTerm;
        switch (labelKTerm) {
            case 0: // no name
                noKinTermBtn.setSelected(true);
                break;
            case 1: // kin term Ref
                kinTmRefBtn.setSelected(true);
                break;
            case 2: // kin term Adr
                kinTmAdrBtn.setSelected(true);
                break;
            case 3: // letter Ref
                ltrRefBtn.setSelected(true);
                break;
            case 4: // letter Adr
                ltrAdrBtn.setSelected(true);
                break;
        }
    }

    /**
     * Rebuild the KinTermMatrix's node lattice from the population's graph of
     * Individuals and Families. Any nodes from the old KTM that have exact
     * counterparts in the new KTM can copy their kin terms to the new one.
     *
     * After the KTM is regenerated, build a new set of DyadTMaps and the Kin
     * Type Index from it.
     */
    public void rebuildKTMatrixEtc() {
        KinTermMatrix oldKTM = ktm;
        ktm = new KinTermMatrix();
        for (Individual pers : Context.current.individualCensus) {
            pers.node = null;
        }
        int oldEgo = currentEgo;
        for (Individual pers : Context.current.individualCensus) {
            if (!pers.deleted) {
                int id = pers.serialNmbr;
                TreeMap newRow = new TreeMap();
                ktm.matrix.put(id, newRow);
                currentEgo = id;
                pers.node = Node.makeSelfNode(Context.current.distinctAdrTerms);
                pers.node.indiv = pers;
                newRow.put(id, pers.node);
                KSQ bfq = new KSQ();
                bfq.enQ(pers);
                propagateNodes(bfq, newRow, null);
                TreeMap oldRow = (TreeMap) oldKTM.matrix.get(id);
                if (oldRow != null) {
                    copyNodes(oldRow, newRow);
                }
                for (Object n : newRow.values()) {  //  reset nodes
                    ((Node) n).indiv.node = null;
                }
            }
        }  //  KTM is now regenerated. Install as official.
        Context.current.ktm = ktm;
        PersonPanel.fillDyadsFromMatrix();
        regenerateKTI();  // rebuild Kin Type Index
        edWin.hideEgoChange = true;
        edWin.changeEgo(oldEgo);
        edWin.hideEgoChange = false;
    }

    void regenerateKTI() {
        KinTypeIndex kti = (Context.current.kti = new KinTypeIndex());
        Iterator ktmIter = ktm.matrix.entrySet().iterator();
        while (ktmIter.hasNext()) {
            Map.Entry entry = (Map.Entry) ktmIter.next();
            Integer egoInt = (Integer) entry.getKey();
            TreeMap row = (TreeMap) entry.getValue();
            kti.updateFromRow(egoInt, row);
        }
    }
    
    boolean hideEgoChange = false;

    boolean changeEgo(int egoNum) {
        boolean oKay = true;
        if (egoNum < 0 || chart.loading) {
            return true;
        }
        try {
            storeInfo();
        } catch (Exception exc) {
            //  Do nothing. This is a "safety" storage.
        }
        
        Individual ind = Context.current.individualCensus.get(egoNum);
        if (ind.deleted) {
            MyResBundle rb = Library.messages;
            String msg = rb.getString("noDelEgo");
            JOptionPane.showMessageDialog(this, msg,
                    rb.getString("invalidSelection"), JOptionPane.ERROR_MESSAGE);
            personPanel1.resetEgoBox(currentEgo);
            return true;
        }
        if (ktm.getRow(currentEgo) != null) {
            // Remove nodes from people in old Ego's row.
            Iterator altIter = ktm.getRow(currentEgo).keySet().iterator();
            while (altIter.hasNext()) {
                Integer alter = (Integer) altIter.next();
                ind = Context.current.individualCensus.get(alter);
                ind.node = null;
            }
        }
        currentEgo = egoNum;
        // Save pre-existing nodes of people in new Ego's Row & post them.
        TreeMap oldRow = ktm.getRow(currentEgo);
        TreeMap<Integer, Node> oldRowCopy = null;
        if (oldRow != null) {
            oldRowCopy = new TreeMap<Integer, Node>();
            Iterator nodeIter = oldRow.entrySet().iterator();
            while (nodeIter.hasNext()) {
                Map.Entry entry = (Map.Entry) nodeIter.next();
                Integer alter = (Integer) entry.getKey();
                Node n = (Node) entry.getValue();
                oldRowCopy.put(alter, n);
            }
        }
        //  Must build NEW nodes for each person who is connected to currentEgo
        ChartPanel.doIndexes = true;
        Individual ego = Context.current.individualCensus.get(egoNum);
        ego.node = Node.makeSelfNode(Context.current.distinctAdrTerms);
        if (oldRow == null) {
            ktm.addNode(egoNum, egoNum, ego.node);
        }
        //  2.2  NEW CODE  Uses a breadth-first queue (bfq)
        KSQ bfq = new KSQ();
        bfq.enQ(ego);
        TreeMap newRow = ktm.getRow(currentEgo);
        propagateNodes(bfq, newRow, null);

        // 2.3  insert old nodes with kin terms into new ktm row
        if (oldRowCopy != null) {
            newRow = ktm.getRow(currentEgo);
            oKay = copyNodes(oldRowCopy, newRow);
        }
        personPanel1.resetEgoBox(currentEgo);
        if (!hideEgoChange) {
            infoPerson = ego;
            showInfo(infoPerson);
            chart.whichFolk = currentEgo;
            chart.whichLink = -1;
            chart.whichKnot = -1;
        }
        chart.repaint();
        return oKay;
    }

    public boolean copyNodes(TreeMap oldRow, TreeMap newRow) {
        Iterator nodeIter = oldRow.entrySet().iterator();
        while (nodeIter.hasNext()) {
            Map.Entry entry = (Map.Entry) nodeIter.next();
            Integer alterInt = (Integer) entry.getKey();
            Node oldNode = (Node) entry.getValue();
            Node newNode = (Node) newRow.get(alterInt);
            if (newNode != null && newNode.pcString.equals(oldNode.pcString)) {
                if (alterInt == currentEgo) {
                    oldNode = oldNode.clone();
                }
                newRow.put(alterInt, oldNode);
                Individual al = Context.current.individualCensus.get(alterInt);
                al.node = oldNode;
            }
        }
        return true;
    }
    
    
    public static void propagateNodes(KSQ bfq, TreeMap newRow, Individual target) {
        ArrayList<String> kinTypeOrder = Context.current.kinTypeOrder;
        TreeMap<String, String> priorities = Context.current.kinTypePriorityTMap;
        Iterator kinTypeIter = kinTypeOrder.iterator();
        try {
            while (!bfq.isEmpty()) {
                Individual currInd = (Individual) bfq.deQ(), nextInd;
                while (kinTypeIter.hasNext()) {
                    String kt = (String) kinTypeIter.next();
                    PropagationMethod method = linkMethods.get(kt);
                    method.prop(currInd, kt, bfq, newRow, target, priorities);
                    if (kt.startsWith("*")) {
                        String inverse = "*inverse" + kt.substring(1);
                        method.prop(currInd, inverse, bfq, newRow, target, priorities);
                    }
                } //  and finally star links (not adoptions; they're handled by AdoptMethod.prop)
                if (currInd.starLinks != null) {
                    for (Object o : currInd.starLinks) {
                        nextInd = (Individual) o;
                        propagate(currInd, nextInd, "**", bfq, newRow, target, priorities);
                    }
                }
                kinTypeIter = kinTypeOrder.iterator();
            }  //  end of while BFQ is not empty    
        } catch (KSDateParseException ksd) {
            // If ksd is thrown, then target is non-null, and a node has been
            // created for target with a PCString. We're done.
        }
    }  //  end of method propagateNodes

    public static void propagate(Individual currInd, Individual nextInd,
            String kinTyp, KSQ bfq, TreeMap newRow, Individual target,
            TreeMap<String, String> priorities)
            throws KSDateParseException {
        // When target != null, we are building PC strings for CB.createExamples
        if (nextInd == null) {
            return;
        }
        Context ctxt = Context.current;
        Node fromNode = currInd.node,
                toNode = nextInd.node;
        if (toNode != null) {
            // If toNode has a shorter path to Ego than we can provide, quit
            // UNLESS toNode's path contains non-chartable starlinks and our path does not.
            // If the 2 paths are equal length, quit if the new path is not 
            // higher priority.
            int toPathLength = toNode.miniPreds.size(),
                    newPathLength = fromNode.miniPreds.size() + 1;
            boolean newShorter = newPathLength < toPathLength,
                    newHigherPriority = false,
                    newMoreNatural = false;
            if (ctxt.hasNonChartables(toNode.miniPreds) &&
                    ! ctxt.hasNonChartables(fromNode.miniPreds) &&
                    ! ctxt.isNonChartable(kinTyp)) {
                newMoreNatural = true;
            }
            if (newPathLength == toPathLength) {
                ArrayList<String> oldPCs = new ArrayList<String>(),
                        newPCs = new ArrayList<String>();
                if (!toNode.pcString.isEmpty()) {
                    oldPCs.addAll(KinTermDef.explodePCSymbols(toNode.pcString));
                }
                if (!fromNode.pcString.isEmpty()) {
                    newPCs.addAll(KinTermDef.explodePCSymbols(fromNode.pcString));
                }
                newPCs.add(kinTyp);
                newHigherPriority = higherPriority(newPCs, oldPCs, priorities);
            }
            if (!newShorter && !newMoreNatural && !newHigherPriority) {
                return;
            }
            // Remove the kin type pair of the old path from KTI
            String oldKinType = toNode.pcString;
            Integer[] pair = KinTermDef.pluckEgoAndAlter(toNode.miniPreds);
            ctxt.kti.removePair(oldKinType, pair);
        }
        //  Proceed to make or modify toNode
        if (toNode == null) {
            nextInd.node = new Node();
            nextInd.node.indiv = nextInd;
            toNode = nextInd.node;
        }
        // The new path will replace any prior path
        toNode.miniPreds = new ArrayList<Object>(fromNode.miniPreds);
        String miniPred = kinTyp + "(#" + nextInd.serialNmbr
                + ",#" + currInd.serialNmbr + ")";
        toNode.miniPreds.add(miniPred);
        // When target != null, toNode.pcString is the main objective
        toNode.pcString = fromNode.pcString + kinTyp;
        int level = fromNode.getLevel();
        if (kinTyp.equals("Mo") || kinTyp.equals("Fa") || kinTyp.equals("Stmo")
                || kinTyp.equals("Stfa") || ctxt.isInverseAdoptionPred(kinTyp)) {  
            level++;
        }
        if (kinTyp.equals("So") || kinTyp.equals("Da") || kinTyp.equals("Stso")
                || kinTyp.equals("Stda") || ctxt.isAdoptionPred(kinTyp)) {
            level--;
        }
        toNode.setLevel(level);
        bfq.enQ(nextInd);  //  Propagate by putting onto the BFQ
        newRow.put(nextInd.serialNmbr, toNode);
        if (target == nextInd) {  // goal reached. End search.
            throw new KSDateParseException("Found alter.");
        }
        // Insert the new kin type pair into KTI
        String newKinType = toNode.pcString;
        Integer[] pair = KinTermDef.pluckEgoAndAlter(toNode.miniPreds);
        Context.current.kti.addPair(newKinType, pair);
    }  //  end of method propagate

    public static boolean higherPriority(ArrayList<String> newPCS,
            ArrayList<String> oldPCS, TreeMap<String, String> priorities) {
        
        ArrayList<String> newPriorities = getPriorities(newPCS, priorities),
                          oldPriorities = getPriorities(oldPCS, priorities);
        for (int i = 0; i < newPriorities.size(); i++) {
            int comp = newPriorities.get(i).compareTo(oldPriorities.get(i));
            if (comp < 0) {
                return true;
            }
            if (comp > 0) {
                return false;
            }
        }
        return false;
    }
    
    static ArrayList<String> getPriorities(ArrayList<String> aPCS, TreeMap<String, String> priorityMap) {
        // In Data-Gathering mode, all pc strings contain symbols found in the priorityMap
        // In example generation (Library processing), we can have *, **, or + as symbols.
        ArrayList<String> priorities = new ArrayList<String>();
        for (String pcs : aPCS) {
            String pri = priorityMap.get(pcs);
            if (pri == null) {
                if (pcs.startsWith("*inverse")) {
                    pri = priorityMap.get("*" + pcs.substring(8));
                } else if (pri == null && pcs.startsWith("*")) {
                    pri = "M";
                } else if (pri == null && pcs.startsWith("+")) {
                    pri = "C";
                } else if (pri == null) {
                    throw new NullPointerException("No priority found for " + pcs);
                }
            }
            priorities.add(pri);
        }
        return priorities;
    }

    //  SETTERS AND GETTERS
    public PersonPanel getPPanel() {
        return personPanel1;
    }

    public FamilyPanel getFPanel() {
        return familyPanel1;
    }

    int getCurrentEgo() {
        return currentEgo;
    }

    void setCurrentEgo(int newEgo) {
        currentEgo = newEgo;
    }

    void setSnapToGrid(boolean setting) {
        snapToGrid.setSelected(setting);
    }

    void storeInfo()
            throws KSParsingErrorException, JavaSystemException,
            KSBadHornClauseException, KSInternalErrorException,
            KSConstraintInconsistency, KSDateParseException {
        if (infoPerson != null) {
            personPanel1.storeInfo(infoPerson);
        } else if (infoMarriage != null) {
            familyPanel1.storeInfo(infoMarriage);
        }
    }

    public void clearInfo()
            throws KSParsingErrorException, JavaSystemException,
            KSBadHornClauseException, KSInternalErrorException,
            KSConstraintInconsistency, KSDateParseException {
        storeInfo();
        if (infoPerson != null) {
            personPanel1.clearInfo();
            infoPerson = null;
        }
        if (infoMarriage != null) {
            familyPanel1.clearInfo();
            infoMarriage = null;
        }
        repaint();  //  needed?
    }

    /** Set the proper local flags, then pass the call to PersonPanel;
     * make sure that the Individual detail display is showing.
     * 
     * @param ind the person whose info we want to show
     */
    public void showInfo(Individual ind) {
        if (ind == null) {
            return;
        }
        infoPerson = ind;
        infoMarriage = null;
        personPanel1.showInfo(ind);
        if (familyPanel1.isVisible()) {
            familyPanel1.setVisible(false);
            personPanel1.setVisible(true);
            validate();
            repaint();
        }
    }

    /** Set the proper local flags, then pass the call to FamilyPanel;
     * make sure that the Family detail display is showing.
     * 
     * @param fam the family whose info we want to show
     */
    public void showInfo(Family fam) {
        if (fam == null) {
            return;
        }
        infoMarriage = fam;
        infoPerson = null;
        familyPanel1.showInfo(fam);
        if (personPanel1.isVisible()) {
            personPanel1.setVisible(false);
            familyPanel1.setVisible(true);
            validate();
            repaint();
        }
    }

    void applyDef(KinTermDef newDef, DomainTheory dt) {
        chart.recomputingDyads = true;
        DomainTheory.current = dt;
        Context ctxt = Context.current;
        for (Object o : newDef.expandedDefs) {
            ClauseBody cb = (ClauseBody) o;
            ArrayList<Integer[]> candidates = ctxt.kti.getList(cb.pcString);
            for (Integer[] pair : candidates) {
                Dyad dad = new Dyad();
                dad.ego = ctxt.individualCensus.get(pair[0]);
                dad.alter = ctxt.individualCensus.get(pair[1]);
                try {
                    if (dt.fit(cb, dad)) {
                        Node nod = ktm.getCell(dad.ego, dad.alter);
                        String clas = (dt.addressTerms ? "address" : "reference");
                        nod.addTerm(newDef.kinTerm, "primary", clas);
                        dad.kinTerm = newDef.kinTerm;
                        dad.pcString = nod.pcString;
                        dad.pcStringStructural = ClauseBody.structStr(dad.pcString);
                        dad.addrOrRef = (dt.addressTerms ? 1 : 0);
                        dad.level = nod.getLevel();
                        dad.kinTermType = 0;
                        dad.makePath(nod.miniPreds, dad.alter);
                        dt.dyadsDefined.dyAdd(dad);
                        if (infoPerson == dad.alter && currentEgo == dad.ego.serialNmbr) {
                            personPanel1.fillTextField(nod, "primary", dt.addressTerms);
                            personPanel1.dirty = true;
                            chart.dirty = true;
                        }
                    }
                } catch (Exception ex) {
                    // Any exception = failure to fit
                    String msg = Library.messages.getString("trialFitError") + "\n" + ex;
                    MainPane.displayError(msg, 
                            Library.messages.getString("seriousError"), JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        chart.recomputingDyads = false;
        personPanel1.debugDyads();
    }

    public void removeDef(DomainTheory dt, int egoInt, int alterInt, String term) {
        Node nod = ktm.getCell(egoInt, alterInt);
        String pcString = nod.pcString;
        String clas = (dt.addressTerms ? "address" : "reference");
        nod.removeTerm(term, "primary", clas);
        dt.dyadsDefined.removeDyad(dt, egoInt, alterInt, term, pcString);
        Individual alter = Context.current.individualCensus.get(alterInt);
        if (infoPerson == alter && currentEgo == egoInt) {
            personPanel1.fillTextField(nod, "primary", dt.addressTerms);
            personPanel1.dirty = true;
            chart.dirty = true;
        }
    }
    
    int suggsInFocus;

    TreeMap<String, ArrayList<Issue>> pickSuggs() {
        TreeMap<String, ArrayList<Issue>> suggs = null;
        boolean refIssues = (suggestionsRef != null && !suggestionsRef.isEmpty()),
                adrIssues = (suggestionsAdr != null && !suggestionsAdr.isEmpty());
        if (refIssues && adrIssues) {
            MyResBundle rb = Library.messages;
            String title = rb.getString("chooseSugg2View"),
                    msg = rb.getString("whichSuggType");
            int btnTyp = JOptionPane.YES_NO_CANCEL_OPTION,
                    msgTyp = JOptionPane.QUESTION_MESSAGE;
            String btn1 = rb.getString("ref"),
                   btn2 = rb.getString("adr"),
                   btn3 = rb.getString("cancel");
            Object[] btnLabels = { btn1, btn2, btn3 };
            int choice = JOptionPane.showOptionDialog(this, msg, title, btnTyp,
                    msgTyp, null, btnLabels, btnLabels[0]);
            if (choice == JOptionPane.NO_OPTION) {
                suggs = suggestionsAdr;
                suggsInFocus = ADR;
            } else if (choice == JOptionPane.CANCEL_OPTION
                    || choice == JOptionPane.CLOSED_OPTION) {
                return null;
            }
        } else if (refIssues) {  //  only refs
            suggs = suggestionsRef;
            suggsInFocus = REF;
        } else if (adrIssues) {  // only adrs
            suggs = suggestionsAdr;
            suggsInFocus = ADR;
        }
        return suggs;  //  null = "What Suggestions?"
    }

    /** Review all accepted definitions against new data then try to learn a definition 
     * for each kin term that is not yet defined. Finally, analyze the data to
     * identify potential umbrella terms and synonyms.
     * 
     * @param learner a specialized DomainTheory that can do active learning
     * @throws KSInternalErrorException
     * @throws KSParsingErrorException
     * @throws KSBadHornClauseException
     * @throws KSConstraintInconsistency
     * @throws ClassNotFoundException
     * @throws JavaSystemException
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public void doActiveLearning(Learned_DT learner)
            throws KSInternalErrorException, KSParsingErrorException, KSBadHornClauseException, KSConstraintInconsistency,
            ClassNotFoundException, JavaSystemException, FileNotFoundException, IOException {
        //  NOTE:  When simulations have ended and real User is employed, I must assure that the entries
        //	   in DyadsUndefined for erroneous terms (e.g. sistex) are removed from the tree once the
        //	   activity dyads have been corrected.  that is currently done in DyadTMap.summaryString()
        int maxConf = learner.ctxt.ignorableP / 2;
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
                ArrayList<Object> validationIssues = learner.validateNewDyads(kterm, learner.ctxt.maxNoiseP);
                learner.postAnomaliesForUser(kterm, validationIssues);
            }
        }  //  end of 1st loop thru kinTerms in theory
        dyadIter = learner.dyadsUndefined.keySet().iterator();
        int ctr = 0;
        while (dyadIter.hasNext()) {
            String kterm = (String) dyadIter.next();
            if (learner.dyadsUndefined.avgDyadsPerPCStr(kterm) >= minDyadsPerPCStr) {
                learner.learnKinTerm(kterm, learner.ctxt.maxNoiseP, learner.ctxt.ignorableP, maxConf, learner);
                ctr++;
            }
        }  //  end of 2nd loop thru kinTerms in DyadTMap
        if (ctr == 0) {
            String msg = Library.messages.getString("2FewDyads");
            throw new KSInternalErrorException(msg);
        }
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
                learner.detectSynonymsAndUmbrellas(kterm, pos, neg, learner.ctxt.maxNoiseP);
            }  //  end of loop thru definedTerm dyads
        }
        // Now analyze all potentialUmbrellas & synonyms found in this loop
        if (learner.potUmbrellas != null) {
            learner.proposeUmbrellas();
        }
        learner.analyzeSynonymsAndUmbrellas();
        learner.validateSynonymsAndUmbrellas();
        learner.ctxt.simDataGen = false;
    } //  end of method doActiveLearning

    void resetSuggsMenu() {
        actOnSuggsItem.setEnabled(true);
        returnToSuggsItem.setEnabled(false);
    }
    

     class RecentActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("n 0")) {
                File f = Library.recentFiles[0];
                if (f != null) {
                    chart.saveFile = f;
                    chart.failureCount = 0;
                    chart.recursionCount = 0;
                    chart.loadSILKFile();
                }
            } else if (e.getActionCommand().equals("n 1")) {
                File f = Library.recentFiles[1];
                if (f != null) {
                    chart.saveFile = f;
                    chart.failureCount = 0;
                    chart.recursionCount = 0;
                    chart.loadSILKFile();
                }
            } else if (e.getActionCommand().equals("n 2")) {
                File f = Library.recentFiles[2];
                if (f != null) {
                    chart.saveFile = f;
                    chart.failureCount = 0;
                    chart.recursionCount = 0;
                    chart.loadSILKFile();
                }
            } else if (e.getActionCommand().equals("n 3")) {
                File f = Library.recentFiles[3];
                if (f != null) {
                    chart.saveFile = f;
                    chart.failureCount = 0;
                    chart.recursionCount = 0;
                    chart.loadSILKFile();
                }
            } else if (e.getActionCommand().equals("n 4")) {
                File f = Library.recentFiles[4];
                if (f != null) {
                    chart.saveFile = f;
                    chart.failureCount = 0;
                    chart.recursionCount = 0;
                    chart.loadSILKFile();
                }
            }
        }
    }

    static abstract class PropagationMethod {

        abstract void prop(Individual currInd, String kinTyp, KSQ bfq,
                TreeMap newRow, Individual target, TreeMap<String, String> priorities)
                throws KSDateParseException;
    }

    static class ParentMethod extends PropagationMethod {

        void prop(Individual currInd, String kinTyp, KSQ bfq,
                TreeMap newRow, Individual target, TreeMap<String, String> priorities)
                throws KSDateParseException {

            if (currInd.birthFamily == null) {
                return;
            }
            Individual nextInd;
            if (kinTyp.equals("Fa") && currInd.birthFamily.husband != null
                    && currInd.birthFamily.husband.gender.equals("M")) {  //  not neuter
                nextInd = currInd.birthFamily.husband;
                propagate(currInd, nextInd, "Fa", bfq, newRow, target, priorities);
            } else if (kinTyp.equals("Mo") && currInd.birthFamily.wife != null
                    && currInd.birthFamily.wife.gender.equals("F")) {
                nextInd = currInd.birthFamily.wife;
                propagate(currInd, nextInd, "Mo", bfq, newRow, target, priorities);
            } else if (kinTyp.equals("P")) {
                nextInd = currInd.birthFamily.wife;
                if (nextInd != null && nextInd.gender.equals("?")) {
                    propagate(currInd, nextInd, "P", bfq, newRow, target, priorities);
                }
                nextInd = currInd.birthFamily.husband;
                if (nextInd != null && nextInd.gender.equals("?")) {
                    propagate(currInd, nextInd, "P", bfq, newRow, target, priorities);
                }
            }
        } // end of prop method
    } // end of inner class ParentMethod

    static class ChildMethod extends PropagationMethod {

        void prop(Individual currInd, String kinTyp, KSQ bfq,
                TreeMap newRow, Individual target, TreeMap<String, String> priorities)
                throws KSDateParseException {

            for (Object o : currInd.marriages) {
                Family fam = (Family) o;
                for (Object child : fam.children) {
                    Individual nextInd = (Individual) child;
                    if ((kinTyp.equals("So") && nextInd.gender.equals("M"))
                            || (kinTyp.equals("Da") && nextInd.gender.equals("F"))
                            || (kinTyp.equals("C") && nextInd.gender.equals("?"))) {
                        propagate(currInd, nextInd, kinTyp, bfq, newRow, target, priorities);
                    }
                }
            }
        } // end of prop method
    } // end of inner class ChildMethod
    
    static class AdoptMethod extends PropagationMethod {

        void prop(Individual currInd, String kinTyp, KSQ bfq,
                TreeMap newRow, Individual target, TreeMap<String, String> priorities)
                throws KSDateParseException {
            // When target != null, we are building PC strings for CB.createExamples
            if (kinTyp.startsWith("*inverse")) {  // propagate to adoptive parents
                String origKinTyp = "*" + kinTyp.substring(8);
                if (Context.current.inverseSpecialRelationships == null
                        || Context.current.inverseSpecialRelationships.get(currInd) == null) {
                    return;
                }
                ArrayList<Individual> ir = Context.current.inverseSpecialRelationships.get(currInd).get(origKinTyp);
                if (ir != null) {                    
                    for (Individual next : ir) {
                        propagate(currInd, next, kinTyp, bfq, newRow, target, priorities);
                    } //  end of propagation to adoptive parents
                }  
            } else {  // propagate to adopted children
                if (Context.current.specialRelationships == null
                        || Context.current.specialRelationships.isEmpty()) {
                    return;
                }
                Individual nextInd;
                Iterator udpIter = currInd.userDefinedProperties.values().iterator();
                while (udpIter.hasNext()) {
                    UserDefinedProperty udp = (UserDefinedProperty) udpIter.next();
                    if (udp.starName.equals(kinTyp) && !udp.value.isEmpty()) {
                        for (Object o : udp.value) {
                            nextInd = (Individual) o;
                            propagate(currInd, nextInd, kinTyp, bfq, newRow, target, priorities);
                        }
                    }
                }  //  end of propagation to adopted children
            }

        } // end of prop method
    } // end of inner class AdoptMethod

    static class SpouseMethod extends PropagationMethod {

        void prop(Individual currInd, String kinTyp, KSQ bfq,
                TreeMap newRow, Individual target, TreeMap<String, String> priorities)
                throws KSDateParseException {

            if ((currInd.gender.equals("M") && kinTyp.equals("Hu"))
                    || (currInd.gender.equals("F") && kinTyp.equals("Wi"))) {
                return;
            }
            for (Object o : currInd.marriages) {
                Family fam = (Family) o;
                if (kinTyp.equals("Wi")) {
                    Individual nextInd = fam.wife;
                    if (nextInd != null && nextInd.gender.equals("F")) {
                        propagate(currInd, nextInd, kinTyp, bfq, newRow, target, priorities);
                    }
                } else if (kinTyp.equals("Hu")) {
                    Individual nextInd = fam.husband;
                    if (nextInd != null && nextInd.gender.equals("M")) {
                        propagate(currInd, nextInd, kinTyp, bfq, newRow, target, priorities);
                    }
                } else if (kinTyp.equals("S")) {
                    Individual nextInd = (currInd == fam.husband ? fam.wife : fam.husband);
                    if (nextInd != null && nextInd.gender.equals("?")) {
                        propagate(currInd, nextInd, kinTyp, bfq, newRow, target, priorities);
                    }
                }
            }
        } // end of prop method
    } // end of inner class SpouseMethod

    static class SiblingMethod extends PropagationMethod {

        void prop(Individual currInd, String kinTyp, KSQ bfq,
                TreeMap newRow, Individual target, TreeMap<String, String> priorities)
                throws KSDateParseException {

            if (currInd.birthFamily == null) {
                return;
            }
            Family bFam = (Family) currInd.birthFamily;
            Individual nextInd;
            for (Object o : bFam.children) {
                if (o != currInd) {
                    nextInd = (Individual) o;
                    if ((kinTyp.equals("Bro") && nextInd.gender.equals("M"))
                            || (kinTyp.equals("Sis") && nextInd.gender.equals("F"))
                            || (kinTyp.equals("Sib") && nextInd.gender.equals("?"))) {
                        propagate(currInd, nextInd, kinTyp, bfq, newRow, target, priorities);
                    }
                }
            }
        } // end of prop method
    } // end of inner class SiblingMethod

    static class HalfSiblingMethod extends PropagationMethod {

        void prop(Individual currInd, String kinTyp, KSQ bfq,
                TreeMap newRow, Individual target, TreeMap<String, String> priorities)
                throws KSDateParseException {

            if (currInd.birthFamily == null) {
                return;
            }
            Family bFam = (Family) currInd.birthFamily;
            Individual nextInd;
            ArrayList<Individual> parents = new ArrayList<Individual>();
            if (bFam.husband != null) {
                parents.add(bFam.husband);
            }
            if (bFam.wife != null) {
                parents.add(bFam.wife);
            }
            for (Individual naturalParent : parents) {
                for (Object o : naturalParent.marriages) {
                    Family mar = (Family) o;
                    if (mar != bFam) {
                        for (Object ch : mar.children) {
                            nextInd = (Individual) ch;
                            if ((nextInd.gender.equals("M") && kinTyp.equals("Hbro"))
                                    || (nextInd.gender.equals("F") && kinTyp.equals("Hsis"))) {
                                propagate(currInd, nextInd, kinTyp, bfq, newRow, target, priorities);
                            }
                        }
                    }
                }
            }  //  end of loop thru natural parents & their other marriages
        } // end of prop method
    } // end of inner class HalfSiblingMethod

    static class StepParentMethod extends PropagationMethod {

        void prop(Individual currInd, String kinTyp, KSQ bfq,
                TreeMap newRow, Individual target, TreeMap<String, String> priorities)
                throws KSDateParseException {

            if (currInd.birthFamily == null) {
                return;
            }
            Family bFam = (Family) currInd.birthFamily;
            Individual nextInd;
            ArrayList<Individual> parents = new ArrayList<Individual>();
            if (bFam.husband != null) {
                parents.add(bFam.husband);
            }
            if (bFam.wife != null) {
                parents.add(bFam.wife);
            }
            for (Individual naturalParent : parents) {
                for (Object o : naturalParent.marriages) {
                    Family mar = (Family) o;
                    if (mar != bFam) {
                        Individual stepParent = (naturalParent == mar.wife ? mar.husband : mar.wife);
                        if (stepParent != null) {
                            if ((stepParent.gender.equals("M") && kinTyp.equals("Stfa"))
                                    || (stepParent.gender.equals("F") && kinTyp.equals("Stmo"))) {
                                propagate(currInd, stepParent, kinTyp, bfq, newRow, target, priorities);
                            }
                        }
                    }
                }
            }  //  end of loop thru natural parents & their other marriages
        } // end of prop method
    } // end of inner class StepParentMethod

    static class StepSiblingMethod extends PropagationMethod {

        void prop(Individual currInd, String kinTyp, KSQ bfq,
                TreeMap newRow, Individual target, TreeMap<String, String> priorities)
                throws KSDateParseException {

            if (currInd.birthFamily == null) {
                return;
            }
            Family bFam = (Family) currInd.birthFamily;
            Individual nextInd;
            ArrayList<Individual> parents = new ArrayList<Individual>();
            if (bFam.husband != null) {
                parents.add(bFam.husband);
            }
            if (bFam.wife != null) {
                parents.add(bFam.wife);
            }
            for (Individual naturalParent : parents) {
                for (Object o : naturalParent.marriages) {
                    Family mar = (Family) o;
                    if (mar != bFam) {
                        Individual stepParent = (naturalParent == mar.wife ? mar.husband : mar.wife);
                        if (stepParent != null) {
                            for (Object m : stepParent.marriages) {
                                Family stepFam = (Family) m;
                                if (stepFam != mar && stepFam != bFam) {
                                    for (Object k : stepFam.children) {
                                        nextInd = (Individual) k;
                                        if ((nextInd.gender.equals("M") && kinTyp.equals("Stbro"))
                                                || (nextInd.gender.equals("F") && kinTyp.equals("Stsis"))) {
                                            propagate(currInd, nextInd, kinTyp, bfq, newRow, target, priorities);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }  //  end of loop thru natural parents & their other marriages          
        } // end of prop method
    } // end of inner class StepSiblingMethod

    static class StepChildMethod extends PropagationMethod {

        void prop(Individual currInd, String kinTyp, KSQ bfq,
                TreeMap newRow, Individual target, TreeMap<String, String> priorities)
                throws KSDateParseException {

            for (Object o : currInd.marriages) {
                Family fam = (Family) o;
                Individual currIndSpouse = (currInd == fam.husband ? fam.wife : fam.husband);
                if (currIndSpouse != null) {
                    for (Object f : currIndSpouse.marriages) {
                        Family spouseFam = (Family) f;
                        if (spouseFam != fam) {
                            for (Object kid : spouseFam.children) {
                                Individual nextInd = (Individual) kid;
                                if ((nextInd.gender.equals("M") && kinTyp.equals("Stso"))
                                        || (nextInd.gender.equals("F") && kinTyp.equals("Stda"))) {
                                    propagate(currInd, nextInd, kinTyp, bfq, newRow, target, priorities);
                                }
                            }
                        }
                    }
                }
            }
        } // end of prop method
    } // end of inner class StepChildMethod
    
    static class FocusListener implements WindowFocusListener {
        
        public void windowGainedFocus(WindowEvent we) {
            Context.current = editingCtxt;
        }
        
        public void windowLostFocus(WindowEvent we) {
            editingCtxt = Context.current;
        }
    }
    
    // Variables declaration
    public JMenuItem actOnSuggsItem;
    private JMenuItem adminSILKinItem;
    public ChartPanel chart;
    private JPanel chartHolderPanel;
    private JPanel chartHolderHorizontal;
            JComboBox chartComboBox;
    private DefaultComboBoxModel chartComboModel;
            JScrollPane chartScrollPane;
    private JMenuItem chooseLanguageItem;
    private JMenu contextMenu;
    private JMenuItem copyItem;
    private JMenuItem cutItem;
    public JCheckBoxMenuItem displayGEDCOM;
    private JMenuItem deleteAllItem;
    private JMenuItem deleteCurrentChartItem;
    private JCheckBoxMenuItem distinctAdrItem;
    private JCheckBoxMenuItem editableItem;
    private JMenuItem editContextItem;
    private JMenu editMenu;
    private JMenuItem editChartDescriptionItem;
    private JMenuItem editPrefsItem;
    private JMenu gedcomSubMenu;
    private JMenuItem exportGEDCOMItem;
    private JMenuItem exportKAESItem;
    private JMenuItem exportDefinitionsItem;
    private FamilyPanel familyPanel1;
    private JMenu fileMenu;
    private JMenu fileNewMenu;
    private JRadioButtonMenuItem firstNameBtn;
    private JMenuItem getSuggestionsItem;
    private JMenu helpMenu;
    private JMenuItem importGEDCOMitem;
    private JRadioButtonMenuItem initialsBtn;
    private JMenuBar jMenuBar1;
    private JPopupMenu.Separator jSeparator1;
    private JPopupMenu.Separator jSeparator2;
    private JPopupMenu.Separator jSeparator3;
    private JPopupMenu.Separator jSeparator4;
    private JPopupMenu.Separator jSeparator5;
    private ButtonGroup kinTermButtonGroup;
    JRadioButtonMenuItem kinTmAdrBtn;
    private JRadioButtonMenuItem kinTmRefBtn;
    private JMenu labelMenu;
    JRadioButtonMenuItem ltrAdrBtn;
    private JRadioButtonMenuItem ltrRefBtn;
    private JRadioButtonMenuItem lastNameBtn;
    private JMenuItem loadItem;
    private JMenu loadRecent;
    private ButtonGroup nameButtonGroup;
    private JMenuItem newContextItem;
    private JMenuItem newLiBrowserItem;
    private JRadioButtonMenuItem noKinTermBtn;
    private JRadioButtonMenuItem noNameBtn;
    private JMenuItem pageSetupItem;
    private JMenuItem pasteItem;
    private PersonPanel personPanel1;
    private JMenu printMenu;
    private JMenuItem printVisible;
    private JMenuItem printCurrentChart;
    private JMenuItem printAllCharts;
    private JMenuItem quitItem;
    public JMenuItem returnToSuggsItem;
    private JMenuItem saveAsItem;
    private JMenuItem saveItem;
    private JCheckBoxMenuItem snapToGrid;
    private JRadioButtonMenuItem wholeNameBtn;
    // End of variables declaration
}
