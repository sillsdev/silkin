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

import java.io.*;
import java.awt.Frame;
import java.awt.FileDialog;
import java.lang.*;
import java.util.StringTokenizer;
import java.net.*;

//			theInfo.setCreator(0x43584D4C); // CXML 
//			theInfo.setType(0x54455854); // TEXT

/**This class is from the original KAES code. It is not used in SILKin.
 * 
 * @author Michael D. Fischer
 */
public class XFile extends Object {

    public static final String Eol = System.getProperty("line.separator");
    public File aFile = null;
    //	DataInputStream diStream;
    BufferedReader diStream = null;

    public StreamTokenizer tokenStream = null;
    public Writer dataWriter = null;
    public final static int READ = 0;
    public final static int WRITE = 1;
    public String errMessage;

    public int Delimiter = ',';

    int xMode = READ; // 0 = read 1 = write
    boolean isOpen = false;

    public XFile() {
        xMode = -1;
    }

    public boolean OpenURL(URL theURL) {
        if (theURL != null) {
            BufferedInputStream theStream;
            try {
                InputStream a = theURL.openStream();
                theStream = new BufferedInputStream(a);

            } catch (java.io.IOException e) {
                theStream = null;
                System.out.println("URL Stream is NULL!");
                return false;
            }
            try {
                diStream = new BufferedReader(new InputStreamReader(theStream));
            } catch (Exception e) {
                errMessage = "Stream is not Available";
                return (false);
            }
            isOpen = true;
            return true;
        }
        return false;
    }

    public boolean Open() {
        if (xMode == -1) {
            errMessage = "Must set mode using setMode(read=0,write=1)";
            return false;
        }
        return (Open(xMode));
    }

    public boolean Open(int omode) {

        if (isOpen || diStream != null) {
            return omode == xMode;
        }

        xMode = omode;

        if (aFile == null) {
            Choose(omode);
        }
        if (xMode == 0) {
            FileInputStream theStream;

            if (aFile.canRead()) {
                try {
                    theStream = new FileInputStream(aFile);
                } catch (Exception e) {
                    errMessage = "File is not Available";
                    return (false);
                }
                try {
                    //BufferedInputStream biStream = ;
                    diStream = new BufferedReader(new InputStreamReader(theStream));
                } catch (Exception e) {
                    errMessage = "Stream is not Available";
                    return (false);
                }
                isOpen = true;
                return true;
            }
            return false;
        } else {
            FileOutputStream theStream;
            //
            try {
                if (aFile.exists()) {
                    theStream = new FileOutputStream(aFile);
                } else {
                    theStream = new FileOutputStream(aFile.getPath());
                }
            } catch (Exception e) {
                errMessage = "Write File is not Available";
                return (false);
            }
            // if (aFile.canWrite()) {
            setFileTypeAndCreator();
            try {
                dataWriter = new BufferedWriter(new OutputStreamWriter(theStream));
            } catch (Exception e) {
                errMessage = "Stream is not Available";
                return (false);
            }
            isOpen = true;
            return true;
			 //	}
            //return false;
        }
    }

    public String[] MakeFields(String aLine, int format[]) {
        int i, nf;
        /*	while (st.hasMoreTokens()) {
         println(st.nextToken());
         }*/

        if (aLine == null) {
            errMessage = "EOF";
            return null;
        } else {
            StringTokenizer st = new StringTokenizer(aLine);
            nf = st.countTokens(); // BreakFields(aLine);
            String[] retStrs = new String[nf];
            for (i = 0; i < nf; i++) {
                retStrs[i] = st.nextToken(); // aLine.substring(theFields[i],theFields[i+1]-2);
            }
            if (format == null || format[0] == -1) {
                return retStrs;
            } else {
                String[] xretStrs = new String[format.length];
                for (i = 0; i < format.length; i++) {
                    if (format[i] < nf) {
                        xretStrs[i] = retStrs[format[i]];
                    } else {
                        errMessage = "Variable out of bounds";
                        xretStrs[i] = "";
                    }
                }
                return xretStrs;
            }
        }
    }

    public String ReadLine() {
        if (diStream != null) {
            try {
                return (diStream.readLine());
            } catch (Exception e) {
                errMessage = "Probably at End of File";
                return (null);
            }
        } else {
            errMessage = "Stream not open";
            return (null);

        }
    }

