package com.example.chemicalsamples.activities

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chemicalsamples.R
import com.example.chemicalsamples.models.SampleChemistry
import kotlinx.android.synthetic.main.activity_sample_chemistry_detail.*
import kotlinx.android.synthetic.main.activity_sample_chemistry_detail.iv_place_image
import kotlinx.android.synthetic.main.item_chemical_place.*

class SampleChemistryDetailActivity : AppCompatActivity() {

    private lateinit var sampleChemistryDetailModel: SampleChemistry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_chemistry_detail)

        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)) {
            sampleChemistryDetailModel =
                intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as SampleChemistry
        }

        if (sampleChemistryDetailModel != null) {

            setSupportActionBar(toolbar_chemist_place_detail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = sampleChemistryDetailModel.title

            toolbar_chemist_place_detail.setNavigationOnClickListener {
                onBackPressed()
            }

            iv_place_image.setImageURI(Uri.parse(sampleChemistryDetailModel.image))
            tv_resultValue.text = sampleChemistryDetailModel.valueResult
            tv_location.text = sampleChemistryDetailModel.location
            tv_resultExplanation.text = sampleChemistryDetailModel.valueExplanation
        }

        setColorResult()

        btn_view_on_map.setOnClickListener {

            val intent = Intent(this@SampleChemistryDetailActivity, MapsCo2Samples::class.java)

            intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, sampleChemistryDetailModel)
            startActivity(intent)

        }
    }

    private fun setColorResult() {

        return if (sampleChemistryDetailModel.valueResult.toDouble() >= 5000) {
            tv_resultValue.setTextColor(Color.parseColor("#EF3939"))

        } else {
            tv_resultValue.setTextColor(Color.parseColor("#CDDC39"))
        }
    }


}



