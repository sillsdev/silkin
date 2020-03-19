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

import java.awt.*;
import java.util.StringTokenizer;
import java.util.*;

/**SILKin's GUI is built on top of the KAES code donated my Michael Fischer; this
 * is the class that Fischer created to represent people. When SILKin began to
 * extensively modify this GUI, the {@link Individual} class was added as an extension
 * to this class. SILKin code normally uses Individual instances, but uses the methods
 * that Individual inherits from this class. Original KAES code, of course, directly 
 * accesses Person objects.
 * 
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class Person  {
    public static int id_no = 0;
    public static int size = 16; // default size;
    public static String refYear = ""; // reference year for drawing genealology
    /**An ArrayList of all the Person instances ever made. If you know the serial
     * number of any Individual, that is their index in this ArrayList.
     */
    public static ArrayList<Individual> folks = new ArrayList<Individual>();
    //  public static int folkIndex = -1;	//  used in array
    public static int maxx = -20000, maxy = -20000, minx = 20000, miny = 20000;
    int lsize = 0; // if not zero use this one - local to this entity
    int myId = ++id_no;
    public boolean drawn = false; // was this symbol drawn last time?
    String name = "New";
    String comment = "";
    String birthYY = "";
    String yobq = "9";
    String deathYY = "";
    String yodq = "9";
    /**A capital letter that is the ID of the chart on which this Person is
     * drawn. Any links to this Person will likely appear on other charts.
     */
    String homeChart = "";
    public Point location = new Point(0, 0);
    Kind sex = null;
    LinkedList marriages = null;
    Family parents = null;
    LinkedList parental_unions = null;
    public boolean selected = false;
    public static final int NOLABEL = 0;
    public static final int INITIALS = 1;
    public static final int FIRST = 2;
    public static final int LAST = 3;
    public static final int WHOLE = 4;
    public static final int NOKINTERM = 0;
    public static final int KINTERMREF = 1;
    public static final int KINTERMADR = 2;
    public static final int LETTERREF = 3;
    public static final int LETTERADR = 4;
    public static int nameLabel = INITIALS;
    public static int kinTermLabel = NOKINTERM;

    Person() {
    }// required expicit zero-arg constructor
	
	Person(Kind gend, Point l) {
		sex = gend;
		setLocation(l);
		name = "Person " + (myId -1);
	}

    void adjustLocation(int extraWidth, int extraHeight) {
        setLocation(location.x + extraWidth, 
                    location.y + extraHeight);
    }
    
    public Point getLocation() { return location; }
    
    public int getLocationX() { return location.x; }

    public int getLocationY() { return location.y; }

    /**
     * Change the location of this Person to a new point.
     * 
     * @param p the Point whose X and Y are the new coordinates.
     */
    public void setLocation(Point p) {
      setLocation(p.x, p.y);
    }
    
    /**
     * Change the location of this Person to a new point (x,y).
     * 
     * @param x the new X.
     * @param y the new Y.
     */  
    public void setLocation(int x, int y) {
      location.x = x;
      location.y = y;
      if (x > maxx) maxx = x;
      if (x < minx) minx = x;
      if (y > maxy) maxy = y;
      if (y < miny) miny = y;
    }
    
    public void setLocationX(int x) {
      setLocation(x,location.y);
    }
    
    public void setLocationY(int y) {
      setLocation(location.x,y);
    }

    public LinkedList getMarriages() {
		return marriages;
	}
		
	public void setMarriages(LinkedList m) {
		marriages = m;
	}
	
	public void addMarriage(Family f) { addMarriage((Marriage) f); } 

	
	public void addMarriage(Marriage p) {
		if (marriages == null) marriages = new LinkedList(p);
		else if (!marriages.isHere(p)) marriages.extend(p);
// update marriage
		if (p.getSpouses() == null) p.setSpouses(new LinkedList(this));
		else if (!p.getSpouses().isHere(this)) 
				p.getSpouses().extend(this);
	}

	
	public void delMarriage(Marriage p) {
		if (marriages != null && marriages.isHere(p)) {
			marriages = (LinkedList) marriages.remove(p);
			if (p.getSpouses() != null) 
				p.setSpouses((LinkedList) p.getSpouses().remove(this));
		}
	}
	 
	public void setParents(Marriage p) {
		if (p == null) {
			parents = null;
			return;
		}
		if (parental_unions == null) parental_unions = new LinkedList(p);
		else if (!parental_unions.isHere(p)) parental_unions.extend(p);
		Marriage k = parents;
		parents = (Family)p;
		if (k != null) {
			// k.delSib(this); // do nothing for now
		}
		if (p != null) p.addSib(this);
	}
	
	public Family getParents() {
		return parents;
	}
	
	public void delPerson() {
		Family k = parents;
		parents = null;
 		LinkedList pu = parental_unions;
		while (pu != null) {
			k = (Family) pu.getValue();
                        if (k != null) k.delSib(this);
			pu =  pu.getNext();
		}
                parental_unions=null;
		LinkedList m = marriages;
		while (m != null) {
			Family x = (Family) m.getValue();
			delMarriage(x);
			m =  m.getNext();
		}
                marriages=null;
	}
	
	public void marryTo(Person p) {
		Family m;
		Point pt = null;
		m = new Family(pt);
		m.addSpouse(p);
		m.addSpouse(this);
		p.addMarriage(m);
		this.addMarriage(m);
	}


	public int getSize() {
		if (lsize > 0) return lsize;
		else return size;
	}

	public void setSize(int x) {
		lsize = x;
	}
        
        public Rectangle bounds() {
		return new Rectangle(location.x,location.y,getSize(),getSize());
	}
	
     /** Set up the call to drawSymbol.
     * 
     * @param g     the graphics object on which to draw
     * @param pbounds   the boundaries within which to draw
     */
    public void drawSymbol(Graphics g, Rectangle pbounds, Color c) {
        Color x = g.getColor();
        g.setColor(c);
        drawSymbol(g, pbounds);
        g.setColor(x);
    }
    /** Modified this method for SILKin, since refYear is
     * not used. Original version below:<br/><br/>
     *<code>
     * if (!deathYY.equals(&quot;&quot;) && !refYear.equals(&quot;&quot;))
     *	return(deathYY.compareTo(refYear) &lt;= 0);<br/>
     *	else return false;</code>
     */
    public boolean hasEnded() {
        if (!deathYY.equals("")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * This is the method that writes a Person's name (or initials, or nothing)
     * and kin term (or a letter, or nothing) beneath their symbol.
     *
     * @param g the Graphics object on which to draw
     * @param theBounds the boundaries within which to draw
     */
    public void drawLabel(Graphics g, Rectangle theBounds) {
        String out = "", out1, outKTerm = "";
        StringTokenizer st = new StringTokenizer(name);
        DomainTheory dt = null;
        ArrayList<Object> terms = null;
        if (nameLabel == NOLABEL && kinTermLabel == NOKINTERM) {
            return;
        }
        if (nameLabel == FIRST) {
            if (st.hasMoreTokens()) {
                out = st.nextToken() + " ";
                while (st.hasMoreTokens()) {
                    out += st.nextToken().substring(0, 1);
                }
            } else {
                out = "<" + (myId - 1) + ">";
            }
        } else if (nameLabel == LAST) {
            if (st.hasMoreTokens()) {
                out1 = st.nextToken();
                out = "";
                while (st.hasMoreTokens()) {
                    out += out1.substring(0, 1);
                    out1 = st.nextToken();
                }
                out += " " + out1;
            } else {
                out = "<" + (myId - 1) + ">";
            }
        } else if (nameLabel == INITIALS) {
            if (st.hasMoreTokens()) {
                out = "";
                while (st.hasMoreTokens()) {
                    out += st.nextToken().substring(0, 1);
                }
            } else {
                out = "<" + (myId - 1) + ">";
            }
        } else if (nameLabel == WHOLE) {
            out = name;
            if (out.equals("") || out.equals(" ")) {
                out = "<" + (myId - 1) + ">";
            }
        }
        Individual ind = (Individual) this;
        String trm;
        if (kinTermLabel == NOKINTERM || ind.node == null) {
            outKTerm = "";
        } else {  // pick the kin term
            boolean ref = (kinTermLabel % 2) == 1;  // odd nmbrs are ref terms
            try {
                dt = (ref ? Context.getCurrent().domTheoryRef()
                        : Context.getCurrent().domTheoryAdr());
                terms = (ref ? ind.node.kinTermsRef()
                        : ind.node.kinTermsAddr());
            } catch (Exception e) {
            }  // Nothing can go wrong....
            if (terms.isEmpty()) {
                outKTerm = "";
            } else if (kinTermLabel < LETTERREF) {  // want actual kin terms
                if (terms != null && terms.size() > 0) {
                    outKTerm = (String) terms.get(0);
                    if (terms.size() > 1) {
                        for (int i = 1; i < terms.size(); i++) {
                            trm = (String) terms.get(i);
                            if (!outKTerm.contains(trm)) {
                                outKTerm += ", " + trm;
                            }

                        }
                    }
                }
            } else {  // want a letter
                if (terms.contains("Ego")) {
                    outKTerm = "Ego";
                } else {
                    TreeMap<String, String> letters = dt.getKTSymbols();
                    if (letters == null) {
                        outKTerm = "";
                    } else {
                        outKTerm = letters.get((String) terms.get(0));
                        if (terms.size() > 1) {
                            for (int i = 1; i < terms.size(); i++) {
                                outKTerm += ", " + letters.get((String) terms.get(i));
                            }
                        }
                    }
                }  // end of want-a-letter
                if (outKTerm == null) {
                    Context.breakpoint();
                }
                outKTerm = PersonPanel.deSlashify(outKTerm);
            }  // end of kin term label picker
        }
        if (out.equals("")) {  // don't print blank over a kin term
            out += (outKTerm != null ? outKTerm : "");
            outKTerm = null;
        }
        int w = g.getFontMetrics().stringWidth(out) / 2;
        int x = theBounds.x + theBounds.width / 2 - w;
        int y = theBounds.y + theBounds.height + 16;
        g.drawString(out, x, y);
        if (outKTerm != null && outKTerm.length() > 0) {
            w = g.getFontMetrics().stringWidth(outKTerm) / 2;
            x = theBounds.x + theBounds.width / 2 - w;
            y = theBounds.y + theBounds.height + 28;
            g.drawString(outKTerm, x, y);
        }
    }
	
     /** Call the symbol for this Person's sex to draw itself.
     * If this is a dead person, draw the End Symbol;
     * otherwise draw the regular symbol. Then draw you label,
     * if any.
     * 
     * @param g     the graphics object on which to draw
     * @param pbounds   the boundaries within which to draw
     */
    public void drawSymbol(Graphics g, Rectangle pbounds) {
        Rectangle myBounds = bounds();
        // myBounds is used to compute the size of the symbol
        drawn = false;

        if (pbounds.contains(location)) {
            if (hasEnded()) {
                sex.symbol.drawEndSymbol(g, myBounds);
            } else {
                sex.symbol.drawSymbol(g, myBounds);
            }
            drawLabel(g, myBounds);
            drawn = true;
        }
    }

	public void personToXML(PrintFormat pf) {
		pf.printf("  <person>"+XFile.Eol+"    <name>%s</name><id>%d</id><sex>%s</sex>"+XFile.Eol, name);
		pf.printF(myId); // id
		if (sex instanceof Female) pf.printF("Female");
		else if (sex instanceof Male) pf.printF("Male");
		else pf.printF("Neuter");
                pf.printf("    <stats><born q=\"%s\">%s</born><died q=\"%s\">%s</died></stats>"+XFile.Eol,
		yobq);
		pf.printF(birthYY.replace(' ','#'));
		pf.printF(yodq);
		pf.printF(deathYY.replace(' ','#'));

		pf.printf("    <location><x>%d</x><y>%d</y></location>"+XFile.Eol,location.x);
		pf.printF(location.y);

		if (comment.equals("")) {
			comment = "None";
		}

		pf.printf("    <comment>%s</comment>"+XFile.Eol+"  </person>"+XFile.Eol+XFile.Eol,comment);
	}

	public static void personsToXML(PrintFormat pf) {
		pf.printf("<people>"+XFile.Eol);
		
		for(Individual p : folks) {
			if (p != null) p.personToXML(pf);
		}
		pf.printf("</people>"+XFile.Eol);
	}
	
	static Female fem = new Female();
	static Male	mal = new Male();

	public static Individual readXML(XFile sFile) {
		String name, sex, born, died, comment;
		String bornqual="9", diedqual="9";
		int pid,locx,locy;
		Individual retp=null;
		
		name = sFile.readTagValue("name");
		pid = new Integer(sFile.readTagValue("id")).intValue();
		sex = sFile.readTagValue("sex");
		born = sFile.readTagValue("born").replace('#',' ');
		if (sFile.attributes.length > 1) bornqual = sFile.attributes[1][1];
		died = sFile.readTagValue("died").replace('#',' ');
		if (sFile.attributes.length > 1) diedqual = sFile.attributes[1][1];
		locx = new Integer(sFile.readTagValue("x")).intValue();
		locy = new Integer(sFile.readTagValue("y")).intValue();
		comment = sFile.readTagValue("comment");
		sFile.readUntilTag("/person");
		if (sex.equalsIgnoreCase("Female")) {
			retp = new Individual(fem, new Point(locx,locy));
		} else if (sex.equalsIgnoreCase("Male") || sex.equalsIgnoreCase("Neuter")) {
			retp = new Individual(mal, new Point(locx,locy));
		} else {
			System.out.println("Bad sex: " + sex);
			return null;
		}
		if (retp == null) {
			System.out.println("Odd error in Person.readXML: " + sex);
			return null;
		}
		retp.name = name;
		retp.myId = pid;
		retp.comment = comment;
		retp.birthYY = born;
		retp.deathYY = died;
		retp.yobq = bornqual;
		retp.yodq = diedqual;
		Person.id_no = pid; // Check this out!!!
		//  while (pid > folks.size()) folks.add(null);  
		//  insert nulls in empty slots - should never be needed 'cuz SIL keeps deleted people in their slots
		if (pid != folks.size()) {  //  in case things are really weird!
			System.out.println("Person's ID out of order in Person.readXML. \nID: " 
					+ pid + " in position " + folks.size());
			return null;
		}
		//  folks.add(retp);  //  Individual constructor inserts into folks
		return retp;
	}

    static public int findPerson(int x, int y) {
        if (folks == null) {
            return -1;
        }
        for (int i = folks.size() - 1; i >= 0; i--) {
            if (folks.get(i) != null
                    && folks.get(i).bounds().contains(x, y)
                    && folks.get(i).homeChart.equals(Context.getCurrent().currentChart)) {
                return i;
            }
        }
        return -1;
    }
}
