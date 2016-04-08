//
//	File:		AboutBox.java
//  KinshipEditor
//
//  Created by Michael D. Fischer on 11/07/2006.
//  Copyright (c) 2006, Centre for Social Anthropology and Computing, 
//  University of Kent. All rights reserved.
//
//
//  Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions
//  are met:
//
//  Redistributions of source code must retain the above copyright
//  notice, this list of conditions and the following disclaimer.
//  Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//  Neither the name of the Centre for Social Anthropology and Computing,
//  University of Kent nor the names of its contributors may be used 
//  to endorse or promote products derived from this software without
//  specific prior written permission.  THIS SOFTWARE IS PROVIDED BY THE 
//  COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
//  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
//  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
//  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS 
//  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
//  OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
//  OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
//  OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
//  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
//  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//  




import java.awt.*;
import java.awt.event.*;

public class AboutBox extends Frame
					  implements ActionListener
{
    protected Button okButton;
    protected Label aboutText;

    @SuppressWarnings("LeakingThisInConstructor")
    public AboutBox()
    {
        super();
        setLayout(new BorderLayout(15, 15));
        setFont(new Font ("SansSerif", Font.BOLD, 14));

        aboutText = new Label ("About SILKin 2.1");
        Panel textPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        textPanel.add(aboutText);
        Label history1 = new Label("Developed by Dr. Gary Morris for the Summer Institute of Linguistics (SIL).");
        Label history2 = new Label("SIL Users may request support through regional Anthropology Coordinators.");
        add (textPanel, BorderLayout.NORTH);
	add(history1, BorderLayout.CENTER);
	add(history2, BorderLayout.CENTER);	
        okButton = new Button("OK");
        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.add (okButton);
        okButton.addActionListener(this);
        add(buttonPanel, BorderLayout.SOUTH);
    }
	

    public void actionPerformed(ActionEvent newEvent) 
    {
        setVisible(false);
    }	
	
}

  