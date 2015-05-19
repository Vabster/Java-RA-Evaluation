import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Tests the time it takes to perform joins, range selects, and point
 * selects using a B+ Tree
 */
public class BPTreeTimes
{
	// Creates the tables that will be used for the tests
	private BPTreeTable StudentTable1000 = new BPTreeTable("Student", "id name address status", "Integer String String String", "id");
	private BPTreeTable StudentTable2000 = new BPTreeTable("Student", "id name address status", "Integer String String String", "id");
	private BPTreeTable StudentTable5000 = new BPTreeTable("Student", "id name address status", "Integer String String String", "id");
	private BPTreeTable StudentTable10000 = new BPTreeTable("Student", "id name address status", "Integer String String String", "id");
	private BPTreeTable StudentTable50000 = new BPTreeTable("Student", "id name address status", "Integer String String String", "id");
	
	private BPTreeTable TranscriptTable1000 = new BPTreeTable("Transcript", "studId crsCode semester grade", "Integer String String String", "studId crsCode semester");
	private BPTreeTable TranscriptTable2000 = new BPTreeTable("Transcript", "studId crsCode semester grade", "Integer String String String", "studId crsCode semester");
	private BPTreeTable TranscriptTable5000 = new BPTreeTable("Transcript", "studId crsCode semester grade", "Integer String String String", "studId crsCode semester");
	private BPTreeTable TranscriptTable10000 = new BPTreeTable("Transcript", "studId crsCode semester grade", "Integer String String String", "studId crsCode semester");
	private BPTreeTable TranscriptTable50000 = new BPTreeTable("Transcript", "studId crsCode semester grade", "Integer String String String", "studId crsCode semester");
	
	// Used to store tables created using relational algebra operators
	// Used in order to make changes to the above tables
	private BPTreeTable tempTable;
	
	// Used to store times for a particular query
	private double[] timesArray = new double[12];
	
	/**
	 * Prints the average time for 12 runs of a particular query
	 * and returns the standard deviation which will be printed
	 * @param num: how many tuples are in the tuple
	 * @return The standard deviation
	 */
	public void standardDeviation(int num)
	{
		// Initializes average time to zero
		double averageTime = 0;
		// Adds up all values from the timesArray
		for(int i = 0; i < timesArray.length; i++)
		{		
			averageTime += timesArray[i];
		}
		System.out.println(num + ": Total Time of 12 queries " + averageTime / (double) 1000000 + " ms");
		// Sets averageTime to the average time
		averageTime = averageTime / timesArray.length;
		// Prints out the average time
		System.out.println(num + ": Average Time of 12 queries " + averageTime / (double) 1000000 + " ms");
		
		// Initializes variance to 0
		double variance = 0;
		// Sets variance to the addition of all the times subtracted by the average time squared
		for(int i = 0; i < timesArray.length; i++)
		{
			timesArray[i] = timesArray[i] - averageTime;
			timesArray[i] = timesArray[i] * timesArray[i];
			variance += timesArray[i];
		}
		
		// Divides variance by the the length of the timesArray
		variance = variance / timesArray.length;
		// Finds standard deviation
		variance = Math.sqrt(variance);
		
		// returns standard deviation
		System.out.println(num + ": Standard Deviation of 12 queries " + variance / (double) 1000000 + " ms");
	}

	/**
	 * Runs the queries and prints out the times
	 * @param args
	 */
	public static void main(String[] args) 
	{
		// Creates object that will run queries and produce times
		BPTreeTimes timePrinter = new BPTreeTimes();
		// Fills up the Student and Transcript tables
		timePrinter.fillTables();
		// Prints the join times
		timePrinter.joinIndexedTimes();
		// Prints the join times
		timePrinter.joinIndexedTimes();
		// Prints the point select times
		timePrinter.pointSelectIndexedTimes();
		// Prints the range select times
		timePrinter.rangeSelectIndexedTimes();
		// Creates files that store the index of StudentTable5000 and TranscriptTable5000
		
		timePrinter.printTableIndex();
	}

