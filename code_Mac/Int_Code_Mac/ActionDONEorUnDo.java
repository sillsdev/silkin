import java.util.*;
import javax.swing.*;

/** This class creates the Action Box for when the User decides to UNDO an action
 * previously taken and recorded.
 *
 * @author  Gary Morris, Northern Virginia Community College
 *      garymorris2245@verizon.net
 *
 * Created on Aug 19, 2011, 4:29:09 PM
 */
public class ActionDONEorUnDo extends javax.swing.JPanel {

    DecisionFrame papa;
    DomainTheory dt; // instantiated by papa
    int suggNmbr;
    Issue issue;
    Context.HistoryItem histItem;
    MyResBundle els = Library.screenElements,
                msgs = Library.messages;

    /** Creates new form ActionDONEorUnDo */
    public ActionDONEorUnDo() {
        initComponents();
    }

    public ActionDONEorUnDo(DecisionFrame df) {
        papa = df;
        initComponents();
        labelComponents();
    }

    public void load(Issue iss, int nmbr) {
        //  Find the corresponding History Item so we can display what 
        //  the User can (potentially) undo.
        issue = iss;
        suggNmbr = nmbr;
        Context ctxt = Context.getCurrent();
        TreeMap<String, ArrayList<Context.HistoryItem>> lrngHist =
                (dt.addressTerms ? ctxt.learningHistoryAdr : ctxt.learningHistoryRef);
        ArrayList<Context.HistoryItem> hList = lrngHist.get(issue.kinTerm);
        autoDefInfoLabel.setText("");
        try {
            if (issue instanceof ProposedDef) {
                if (dt.theory.containsKey(issue.kinTerm)) {
                    Context.AcceptedDefPtr ptr = null;
                    for (Context.HistoryItem hi : hList) {
                        if (hi instanceof Context.AcceptedDefPtr) {
                            ptr = (Context.AcceptedDefPtr) hi;
                        }  //  if multiple defs accepted, keep the latest one                
                    }
                    priorActionText.setText(ptr.description(dt));
                    histItem = ptr;
                    if (!ptr.autoDefPairs.isEmpty()) {
                        int size = ptr.autoDefPairs.size();
                        String plural = (size == 1 ? "" : "s");
                        autoDefInfoLabel.setText(els.getString("autoApplied") + " "
                                + size + " " + els.getString("e/aPairs"));
                    }
                } else {  // theory does not have definition for this kin term
                    Context.RejectedPropDefPtr ptr = null;
                    for (Context.HistoryItem hi : hList) {
                        if (hi instanceof Context.RejectedPropDefPtr) {
                            ptr = (Context.RejectedPropDefPtr) hi;
                        }  //  if multiple defs rejected, get the latest one                
                    }
                    KinTermDef ktdef = ((ProposedDef) issue).ktd;
                    priorActionText.setText(ptr.description(ktdef));
                    histItem = ptr;
                }
                nameOfSuggLabel.setText(els.getString("alreadyPropDef"));
            } else if (issue instanceof SynonymCandidate) {
                String otherTerm = ((SynonymCandidate) issue).otherTerm();
                Context.AcceptedSynonym ac = null, potentialAC;
                for (Context.HistoryItem hi : hList) {
                    if (hi instanceof Context.AcceptedSynonym) {
                        potentialAC = (Context.AcceptedSynonym) hi;
                        if (potentialAC.synTerm.equals(otherTerm)
                                && !potentialAC.rescinded) {
                            ac = potentialAC;
                        }
                    }               
                }
                if (ac == null) {  //  must have been rejected
                    Context.RejectedSynonym reject = null, potentialReject;
                    for (Context.HistoryItem hi : hList) {
                        if (hi instanceof Context.RejectedSynonym) {
                            potentialReject = (Context.RejectedSynonym) hi;
                            if (potentialReject.synTerm.equals(otherTerm)
                                    && ! potentialReject.rescinded) {
                                reject = potentialReject;
                            }
                        }  //  if multiple rejections, get the latest one                
                    }
                    priorActionText.setText(reject.description());
                    histItem = reject;
                } else {
                    priorActionText.setText(ac.description());
                    histItem = ac;
                }
                nameOfSuggLabel.setText(els.getString("alreadyPropSyn"));
            } else if (issue instanceof UmbrellaCandidate) {
                Context.AcceptedUmbrella au = null, potentialAU;
                UmbrellaCandidate uc = (UmbrellaCandidate)issue;                    
                ArrayList<String> candSubTerms = new ArrayList<String>(uc.subTerms.keySet());
                if (dt.umbrellas.containsKey(uc.kinTerm)) {
                    for (Context.HistoryItem hi : hList) {
                        if (hi instanceof Context.AcceptedUmbrella) {
                            potentialAU = (Context.AcceptedUmbrella) hi;
                            if (potentialAU.origSubTerms.equals(candSubTerms)) {
                                au = potentialAU;
                            }
                        }  //  if multiple acceptances, get the latest one                         
                    }
                    if (au != null) {
                        priorActionText.setText(au.description());
                        histItem = au;
                    }
                }
                Context.RejectedUmbrella ru = null, potentialRU;
                if (au == null && dt.nonUmbrellas.containsKey(uc.kinTerm)) {
                    for (Context.HistoryItem hi : hList) {
                        if (hi instanceof Context.RejectedUmbrella) {
                            potentialRU = (Context.RejectedUmbrella) hi;
                            if (potentialRU.subTerms.equals(candSubTerms)) {
                                ru = potentialRU;
                            }
                        }  //  if multiple acceptances, get the latest one                         
                    }
                    if (ru != null) {
                        priorActionText.setText(ru.description());
                        histItem = ru;
                    }
                }
                if (au == null && ru == null) {  //  must be umb-to-syn
                    Context.UmbrellaIntoSyns u2s = null, potentialU2S;
                    for (Context.HistoryItem hi : hList) {
                        if (hi instanceof Context.UmbrellaIntoSyns) {
                            potentialU2S = (Context.UmbrellaIntoSyns) hi;
                            if (potentialU2S.subTerms.equals(candSubTerms)) {
                                u2s = potentialU2S;
                            }
                        }  //  if multiple conversions, get the latest one                         
                    }
                    if (u2s != null) {
                        priorActionText.setText(u2s.description());
                        histItem = u2s;
                    }else {
                        String msg = msgs.getString("noHistUmbrella");
                        throw new KSInternalErrorException(msg);
                    }
                }                
                nameOfSuggLabel.setText(els.getString("alreadyPropUmb"));
            } else if (issue instanceof OverlapCandidate) {
                OverlapCandidate oc = (OverlapCandidate)issue;
                Context.AcceptedOverlap ao = null;
                Context.RejectedOverlap ro = null;
                if (dt.overlaps.containsKey(oc.kinTerm) && 
                        ((ArrayList)dt.overlaps.get(oc.kinTerm)).contains(oc.otherTerm)) {                    
                    for (Context.HistoryItem hi : hList) {
                        if (hi instanceof Context.AcceptedOverlap) {
                            ao = (Context.AcceptedOverlap) hi;
                        }  //  if multiple overlaps accepted, get the latest one                         
                    }
                    if (ao != null) {
                        priorActionText.setText(ao.description());
                        histItem = ao;
                    }                    
                }
                if (ao == null && dt.nonOverlaps.containsKey(oc.kinTerm) && 
                        ((ArrayList)dt.nonOverlaps.get(oc.kinTerm)).contains(oc.otherTerm)) {                    
                    for (Context.HistoryItem hi : hList) {
                        if (hi instanceof Context.RejectedOverlap) {
                            ro = (Context.RejectedOverlap) hi;
                        }  //  if multiple overlaps accepted, get the latest one                         
                    }
                    if (ro != null) {
                        priorActionText.setText(ro.description());
                        histItem = ro;
                    }
                }
                if (ao == null && ro == null) {  
                    String msg = msgs.getString("noHistOverlap");
                    throw new KSInternalErrorException(msg);
                }
                nameOfSuggLabel.setText(els.getString("alreadyPropOverlap"));
            } else {
                String msg = "Processed issue (" + issue.getClass() + ") is not recognized type.";
                throw new KSInternalErrorException(msg);
            }
        } catch (Exception ex) {
            String msg = "Error encountered while searching for a Learning History Item:\n" + ex;
            msg += "\nThe issue was " + issue.toThyString();
            MainPane.displayError(msg, "Internal Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameOfSuggLabel = new javax.swing.JLabel();
        instructionLabel_1 = new javax.swing.JLabel();
        instructionLabel_2 = new javax.swing.JLabel();
        cancelBtn = new javax.swing.JButton();
        unDoBtn = new javax.swing.JButton();
        priorActionPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        priorActionText = new javax.swing.JTextArea();
        autoDefInfoLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Action Box: Already Processed / UnDo"));

        nameOfSuggLabel.setText("You have already processed this Suggestion.");

        instructionLabel_1.setText("Hit 'Cancel' (or choose a different Suggestion) to leave it alone. Or hit");

        instructionLabel_2.setText("'UnDo' if you want to modify your previous actions.");

        cancelBtn.setText("Cancel");
        cancelBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelBtnActionPerformed(evt);
            }
        });