    public String ReadLineNN() {
        String aLine;
        do {
            aLine = this.ReadLine();
            if (aLine != null) {
                if (!aLine.equals("")) {
                    break;
                }
            }
        } while (aLine != null);
        return (aLine);
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean Choose(int omode) {
        return Choose(omode, "", "");
    }

    public boolean Choose(int omode, String fileName, String aMessage) {
        //	System.out.println(System.getProperty("os.name"));
        xMode = omode;
        if (xMode == -1) {
            return false;
        }
        Frame aFrame = new Frame();
        if (aMessage.equals("")) {
            if (xMode == WRITE) {
                aMessage = "Write to file ...";
            } else {
                aMessage = "Read from file ...";
            }
        }
        // FileDialog aFD = new FileDialog(aFrame,aMessage,omode); // should be this but a bug in read mode
        FileDialog aFD = new FileDialog(aFrame, aMessage, xMode); // bug workaround ... user must type in
        aFD.setFile(fileName);
        try {
            aFD.show();

            aFile = new File(aFD.getDirectory().substring(0, aFD.getDirectory().length() - 1), aFD.getFile());
        } catch (Exception e) {
            errMessage = "Cancelled or error";
            return false;
        }
        return true;
    }

    synchronized void setFileTypeAndCreator() {
		//	com.apple.MacOS.FInfo theInfo = new com.apple.MacOS.FInfo(new com.apple.MacOS.FSSpec(aFile));
        //	theInfo.setCreator(creator); // CXML
        //	theInfo.setType(type); // TEXT
    }

    public boolean Close() {
        isOpen = false;
        if (xMode == 0) {
            try {
                diStream.close();
                diStream = null;
            } catch (Exception e) {
                errMessage = "Can not Close Read Stream";
                return false;
            }
        } else {
            try {
                dataWriter.close();
                dataWriter = null;
            } catch (Exception e) {
                errMessage = "Can not Close Write Stream";
                return false;
            }
        }
        setFileTypeAndCreator();

        return true;
    }

    public boolean WriteString(String line) {
        try {
            dataWriter.write(line);
            return true;
        } catch (Exception e) {
            errMessage = "Can not Write Line";
            return false;
        }
    }

    public boolean WriteBytes(String line) {
        try {
            dataWriter.write(line); // probably fix  .. translate to chars!
            return true;
        } catch (Exception e) {
            errMessage = "Can not write bytes";
            return false;
        }
    }

    private char c[] = new char[1];

    public String getStringToken() {
        if (tokenStream == null) {
            return null;
        }
        try {
            tokenStream.nextToken();
            String k = null;
            switch (tokenStream.ttype) {
                case StreamTokenizer.TT_WORD:
                    k = tokenStream.sval;
                    break;
                case StreamTokenizer.TT_NUMBER:
                    k = String.valueOf(tokenStream.nval);
                    break;
                case StreamTokenizer.TT_EOL:
                    k = Eol;
                    break;
                case StreamTokenizer.TT_EOF:
                    k = null;
                    break;
                default:
                    c[0] = (char) tokenStream.ttype;
                    k = new String(c);
                    break;
            }
            return k;
        } catch (Exception e) {
        }
        return null; // null as eof
    }

    public Long getLong() { // return next long in stream ... will skip other stuff
        Double p = getDouble();
        if (p == null) {
            return null;
        }
        return new Long(p.longValue());
    }

    public Double getDouble() { // return next double in stream ... will skip other stuff
        Token tok;
        if (tokenStream == null) {
            return null;
        }
        for (;;) {
            try {
                tokenStream.nextToken();
                String k = null;
                switch (tokenStream.ttype) {
                    case StreamTokenizer.TT_WORD:
                        k = tokenStream.sval;
                        break;
                    case StreamTokenizer.TT_NUMBER:
                        return new Double(tokenStream.nval);
                    case StreamTokenizer.TT_EOL:
                        k = Eol;
                        break;
                    case StreamTokenizer.TT_EOF:
                        k = null;
                        break;
                    default:
                        c[0] = (char) tokenStream.ttype;
                        k = new String(c);
                        break;
                }
            } catch (Exception e) {
                return null;
            }
        }
    }

    public Token getToken() {
        Token tok;
        if (tokenStream == null) {
            return null;
        }
        try {
            tokenStream.nextToken();
            String k = null;
            switch (tokenStream.ttype) {
                case StreamTokenizer.TT_WORD:
                    k = tokenStream.sval;
                    break;
                case StreamTokenizer.TT_NUMBER:
                    k = String.valueOf(tokenStream.nval);
                    break;
                case StreamTokenizer.TT_EOL:
                    k = Eol;
                    break;
                case StreamTokenizer.TT_EOF:
                    k = null;
                    break;
                default:
                    c[0] = (char) tokenStream.ttype;
                    k = new String(c);
                    break;
            }
            return new Token(k, tokenStream.ttype);
        } catch (Exception e) {
        }
        return null; // null as eof
    }

    /**
     * Reads the next SGML style tag from the Xfile DataInputStream specified
     * input stream with the specified buffer size.
     *
     * @return String[] containing the tag and any parameters returns null if
     * EOF reached;
     * @since CSAC
     */
    public String[][] readTag() {
        return readTag(false);
    }

    /**
     * Reads the next SGML style tag from the Xfile DataInputStream specified
     * input stream with the specified buffer size.
     *
     * @return String[] containing the tag and any parameters returns null if
     * EOF reached;
     * @param start have we already read the '<'
     * @since CSAC
     */
    String[][] attributes = null;

    public String[][] readTag(boolean start) {
        java.util.Vector h = new java.util.Vector(10);
        char c;
        StringBuffer s = new StringBuffer(100);
        StringBuffer a = new StringBuffer(100);
        StringBuffer av = new StringBuffer(300);
        String[][] r = null;
        try {
            if (!start) {
                while (diStream.read() != '<');
            }
            while ((c = (char) diStream.read()) != '>' && c != ' ') {
                s.append(c);
            }
            if (c == '>') {
                r = new String[1][1];
                r[0][0] = s.toString();
            } else {
                while ((c = (char) diStream.read()) != '>') {
                    a.setLength(0);
                    av.setLength(0);
                    while (c == ' ') {
                        c = (char) diStream.read();
                    }
                    if (c == '>') {
                        break;
                    }
                    while (c != ' ' && c != '=' && c != '>') {
                        a.append(c);
                        c = (char) diStream.read();
                    }
                    if (c == '>') {
                        break;
                    }
                    while (c == '"' || c == '=' || c == ' ') {
                        c = (char) diStream.read();
                    }
                    while (c != '"' && c != '>') {
                        av.append(c);
                        c = (char) diStream.read();
                    }
                    if (c == '>') {
                        h.addElement(a.toString());
                        h.addElement(av.toString());
                        break;
                    }
                    h.addElement(a.toString());
                    h.addElement(av.toString());
                }
                r = new String[h.size() / 2 + 1][2];
                r[0][0] = s.toString();
                for (int i = 0; i < (h.size() / 2); i++) {
                    r[i + 1][0] = (String) h.elementAt(i * 2);
                    r[i + 1][1] = (String) h.elementAt(i * 2 + 1);
                }
            }
        } catch (IOException e) {
            errMessage = "EOF before tag found or finished";
            return null;
        }
        attributes = r;
        return r;
    }

    public boolean readUntilTag(String tag) {
        return readUntilTag(tag, false);
    }

    public boolean readUntilTag(String tag, boolean readLBracket) {
        String x[][];
        for (;;) {
            x = readTag(readLBracket);
            readLBracket = false;
            if (x == null) {
                return false; // not found
            }
            if (x[0][0].equalsIgnoreCase(tag)) {
                //		System.out.println(" readUntilTag: "+x[0]);
                return true;
            }
        }
    }

    public String readTagValue(String tag) {
        return readTagValue(tag, false);
    }

    public String readTagValue(String tag, boolean readLBracket) {
        StringBuffer a = new StringBuffer(100);
        char c;
        if (!readUntilTag(tag, readLBracket)) {
            return null;
        }
        try {
            while ((c = ((char) diStream.read())) != '<') {
                a.append(c);
            }
            while (((char) diStream.read()) != '>');
        } catch (Exception e) {
            errMessage = "EOF before tag found or finished";
            return null;
        }
// 		System.out.println(" readTagValue: "+a.toString().trim());
        return a.toString().trim();
    }

    public String readThisTagValue() {
        StringBuffer a = new StringBuffer(100);
        char c;
        try {
            while ((c = ((char) diStream.read())) != '<') {
                a.append(c);
            }
            while (((char) diStream.read()) != '>');
        } catch (Exception e) {
            errMessage = "EOF before tag found or finished";
            return null;
        }
        // 		System.out.println(" readThisTagValue: "+a.toString().trim());
        return a.toString().trim();
    }

    public String getFileName() {
        if (aFile == null) {
            return "";
        }
        return aFile.getName();
    }

    public String getParentDirectory() {
        if (aFile == null) {
            return "";
        }
        if (aFile.getParent() == null) {
            return "";
        }
        return aFile.getParent();
    }
}
