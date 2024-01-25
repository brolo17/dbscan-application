import java.io.File;
import java.util.*;

/**
* Class used to read a CSV file and store its values in order to be able to manipulate them later
*
* @author Name: Francesco Monti - Student Number: 300177975
* Class previosuly created by my self for an ITI1121 assignment
* Now modified and adapted to the needs of this project 
*
*/

public class CSVReader {

	/** INSTANCE VARIABLES **/
	/**
	 * Charcater representing the delimiter that separate values in a CSV file
	 */
	private static final char DELIMITER = ',';

	/**
	 * Charcater allowing an escape for values that contains the delimiter
	 */
	private static final char QUOTE_MARK = '\'';

	/**
	 * Integer representing the number of columns of a CSV file
	 */
	private int numColumns;

	/**
	 * Integer representing the number of rows of a CSV file
	 */
	private int numRows;

	/**
	 * String representing the name of a CSV file
	 */
	private String filePath;

	/**
	 * String array containing the names of the attributes of a CSV file
	 */
	private String[] attributeNames;

	/**
	 * 2D string array containing the values of a CSV file
	 */
	private String[][] matrix;


	/** CONSTRUCTOR **/
	/**
	 * Construct a CSVReader object containing the values read from a CSV file
	 * 
	 * @param filePath is the name of the CSV file
	 */
	public CSVReader(String filePath) throws Exception {

		this.filePath = filePath;
		calculateDimensions();
		attributeNames = new String[numColumns];
		matrix = new String[numRows][numColumns];
		instantiateFromFile();
	}


	/** GETTERS **/
	/**
	 * @return a string representing the name of the CSV file
	 */
	public String getSourceId() {
		return filePath;
	}

	/**
	 * @return an integer representing the number of columns of the CSV file
	 */
	public int getNumberOfColumns() {
		return numColumns;
	}

	/**
	 * @return an integer representing the number of rows of the CSV file
	 */
	public int getNumberOfDataRows() {
		return numRows;
	}

	/**
	 * @return a string array containing the names of the attributes of the CSV file
	 */
	public String[] getAttributeNames() {
		return attributeNames; 
	}

	/**
	 * @return a 2D string array containing the values of the CSV file
	 */
	public String[][] getData() {
		return matrix;
	}


	/** OTHER METHODS **/
	/**
	 * Returns a string array containing the values specific to a particular attribute
	 * 
	 * @param attributeName is the name of the attribute for which the values have to be returned
	 * @return a string array containing the values of the specific attribute
	 */
	public String[] getAttributeValues(String attributeName) {
		if (attributeName == null) {
			return null;
		}

		for (int i = 0; i < numColumns; i++) {
			if (attributeNames[i].equals(attributeName)) {
				return getColumnValues(matrix,i);
			} 
		}

		return null;
	}

	
	/** PRIVATE METHODS **/
	/**
	 * Calculates the number of columns and rows of the CSV files
	 * Then, it sets them as the CSVReader's instances variables values
	 */
	private void calculateDimensions() throws Exception {

		Scanner scanner = new Scanner(new File(filePath));

		boolean firstLine = true;

		while (scanner.hasNext()) {
			String str = scanner.nextLine();

			if (!str.trim().isEmpty()) {
				if (firstLine) {
					numColumns = countColumns(str);
					firstLine = false;
				} else {
					numRows++;
				}
			}
		}

		scanner.close();
	}

	private void instantiateFromFile() throws Exception {
		Scanner scanner = new Scanner(new File(filePath));

		boolean firstLine = true;

		int rowNum = 0;

		while (scanner.hasNext()) {
			String str = scanner.nextLine();

			if (!str.trim().isEmpty()) {

				if (firstLine) {
					firstLine = false;
					populateAttributeNames(str);

				} else {
					populateRow(str, rowNum++);
				}
			}
		}

		scanner.close();
	}

	private void populateAttributeNames(String str) {

		if (str == null || str.isEmpty()) {
			return;
		}

		StringBuffer buffer = new StringBuffer();

		boolean isInQuote = false;

		int position = 0;

		char[] chars = str.toCharArray();
		char ch;

		for (int i = 0; i < chars.length; i++) {

			ch = chars[i];

			if (isInQuote) {
				if (ch == QUOTE_MARK) {
					isInQuote = false;
				} else {
					buffer.append(ch);
				}

			} else if (ch == QUOTE_MARK) {
				isInQuote = true;
			} else if (ch == DELIMITER) {
				attributeNames[position++] = buffer.toString().trim();
				buffer.delete(0, buffer.length());
			} else {
				buffer.append(ch);
			}
		}

		if (buffer.toString().trim().length() > 0) { // deal with last attribute name
			attributeNames[position++] = buffer.toString().trim();
		}

	}

	private void populateRow(String str, int currentRow) {

		if (str == null || str.isEmpty()) {
			return;
		}

		StringBuffer buffer = new StringBuffer();

		boolean isInQuote = false;

		int position = 0;

		char[] chars = str.toCharArray();
		char ch;

		for (int i = 0; i < chars.length; i++) {

			ch = chars[i];

			if (isInQuote) {
				if (ch == QUOTE_MARK) {
					isInQuote = false;
				} else {
					buffer.append(ch);
				}

			} else if (ch == QUOTE_MARK) {
				isInQuote = true;
			} else if (ch == DELIMITER) {
				matrix[currentRow][position++] = buffer.toString().trim();
				buffer.delete(0, buffer.length());
			} else {
				buffer.append(ch);
			}
		}

		if (buffer.toString().trim().length() > 0) { // deal with last attribute value
			matrix[currentRow][position++] = buffer.toString().trim();
		} else if (chars[chars.length - 1] == ',') {// deal with potentially missing last attribute value
			matrix[currentRow][position++] = "";
		}
	}

	private int countColumns(String str) {

		int count = 0;

		if (str == null || str.isEmpty()) {
			return count;
		}

		char[] chars = str.toCharArray();
		boolean isInQuote = false;
		char ch;

		for (int i = 0; i < chars.length; i++) {
			ch = chars[i];

			if (isInQuote) {
				if (ch == QUOTE_MARK) {
					isInQuote = false;
				}
			} else if (ch == QUOTE_MARK) {
				isInQuote = true;
			} else if (ch == DELIMITER) {
				count++;
			}
		}

		return count + 1;
	}

	private String[] getColumnValues(String[][] matrix, int index) {
		LinkedList<String> columnValues = new LinkedList<>();

    	for(int i = 0; i < numRows; i++){
       		columnValues.add(matrix[i][index]);
    	}

    	String[] column = new String[columnValues.size()];
    	for (int i = 0; i < column.length; i++) {
    		column[i] = columnValues.get(i);
    	}

    	return column;
	}

}