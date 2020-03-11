

import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.Dimension;
import java.beans.PropertyVetoException;

/**This class is part of the SILKin program's GUI. It is modeled after the
 * KAES KinshipEditor by Michael D. Fischer of the Centre for Social
 * Anthropology and Computing, University of Kent.
 * Those portions taken from his code are copyrighted by him; all rights
 * reserved. See his complete copyright statement and terms of re-use in
 * e.g. KinshipEditor.java in this package.
 * <p>A PersonPanel displays all the data about a Person and allows the User to
 * edit any of it (except the serial number, of course).
 * </p>
 * <p>
 * DATA POSTING STRATEGY - As soon as any field in this panel loses focus, we
 * post the latest data to the Individual/Person being displayed ("infoPerson").
 * When a new person is chosen for display, we validate all data and if any
 * invalid fields are found, we do not allow the change in infoPerson until
 * valid data is entered. Then the person's record is updated and we proceed to
 * let user choose a new infoPerson to display/edit.
 * </p>
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class PersonPanel extends javax.swing.JPanel {

    public static final int ADD = 2, DELETE = 1, CANCEL = 0;
    public static final String[] reservedWords = { "father", "mother", "parent", 
        "child", "husband", "wife", "spouse", "son", "daughter", "male", "female",
        "elder", "younger", "equal", "not", "divorced", "dead", "gender", 
        "lessThan", "greaterThan", "lessOrEql", "greaterOrEql", "contains", "allowCreation", 
        "sibling", "siblings", "brother", "sister", "half_brother", "half_sister",
        "step_brother", "step_sister", "parents", "step_father", "step_mother", "spice", 
        "children", "step_son", "step_daughter"};
    static MyResBundle se = Library.screenElements,
                     msgs = Library.messages;
    
    /** Creates new form PersonPanel */
    public PersonPanel() {
        storing = true;
        initComponents();
        prepComponents();
        setMaximumSize(new Dimension(910,320));
        setMinimumSize(new Dimension(910,320));
        labelComponents();
        storing = false;
    }
    
    public void prepComponents() {
        personBirthMM.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                personBirthMMFocusLost(evt);
            }
        });
        personComments.getDocument().addDocumentListener(new CommentListener());
        linksLabel.setVisible(false);
        linksComboBox.setVisible(false);
        chartLabel.setVisible(false);
        chartField.setVisible(false);
        parent = SIL_Edit.edWin;
        alterID.setColumns(3);
        alterRefTerm.setEditable(false);
        recipRefTerm.setEditable(false);
        egoChoiceModel = (DefaultComboBoxModel)egoChoiceBox.getModel();
        egoChoiceModel.removeAllElements();  //  ready for first person
        linksComboModel = (DefaultComboBoxModel)linksComboBox.getModel();
        linksComboModel.removeAllElements();  //  ready for first person
        udpComboModel = (DefaultComboBoxModel)udpComboBox.getModel();
        initUDPCombo();        
    }

    SIL_Edit parent = null;  //  The container holding this JPanel.
    DefaultComboBoxModel egoChoiceModel, linksComboModel, udpComboModel;

    boolean dirty = false;  //  This 'dirty bit' applies only to the current
                            //  infoPerson.
    boolean storing = false;  // true when storing info on Person
    boolean udpsPresent = false;
    
    static String alterKinTermRefImg, alterKinTermAdrImg, recipKinTermRefImg, recipKinTermAdrImg;
    JTextField[] focusFields;
    Individual currentInd = null;

    void buildFocusFields() {
        int size = 3, ndx = 0;
        if (Context.current.surnameNormallyCaptured) size++;
        if (Context.current.birthDateNormallyCaptured) size++;
        if (parent.chart.distinctAdrTerms) size += 2;
        focusFields = new JTextField[size];
        focusFields[ndx++] = alterFirstNames;
        if (Context.current.surnameNormallyCaptured) {
            focusFields[ndx++] = alterLastName;
        }
        if (Context.current.birthDateNormallyCaptured) {
            focusFields[ndx++] = personBirthYr;
        }
        focusFields[ndx++] = alterRefTerm;
        focusFields[ndx++] = recipRefTerm;
        if (parent.chart.distinctAdrTerms) {
            focusFields[ndx++] = alterAdrTerm;
            focusFields[ndx++] = recipAdrTerm;
        }
    }
    
    /**If any UDPs are defined in this context, then build a ComboBox User so can
     * pick which UDP to display in the Detail Display for an Individual.  
     */
    public void initUDPCombo() {
        boolean vis = true, chartable = false;
        udpValTextArea.setEditable(false); // Display only.
        if (Context.current == null
                || Context.current.userDefinedProperties == null
                || Context.current.userDefinedProperties.size() == 0) {
            vis = false;
        } else {
            parent.chart.loading = true;
            udpComboModel.removeAllElements();
            for (Object o : Context.current.userDefinedProperties.keySet()) {
                udpComboModel.addElement((String) o);
            }
            udpComboBox.setSelectedIndex(0);
            udpValTextArea.setText("");
            udpsPresent = true;
            String udNam = (String) udpComboModel.getElementAt(0);
            UserDefinedProperty udp = (UserDefinedProperty) Context.current.userDefinedProperties.get(udNam);
            chartable = udp.chartable;
            parent.chart.loading = false;
        }
        udpLabel.setVisible(vis);
        udpComboBox.setVisible(vis);
        udpScrollPane.setVisible(vis);
        udpEditButton.setVisible(vis);
        udpEditButton.setEnabled(!chartable);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        alterFirstNames = new javax.swing.JTextField();
        alterLastName = new javax.swing.JTextField();
        egoPanel = new javax.swing.JPanel();
        egoChoiceBox = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        alterID = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        personBirthMM = new javax.swing.JTextField();
        personBirthYr = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        personDeathYear = new javax.swing.JTextField();
        personDeathMon = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        alterRefTerm = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        recipRefTerm = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        alterAdrTerm = new javax.swing.JTextField();
        recipAdrTerm = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        personComments = new javax.swing.JTextPane();
        bornYrLabel = new javax.swing.JLabel();
        personBirthDD = new javax.swing.JTextField();
        personDeathDD = new javax.swing.JTextField();
        dataChgDateLabel = new javax.swing.JLabel();
        dataChgDate = new javax.swing.JTextField();
        chartLabel = new javax.swing.JLabel();
        chartField = new javax.swing.JTextField();
        linksLabel = new javax.swing.JLabel();
        linksComboBox = new javax.swing.JComboBox();
        udpLabel = new javax.swing.JLabel();
        udpComboBox = new javax.swing.JComboBox();
        udpScrollPane = new javax.swing.JScrollPane();
        udpValTextArea = new javax.swing.JTextArea();
        udpEditButton = new javax.swing.JButton();

        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current Alter", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(0, 0, 204)));
        setMaximumSize(new java.awt.Dimension(907, 309));
        setMinimumSize(new java.awt.Dimension(907, 309));

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Name");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

        alterFirstNames.setEditable(false);
        alterFirstNames.setText("first names");
        alterFirstNames.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                alterFirstNamesFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                alterFirstNamesFocusLost(evt);
            }
        });

        alterLastName.setEditable(false);
        alterLastName.setText("last name");
        alterLastName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                alterLastNameFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                alterLastNameFocusLost(evt);
            }
        });

        egoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Current Ego", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 1, 10), new java.awt.Color(255, 0, 51))); // NOI18N

        egoChoiceBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        egoChoiceBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                egoChoiceBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout egoPanelLayout = new org.jdesktop.layout.GroupLayout(egoPanel);
        egoPanel.setLayout(egoPanelLayout);
        egoPanelLayout.setHorizontalGroup(
            egoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(egoPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(egoChoiceBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 288, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        egoPanelLayout.setVerticalGroup(
            egoPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(egoChoiceBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
        );

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("ID#");

        alterID.setEditable(false);
        alterID.setText("nnn");
        alterID.setMinimumSize(new java.awt.Dimension(100, 28));

        jLabel8.setText("Born:");

        personBirthMM.setEditable(false);
        personBirthMM.setColumns(2);
        personBirthMM.setText("MM");
        personBirthMM.setMaximumSize(new java.awt.Dimension(64, 28));
        personBirthMM.setMinimumSize(new java.awt.Dimension(64, 28));
        personBirthMM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                personBirthMMActionPerformed(evt);
            }
        });

        personBirthYr.setEditable(false);
        personBirthYr.setColumns(4);
        personBirthYr.setText("YYYY");
        personBirthYr.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                personBirthYrFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                personBirthYrFocusLost(evt);
            }
        });

        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel12.setText("Notes on Alter");

        personDeathYear.setEditable(false);
        personDeathYear.setColumns(4);
        personDeathYear.setText("YYYY");
        personDeathYear.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                personDeathYearFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                personDeathYearFocusLost(evt);
            }
        });

        personDeathMon.setEditable(false);
        personDeathMon.setColumns(2);
        personDeathMon.setText("MM");
        personDeathMon.setMaximumSize(new java.awt.Dimension(73, 28));
        personDeathMon.setMinimumSize(new java.awt.Dimension(64, 28));

        jLabel9.setText("Died:");

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Ego refers to Alter:");

        alterRefTerm.setEditable(false);
        alterRefTerm.setText("kin term");
        alterRefTerm.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                alterRefTermFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                alterRefTermFocusLost(evt);
            }
        });

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Alter refers to Ego:");

        recipRefTerm.setEditable(false);
        recipRefTerm.setText("reciprocal kin term");
        recipRefTerm.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                recipRefTermFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                recipRefTermFocusLost(evt);
            }
        });

        jLabel2.setText("Ego addresses Alter:");

        jLabel3.setText("Alter addresses Ego:");

        alterAdrTerm.setEditable(false);
        alterAdrTerm.setText("kin term of address");
        alterAdrTerm.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                alterAdrTermFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                alterAdrTermFocusLost(evt);
            }
        });

        recipAdrTerm.setEditable(false);
        recipAdrTerm.setText("reciprocal term of address");
        recipAdrTerm.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                recipAdrTermFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                recipAdrTermFocusLost(evt);
            }
        });

        jScrollPane1.setMaximumSize(new java.awt.Dimension(240, 200));
        jScrollPane1.setViewportView(personComments);

        bornYrLabel.setText("Yr");

        personBirthDD.setColumns(2);
        personBirthDD.setText("DD");
        personBirthDD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                personBirthDDActionPerformed(evt);
            }
        });
        personBirthDD.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                personBirthDDFocusGained(evt);
            }
        });

        personDeathDD.setColumns(2);
        personDeathDD.setText("DD");
        personDeathDD.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                personDeathDDFocusGained(evt);
            }
        });

        dataChgDateLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        dataChgDateLabel.setText("Last Data Change: ");

        dataChgDate.setEditable(false);

        chartLabel.setText("Chart:");

        chartField.setText("A   ");

        linksLabel.setText("Links:");

        linksComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        linksComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linksComboBoxActionPerformed(evt);
            }
        });

        udpLabel.setText("User-Defined Properties:");

        udpComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        udpComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                udpComboBoxActionPerformed(evt);
            }
        });

        udpScrollPane.setMinimumSize(new java.awt.Dimension(23, 50));

        udpValTextArea.setColumns(20);
        udpValTextArea.setRows(5);
        udpScrollPane.setViewportView(udpValTextArea);

        udpEditButton.setText("Edit UDP Value");
        udpEditButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                udpEditButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(21, 21, 21)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(layout.createSequentialGroup()
                                .add(jLabel5)
                                .add(2, 2, 2)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .add(alterID, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(19, 19, 19)
                                        .add(jLabel8))
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel9))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(bornYrLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(personDeathYear)
                                    .add(personBirthYr))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                    .add(personDeathMon, 0, 1, Short.MAX_VALUE)
                                    .add(personBirthMM, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, Short.MAX_VALUE)))
                            .add(layout.createSequentialGroup()
                                .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 97, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(alterFirstNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(personBirthDD, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(39, 39, 39)
                                .add(chartLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(chartField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(127, 127, 127)
                                .add(linksLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(linksComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(personDeathDD, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(39, 39, 39)
                                .add(dataChgDateLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 273, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(dataChgDate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 174, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(alterLastName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 119, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(99, 99, 99)
                                .add(egoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(34, 34, 34)
                                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel10)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel11)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel2)
                                            .add(org.jdesktop.layout.GroupLayout.TRAILING, jLabel3)))
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .addContainerGap()
                                        .add(udpLabel)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(recipAdrTerm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 346, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(alterAdrTerm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 346, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(recipRefTerm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 346, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(alterRefTerm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 346, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(6, 6, 6)
                                        .add(udpComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 263, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                            .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(udpEditButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(udpScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 340, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(32, 32, 32)
                                .add(jLabel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 259, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(layout.createSequentialGroup()
                                .add(18, 18, 18)
                                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 304, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(59, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(11, 11, 11)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(alterFirstNames, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(alterLastName)
                            .add(jLabel1)))
                    .add(egoPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel5)
                        .add(alterID, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(jLabel8)
                        .add(bornYrLabel)
                        .add(personBirthYr, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(personBirthMM, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(personBirthDD, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(chartLabel)
                        .add(chartField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(linksLabel)
                            .add(linksComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabel9)
                        .add(personDeathYear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(personDeathMon, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(personDeathDD, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .add(6, 6, 6)
                            .add(dataChgDateLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 24, Short.MAX_VALUE))
                        .add(dataChgDate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(20, 20, 20)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(alterRefTerm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel10))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(recipRefTerm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel11))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(alterAdrTerm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel2))
                        .add(12, 12, 12)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(recipAdrTerm, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(jLabel3))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(udpComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .add(udpLabel))
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(16, 16, 16)
                                .add(udpEditButton)
                                .addContainerGap(33, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(udpScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addContainerGap())))
                    .add(layout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(jLabel12)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(1, 1, 1))))
        );
    }// </editor-fold>//GEN-END:initComponents

    void labelComponents() {
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, 
                se.getString("currentAlter"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, null, 
                new java.awt.Color(0, 0, 204)));
        jLabel1.setText(se.getString("name"));
        alterFirstNames.setText(se.getString("firstNames"));
        alterLastName.setText(se.getString("lastName"));
        egoPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, 
                se.getString("currentEgo"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, 
                javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new java.awt.Font("Lucida Grande", 1, 10), 
                new java.awt.Color(255, 0, 51))); // NOI18N
        jLabel8.setText(se.getString("born"));
        personBirthMM.setText(se.getString("mm"));
        personBirthYr.setText(se.getString("yyyy"));
        jLabel12.setText(se.getString("notesOnAlter"));
        personDeathYear.setText(se.getString("yyyy"));
        personDeathMon.setText(se.getString("mm"));
        jLabel9.setText(se.getString("died"));
        jLabel10.setText(se.getString("egoRefers2Alter"));
        alterRefTerm.setText(se.getString("kinTerm"));
        recipRefTerm.setText(se.getString("reciprocalKinTerm"));
        jLabel2.setText(se.getString("egoAddressesAlter:"));
        jLabel3.setText(se.getString("alterAddressesEgo:"));
        alterAdrTerm.setText(se.getString("kinTermAddress"));  
        recipAdrTerm.setText(se.getString("recipKinTermAddress"));
        bornYrLabel.setText(se.getString("year"));
        personBirthDD.setText(se.getString("dd"));
        personDeathDD.setText(se.getString("dd"));
        dataChgDateLabel.setText(se.getString("lastDataChange:") + " ");
        chartLabel.setText(se.getString("chart"));
        linksLabel.setText(se.getString("links"));        
        udpLabel.setText(se.getString("userDefinedProperties"));
        udpEditButton.setText(se.getString("editUDPValue"));        
    }
    
//  DATA CAPTURE STRATEGY
//  This panel's 'dirty bit' records whether any data has been entered on the
//  panel but not yet stored on the Family/Marriage object.
//  The KinEditPanel2's dirty bit, by contrast, records whether any objects
//  have been updated but not yet saved to disk.
//  The panel dirty bit is turned on whenever an editable field gains the
//  focus. If that field later loses the focus, a data store is triggered.
//  But if the user takes some other action that does NOT fire a Focus_Lost
//  event, the dirty bit will prevent loss of data.

    public void fireAlterFirstNamesFocusLost() {
        alterFirstNamesFocusLost(null);
    }
    
    private void alterFirstNamesFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_alterFirstNamesFocusLost
        String s = alterFirstNames.getText();
        if (parent.infoPerson != null) {
            if (parent.infoPerson.firstNames == null || ! parent.infoPerson.firstNames.equals(s)) {
                parent.infoPerson.firstNames = s;
                parent.infoPerson.updateNames();
                dirty = true;
            }
        }
    }//GEN-LAST:event_alterFirstNamesFocusLost

    private void alterLastNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_alterLastNameFocusLost
        String s = alterLastName.getText();
        if (parent.infoPerson != null) {
            if (parent.infoPerson.surname == null || ! parent.infoPerson.surname.equals(s)) {
                parent.infoPerson.surname = s;
                parent.infoPerson.updateNames();
                dirty = true;
            }
        }
    }//GEN-LAST:event_alterLastNameFocusLost

    private void personBirthYrFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_personBirthYrFocusLost
        //  Entries are not validated until a new Person/Family is clicked.
        String s =  personBirthYr.getText();
        if (parent.infoPerson != null) {
            if (parent.infoPerson.birthYY == null || ! parent.infoPerson.birthYY.equals(s)) {
                parent.infoPerson.birthYY = s;
                dirty = true;
                if (parent.infoPerson.birthFamily != null) {
                    parent.infoPerson.birthFamily.computeBirthGrps();
                    parent.chart.repaint();
                }                
            }
        }
    }//GEN-LAST:event_personBirthYrFocusLost

    
    private void personBirthMMFocusLost(java.awt.event.FocusEvent evt) {                                        
        //  Entries are not validated until a new Person/Family is clicked.
        String s =  personBirthMM.getText();
        if (parent.infoPerson != null) {
            if (parent.infoPerson.getBirthMM() == null || ! parent.infoPerson.getBirthMM().equals(s)) {
                parent.infoPerson.setBirthMM(s);
                dirty = true;
                if (parent.infoPerson.birthFamily != null) {
                    parent.infoPerson.birthFamily.computeBirthGrps();
                    parent.chart.repaint();
                }                
            }
        }
    }                                       

    private void personDeathYearFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_personDeathYearFocusLost
        //  Entries are not validated until a new Person/Family is clicked.
        String s =  personDeathYear.getText();
        if (parent.infoPerson != null) {
            if (parent.infoPerson.deathYY == null || ! parent.infoPerson.deathYY.equals(s)) {
                parent.infoPerson.deathYY = s;
                dirty = true;
            }
        }
    }//GEN-LAST:event_personDeathYearFocusLost

    private void alterRefTermFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_alterRefTermFocusLost
        String newTerm = alterRefTerm.getText();
        if (newTerm.isEmpty()) {
            return;
        }
        if (currentInd != null && !newTerm.equals("Ego") && 
                !newTerm.equals(se.getString("notLinked2Ego"))) {
            if (Character.isUpperCase(newTerm.charAt(0))
                    || Character.isDigit(newTerm.charAt(0))
                    || newTerm.startsWith("\"")) {
                String msg = "'" + newTerm + se.getString("notLegalKinTerm");
                MainPane.displayError(msg, se.getString("inEgoCallsAlter"), 
                        JOptionPane.ERROR_MESSAGE);
                alterRefTerm.grabFocus();
                return;
            }
            JTextField temp = new JTextField();
            fillTextField(currentInd.node, temp, false);
            if (!temp.getText().equals(newTerm)) {
                dirty = true;
            }
        }
    }//GEN-LAST:event_alterRefTermFocusLost

    private void recipRefTermFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_recipRefTermFocusLost
        String newTerm = recipRefTerm.getText();
        if (newTerm.isEmpty()) {
            return;
        }
        if (currentInd != null && !newTerm.equals("Ego") && 
                !newTerm.equals(se.getString("notLinked2Ego"))) {
            TreeMap tmap = parent.ktm.getRow(currentInd.serialNmbr);
            Node altNode = null;
            if (tmap != null) {
                altNode = (Node) tmap.get(parent.getCurrentEgo());
            }
            if (altNode != null) {
                if (Character.isUpperCase(newTerm.charAt(0))
                    || Character.isDigit(newTerm.charAt(0))
                    || newTerm.startsWith("\"")) {
                String msg = "'" + newTerm + se.getString("notLegalKinTerm");
                MainPane.displayError(msg, se.getString("inEgoCallsAlter"), 
                        JOptionPane.ERROR_MESSAGE);
                recipRefTerm.grabFocus();
                return;
            }
                JTextField temp = new JTextField();
                fillTextField(altNode, temp, false);
                if (!temp.getText().equals(newTerm)) {
                    dirty = true;
                }
            }
        }
    }//GEN-LAST:event_recipRefTermFocusLost

    private void alterFirstNamesFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_alterFirstNamesFocusGained
//        dirty = true;
    }//GEN-LAST:event_alterFirstNamesFocusGained

    private void alterLastNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_alterLastNameFocusGained
//        dirty = true;
    }//GEN-LAST:event_alterLastNameFocusGained

    private void personBirthYrFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_personBirthYrFocusGained
//        dirty = true;
    }//GEN-LAST:event_personBirthYrFocusGained

    private void personDeathYearFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_personDeathYearFocusGained
//        dirty = true;
    }//GEN-LAST:event_personDeathYearFocusGained

    private void alterRefTermFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_alterRefTermFocusGained
//        dirty = true;
    }//GEN-LAST:event_alterRefTermFocusGained

    private void recipRefTermFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_recipRefTermFocusGained
//        dirty = true;
    }//GEN-LAST:event_recipRefTermFocusGained

    private void egoChoiceBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_egoChoiceBoxActionPerformed
        if (! storing) {
            parent.changeEgo(egoChoiceBox.getSelectedIndex());
        }
    }//GEN-LAST:event_egoChoiceBoxActionPerformed

    private void alterAdrTermFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_alterAdrTermFocusGained
//        dirty = true;
    }//GEN-LAST:event_alterAdrTermFocusGained

    private void recipAdrTermFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_recipAdrTermFocusGained
