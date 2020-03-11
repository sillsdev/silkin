import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Label;
import javax.swing.JPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;

/**This class builds a frame that is displayed when the user
 * clicks on "About SILKin" on a Mac. 
 * 
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class AboutBox extends JFrame
        implements com.apple.eawt.AboutHandler, ActionListener  {
    
    protected Button okButton;
    protected Label aboutText;
    JFrame aboutFrame;
    
    @Override
    public void handleAbout(com.apple.eawt.AppEvent.AboutEvent ae) {
        aboutFrame = new JFrame();
        aboutFrame.setLayout(new BorderLayout(15, 15));
        aboutFrame.setFont(new Font ("SansSerif", Font.BOLD, 24));
        aboutFrame.setSize(400, 400);
        aboutText = new Label("                        About SILKin 2.3");
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.PAGE_AXIS));
        textPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        textPanel.add(aboutText);
        JPanel histPanel = new JPanel();
        histPanel.setLayout(new BoxLayout(histPanel, BoxLayout.PAGE_AXIS));
        String history1 = "Developed by Dr. Gary Morris for the";
        String history2 = "Summer Institute of Linguistics (SIL).";
        String history3 = "SIL Users may request support through";
        String history4 = "regional Anthropology Coordinators.";
        if (Library.screenElements != null) {
            history1 = Library.screenElements.getString("devBy1");
            history2 = Library.screenElements.getString("devBy2");            
            history3 = Library.screenElements.getString("sILHelp1");
            history4 = Library.screenElements.getString("sILHelp2");
        }
        Label hist1 = new Label(history1);
        Label hist2 = new Label(history2);
        Label hist3 = new Label(history3);
        Label hist4 = new Label(history4);
        histPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        histPanel.add(hist1);
        histPanel.add(hist2);
        histPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        histPanel.add(hist3);
        histPanel.add(hist4);
        JPanel overHist = new JPanel();
        overHist.setLayout(new BoxLayout(overHist, BoxLayout.LINE_AXIS));
        overHist.add(Box.createRigidArea(new Dimension(40, 0)));
        overHist.add(histPanel);
        overHist.add(Box.createRigidArea(new Dimension(40, 0)));
        aboutFrame.add (textPanel, BorderLayout.NORTH);
	aboutFrame.add(overHist, BorderLayout.CENTER);
	okButton = new Button("OK");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.add (okButton);
        okButton.addActionListener(this);
        aboutFrame.add(buttonPanel, BorderLayout.SOUTH);
    
        
        aboutFrame.pack();
        aboutFrame.setVisible(true);
    }

    /** When any ActionEvent occurs, this method closes the frame.
     * 
     * @param newEvent 
     */
    public void actionPerformed(ActionEvent newEvent) {
        aboutFrame.setVisible(false);
    }		
	
}

  