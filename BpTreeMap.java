
/************************************************************************************
 * @file BpTreeMap.java
 *
 * @author  John Miller
 */

import java.io.*;
import java.lang.reflect.Array;

import static java.lang.System.out;

import java.util.*;




/************************************************************************************
 * This class provides B+Tree maps.  B+Trees are used as multi-level index structures
 * that provide efficient access for both point queries and range queries.
 */
public class BpTreeMap <K extends Comparable <K>, V>
       extends AbstractMap <K, V>
       implements Serializable, Cloneable, SortedMap <K, V>
{
    /** The maximum fanout for a B+Tree node.
     */
    private static final int ORDER = 5;

    /** The class for type K.
     */
    private final Class <K> classK;

    /** The class for type V.
     */
    private final Class <V> classV;
    private TreeMap<K,V> map;

    /********************************************************************************
     * This inner class defines nodes that are stored in the B+tree map.
     */
    private class Node
    {
        boolean   isLeaf;
        int       nKeys;
        K []      key;
        Object [] ref;
        @SuppressWarnings("unchecked")
        Node (boolean _isLeaf)
        {
            isLeaf = _isLeaf;
            nKeys  = 0;
            key    = (K []) Array.newInstance (classK, ORDER - 1);
            if (isLeaf) {
                //ref = (V []) Array.newInstance (classV, ORDER);
                ref = new Object [ORDER];
            } else {
                ref = (Node []) Array.newInstance (Node.class, ORDER);
            } // if
        } // constructor
    } // Node inner class


    /** The root of the B+Tree
     */
    private Node root;

    /** The counter for the number nodes accessed (for performance testing).
     */
    private int count = 0;

    
    /********************************************************************************
     * Construct an empty B+Tree map.
     * @param _classK  the class for keys (K)
     * @param _classV  the class for values (V)
     */
    public BpTreeMap (Class <K> _classK, Class <V> _classV)
    {
        classK = _classK;
        classV = _classV;
        root   = new Node (true);
       
    } // constructor

    /********************************************************************************
     * Return null to use the natural order based on the key type.  This requires the
     * key type to implement Comparable.
     */
    public Comparator <? super K> comparator () 
    {
        return null;
    } // comparator

    /********************************************************************************
     * Return a set containing all the entries as pairs of keys and values.
     * @return  the set view of the map
     *
     */
    
    public Set <Map.Entry <K, V>> entrySet ()
    {
    	Set <Map.Entry <K, V>> enSet = new LinkedHashSet <> ();

    	// Creates node bottomLevel that will start as the root, and move its way down
    	// to the bottom of the b+ tree
        Node bottomLevel = root;

        // Moves to the bottom left child node until it reaches the leaf level
        while (bottomLevel.isLeaf == false) 
        { 
        	bottomLevel = (Node) bottomLevel.ref[0]; 
        }
        
        // Moves through each leaf and and adds the key and ref value to the enset
        while (true) 
        {
        	// Used to iterate through ref
        	int i = 0;
            // For every key in the node, adds the key and ref pair to enSet
            for (K key : bottomLevel.key) 
            {
            	if(key == null)
            	{
            		break;
            	}
            	// Creates the entry to be added
            	java.util.AbstractMap.SimpleEntry<K, V> enSetEntry = new AbstractMap.SimpleEntry <> (key, (V) bottomLevel.ref[i]);
            	// Adds the entry
            	enSet.add(enSetEntry);
            	i++;
            }
            
            // Moves to the next node moving to the right, while loop ends when this becomes null
           // out.println("bottom level " + bottomLevel);
            bottomLevel = (Node) bottomLevel.ref[bottomLevel.nKeys];    
            if(bottomLevel == null)
            {
            	// Exits loop and returns enSet
            	break;
            }
        } 
            
        return enSet;
    } // entrySet

    /********************************************************************************
     * Given the key, look up the value in the B+Tree map.
     * @param key  the key used for look up
     * @return  the value associated with the key
     */
    @SuppressWarnings("unchecked")
    public V get (Object key)
    {
    	return find ((K) key, root);
    } // get

    /********************************************************************************
     * Put the key-value pair in the B+Tree map.
     * @param key    the key to insert
     * @param value  the value to insert
     * @return  null (not the previous value)
     */
    public V put (K key, V value)
    {
        insert (key, value, root, null);
        return null;
    } // put

    /********************************************************************************
     * Return the first (smallest) key in the B+Tree map.
     * @return  the first key in the B+Tree map.
     */
    public K firstKey () 
    {
    	// Initializes firstNode, starts out as the root, and later becomes first leaf node
    	Node firstNode = root;
    	
    	// Moves to the left child continuously until it reaches the child node
    	while (!firstNode.isLeaf) 
    	{
    		firstNode = (Node) firstNode.ref[0];
    	}
    	
    	K firstKey = firstNode.key[0];
        
        
        return firstKey;

    }

    /********************************************************************************
     * Return the last (largest) key in the B+Tree map.
     * @return  the last key in the B+Tree map.
     */
    public K lastKey () 
    {
    	
    	Node lastNode = root;
    	
    	// Moves down to the right child until it reaches child level
    	while (!lastNode.isLeaf) 
    	{
    		lastNode = (Node) lastNode.ref[lastNode.nKeys];
    	}
    	
    	int finalKey = 0;
    	// Goes through node until it reaches a null node, records last non-null node.
    	for(int i = 0; i < lastNode.nKeys; i++)
    	{
    		if(lastNode.key[i] == null)
    		{
    			break;
    		}
    		finalKey = i;
    	}
    	K lastKey = lastNode.key[finalKey];
        
        return lastKey;
    } // lastKey

    /********************************************************************************
     * Return the portion of the B+Tree map where key < toKey.
     * @return  the submap with keys in the range [firstKey, toKey)
     */
    public SortedMap <K,V> headMap (K toKey)
    {
    	// Starts node at the root
    	Node node = root;
    	
    	// Will store the headMap
    	SortedMap<K,V> headMapHolder = new TreeMap<>();
    	
    	// Goes down to the leftmost leaf node
    	 while (node.isLeaf == false) 
         { 
         	node = (Node) node.ref[0]; 
         }
    	 
    	 boolean continueLoop = true;
         while ( continueLoop && node != null) 
         { 
             // Adds all keys between the firstKey and toKey
             for (int i = 0; i < node.nKeys; i++) 
             {
            	 // If the key is less than or equal to toKey add it to the map
                 if(node.key[i].compareTo(toKey) <= 0) 
                 {
                 	headMapHolder.put(node.key[i], (V) node.ref[i]);
                 }
                 // If the key is greater than toKey, end the loops
                 if(node.key[i].compareTo(toKey) == 1)
                 {
                 	continueLoop = false;
                 	break;
                 }
             } 
             node = (Node) node.ref[node.nKeys];   
         }
        return headMapHolder;
    } // headMap

    /********************************************************************************
     * Return the portion of the B+Tree map where fromKey <= key.
     * @return  the submap with keys in the range [fromKey, lastKey]
     */
    public SortedMap <K,V> tailMap (K fromKey)
    {
    	// Will store the tailmap
    	SortedMap<K,V> tailMapHolder = new TreeMap<>();
    	// initializes index
    	int index;
        // Determine which ref index to go down into
        Node node = root;
        while(node.isLeaf == false) 
        { 
        	index = 0;
        	for(int i = 0; i < node.nKeys; i++)
        	{
        		if(node.key[i].compareTo(fromKey) >= 0)
        		{
        			break;
        		}
        		index++;
        	}
        
        	node = (Node) node.ref[index]; 
        }
        
        
        while (node != null) 
        { 
            // if a key is between fromKey and toKey, add it to the map
            for (int i = 0; i < node.nKeys; i++) 
            {
            	// Checks for condition
                if(fromKey.compareTo(node.key[i]) <= 0) 
                {
                	tailMapHolder.put(node.key[i], (V) node.ref[i]);
                }
            } 
            
            node = (Node) node.ref[node.nKeys];   
        }
                
        
        return tailMapHolder;
    } // tailMap

    /********************************************************************************
     * Return the portion of the B+Tree map whose keys are between fromKey and toKey,
     * i.e., fromKey <= key < toKey.
     * @return  the submap with keys in the range [fromKey, toKey)
     * @author Michael Tan
     */
    public SortedMap<K,V> subMap(K fromKey, K toKey) 
    {
    	// Will store the submap
    	SortedMap<K,V> subMapHolder = new TreeMap<>();
    	// initializes index
    	int index;
        // Determine which ref index to go down into
        Node node = root;
        while(node.isLeaf == false) 
        { 
        	index = 0;
        	for(int i = 0; i < node.nKeys; i++)
        	{
        		if(node.key[i].compareTo(fromKey) >= 0)
        		{
        			break;
        		}
        		index++;
        	}
        
        	node = (Node) node.ref[index]; 
        }
        
        boolean continueLoop = true;
        while ( continueLoop && node != null) 
        { 
            // if a key is between fromKey and toKey, add it to the map
            for (int i = 0; i < node.nKeys; i++) 
            {
            	// Checks for condition
                if(fromKey.compareTo(node.key[i]) <= 0  && node.key[i].compareTo(toKey) <= 0) 
                {
                	subMapHolder.put(node.key[i], (V) node.ref[i]);
                }
                // End loop if key is greater than toKey
                if(node.key[i].compareTo(toKey) == 1)
                {
                	continueLoop = false;
                	break;
                }
            } 
            
            node = (Node) node.ref[node.nKeys];   
        }
                
        
        return subMapHolder;
	} // subMap

    /********************************************************************************
     * Return the size (number of keys) in the B+Tree.
     * @return  the size of the B+Tree
     */
    public int size ()
    {
    	 int sum = 0;
    	
    	 // Moves down to leftmost leaf node
         Node node = root;

      	 // Moves down to leftmost leaf node
         while (node.isLeaf == false) 
         { 
         	node = (Node) node.ref[0]; 
         }
         
         while (node != null) 
         { 
        	 sum = sum + node.nKeys;
        	 node = (Node) node.ref[node.nKeys];
         }
         
         return sum;
    } // size

    /********************************************************************************
     * Print the B+Tree using a pre-order traveral and indenting each level.
     * @param n      the current node to print
     * @param level  the current level of the B+Tree
     */
    @SuppressWarnings("unchecked")
    private void print (Node n, int level)
    {
    	if (n == null)
    	{
    		return;
    	}
    	out.println ("BpTreeMap");
        out.println ("-------------------------------------------");

        for (int j = 0; j < level; j++) out.print ("\t");
        out.print ("[ . ");
        for (int i = 0; i < n.nKeys; i++) out.print (n.key [i] + " . ");
        out.println ("]");
        if ( ! n.isLeaf) {
            for (int i = 0; i <= n.nKeys; i++) print ((Node) n.ref [i], level + 1);
        } // if

        out.println ("-------------------------------------------");
    } // print

    /********************************************************************************
     * Recursive helper function for finding a key in B+trees.
     * @param key  the key to find
     * @param ney  the current node
     */
    /********************************************************************************
     * Recursive helper function for finding a key in B+trees.
     * @param key  the key to find
     * @param ney  the current node
     */
    @SuppressWarnings("unchecked")
    private V find (K key, Node node)
    {
		// Increments count
		count++;
		// Traverses the tree and searches for the key.
		for (int i = 0; i < node.nKeys; i++) 
		{
			// Looks in leaf node to check if key is present
			if (node.isLeaf) 
			{
				if (key.compareTo(node.key[i]) == -1) 
				{
					// No match is present, so it returns null
					return null;
				} 
				else if (key.compareTo(node.key[i]) == 0) 
				{ 
					return (V) node.ref[i];				
				} 
				else
				{
					continue;
				}
			}
			else
			{
				//Moves down the tree to find key
				if (i == 0 && key.compareTo(node.key[i]) < 0 || (i > 0 && key.compareTo(node.key[i-1]) >= 0 && key.compareTo(node.key[i]) < 0)) 
				{ 				
					count++;
					node = (Node) node.ref[i];	
					i = -1;
				} 
				else if (i == node.nKeys - 1) 
				{ 				
					count++;
					node = (Node) node.ref[i+1];	
					i = -1;
				}
				else
				{
					continue;
				}
			}
		}
		
		return null;
    } // find
    
    /**
     * Makes a replica of a node
     * @param n the node to be replicated
     * @return replicated node
     */
    public Node replicateNode(Node n)
    {
    	Node replica = new Node(n.isLeaf);
    	                            
        for (int i = 0; i < n.nKeys; i++) 
        {
        	replica.nKeys++;
        	replica.ref[i] = n.ref[i]; 
            replica.key[i] = n.key[i];

            if (i == n.nKeys -1 && !n.isLeaf) 
            {
            	replica.ref[replica.nKeys] = n.ref[n.nKeys];
            }
        }
       
        
    	return replica; 	
    }

    /********************************************************************************
     * Recursive helper function for inserting a key in B+trees.
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     * @param p    the parent node
     */
    private void insert (K key, V ref, Node n, Node p)
    { 
    	// Checks that duplicate key is not added
    	if(get(key) != null)
    	{
    		//out.println("BpTreeMap:insert: attempt to insert duplicate key = " + key);
    		//Resets count
    		count = 0;
            return;
    	}
    	//Resets count
    	count = 0;
    	// Keeps track of all the parent nodes
        List<Node> parentList = new ArrayList<Node>();
        
        
        // Finds the correct node to make insert
        while (!n.isLeaf) 
        {                                                  
            parentList.add(n);
            // Goes through a branch of the tree
            for (int i = 0; i < n.nKeys; i++) 
            {   
                if (key.compareTo(n.key[i]) == -1) 
                { 
                    n = (Node) n.ref[i]; 
                    break;
                }                
                else if (key.equals(n.key[i])) 
                {
                    out.println("BpTreeMap:insert: attempt to insert duplicate key = " + key);
                    return;
                }
                else if(i == n.nKeys -1)
                {
                	n = (Node) n.ref[n.nKeys];
                	break;
                }         
            }                   
        }
        
        // make insertion  into node, if possible
        if (n.nKeys < (ORDER - 1)) 
        {
            for (int i = 0; i < n.nKeys; i++)
            {
                K k_i = n.key [i];
                if (key.compareTo (k_i) < 0) 
                {
                	Node temp = n;
                	temp = (Node) n.ref[n.nKeys];
                	// Determines which wedge to do, based on whether the node to the right of n is null or not
                	if(temp == null)
                	{
                		wedge (key, ref, n, i);
                	}
                	else
                	{
                		wedge2 (key, ref, n, i);
                	}
                    return;
                }                        
            }             
            Node temp = n;
        	temp = (Node) n.ref[n.nKeys];
        	// Determines which wedge to do, based on whether the node to the right of n is null or not
        	if(temp == null)
        	{
        		wedge (key, ref, n, n.nKeys);
        	}
        	else
        	{
        		wedge2 (key, ref, n, n.nKeys);
        	}
            
            return;
        }
        
        insert2(key, ref, n, parentList, parentList.size()-1);
        
        	
    } // insert
    
    /**
     * Recursive insert function that is called by insert
     * @param key key to be inserted
     * @param ref the key reference
     * @param n Current node
     * @param p parent of the node
     * @param parentList list of parents
     * @param level what level of the parent list we are in
     */
    public void insert2(K key, V ref, Node n,  List<Node> parentList, int level)
    {           
    	
    	
        // Split node into 2/3 split
        Node sib = split (key, ref, n);
        // reassigns sibling values
        Node temp;
        Node temp2;
    	
    	for(int refVal = 0; refVal <= sib.nKeys; refVal++)
    	{
    		try
    		{
    			
    			temp = (Node)sib.ref[refVal];
    			
    			//out.println(refVal + " is refVal");
    		}
    		
    		catch(Exception e)
    		{
    			temp = null;
    		}
    		try
    		{    			
    			temp2 = (Node)n.ref[refVal];
    		}
    		
    		catch(Exception e)
    		{
    			temp2 = null;
    		}
    		
    		boolean check = true;
    		if(temp != null)
    		{
    			if(temp.key[0] == key && !temp.isLeaf)
    			{
    				check = false;
    				//out.println("Were in final block with key " + key);
    				for (int i = 0; i < sib.nKeys-1; i++) 
                    {
                		//out.println(sib.key[i]);
                		temp.ref[i] = temp.ref[i+1];
                		temp.key[i] = temp.key[i+1];  
                        if(i == temp.nKeys - 2)
                        {
                        	temp.nKeys--;
                        	temp.ref[temp.nKeys] =  temp.ref[temp.nKeys + 1];
                        	temp.key[temp.nKeys] = null;
                        	temp.ref[temp.nKeys + 1] = null;                                
                        }
                    }     	  
    			}
    				
    		}
    		if(temp2 != null && check)
    		{
    			if(temp2.key[0] == key && !temp2.isLeaf)
    			{
    				//out.println("Were in final block with key " + key);
    				for (int i = 0; i < temp2.nKeys-1; i++) 
                    {
                		//out.println(sib.key[i]);
                		temp2.ref[i] = temp2.ref[i+1];
                		temp2.key[i] = temp2.key[i+1];  
                        if(i == temp2.nKeys - 2)
                        {
                        	temp2.nKeys--;
                        	temp2.ref[temp2.nKeys] =  temp2.ref[temp2.nKeys + 1];
                        	temp2.key[temp2.nKeys] = null;
                        	temp2.ref[temp2.nKeys + 1] = null;                                
                        }
                    }     	  
    			}
    				
    		}
    	}
       
        // enters this if there are no more parents left
        if (level < 0) 
        {
        	
        	
        	
        	
        	
        	
        	
            // sets value to node n
        	Node nodeSetter = replicateNode(n);  
            // reassigns n keys and vals
            for (int i = 0; i < n.nKeys; i++) 
            {
            	n.ref[i] = null;
                n.key[i] = null;
                if(i == n.nKeys -1)
                {
                    // connects nodes correctly
                	n.ref[n.nKeys] = null;
                    n.ref[0] = (Node) nodeSetter;
                    n.ref[1] = (Node) sib;
                    n.nKeys = 1;
                    n.key[0] = sib.key[0];    
                    n.isLeaf = false;
                    // Sets sib as sibling to n
                    
                    if (sib.isLeaf) 
                    {
                        nodeSetter.ref[nodeSetter.nKeys] = (Node) sib;
                    } 
                    else 
                    {   
                    	
                        // reassigns sibling values
                        for (int j = 0; j < sib.nKeys-1; j++) 
                        {
                        	//out.println(sib.key[j]);
                        	//out.println(sib.key[j+1]);
                            sib.ref[j] = sib.ref[j+1];
                            sib.key[j] = sib.key[j+1];                        
                        }
                        sib.nKeys--;
                        sib.key[sib.nKeys] = null;
                        sib.ref[sib.nKeys] =  sib.ref[sib.nKeys + 1];
                        sib.ref[sib.nKeys + 1] = null;
                        
                        
                    }
                }
            }
            
        } 
        // Moves key up if there is no room in parent
        else if (parentList.get(level).nKeys >= ORDER - 1) 
        {
        	
        	
        	 key = sib.key[0];
             n = parentList.get(level);
             ref = (V) sib;
             
             
             insert2(key, ref, n,  parentList, level - 1);        
        } 
        
        else 
        {   
        	
        	
        	
        	// sets wedge position           
            int wedgePosition = 0;
            while ((wedgePosition < parentList.get(level).nKeys) && (sib.key[0].compareTo(parentList.get(level).key[wedgePosition]) > 0)) 
            { 
            	wedgePosition++; 
            }     

            wedge (sib.key[0], (V)sib, parentList.get(level), wedgePosition); 
            
            // Sets the sibs keys and ref values
            if(!sib.isLeaf)
            {
            	
            	for (int i = 0; i < sib.nKeys; i++) 
                {
            		//out.println(sib.key[i]);
            		sib.ref[i] = sib.ref[i+1];
            		sib.key[i] = sib.key[i+1];  
                    if(i == sib.nKeys - 2)
                    {
                    	sib.nKeys--;
                    	sib.ref[sib.nKeys] =  sib.ref[sib.nKeys + 1];
                    	sib.key[sib.nKeys] = null;
                    	sib.ref[sib.nKeys + 1] = null;                                
                    }
                }     	                 
            }                                                                           
        }
    
    }
    
    /********************************************************************************
     * Wedge the key-ref pair into node n.
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     * @param i    the insertion position within node n
     */
    private void wedge (K key, V ref, Node n, int i)
    {                       
        for (int j = n.nKeys; j > i; j--) 
        {            
            n.key [j] = n.key [j - 1];
            if (!n.isLeaf) 
            { 
            	n.ref [j + 1] = n.ref [j]; 
            }
            else 
            { 
            	n.ref [j] = n.ref [j - 1]; 
            }
        } // for
        n.key [i] = key;
        
        // Sets reference based on if child is leaf or not
        if (n.isLeaf) 
        { 
        	n.ref [i] = ref; 
        	n.nKeys++;
        }
        else 
        { 
        	n.ref [i + 1] = ref; 
        	n.nKeys++;
        }
        
    } // wedge

    /********************************************************************************
     * Wedge the key-ref pair into node n. Used in a different situation than wedge
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     * @param i    the insertion position within node n
     */
    private void wedge2 (K key, V ref, Node n, int i)
    {    
    	 //out.println("Key = " + key);
    	 //out.println("Before");
    	 List<Node> nodesList = reconnectChildren();
    	 //out.println("After");
		 Node node = root;
		 try
		 {
			 while (node.isLeaf == false) 
		        { 
		        	node = (Node) node.ref[0]; 
		        }
			 boolean continueLoop = true;
			 while(continueLoop)
			 {
				 if(node.equals(n))
				 {
				 	continueLoop = false;
				 } 
				 node = (Node) node.ref[node.nKeys];
			 }
			  
			 	
			 	
			 	//out.println(node.key[0]);
		 }
		 catch(Exception e)
		 {
			 //out.println("catch 1");
		 }
    	
    	 for (int j = n.nKeys; j > i; j--) 
    	 {   
    		
             n.key [j] = n.key [j - 1];
             if (!n.isLeaf) 
             { 
            	 n.ref [j + 1] = n.ref [j]; 
             }
             else 
             { 
            	 n.ref [j] = n.ref [j - 1]; 
             }
         } // for
         n.key [i] = key;
         if (!n.isLeaf) 
         { 
        	 n.ref [i + 1] = ref; 
        	 n.nKeys++;
         }
         else 
         { 
        	 n.ref [i] = ref; 
        	 n.nKeys++;
         }
         
    	 if(node == null)
    	 {
    		 //out.println("It's null");
    	 }
    	 else
    	 {
    		 //out.println("Hi");
    		 for(int a = 0 ; a < nodesList.size() - 1; a++)
    		 {
    			 node = nodesList.get(a+1);
    			 n = nodesList.get(a);
    			 node.ref[node.nKeys] = (Node) n.ref[n.nKeys];
                 
                 n.ref[n.nKeys] = (Node) node;  
    			 
    			 //out.println(nodesList.get(a).key[0]);
    		 }
    		 node.ref[node.nKeys] = null;
    	 }
    } // wedge2

    /********************************************************************************
     * Split node n and return the newly created node.
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     */
    private Node split (K key, V ref, Node n)
    {                        
        // Sets half way point in a full node     
        int nodeCenter = (ORDER - 1) / 2; 
       
        int nodePosition = 0;
        // locate at what where the key will be located in the node
        while ((nodePosition < (ORDER - 1)) && key.compareTo(n.key[nodePosition]) == 1)
        { 
        	nodePosition++; 
        }        
         
        // create splitNode
        Node splitNode = new Node(n.isLeaf);

        // Splits n based on what position the key should be placed
        if(nodePosition >= nodeCenter)
        {
        	// Determine each of the vals in the split nodes
        	for (int i = nodeCenter; i < (ORDER - 1); i++) 
        	{   
            	splitNode.key[i - nodeCenter] = n.key[i];            
            	n.key[i] = null;
            
            	// Places vals in nodes based on whether node is leaf or not
            	if (n.isLeaf) 
            	{ 
            		splitNode.ref[i - nodeCenter] = n.ref[i]; 
                	n.ref[i] = null;
                	splitNode.nKeys++;
                	n.nKeys--;
            	} 
            	else 
            	{ 
            		splitNode.ref[i - nodeCenter+1] = n.ref[i + 1]; 
                	n.ref[i + 1] = null;	
                	splitNode.nKeys++;
                	n.nKeys--;
            	}        	
        	} 
        	
            nodePosition -= nodeCenter;
            Node temp = n;
            temp = (Node) n.ref[n.nKeys]; 
            // wedge into correct position
            wedge (key, ref, splitNode, nodePosition);
                   
            if (splitNode.isLeaf) 
            {        
            	// Connects the two pointers
                splitNode.ref[splitNode.nKeys] = (Node) n.ref[(ORDER - 1)];
                n.ref[n.nKeys] = (Node) splitNode;                                    
            } 
            
        }
        else
        {
        	nodeCenter = nodeCenter - 1;
        	for (int i = nodeCenter; i < (ORDER - 1); i++) 
        	{   
            	splitNode.key[i-nodeCenter] = n.key[i];            
            	n.key[i] = null;
            	// Determine each of the vals in the split nodes
            	if (n.isLeaf) 
            	{ 
            		splitNode.ref[i-nodeCenter] = n.ref[i]; 
                	n.ref[i] = null;
                	n.nKeys--;
                	splitNode.nKeys++;	
            	} 
            	else 
            	{ 
            		splitNode.ref[i + 1 - nodeCenter] = n.ref[i+1]; 
                	n.ref[i+1] = null;
                	n.nKeys--;
                	splitNode.nKeys++;               	
            	}    	
        	} 
        	// Resets val to original contents
        	nodeCenter = nodeCenter + 1;
        	Node temp = n;
        	temp = (Node) n.ref[n.nKeys];  
        	// Wedge into correct position
        	wedge (key, ref, n, nodePosition);
        	
            if (splitNode.isLeaf) 
            {   
            	// Connects the two pointers
                splitNode.ref[splitNode.nKeys] = (Node) n.ref[(ORDER - 1)];
                n.ref[n.nKeys] = (Node) splitNode;                                    
            }
        }           
                
        return splitNode;
    } // split
    
    /**
     * Creates a list of all child nodes, that will be used to "connect" them
     * in wedge2
     * @return a list of childrenNodes
     */
    public List<Node> reconnectChildren()
    {
    	Node node = root;
    	
    	 while (node.isLeaf == false) 
         { 
         	node = (Node) node.ref[0]; 
         }
    	 List<Node> nodesList = new ArrayList<Node>();
    	 
    	 Node prev = node;
    	 
    	do
    	{
    		 nodesList.add(node);
    		 
    		 prev = node;
    		 node = (Node) node.ref[node.nKeys];
    	 } while(node != null && node.equals(prev) == false);
    	 
    	 
    	 return nodesList;
    	
    }

    /********************************************************************************
     * The main method used for testing.
     * @param  the command-line arguments (args [0] gives number of keys to insert)
     */
    public static void main (String [] args)
    {
    	
        BpTreeMap <Integer, Integer> bpt = new BpTreeMap <> (Integer.class, Integer.class);
        
        int totKeys = 1000;
        if (args.length == 1) totKeys = Integer.valueOf (args [0]);
        //for (int i = 1; i < totKeys; i += 2) { bpt.put (i, i * i);} // for
        for (int i = totKeys; i > 1; i--) { bpt.put (i, i * i);} // for
        int count = 0;
        //for(int i = 0; i < totKeys; i += 2){out.println(i + " " + (totKeys-i));if(count % 2 == 0){bpt.put (i, i * i);}else{bpt.put(totKeys - i, i * i);}count++;}
        
        //int[] array = {20,19, 18,11,10,9,8,6,5,4,3,2,1};
        //int[] array = {1,2,3,4,5,6,7,8,9,10,11,111,1111,11111,111111};
        //int[] array = {1000, 800, 600, 900, 400, 200, 1200, 20000, 1 , 100,  8, 30000, 40000, 300, 1100, 5, 4};
        int[] array = {1,100,2,99,3,98,4,97,5,96,6,95,7,94,8,93,9,92,10,91,11,90,12,89,13,88,14,87,15,86,16,85,17,84,18,83,19,82,20,81,1};
        //for(int i = 0; i < array.length; i++){bpt.put(array[i], i);}
        
        
        bpt.print(bpt.root, 0);
        //bpt.prac();
        out.println(bpt.entrySet());
        //SortedMap<Integer, Integer> SubMapTest = (SortedMap<Integer, Integer>) bpt.subMap(12, 36);
        SortedMap<Integer, Integer> SubMapTest = (SortedMap<Integer, Integer>) bpt.subMap(60,80);
      	String  st = SubMapTest.toString();
      	out.println(st);
      	out.println(bpt.size());
      	out.println(bpt.firstKey());
      	out.println(bpt.lastKey());
      	
      	 //for (int i = 0; i < totKeys; i++) {out.println ("key = " + i + " value = " + bpt.get (i));} // for
      	 out.println ("-------------------------------------------");
         out.println ("Average number of nodes accessed = " + bpt.count / (double) totKeys);
      	
        
    } // main
    
    
    
    

} // BpTreeMap class
