package com.akhil.bitfit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var bitfitRV: RecyclerView
    private lateinit var bitFitAdapter: BitFitAdapter
    private val bitfits = mutableListOf<BitFit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bitfitRV = findViewById(R.id.bitFitRV)
        bitFitAdapter = BitFitAdapter(this, bitfits)
        bitfitRV.adapter = bitFitAdapter
        bitfitRV.layoutManager = LinearLayoutManager(this).also {
            val dividerItemDecorator = DividerItemDecoration(this, it.orientation)
            bitfitRV.addItemDecoration(dividerItemDecorator)
        }

        lifecycleScope.launch{
            (application as BitFitApplication).db.bitFitDao().getAll().collect{
                databaseList -> databaseList.map { entity ->
                BitFit(
                    entity.foodName,
                    entity.calorieCount
                )
            }.also { mappedList ->
                bitfits.clear()
                bitfits.addAll(mappedList)
                bitFitAdapter.notifyDataSetChanged()
                }
            }
        }

        val addFood = findViewById<Button>(R.id.addNewFoodButton)
        addFood.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, ActivityBitfitInput::class.java)
            startActivityForResult(intent, 1)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1)
        {
            if(resultCode == RESULT_OK)
            {
                //data updated in the recycler view
                val bitFitResult = data?.getSerializableExtra("result") as BitFit
                bitfits.add(bitFitResult)
                bitFitAdapter.notifyDataSetChanged()
                //data added to the DB
                lifecycleScope.launch(IO){
                    (application as BitFitApplication).db.bitFitDao().insert(
                        BitFitEntity(
                        foodName = bitFitResult.foodName,
                        calorieCount = bitFitResult.calorieCount)
                    )
                }
            }
        }
    }
}