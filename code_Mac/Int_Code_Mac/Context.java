import java.util.*;
import java.text.*;
import java.io.*;
import java.awt.*;
import javax.swing.JOptionPane;

/** Each instance of Context is a 'workspace' in which a particular culture's 
 * Kinship System is under construction or examination.  Each contains its own 
 * population of {@link Family} and {@link Individual} objects, domain theories, 
 * etc.
 *   Class-level (static) fields act as global variables.
 *
@author		Gary Morris, Northern Virginia Community College
*                   garymorris2245@verizon.net
*/
public class Context implements Serializable {

    static Context current;  // for Library Browser purposes.  User's current Context = Library.contextUnderConstruction
    static boolean simulation = false;
    static float spellingNoise, classNoise;
    static String[] chartLtrs = LiteralAbstract1.argCodes;
    static int breakCount1 = 0;
    static int breakCount2 = 0;
    static boolean breakFlag = false;
    
    //  ---  Instance Variables  --- 
    /**	Comments hold special comment from (or about) the author of the data about this culture.	*/
    public String comments = "",
            /**	Date this context was initialized.	*/
            createDate;
    public String dateOfLastSuggestion, dateOfLastDataChange, currentChart = "A";
    ArrayList<String> dataAuthors = new ArrayList<String>();
    ArrayList<String> chartDescriptions = new ArrayList<String>();
    int indSerNumGen = 0, famSerNumGen = 0, linkSerNumGen = 0;
    /**	Name of the language spoken in this culture. It is limited administratively to 28 characters.	*/
    public String languageName;
    ArrayList<Object> selectedKinTerms;
    /**	List of all persons in this culture, in serialNmbr order (beginning with 0).	*/
    public ArrayList<Individual> individualCensus = new ArrayList<Individual>();
    /**		List of all families in this culture, in serialNmbr order (beginning with 0).	*/
    public ArrayList<Family> familyCensus = new ArrayList<Family>();
    public ArrayList<Link> linkCensus = new ArrayList<Link>();
    ArrayList<Object> bagOfEgos = new ArrayList<Object>();
    private DomainTheory domTheoryRef, domTheoryAdr;
    /** Two-dimensional matrix of all kin terms known for pairs of persons. Row# = Ego's serialNmbr.
    Column = Alter's serialNmbr.  Each cell contains null or a {@link Node} with the kinTerms
    <code>Row</code> (Ego) could use to refer to <code>Column</code> (Alter).  */
    public KinTermMatrix ktm = new KinTermMatrix();
    /** Index of all {@link Node}str according to their kin type. Used to create
     *  named-data-requests.*/
    public KinTypeIndex kti = new KinTypeIndex();
    Individual currentEgo;
    String editDirectory;
    /**	True if polygamy is accepted in this culture (regardless of whether it has ever happened).	*/
    public boolean polygamyPermit, featuresHaveChanged = false;  //  flag -> true when definition added
    // These 2 parameters (plus maxNoiseP and ignorableP) control the Learning Algorithm
    public boolean doBaseCBs = false, doInduction = false, adoptionHelp = true;
    /**	Optional extra fields, defined by user.  Stored as a TreeMap of pairs:
    property-name (must begin with '*') -> UserDefinedProperty-object.	*/
    public TreeMap userDefinedProperties;
    // connectingUDPValueLists = UDP_Name -> UDP_Value -> AList of individuals with that value
    public TreeMap<String, TreeMap<String, ArrayList<Individual>>> connectingUDPValueLists;
    /** The <code>saveState</code> boolean flag determines whether this instance of Context has its complete state
    saved to disk at shut-down.  The context in which Data_Gathering is done (the 'target culture') MUST be
    saved between sessions.  Other contexts can be saved by the User's explicit menu choice.  */
    public boolean saveState = false, simDataGen = false, maleFirst = true, libraryBrowsing = false;
    /** The learningHistory (Ref or Adr) is a TreeMap from kinTerm -> ArrayList<HistoryItem>.
     *  Each history item records a User decision on a Suggestion.   */
    public TreeMap<String, ArrayList<HistoryItem>> learningHistoryRef = new TreeMap<String, ArrayList<HistoryItem>>();
    public TreeMap<String, ArrayList<HistoryItem>> learningHistoryAdr;
    /** The autoDef (Ref or Adr) is a TreeMap from kinType -> ArrayList<kinTerm>.
     *  This indexes by kin type each accepted definition that could apply to that kin type.
     *  It is used to determine if we should attempt to pre-fill the kinTerm field for a new Alter.  */
    public TreeMap<String, ArrayList<CB_Ptr>> autoDefRef = new TreeMap<String, ArrayList<CB_Ptr>>();
    public TreeMap<String, ArrayList<CB_Ptr>> autoDefAdr;
    public String[] defaultKinTypeOrder = { "Fa", "Mo", "P", "So", "Da", "C", "Hu", "Wi", "S",
           "Bro", "Sis", "Sib", "Hbro", "Hsis", "Stfa", "Stmo", "Stbro", "Stsis", "Stso", "Stda" };
    public String[] defaultKinTypePriority = { "A", "A", "A", "A", "A", "A", "B", "B", "B", 
            "D", "D", "D", "E", "E", "F", "F", "F", "F", "F", "F" };
    public ArrayList<String> kinTypeOrder, kinTypePriority;
    public TreeMap<String, String> kinTypePriorityTMap;
    /** A list of all the Special Relationships in this context, 
     *  recorded in SpecRelTriples.
     */
    public TreeMap<String, ArrayList<SpecRelTriple>> specialRelationships;
    // Inverse is TMap Child > (TMap predName -> AList of adoptiveParents)
    public TreeMap<Individual, TreeMap<String, ArrayList<Individual>>> inverseSpecialRelationships;
    public TreeMap<String, ParserGEDCOM.GEDCOMitem> gedcomHeaderItems;
    public TreeMap<String, ParserGEDCOM.GEDCOMitem> gedcomNotes;
    public TreeMap<String, ParserGEDCOM.GEDCOMitem> gedcomSources;
    public TreeMap<String, ParserGEDCOM.GEDCOMitem> gedcomExtra;
        
    /* The following fields were added to allow a Context to carry the parameters
     * needed by KAES back to the loadFile method.     */
    public Point origin;
    public Dimension area;
    public int infoPerson, infoMarriage, labelChoice, ktLabelChoice, maxNoiseP = 25, ignorableP = 5;
    public boolean editable, distinctAdrTerms, birthDateNormallyCaptured = false,
            surnameNormallyCaptured = true, displayGEDCOM = false;

    /** This zero-arg constructor is for use by Serialization ONLY.  */
    public Context() {
        createDate = UDate.today();
        Context.current = this;
        loadDefaultKinTypeStuff();
    }
// end of zero-arg constructor

    /**
    Constructor with only a {@link DomainTheory} as argument.
    
    @param	dt	a (possibly empty) domain theory for a Kinship System
     */
    public Context(DomainTheory dt) {
        createDate = UDate.today();
        Context.current = this;
        dt.ctxt = this;
        if (dt.addressTerms) {
            domTheoryAdr = dt;
        } else {
            domTheoryRef = dt;
        }
        languageName = dt.languageName;
        int end = languageName.indexOf("(");
        if (end > -1) {
            languageName = languageName.substring(0, end);
        }
        polygamyPermit = dt.polygamyOK;
        userDefinedProperties = dt.userDefinedProperties;
        loadDefaultKinTypeStuff();
        Library.activeContexts.put(languageName, this);
    }  // end of theory-only constructor

    /**
    Constructor with a {@link DomainTheory} and comments-string.
    
    @param		dt      a (possibly empty) domain theory for a Kinship System
    @param		cmts	string to begin the Comments field of this instance.
     */
    public Context(DomainTheory dt, String cmts) {
        createDate = UDate.today();
        Context.current = this;
        dt.ctxt = this;
        comments = cmts;
        if (dt.addressTerms) {
            domTheoryAdr = dt;
        } else {
            domTheoryRef = dt;
        }
        languageName = dt.languageName;
        int end = languageName.indexOf("(");
        if (end > -1) {
            languageName = languageName.substring(0, end);
        }
        polygamyPermit = dt.polygamyOK;
        userDefinedProperties = dt.userDefinedProperties;
        loadDefaultKinTypeStuff();
        Library.activeContexts.put(languageName, this);
    }  // end of 2-arg constructor

    public static void breakpoint() {
        return;
    }  //  end of breakpoint

    /** Converts double quotes to single quotes, using the {@link FamilyPanel}
     *  method.
     * 
     * @param s the string, perhaps containing some double quotes.
     * @return  a converted string
     */
    public static String convertDoubleQuotes(String s) {
        return FamilyPanel.convertBannedCharacters(s);
    }  //  end of method convertDoubleQuotes
    
    public static Context tempCtxt(DomainTheory dt) {
        Context c = new Context(dt);
        Library.activeContexts.remove(c.languageName);
        return c;
    }

    /** Add a Domain Theory to this context.
    
    @param	theory	new theory to be added, covering kinship terms of Reference or Address.
    
    @throws KSInternalErrorException if the new theory will over-write an existing one
     */
    public void addDomainTheory(DomainTheory theory) throws KSInternalErrorException {
        theory.ctxt = this;
        if (theory.addressTerms) {
            if (domTheoryAdr == null || domTheoryAdr.isEmpty()) {
                domTheoryAdr = theory;
            } else {
                // Msg for developer. Not translated.
                String st = "Attempt to over-write existing Domain Theory (Address) "
                        + "for language " + theory.languageName;
                throw new KSInternalErrorException(st);
            }
        } else {
            if (domTheoryRef == null || domTheoryRef.isEmpty()) {
                domTheoryRef = theory;
            } else {
                // Msg for developer. Not translated.
                String st = "Attempt to over-write existing Domain Theory (Reference) "
                        + "for language " + theory.languageName;
                throw new KSInternalErrorException(st);
            }
        }
        if (userDefinedProperties == null) {
            userDefinedProperties = theory.userDefinedProperties;
        } else if (theory.userDefinedProperties != null) {
            //  Aha!  Both theories have non-null TreeMaps of userDefinedProperties
            //  We must merge them.  IF THERE ARE CONFLICTS, the newly-added theory's version will overwrite.
            Iterator udpIter = theory.userDefinedProperties.entrySet().iterator();
            Map.Entry entry;
            while (udpIter.hasNext()) {
                entry = (Map.Entry) udpIter.next();
                userDefinedProperties.put(entry.getKey(), entry.getValue());
            }  //  end iteration over all entries in new theory's TMap
        }  //  end of 2 non-null TreeMaps of userDefinedProperties
    }  // end of method addDomainTheory
    
    /** This method is intended for use only on contexts already in the Library;
     *  use on a context under construction may have harmful effects.
     * 
     * @return  the full theory of reference terms as stored on disk
     * @throws KSParsingErrorException
     * @throws JavaSystemException
     * @throws KSBadHornClauseException
     * @throws KSInternalErrorException
     * @throws KSConstraintInconsistency 
     */
    public DomainTheory updateDomTheoryRef()  throws KSParsingErrorException, JavaSystemException, KSBadHornClauseException,
            KSInternalErrorException, KSConstraintInconsistency {
        Context originalCtxt = current;
        if (domTheoryRef == null) {
            DomainTheory dt = domTheoryRef();
            current = originalCtxt; 
            return dt;
        }
        String fileName = Library.thyDirectory + languageName + ".thy";
        DomainTheory newDT = Library.readThyFile(fileName, true);
        if (newDT.theory.size() >= domTheoryRef.theory.size()) {
            domTheoryRef = newDT;
        }
        current = originalCtxt; 
        return domTheoryRef;
    } 

    /** This method is intended for use only on contexts already in the Library;
     *  use on a context under construction may have harmful effects.
     * 
     * @return  the full theory of address terms as stored on disk
     * @throws KSParsingErrorException
     * @throws JavaSystemException
     * @throws KSBadHornClauseException
     * @throws KSInternalErrorException
     * @throws KSConstraintInconsistency 
     */
    public DomainTheory updateDomTheoryAdr()  throws KSParsingErrorException, JavaSystemException, KSBadHornClauseException,
            KSInternalErrorException, KSConstraintInconsistency {
        Context originalCtxt = current;
        if (domTheoryAdr == null) {
            DomainTheory dt = domTheoryAdr();
            current = originalCtxt; 
            return dt;
        }
        String fileName = Library.thyDirectory + languageName + "(Adr).thy";
        DomainTheory newDT = Library.readThyFile(fileName, true);
        if (newDT.theory.size() >= domTheoryAdr.theory.size()) {
            domTheoryAdr = newDT;
        }
        current = originalCtxt;
        return domTheoryAdr;
    } 

    /** Returns the Domain Theory for Terms of Reference in this context.	*/
    public DomainTheory domTheoryRef() throws KSParsingErrorException, JavaSystemException, KSBadHornClauseException,
            KSInternalErrorException, KSConstraintInconsistency {
        if (domTheoryRef != null) {
            return domTheoryRef;
        }
        String fileName = Library.thyDirectory + languageName + ".thy";
        domTheoryRef = Library.readThyFile(fileName);
        return domTheoryRef;
    }  //  end of accessor method domTheoryRef

