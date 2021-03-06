import java.util.*;
import java.io.*;
import javax.swing.JOptionPane;

/**A KinTermMatrix has one row and one column for each person in a culture; 
 * each cell contains null or a {@link Node} containing the kinTerms 
 * <code>Row</code> (Ego) could use to refer to <code>Column</code> (Alter).
 * This sparse matrix is currently implemented as a nested TreeMap.  That may 
 * change in the future.
 * <p>
 * A KinTermMatrix grows by adding a new person (indexed by their serialNmbr) 
 * to the rows (list of Egos) and columns (list of Alters) of the matrix.  
 * Each Node holds lists of terms that Ego calls Alter, plus data about the most 
 * recent diagram that has been drawn.</p> <p>
 * The matrix is normally built one person at a time as a Field Worker gathers 
 * data about people in the local culture.  It is imperative that the retrieval 
 * of kinTerms by (row, col) address be fast and scalable.</p> <p>
 * A KinTermMatrix has 2 associated indexes of kinTerms, for Reference terms and 
 * terms of Address.  For every kinTerm that has been recorded in a matrix, its 
 * index entry has a list of all the Egos who have a kin of that type, and for 
 * each one has a list of Alters whom Ego calls by that kinTerm.
 * </p>
 *  @author		Gary Morris, Northern Virginia Community College
 *                              garymorris2245@verizon.net
 */
public class KinTermMatrix implements Serializable {

    TreeMap matrix = new TreeMap();

    
    public KinTermMatrix clone() {
        KinTermMatrix clon = new KinTermMatrix();
        Iterator egoIter = matrix.keySet().iterator();
        while (egoIter.hasNext()) {
            Integer egoNum = (Integer)egoIter.next();            
            TreeMap cloneRow = new TreeMap(getRow(egoNum));
            clon.matrix.put(egoNum, cloneRow);
        }
        return clon;
    }

    /** This method removes the row and column associated
     *  with an Individual. This method is used to clean up
     *  any possible by-products of the accidental creation
     *  of this person being removed from the system.
     *
     *  @param ind  the person being deleted.
     */
    public void deletePerson(Individual ind) {
        Integer ndx = new Integer(ind.serialNmbr);
        TreeMap herRow = (TreeMap) matrix.get(ndx);
        if (herRow != null) {
            for (Object o : herRow.values()) {
                Node nod = (Node) o;
                Context.getCurrent().deleteDyads(nod, ndx);
            }
        }
        matrix.remove(ndx);
        Iterator rowIter = matrix.entrySet().iterator();
        while (rowIter.hasNext()) {
            Map.Entry entry = (Map.Entry) rowIter.next();
            Integer altSerial = (Integer) entry.getKey();
            TreeMap row = (TreeMap) entry.getValue();
            Node nod = (Node) row.get(ndx);
            Context.getCurrent().deleteDyads(nod, altSerial);
            row.remove(ndx);
        }
    }
    
    public void deleteKinTerm(int ego, int alter, String kinTerm, boolean adr) {
        TreeMap egoRow = (TreeMap)matrix.get(ego);
        Node nod = (Node)egoRow.get(alter);
        ArrayList<Object> lst = (adr ? nod.kinTermsAddr : nod.kinTermsRef);
        lst.remove(kinTerm);
    }
    
    public void correctKinTerm(int ego, int alter, String oldTerm, String newTerm, boolean adr) {
        TreeMap egoRow = (TreeMap)matrix.get(ego);
        Node nod = (Node)egoRow.get(alter);
        ArrayList<Object> lst = (adr ? nod.kinTermsAddr : nod.kinTermsRef);
        lst.remove(oldTerm);
        if (!lst.contains(newTerm)) {
            lst.add(newTerm);
        }
    }
    
