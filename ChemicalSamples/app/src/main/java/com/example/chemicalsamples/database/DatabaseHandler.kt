package com.example.chemicalsamples.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import com.example.chemicalsamples.models.SampleChemistry

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "SampleChemistryDatabase"
        private const val TABLE_SAMPLE_CHEMISTRY = "SampleChemistry"

        private const val KEY_ID = "_id"
        private const val KEY_TITLE = "title"
        private const val KEY_IMAGE = "image"
        private const val KEY_SAMPLE_1 = "sample_one"
        private const val KEY_SAMPLE_2 = "sample_two"
        private const val KEY_SAMPLE_3 = "sample_three"
        private const val KEY_TIME_1 = "sample_time_one"
        private const val KEY_TIME_2 = "sample_time_two"
        private const val KEY_TIME_3 = "sample_time_three"
        private const val KEY_VALUE_RESULT = "valueResult"
        private const val KEY_VALUE_EXPLANATION = "valueExplanation"
        private const val KEY_DATE = "date"
        private const val KEY_LOCATION = "location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
    }

    override fun onCreate(db: SQLiteDatabase?) {

        val createsamplechemistry = ("CREATE TABLE " + TABLE_SAMPLE_CHEMISTRY + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_TITLE + " TEXT,"
                + KEY_IMAGE + " TEXT,"
                + KEY_SAMPLE_1 + " TEXT,"
                + KEY_SAMPLE_2 + " TEXT,"
                + KEY_SAMPLE_3 + " TEXT,"
                + KEY_TIME_1 + " TEXT,"
                + KEY_TIME_2 + " TEXT,"
                + KEY_TIME_3 + " TEXT,"
                + KEY_VALUE_RESULT + " TEXT,"
                + KEY_VALUE_EXPLANATION + " TEXT,"
                + KEY_DATE + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT)")
        db?.execSQL(createsamplechemistry)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_SAMPLE_CHEMISTRY")
        onCreate(db)
    }

    fun addSampleChemistry(sampleChemistry: SampleChemistry): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_TITLE, sampleChemistry.title)
        contentValues.put(KEY_IMAGE, sampleChemistry.image)
        contentValues.put(KEY_SAMPLE_1, sampleChemistry.sample_1)
        contentValues.put(KEY_SAMPLE_2, sampleChemistry.sample_2)
        contentValues.put(KEY_SAMPLE_3, sampleChemistry.sample_3)
        contentValues.put(KEY_TIME_1, sampleChemistry.sample_time_1)
        contentValues.put(KEY_TIME_2, sampleChemistry.sample_time_2)
        contentValues.put(KEY_TIME_3, sampleChemistry.sample_time_3)
        contentValues.put(KEY_VALUE_RESULT, sampleChemistry.valueResult)
        contentValues.put(KEY_VALUE_EXPLANATION, sampleChemistry.valueExplanation)
        contentValues.put(KEY_DATE, sampleChemistry.date)
        contentValues.put(KEY_LOCATION, sampleChemistry.location)
        contentValues.put(KEY_LATITUDE, sampleChemistry.latitude)
        contentValues.put(KEY_LONGITUDE, sampleChemistry.longitude)

        val result = db.insert(TABLE_SAMPLE_CHEMISTRY, null, contentValues)

        db.close()
        return result
    }


    fun getChemistPlacesList(): ArrayList<SampleChemistry> {

        val chemistPlaceList: ArrayList<SampleChemistry> = ArrayList()

        val selectQuery = "SELECT  * FROM $TABLE_SAMPLE_CHEMISTRY"


        val db = this.readableDatabase

        try {
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                do {
                    val place = SampleChemistry(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                        cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(KEY_SAMPLE_1)),
                        cursor.getString(cursor.getColumnIndex(KEY_SAMPLE_2)),
                        cursor.getString(cursor.getColumnIndex(KEY_SAMPLE_3)),
                        cursor.getString(cursor.getColumnIndex(KEY_TIME_1)),
                        cursor.getString(cursor.getColumnIndex(KEY_TIME_2)),
                        cursor.getString(cursor.getColumnIndex(KEY_TIME_3)),
                        cursor.getString(cursor.getColumnIndex(KEY_VALUE_RESULT)),
                        cursor.getString(cursor.getColumnIndex(KEY_VALUE_EXPLANATION)),
                        cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                    )
                    chemistPlaceList.add(place)

                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLiteException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return chemistPlaceList
    }
    fun updateChemistPlace(sampleChemistry: SampleChemistry): Int {

        val db = this.writableDatabase
        val contentValues = ContentValues()

        contentValues.put(KEY_TITLE, sampleChemistry.title)
        contentValues.put(KEY_IMAGE, sampleChemistry.image)
        contentValues.put(KEY_SAMPLE_1, sampleChemistry.sample_1)
        contentValues.put(KEY_SAMPLE_2, sampleChemistry.sample_2)
        contentValues.put(KEY_SAMPLE_3, sampleChemistry.sample_3)
        contentValues.put(KEY_TIME_1, sampleChemistry.sample_time_1)
        contentValues.put(KEY_TIME_2, sampleChemistry.sample_time_3)
        contentValues.put(KEY_TIME_3, sampleChemistry.sample_time_3)
        contentValues.put(KEY_VALUE_RESULT, sampleChemistry.valueResult)
        contentValues.put(KEY_VALUE_EXPLANATION, sampleChemistry.valueExplanation)
        contentValues.put(KEY_DATE, sampleChemistry.date)
        contentValues.put(KEY_LOCATION, sampleChemistry.location)
        contentValues.put(KEY_LATITUDE, sampleChemistry.latitude)
        contentValues.put(KEY_LONGITUDE, sampleChemistry.longitude)

        val success = db.update(TABLE_SAMPLE_CHEMISTRY, contentValues, KEY_ID + "=" + sampleChemistry.id, null)

        db.close()

        return success
    }


    fun deleteChemistPlace(sampleChemistry: SampleChemistry): Int {

        val db = this.writableDatabase
        val success = db.delete(TABLE_SAMPLE_CHEMISTRY, KEY_ID + "=" + sampleChemistry.id, null)

        db.close()

        return success
    }

}