    /** Returns the Domain Theory for Terms of Address in this context.	*/
    public DomainTheory domTheoryAdr() throws KSParsingErrorException, JavaSystemException, KSBadHornClauseException,
            KSInternalErrorException, KSConstraintInconsistency {
        if (domTheoryAdr != null) {
            return domTheoryAdr;
        }
        String fileName = Library.thyDirectory + languageName + "(Adr).thy";
        domTheoryAdr = Library.readThyFile(fileName);
        return domTheoryAdr;
    }  //  end of accessor method domTheoryAdr

    public boolean hasIssues() {
        if (domTheoryRef != null && !domTheoryRef.issuesForUser.isEmpty()) {
            return true;
        }
        if (domTheoryAdr != null && !domTheoryAdr.issuesForUser.isEmpty()) {
            return true;
        }
        return false;
    }
    
    public boolean domTheoryAdrLoaded() {
        return domTheoryAdr != null;
    }

    public boolean domTheoryRefLoaded() {
        return domTheoryRef != null;
    }

    /** Returns true if a domain theory for Terms of Address exists. It may not be loaded into memory yet.	*/
    public boolean domTheoryAdrExists() {
        if (domTheoryAdr != null) {
            return true;
        }
        Library.ContextStub cStub = Library.retrieveOrCreateStub(languageName);
        return cStub.adrThyExists;
    }

    /** Returns true if a domain theory for Terms of Reference exists. It may not be loaded into memory yet.	*/
    public boolean domTheoryRefExists() {
        if (domTheoryRef != null) {
            return true;
        }
        Library.ContextStub cStub = Library.retrieveOrCreateStub(languageName);
        return cStub.refThyExists;
    }

    void domTheoryRefNullify() {
        domTheoryRef = null;
    }

    void domTheoryAdrNullify() {
        domTheoryAdr = null;
    }

    /** Remove this domain theory from this context.
    @param	dt	theory to be removed.	*/
    public void removeDomainTheory(DomainTheory dt) {
        //   They should all be this simple.
        if (dt.addressTerms) {
            domTheoryAdr = null;
        } else {
            domTheoryRef = null;
        }
        dt.ctxt = null;
    }  // end of method removeDomainTheory

    /**Delete all dyads associated with node from the dyadsDefined
     * and dyadsUndefined of the 2 DomainTheories for this context.
     *
     * @param node  The source of any dyads.
     */
    void deleteDyads(Node node, int egoSerial) {
        if (node == null) {
            return;
        }
        if (node.indiv != null) {
            int alterSerial = node.indiv.serialNmbr;
            String kinType = node.pcString;
            if (domTheoryRef != null) {
                for (Object o : node.kinTermsRef) {
                    String kinTerm = (String) o;
                    deleteDyad(domTheoryRef, kinTerm, kinType, egoSerial, alterSerial);
                }
                for (Object o : node.extKinTermsRef) {
                    deleteDyad(domTheoryRef, (String) o, kinType, egoSerial, alterSerial);
                }
            }
            if (domTheoryAdr != null) {
                for (Object o : node.kinTermsAddr) {
                    deleteDyad(domTheoryAdr, (String) o, kinType, egoSerial, alterSerial);
                }
                for (Object o : node.extKinTermsAddr) {
                    deleteDyad(domTheoryAdr, (String) o, kinType, egoSerial, alterSerial);
                }
            }
        }
    }

    void addDyad(DomainTheory dt, Dyad d) {
        DyadTMap tm;
        if (dt.theory.containsKey(d.kinTerm)) {
            tm = dt.dyadsDefined;
        } else {
            tm = dt.dyadsUndefined;
        }
        tm.dyAdd(d);
    }

    void confirmDyad(DomainTheory dt, Dyad dy1) {
        DyadTMap tm;
        if (dt.theory.containsKey(dy1.kinTerm)) {
            tm = dt.dyadsDefined;
        } else {
            tm = dt.dyadsUndefined;
        }
        ArrayList<Dyad> lst = tm.findDyadList(dy1);
        for (Dyad dy2 : lst) {
            if (dy1.equals(dy2)) {
                dy2.confirmed = true;
                dy2.challenged = false;
                break;
            }
        } // if we get here, did not find a match
        String msg = "Did not find match for dyad: " + dy1;
        //  Msg for developer. Not translated.
        MainPane.displayError(msg, "Internal Error", JOptionPane.ERROR_MESSAGE);
    }

    void deleteDyad(DomainTheory dt, String kinTerm, String kinType,
            int egoSerial, int alterSerial) {
        TreeMap kinTypeTree, dyadTMap = dt.dyadsDefined;
        if (dyadTMap.containsKey(kinTerm)) {
            kinTypeTree = (TreeMap) dyadTMap.get(kinTerm);
        } else {
            dyadTMap = dt.dyadsUndefined;
            kinTypeTree = (TreeMap) dyadTMap.get(kinTerm);
        }
        if (kinTypeTree != null && kinTypeTree.containsKey(kinType)) {
            ArrayList<Object> dyads = (ArrayList<Object>) kinTypeTree.get(kinType);
            Iterator dyIter = dyads.iterator();
            while (dyIter.hasNext()) {
                Dyad dy = (Dyad) dyIter.next();
                if (dy.ego.serialNmbr == egoSerial
                        && dy.alter.serialNmbr == alterSerial) {
                    dyIter.remove();
                    break;
                }
            }  //  if we deleted a singleton, clean up.
            if (dyads.isEmpty()) {
                kinTypeTree.remove(kinType);
            }
            if (kinTypeTree.isEmpty()) {
                dyadTMap.remove(kinTerm);
            }
        }
    }

    public ArrayList<Object> getPair() throws KSInternalErrorException {
        //  return 1 male and 1 female person from IndividualCensus
        ArrayList<Object> pair = new ArrayList<Object>();
        Individual first = individualCensus.get(0), second;
        String nextSex = (first.gender.equals("M") ? "F" : "M");
        pair.add(first);
        Iterator iter = individualCensus.iterator();
        while (iter.hasNext()) {
            second = (Individual) iter.next();
            if (second.gender.equals(nextSex)) {
                pair.add(second);
                return pair;
            }  //  end of BINGO
        }  //  end of loop thru individualCensus
        //  Msg for developer. Not translated.
        throw new KSInternalErrorException("Context.getPair couldn't find 1 male & 1 female.");
     }  //  end of method getPair

    /**
    Add an individual (stored by serial number) to this context.
    @param	indiv	person to be added	*/
    public void addIndividual(Individual indiv) {  // just for type-checking
        individualCensus.add(indiv.serialNmbr, indiv);
    }  // end of method addIndividual

    /**
    Add a family (stored by serial number) to this context.
    
    @param	fam		family to be added	*/
    public void addFamily(Family fam) {  // just for type-checking
        familyCensus.add(fam.serialNmbr, fam);
    }  // end of method addFamily

    /**
    Build a string with a brief summary of this context.
    
    @return	a string containing:
    <ul>
    <li>		Lanuguage name
    <li>		Field worker's comments (if any)
    <li>		A count of the individuals and families in this context
    </ul>
     */
    public String toString() {
        String image = createDate + "\nContext for " + languageName + " language";
        if (comments == null || comments.length() == 0) {
            image += " is:\n";
        } else {
            image += " (" + comments + ") is:\n";
        }
        image += indSerNumGen + " individuals and " + famSerNumGen + " families registered.\n";
        return image;
    }  // end of description method -- short (default)

    /** Write out a domain theory to a file.
    
    @param	file	name of file to hold a complete theory.
    @param	type	if type = 'address' the Terms of Address are written. Else Terms of Reference.
    
    @throws	an internal error if the chosen domain theory does not exist or can't be written.	*/
    public void printTheory(PrintWriter file, String type) throws KSInternalErrorException {
        if (type.equals("address")) {
            domTheoryAdr.toThyFile(file);
        } else {
            domTheoryRef.toThyFile(file);
        }
    }  //  end of method printTheory

    /** Write out a domain theory for Terms of Reference to a file.
    
    @param	file	name of file to hold a complete theory.
    
    @throws	an internal error if the chosen domain theory does not exist or can't be written.	*/
    public void printTheory(PrintWriter file)
            throws KSInternalErrorException {
        domTheoryRef.toThyFile(file);
    }  //  end of method printTheory

    /** Write to a file a complete census of Families and Individuals for this context.
    
    @param	file	name of file to hold the census. 	*/
    public void printCensus(PrintWriter file) {
        file.print(createDate + "\nContext for " + languageName + " language");
        if (comments == null || comments.length() == 0) {
            file.println(" is:");
        } else {
            file.println(" (" + comments + ") is:");
        }
        file.println(indSerNumGen + " individuals and " + famSerNumGen + " families registered.\n");
        file.println("Individuals:");
        for (int i = 0; i < individualCensus.size(); i++) {
            file.println(individualCensus.get(i));
        }
        file.println("\nFamilies:");
        for (int j = 0; j < familyCensus.size(); j++) {
            file.println(familyCensus.get(j));
        }
        file.println("\n-------End------\n");
        file.flush();
        file.close();
    }  //  end of method printCensus

    /**
    <p>Write output file in GEDCOM (v 5.5) format, containing all individuals and
    families in this context.
    </p>
    File will contain:
    <ul>
    <li>	Header: Source program & version, </li>
    <li>	Destination program & version, </li>
    <li>	Date, Filename, GEDCOM version </li>
    <li>	Individuals:  1 GEDCOM record each </li>
    <li>	Families:  1 GEDCOM record each </li>
    <li>	Trailer. </li>
    </ul>
    
    @param	outFile		a {@link PrintWriter} to write to.
    @param	fileName	direct filename; required extension is '.ged'
     */
    public void exportGEDCOM(PrintWriter outFile, String fileName, boolean realData, 
            String destination, String includeAuxs) {
        // Write out the header, all individual and family records, & then trailer.
        outFile.println("0 HEAD");
        outFile.println("1 SOUR SILKin");
        outFile.println("2 VERS 1.2");
        outFile.println("2 NAME SIL Kinship Analysis Tools");
        outFile.println("1 DEST " + destination);
        String today = UDate.today();
        outFile.println("1 DATE " + UDate.xsdToEuropean(today));
        //  File is required to have extension ".GED"
        outFile.println("1 FILE " + fileName.substring(0, fileName.lastIndexOf(".")));
        outFile.println("1 GEDC");
        outFile.println("2 VERS 5.5");
        outFile.println("2 FORM LINEAGE-LINKED");
        outFile.println("1 CHAR UTF-8");
        if (domTheoryRef != null && domTheoryRef.author != null && !domTheoryRef.author.isEmpty()) {
            int ref = dataAuthors.indexOf(domTheoryRef.author);
            if (ref > -1) {
                outFile.println("1 SUBM @" + ref + "@");
            }
        }
        for (int d=0; d < dataAuthors.size(); d++) {
            outFile.println("0 @" + d + "@ SUBM");
            outFile.println("1 NAME " + dataAuthors.get(d));
        }        
        Individual ind;
        Family fam;
        if (DomainTheory.current == null) {
            DomainTheory.current = domTheoryRef;
        }
        Iterator iter1 = individualCensus.iterator();
        while (iter1.hasNext()) {
            ind = (Individual) iter1.next();
            String adoptNote = encodeAdoptionEvent(ind);
            if (! ind.deleted) {
                ind.exportGEDCOM(outFile, realData, includeAuxs, 
                        DomainTheory.current.nonTerms, adoptNote);
            }
        }  //  end of loop through Individuals
        iter1 = familyCensus.iterator();
        while (iter1.hasNext()) {
            fam = (Family) iter1.next();
            if (! fam.deleted) {
                fam.exportGEDCOM(outFile);
            }
        }  //  end of loop through Families
        if (gedcomNotes != null) {
            iter1 = gedcomNotes.values().iterator();
            while (iter1.hasNext()) {
                ParserGEDCOM.GEDCOMitem itm = (ParserGEDCOM.GEDCOMitem)iter1.next();
                outFile.println(itm.toGEDCOMString());
            }
        }
        if (gedcomSources != null) {
            iter1 = gedcomSources.values().iterator();
            while (iter1.hasNext()) {
                ParserGEDCOM.GEDCOMitem itm = (ParserGEDCOM.GEDCOMitem)iter1.next();
                outFile.println(itm.toGEDCOMString());
            }
        }
        if (gedcomExtra != null) {
            iter1 = gedcomExtra.values().iterator();
            while (iter1.hasNext()) {
                ParserGEDCOM.GEDCOMitem itm = (ParserGEDCOM.GEDCOMitem)iter1.next();
                outFile.println(itm.toGEDCOMString());
            }
        }
        outFile.println("0 TRLR");
        return;
    }  // end of method exportGEDCOM
    
    String encodeAdoptionEvent(Individual ind) {
        String note = "";
        TreeMap<String, ArrayList<Individual>> adoptions =
                inverseSpecialRelationships.get(ind);
        if (adoptions == null) {
            return null;
        }
        ArrayList<Individual> parents = new ArrayList<Individual>();
        Iterator iter = adoptions.values().iterator();
        while (iter.hasNext()) {
            ArrayList<Individual> subList = (ArrayList<Individual>)iter.next();
            for (Individual p : subList) {
                if (! parents.contains(p)) {
                    parents.add(p);
                }
            }
        }
        ArrayList familyUnits = findAdoptiveFamilies(parents);  
        while (! familyUnits.isEmpty()) {
            //  always an even number of elements
            Family fam = (Family)familyUnits.remove(0);
            String typ = (String)familyUnits.remove(0);
            note += "1 ADOP Y\n2 FAMC @F" + fam.serialNmbr + "@";
            note += "\n2 ADOPTED_BY_WHICH_PARENT " + typ + "\n";
        }        
        return note;
    }

