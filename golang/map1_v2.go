/**
 * Project CSI2120/CSI2520 
 * Winter 2022
 * Robert Laganiere, uottawa.ca
 * version 1.2
 * 
 * Code Modified By:
 * Student Name: Francesco Monti
 * Student Number: 300177975
 */


package main

import (
	"fmt"
	"time"
	"runtime"
	"os"
	"io"
	"strconv"
	"encoding/csv"
	"math"
	"sync"
)

type GPScoord struct {
    lat float64
	long float64
}

type LabelledGPScoord struct {
    GPScoord
	ID int     // point ID
	Label int  // cluster ID
	Status int // status of the point (Undefined = 0, Noise = 1, Cluster = 2)
}


//Partition Structure. Stores the information to run a DBSCAN: 
//A slice of LabelledGPScoord (the points to be clustered), an integer representing 
//the minimun number of points to create a cluster,the eps and offstet values
type Partition struct {
	GPScoords []LabelledGPScoord
	MinPts int
	eps float64
	offset int
}

const N int=4
const MinPts int=5
const eps float64= 0.0003
const filename string="yellow_tripdata_2009-01-15_9h_21h_clean.csv"

func main() {

    start := time.Now(); 

    gps, minPt, maxPt := readCSVFile(filename)
	fmt.Printf("Number of points: %d\n", len(gps))
	
	minPt = GPScoord{40.7, -74.}
	maxPt = GPScoord{40.8, -73.93}
	
	// geographical limits
	fmt.Printf("SW:(%f , %f)\n", minPt.lat, minPt.long)
	fmt.Printf("NE:(%f , %f) \n\n", maxPt.lat, maxPt.long)
	
	// Parallel DBSCAN STEP 1.
	incx := (maxPt.long-minPt.long)/float64(N)
	incy := (maxPt.lat-minPt.lat)/float64(N)
	
	var grid [N][N][]LabelledGPScoord  // a grid of GPScoord slices
	
	// Create the partition
	// triple loop! not very efficient, but easier to understand
	
	partitionSize:=0
    for j:=0; j<N; j++ {
        for i:=0; i<N; i++ {
		
		    for _, pt := range gps {
			
			    // is it inside the expanded grid cell
			    if (pt.long >= minPt.long+float64(i)*incx-eps) && (pt.long < minPt.long+float64(i+1)*incx+eps) && (pt.lat >= minPt.lat+float64(j)*incy-eps) && (pt.lat < minPt.lat+float64(j+1)*incy+eps) {
				
                    grid[i][j]= append(grid[i][j], pt) // add the point to this slide
					partitionSize++;
                }				
			}
	    }
	}
	
	// ***
	// This is the non-concurrent procedural version
	// It should be replaced by a producer thread that produces jobs (partition to be clustered)
	// And by consumer threads that clusters partitions
     /*for j:=0; j<N; j++ {
        for i:=0; i<N; i++ {
			
			ActualDBscan(&Partition{grid[i][j],MinPts,eps,i*10000000+j*1000000})
		    //DBscan(grid[i][j], MinPts, eps, i*10000000+j*1000000)
		}
	}*/
	
	// Parallel DBSCAN STEP 2.
	// Apply DBSCAN on each partition
	// ...
	

	//Channel for sending partitions
	partitions := make(chan Partition,N*N)

	//For synchronisation
	var waitGroup sync.WaitGroup
	waitGroup.Add(4)

	//Number of Consumer Threads
	for k := 0; k < 4; k++ {
		go DBscan(partitions,&waitGroup)
	}

	//Sends partitions into the channel
	for j:=0; j < N; j++ {
		for i:=0; i < N; i++ {
			partitions <- Partition{grid[i][j],MinPts,eps,i*10000000+j*1000000}
			//time.Sleep(time.Second) // wasting time
			//fmt.Println("Partition Sent")
		}
	}

	close(partitions)
	waitGroup.Wait() //Wait for consumers to be done
	
	// Parallel DBSCAN step 3.
	// merge clusters
	// *DO NOT PROGRAM THIS STEP
	
	end := time.Now();
    fmt.Printf("\nExecution time: %s of %d points\n", end.Sub(start), partitionSize)
    fmt.Printf("Number of CPUs: %d", runtime.NumCPU())
}


// Applies DBSCAN algorithm on LabelledGPScoord points
// LabelledGPScoord: the slice of LabelledGPScoord points
// MinPts, eps: parameters for the DBSCAN algorithm
// offset: label of first cluster (also used to identify the cluster)
// returns number of clusters found

//DBscan acts as the consumer function... makes a call to ActualDBscan that takes charge of running the actual DBSCAN algorithm
func DBscan(partitions chan Partition, wg *sync.WaitGroup) {
	for {
		partition,more := <- partitions

		if more {
			//fmt.Println("new dbscan started")
			ActualDBscan(&partition)
			time.Sleep(time.Second)
		} else {
			//fmt.Println("DONE")
			wg.Done()
			return
		}
	}
}

