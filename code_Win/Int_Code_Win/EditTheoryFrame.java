import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.event.*;
import java.io.Serializable;


/**The Theory Editor allows an experienced User to fine-tune definitions that 
 * have been accepted, or to directly enter definitions they have deduced.
 *  This is the only way a User can define an auxiliary predicate.
 *  <ul>
 * <li>In any domain theory, a kin term is either defined (def is in theory, and 
 * all dyads are in dt.dyadsDefined) or undefined (not in theory, dyads in 
 * dt.dyadsUndefined). BUT, in this theory editor the User can choose a defined 
 * term to edit. Therefore, localDefined & localUndefined reflect the current 
 * status of a term in the editor, not its status in the domain theory. </li>
 * 
 * <li>Generally in SILKin, all kin terms are stored in 'slashified' form, and 
 * 'de-slashified' only when they are placed in a display field. However, since 
 * kin terms are being listed in menus, etc. in a Theory Editor, they are kept 
 * in de-slashified form, and only slashified when written out or stored on the 
 * Domain Theory. </li>
 * </ul>
 * 
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class EditTheoryFrame extends JFrame {

    static EditTheoryFrame current = null;
    static EditInProgress currentEdit;
    static KinTermDef commentKTD = null;
    DomainTheory dt;
    TreeMap<String, EditInProgress> editsInProgress;
    boolean dirty = false;
    static MyResBundle se = Library.screenElements,
                     msgs = Library.messages;
    ArrayList<String> auxiliaries = new ArrayList<String>(),
                      uDPs = new ArrayList<String>(),
                      localDefined = new ArrayList<String>(),
                      localUndefined = new ArrayList<String>();
    static String seperator = "--------";
    static String[] primitives = { "father", "mother", "parent", 
        "son", "daughter", "child", "husband", "wife", "spouse", seperator, 
        "dead", "divorced", "elder", "younger", "equal", "not", 
        "male", "female", "gender", seperator, 
        "contains", "greaterOrEql", "greaterThan", "lessOrEql", "lessThan" };
    static String[] constraints = { "divorced", "elder", "younger", 
        "equal", "not", "gender", "contains", 
        "greaterOrEql", "greaterThan", "lessOrEql", "lessThan" };
    static String[] stdMacros = { "brother", "sister", "sibling", "siblings", 
        "half_brother", "half_sister", "step_brother", "step_sister",  seperator, 
        "step_father", "step_mother", "step_son", "step_daughter",  seperator, 
        "parents", "children", "spice" };
    static ArrayList<String> unaryPreds = unaries();
    static String[] vars = { "B", "C", "D", "F", "G", "H", "I", "J", 
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", 
                "X", "Y", "Z" };
    static String indenter = "                                             ";
    static String[] primComboBoxModel = { se.getString("preDefinedTerms"), 
        "father", "mother", "parent", "parents", 
        "son", "daughter", "child", "children", "husband", "wife", "spouse", "spice", 
        "brother", "sister", "sibling", "siblings", 
        seperator, "step_father", "step_mother", "step_son", "step_daughter",  seperator, 
        "half_brother", "half_sister", "step_brother", "step_sister",  seperator, 
        "male", "female", "gender", seperator, 
        "dead", "divorced", "elder", "younger", "equal", "not", seperator, 
        "contains", "greaterOrEql", "greaterThan", "lessOrEql", "lessThan" };
    static boolean loading = false;
    static String displayTerm = null;
    static ArrayList<Exception> syntaxErrors = new ArrayList<Exception>();
    
    /** Creates new form EditTheoryFrame */
    private EditTheoryFrame() {
        initComponents();
        labelComponents();
        current = this;
        notEditableBtn.setSelected(true);
        addWindowListener(new CloseListener());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    public static EditTheoryFrame getEditTheoryFrame(DomainTheory domTh) {
        if (current != null) {
            current.loadTheory(domTh);
            return current;
        }else {
            EditTheoryFrame etf = new EditTheoryFrame();
            etf.loadTheory(domTh);
            return etf;
        }
    }
    
    static ArrayList<String> unaries() {
        ArrayList<String> s = new ArrayList<String>(4);
        s.add("dead");
        s.add("male");
        s.add("female");
        return s;
    }
    
    String slashify(String s){
        return PersonPanel.slashify(current, s);
    }
    
    /** Decompose a Horn Clause test string (with possible multiple clauses) into
     *  it's components, slashify all the predicates, and then return the string. 
     *  This method calls 'slashifyCB' on each clause. 
     * @param hc    a Horn Clause, must start with a head
     * @return      a slashified Horn Clause
     * @throws KSBadHornClauseException if hc is not in proper Horn Clause syntax.
     */
    String slashifyHC(String hc) throws KSBadHornClauseException {
        int implies = hc.indexOf(":-"),
                left = hc.indexOf("(");
        String term = hc.substring(0, left),
               slashified = slashify(term) + hc.substring(left, implies + 2) + " ", 
               body = "", args;
        if ((hc.length() - implies) < 3) {  //  there's no body, just a header
            return slashified;
        }
        ArrayList<String> cbs = breakIntoCBs(hc);
        for (int i=0; i < cbs.size(); i++) {
            if (i > 0) {
                slashified += "\n\t | ";
            }
            slashified += slashifyCB(cbs.get(i));
        }        
        return slashified;
    }
   
    
    /** Decompose a ClauseBody into its components, slashify the predicate, and return
     *  the slashified string.
     * 
     * @param cb    the text of a 'headless' ClauseBody
     * @return      the ClauseBody with all predicates slashified
     */
    public String slashifyCB(String cb) {  
        String slashified = "", term, args;
        int left = cb.indexOf("("),
            predStart = 0, right;
        while (left > 0) {
            term = cb.substring(predStart, left).trim();             
            right = cb.indexOf(")", left);
            args = cb.substring(left, right +1);
            slashified += slashify(term) + args;
            slashified += cb.substring(right +1, right +2); // the required punctuation after a right paren
            left = cb.indexOf("(", right);
            predStart = right +2;
        }        
        return slashified;
    }
    
    /** Pass the call to PersonPanel.deSlashify(s).
     * 
     * @param s the String to be processed
     * @return  the string with all slashes removed.
     */
    String deSlashify(String s) {
        return PersonPanel.deSlashify(s);
    }
    
    /** Loads (or reloads) specific DomainTheory into this editor. */
    public void loadTheory(DomainTheory domTh) {
        if (dt != null) {
            SIL_Edit.edWin.chart.saveSILKFile();
        }
        dt = domTh;
        // Load fields from DomainTheory
        loading = true;
        if (dt.author != null && !dt.author.trim().isEmpty()) {
            authorName.setText(dt.author);
        }else {
            try  {
            dt.author = SIL_Edit.edWin.chart.getCurrentUser(this, se.getString("choosePRIMARYAUTHOR"));
            authorName.setText(dt.author);
            }catch(KSInternalErrorException exc){
                MainPane.displayError(exc.toString(), msgs.getString("seriousError"), 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
        windowTitle.setText(dt.languageName + " " + se.getString("termsOf") + " " + 
                (dt.addressTerms ? se.getString("address")
                        : se.getString("reference")));
        if (dt.citation != null) {
            citationText.setText(PersonPanel.restoreLineBreaks(dt.citation));
        }
        dt.mergeUDPs();
        citationText.getDocument().addDocumentListener(new CitationListener());
        commentsLabel.setVisible(false);
        hornClauseTextArea.getDocument().addDocumentListener(new HCEditListener()); 
        localDefined.clear();       
        localDefined.addAll(dt.dyadsDefined.keySet());
        localDefined = deSlashifyArray(localDefined);
        if (dt.ctxt.userDefinedProperties != null) {
            uDPs.clear();
            uDPs.addAll(dt.ctxt.userDefinedProperties.keySet());
        }
        localUndefined.clear();
        localUndefined.addAll(dt.dyadsUndefined.keySet());
        localUndefined.remove("no__term");
        localUndefined = deSlashifyArray(localUndefined);
        loadEditsInProgress();
        loadKinTermDefsComboBox();
        findAuxiliaries();
        loadAuxiliaryDefsComboBox();
        primitiveCombo.setModel(new DefaultComboBoxModel(primComboBoxModel)); 
        loadLocalTermComboBox(); 
        loading = false;
    }
    
    /** Call PersonPanel.deSlashify(s) on each member of the ArrayList.
     * 
     * @param as    an ArrayList of strings
     * @return      an ArrayList of those strings deSlashified.
     */
    public static ArrayList<String> deSlashifyArray(ArrayList<String> as) {
        ArrayList<String> result = new ArrayList<String>();
        for (String s : as) {
            result.add(PersonPanel.deSlashify(s));
        }
        return result;
    }
    /**Load the local 'editsInProgress' field with the contents of the domain
     * theory's 'editsInProgress' field. Remove all kin terms whose definitions 
     * are being edited from the list of defined terms.
     * 
     */
    public void loadEditsInProgress() {
        // Load from DT. Subtract from local-X
        if (dt.editsInProgress != null) {
            editsInProgress = dt.editsInProgress;
            for (EditInProgress eip : editsInProgress.values()) {
                String kt = deSlashify(eip.kinTerm);
                if (!localDefined.remove(kt)) {
                    localUndefined.remove(kt);
                }
            }
        }else {
            editsInProgress = new TreeMap<String, EditInProgress>();
        }
    }
    
    /**Load the drop-down ComboBox with the names of all edits-in-progress,
     * all local terms that are already defined, and terms that are not yet 
     * defined.
     * 
     */
    public void loadKinTermDefsComboBox() {
        ArrayList<String> preModel = new ArrayList<String>();
        String dashBlk = "--- ",
               blkDash = " ---";
        preModel.add(se.getString("kTAndEditsInProgress"));
        if (!editsInProgress.isEmpty()) {
            preModel.add(dashBlk + se.getString("editsInProgress") + blkDash);
            preModel.addAll(editsInProgress.keySet());
        }
        if (!localDefined.isEmpty()) {
            preModel.add(dashBlk + se.getString("definedLocalTerms") + blkDash);
            preModel.addAll(localDefined);
        }
        if (!localUndefined.isEmpty()) {
            preModel.add(dashBlk + se.getString("undefinedLocalTerms") + blkDash);
            preModel.addAll(localUndefined);
        }
        String[] model = new String[preModel.size()];
        int loc = 0;
        for (String s : preModel) {
            model[loc++] = deSlashify(s);
        }
        kinTermDefsCombo.setModel(new DefaultComboBoxModel(model));      
    }
    
    /**Find all predicates marked as auxiliaries and place them in the
     * 'auxiliaries' ArrayList.
     */
    public void findAuxiliaries() {
        if (dt.theory == null) return;
        auxiliaries.clear();
        for (Object o : dt.theory.keySet()) {
            String s = (String)o;
            if (s.contains("[aux]")) {
                auxiliaries.add(s);
            }
        }
        auxiliaries = deSlashifyArray(auxiliaries);
    }

    /** Load the drop-down ComboBox with the names of all known auxiliaries, 
     *  plus the option "Define New Auxiliary Term". 
     * 
     */
    public void loadAuxiliaryDefsComboBox() {
        String[] model = new String[auxiliaries.size() + 3];
        int n = 0;
        model[n++] = se.getString("auxTerms");
        for (String s : auxiliaries) {
            model[n++] = s;
        }
        model[n++] = seperator;
        model[n++] = se.getString("defineNewAuxTerm");
        auxiliaryDefsCombo.setModel(new DefaultComboBoxModel(model)); 
    }
    
    
/**Load the drop-down ComboBox with the names of all defined local kin terms,
 * auxiliaries, and UDPs.
 * 
 */
    public void loadLocalTermComboBox() {
        int n = 1, 
            sz = auxiliaries.size() + localDefined.size() + uDPs.size() +1;
        boolean auxPresent = auxiliaries.size() > 0,
                localPresent = localDefined.size() > 0,
                udpsPresent = uDPs.size() >0;
        if (localPresent && udpsPresent) sz++;
        if (localPresent && auxPresent) sz++;
        String[] model = new String[sz];
        model[0] = se.getString("definedLocalTerms");
        for (String lt : localDefined) {
            model[n++] = lt;
        }
        if (n > 1 && n < sz) {
            model[n++] = seperator;            
        }
        for (String lt : auxiliaries) {
            model[n++] = lt;
        }
        if (auxPresent && udpsPresent) {
            model[n++] = seperator;            
        }
        for (String lt : uDPs) {
            model[n++] = lt;
        }
        localTermCombo.setModel(new DefaultComboBoxModel(model)); 
    }
    
    public ArrayList<String> realphabetize(ArrayList<String> list) {
        TreeMap<String, String> sorter = new TreeMap<String, String> ();
        for (String s : list) sorter.put(s, s);
        return new ArrayList<String>(sorter.keySet());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        windowTitle = new javax.swing.JLabel();
        authorLabel = new javax.swing.JLabel();
        authorName = new javax.swing.JTextField();
        citationLabel = new javax.swing.JLabel();
        citationScrollPane = new javax.swing.JScrollPane();
        citationText = new javax.swing.JTextArea();
        primitiveCombo = new javax.swing.JComboBox();
        localTermCombo = new javax.swing.JComboBox();
        primitiveInsertBtn = new javax.swing.JButton();
        localTermInsertBtn = new javax.swing.JButton();
        hornClauseScrollPane = new javax.swing.JScrollPane();
        hornClauseTextArea = new javax.swing.JTextArea();
        kinTermDefsCombo = new javax.swing.JComboBox();
        auxiliaryDefsCombo = new javax.swing.JComboBox();
        syntaxCheckBtn = new javax.swing.JButton();
        dyadCheckBtn = new javax.swing.JButton();
        acceptDefBtn = new javax.swing.JButton();
        helpBtn = new javax.swing.JButton();
        deleteEditBtn = new javax.swing.JButton();
        autoGenCkBox = new javax.swing.JCheckBox();
        periodBtn = new javax.swing.JButton();
        editDefBtn = new javax.swing.JRadioButton();
        editCommentsBtn = new javax.swing.JRadioButton();
        commentsLabel = new javax.swing.JLabel();
        notEditableBtn = new javax.swing.JRadioButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });

        windowTitle.setFont(new java.awt.Font("Palatino", 1, 18)); // NOI18N
        windowTitle.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        windowTitle.setText("LangName Terms of Reference");

        authorLabel.setText("Primary Author:");

        authorName.setText("                                       ");
        authorName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                authorNameActionPerformed(evt);
            }
        });
        authorName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                authorNameFocusLost(evt);
            }
        });

        citationLabel.setText("Citation:");

        citationText.setColumns(20);
        citationText.setRows(5);
        citationScrollPane.setViewportView(citationText);

        primitiveCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Pre-Defined Terms", "daughter", "father", "mother", "etc." }));
        primitiveCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                primitiveComboActionPerformed(evt);
            }
        });

        localTermCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Local Terms", "half_brother", "step_mother", "etc." }));
        localTermCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localTermComboActionPerformed(evt);
            }
        });

        primitiveInsertBtn.setText("Insert");
        primitiveInsertBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                primitiveInsertBtnActionPerformed(evt);
            }
        });

        localTermInsertBtn.setText("Insert");
        localTermInsertBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                localTermInsertBtnActionPerformed(evt);
            }
        });

        hornClauseTextArea.setColumns(20);
        hornClauseTextArea.setRows(5);
        hornClauseTextArea.setText("  [No definition selected for editing.]");
        hornClauseTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                hornClauseTextAreaFocusLost(evt);
            }
        });
        hornClauseScrollPane.setViewportView(hornClauseTextArea);

        kinTermDefsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Kin Terms & Edits in Progress", "term1", "term2", "term3" }));
        kinTermDefsCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kinTermDefsComboActionPerformed(evt);
            }
        });
        kinTermDefsCombo.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                kinTermDefsComboFocusGained(evt);
            }
        });

        auxiliaryDefsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Auxiliary Terms", "gen[aux]", "cross[aux]", "etc." }));
        auxiliaryDefsCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                auxiliaryDefsComboActionPerformed(evt);
            }
        });

        syntaxCheckBtn.setText("Syntax Check");
        syntaxCheckBtn.setEnabled(false);
        syntaxCheckBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syntaxCheckBtnActionPerformed(evt);
            }
        });

        dyadCheckBtn.setText("Dyad Check");
        dyadCheckBtn.setEnabled(false);
        dyadCheckBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dyadCheckBtnActionPerformed(evt);
            }
        });

        acceptDefBtn.setText("Accept Def");
        acceptDefBtn.setEnabled(false);
        acceptDefBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptDefBtnActionPerformed(evt);
            }
        });

        helpBtn.setForeground(new java.awt.Color(255, 0, 0));
        helpBtn.setText("?");
        helpBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpBtnActionPerformed(evt);
            }
        });

        deleteEditBtn.setText("Delete This Edit");
        deleteEditBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteEditBtnActionPerformed(evt);
            }
        });

        autoGenCkBox.setSelected(true);
        autoGenCkBox.setText("Generate Variables Automatically");

        periodBtn.setText("Period");
        periodBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                periodBtnActionPerformed(evt);
            }
        });

        buttonGroup1.add(editDefBtn);
        editDefBtn.setSelected(true);
        editDefBtn.setText("Edit Def");
        editDefBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editDefBtnActionPerformed(evt);
            }
        });

        buttonGroup1.add(editCommentsBtn);
        editCommentsBtn.setText("Edit Comments");
        editCommentsBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editCommentsBtnActionPerformed(evt);
            }
        });

        commentsLabel.setText("          ");

        buttonGroup1.add(notEditableBtn);
        notEditableBtn.setText("Display NOT Editable");
        notEditableBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                notEditableBtnActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(33, 33, 33)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(autoGenCkBox)
                                .add(132, 643, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(primitiveCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 218, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(layout.createSequentialGroup()
                                        .add(24, 24, 24)
                                        .add(notEditableBtn)))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(editDefBtn)
                                    .add(layout.createSequentialGroup()
                                        .add(primitiveInsertBtn)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 268, Short.MAX_VALUE)
                                        .add(periodBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 71, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(18, 18, 18)
                                        .add(localTermInsertBtn)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(localTermCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .add(deleteEditBtn)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 324, Short.MAX_VALUE)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(editCommentsBtn)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                        .add(commentsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 240, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                    .add(layout.createSequentialGroup()
                                        .add(syntaxCheckBtn)
                                        .add(27, 27, 27)
                                        .add(dyadCheckBtn)
                                        .add(27, 27, 27)
                                        .add(acceptDefBtn)))))
                        .addContainerGap())
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(hornClauseScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 853, Short.MAX_VALUE)
                            .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(layout.createSequentialGroup()
                                        .add(authorLabel)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                        .add(authorName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 288, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 367, Short.MAX_VALUE))
                                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                        .add(0, 0, Short.MAX_VALUE)
                                        .add(windowTitle, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 563, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(63, 63, 63)))
                                .add(helpBtn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(52, 52, 52))
                            .add(layout.createSequentialGroup()
                                .add(citationLabel)
                                .add(18, 18, 18)
                                .add(citationScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 569, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(0, 0, Short.MAX_VALUE))
                            .add(layout.createSequentialGroup()
                                .add(kinTermDefsCombo, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(18, 18, 18)
                                .add(auxiliaryDefsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 359, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(35, 35, 35))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(windowTitle)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(authorLabel)
                    .add(authorName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(helpBtn))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(citationLabel)
                    .add(citationScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(kinTermDefsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(auxiliaryDefsCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(hornClauseScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 132, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(editDefBtn)
                    .add(editCommentsBtn)
                    .add(commentsLabel)
                    .add(notEditableBtn))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(primitiveCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(primitiveInsertBtn)
                    .add(localTermCombo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(localTermInsertBtn)
                    .add(periodBtn))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(acceptDefBtn)
                    .add(dyadCheckBtn)
                    .add(syntaxCheckBtn)
                    .add(deleteEditBtn))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(autoGenCkBox)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    void labelComponents() {
        windowTitle.setText(se.getString("languageNameTermsReference"));
        authorLabel.setText(se.getString("primaryAuthor"));
        citationLabel.setText(se.getString("citation"));
        primitiveCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] 
            { se.getString("preDefinedTerms"), "daughter", "father", "mother", "etc." }));
        localTermCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] 
            { se.getString("localTerms"), "half_brother", "step_mother", "etc." }));
        primitiveInsertBtn.setText(se.getString("insert"));
        localTermInsertBtn.setText(se.getString("insert"));
        hornClauseTextArea.setText("  " + se.getString("noDefSelected"));
        kinTermDefsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] 
            { se.getString("kTAndEditsInProgress"), "term1", "term2", "term3" }));
        auxiliaryDefsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] 
            { se.getString("auxTerms"), "gen[aux]", "cross[aux]", "etc." }));
        syntaxCheckBtn.setText(se.getString("syntaxCheck"));
        dyadCheckBtn.setText(se.getString("dyadCheck"));
        acceptDefBtn.setText(se.getString("acceptDef"));
        deleteEditBtn.setText(se.getString("deleteThisEdit"));
        autoGenCkBox.setText(se.getString("genVariables"));
        periodBtn.setText(se.getString("period"));
        editDefBtn.setText(se.getString("editDef"));
        editCommentsBtn.setText(se.getString("editComments"));
        notEditableBtn.setText(se.getString("displayNOTEditable"));        
    }
    
    private void authorNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_authorNameActionPerformed
        // No action needed -- validity check performed when focus lost.
    }//GEN-LAST:event_authorNameActionPerformed

    private void kinTermDefsComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kinTermDefsComboActionPerformed
        if (loading) return;
        String menuChoice = (String)kinTermDefsCombo.getSelectedItem();
        loading = true;
        auxiliaryDefsCombo.setSelectedIndex(0);
        // Menu choice will be 1 of 4 types:
        // Invalid -- the separator or title line - re-display title line
        if (menuChoice.equals(se.getString("kTAndEditsInProgress")) 
                || menuChoice.startsWith("---")) {
            kinTermDefsCombo.setSelectedIndex(0);
            hornClauseTextArea.setText(se.getString("noDefSelected"));
//            setEditable(false);
            deleteEditBtn.setEnabled(false);
            if (editCommentsBtn.isSelected()) {
                editContextComments();
            }
            loading = false;
            return;
        }
        // Edit-in-Progress -- restore the partial edit to the editor window
        // EIPs don't have comments
        String slashifiedMenuChoice = slashify(menuChoice);
        if (editsInProgress.containsKey(slashifiedMenuChoice)) {            
            EditInProgress eip = editsInProgress.get(slashifiedMenuChoice);
            currentEdit = eip;  //  eip.currentText is always deSlashified
            hornClauseTextArea.setText(PersonPanel.restoreLineBreaks(eip.currentText));
            deleteEditBtn.setEnabled(true);
            syntaxCheckBtn.setEnabled(true);
            dyadCheckBtn.setEnabled(eip.syntaxOK);
            acceptDefBtn.setEnabled(eip.dyadCkOK);
//            setEditable(true);
        }
        // Defined Term -- load KTD in editor. 
        else if (localDefined.contains(menuChoice)) {            
            KinTermDef ktd = (KinTermDef) dt.theory.get(slashifiedMenuChoice);
            if (editDefBtn.isSelected() || notEditableBtn.isSelected()) {
                hornClauseTextArea.setText(deSlashify(ktd.toString("original", true, true)));
                if (notEditableBtn.isSelected()) {  //  Just Looking
                    currentEdit = null;
                    syntaxCheckBtn.setEnabled(false);
                    deleteEditBtn.setEnabled(false);
                } else {  // Going to Edit                    
                    convertToEIP(slashifiedMenuChoice);          
                    syntaxCheckBtn.setEnabled(true);  
                    deleteEditBtn.setEnabled(true);        
                }
                displayTerm = menuChoice;
                dyadCheckBtn.setEnabled(false);
                acceptDefBtn.setEnabled(false);
            } else {  // edit of comments is selected
                commentKTD = ktd;
                editKTDComments();
            }
        }
        // Undefined term -- create EIP and add to edits-in-progress
        else if (localUndefined.contains(menuChoice)) {
            makeNewEIP(slashifiedMenuChoice);
            localUndefined.remove(menuChoice); 
            editDefBtn.setSelected(true);            
        }else {
            MainPane.displayError(msgs.getString("menuChoiceInvalid"), 
                    msgs.getString("seriousError"), JOptionPane.ERROR_MESSAGE);
        }
        hornClauseTextArea.setEditable(! notEditableBtn.isSelected());
        loading = false;
    }//GEN-LAST:event_kinTermDefsComboActionPerformed

   
    private void makeNewEIP(String term) {
        //  term is already slashified
        if (currentEdit != null && currentEdit.deletions) {
            currentEdit.resetCounters(hornClauseTextArea.getText());
        }
        EditInProgress eip = new EditInProgress(term);
        currentEdit = eip;
        editsInProgress.put(eip.kinTerm, eip);
        eip.currentDef = new KinTermDef(eip.kinTerm);
        eip.currentText = deSlashify(eip.currentDef.clauseHead.toString()) + " :- ";
        eip.checkpoint = eip.currentText.length() - 1;
        eip.currentDef.domTh = dt;
        eip.indent = (int) (1.5 * eip.checkpoint);
        loadKinTermDefsComboBox();
        kinTermDefsCombo.setSelectedItem(deSlashify(term));
        hornClauseTextArea.setText(eip.currentText + " ");
        deleteEditBtn.setEnabled(true);
        syntaxCheckBtn.setEnabled(true);
        dyadCheckBtn.setEnabled(eip.syntaxOK);
        acceptDefBtn.setEnabled(eip.dyadCkOK);
//        setEditable(true);
    }    
    
    private void convertToEIP(String definedTerm) {
        //  definedTerm is already slashified
        String deSlashifiedTerm = deSlashify(definedTerm);
        EditInProgress eip = new EditInProgress(definedTerm);
        currentEdit = eip;
        editsInProgress.put(eip.kinTerm, eip);
        eip.currentDef = (KinTermDef) dt.theory.get(definedTerm);
        eip.currentText = deSlashify(eip.currentDef.toString("original", true, true));
        eip.checkpoint = eip.currentText.length() - 1;
        eip.indent = (int) (1.5 * (eip.currentDef.clauseHead.toString().length() +1));        
        localDefined.remove(deSlashifiedTerm);
        loadKinTermDefsComboBox();
        loadLocalTermComboBox();
        kinTermDefsCombo.setSelectedItem(deSlashifiedTerm);
        hornClauseTextArea.setText(eip.currentText);
        deleteEditBtn.setEnabled(true);
        syntaxCheckBtn.setEnabled(true);
        dyadCheckBtn.setEnabled(eip.syntaxOK);
        acceptDefBtn.setEnabled(eip.dyadCkOK);
//        setEditable(true);
    }    
   
    private void auxiliaryDefsComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_auxiliaryDefsComboActionPerformed
        if (loading) return;
        String menuChoice = (String)auxiliaryDefsCombo.getSelectedItem();
        loading = true;
        kinTermDefsCombo.setSelectedIndex(0);
        if (menuChoice.equals(se.getString("auxTerms")) 
                || menuChoice.equals(seperator)) {
            auxiliaryDefsCombo.setSelectedIndex(0);
            hornClauseTextArea.setText(se.getString("noDefSelected"));
            if (editCommentsBtn.isSelected()) {
                editContextComments();
            }
            loading = false;
            return;
        }
        if (menuChoice.equals(se.getString("defineNewAuxTerm"))) {
            String auxTerm = getValidAuxTerm();
            if (auxTerm == null) return;
            makeNewEIP(slashify(auxTerm));
        }
        else if (auxiliaries.contains(menuChoice)) {  // It's not edited unless text changes
            KinTermDef ktd = (KinTermDef) dt.theory.get(slashify(menuChoice));
            if (editDefBtn.isSelected()) {
                hornClauseTextArea.setText(deSlashify(ktd.toString("original", true, true)));
                currentEdit = null;
                displayTerm = menuChoice;
//                setEditable(false);
                deleteEditBtn.setEnabled(false);
                syntaxCheckBtn.setEnabled(false);
                dyadCheckBtn.setEnabled(false);
                acceptDefBtn.setEnabled(false);
            } else {  //  edit comments
                commentKTD = ktd;
                editKTDComments();
            }

        }
        else {
            MainPane.displayError(msgs.getString("menuChoiceInvalid"), 
                    msgs.getString("seriousError"), JOptionPane.ERROR_MESSAGE);
        }
        loading = false;
    }//GEN-LAST:event_auxiliaryDefsComboActionPerformed

    
    private void editContextComments() {
        boolean empty = (dt.ctxt.comments == null || dt.ctxt.comments.isEmpty());
        String txt = (empty ? " " : dt.ctxt.comments);
        hornClauseTextArea.setText(PersonPanel.restoreLineBreaks(txt));
        commentsLabel.setText(se.getString("onDT") + " " + dt.languageName);
        commentsLabel.setVisible(true);
//        setEditable(true);
        commentKTD = null;
        deleteEditBtn.setEnabled(false);
        syntaxCheckBtn.setEnabled(false);
        dyadCheckBtn.setEnabled(false);
        acceptDefBtn.setEnabled(false);
    }
    
    private void editKTDComments() {
        boolean empty = (commentKTD.comments == null || commentKTD.comments.isEmpty());
        String txt = (empty ? " " : commentKTD.comments);
        hornClauseTextArea.setText(PersonPanel.restoreLineBreaks(txt));
        commentsLabel.setText(se.getString("onDef") + " \"" + commentKTD.kinTerm + "\"");
        commentsLabel.setVisible(true);
//        setEditable(true);
        deleteEditBtn.setEnabled(false);
        syntaxCheckBtn.setEnabled(false);
        dyadCheckBtn.setEnabled(false);
        acceptDefBtn.setEnabled(false);
    }
    
    
    private void authorNameFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_authorNameFocusLost
        String nam = authorName.getText();
            if (!Context.getCurrent().dataAuthors.contains(nam)) {
                String msg = nam + " " + se.getString("notKnownAuthor");
                JOptionPane.showMessageDialog(current, msg);
                try {
                    nam = SIL_Edit.edWin.chart.getCurrentUser(current, 
                            se.getString("choosePRIMARYAUTHOR"));
                } catch (Exception exc) {
                    MainPane.displayError(exc.toString(),
                            msgs.getString("errorChoosingPrimaryAuthor"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
            dt.author = nam;
            authorName.setText(nam);
    }//GEN-LAST:event_authorNameFocusLost

    
    private void primitiveInsertBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_primitiveInsertBtnActionPerformed
        insertFrom(primitiveCombo);
    }//GEN-LAST:event_primitiveInsertBtnActionPerformed

    
    private void insertFrom(JComboBox combo) {
        if (combo.getSelectedIndex() == 0) {
            return;
        }
        loading = true;
        String verb = (String) combo.getSelectedItem();
        int cursor = hornClauseTextArea.getCaretPosition();
        hornClauseTextArea.append(" ");
        String suffix = "()", hdr = "", txtSoFar = hornClauseTextArea.getText();
        int end = txtSoFar.trim().length() - 1;  // position of last non-blank
        //  If User is editing the middle of an existing clause, all bets are off
        boolean messingInMiddle = (cursor < end &&
                (txtSoFar.substring(cursor).contains("|")
                || txtSoFar.substring(cursor).contains(".")));
        if (!messingInMiddle) {
            EditInProgress eip = currentEdit;
            int endOfHead = deSlashify(eip.currentDef.clauseHead.toString()).length() + 1;
            if (autoGenCkBox.isSelected() && cursor >= end) {
                int dot = Math.max(endOfHead, txtSoFar.lastIndexOf("."));
                boolean newStart = (dot != endOfHead && !txtSoFar.substring(dot).contains("|"));
                if (newStart) {
                    hdr = indenter.substring(0, eip.indent) + "| ";
                }
                if (newStart || eip.deletions || eip.insertions) {
                    eip.resetVars();
                    // eip uses slashified text
                    endOfHead = eip.currentDef.clauseHead.toString().length() + 1;
                    eip.checkpoint = Math.max(endOfHead, txtSoFar.lastIndexOf(".") + 1);
                    eip.resetCounters(txtSoFar);
                    dyadCheckBtn.setEnabled(false);
                    acceptDefBtn.setEnabled(false);
                    eip.deletions = false;
                    eip.insertions = false;
                }
                if (Arrays.binarySearch(constraints, verb) > -1) {
                    suffix = "( , ),";
                } else if (unaryPreds.contains(verb)) {
                    suffix = "(" + eip.lastVar + "),";
                } else {
                    String arg1 = eip.lastVar, arg0 = eip.getNextVar();
                    suffix = "(" + arg0 + "," + arg1 + "),";
                }
            }
        }
        cursor = hornClauseTextArea.getCaretPosition();  // may have changed
        hornClauseTextArea.insert(hdr + " " + verb + suffix, cursor);
        loading = false;
        hornClauseTextArea.grabFocus();
    }    
    
    private void hornClauseTextAreaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_hornClauseTextAreaFocusLost
        if (notEditableBtn.isSelected()) {
            return;
        }
        String latestTxt = hornClauseTextArea.getText().trim();
        if (editDefBtn.isSelected()) {
            if (currentEdit == null) {
                return;
            }
            if (currentEdit.deletions || currentEdit.insertions) {  //  something changed
                currentEdit.currentText = latestTxt;
                acceptDefBtn.setEnabled(false);
                currentEdit.syntaxOK = false;
                dyadCheckBtn.setEnabled(false);
                currentEdit.dyadCkOK = false;                    
                
            }
        } else {  //  editing comments
            latestTxt = FamilyPanel.convertBannedCharacters(latestTxt);
            if (commentKTD != null) {  //  editing a KTD's comments
                commentKTD.comments = latestTxt;

            } else {  //  we're editing Context comments 
                dt.ctxt.comments = latestTxt;
                SIL_Edit.edWin.chart.dirty = true;
            }
        }

    }//GEN-LAST:event_hornClauseTextAreaFocusLost

    private void primitiveComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_primitiveComboActionPerformed
        String item = (String) primitiveCombo.getSelectedItem();
        if (item.startsWith(se.getString("preDefinedTerms")) 
                || item.startsWith("---")) {
            primitiveCombo.setSelectedIndex(0);
        }
    }//GEN-LAST:event_primitiveComboActionPerformed

    private void deleteEditBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteEditBtnActionPerformed
        EditInProgress eip = currentEdit;
        String s1 = msgs.getString("reallyTrashEdit") + " '" + eip.kinTerm + "'?";
        Object[] btns = {se.getString("oK"), se.getString("cancel")};
        int ch = JOptionPane.showOptionDialog(current, s1,
                msgs.getString("beSure"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, btns, btns[0]);
        if (ch == JOptionPane.NO_OPTION) {
            return;
        }
        editsInProgress.remove(eip.kinTerm);
        if (dt.dyadsDefined.keySet().contains(eip.kinTerm)) {
            String deSlasherm = deSlashify(eip.kinTerm);
            if (!localDefined.contains(deSlasherm)) {
                localDefined.add(deSlasherm);
                localDefined = realphabetize(localDefined);
            }
            loadKinTermDefsComboBox();
            loadLocalTermComboBox();
        } else if (dt.dyadsUndefined.keySet().contains(eip.kinTerm)) {
            localUndefined.add(eip.kinTerm);
            localUndefined = realphabetize(localUndefined);
            loadKinTermDefsComboBox();
        } else if (eip.kinTerm.contains("[aux]")) {
            auxiliaries.add(eip.kinTerm);
            auxiliaries = realphabetize(auxiliaries);
            loadAuxiliaryDefsComboBox();
        }
        kinTermDefsCombo.setSelectedIndex(0);
        auxiliaryDefsCombo.setSelectedIndex(0);
        hornClauseTextArea.setText(se.getString("noDefSelected"));
//        setEditable(false);
        currentEdit = null;
    }//GEN-LAST:event_deleteEditBtnActionPerformed

    private void localTermInsertBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localTermInsertBtnActionPerformed
        insertFrom(localTermCombo);
    }//GEN-LAST:event_localTermInsertBtnActionPerformed

    private void periodBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_periodBtnActionPerformed
        String txtSoFar = hornClauseTextArea.getText().trim();
        int end = txtSoFar.length() - 1;  // position of last visible character
        if (txtSoFar.charAt(end) == ',') {
            txtSoFar = txtSoFar.substring(0, end) + ".";
            EditInProgress eip = currentEdit;
            String alreadyProcessed = txtSoFar.substring(0, eip.checkpoint),
                    processed = txtSoFar.substring(eip.checkpoint);
            processed = processed.replace(eip.lastVar, "Alter") + "\n";
            eip.currentText = alreadyProcessed + processed;
            loading = true;
            hornClauseTextArea.setText(eip.currentText);
            loading = false;
            eip.resetVars();
            eip.checkpoint = eip.currentText.length() - 1;
        }
    }//GEN-LAST:event_periodBtnActionPerformed

    private void syntaxCheckBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syntaxCheckBtnActionPerformed
        // Read contents of hornClauseTextArea as a KTD. If it parses OK,
        // syntax ck is passed. Pop up a congrats msg and light the 'Dyad Ck' btn.
        // If parse bombs, try to suggest why.
        String textDefinition = hornClauseTextArea.getText();
        ArrayList<String> cbs, cbSlash = new ArrayList<String>();
        try {
            cbs = breakIntoCBs(textDefinition);
            for (String s : cbs) {
                cbSlash.add(slashifyCB(s));
            }
        } catch (KSBadHornClauseException exc) {
            JOptionPane.showMessageDialog(current, exc.toString(),
                    msgs.getString("syntaxError"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (cbs.isEmpty()) {
            JOptionPane.showMessageDialog(current, msgs.getString("noHornClauseFound"), 
                    msgs.getString("syntaxError"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        KinTermDef ktd = currentEdit.currentDef;
        clearKTD(ktd);
        for (String s : cbSlash) {
            ClauseBody cb = null;
            try {
                cb = Library.readCBfromString(s, dt);
                verifyArity(cb);
                verifyChainOfRelations(cb);
            } catch (Exception exc) {
                String msg = msgs.getString("inClause") + " " + cb 
                        + "\n" + decodeException(exc, cbs);
                JOptionPane.showMessageDialog(current, msg,
                        msgs.getString("syntaxError"), JOptionPane.ERROR_MESSAGE);
                clearKTD(ktd);
                return;
            }
            ktd.addClause(cb);
        }
//        int indivReset = dt.ctxt.indSerNumGen,
//            famReset = dt.ctxt.famSerNumGen;
//        KinTermMatrix originalMatrix = dt.ctxt.ktm;
//        dt.ctxt.ktm = originalMatrix.clone();
        
        Context original = Context.getCurrent();
        Context.setCurrent(Context.tempCtxt(dt));  
        ArrayList<Object> egoBag = dt.maleAndFemaleCreatedHeThem();
        DomainTheory.current = dt;
        syntaxErrors.clear();
        try {
            ktd.assureExamplesGenerated(egoBag);
            ktd.makeSigStrings();
        } catch (Exception exc) {
            // We don't expect any exceptions in this case, but ...
            String msg = msgs.getString("whileTrying2Expand") + exc.toString();
            JOptionPane.showMessageDialog(current, msg,
                    msgs.getString("syntaxError"), JOptionPane.ERROR_MESSAGE);
            clearKTD(ktd);
            ktd.argValsRemover();
            restoreContext(original);
            return;
        }
        ktd.argValsRemover();
        restoreContext(original);
        if (syntaxErrors.isEmpty()) {
            // If we make it to here, the KTD is syntactically valid.
            JOptionPane.showMessageDialog(current, msgs.getString("syntaxCorrect"),
                    msgs.getString("syntaxOK"), JOptionPane.PLAIN_MESSAGE);
            dyadCheckBtn.setEnabled(true);
            currentEdit.syntaxOK = true;
        } else {
            Iterator goofs = syntaxErrors.iterator();
            String msg = "-  " + decodeException((Exception) goofs.next(), cbs);
            while (goofs.hasNext()) {
                msg += "\n-  " + decodeException((Exception) goofs.next(), cbs);
            }
            String pl = (syntaxErrors.size() > 1 ? 
                    msgs.getString("syntaxErrors") : 
                    msgs.getString("syntaxError"));
            JOptionPane.showMessageDialog(current, msg, pl, JOptionPane.ERROR_MESSAGE);
            clearKTD(ktd);
        }        
       if (currentEdit.kinTerm.contains("[aux]")) {
            //  DyadCheck makes no sense for an auxiliary term -- there is no
            //  kin term for this relationship.
            acceptDefBtn.setEnabled(true);
            currentEdit.dyadCkOK = true;
        }
    }//GEN-LAST:event_syntaxCheckBtnActionPerformed

    void restoreContext(Context c) {
        Context.setCurrent(c);
        dt.ctxt = c;
    }
    
    
    private void dyadCheckBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dyadCheckBtnActionPerformed
        if (!currentEdit.syntaxOK) return;
        String kinTerm = currentEdit.kinTerm;
        KinTermDef ktd = currentEdit.currentDef;
        DyadTestReport dtr = new DyadTestReport(dt, kinTerm, ktd);
        if (dt.dyadsDefined.containsKey(kinTerm)) {
            dtr.kTermTree = (TreeMap)dt.dyadsDefined.get(kinTerm);
        }else if (dt.dyadsUndefined.containsKey(kinTerm)) {
            dtr.kTermTree = (TreeMap)dt.dyadsUndefined.get(kinTerm);
        } else if (currentEdit.kinTerm.contains("[aux]")) {
            String msg = msgs.getString("auxTermsNotCorrespond");
            JOptionPane.showMessageDialog(current, msg,
                    msgs.getString("dyadCheckNotApplicable"), JOptionPane.PLAIN_MESSAGE);
            return;
        }else {
            String msg = msgs.getString("errorNotYourFault1") + " '" 
                    + deSlashify(kinTerm) + "' " 
                    + msgs.getString("errorNotYourFault2")
                    + msgs.getString("sendBugReport");
            JOptionPane.showMessageDialog(current, msg, 
                    msgs.getString("seriousError"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            dtr.dyadTest();
        }catch(Exception exc) {
            JOptionPane.showMessageDialog(current, exc.toString(), 
                    msgs.getString("seriousError"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (dtr.falsePos.isEmpty() && dtr.missingKinTypes.isEmpty()
                && dtr.falseNegs.isEmpty()) {
            JOptionPane.showMessageDialog(current, msgs.getString("allDyadsFit"),
                    msgs.getString("dyadCheckOK"), JOptionPane.PLAIN_MESSAGE);
            acceptDefBtn.setEnabled(true);
            currentEdit.dyadCkOK = true;
        } else if (dtr.percentMissed <= dtr.ignorablePercent) {
            java.text.NumberFormat pf = java.text.NumberFormat.getPercentInstance();
            String msg = msgs.getString("allExcept1") + " " 
                    + pf.format(dtr.percentMissed) + " " + msgs.getString("allExcept2");
            msg += msgs.getString("allExcept3");
            JOptionPane.showMessageDialog(current, msg,
                    msgs.getString("dyadCheckOK"), JOptionPane.PLAIN_MESSAGE);
            acceptDefBtn.setEnabled(true);
            currentEdit.dyadCkOK = true;
        } else {
            String plural1, plural2, msg = msgs.getString("defNotFitAllDyads");
            if (!dtr.missingKinTypes.isEmpty()) {
                plural1 = (dtr.missingKinTypes.size() > 1 ? 
                        msgs.getString("withKinTypes") : 
                        msgs.getString("withKinType"));
                plural2 = (dtr.numDys4MisgType > 1 ? 
                        msgs.getString("dyads") : 
                        msgs.getString("dyad"));
                msg += msgs.getString("doesNotProvideFor") + " " + dtr.numDys4MisgType + " " 
                        + plural2 + " " + plural1 + ":\n" 
                        + dtr.missingKinTypes;
            }
            if (!dtr.falsePos.isEmpty()) {
                if (dtr.falsePos.size() > 1) {
                    msg += msgs.getString("falsePos2");
                }else {
                    msg += msgs.getString("falsePos1");
                }
            } 
            if (!dtr.falseNegs.isEmpty()) {
                if (dtr.falseNegs.size() > 1) {
                    msg += msgs.getString("falseNeg2");
                }else {
                    msg += msgs.getString("falseNeg1");
                }
            }
            String ttl = msgs.getString("dyadCheckFailed");
            int opType = JOptionPane.OK_CANCEL_OPTION;
            int msgType = JOptionPane.ERROR_MESSAGE;
            String[] options = { msgs.getString("oK"), 
                                msgs.getString("showProblemDyads") };
            String initVal = options[0];
            int choice = JOptionPane.showOptionDialog(current, msg, ttl, 
                    opType, msgType, null, options, initVal);
            if (choice == JOptionPane.NO_OPTION) {
                ProblemDyadFrame pdf = new ProblemDyadFrame(dtr);
                pdf.setVisible(true);
            }
            return;
        }
    }//GEN-LAST:event_dyadCheckBtnActionPerformed

    /**Dbl-Ck that syntax and dyad OK. Then remove from edits-in-progress,
     * add accepted def to Defined Terms, and add it to domain theory. 
     * Reset combo boxes
     * 
     * @param evt unused parameter
     */
    private void acceptDefBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptDefBtnActionPerformed
        EditInProgress eip = currentEdit;
        if (!eip.syntaxOK || !eip.dyadCkOK) {
            acceptDefBtn.setEnabled(false);
            if (!eip.syntaxOK) {
                dyadCheckBtn.setEnabled(false);
            }
            return;
        }
        SIL_Edit.edWin.chart.dirty = true;
        boolean aux = (eip.kinTerm.contains("[aux]"));
        editsInProgress.remove(eip.kinTerm);
        if (aux) {
            auxiliaries.add(eip.kinTerm);
            auxiliaries = realphabetize(auxiliaries);
            loadAuxiliaryDefsComboBox();
        } else {  //  must be non-auxiliary
            String deSlashTerm = deSlashify(eip.kinTerm);
            if (! localDefined.contains(deSlashTerm)) {
                localDefined.add(deSlashTerm);
                localDefined = realphabetize(localDefined);
            }
        }
        loadKinTermDefsComboBox();
        loadLocalTermComboBox();
        kinTermDefsCombo.setSelectedIndex(0);
        auxiliaryDefsCombo.setSelectedIndex(0);
        KinTermDef newDef = eip.currentDef;
        dt.theory.put(eip.kinTerm, newDef);
        newDef.domTh = dt;
        TreeMap subtree = (TreeMap) dt.dyadsUndefined.remove(eip.kinTerm);
        //  An aux has no dyads
        if (subtree != null && !subtree.isEmpty()) {
            dt.dyadsDefined.put(eip.kinTerm, subtree);
        }
        if (aux) return;  //  Remaining only makes sense for kin terms
        TreeMap<String, ArrayList<Context.CB_Ptr>> autoDef = // autoDef: kinType => AList of Context.CB_Ptrs
                (dt.addressTerms ? dt.ctxt.autoDefAdr : dt.ctxt.autoDefRef);
        for (Object o : newDef.expandedDefs) {
            ClauseBody cb = (ClauseBody) o;
            String kinType = cb.pcString;
            if (autoDef.get(kinType) == null) {
                autoDef.put(kinType, new ArrayList<Context.CB_Ptr>());
            }
            ArrayList<Context.CB_Ptr> defList = autoDef.get(kinType);
            defList.add(new Context.CB_Ptr(newDef.kinTerm, cb.seqNmbr));
        }
        TreeMap<String, ArrayList<Context.HistoryItem>> learningHistory
                = (dt.addressTerms ? dt.ctxt.learningHistoryAdr : dt.ctxt.learningHistoryRef);
        Context.AcceptedDefPtr ptr;
        String cmt = (newDef.comments == null ? "" : newDef.comments);
        ptr = new Context.AcceptedDefPtr(newDef.kinTerm, UDate.today(), "false", cmt);
        ptr.postToHistory(learningHistory);
        String msg = msgs.getString("autoApplyDef");
        Object[] btns = {se.getString("yes"), se.getString("no")};
        int decision = JOptionPane.showOptionDialog(this, msg,
                msgs.getString("applyDefinition"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, btns, btns[0]);
        if (decision == JOptionPane.YES_OPTION) {
            SIL_Edit.edWin.rebuildKTMatrixEtc();
            SIL_Edit.edWin.applyDef(newDef, dt);
        }
    }//GEN-LAST:event_acceptDefBtnActionPerformed

    private void editDefBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editDefBtnActionPerformed
        commentsLabel.setVisible(false);
        hornClauseTextArea.setEditable(true);
        int choice = auxiliaryDefsCombo.getSelectedIndex();
        if (choice > 0) {  // go back to editing the aux
            auxiliaryDefsCombo.setSelectedIndex(choice);
        } else {  //  go back to editing def, if any
            choice = kinTermDefsCombo.getSelectedIndex();
            kinTermDefsCombo.setSelectedIndex(choice);
        }
        syntaxCheckBtn.setEnabled(true);
        dyadCheckBtn.setEnabled(false);
        acceptDefBtn.setEnabled(false);
    }//GEN-LAST:event_editDefBtnActionPerformed

    private void editCommentsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editCommentsBtnActionPerformed
        hornClauseTextArea.setEditable(true);
        String ktChoice = (String)kinTermDefsCombo.getSelectedItem(),
               auxChoice = (String)auxiliaryDefsCombo.getSelectedItem();
        if (kinTermDefsCombo.getSelectedIndex() == 0
                && auxiliaryDefsCombo.getSelectedIndex() == 0) {
            editContextComments();
        } else if (dt.theory.containsKey(ktChoice)) {
            commentKTD = (KinTermDef)dt.theory.get(ktChoice);
            editKTDComments();
        } else if (dt.theory.containsKey(auxChoice)) {
            commentKTD = (KinTermDef)dt.theory.get(auxChoice);
            editKTDComments();
        } else {
            String msg = msgs.getString("commentAfterDefine");
            JOptionPane.showMessageDialog(this, msg,
                msgs.getString("invalidCommand"), JOptionPane.PLAIN_MESSAGE);
            editDefBtn.setSelected(true);
        }
    }//GEN-LAST:event_editCommentsBtnActionPerformed

    private void helpBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpBtnActionPerformed
        //  Just launch the Help system, with Theory Editor page open.                                         
        HelpFrame.window.displayPage(HelpFrame.THEORY_EDIT, "layout");
    }//GEN-LAST:event_helpBtnActionPerformed

    private void localTermComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_localTermComboActionPerformed
        String item = (String) localTermCombo.getSelectedItem();
        if (item.equals(se.getString("definedLocalTerms")) || item.startsWith("---")) {
            localTermCombo.setSelectedIndex(0);
        }
    }//GEN-LAST:event_localTermComboActionPerformed

    private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
        //  Somehow, this never gets triggered???
    }//GEN-LAST:event_formFocusGained

    private void kinTermDefsComboFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_kinTermDefsComboFocusGained
        if (dirty) {
            localDefined.clear();
            ArrayList<String> ldTemp = new ArrayList<String>();
            ldTemp.addAll(dt.dyadsDefined.keySet());
            for (String s : ldTemp) {
                localDefined.add(deSlashify(s));
            }
            if (dt.ctxt.userDefinedProperties != null) {
                uDPs.clear();
                uDPs.addAll(dt.ctxt.userDefinedProperties.keySet());
            }
            localUndefined.clear();
            ldTemp = new ArrayList<String>();
            ldTemp.addAll(dt.dyadsUndefined.keySet());
            ldTemp.remove("no__term");
            for (String s : ldTemp) {
                localUndefined.add(deSlashify(s));
            }
            loadKinTermDefsComboBox();
            loadLocalTermComboBox();
            dirty = false;
        }
    }//GEN-LAST:event_kinTermDefsComboFocusGained

    private void notEditableBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_notEditableBtnActionPerformed
        commentsLabel.setVisible(false);
        hornClauseTextArea.setEditable(false);
    }//GEN-LAST:event_notEditableBtnActionPerformed


    private void verifyArity(ClauseBody cb) throws KSBadHornClauseException {
        for (Object o : cb.body) {
            Literal lit = (Literal)o;
            String pred = lit.predicate.toString(), msg;
            if (unaryPreds.contains(pred) && lit.args.size() != 1) {
                msg = msgs.getString("found") + lit;
                msg += msgs.getString("need1Arg");
                throw new KSBadHornClauseException(msg);
            }else if (!unaryPreds.contains(pred) && lit.args.size() != 2) {
                msg = msgs.getString("found") + lit;
                msg += msgs.getString("need2Args");
                throw new KSBadHornClauseException(msg);
            }
        }
    }
    
    
    private void verifyChainOfRelations(ClauseBody cb) throws KSBadHornClauseException {
        ArrayList<String> oneTime = new ArrayList<String>(),
                more = new ArrayList<String>();
        for (Object o : cb.body) {
            Literal lit = (Literal) o;
            for (Object o1 : lit.args) {
                String argName = ((Argument) o1).argName;
                if (more.contains(argName)) {
                    continue;  //  already seen 2+ occurences
                }
                if (oneTime.contains(argName)) {
                    more.add(argName);  //  this is 2nd occurences
                    oneTime.remove(argName);
                } else {  //  1st occurence
                    oneTime.add(argName);
                }
            }
        }
        for (String arg : oneTime) {
            if (arg.equals("Ego") || arg.equals("Alter")) {
                continue;
            }else {
                String msg = msgs.getString("oneTime1") + arg + 
                        msgs.getString("oneTime2") + msgs.getString("oneTime3");
                throw new KSBadHornClauseException(msg);
            }
        }
    }
    
    
    private void clearKTD(KinTermDef kt) {        
            kt.definitions.clear();
            kt.expandedDefs.clear();
            kt.flags.clear();
            if (kt.exactEQCs != null) {
                kt.exactEQCs.clear();
            }
            if (kt.structEQCs != null) {
                kt.structEQCs.clear();
            }
            kt.eqcSigExact = null;
            kt.eqcSigStruct = null;
    }
    
    String decodeException(Exception exc, ArrayList<String> cbs) {
        if (exc instanceof KSNoChainOfRelations2Alter) {
            String msg = "", exceptionTxt = exc.toString(),
                    msg2 = msgs.getString("noChain");
            try {
                int     start = exceptionTxt.lastIndexOf("[Expansion Path:") + 17,
                        colon = exceptionTxt.indexOf(":", start),
                        comma = exceptionTxt.indexOf(",", colon),
                        cbNum = Integer.parseInt(exceptionTxt.substring(colon + 1, comma).trim());
                String claws = cbs.get(cbNum);
                msg = msgs.getString("in") + claws + "\"\n" + msg2;
            } catch (Exception xc) {
                msg = msg2;
            }
            return msg;
        } else if (exc instanceof ClassCastException) {
            return msgs.getString("unparseable");
        } else if (exc instanceof KSParsingErrorException) {
            //  Assume that the text of parsing exceptions will always be in English
            String msg = "", exceptionTxt = exc.toString();
            try {
                int start = exceptionTxt.indexOf("On Line # ") + 10,
                        end = exceptionTxt.indexOf("Character Position", start),
                        cbNum = Integer.parseInt(exceptionTxt.substring(start, end).trim()),
                        colon = exceptionTxt.indexOf(":", end),
                        of = exceptionTxt.indexOf("of", colon),
                        charNum = Integer.parseInt(exceptionTxt.substring(colon + 1, of).trim());
                String claws = cbs.get(cbNum), cause = "", cure = "";
                if (exceptionTxt.contains("period or comma")) {
                    cause = msgs.getString("cause1");
                    cure = msgs.getString("cure1");
                } else if (exceptionTxt.contains("symbol")) {
                    cause = msgs.getString("cause2");
                    cure = msgs.getString("cure2");
                } else if (exceptionTxt.contains("Paren")) {
                    cause = msgs.getString("cause3");
                    cure = msgs.getString("cure3");
                } else {
                    cause = msgs.getString("cause4");
                    cure = msgs.getString("cure4");
                }
                msg = msgs.getString("thereIsLikely") + " " + cause + " " 
                        + msgs.getString("theStars");
                msg += claws.substring(0, charNum) + " ***" + claws.substring(charNum) + cure;
            } catch (Exception xc) {
                msg = msgs.getString("causeX");
            }
            return msg;
        } else {
            return exc.toString();
        }
    }
    
    public ArrayList<String> breakIntoCBs(String textDefinition) 
        throws KSBadHornClauseException  {
        ArrayList<String> list = new ArrayList<String>();
        int start = textDefinition.indexOf(":-") + 2,
            stop = textDefinition.indexOf(".", start),
            end = textDefinition.length() -1;
        if (start >= end) {
            return list;
        }
        String msg;
        if (start > 1 && stop == -1) {
            msg = msgs.getString("needPeriod") + textDefinition.substring(start)
                    + "\"";
            throw new KSBadHornClauseException(msg);
        }
        while (start > 1 && stop > -1) {
            String cbTxt = textDefinition.substring(start, stop +1);
            list.add(cbTxt);
            start = textDefinition.indexOf("|", stop) + 1;
            if (start == 0) {
                if (!textDefinition.substring(stop +1).trim().isEmpty()) {
                    msg = msgs.getString("extraText1") + textDefinition.substring(stop +1).trim()
                            + msgs.getString("extraText2");
                    throw new KSBadHornClauseException(msg);
                }
                break;
            }
            stop = textDefinition.indexOf(".", start);
            if (stop == -1) {
                msg = msgs.getString("finalClause1") + textDefinition.substring(start)
                        + msgs.getString("efinalClause2");
                throw new KSBadHornClauseException(msg);
            }
        }
        return list;
    }

    /**Prompt User for a valid name of an auxiliary predicate. A valid name
     * must contain the flag "[aux]". It may not duplicated a name already
     * in use.
     * 
     * @return a valid name
     */
    public String getValidAuxTerm() {
        String baddie = "Bad$$#*@Name",
               auxTerm = baddie,
               msg = msgs.getString("chooseAuxName"),
               xmpl = msgs.getString("example1") + " 'cross_cousin[aux]", msg2;
        while (auxTerm == baddie) {
            auxTerm = JOptionPane.showInputDialog(current, msg);
            if (auxTerm == null) return null;
            if (auxTerm.isEmpty() 
                    || !Character.isLowerCase(auxTerm.charAt(0))
                    || !auxTerm.contains("[aux]")) {
                auxTerm = baddie;
                msg += xmpl;
                xmpl = "";
            }
            if (auxiliaries.contains(auxTerm)) {
                msg2 = msgs.getString("auxDup1");
                JOptionPane.showMessageDialog(current, msg2, msgs.getString("dupNames"),  
                        JOptionPane.ERROR_MESSAGE);
                auxTerm = baddie;
            }
            if (editsInProgress.containsKey(auxTerm)) {
                msg2 = msgs.getString("auxDup2");
                JOptionPane.showMessageDialog(current, msg2, msgs.getString("dupNames"), 
                        JOptionPane.ERROR_MESSAGE);
                auxTerm = baddie;
            }
        }        
        return auxTerm;
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(EditTheoryFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EditTheoryFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EditTheoryFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EditTheoryFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new EditTheoryFrame().setVisible(true);
            }
        });
    }
    // TODO Decide if I need an "Edit" button to make HC editable.
    
    // GUI Variables are below. Other fields at top of page
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton acceptDefBtn;
    private javax.swing.JLabel authorLabel;
    private javax.swing.JTextField authorName;
    private javax.swing.JCheckBox autoGenCkBox;
    private javax.swing.JComboBox auxiliaryDefsCombo;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel citationLabel;
    private javax.swing.JScrollPane citationScrollPane;
    private javax.swing.JTextArea citationText;
    private javax.swing.JLabel commentsLabel;
    private javax.swing.JButton deleteEditBtn;
    private javax.swing.JButton dyadCheckBtn;
    private javax.swing.JRadioButton editCommentsBtn;
    private javax.swing.JRadioButton editDefBtn;
    private javax.swing.JButton helpBtn;
    private javax.swing.JScrollPane hornClauseScrollPane;
    private javax.swing.JTextArea hornClauseTextArea;
    private javax.swing.JComboBox kinTermDefsCombo;
    private javax.swing.JComboBox localTermCombo;
    private javax.swing.JButton localTermInsertBtn;
    private javax.swing.JRadioButton notEditableBtn;
    private javax.swing.JButton periodBtn;
    private javax.swing.JComboBox primitiveCombo;
    private javax.swing.JButton primitiveInsertBtn;
    private javax.swing.JButton syntaxCheckBtn;
    private javax.swing.JLabel windowTitle;
    // End of variables declaration//GEN-END:variables

    
    
    static class EditInProgress implements Serializable  {
        
        String kinTerm;
        KinTermDef currentDef;
        String currentText;
        boolean syntaxOK = false;
        boolean dyadCkOK = false;
        boolean deletions = false;
        boolean insertions = false;
        String lastVar = "Ego";    
        int nextVar = 0;
        int checkpoint = 0;
        int indent = 0;
        
        public EditInProgress(String kt) {
            kinTerm = kt;
        }

        String getNextVar() {
            lastVar = vars[nextVar++];  // increment it for next time
            return lastVar;
        }

        void resetVars() {
            lastVar = "Ego";
            nextVar = 0;
            if (currentDef != null) {
                checkpoint = currentDef.clauseHead.toString().length() +1;
                indent = (int)(1.5 * checkpoint);
            }
        }
     
        void resetCounters(String txt) {
            if (checkpoint >= txt.length()) {
                checkpoint = txt.lastIndexOf(".");
                if (checkpoint == -1) {
                    checkpoint = currentDef.clauseHead.toString().length() +1;
                }
            }
            String subTxt = txt.substring(checkpoint);
            lastVar = "Ego";
            int rank, bestRank = -1;
            if (!subTxt.trim().isEmpty()) {
                for (rank = 0; rank < 24; rank++) {
                    String s = vars[rank];
                    if (subTxt.contains(s) && rank > bestRank) {
                        bestRank = rank;
                        lastVar = s;
                    }
                }
            }
            nextVar = bestRank + 1;
            syntaxOK = false;
            dyadCkOK = false;
        }

        public String toString() {
            return toSILKString("");
        }
        
        public String toSILKString(String baser) {
            String xml = baser + "<eip kinTerm=\"" + kinTerm;
            String curTxt = "";
            try {
                curTxt = FamilyPanel.convertBannedCharacters(current.slashifyHC(currentText));
            }catch(Exception exc) { // it was slashified already, with no issues.     
            }
            xml += "\" text=\"" + curTxt;
            xml += "\" indent=\"" + indent;
            xml += "\" lastVar=\"" + lastVar;
            xml += "\" nextVar=\"" + nextVar;
            xml += "\" checkpoint=\"" + checkpoint;
            xml += "\" syntaxOK=\"" + syntaxOK;
            xml += "\" dyadCkOK=\"" + dyadCkOK + "\">\n"; 
            xml += currentDef.toSILKString(baser + "\t", false, false);
            xml += "\n" + baser + "</eip>\n";
            return xml;
        }
    }  //  end of inner class EditInProgress
    
    /** This inner class tests a group of dyads to see if they fit a particular
     *  kin term definition. It will identify mis-Fits.
     * 
     */
    public class DyadTestReport {
        ArrayList<Dyad> falsePos = new ArrayList<Dyad>();
        ArrayList<Dyad> falseNegs = new ArrayList<Dyad>();
        ArrayList<Dyad> missingKinTypeDyads = new ArrayList<Dyad>();
        ArrayList<String> missingKinTypes = new ArrayList<String>();
        TreeMap<String, ArrayList<ClauseBody>> sorter = 
                new TreeMap<String, ArrayList<ClauseBody>>();
        TreeMap kTermTree;
        DomainTheory dt;
        String kinTerm;
        KinTermDef ktd;
        int numDys4MisgType = 0, totalPosDyads = 0;
        float percentMissed = 0f, ignorablePercent;
        
        
        public DyadTestReport(DomainTheory dth, String kTerm, KinTermDef ktdef) {
            dt = dth;
            kinTerm = kTerm;
            ktd = ktdef;           
            ignorablePercent = Context.getCurrent().ignorableP / 100f;
        }  //  end of constructor
        
        
        String problemText() {
            String txt = "";
            if (!missingKinTypeDyads.isEmpty()) {
                txt += msgs.getString("defNotCover");
                for (Dyad d : missingKinTypeDyads) {
                    txt += "\n" + d.toProblemString();
                }
                txt += "\n\n";
            }
            if (!falsePos.isEmpty()) {
                txt += msgs.getString("coversNotParticularDyads");
                for (Dyad d : falsePos) {
                    txt += "\n" + d.toProblemString();
                }
                txt += "\n\n";
            }
            if (!falseNegs.isEmpty()) {
                txt += msgs.getString("coversOtherKT");
                for (Dyad d : falseNegs) {
                    txt += "\n" + d.toProblemString();
                }
            }
            return txt;            
        }
        
        /** Iterate thru dyads for this kin term, grouped by pcString 
         *  (kin type). If any dyads don't have any expandedDefs for their kin 
         *  type, put them in the mis-fits list. If a dyad has a list of expDefs 
         *  for its kin type, then test that dyad against each expDef in the 
         *  list until we find a good fit. If we run out of expDefs with no fit, 
         *  put this dyad in mis-fit list.
         * 
         * @throws KSInternalErrorException 
         */
        public void dyadTest() throws KSInternalErrorException {
            for (int i = 0; i < ktd.expandedDefs.size(); i++) {
                ClauseBody cb = (ClauseBody) ktd.expandedDefs.get(i);
                if (sorter.get(cb.pcString) == null) {
                    sorter.put(cb.pcString, new ArrayList<ClauseBody>());
                }
                sorter.get(cb.pcString).add(cb);
            }            
            Iterator kinTypeIter = kTermTree.entrySet().iterator();
            while (kinTypeIter.hasNext()) {
                Map.Entry entry = (Map.Entry) kinTypeIter.next();
                String kinType = (String) entry.getKey();
                ArrayList dyads = (ArrayList) entry.getValue();
                ArrayList<ClauseBody> cbs = sorter.get(kinType);
                totalPosDyads += dyads.size();
                if (cbs == null) {
                    missingKinTypes.add(kinType);
                    missingKinTypeDyads.addAll(dyads);
                    numDys4MisgType += dyads.size();
                } else {
                    for (Object o : dyads) {
                        Dyad dy = (Dyad) o;
                        boolean goodFit = false;
                        for (ClauseBody cb : cbs) {
                            try {
                                goodFit = dt.fit(cb, dy);
                                if (goodFit) {
                                    break;
                                }
                            } catch (Exception exc) {
                                String msg = msgs.getString("notYourFault")
                                        + exc + msgs.getString("sendBugReport");
                                throw new KSInternalErrorException(msg);
                            }
                        }
                        if (!goodFit) {
                            falsePos.add(dy);
                        }
                    }
                }
            }  //  Now check for false negatives
            ArrayList<String> kinTypes = new ArrayList<String>(sorter.keySet());
            TreeMap<String, ArrayList<Dyad>> negs = findNegs(kinTypes);
            kinTypeIter = negs.entrySet().iterator();
            while (kinTypeIter.hasNext()) {
                Map.Entry entry = (Map.Entry) kinTypeIter.next();
                String kinType = (String) entry.getKey();
                ArrayList dyads = (ArrayList) entry.getValue();
                ArrayList<ClauseBody> cbs = sorter.get(kinType);
                for (Object o : dyads) {
                    Dyad dy = (Dyad) o;
                    boolean goodFit = false;
                    for (ClauseBody cb : cbs) {
                        try {
                            goodFit = dt.fit(cb, dy);
                            if (goodFit) {
                                break;
                            }
                        } catch (Exception exc) {
                            String msg = msgs.getString("notYourFault")
                                        + exc + msgs.getString("sendBugReport");
                            throw new KSInternalErrorException(msg);
                        }
                    }
                    if (goodFit) {
                        falseNegs.add(dy);
                    }
                }
            }
            percentMissed = (falsePos.size() + numDys4MisgType + falseNegs.size()) 
                    / ((totalPosDyads + falseNegs.size()) * 1.0f);        
        }  //  end of method dyadTest
        
        
        TreeMap<String, ArrayList<Dyad>> findNegs(ArrayList<String> kinTypes) {
            TreeMap<String, ArrayList<Dyad>> negs = new TreeMap<String, ArrayList<Dyad>>();
            Iterator termIter = dt.dyadsDefined.entrySet().iterator();
            while (termIter.hasNext()) {
                Map.Entry entry = (Map.Entry) termIter.next();
                String term = (String) entry.getKey();
                if (!term.equals(kinTerm)) {
                    TreeMap typeMap = (TreeMap) entry.getValue();
                    for (String typ : kinTypes) {
                        if (typeMap.containsKey(typ)) {
                            addToTMap(negs, typ, (ArrayList) typeMap.get(typ));
                        }
                    }
                }
            }
                
            termIter = dt.dyadsUndefined.entrySet().iterator();
            while (termIter.hasNext()) {
                Map.Entry entry = (Map.Entry) termIter.next();
                String term = (String) entry.getKey();
                if (!term.equals(kinTerm)) {
                    TreeMap typeMap = (TreeMap) entry.getValue();
                    for (String typ : kinTypes) {
                        if (typeMap.containsKey(typ)) {
                            addToTMap(negs, typ, (ArrayList) typeMap.get(typ));
                        }
                    }
                }
            }
            return negs;
        }
        
        void addToTMap(TreeMap<String, ArrayList<Dyad>> negs, String typ, ArrayList lst) {
            ArrayList<Dyad> dys = negs.get(typ);
            if (dys == null) {
                negs.put(typ, new ArrayList<Dyad>());
            }
            for (Object o : lst) {
                negs.get(typ).add((Dyad)o);
            }
        }
        
        
    }  //  end of class DyadTestReport
    
    class CloseListener extends WindowAdapter {
        public void windowClosing(WindowEvent evt) {
            dt.editsInProgress = editsInProgress;
            SIL_Edit.edWin.chart.dirty = true;
        }
    }

    class HCEditListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            if (loading) return;
            if (editCommentsBtn.isSelected()) return;
            if (currentEdit != null) currentEdit.insertions = true; 
        }
        
        public void removeUpdate(DocumentEvent e) {
            if (loading) return;
            if (editCommentsBtn.isSelected()) return;
            if (currentEdit != null) currentEdit.deletions = true;           
        }

        public void changedUpdate(DocumentEvent e) {
            //Plain text components do not fire these events
        }
    }  // end of inner class HCEditListener
    
    
    class CitationListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            dt.citation = FamilyPanel.convertBannedCharacters(citationText.getText());             
        }

        public void removeUpdate(DocumentEvent e) {
            dt.citation = FamilyPanel.convertBannedCharacters(citationText.getText());            
        }

        public void changedUpdate(DocumentEvent e) {
            //Plain text components do not fire these events
        }
    }  // end of inner class CitationListener
    
    
    }