    ArrayList findAdoptiveFamilies(ArrayList<Individual> parents) {
        // return as many pairs as possible: pair = Family, HUSB/WIFE/BOTH
        ArrayList results = new ArrayList();
        while (!parents.isEmpty()) {
            Family fam = null;
            String typ = "";
            Individual mate1 = parents.remove(0),
                    mate2 = null;
            if (mate1.marriages.isEmpty()) {
                // Must be a single adoptive parent. Make a single-parent family unit
                try {
                    fam = new Family(this, true);
                    typ = (mate1.gender.equals("M") ? "HUSB" : "WIFE");
                    fam.addParent(mate1);
                    fam.homeChart = mate1.homeChart;
                    fam.location = new Point(mate1.location.x + 10, mate1.location.y);
                    fam.comment += "This family created to provide a GEDCOM";
                    fam.comment += "$$br$$adoptive family. No sexual union is implied.";
                } catch (Exception exc) {
                    //  addParent should have no exceptions
                }
            } else {  // she's married
                for (Object f : mate1.marriages) {
                    fam = (Family) f;
                    Individual mate = (fam.wife == mate1 ? fam.husband : fam.wife);
                    if (mate != null && parents.contains(mate)) {
                        mate2 = mate;
                        parents.remove(mate);
                        typ = "BOTH";
                        break;
                    }
                } // 
                if (typ.isEmpty()) {
                    typ = (mate1.gender.equals("M") ? "HUSB" : "WIFE");
                }
            }
            results.add(fam);
            results.add(typ);
        }
        return results;
    }
    boolean adoptionsInitialized = false;
    
    void initializeAdoptions(String adoptionPred) {
        if (adoptionsInitialized) {
            return;
        }
        if (specialRelationships == null) {
            specialRelationships = new TreeMap<String, ArrayList<SpecRelTriple>>();
        }
        if (inverseSpecialRelationships == null) {
            inverseSpecialRelationships = new TreeMap<Individual, TreeMap<String, ArrayList<Individual>>>();
        }
        if (userDefinedProperties == null) {
            userDefinedProperties = new TreeMap<String, UserDefinedProperty>();
        }
        UserDefinedProperty defaultUDP = (UserDefinedProperty) userDefinedProperties.get(adoptionPred);
        if (defaultUDP == null) {
            defaultUDP = new UserDefinedProperty(adoptionPred);
            userDefinedProperties.put(adoptionPred, defaultUDP);
            defaultUDP.chartable = true;
            defaultUDP.singleValue = false;
            defaultUDP.typ = "individual";
            defaultUDP.chartColor = Color.RED;
        }
        if (domTheoryRefExists()) {
            try {
                DomainTheory dt = domTheoryRef();
                dt.userDefinedProperties = userDefinedProperties;
                if (domTheoryAdrExists()) {
                    dt = domTheoryAdr();
                    dt.userDefinedProperties = userDefinedProperties;
                }
            } catch (Exception exc) {
            } // No parsing errors expected.
        }
        if (SIL_Edit.edWin != null) {
            SIL_Edit.edWin.getPPanel().initUDPCombo();
        }
        Individual ind;
        UserDefinedProperty newInstance;
        Iterator indIter = individualCensus.iterator();
        while (indIter.hasNext()) {
            ind = (Individual) indIter.next();
            newInstance = new UserDefinedProperty(defaultUDP, true);  //  clone the new template
            if (ind.userDefinedProperties == null) {
                ind.userDefinedProperties = new TreeMap<String, UserDefinedProperty>();
            }
            ind.userDefinedProperties.put(adoptionPred, newInstance);
        }  //  end of loop thru individuals
        insertAdoptionPriority(adoptionPred);
        saveState = true;
        adoptionsInitialized = true;
    }
    
    void addSpecialRelationship(SpecRelTriple trpl, String chartLtr) {
        // This method used by GEDCOM Parser and CB.generateExamples, so child/parent are never links.
        if (specialRelationships == null) {
            specialRelationships = new TreeMap<String, ArrayList<SpecRelTriple>>();
        }
        if (inverseSpecialRelationships == null) {
            inverseSpecialRelationships = new TreeMap<Individual, TreeMap<String, ArrayList<Individual>>>();
        }
        if (specialRelationships.get(chartLtr) == null) {
            specialRelationships.put(chartLtr, new ArrayList<SpecRelTriple>());
        }
        ArrayList<SpecRelTriple> array = specialRelationships.get(chartLtr);
        if (!array.contains(trpl)) {
            array.add(trpl);
        }
        Individual kid;
        if (trpl.child instanceof Link) {
            kid = ((Link)trpl.child).personPointedTo;
        } else {
            kid = (Individual)trpl.child;
        }
        Locatable parent = (Locatable)trpl.parent;        
        if (inverseSpecialRelationships.get(kid) == null) {
            inverseSpecialRelationships.put(kid, new TreeMap<String, ArrayList<Individual>>());
        }
        TreeMap<String, ArrayList<Individual>> tmap = inverseSpecialRelationships.get(kid);
        if (tmap.get(trpl.udpName) == null) {
            tmap.put(trpl.udpName, new ArrayList<Individual>());
        }
        if (parent instanceof Individual) {
            tmap.get(trpl.udpName).add((Individual)parent);    
        } else {
            Family f = (Family)parent;
            if (f.husband != null) {
                tmap.get(trpl.udpName).add(f.husband);
            }
            if (f.wife != null) {
                tmap.get(trpl.udpName).add(f.wife);
            }
        }
    }
    
    void loadDefaultKinTypeStuff() {
    //  Since no link priority order is specified for this context, load the deaults
        kinTypeOrder = new ArrayList<String>();
        kinTypePriority = new ArrayList<String>();
        for (String kt : defaultKinTypeOrder) {
            kinTypeOrder.add(kt);
        }
        for (String prio : defaultKinTypePriority) {
            kinTypePriority.add(prio);
        }
        kinTypePriorityTMap = new TreeMap<String, String>();
        for (int i=0; i < kinTypeOrder.size(); i++) {
            kinTypePriorityTMap.put(defaultKinTypeOrder[i], defaultKinTypePriority[i]);
        }
    }
    
    /** Rarely, if the various indexes and pre-computed tables get out of synch,
     *  it is necessary to rebuild the KinTermMatrix and all the other indexes
     *  from scratch. The rebuilding process (SIL_Edit.rebuildKTMatrixEtc)
     *  uses a set of rules for propagating kinship links from one individual to
     *  another. Those 'LinkMethods' are all inner classes of {@link SIL_Edit}.
     *  they will change if, for example, a new UDP is created or an old one 
     *  is deleted.
     */
    public void rebuildLinkMethods() {
        SIL_Edit.linkMethods = SIL_Edit.buildLinkMethods();
        if (userDefinedProperties != null) {
            Iterator udIter = userDefinedProperties.values().iterator();
            while (udIter.hasNext()) {
                UserDefinedProperty ud = (UserDefinedProperty) udIter.next();
                if (ud.chartable) {
                    SIL_Edit.linkMethods.put(ud.starName, new SIL_Edit.AdoptMethod());
                }
            }
        }
    }
    