// Applies DBSCAN algorithm on a Partition
// Partition includes:
// A slice of LabelledGPScoord points
// The 2 parameters for the DBSCAN algorithm (MinPts, eps)
// The label label of first cluster, also used to identify the cluster (offset)
// Returns the number of clusters found
func ActualDBscan(partition *Partition) (nclusters int) {

	nclusters = 0
	for a := 0; a < len(partition.GPScoords); a++ {

		coord := &partition.GPScoords[a]

		//for testing... shows in general how far the algortihm has gone and scanned the points
		//fmt.Printf("dbscan main for loop ran: %d/%d times\n",a+1,len(partition.GPScoords))

		if coord.Status == 0 { // 0 = Undefined

			neighbors := RangeQuery(partition,coord)
			if len(neighbors) < partition.MinPts {
				coord.Status = 1 // 1 = Noise
			}


			if coord.Status != 1 {

				nclusters++ //New cluster found
				for b := 0; b < len(neighbors); b++ {

					newCoord := neighbors[b]

					if newCoord.Status == 1 {
						newCoord.Status = 2 // 2 = Cluster
						newCoord.Label = partition.offset + nclusters
					} else if newCoord.Status == 0 {
						newCoord.Status = 2
						newCoord.Label = partition.offset + nclusters

						seeds := RangeQuery(partition,newCoord)
						
						if len(seeds) >= partition.MinPts {

							for k := 0; k < len(seeds); k++ {
								coordToMerge := seeds[k]

								if contains(neighbors,coordToMerge) == false {
									neighbors = append(neighbors,coordToMerge)
								}
							}
						}
					}
				} 
			}
		}
	}

	// End of DBscan function
	// Printing the result (do not remove)
    fmt.Printf("Partition %10d : [%4d,%6d]\n", partition.offset, nclusters, len(partition.GPScoords))
    return nclusters
}

// Find the neighbors of a given LabelledGPScoord point
func RangeQuery(partition *Partition,coord *LabelledGPScoord) []*LabelledGPScoord {
	neighbors := make([]*LabelledGPScoord,0)

	for i := 0; i < len(partition.GPScoords); i++ {
		otherCoord := &partition.GPScoords[i]

		if (otherCoord != coord) {
			distance := calculateDistance(coord.GPScoord,otherCoord.GPScoord)

			if  distance <= partition.eps {
				neighbors = append(neighbors,otherCoord)		
			}
		}	
	}
	return neighbors
}

// Calculate the distance between two GPScoord
func calculateDistance(coord GPScoord, other GPScoord) (distance float64) {
	return math.Sqrt( math.Pow((other.long - coord.long),2) + math.Pow((other.lat - coord.lat),2) )
}

// Return true if slice of LabelledGPScoord points contains a particular LabelledGPScoord point, false otherwise
func contains(list []*LabelledGPScoord, coordToSearch *LabelledGPScoord) (bool) {
	for i := 0; i < len(list); i++ {
		coord := list[i]
		if coord == coordToSearch {
			return true
		}
	}
	return false
}

// reads a csv file of trip records and returns a slice of the LabelledGPScoord of the pickup locations  
// and the minimum and maximum GPS coordinates
func readCSVFile(filename string) (coords []LabelledGPScoord, minPt GPScoord, maxPt GPScoord) {

    coords= make([]LabelledGPScoord, 0, 5000)

    // open csv file
    src, err := os.Open(filename)
	defer src.Close()
    if err != nil {
        panic("File not found...")
    }
	
	// read and skip first line
    r := csv.NewReader(src)
    record, err := r.Read()
    if err != nil {
        panic("Empty file...")
    }

    minPt.long = 1000000.
    minPt.lat = 1000000.
    maxPt.long = -1000000.
    maxPt.lat = -1000000.
	
	var n int=0
	
    for {
        // read line
        record, err = r.Read()

        // end of file?
        if err == io.EOF {
            break
        }

        if err != nil {
             panic("Invalid file format...")
        }
		
		// get lattitude
		lat, err := strconv.ParseFloat(record[9], 64)
        if err != nil {
             panic("Data format error (lat)...")
        }

        // is corner point?
		if lat>maxPt.lat {
		    maxPt.lat= lat
		}		
		if lat<minPt.lat {
		    minPt.lat= lat
		}
		
		// get longitude
		long, err := strconv.ParseFloat(record[8], 64)
        if err != nil {
             panic("Data format error (long)...")
        }
		
        // is corner point?
		if long>maxPt.long {
		    maxPt.long= long
		}
		
		if long<minPt.long {
		    minPt.long= long
		}

        // add point to the slice
		n++
        pt:= GPScoord{lat,long}
        coords= append(coords, LabelledGPScoord{pt,n,0,0})
    }

    return coords, minPt,maxPt
}