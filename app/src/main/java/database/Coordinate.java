package database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "coordinates")
public class Coordinate {

    @PrimaryKey
    private int id;

    @ColumnInfo(name = "position")
    private String position;


    @ColumnInfo (name = "latitude")
    private double latitude;

    @ColumnInfo (name = "longitude")
    private double longitude;



    public Coordinate(int id, String position, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.position = position;
        this.longitude = longitude;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