    public void renameChartableUDP(String oldName, String newName) {
        String oldInverse = "*inverse" + oldName.substring(1),
               newInverse = "*inverse" + newName.substring(1);
        Iterator egoIter = matrix.values().iterator();
        while (egoIter.hasNext()) {
            TreeMap egoRow = (TreeMap) egoIter.next();
            for (Object o : egoRow.values()) {
                Node nod = (Node) o;
                nod.pcString = nod.pcString.replace(oldName, newName);
                nod.pcString = nod.pcString.replace(oldInverse, newInverse);
                ArrayList<Object> newMiniPreds = new ArrayList<Object>();
                for (Object obj : nod.miniPreds) {
                    String s = (String) obj;
                    int ndx = s.indexOf("(");
                    String pred = s.substring(0, ndx);
                    if (pred.equals(oldName)) {
                        newMiniPreds.add(newName + s.substring(ndx));
                    } else if (pred.equals(oldInverse)) {
                        newMiniPreds.add(newInverse + s.substring(ndx));
                    } else {
                        newMiniPreds.add(s);
                    }
                }
                nod.miniPreds = newMiniPreds;
            }
        }
    }
    
    public void removeChartableUDP(String udName) {
        String inverse = "*inverse" + udName.substring(1);
        Iterator egoIter = matrix.values().iterator();
        while (egoIter.hasNext()) {
            TreeMap egoRow = (TreeMap) egoIter.next();
            Iterator rowIter = egoRow.values().iterator();
            while (rowIter.hasNext()) {
                Node nod = (Node) rowIter.next();
                if (nod.pcString.contains(inverse) ||
                        nod.pcString.contains(udName)) {
                    rowIter.remove();
                }
            }            
        }
    }
    
    public int numberOfKinTerms() {
        int s = 0;
        Iterator egoIter = matrix.entrySet().iterator(), altIter;
        Map.Entry egoEntry, altEntry;
        Node cell;
        while (egoIter.hasNext()) {
            egoEntry = (Map.Entry) egoIter.next();
            altIter = ((TreeMap) egoEntry.getValue()).entrySet().iterator();
            while (altIter.hasNext()) {
                altEntry = (Map.Entry) altIter.next();
                cell = (Node) altEntry.getValue();
                s += cell.nmbrOfKinTerms();
            }  //  end of loop thru cells in the row            
        }  //  end of loop thru the rows
        return s;
    }

    public int numberOfCells() {
        return DomainTheory.countLeaves(matrix);
    }

    /** Verify that each row has a self-node, and that an 'Ego' term of address  
     *  appears only if this context has separate terms of address.
     */
    public void checkSelfNodes() {
        boolean doublEgo = Context.getCurrent().distinctAdrTerms;
        Node cell;
        for (int n=0; n < matrix.size(); n++) {
            cell = getCell(n,n);  //  this is the self node
            if (cell == null) {
                continue;
            }
            if (doublEgo) {  //  should be an address term
                if (cell.kinTermsAddr.isEmpty()) {
                    cell.kinTermsAddr.add("Ego");
                }
            }else {  //  no address terms allowed
                cell.kinTermsAddr.clear();
            }
        }  //  end of loop thru the rows
    }

    /** For every Node in the matrix, make terms of address that mimic the
     *  terms of reference.
     */
    public void addAdrCloneTerms() {
        Iterator rowIter = matrix.values().iterator();
        while (rowIter.hasNext()) {
            TreeMap rowMap = (TreeMap)rowIter.next();
            Iterator nodeIter = rowMap.values().iterator();
            while (nodeIter.hasNext()) {
                Node nod = (Node)nodeIter.next();
                nod.kinTermsAddr = union(nod.kinTermsRef, nod.kinTermsAddr);
                nod.extKinTermsAddr = union(nod.extKinTermsRef, nod.extKinTermsAddr);
            }
        }
    }
    
    
    /** Used when deleting a domain theory for Terms of Address.
     *  Visit every node of every row and nullify the Address terms.
     * */
    public void cleanAdrTerms() {
        Iterator rowIter = matrix.values().iterator(), altIter;
        while (rowIter.hasNext()) {
            TreeMap egoRow = (TreeMap)rowIter.next();
            altIter = egoRow.values().iterator();
            while (altIter.hasNext()) {
                Node nod = (Node)altIter.next();
                nod.kinTermsAddr.clear();
                nod.extKinTermsAddr.clear();
            }
        }
    }
    
    public ArrayList union(ArrayList lst1, ArrayList lst2) {
        ArrayList merge = new ArrayList(lst1);
        for (Object o : lst2) {
            if (! merge.contains(o)) {
                merge.add(o);
            }
        }
        return merge;
    }