	/**
	 * For each of the table sizes
	 * 		Prints the run time of 12 joins
	 * 		Prints the average run time of 12 joins
	 * 		Prints the standard deviation 
	 * For B+ Tree Index Joins
	 */
	public void joinIndexedTimes() {
		System.out.println("B+ Tree: Index Join Times");

		this.joinIndexed(1000);
		this.standardDeviation(1000);
		System.out.println();
		
		this.joinIndexed(2000);
		this.standardDeviation(2000);
		System.out.println();

		this.joinIndexed(5000);
		this.standardDeviation(5000);
		System.out.println();

		this.joinIndexed(10000);
		this.standardDeviation(10000);
		System.out.println();

		this.joinIndexed(50000);
		this.standardDeviation(50000);
		System.out.println();
	}
	
	/**
	 * For each of the table sizes
	 * 		Prints the run time of 12 joins
	 * 		Prints the average run time of 12 joins
	 * 		Prints the standard deviation 
	 * For B+ Tree Point Selects
	 */
	public void pointSelectIndexedTimes() {
		System.out.println("B+ Tree: Point select times");
		
		this.pointSelectIndexed(1000);
		this.standardDeviation(1000);
		System.out.println();
		
		this.pointSelectIndexed(2000);
		this.standardDeviation(2000);
		System.out.println();

		this.pointSelectIndexed(5000);
		this.standardDeviation(5000);
		System.out.println();

		this.pointSelectIndexed(10000);
		this.standardDeviation(10000);
		System.out.println();

		this.pointSelectIndexed(50000);
		this.standardDeviation(50000);
		System.out.println();

		System.out.println();
	}

	/**
	 * For each of the table sizes
	 * 		Prints the run time of 12 joins
	 * 		Prints the average run time of 12 joins
	 * 		Prints the standard deviation 
	 * For B+ Tree Range Selects
	 */
	public void rangeSelectIndexedTimes() {
		
		System.out.println("B+ Tree: Range select times");
		this.rangeSelectIndexed(1000);
		this.standardDeviation(1000);
		System.out.println();
		
		this.rangeSelectIndexed(2000);
		this.standardDeviation(2000);
		System.out.println();

		this.rangeSelectIndexed(5000);
		this.standardDeviation(5000);
		System.out.println();

		this.rangeSelectIndexed(10000);
		this.standardDeviation(10000);
		System.out.println();

		this.rangeSelectIndexed(50000);
		this.standardDeviation(50000);
		System.out.println();

		System.out.println();
	}

	private void joinIndexed(int num)
	{
		double startTime;
		double endTime;
		switch(num)
		{
			case 1000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = TranscriptTable1000.indexedJoin("studId",  "id",  StudentTable1000);	
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			case 2000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = TranscriptTable2000.indexedJoin("studId",  "id",  StudentTable2000);	
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			case 5000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = TranscriptTable5000.indexedJoin("studId",  "id",  StudentTable5000);	
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			case 10000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = TranscriptTable10000.indexedJoin("studId",  "id",  StudentTable10000);	
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			case 50000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = TranscriptTable50000.indexedJoin("studId",  "id",  StudentTable50000);	
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			default:
				break;
		}
	}
	
	private void pointSelectIndexed(int num)
	{
		double startTime;
		double endTime;
		switch(num)
		{
			case 1000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = StudentTable1000.select(new KeyType(i));
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			case 2000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = StudentTable2000.select(new KeyType(i));
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			case 5000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = StudentTable5000.select(new KeyType(i));
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			case 10000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = StudentTable10000.select(new KeyType(i));
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			case 50000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = StudentTable50000.select(new KeyType(i));
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			default:
				break;
		}
	}
	
	private void rangeSelectIndexed(int num)
	{
		double startTime;
		double endTime;
		switch(num)
		{
			case 1000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = StudentTable1000.select(t -> t[StudentTable1000.col("id")].compareTo(40000) >= 0
						&& t[StudentTable1000.col("id")].compareTo(42000) < 0);
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			case 2000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = StudentTable2000.select(t -> t[StudentTable2000.col("id")].compareTo(40000) >= 0
						&& t[StudentTable2000.col("id")].compareTo(42000) < 0);
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			case 5000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = StudentTable5000.select(t -> t[StudentTable5000.col("id")].compareTo(40000) >= 0
						&& t[StudentTable5000.col("id")].compareTo(42000) < 0);
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			case 10000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = StudentTable10000.select(t -> t[StudentTable10000.col("id")].compareTo(40000) >= 0
						&& t[StudentTable10000.col("id")].compareTo(42000) < 0);
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			case 50000:
				for(int i = 0; i < 12; i++)
				{
					startTime= System.nanoTime();
					tempTable = StudentTable50000.select(t -> t[StudentTable50000.col("id")].compareTo(40000) >= 0
						&& t[StudentTable50000.col("id")].compareTo(42000) < 0);
					endTime = System.nanoTime();
					timesArray[i] = endTime - startTime;
				}
				break;
			default:
				break;
		}
	}
	

