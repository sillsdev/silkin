
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JComboBox;
import java.util.*;
import java.io.*;

/**
 * This class is part of the SILKin program's GUI. It is modeled after the
 * KAES KinshipEditor by Michael D. Fischer of the Centre for Social 
 * Anthropology and Computing, University of Kent.
 * Those portions taken from his code are copyrighted by him; all rights
 * reserved. See his complete copyright statement and terms of re-use in
 * e.g. KinshipEditor.java in this package.
 *
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class ChartPanel extends JPanel implements MouseInputListener {
    // class variables

    static SIL_Edit edWin;
    /**
     * The dirty bit is TRUE when changes have been made to the Chart
     * since the last file save.
     */
    public boolean dirty = false;
    static String EOL = System.getProperty("line.separator");
    static boolean doIndexes = true;
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
    public static final int GAP = 5;
    // instance variables
    java.awt.List personMenu;
    Point lastLoc = new Point(-1, -1);
    int     whichFolk = -1,
            priorAlter = -1,
            whichLink = -1,
            priorLink = -1,
            whichKnot = -1,
            tiedKnot = -1,
            whichHalf = -1;
    Line selectLine;
    int lastFolk = -1;
    int lastKnot = -1;
    boolean editable = true,
            dragged = false,
            resize = false,
            distinctAdrTerms = false,
            recomputingDyads = false,
            makingSpecialRelationship = false, 
            loading = false;
    int originX = 0;
    int originY = 0;
    int refYear = 0;
    int nameLabel = INITIALS;
    int kinTermLabel = NOKINTERM;
    ArrayList<Individual> draggedPersons = new ArrayList<Individual>();
    ArrayList<Family> draggedMarriages = new ArrayList<Family>();
    ArrayList<Link> draggedLinks = new ArrayList<Link>();
    Point lastPersonLoc = null;
    Dimension area = new Dimension(0, 0);
    ArrayList<Individual> reSizInds = new ArrayList<Individual>();
    ArrayList<Family> reSizFams = new ArrayList<Family>();
    ArrayList<Link> reSizLinks = new ArrayList<Link>();
    Individual parent_Initiator = null, child_Recipient = null;
    UserDefinedProperty chosenUDP = null;
    static Font chartFont = new Font("Dialog", Font.PLAIN, 12);  // Default font
    MyResBundle se = Library.screenElements,
                msgs = Library.messages;
    

    public void init(SIL_Edit k) {
        edWin = k;
        setLayout(null);
        personMenu = new java.awt.List(5, false);
        personMenu.add(se.getString("female"));
        personMenu.add(se.getString("male"));
        personMenu.add(se.getString("union"));
        personMenu.add(se.getString("link2"));
        personMenu.add(se.getString("drawSpecial"));
        personMenu.setVisible(false);
        personMenu.setSize(155, 90);
        add(personMenu);
        personMenu.setFont(new Font("Dialog", Font.PLAIN, 10));
        selectLine = null;
        addMouseListener(this);
        addMouseMotionListener(this);
    }

//  Methods required for MouseListener interface
    public void mousePressed(MouseEvent e) {
        KinshipEditor_MouseDown(e);
    }

    public void mouseReleased(MouseEvent e) {
        KinshipEditor_MouseUp(e);
        dragged = false;
        if (!reSizInds.isEmpty()) {
            checkIndArea(reSizInds);
            reSizInds.clear();
        }
        if (!reSizFams.isEmpty()) {
            checkFamArea(reSizFams);
            reSizFams.clear();
        }
        if (!reSizLinks.isEmpty()) {
            checkLinkArea(reSizLinks);
            reSizLinks.clear();
        }
    }

    public void mouseEntered(MouseEvent e) {
    } //  Not needed.

    public void mouseExited(MouseEvent e) {
    }  //  Not needed.

    public void mouseClicked(MouseEvent e) {
    } //  Not needed.