    /**  This method builds a string that represents a KinTermMatrix in a SILKin data (_.silk) file.   */
    public String toSILKString() {
        String image = "";
        int ego, alter;
        Iterator egoIter = matrix.entrySet().iterator(), altIter, ktIter;
        Map.Entry egoEntry, altEntry;
        Node cell;
        while (egoIter.hasNext()) {
            egoEntry = (Map.Entry) egoIter.next();
            ego = ((Integer) egoEntry.getKey()).intValue();
            image += "<row ego=\"" + ego + "\">";
            altIter = ((TreeMap) egoEntry.getValue()).entrySet().iterator();
            while (altIter.hasNext()) {
                altEntry = (Map.Entry) altIter.next();
                alter = (Integer) altEntry.getKey();
                cell = (Node) altEntry.getValue();
                image += "\n\t<cell alter=\"" + alter + "\">" + cell.toSILKString() + "</cell>";
            }  //  end of loop thru cells in the row
            image += "\n</row>";
        }  //  end of loop thru the rows
        return image;
    }  //  end of method toSILKString

    /** Return a printable String displaying the complete contents of this Matrix and Indices */
    public String toString() {
        return printMatrix();
    }  //  end of over-riding method

    /**
     * 
     */
    public String printMatrixSummary() {
        String temp1, temp2, image = "\nSummary of Relationships in " + DomainTheory.current.languageName;
        image += ".\nEgo\t   Alter\tTerms\n";
        Iterator egoIter = matrix.entrySet().iterator(), altIter, ktIter;
        Map.Entry egoEntry, altEntry;
        Node cell;
        while (egoIter.hasNext()) {
            egoEntry = (Map.Entry) egoIter.next();
            image += "\nRow " + egoEntry.getKey().toString();
            altIter = ((TreeMap) egoEntry.getValue()).entrySet().iterator();
            while (altIter.hasNext()) {
                altEntry = (Map.Entry) altIter.next();
                temp1 = "\n\t" + altEntry.getKey().toString() + "\t";
                cell = (Node) altEntry.getValue();
                temp2 = cell.printTerms();
                if (! temp2.equals("0")) {
                    image += temp1 + temp2;
                }
            }
        }
        return image;
    }

    /** Return a printable String displaying the contents of this Matrix */
    public String printMatrix() {
        String image = "\nMatrix of Relationships in " + DomainTheory.current.languageName;
        image += ".\nEgo\n   Alter   (Primaries)  [Extended]  {Exceptions}    * = Terms of Address\n";
        Iterator egoIter = matrix.entrySet().iterator(), altIter, ktIter;
        Map.Entry egoEntry, altEntry;
        Node cell;
        while (egoIter.hasNext()) {
            egoEntry = (Map.Entry) egoIter.next();
            image += "\n" + egoEntry.getKey().toString() + "\n";
            altIter = ((TreeMap) egoEntry.getValue()).entrySet().iterator();
            while (altIter.hasNext()) {
                altEntry = (Map.Entry) altIter.next();
                image += "   " + altEntry.getKey().toString();
                cell = (Node) altEntry.getValue();
                image += cell.toString() + "\n";
            }
        }
        return image;
    }  //  end of method for printing matrix of relationship terms

    /**
    Enter into this KinTermMatrix the kinship term that <code>ego</code> calls <code>alter</code>.
    For example:  addKinTerm(DonaldDuckSerial#, ScroogeMcDuckSerial#, "uncle", "primary", false).

    @param	egoInt      (serial number of) speaker of this kinTerm: the focus person.
    @param	alterInt    (serial number of) person ego would call this kinTerm
    @param	kinTerm     the term to be found
    @param	type        'primary' or 'extended' or 'exception'
    @param      addr        true = searching Terms of Address, false = searching Terms of Reference
     */
    public void addTerm(int egoInt, int alterInt, String kinTerm, String type, boolean addr)
            throws KSInternalErrorException {
        if ((!type.equals("primary")) && (!type.equals("extended")) && (!type.equals("exception"))) {
            throw new KSInternalErrorException("Invalid 'type' given to method KinTermMatrix.addTerm.");
        }
        Integer ego = new Integer(egoInt), alter = new Integer(alterInt);
        String clas = (addr ? "address" : "reference");
        if (matrix.get(ego) == null) {
            matrix.put(ego, new TreeMap());
        }
        if (((TreeMap) matrix.get(ego)).get(alter) == null) {
            ((TreeMap) matrix.get(ego)).put(alter, new Node());
        }
        ((Node) ((TreeMap) matrix.get(ego)).get(alter)).addTerm(kinTerm, type, clas);
    }  //  end of method replaceTerms with ints
    
