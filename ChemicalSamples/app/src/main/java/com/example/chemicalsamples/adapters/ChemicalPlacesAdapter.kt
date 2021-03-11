package com.example.chemicalsamples.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chemicalsamples.R
import com.example.chemicalsamples.activities.AddSampleChemistryActivity
import com.example.chemicalsamples.activities.MainActivity
import com.example.chemicalsamples.database.DatabaseHandler
import com.example.chemicalsamples.models.SampleChemistry
import kotlinx.android.synthetic.main.item_chemical_place.view.*

open class ChemicalPlacesAdapter(
    private val context: Context,
    private var list: ArrayList<SampleChemistry>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_chemical_place,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.iv_place_image.setImageURI(Uri.parse(model.image))
            holder.itemView.tvTitle.text = model.title
            holder.itemView.tvValue.text = model.valueResult
            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context, AddSampleChemistryActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(
                intent,
                requestCode
        )

        notifyItemChanged(position)
    }


    fun removeAt(position: Int) {

        val dbHandler = DatabaseHandler(context)
        val isDeleted = dbHandler.deleteChemistPlace(list[position])

        if (isDeleted > 0) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }


    interface OnClickListener {
        fun onClick(position: Int, model: SampleChemistry)
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
