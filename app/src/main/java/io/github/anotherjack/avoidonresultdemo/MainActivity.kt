package io.github.anotherjack.avoidonresultdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import io.github.anotherjack.avoidonresult.AvoidOnResult
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val REQUEST_CODE_CALLBACK = 23
    private val REQUEST_CODE_RXJAVA = 24
    private val REQUEST_CODE_NORMAL = 25

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //callback方式
        callback.setOnClickListener {
            AvoidOnResult(this).startForResult(FetchDataActivity::class.java, REQUEST_CODE_CALLBACK, object : AvoidOnResult.Callback {
                override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) =
                        if (resultCode == Activity.RESULT_OK) {
                            val text = data?.getStringExtra("text")
                            Toast.makeText(this@MainActivity, "callback -> " + text, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "callback canceled", Toast.LENGTH_SHORT).show()
                        }

            })
        }

        //rxjava方式
        rxjava.setOnClickListener {
            AvoidOnResult(this)
                    .startForResult(FetchDataActivity::class.java, REQUEST_CODE_RXJAVA)
                    .filter { it.resultCode == Activity.RESULT_OK }
                    .flatMap {
                        val text = it.data.getStringExtra("text")
                        Observable.fromIterable(text.asIterable())
                    }
                    .subscribe({
                        Log.d("-------> ", it.toString())
                    }, {
                        Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
                    }, {
                        Toast.makeText(this, "complete", Toast.LENGTH_SHORT).show()
                    })
        }

        //普通方式
        normal.setOnClickListener {
            val intent = Intent(this, FetchDataActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_NORMAL)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_NORMAL) {
            val text = data?.getStringExtra("text")
            Toast.makeText(this, "normal -> " + text, Toast.LENGTH_SHORT).show()
        }
    }
}