    public void insertNewRow(int serialNmbr) {
        TreeMap row = new TreeMap();
        Node n = Node.makeSelfNode(false);
        row.put(serialNmbr, n);
        matrix.put(serialNmbr, row);
    }

    public void addNode(int egoInt, int alterInt, Node node) {
        Integer ego = new Integer(egoInt), alter = new Integer(alterInt);
        Node oldNode = null;
        if (matrix.get(ego) == null) {
            matrix.put(ego, new TreeMap());
        } else {
            oldNode = (Node) ((TreeMap) matrix.get(ego)).get(alter);
        }
        ((TreeMap) matrix.get(ego)).put(alter, node);
        if (oldNode != null) {
            try {
                ArrayList<String> deletedTerms = oldNode.getKinTerms(false);
                deletedTerms.removeAll(node.getKinTerms(false));
                DomainTheory dt = Context.getCurrent().domTheoryRef();
                deleteDyads(deletedTerms, egoInt, alterInt, node, dt);                
                String oldPC = oldNode.pcString, 
                       newPC = node.pcString;
                if (! oldPC.equals(newPC)) {
                    KinTypeIndex kti = Context.getCurrent().kti;
                    Integer[] pair = {egoInt, alterInt};
                    kti.removePair(oldPC, pair);
                }
                if (SIL_Edit.edWin.chart.distinctAdrTerms) {
                    dt = Context.getCurrent().domTheoryAdr();
                    deletedTerms = oldNode.getKinTerms(true);
                    deletedTerms.removeAll(node.getKinTerms(true));
                    deleteDyads(deletedTerms, egoInt, alterInt, node, dt);                     
                }
            } catch (Exception ex) {
                String msg = "Error while loading domain theories: " + ex;
                MainPane.displayError(msg, "Internal Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    void deleteDyads(ArrayList<String> deletedTerms, int egoInt, int alterInt,
            Node node, DomainTheory dt) {
        DyadTMap dyadsDef = dt.dyadsDefined;
        DyadTMap dyadsUndef = dt.dyadsUndefined;
        for (String kinTerm : deletedTerms) {
            if (dyadsDef.containsKey(kinTerm)) {
                dyadsDef.removeDyad(dt, egoInt, alterInt, kinTerm, node.pcString);
            } else {
                dyadsUndef.removeDyad(dt, egoInt, alterInt, kinTerm, node.pcString);
            }
        }
    }

    /**
    Return a {@link Node} of KinTerms that <code>ego</code> may call <code>alter</code>.

    @param	egoInt		(serial number of) speaker of this kinTerm: the focus person.
    @param	alterInt		(serial number of) person ego would call this kinTerm

    @return		the Node of KinTerms, or null if no terms are defined for this pair.
     */
    public Node getCell(int egoInt, int alterInt) {
        Integer ego = new Integer(egoInt), alter = new Integer(alterInt);
        if (matrix.get(ego) == null) {
            return null;
        }
        return (Node) ((TreeMap) matrix.get(ego)).get(alter);
    }  //  end of method getCell

    /**
    Return a {@link Node} of KinTerms that <code>ego</code> may call <code>alter</code>.

    @param	egoInd		{@link Individual} who uses this kinTerm: the focus person.
    @param	alterInd        <code>Individual</code> ego would call this kinTerm

    @return		the Node of KinTerms
     */
    public Node getCell(Individual egoInd, Individual alterInd) {
        Integer ego = new Integer(egoInd.serialNmbr), alter = new Integer(alterInd.serialNmbr);
        if (matrix.get(ego) == null) {
            return null;
        }
        return (Node) ((TreeMap) matrix.get(ego)).get(alter);
    }  //  end of method getCell

    /**
    Return an ArrayList<Object> of Nodes containing the kinTerms ego would use for each
    person in the list of individuals.  The returned list is exactly the same length as the
    input list, or <code>null</code> if there are no kinTerms for this Ego.

    @param	ego		serial number of a focal person.
    @param	individuals	array of serial numbers

    @return	  a list of Nodes; each (possibly empty) holds appropriate kinTerms (Strings),
    for the Individual (who has a serial number) in the corresponding position of the
    input ArrayList<Object> <code>individuals</code>.
     */
    public ArrayList<Object> getEgoTermList(int ego, int[] individuals) {
        ArrayList<Object> reply = new ArrayList<Object>();
        TreeMap tree = (TreeMap) matrix.get(new Integer(ego));
        if (tree == null) {
            return null;
        }
        Integer serial;
        for (int j = 0; j < individuals.length; j++) {
            serial = new Integer(individuals[j]);
            if (tree.get(serial) == null) {
                reply.add(new Node());
            } else {
                reply.add(tree.get(serial));
            }
        }  //  end of loop thru individuals
        return reply;
    }  //  end of method getEgoTermList

    /**
    Return a TreeMap which represents <code>ego</code>'s row in this KinTermMatrix, or <code>null</code> if no row exists yet.
    The returned TreeMap has one Node associated with each Alter that has any kinTerms defined.

    @param	egoInd	a focal person.

    @return	  a TreeMap of Nodes associated with Alter Serial Numbers.
     */
    public TreeMap getRow(Individual egoInd) {
        Integer ego = new Integer(egoInd.serialNmbr);
        return (TreeMap) matrix.get(ego);
    }  //  end of method getRow

    /**
    Return a TreeMap which represents <code>ego</code>'s row in this KinTermMatrix, or <code>null</code> if no row exists yet.  
    The returned TreeMap has one Node associated with each Alter that has any kinTerms defined.

    @param	egoInt	serial number of a focal person.

    @return	  a TreeMap of Nodes associated with Alter Serial Numbers.
     */
    public TreeMap getRow(int egoInt) {
        Integer ego = new Integer(egoInt);
        if (matrix.get(ego) == null) {
            return null;
        } else {
            return (TreeMap) matrix.get(ego);
        }
    }  //  end of method getRow

    /**  Build an array of ints = the linking kinsmen between ego and alter.
     *   A "miniPred" looks like:  "Fa(#3,#8)"
     *
     * @param egoNum    ego's serial number
     * @param alterNum  alter's serial number
     * @return  the array
     */
    public ArrayList<Integer> getPath(int egoNum, int alterNum)
            throws KSInternalErrorException {
        ArrayList<Integer> path = new ArrayList<Integer>();
        Node n = getCell(egoNum, alterNum);
        if (n == null || n.miniPreds == null) {
            return path;
        }
        for (Object o : n.miniPreds) {
            String pred = (String) o, pcStr;
            int leftParen, start, comma, close, p1, p2;
            leftParen = pred.indexOf("(");
            start = pred.indexOf("#");
            comma = pred.indexOf(",");
            close = pred.indexOf(")");
            pcStr = pred.substring(0, leftParen);
            p1 = Integer.parseInt(pred.substring(start + 1, comma));
            start = pred.indexOf("#", comma);
            p2 = Integer.parseInt(pred.substring(start + 1, close));
            if (p1 != egoNum && p1 != alterNum && !path.contains(p1)) {
                path.add(p1);
            }
            if (Context.getCurrent().chartDescriptions.size() > 1 && pcStr.startsWith("St")) {
                //  highlighting linking natural kin of step-relations is only needed if multi charts  
                try {
                    if (pcStr.equals("Stfa") || pcStr.equals("Stmo")) {
                        findNaturalLinkingKin(p1, p2, path);
                    } else {
                        findNaturalLinkingKin(p2, p1, path);
                    }
                } catch (KSInternalErrorException exc) {
                    throw new KSInternalErrorException("Found invalid minipred: " + pred);
                }
            }

            if (p2 != egoNum && p2 != alterNum && !path.contains(p2)) {
                path.add(p2);
            }
        }
        return path;
    }

    void findNaturalLinkingKin(int parent, int child, ArrayList<Integer> path)
            throws KSInternalErrorException {
        Individual par = Context.getCurrent().individualCensus.get(parent),
                kid = Context.getCurrent().individualCensus.get(child),
                spouse = null, link = null;
        // par is a step-parent of kid
        bigLoop:
        for (Object o : par.marriages) {
            Family fam = (Family) o;
            spouse = (fam.wife == par ? fam.husband : fam.wife);
            for (Object o2 : spouse.marriages) {
                Family stepFam = (Family) o2;
                if (stepFam != fam && stepFam.children.contains(kid)) {
                    link = spouse;
                    break bigLoop;
                }
            }  
        }
        if (link == null) {
            throw new KSInternalErrorException("");
        }
        path.add(link.serialNmbr);
    }

    /**
    Update <code>ego</code>'s row in this KinTermMatrix.  For each person in the ArrayList<Object>
    <code>individuals</code>, let its current node become the new <code>Node</code> for that person in ego's row.
    The Index of kinTerms associated with this KinTermMatrix is also updated.
     * This method is used primarily in Example Generation *

    @param	egoInd		a focal person.
    @param	individuals	an ArrayList<Object> of {@link Individual}s
     */
    public void updateRow(Individual egoInd, ArrayList<Individual> individuals) throws NumberFormatException {
        //  Every individual who has a node appears on Ego's chart.  So store his node in Ego's row.
        //  CAUTION:  watch out for substitute egos.
        Integer ego = new Integer(egoInd.serialNmbr), alter;
        if (matrix.get(ego) == null) {
            matrix.put(ego, new TreeMap());
        }
        Individual person;
        TreeMap tree = (TreeMap) matrix.get(ego), altTree;
        Iterator indIter = individuals.iterator();
        while (indIter.hasNext()) {
            person = (Individual) indIter.next();
            alter = new Integer(person.serialNmbr);
            if (person.node != null && person.node.ktSuffix.length() > 0) {
                Integer altEgo = new Integer(pullSerial(person.node.ktSuffix));
                if (altEgo.intValue() == egoInd.serialNmbr) {  //  regular ego
                    tree.put(alter, person.node);
                } else {  //  substitute ego
                    if (matrix.get(altEgo) == null) {
                        matrix.put(altEgo, new TreeMap());
                    }
                    altTree = (TreeMap) matrix.get(altEgo);
                    altTree.put(alter, person.node);
                }
            } else {
                tree.remove(alter);
            }
        }  //  end of loop thru serial numbers and cells
    }  //  end of method updateRow

    public int pullSerial(String suffix) throws NumberFormatException {
        int end = suffix.indexOf(")");
        return Integer.parseInt(suffix.substring(1, end));
    }  //  end of method pullSerial

    /**Replace <code>ego</code>'s row in this KinTermMatrix with this TreeMap.  
     * All terms in the old row are erased. Update the Indices of kinTerms 
     * associated with this KinTermMatrix.
     * 
     * @param	egoInt		serial number of a focal person.
     * @param	newRow		new TreeMap of serial numbers associated with <code>Node</code>s for <code>ego</code>'s row.
     * 
     * @throws	  a KSInternalErrorException if the values in the TreeMap <code>newRow</code>
     *              are not <code>Node</code>s and the keys are not Integers.
     */
    public void replaceRow(int egoInt, TreeMap newRow) {

        Integer ego = new Integer(egoInt), alter;
        Iterator entries = newRow.entrySet().iterator();
        Map.Entry entry;
        Node cell;
        while (entries.hasNext()) {
            entry = (Map.Entry) entries.next();
            Integer person = (Integer) entry.getKey();
            cell = (Node) entry.getValue();
        }  //  end of validity-test loop thru newRow's contents
        //  If we made it this far, all data is valid.
        matrix.put(ego, newRow);
        entries = newRow.entrySet().iterator();
        while (entries.hasNext()) {
            entry = (Map.Entry) entries.next();
            alter = (Integer) entry.getKey();
            cell = (Node) entry.getValue();
        }  //  end of loop thru newRow's contents
    }  //  end of method replaceRow
}  //  end of class KinTermMatrix
