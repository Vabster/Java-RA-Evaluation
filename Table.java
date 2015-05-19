
/****************************************************************************************
 * @file  Table.java
 *
 * @author   John Miller
 */

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.*;

import static java.lang.Boolean.*;
import static java.lang.System.out;

/****************************************************************************************
 * This class implements relational database tables (including attribute names, domains
 * and a list of tuples.  Five basic relational algebra operators are provided: project,
 * select, union, minus join.  The insert data manipulation operator is also provided.
 * Missing are update and delete data manipulation operators.
 */
public class Table
       implements Serializable
{
    /** Relative path for storage directory
     */
    private static final String DIR = "store" + File.separator;

    /** Filename extension for database files
     */
    private static final String EXT = ".dbf";

    /** Counter for naming temporary tables.
     */
    private static int count = 0;

    /** Table name.
     */
    private final String name;

    /** Array of attribute names.
     */
    private final String [] attribute;

    /** Array of attribute domains: a domain may be
     *  integer types: Long, Integer, Short, Byte
     *  real types: Double, Float
     *  string types: Character, String
     */
    private final Class [] domain;

    /** Collection of tuples (data storage).
     */
    private final List <Comparable []> tuples;

    /** Primary key. 
     */
    private final String [] key;

    /** Index into tuples (maps key to tuple number).
     */
    private final Map <KeyType, Comparable []> index;

    //----------------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------------

    /************************************************************************************
     * Construct an empty table from the meta-data specifications.
     *
     * @param _name       the name of the relation
     * @param _attribute  the string containing attributes names
     * @param _domain     the string containing attribute domains (data types)
     * @param _key        the primary key
     */  
    public Table (String _name, String [] _attribute, Class [] _domain, String [] _key)
    {
        name      = _name;
        attribute = _attribute;
        domain    = _domain;
        key       = _key;
        tuples    = new ArrayList <> ();
        index     = new BpTreeMap<>(KeyType.class, Comparable[].class);      // also try BPTreeMap, LinHashMap or ExtHashMap
    } // constructor

    /************************************************************************************
     * Construct a table from the meta-data specifications and data in _tuples list.
     *
     * @param _name       the name of the relation
     * @param _attribute  the string containing attributes names
     * @param _domain     the string containing attribute domains (data types)
     * @param _key        the primary key
     * @param _tuple      the list of tuples containing the data
     */  
    public Table (String _name, String [] _attribute, Class [] _domain, String [] _key,
                  List <Comparable []> _tuples)
    {
        name      = _name;
        attribute = _attribute;
        domain    = _domain;
        key       = _key;
        tuples    = _tuples;
        index     = new BpTreeMap<>(KeyType.class, Comparable[].class);;       // also try BPTreeMap, LinHashMap or ExtHashMap
    } // constructor

    /************************************************************************************
     * Construct an empty table from the raw string specifications.
     *
     * @param name        the name of the relation
     * @param attributes  the string containing attributes names
     * @param domains     the string containing attribute domains (data types)
     */
    public Table (String name, String attributes, String domains, String _key)
    {
        this (name, attributes.split (" "), findClass (domains.split (" ")), _key.split(" "));

        out.println ("DDL> create table " + name + " (" + attributes + ")");
    } // constructor

    //----------------------------------------------------------------------------------
    // Public Methods
    //----------------------------------------------------------------------------------

    /************************************************************************************
     * Project the tuples onto a lower dimension by keeping only the given attributes.
     * Check whether the original key is included in the projection.
     *
     * #usage movie.project ("title year studioNo")
     *
     * @param attributes  the attributes to project onto
     * @return  a table of projected tuples
     */
    public Table project (String attributes)
    {
    	out.println ("RA> " + name + ".project (" + attributes + ")");
        String [] attrs     = attributes.split (" ");
        Class []  colDomain = extractDom (match (attrs), domain);
        String [] newKey    = (Arrays.asList (attrs).containsAll (Arrays.asList (key))) ? key : attrs;

        List <Comparable []> rows = new ArrayList<Comparable[]>();
        for(Map.Entry<KeyType, Comparable[]> mapIndex: index.entrySet()){
        	rows.add(extract(mapIndex.getValue(),attrs));
		
        		}

        return new Table (name + count++, attrs, colDomain, newKey, rows);
    } // project

    /************************************************************************************
     * Select the tuples satisfying the given predicate (Boolean function).
     *
     * #usage movie.select (t -> t[movie.col("year")].equals (1977))
     *
     * @param predicate  the check condition for tuples
     * @return  a table with tuples satisfying the predicate
     */
    public Table select (Predicate <Comparable []> predicate)
    {
        out.println ("RA> " + name + ".select (" + predicate + ")");

        List <Comparable []> rows = new ArrayList<Comparable []>();
        
        //Table check = new Table("check", attribute, domain, key, rows);
        for (Comparable [] tup : this.tuples) 
    	{
        	if(predicate.test(tup))
    		{
    			rows.add(tup);
    		}
        } 

        return new Table (name + count++, attribute, domain, key, rows);
    } // select

    /************************************************************************************
     * Select the tuples satisfying the given key predicate (key = value).  Use an index
     * (Map) to retrieve the tuple with the given key value.
     *
     * @param keyVal  the given key value
     * @return  a table with the tuple satisfying the key predicate
     */
    public Table select (KeyType keyVal)
    {
        out.println ("RA> " + name + ".select (" + keyVal + ")");

        List <Comparable []> rows = new ArrayList<Comparable []>();
			
        rows.add(index.get(keyVal));

        return new Table (name + count++, attribute, domain, key, rows);
    } // select

    /************************************************************************************
     * Union this table and table2.  Check that the two tables are compatible.
     * Does NOT check for compatability, as he said we did not have to worry
     * about this until project 2
     * #usage movie.union (show)
     *
     * @param table2  the rhs table in the union operation
     * @return  a table representing the union
     */
    public Table union (Table table2)
    {
        out.println ("RA> " + name + ".union (" + table2.name + ")");
        if (! compatible (table2)) return null;

        List <Comparable []> rows = new ArrayList<Comparable []>();

     // Iterate through current tuples
     		for (Map.Entry<KeyType, Comparable[]> e : index.entrySet())
     			// Add current table tupples
     			rows.add(e.getValue());
     		
     		// Iterate through table2 tuples
     		for (Map.Entry<KeyType, Comparable[]> e : table2.index.entrySet())
     			// Add table2 table tupples
     			rows.add(e.getValue());
        
        //check.print();

        return new Table (name + count++, attribute, domain, key, rows);
    } // union

    /************************************************************************************
     * Take the difference of this table and table2.  Check that the two tables are
     * compatible.
     *
     * #usage movie.minus (show)
     *
     * @param table2  The rhs table in the minus operation
     * @return  a table representing the difference
     */
    public Table minus (Table table2)
    {
    	int count = 0;
        out.println ("RA> " + name + ".minus (" + table2.name + ")");
       if (! compatible (table2)) return null;
        boolean minus = false;
        //List <Comparable []> rows = null;
        List <Comparable []> rows = new ArrayList<Comparable[]>();
        for(Map.Entry<KeyType, Comparable[]> mapIndex : index.entrySet()){
        	for(Map.Entry<KeyType, Comparable[]> mapIndex2: table2.index.entrySet()){
        		if(mapIndex.getKey().equals(mapIndex2.getKey()) ){
        			minus = true;
        			}
        		}//for mapIndex2
        	if(minus == false){
    			rows.add(mapIndex.getValue());
    		}
    		minus = false;
        	}//for mapIndex
        	

        return new Table (name + count++, attribute, domain, key, rows);
    } // minus

    /************************************************************************************
     * Join this table and table2 by performing an equijoin.  Tuples from both tables
     * are compared requiring attributes1 to equal attributes2.  Disambiguate attribute
     * names by append "2" to the end of any duplicate attribute name.
     *
     * #usage movie.join ("studioNo", "name", studio)
     * #usage movieStar.join ("name == s.name", starsIn)
     *
     * @param attribute1  the attributes of this table to be compared (Foreign Key)
     * @param attribute2  the attributes of table2 to be compared (Primary Key)
     * @param table2      the rhs table in the join operation
     * @return  a table with tuples satisfying the equality predicate
     */
    public Table join(String attribute1, String attribute2, Table table2) {
    	out.println ("RA> " + name + ".join (" + attribute1 + ", " + attribute2 + ", "
				+ table2.name + ")");


		String [] t_attrs = attribute1.split (" ");
		String [] u_attrs = attribute2.split (" ");

		int i=0;
		boolean are_equal = true;
		List <Comparable []> rows = new ArrayList <Comparable []>();
		//System.out.println(col(u_attrs[0]));
		//System.out.println();
		
		if(t_attrs.length != u_attrs.length)
			return null;
		
		int[] tArray = new int[t_attrs.length];	
		int[] uArray = new int[u_attrs.length];	
		
		for(int k = 0; k < tArray.length; k++)
		{
			tArray[k] = col(t_attrs[k]);
			uArray[k] = table2.col(u_attrs[k]);
		}
		
		List <Comparable []> table1Tuples = new ArrayList <Comparable []>();
		List <Comparable []> table2Tuples = new ArrayList <Comparable []>();
		System.out.println(table1Tuples.size());
		
		for (int k = 0; k < this.tuples.size(); k++)
		{
			table1Tuples.add(this.tuples.get(k));
		}
		
		for (int k = 0; k < table2.tuples.size(); k++)
		{
			table2Tuples.add(table2.tuples.get(k));
		}
		
		
		int longer = table1Tuples.size();
		if (longer < table2Tuples.size())
		{
			longer =  table2Tuples.size();
		}
		
		boolean equal;
		for(int k = 0; k < table1Tuples.size(); k++)
		{
			for(int j = 0; j < table2Tuples.size(); j++)
			{
				equal = true;
				for(int a = 0; a < tArray.length; a++)
				{
					if(!(table1Tuples.get(k)[tArray[a]] == table2Tuples.get(j)[uArray[a]]))
					{
						equal = false;
					}
				}
				if(equal)
				{
					rows.add(ArrayUtil.concat(this.tuples.get(k), table2.tuples.get(j)));
				}
				
			}
		}
		
		
			
		
		

		return new Table (name + count++, ArrayUtil.concat (attribute, table2.attribute),
				ArrayUtil.concat (domain, table2.domain), key, rows);
	} // join

    /************************************************************************************
     * Return the column position for the given attribute name.
     *
     * @param attr  the given attribute name
     * @return  a column position
     */
    public int col (String attr)
    {
        for (int i = 0; i < attribute.length; i++) {
           if (attr.equals (attribute [i])) return i;
        } // for

        return -1;  // not found
    } // col

    /************************************************************************************
     * Insert a tuple to the table.
     *
     * #usage movie.insert ("'Star_Wars'", 1977, 124, "T", "Fox", 12345)
     *
     * @param tup  the array of attribute values forming the tuple
     * @return  whether insertion was successful
     */
    public boolean insert (Comparable [] tup)
    {
        out.println ("DML> insert into " + name + " values ( " + Arrays.toString (tup) + " )");

        if (typeCheck (tup)) {
            tuples.add (tup);
            Comparable [] keyVal = new Comparable [key.length];
            int []        cols   = match (key);
            for (int j = 0; j < keyVal.length; j++) keyVal [j] = tup [cols [j]];
            index.put (new KeyType (keyVal), tup);
            return true;
        } else {
            return false;
        } // if
    } // insert

    /************************************************************************************
     * Get the name of the table.
     *
     * @return  the table's name
     */
    public String getName ()
    {
        return name;
    } // getName

    /************************************************************************************
     * Print this table.
     */
    public void print ()
    {
        out.println ("\n Table " + name);
        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
        out.print ("| ");
        for (String a : attribute) out.printf ("%15s", a);
        out.println (" |");
        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
        for (Comparable [] tup : tuples) {
            out.print ("| ");
            for (Comparable attr : tup) 
            {
            	if(attr == null)
            	{
            		break;
            	}
            	out.printf ("%15s", attr);
            }
            out.println (" |");
        } // for
        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
    } // print

    /************************************************************************************
     * Print this table's index (Map).
     */
    public void printIndex ()
    {
        out.println ("\n Index for " + name);
        out.println ("-------------------");
        for (Map.Entry <KeyType, Comparable []> e : index.entrySet ()) {
            out.println (e.getKey () + " -> " + Arrays.toString (e.getValue ()));
        } // for
        out.println ("-------------------");
    } // printIndex

    /************************************************************************************
     * Load the table with the given name into memory. 
     *
     * @param name  the name of the table to load
     */
    public static Table load (String name)
    {
        Table tab = null;
        try {
            ObjectInputStream ois = new ObjectInputStream (new FileInputStream (DIR + name + EXT));
            tab = (Table) ois.readObject ();
            ois.close ();
        } catch (IOException ex) {
            out.println ("load: IO Exception");
            ex.printStackTrace ();
        } catch (ClassNotFoundException ex) {
            out.println ("load: Class Not Found Exception");
            ex.printStackTrace ();
        } // try
        return tab;
    } // load

    /************************************************************************************
     * Save this table in a file.
     */
    public void save ()
    {
        try {
            ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream (DIR + name + EXT));
            oos.writeObject (this);
            oos.close ();
        } catch (IOException ex) {
            out.println ("save: IO Exception");
            ex.printStackTrace ();
        } // try
    } // save
    
    public boolean equalityCheck(Table checkTable)
    {
    	
    	if(attribute.length != checkTable.attribute.length)
    		return false;
    	for(int i = 0; i < attribute.length; i++)
    	{
    		if(!(attribute[i].equals(checkTable.attribute[i]) && domain[i].equals(checkTable.domain[i])))
    		{
    			return false;
    		}
    	}
    	System.out.println("Tuples");
    	System.out.println(tuples.size());
    	if(tuples.size() != checkTable.tuples.size())
    	{
    		return false;
    	}
    	String tuplesCheck0 = "";
    	for (Comparable [] tup : tuples) 
    	{
            for (Comparable attr : tup) 
            {
            	tuplesCheck0 = tuplesCheck0 + attr;
            }
                 
        } 
    	
    	String tuplesCheck1 = "";
    	for (Comparable [] tup : checkTable.tuples) 
    	{
            for (Comparable attr : tup) 
            {
            	tuplesCheck1 = tuplesCheck1 + attr;
            }
                 
        } 
    	
    	System.out.println(tuplesCheck0);
    	System.out.println(tuplesCheck1);
    	
    	if(!(tuplesCheck0.equals(tuplesCheck1)))
    	{
    		return false;
    	}
    	
    	return true;
    }

    //----------------------------------------------------------------------------------
    // Private Methods
    //----------------------------------------------------------------------------------

    /************************************************************************************
     * Determine whether the two tables (this and table2) are compatible, i.e., have
     * the same number of attributes each with the same corresponding domain.
     *
     * @param table2  the rhs table
     * @return  whether the two tables are compatible
     */
    private boolean compatible (Table table2)
    {
        if (domain.length != table2.domain.length) {
            out.println ("compatible ERROR: table have different arity");
            return false;
        } // if
        for (int j = 0; j < domain.length; j++) {
            if (domain [j] != table2.domain [j]) {
                out.println ("compatible ERROR: tables disagree on domain " + j);
                return false;
            } // if
        } // for
        return true;
    } // compatible

    /************************************************************************************
     * Match the column and attribute names to determine the domains.
     *
     * @param column  the array of column names
     * @return  an array of column index positions
     */
    private int [] match (String [] column)
    {
        int [] colPos = new int [column.length];

        for (int j = 0; j < column.length; j++) {
            boolean matched = false;
            for (int k = 0; k < attribute.length; k++) {
                if (column [j].equals (attribute [k])) {
                    matched = true;
                    colPos [j] = k;
                } // for
            } // for
            if ( ! matched) {
                out.println ("match: domain not found for " + column [j]);
            } // if
        } // for

        return colPos;
    } // match

    /************************************************************************************
     * Extract the attributes specified by the column array from tuple t.
     *
     * @param t       the tuple to extract from
     * @param column  the array of column names
     * @return  a smaller tuple extracted from tuple t 
     */
    private Comparable [] extract (Comparable [] t, String [] column)
    {
        Comparable [] tup = new Comparable [column.length];
        int [] colPos = match (column);
        for (int j = 0; j < column.length; j++) tup [j] = t [colPos [j]];
        return tup;
    } // extract

    /************************************************************************************
     * Check the size of the tuple (number of elements in list) as well as the type of
     * each value to ensure it is from the right domain. 
     *
     * @param t  the tuple as a list of attribute values
     * @return  whether the tuple has the right size and values that comply
     *          with the given domains
     */
    private boolean typeCheck (Comparable [] t)
    { 
    	// Makes sure the tuple length is equal to attribute length
    	if(t.length != this.attribute.length)
    	{
    		return false;
    	}
    	
    	int i = 0;
    	// Loops through the attributes of tuple t and compares to domain
    	// Returns false if there is a non-matching domain
    	for (Comparable attr : t)
    	{
    		if (!(attr.getClass().equals(this.domain[i]))) 
        	{
    			// Default float type is double, this guards against insert errors due to this
    			if(attr.getClass().equals(Double.class) || this.domain[i].equals(Float.class))
    			{
    				i++;
    				continue;
    			}
        		return false;
            }
    		// increments i, which determines where in the domain array we are
        	i++;
    	}

        return true;
    } // typeCheck

    /************************************************************************************
     * Find the classes in the "java.lang" package with given names.
     *
     * @param className  the array of class name (e.g., {"Integer", "String"})
     * @return  an array of Java classes
     */
    private static Class [] findClass (String [] className)
    {
        Class [] classArray = new Class [className.length];

        for (int i = 0; i < className.length; i++) {
            try {
                classArray [i] = Class.forName ("java.lang." + className [i]);
            } catch (ClassNotFoundException ex) {
                out.println ("findClass: " + ex);
            } // try
        } // for

        return classArray;
    } // findClass

    /************************************************************************************
     * Extract the corresponding domains.
     *
     * @param colPos the column positions to extract.
     * @param group  where to extract from
     * @return  the extracted domains
     */
    private Class [] extractDom (int [] colPos, Class [] group)
    {
        Class [] obj = new Class [colPos.length];

        for (int j = 0; j < colPos.length; j++) {
            obj [j] = group [colPos [j]];
        } // for

        return obj;
    } // extractDom
    
    
    

} // Table class