//  Methods required for MouseMotionListener interface
    public void mouseDragged(MouseEvent e) {
        KinshipEditor_MouseDrag(e);
        dragged = true;  //  Will flip to false when button released.
    }

    public void mouseMoved(MouseEvent e) {
    }  //  Not Needed


    public boolean handleEvent(Event event) {
        event.x -= originX;
        event.y -= originY;
        if (event.target == personMenu && event.id == Event.LIST_SELECT) {
            personMenu_ListSelect(event);
            return true;
        }
        return super.handleEvent(event);
    }

    /** returns the labeling mode as an int
     **/
    public int getNameLabel() {
        return nameLabel;
    }

    /** Sets up the name labeling mode. These are code symbols, not text 
     *  seen by the User.
     **  @param c the label to interpret
     */
    public void setNameLabel(String c) {
        if (c.equals("Initials")) {
            nameLabel = INITIALS;
        } else if (c.equals("First")) {
            nameLabel = FIRST;
        } else if (c.equals("Last")) {
            nameLabel = LAST;
        } else if (c.equals("Whole")) {
            nameLabel = WHOLE;
        } else {
            nameLabel = NOLABEL;
        }
        Person.nameLabel = nameLabel;
        dirty = true;
        repaint();
    }

    /** sets up the kin term labeling mode
     **  @param c the label to interpret
     **/
    public void setKTmLabel(String c) {
        // These are code symbols, not text seen by the User.
        if (c.equals("KinTermRef")) {
            kinTermLabel = KINTERMREF;
        } else if (c.equals("KinTermAdr")) {
            kinTermLabel = KINTERMADR;
        } else if (c.equals("LetterRef")) {
            kinTermLabel = LETTERREF;
        } else if (c.equals("LetterAdr")) {
            kinTermLabel = LETTERADR;
        } else {
            kinTermLabel = NOKINTERM;
        }
        Person.kinTermLabel = kinTermLabel;
        dirty = true;
        repaint();
    }

    /** sets the reference year in the Person and Marriage objects
     **  @param x the reference year
     **/
    public void setRefYear(int x) {
        refYear = x;
        if (refYear <= 0) {
            Person.refYear = "";
            Marriage.refYear = "";
        } else {
            Person.refYear = refYear + "";
            Marriage.refYear = refYear + "";
        }
    }

    public void setOrigin(int x, int y) {
        originX = x;
        originY = y;
    }  //  Only used once, for 0,0.   ???

    int mouseX, mouseY, theInd, theLink, theFam ;
    Context.SpecRelTriple theSpecRel;
    
    /**
     * This runs AFTER the personMenu_ListSelect method; if User has created
     * a chartable UDP it calls for follow-up processing.
     * 
     * @param event the mouse_down event
     */
    public void KinshipEditor_MouseDown(MouseEvent event) {
        mouseX = event.getX();
        mouseY = event.getY();
        theInd = Person.findPerson(mouseX, mouseY);
        theLink = Link.findLink(mouseX, mouseY);
        theFam = findMarriage(mouseX, mouseY);
        if (makingSpecialRelationship) {
            captureAdoption();
            return;
        }

        if ((theInd + theLink + theFam) > -3) { // SOMETHING was clicked
            if (personMenu.isShowing()) {
                personMenu.setVisible(false);
                edWin.loadingCharts = false;
            }
            try {
                if (editable) {
                    edWin.getPPanel().fireAlterFirstNamesFocusLost();
                    edWin.storeInfo();
                }
                repaint();
            } catch (Exception pe) {
                displayError(pe);
                return;
            }  //  If no exception, proceed to switch

            if (theInd >= 0) { //  CLICKED ON A PERSON            
                if (whichFolk == theInd) {
                    lastLoc = new Point(mouseX, mouseY);
                } else {
                    priorAlter = whichFolk;
                    whichFolk = theInd;
                    edWin.getPPanel().dirty = true;
                }
                lastPersonLoc = new Point(Person.folks.get(theInd).location);
                whichKnot = -1;
                whichLink = -1;
                return;
            } else {
                whichFolk = -1;
            }
            if (theLink >= 0) { // CLICKED ON A LINK
                if (whichLink == theLink) {
                    lastLoc = new Point(mouseX, mouseY);
                } else {
                    priorLink = whichLink;
                    whichLink = theLink;
                    edWin.getPPanel().dirty = true;
                }
                lastPersonLoc = new Point(Context.current.linkCensus.get(theLink).location);
                whichKnot = -1;
                whichFolk = -1;
                return;
            } else {
                whichLink = -1;
            }
            if (theFam >= 0) { //  CLICKED ON A MARRIAGE
                if (whichKnot == theFam) {
                    lastLoc = new Point(mouseX, mouseY);
                } else {
                    whichKnot = theFam;
                    edWin.getFPanel().dirty = true;
                }
                whichFolk = -1;
                whichLink = -1;
                return;
            } else {
                whichKnot = -1;
            }
        }
        //  CLICKED ON A BLANK AREA
        try {
            edWin.getPPanel().fireAlterFirstNamesFocusLost();
            edWin.clearInfo();
        } catch (Exception pe) {
            displayError(pe);
            return;
        }  // theoretically can't happen
        if (personMenu.isShowing()) {
            personMenu.setVisible(false);
            lastLoc = new Point(-1, -1);
            try {
                edWin.storeInfo();
            } catch (Exception pe) {
                displayError(pe);
                return;
            }
            edWin.getPPanel().displayClues();
        } else if (editable) {
            try {
                edWin.storeInfo();
            } catch (Exception pe) {
                displayError(pe);
                return;
            }
            lastLoc = new Point(mouseX, mouseY);
            personMenu.setLocation(mouseX + originX, mouseY + originY);
            personMenu.deselect(personMenu.getSelectedIndex());
            personMenu.setVisible(true);
        }
    }
    
    /**
     * Gather information about the persons clicked on as 'parent' or 'child' in
     * this special relationship, then call the appropriate method to take the
     * next step in creating the chartable UDP.
     */
    public void captureAdoption() {
        // Alternate action for Mouse_Down when designating adoption participants
        if (parent_Initiator == null) { // selected the edWin(s)
            if (theInd > -1) {
                parent_Initiator = Context.current.individualCensus.get(theInd);
                theSpecRel.parent = parent_Initiator;
            } else if (theLink > -1) {
                Link lk = Context.current.linkCensus.get(theLink);
                parent_Initiator = lk.personPointedTo;
                theSpecRel.parent = lk;
            } else if (theFam > -1) {
                Family fam = Context.current.familyCensus.get(theFam);
                if (fam.husband == null && fam.wife == null) {
                    String msg = msgs.getString("emptyFamily");
                    Object[] btns = {se.getString("oK"), se.getString("cancel")};
                    int answer = JOptionPane.showOptionDialog(edWin, msg,
                            msgs.getString("invalidDesignation"),
                            JOptionPane.OK_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null, btns, btns[0]);
                    if (answer == JOptionPane.CANCEL_OPTION) {
                        cancelAdoption();
                        return;
                    }
                }
                theSpecRel.parent = fam;
                parent_Initiator = (fam.wife == null ? fam.husband : fam.wife);
                //  we'll make a 2nd UDP for the husband later
            } else {  //  Nothing good was clicked
                String msg = msgs.getString("emptySpace");
                Object[] btns = {se.getString("oK"), se.getString("cancel")};
                int answer = JOptionPane.showOptionDialog(edWin, msg,
                        msgs.getString("invalidDesignation"),
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, btns, btns[0]);
                if (answer == JOptionPane.CANCEL_OPTION) {
                    child_Recipient = null;
                    makingSpecialRelationship = false;
                }
                return;
            }
            createChartableUDP_2();
        } else {  //  parent is already designated; this is the child
            if (theInd > -1) {
                child_Recipient = Context.current.individualCensus.get(theInd);
                theSpecRel.child = child_Recipient;
            } else if (theLink > -1) {
                Link lk = Context.current.linkCensus.get(theLink);
                child_Recipient = lk.personPointedTo;
                theSpecRel.child = lk;
            } else {  //  Nothing useful was clicked
                String msg = msgs.getString("mustClickPerson");
                Object[] btns = {se.getString("oK"), se.getString("cancel")};
                int answer = JOptionPane.showOptionDialog(edWin, msg,
                        msgs.getString("invalidDesignation"),
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, btns, btns[0]);
                if (answer == JOptionPane.CANCEL_OPTION) {
                    cancelAdoption();
                }
                return;
            }
            createChartableUDP_3();
        }
        
    }
    /**Called when the 1st person/union is created in any context.
     * <ol>
     * <li>Prompts for a project name of at least 2 characters; failing to
     * get one from User generates a project name of "Temp".</li>
     * <li>Prompts for the name of the author of the date (i.e. the
     * User's name).</li>
     * <li>Creates the SILK file for this project and loads the Standard Macros.</li>
     * <li>Sets various fields in this class and others to reflect the current project.</li>
     * </ol>
     */
    public void getProjectName() { 
        String msg = msgs.getString("enterCtxtName"),
                title = msgs.getString("nameThisProject"),
                ctxtName = null;
        boolean keepLooping = true;
        while (keepLooping) {
            ctxtName = JOptionPane.showInputDialog(edWin, msg, title, JOptionPane.QUESTION_MESSAGE);
            if (ctxtName != null && ctxtName.trim().length() > 1
                    && Library.validateFileName(ctxtName, false)) {
                keepLooping = false;
            } else if (ctxtName == null) {
                String msg2 = msgs.getString("willUseTemp");
                Object[] options = {msgs.getString("useTemp"), msgs.getString("askAgain")};
                int confirm = JOptionPane.showOptionDialog(edWin, msg2, title,
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        options, options[0]);
                if (confirm == JOptionPane.YES_OPTION) {
                    ctxtName = "Temp";
                    keepLooping = false;
                }
            } else {
                String msg3 = msgs.getString("projectNameRules");
                JOptionPane.showMessageDialog(edWin, msg3, 
                        msgs.getString("tryAgain"), JOptionPane.PLAIN_MESSAGE);
            }
        }
        msg = msgs.getString("nameOfDataAuthor");
        String auth = JOptionPane.showInputDialog(edWin, msg),
                createDate = UDate.today();
        Library.currDataAuthor = auth;
        DomainTheory tempDT;
        try {
            Linus macroLineServer = new Linus(Library.libraryDirectory + "Standard_Macros");
            ParserDomainTheory parzer = new ParserDomainTheory(new Tokenizer(Library.getDFA(), macroLineServer));
            tempDT = parzer.parseMacrosOnly();
            DomainTheory dt = new DomainTheory(ctxtName, auth, createDate, false);
            dt.addressTerms = false;
            dt.dyadsDefined = new DyadTMap();
            dt.dyadsUndefined = new DyadTMap();
            // Load Std Macros
            Iterator macroIter = tempDT.theory.values().iterator();
            while (macroIter.hasNext()) {
                KinTermDef ktd = (KinTermDef) macroIter.next();
                dt.addTerm(ktd);
                dt.nonTerms.add(ktd.kinTerm);
            }
            Context.current = new Context(dt);
            DomainTheory.current = dt;
            Context.current.dataAuthors.add(auth);
        } catch (Exception e) {
            // Message for developer.
            msg = "Fatal error while creating new context.\n" + e;
            MainPane.displayError(msg, "Internal Error", JOptionPane.ERROR_MESSAGE);
            System.exit(9);
        } //  No overwrite will occur

        Person.folks = Context.current.individualCensus;
        Marriage.knots = Context.current.familyCensus;
        Library.contextUnderConstruction = Context.current;
        Library.activeContexts.put(ctxtName, Context.current);
        Context.current.editDirectory = Library.editDirectory;
        edWin.ktm = Context.current.ktm;
        Context.current.loadDefaultKinTypeStuff();
        edWin.rebuildChartCombo();
        edWin.getPPanel().udpsPresent = false;
        edWin.getPPanel().initUDPCombo();
        saveFile = null;
    }  //  end of Check when 1st person/union created

    void displayError(Exception pe) {
        System.out.println("Data entry error: " + pe.getMessage());
        JOptionPane.showMessageDialog(this, pe.getMessage(),
                msgs.getString("invalidData"), JOptionPane.ERROR_MESSAGE);
    }

    /**Called when User makes a selection from the menu of new symbols
     * that can be created on a chart:
     * <ul><li>Female</li>
     * <li>Male</li>
     * <li>Union (a marriage or coupling that produces children)</li>
     * <li>Link to an existing person</li>
     * <li>Draw special relationship (UDP)</li>
     * </ul>
     * Creates the symbol at the location clicked (or nearest grid point
     * if snapToGrid has been selected), and then creates the
     * corresponding object (male, link, etc.) in this context.
     * 
     * @param event the mouse event that triggered this method
     */
    public void personMenu_ListSelect(Event event) {
        // The baby-maker method
        int theIndex = 0;
        if (!personMenu.isVisible()) {
            return;
        }
        personMenu.setVisible(false);
        personMenu.select((theIndex = personMenu.getSelectedIndex()));
        if (Library.contextUnderConstruction == null) {
            getProjectName();
        }
        Individual newPerson = null;
        Family newMar = null;
        Link newLink = null;
        lastLoc = gridSnap(lastLoc);
        switch (theIndex) {
            case 0: // female
                newPerson = new Individual(Person.fem, new Point(lastLoc.x, lastLoc.y));
                newPerson.myId = newPerson.serialNmbr + 1;
                newPerson.homeChart = Context.current.currentChart;
                edWin.getPPanel().addToEgoChoices(newPerson);
                break;
            case 1: // male
                newPerson = new Individual(Person.mal, new Point(lastLoc.x, lastLoc.y));
                newPerson.myId = newPerson.serialNmbr + 1;
                newPerson.homeChart = Context.current.currentChart;
                edWin.getPPanel().addToEgoChoices(newPerson);
                break;
            case 2: // marriage
                newMar = new Family(new Point(lastLoc.x, lastLoc.y));
                newMar.mid = newMar.serialNmbr + 1;
                break;
            case 3: // Linking to existing person
                // Build menu of existing persons, let User choose one.
                Object[] people = new Object[Context.current.indSerNumGen];
                String newName;
                int index = 0;
                for (Individual ind : Context.current.individualCensus) {
                    newName = (ind.deleted ? "deleted" + " <" + ind.serialNmbr + ">" 
                            : ind.homeChart + ": " + ind.name + " <" + ind.serialNmbr + ">");
                    people[index++] = newName;
                }
                String person = (String) JOptionPane.showInputDialog(
                        edWin, msgs.getString("choosePerson"), 
                        msgs.getString("link2ExistingPerson"),
                        JOptionPane.PLAIN_MESSAGE, null, people, people[0]);
                if (person == null) {
                    return;
                }
                if(person.startsWith("deleted")) {
                    Object[] btns = {se.getString("oK")};
                    JOptionPane.showOptionDialog(edWin, null,
                        msgs.getString("can'tLinkDeleted"),
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, btns, btns[0]);
                    return; 
                }
                int strt = person.indexOf("<") + 1,
                 end = person.length() - 1;
                int serial = Integer.parseInt(person.substring(strt, end));
                Individual newbie = Context.current.individualCensus.get(serial);
                Point location = new Point(lastLoc.x, lastLoc.y);
                // Create the Link
                newLink = new Link(newbie, Context.current.currentChart, location);
                break;
            case 4:
                //  Draw a chartable UDP
                if (! edWin.getPPanel().udpsPresent) {
                    String msg = msgs.getString("noChartableUDPs");
                    JOptionPane.showMessageDialog(edWin, msg, 
                            msgs.getString("thisOptionNotAvailable"), 
                            JOptionPane.ERROR_MESSAGE);
                    break;
                } else {
                    createChartableUDP_1();
                }
        }
        if (newPerson != null) {
            // Attach the standard 'self' node and store it in the KTM
            newPerson.node = Node.makeSelfNode(distinctAdrTerms);
            newPerson.node.indiv = newPerson;
            int nmbr = newPerson.serialNmbr;
            edWin.ktm.addNode(nmbr, nmbr, newPerson.node);
            // Unless newPerson is currentEgo, erase the node from her Person
            if (newPerson.serialNmbr != edWin.getCurrentEgo()) {
                newPerson.node = null;
            }
            delayedAreaCk(newPerson);
            showInfo(newPerson);
        } else if (newMar != null) {
            newMar.homeChart = Context.current.currentChart;
            delayedAreaCk(newMar);
            showInfo(newMar);
        } else if (newLink != null) {
            delayedAreaCk(newLink);
            showInfo(newLink.personPointedTo);
        }
        dirty = true;
        repaint();
    }
    
    public Point gridSnap(Point p) {
        if (! Library.snapToGrid) {
            return p;
        }
        int deltaX = Library.gridX, deltaY = Library.gridY;
        int newX = ((p.x + (deltaX/2)) / deltaX) * deltaX, 
            newY = ((p.y + (deltaY/2)) / deltaY) * deltaY;        
        return new Point(newX, newY);
    }

    public int findMarriage(int x, int y) {
        if (Marriage.knots == null || Context.current == null) {
            return -1;
        }
        for (Family m : Marriage.knots) {
            if (m != null && 
                    m.homeChart.equals(Context.current.currentChart)
                    && m.bounds().contains(x, y)) {
                return m.mid - 1;
            }
        }
        return -1;
    }
    
    public int[] chartSize(Rectangle viewArea) {
        int[] results = new int[4];
        int minX = 1000000, minY = 1000000, maxX = -1, maxY = -1;
        int extraX = Library.gridX, extraY = Library.gridY;
        Rectangle printArea = new Rectangle(0, 0, 100000, 100000);
        if (viewArea != null) {
            printArea = viewArea;
        }
        String currChart = Context.current.currentChart;
        for (Family f : Context.current.familyCensus) {
            if (!f.deleted && f.homeChart.equals(currChart)
                    && printArea.contains(f.location)) {
                if (f.location.x < minX) {
                    minX = f.location.x;
                }
                if (f.location.y < minY) {
                    minY = f.location.y;
                }
                if (f.location.x + extraX > maxX) {
                    maxX = f.location.x + extraX;
                }
                if (f.location.y + extraY > maxY) {
                    maxY = f.location.y + extraY;
                }
            }
        }
        for (Individual ind : Context.current.individualCensus) {
            if (!ind.deleted && ind.homeChart.equals(currChart)
                    && printArea.contains(ind.location)) {
                if (ind.location.x < minX) {
                    minX = ind.location.x;
                }
                if (ind.location.y < minY) {
                    minY = ind.location.y;
                }
                if (ind.location.x + extraX > maxX) {
                    maxX = ind.location.x + extraX;
                }
                if (ind.location.y + extraY > maxY) {
                    maxY = ind.location.y + extraY;
                }
            }
        }
        for (Link lk : Context.current.linkCensus) {
            if (!lk.deleted && lk.homeChart.equals(currChart)
                    && printArea.contains(lk.location)) {
                if (lk.location.x < minX) {
                    minX = lk.location.x;
                }
                if (lk.location.y < minY) {
                    minY = lk.location.y;
                }
                if (lk.location.x + extraX > maxX) {
                    maxX = lk.location.x + extraX;
                }
                if (lk.location.y + extraY > maxY) {
                    maxY = lk.location.y + extraY;
                }
            }
        }
        if (maxY == -1) {  //  never found a valid Y
            results[0] = 0;
            results[1] = 0;
        } else {
            results[0] = maxX - minX + extraX;
            results[1] = maxY - minY + extraY;
            results[2] = minX - extraX;
            results[3] = minY - extraY;
        }
        return results;
    }

    void delayedAreaCk(Individual ind) {
        if (!reSizInds.contains(ind)) {
            reSizInds.add(ind);
        }
    }

    /** Add everyone on this list to the list of people to check later.
     * Their new locations might require re-sizing our chart.
     * 
     * @param people a list of people whose location has changed
     */
    public void delayedAreaCk(ArrayList<Individual> people) {
        for (Individual ind : people) {
            if (!reSizInds.contains(ind)) {
                reSizInds.add(ind);
            }
        }
    }
    
    /** Add this link to the list of links to check later.
     * Their new locations might require re-sizing our chart.
     * 
     * @param link the link to add
     */
    public void delayedAreaCk(Link link) {
        if (!reSizLinks.contains(link)) {
            reSizLinks.add(link);
        }
    }
    
    /** Add every link on this list to the list of links to check later.
     * Their new locations might require re-sizing our chart.
     * 
     * @param lynx 
     */
    public void delayedAreaCheck(ArrayList<Link> lynx) {
        for (Link lk : lynx) {
            if (!reSizLinks.contains(lk)) {
                reSizLinks.add(lk);
            }
        }
    }

    /** Add this family to the list of families to check later.
     * Their new locations might require re-sizing our chart.
     * 
     * @param fam the family to add
     */
    public void delayedAreaCk(Family fam) {
        if (!reSizFams.contains(fam)) {
            reSizFams.add(fam);
        }
    }

    public void checkFamArea(ArrayList<Family> fams) {
        int minX = 1000000, minY = 1000000, maxX = -1, maxY = -1, maxSz = -1;
        for (Family f : fams) {

            if (!f.deleted) {
                int sz = f.getSize();
                if (sz > maxSz) {
                    maxSz = sz;
                }
                if (f.location.x < minX) {
                    minX = f.location.x;
                }
                if (f.location.y < minY) {
                    minY = f.location.y;
                }
                if (f.location.x + sz > maxX) {
                    maxX = f.location.x + sz;
                }
                if (f.location.y + sz > maxY) {
                    maxY = f.location.y + sz;
                }
            }
        }
        int wide = maxX - minX, high = maxY - minY;
        checkArea(minX, minY, wide, high, maxSz);
    }

    void checkIndArea(ArrayList<Individual> people) {
        int minX = 1000000, minY = 1000000, maxX = -1, maxY = -1, maxSz = -1;
        for (Individual ind : people) {
            if (!ind.deleted) {
                int sz = ind.getSize();
                if (sz > maxSz) {
                    maxSz = sz;
                }
                if (ind.location.x < minX) {
                    minX = ind.location.x;
                }
                if (ind.location.y < minY) {
                    minY = ind.location.y;
                }
                if (ind.location.x + sz > maxX) {
                    maxX = ind.location.x + sz;
                }
                if (ind.location.y + sz > maxY) {
                    maxY = ind.location.y + sz;
                }
            }
        }
        int wide = maxX - minX, high = maxY - minY;
        checkArea(minX, minY, wide, high, maxSz);
    }

    void checkLinkArea(ArrayList<Link> links) {
        int minX = 1000000, minY = 1000000, maxX = -1, maxY = -1, maxSz = -1;
        for (Link lk : links) {
            if (!lk.deleted) {
                int sz = lk.getSize();
                if (sz > maxSz) {
                    maxSz = sz;
                }
                if (lk.location.x < minX) {
                    minX = lk.location.x;
                }
                if (lk.location.y < minY) {
                    minY = lk.location.y;
                }
                if (lk.location.x + sz > maxX) {
                    maxX = lk.location.x + sz;
                }
                if (lk.location.y + sz > maxY) {
                    maxY = lk.location.y + sz;
                }
            }
        }
        int wide = maxX - minX, high = maxY - minY;
        checkArea(minX, minY, wide, high, maxSz);
    }

    void checkArea(int left, int top, int wide, int high, int sz) {
        // if bottom or right has inadequate margin, add extra space
        resize = false;
        int desiredWidth = left + wide + (4 * sz),
            desiredHeight = top + high + (4 * sz);
        if (desiredWidth > area.width) {
            area.width = desiredWidth;
            resize = true;
        }
        if (desiredHeight > area.height) {
            area.height = desiredHeight;
            resize = true;
        }
        // if top or left has inadequate margin, add extra space
        // if snap2grid is on, round to nearest multiples
        int extraWidth = Math.max((4 * sz) - left, 0);
        int extraHeight = Math.max((4 * sz) - top, 0);
        Point extra = gridSnap(new Point(extraWidth, extraHeight));
        if (extra.x + extra.y > 0) {
            for (Individual ind : Context.current.individualCensus) {
                ind.adjustLocation(extra.x, extra.y);
            }
            for (Family fam : Context.current.familyCensus) {
                fam.adjustLocation(extra.x, extra.y);
                for (Marriage.BirthGroup bg : fam.birthGrps) {
                    bg.topPtX += extra.x;
                }
            }
            for (Link lk : Context.current.linkCensus) {
                lk.setLocationX(lk.getLocationX() + extra.x);
                lk.setLocationY(lk.getLocationY() + extra.y);
            }
            area.width += extra.x;
            area.height += extra.y;
            resize = true;
        }
        if (resize) {
            resize = false;
            setPreferredSize(area);
            revalidate();
        }
    }
    
    /**
     * Make a new instance of a pre-defined {@link UDP} and prepare for 
     * adding it to the chart.
     */
    public void createChartableUDP_1() {
        makingSpecialRelationship = true;
        parent_Initiator = null;
        child_Recipient = null;
        chosenUDP = null;
        edWin.chartComboSetEnabled(false);
        if (Context.current.adoptionHelp) {
            HelpFrame.window.displayPage(HelpFrame.NON_GEN, "rules");
            HelpFrame.window.setLocationRelativeTo(edWin.getPPanel());
        }
        try {
            // If only one chartable UDP, show it. Else menu choose.
            String chosenUDPName;
            TreeMap udpMap = Context.current.userDefinedProperties;
            if (udpMap == null || udpMap.isEmpty()) {
                String msg = msgs.getString("noChartableUDPs");
                JOptionPane.showMessageDialog(this, msg, 
                        msgs.getString("thisOptionNotAvailable"),
                        JOptionPane.ERROR_MESSAGE);
                edWin.getPPanel().udpsPresent = false;
                return;
            }            
            ArrayList<String> udpNames = new ArrayList<String>();
            Iterator udpIter = udpMap.entrySet().iterator();
            while (udpIter.hasNext()) {
                Map.Entry entry = (Map.Entry) udpIter.next();
                String udpName = (String) entry.getKey();
                UserDefinedProperty udp = (UserDefinedProperty) entry.getValue();
                if (udp.chartable) {
                    udpNames.add(udpName);
                }
            }
            int c = udpNames.size();
            if (c == 0) {
                String msg = msgs.getString("noChartableUDPs");
                JOptionPane.showMessageDialog(this, msg, 
                        msgs.getString("thisOptionNotAvailable"),
                        JOptionPane.ERROR_MESSAGE);
                edWin.getPPanel().udpsPresent = false;
                return;
            }
            String[] udpNamz = new String[udpNames.size()];  
            for (int i=0; i<c; i++) {
                udpNamz[i] = udpNames.get(i);
            }
            String msg, title;
            if (c == 1) {
                chosenUDPName = udpNamz[0];
            } else {
                msg = msgs.getString("pickUDP");
                chosenUDPName = (String) JOptionPane.showInputDialog(this, msg,
                        msgs.getString("specialRelationship2Draw"),
                        JOptionPane.PLAIN_MESSAGE, null, udpNamz, udpNamz[0]);
                if (chosenUDPName == null) {
                    makingSpecialRelationship = false;
                    return;
                }              
            }
            chosenUDP = (UserDefinedProperty) udpMap.get(chosenUDPName); 
            title = msgs.getString("draw") + " " + chosenUDPName + 
                    msgs.getString("from") + msgs.getString("elipsis") + " ";
            msg = msgs.getString("clickOKParent");
            JOptionPane.showMessageDialog(this, msg, title, JOptionPane.PLAIN_MESSAGE);
            theSpecRel = new Context.SpecRelTriple();
            theSpecRel.udpName = chosenUDPName;
        } catch (Exception exc) {
            String msg = msgs.getString("errorDrawingSpecial") + exc;
            msg += msgs.getString("reloadAndRetry");
            MainPane.displayError(msg, msgs.getString("seriousError"), 
                    JOptionPane.PLAIN_MESSAGE);
            cancelAdoption();
            return;
        }
    }
    
    /** The User has canceled an effort to create a chartable Special
     *  Relationship. Erase all the flags and data. 
     */
    public void cancelAdoption() {
        makingSpecialRelationship = false;
        parent_Initiator = null;
        child_Recipient = null;
        chosenUDP = null;
        theSpecRel = null;
        HelpFrame.window.setVisible(false);
        edWin.chartComboSetEnabled(true);
    }

    /**
     * Present a confirmation dialog to User showing the parent and child to be
     * connected by this chartable UDP.
     */
    public void createChartableUDP_2() {
        String msg = msgs.getString("clickOKChild"),
                title = msgs.getString("draw") + " " + chosenUDP.starName + " " 
                + msgs.getString("from") + " " 
                + parent_Initiator.name + msgs.getString("elipsis");
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Make a final error check, then create the chartable UDP and draw it on chart.
     * First make sure the parent and child are not the same person; if so cancel.
     * 
     * Check to see if User has designated an existing UDP -- a re-click; if so, then
     * the desired action is to DELETE this UDP.
     * 
     * Otherwise, CREATE the UDP instance. If the 'parent' is a 2-parent family, 
     * we will actually create 2 UDPs: one for each parent. Post the UDP(s) to 
     * the context's lists of SpecialRelationships and InverseSpecialRelationships.
     * Rebuild the affected rows of the KinTerm Matrix and update the Detail Display
     * data for both parent and child. Then clear the flags indicating an adoption
     * is in progress.
     */
    public void createChartableUDP_3() {
        if (child_Recipient == parent_Initiator) {
            String msg = msgs.getString("noSelfies");
            Object[] btns = {se.getString("oK"), se.getString("cancel")};
            int answer = JOptionPane.showOptionDialog(this, msg,
                    msgs.getString("invalidDesignation"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, btns, btns[0]);
            if (answer != JOptionPane.OK_OPTION) {
                cancelAdoption();
            }
            child_Recipient = null;
            return;
        }
        String uName = chosenUDP.starName;
        UserDefinedProperty 
                udp = (UserDefinedProperty) parent_Initiator.userDefinedProperties.get(uName),
                udp2 = null;
        Family fam = null;
        if (Context.current.specialRelationships == null) {
            Context.current.specialRelationships = new TreeMap<String, ArrayList<Context.SpecRelTriple>>();
        }
        if (Context.current.inverseSpecialRelationships == null) {
            Context.current.inverseSpecialRelationships = new TreeMap<Individual, TreeMap<String, ArrayList<Individual>>>();
        }
        TreeMap<String, ArrayList<Context.SpecRelTriple>> sRs = Context.current.specialRelationships;
        TreeMap<Individual, TreeMap<String, ArrayList<Individual>>> iRs = Context.current.inverseSpecialRelationships;        
        String chartLtr = Context.current.currentChart;
        
        if (udp.value.contains(child_Recipient)) {  //  This is a re-drop = REMOVAL
            udp.value.remove(child_Recipient);
            removeSpecialRelationship(chartLtr, theSpecRel);
            if (theSpecRel.parent instanceof Family) { // might need a 2nd Removal
                fam = (Family) theSpecRel.parent;
                if (fam.wife != null && fam.husband != null) {  //  did wife previously
                    udp2 = (UserDefinedProperty) fam.husband.userDefinedProperties.get(uName);
                    udp2.value.remove(child_Recipient);
                }
            }
            iRs.get(child_Recipient).get(uName).remove(parent_Initiator);
            if (udp2 != null) {
                iRs.get(child_Recipient).get(uName).remove(fam.husband);
            }
            if (iRs.get(child_Recipient).get(uName).isEmpty()) {
                iRs.get(child_Recipient).remove(uName);
            }
            if (iRs.get(child_Recipient).isEmpty()) {
                iRs.remove(child_Recipient);                
            }
            edWin.rebuildKTMatrixEtc();
        } else {  // Not a re-drop:  ADD
        // Build the UDP            
            try {
                udp.value.add(child_Recipient);
                if (theSpecRel.parent instanceof Family) { // might need a 2nd UDP
                    fam = (Family) theSpecRel.parent;
                    if (fam.wife != null && fam.husband != null) {  //  did wife previously
                        udp2 = (UserDefinedProperty) fam.husband.userDefinedProperties.get(uName);
                        udp2.value.add(child_Recipient);
                    }
                }
            } catch (Exception exc) {
                String msg = msgs.getString("errorDrawingSpecial") + exc;
                msg += msgs.getString("reloadAndRetry");
                MainPane.displayError(msg, msgs.getString("seriousError"), 
                        JOptionPane.PLAIN_MESSAGE);
                cancelAdoption();
                return;
            }  //  Post the Triple on the context
            if (sRs.get(chartLtr) == null) {
                sRs.put(chartLtr, new ArrayList<Context.SpecRelTriple>());
            }
            sRs.get(chartLtr).add(theSpecRel);
            if (iRs.get(child_Recipient) == null) {
                iRs.put(child_Recipient, new TreeMap<String,ArrayList<Individual>>());
            }  
            if (iRs.get(child_Recipient).get(uName) == null) {
                iRs.get(child_Recipient).put(uName, new ArrayList<Individual>());
            }  //  Post the Inverse Special Relationship
            iRs.get(child_Recipient).get(uName).add(parent_Initiator);
            if (udp2 != null) {
                iRs.get(child_Recipient).get(uName).add(fam.husband);
            }
        }        
        int oldEgo = edWin.getCurrentEgo();
        edWin.hideEgoChange = true;
        edWin.changeEgo(parent_Initiator.serialNmbr);  //  force rebuild of KTM row
        edWin.changeEgo(child_Recipient.serialNmbr);
        edWin.changeEgo(oldEgo);
        edWin.hideEgoChange = false;
        edWin.showInfo(parent_Initiator);
        edWin.getPPanel().setUDPSelection(uName);
        edWin.infoPerson = parent_Initiator;
        whichFolk = parent_Initiator.serialNmbr;
        cancelAdoption();
        dirty = true;
        repaint();
    }
    
    void removeSpecialRelationship(String chartLtr, Context.SpecRelTriple triple) {
        Iterator iter = Context.current.specialRelationships.get(chartLtr).iterator();
        while (iter.hasNext()) {
            Context.SpecRelTriple tpl = (Context.SpecRelTriple) iter.next();
            if (tpl.equals(triple)) {
                iter.remove();
                return;
            }
        }
    }
    

    /** Gather a few additional parameters, then call the main painting method.
     *  Because ChartPanel extends JPanel, the <i>repaint</i> method of the 
     *  superclass will call this method.
     * 
     * @param g the graphics object to draw on
     */
    public void paint(Graphics g) {
        super.paint(g);
        if (loading) {
            return;
        }
        if (Context.current != null && Context.current.libraryBrowsing) {
            paint1(g);
        } else {
            g.translate(originX, originY);
            Rectangle myRect = getBounds();
            myRect.setLocation(-originX, -originY);
            String currChart = null;
            if (Context.current != null && Context.current.currentChart != null) {
                currChart = Context.current.currentChart;
            }
            paint0(g, myRect, currChart);
        }
    }
    
    /** The paint method when we are just browsing the Library.
     * 
     * @param g the graphics object to draw on
     */
    public void paint1(Graphics g) {    
        int originX = 0;
        int originY = 0;
        int GAP = 5;
        Color originalColor = g.getColor();
        g.translate(originX, originY);
        Rectangle myRect = getBounds();
        myRect.setLocation(-originX, -originY);
        for (Link lk : Context.current.linkCensus) {
            if (!lk.deleted && lk.homeChart.equals(Context.current.currentChart)) {
                Individual p = lk.personPointedTo;
                if (p.name.equals("Ego")) {
                    lk.drawSymbol(g, myRect, Color.red);
                } else {
                    lk.drawSymbol(g, myRect, Link.defaultColor);
                }
            }
        }  //  end of loop thru Links, w/ default color = green
        for (Individual p : Context.current.individualCensus) {
            if (p != null && !p.deleted && p.homeChart.equals(Context.current.currentChart)) {
                if (p.name.equals("Ego")) {
                // I want P's ID# to print on chart as 'name'    
                    p.name = "I-" + p.serialNmbr;
                    p.drawSymbol(g, myRect, Color.red);
                    p.name = "Ego";
                } else {
                    p.drawSymbol(g, myRect);
                }
            }
        }  //  end of loop thru Individuals with defailt color = black
        for (Family m : Context.current.familyCensus) {
            if (m != null && !m.deleted  && m.homeChart.equals(Context.current.currentChart)) {
                m.drawSymbol(g, myRect, Color.black);
                m.drawLines(g, myRect);
            }
        }  //  enf of loop thru families, who draw lines to their members
        if (Context.current.specialRelationships != null && 
                Context.current.specialRelationships.get(Context.current.currentChart) != null) {
            for (Context.SpecRelTriple sr : 
                    Context.current.specialRelationships.get(Context.current.currentChart)) {
                Rectangle pr = sr.parent.bounds(), kr = sr.child.bounds();
                int parentMidX = pr.x + (pr.width / 2),
                    kidMidX = kr.x + (kr.width / 2),
                    parBotY = pr.y + pr.height + GAP,
                    kidTopY = kr.y;
                Color clr = ((UserDefinedProperty)Context.current.userDefinedProperties.get(sr.udpName)).chartColor;
                g.setColor(clr);
                g.drawLine(parentMidX, parBotY, kidMidX, kidTopY);
            }
        }
        g.setColor(originalColor);        
    }    
    

    /**
     * Paint the visible portion of the current chart with all persons, links, 
     * families, and special relationships.
     * First identify the persons, if any, that are linking kinsmen between
     * Alter (whichFolk) and Ego. 
     * Then iterate through all the individuals, families, and links in this
     * context. If they are part of this chart,and in the visible portion, then
     * color them red if Ego, blue if Alter, green if a link to a person on
     * another chart, or orange if a linking kinsman between Ego & Alter.
     * Use each object's drawSymbol() method that will label that symbol 
     * according to the User's current settings.
     * 
     * If the mouse event that triggered this repaint was the dragging of a 
     * Person to the top or bottom of a Union (Marriage), then also put a 
     * colored oval on the top or bottom of the Union symbol, to confirm that
     * the User can drop the person and that Person will be added to the Union.
     * 
     * @param g the graphics object to draw on
     * @param myRect the bounds within which we will paint
     * @param chart the name of the only chart we are painting.
     */
    public void paint0(Graphics g, Rectangle myRect, String chart) {
        Rectangle theRect;
        if (Context.current == null) {
            return;
        }
        if (edWin.chartComboBox.isPopupVisible()) {
            return;
        }
        if (selectLine != null) {
            selectLine.paint(g);
        }
        Color originalColor = g.getColor();
        int oldFolk = whichFolk;
        ArrayList<Integer> path = new ArrayList<Integer>();
        try {
            if (whichFolk > -1) {
                path = Context.current.ktm.getPath(edWin.getCurrentEgo(), whichFolk);
            } else if (whichLink > -1) {
                int ppt = Context.current.linkCensus.get(whichLink).personPointedTo.serialNmbr;
                path = Context.current.ktm.getPath(edWin.getCurrentEgo(), ppt);
            }
        } catch (KSInternalErrorException exc) {
            JOptionPane.showMessageDialog(this,
                    "SERIOUS problem. " + exc + "\nProbably due to deletion of people or unions.\n"
                    + "Not your fault. Send copy of your SILK file to SILKin Team for correction.",
                    "Data Corruption Error",
                    JOptionPane.ERROR_MESSAGE);
            saveSILKFile();
            System.exit(9);
        }
        if (chart == null) {
            chart = "A";
        }   
        g.setFont(chartFont);
        int tempSerial = -1;
        for (Link lk : Context.current.linkCensus) {
            if (!lk.deleted && lk.homeChart.equals(chart)) {
                Individual p = lk.personPointedTo;
                if (p.serialNmbr == edWin.getCurrentEgo()) {
                    lk.drawSymbol(g, myRect, Color.red);
                }  // If we clicked on Ego, he'll be blue (Alter)
                if (lk.serialNmbr == whichLink) { // clicked on Link
                    lk.drawSymbol(g, myRect, Color.blue);
                    tempSerial = p.serialNmbr;
                } else if (p.serialNmbr == whichFolk) {
                    lk.drawSymbol(g, myRect, Color.blue);
                } else if (path.contains(p.serialNmbr)) {
                    lk.drawSymbol(g, myRect, Color.orange);
                } else {
                    lk.drawSymbol(g, myRect, Link.defaultColor);
                }
            }
        }  //  end of loop thru Links, w/ default color = green
        for (Individual p : Person.folks) { // clicked on Individual
            if (p != null && !p.deleted && p.homeChart.equals(chart)) {
                if (p.serialNmbr == whichFolk) {
                    p.drawSymbol(g, myRect, Color.blue);                    
                } else if (p.serialNmbr == tempSerial) {
                    p.drawSymbol(g, myRect, Color.blue); 
                }
                else if (p.serialNmbr == edWin.getCurrentEgo()) {
                    p.drawSymbol(g, myRect, Color.red);
                } else if (path.contains(p.serialNmbr)) {
                    p.drawSymbol(g, myRect, Color.orange);
                } else {
                    p.drawSymbol(g, myRect);
                }
            }
        }  //  end of loop thru Individuals
        if (lastFolk != whichFolk && whichFolk >= 0) {
            showInfo(Context.current.individualCensus.get(whichFolk));
        } else if (lastFolk != tempSerial && tempSerial >= 0) {
            showInfo(Context.current.individualCensus.get(tempSerial));
        }
        
        whichFolk = oldFolk;
        lastFolk = whichFolk;  // lastFolk == person previously displayed
        whichHalf = -1;
        for (Family m : Marriage.knots) {
            if (m != null && !m.deleted && m.homeChart.equals(chart)) {
                theRect = m.bounds();
                if (m.mid - 1 == whichKnot) {
                    m.drawSymbol(g, myRect, Color.blue);
                    if (lastKnot != whichKnot) {
                        showInfo(m);
                    }
                } else {
                    m.drawSymbol(g, myRect, Color.black);
                }
                m.drawLines(g, myRect);
                //  SelectLine shows that a person is being dragged to top or bottom of a marriage symbol
                if (selectLine != null) {
                    theRect = new Rectangle(theRect);
                    theRect.height *= 2;
                    theRect.y -= theRect.height / 4;
                    if (theRect.contains(selectLine.toP.x, selectLine.toP.y)) {
                        tiedKnot = m.mid - 1;
                        Rectangle upper = new Rectangle(theRect.x, theRect.y, theRect.width,
                                theRect.height / 2);
                        Rectangle lower = new Rectangle(theRect.x, theRect.y + theRect.height / 2,
                                theRect.width, theRect.height / 2);
                        Color cx = g.getColor();
                        g.setColor(Color.green);
                        if (upper.contains(selectLine.toP.x, selectLine.toP.y)) {
                            g.fillOval(upper.x, upper.y, upper.width, upper.height);
                            whichHalf = 1;
                        } else if (lower.contains(selectLine.toP.x, selectLine.toP.y)) {
                            g.setColor(Color.magenta);
                            g.fillOval(lower.x, lower.y, lower.width, lower.height);
                            whichHalf = 2;
                        }
                        g.setColor(cx);
                    }
                }
            }
        }
        lastKnot = whichKnot;
        if (Context.current.specialRelationships != null && 
                Context.current.specialRelationships.get(Context.current.currentChart) != null) {
            for (Context.SpecRelTriple sr : Context.current.specialRelationships.get(Context.current.currentChart)) {
                Rectangle pr = sr.parent.bounds(), kr = sr.child.bounds();
                int parentMidX = pr.x + (pr.width / 2),
                    kidMidX = kr.x + (kr.width / 2),
                    parBotY = pr.y + pr.height + GAP,
                    kidTopY = kr.y;
                Color clr = ((UserDefinedProperty)Context.current.userDefinedProperties.get(sr.udpName)).chartColor;
                g.setColor(clr);
                g.drawLine(parentMidX, parBotY, kidMidX, kidTopY);
            }
        }
        g.setColor(originalColor);
    }  //  end of method paint0
    
    public void setAlter(int serial) {
        whichFolk = serial;
        edWin.infoPerson = Context.current.individualCensus.get(serial);
        repaint();
    }
    
    
/** Make sure we ares not in loading mode, then pass the call on to SIL_Edit.
 * 
 * @param p the person whose info should be shown
 */
    public void showInfo(Individual p) {
        if (!loading) {
            edWin.showInfo(p);
        }
    }

/** Make sure we ares not in loading mode, then pass the call on to SIL_Edit.
 * 
 * @param f the family whose info should be shown
 */
 public void showInfo(Family f) {
        if (!loading) {
            edWin.showInfo(f);
        }
    }

    public void clearInfo() {
        if (!loading) {
            try {
                edWin.clearInfo();
            } catch (Exception e) {
                displayError(e);
            }
        }
    }

    /** Provide for 3 different kinds of Family drags, depending on the modifier
     * keys depressed, or a single type of drag for an individual. Capture the 
     * destination X & Y coordinates, then place the person, family symbol, nuclear
     * family, or entire descent at the new location.
     * 
     * @param event the mouse-drag event
     */
    public void KinshipEditor_MouseDrag(MouseEvent event) {
        if (!editable) {
            return; // can't edit;
        }
        int mouseX = event.getX(), mouseY = event.getY();
        boolean shiftDn = event.isShiftDown(),
                altDn = event.isAltDown(),
                metaDn = event.isMetaDown();
        if (shiftDn) {  //  Shift-drag on a Family = move nuclear family
            if (whichKnot != -1) {
                int dx = Marriage.knots.get(whichKnot).location.x - mouseX + 10;
                int dy = Marriage.knots.get(whichKnot).location.y - mouseY + 10;
                Marriage.knots.get(whichKnot).deltaMove(dx, dy);
                // deltaMove calls delayedAreaCk
                whichFolk = -1;
                dirty = true;
                repaint();
            } else if (whichFolk != -1) {
                Person p = Person.folks.get(whichFolk);
                selectLine = new Line(new Point(p.location.x + 10, p.location.y + 10),
                        new Point(mouseX, mouseY));
                repaint();
            } else if (whichLink != -1) {
                Link lk = Context.current.linkCensus.get(whichLink);
                selectLine = new Line(new Point(lk.location.x + 10, lk.location.y + 10),
                        new Point(mouseX, mouseY));
                repaint();
            }
        } else if (metaDn || altDn) {   //  Alt-drag on a Family = move all descendants
            if (whichKnot != -1) {      //  Option-drag or Command-drag on a Mac
                int dx = Marriage.knots.get(whichKnot).location.x - mouseX + 10;
                int dy = Marriage.knots.get(whichKnot).location.y - mouseY + 10;
                ArrayList<Individual> people = new ArrayList<Individual>();
                ArrayList<Link> lynx = new ArrayList<Link>();
                Marriage.knots.get(whichKnot).lineageDeltaMove(dx, dy, people, lynx);
                for (Individual ind : people) {
                    if (!draggedPersons.contains(ind)) {
                        draggedPersons.add(ind);
                    }
                }
                for (Link lk : lynx) {
                    if (!draggedLinks.contains(lk)) {
                        draggedLinks.add(lk);
                    }
                }
                delayedAreaCk(people);
                delayedAreaCheck(lynx);
                whichFolk = -1;
                dirty = true;
                repaint();
            } else if (whichFolk != -1) {
                Person p = Person.folks.get(whichFolk);
                selectLine = new Line(new Point(p.location.x + 10, p.location.y + 10),
                        new Point(mouseX, mouseY));
//                delayedAreaCk((Individual) p);
                repaint();
            }else if (whichLink != -1) {
                Link lk = Context.current.linkCensus.get(whichLink);
                selectLine = new Line(new Point(lk.location.x + 10, lk.location.y + 10),
                        new Point(mouseX, mouseY));
//                delayedAreaCk(lk);
                repaint();
            }

        } else {  //  No mod keys = drag current object only
            selectLine = null;
            if (whichFolk != -1) {
                Individual p = Person.folks.get(whichFolk);
                Point newLoc = new Point(mouseX - 10, mouseY - 10);
                if (!draggedPersons.contains(p)) {
                    draggedPersons.add(p);
                }
                p.setLocation(newLoc);
                whichKnot = -1;
                whichLink = -1;
                dirty = true;
                selectLine = new Line(lastPersonLoc, newLoc);
                repaint();
            } else if (whichLink != -1) {
                Link lk = Context.current.linkCensus.get(whichLink);
                Point newLoc = new Point(mouseX - 10, mouseY - 10);
                if (!draggedLinks.contains(lk)) {
                    draggedLinks.add(lk);
                }
                lk.location = newLoc;
                whichKnot = -1;
                whichFolk = -1;
                dirty = true;
                selectLine = new Line(lastPersonLoc, newLoc);
                repaint();
            } else if (whichKnot != -1) {
                Family m = Marriage.knots.get(whichKnot);
                Point newLoc = new Point(mouseX - 10, mouseY - 10);
                if (!draggedMarriages.contains(m)) {
                    draggedMarriages.add(m);
                }
                m.setLocationX(newLoc.x);
                m.setLocationY(newLoc.y);                
                dirty = true;
                repaint();
            } else {
                repaint();
            }
        }
    }

    /** This is where we check to see if anything has been dragged, a new Ego
     *  chosen, a symbol has been moved, or someone has been added to a Union.
     * MouseUp is fired before MouseReleased. 
     * 
     * @param event the mouse event
     */
    public void KinshipEditor_MouseUp(MouseEvent event) {
        if (!editable) {
            return;
        }
        int mouseX = event.getX(), mouseY = event.getY();
        boolean ctrlDn = event.isControlDown(),
                shiftDn = event.isShiftDown(),
                altDn = event.isAltDown(),
                metaDn = event.isMetaDown(),
                needPaintJob = false;
        ArrayList<Family> fams = new ArrayList<Family>(); 
        // fams with birthGrps affected by the drag
        if (!draggedPersons.isEmpty()) {            
            for (Individual p : draggedPersons) {
                Point newLoc = p.location;
                p.setLocation(gridSnap(newLoc));
                delayedAreaCk(p);
                if (p.birthFamily != null && ! fams.contains(p.birthFamily)) {
                    fams.add(p.birthFamily);
                }
            }
            needPaintJob = true;
            draggedPersons.clear();
        }
        if (!draggedMarriages.isEmpty()) {
            for (Family m : draggedMarriages) {
                Point newLoc = gridSnap(m.location);
                m.location = newLoc;
                if (m.husband != null && m.husband.birthFamily != null
                        && !fams.contains(m.husband.birthFamily)) {
                    fams.add(m.husband.birthFamily);
                }
                if (m.wife != null && m.wife.birthFamily != null
                        && !fams.contains(m.wife.birthFamily)) {
                    fams.add(m.wife.birthFamily);
                }
            }
            needPaintJob = true;
            draggedMarriages.clear();
        }
        if (!draggedLinks.isEmpty()) {
            for (Link lk : draggedLinks) {
                lk.location = gridSnap(lk.location);
                delayedAreaCk(lk);
                if (lk.personPointedTo.birthFamily != null && 
                        ! fams.contains(lk.personPointedTo.birthFamily)) {
                    fams.add(lk.personPointedTo.birthFamily);
                }
            }
            needPaintJob = true;
            draggedLinks.clear();
        }
        for (Family fam : fams) {
            fam.computeBirthGrps();
        }
        if (needPaintJob) {
            repaint();
        }
        if (altDn && whichFolk > -1) { // Chose New Ego
            edWin.changeEgo(whichFolk);
            edWin.getPPanel().resetEgoBox(edWin.getCurrentEgo());
            whichFolk = priorAlter;
            repaint();
            return;
        }
        if (altDn && whichLink > -1) { // Chose New Ego
            Individual ind = Context.current.linkCensus.get(whichLink).personPointedTo;
            edWin.changeEgo(ind.serialNmbr);
            edWin.getPPanel().resetEgoBox(edWin.getCurrentEgo());
            whichFolk = priorAlter;
            repaint();
            return;
        }
        //  This test = 'no control key' or 'shift' or 'meta'
        if (shiftDn || metaDn || !ctrlDn) {
            if (selectLine != null && whichHalf > 0 && whichFolk > -1) {
            //  We've released a Person after dragging to a Marriage
                Family fx = Marriage.knots.get(tiedKnot);
                Individual ix = Person.folks.get(whichFolk);
                Rectangle theRect = makeRect(fx);
                if (theRect.contains(selectLine.toP.x, selectLine.toP.y)) {
                    Rectangle upperHalf = new Rectangle(theRect.x, theRect.y -5, theRect.width,
                            theRect.height / 2);
                    Rectangle lowerHalf = new Rectangle(theRect.x, theRect.y + theRect.height / 2,
                            theRect.width, (theRect.height / 2) + 5);
                    if (upperHalf.contains(selectLine.toP.x, selectLine.toP.y)) { //  intend to add new spouse
                        addOrDeleteSpouse(ix, fx);
                    } else if (lowerHalf.contains(selectLine.toP.x, selectLine.toP.y)) { //  Intend to add child
                        if (ix.birthFamily != null && ix.birthFamily != fx) { // kid already has a different birthFamily
                            String msg = msgs.getString("person") + " <" + ix.serialNmbr 
                                    + "> " + msgs.getString("isChildInFamily") + " <" + fx.serialNmbr + ">"
                                    + msgs.getString("mustDeleteFromIncorrectFamily"),
                                    ttl = msgs.getString("familyBuildingError");
                            JOptionPane.showMessageDialog(edWin, msg, ttl, JOptionPane.WARNING_MESSAGE);
                            ix.setLocation(lastPersonLoc);
                            repaint();
                            return;
                        }
                        if (!fx.isSib(ix)) {
                            addChild(ix, fx);
                        } else {
                            removeChild(ix, fx);
                        }
                        fx.computeBirthGrps();
                    }
                }
            }else if (selectLine != null && whichHalf > 0 && whichLink > -1) {
                //  We've released a Link after dragging to a Marriage
                Family fx = Marriage.knots.get(tiedKnot);
                Link lk = Context.current.linkCensus.get(whichLink);
                Rectangle theRect = makeRect(fx);
                if (theRect.contains(selectLine.toP.x, selectLine.toP.y)) {
                    Rectangle upperHalf = new Rectangle(theRect.x, theRect.y -5, theRect.width,
                            theRect.height / 2);
                    Rectangle lowerHalf = new Rectangle(theRect.x, theRect.y + theRect.height / 2,
                            theRect.width, (theRect.height / 2) + 5);
                    if (upperHalf.contains(selectLine.toP.x, selectLine.toP.y)) { //  intend to add new spouseLink
                        addOrDeleteSpouseLink(lk, fx);
                    } else if (lowerHalf.contains(selectLine.toP.x, selectLine.toP.y)) { //  Intend to add child
                        Family origBFam = lk.personPointedTo.birthFamily;
                        if (origBFam != null && origBFam != fx) { // kid already has a different birthFamily
                            String msg = msgs.getString("personLinkedTo") + ": " + lk.personPointedTo.name 
                                    + " <" + lk.personPointedTo.serialNmbr 
                                    + ">\n "+ msgs.getString("isChildInFamily") + " <" + fx.serialNmbr + ">"
                                    + msgs.getString("cannotBeIn2Families") ,
                                    ttl = msgs.getString("familyBuildingError");
                            JOptionPane.showMessageDialog(edWin, msg, ttl, JOptionPane.WARNING_MESSAGE);
                            lk.location = lastPersonLoc;
                            repaint();
                            return;
                        }
                        if (!fx.isSib(lk.personPointedTo)) {
                            addChildLink(lk, fx);
                        } else {
                            removeChildLink(lk, fx);
                        }
                        fx.computeBirthGrps();
                    }
                    lk.location = lastPersonLoc;
                }                
                
            }else if (dragged) { //  Perhaps we just dragged something to a new location
            }
            whichHalf = -1;
            tiedKnot = -1;
            selectLine = null;
            repaint();
        } else if (ctrlDn) {  //  Control = deletion and deletion gets messy
            int serialNum;
            if ((serialNum = Person.findPerson(mouseX, mouseY)) >= 0) { //  Want to delete a Person
                if (whichFolk == serialNum) {
                    lastLoc = new Point(mouseX, mouseY);
                } else {
                    whichFolk = serialNum;
                }                
                Individual ind = Context.current.individualCensus.get(whichFolk);
                try {
                    deleteIndividual(ind);
                } catch (KinshipSystemException exc) {
                    //  we refused the deletion; no big deal
                }
                whichFolk = -1;
                dirty = true;
                repaint();
                return;
            } else {
                whichFolk = -1;
            }
            if ((serialNum = findMarriage(mouseX, mouseY)) >= 0) { //  Want to delete a Family
                if (whichKnot == serialNum) {
                    lastLoc = new Point(mouseX, mouseY);
                } else {
                    whichKnot = serialNum;
                }
                Family fam = Marriage.knots.get(whichKnot);
                deleteFam(fam);
                whichKnot = -1;
                dirty = true;
                repaint();
                return;
            } else {
                whichKnot = -1;
            }
            if ((serialNum = Link.findLink(mouseX, mouseY)) >= 0) { // Want to delete a Link
                if (whichLink == serialNum) {
                    lastLoc = new Point(mouseX, mouseY);
                } else {
                    whichLink = serialNum;
                }
                Link lk = Context.current.linkCensus.get(serialNum);
                try {
                    deleteLink(lk);
                } catch (KinshipSystemException exc) {
                    //  deletion refused. It's OK.
                }
            }
            whichLink = -1; 
            repaint();
        }        
    }
    
    Rectangle makeRect(Family fx) {
        Rectangle theRect = new Rectangle(fx.location.x, fx.location.y, 20, 20);
        theRect = new Rectangle(fx.bounds());
        theRect.height *= 2;
        theRect.y -= theRect.height / 4;
        return theRect;
    }
    
    void deleteIndividual(Individual ind) throws KinshipSystemException {
        int serialNmbr = ind.serialNmbr;
        if (Context.current.indSerNumGen > 1) {
            String msg = "", msg2 = "", title = msgs.getString("cannotDelete") + " " + ind.name;
            if (serialNmbr == edWin.getCurrentEgo()) {
                msg = msgs.getString("cannotDeleteEgo");
            } else if (ind.birthFamily != null) {
                msg2 = msgs.getString("cannotDeleteChild");
            } else if (ind.marriages.size() > 0) {
                msg2 = msgs.getString("cannotDeleteSpouse");
            } else if (inAdoption(ind)) {
                msg2 = msgs.getString("cannotDeleteAdopted");
            } else if (isLinkee(ind)) {
                msg = msgs.getString("cannotDeleteLinkee");
            }
            if (!msg.isEmpty()) {
                JOptionPane.showMessageDialog(edWin, msg, title, JOptionPane.WARNING_MESSAGE);
                throw new KinshipSystemException("");
            } else if (!msg2.isEmpty()) {
                Object[] options = {msgs.getString("helpWithDisconnecting"), 
                                    msgs.getString("oK")};
                int confirm = JOptionPane.showOptionDialog(edWin, msg2, title,
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        options, options[1]);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (msg2.equals(msgs.getString("cannotDeleteAdopted"))) {
                        HelpFrame.window.displayPage(HelpFrame.NON_GEN, "delete");
                    } else {
                        HelpFrame.window.displayPage(HelpFrame.CHART, "del-rel");
                    }                    
                }
                throw new KinshipSystemException("");
            }
        }
        ind.delPerson();
        try {
            ind.delete();
        } catch (KSInternalErrorException k) {
        }  //  deleted persons may be messed up
        if (serialNmbr == Context.current.indSerNumGen - 1 //  Delete last person made
                && serialNmbr != edWin.getCurrentEgo()) {   //  if they are not Ego
            Person.folks.remove(--Context.current.indSerNumGen);
            edWin.getPPanel().rebuildEgoBox();
        } else {
            edWin.getPPanel().updateEgoNames(ind);
            Person.folks.get(serialNmbr).location = new Point(-100, -100);
        }
        Context.current.ktm.deletePerson(ind);
        //  Because we do not allow deletion of persons who are married, adopted, or in a birth family,
        //  no recomputing of nodes is needed. Just delete this guy's row and column in the KTM
        //  and any associated dyads.                
    }
    
    void deleteLink(Link lk) throws KinshipSystemException {
        if (!lk.deleted) {
            Individual ind = lk.personPointedTo;
            boolean linkIsKid = ind.birthFamily != null && ind.birthFamily.kidLinks.contains(lk);
            boolean linkIsSpouse = spouseLinkIn(ind.marriages, lk) != null;
            if (Context.current.indSerNumGen > 1) {
                String msg = "", title = msgs.getString("cannotDelete") + " " + lk.personPointedTo.name;
                if (linkIsKid) {
                    msg = msgs.getString("cannotDeleteChild");
                } else if (linkIsSpouse) {
                    msg = msgs.getString("cannotDeleteSpouse");
                } else if (inAdoption(lk)) {
                    msg = msgs.getString("cannotDeleteAdopted");
                }
                if (!msg.isEmpty()) {
                JOptionPane.showMessageDialog(edWin, msg, title, JOptionPane.WARNING_MESSAGE);
                throw new KinshipSystemException("");
                }
            }
            Link.delete(lk);
            lk.personPointedTo.links.remove(lk);
        }
    }
    
    void deleteFam(Family fam) {
        if (fam.husband != null || fam.wife != null ||
                !fam.children.isEmpty()) {
            String msg = msgs.getString("famHasMembers"), 
                   title = msgs.getString("cannotDelete");
            JOptionPane.showMessageDialog(edWin, msg, title, JOptionPane.WARNING_MESSAGE);
            return;
        }
        fam.delete();
        fam.delMarriage();
        Context c = Context.current;
        if (c.specialRelationships != null && 
                c.specialRelationships.get(c.currentChart) != null) {            
            ArrayList<Context.SpecRelTriple> oldies = new ArrayList<Context.SpecRelTriple>(),
                     sr = c.specialRelationships.get(c.currentChart);
            Iterator iter = sr.iterator();
            while (iter.hasNext()) {
                Context.SpecRelTriple tr  = (Context.SpecRelTriple)iter.next();
                if (tr.parent == fam) {
                    oldies.add(tr);
                    iter.remove();
                }
            }
        }        
        fam.location = new Point(-100, -100);
    }
    
    boolean inAdoption(Individual ind) {
        Context c = Context.current;
        if (c.specialRelationships == null
                || c.specialRelationships.get(c.currentChart) == null) {
            return false;
        }
        for (Context.SpecRelTriple tr : c.specialRelationships.get(c.currentChart)) {
            if (tr.parent == ind) {
                return true;
            }
            if (tr.child == ind) {
                return true;
            } else if (tr.parent instanceof Link) {
                Link lk = (Link) tr.parent;
                if (lk.personPointedTo == ind) {
                    return true;
                }
            } else if (tr.parent instanceof Family) {
                Family fam = (Family) tr.parent;
                if (fam.wife == ind || fam.husband == ind) {
                    return true;
                }
            } else if (tr.child instanceof Link) {
                Link lk = (Link) tr.child;
                if (lk.personPointedTo == ind) {
                    return true;
                }
            }
        }
        return false;
    }
    
    boolean inAdoption(Link link) {
        Context c = Context.current;
        if (c.specialRelationships == null
                || c.specialRelationships.get(c.currentChart) == null) {
            return false;
        } else {
            for (Context.SpecRelTriple tr : c.specialRelationships.get(c.currentChart)) {
                if (tr.parent == link) {
                    return true;
                }
                if (tr.child == link) {
                    return true;
                }
                if (tr.parent instanceof Family) {
                    Family fam = (Family) tr.parent;
                    if (fam.wifeLink == link || fam.husbandLink == link) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    boolean isLinkee(Individual ind) {
        for (Link lk : Context.current.linkCensus) {
            if (!lk.deleted && lk.personPointedTo == ind) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * If ix is not already a spouse (or child) in ix, and is not the same sex as an existing
     * spouse, and there is spouse slot empty, then add ix as a spouse to fx.
     * If ix is already a spouse in fx, then this is a deletion request; remove ix.
     * 
     * @param ix the Individual to be added or deleted
     * @param fx the Family to receive or lose ix
     */
    public void addOrDeleteSpouse(Individual ix, Family fx) {
        if (!fx.isSpouse(ix)) {  //  Add ix as a spouse, if OK
            String marriageLicense = fx.eligibleSpouse(ix);  //  'OK' or reason for rejecting marriage, or null
            if (marriageLicense.equals("gay")) {
                String msg = msgs.getString("noGayMarriages"),
                        ttl = msgs.getString("marriageAttemptRejected");
                JOptionPane.showMessageDialog(edWin, msg, ttl, JOptionPane.WARNING_MESSAGE);
                ix.setLocation(lastPersonLoc);
                repaint();
                return;
            } else if (marriageLicense.equals("bigamy")) {
                String msg =msgs.getString("only2InMarriage"),
                        ttl = msgs.getString("marriageAttemptRejected");
                JOptionPane.showMessageDialog(edWin, msg, ttl, JOptionPane.WARNING_MESSAGE);
                ix.setLocation(lastPersonLoc);
                repaint();
                return;
            }
            if (fx.isSib(ix)) {
                if (lastPersonLoc.y > fx.location.y) {
                    lastPersonLoc.y = fx.location.y + (fx.location.y - lastPersonLoc.y);
                }
                ix.setLocation(lastPersonLoc);
                try {
                    fx.deleteChild(ix);
                } catch (KSInternalErrorException ksiee) {
                    String msg = ksiee.getMessage(),
                            ttl = msgs.getString("siblingDeletionRejected");
                    JOptionPane.showMessageDialog(edWin, msg, ttl, JOptionPane.WARNING_MESSAGE);
                    repaint();
                    return;
                }
                fx.delSib(ix);
            }
            ix.setLocation(lastPersonLoc);
            try {
                fx.addSpouse(ix);  // the KAES method
                fx.addParent(ix);  // the SILKin method
            } catch (KSInternalErrorException ksiee) {
                String msg = ksiee.getMessage(),
                        ttl = msgs.getString("marriageAttemptRejected");
                JOptionPane.showMessageDialog(edWin, msg, ttl, JOptionPane.WARNING_MESSAGE);
                repaint();
                return;
            }
            // Changing Ego forces flesh out of new Ego's KTM row
            int storedEgo = edWin.getCurrentEgo();
            edWin.hideEgoChange = true;
            edWin.changeEgo(ix.serialNmbr);
            edWin.changeEgo(storedEgo);
            edWin.hideEgoChange = false;
            
            showInfo(ix);
            dirty = true;
        } else {  // ix is already a spouse in fx. This is a deletion request.
            ix.setLocation(lastPersonLoc);
            // If deleted spouse shared a "family adoption" it now becomes a personal one
            Context c = Context.current;
            if (c.specialRelationships != null
                    && c.specialRelationships.get(c.currentChart) != null) {
                ArrayList<Context.SpecRelTriple> famAdoptions = new ArrayList<Context.SpecRelTriple>(),
                        sr = c.specialRelationships.get(c.currentChart);
                for (Context.SpecRelTriple triple : sr) {
                    if (triple.parent == fx) {
                        UserDefinedProperty udp = (UserDefinedProperty)ix.userDefinedProperties.get(triple.udpName);                        
                        ArrayList adoptees = udp.value;
                        Individual kid;
                        if (triple.child instanceof Link) {
                            kid = ((Link)triple.child).personPointedTo;
                        } else {
                            kid = (Individual)triple.child;
                        }
                        if (adoptees.contains(kid)) {
                            famAdoptions.add(triple);
                        }  // Adoptees now has all SpecRels that should become personal                        
                    }
                }
                for (Context.SpecRelTriple triple : famAdoptions) {
                    Context.SpecRelTriple indivAdoption = new Context.SpecRelTriple();
                    indivAdoption.parent = ix;
                    indivAdoption.child = triple.child;
                    indivAdoption.udpName = triple.udpName;
                    sr.add(indivAdoption);
                }
            }
            try {
                fx.deleteSpouse(ix);
                fx.delSpouse(ix);
                removePersonAndRecomputeNodes(ix, fx);
            } catch (KSInternalErrorException ksiee) {
                String msg = ksiee.getMessage(),
                        ttl = msgs.getString("annulmentRejected");
                JOptionPane.showMessageDialog(edWin, msg, ttl, JOptionPane.WARNING_MESSAGE);
                repaint();
                return;
            }
            showInfo(ix);
            dirty = true;
        }
        if (ix.birthFamily != null) {
            ix.birthFamily.computeBirthGrps();
        }
    }

    /**
     * If we can successfully add/delete ix as a spouse in fx, then we only need 
     * to record/erase that lk is a Link to ix and restore ix's old location.
     * 
     * @param lk the link that was dropped on fx
     * @param fx the Family on which ix was dropped
     */
    public void addOrDeleteSpouseLink(Link lk, Family fx) { 
        Individual ix = lk.personPointedTo;
        int oldX = ix.location.x, oldY = ix.location.y;
        addOrDeleteSpouse(ix, fx);
        if (fx.isSpouse(ix)) { // must have successfully added
            if (ix.gender.equals("M")) {
                fx.husbandLink = lk;
            }else {
                fx.wifeLink = lk;
            }            
        }else { // must have failed to add, or else deleted
            if (fx.husbandLink == lk) {
                fx.husbandLink = null;
            }else if (fx.wifeLink == lk) {
                fx.wifeLink = null;
            }
        }
        ix.setLocation(new Point(oldX, oldY));
        if (ix.birthFamily != null) {
            ix.birthFamily.computeBirthGrps();
        }
    }

    /**
     * Remove this child from the family.
     * 
     * @param ix    kid to be removed
     * @param fx    family to remove from
     */
    public void removeChild(Individual ix, Family fx) {
        ix.setLocation(lastPersonLoc);
        try {
            fx.deleteChild(ix);
            fx.delSib(ix);
            removePersonAndRecomputeNodes(ix, fx);
        } catch (KSInternalErrorException ksiee) {
            String msg = ksiee.getMessage(),
                    ttl = msgs.getString("siblingDeletionRejected");
            JOptionPane.showMessageDialog(edWin, msg, ttl, JOptionPane.WARNING_MESSAGE);
            repaint();
            return;
        }
        showInfo(ix);
        dirty = true;
    }
    
    /**
     * Remove this child link from the family.
     * 
     * @param lk    kid link to be removed
     * @param fx    family to remove from
     */
    public void removeChildLink(Link lk, Family fx) {
        Individual ix = lk.personPointedTo;
        int ixX = ix.location.x, ixY = ix.location.y;
        removeChild(ix, fx);
        if (!fx.isSib(ix)) { // must have successfully removed
            fx.kidLinks.remove(lk);            
        }
        ix.setLocation(ixX, ixY);
    }

    /**
     * Add a person to the children of this family. If ix is a spouse in this
     * family, change them to a child. If ix cannot be removed as a spouse for
     * any reason, send error message and return to status quo ante.
     * 
     * @param ix    person to be added
     * @param fx    family to receive the new kid
     */
    public void addChild(Individual ix, Family fx) {
        if (fx.isSpouse(ix)) {
            if (lastPersonLoc.y < fx.location.y) {
                lastPersonLoc.y = fx.location.y + (fx.location.y - lastPersonLoc.y);
            }
            try {
                fx.deleteSpouse(ix);
            } catch (KSInternalErrorException ksiee) {
                String msg = ksiee.getMessage(),
                        ttl = msgs.getString("annulmentRejected");
                JOptionPane.showMessageDialog(edWin, msg, ttl, JOptionPane.WARNING_MESSAGE);
                ix.setLocation(lastPersonLoc);
                repaint();
                return;
            }
            fx.delSpouse(ix);
        }
        ix.setLocation(lastPersonLoc);
        fx.addSib(ix);
        fx.addChild(ix);
        // Changing Ego forces flesh out of new Ego's KTM row
        int storedEgo = edWin.getCurrentEgo();
        edWin.hideEgoChange = true;
        edWin.changeEgo(ix.serialNmbr);
        edWin.changeEgo(storedEgo);
        edWin.hideEgoChange = false;
        //  end of NEW CODE
        showInfo(ix);
        dirty = true;
    }
    
    /**
     * Add a link to this family's children. If we can successfully add ix as a 
     * child in fx, then we only need to record that lk is a Link to ix and 
     * restore ix's old location. 
     * 
     * @param lk    a link to a person who should be added as a child to fx
     * @param fx    the family to which we want to add the link
     */
    public void addChildLink(Link lk, Family fx) {
        Individual ix = lk.personPointedTo;
        int oldX = ix.location.x, oldY = ix.location.y;
        fx.kidLinks.add(lk);
        addChild(ix, fx);
        if (!fx.isSib(ix)) { // must not have successfully added
            fx.kidLinks.remove(lk);           
        }
        ix.setLocation(oldX, oldY);
        lk.location = lastPersonLoc;
    }
    
    Family spouseLinkIn(ArrayList mars, Link lk) {
        // Does the link appear as a spouse in any of these marriages?
        if (mars == null) {
            return null;
        }
        for (Object o : mars) {
            Family fam = (Family)o;
            if (fam.husbandLink == lk || fam.wifeLink == lk) {
                return fam;                
            }
        }
        return null;
    }

    static void post(Individual ind, TreeMap<Integer, ArrayList<Individual>> sorTree, int num) {
        Integer sortNmbr = new Integer(num);
        if (sorTree.get(sortNmbr) == null) {
            sorTree.put(sortNmbr, new ArrayList<Individual>());
        }
        ArrayList<Individual> theList = sorTree.get(sortNmbr);
        theList.add(ind);
    }


    /** Remove this person from fam, then modify/remove all nodes
     *  in the KinTermMatrix and all dyads in the DyadTMaps that were affected.
     *
     * @param ix   Individual to be removed.
     * @param fam  Family object from serialNum to remove ix.
     */
    public void removePersonAndRecomputeNodes(Individual ix, Family fam) {
        if (ix == fam.husband) {
            fam.husband = null;
        } else if (ix == fam.wife) {
            fam.wife = null;
        } else if (fam.children.contains(ix)) {
            fam.children.remove(ix);
        }
        recomputeNodes();
        try {
            if ((Context.current.domTheoryRefExists()
                    && !Context.current.domTheoryRef().issuesForUser.isEmpty())
                    || (Context.current.domTheoryAdrExists()
                    && !Context.current.domTheoryAdr().issuesForUser.isEmpty())) {
                String msg = msgs.getString("priorSuggs1") + " " + ix.name 
                        + " " + msgs.getString("priorSuggs2");
                MainPane.displayError(msg, msgs.getString("potentialProblem"), JOptionPane.PLAIN_MESSAGE);
            }
        } catch (Exception ex) {
        } // skip it
    }
    
    void recomputeNodes() {
        recomputingDyads = true;
        int savedEgo = edWin.getCurrentEgo();
        edWin.rebuildKTMatrixEtc();
        edWin.changeEgo(savedEgo);
        // re-attach nodes in Ego's row of KTM
        TreeMap<Integer, Node> egoRow = edWin.ktm.getRow(savedEgo);
        Iterator rowIter = egoRow.entrySet().iterator();
        while (rowIter.hasNext()) {
            Map.Entry entry = (Map.Entry) rowIter.next();
            Integer altInt = (Integer) entry.getKey();
            Node nod = (Node) entry.getValue();
            nod.indiv.node = nod;
        }
        recomputingDyads = false;
        
    }
    
    XFile sFile = null;  // Used for KAES-format files
    File saveFile = null; // Used for SILKin files
    
    public void changeFileName(String newName) {
        Context currCtxt = Library.contextUnderConstruction;
        if (! newName.equals(currCtxt.languageName)) {
            return;
        }
        String filePath = (currCtxt.editDirectory != null
                            ? currCtxt.editDirectory + "/" : "");
        filePath += currCtxt.languageName + ".silk";
        saveFile = new File(filePath);
    }
/**
 * Prompt User for a new file name, validity check it, make this the new
 * 'saveFile' pathname, and then call saveSILKFile.
 */
    public void saveAsFile() {
        Context currCtxt = Library.contextUnderConstruction;
        if (currCtxt == null) {
            return;
        }
        //  Set up file chooser with defaults
        if (Library.fc == null) {
            Library.fc = new JFileChooser();
        }
        JFileChooser fc = Library.fc;
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setDialogTitle(se.getString("chooseDirFile"));
        fc.setApproveButtonText(se.getString("saveAs"));
        String filePath = (currCtxt.editDirectory != null
                ? currCtxt.editDirectory + "/" : "");
        filePath += currCtxt.languageName + ".silk";
        fc.setSelectedFile(new File(filePath));
        //  Now get user's desired location
        int returnVal = fc.showSaveDialog(edWin);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File temp = fc.getSelectedFile();
            String tempName = temp.getName();
            if (! Library.validateFileName(tempName, false)){
                String msg3 = se.getString("fileName1") + " '" + tempName 
                        + "' " + se.getString("fileName2");
                JOptionPane.showMessageDialog(edWin, msg3, se.getString("tryAgain"), 
                        JOptionPane.PLAIN_MESSAGE);
                return;
            }
            saveFile = temp;
            String pathName = saveFile.getPath(),
                    extension = "";
            int dot = pathName.lastIndexOf(".");
            if (dot > -1) {
                extension = pathName.substring(dot);  //  if it is ".silk" we're good
            }
            if (!extension.equalsIgnoreCase(".silk")) {  //  User specified directory only
                String fileBaseName = saveFile.getName();
                String fileDirName = saveFile.getParent();
                int start = fileDirName.length() - fileBaseName.length();
                File parent;
                if (fileBaseName.equals(fileDirName.substring(start))) {
                    parent = new File(fileDirName);  //  Macintosh case
                } else {
                    parent = new File(pathName);  //  Windows case
                }
                saveFile = new File(parent, currCtxt.languageName + ".silk");
            }
            Library.addRecentFile(saveFile);
            edWin.rebuildRecentSubMenu();
            saveSILKFile();
        }
    }

    /** Write the current SILK file to disk. If dirty == false), just return;
     *  there have been no changes since the last save. Save the Context as well.
     * 
     */
    public void saveSILKFile() {
        // 
        if (saveFile == null) {
            saveAsFile();
            return;
        }
        try {
            edWin.storeInfo();
        } catch (Exception pe) {
                displayError(pe);
                return;
        }
        Context currCtxt = Library.contextUnderConstruction;
        Individual currEgo = currCtxt.individualCensus.get(edWin.getCurrentEgo());
        currCtxt.currentEgo = currEgo;
        checkNames();
        currCtxt.editDirectory = saveFile.getParent();
        Library.editDirectory = currCtxt.editDirectory;
        try {
            currCtxt.writeSILKFile(saveFile, editParameters(currCtxt));
            Library.saveUserContext();
        } catch (Exception kse) {
            String msg = se.getString("errorSavingSilk") + ":\n" + kse;
            System.err.println(msg);
            MainPane.displayError(msg, msgs.getString("seriousError"), JOptionPane.PLAIN_MESSAGE);
        }
        edWin.setTitle("Editing: " + saveFile.getName() + ".");
        dirty = false;
    }

    public String editParameters(Context ctxt) {
        String params = "  <origin x=\"" + originX + "\" y=\"" + originY + "\"/>" + EOL;
        params += "  <area W=\"" + area.width + "\" H=\"" + area.height + "\"/>" + EOL;
        params += "  <infoPerson>" + whichFolk + "</infoPerson>" + EOL;
        params += "  <infoMarriage>" + whichKnot + "</infoMarriage>" + EOL;
        params += "  <label>" + nameLabel + "</label>" + EOL;
        params += "  <ktlabel>" + kinTermLabel + "</ktlabel>" + EOL + "  <editable>";
        params += (editable ? "true" : "false") + "</editable>" + EOL + "  <distinctAdrTerms>";
        params += (distinctAdrTerms ? "true" : "false") + "</distinctAdrTerms>" + EOL;
        params += "  <maxNoise>" + ctxt.maxNoiseP + "</maxNoise>" + EOL;
        params += "  <ignorable>" + ctxt.ignorableP + "</ignorable>" + EOL;
        params += "  <doBaseCBs value=\"" + ctxt.doBaseCBs + "\"/>" + EOL;
        params += "  <doInduction value=\"" + ctxt.doInduction + "\"/>" + EOL;
        params += "  <surnameCapture value=\"" +   ctxt.surnameNormallyCaptured + "\"/>" + EOL;
        params += "  <birthdateCapture value=\"" +   ctxt.birthDateNormallyCaptured + "\"/>";
        String logicalName = ChartPanel.chartFont.getName();
        int size = ChartPanel.chartFont.getSize();
        params += EOL + "  <chartFont name=\"" + logicalName + "\" size=\"" + size + "\"/>"; 
        if (PrintChart.printFont != null) {
             logicalName = PrintChart.printFont.getName();
             size = PrintChart.printFont.getSize();
            params += EOL + "  <printFont name=\"" + logicalName + "\" size=\"" + size + "\"/>";            
        }
        if (PrintChart.pgFormat != null) {
            int orient = PrintChart.pgFormat.getOrientation();
            params += EOL + "  <printOrientation value=\"" + orient + "\"/>";
        }
        return params;
    }

    public void saveKAESFile() {
        // if (dirty == false) return;
        if (sFile == null) {
            saveAsFile();
            return;
        }
        PrintFormat pf = new PrintFormat(true);
        sFile.Open(XFile.WRITE);
        pf.printf("<?xml version=\"1.0\"?>" + EOL);
        pf.printf("<!DOCTYPE kindata>" + EOL + EOL);
        pf.printf("<!--  Kinship Editor Save File - Do not edit by hand!  -->" + EOL + EOL);
        pf.printf("<kindata>" + EOL);
        // write Person and Marriage data
        Person.personsToXML(pf);
        pf.printf(EOL);
        Marriage.unionsToXML(pf);
        pf.printf(EOL);
        // Write parameter data
        pf.printf("<parameters>" + EOL);
        pf.printf("  <origin><x>%d</x><y>%d</y></origin>" + EOL, originX);
        pf.printF(originY);
        pf.printf("  <ego>%d</ego>" + EOL, whichFolk + 1);
        pf.printf("  <marriage>%d</marriage>" + EOL, whichKnot + 1);
        pf.printf("  <label>%d</label>" + EOL, nameLabel);
        pf.printf("  <beginyear>%s</beginyear>" + EOL, edWin.getFPanel().famStartYear().getText());
        pf.printf("  <endyear>%s</endyear>" + EOL, edWin.getFPanel().famEndYear().getText());
        if (editable) {
            pf.printf("  <editable>true</editable>" + EOL);
        } else {
            pf.printf("  <editable>false</editable>" + EOL);
        }
        pf.printf("</parameters>" + EOL);
        pf.printf("</kindata>" + EOL);
        sFile.WriteBytes(pf.toString());
        sFile.Close();
        dirty = false;
    }

    void checkNames() {
        for (Individual i : Library.contextUnderConstruction.individualCensus) {
            if (i.name.indexOf("Person ") > -1) {
                i.surname = i.name.substring(7);
                i.firstNames = "Person";
            }
        }  //  end of loop thru individuals
    }  //  end of method checkNames

    /**
     * First check for unsaved changes to the current context. Then delete all
     * mention of the context: its persons, families, links, data structures, etc.
     * This restores SILKin to the state in which it has been launched but no
     * context has been loaded.
     */
    public synchronized void deleteAll() {
        if (dirty) {
            doWantToSave();
        }
        whichFolk = -1;
        whichKnot = -1;
        lastFolk = -1;
        lastKnot = -1;
        dirty = false;
        edWin.getPPanel().dirty = false;
        edWin.getFPanel().dirty = false;
        clearInfo();
        originX = 0;
        originY = 0;
        Context.current = null;
        Library.contextUnderConstruction = null;
        MainPane.curr_CUC_Editor = null;
        saveFile = null;
        edWin.getPPanel().clearEgoBox();
        edWin.infoPerson = null;
        edWin.infoMarriage = null;
        edWin.ktm = null;
        edWin.suggestionsRef = null;
        edWin.suggestionsAdr = null;
        edWin.rebuildChartCombo();
        edWin.setTitle("");
        repaint();
    }

    boolean doWantToSave() {
        dirty = false;
        String msg = se.getString("saveChanges"),
                ttl = se.getString("saveOrLose");
        Object[] btns = {se.getString("yes"), se.getString("no")};
        int choice = JOptionPane.showOptionDialog(edWin, msg, ttl,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, btns, btns[0]);
        if (choice == JOptionPane.NO_OPTION) {
            saveFile = null;
            return false;
        } else {
            saveSILKFile();
            return true;
        }
    }

    public void exportKAESFile() {
        sFile = new XFile();
        if (sFile.Choose(XFile.WRITE)) {
            saveKAESFile();
        } else {
            return;
        }
    }

    /** Using the shared FileChooser, ask User to identify the SILK file
     *  to load.
     */
    public void pickSILKFile() {  // Loads integrated record for SIL_Edit
        loading = true;
        if (Library.fc == null) {
            Library.fc = new JFileChooser();
        }
        JFileChooser fc = Library.fc;
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setDialogTitle(se.getString("chooseSilk2Load"));
        fc.setApproveButtonText(se.getString("load"));
        String[] extensions = {"silk"};
        fc.setFileFilter(new FileNameExtensionFilter(null, extensions));
        File initialDir = null;
        if (Library.editDirectory.length() > 6) {
            initialDir = new File(Library.editDirectory);
        }
        fc.setCurrentDirectory(initialDir);
        int returnVal = fc.showOpenDialog(edWin);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            deleteAll();
            setOrigin(0, 0);
            saveFile = fc.getSelectedFile();
            Library.userDirectory = saveFile.getName();
            loadSILKFile();
        }
    }
    
    /** Load the SILK file that has been stored in saveFile, verify that all
     *  necessary data is present, and load everything into its proper internal 
     * fields. Almost all the parameters and settings will be loaded to 'edWin'
     * (the SIL_Edit window), the context, or this ChartPanel.
     */
    public void loadSILKFile() {
        Context ctxt = null;
        try {
            Library.loadSILKFile(saveFile);  // makes a new Context.current from SILK file
            ctxt = Context.current;
            edWin.ktm = ctxt.ktm;
            DomainTheory dt = ctxt.domTheoryRef();
            if (!dt.theory.containsKey("step_brother")) {
                Linus macroLineServer = new Linus(Library.libraryDirectory + "Standard_Macros");
                Tokenizer tok = new Tokenizer(Library.getDFA(), macroLineServer);
                ParserDomainTheory parzer = new ParserDomainTheory(tok, tok);
                parzer.parseStandardMacros(dt);
            }
            if (ctxt.kinTypeOrder == null) {
                ctxt.loadDefaultKinTypeStuff();
            }
            ctxt.rebuildLinkMethods();
            edWin.suggestionsRef = dt.issuesForUser;
            if (ctxt.domTheoryAdrExists()) {
                edWin.suggestionsAdr = ctxt.domTheoryAdr().issuesForUser;
            }
            int egoNum = ctxt.currentEgo.serialNmbr;
            edWin.setCurrentEgo(egoNum);
            originX = ctxt.origin.x;
            originY = ctxt.origin.y;
            area = ctxt.area;
            nameLabel = ctxt.labelChoice;
            kinTermLabel = ctxt.ktLabelChoice;
            edWin.synchronizeLabelParams(nameLabel, kinTermLabel);
            editable = ctxt.editable;
            edWin.getPPanel().setDistinctAdrTerms(ctxt.distinctAdrTerms);
            edWin.setSnap(Library.snapToGrid);
            whichFolk = ctxt.infoPerson;
            whichKnot = ctxt.infoMarriage;
            boolean empty = ctxt.infoPerson == -1;
            edWin.infoPerson = (empty ? null : ctxt.individualCensus.get(ctxt.infoPerson));
            empty = ctxt.infoMarriage == -1;
            edWin.infoMarriage = (empty ? null : ctxt.familyCensus.get(ctxt.infoMarriage));
            if (edWin.ktm.getRow(egoNum) != null) {
                Iterator nodeIter = edWin.ktm.getRow(egoNum).values().iterator();
                while (nodeIter.hasNext()) {
                    Node n = (Node) nodeIter.next();
                    if (n.indiv != null) {
                        n.indiv.node = n;
                    }
                }
            }
            Library.currDataAuthor = getCurrentUser(edWin, se.getString("registerCurrentUser"));
            edWin.setActOnSuggsEnabled(ctxt.hasIssues());
        } catch (Exception e) {
            String msg = se.getString("whileReading") + " " + saveFile.getName() + "\n" + e;
            StackTraceElement[] bad = e.getStackTrace();
            for (int i = 0; i < 5; i++) {
                msg += "\n" + bad[i];
            }
            System.err.println(msg);
            MainPane.displayError(msg, msgs.getString("seriousError"), JOptionPane.WARNING_MESSAGE);
        }
        Person.folks = ctxt.individualCensus;
        Marriage.knots = ctxt.familyCensus;
        if (ctxt.chartDescriptions.isEmpty()) {
            ctxt.chartDescriptions.add(se.getString("defaultChart"));
        }
        edWin.editingCtxt = ctxt;
        Context.current = ctxt;
        edWin.rebuildChartCombo();
        String frameTitle = saveFile.getName();
        edWin.setTitle(se.getString("editing") + frameTitle);
        Library.addRecentFile(saveFile);
        edWin.rebuildRecentSubMenu();
        boolean gedSetting = (ctxt.gedcomHeaderItems != null);
        edWin.displayGEDCOM.setVisible(gedSetting);
        ctxt.displayGEDCOM = gedSetting;
        //  We read in some people, so update egoChoiceBox
        edWin.getPPanel().rebuildEgoBox();
        //  May have some UDPs, so update udpComboBox
        edWin.getPPanel().initUDPCombo();
        checkSizeOfChart(ctxt);
        edWin.getPPanel().dirty = false;
        edWin.getFPanel().dirty = false;
        loading = false;
        resizeAndRepaint();
    }
    
    /** This does what it says.
     * 
     */
    public void resizeAndRepaint() {
        setPreferredSize(area);
        revalidate();
        repaint();
    }
    
    /** Survey the locations of all symbols on this chart. Then set size of 
     * Chart to ideal for this diagram.   
     * @param ctxt  the context holding all the symbols for this chart.  
    */
    public void checkSizeOfChart(Context ctxt) {
        int minX = 1000000, minY = 1000000, maxX = -1, 
            maxY = -1, sz = 20;
        for (Individual ind : ctxt.individualCensus) {
            if (!ind.deleted && ind.homeChart.equals(ctxt.currentChart)) {
                sz = ind.getSize();
//                System.out.println("I-" + ind.serialNmbr + "  " + ind.location);
                minX = Math.min(minX, ind.location.x);
                minY = Math.min(minY, ind.location.y);
                maxX = Math.max(maxX, ind.location.x + sz);
                maxY = Math.max(maxY, ind.location.y + sz);                
            }
        }
        for (Family f : ctxt.familyCensus) {
            if (!f.deleted && f.homeChart.equals(ctxt.currentChart)) {
                sz = f.getSize();
//                System.out.println("F-" + f.serialNmbr + "  " + f.location);
                minX = Math.min(minX, f.location.x);
                minY = Math.min(minY, f.location.y);
                maxX = Math.max(maxX, f.location.x + sz);
                maxY = Math.max(maxY, f.location.y + sz); 
            }
        }
        for (Link lk : ctxt.linkCensus) {
            if (!lk.deleted && lk.homeChart.equals(ctxt.currentChart)) {
                sz = lk.getSize();
//                System.out.println("L-" + lk.serialNmbr + "  " + lk.location);
                minX = Math.min(minX, lk.location.x);
                minY = Math.min(minY, lk.location.y);
                maxX = Math.max(maxX, lk.location.x + sz);
                maxY = Math.max(maxY, lk.location.y + sz); 
            }
        }
        Point idealTopLeft = gridSnap(new Point(2 * sz, 2 * sz));
        int adjustX = idealTopLeft.x - minX,
            adjustY = idealTopLeft.y - minY,
            idealWidth = maxX - minX + (3 * idealTopLeft.x),
            idealHeight = maxY - minY + (3 * idealTopLeft.y);
        int scrollHeight = edWin.chartScrollPane.getHeight(),
            scrollWidth = edWin.chartScrollPane.getWidth();
        if (maxX <= scrollWidth) {
            adjustX = 0;
        }
        if (maxY <= scrollHeight) {
            adjustY = 0;
        }
        if (adjustX != 0 || adjustY != 0) {
            for (Individual ind : ctxt.individualCensus) {
                if (ind.homeChart.equals(ctxt.currentChart)) {
                    ind.adjustLocation(adjustX, adjustY);
//                System.out.println("I-" + ind.serialNmbr + "  " + ind.location);
                }
            }
            for (Link lk : ctxt.linkCensus) {
                if (lk.homeChart.equals(ctxt.currentChart)) {
                    lk.adjustLocation(adjustX, adjustY);
//                System.out.println("L-" + lk.serialNmbr + "  " + lk.location);
                }
            }
            for (Family f : ctxt.familyCensus) {
                if (f.homeChart.equals(ctxt.currentChart)) {
                    f.adjustLocation(adjustX, adjustY);
//                System.out.println("F-" + f.serialNmbr + "  " + f.location);
                    for (Marriage.BirthGroup bg : f.birthGrps) {
                        bg.topPtX += adjustX;
                    }
                }
            }
        }
        area.setSize(idealWidth, idealHeight);
    }

    String getCurrentUser(JFrame parentFrame, String paneTitle) throws KSInternalErrorException {
        ArrayList<String> dataAuthors = Context.current.dataAuthors;
        int size = dataAuthors.size(),
                   repeats = -1;
        String[] authors = new String[size + 1];
        for (int i = 0; i < size; i++) {
            authors[i] = dataAuthors.get(i);
        }
        authors[size] = se.getString("addNewUser");
        String author = (String) JOptionPane.showInputDialog(
                parentFrame, se.getString("findYourself"),
                paneTitle, JOptionPane.PLAIN_MESSAGE, null,
                authors, authors[0]);
        if (author == null) {
            author = System.getProperty("user.name");
            String msg = se.getString("willRegisterDA") + " '" + author + "'.";
            JOptionPane.showMessageDialog(parentFrame, msg);
            if (!dataAuthors.contains(author)) {
                dataAuthors.add(author);
            }
        }
                
        while (author.equals(se.getString("addNewUser"))) {
            repeats++;
            String rep = (repeats == 0 ? "\n" : se.getString("carefully")),
                    msg = se.getString("pleaseEnterName") + rep 
                    + se.getString("asShouldAppear"),
                    title = author;
            author = JOptionPane.showInputDialog(parentFrame,
                    msg, title, JOptionPane.PLAIN_MESSAGE);
            while (author == null || author.isEmpty() || author.length() < 3) {
                String badAuth = (author == null || author.isEmpty() ? se.getString("that") + " " : "'" + author + "' ");
                msg = badAuth + se.getString("badAuthor");
                author = JOptionPane.showInputDialog(parentFrame,
                        msg, title, JOptionPane.PLAIN_MESSAGE);
                if (repeats++ > 2 && (author == null || author.isEmpty() || author.length() < 3)) {
                    author = System.getProperty("user.name");
                    msg = se.getString("willRegisterDA") + " '" + author + "'.";
                    JOptionPane.showMessageDialog(parentFrame, msg);
                }
            }
            Object[] options = { se.getString("use") + " '" + author + "'", 
                                 se.getString("tryAgain") };
            msg = se.getString("displayNameAs") + " '" + author + "'?";
            int ch = JOptionPane.showOptionDialog(parentFrame, msg,
                    se.getString("justMakingSure"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
            
            
            if (ch == JOptionPane.NO_OPTION) {
                author = se.getString("addNewUser");
            } else if (ch == JOptionPane.CANCEL_OPTION) {
                author = System.getProperty("user.name");
                msg = se.getString("willRegisterDA") + " '" + author + "'.";
                JOptionPane.showMessageDialog(parentFrame, msg);
                if (!dataAuthors.contains(author)) {
                    dataAuthors.add(author);
                }
            } else {
                if (!dataAuthors.contains(author)) {
                    dataAuthors.add(author);
                }
                return author;
            }
        }
        return author;
    }
}
