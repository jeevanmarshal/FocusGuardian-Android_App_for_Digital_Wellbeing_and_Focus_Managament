package com.focusguardian.data.local.dao

import androidx.room.*
import com.focusguardian.data.local.entity.BlockingProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM blocking_profiles")
    fun getAllProfiles(): Flow<List<BlockingProfile>>
    
    @Query("SELECT * FROM blocking_profiles WHERE id = :id")
    suspend fun getProfileById(id: Int): BlockingProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BlockingProfile): Long

    @Update
    suspend fun updateProfile(profile: BlockingProfile)

    @Delete
    suspend fun deleteProfile(profile: BlockingProfile)
}
