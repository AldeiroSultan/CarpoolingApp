package com.carpoolingapp.models;

// File: CarpoolingApp/app/src/main/java/com/carpooling/app/models/Ride.java
public class Ride {
    private String rideId;
    private String driverId;
    private String driverName;
    private String fromLocation;
    private String toLocation;
    private double fromLat;
    private double fromLng;
    private double toLat;
    private double toLng;
    private String date;
    private String time;
    private int availableSeats;
    private double pricePerSeat;
    private String status;
    private long createdAt;
    private long updatedAt;
    private String rideType;

    // NEW: Amenities
    private boolean allowsLuggage;
    private boolean allowsPets;
    private boolean allowsBikes;
    private boolean allowsSnowboards;

    public Ride() {
    }

    public Ride(String driverId, String driverName, String fromLocation, String toLocation,
                double fromLat, double fromLng, double toLat, double toLng,
                String date, String time, int availableSeats, double pricePerSeat, String rideType) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.fromLat = fromLat;
        this.fromLng = fromLng;
        this.toLat = toLat;
        this.toLng = toLng;
        this.date = date;
        this.time = time;
        this.availableSeats = availableSeats;
        this.pricePerSeat = pricePerSeat;
        this.rideType = rideType;
        this.status = "active";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.allowsLuggage = false;
        this.allowsPets = false;
        this.allowsBikes = false;
        this.allowsSnowboards = false;
    }

    // Getters and Setters
    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getFromLocation() { return fromLocation; }
    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }

    public String getToLocation() { return toLocation; }
    public void setToLocation(String toLocation) { this.toLocation = toLocation; }

    public double getFromLat() { return fromLat; }
    public void setFromLat(double fromLat) { this.fromLat = fromLat; }

    public double getFromLng() { return fromLng; }
    public void setFromLng(double fromLng) { this.fromLng = fromLng; }

    public double getToLat() { return toLat; }
    public void setToLat(double toLat) { this.toLat = toLat; }

    public double getToLng() { return toLng; }
    public void setToLng(double toLng) { this.toLng = toLng; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }



    public double getPricePerSeat() { return pricePerSeat; }
    public void setPricePerSeat(double pricePerSeat) { this.pricePerSeat = pricePerSeat; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public String getRideType() { return rideType; }
    public void setRideType(String rideType) { this.rideType = rideType; }

    // Amenities
    public boolean isAllowsLuggage() { return allowsLuggage; }
    public void setAllowsLuggage(boolean allowsLuggage) { this.allowsLuggage = allowsLuggage; }

    public boolean isAllowsPets() { return allowsPets; }
    public void setAllowsPets(boolean allowsPets) { this.allowsPets = allowsPets; }

    public boolean isAllowsBikes() { return allowsBikes; }
    public void setAllowsBikes(boolean allowsBikes) { this.allowsBikes = allowsBikes; }

    public boolean isAllowsSnowboards() { return allowsSnowboards; }
    public void setAllowsSnowboards(boolean allowsSnowboards) { this.allowsSnowboards = allowsSnowboards; }
}