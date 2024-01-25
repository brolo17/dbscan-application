import java.util.*;
import java.io.*;

/**
* Class that implements the DBSCAN algorithm to cluster the various taxi trip records 
* This is done by using the GPS coordinates of the starting points
*
* @author Name: Francesco Monti - Student Number: 300177975
*
*/

public class TaxiCluster  {

	/** ENUM **/
	/**
	 * Enum used to represent the status of a specific trip record
	 */
	private enum Status {NOISE,CLUSTER}


	/** INSTANCE VARIABLES **/
	/**
	 * Integer representing the minimum number of points necessary to build a cluster
	 */
	private int minPts;

	/**
	 * Double representing the minimum distance between two points in order for them to be considered neighbours
	 */
	private double eps;

	/**
	 * List representing the different clusters
	 */
	private LinkedList<Cluster> clusters;

	/**
	 * List representing the taxi trip records that are going to be scanned by the dbscan algorithm
	 */
	private LinkedList<TripRecord> tripRecords;

	/**
	 * Map used to store the status of every taxi trip record 
	 */
	private Map<TripRecord,Status> tripRecordsStatus;

	
	/** CONSTRUCTOR **/
	/**
	 * Constructs a TaxiCluster
	 * 
	 * @param tripRecords is the list representing the taxi trip records that are going to be scanned
	 * @param minPts is the minimum number of points necessary to build a cluster
	 * @param eps is the minimum distance between two points in order for them to be considered neighbours
	 */
	public TaxiCluster(LinkedList<TripRecord> tripRecords,int minPts,double eps) {
		this.tripRecords = tripRecords;
		this.minPts = minPts;
		this.eps = eps;

		this.clusters = new LinkedList<>();
		this.tripRecordsStatus = new HashMap<TripRecord,Status>();	
	}

	/** GETTERS **/
	/**
	 * @return the minimum number of points necessary to build a cluster
	 */
	public int getMinPts() {
		return this.minPts;
	}	

	/**
	 * @return the minimum distance between two points in order for them to be considered neighbours
	 */
	public double getEps() {
		return this.eps;
	}	

	/**
	 * @return the different clusters associated to the TaxiCluster object
	 */
	public LinkedList<Cluster> getClusters() {
		return this.clusters;
	}	

	/**
	 * @return the list representing the taxi trip records that are scanned
	 */
	public LinkedList<TripRecord> getTripRecords() {
		return this.tripRecords;
	}	


	/** OTHER METHODS **/
	/**
	 * Implementation of the DBSCAN algortihm
	 */
	public void dbscan() {
		for (int i = 0; i < this.tripRecords.size(); i++) {

			//for testing... shows in general how far the algortihm has gone and scanned the points
			System.out.println("dbscan main for loop ran: " + (i+1) + "/" + this.tripRecords.size() + " times"); 
			TripRecord currentTrip = tripRecords.get(i);

			//If TripRecord status is either "NOISE" or "CLUSTER" skip
			if (this.tripRecordsStatus.get(currentTrip) != null) {
				continue;
			}

			//Gets the neighbours of the TripRecord that is currently scanned
			//Then check if it contains enough points to create a cluster
			LinkedList<TripRecord> n = rangeQuery(currentTrip);

			if (n.size() >= this.minPts) {

				//Enough points to create a cluster
				//Search every eligible point going throgh all the neighbours and neighbours of neighbours... and so on
				//Then create the cluster
				int k = 0;
				while (k < n.size()) {

					TripRecord currentTrip2 = n.get(k);

					//If TripRecord status is either not "NOISE" nor "CLUSTER", search for its neighbours
					//If there are enough points, merge the "new" neighbours to the "old" one
					//If not, just continue
					if (this.tripRecordsStatus.get(currentTrip2) == null) {
						LinkedList<TripRecord> n2 = rangeQuery(currentTrip2);
						if (n2.size() >= minPts) {
							for (int x = 0; x < n2.size(); x++) {
								if (!n.contains(n2.get(x))) {
									n.add(n2.get(x));
								}
							}
						}
					}

					//If TripRecord status is "NOISE", sets it to "CLUSTER"
					if (this.tripRecordsStatus.get(currentTrip2) != Status.CLUSTER) {
						this.tripRecordsStatus.put(currentTrip2,Status.CLUSTER);
					}

					k++;

				}

				//Creates a new cluster and initialize all of its instance variables
				//Adds it to the "this.clusters" instace variables
				Cluster newCluster = new Cluster();
				newCluster.setPoints(n);
				newCluster.addSinglePoint(currentTrip);
				newCluster.setNumberOfPoints(newCluster.getPoints().size());
				newCluster.calculateAndSetPosition();
				this.clusters.add(newCluster);

			} 
			else {
				//Not enough points to create a cluster
				//Sets the status of the TripRecord that is currently scanned to "NOISE"
				this.tripRecordsStatus.put(currentTrip,Status.NOISE);
			}
			
		}
	}

	/**
	 * Creates a new CSV file with the given name
	 * Specifies on it, for each cluster, its position and the number of points it contains
	 * 
	 * @param fileName is the name of the CSV file to be created
	 */
	public void printClustersToCSV (String fileName) throws IOException {

		FileWriter writer = new FileWriter(fileName);
		StringBuilder sb = new StringBuilder();

		String attributesLine = "CLUSTER#,NUMBER OF POINTS,POSITION LONGITUDE,POSITION LATITUDE";
		sb.append(attributesLine);
		sb.append('\n');

		for (int i = 0; i < this.getClusters().size(); i++) {
			String line = (i+1) + "," + 
			String.valueOf(this.getClusters().get(i).getNumberOfPoints()) + "," + 
			String.valueOf(this.getClusters().get(i).getPosition().getLongitude()) + "," +
			String.valueOf(this.getClusters().get(i).getPosition().getLatitude());
			sb.append(line);
			sb.append('\n');
		}

		writer.write(sb.toString());
		writer.close();
	}