	/**
	 * Fills the tables with data using the TupleGenerator class
	 */
	private void fillTables()
	{
		// Creates a TupleGenerator object that will fill up the student and transcript tables
		TupleGenerator test = new TupleGeneratorImpl();

		test.addRelSchema("Student", "id name address status", "Integer String String String", "id", null);
		test.addRelSchema("Transcript", "studId crsCode semester grade", "Integer String String String", "studId crsCode semester", null);
		Comparable[][][] resultTest = test.generate(new int[]{50000, 50000});

		// Insert values into Student tables
		for (int i = 0; i < resultTest[0].length; i++) 
		{
			if(i >= 10000)
			{
				StudentTable50000.insert(resultTest[0][i]);
			}
			else if(i >= 5000)
			{
				StudentTable50000.insert(resultTest[0][i]);
				StudentTable10000.insert(resultTest[0][i]);
			}
			else if(i >= 2000)
			{
				StudentTable50000.insert(resultTest[0][i]);
				StudentTable10000.insert(resultTest[0][i]);
				StudentTable5000.insert(resultTest[0][i]);
			}
			else if(i >= 1000)
			{
				StudentTable50000.insert(resultTest[0][i]);
				StudentTable10000.insert(resultTest[0][i]);
				StudentTable5000.insert(resultTest[0][i]);
				StudentTable2000.insert(resultTest[0][i]);
			}
			else
			{
				StudentTable50000.insert(resultTest[0][i]);
				StudentTable10000.insert(resultTest[0][i]);
				StudentTable5000.insert(resultTest[0][i]);
				StudentTable2000.insert(resultTest[0][i]);
				StudentTable1000.insert(resultTest[0][i]);
			}
		}
		
		// Insert data into Transcript tables
		for (int i = 0; i < resultTest[1].length; i++) 
		{
			if(i >= 10000)
			{
				TranscriptTable50000.insert(resultTest[1][i]);
			}
			else if(i >= 5000)
			{
				TranscriptTable50000.insert(resultTest[1][i]);
				TranscriptTable10000.insert(resultTest[1][i]);
			}
			else if(i >= 2000)
			{
				TranscriptTable50000.insert(resultTest[1][i]);
				TranscriptTable10000.insert(resultTest[1][i]);
				TranscriptTable5000.insert(resultTest[1][i]);
			}
			else if(i >= 1000)
			{
				TranscriptTable50000.insert(resultTest[1][i]);
				TranscriptTable10000.insert(resultTest[1][i]);
				TranscriptTable5000.insert(resultTest[1][i]);
				TranscriptTable2000.insert(resultTest[1][i]);
			}
			else
			{
				TranscriptTable50000.insert(resultTest[1][i]);
				TranscriptTable10000.insert(resultTest[1][i]);
				TranscriptTable5000.insert(resultTest[1][i]);
				TranscriptTable2000.insert(resultTest[1][i]);
				TranscriptTable1000.insert(resultTest[1][i]);
			}
		}
	}
	
	public void printTableIndex() 
	{
		try 
		{
			// Creates a Path variable
			Path directoryPath = Paths.get(System.getProperty("user.dir"));
			// Creates files for the indexes of the Student and Transcript tables
			System.setOut(new PrintStream(new FileOutputStream(directoryPath.resolve("B+Tree-StudentTable50000.txt").toFile())));
			StudentTable50000.printIndex();
			System.setOut(new PrintStream(new FileOutputStream(directoryPath.resolve("B+Tree-TranscriptTable50000.txt").toFile())));
			TranscriptTable50000.printIndex();
		} 
		catch (Exception e) 
		{
			System.out.println("Could not print structures");
		}
	}
	
}
