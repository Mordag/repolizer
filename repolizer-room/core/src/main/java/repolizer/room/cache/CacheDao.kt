package repolizer.room.cache

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.*

@Dao
interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg cacheItem: CacheItem)

    @Query("SELECT * FROM cache_table WHERE url = :url")
    fun getCache(url: String): LiveData<CacheItem>

    @Delete
    fun delete(vararg cacheItem: CacheItem)
}