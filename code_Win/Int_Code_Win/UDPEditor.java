
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;

/**The UDPEditor is a window that displays a {@link UserDefinedProperty}'s fields 
 * for  editing. It is an extension of KSJInternalFrame because I want this to 
 * appear in the View menu.
 * 
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class UDPEditor extends KSJInternalFrame {

    public static final int MAXCHARTABLES = 3;
    public Context ctxt;
    public KSJInternalFrame ctxtEd;
    public boolean newUDP, newSingleValue, 
                   validValsChanged = false, 
                   defaultValExists = false,
                   name1Changed = false,
                   certainTextChanged = false,
                   defaultTextChanged = false,
                   newConnects = false,
                   newSameVal = true,
                   reName = false;
    public UserDefinedProperty theUDP;	//  The various newxxx fields are holders for the
    public String newStarName, newType; //  new values entered into this editor.
    public ArrayList<Object> newValidEntries;   //  After validation, they'll be stored on theUDP.
    Color newChartColor;
    Number newMinVal, newMaxVal;
    Object newDefaultValue;
    UDPListener listener;
    JComboBox typePick;
    JRadioButton nocertain, yescertain, yesdefault, nodefault, nomulti, yesmulti, 
                 chartYes, chartNo, connectYes, connectNo, sameVal, differentVal;
    JTextField minText, maxText;
    JTextArea certainText, name_1, defaultText;      
    JLabel certainEditLabel, defeditLabel, colorSample;
    JButton udpCheckSave, edColorBtn;
//  This element of the editor GUI is accessed by the Listener
    JPanel progBox, colorBox, connectOnBox;
    MyResBundle se = Library.screenElements,
               msgs = Library.messages;

    public UDPEditor(Context cntxt, KSJInternalFrame ctEd, String title, boolean newUDPee,
            UserDefinedProperty theUDPee) {
        super(title);
        ctxt = cntxt;
        ctxtEd = ctEd;
        newUDP = newUDPee;
        theUDP = theUDPee;
        if (newUDP) {
            windowNum = se.getString("addAUDP");
        } else {
            windowNum = se.getString("uDPEditor") + " " + theUDP.starName;
        }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addInternalFrameListener(this);  // allows View Menu to work
        listener = new UDPListener(this);

        JPanel editor = new JPanel();
        editor.setLayout(new BoxLayout(editor, BoxLayout.PAGE_AXIS));

        //  NAME BOX
        JPanel nameBox = new JPanel();
        nameBox.setLayout(new BoxLayout(nameBox, BoxLayout.LINE_AXIS));
        name_1 = new JTextArea();
        if (!newUDP) {
            name_1.setText(theUDP.starName);
        }else {
            name_1.setText("*");
        }
        name_1.setMaximumSize(new Dimension(225, 22));
        name_1.getDocument().addDocumentListener(new Name1Listener());
        JLabel nameLabel = new JLabel(se.getString("uDPName") + " ");
        nameBox.add(nameLabel);
        nameBox.add(name_1);
        JPanel nameEdBox = new JPanel();
        nameEdBox.setLayout(new BoxLayout(nameEdBox, BoxLayout.PAGE_AXIS));
        nameBox.setAlignmentX(0.5f);
        String lab;
        if (newUDP) {
            lab = se.getString("enter*Name");
        } else {
            lab = se.getString("editName");
        }
        JLabel nameEdLabel = new JLabel(lab);
        nameEdLabel.setAlignmentX(0.5f);
        nameEdLabel.setMaximumSize(new Dimension(300, 22));
        nameEdBox.add(nameBox);
        nameEdBox.add(nameEdLabel);
        nameEdBox.setAlignmentX(0.5f);
        editor.add(nameEdBox);

        //  CHARTABLE?
        JPanel chartBox = new JPanel();
        chartBox.setLayout(new BoxLayout(chartBox, BoxLayout.LINE_AXIS));
        JLabel chartLabel = new JLabel(se.getString("displayOnCharts"));
        chartYes = new JRadioButton(se.getString("yes"));
        chartYes.setActionCommand("chart yes");
        chartYes.addActionListener(listener);
        chartNo = new JRadioButton(se.getString("no"));
        chartNo.setActionCommand("chart no");
        chartNo.addActionListener(listener);
        ButtonGroup chartBtns = new ButtonGroup();
        chartBtns.add(chartYes);
        chartBtns.add(chartNo);
        if (theUDP.chartable) {
            chartYes.setSelected(true);
        } else {
            chartNo.setSelected(true);
        }
        chartBox.add(chartLabel);
        chartBox.add(chartYes);
        chartBox.add(chartNo);
        editor.add(Box.createRigidArea(new Dimension(0, 4)));
        editor.add(chartBox);
        
        // COLOR SELECTION
        colorBox = new JPanel();
        colorBox.setLayout(new BoxLayout(colorBox, BoxLayout.LINE_AXIS));
        JLabel colorLabel = new JLabel(se.getString("lineColor"));
        colorSample = new JLabel("-------");
        colorSample.setBackground(Color.white);
        edColorBtn = new JButton(se.getString("chooseColor"));
        edColorBtn.setActionCommand("change color");
        edColorBtn.addActionListener(listener);
        colorBox.add(colorLabel);
        colorBox.add(Box.createRigidArea(new Dimension(4, 0)));
        colorBox.add(colorSample);
        colorBox.add(Box.createRigidArea(new Dimension(6, 0)));
        colorBox.add(edColorBtn);
        editor.add(colorBox);
        if (theUDP.chartable) {
            colorSample.setForeground(newChartColor = theUDP.chartColor);
            edColorBtn.setText(se.getString("changeColor"));
            colorBox.setVisible(true);
        } else {
            colorSample.setText(se.getString("none_"));
            colorBox.setVisible(false);
        }
        
                //  CONNECTS?
        JPanel connectBox = new JPanel();
        connectBox.setLayout(new BoxLayout(connectBox, BoxLayout.LINE_AXIS));
        JLabel connectLabel = new JLabel(se.getString("unchartedConnections"));
        connectYes = new JRadioButton(se.getString("yes"));
        connectYes.setActionCommand("connect yes");
        connectYes.addActionListener(listener);
        connectNo = new JRadioButton(se.getString("no"));
        connectNo.setActionCommand("connect no");
        connectNo.addActionListener(listener);
        ButtonGroup connectBtns = new ButtonGroup();
        connectBtns.add(connectYes);
        connectBtns.add(connectNo);
        connectYes.setSelected(true);
        connectBox.add(connectLabel);
        connectBox.add(connectYes);
        connectBox.add(connectNo);
        editor.add(Box.createRigidArea(new Dimension(0, 4)));
        editor.add(connectBox);
        
        connectOnBox = new JPanel();
        connectOnBox.setLayout(new BoxLayout(connectOnBox, BoxLayout.LINE_AXIS));
        JLabel connectOnLabel = new JLabel(se.getString("connectionsOn"));
        sameVal = new JRadioButton(se.getString("sameValue"));
        sameVal.setActionCommand("same value");
        sameVal.addActionListener(listener);
        differentVal = new JRadioButton(se.getString("differentValue"));
        differentVal.setActionCommand("different value");
        differentVal.addActionListener(listener);
        ButtonGroup connectOnBtns = new ButtonGroup();
        connectOnBtns.add(sameVal);
        connectOnBtns.add(differentVal);
        connectOnBox.add(connectOnLabel);
        connectOnBox.add(sameVal);
        connectOnBox.add(differentVal);
        if (theUDP.connects) {
            connectYes.setSelected(true);
            sameVal.setSelected(theUDP.sameVal);
            differentVal.setSelected(!theUDP.sameVal);
            connectOnBox.setVisible(true);
        } else {
            connectNo.setSelected(true);
            connectOnBox.setVisible(false);
        }        
        editor.add(connectOnBox);

        //  DATA TYPE
        JPanel dTypeBox = new JPanel();
        dTypeBox.setLayout(new BoxLayout(dTypeBox, BoxLayout.LINE_AXIS));
        JLabel typeLabel = new JLabel(se.getString("dataType") + " ");
        Dimension sizer = new Dimension(150, 50);
        String[] typeMenu = {se.getString("string"), 
            se.getString("integer"), se.getString("realNumber"), 
            se.getString("boolean"), se.getString("person")};
        typePick = new JComboBox(typeMenu);
        if (newUDP) {
            typePick.setSelectedIndex(0);
            newType = "string";
        } else {
            if (theUDP.typ.equals("integer")) {
                typePick.setSelectedIndex(1);
            } else if (theUDP.typ.equals("float")) {
                typePick.setSelectedIndex(2);
            } else if (theUDP.typ.equals("string")) {
                typePick.setSelectedIndex(0);
            } else if (theUDP.typ.equals("boolean")) {
                typePick.setSelectedIndex(3);
            } else {
                typePick.setSelectedIndex(4);
            }
            newType = theUDP.typ;
        }  //  end of setting the current data type
        typePick.addActionListener(listener);
        typePick.setActionCommand("data type");
        typePick.setMinimumSize(sizer);
        typePick.setMaximumSize(sizer);
        dTypeBox.add(typeLabel);
        dTypeBox.add(typePick);
        editor.add(Box.createRigidArea(new Dimension(0, 4)));
        editor.add(dTypeBox);

        //  MULTIPLE VALUES?
        JPanel multiBox = new JPanel();
        multiBox.setLayout(new BoxLayout(multiBox, BoxLayout.LINE_AXIS));
        JLabel multiLabel = new JLabel(se.getString("multiValues"));
        yesmulti = new JRadioButton(se.getString("yes"));
        yesmulti.setActionCommand("multiVal yes");
        yesmulti.addActionListener(listener);
        nomulti = new JRadioButton(se.getString("no"));
        nomulti.setActionCommand("multiVal no");
        nomulti.addActionListener(listener);
        if (theUDP.singleValue) {
            nomulti.setSelected(true);
        } else {
            yesmulti.setSelected(true);
        }
        newSingleValue = theUDP.singleValue;
        ButtonGroup multiBtns = new ButtonGroup();
        multiBtns.add(yesmulti);
        multiBtns.add(nomulti);
        multiBox.add(multiLabel);
        multiBox.add(yesmulti);
        multiBox.add(nomulti);
        editor.add(Box.createRigidArea(new Dimension(0, 4)));
        editor.add(multiBox);

        //  RESTRICTED TO CERTAIN VALUES?
        JPanel certainBox = new JPanel();
        certainBox.setLayout(new BoxLayout(certainBox, BoxLayout.LINE_AXIS));
        JLabel certainLabel = new JLabel(se.getString("restrictedValues"));
        yescertain = new JRadioButton(se.getString("yes"));
        yescertain.setActionCommand("certainVal yes");
        yescertain.addActionListener(listener);
        nocertain = new JRadioButton(se.getString("no"));
        nocertain.setActionCommand("certainVal no");
        nocertain.addActionListener(listener);
        if (newUDP || (theUDP.validEntries == null)
                || (theUDP.validEntries.isEmpty()) || (newType.equals("individual"))) {
            nocertain.setSelected(true);
        } else {
            yescertain.setSelected(true);
        }
        newValidEntries = theUDP.validEntries;
        ButtonGroup certainBtns = new ButtonGroup();
        certainBtns.add(yescertain);
        certainBtns.add(nocertain);
        certainBox.add(certainLabel);
        certainBox.add(yescertain);
        certainBox.add(nocertain);

        certainText = new JTextArea();
        certainText.setMinimumSize(new Dimension(400, 30));
        certainText.setMaximumSize(new Dimension(400, 30));
        certainText.setAlignmentX(0.5f);
        certainEditLabel = new JLabel();
        certainEditLabel.setAlignmentX(0.5f);
        if (newUDP || (theUDP.validEntries == null) || (theUDP.validEntries.isEmpty())) {
            certainText.setText("");
            certainEditLabel.setText("");
            certainText.setEditable(false);
            newValidEntries = null;
        } else {
            certainText.setEditable(true);
            certainEditLabel.setText(se.getString("editValues"));
            newValidEntries = theUDP.validEntries;
            certainText.setText(theUDP.getValidEntriesString());
            certainText.getDocument().addDocumentListener(new CertainTextListener());
        }  //  end of it's restricted
        editor.add(Box.createRigidArea(new Dimension(0, 4)));
        editor.add(certainBox);
        editor.add(Box.createRigidArea(new Dimension(0, 4)));
        editor.add(certainEditLabel);
        editor.add(certainText);

        //  DEFAULT VALUE
        JPanel defaultBox = new JPanel();
        defaultBox.setLayout(new BoxLayout(defaultBox, BoxLayout.LINE_AXIS));
        JLabel defaultLabel = new JLabel(se.getString("defaultValue"));
        yesdefault = new JRadioButton(se.getString("yes"));
        yesdefault.setActionCommand("defaultVal yes");
        yesdefault.addActionListener(listener);
        nodefault = new JRadioButton(se.getString("no"));
        nodefault.setActionCommand("defaultVal no");
        nodefault.addActionListener(listener);
        nodefault.setSelected(true);
        ButtonGroup defaultBtns = new ButtonGroup();
        defaultBtns.add(yesdefault);
        defaultBtns.add(nodefault);
        defaultBox.add(defaultLabel);
        defaultBox.add(yesdefault);
        defaultBox.add(nodefault);
        defaultText = new JTextArea();
        defaultText.setMinimumSize(new Dimension(400, 30));
        defaultText.setMaximumSize(new Dimension(400, 30));
        defaultText.setAlignmentX(0.5f);
        defeditLabel = new JLabel();
        defeditLabel.setAlignmentX(0.5f);
        if (newUDP || (theUDP.defaultValue == null)) {
            defaultText.setText("");
            defeditLabel.setText("");
            defaultText.setEditable(false);
            defaultValExists = false;
        } else {
            defaultText.setEditable(true);
            defeditLabel.setText(se.getString("editDefaultValue"));
            newDefaultValue = theUDP.defaultValue;
            yesdefault.setSelected(true);
            defaultText.setText(theUDP.defaultValue.toString());
            defaultText.getDocument().addDocumentListener(new DefaultTextListener());
            defaultValExists = true;
        }  //  end of there is a default value
        editor.add(Box.createRigidArea(new Dimension(0, 4)));
        editor.add(defaultBox);
        editor.add(Box.createRigidArea(new Dimension(0, 4)));
        editor.add(defeditLabel);
        editor.add(defaultText);

        //  MIN AND MAX VALUES
        JPanel minMaxBox = new JPanel();
        minMaxBox.setLayout(new BoxLayout(minMaxBox, BoxLayout.LINE_AXIS));
        JLabel minLabel = new JLabel(se.getString("minimumValue") + " ");
        minText = new JTextField();
        minText.setMinimumSize(new Dimension(50, 30));
        minText.setMaximumSize(new Dimension(50, 30));
        if (theUDP.minVal == null) {
            minText.setText("");
        } else {
            minText.setText(theUDP.minVal.toString());
        }
        minText.addActionListener(listener);
        minText.setActionCommand("minText edit");
        minMaxBox.add(minLabel);
        minMaxBox.add(minText);
        JPanel maxBox = new JPanel();
        maxBox.setLayout(new BoxLayout(maxBox, BoxLayout.PAGE_AXIS));
        JLabel maxLabel = new JLabel(se.getString("maximumValue") + " ");
        maxText = new JTextField();
        maxText.setMinimumSize(new Dimension(50, 30));
        maxText.setMaximumSize(new Dimension(50, 30));
        if (theUDP.maxVal == null) {
            maxText.setText("");
        } else {
            maxText.setText(theUDP.maxVal.toString());
        }
        maxText.addActionListener(listener);
        maxText.setActionCommand("maxText edit");
        if (((String) typePick.getSelectedItem()).equals(se.getString("integer")) || 
                ((String) typePick.getSelectedItem()).equals(se.getString("realNumber"))) {
            minText.setEditable(true);
            maxText.setEditable(true);
        } else {
            minText.setEditable(false);
            maxText.setEditable(false);
        }
        minMaxBox.add(Box.createRigidArea(new Dimension(50, 0)));
        minMaxBox.add(maxLabel);
        minMaxBox.add(maxText);
        editor.add(Box.createRigidArea(new Dimension(0, 4)));
        editor.add(minMaxBox);

        //  BUTTONS AT THE BOTTOM
        JPanel bottomBtns = new JPanel();
        bottomBtns.setLayout(new BoxLayout(bottomBtns, BoxLayout.LINE_AXIS));
        JButton udpCancel = new JButton(se.getString("cancel"));
        udpCancel.setActionCommand("cancel");
        udpCancel.addActionListener(listener);
        bottomBtns.add(udpCancel);
        if (!newUDP) {  //  no need to delete a UDP being created -- just cancel
            JButton udpDelete = new JButton(se.getString("deleteUDP"));
            udpDelete.setActionCommand("delete");
            udpDelete.addActionListener(listener);
            bottomBtns.add(udpDelete);
        }
        udpCheckSave = new JButton(se.getString("check4Errors"));
        udpCheckSave.setActionCommand("check");
        udpCheckSave.addActionListener(listener);
        bottomBtns.add(udpCheckSave);
        editor.add(Box.createRigidArea(new Dimension(0, 6)));
        editor.add(bottomBtns);

        progBox = new JPanel();
        progBox.setLayout(new BoxLayout(progBox, BoxLayout.PAGE_AXIS));
        String progMod = " " + se.getString("revisedUDP");
        if (newUDP) {
            progMod = " " + se.getString("newUDP");
        }
        JLabel progLabel = new JLabel(se.getString("updating") 
                + " " + ctxt.indSerNumGen + " " 
                + se.getString("personsWith") + progMod);
        progBox.add(progLabel);
        progBox.setVisible(false);
        editor.add(Box.createRigidArea(new Dimension(0, 6)));
        editor.add(progBox);

        JPanel sideFrame = new JPanel();
        sideFrame.setLayout(new BoxLayout(sideFrame, BoxLayout.LINE_AXIS));
        sideFrame.add(Box.createRigidArea(new Dimension(10, 0)));
        sideFrame.add(editor);
        sideFrame.add(Box.createRigidArea(new Dimension(10, 0)));
        getContentPane().add(sideFrame);
        addInternalFrameListener(this);
        setSize(600, 400);
        setVisible(true);
    }  //  end of UDPEditor constructor

   public void flipFinalBtn(String saveORnot) {
        if (saveORnot.equals("save")) {
            udpCheckSave.setActionCommand("save");
            udpCheckSave.setText(se.getString("save"));
        } //  end of flip to SAVE
        else {
            udpCheckSave.setActionCommand("check");
            udpCheckSave.setText(se.getString("check4Errors"));
        }  //  end of flip to CHECK
    }  //  end of method flipFinalBtn

    public void validateCertainVals() {
        String theVals = certainText.getText();
        if (theVals.isEmpty()) {
            newValidEntries.clear();
            return;  //  nothing to validate
        }
        validValsChanged = true;
        ArrayList<Object> safetyNet = new ArrayList<Object>(newValidEntries);  // in case the edits were bad
        newValidEntries.clear();
        int start = 0, nextComma = 0, length = theVals.length();
        //  In effect, we reverse the logic of getValidEntriesString
        try {
            if (newType.equals("string")) {
                while (nextComma < length) {
                    nextComma = theVals.substring(start).indexOf(",");
                    if (nextComma == -1) {
                        nextComma = length;
                    } else {
                        nextComma += start;
                    }
                    newValidEntries.add(theVals.substring(start, nextComma).trim());
                    start = nextComma + 1;
                }
                // Now test for embedded blanks == missing commas in theVals
                for (Object o : newValidEntries) {
                    String s = (String) o, msg;
                    if (s.contains(" ")) {
                        msg = "Embedded blanks found in '" + s + "'\n"
                                + "Separate values with commas. Use underscores in compound words.";
                        throw new KSConstraintInconsistency(msg);
                    }
                    try {
                        PersonPanel.sanitizeKinTerms(this, s, "this value");
                    } catch (KSParsingErrorException exc) {
                        msg = "In value '" + s + "': " + exc.toString();
                        throw new KSConstraintInconsistency(msg);
                    }
                }  //  end of typ = string
            } else if (newType.equals("integer")) {
                while (nextComma < length) {
                    nextComma = theVals.substring(start).indexOf(",");
                    if (nextComma == -1) {
                        nextComma = length;
                    } else {
                        nextComma += start;
                    }
                    newValidEntries.add(new Integer(theVals.substring(start, nextComma).trim()));
                    start = nextComma + 1;
                }
            } //  //  end of typ = int
            else if (newType.equals("float")) {
                while (nextComma < length) {
                    nextComma = theVals.substring(start).indexOf(",");
                    if (nextComma == -1) {
                        nextComma = length;
                    } else {
                        nextComma += start;
                    }
                    newValidEntries.add(new Float(theVals.substring(start, nextComma).trim()));
                    start = nextComma + 1;
                }
            }  //  end of typ = float
        } catch (NumberFormatException nfe) {
            if (MainPane.activity == null) {
                MainPane.createActivityLog(desktop, menuView);
            }
            String eMsg = msgs.getString("dataTypeIs") + newType + 
                    msgs.getString("entriesNotAccepted");
            MainPane.activity.log.append("While parsing 'int' Valid Entries:\n" + nfe + "\n" + eMsg);
            JOptionPane.showMessageDialog(this, eMsg, msgs.getString("typeViolation"), 
                    JOptionPane.INFORMATION_MESSAGE);
            newValidEntries = safetyNet;
            return;
        } catch (Exception exc) {
            if (MainPane.activity == null) {
                MainPane.createActivityLog(desktop, menuView);
            }
            String eMsg = msgs.getString("errorWhileParsing") + " " + newType 
                    + msgs.getString("inUDPEd") + exc 
                    + msgs.getString("entriesNotAccepted");
            MainPane.activity.log.append(eMsg + "\n" + exc);
            JOptionPane.showMessageDialog(this, eMsg, msgs.getString("typeViolation"), 
                    JOptionPane.INFORMATION_MESSAGE);
            newValidEntries = safetyNet;
            return;
        }  //  end of catch blocks
        flipFinalBtn("check");
        certainTextChanged = false;
        return;
    }  //  end of method validateCertainVals()

    public boolean validateDefaultVal() {
        //  Can't get here if typ = indiv
        if (!defaultValExists) {
            return true;  //  Nothing to validate
        }
        String defVal = defaultText.getText();
        Object safetyNet = newDefaultValue;
        if (defVal.length() > 0) {
            try {
                if (newType.equals("string")) {
                    newDefaultValue = defVal;
                } else if (newType.equals("integer")) {
                    newDefaultValue = new Integer(defVal);
                } else if (newType.equals("float")) {
                    newDefaultValue = new Float(defVal);
                } else if (newType.equals("boolean")) {
                    if (!((defVal.equals("true")) || (defVal.equals("false")))) {
                        throw new KinshipSystemException("Did not enter 'true' or 'false'.");
                    } else {
                        newDefaultValue = new Boolean(defVal);
                    }
                }  //  end of type=boolean
            } catch (Exception exc) {
                if (MainPane.activity == null) {
                    MainPane.createActivityLog(desktop, menuView);
                }
                String eMsg = msgs.getString("errorWhileParsing") + " " + newType 
                    + msgs.getString("inUDPEd") + exc 
                    + msgs.getString("entriesNotAccepted");
                MainPane.activity.log.append(eMsg + "\n" + exc);
                JOptionPane.showMessageDialog(this, eMsg, msgs.getString("typeViolation"), 
                        JOptionPane.INFORMATION_MESSAGE);
                newDefaultValue = safetyNet;
                return false;
            }  //  end of catch block
            //  Check for consistency with Restricted Values
            if ((newValidEntries != null) && (newValidEntries.size() > 0) && (!newValidEntries.contains(newDefaultValue))) {
                if (MainPane.activity == null) {
                    MainPane.createActivityLog(desktop, menuView);
                }
                String eMsg = msgs.getString("yourDefaultValue") + " " + defVal 
                        + " " + msgs.getString("notValidRestricted");
                MainPane.activity.log.append(eMsg);
                JOptionPane.showMessageDialog(this, eMsg, 
                        msgs.getString("restrictedValueViolation"), 
                        JOptionPane.INFORMATION_MESSAGE);
                newDefaultValue = safetyNet;
                return false;
            }  //  end of inconsistent-with-Valid-Entries
            flipFinalBtn("check");
            defaultTextChanged = false;
            return true;
        } else {  //  default required but none entered
            JOptionPane.showMessageDialog(this, msgs.getString("noDefaultEntered1"),
                    msgs.getString("noDefaultEntered2"), JOptionPane.INFORMATION_MESSAGE);
            return false;
        }  //  end of none entered
    }  //  end of method validateDefaultVal()

    public boolean validateMinText() {
        //  The Min field is only valid for data types of integer and float (Real Number)
        Number newVal;
        if (minText.getText().equals("")) {
            return true;
        }
        try {
            if (newType.equals("float")) {
                newVal = new Float(minText.getText());
            } else {
                newVal = new Integer(minText.getText());
            }
        } catch (NumberFormatException nfe) {
            if (MainPane.activity == null) {
                MainPane.createActivityLog(desktop, menuView);
            }
            String eMsg = msgs.getString("yourMinVal") + minText.getText() + " "
                    + msgs.getString("isNotAValid")
                    + newType + msgs.getString("tryAgain");
            MainPane.activity.log.append(eMsg);
            JOptionPane.showMessageDialog(this, eMsg, 
                    msgs.getString("invalidMinimumValue"), JOptionPane.INFORMATION_MESSAGE);
            return false;
        }  //  end of catch block
        if ((newValidEntries != null) && (newValidEntries.size() > 0) && (newValidEntries.get(0) instanceof Number)) {
            for (int i = 0; i < newValidEntries.size(); i++) {
                if (((Number) newValidEntries.get(i)).floatValue() < newVal.floatValue()) {
                    if (MainPane.activity == null) {
                        MainPane.createActivityLog(desktop, menuView);
                    }
                    String eMsg = msgs.getString("yourMinVal") + newVal + 
                            msgs.getString("isGreaterThan") + " "
                            + newValidEntries.get(i) + msgs.getString("oneOfCertainVals");
                    MainPane.activity.log.append(eMsg);
                    JOptionPane.showMessageDialog(this, eMsg, 
                            msgs.getString("invalidMinimumValue"), JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }  //  end of activity-found
            }
        }
        if ((newDefaultValue != null) && (newDefaultValue instanceof Number)
                && (((Number) newDefaultValue).floatValue() < newVal.floatValue())) {
            if (MainPane.activity == null) {
                MainPane.createActivityLog(desktop, menuView);
            }
            String eMsg = msgs.getString("yourMinVal") + newVal + 
                            msgs.getString("isGreaterThan") + " "
                    + newDefaultValue + msgs.getString("yourDefValTryAgain");
            MainPane.activity.log.append(eMsg);
            JOptionPane.showMessageDialog(this, eMsg, 
                    msgs.getString("invalidMinimumValue"), JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        if ((newMaxVal != null) && (newMaxVal.floatValue() < newVal.floatValue())) {
            if (MainPane.activity == null) {
                MainPane.createActivityLog(desktop, menuView);
            }
            String eMsg = msgs.getString("yourMinVal") + newVal + 
                            msgs.getString("isGreaterThan") + " "
                    + newMaxVal + msgs.getString("yourMaxValTryAgain");
            MainPane.activity.log.append(eMsg);
            JOptionPane.showMessageDialog(this, eMsg, 
                    msgs.getString("invalidMinimumValue"), JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        newMinVal = newVal;
        return true;
    }  //  end of method validateMinText

    public boolean validateMaxText() {
        //  The Max field is only valid for data types of integer and float (Real Number)
        Number newVal;
        if (maxText.getText().equals("")) {
            return true;
        }
        try {
            if (newType.equals("float")) {
                newVal = new Float(maxText.getText());
            } else {
                newVal = new Integer(maxText.getText());
            }
        } catch (NumberFormatException nfe) {
            if (MainPane.activity == null) {
                MainPane.createActivityLog(desktop, menuView);
            }
            String eMsg = msgs.getString("yourMaxVal") + maxText.getText() + msgs.getString("isNotAValid")
                    + newType + msgs.getString("tryAgain");
            MainPane.activity.log.append(eMsg);
            JOptionPane.showMessageDialog(this, eMsg, 
                    msgs.getString("invalidMaximumValue"), JOptionPane.INFORMATION_MESSAGE);
            return false;
        }  //  end of catch block
        if ((newValidEntries != null) && (newValidEntries.size() > 0) && (newValidEntries.get(0) instanceof Number)) {
            for (int i = 0; i < newValidEntries.size(); i++) {
                if (((Number) newValidEntries.get(i)).floatValue() > newVal.floatValue()) {
                    if (MainPane.activity == null) {
                        MainPane.createActivityLog(desktop, menuView);
                    }
                    String eMsg = msgs.getString("yourMaxVal") + newVal + 
                            msgs.getString("isLessThan") + " "
                            + newValidEntries.get(i) + msgs.getString("oneOfCertainVals");
                    MainPane.activity.log.append(eMsg);
                    JOptionPane.showMessageDialog(this, eMsg, 
                    msgs.getString("invalidMaximumValue"), JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }  //  end of activity-found
            }
        }
        if ((newDefaultValue != null) && (newDefaultValue instanceof Number)
                && (((Number) newDefaultValue).floatValue() > newVal.floatValue())) {
            if (MainPane.activity == null) {
                MainPane.createActivityLog(desktop, menuView);
            }
            String eMsg = msgs.getString("yourMaxVal") + newVal + 
                            msgs.getString("isLessThan") + " "
                    + newDefaultValue + msgs.getString("yourDefValTryAgain");
            MainPane.activity.log.append(eMsg);
            JOptionPane.showMessageDialog(this, eMsg, 
                    msgs.getString("invalidMaximumValue"), JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        if ((newMinVal != null) && (newMinVal.floatValue() > newVal.floatValue())) {
            if (MainPane.activity == null) {
                MainPane.createActivityLog(desktop, menuView);
            }
            String eMsg = msgs.getString("yourMaxVal") + newVal + 
                          msgs.getString("isLessThan") + " "
                          + newMinVal + msgs.getString("yourMinValTryAgain");
            MainPane.activity.log.append(eMsg);
            JOptionPane.showMessageDialog(this, eMsg, 
                    msgs.getString("invalidMaximumValue"), JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        newMaxVal = newVal;
        return true;
    }  //  end of method validateMinText
    
    
     public class UDPListener implements ActionListener {

        KSJInternalFrame ed;

        public UDPListener(KSJInternalFrame uded) {
            ed = uded;
        }  //  end of constructor

        public void actionPerformed(ActionEvent e) {
            Set names;
            if (ctxt.userDefinedProperties != null) {
                names = ctxt.userDefinedProperties.keySet();
            } else {
                names = new TreeSet();
            }
            if (name1Changed) {  //  This check always performed
                String newName = name_1.getText(), msg,
                       rest = newName.substring(1),
                       test = rest.toLowerCase();                
                if (!newName.startsWith("*") || !rest.equals(test)
                        || newName.length() < 2) {
                    msg = msgs.getString("theName") + newName + msgs.getString("violatesNameRules");
                    JOptionPane.showMessageDialog(ed, msg, 
                            msgs.getString("namingRulesViolation"),
                            JOptionPane.WARNING_MESSAGE);
                    flipFinalBtn("check");
                    name_1.setText("*");
                    name1Changed = false;
                } else if (newUDP && names.contains(newName)) {
                    msg = msgs.getString("theName") + newName + msgs.getString("dupUDPName");
                    JOptionPane.showMessageDialog(ed, msg, 
                            msgs.getString("namingRulesViolation"),
                            JOptionPane.WARNING_MESSAGE);
                    flipFinalBtn("check");
                    name_1.setText("*");
                    newName = "%%%%%%%%%%";
                    name1Changed = false;
                } else {
                    try {
                        newName = PersonPanel.sanitizeKinTerms(ed, 
                                newName.substring(1), msgs.getString("thisUDPName"));
                    } catch (KSParsingErrorException exc) {
                        flipFinalBtn("check");
                        name_1.setText("*");
                        newName = "%%%%%%%%%%";
                        name1Changed = false;
                    }
                }
                if (!newName.equals("%%%%%%%%%%")) {
                    //  Once past validity-check, we can safely record the new starName
                    newStarName = "*" + newName;
                    name_1.setText(newStarName);
                    if ((!newUDP) && (!newStarName.equals(theUDP.starName))) {  //  name change
                        reName = true;
                    }  //  end of name-change
                    flipFinalBtn("check");
                    name1Changed = false;
                }
            }            
            if (certainTextChanged) {
                validateCertainVals();
            }
            if (defaultTextChanged) {
                validateDefaultVal();
            }
            
            if (e.getActionCommand().equals("chart yes")) {
                String msg;
                int count = 0;
                if (ctxt.userDefinedProperties != null) {
                    Iterator udpIter = ctxt.userDefinedProperties.values().iterator();
                    while (udpIter.hasNext()) {
                        UserDefinedProperty udie = (UserDefinedProperty) udpIter.next();
                        if (udie.chartable) {
                            count++;
                        }
                    }
                    if (count >= MAXCHARTABLES) {
                        msg = msgs.getString("youAlreadyHave") + " " + 
                                MAXCHARTABLES + " " + msgs.getString("maxAllowedUDPs");
                        JOptionPane.showMessageDialog(ctxtEd, msg, 
                                msgs.getString("cannotMakeAnotherChartable"),
                                JOptionPane.ERROR_MESSAGE);
                        chartNo.setSelected(true);
                        colorBox.setVisible(false);
                        return;
                    }
                }
                // If a new UDP, reject for name error only.
                // Other faults for new UDP => offer to fix.
                if (newUDP) {
                    if (newSingleValue || newType == null
                            || !newType.equals("individual")) {
                        msg = msgs.getString("needMultiPersons")
                                + msgs.getString("makeSettings4You");
                        Object[] btns = {se.getString("yes"), se.getString("no")};
                        int ch = JOptionPane.showOptionDialog(ctxtEd, msg,
                                msgs.getString("illegalCombination"),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE,
                                null, btns, btns[0]);
                        if (ch == JOptionPane.YES_OPTION) {
                            typePick.setSelectedItem(se.getString("person"));
                            yesmulti.setSelected(true);
                        } else {
                            chartNo.setSelected(true);
                            colorBox.setVisible(false);
                            return;
                        }
                    }
                } else if (newSingleValue  //  not new
                        || ! newType.equals("individual")) {
                    msg = msgs.getString("needMultiPersons");
                    JOptionPane.showMessageDialog(ctxtEd, msg, 
                            msgs.getString("illegalCombination"),
                            JOptionPane.ERROR_MESSAGE);
                    chartNo.setSelected(true);
                    colorBox.setVisible(false);
                    return;
                }
                Color c = (theUDP.chartColor == null ? Color.black : theUDP.chartColor);
                colorSample.setForeground(c);
                colorSample.setBackground(Color.white);
                edColorBtn.setText("Choose Color");
                colorBox.setVisible(true);
            }
            
            else if (e.getActionCommand().equals("chart no")) {
                newChartColor = null;
                colorBox.setVisible(false);
            } else if (e.getActionCommand().equals("connect yes")) {
                String msg;
                if (!newSingleValue || newType == null
                        || !newType.equals("string") || nocertain.isSelected()) {
                    msg = msgs.getString("needSingleString");
                    msg += msgs.getString("makeSettings4You");
                    Object[] btns = {se.getString("yes"), se.getString("no")};
                    int ch = JOptionPane.showOptionDialog(ctxtEd, msg,
                            msgs.getString("illegalCombination"),
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE,
                            null, btns, btns[0]);
                    if (ch == JOptionPane.YES_OPTION) {
                        typePick.setSelectedItem(se.getString("string"));
                        nomulti.setSelected(true);
                        if ((newUDP) || (theUDP.validEntries == null)) {
                            newValidEntries = new ArrayList<Object>();
                            certainText.setText("");
                            certainEditLabel.setText(se.getString("enterValsWithCommas"));
                        } else {
                            newValidEntries = theUDP.validEntries;
                            certainText.setText(theUDP.getValidEntriesString());
                            certainEditLabel.setText(se.getString("editValsWithCommas"));
                        }
                        newSingleValue = true;
                        certainText.setEditable(true);
                        yescertain.setSelected(true);
                        certainText.getDocument().addDocumentListener(new CertainTextListener());
                    } else {  // She declined our offer to make settings
                        connectNo.setSelected(true);
                        return;
                    }
                }  //  end of check for illegal combinations
                newConnects = true;
                connectOnBox.setVisible(true);
            }
            
            else if (e.getActionCommand().equals("connect no")) {
                newConnects = false;
                connectOnBox.setVisible(false);
            }
            
            else if (e.getActionCommand().equals("same value")) {
                newSameVal = true;
            }
            
            else if (e.getActionCommand().equals("different value")) {
                newSameVal = false;
            }
            
            else if (e.getActionCommand().equals("change color")) {
                newChartColor = JColorChooser.showDialog(ed,
                        se.getString("colorOfLines"),
                        colorSample.getBackground());
                colorSample.setText("-------");
                colorSample.setForeground(newChartColor);
                edColorBtn.setText(se.getString("changeColor"));
            }
            
            else if (e.getActionCommand().equals("data type")) {
                String priorType = newType;  //  prior value
                String typ = (String) typePick.getSelectedItem();
                if (typ.equals(se.getString("string"))) {
                    newType = "string";
                    if (priorType.equals(newType)) {
                        return;  //  Nothing changed
                    }
                    minText.setText("");
                    minText.setEditable(false);
                    maxText.setText("");
                    maxText.setEditable(false);
                    if (yescertain.isSelected()) {
                        certainEditLabel.setText(se.getString("edVals"));
                        certainText.setEditable(true);
                    } else {
                        certainEditLabel.setText("");
                        certainText.setEditable(false);
                    }
                    if (yesdefault.isSelected()) {
                        defaultText.setEditable(true);
                        defeditLabel.setText(se.getString("editDefaultValue"));
                    } else {
                        defaultText.setEditable(false);
                        defeditLabel.setText("");
                    }
                } else if (typ.equals(se.getString("integer"))) {
                    newType = "integer";
                    if (priorType.equals(newType)) {
                        return;  //  Nothing changed
                    }
                    minText.setEditable(true);
                    maxText.setEditable(true);
                    connectNo.setSelected(true);
                    if (yescertain.isSelected()) {
                        certainEditLabel.setText(se.getString("editValues"));
                        certainText.setEditable(true);
                    } else {
                        certainEditLabel.setText("");
                        certainText.setEditable(false);
                    }
                    if (yesdefault.isSelected()) {
                        defaultText.setEditable(true);
                        defeditLabel.setText(se.getString("editDefaultValue"));
                    } else {
                        defaultText.setEditable(false);
                        defeditLabel.setText("");
                    }
                } else if (typ.equals(se.getString("realNumber"))) {
                    newType = "float";
                    if (priorType.equals(newType)) {
                        return;  //  Nothing changed
                    }
                    minText.setEditable(true);
                    maxText.setEditable(true);
                    connectNo.setSelected(true);
                    if (yescertain.isSelected()) {
                        certainEditLabel.setText(se.getString("editValues"));
                        certainText.setEditable(true);
                    } else {
                        certainEditLabel.setText("");
                        certainText.setEditable(false);
                    }
                    if (yesdefault.isSelected()) {
                        defaultText.setEditable(true);
                        defeditLabel.setText(se.getString("editDefaultValue"));
                    } else {
                        defaultText.setEditable(false);
                        defeditLabel.setText("");
                    }
                } else if (typ.equals(se.getString("boolean"))) {
                    newType = "boolean";
                    if (priorType.equals(newType)) {
                        return;  //  Nothing changed
                    }
                    minText.setText("");
                    minText.setEditable(false);
                    maxText.setText("");
                    maxText.setEditable(false);
                    nomulti.setSelected(true);
                    newSingleValue = true;
                    nocertain.setSelected(true);
                    certainText.setText("");
                    certainEditLabel.setText("");
                    certainText.setEditable(false);
                    newValidEntries = null;
                    connectNo.setSelected(true);
                } else if (typ.equals(se.getString("person"))) {
                    newType = "individual";
                    if (priorType.equals(newType)) {
                        return;  //  Nothing changed
                    }
                    minText.setText("");
                    minText.setEditable(false);
                    maxText.setText("");
                    maxText.setEditable(false);
                    nocertain.setSelected(true);
                    certainText.setText("");
                    certainEditLabel.setText("");
                    certainText.setEditable(false);
                    newValidEntries = null;
                    nodefault.setSelected(true);
                    defaultText.setEditable(false);
                    defeditLabel.setText("");
                    connectNo.setSelected(true);
                }
                if ((newValidEntries != null) && (newValidEntries.size() > 0)) {
                    //  Changed type after specifying valid entries
                    newValidEntries = null;
                    certainText.setText("");
                    certainEditLabel.setText("");
                    nocertain.setSelected(true);
                }

                flipFinalBtn("check");
                return;
            }
            else if (e.getActionCommand().equals("multiVal yes")) {
                if (newType.equals("boolean")) {
                    newSingleValue = true;
                    nomulti.setSelected(true);
                    JOptionPane.showMessageDialog(ed, 
                            msgs.getString("noMultiBooleans"),
                            msgs.getString("illegalCombination"),
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    newSingleValue = false;
                }
                return;
            }
            else if (e.getActionCommand().equals("multiVal no")) {
                newSingleValue = true;
                return;
            }
            else if (e.getActionCommand().equals("certainVal yes")) {
                if (newType.equals("individual")) {
                    JOptionPane.showMessageDialog(ed, 
                            msgs.getString("noRestrictedPersons"),
                            msgs.getString("illegalCombination"),
                            JOptionPane.INFORMATION_MESSAGE);
                    nocertain.setSelected(true);
                    return;
                } //  end of type = indiv
                else if (newType.equals("boolean")) {
                    JOptionPane.showMessageDialog(ed, 
                            msgs.getString("noRestrictedBooleans"),
                            msgs.getString("illegalCombination"),
                            JOptionPane.INFORMATION_MESSAGE);
                    nocertain.setSelected(true);
                    return;
                } //  end of type = boolean
                else if ((newUDP) || (theUDP.validEntries == null)) {
                    newValidEntries = new ArrayList<Object>();
                    certainText.setText("");
                    certainEditLabel.setText(se.getString("enterValsWithCommas"));
                } else {
                    newValidEntries = theUDP.validEntries;
                    certainText.setText(theUDP.getValidEntriesString());
                    certainEditLabel.setText(se.getString("editValsWithCommas"));
                }
                certainText.setEditable(true);
                certainText.getDocument().addDocumentListener(new CertainTextListener());
                flipFinalBtn("check");
                return;
            }
            else if (e.getActionCommand().equals("certainVal no")) {
                newValidEntries = null;
                certainText.setText("");
                certainEditLabel.setText("");
                certainText.setEditable(false);
                flipFinalBtn("check");
                return;
            }
            else if (e.getActionCommand().equals("defaultVal yes")) {
                if (newType.equals("individual")) {
                    JOptionPane.showMessageDialog(ed, 
                            msgs.getString("noDefaultPersons"),
                            msgs.getString("illegalCombination"),
                            JOptionPane.INFORMATION_MESSAGE);
                    nodefault.setSelected(true);
                    return;
                }  //  end of type = indiv
                if (theUDP.defaultValue != null) {
                    newDefaultValue = theUDP.defaultValue;
                    defaultText.setText(theUDP.defaultValue.toString());
                }
                defaultText.setEditable(true);
                defaultText.getDocument().addDocumentListener(new DefaultTextListener());
                defeditLabel.setText(se.getString("editDefaultValue"));
                defaultValExists = true;
                flipFinalBtn("check");
                return;
            }
            else if (e.getActionCommand().equals("defaultVal no")) {
                newDefaultValue = null;
                defaultText.setText("");
                defeditLabel.setText("");
                defaultText.setEditable(false);
                defaultValExists = false;
                flipFinalBtn("check");
                return;
            }
            else if (e.getActionCommand().equals("minText edit")) {
                validateMinText();
                return;
            }
            else if (e.getActionCommand().equals("maxText edit")) {
                validateMaxText();
                return;
            }
            else if (e.getActionCommand().equals("cancel")) {
                theUDP = null;
                try {
                    ed.setClosed(true);  //  by not writing & saving, we cancel
                } catch (PropertyVetoException pve) {
                    if (MainPane.activity == null) {
                        MainPane.createActivityLog(desktop, menuView);
                    }
                    MainPane.activity.log.append("In UDPEditor:\n" + pve);
                    System.out.println("In UDPEditor:\n" + pve);
                }
                return;
            }
            else if (e.getActionCommand().equals("delete")) {
                int numWithProp = ctxt.individualCensus.size(),
                        numWithVals = 0;
                Iterator indIter = ctxt.individualCensus.iterator();
                UserDefinedProperty udp;
                while (indIter.hasNext()) {
                    udp = (UserDefinedProperty) ((Individual) indIter.next()).userDefinedProperties.get(theUDP.starName);
                    if (udp != null && udp.value.size() > 0) {
                        numWithVals++;
                    }
                }  //  end of loop thru persons on this context
                String plural = (numWithVals == 1 ? msgs.getString("thereIs")
                        : msgs.getString("thereAre"));
                String msg = msgs.getString("thereAre") + " " + numWithProp + " "
                        + msgs.getString("personsWithProp") + plural + " "
                        + numWithVals + " " + msgs.getString("withValSureDelete");
                Object[] btns = {se.getString("yes"), se.getString("no")};
                int choice = JOptionPane.showOptionDialog(ed, msg,
                        se.getString("confirm"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null, btns, btns[0]);

                if (choice == JOptionPane.YES_OPTION) {
                    indIter = ctxt.individualCensus.iterator();
                    while (indIter.hasNext()) {
                        ((Individual) indIter.next()).userDefinedProperties.remove(theUDP.starName);
                    }
                    ctxt.userDefinedProperties.remove(theUDP.starName); 
                    if (theUDP.chartable) {                                                
                        ctxt.removeChartableUDP(theUDP.starName);                        
                    }
                    if (SIL_Edit.edWin != null) {
                        SIL_Edit.edWin.getPPanel().initUDPCombo();
                        SIL_Edit.edWin.chart.repaint();
                    }
                }  //  end of User-chose-to-delete-UDP-from-all-persons
                try {
                    ed.setClosed(true);
                    ContextEditor newCtxtEd = new ContextEditor(ctxt);
                    newCtxtEd.setLocation(ctxtEd.getLocation());  //  put new one where old was
                    ctxtEd.setClosed(true);
                    newCtxtEd.desktop = desktop;
                    desktop.add(newCtxtEd);
                    MainPane.curr_CUC_Editor = newCtxtEd;
                    newCtxtEd.show();
                    newCtxtEd.moveToFront();
                    newCtxtEd.setSelected(true);
                    newCtxtEd.miViewMe = menuView.add(newCtxtEd.windowNum);
                    newCtxtEd.miViewMe.addActionListener(newCtxtEd);
                    newCtxtEd.menuView = menuView;
                } catch (PropertyVetoException pve) {
                    if (MainPane.activity == null) {
                        MainPane.createActivityLog(desktop, menuView);
                    }
                    MainPane.activity.log.append("In UDPEditor:\n" + pve);
                    System.out.println("In UDPEditor:\n" + pve);
                }
                return;
            }
            else if (e.getActionCommand().equals("check")) {
                //  Run consistency checks again, just to be sure.
                // CAUTION:  code copied from above.
                boolean errorFlag = false;

                String newName = name_1.getText(), msg,
                       rest = newName.substring(1),
                        test = rest.toLowerCase();
                if (!newName.startsWith("*") || !rest.equals(test) ||
                        newName.length() < 2) {
                    msg = msgs.getString("theName") + newName + 
                            msgs.getString("violatesNameRules");
                    JOptionPane.showMessageDialog(ed, msg, 
                            msgs.getString("namingRulesViolation"),
                            JOptionPane.WARNING_MESSAGE);
                    flipFinalBtn("check");
                    name_1.setText("*");
                    return;
                }
                try {
                    newName = PersonPanel.sanitizeKinTerms(ed, newName.substring(1), 
                            msgs.getString("thisUDPName"));
                }catch(KSParsingErrorException exc) {
                    flipFinalBtn("check");
                    return;
                }
                //  Once past validity-check, we can safely record the new starName
                newStarName = "*" + newName;
                name_1.setText(newStarName);
                
                if (!newUDP && !reName && !newStarName.equals(theUDP.starName)) {  //  name change
                    reName = true;
                } 

                if ((newType.equals("boolean") || newType.equals("individual")) 
                        && newValidEntries != null && newValidEntries.size() > 0) {
                    JOptionPane.showMessageDialog(ed, msgs.getString("whenDataTypeIs") 
                            + newType + msgs.getString("cannotRestrict"),
                            msgs.getString("illegalCombination"),
                            JOptionPane.INFORMATION_MESSAGE);
                    nocertain.setSelected(true);
                    newValidEntries = null;
                    errorFlag = true;
                }  //  re-checked consistency
                
                if ((newValidEntries != null) && (newValidEntries.size() < 1)) {
                    JOptionPane.showMessageDialog(ed, 
                            msgs.getString("restrictButNoList"),
                            msgs.getString("inconsistentInformation"),
                            JOptionPane.WARNING_MESSAGE);
                    errorFlag = true;
                }  //  end of check for Value Restriction with no list of legal values
                if ((newType.equals("individual")) && (newDefaultValue != null)) {
                    JOptionPane.showMessageDialog(ed, 
                            msgs.getString("noDefaultPersons"),
                            msgs.getString("inconsistentInformation"),
                            JOptionPane.WARNING_MESSAGE);
                    errorFlag = true;
                }  //  end of check for a default individual
                if ((newType.equals("individual") || newType.equals("boolean") 
                        || newType.equals("string"))
                        && (newMaxVal != null || newMinVal != null)) {
                    JOptionPane.showMessageDialog(ed, 
                            msgs.getString("improperMaxMin"),
                            msgs.getString("inconsistentInformation"),
                            JOptionPane.WARNING_MESSAGE);
                    errorFlag = true;
                }  //  end of check for min/max val of non-numeric data
                if (!validateDefaultVal()) {
                    errorFlag = true; //  re-check consistency: default value & data type
                }
                if (!validateMinText()) {
                    errorFlag = true;
                }
                if (!validateMaxText()) {
                    errorFlag = true;
                }
                String s;
                if (chartYes.isSelected() && (newSingleValue
                        || ! newType.equals("individual"))) {
                    s = msgs.getString("needMultiPersons");
                    JOptionPane.showMessageDialog(ctxtEd, s, 
                            msgs.getString("illegalCombination"),
                            JOptionPane.ERROR_MESSAGE);
                    chartNo.setSelected(true);
                    colorBox.setVisible(false);
                    errorFlag = true;
                }
                if (chartYes.isSelected() && newChartColor == null) {
                    s = msgs.getString("needColor");
                    JOptionPane.showMessageDialog(ctxtEd, s, 
                            msgs.getString("illegalCombination"),
                            JOptionPane.ERROR_MESSAGE);
                    errorFlag = true;
                }
                if (connectYes.isSelected() && 
                        (!newSingleValue || newValidEntries == null || newValidEntries.isEmpty()
                        || newValidEntries.size() == 1 || !newType.equals("string"))) {
                    msg = msgs.getString("needSingleString");
                    JOptionPane.showMessageDialog(ctxtEd, msg, 
                            msgs.getString("illegalCombination"),
                            JOptionPane.ERROR_MESSAGE);
                    errorFlag = true;
                }
                
                if (!errorFlag) {
                    flipFinalBtn("save");
                }
                return;
            }
            else if (e.getActionCommand().equals("save")) {
                ctxt.saveState = true;
                String oldName = theUDP.starName, 
                       oldType = theUDP.typ;
                boolean retroactive = false, cancellation = false,
                        oldSame = theUDP.sameVal;
                theUDP.starName = newStarName;
                theUDP.typ = newType;
                theUDP.validEntries = newValidEntries;
                theUDP.singleValue = newSingleValue;
                theUDP.defaultValue = newDefaultValue;
                theUDP.minVal = newMinVal;
                theUDP.maxVal = newMaxVal;
                theUDP.chartable = chartYes.isSelected();
                theUDP.chartColor = newChartColor;
                if (!newUDP && newConnects && !theUDP.connects) {
                    retroactive = true;
                }
                if (theUDP.connects && !newConnects) {
                    cancellation = true;
                }
                theUDP.connects = newConnects;
                theUDP.sameVal = newSameVal;
                if (newUDP || reName) {
                    if (ctxt.userDefinedProperties == null) {
                        ctxt.userDefinedProperties = new TreeMap();
                    }
                    ctxt.userDefinedProperties.put(newStarName, theUDP);
                }
                if (reName && oldName != null) {
                    ctxt.userDefinedProperties.remove(oldName);  // Update UDPs
                    if (theUDP.chartable) {                                                
                        ctxt.renameChartableUDP(oldName, newStarName);                        
                    }
                }
                // Now propogate the updated UDPs to each DomainTheory
                if (ctxt.domTheoryRefExists()) {
                    try {
                        DomainTheory dt = ctxt.domTheoryRef();
                        dt.userDefinedProperties = ctxt.userDefinedProperties;
                        if (ctxt.domTheoryAdrExists()) {
                            dt = ctxt.domTheoryAdr();
                            dt.userDefinedProperties = ctxt.userDefinedProperties;
                        }
                    } catch (Exception exc) {  } // No parsing errors expected.
                }
                if (SIL_Edit.edWin != null) {
                    SIL_Edit.edWin.getPPanel().initUDPCombo();
                }
                progBox.setVisible(true);
                UserDefinedProperty oldudp, newInstance;
                Individual ind;
                Iterator indIter = ctxt.individualCensus.iterator();

                if (newUDP) {  //  no possible clashes: just make one for each person
                    while (indIter.hasNext()) {
                        ind = (Individual) indIter.next();
                        newInstance = new UserDefinedProperty(theUDP, true);  //  clone the new template
                        if (ind.userDefinedProperties == null) {
                            ind.userDefinedProperties = new TreeMap();
                        }
                        ind.userDefinedProperties.put(newStarName, newInstance);
                    }  //  end of loop thru individuals
                } else {  //  revised UDP could be inconsistent with some existing instances of old definition
                    if (!oldType.equals(newType)) {  // type clash, instance 'value' fields must be empty
                        while (indIter.hasNext()) {
                            ind = (Individual) indIter.next();
                            if (reName) {
                                ind.userDefinedProperties.remove(oldName);
                            }
                            newInstance = new UserDefinedProperty(theUDP, false);  //  clone the new template
                            ind.userDefinedProperties.put(newStarName, newInstance);
                        }  //  end of loop thru individuals
                    } else if (newSingleValue && (!theUDP.singleValue)) {  // no type clash but arity clash
                        while (indIter.hasNext()) {
                            ind = (Individual) indIter.next();
                            oldudp = (UserDefinedProperty) ind.userDefinedProperties.get(oldName);
                            if (reName) {
                                ind.userDefinedProperties.remove(oldName);
                            }
                            newInstance = new UserDefinedProperty(theUDP, false);  //  clone the new template
                            if (validValsChanged) {  //  check for change in valid entries
                                for (int i = 0; i < oldudp.value.size(); i++) {
                                    if (newValidEntries.contains(oldudp.value.get(i))) {
                                        newInstance.value.add(oldudp.value.get(i));
                                        i = 10000000;  // only add first conforming value
                                    }
                                }
                            } else if (oldudp.value.size() > 0) {
                                newInstance.value.add(oldudp.value.get(0));  //  add first value from old value list
                            }
                            ind.userDefinedProperties.put(newStarName, newInstance);
                        }  //  end of loop thru individuals
                    } else {  // no type clash, no arity clash 
                        while (indIter.hasNext()) {
                            ind = (Individual) indIter.next();
                            oldudp = (UserDefinedProperty) ind.userDefinedProperties.get(oldName);
                            if (reName) {
                                ind.userDefinedProperties.remove(oldName);
                            }
                            newInstance = new UserDefinedProperty(theUDP, false);  //  clone the new template
                            if (validValsChanged) {  //  check for change in valid entries
                                for (int i = 0; i < oldudp.value.size(); i++) {
                                    if (newValidEntries.contains(oldudp.value.get(i))) {
                                        newInstance.value.add(oldudp.value.get(i));
                                    }
                                }
                            } else {
                                newInstance.value = oldudp.value;  //  copy the entire old value list
                            }
                            if ((newInstance.value.size() < 1) && (newDefaultValue != null)) // there was no prior value, but now there's a default
                            {
                                newInstance.value.add(newDefaultValue);
                            }
                            ind.userDefinedProperties.put(newStarName, newInstance);
                        }  //  end of loop thru individuals
                    }
                }
                if (retroactive) {  // retroactive designation of connection
                    ctxt.addConnectionLists(theUDP);
                } else if (cancellation) {
                    UserDefinedProperty udCopy = new UserDefinedProperty(theUDP, false);
                    udCopy.sameVal = oldSame;
                    ctxt.deleteConnectionList(udCopy);
                }
                ArrayList<String> udpsToBeAdded = new ArrayList<String>();
                Iterator udpIter = ctxt.userDefinedProperties.entrySet().iterator();
                while (udpIter.hasNext()) {
                    Map.Entry entry = (Map.Entry)udpIter.next();
                    String udName = (String)entry.getKey();
                    UserDefinedProperty ud = (UserDefinedProperty)entry.getValue();
                    if (ud.chartable && !ctxt.kinTypeOrder.contains(udName)) {
                        udpsToBeAdded.add(udName);
                    }
                }
                if (!udpsToBeAdded.isEmpty()) {
                    boolean mult = udpsToBeAdded.size() > 1;
                    String plural = (mult ? msgs.getString("uDPsWere") : 
                            msgs.getString("uDPWas"));
                    String msg = msgs.getString("newOrRevisedChartable1") + " " 
                            + plural + msgs.getString("newOrRevisedChartable2");
                    JOptionPane.showMessageDialog(ctxtEd, msg, 
                            msgs.getString("automaticPriorityAssignment"),
                            JOptionPane.PLAIN_MESSAGE);
                    for (String udName : udpsToBeAdded) {
                        ctxt.insertAdoptionPriority(udName);
                    }
                }
            Individual indy =  SIL_Edit.edWin.getPPanel().currentInd;
            if (indy != null) {  //  Refresh display with any new properties
                SIL_Edit.edWin.getPPanel().showInfo(indy);
            }
                try {
                    ed.setClosed(true);
                    ContextEditor newCtxtEd = new ContextEditor(ctxt);
                    newCtxtEd.setLocation(ctxtEd.getLocation());  //  put new one where old was
                    ctxtEd.setClosed(true);
                    newCtxtEd.desktop = desktop;
                    desktop.add(newCtxtEd);
                    MainPane.curr_CUC_Editor = newCtxtEd;
                    newCtxtEd.show();
                    newCtxtEd.moveToFront();
                    newCtxtEd.setSelected(true);
                    newCtxtEd.miViewMe = menuView.add(newCtxtEd.windowNum);
                    newCtxtEd.miViewMe.addActionListener(newCtxtEd);
                    newCtxtEd.menuView = menuView;
                } catch (Exception ex) {
                    if (MainPane.activity == null) {
                        MainPane.createActivityLog(desktop, menuView);
                    }
                    MainPane.activity.log.append("While closing UDP editor & re-building a context editor, " + ex + "\n\n");
                }
            }  //  end of action = save
            else if (e.getActionCommand().equals("delete")) {
                String msg = msgs.getString("deleteUDPSure1");
                if (!ctxt.individualCensus.isEmpty()) {
                    msg += msgs.getString("deleteUDPSure2");
                    if (theUDP.chartable) {
                        msg += msgs.getString("deleteUDPSure3");
                    }
                }
                Object[] btns = {se.getString("yes"), se.getString("no")};
                int choice = JOptionPane.showOptionDialog(ed, msg,
                        se.getString("confirm"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null, btns, btns[0]);

                if (choice == JOptionPane.YES_OPTION) {
                    ctxt.saveState = true;
                    ctxt.userDefinedProperties.remove(theUDP.starName);
                    for (Individual ind : ctxt.individualCensus) {
                        ind.userDefinedProperties.remove(theUDP.starName);
                    }
                    if (theUDP.chartable) {
                        ctxt.removeAdoptionPriority(theUDP.starName);
                        ctxt.ktm.removeChartableUDP(theUDP.starName);
                        ctxt.kti.removeChartableUDP(theUDP.starName);
                        SIL_Edit.edWin.rebuildKTMatrixEtc();
                    }
                    try {
                        ed.setClosed(true);
                        ContextEditor newCtxtEd = new ContextEditor(ctxt);
                        newCtxtEd.setLocation(ctxtEd.getLocation());  //  put new one where old was
                        ctxtEd.setClosed(true);
                        newCtxtEd.desktop = desktop;
                        desktop.add(newCtxtEd);
                        MainPane.curr_CUC_Editor = newCtxtEd;
                        newCtxtEd.show();
                        newCtxtEd.moveToFront();
                        newCtxtEd.setSelected(true);
                        newCtxtEd.miViewMe = menuView.add(newCtxtEd.windowNum);
                        newCtxtEd.miViewMe.addActionListener(newCtxtEd);
                        newCtxtEd.menuView = menuView;
                    } catch (Exception ex) {
                        if (MainPane.activity == null) {
                            MainPane.createActivityLog(desktop, menuView);
                        }
                        MainPane.activity.log.append("While closing UDP editor & re-building a context editor, " + ex + "\n\n");
                    }
                }  //  end of confirmed deletion
            }  //  end of action = delete
        }  //  end of ActionListerner method actionPerformed
    }  //  end of inner class UDPListener

    
    class Name1Listener implements DocumentListener {
        
        public void insertUpdate(DocumentEvent e) {
            name1Changed = true;
        }

        public void removeUpdate(DocumentEvent e) {
            name1Changed = true;
        }

        public void changedUpdate(DocumentEvent e) {
            //Plain text components do not fire these events
        }
    }  // end of inner class Name1Listener

    
    class CertainTextListener implements DocumentListener {
        
        public void insertUpdate(DocumentEvent e) {
            certainTextChanged = true;
        }

        public void removeUpdate(DocumentEvent e) {
            certainTextChanged = true;
        }

        public void changedUpdate(DocumentEvent e) {
            //Plain text components do not fire these events
        }
    }  // end of inner class CertainTextListener        defaultTextChanged

    
    class DefaultTextListener implements DocumentListener {
        
        public void insertUpdate(DocumentEvent e) {
            defaultTextChanged = true;
        }

        public void removeUpdate(DocumentEvent e) {
            defaultTextChanged = true;
        }

        public void changedUpdate(DocumentEvent e) {
            //Plain text components do not fire these events
        }
    }  // end of inner class DefaultTextListener
    
}  //  end of class UDPEditor

