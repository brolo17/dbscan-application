/**
* Class used to represent a GPS coordinate by its longitude and latitude values
*
* @author Name: Francesco Monti - Student Number: 300177975
*
*/

public class GPScoord {
	
	/** INSTANCE VARIABLES **/
	/**
	 * Double representing the longitude of the GPS coordinates
	 */
	private double longitude;
	
	/**
	 * Double representing the latitude of the GPS coordinates
	 */
	private double latitude;

	/** CONSTRUCTOR **/
	/**
	 * Constructs a GPS coordinate
	 * 
	 * @param longitude is the value of the longitude
	 * @param latitude is the value of the latitude
	 */
	public GPScoord(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}

	/** GETTERS **/
	/**
	 * @return the longitude of the GPS coordinate
	 */
	public double getLongitude() {
		return this.longitude;
	}

	/**
	 * @return the latitude of the GPS coordinate
	 */
	public double getLatitude() {
		return this.latitude;
	}

	/** OTHER METHODS **/
	/**
	 * @param other is a GPScoord 
	 * 
	 * @return the distance between two GPS coordinates (this and other)
	 */
	public double getDistance(GPScoord other) {
		if (other == null) {
			throw new NullPointerException("Parameter GPScoord other cannot be null");
		}
		return (Math.sqrt( Math.pow((other.longitude - this.longitude),2) + Math.pow((other.latitude - this.latitude),2) ));
	}

}