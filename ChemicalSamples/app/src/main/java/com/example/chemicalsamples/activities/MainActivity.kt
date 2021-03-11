package com.example.chemicalsamples.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chemicalsamples.R
import com.example.chemicalsamples.adapters.ChemicalPlacesAdapter
import com.example.chemicalsamples.database.DatabaseHandler
import com.example.chemicalsamples.models.SampleChemistry
import com.example.chemicalsamples.utils.SwipeToDeleteCallback
import com.example.chemicalsamples.utils.SwipeToEditCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fabAddChemicalPlace.setOnClickListener {
            val intent = Intent(this@MainActivity, AddSampleChemistryActivity::class.java)
            startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        getChemicalPlacesListFromLocalDB()
    }


        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)

            if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    getChemicalPlacesListFromLocalDB()
                } else {
                    Log.e("Actividad", "Cancelado")
                }
            }
        }


        private fun getChemicalPlacesListFromLocalDB() {

            val dbHandler = DatabaseHandler(this)

            val getChemistPlacesList = dbHandler.getChemistPlacesList()

            if (getChemistPlacesList.size > 0) {
                rv_chemist_places_list.visibility = View.VISIBLE
                tv_no_records_available.visibility = View.GONE
                setupChemistPlacesRecyclerView(getChemistPlacesList)
            } else {
                rv_chemist_places_list.visibility = View.GONE
                tv_no_records_available.visibility = View.VISIBLE
            }
        }


        private fun setupChemistPlacesRecyclerView(chemistPlacesList: ArrayList<SampleChemistry>) {

            rv_chemist_places_list.layoutManager = LinearLayoutManager(this)
            rv_chemist_places_list.setHasFixedSize(true)

            val chemicalPlacesAdapter = ChemicalPlacesAdapter(this, chemistPlacesList)
            rv_chemist_places_list.adapter = chemicalPlacesAdapter

            chemicalPlacesAdapter.setOnClickListener(object :
                    ChemicalPlacesAdapter.OnClickListener {
                override fun onClick(position: Int, model: SampleChemistry) {
                    val intent = Intent(this@MainActivity, SampleChemistryDetailActivity::class.java)
                    intent.putExtra(EXTRA_PLACE_DETAILS, model)
                    startActivity(intent)
                }
            })

            val editSwipeHandler = object : SwipeToEditCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter = rv_chemist_places_list.adapter as ChemicalPlacesAdapter
                    adapter.notifyEditItem(
                            this@MainActivity,
                            viewHolder.adapterPosition,
                            ADD_PLACE_ACTIVITY_REQUEST_CODE
                    )
                }
            }
            val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
            editItemTouchHelper.attachToRecyclerView(rv_chemist_places_list)


            val deleteSwipeHandler = object : SwipeToDeleteCallback(this) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                    val adapter = rv_chemist_places_list.adapter as ChemicalPlacesAdapter
                    adapter.removeAt(viewHolder.adapterPosition)

                    getChemicalPlacesListFromLocalDB() // Gets the latest list from the local database after item being delete from it.

                }
            }
            val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
            deleteItemTouchHelper.attachToRecyclerView(rv_chemist_places_list)

        }



companion object{
    private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
    internal const val EXTRA_PLACE_DETAILS = "extra_place_details"
}
}