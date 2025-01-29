package com.riog.accel

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "LocationTracker.db"
        private const val DATABASE_VERSION = 2  // Increment version for schema change
        private const val TABLE_LOCATIONS = "locations"
        private const val COLUMN_ID = "id"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
        private const val COLUMN_TIMESTAMP = "timestamp"
        // New columns for street location information
        private const val COLUMN_STREET_NUMBER = "street_number"
        private const val COLUMN_STREET_NAME = "street_name"
        private const val COLUMN_CITY = "city"
        private const val COLUMN_STATE = "state"
        private const val COLUMN_POSTAL_CODE = "postal_code"
        private const val COLUMN_COUNTRY = "country"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """CREATE TABLE $TABLE_LOCATIONS (
            $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COLUMN_LATITUDE REAL NOT NULL,
            $COLUMN_LONGITUDE REAL NOT NULL,
            $COLUMN_TIMESTAMP INTEGER NOT NULL,
            $COLUMN_STREET_NUMBER TEXT,
            $COLUMN_STREET_NAME TEXT,
            $COLUMN_CITY TEXT,
            $COLUMN_STATE TEXT,
            $COLUMN_POSTAL_CODE TEXT,
            $COLUMN_COUNTRY TEXT
        )"""
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add new columns for street location information
            val upgradeQuery = """
                ALTER TABLE $TABLE_LOCATIONS 
                ADD COLUMN $COLUMN_STREET_NUMBER TEXT;
                ALTER TABLE $TABLE_LOCATIONS 
                ADD COLUMN $COLUMN_STREET_NAME TEXT;
                ALTER TABLE $TABLE_LOCATIONS 
                ADD COLUMN $COLUMN_CITY TEXT;
                ALTER TABLE $TABLE_LOCATIONS 
                ADD COLUMN $COLUMN_STATE TEXT;
                ALTER TABLE $TABLE_LOCATIONS 
                ADD COLUMN $COLUMN_POSTAL_CODE TEXT;
                ALTER TABLE $TABLE_LOCATIONS 
                ADD COLUMN $COLUMN_COUNTRY TEXT;
            """
            db.execSQL(upgradeQuery)
        }
    }

    fun insertLocation(
        latitude: Double, 
        longitude: Double, 
        streetNumber: String? = null,
        streetName: String? = null,
        city: String? = null,
        state: String? = null,
        postalCode: String? = null,
        country: String? = null
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LATITUDE, latitude)
            put(COLUMN_LONGITUDE, longitude)
            put(COLUMN_TIMESTAMP, System.currentTimeMillis())
            put(COLUMN_STREET_NUMBER, streetNumber)
            put(COLUMN_STREET_NAME, streetName)
            put(COLUMN_CITY, city)
            put(COLUMN_STATE, state)
            put(COLUMN_POSTAL_CODE, postalCode)
            put(COLUMN_COUNTRY, country)
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
                
                // Retrieve new street location columns
                val streetNumber = getString(getColumnIndexOrThrow(COLUMN_STREET_NUMBER))
                val streetName = getString(getColumnIndexOrThrow(COLUMN_STREET_NAME))
                val city = getString(getColumnIndexOrThrow(COLUMN_CITY))
                val state = getString(getColumnIndexOrThrow(COLUMN_STATE))
                val postalCode = getString(getColumnIndexOrThrow(COLUMN_POSTAL_CODE))
                val country = getString(getColumnIndexOrThrow(COLUMN_COUNTRY))

                locations.add(
                    LocationEntry(
                        id,
                        lat,
                        lon,
                        timestamp,
                        streetNumber,
                        streetName,
                        city,
                        state,
                        postalCode,
                        country
                    )
                )
            }
        }
        cursor.close()
        return locations
    }

    fun deleteAllLocations(): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_LOCATIONS, null, null)
    }

    fun dropLocationsTable() {
        val db = this.writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOCATIONS")
        onCreate(db)  // Recreate the table with the current schema
    }
}

data class LocationEntry(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val streetNumber: String? = null,
    val streetName: String? = null,
    val city: String? = null,
    val state: String? = null,
    val postalCode: String? = null,
    val country: String? = null
)