/**This class creates a panel that displays the status of an Anomaly on which
 * the User has already taken action. This provides the option of editing or 
 * deleting the previous action.
 *
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class AnomalyAlreadyProcessed extends javax.swing.JPanel {
    
    MyResBundle se = Library.screenElements;
    
    public AnomalyAlreadyProcessed() {
        initComponents();
    }

    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();

        setBorder(javax.swing.BorderFactory.createTitledBorder(se.getString("alreadyProcessed")));
        setPreferredSize(new java.awt.Dimension(425, 300));  

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        setDisplay(se.getString("dyads"));
        jScrollPane1.setViewportView(jTextArea1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 373, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 260, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }
    
    void setDisplay(String text) {
        jTextArea1.setText(se.getString("alreadyProcessed1") 
                + " " + text + se.getString("alreadyProcessed2"));        
    }
    
    void addToDisplay(String moreText) {
        String newTxt = jTextArea1.getText() + "\n" + moreText;
        jTextArea1.setText(newTxt);
    }
    
    

    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;

}