	/** PRIVATE METHODS **/
	/**
	 * @param trip is the TripRecord for which it is desired to find its neighbours
	 * 
	 * @return a list containing the neighbours of the given TripRecord
	 */
	private LinkedList<TripRecord> rangeQuery(TripRecord trip) {
		LinkedList<TripRecord> n = new LinkedList<>();

		for (int i = 0; i < this.tripRecords.size(); i++) {
			TripRecord other = this.tripRecords.get(i);

			if (other != trip) {
				if (trip.getPickUpLocation().getDistance(other.getPickUpLocation()) <= this.eps) {
					n.add(other);
				}
			}
		}
		return n;
	}


	/** MAIN METHOD**/
	public static void main(String[] args) throws Exception { 
		Scanner scanner = new Scanner(System.in);

		System.out.println("Enter the name of the file: ");
		String file_name = scanner.nextLine().trim();

		int minPts;
		System.out.println("Enter the value of 'minPts: ");
		try {
			minPts = scanner.nextInt();
		} catch (InputMismatchException ex) {
			throw new InputMismatchException("A InputMismatchException occureed! Insert an integer for minPts");
		} catch (IllegalStateException excep) {
			throw new IllegalStateException("An IllegalStateException occurred!");
		} catch (Exception exception) {
			throw new Exception("An exception occurred!");
		}
		 
		double eps;
		System.out.println("Enter the value of 'eps': ");
		try {
			eps = scanner.nextDouble();
		} catch (InputMismatchException ex) {
			throw new InputMismatchException("A InputMismatchException occureed! Insert a double for eps");
		} catch (IllegalStateException excep) {
			throw new IllegalStateException("An IllegalStateException occurred!");
		} catch (Exception exception) {
			throw new Exception("An exception occurred!");
		}

		CSVReader reader;
		try {
			reader = new CSVReader(file_name);
		} catch(FileNotFoundException ex) {
			throw new FileNotFoundException("A FileNotFoundException occurred! Make sure the file exists in the directory");
		} catch (IOException excep) {
			throw new IOException("An IOException occurred!");
		} catch (IllegalStateException except) {
			throw new IllegalStateException("An IllegalStateException occurred!");
		} catch (Exception exception) {
			throw new Exception("An exception occurred!");
		}

		scanner.nextLine();
		System.out.println("Enter the name of the output CSV file (include the '.csv' extension: ");
		String out_fileName = scanner.nextLine().trim();

		scanner.close();

		//Get PickUp Times
		String[] pickUpTimes = reader.getAttributeValues("Trip_Pickup_DateTime");
		
		//Get Pick Up Latitudes and Longitudes
		String[] pickUpLon = reader.getAttributeValues("Start_Lon");
		String[] pickUpLat = reader.getAttributeValues("Start_Lat");
		double[] pickUpLongitudes = new double[pickUpLon.length];
		double[] pickUpLatitudes = new double[pickUpLat.length];

		//Get Drop Off Latitudes and Longitudes
		String[] dropOffLon = reader.getAttributeValues("End_Lon");
		String[] dropOffLat = reader.getAttributeValues("End_Lat");
		double[] dropOffLongitudes = new double[dropOffLon.length];
		double[] dropOffLatitudes = new double[dropOffLat.length];

		for (int i = 0; i < pickUpLat.length; i++) {
			pickUpLongitudes[i] = Double.valueOf(pickUpLon[i]);
			pickUpLatitudes[i] = Double.valueOf(pickUpLat[i]);
			dropOffLongitudes[i] = Double.valueOf(dropOffLon[i]);
			dropOffLatitudes[i] = Double.valueOf(dropOffLat[i]);
		}
		
		//Create Pick Up and Drop Off GPSCoords
		GPScoord[] pickUp_gpsCoordinates = new GPScoord[pickUpLat.length];
		GPScoord[] dropOff_gpsCoordinates = new GPScoord[pickUpLat.length];
		for (int i = 0; i < pickUp_gpsCoordinates.length; i++) {
			pickUp_gpsCoordinates[i] = new GPScoord(pickUpLongitudes[i],pickUpLatitudes[i]);
			dropOff_gpsCoordinates[i] = new GPScoord(dropOffLongitudes[i],dropOffLatitudes[i]);
		}
		
		//Get Distances
		String[] distances = reader.getAttributeValues("Trip_Distance");
		float[] trip_distances = new float[distances.length];

		for (int i = 0; i < trip_distances.length; i++) {
			trip_distances[i] = Float.valueOf(distances[i]);
		}

		//Create Trip Records
		LinkedList<TripRecord> trip_records = new LinkedList<>();
		for (int i = 0; i < pickUp_gpsCoordinates.length; i++) {
			trip_records.add(new TripRecord(pickUpTimes[i],pickUp_gpsCoordinates[i],dropOff_gpsCoordinates[i],trip_distances[i]));
		}

		/*LinkedList<TripRecord> trip_records2 = new LinkedList<>();
		for (int i = 0; i < 7000; i++) {
			trip_records2.add(trip_records.get(i));
		} */

		//Create TaxiCluster object, calls dbscan() algorithm method, and printClusterToCSV() to create output file
		TaxiCluster taxi_cluster = new TaxiCluster(trip_records,minPts,eps);
		taxi_cluster.dbscan();

		System.out.println("Total Number of Clusters: " + taxi_cluster.getClusters().size());

		try {
			taxi_cluster.printClustersToCSV(out_fileName);	
		} catch (IOException ex) {
			throw new IOException("An IOException occurred while writing the output file!");
		} 
		
	}
}
