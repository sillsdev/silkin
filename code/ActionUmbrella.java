import javax.swing.*;
import java.util.*;

/** This class creates the Action Box for a Proposed Definition in the
 * {@link DecisionFrame} whenever the suggestion chosen by User is a
 * ProposedDef.
 *
 * @author  Gary Morris, University of Pennsylvania		garymorris2245@verizon.net
 *
 * Created on Feb 23, 2011, 3:21:57 PM
 */
public class ActionUmbrella extends JPanel {
    // Logical variables.  GUI variables at end of file.
    DecisionFrame papa;
    TreeMap<String, ArrayList<Context.HistoryItem>> learningHistory;
    UmbrellaCandidate umb;
    int suggNmbr;
    DomainTheory dt = null;  // set externally by DecisionFrame
    ArrayList<String> priorSubTerms;


    /** Creates new form ActionUmbrella */
    public ActionUmbrella() {
        initComponents();
    }

    /** Creates new ActionUmbrella linked to parent frame */
    public ActionUmbrella(DecisionFrame frame) {
        initComponents();
        papa = frame;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        actionBtnGrp = new javax.swing.ButtonGroup();
        acceptBtn = new javax.swing.JRadioButton();
        rejectBtn = new javax.swing.JRadioButton();
        noActionBtn = new javax.swing.JRadioButton();
        synonymBtn = new javax.swing.JRadioButton();
        commentsScrollPane = new javax.swing.JScrollPane();
        comments = new javax.swing.JTextArea();
        doneBtn = new javax.swing.JButton();
        umbTermLabel = new javax.swing.JLabel();
        umbTerm = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        subTerms = new javax.swing.JTextArea();
        subTermsLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Action Box: Umbrella"));

        actionBtnGrp.add(acceptBtn);
        acceptBtn.setText("Accept Umbrella");
        acceptBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptBtnActionPerformed(evt);
            }
        });

        actionBtnGrp.add(rejectBtn);
        rejectBtn.setText("Reject Umbrella");
        rejectBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rejectBtnActionPerformed(evt);
            }
        });

        actionBtnGrp.add(noActionBtn);
        noActionBtn.setSelected(true);
        noActionBtn.setText("No Action");
        noActionBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noActionBtnActionPerformed(evt);
            }
        });

        actionBtnGrp.add(synonymBtn);
        synonymBtn.setText("These are really Synonyms");
        synonymBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                synonymBtnActionPerformed(evt);
            }
        });

        commentsScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Notes: "));

        comments.setColumns(20);
        comments.setRows(5);
        commentsScrollPane.setViewportView(comments);

        doneBtn.setText("Done");
        doneBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doneBtnActionPerformed(evt);
            }
        });

        umbTermLabel.setText("Umbrella Term:");

        umbTerm.setText("____________________");

        subTerms.setColumns(20);
        subTerms.setRows(5);
        jScrollPane1.setViewportView(subTerms);

        subTermsLabel.setText("Sub-Terms:");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                            .add(commentsScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                            .addContainerGap())
                        .add(layout.createSequentialGroup()
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(acceptBtn)
                                .add(noActionBtn))
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 109, Short.MAX_VALUE)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                .add(rejectBtn)
                                .add(synonymBtn))
                            .addContainerGap(56, Short.MAX_VALUE))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                            .add(doneBtn)
                            .add(30, 30, 30)))
                    .add(layout.createSequentialGroup()
                        .add(29, 29, 29)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(layout.createSequentialGroup()
                                .add(umbTermLabel)
                                .add(18, 18, 18)
                                .add(umbTerm))
                            .add(layout.createSequentialGroup()
                                .add(subTermsLabel)
                                .add(18, 18, 18)
                                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 323, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(54, 54, 54))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(acceptBtn)
                    .add(rejectBtn))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(noActionBtn)
                    .add(synonymBtn))
                .add(35, 35, 35)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(umbTermLabel)
                    .add(umbTerm))
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(41, 41, 41)
                        .add(subTermsLabel))
                    .add(layout.createSequentialGroup()
                        .add(18, 18, 18)
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 61, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(commentsScrollPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(doneBtn))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void load(UmbrellaCandidate uc, int nmbr) {
        umb = uc;
        suggNmbr = nmbr;
        if (dt.addressTerms) {
            learningHistory = dt.ctxt.learningHistoryAdr;
        }else {
            learningHistory = dt.ctxt.learningHistoryRef;
        }
        // fill in primary term and subTerms
        umbTerm.setText(umb.kinTerm);
        Iterator subTmIter = umb.subTerms.keySet().iterator();
        String subText = (String)subTmIter.next();
        while (subTmIter.hasNext()) {
            subText += ", " + (String)subTmIter.next();
        }
        subTerms.setText(subText);
        priorSubTerms = PersonPanel.getKinTerms(subText);
        noActionBtn.setSelected(true);
        comments.setText("");
    }


    private void acceptBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptBtnActionPerformed
        subTerms.setEditable(true);
    }//GEN-LAST:event_acceptBtnActionPerformed

    private void noActionBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noActionBtnActionPerformed
      // No action; the button serves as a switch
    }//GEN-LAST:event_noActionBtnActionPerformed

    private void rejectBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rejectBtnActionPerformed
        subTerms.setEditable(false);
    }//GEN-LAST:event_rejectBtnActionPerformed

    private void synonymBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_synonymBtnActionPerformed
        // No action; the button serves as a switch
    }//GEN-LAST:event_synonymBtnActionPerformed

    private void doneBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doneBtnActionPerformed
        ArrayList<String> newSubTerms = PersonPanel.getKinTerms(subTerms.getText());
        if (!acceptBtn.isSelected() && !newSubTerms.equals(priorSubTerms)) {
            String msg = "You may only edit the Sub-Terms if you are accepting this umbrella.\n";
            msg += "If you take No Action or Reject this suggestion, the suggestion is unchanged.\n";
            msg += "To Accept with these edited Sub-Terms, click 'Accept' and then 'Done.'";
            JOptionPane.showMessageDialog(papa, msg, "Operation Not Allowed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Context.HistoryItem item;
        if (acceptBtn.isSelected()) {
            item = new Context.AcceptedUmbrella(umb.kinTerm, UDate.today(), "false",
                   FamilyPanel.convertBannedCharacters(comments.getText()),
                   priorSubTerms, newSubTerms);
            ((Context.AcceptedUmbrella)item).addedSubTerms = postToDT();
        }else if (rejectBtn.isSelected()) {
            item = new Context.RejectedUmbrella(umb.kinTerm, UDate.today(), "false",
                   FamilyPanel.convertBannedCharacters(comments.getText()),
                   priorSubTerms);
            postToDT();
        }else if (synonymBtn.isSelected()) {
            Object[] terms = new Object[newSubTerms.size() +1];
            int cntr = 1;
            terms[0] = umb.kinTerm;
            for (String term : newSubTerms) {
                terms[cntr++] = term;
            }
            String msg = "Choose below the primary term.\n" + 
                    "All others are its synonyms.";
            int ch = JOptionPane.showOptionDialog(papa, msg,
                    "Pick the Primary Term",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    terms,
                    terms[0]);
            if (ch == JOptionPane.CLOSED_OPTION) {
                return;
            }
            String keyTerm = (String)terms[ch];
            ArrayList<String> newSubs = new ArrayList<String>(newSubTerms);
            newSubs.add(umb.kinTerm);
            newSubs.remove(keyTerm);
            item = new Context.UmbrellaIntoSyns(umb.kinTerm, UDate.today(), "false",
                   FamilyPanel.convertBannedCharacters(comments.getText()),
                   priorSubTerms, newSubs, keyTerm);
            for (String sub : newSubs) {
                dt.synonyms.put(sub, keyTerm);
            }
        }else return;  //  No Action
        item.postToHistory(learningHistory);
        papa.markProcessed(suggNmbr);
        papa.reset();
    }//GEN-LAST:event_doneBtnActionPerformed

    
    ArrayList<String> postToDT() {
        TreeMap theMap = null;
        ArrayList<String> subs = null, newAdds = new ArrayList<String>();
        if (acceptBtn.isSelected()) {
            if (dt.umbrellas == null) {
                dt.umbrellas = new TreeMap();
            }
            theMap = dt.umbrellas;
            subs = PersonPanel.getKinTerms(subTerms.getText());
        }else if (rejectBtn.isSelected()) {
            if (dt.nonUmbrellas == null) {
                dt.nonUmbrellas = new TreeMap();
            }
            theMap = dt.nonUmbrellas;
            subs = priorSubTerms;
        }
        if (theMap.get(umb.kinTerm) == null) {
            theMap.put(umb.kinTerm, new ArrayList<String>());
        }
        ArrayList<String> lst = (ArrayList<String>)theMap.get(umb.kinTerm); 
        for (String s : subs) {
            if (!lst.contains(s)) {
                lst.add(s);
                newAdds.add(s);
            }
        }
        return newAdds;
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton acceptBtn;
    private javax.swing.ButtonGroup actionBtnGrp;
    private javax.swing.JTextArea comments;
    private javax.swing.JScrollPane commentsScrollPane;
    private javax.swing.JButton doneBtn;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton noActionBtn;
    private javax.swing.JRadioButton rejectBtn;
    private javax.swing.JTextArea subTerms;
    private javax.swing.JLabel subTermsLabel;
    private javax.swing.JRadioButton synonymBtn;
    private javax.swing.JLabel umbTerm;
    private javax.swing.JLabel umbTermLabel;
    // End of variables declaration//GEN-END:variables

}
