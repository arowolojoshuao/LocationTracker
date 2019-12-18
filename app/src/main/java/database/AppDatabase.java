package database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;


@Database(entities = {Coordinate.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract MyDao myDao();

    private static AppDatabase instance;


   public static AppDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "my_database")
                    .build();

        }
        return instance;
    }
}