    void deleteConnectingUDPVal(Individual ind, String starName, String val, boolean sameVal) {
        if (connectingUDPValueLists == null || connectingUDPValueLists.get(starName) == null
                || connectingUDPValueLists.get(starName).get(val) == null) {
            return;
        }
        if (sameVal) {  //  disconnect on equal values
            ArrayList<Individual> sameList = connectingUDPValueLists.get(starName).get(val);
            sameList.remove(ind);
            for (Individual other : sameList) {
                other.starLinks.remove(ind);
                ind.starLinks.remove(other);
            }
        } else {  //  disconnect on different values
            TreeMap<String, ArrayList<Individual>> tree = connectingUDPValueLists.get(starName);
            Iterator iter = tree.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String value = (String) entry.getKey();
                ArrayList<Individual> list = (ArrayList<Individual>) entry.getValue();
                if (value.equals(val)) {
                    list.remove(ind);
                } else {  //  a different value
                    for (Individual other : list) {
                        other.starLinks.remove(ind);
                        ind.starLinks.remove(other);
                    }
                }
            }
        }
    }
    
    void addConnectingUDPVal(Individual ind, String starName, String val, boolean sameVal) {
        if (connectingUDPValueLists == null) {
            connectingUDPValueLists = new TreeMap<String, TreeMap<String, ArrayList<Individual>>>();
        }
        if (connectingUDPValueLists.get(starName) == null) {
            connectingUDPValueLists.put(starName, new TreeMap<String, ArrayList<Individual>>());
        }
        if (connectingUDPValueLists.get(starName).get(val) == null) {
            connectingUDPValueLists.get(starName).put(val, new ArrayList<Individual>());
        }
        if (ind.starLinks == null) {
            ind.starLinks = new ArrayList();
        }
        if (sameVal) {  //  connect on equal values
            //  make a StarLink between ind and every other person who shares this value
            ArrayList<Individual> sameList = connectingUDPValueLists.get(starName).get(val);
            for (Individual other : sameList) {
                if (other.starLinks == null) {
                    other.starLinks = new ArrayList();
                }
                other.starLinks.add(ind);
                ind.starLinks.add(other);
            }
            sameList.add(ind);
        } else {  //  connect on different values
            TreeMap<String, ArrayList<Individual>> tree = connectingUDPValueLists.get(starName);
            Iterator iter = tree.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String value = (String) entry.getKey();
                ArrayList<Individual> list = (ArrayList<Individual>) entry.getValue();
                if (value.equals(val)) {
                    list.add(ind);
                } else {  //  a different value
                    for (Individual other : list) {
                        if (other.starLinks == null) {
                            other.starLinks = new ArrayList();
                        }
                        other.starLinks.add(ind);
                        ind.starLinks.add(other);
                    }
                }
            }
        }
    }
    
    void addConnectionLists(UserDefinedProperty udp) {
        if (connectingUDPValueLists == null) {
            connectingUDPValueLists = new TreeMap<String, TreeMap<String, ArrayList<Individual>>>();
        }
        TreeMap<String, ArrayList<Individual>> connecTree = new TreeMap<String, ArrayList<Individual>>();
        connectingUDPValueLists.put(udp.starName, connecTree);
        for (Object o : udp.validEntries) {
            String valid = (String) o;  //  Connecting UDPs must have type = string
            connecTree.put(valid, new ArrayList<Individual>());  //  1 branch for each valid value
        }
        for (Individual ind : individualCensus) {
            UserDefinedProperty indUDP = (UserDefinedProperty) ind.userDefinedProperties.get(udp.starName);
            if (ind.deleted || indUDP.value.isEmpty()) {
                continue;
            }
            if (ind.starLinks == null) {
                ind.starLinks = new ArrayList();
            }
            String indVal = (String) indUDP.value.get(0);
            if (udp.sameVal) {  //  connect on equal values
                //  make a StarLink between ind and every other person who shares this value
                ArrayList<Individual> sameList = connecTree.get(indVal);
                for (Individual other : sameList) {
                    if (other.starLinks == null) {
                        other.starLinks = new ArrayList();
                    }
                    other.starLinks.add(ind);
                    ind.starLinks.add(other);
                }
                sameList.add(ind);
            } else {  //  connect on different values
                Iterator iter = connecTree.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String value = (String) entry.getKey();
                    ArrayList<Individual> list = (ArrayList<Individual>) entry.getValue();
                    if (value.equals(indVal)) {
                        list.add(ind);
                    } else {  //  a different value
                        for (Individual other : list) {
                            if (other.starLinks == null) {
                                other.starLinks = new ArrayList();
                            }
                            other.starLinks.add(ind);
                            ind.starLinks.add(other);
                        }
                    }
                }
            }
        }
    }
    
    void deleteConnectionList(UserDefinedProperty udp) {
        if (connectingUDPValueLists == null || 
                connectingUDPValueLists.get(udp.starName) == null) {
            return;
        }
        TreeMap<String, ArrayList<Individual>> connecTree = 
                connectingUDPValueLists.remove(udp.starName);
        if (udp.sameVal) {
            Iterator iter1 = connecTree.values().iterator();
            while (iter1.hasNext()) {
                ArrayList<Individual> valTree = (ArrayList<Individual>)iter1.next();
                while (!valTree.isEmpty()) {
                    Individual ind = valTree.remove(0);
                    for (Individual other : valTree) {
                        ind.starLinks.remove(other);
                        other.starLinks.remove(ind);
                    }
                }                
            }
        } else {  //  different values
            Iterator iter1 = connecTree.entrySet().iterator();
            while (iter1.hasNext()) {
                Map.Entry entry1 = (Map.Entry)iter1.next();
                String thisVal = (String)entry1.getKey();
                ArrayList<Individual> thisValHolders = (ArrayList<Individual>)entry1.getValue();
                for (Individual ind : thisValHolders) {
                    Iterator iter2 = connecTree.entrySet().iterator();
                    while (iter2.hasNext()) {
                        Map.Entry entry2 = (Map.Entry)iter2.next();
                        String otherVal = (String)entry2.getKey();
                        if (thisVal.equals(otherVal)) {
                            continue;
                        }
                        ArrayList<Individual> otherValHolders = 
                                (ArrayList<Individual>)entry2.getValue();
                        for (Individual other : otherValHolders) {
                            other.starLinks.remove(ind);
                        }
                    }
                }
            }
        }
    }
    
    public boolean hasNonChartables(ArrayList miniPreds) {
        for (Object o : miniPreds) {
            String s = (String)o;
            int paren = s.indexOf("(");
            if (s.startsWith("*inverse") && ! kinTypeOrder.contains("*" + s.substring(8, paren))) {
                return true;
            }
            if (s.startsWith("*") && !s.startsWith("*inverse") 
                    && ! kinTypeOrder.contains(s.substring(0, paren))) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isNonChartable(String pred) {
        if (pred.startsWith("*inverse") && 
                !kinTypeOrder.contains("*" + pred.substring(8))) {
            return true;
        }
        if (pred.startsWith("*") && !pred.startsWith("*inverse")
                && !kinTypeOrder.contains(pred)) {
            return true;
        }
        return false;
    }
    
    public boolean isAdoptionPred(String pred) {
        return (pred.startsWith("*") && !pred.startsWith("*inverse")
                && kinTypeOrder.contains(pred));
    }
    
    public boolean isInverseAdoptionPred(String pred) {
        return (pred.startsWith("*inverse") && 
                kinTypeOrder.contains(pred.substring(8)));
    }
    
    public void insertAdoptionPriority(String newUDPname) {
        ArrayList<String> refSet = new ArrayList<String>();
        for (int i=0; i < 9; i++) {
            refSet.add(defaultKinTypeOrder[i]);
        }
        int ndx;
        for (ndx=0; ndx < kinTypeOrder.size(); ndx++) {
            refSet.remove(kinTypeOrder.get(ndx));
            if (refSet.isEmpty()) {
                break;
            }
        }
        kinTypeOrder.add(++ndx, newUDPname);
        kinTypePriority.add(ndx, "C");
        kinTypePriorityTMap.put(newUDPname, "C");
        if (SIL_Edit.linkMethods.get(newUDPname) == null) {
            SIL_Edit.linkMethods.put(newUDPname, new SIL_Edit.AdoptMethod());
        }
    }
    
    public void removeAdoptionPriority(String udName) {
        kinTypeOrder.remove(udName);
        kinTypePriority.remove(udName);
        kinTypePriorityTMap.remove(udName);
        SIL_Edit.linkMethods.remove(udName);
    }
    
    public void renameChartableUDP(String oldName, String newName) {
        if (specialRelationships != null) {
            Iterator srIter = specialRelationships.values().iterator();
            while (srIter.hasNext()) {  //  Update Special Relationships
                ArrayList<Context.SpecRelTriple> trips =
                        (ArrayList<Context.SpecRelTriple>) srIter.next();
                for (Context.SpecRelTriple triple : trips) {
                    if (triple.udpName.equals(oldName)) {
                        triple.udpName = newName;
                    }
                }
            }
        }
        if (inverseSpecialRelationships != null) {
            Iterator irs = inverseSpecialRelationships.values().iterator();
            while (irs.hasNext()) {  //  Update Inverse Special Relationships
                TreeMap<String, ArrayList<Individual>> ir =
                        (TreeMap<String, ArrayList<Individual>>) irs.next();
                if (ir.get(oldName) != null) {
                    ArrayList<Individual> lst = ir.get(oldName);
                    ir.remove(oldName);
                    ir.put(newName, lst);
                }
            }
        }
        rebuildLinkMethods();    //  Update link methods
        int ndx = kinTypeOrder.indexOf(oldName);  //  Update Kin Type Ordering
        if (ndx > -1) {
            kinTypeOrder.remove(ndx);
            kinTypeOrder.add(ndx, newName);
            kinTypePriorityTMap.remove(oldName);
            kinTypePriorityTMap.put(newName, kinTypePriority.get(ndx));
        }
        ktm.renameChartableUDP(oldName, newName);
        kti.renameChartableUDP(oldName, newName);
    }
    
    public void removeChartableUDP(String udpName) {
        if (specialRelationships != null) {
            Iterator srIter = specialRelationships.values().iterator();
            while (srIter.hasNext()) {  //  Update Special Relationships
                ArrayList<Context.SpecRelTriple> trips =
                        (ArrayList<Context.SpecRelTriple>) srIter.next();
                Iterator tripIter = trips.iterator();
                while (tripIter.hasNext()) {
                    Context.SpecRelTriple triple = (Context.SpecRelTriple)tripIter.next();
                    if (triple.udpName.equals(udpName)) {
                        tripIter.remove();
                    }
                }
            }
        }
        if (inverseSpecialRelationships != null) {
            Iterator irs = inverseSpecialRelationships.values().iterator();
            while (irs.hasNext()) {  //  Update Inverse Special Relationships
                TreeMap<String, ArrayList<Individual>> ir =
                        (TreeMap<String, ArrayList<Individual>>) irs.next();
                ir.remove(udpName);
                if (ir.isEmpty()) {
                    irs.remove();
                }
            }
        }
        rebuildLinkMethods();    //  Update link methods
        int ndx = kinTypeOrder.indexOf(udpName);  //  Update Kin Type Ordering
        if (ndx > -1) {
            kinTypeOrder.remove(ndx);
            kinTypePriorityTMap.remove(udpName);
        }
        SIL_Edit.edWin.rebuildKTMatrixEtc();
    }
    
    public String getChartDescription(String chart) {
        String description = Library.messages.getString("noDescriptionFound");
        int index = Arrays.binarySearch(chartLtrs, chart);
        if (index >= 0 && index < chartDescriptions.size()) {
            description = chartDescriptions.get(index);
        }        
        return description;
    }
    
    public int getChartIndex(String chartLtr) {
        return Arrays.binarySearch(chartLtrs, chartLtr);
    }
    
    public String getChartLtr(int n) {
        return chartLtrs[n];
    }
    
    public String getNextChartLtr() {
        int nextIndex = chartDescriptions.size();
        if (nextIndex >= chartLtrs.length) {
            return null;
        }
        return chartLtrs[nextIndex];
    }
    
    /** Write a tab-delimited file that formats the Kin Term Matrix 
     *  as a spreadsheet.
     * 
     * @param out a PrintWriter onto which we write
     * @param typ either "Reference" or "Address"
     */
    public void exportKTM(PrintWriter out, String typ) {
        ArrayList<Individual> folks = individualCensus;
        out.println("\t\tKin Term Matrix for " + languageName 
                + " terms of " + typ + "\n");
        out.print("Serial Nmbr \tName");
        int size = ktm.matrix.size();  // = number rows and columns in KTM
        for (int i=0; i < size; i++) {
            out.print("\t  #" + i);
        }
        out.println();  //  This ends the row of column titles
        Iterator egoIter = ktm.matrix.entrySet().iterator();
        //  Write out a row for each Ego in the KTM
        while (egoIter.hasNext()) {
            Map.Entry EgoEntry = (Map.Entry) egoIter.next();
            int egoSerial = ((Integer) EgoEntry.getKey()).intValue();
            String name = folks.get(egoSerial).name;
            out.print(" #" + egoSerial + "\t" + name);
            TreeMap row = (TreeMap) EgoEntry.getValue();
            if (row.size() > 0) {  //  Write rest of the row. 
                Iterator alterIter = ((TreeMap) EgoEntry.getValue()).entrySet().iterator();
                int ndx = 0;
                while (alterIter.hasNext()) {
                    Map.Entry AltEntry = (Map.Entry) alterIter.next();
                    int alterSerial = ((Integer) AltEntry.getKey()).intValue();
                    Node nod = (Node) AltEntry.getValue();
                    for (int i = ndx; i < alterSerial; i++) { // For any missing AlterSerial#, write a tab
                        out.print("\t");
                    }
                    if (nod == null || ! nod.hasKinTerms()) {
                        out.print("\t");
                    } else {  //  write a summary of the node
                        out.print(nod.toExportString(typ));
                    }
                    ndx = alterSerial +1;
                }  //  end of loop thru Alters
            }  //  end of if row size > 0
            out.println();  //  write end of row
        }  //  end of loop thru Egos       
    }
    
    
    private String editorParameters = "";

    /** Accept a new parameter string, then call main writeSILKFile method.
     * 
     * @param f         output file
     * @param params    new parameters
     * @throws FileNotFoundException
     * @throws KSInternalErrorException
     * @throws KSDateParseException 
     */
    public void writeSILKFile(File f, String params) throws FileNotFoundException,
            KSInternalErrorException, KSDateParseException {
        editorParameters = params;
        writeSILKFile(f);
    }

    /**
    Write output file in _.silk format, containing all parameters, dyads, 
    individuals and families in this context. This is an XML-style file, with
    blocks defined as follows:
    <p>
    File will contain:
    <ul>
    <li>	a <parameters> block.  </li>
    <li>	an <IndividualCensus> block containing zero or more <individual> blocks.  </li>
    <li>	a <FamilyCensus> block containing zero or more <family> blocks  </li>
    <li>	a <matrix> block containing zero or more <row> blocks (specified below).   
    This block represents the contents of the KinTermMatrix.  </li>
    <li>	a <dyadsUndefinedRef> block containing zero or more <dyad> blocks 
    for terms of reference.  </li>
    <li>	a <dyadsDefinedRef> block containing zero or more <dyad> blocks 
    for terms of reference.  </li>
    <li>	(optionally) a <dyadsUndefinedAddr> block containing zero or more <dyad> blocks 
    for terms of address.  </li>
    <li>	(optionally) a <dyadsDefinedAddr> block containing zero or more <dyad> blocks 
    for terms of address.  </li>
    </ul> </p>
    
    @param	f		a file to write to.
     */
    public void writeSILKFile(File f) throws FileNotFoundException, KSInternalErrorException,
            KSDateParseException {
        String directory = f.getParent(), path, lang;
        PrintWriter silk = null;
        try {
            silk = new PrintWriter(f, "UTF-8");
        } catch (Exception ex) {
            System.err.println("Encoding 'UTF-8' rejected.");
            System.exit(9);
        }
        silk.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        String ml = (Library.menuLanguage.isEmpty() ? "" : "_" + Library.menuLanguage);
        String next = "text/xsl\" href=\"Silk-status_" + ml + ".xsl\"?>";
        silk.println("<?xml-stylesheet type=\"" + next);
        silk.println("<!DOCTYPE SIL_KinData>");
        silk.println("<SIL_KinData>");
        writeSILKGuts(silk, directory);
        if (domTheoryRef != null && domTheoryRef.issuesForUser != null
                && !domTheoryRef.issuesForUser.isEmpty()) {
            DomainTheory.current = domTheoryRef;
            printSuggestions(silk, domTheoryRef, "Reference");
        }
        if (domTheoryAdr != null && domTheoryAdr.issuesForUser != null
                && !domTheoryAdr.issuesForUser.isEmpty()) {
            DomainTheory.current = domTheoryAdr;
            printSuggestions(silk, domTheoryAdr, "Address");
        }
        silk.println("</SIL_KinData>");
        silk.flush();
        silk.close();
        saveState = false;
    }  //  end of method writeSILKFile

    /** This method writes the entire saved state of the SIL_Edit session in XML-like format.
     *  It is used by writeSILKFile for xxx.silk files, and by writeSuggsFile for xxx.sugg files.
     *
     * @param silk      the file to be written
     * @param directory the directory containing 'silk', or null
     * @param sug       true = writing a Suggestion file. false = a SILK file
     *
     * @throws FileNotFoundException        if there's a file system error
     * @throws KSInternalErrorException     if SILKin code crashes
     */
    void writeSILKGuts(PrintWriter silk, String directory)
            throws FileNotFoundException, KSInternalErrorException, KSDateParseException {
        String path, lang;
        silk.println("<parameters>");
        silk.println("  <language name=\"" + languageName + "\"/>");
        if (comments != null && comments.length() > 1) {
            silk.print("  <comments txt=\"");
            comments = convertDoubleQuotes(comments); // replace doubles with singles, etc.
            silk.print(comments);
            silk.println("\"/>");
        }  //  end of optional comments
        if (createDate != null) {
            if (!UDate.validXSD(createDate)) {
                createDate = UDate.convertToXSD(createDate);
            }
            silk.println("  <createDate value=\"" + createDate + "\"/>");
        }  //  end of optional createDate
        silk.println("  <dataAuthors>");
        for (String auth : dataAuthors) {
            silk.println("\t<dataAuthor name=\"" + auth + "\"/>");
        }
        silk.println("  </dataAuthors>");  //  end of dataAuthors
        if (dateOfLastDataChange != null) {
            silk.println("  <lastDataChangeDate value=\"" + dateOfLastDataChange + "\"/>");
        }
        if (dateOfLastSuggestion != null) {
            silk.println("  <lastSuggestionDate value=\"" + dateOfLastSuggestion + "\"/>");
        }
        silk.println("  <indSerNum>" + indSerNumGen + "</indSerNum> ");
        silk.println("  <linkSerNum>" + linkSerNumGen + "</linkSerNum> ");
        silk.println("  <famSerNum>" + famSerNumGen + "</famSerNum> ");
        silk.println("  <polygamyPermit>" + polygamyPermit + "</polygamyPermit> ");
        if (userDefinedProperties != null) {
            silk.println("<userDefinedProperties>");
            Iterator iter = userDefinedProperties.entrySet().iterator();
            Map.Entry entry;
            while (iter.hasNext()) {
                silk.println("\t<UDP>");
                entry = (Map.Entry) iter.next();
                String propName = (String) entry.getKey();
                UserDefinedProperty udp = (UserDefinedProperty) entry.getValue();
                silk.println("\t\t<propertyName>" + propName + "</propertyName>");
                silk.println(udp.toSILKString("full"));
                silk.println("\t</UDP>");
            }  //  end of loop thru UDPs
            silk.println("</userDefinedProperties>");
        }  //  end of optional UDPs
        silk.println("</parameters>");
        silk.println("<editorSettings>");
        if (currentEgo != null) {
            silk.println("  <currentEgo n=\"" + currentEgo.serialNmbr + "\"/>");
        }
        if (editDirectory != null) {
            silk.println("  <editDirectory dir=\"" + editDirectory + "\"/>");
        }
        silk.println("  <currentChart id=\"" + currentChart + "\"/>");
        if (!chartDescriptions.isEmpty()) {
            silk.println("  <chartDescriptions>");
            for (String s : chartDescriptions) {
                silk.println("    <chart name=\"" + s + "\"/>");
            }
            silk.println("  </chartDescriptions>");
        }
        silk.println(editorParameters);
        if (kti.lastSerial != -1) {
            silk.println("  <lastPersonIndexed>" + kti.lastSerial + "</lastPersonIndexed>");
        }
        if (kinTypeOrder != null && ! kinTypeOrder.isEmpty()) {
            silk.println("  <kinTypePriorities maleFirst=\"" + maleFirst + "\">");
            for (int i=0; i < kinTypeOrder.size(); i++) {
                silk.println("\t<kinType name=\"" + kinTypeOrder.get(i) + "\" priority=\"" 
                        + kinTypePriority.get(i) + "\"/>");
            }
            silk.println("  </kinTypePriorities>");            
        }
        silk.println("  <snapToGrid val=\"" + Library.snapToGrid + "\" x=\"" 
                + Library.gridX + "\" y=\"" + Library.gridY + "\"/>");
        silk.println("  <adoptionHelp val=\"" + adoptionHelp + "\"/>");
        silk.println("  <displayGEDCOM val=\"" + displayGEDCOM + "\"/>");
        if (gedcomHeaderItems != null) {
            printGEDCOMTree(silk, "gedcomHeaderItems", gedcomHeaderItems);            
        }
        if (gedcomNotes != null) {
            printGEDCOMTree(silk, "gedcomNotes", gedcomNotes);
        }        
        if (gedcomSources != null) {
            printGEDCOMTree(silk, "gedcomSources", gedcomSources);
        }
        if (gedcomExtra != null) {
            printGEDCOMTree(silk, "gedcomExtra", gedcomExtra);
        }        
        if (specialRelationships != null && !specialRelationships.isEmpty()) {
            silk.println("  <specialRelationships>");
            Iterator srIter = specialRelationships.entrySet().iterator();
            while (srIter.hasNext()) {
                Map.Entry entry = (Map.Entry)srIter.next();
                String chLtr = (String)entry.getKey();
                ArrayList<SpecRelTriple> trips = (ArrayList<SpecRelTriple>)entry.getValue();
                silk.println("    <chart name=\"" + chLtr + "\">");
                for (SpecRelTriple trey : trips) {
                    String parType, kidType;
                    if (trey.parent instanceof Individual) {
                        parType = "I-";
                    } else {
                        parType = (trey.parent instanceof Link ? "L-" : "F-");
                    }
                    kidType = (trey.child instanceof Link ? "L-" : "I-");
                    silk.println("      <specRel parent=\"" + parType + trey.parent.getSerialNmbr() + 
                            "\" child=\"" + kidType + trey.child.getSerialNmbr() +
                             "\" udpName=\"" + trey.udpName + "\"/>");
                }
                silk.println("    </chart>");
            }  
            silk.println("  </specialRelationships>");
        }
        silk.println("</editorSettings>\n");
        if (domTheoryRef != null) {
            silk.println(domTheoryRef.toSILKString(""));
        }
        if (domTheoryAdr != null) {
            silk.println(domTheoryAdr.toSILKString(""));
        }        
        silk.println("<individualCensus>");
        for (Individual ind : individualCensus) {
            silk.println(ind.toSILKString());
        }
        silk.println("</individualCensus>");
        if (!linkCensus.isEmpty()) {
            silk.println("<linkCensus>");
            for (Link lk : linkCensus) {
                silk.println(lk.toSILKString());
            }
            silk.println("</linkCensus>");
        }        
        silk.println("<familyCensus>");
        for (Family fam : familyCensus) {
            silk.println(fam.toSILKString());
        }
        silk.println("</familyCensus>");
        silk.println("<matrix>");
        if (ktm.matrix.size() > 0) {
            silk.println(ktm.toSILKString());
        }
        silk.println("</matrix>");
        silk.println("<dyadsUndefinedRef>");
        if (domTheoryRef != null && domTheoryRef.dyadsUndefined != null) {
            silk.print(domTheoryRef.dyadsUndefined.toSILKString());
        }
        silk.println("</dyadsUndefinedRef>");
        silk.println("<dyadsDefinedRef>");
        if (domTheoryRef != null && domTheoryRef.dyadsDefined != null) {
            silk.print(domTheoryRef.dyadsDefined.toSILKString());
        }
        silk.println("</dyadsDefinedRef>");
        if (domTheoryAdr != null && domTheoryAdr.dyadsUndefined != null) {
            silk.println("<dyadsUndefinedAddr>");
            silk.print(domTheoryAdr.dyadsUndefined.toSILKString());
            silk.println("</dyadsUndefinedAddr>");
        }
        if (domTheoryAdr != null && domTheoryAdr.dyadsDefined != null) {
            silk.println("<dyadsDefinedAddr>");
            silk.print(domTheoryAdr.dyadsDefined.toSILKString());
            silk.println("</dyadsDefinedAddr>");
        }
        if (!kti.isEmpty() && directory != null) {
            silk.println("\n<kinTypeIndex>");
            silk.println(kti.toSILKString());
            silk.println("</kinTypeIndex>");
        }
        if (!learningHistoryRef.isEmpty()) {
            silk.println("\n<learning-history type=\"Ref\">");
            Iterator iter = learningHistoryRef.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, ArrayList<HistoryItem>> entry =
                        (Map.Entry<String, ArrayList<HistoryItem>>) iter.next();
                String kinTerm = entry.getKey();
                ArrayList<HistoryItem> list = entry.getValue();
                silk.println("\t<history kinTerm=\"" + kinTerm + "\">");
                for (HistoryItem hi : list) {
                    silk.println("\t" + hi.toSILKString());
                }
                silk.println("\t</history>");
            }
            silk.println("</learning-history>\n");
        }
        if (learningHistoryAdr != null && !learningHistoryAdr.isEmpty()) {
            silk.println("\n<learning-history type=\"Adr\">");
            Iterator iter = learningHistoryAdr.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, ArrayList<HistoryItem>> entry =
                        (Map.Entry<String, ArrayList<HistoryItem>>) iter.next();
                String kinTerm = entry.getKey();
                ArrayList<HistoryItem> list = entry.getValue();
                silk.println("\t<history kinTerm=\"" + kinTerm + "\">");
                for (HistoryItem hi : list) {
                    silk.println("\t" + hi.toSILKString());
                }
                silk.println("\t</history>");
            }
            silk.println("</learning-history>\n");
        }
        if (!autoDefRef.isEmpty()) {
            printAutoDefs(silk, "Ref");
        }
        if (autoDefAdr != null && !autoDefAdr.isEmpty()) {
            printAutoDefs(silk, "Adr");
        }
    }
    
    void printGEDCOMTree(PrintWriter silk, String title, 
            TreeMap<String, ParserGEDCOM.GEDCOMitem> gedcomItems) {
        silk.println("  <" + title + ">");
            Iterator itemIter = gedcomItems.values().iterator();
            while (itemIter.hasNext()) {
                ParserGEDCOM.GEDCOMitem item = (ParserGEDCOM.GEDCOMitem)itemIter.next();
                silk.println(item.toSILKString());
            }
            silk.println("  </" + title + ">");
    }

    void printAutoDefs(PrintWriter pw, String typ) {
        TreeMap<String, ArrayList<CB_Ptr>> autos =
                (typ.equals("Ref") ? autoDefRef : autoDefAdr);
        pw.println("<auto-def type=\"" + typ + "\">");
        Iterator typeIter = autos.entrySet().iterator();
        while (typeIter.hasNext()) {
            Map.Entry<String, ArrayList<CB_Ptr>> entry =
                    (Map.Entry<String, ArrayList<CB_Ptr>>) typeIter.next();
            String kinType = entry.getKey();
            ArrayList<CB_Ptr> lst = entry.getValue();
            pw.println("\t<kinType type=\"" + kinType + "\">");
            for (CB_Ptr ptr : lst) {
                pw.println(ptr.toSILKString("\t\t"));
            }
            pw.println("\t</kinType>");
        }
        pw.println("</auto-def>\n");
    }

    public void printSuggestions(PrintWriter pw, DomainTheory dt, String typ) {
        TreeMap<String, ArrayList<Issue>> suggs = dt.issuesForUser;
        pw.println("<silkin-issues type=\"" + typ + "\">");
        Iterator suggIter = suggs.values().iterator();
        while (suggIter.hasNext()) {
            ArrayList<Issue> issues = (ArrayList<Issue>) suggIter.next();
            for (Issue issue : issues) {
                issue.toSILKFile(pw);
            }
        }
        pw.println("</silkin-issues>");
    }

    /** Designate a new Ego (interview subject).
    
    @param	ego		the new person whose data is being gathered (being interviewed). 	*/
    public void changeEgoTo(Individual ego) {
        //  First, store all data for currentEgo
        if (currentEgo != null) {
            ktm.updateRow(currentEgo, individualCensus);
        }
        //  Now prep for the new currentEgo
        currentEgo = ego;
        if (ego == null) {
            return;  //  for final storage of KTM before shutdown
        }
        TreeMap egoRow = ktm.getRow(ego);
        for (int i = 0; i < individualCensus.size(); i++) {
            Individual person = individualCensus.get(i);
            person.dyad = null;
            person.node = new Node();
            if (egoRow != null && person != ego) {
                Node nod = (Node) egoRow.get(new Integer(person.serialNmbr));
                if (nod != null) {
                    person.node = nod;
                }
            }
        }  //  end of loop thru all people
    }  //  end of method changeEgoTo

    /**
    Restore this context to a prior state, ELIMINATING all persons and families
    created with serial numbers greater-or-equal-to these.  USE WITH CAUTION.  After this purging,
    the census populations will contain 'nmbrIndivs' individuals and 'nmbrFams' families.
    
    @param	nmbrIndivs	lowest individual serial number to be removed.
    @param	nmbrFams	lowest family serial number to be removed.
    
    @throws			KSInternalErrorException if the population counts do not add up
    to the proper figures after purging.
     */
    public void resetTo(int nmbrIndivs, int nmbrFams) throws KSInternalErrorException {
        if (nmbrIndivs == 0 && nmbrFams == 0) {  //  Erase all and start over.
            individualCensus = new ArrayList<Individual>();
            familyCensus = new ArrayList<Family>();
            linkCensus = new ArrayList<Link>();
            currentEgo = null;
            indSerNumGen = 0;
            famSerNumGen = 0;
            return;
        }
        Individual currInd;
        Family fam, currFam;
        for (int i = individualCensus.size() - 1; i >= nmbrIndivs; i--) {
            // work from end of list back to reset point.
            currInd = individualCensus.get(i);
            indSerNumGen--;
            for (int j = 0; j < currInd.marriages.size(); j++) {
                fam = (Family) currInd.marriages.get(j);
                if (fam.husband == currInd) {
                    fam.husband = null;
                } else if (fam.wife == currInd) {
                    fam.wife = null;
                } else {
                    //  Msg for developer. Not translated.
                    String msg = "Indiv lists marriage but is neither H nor W.\n";
                    msg += currInd + "\n";
                    msg += fam;
                    throw new KSInternalErrorException(msg);
                }  //  end of error
            }  //  end of for-each-marriage-she-is-in
            if (currInd.birthFamily != null) {
                fam = currInd.birthFamily;
                int k = fam.children.lastIndexOf(currInd);
                if ((k < 0) || (fam.nmbrOfKids <= 0)) {
                    //  Msg for developer. Not translated.
                    String msg2 = "Indiv lists birthFamily but is not a child in it.";
                    msg2 += currInd + "\n";
                    msg2 += fam;
                    throw new KSInternalErrorException(msg2);
                }  //  end of error
                fam.children.remove(k);
                fam.nmbrOfKids--;
            }  //  end of remove-from-birthFamily
            individualCensus.remove(i);
        }  //  end of Individual purge
        //  It is possible that families created after the reset point could have one or both
        //  parents from prior to the reset point, and even kids created pre-reset.  Ergo, we can't
        //  do any error-checking before purging this family.
        for (int i = familyCensus.size() - 1; i >= nmbrFams; i--) {
            currFam = familyCensus.get(i);
            famSerNumGen--;
            if (currFam.husband != null) {
                int k = currFam.husband.marriages.lastIndexOf(currFam);
                currFam.husband.marriages.remove(k);
            }  //  end of husband processing
            if (currFam.wife != null) {
                int k = currFam.wife.marriages.lastIndexOf(currFam);
                currFam.wife.marriages.remove(k);
            }  //  end of husband processing
            for (int j = 0; j < currFam.nmbrOfKids; j++) {
                currInd = (Individual) currFam.children.get(j);
                currInd.birthFamily = null;
            }  //  end of kid processing
            familyCensus.remove(i);
        }  //  end of Family purge
        try {
            if (domTheoryRefExists()) {
                //  Sometimes the Context Stub says the Ref exists because it is under construction
                domTheoryRef().dyadsDefined.purgeDyads(nmbrIndivs);
                domTheoryRef().dyadsUndefined.purgeDyads(nmbrIndivs);
            }
        }catch(Exception exc) {}  // if FileNotFound, don't worry about it.
        try {
            if (domTheoryAdrExists()) {
                domTheoryAdr().dyadsDefined.purgeDyads(nmbrIndivs);
                domTheoryAdr().dyadsUndefined.purgeDyads(nmbrIndivs);
            }
        }catch(Exception exc) { }  // if FileNotFound, don't worry about it.

        if ((familyCensus.size() != nmbrFams) || (individualCensus.size() != nmbrIndivs)) {
            //  Msg for developer. Not translated.
            throw new KSInternalErrorException("Population counts after reset-purge don't balance.");
        }
        return;
    }  //  end of method resetTo

    /** Start from ego and create a {@link Dyad} for each named relationship encountered on a breadth-first
    walk of ego's family tree.  We do the walk in 2 passes:  the first pass uses ONLY biological links (i.e.
    spousal links and birth links).  The second pass adds any Star-Links (based on UDPs) which add new relatives
    of Ego.  This 2-pass method is required because kinship defined in terms of biological relationships almost
    always takes precedence over a relationship based on UDPs (e.g. clan membership).
    <p>
    This method is only called on individuals created in the Data-Gathering
    Module and graphed on-screen.  Therefore, we assume that each person has a non-null {@link Node} attached, 
    which contains all the relationship information, levels, etc.  Generate a complete PC_String for each dyad,
    containing a fully-reduced (i.e. has Stfa or Hbro where appropriate) exact string.
    <p>
    When posting dyads (Undefined) created by Example Generation, not all nodes on Ego's relatives document kinship to her;
    some document the kinship of some alternate ego.  Test for that on the node.
    
    @param  ego     the focus person, to whom all others are related in this family tree.  		*/
    public void addDyads(Individual ego) throws KSInternalErrorException {
        TreeMap bioDyads = new TreeMap();
        if (addDyads2(ego, bioDyads, true)) //  1st pass fills in bioDyads; returns true if starLinks exist anywhere
        {
            addDyads2(ego, bioDyads, false); //  2nd pass, done only of 1st pass found starLinks.
        }
    }

    public boolean addDyads2(Individual ego, TreeMap bioDyads, boolean onlyBioLinks) throws KSInternalErrorException {
        //  Clean off the "dyad" field of each person in the population (used to keep track of who's processed)
        //  Maybe later, only clean off the dyad field of persons who've had kinTerms added since last call to this fn.
        for (int i = 0; i < indSerNumGen; i++) {
            (individualCensus.get(i)).dyad = null;
        }
        //  initialize ego and place her in the ready_Queue
        if (ego == null) {
            return false;
        }
        boolean starLinksFound = false;
        int egoLevel = ego.node.getLevel();
        Family fam;
        String pcs;
        KSQ ready = new KSQ();
        ego.dyad = new Dyad(ego);
        ready.enQ(ego);
        //  Now process the queue until empty (a sort-of-breadth-first walk)
        while (!ready.isEmpty()) {
            Individual self = (Individual) ready.deQ();
            if (self.birthFamily != null) {  //  birthFam processing
                Individual dad = self.birthFamily.husband;
                makeNextDyad(self, dad, "Fa", ready);
                Individual mom = self.birthFamily.wife;
                makeNextDyad(self, mom, "Mo", ready);
                //  end of full parent processing
                for (int i = 0; i < self.birthFamily.children.size(); i++) {
                    Individual sib = (Individual) self.birthFamily.children.get(i);
                    if (sib != self) {
                        pcs = (sib.gender.equals("M") ? "Bro" : "Sis");
                        makeNextDyad(self, sib, pcs, ready);
                    }
                }   //  end of full sibling processing
                Individual[] parents = {mom, dad};
                for (int i = 0; i < 2; i++) {
                    Individual parent = parents[i];
                    if (parent.marriages.size() > 1) {
                        for (int j = 0; j < parent.marriages.size(); j++) {
                            Family fam2 = (Family) parent.marriages.get(j);
                            if (fam2.husband != dad || fam2.wife != mom) {
                                Individual stepParent = (fam2.husband == parent ? fam2.wife : fam2.husband);
                                pcs = (stepParent.gender.equals("M") ? "Stfa" : "Stmo");
                                makeNextDyad(self, stepParent, pcs, ready);
                                //  end of step-Parent processing
                                for (int k = 0; k < fam2.children.size(); k++) {
                                    Individual halfSib = (Individual) fam2.children.get(k);
                                    pcs = (halfSib.gender.equals("M") ? "Hbro" : "Hsis");
                                    makeNextDyad(self, halfSib, pcs, ready);
                                }  //  end of half-sibling processing
                                if (stepParent.marriages.size() > 1) {
                                    for (int m = 0; m < stepParent.marriages.size(); m++) {
                                        Family fam3 = (Family) stepParent.marriages.get(m);
                                        if (fam3.husband != parent || fam3.wife != parent) {
                                            for (int n = 0; n < fam3.children.size(); n++) {
                                                Individual stepSib = (Individual) fam3.children.get(n);
                                                pcs = (stepSib.gender.equals("M") ? "Stbro" : "Stsis");
                                                makeNextDyad(self, stepSib, pcs, ready);
                                            }  //  end of step-Sibling processing
                                        }
                                    }  //  end of loop thru step parent's other marriages
                                }
                            }  //  end of this-is-not-self's-birthFam
                        }  //  end of loop thru parent's other marriages
                    }
                }  //  end of loop thru mom and dad
            }  //  end of birthFam processing
            for (int i = 0; i < self.marriages.size(); i++) {  //  processing for self's marriages
                fam = (Family) self.marriages.get(i);
                Individual spouse = (fam.husband == self ? fam.wife : fam.husband);
                pcs = (spouse.gender.equals("M") ? "Hu" : "Wi");
                makeNextDyad(self, spouse, pcs, ready);
                //  end of (ex-)spouse processing
                for (int j = 0; j < fam.children.size(); j++) {
                    Individual kid = (Individual) fam.children.get(j);
                    pcs = (kid.gender.equals("M") ? "So" : "Da");
                    makeNextDyad(self, kid, pcs, ready);
                }  //  end of loop thru self's kids
                if (spouse.marriages.size() > 1) {
                    for (int j = 0; j < spouse.marriages.size(); j++) {
                        Family fam4 = (Family) spouse.marriages.get(j);
                        if (fam4.husband != self && fam4.wife != self) {
                            for (int k = 0; k < fam4.children.size(); k++) {
                                Individual stepKid = (Individual) fam4.children.get(k);
                                pcs = (stepKid.gender.equals("M") ? "Stso" : "Stda");
                                makeNextDyad(self, stepKid, pcs, ready);
                            }  //  end of step-child processing
                        }
                    }  //  end of loop thru mate's other marriages
                }
            }  //  end of processing for self's marriages
            //  Now handle Star-links -- if any.
            if (self.starLinks != null) {
                if (onlyBioLinks) {
                    starLinksFound = true;
                } else {
                    for (int i = 0; i < self.starLinks.size(); i++) {  //  A star-link is 2 star-preds that form a link between individuals, so PC_String is '**'
                        makeNextDyad(self, (Individual) self.starLinks.get(i), "**", ready);
                    }
                }
            }
            //  Finalize the dyads (if any are needed) for self and send them to the Dyad TreeMap
            if ((self.dyad.path.size() > 0) && (self.node.hasKinTerms()) // only ego has an empty path; no dyad needed for her
                    && (onlyBioLinks || self.dyad.pcString.indexOf("*") > -1)) { //  if not bios-only, then must have * in path
                self.dyad.alter = self;
                generateDyads(self, egoLevel, onlyBioLinks, bioDyads);
            }
        }  //  end of while-loop thru ready_Queue
        return starLinksFound;
    }  //  end of method addDyads2()

    boolean nonDupStarDyad(Dyad dy, TreeMap bioDyads) {
        if (dy.pcString.indexOf("*") == -1) {
            return false;
        }
        Integer altSerial = new Integer(dy.alter.serialNmbr);
        if (bioDyads.get(altSerial) == null
                || !((ArrayList<Object>) bioDyads.get(altSerial)).contains(dy.kinTerm)) {
            return true;
        }
        return false;
    }  //  end of method  nonDupStarDyad

    void makeNextDyad(Individual curent, Individual next, String pcs, KSQ queue) {
        //  5/12/06  Add more precise logic re: pcCounter and levels
        //  next's level should be relative to curent's.  Here we encode only the pcStrings for
        //  people we can reach directly from curent's Individual (in addDyads() above).
        if (next.dyad == null) { //  person hasn't been processed yet
            next.dyad = new Dyad(curent.dyad);
            next.dyad.path.add(curent);
            next.dyad.pcString += pcs;
            if (pcs.equals("Fa") || pcs.equals("Mo")) {
                next.dyad.level++;
            } else if (pcs.equals("So") || pcs.equals("Da")) {
                next.dyad.level--;
            } else if (pcs.equals("Stso") || pcs.equals("Stda")) {
                next.dyad.level--;
            } else if (pcs.equals("Stfa") || pcs.equals("Stmo")) {
                next.dyad.level++;
            }
            queue.enQ(next);
        }  //  end of process
    }  //  end of method makeNextDyad

    /**   Generate one Dyad for each kinTerm found in any of the six lists on this Individual's Node.
    
    @param	current		 the Individual whose kinTerms we are recording in dyadsUndefined.  
    @param  egoLevel	 the treeLevel assigned to ego in this set of Nodes.  We normalize the treeLevels for use
    in indexing, with ego's level = 0.
    @param onlyBioLinks  is true when we are in 1st pass, and must post to bioDyads.
    @param bioDyads		 has structure Alter.serialNmbr => ArrayList<Object> of kinTerms.   */
    public void generateDyads(Individual current, int egoLevel, boolean onlyBioLinks, TreeMap bioDyads)
            throws KSInternalErrorException, NumberFormatException {
        if (current.node.ktSuffix.length() > 0) {
            int egoForCurrent = ktm.pullSerial(current.node.ktSuffix);
            if (egoForCurrent != currentEgo.serialNmbr) {
                return;  //  if substitute ego, skip it.
            }
        }
        ArrayList<Object> terms = current.node.kinTermsRef();
        DomainTheory dt = domTheoryRef;
        String auth = (domTheoryAdr == null ? "" : domTheoryAdr.author);
        Dyad newDyad;
        ClauseBody cb = new ClauseBody();
        String structString = ClauseBody.structStr(current.dyad.pcString);
        for (int i = 0; i < terms.size(); i++) {
            newDyad = new Dyad(current.dyad);
            newDyad.path.remove(0);  //  remove ego from head of the list
            newDyad.kinTerm = (String) terms.get(i);
            newDyad.kinTermType = Dyad.PRIMARY;
            newDyad.addrOrRef = Dyad.REF;
            newDyad.level = current.dyad.level - egoLevel;
            newDyad.pcStringStructural = structString;
            if (dt == null) {
                domTheoryRef = new DomainTheory(this, false, auth);
            }
            if (onlyBioLinks) {
                postBioDyads(newDyad, bioDyads);
                dt.dyadsUndefined.dyAdd(newDyad);
            } else if (nonDupStarDyad(newDyad, bioDyads)) {
                dt.dyadsUndefined.dyAdd(newDyad);
            }
        }  //  end of loop thru kinTermsRef 
        terms = current.node.extKinTermsRef();
        for (int i = 0; i < terms.size(); i++) {
            newDyad = new Dyad(current.dyad);
            newDyad.path.remove(0);  //  remove ego from head of the list
            newDyad.kinTerm = (String) terms.get(i);
            newDyad.kinTermType = Dyad.EXTENDED;
            newDyad.addrOrRef = Dyad.REF;
            newDyad.level = current.dyad.level - egoLevel;
            newDyad.alter = current;
            newDyad.pcStringStructural = structString;
            if (dt == null) {
                domTheoryRef = new DomainTheory(this, false, auth);
            }
            if (onlyBioLinks) {
                postBioDyads(newDyad, bioDyads);
                dt.dyadsUndefined.dyAdd(newDyad);
            } else if (nonDupStarDyad(newDyad, bioDyads)) {
                dt.dyadsUndefined.dyAdd(newDyad);
            }
        }  //  end of loop thru extKinTermsRef 
        terms = current.node.kinTermsAddr();
        dt = domTheoryAdr;
        auth = (domTheoryRef == null ? "" : domTheoryRef.author);
        for (int i = 0; i < terms.size(); i++) {
            newDyad = new Dyad(current.dyad);
            newDyad.path.remove(0);  //  remove ego from head of the list
            newDyad.kinTerm = (String) terms.get(i);
            newDyad.kinTermType = Dyad.PRIMARY;
            newDyad.addrOrRef = Dyad.ADDR;
            newDyad.level = current.dyad.level - egoLevel;
            newDyad.alter = current;
            newDyad.pcStringStructural = structString;
            if (dt == null) {
                domTheoryAdr = new DomainTheory(this, true, auth);
            }
            if (onlyBioLinks) {
                postBioDyads(newDyad, bioDyads);
                dt.dyadsUndefined.dyAdd(newDyad);
            } else if (nonDupStarDyad(newDyad, bioDyads)) {
                dt.dyadsUndefined.dyAdd(newDyad);
            }
        }  //  end of loop thru kinTermsAddr 
        terms = current.node.extKinTermsAddr();
        for (int i = 0; i < terms.size(); i++) {
            newDyad = new Dyad(current.dyad);
            newDyad.path.remove(0);  //  remove ego from head of the list
            newDyad.kinTerm = (String) terms.get(i);
            newDyad.kinTermType = Dyad.EXTENDED;
            newDyad.addrOrRef = Dyad.ADDR;
            newDyad.level = current.dyad.level - egoLevel;
            newDyad.alter = current;
            newDyad.pcStringStructural = structString;
            if (dt == null) {
                domTheoryAdr = new DomainTheory(this, true, auth);
            }
            if (onlyBioLinks) {
                postBioDyads(newDyad, bioDyads);
                dt.dyadsUndefined.dyAdd(newDyad);
            } else if (nonDupStarDyad(newDyad, bioDyads)) {
                dt.dyadsUndefined.dyAdd(newDyad);
            }
        }  //  end of loop thru extKinTermsAddr 
    }  //  end of method generateDyads

    void postBioDyads(Dyad newDyad, TreeMap bioDyads) {
        //   bioDyads		 has structure Alter.serialNmbr => ArrayList<Object> of kinTerms.
        Integer altSerial = new Integer(newDyad.alter.serialNmbr);
        if (bioDyads.get(altSerial) == null) {
            bioDyads.put(altSerial, new ArrayList<Object>());
        }
        ArrayList<Object> altKinTermList = (ArrayList<Object>) bioDyads.get(altSerial);
        if (!altKinTermList.contains(newDyad.kinTerm)) {
            altKinTermList.add(newDyad.kinTerm);
        }
    }  //  end of method postBioDyads

    public ContextEditor editFields() throws KSInternalErrorException {
        //  Open an editor window within frame.  Allow editing of certain fields of this Context.
        return new ContextEditor(this);
        //   .... and then???
    }  //  end of method editFields

    public boolean notRejected(Library.KTD_EQC eqc, boolean adr) {
        if (adr && learningHistoryAdr == null) {
            learningHistoryAdr = new TreeMap<String, ArrayList<HistoryItem>>();
        }
        TreeMap<String, ArrayList<HistoryItem>> history =
                (adr ? learningHistoryAdr : learningHistoryRef);
        ArrayList<HistoryItem> list = history.get(eqc.prototype.kinTerm);
        if (list == null) {
            return true;
        }
        for (HistoryItem hi : list) {
            if (hi instanceof RejectedPropDefPtr) {
                RejectedPropDefPtr reject = (RejectedPropDefPtr) hi;
                Library.KTD_Ptr ptr = eqc.prototype;
                if (ptr.languageName.equals(reject.eqcProtoLangName)
                        && ptr.kinTerm.equals(reject.eqcProtoKinTerm)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * This convenience class has 3 named fields: parent, child, and udpName.
     * It makes the code more readable.
     */
    public static class SpecRelTriple implements Serializable {
        public Locatable parent;
        public Locatable child;
        public String udpName;
        
        /** My custom equality test.
         * 
         * @param other the other SpecRelTriple
         * @return true only if contents of all 3 fields are equal.
         */
        public boolean equals(SpecRelTriple other) {
            if (udpName.equals(other.udpName) && 
                parent == other.parent && child == other.child) {
                return true;
            }
            return false;
        }
    }

    public static class CB_Ptr implements Serializable {

        String kinTerm;
        int clauseNmbr;

        CB_Ptr(String kt, int cn) {
            kinTerm = kt;
            clauseNmbr = cn;
        }

        public String toSILKString(String bacer) {
            return bacer + "<def kinTerm=\"" + kinTerm
                    + "\" clause-number=\"" + clauseNmbr + "\"/>";
        }

        public String toString() {
            return toSILKString("");
        }
    }
    
    public static String hornClauseFormatter(String in) {
        String out = "", linePad,
               padding = "                                        ";
        int max = 72, start, end, cut, padPt = 0;
        ArrayList<String> lines = makeLines(in);
        for (String line : lines) {
            start = 0;
            end = max;
            padPt = line.indexOf(":-") + 3;  //  look for implication symbol
            if (padPt == 2) {
                padPt = line.indexOf("|") + 2;  //  look for "OR"
                if (padPt == 1) {
                    padPt = 0;
                }
            }
            if (padPt > 40) padPt = 40;
            linePad = padding.substring(0, padPt);
            boolean done = false,
                    first = true;
            while (!done) {
                if (line.substring(start).length() < max + (first ? 0 : padPt)) {
                    if (! first) {
                        out += linePad;
                    }
                    out += line.substring(start) + "\n";
                    done = true;
                } else {
                    cut = line.substring(start, end).lastIndexOf("),") + 3;
                    if (cut < 0) {
                        cut = max;
                    }
                    if (first) {
                        first = false;
                    } else {
                        out += linePad;
                    }
                    out += line.substring(start, start + cut) + "\n";
                    start += cut;
                    end = start + max;
                }
            }
        }
        return out;
    }
    
    public static ArrayList<String> makeLines(String in) {
        ArrayList<String> out = new ArrayList<String>();
        int start = 0, end;
        boolean done = false;
        while (!done) {
            end = in.indexOf("\n", start);
            if (end == -1) {
                out.add(in.substring(start));
                done = true;
            }else {
                out.add(in.substring(start, end));
                start = end +1;
                if (start >= in.length()) {
                    done = true;
                }
            }
        }
        return out;
    }

    abstract static class HistoryItem implements Serializable {

        String kinTerm, date, notes;
        boolean rescinded = false;

        void postToHistory(TreeMap<String, ArrayList<HistoryItem>> history) {
            if (history.get(kinTerm) == null) {
                history.put(kinTerm, new ArrayList<HistoryItem>());
            }
            ArrayList<HistoryItem> ktList = (ArrayList<HistoryItem>) history.get(kinTerm);
            ktList.add(this);
        }

        public String toString() {
            return toSILKString();
        }

        abstract String toSILKString();
        
        abstract void unDo(DomainTheory dt);
        
        abstract String unDoDescription();
    }

    /**This inner class represents a pointer to a KinTermDef
     * that was accepted by the User and added to the domain theory.
     * 
     */
    public static class AcceptedDefPtr extends HistoryItem implements Serializable {

        ArrayList<Integer[]> autoDefPairs = new ArrayList<Integer[]>();

        AcceptedDefPtr(String kt, String dat, String resc, String nt) {
            kinTerm = kt;
            date = dat;
            rescinded = Boolean.parseBoolean(resc);
            notes = nt;
        }

        public String toSILKString() {
            String img = "\t<accepted-def kinTerm=\"";
            img += kinTerm + "\" date=\"" + date;
            img += "\" rescinded=\"" + rescinded;            
            img += "\" notes=\"" + notes + "\">\n";
            if (!autoDefPairs.isEmpty()) {
                img += "\t\t\t<auto-defs>\n";
                for (Integer[] pair : autoDefPairs) {
                    img += "\t\t\t\t<pair ego=\"" + pair[0] + "\" alter=\"" + pair[1] + "\"/>\n";
                }
                img += "\t\t\t</auto-defs>\n";
            }
            img += "\t\t</accepted-def>";
            return img;
        }

        public String description(DomainTheory dt) {
            MyResBundle se = Library.screenElements;
            String img = se.getString("aCCEPTEDdef") + kinTerm + ":";
            KinTermDef ktd = (KinTermDef) dt.theory.get(kinTerm);
            img += "\n" + ktd.toString();
            return hornClauseFormatter(img);
        }  // 
        
        public void unDo(DomainTheory dt) {
            KinTermDef ktd = (KinTermDef) dt.theory.remove(kinTerm);
            rescinded = true;
            SIL_Edit win = SIL_Edit.edWin;
            for (Integer[] pair : autoDefPairs) {
                win.removeDef(dt, pair[0], pair[1], kinTerm);
            }
            TreeMap<String, ArrayList<CB_Ptr>> autoDef =
                    (dt.addressTerms ? dt.ctxt.autoDefAdr : dt.ctxt.autoDefRef);
            for (Object o : ktd.expandedDefs) {
                ClauseBody cb = (ClauseBody) o;
                String kinType = cb.pcString;
                ArrayList<CB_Ptr> ptrList = autoDef.get(kinType);
                Iterator ptrIter = ptrList.iterator();
                while (ptrIter.hasNext()) {
                    CB_Ptr ptr = (CB_Ptr) ptrIter.next();
                    if (ptr.kinTerm.equals(kinTerm)) {
                        ptrIter.remove();
                    }
                }
                if (ptrList.isEmpty()) {
                    autoDef.remove(kinType);
                }
            }
            win.showInfo(win.infoPerson);
            win.chart.repaint();
        }
        
        
        public String unDoDescription() {
            String s = Library.messages.getString("withdrewAcceptedDef1") + " " + kinTerm;
            s += Library.messages.getString("withdrewAcceptedDef2");
            if (autoDefPairs.size() > 0) {
                s += Library.messages.getString("withdrewAcceptedDef3");
                String lead = "";
                for (Integer[] pair : autoDefPairs) {
                    s += lead + pair[0] + "/" + pair[1];
                    lead = ", ";
                }
            }
            return s;            
        }
        
    } // end of inner class AcceptedDefPtr

    /**This inner class represents a pointer to a KinTermDef
     * (or EQC of them) that was proposed to User but rejected.
     *
     */
    public static class RejectedPropDefPtr extends HistoryItem implements Serializable {

        String sigString, eqcProtoLangName, eqcProtoKinTerm;

        RejectedPropDefPtr(String kt, String dat, String resc, String not, String sig,
                String protoLang, String protoKT) {
            kinTerm = kt;
            date = dat;
            rescinded = Boolean.parseBoolean(resc);
            notes = not;
            sigString = sig;
            eqcProtoLangName = protoLang;
            eqcProtoKinTerm = protoKT;
        }

        public String toSILKString() {
            String img = "\t<rejected-def kinTerm=\"";
            img += kinTerm + "\" date=\"" + date;
            img += "\" rescinded=\"" + rescinded;
            img += "\" notes=\"" + notes + "\" sigString=\"" + sigString;
            img += "\" protoLang=\"" + eqcProtoLangName;
            img += "\" protoKT=\"" + eqcProtoKinTerm + "\"/>";
            return img;
        }

        public String description(KinTermDef ktd) {
            String img = Library.messages.getString("rejectedDef") + " " + kinTerm + ":";
            img += "\n" + ktd.toString().replace(eqcProtoKinTerm, kinTerm);
            return hornClauseFormatter(img);
        }
        
        public void unDo(DomainTheory dt) {            
            rescinded = true;
        }
        
        public String unDoDescription() {
            String s = Library.messages.getString("rescindedRejection1") + " " + kinTerm + ".";
            s += Library.messages.getString("rescindedRejection2");            
            return s;            
        }
        
    } // end of inner class RejectedPropDefPtr

    /**This inner class documents User's rejection of a proposed Synonym pair
     * 
     */
    public static class RejectedSynonym extends HistoryItem implements Serializable {

        String synTerm;

        RejectedSynonym(String kt, String dat, String resc, String comments, String st) {
            kinTerm = kt;
            date = dat;
            rescinded = Boolean.parseBoolean(resc);
            notes = comments;
            synTerm = st;
        }

        public String toSILKString() {
            String img = "\t<rejected-synonym primary=\"";
            img += kinTerm + "\" date=\"" + date;
            img += "\" rescinded=\"" + rescinded;
            img += "\" notes=\"" + notes + "\" synonym=\"" + synTerm + "\"/>";
            return img;
        }

        public String description() {
            return Library.messages.getString("rejected") + " " + synTerm 
                    + " " + Library.messages.getString("asSyn4") + " "
                    + kinTerm + ".";
        }
        
        public void unDo(DomainTheory dt) {            
            rescinded = true;
            ArrayList<Object> rejects = dt.nonSynonyms;
            String pair, term1, term2;
            if (kinTerm.compareTo(synTerm) < 0) {
                term1 = kinTerm;
                term2 = synTerm;
            }else {
                term2 = kinTerm;
                term1 = synTerm;
            }
            pair = term1 + "/" + term2;
            rejects.remove(pair);            
        }
        
        public String unDoDescription() {
            String s =  Library.messages.getString("unDoSyn1") + " " + synTerm + 
                    " " + Library.messages.getString("unDoSyn2") + " " + kinTerm + ".";
            s += Library.messages.getString("free2Accept");             
            return s;            
        }
        
    }  //  end of Inner class RejectedSynonym

    /**This inner class documents User's acceptance of a proposed Synonym pair
     * 
     */
    public static class AcceptedSynonym extends HistoryItem implements Serializable {

        String synTerm;

        AcceptedSynonym(String kt, String dat, String resc, String comments, String st) {
            kinTerm = kt;
            date = dat;
            rescinded = Boolean.parseBoolean(resc);
            notes = comments;
            synTerm = st;
        }

        public String toSILKString() {
            String img = "\t<accepted-synonym primary=\"";
            img += kinTerm + "\" date=\"" + date;
            img += "\" rescinded=\"" + rescinded;
            img += "\" notes=\"" + notes + "\" synonym=\"" + synTerm + "\"/>";
            return img;
        }

        public String description() {
            return Library.messages.getString("accepted") + " " + synTerm 
                    + " " + Library.messages.getString("asSynonym") + " "
                    + kinTerm + ".";
        }
        
        public void unDo(DomainTheory dt) {        
            rescinded = true;
            String oldPrimary = (String)dt.synonyms.get(synTerm);
            if (oldPrimary.equals(kinTerm)) {
                dt.synonyms.remove(synTerm);
            }
        }
        
        public String unDoDescription() {
            String s = Library.messages.getString("unDoAcceptSyn1") + " " + synTerm + 
                    " " + Library.messages.getString("unDoAcceptSyn2") + " " + kinTerm + ".";
            s += Library.messages.getString("free2Reject");  
            return s;            
        }
        
    }  //  end of Inner class AcceptedSynonym

    /**This inner class documents User's rejection of a proposed Umbrella
     * 
     */
    public static class RejectedUmbrella extends HistoryItem implements Serializable {

        ArrayList<String> subTerms;
        MyResBundle msgs = Library.messages;

        RejectedUmbrella(String kt, String dat, String resc, String comments, ArrayList<String> st) {
            kinTerm = kt;
            date = dat;
            rescinded = Boolean.parseBoolean(resc);
            notes = comments;
            subTerms = st;
        }

        public String toSILKString() {
            String img = "\t<rejected-umbrella umbTerm=\"";
            img += kinTerm + "\" date=\"" + date;
            img += "\" rescinded=\"" + rescinded;
            img += "\" notes=\"" + notes + "\">";
            img += "<\t\t<original-sub-terms>\n";
            for (String tm : subTerms) {
                img += "\t\t\t<sub-term value=\"" + tm + "\"/>";
            }
            img += "<\t\t</original-sub-terms>\n";
            img += "\t<rejected-umbrella>\n";
            return img;
        }

        public String description() {
            String img = msgs.getString("rejected") + " " + kinTerm 
                    + " " + msgs.getString("asUmbTerm");
            String leader = "";
            for (String s : subTerms) {
                img += leader + s;
                leader = ", ";
            }
            return img;
        }
        
        public void unDo(DomainTheory dt) {       
            rescinded = true;
            ArrayList<Object> lst = (ArrayList<Object>)dt.nonUmbrellas.get(kinTerm);
            for (String st : subTerms) {
                lst.remove(st);
            }
            if (lst.isEmpty()) {
                dt.nonUmbrellas.remove(kinTerm);
            }
        }
        
        public String unDoDescription() {
            String img =msgs.getString("unDoSyn1") + " " + kinTerm + 
                    " " + msgs.getString("asUmbTerm");
            String leader = "";
            for (String s : subTerms) {
                img += leader + s;
                leader = ", ";
            }
            img += msgs.getString("free2Accept");             
            return img;            
        }
        
    }  //  end of Inner class RejectedUmbrella

    /**This inner class documents User's acceptance of a proposed Umbrella
     * 
     */
    public static class AcceptedUmbrella extends HistoryItem implements Serializable {
        //  Original subterms were as proposed by the UmbrellaCandidate. 
        //  Edited subterms are as approved by User.
        //  Added subterms are memebers of edited subterms that were new additions to the
        //  umbrella as stored on the DomainTheory.

        ArrayList<String> origSubTerms, editedSubTerms, addedSubTerms;
        MyResBundle msgs = Library.messages;

        AcceptedUmbrella(String kt, String dat, String resc, String comments,
                ArrayList<String> ost, ArrayList<String> est) {
            kinTerm = kt;
            date = dat;
            rescinded = Boolean.parseBoolean(resc);
            notes = comments;
            origSubTerms = ost;
            editedSubTerms = est;
        }

        public String toSILKString() {
            String img = "\t<accepted-umbrella umbTerm=\"";
            img += kinTerm + "\" date=\"" + date;
            img += "\" rescinded=\"" + rescinded;
            img += "\" notes=\"" + notes + "\">\n";
            img += "\t\t<original-sub-terms>\n";
            for (String tm : origSubTerms) {
                img += "\t\t\t<sub-term value=\"" + tm + "\"/>\n";
            }
            img += "\t\t</original-sub-terms>\n";
            img += "\t\t<edited-sub-terms>\n";
            for (String tm : editedSubTerms) {
                img += "\t\t\t<sub-term value=\"" + tm + "\"/>\n";
            }
            img += "\t\t</edited-sub-terms>\n";
            if (addedSubTerms != null & !addedSubTerms.isEmpty()) {
                img += "\t\t<added-sub-terms>\n";
                for (String tm : addedSubTerms) {
                    img += "\t\t\t<sub-term value=\"" + tm + "\"/>\n";
                }
                img += "\t\t</added-sub-terms>\n";
            }
            img += "\t</accepted-umbrella>\n";
            return img;
        }

        public String description() {
            String img = msgs.getString("accepted") + " " + kinTerm + " " 
                    + msgs.getString("asUmbTerm");
            String leader = "";
            for (String s : editedSubTerms) {
                img += leader + s;
                leader = ", ";
            }
            if (addedSubTerms != null & !addedSubTerms.isEmpty()) {
                img += msgs.getString("newAdds2Umb");
                leader = "";
                for (String s : addedSubTerms) {
                    img += leader + s;
                    leader = ", ";
                }
            }else {
                img += msgs.getString("allKnown");
            }
            return img;
        }
        
        public void unDo(DomainTheory dt) {
            rescinded = true;
            ArrayList<Object> lst = (ArrayList<Object>)dt.umbrellas.get(kinTerm);
            for (String st : editedSubTerms) {
                lst.remove(st);
            }
            if (lst.isEmpty()) {
                dt.umbrellas.remove(kinTerm);
            }
        }
        
        public String unDoDescription() {
            String img = msgs.getString("unDoAcceptSyn1") + " " + kinTerm + 
                    " " + msgs.getString("asUmbTerm");
            String leader = "";
            for (String s : editedSubTerms) {
                img += leader + s;
                leader = ", ";
            }
            img += msgs.getString("free2Reject");            
            return img;             
        }
        
    }  //  end of Inner class AcceptedUmbrella

    /**This inner class documents User's conversion of a proposed Umbrella
     * into one or more Synonyms
     * 
     */
    public static class UmbrellaIntoSyns extends HistoryItem implements Serializable {

        ArrayList<String> subTerms, synTerms;
        String keyTerm;
        MyResBundle msgs = Library.messages;

        UmbrellaIntoSyns(String kt, String dat, String resc, String comments, ArrayList<String> subs,
                ArrayList<String> syns, String newKey) {
            kinTerm = kt;
            date = dat;
            rescinded = Boolean.parseBoolean(resc);
            notes = comments;
            subTerms = subs;
            synTerms = syns;
            keyTerm = newKey;
        }

        public String toSILKString() {
            String img = "\t<umbrella-into-synonyms umbTerm=\"";
            img += kinTerm + "\" date=\"" + date;
            img += "\" rescinded=\"" + rescinded;
            img += "\" newKey=\"" + keyTerm;
            img += "\" notes=\"" + notes + "\">\n";
            if (subTerms != null && !subTerms.isEmpty()) {
                img += "\t\t<sub-terms>\n";
                for (String tm : subTerms) {
                    img += "\t\t\t<sub-term value=\"" + tm + "\"/>";
                }
                img += "\t\t</sub-terms>\n";
            }
            if (synTerms != null && !synTerms.isEmpty()) {
                img += "\t\t<syn-terms>\n";
                for (String tm : synTerms) {
                    img += "\t\t\t<syn-term value=\"" + tm + "\"/>";
                }
                img += "\t\t</syn-terms>\n";
            }
            img += "\t\t</umbrella-into-synonyms>";
            return img;
        }
        
        public String description() {
            String plural = (synTerms.size() > 1 ? 
                    msgs.getString("umbPlural2") : 
                    msgs.getString("umbPlural1") + " ");
            String img = msgs.getString("umb2Syn1") + kinTerm + msgs.getString("umb2Syn2");
            img += msgs.getString("primaryTermIs") + " " + keyTerm + ". " + plural;
            String leader = "";
            for (String s : synTerms) {
                img += leader + s;
                leader = ", ";
            }
           return img;
        }
        
        public void unDo(DomainTheory dt) {
            rescinded = true;
            // It is possible that a particular term might be proposed as a synonym
            // more than once. Since any synonym can have only one primary term,
            // if we accepted TWO ProposedSynonyms, only the 2nd one would still be
            // recorded (it replaces the 1st). Therefore, we should check that the
            // currently-recorded synonym is the one we want to rescind. 
            for (String syn : synTerms) {
                String oldPrimary = (String)dt.synonyms.get(syn);
                if (oldPrimary.equals(keyTerm)) {
                    dt.synonyms.remove(syn);
                }                
            }
        }
        
        public String unDoDescription() {
            String plural = (synTerms.size() > 1 ? 
                    msgs.getString("umbPlural2") : 
                    msgs.getString("umbPlural1") + " ");
            String str = msgs.getString("rescindedConversion1") 
                    + kinTerm + msgs.getString("rescindedConversion2");
            str += msgs.getString("primaryTermIs") + " " + keyTerm + ". " + plural;
            String leader = "";
            for (String s : synTerms) {
                str += leader + s;
                leader = ", ";
            }
            return str;            
        }
        
    }  //  end of Inner class UmbrellaIntoSyns

    /**This inner class documents User's acceptance of a proposed Overlap
     * 
     */
    public static class AcceptedOverlap extends HistoryItem implements Serializable {

        String otherTerm;
        MyResBundle msgs = Library.messages;

        AcceptedOverlap(String kt, String dat, String resc, String comments, String ot) {
            kinTerm = kt;
            date = dat;
            rescinded = Boolean.parseBoolean(resc);
            notes = comments;
            otherTerm = ot;
        }

        public String toSILKString() {
            String img = "\t<accepted-overlap term1=\"";
            img += kinTerm + "\" date=\"" + date;
            img += "\" rescinded=\"" + rescinded;
            img += "\" notes=\"" + notes + "\" term2=\"" + otherTerm + "\"/>";
            return img;
        }
        
        public String description() {
            return msgs.getString("acceptedOverlap") + " " + kinTerm 
                    + " " + msgs.getString("and") + " " + otherTerm + ".";
        }
        
        public void unDo(DomainTheory dt) {
            rescinded = true;
            ArrayList<Object> lst = (ArrayList<Object>)dt.overlaps.get(kinTerm);
            lst.remove(otherTerm);            
            if (lst.isEmpty()) {
                dt.overlaps.remove(kinTerm);
            }
            // Remove the inverse also
            lst = (ArrayList<Object>)dt.overlaps.get(otherTerm);
            lst.remove(kinTerm);            
            if (lst.isEmpty()) {
                dt.overlaps.remove(otherTerm);
            }
        }
        
        public String unDoDescription() {
            String img = msgs.getString("rescindedOverlap") + kinTerm + 
                    " " + msgs.getString("and") + " " + otherTerm + ".";           
            img += msgs.getString("free2Reject");             
            return img;            
        }
        
    }  //  end of Inner class AcceptedOverlap

    /**This inner class documents User's rejection of a proposed Overlap
     * 
     */
    public static class RejectedOverlap extends HistoryItem implements Serializable {

        String otherTerm;
        MyResBundle msgs = Library.messages;

        RejectedOverlap(String kt, String dat, String resc, String comments, String ot) {
            kinTerm = kt;
            date = dat;
            rescinded = Boolean.parseBoolean(resc);
            notes = comments;
            otherTerm = ot;
        }

        public String toSILKString() {
            String img = "\t<rejected-overlap term1=\"";
            img += kinTerm + "\" date=\"" + date;
            img += "\" rescinded=\"" + rescinded;
            img += "\" notes=\"" + notes + "\" term2=\"" + otherTerm + "\"/>";
            return img;
        }
        
        public String description() {
            return msgs.getString("rejectedOverlap") + " " + kinTerm + " " 
                    + msgs.getString("and") + " " + otherTerm + ".";
        }
        
        public void unDo(DomainTheory dt) {
            rescinded = true;
            ArrayList<Object> lst = (ArrayList<Object>)dt.nonOverlaps.get(kinTerm);
            lst.remove(otherTerm);            
            if (lst.isEmpty()) {
                dt.nonOverlaps.remove(kinTerm);
            }
            // Remove the inverse also
            lst = (ArrayList<Object>)dt.nonOverlaps.get(otherTerm);
            lst.remove(kinTerm);            
            if (lst.isEmpty()) {
                dt.nonOverlaps.remove(otherTerm);
            }
        }
        
        public String unDoDescription() {
            String img = msgs.getString("rescindedRejectOverlap") + kinTerm + 
                    " " + msgs.getString("and") + " " + otherTerm + ".";            
            img += msgs.getString("free2Accept");              
            return img;            
        }
        
    }  //  end of Inner class RejectedOverlap
} // end of public class Context


