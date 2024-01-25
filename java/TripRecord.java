/**
* Class used to represent a trip record (in this case of a taxi trip)
*
* @author Name: Francesco Monti - Student Number: 300177975
*
*/

public class TripRecord {
	
	/** INSTANCE VARIABLES **/
	/**
	 * String representing the pick up date and time of the trip
	 */
	private String pickup_DateTime;

	/**
	 * GPScoord representing the GPS coordinate of the pick up location of the trip
	 */
	private GPScoord pickup_Location;

	/**
	 * GPScoord representing the GPS coordinate of the drop off location of the trip
	 */
	private GPScoord dropoff_Location;

	/**
	 * Float representing the distance of the trip
	 */
	private float trip_Distance;


	/** CONSTRUCTOR **/
	/**
	 * Constructs a TripRecord
	 * 
	 * @param pickup_DateTime is the pick up date and time of the trip
	 * @param pickup_Location is the GPS coordinate of the pick up location of the trip
	 * @param dropoff_Location is the GPS coordinate of the drop off location of the trip
	 * @param trip_Distance is the distance of the trip
	 */
	public TripRecord(String pickup_DateTime, GPScoord pickup_Location, GPScoord dropoff_Location, float trip_Distance) {
		this.pickup_DateTime = pickup_DateTime;
		this.pickup_Location = pickup_Location;
		this.dropoff_Location = dropoff_Location;
		this.trip_Distance = trip_Distance;
	}


	/** GETTERS **/
	/**
	 * @return the pick up date and time of the trip
	 */
	public String getPickUpDateTime() {
		return this.pickup_DateTime;
	}

	/**
	 * @return the GPS coordinate of the pick up location of the trip
	 */
	public GPScoord getPickUpLocation() {
		return this.pickup_Location;
	}

	/**
	 * @return the GPS coordinate of the drop off location of the trip
	 */
	public GPScoord getDropOffLocation() {
		return this.dropoff_Location;
	}

	/**
	 * @return the distance of the trip
	 */
	public float getTripDistance() {
		return this.trip_Distance;
	}
	
}