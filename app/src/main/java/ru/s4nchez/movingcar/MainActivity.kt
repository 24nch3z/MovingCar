package ru.s4nchez.movingcar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        car_view.setOnClickListener {
            hint_view.animate()
                    .setDuration(2000L)
                    .alpha(0.0f)
                    .start()
        }
    }
}
