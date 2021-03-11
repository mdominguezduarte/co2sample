package com.example.chemicalsamples.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.nfc.FormatException
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.chemicalsamples.R
import com.example.chemicalsamples.database.DatabaseHandler
import com.example.chemicalsamples.models.SampleChemistry
import com.example.chemicalsamples.utils.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_sample_chemistry.*
import kotlinx.android.synthetic.main.activity_add_sample_chemistry.iv_place_image
import kotlinx.android.synthetic.main.item_chemical_place.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddSampleChemistryActivity : AppCompatActivity(), View.OnClickListener {


    private var cal = Calendar.getInstance()

    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var saveImageToInternalStorage: Uri? = null

    private var mLatitude: Double = 0.0
    private var mLongitude: Double = 0.0
    private var valueResult: String = "0.0"
    private var valueExplanation: String = "Test"
    private var sampleChemistryDetails: SampleChemistry? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_sample_chemistry)

        setSupportActionBar(toolbar_add_sample)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_add_sample.setNavigationOnClickListener {
            onBackPressed()
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!Places.isInitialized()) {
            Places.initialize(
                this@AddSampleChemistryActivity,
                resources.getString(R.string.google_maps_api_key)
            )
        }

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            sampleChemistryDetails =
                    intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as SampleChemistry
        }


        dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                updateDateInView()
            }

        updateDateInView()

        if (sampleChemistryDetails != null) {
            supportActionBar?.title = "Edit Chemist Place"

            et_title.setText(sampleChemistryDetails!!.title)
            et_sample_1.setText(sampleChemistryDetails!!.sample_1)
            et_sample_2.setText(sampleChemistryDetails!!.sample_2)
            et_sample_3.setText(sampleChemistryDetails!!.sample_3)
            et_sample_time_1.setText(sampleChemistryDetails!!.sample_time_1)
            et_sample_time_2.setText(sampleChemistryDetails!!.sample_time_2)
            et_sample_time_3.setText(sampleChemistryDetails!!.sample_time_3)
            et_date.setText(sampleChemistryDetails!!.date)
            et_location.setText(sampleChemistryDetails!!.location)
            mLatitude = sampleChemistryDetails!!.latitude
            mLongitude = sampleChemistryDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(sampleChemistryDetails!!.image)

            iv_place_image.setImageURI(saveImageToInternalStorage)

            btn_save.text = "UPDATE"
        }

        et_date.setOnClickListener(this)
        tv_add_image.setOnClickListener(this)
        btn_save.setOnClickListener(this)
        et_location.setOnClickListener(this)
        tv_select_current_location.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddSampleChemistryActivity,
                    dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            R.id.tv_add_image -> {
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems =
                    arrayOf("Select photo from gallery", "Capture photo from camera")
                pictureDialog.setItems(
                    pictureDialogItems
                ) { dialog, which ->
                    when (which) {
                        0 -> choosePhotoFromGallery()
                        1 -> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }
            R.id.et_location -> {
                try {
                    val fields = listOf(
                        Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                            .build(this@AddSampleChemistryActivity)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            R.id.tv_select_current_location -> {

                if (!isLocationEnabled()) {
                    Toast.makeText(
                        this,
                        "Your location provider is turned off. Please turn it on.",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {

                    Dexter.withActivity(this)
                        .withPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        .withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                if (report!!.areAllPermissionsGranted()) {
                                    requestNewLocationData()
                                }
                            }

                            override fun onPermissionRationaleShouldBeShown(
                                permissions: MutableList<PermissionRequest>?,
                                token: PermissionToken?
                            ) {
                                showRationalDialogForPermissions()
                            }
                        }).onSameThread()
                        .check()
                }
            }

            R.id.btn_save -> {

                when {
                    et_title.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Introduce un título", Toast.LENGTH_SHORT).show()
                    }
                    et_sample_1.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Introduce muestra uno", Toast.LENGTH_SHORT)
                            .show()
                    }
                    et_sample_2.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Introduce muestra dos", Toast.LENGTH_SHORT)
                            .show()
                    }
                    et_sample_3.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Introduce muestra tres", Toast.LENGTH_SHORT)
                            .show()
                    }
                    et_sample_time_1.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Introduce tiempo muestra uno", Toast.LENGTH_SHORT)
                            .show()
                    }
                    et_sample_time_2.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Introduce tiempo muestra dos", Toast.LENGTH_SHORT)
                            .show()
                    }
                    et_sample_time_3.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Introduce tiempo muestra tres", Toast.LENGTH_SHORT)
                            .show()
                    }
                    et_location.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Introduce lugar", Toast.LENGTH_SHORT)
                            .show()
                    }
                    et_date.text.isNullOrEmpty() -> {
                        Toast.makeText(this, "Introduce fecha", Toast.LENGTH_SHORT)
                            .show()
                    }
                    saveImageToInternalStorage == null -> {
                        Toast.makeText(this, "Añade una imagen", Toast.LENGTH_SHORT).show()
                    }
                    else -> {

                        valueResult = "%.1f".format(calculateResult())
                        valueExplanation = valueExplanation()
                        val sampleChemistry = SampleChemistry(
                            if (sampleChemistryDetails == null) 0 else sampleChemistryDetails!!.id,
                            et_title.text.toString(),
                            saveImageToInternalStorage.toString(),
                            et_sample_1.text.toString(),
                            et_sample_2.text.toString(),
                            et_sample_3.text.toString(),
                            et_sample_time_1.text.toString(),
                            et_sample_time_2.text.toString(),
                            et_sample_time_3.text.toString(),
                            valueResult,
                            valueExplanation,
                            et_date.text.toString(),
                            et_location.text.toString(),
                            mLatitude,
                            mLongitude
                        )


                        val dbHandler = DatabaseHandler(this)

                        if (sampleChemistryDetails == null) {

                            val addSampleChemistry = dbHandler.addSampleChemistry(sampleChemistry)

                            if (addSampleChemistry > 0) {
                                setResult(Activity.RESULT_OK)
                                finish();
                            }
                        } else {
                            val updateChemistPlace = dbHandler.updateChemistPlace(sampleChemistry)

                            if (updateChemistPlace > 0) {
                                setResult(Activity.RESULT_OK);
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        @Suppress("DEPRECADO")
                        val selectedImageBitmap =
                            MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)

                        saveImageToInternalStorage =
                            saveImageToInternalStorage(selectedImageBitmap)
                        Log.e("Imagen guardada : ", "Path :: $saveImageToInternalStorage")

                        iv_place_image!!.setImageBitmap(selectedImageBitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@AddSampleChemistryActivity,
                            "Falló!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
            } else if (requestCode == CAMERA) {

                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap // Bitmap from camera

                saveImageToInternalStorage =
                    saveImageToInternalStorage(thumbnail)
                Log.e("Imagen guardada : ", "Path :: $saveImageToInternalStorage")

                iv_place_image!!.setImageBitmap(thumbnail) // Set to the imageView.

            } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {

                val place: Place = Autocomplete.getPlaceFromIntent(data!!)

                et_location.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Cancelado", "Cancelado")
        }
    }


    private fun updateDateInView() {
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())
    }


    private fun choosePhotoFromGallery() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                    if (report!!.areAllPermissionsGranted()) {

                        val galleryIntent = Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )

                        startActivityForResult(galleryIntent, GALLERY)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread()
            .check()
    }


    private fun takePhotoFromCamera() {

        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    // Here after all the permission are granted launch the CAMERA to capture an image.
                    if (report!!.areAllPermissionsGranted()) {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread()
            .check()
    }


    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("Establece los requerimientos en configuración")
            .setPositiveButton(
                "ir a configuración"
            ) { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("empaquetado", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancelado") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }

    private fun calculateResult(): Double {

            var  sample_1 = et_sample_1.text.toString().toDouble()
            var  sample_2 = et_sample_2.text.toString().toDouble()
            var  sample_3 = et_sample_3.text.toString().toDouble()
            var  sample_time_1 = et_sample_time_1.text.toString().toDouble()
            var  sample_time_2 = et_sample_time_2.text.toString().toDouble()
            var  sample_time_3 = et_sample_time_3.text.toString().toDouble()

            var numeradorHoras = ((sample_1 * sample_time_1) + (sample_2 * sample_time_2)+(sample_3 * sample_time_3))/ 60
            var resultado = numeradorHoras / 8


            return resultado

        }







    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri {

        val wrapper = ContextWrapper(applicationContext)

        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)

        file = File(file, "${UUID.randomUUID()}.jpg")

        try {

            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            mLatitude = mLastLocation.latitude
            Log.e("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")
            val addressTask =
                GetAddressFromLatLng(this@AddSampleChemistryActivity, mLatitude, mLongitude)

            addressTask.setAddressListener(object :
                GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    Log.e("Address ::", "" + address)
                    et_location.setText(address)
                }

                override fun onError() {
                    Log.e("Obtener dirección ::", "Algo ha fallado...")
                }
            })

            addressTask.getAddress()
            // END
        }
    }
    /*Evaluate the  valueresult */
    private fun valueExplanation():String{

        return if (valueResult <= "1000") {
            "Genial! Revisar en 7 días"

        } else if (valueResult < "2500") {
            "Bien! Revisar en 3 días"

        } else if (valueResult < "5000") {
            "Revisar en dos días"

        } else if (valueResult >= "5000") {
            "Revisar dentro de las 24 horas"

        } else {
            "Parece qué no es un dato válido"
        }
    }




    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
        private const val IMAGE_DIRECTORY = "ChemicalSamples"

    }
}