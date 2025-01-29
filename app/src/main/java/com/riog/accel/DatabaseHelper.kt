package com.riog.accel

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "LocationTracker.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_LOCATIONS = "locations"
        private const val COLUMN_ID = "id"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """CREATE TABLE $TABLE_LOCATIONS (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_LATITUDE REAL NOT NULL,
            $COLUMN_LONGITUDE REAL NOT NULL,
            $COLUMN_TIMESTAMP INTEGER NOT NULL
        )"""
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCATIONS")
        onCreate(db)
    }

    fun insertLocation(latitude: Double, longitude: Double): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LATITUDE, latitude)
            put(COLUMN_LONGITUDE, longitude)
            put(COLUMN_TIMESTAMP, System.currentTimeMillis())
        }
        return db.insert(TABLE_LOCATIONS, null, values)
    }

    fun getAllLocations(): List<LocationEntry> {
        val locations = mutableListOf<LocationEntry>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_LOCATIONS, 
            null, 
            null, 
            null, 
            null, 
            null, 
            "$COLUMN_TIMESTAMP DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COLUMN_ID))
                val lat = getDouble(getColumnIndexOrThrow(COLUMN_LATITUDE))
                val lon = getDouble(getColumnIndexOrThrow(COLUMN_LONGITUDE))
                val timestamp = getLong(getColumnIndexOrThrow(COLUMN_TIMESTAMP))
                locations.add(LocationEntry(id, lat, lon, timestamp))
            }
        }
        cursor.close()
        return locations
    }
}

data class LocationEntry(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)