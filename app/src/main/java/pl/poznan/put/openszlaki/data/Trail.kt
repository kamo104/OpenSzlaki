package pl.poznan.put.openszlaki.data

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "trails")
data class Trail(
    @PrimaryKey(autoGenerate = true) var uid: Int = 0,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "imagePaths") var imagePaths: MutableList<Uri>,
    @ColumnInfo(name = "measurements") var measurements: MutableList<Measurement>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Trail
        if (uid != other.uid) return false
        if (name != other.name) return false
        if (imagePaths == other.imagePaths) return false
        if (measurements == other.measurements) return false
        return true
    }

    override fun hashCode(): Int {
        var result = uid
        result = 31 * result + name.hashCode()
        result = 31 * result + imagePaths.hashCode()
        result = 31 * result + measurements.hashCode()
        return result
    }
}

@Dao
interface TrailDao {
    @Query("SELECT * FROM trails")
    suspend fun getAll(): List<Trail>

    @Query("SELECT * FROM trails ORDER BY uid")
    fun getSortedFlow(): Flow<List<Trail>>

    @Query("SELECT * FROM trails")
    fun getFlow(): Flow<List<Trail>>

    @Query("SELECT * FROM trails")
    fun getLive(): LiveData<List<Trail>>

    @Query("SELECT * FROM trails WHERE name = :inName")
    suspend fun get(inName:String): Trail

    @Query("SELECT * FROM trails WHERE uid = :inUid")
    suspend fun get(inUid:Int): Trail

    @Query("SELECT COUNT(*) FROM trails")
    suspend fun getCount(): Int

    @Insert
    suspend fun insertAll(vararg trails: Trail)

    @Delete
    suspend fun delete(trail: Trail)

    @Update
    suspend fun updateTrail(trail: Trail)
}

