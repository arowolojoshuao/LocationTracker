package database;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;


public class RoomRepository {

    private MyDao myDao;
    private Context context;
    public static AppDatabase myDatabase;
    private List<Coordinate> coordinates;


    public RoomRepository(Application application){
        AppDatabase database = AppDatabase.getInstance(application);
       myDao = database.myDao();
        coordinates = myDao.getCoordinates();


    }

    public void addCoordinate(Coordinate coordinate){
        new addCoordinateAsyncTask(myDao).execute(coordinate);
    }


    public List<Coordinate> getAllCoordinates(){
        return coordinates;
    }

    private static class addCoordinateAsyncTask extends AsyncTask<Coordinate, Void, Void>{

        private MyDao myDao;

        private addCoordinateAsyncTask(MyDao myDao){
            this.myDao = myDao;
        }

        @Override
        protected Void doInBackground(Coordinate... coordinates) {
            myDao.addCoordinate(coordinates[0]);
            return null;
        }
    }




}