//        dirty = true;
    }//GEN-LAST:event_recipAdrTermFocusGained

    private void personBirthMMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_personBirthMMActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_personBirthMMActionPerformed

    private void personBirthDDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_personBirthDDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_personBirthDDActionPerformed

    private void personDeathDDFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_personDeathDDFocusGained
//        dirty = true;
    }//GEN-LAST:event_personDeathDDFocusGained

    private void personBirthDDFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_personBirthDDFocusGained
//        dirty = true;
    }//GEN-LAST:event_personBirthDDFocusGained

    private void linksComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linksComboBoxActionPerformed
        if (storing) {
            return;
        }
        String ch = (String) linksComboBox.getSelectedItem();
        if (ch != null) {
            Context.current.currentChart = ch.substring(0, ch.indexOf(":"));
            if (parent.infoPerson != null) {
                parent.chart.whichFolk = parent.infoPerson.serialNmbr;
            }
            parent.chartComboUpdateTitle();
            parent.chart.checkSizeOfChart(Context.current);
            parent.chart.resizeAndRepaint();
        }
    }//GEN-LAST:event_linksComboBoxActionPerformed

    private void udpComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_udpComboBoxActionPerformed
        if (parent.chart.loading) {
            return;
        }
        displayUDPVals(currentInd);
    }//GEN-LAST:event_udpComboBoxActionPerformed

    private void udpEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_udpEditButtonActionPerformed
        String udNam = (String) udpComboBox.getSelectedItem();
        UserDefinedProperty udp = (UserDefinedProperty) parent.infoPerson.userDefinedProperties.get(udNam);
        String[] choices = { se.getString("cancel"), 
            se.getString("delete"), se.getString("add") };
        String newbie, msg = se.getString("aDDValueOrDELETE");
        int choice = JOptionPane.showOptionDialog(this, msg, se.getString("typeOfEdit"),
                JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, 
                null, choices, choices[2]);
        if (choice == CANCEL) {
            return;
        }
        if (choice == DELETE && udp.value.isEmpty()) {
            JOptionPane.showMessageDialog(this, se.getString("noValues2Delete"));
            return;
        }
        if (choice == ADD && udp.singleValue && udp.value.size() > 0) {
            msg = se.getString("singleValueAlreadyHasOne");
            JOptionPane.showMessageDialog(this, msg);
            return;
        }
        if (udp.typ.equals("individual")) {
            Individual ind, tempInd;
            ArrayList<Individual> census = Context.current.individualCensus;
            if (choice == DELETE) { 
                if (udp.value.size() == 1) {
                    ind = (Individual) udp.value.get(0);
                } else { // must choose value to delete
                    String[] people = new String[udp.value.size()];
                    for (int i = 0; i < udp.value.size(); i++) {
                        tempInd = (Individual) udp.value.get(i);
                        people[i] = tempInd.homeChart + ": " + tempInd.name + " <" + tempInd.serialNmbr + ">";
                    }
                    msg = se.getString("choosePerson2DELETE") + " " + udp.starName;
                    String corpse = (String) JOptionPane.showInputDialog(this,
                            msg, se.getString("deletingAValue"),
                            JOptionPane.PLAIN_MESSAGE, null, people, people[0]);
                    if (corpse == null) {
                        return;
                    }
                    int serial = Integer.parseInt(corpse.substring(corpse.indexOf("<") + 1, corpse.indexOf(">")));
                    ind = census.get(serial);
                }
                Object[] btns = {se.getString("yes"), se.getString("no")};
                msg = se.getString("delete") + " " + ind.homeChart + ": "
                        + ind.name + " <" + ind.serialNmbr + se.getString("right?");
                int confirm = JOptionPane.showOptionDialog(this, msg,
                        se.getString("confirmDeletion"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null, btns, btns[0]);
                if (confirm == JOptionPane.YES_OPTION) {
                    udp.value.remove(ind);
                    parent.showInfo(parent.infoPerson);
                }
            } else { //  choice is ADD
                ArrayList<String> peopleList = new ArrayList<String>();
                for (Individual tempI : census) {  //  filter out deleted persons
                    if (!tempI.deleted) {
                        peopleList.add(tempI.homeChart + ": " + tempI.name + " <" + tempI.serialNmbr + ">");
                    }
                }
                String[] people = new String[peopleList.size()];
                for (int i=0; i < peopleList.size(); i++) {
                    people[i] = peopleList.get(i);
                }
                
                msg = se.getString("choosePerson2ADD") + " " + udp.starName;
                newbie = (String) JOptionPane.showInputDialog(this, msg, 
                        se.getString("addingAValue"),
                        JOptionPane.PLAIN_MESSAGE, null, people, people[0]);
                if (newbie == null) {
                    return;
                }
                int serial = Integer.parseInt(newbie.substring(newbie.indexOf("<") + 1, newbie.indexOf(">")));
                ind = census.get(serial);
                udp.value.add(ind);
                parent.showInfo(parent.infoPerson);
            }
        } else {  // it's integer, float, boolean, or string
            if (choice == DELETE) {
                String val;
                int index;
                if (udp.value.size() == 1) {
                    val = udp.value.get(0).toString();
                    index = 0;
                } else { // must choose value to delete
                    String[] vals = new String[udp.value.size()];
                    for (int i = 0; i < udp.value.size(); i++) {
                        vals[i] = udp.value.get(i).toString();
                    }
                    msg = se.getString("chooseValue2DELETE") + " " + udp.starName;
                    val = (String) JOptionPane.showInputDialog(this, msg, 
                            se.getString("deletingAValue"),
                            JOptionPane.PLAIN_MESSAGE, null, vals, vals[0]);
                    if (val == null) {
                        return;
                    }
                    for (index = 0; index < vals.length; index++) {
                        if (val.equals(vals[index])) {
                            break;
                        }
                    }
                }
                msg = se.getString("deleteTheValue") + val + se.getString("right?");
                Object[] btns = {se.getString("yes"), se.getString("no")};
                int confirm = JOptionPane.showOptionDialog(this, msg,
                        se.getString("confirmDeletion"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null, btns, btns[0]);                
                if (confirm == JOptionPane.YES_OPTION) {  //  Go ahead and delete it
                    udp.value.remove(index);
                    if (udp.connects) {
                        Context.current.deleteConnectingUDPVal(currentInd, udp.starName, val, udp.sameVal);
                    }
                }
            } else {  //  choice = ADD
                Object newObj;
                if (udp.validEntries != null && !udp.validEntries.isEmpty()) {
                    String[] valids = udp.getValidEntryArray();
                    msg = se.getString("chooseNewValue");
                    newbie = (String) JOptionPane.showInputDialog(this, msg, 
                            se.getString("addingAValue"),
                            JOptionPane.PLAIN_MESSAGE, null, valids, valids[0]);
                } else {
                    msg = se.getString("enterNewValue") + " " + udp.starName;
                    newbie = JOptionPane.showInputDialog(this, msg,
                            se.getString("newValue"), JOptionPane.PLAIN_MESSAGE);
                }
                if (newbie == null) {
                    return;
                }
                try {
                    if (udp.typ.equals("integer")) {
                        newObj = new Integer(newbie);
                    } else if (udp.typ.equals("float")) {
                        newObj = new Float(newbie);
                    } else if (udp.typ.equals("boolean")) {
                        newObj = new Boolean(newbie);
                    } else {
                        newObj = newbie;
                    }
                } catch (Exception exc) {
                    msg = se.getString("couldNotInterpretAs") + " " + udp.typ
                            + ".\n" + exc;
                    JOptionPane.showMessageDialog(this, msg,
                            se.getString("invalidInput"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (udp.connects) {
                    Context.current.addConnectingUDPVal(currentInd, udp.starName, newbie, udp.sameVal);
                }
                udp.value.add(newObj);                
            }
            // Whether ADD or DELETE, must ck for changes in kin types
            if (udp.connects) { 
                parent.rebuildKTMatrixEtc();
            }
            parent.showInfo(parent.infoPerson);
        }
    }//GEN-LAST:event_udpEditButtonActionPerformed

    private void alterAdrTermFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_alterAdrTermFocusLost
        String newTerm = alterAdrTerm.getText();
        if (newTerm.isEmpty()) {
            return;
        }
        if (currentInd != null) {
            if (Character.isUpperCase(newTerm.charAt(0))
                    || Character.isDigit(newTerm.charAt(0))
                    || newTerm.startsWith("\"")) {
                String msg = "'" + newTerm + se.getString("notLegalKinTerm");
                MainPane.displayError(msg, se.getString("inEgoCallsAlter"), 
                        JOptionPane.ERROR_MESSAGE);
                alterAdrTerm.grabFocus();
                return;
            }
            JTextField temp = new JTextField();
            fillTextField(currentInd.node, temp, true);
            if (!temp.getText().equals(newTerm)) {
                dirty = true;
            }
        }  
    }//GEN-LAST:event_alterAdrTermFocusLost

    private void recipAdrTermFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_recipAdrTermFocusLost
        String newTerm = recipAdrTerm.getText();
        if (newTerm.isEmpty()) {
            return;
        }
        if (currentInd != null) {
            TreeMap tmap = parent.ktm.getRow(currentInd.serialNmbr);
            Node altNode = null;
            if (tmap != null) {
                altNode = (Node) tmap.get(parent.getCurrentEgo());
            }
            if (altNode != null) {
                if (Character.isUpperCase(newTerm.charAt(0))
                        || Character.isDigit(newTerm.charAt(0))
                        || newTerm.startsWith("\"")) {
                    String msg = "'" + newTerm + se.getString("notLegalKinTerm");
                    MainPane.displayError(msg, se.getString("inEgoCallsAlter"), 
                        JOptionPane.ERROR_MESSAGE);
                    recipAdrTerm.grabFocus();
                    return;
                }
                JTextField temp = new JTextField();
                fillTextField(altNode, temp, true);
                if (!temp.getText().equals(newTerm)) {
                    dirty = true;
                }
            }
        }
    }//GEN-LAST:event_recipAdrTermFocusLost

    
    void clearInfo() {
        storing = true;
        alterFirstNames.setText("");
        alterLastName.setText("");
        alterID.setText("");
        personBirthDD.setText("");
        personDeathDD.setText("");
        personBirthMM.setText("");
        personDeathMon.setText("");
        personBirthYr.setText("");
        personDeathYear.setText("");
        dataChgDate.setText("");
        personComments.setText("");
        alterRefTerm.setText("");
        recipRefTerm.setText("");
        alterAdrTerm.setText("");
        recipAdrTerm.setText("");
        alterRefTerm.setEditable(false);
        recipRefTerm.setEditable(false);
        alterAdrTerm.setEditable(false);
        recipAdrTerm.setEditable(false);
        alterFirstNames.setEditable(false);
        alterLastName.setEditable(false);
        personBirthMM.setEditable(false);
        personDeathMon.setEditable(false);
        personBirthYr.setEditable(false);
        personDeathYear.setEditable(false);
        personComments.setEditable(false);
        linksLabel.setVisible(false);
        linksComboBox.setVisible(false);
        chartLabel.setVisible(false);
        chartField.setVisible(false);
        storing = false;
    }

    public void displayClues() {
        storing = true;
        alterFirstNames.setText(se.getString("firstNames"));
        alterLastName.setText(se.getString("lastName"));
        alterID.setText("nnn");
        personBirthDD.setText(se.getString("dd"));
        personDeathDD.setText(se.getString("dd"));
        personBirthMM.setText(se.getString("mm"));
        personDeathMon.setText(se.getString("mm"));
        personBirthYr.setText(se.getString("yyyy"));
        personDeathYear.setText(se.getString("yyyy"));
        dataChgDate.setText(se.getString("entryDate"));
        personComments.setText("");
        alterRefTerm.setText(se.getString("kinTerm"));
        recipRefTerm.setText(se.getString("reciprocalKinTerm"));
        alterAdrTerm.setText(se.getString("kinTermAddress"));
        recipAdrTerm.setText(se.getString("recipKinTermAddress"));
        linksLabel.setVisible(false);
        linksComboBox.setVisible(false);
        chartLabel.setVisible(false);
        chartField.setVisible(false);
        if (udpsPresent) {
            udpValTextArea.setText(se.getString("value(s)4PersonUDPs"));
        }
        storing = false;
    }

    /** Set local flags, then load the data from ind's object into the
     * proper fields of the Detail Display. Reformat the comments for
     * display. Names were format-checked when User entered them.
     * 
     * @param ind the person whose info we want to show
     */
    public void showInfo(Individual ind) {
        storing = true;
        parent.loadingCharts = false;
        currentInd = ind;
        if (focusFields == null) {
            buildFocusFields();
        }
        alterFirstNames.setText(ind.firstNames);
        alterLastName.setText(ind.surname);
        alterID.setText(String.valueOf(ind.serialNmbr));
        personBirthDD.setText(ind.getBirthDD());
        personDeathDD.setText(ind.getDeathDD());
        personBirthMM.setText(ind.getBirthMM());
        personDeathMon.setText(ind.getDeathMM());
        personBirthYr.setText(ind.getBirthYr());
        personDeathYear.setText(ind.getDeathYr());
        dataChgDate.setText(ind.dataChangeDate);
        personComments.setText(restoreLineBreaks(ind.comment));
        if (Context.current.displayGEDCOM) {
            String items = ind.addGEDCOMItems();
            personComments.setText(personComments.getText() + items);
        }
        alterFirstNames.setEditable(true);
        alterLastName.setEditable(true);
        personBirthMM.setEditable(true);
        personDeathMon.setEditable(true);
        personBirthYr.setEditable(true);
        personDeathYear.setEditable(true);
        chartField.setText(ind.homeChart);
        chartField.setEditable(false);
        chartLabel.setVisible(true);
        chartField.setVisible(true);
        if (ind.links == null || ind.links.isEmpty()) {
            linksLabel.setVisible(false);
            linksComboBox.setVisible(false);
        }else {
            linksLabel.setVisible(true);
            rebuildLinksBox(ind);
            linksComboBox.setVisible(true);
        }
        personComments.setEditable(true);
        if (ind.serialNmbr == parent.getCurrentEgo()) {
            alterRefTerm.setText("Ego");
            recipRefTerm.setText("Ego");
            alterAdrTerm.setText("Ego");
            recipAdrTerm.setText("Ego");
            alterRefTerm.setEditable(false);
            recipRefTerm.setEditable(false);
            alterAdrTerm.setEditable(false);
            recipAdrTerm.setEditable(false);
        } else if (ind.node != null) {
        // There may be kin terms already recorded.
        // Terms on nodes are slashified. fillTextField de-slashifies. 
            Individual ego = Context.current.individualCensus.get(parent.getCurrentEgo());
            checkForAutoDefs(ind.node, ego);
            fillTextField(ind.node, alterRefTerm, false);
            fillTextField(ind.node, alterAdrTerm, parent.chart.distinctAdrTerms);
            TreeMap tmap = parent.ktm.getRow(ind.serialNmbr);
            Node altNode =  null;
            if (tmap != null) {
                altNode = (Node) tmap.get(parent.getCurrentEgo());
            }
            if (altNode != null) {
                checkForAutoDefs(altNode, ind);
                fillTextField(altNode, recipRefTerm, false);
                fillTextField(altNode, recipAdrTerm, parent.chart.distinctAdrTerms);
            }else {
                recipRefTerm.setText("");
                recipAdrTerm.setText("");
            }
            alterRefTerm.setEditable(true);
            recipRefTerm.setEditable(true);
            alterAdrTerm.setEditable(parent.chart.distinctAdrTerms);
            recipAdrTerm.setEditable(parent.chart.distinctAdrTerms);

        } else {
        //  No node means not yet connected
            alterRefTerm.setText(se.getString("notLinked2Ego"));
            recipRefTerm.setText(se.getString("notLinked2Ego"));
            alterAdrTerm.setText(se.getString("notLinked2Ego"));
            recipAdrTerm.setText(se.getString("notLinked2Ego"));
            alterRefTerm.setEditable(false);
            recipRefTerm.setEditable(false);
            alterAdrTerm.setEditable(false);
            recipAdrTerm.setEditable(false);
        } // store images of 4 boxes before any User edits
        alterKinTermRefImg = alterRefTerm.getText();
        alterKinTermAdrImg = alterAdrTerm.getText();
        recipKinTermRefImg = recipRefTerm.getText();
        recipKinTermAdrImg = recipAdrTerm.getText();
        if (udpsPresent && ind.userDefinedProperties != null) {
            displayUDPVals(ind);
        }
        for (JTextField fld : focusFields) {
            if (fld.getText().equals("")) {
                fld.grabFocus();
                break;
            }
        }
        storing = false;
    }

    /**For the particular UDP shown in the UDP Combo Box, pull the current
     * value(s) from the Individual's object, and display them in the UDP area.
     * 
     * This method can be called from the ContextEditor, and there could be no
     * UDP's defined for this context. So we check for null first.
     * 
     * @param ind the Person whose UDPs we want to display
     */
    public void displayUDPVals(Individual ind) {
        if (ind.userDefinedProperties == null || ind.userDefinedProperties.isEmpty()) {
            return;
        }
        String st = (String) udpComboBox.getSelectedItem();
        UserDefinedProperty udp = (UserDefinedProperty) ind.userDefinedProperties.get(st);
        String fill = "", valueText;
        udpValTextArea.setText(""); // erase it
        for (Object o : udp.value) {
            if (udp.typ.equals("individual")) {
                Individual i2 = (Individual) o;
                valueText = (i2.deleted ? "deleted" : i2.homeChart + ": " + i2.name + " <" + i2.serialNmbr + ">");
            } else {
                valueText = o.toString();
            }
            udpValTextArea.append(fill + valueText);
            fill = "\n";
        }
        udpEditButton.setEnabled(!udp.chartable);
    }
    
    /**To avoid parsing problems, when text is saved to a SILK file all line
     * breaks (e.g. '\n') are replaced with '$$br$$'. Do a simple global replace
     * and we're done.
     * 
     * @param in    the input string in which to restore
     * @return      the restored string
     */
    public static String restoreLineBreaks(String in) {        
        if (in == null) {
            return "";
        }
        return in.replace("$$br$$", "\n");
    }
    
    /**If any kin term definitions have been selected by User for auto-fill-in,
     * check to see if the relationship between Ego & Alter fits one of them;
     * if so, apply it by calling applyAutoDef. Do this always for terms of 
     * reference, and also for terms of address if they are separately defined.
     * 
     * @param nod the Node object for Alter
     * @param ego the Individual object for Ego
     */
    public void checkForAutoDefs(Node nod, Individual ego) {
        TreeMap<String, ArrayList<Context.CB_Ptr>> map = Context.current.autoDefRef;
        ArrayList<Context.CB_Ptr> list = map.get(nod.pcString);
        DomainTheory dt = null;
        try {
            dt = Context.current.domTheoryRef();
        } catch (Exception ex) {  }  //  nothing can go wrong, go wrong, go wrong...
        applyAutoDef(nod, list, dt, ego);
        if (Context.current.domTheoryAdrExists() && parent.chart.distinctAdrTerms
                && Context.current.autoDefAdr != null) {
            map = Context.current.autoDefAdr;
            list = map.get(nod.pcString);
            try {
                dt = Context.current.domTheoryAdr();
            } catch (Exception ex) {  }  
            applyAutoDef(nod, list, dt, ego);
        }
    }
    
    /**For every ClauseBody in the list, create a new Dyad; if it does not
     * duplicate an existing dyad, then fill in all fields and post it to
     * the Context's dyadsDefined. This method is invoked if User has accepted
     * a proposed definition and has agreed to apply that definition to all
     * Ego-Alter pairs that fit the definition.
     * 
     * @param nod the Node object for Alter
     * @param list a list of the kin terms that apply to this Ego/Alter pair.
     * @param dt the DomainTheory containing the definition
     * @param ego the Individual object for Ego 
     */
    public void applyAutoDef(Node nod, ArrayList<Context.CB_Ptr> list, 
            DomainTheory dt, Individual ego) {
        if (list == null) {
            return;
        }
        Context ctxt = Context.current;
        Dyad dad = new Dyad(ego);
        dad.alter = nod.indiv;
        dad.pcString = nod.pcString;
        for (Context.CB_Ptr ptr : list) {
            if (nod.hasKinTerm(ptr.kinTerm)) {
                continue;  //  no need for auto-def if term is already here
            }
            try {
                KinTermDef ktd = (KinTermDef) dt.theory.get(ptr.kinTerm);
                if (ktd == null) {
                    continue;
                }
                ClauseBody cb = (ClauseBody) ktd.expandedDefs.get(ptr.clauseNmbr);
                if (dt.fit(cb, dad)) {
                    String clas = (dt.addressTerms ? "address" : "reference");
                    nod.addTerm(ptr.kinTerm, "primary", clas);
                    dad.kinTerm = ptr.kinTerm;
                    dad.level = cb.level;
                    dad.kinTermType = 0;  // 0 = primary
                    if (dt.addressTerms) dad.addrOrRef = 1;  //  0 is default
                    dad.pcStringStructural = ClauseBody.structStr(dad.pcString);
                    dad.makePath(nod.miniPreds, dad.alter);
                    dt.dyadsDefined.dyAdd(dad);
                    TreeMap<String, ArrayList<Context.HistoryItem>> history = 
                            (dt.addressTerms ? ctxt.learningHistoryAdr : ctxt.learningHistoryRef);
                    postToLrngHist(history, dad);
                }
            } catch (Exception ex) {
                String msg = msgs.getString("exceptionTrialFitting") + ex;
                MainPane.displayError(msg, msgs.getString("seriousError"), 
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void postToLrngHist(TreeMap<String, ArrayList<Context.HistoryItem>> history, Dyad dad) {
        ArrayList<Context.HistoryItem> items = history.get(dad.kinTerm);
        Context.AcceptedDefPtr adp = null;
        if (items != null) {
            for (Context.HistoryItem item : items) {
                if (item instanceof Context.AcceptedDefPtr && ! item.rescinded) {
                    adp = (Context.AcceptedDefPtr)item;
                }
            }
            if (adp != null) {
                Integer[] pair = new Integer[2];
                pair[0] = dad.ego.serialNmbr;
                pair[1] = dad.alter.serialNmbr;
                adp.autoDefPairs.add(pair);
            }
        }
    }
    
    /**An alternative format for calling <code>fillTextField</code>.
     * 
     * @param nod        a Node containing kin term data
     * @param fieldType either 'primary' or 'reciprocal'
     * @param addr       true if this is a term of address
     */
    public void fillTextField(Node nod, String fieldType, boolean addr) {
        JTextField field = null;
        if (fieldType.equals("primary")) {
            field = (addr ? alterAdrTerm : alterRefTerm);
        }else if (fieldType.equals("reciprocal")) {
            field = (addr ? recipAdrTerm : recipRefTerm);
        }
        fillTextField(nod, field, addr);
    }

    
    /**Get the appropriate text from the Node, reformat it, and fill in the
     * text field.
     * 
     * @param nod  a Node containing kin term data
     * @param field the text field to be filled
     * @param addr true if this is a term of address
     */ 
    public void fillTextField(Node nod, JTextField field, boolean addr) {
        if (nod == null) {
            return;
        }
        ArrayList<String> terms = nod.getKinTerms(addr);
        if (terms.isEmpty()) {
            field.setText("");
        } else {
            String newText = "";
            int soFar = 0;
            for (String s : terms) {
                String ds = deSlashify(s);
                newText += (soFar++ == 0 ? "" : ", ") + ds;
            }
            field.setText(newText);
        }
    }
    
    public String sanitizeName(String nam) {
        return nam.replace("\"", "'").replace('<', '[').replace('>', ']').trim();
    }
    
    /**Finds all characters normally prohibited in a symbol, and precedes them 
     * with a backSlash. This prepares them for writing to the SILK file.
     * 
     *   STRATEGY: Detect multiple terms separated by comma-space. For each term,
     *             check for all the permissible types. Change all non-permissible characters to
     *             backslash-char.  BUT replace any double-quote with a single-quote. 
     * @param   c   window to which messages should be posted
     * @param   k   the kinTerm to be slashified
     * 
     * @return  a set of slashified kin terms
    */
    public static String slashify(java.awt.Component c, String k) {
        if (k.isEmpty()) {
            return k;
        }
        String slashified = "";
        ArrayList<String> terms = getKinTerms(k);
        boolean quoteDetected = false;
        for (String s : terms) {
            String newS = "";
            for (int n = 0; n < s.length(); n++) {
                char ch = s.charAt(n);
                if ((!Character.isJavaIdentifierPart(ch))
                        || (n == 0 && (Character.isUpperCase(ch) || Character.isDigit(ch)))) {
                    newS += '\\';
                }
                newS += Character.toString(ch);
            }
            if (slashified.length() > 0) {
                slashified += ", ";
            }
            if (newS.contains("\"")) {
                quoteDetected = true;
            }
            slashified += newS.replace("\"", "'");  //  double-quote strictly prohibited
        }
        if (quoteDetected) {
            String msg = msgs.getString("doubleQuotesReplaced");
            JOptionPane.showMessageDialog(c, msg,
                    msgs.getString("restrictionsOnTermCharacters"),
                    JOptionPane.INFORMATION_MESSAGE);
        }
        return slashified;
    }
    
    
    /**Reversing slashification is simple; replace all slashes with blanks.
     * 
     * @param s the string to be deSlashified
     * @return the altered string
     */
    public static String deSlashify(String s) {
        if (s==null) {
            return s;
        }
        return s.replace("\\", "").trim();
    }
    
    /** Change all forbidden characters into their legal replacements. This is
     * an alternative to slashifying that was used prior to liberalizing the
     * rules for permissible characters in a kin term.
     * 
     * @param c the window to write to
     * @param k the string to be sanitized
     * @param typ the type of string we are sanitizing
     * @return  the input string with all forbidden characters corrected
     * @throws KSParsingErrorException 
     */
    public static String sanitizeKinTerms(java.awt.Component c, String k, String typ) throws KSParsingErrorException {
        if (k.isEmpty()) {
            return k;
        }
        String sanitized = k.replace('-', '_').replace("\"", "_").replace("'", "_")
                .replace('<', '[').replace('>', ']').trim();
        ArrayList<String> badChars = new ArrayList<String>(), 
                          goodChars = new ArrayList<String>();
        if (k.indexOf("-") > -1) {
            badChars.add("-");
            goodChars.add("_");
        }
        if (k.indexOf("\"") > -1) {
            badChars.add("dbl-quote");
            goodChars.add("underscore");
        }
        if (k.indexOf("\'") > -1) {
            badChars.add("single-quote");
            goodChars.add("underscore");
        }
        if (k.indexOf("<") > -1) {
            badChars.add("<");
            goodChars.add("[");
        }
        if (k.indexOf(">") > -1) {
            badChars.add(">");
            goodChars.add("]");
        }
        String msg, pl;
        if (!badChars.isEmpty()) {
            pl = (badChars.size() > 1 ? "characters" : "character");
            msg = msgs.getString("illegal") + " " + pl + " " + msgs.getString("detected");
            for (int i=0; i < badChars.size(); i++) {
                msg += "\n'" + badChars.get(i) + msgs.getString("replacedBy") + goodChars.get(i) + "'";
            }
            msg += "\n " + msgs.getString("in") +  " '" + typ + "'";
            JOptionPane.showMessageDialog(c, msg, 
                    msgs.getString("restrictionsOnTermCharacters"), 
                    JOptionPane.INFORMATION_MESSAGE);
        }
        if (!allBlanksPrecededByCommas(sanitized)) {
            msg = msgs.getString("In") +  " '" + typ + "'\n";
            msg += msgs.getString("noBlanksEtc");
            JOptionPane.showMessageDialog(c, msg, 
                    msgs.getString("restrictionsOnTermCharacters"), 
                    JOptionPane.ERROR_MESSAGE);
            throw new KSParsingErrorException(msgs.getString("mustRemoveBlanks"));
        }
        char[] letters = sanitized.toCharArray();
        ArrayList<Character> baddies = new ArrayList<Character>();
        for (char ch : letters) {
            if (!Character.isJavaIdentifierPart(ch) && ch != ',' && ch != ' '
                    && ch != '[' && ch != ']' && ch != '{' && ch != '}') {
                baddies.add(ch);
            }
        }
        if (!baddies.isEmpty()) {
            pl = (baddies.size() > 1 ? "characters" : "character");
            msg = msgs.getString("illegal") + " " + pl + " " + 
                    msgs.getString("detected") + baddies;
            msg += "\n " + msgs.getString("in") +  " '" + typ + "'";
            JOptionPane.showMessageDialog(c, msg, 
                    msgs.getString("restrictionsOnTermCharacters"), 
                    JOptionPane.ERROR_MESSAGE);
            throw new KSParsingErrorException(msgs.getString("mustReplaceIllegalCharacters"));
        }
        char ch = sanitized.charAt(0);
        if (!Character.isLetter(ch)) {
            msg = msgs.getString("mustBeALetter");
            JOptionPane.showMessageDialog(c, msg, 
                    msgs.getString("restrictionsOnTermCharacters"), 
                    JOptionPane.ERROR_MESSAGE);
            throw new KSParsingErrorException(msgs.getString("replaceFirstCharacter"));
        }
        ArrayList<String> words = getKinTerms(sanitized);
        for (String word : words) {
            if (wordFound(reservedWords, word)) {
                // Tsk Tsk. A reserved word was used
                msg = "'" + word + msgs.getString("reservedWordError");
                JOptionPane.showMessageDialog(c, msg,
                        msgs.getString("restrictionsOnTermCharacters"),
                        JOptionPane.ERROR_MESSAGE);
                throw new KSParsingErrorException(msgs.getString("noReservedWords"));
            }
        }        
        return sanitized;
    }
    
    /** Assure all blanks are right after commas; embedded blanks are forbidden
     * in kin terms. 
     * 
     * @param s the string to be checked
     * @return true if all blanks are preceded by commas
     */
    public static boolean allBlanksPrecededByCommas(String s) {
        s = s.trim();
        int bl = s.indexOf(" "), start;
        if (bl == -1) {
            return true;
        }
        while(bl > -1) {
            if (s.charAt(bl -1) != ',' && s.charAt(bl -1) != ' ') {
                return false;
            }
            start = bl +1;
            bl = s.indexOf(" ", start);
        }
        return true;
    }
    
    static boolean wordFound(String[] words, String st) {
        for(String word : words) {
            if (word.equals(st)) {
                return true;
            }
        }
        return false;
    }
    

    void storeInfo(Individual infoPerson) 
            throws KSParsingErrorException, JavaSystemException,
		   KSBadHornClauseException, KSInternalErrorException,
                   KSConstraintInconsistency, KSDateParseException {
	if (! dirty) {
            return;
        }  //  No changes have been made
        storing = true;
        String a, b, c;
        int currEgoNum = parent.getCurrentEgo();
        Individual currEgo =
                    Context.current.individualCensus.get(currEgoNum);
        a = sanitizeName(alterFirstNames.getText());
        b = sanitizeName(alterLastName.getText());
        if (! infoPerson.name.equals(a + " " + b)) {
            infoPerson.name = a + " " + b;
            infoPerson.surname = b;
            infoPerson.firstNames = a;
            parent.chart.dirty = true;            
        }
        if (! infoPerson.deleted) updateEgoNames(infoPerson);
        boolean ok = true;
        a = personBirthMM.getText().trim();
        if (a.length() == 1) a = "0"+ a;  // canonical form
        b = personBirthYr.getText().trim();
        c = personBirthDD.getText().trim();
        if (c.length() == 1) c = "0"+ c;
        if (! infoPerson.getDateOfBirth().equals(UDate.formatAsXSD(b, a, c))) {
            ok = UDate.valiDate(b, a, c, infoPerson, false);
            if (! ok) {
                c = msgs.getString("invalidDoB") + b + "-" + a + "-" + c;
                throw new KSDateParseException(c);
            }
            if (infoPerson.birthFamily != null) {
                infoPerson.birthFamily.computeBirthGrps();
            }
        }
        a = personDeathMon.getText().trim();
        if (a.length() == 1) a = "0"+ a;
        b = personDeathYear.getText().trim();
        c = personDeathDD.getText().trim();
        if (c.length() == 1) c = "0"+ c;
        if (! infoPerson.getDateOfDeath().equals(UDate.formatAsXSD(b, a, c))) {
            ok = UDate.valiDate(b, a, c, infoPerson, true);
            if (! ok) {
                c = msgs.getString("invalidDoD") + b + "-" + a + "-" + c;
                throw new KSDateParseException(c);
            }
        }
        //  The comments field is handled by a DocumentListner
        ArrayList<String> oldTerms, newTerms;
        if (infoPerson.node != null && infoPerson.serialNmbr != currEgoNum) {
            a = slashify(parent, alterRefTerm.getText());
            alterRefTerm.setText(deSlashify(a));  //  assures text field holds cannonical form.
            if (!a.equals(alterKinTermRefImg.trim())) { 
                oldTerms = getKinTerms(alterKinTermRefImg);
                newTerms = getKinTerms(a);
                updateKinTerms(currEgo, infoPerson, infoPerson.node, oldTerms, newTerms, "Ref");
            } // end of User must have edited alterKinTermsRef
            if (parent.chart.distinctAdrTerms) {
                // Must pick up the term of address, if any
                a = slashify(parent, alterAdrTerm.getText());
                alterAdrTerm.setText(deSlashify(a));
                if (!a.equals(alterKinTermAdrImg.trim())) {
                    oldTerms = getKinTerms(alterKinTermAdrImg);
                    newTerms = getKinTerms(a);
                    updateKinTerms(currEgo, infoPerson, infoPerson.node, oldTerms, newTerms, "Adr");
                }
            }  // end of there-were-distinct-address-terms
            a = slashify(parent, recipRefTerm.getText());
            recipRefTerm.setText(deSlashify(a));
            TreeMap tmap = parent.ktm.getRow(infoPerson.serialNmbr);
            Node recipNode = (Node) tmap.get(currEgoNum);
            if (!a.equals(recipKinTermRefImg.trim())) {
                oldTerms = getKinTerms(recipKinTermRefImg);
                newTerms = getKinTerms(a);
                if (recipNode != null) {
                    updateKinTerms(infoPerson, currEgo, recipNode, oldTerms, newTerms, "Ref");
                } else {  //  Must create the reciprocal node
                    recipNode = new Node();
                    recipNode.indiv = currEgo;
                    recipNode.setLevel(-1 * infoPerson.node.getLevel());
                    recipNode.addTerms(newTerms, "primary", "reference");
                    infoPerson.node.invertMiniPredsAndPCStringFor(recipNode);
                    parent.ktm.addNode(infoPerson.serialNmbr, currEgoNum, recipNode);
                    updateKinTerms(infoPerson, currEgo, recipNode, oldTerms, newTerms, "Ref");
                }
            } //  end of recip-ref-terms-were-edited
            if (parent.chart.distinctAdrTerms) {
                // Must pick up the reciprocal term of address, if any
                a = slashify(parent, recipAdrTerm.getText());
                recipAdrTerm.setText(deSlashify(a));
                if (!a.equals(recipKinTermAdrImg.trim())) {
                    oldTerms = getKinTerms(recipKinTermAdrImg);
                    newTerms = getKinTerms(a);
                    updateKinTerms(infoPerson, currEgo, recipNode, oldTerms, newTerms, "Adr");
                }  //  end of recip-address-terms-were-edited
            }  //  end of distinct-address-terms           
        }  //  end of non-ego-has-a-node
        while (Library.currDataAuthor == null || Library.currDataAuthor.length() == 0) {
            Library.currDataAuthor = 
                    parent.chart.getCurrentUser(parent, 
                            msgs.getString("registerCurrentUser"));
        }
        infoPerson.dataAuthor = Library.currDataAuthor;
        infoPerson.dataChangeDate = UDate.today();
        Context.current.dateOfLastDataChange = infoPerson.dataChangeDate;
        parent.chart.dirty = true;
        dirty = false;
        storing = false;
        if (++Library.changeCounter % Library.saveInterval == 0) {
            parent.chart.saveSILKFile();
        }
        debugDyads();
    }
    
    
    static ArrayList<String> getKinTerms(String s) {
        ArrayList<String> list1 = new ArrayList<String>(),
                          list2 = new ArrayList<String>();
        s = s.trim();
        int comma = s.indexOf(","), start = 0, stop;
        while(comma > 0) {
            stop = (comma > 0 ? comma : s.length());
            list1.add(s.substring(start, stop).trim());
            start = stop +1;
            comma = s.indexOf(",", start);
        }
        if ((s.length() - start) > 0) {
            list1.add(s.substring(start, s.length()).trim());
        }  
        //  Now check for duplicates
        for (String str : list1) {
            if (!list2.contains(str)) {
                list2.add(str);
            }
        }
        if (list1.size() != list2.size()) {
            String msg = msgs.getString("removedDupEntry") + s + "'.";
            JOptionPane.showMessageDialog(SIL_Edit.edWin, msg, 
                    msgs.getString("errorDetectionKinTerms"), 
                    JOptionPane.ERROR_MESSAGE);
        }
        return list2;
    }

    
    static void updateKinTerms(Individual currEgo, Individual infoPerson, Node nod,
            ArrayList<String> oldTerms, ArrayList<String> newTerms, String typ) {

        ArrayList<String> deletedTerms = new ArrayList<String>(),
                          addedTerms = new ArrayList<String>();
        for (String newbie : newTerms) {
            if (!oldTerms.contains(newbie)) {
                addedTerms.add(newbie);
            }
        }
        for (String oldie : oldTerms) {
            if (!newTerms.contains(oldie)) {
                deletedTerms.add(oldie);
            } // NOTE: In both the node and dyad, we assume (for now) that
        }     // only primary terms are being captured. To be Reviewed.
        nod.replaceTerms(newTerms, "primary", (typ.equals("Ref") ? "reference" : "address"));
        try {
            DomainTheory dt = (typ.equals("Ref") ? Context.current.domTheoryRef()
                    : Context.current.domTheoryAdr());            
            for (String term : deletedTerms) {
                if (dt.dyadsDefined.containsKey(term)) {
                    dt.dyadsDefined.removeDyad(term, nod.pcString, currEgo, infoPerson, dt);
                } else {
                    dt.dyadsUndefined.removeDyad(term, nod.pcString, currEgo, infoPerson, dt);
                }
            }
            for (String term : addedTerms) {
                Dyad newDyad = makeDyad(currEgo, infoPerson, term, nod);
                if (dt.dyadsDefined.containsKey(term)) {
                    dt.dyadsDefined.dyAddOrUpdate(newDyad);
                } else {
                    dt.dyadsUndefined.dyAddOrUpdate(newDyad);
                }
            }
        } catch (Exception exc) {
            System.err.println("Error while retrieving Domain Theory from disk:\n" + exc);
        }
    }

    public static Dyad makeDyad(Individual currEgo, Individual infoPerson, String kinTerm, Node alterNode) {
        Dyad d = new Dyad(currEgo);
        d.alter = infoPerson;
        d.level = alterNode.getLevel();
        d.kinTerm = kinTerm;
        d.kinTermType = Dyad.PRIMARY;
        d.addrOrRef = (DomainTheory.addrTerms ? Dyad.ADDR : Dyad.REF);
        d.pcString = alterNode.pcString;
        try {
            d.pcStringStructural = ClauseBody.structStr(d.pcString);
        } catch (KSInternalErrorException ie) {
            String msg = "Problem making structural PCString in PPanel.storeInfo:\n" + ie;
            MainPane.displayError(msg, msgs.getString("seriousError"), 
                    JOptionPane.WARNING_MESSAGE);
        }
        d.makePath(alterNode.miniPreds, infoPerson);
        // debugging msg
        if (d.path.size() != (symbolCount(d.pcString) -1)) {
            String msg = "\nERROR: Path size incorrect.\nPath = " + d.path;
            msg += "\nPC String = " + d.pcString;
            msg += "\nEgo = #" + currEgo.serialNmbr;
            msg += "\nAlter = #" + infoPerson.serialNmbr;
            System.out.println(msg);     
            Context.breakpoint();
        }
        return d;
    }

    /** Count the number of capital letters and double-asterisks in the
     * PC String. Each is the start of a symbol like 'Fa' or 'Hbro' or '**'
     * which represents one of the standard macro relationships.
     *
     * @param pcString  the kin type, like 'FaBroWiMo'
     * @return  the count
     */
    public static int symbolCount(String pcString) {
        int cnt = 0,
            siz = pcString.length();
        char ch, peek;
        for (int i=0; i < siz; i++) {
            ch = pcString.charAt(i);
            if (Character.isJavaIdentifierStart(ch) &&
		Character.isUpperCase(ch)) { // Capital Ltr
                cnt++;
            } else if (ch == '*') { // single-asterisk
                if (i < siz -1) {
                    peek = pcString.charAt(i +1);
                    if (peek == '*') { // double-asterisk
                        ch = pcString.charAt(++i);                        
                    }
                }
                cnt++;
            } else if (ch == '+') {
                cnt++;
            }
        }
        return cnt;
    }

    public static String getSymbolNumber(int number, String pcString) {
        String symbol = null;
        int start = 0;
        for (int n=0; n <= number; n++) {
            symbol = firstSymbolOf(pcString.substring(start));
            start += symbol.length();
        }
        return symbol;
    }

    public static String firstSymbolOf(String pcString) {
        if (pcString.substring(0,2).equals("**")) {
            return "**";
        }
        char ch;
        for (int i=1; i < pcString.length(); i++) {
            ch = pcString.charAt(i);
            if ((Character.isJavaIdentifierStart(ch) && Character.isUpperCase(ch))
                    || ch == '*') {
                return pcString.substring(0, i);
            }
        } // If we get here, must have been a single symbol
        return pcString;
    }

    public static void debugDyads() {
        if (SIL_Edit.edWin.chart.recomputingDyads) {
            return;
        }
        if (ktMatrixInBalance(false)) {
            return;
        }
        SIL_Edit.edWin.rebuildKTMatrixEtc();
        if (ktMatrixInBalance(true)) {
            return;
        }
    }
    
    static boolean ktMatrixInBalance(boolean giveUp) {
        int ktmSz = 0, ktmCells = 0, mult = 0, popSz = 0, refSz = 0, adrSz = 0;
        Context.current.ktm.checkSelfNodes();
        try {
            ktmSz = Context.current.ktm.numberOfKinTerms();
            ktmCells = Context.current.ktm.numberOfCells();
            mult = (Context.current.domTheoryAdrExists() ? 2 : 1);
            popSz = popSize();
            refSz = DomainTheory.countLeaves(Context.current.domTheoryRef().dyadsUndefined)
                    + DomainTheory.countLeaves(Context.current.domTheoryRef().dyadsDefined);
            adrSz = (!Context.current.domTheoryAdrExists() ? 0
                    : DomainTheory.countLeaves(Context.current.domTheoryAdr().dyadsUndefined)
                    + DomainTheory.countLeaves(Context.current.domTheoryAdr().dyadsDefined));
        } catch (Exception exc) {
        }
        boolean ktminBalance = (refSz + adrSz) == (ktmSz - (mult * popSz));
        if (!ktminBalance && giveUp) {
            // debugging msg
            String msg = "Ref dyads: " + refSz + "\tAdr dyads: " + adrSz + "\tktmTerms: "
                    + ktmSz + " - " + (mult * popSz) + " = " + (ktmSz - (mult * popSz)) + "\tktmCells: " + ktmCells;
            MainPane.displayError(msg, "Data Loss Monitoring", JOptionPane.WARNING_MESSAGE);
            System.out.println(msg);
            System.out.println(Context.current.ktm.printMatrixSummary());
            try {
                System.out.println("\nUndefined Dyads");
                System.out.println(Context.current.domTheoryRef().dyadsUndefined.summaryString());
                System.out.println("\n\nDefined Dyads");
                System.out.println(Context.current.domTheoryRef().dyadsDefined.summaryString());
            }catch(Exception exc) {
            }
            Context.breakpoint();
            return false;
        }else {
            return ktminBalance;
        }
    }
    
    static int popSize() {  // Don't count deleted persons
        int census = 0;
        Iterator censusIter = Context.current.individualCensus.iterator();
        while (censusIter.hasNext()) {
            Individual ind = (Individual)censusIter.next();
            if (! ind.deleted) {
                census++;
            }
        }
        return census;
    }
    
    static void fillDyadsFromMatrix() {
        // Always called to regenerate dyads from scratch, thus duR etc. sre cleared
        DyadTMap duR = null, ddR = null, duA = null, ddA = null;
        DomainTheory domThRef = null, domThAdr = null;
        Context ctxt = Context.current;
        try {
            if (ctxt.domTheoryRefExists()) {
                domThRef = ctxt.domTheoryRef();
                duR = domThRef.dyadsUndefined;
                duR.clear();
                ddR = domThRef.dyadsDefined;
                ddR.clear();
            }
            if (ctxt.domTheoryAdrExists()) {
                domThAdr = ctxt.domTheoryAdr();
                duA = domThAdr.dyadsUndefined;
                duA.clear();
                ddA = domThAdr.dyadsDefined;
                ddA.clear();
            }
        } catch (Exception e) {
        }
        Iterator egoIter = ctxt.ktm.matrix.entrySet().iterator();
        while (egoIter.hasNext()) {
            Map.Entry entry1 = (Map.Entry) egoIter.next();
            Integer egoInt = (Integer) entry1.getKey();
            TreeMap row = (TreeMap) entry1.getValue();
            Iterator rowIter = row.entrySet().iterator();
            while (rowIter.hasNext()) {
                Map.Entry entry2 = (Map.Entry) rowIter.next();
                Integer altInt = (Integer) entry2.getKey();
                Node n = (Node) entry2.getValue();
                if (!egoInt.equals(altInt)) {  //  don't include self-nodes
                    Individual ego = ctxt.individualCensus.get(egoInt);
                    for (Dyad dy : Dyad.makeDyads(n, ego)) {
                        if (dy.addrOrRef == 0) {  // Ref DyadTMaps are always
                            if (domThRef.theory.containsKey(dy.kinTerm)) {
                                ddR.dyAdd(dy);
                            } else {
                                duR.dyAdd(dy);
                            }
                        } else {
                            if (domThAdr.theory.containsKey(dy.kinTerm)) {
                                ddA.dyAdd(dy);
                            } else {
                                duA.dyAdd(dy);
                            }
                        }
                    }
                }
            }
        }
    }
    

    void clearEgoBox() {
        egoChoiceBox.removeAllItems();
    }
    
    /**Rebuild the ComboBox with the names & IDs of every (non-deleted) person
     * in this Context.
     */
    public void rebuildEgoBox() {
        storing = true;
        egoChoiceBox.removeAllItems();
        String newName;
        for (Individual ind : Context.current.individualCensus) {
            newName = (ind.deleted ? 
                    "deleted" : 
                    ind.homeChart + ": " + ind.name+ " <" + ind.serialNmbr +">");
            egoChoiceModel.addElement(newName);
        }
        egoChoiceBox.setSelectedIndex(parent.getCurrentEgo());
        storing = false;
    }

    void rebuildLinksBox(Individual ind) {
        storing = true;
        linksComboBox.removeAllItems();
        String newChart;        
        for (Link lk : ind.links) {
            newChart = (lk.homeChart + ": ");
            newChart += Context.current.getChartDescription(lk.homeChart);
            linksComboModel.addElement(newChart);
        }        
        linksComboBox.setSelectedIndex(0);
        storing = false;
    }

    /**Add this person to the drop-down menu of possible Egos that appears in 
     * the Detail Display.
     * 
     * @param ind the person to be added
     */
    public void addToEgoChoices(Individual ind) {
        String newName = (ind.deleted ? 
                "deleted" : 
                ind.homeChart + ": " + ind.name + " <" + ind.serialNmbr + ">");
        egoChoiceModel.addElement(newName);
        if (egoChoiceModel.getSize() == 1) {
            egoChoiceBox.setSelectedIndex(0);
            parent.changeEgo(0);
        }
    }

    void resetEgoBox(int egoNum) {
        egoChoiceBox.setSelectedIndex(egoNum);
    }

    void updateEgoNames(Individual ind) {
        int ndx = ind.serialNmbr;
        String newName = (ind.deleted ? 
                "deleted" : 
                ind.homeChart + ": " + ind.name + " <" + ind.serialNmbr + ">");
        if (ndx < egoChoiceModel.getSize()) {
            egoChoiceModel.removeElementAt(ndx);
        }
        egoChoiceModel.insertElementAt(newName, ndx);
        egoChoiceBox.setSelectedIndex(parent.getCurrentEgo());
    }
    
    void setUDPSelection(String udpName) {
        int ndx = udpComboModel.getIndexOf(udpName);
        udpComboBox.setSelectedIndex(ndx);
    }

    /**Set all the flags that record whether there are separate Terms of Address.
     * 
     * @param val true = this Context has distinct terms of address
     */
    public void setDistinctAdrTerms(boolean val) {
        parent.chart.distinctAdrTerms = val;
        alterAdrTerm.setEditable(val);
        recipAdrTerm.setEditable(val);
        parent.kinTmAdrBtn.setEnabled(val);
        parent.ltrAdrBtn.setEnabled(val);
        parent.setDistinctAdrMenuItemSelected(val);
        Library.setStubAdrFileExists(Context.current.languageName, val);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField alterAdrTerm;
    private javax.swing.JTextField alterFirstNames;
    private javax.swing.JTextField alterID;
    private javax.swing.JTextField alterLastName;
    private javax.swing.JTextField alterRefTerm;
    private javax.swing.JLabel bornYrLabel;
    private javax.swing.JTextField chartField;
    private javax.swing.JLabel chartLabel;
    private javax.swing.JTextField dataChgDate;
    private javax.swing.JLabel dataChgDateLabel;
    private javax.swing.JComboBox egoChoiceBox;
    private javax.swing.JPanel egoPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox linksComboBox;
    private javax.swing.JLabel linksLabel;
    private javax.swing.JTextField personBirthDD;
    private javax.swing.JTextField personBirthMM;
    private javax.swing.JTextField personBirthYr;
    private javax.swing.JTextPane personComments;
    private javax.swing.JTextField personDeathDD;
    private javax.swing.JTextField personDeathMon;
    private javax.swing.JTextField personDeathYear;
    private javax.swing.JTextField recipAdrTerm;
    private javax.swing.JTextField recipRefTerm;
    private javax.swing.JComboBox udpComboBox;
    private javax.swing.JButton udpEditButton;
    private javax.swing.JLabel udpLabel;
    private javax.swing.JScrollPane udpScrollPane;
    private javax.swing.JTextArea udpValTextArea;
    // End of variables declaration//GEN-END:variables

    class CommentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            if (!storing) {
                if (parent.infoPerson != null) {
                    String txt = FamilyPanel.convertBannedCharacters(personComments.getText());
                    parent.infoPerson.comment = txt;
                    parent.chart.dirty = true;
                }
            }
        }

        public void removeUpdate(DocumentEvent e) {
            if (!storing) {
                if (parent.infoPerson != null) {
                    String txt = FamilyPanel.convertBannedCharacters(personComments.getText());
                    parent.infoPerson.comment = txt;
                    parent.chart.dirty = true;
                }
            }
        }

        public void changedUpdate(DocumentEvent e) {
            //Plain text components do not fire these events
        }
    }  // end of inner class CommentListener

}
