package database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MyDao {

    @Insert
     void addCoordinate(Coordinate coordinate);

    @Query("select * from coordinates")
     List<Coordinate> getCoordinates();
}
