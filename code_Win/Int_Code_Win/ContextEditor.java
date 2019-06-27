import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;

/**
 * The ContextEditor is a window that displays some of a {@link Context}'s
 * fields for user editing. It is an extension of KSJInternalFrame because I
 * want this to appear in the View menu. <p> The ContextEditor can only display
 * one context - the current
 * <code>Library.contextUnderConstruction</code>. Often there will be only one;
 * if there are more, the User must choose one to be his current focus, then
 * edit it.</p>
 *
 * @author	Gary Morris, Northern Virginia Community College
 *                  garymorris2245@verizon.net
 */
public class ContextEditor extends KSJInternalFrame {

    public Context ctxt;
    public JTextField name, folder;
    public JComboBox UDPick, indPick, famPick;
    public boolean rebuilding = false;
    public JPanel populationBox = new JPanel();
    public ArrayList<Object> peopleList;
    public JLabel indivLabel, famLabel;
    public ArrayList<Family> famList;
    public JTextArea comments;
    public CEListener listener;
    static MyResBundle se = Library.screenElements;

    public ContextEditor(Context cntxt) {
        super(se.getString("editUserContext") + " " + localFileName(cntxt));
        ctxt = cntxt;
        windowNum = ctxt.languageName + " " + se.getString("contextEditor");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        listener = new CEListener(this);

        JPanel nameBox = new JPanel();
        nameBox.setLayout(new BoxLayout(nameBox, BoxLayout.LINE_AXIS));
        name = new JTextField(ctxt.languageName, 28);
        name.setMaximumSize(new Dimension(225, 22));
        name.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                nameFocusLost(evt);
            }
        });

        JLabel nameLabel = new JLabel(se.getString("languageName") + " ");
        nameBox.add(nameLabel);
        nameBox.add(name);

        JPanel folderBox = new JPanel();
        folderBox.setLayout(new BoxLayout(folderBox, BoxLayout.LINE_AXIS));
        folder = new JTextField(ctxt.editDirectory);
        folder.setMaximumSize(new Dimension(225, 22));