        unDoBtn.setText("UnDo Prior Action");
        unDoBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unDoBtnActionPerformed(evt);
            }
        });

        priorActionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Prior Action: ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(255, 0, 51))); // NOI18N
        priorActionPanel.setPreferredSize(new java.awt.Dimension(448, 200));

        jScrollPane1.setPreferredSize(new java.awt.Dimension(259, 184));

        priorActionText.setColumns(20);
        priorActionText.setEditable(false);
        priorActionText.setRows(5);
        priorActionText.setText("Description of Prior Action.");
        priorActionText.setMinimumSize(new java.awt.Dimension(200, 100));
        priorActionText.setPreferredSize(new java.awt.Dimension(450, 1500));
        jScrollPane1.setViewportView(priorActionText);

        org.jdesktop.layout.GroupLayout priorActionPanelLayout = new org.jdesktop.layout.GroupLayout(priorActionPanel);
        priorActionPanel.setLayout(priorActionPanelLayout);
        priorActionPanelLayout.setHorizontalGroup(
            priorActionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 436, Short.MAX_VALUE)
        );
        priorActionPanelLayout.setVerticalGroup(
            priorActionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 172, Short.MAX_VALUE)
        );

        autoDefInfoLabel.setText("AutoDef Info");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(instructionLabel_1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(instructionLabel_2)
                        .add(layout.createSequentialGroup()
                            .add(cancelBtn)
                            .add(128, 128, 128)
                            .add(unDoBtn))
                        .add(priorActionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(autoDefInfoLabel)
                    .add(nameOfSuggLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(nameOfSuggLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(instructionLabel_1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(instructionLabel_2)
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(cancelBtn)
                    .add(unDoBtn))
                .add(18, 18, 18)
                .add(priorActionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(autoDefInfoLabel)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    void labelComponents() {
        setBorder(javax.swing.BorderFactory.createTitledBorder(els.getString("aBoxUnDo")));
        nameOfSuggLabel.setText(els.getString("alreadySugg"));
        instructionLabel_1.setText(els.getString("instruct1"));
        instructionLabel_2.setText(els.getString("instruct2"));
        cancelBtn.setText(els.getString("cancel"));
        unDoBtn.setText(els.getString("unDoPriorAction"));
        priorActionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, 
                els.getString("priorAction") + ": ", 
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, 
                new java.awt.Font("Lucida Grande", 0, 13), new java.awt.Color(255, 0, 51))); // NOI18N
        priorActionText.setText(els.getString("descriptionPriorAction"));
        autoDefInfoLabel.setText(els.getString("autoDefInfo"));        
    }
    
    private void cancelBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnActionPerformed
        papa.reset();
    }//GEN-LAST:event_cancelBtnActionPerformed

    private void unDoBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unDoBtnActionPerformed
        histItem.unDo(dt);
        String msg = histItem.unDoDescription();
        JOptionPane.showMessageDialog(papa, msg, 
                msgs.getString("reversalCompleted"), JOptionPane.PLAIN_MESSAGE);
        papa.markUnProcessed(suggNmbr);
        papa.reset();        
    }//GEN-LAST:event_unDoBtnActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel autoDefInfoLabel;
    private javax.swing.JButton cancelBtn;
    private javax.swing.JLabel instructionLabel_1;
    private javax.swing.JLabel instructionLabel_2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel nameOfSuggLabel;
    private javax.swing.JPanel priorActionPanel;
    private javax.swing.JTextArea priorActionText;
    private javax.swing.JButton unDoBtn;
    // End of variables declaration//GEN-END:variables
}
