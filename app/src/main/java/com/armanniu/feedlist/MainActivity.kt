package com.armanniu.feedlist

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val adapter = FLAdapter.Factory.DEFAULT.getAdapter("StringFLItem")
        Log.d("FeedList", adapter?.type?.toString() ?: "NULL")
    }
}