//        folder.addActionListener(listener);
//        folder.setActionCommand("folder edit");
        folder.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusLost(java.awt.event.FocusEvent evt) {
                folderFocusLost(evt);
            }
        });

        JLabel folderLabel = new JLabel(se.getString("sILKFileFolder") + " ");
        folderBox.add(folderLabel);
        folderBox.add(folder);

        JPanel nameFolderBox = new JPanel();
        nameFolderBox.setLayout(new BoxLayout(nameFolderBox, BoxLayout.PAGE_AXIS));
        nameBox.setAlignmentX(0.5f);
        nameFolderBox.add(nameBox);
        nameFolderBox.add(Box.createRigidArea(new Dimension(0, 4)));
        nameFolderBox.add(folderBox);
        nameFolderBox.setAlignmentX(0.5f);

        buildPopulationBox();

        JPanel btnBoxUDPs = new JPanel(), subBoxUDP = new JPanel();
        btnBoxUDPs.setLayout(new BoxLayout(btnBoxUDPs, BoxLayout.PAGE_AXIS));
        subBoxUDP.setLayout(new BoxLayout(subBoxUDP, BoxLayout.LINE_AXIS));
        int numUDPs = 0;
        if (ctxt.userDefinedProperties != null) {
            numUDPs = ctxt.userDefinedProperties.size();
        }
        String plur = se.getString("userDefinedProperties");
        if (numUDPs == 1) {
            plur = se.getString("userDefinedProperty");
        }
        JLabel udpLabel = new JLabel(" " + numUDPs + " " + plur);
        subBoxUDP.add(udpLabel);
        JButton addUDP = new JButton(se.getString("addUDP"));
        addUDP.setActionCommand("add UDP");
        addUDP.addActionListener(listener);
        subBoxUDP.add(addUDP);
        btnBoxUDPs.add(subBoxUDP);
        if (numUDPs > 0) {
            Dimension sizer = new Dimension(250, 50);
            String[] udpMenu = genUDPMenu();
            UDPick = new JComboBox(udpMenu);
            UDPick.addActionListener(listener);
            UDPick.setActionCommand("view/edit UDP");
            UDPick.setMinimumSize(sizer);
            UDPick.setMaximumSize(sizer);
            UDPick.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.blue),
                    se.getString("viewEditUDPs")));
            btnBoxUDPs.add(UDPick);
        }  //  end of if-any-UDPs-exist

        JPanel domThs = new JPanel();
        domThs.setLayout(new BoxLayout(domThs, BoxLayout.PAGE_AXIS));
        domThs.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.blue),
                se.getString("ksDomainTheories")));
        domThs.setAlignmentX(0.5f);
        JPanel dtRefBtnBox = new JPanel();
        dtRefBtnBox.setLayout(new BoxLayout(dtRefBtnBox, BoxLayout.LINE_AXIS));
        dtRefBtnBox.setAlignmentX(0.0f);
        JLabel dtRefLabel = new JLabel("     " + se.getString("termsOfReference") + "   ");
        dtRefBtnBox.add(dtRefLabel);
        if (ctxt.domTheoryRefExists()) {
            JButton dtRefEdit = new JButton(se.getString("editTheory"));
            dtRefEdit.setActionCommand("edit dtRef");
            dtRefEdit.addActionListener(listener);
            dtRefBtnBox.add(dtRefEdit);
        } //  end of if-dt-exists
        else {  //  if does not exist
            JLabel dtRefNone = new JLabel(se.getString("<none>"));
            dtRefBtnBox.add(dtRefNone);
        }  //  end of does-not-exist
        domThs.add(dtRefBtnBox);

        JPanel dtAddrBtnBox = new JPanel();
        dtAddrBtnBox.setLayout(new BoxLayout(dtAddrBtnBox, BoxLayout.LINE_AXIS));
        dtAddrBtnBox.setAlignmentX(0.0f);
        JLabel dtAddrLabel = new JLabel("     " + se.getString("termsOf") + " " 
                + se.getString("address") + "   ");
        dtAddrBtnBox.add(dtAddrLabel);
        if (ctxt.domTheoryAdrExists()) {
            JButton dtAddrEdit = new JButton(se.getString("editTheory"));
            dtAddrEdit.setActionCommand("edit dtAddr");
            dtAddrEdit.addActionListener(listener);
            dtAddrBtnBox.add(dtAddrEdit);
        } //  end of if-dt-exists
        else {  //  if does not exist
            JLabel dtAddrNone = new JLabel(se.getString("<none>"));
            dtAddrBtnBox.add(dtAddrNone);
        }  //  end of does-not-exist
        domThs.add(dtAddrBtnBox);
        // End of the left hand portion
        // Right hand portion follows. it is narrower.

        JPanel polyBox = new JPanel();
        polyBox.setLayout(new BoxLayout(polyBox, BoxLayout.PAGE_AXIS));
        polyBox.setAlignmentX(0.5f);
        JLabel polyLabelA = new JLabel(se.getString("polygamy"));
        JLabel polyLabelB = new JLabel(se.getString("permitted"));
        JRadioButton yesPoly = new JRadioButton(se.getString("yes"));
        yesPoly.setActionCommand("polygamy yes");
        yesPoly.addActionListener(listener);
        JRadioButton noPoly = new JRadioButton(se.getString("no"));
        noPoly.setActionCommand("polygamy no");
        noPoly.addActionListener(listener);
        if (cntxt.polygamyPermit) {
            yesPoly.setSelected(true);
        } else {
            noPoly.setSelected(true);
        }
        ButtonGroup polyBtns = new ButtonGroup();
        polyBtns.add(yesPoly);
        polyBtns.add(noPoly);
        polyBox.add(polyLabelA);
        polyBox.add(polyLabelB);
        polyBox.add(yesPoly);
        polyBox.add(noPoly);

        JPanel matrixBox = new JPanel();
        matrixBox.setLayout(new BoxLayout(matrixBox, BoxLayout.PAGE_AXIS));
        matrixBox.setAlignmentX(0.5f);
        JLabel matrixLabelA = new JLabel(se.getString("kinTermMatrix"));
        JLabel matrixLabelC = new JLabel(ctxt.indSerNumGen + " " 
                + se.getString("rows"));
        JLabel matrixLabelD = new JLabel(ctxt.ktm.numberOfKinTerms() + " " 
                + se.getString("terms"));
        matrixLabelA.setAlignmentX(0.5f);
        matrixLabelC.setAlignmentX(0.5f);
        matrixLabelD.setAlignmentX(0.5f);
        JButton matrixEditBtn = new JButton(se.getString("exportMatrix"));
        matrixEditBtn.setEnabled(true);
        matrixEditBtn.setActionCommand("export matrix");
        matrixEditBtn.addActionListener(listener);
        matrixEditBtn.setAlignmentX(0.5f);
        matrixBox.add(matrixLabelA);
        matrixBox.add(matrixLabelC);
        matrixBox.add(matrixLabelD);
        matrixBox.add(matrixEditBtn);


        JPanel distinctBox = new JPanel();
        distinctBox.setLayout(new BoxLayout(distinctBox, BoxLayout.PAGE_AXIS));
        distinctBox.setAlignmentX(0.5f);
        JLabel distinctLabelA = new JLabel(se.getString("distinctTerms"));
        JLabel distinctLabelB = new JLabel(se.getString("ofAddress"));
        distinctLabelA.setAlignmentX(0.5f);
        distinctLabelB.setAlignmentX(0.5f);
        JRadioButton yesDistinct = new JRadioButton(se.getString("yes"));
        yesDistinct.setActionCommand("distinct yes");
        yesDistinct.addActionListener(listener);
        JRadioButton noDistinct = new JRadioButton(se.getString("no"));
        noDistinct.setActionCommand("distinct no");
        noDistinct.addActionListener(listener);
        yesDistinct.setAlignmentX(0.5f);
        noDistinct.setAlignmentX(0.5f);
        if (ctxt.distinctAdrTerms) {
            yesDistinct.setSelected(true);
        } else {
            noDistinct.setSelected(true);
        }
        ButtonGroup distinctBtns = new ButtonGroup();
        distinctBtns.add(yesDistinct);
        distinctBtns.add(noDistinct);
        distinctBox.add(distinctLabelA);
        distinctBox.add(distinctLabelB);
        distinctBox.add(yesDistinct);
        distinctBox.add(noDistinct);


        /*
         * NOTE: It should be possible to put all these elements directly into
         * the ContentPane. But that doesn't work; the layout is truly ugly and
         * stuff gets stacked on top of other stuff. What works is to put
         * everything into a JPanel with BoxLayout. Then put the JPanel into
         * ContentPane.
         */
        JPanel leftCol = new JPanel();
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.PAGE_AXIS));
        leftCol.add(nameFolderBox);
        leftCol.add(Box.createRigidArea(new Dimension(0, 4)));
        leftCol.add(populationBox);
        leftCol.add(Box.createRigidArea(new Dimension(0, 8)));
        leftCol.add(btnBoxUDPs);
        leftCol.add(Box.createRigidArea(new Dimension(0, 8)));
        leftCol.add(domThs);
        leftCol.add(new JLabel(" "));

        JPanel rightCol = new JPanel();
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.PAGE_AXIS));
        rightCol.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.blue),
                se.getString("options")));
        rightCol.add(Box.createRigidArea(new Dimension(0, 20)));
        rightCol.add(polyBox);
        rightCol.add(Box.createRigidArea(new Dimension(0, 20)));
        rightCol.add(matrixBox);
        int high = (numUDPs > 0 ? 120 : 20);
        rightCol.add(Box.createRigidArea(new Dimension(0, high)));
        rightCol.add(distinctBox);

        JPanel commentBox = new JPanel();
        commentBox.setLayout(new BoxLayout(commentBox, BoxLayout.PAGE_AXIS));
        commentBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.blue), se.getString("notesOnCulture")));
        JScrollPane commentsPane = new JScrollPane();
        comments = new JTextArea();
        comments.setColumns(20);
        comments.setEditable(true);
        comments.setRows(3);
        comments.setText(PersonPanel.restoreLineBreaks(ctxt.comments));
        comments.getDocument().addDocumentListener(new CommentListener());
        commentsPane.setViewportView(comments);
        commentBox.add(commentsPane);

        JPanel guts = new JPanel();  // Holds the guts of this window
        guts.setLayout(new BoxLayout(guts, BoxLayout.LINE_AXIS));
        guts.add(leftCol);
        guts.add(Box.createRigidArea(new Dimension(10, 4)));
        guts.add(rightCol);

        JPanel wholeThing = new JPanel();
        wholeThing.setLayout(new BoxLayout(wholeThing, BoxLayout.PAGE_AXIS));
        wholeThing.add(Box.createRigidArea(new Dimension(0, 4)));
        wholeThing.add(guts);
        wholeThing.add(Box.createRigidArea(new Dimension(0, 8)));
        wholeThing.add(commentBox);
        wholeThing.add(Box.createRigidArea(new Dimension(0, 4)));

        getContentPane().add(wholeThing);

        addInternalFrameListener(this);
        setSize(700, 620);
        setVisible(true);
    }  //  end of ContextEditor constructor
    
    public static String localFileName(Context cntxt) {
        String fileName = cntxt.languageName;
        if (SIL_Edit.edWin != null && SIL_Edit.edWin.chart != null
                && SIL_Edit.edWin.chart.saveFile != null) {
            fileName = SIL_Edit.edWin.chart.saveFile.getName(); 
        }
        return fileName;
    }

    public void buildPopulationBox() {
        rebuilding = true;
        populationBox.removeAll();
        peopleList = new ArrayList<Object>();
        famList = new ArrayList<Family>();
        peopleList.addAll(ctxt.individualCensus);
        String plur = (peopleList.size() == 1 ? 
                se.getString("individual") : 
                se.getString("individuals"));
        populationBox.setLayout(new BoxLayout(populationBox, BoxLayout.PAGE_AXIS));
        populationBox.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.blue),
                se.getString("currentPopulation")));
        populationBox.setAlignmentX(0.5f);
        populationBox.add(Box.createRigidArea(new Dimension(8, 0)));
        indivLabel = new JLabel(se.getString("contains") + " " 
                + peopleList.size() + " " + plur);
        indivLabel.setAlignmentX(0.5f);
        populationBox.add(indivLabel);
        if (peopleList.size() > 0) {
            JPanel indivBtnBox = new JPanel();
            indivBtnBox.setLayout(new BoxLayout(indivBtnBox, BoxLayout.LINE_AXIS));
            Dimension sizer2 = new Dimension(350, 50);
            String[] indMenu = genIndMenu(peopleList);
            indPick = new JComboBox(indMenu);
            indPick.addActionListener(listener);
            indPick.setActionCommand("view/edit person");
            indPick.setMinimumSize(sizer2);
            indPick.setMaximumSize(sizer2);
            indPick.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.blue),
                    se.getString("view/EditPerson")));
            indivBtnBox.add(indPick);
            populationBox.add(indivBtnBox);
        }  //  end of if-any-people-exist

        famList.addAll(ctxt.familyCensus);   //  end of filtering deleted records
        plur = (famList.size() == 1 ? se.getString("family") : 
                se.getString("families"));
        famLabel = new JLabel(se.getString("contains") + " "  + famList.size() 
                + " " + plur);
        famLabel.setAlignmentX(0.5f);
        populationBox.add(Box.createRigidArea(new Dimension(0, 4)));
        populationBox.add(famLabel);
        if (famList.size() > 0) {
            JPanel famBtnBox = new JPanel();
            famBtnBox.setLayout(new BoxLayout(famBtnBox, BoxLayout.LINE_AXIS));
            Dimension sizer2 = new Dimension(350, 50);
            String[] famMenu = genFamMenu(famList);
            famPick = new JComboBox(famMenu);
            famPick.addActionListener(listener);
            famPick.setActionCommand("view/edit family");
            famPick.setMinimumSize(sizer2);
            famPick.setMaximumSize(sizer2);
            famPick.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.blue),
                    se.getString("view/EditFamily")));
            famBtnBox.add(famPick);
            populationBox.add(famBtnBox);
        } //  end of if-any-families-exist
        rebuilding = false;
    }  //  end of method buildPopulationBox

    public static String[] genIndMenu(ArrayList<Object> source) {
        String[] menu = new String[source.size()];
        Iterator indIter = source.iterator();
        Individual ind;
        String item;
        int ndex = 0;
        while (indIter.hasNext()) {
            ind = (Individual) indIter.next();
            item = se.getString("noName");
            if ((ind.name != null) && (ind.name.length() > 0)) {
                item = ind.homeChart + ": " + ind.name;
            }
            item += " <" + ind.serialNmbr + ">";
            if (ind.deleted) {
                item += "     " + se.getString("dELETED");
            }
            menu[ndex++] = item;
        }
        return menu;
    }  //  end of method genIndMenu(source)

    public static String[] genFamMenu(ArrayList<Family> source) {
        String[] menu = new String[source.size()];
        Iterator famIter = source.iterator();
        String dad, mom, item;
        Family fam;
        int ndex = 0;
        while (famIter.hasNext()) {
            fam = (Family) famIter.next();
            dad = se.getString("anonymous");
            mom = se.getString("anonymous");
            if (fam.husband != null) {
                dad = fam.husband.name;
            }
            if (fam.wife != null) {
                mom = fam.wife.name;
            }
            item = dad + " & " + mom + " (" + fam.serialNmbr + ")";
            if (fam.deleted) {
                item += "     " + se.getString("dELETED");
            }
            menu[ndex++] = item;
        }
        return menu;
    }  //  end of method genFamMenu(source)

    public String[] genUDPMenu() {
        String[] menu = new String[ctxt.userDefinedProperties.size()];
        Iterator udpIter = ctxt.userDefinedProperties.keySet().iterator();
        String item;
        int ndex = 0;
        while (udpIter.hasNext()) {
            item = (String) udpIter.next();
            menu[ndex++] = item;
        }
        return menu;
    }  //  end of method genUDPMenu()

    public void nameFocusLost(java.awt.event.FocusEvent evt) {
        ctxt.saveState = true;
        String newName = name.getText(), msg;
        while (!Library.validateFileName(newName, false)) {
            msg = se.getString("theName") + " '" + newName 
                    + "' " + se.getString("violatesNames");
            newName = JOptionPane.showInputDialog(msg);
        }  //  end of harrass-em-until-they-give-a-good-name
        name.setText(newName);
        if (!newName.equals(ctxt.languageName)) { // Made a change
            msg = se.getString("changeContextName") + " " + newName + "?";
            String[] options = {newName, ctxt.languageName};
            int choice = JOptionPane.showOptionDialog(this, msg, 
                    se.getString("confirmChangedLangName"),
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            if (choice == 0) {  //  Change is confirmed
                ctxt.languageName = newName;
                msg = se.getString("normalFileName") + " " + newName + "?";
                options[0] = se.getString("changeFileName");
                options[1] = se.getString("doNotChange");
                choice = JOptionPane.showOptionDialog(this, msg, 
                        se.getString("confirmFileName"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);
                if (choice == 0) {
                    Library.userContextName = newName;
                    if (SIL_Edit.edWin != null && ctxt == Library.contextUnderConstruction) {
                        SIL_Edit.edWin.chart.changeFileName(newName);
                    }
                }
            }  //  end of Change-is-confirmed
        }  //  end of change-was-made       
    }

    public void folderFocusLost(java.awt.event.FocusEvent evt) {
        ctxt.saveState = true;
        ctxt.editDirectory = folder.getText();
    }

    /**
     * This internal class is the ActionListener for a ContextEditor; it
     * contains all the code behind the buttons, combo boxes, etc. in the
     * editor.
     */
    public class CEListener implements ActionListener {

        KSJInternalFrame ed;

        public CEListener(KSJInternalFrame ctxted) {
            ed = ctxted;
        }  //  end of constructor

        public void actionPerformed(ActionEvent e) {
            if (rebuilding) {
                return;
            }
            if (e.getActionCommand().equals("polygamy yes")) {
                ctxt.saveState = true;
                ctxt.polygamyPermit = true;
                SIL_Edit.edWin.chart.dirty = true;
            }
            if (e.getActionCommand().equals("polygamy no")) {
                ctxt.saveState = true;
                ctxt.polygamyPermit = false;
                SIL_Edit.edWin.chart.dirty = true;
            }
            if (e.getActionCommand().equals("distinct yes")) {
                ctxt.saveState = true;
                ctxt.distinctAdrTerms = true;
                SIL_Edit.edWin.setDistinctAdrMenuItemSelected(true);
                SIL_Edit.edWin.distinctAdrItemActionPerformed(null);
                SIL_Edit.edWin.chart.dirty = true;
                MainPane.topPane.setVisible(true);
            }
            if (e.getActionCommand().equals("distinct no")) {
                ctxt.saveState = true;
                ctxt.distinctAdrTerms = false;
                SIL_Edit.edWin.setDistinctAdrMenuItemSelected(false);
                SIL_Edit.edWin.distinctAdrItemActionPerformed(null);
                SIL_Edit.edWin.chart.dirty = true;
                MainPane.topPane.setVisible(true);
            }
            if (e.getActionCommand().equals("export matrix")) {
                //  write a tab delimited file with the contents of
                //  the Kin Term Matrix. For export to a spreadsheet.               
                String msg;
                MyResBundle msgs = Library.messages;
                int population = Context.current.indSerNumGen + Context.current.famSerNumGen;
                if (population < 2) {
                    JOptionPane.showMessageDialog(desktop,
                            msgs.getString("exportingOnly") + " " + population
                            + msgs.getString("exportingOnly2") + " ",
                            msgs.getString("nothingToExport"),
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } //  end of too-small-population
                JFileChooser fc = MainPane.topPane.fc;
                if (fc == null) {
                    fc = new JFileChooser();
                }
                File file;
                fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fc.setDialogTitle(msgs.getString("getKTMLocation"));
                //  Display File-Open dialog and get User's selection
                int returnVal = fc.showSaveDialog(desktop);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    file = fc.getSelectedFile();
                    String fName = file.getName(), fPath = file.getPath();
                    int extLoc = fName.lastIndexOf(".");
                    String ext = (extLoc == -1 ? "" : fName.substring(extLoc));
                    if (!ext.equalsIgnoreCase(".txt")) {
                        msg = msgs.getString("mustEnd.txt");
                        JOptionPane.showMessageDialog(desktop, msg,
                                msgs.getString("fileNameError"),
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try {
                        PrintWriter outFile = new PrintWriter(fPath, "UTF-8");
                        Context.current.exportKTM(outFile, "Reference");
                        if (Context.current.domTheoryAdrExists()) {
                            outFile.println("\n\n");
                            Context.current.exportKTM(outFile, "Address");
                        }
                        outFile.flush();
                        outFile.close();
                    } catch (IOException exc) {
                        msg = msgs.getString("whileExportingKTM") + " " + 
                                MainPane.topPane.prettify(exc.toString())
                                + msgs.getString("rECOMMENDATION7");
                        JOptionPane.showMessageDialog(desktop, msg,
                                msgs.getString("seriousError"),
                                JOptionPane.ERROR_MESSAGE);
                        MainPane.activity.log.append(msg + "\n\n");
                    } catch (Exception exc) {
                        msg = msgs.getString("whileExportingKTM") + " " 
                                + MainPane.topPane.prettify(exc.toString())
                                + msgs.getString("rECOMMENDATION6");
                        JOptionPane.showMessageDialog(desktop, msg,
                                msgs.getString("seriousError"),
                                JOptionPane.ERROR_MESSAGE);
                        MainPane.activity.log.append(msg + "\n\n");
                    }  //  end of catch bodies
                }  //  end of if-APPROVE_OPTION

            }
            if (e.getActionCommand().equals("view/edit person")) {
                ctxt.saveState = true;
                Individual edee = (Individual) peopleList.get(indPick.getSelectedIndex());
                PersonEditor pEd = new PersonEditor(ctxt, ed, 
                        se.getString("view/EditPerson"), edee, "census", 0);
                if (!pEd.dupEditor) {
                    pEd.desktop = desktop;
                    desktop.add(pEd);
                    pEd.miViewMe = menuView.add(pEd.windowNum);
                    pEd.miViewMe.addActionListener(pEd);
                    pEd.menuView = menuView;
                    pEd.show();
                    pEd.moveToFront();
                    try {
                        pEd.setSelected(true);
                    } catch (PropertyVetoException pv) {
                    }
                } else {
                    try {
                        pEd.setClosed(true);
                    } catch (PropertyVetoException pv) {
                    }
                }
            }
            if (e.getActionCommand().equals("view/edit family")) {
                ctxt.saveState = true;
                int serial = famPick.getSelectedIndex();
                Family edee = (Family) famList.get(serial);
                FamilyEditor fEd = new FamilyEditor(ctxt, ed, 
                        se.getString("view/EditFamily"), edee, "census");
                if (!fEd.dupEditor) {
                    fEd.desktop = desktop;
                    fEd.setLocation(350, 100);
                    desktop.add(fEd);
                    fEd.miViewMe = menuView.add(fEd.windowNum);
                    fEd.miViewMe.addActionListener(fEd);
                    fEd.menuView = menuView;
                    fEd.show();
                    fEd.moveToFront();
                    try {
                        fEd.setSelected(true);
                    } catch (PropertyVetoException pv) {
                    }
                } else {
                    try {
                        fEd.setClosed(true);
                    } catch (PropertyVetoException pv) {
                    }
                }
            }
            if (e.getActionCommand().equals("add UDP")) {
                UserDefinedProperty newU = new UserDefinedProperty("*newUDP");
                UDPEditor eddy = new UDPEditor(ctxt, ed, 
                        se.getString("createNewUDP"), true, newU);
                eddy.desktop = desktop;
                eddy.setLocation(250, 50);
                desktop.add(eddy);
                eddy.miViewMe = menuView.add(eddy.windowNum);
                eddy.miViewMe.addActionListener(eddy);
                eddy.menuView = menuView;
                eddy.show();
                eddy.moveToFront();
                try {
                    eddy.setSelected(true);
                } catch (PropertyVetoException pv) {
                }
            }
            if (e.getActionCommand().equals("view/edit UDP")) {
                String victim = (String) UDPick.getSelectedItem();
                UserDefinedProperty theUDP = (UserDefinedProperty) ctxt.userDefinedProperties.get(victim);
                UDPEditor eddy = new UDPEditor(ctxt, ed, 
                        se.getString("editUDP") + " " + victim, false, theUDP);
                eddy.desktop = desktop;
                eddy.setLocation(250, 50);
                desktop.add(eddy);
                eddy.miViewMe = menuView.add(eddy.windowNum);
                eddy.miViewMe.addActionListener(eddy);
                eddy.menuView = menuView;
                eddy.show();
                eddy.moveToFront();
                try {
                    eddy.setSelected(true);
                } catch (PropertyVetoException pv) {
                }
            }

            if (e.getActionCommand().equals("edit dtRef")) {
                ctxt.saveState = true;
                try {
                    EditTheoryFrame etf = EditTheoryFrame.getEditTheoryFrame(ctxt.domTheoryRef());
                    etf.setVisible(true);
                } catch (Exception exc) {
                    System.err.println("ERROR in creating Edit Frame.\n" + exc);
                }
            }
            if (e.getActionCommand().equals("edit dtAddr")) {
                ctxt.saveState = true;
                try {
                    EditTheoryFrame etf = EditTheoryFrame.getEditTheoryFrame(ctxt.domTheoryAdr());
                    etf.setVisible(true);
                } catch (Exception exc) {
                    System.err.println("ERROR in creating Edit Frame.\n" + exc);
                }
            }
        }  //  end of ActionListener method actionPerformed
    }  //  end of inner class CEListener

    class CommentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            ctxt.comments = FamilyPanel.convertBannedCharacters(comments.getText());
        }

        public void removeUpdate(DocumentEvent e) {
            ctxt.comments = FamilyPanel.convertBannedCharacters(comments.getText());
        }

        public void changedUpdate(DocumentEvent e) {
            //Plain text components do not fire these events
        }
    }  // end of inner class CommentListener
}  //  end of class ContextEditor
