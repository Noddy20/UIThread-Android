package com.uithread.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.uithread.android.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        viewBinding.btnUpdate.setOnClickListener {
            updateMessage()
        }
    }

    private fun updateMessage() = with(viewBinding) {
        val selectedDispatcher = if (radioGroup.checkedRadioButtonId == R.id.radioBtnMainThread) {
            Dispatchers.Main
        } else {
            Dispatchers.IO
        }

        lifecycleScope.launch(selectedDispatcher) {
            val currentThreadName = Thread.currentThread().name
            val message = getString(R.string.app_message, currentThreadName)

            tvAppMessage.text = message
        }
    }
}