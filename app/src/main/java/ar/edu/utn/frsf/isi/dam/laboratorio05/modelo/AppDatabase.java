package ar.edu.utn.frsf.isi.dam.laboratorio05.modelo;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Reclamo.class},version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ReclamoDao reclamoDao();
}
