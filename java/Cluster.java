import java.util.*;

/**
* Class used to represent a cluster composed of points (in this case TripRecord(s))
*
* @author Name: Francesco Monti - Student Number: 300177975
*
*/

public class Cluster {

	/** INSTANCE VARIABLES **/
	/**
	 * Integer representing the number of points (TripRecord(s)) in the cluster
	 */
	private int numberOfPoints;

	/**
	 * List of points (TripRecord(s)) present in the cluster
	 */
	private LinkedList<TripRecord> points;

	/**
	 * GPScoord representing the average value of the GPS coordinates of the cluster's point (TripRecord) set
	 */
	private GPScoord position;
	
	
	/** CONSTRUCTORS **/
	/**
	 * Constructs an empty Cluster
	 */
	public Cluster () {
		points = new LinkedList<>();
	}

	/**
	 * Constructs a Cluster
	 * 
	 * @param numberOfPoints is the number of points (TripRecord(s)) present in the cluster
	 * @param points is a list of points (TripRecord(s)) of the cluster
	 * @param position is the average value of the GPS coordinates of the cluster's point (TripRecord) set
	 */
	public Cluster (int numberOfPoints,LinkedList<TripRecord> points,GPScoord position) {
		this.numberOfPoints = numberOfPoints;
		this.points = points;
		this.position = position;
	}

	/** GETTERS **/
	/**
	 * @return the number of points present in the cluster
	 */
	public int getNumberOfPoints() {
		return this.numberOfPoints;
	}

	/**
	 * @return the average value of the GPS coordinates of the cluster's point (TripRecord) set
	 */
	public GPScoord getPosition() {
		return this.position;
	}	

	/**
	 * @return the list of points (TripRecord(s)) of the cluster
	 */
	public LinkedList<TripRecord> getPoints() {
		return this.points;
	}


	/** SETTERS **/
	/**
	 * @param numberOfPoints is the number of points present in the cluster
	 */
	public void setNumberOfPoints(int numberOfPoints) {
		this.numberOfPoints = numberOfPoints;
	}

	/**
	 * @param position is the average value of the GPS coordinates of the cluster's point (TripRecord) set
	 */
	public void setPosition(GPScoord position) {
		this.position = position;
	}

	/**
	 * @param points is the list of points (TripRecord(s)) of the cluster
	 */
	public void setPoints(LinkedList<TripRecord> points) {
		this.points = points;
	}


	/** OTHER METHODS **/
	/**
	 * Allows to add a single point (TripRecord) to the list of points of the cluster
	 * 
	 * @param point is the point (TripRecord) to be added
	 */
	public void addSinglePoint(TripRecord point) {
		this.points.add(point);
	}

	/**
	 * Calculates the average value of the GPS coordinates of the cluster's point (TripRecord) set 
	 * Then, it sets it as the cluster's instance variable value
	 */
	public void calculateAndSetPosition() {
		double longitude = 0;
		double latitude = 0;
		int counter = 0;

		//Sum all the longitudes and latitude
		//Calculate average longitude and latitude (counter is the number of GPScoord treated)
		//Creates new GPScoord with average values
		for (int i = 0; i < this.getPoints().size(); i++) {
			GPScoord current = this.getPoints().get(i).getPickUpLocation();
			longitude = longitude + current.getLongitude();
			latitude = latitude + current.getLatitude();
			counter++;
		}

		this.setPosition(new GPScoord((longitude/counter),(latitude/counter)));
	}
}