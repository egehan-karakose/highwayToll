package com.egehankarakose.radaralert

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_alarm.*
import java.sql.Time

/**
 * A simple [Fragment] subclass.
 */
class AlarmFragment(messageFromAct : Time) : DialogFragment() {

    private var message = messageFromAct
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
       var view = inflater.inflate(R.layout.fragment_alarm, container, false)

        var alarmCloseBtn = view.findViewById<Button>(R.id.alarmCloseButton)
        var waitingTimetxt = view.findViewById<TextView>(R.id.waitingTimeText)



        var waitingString = message.toString()
        waitingTimetxt.text = "$waitingString"




        val vibe:Vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26){
            var effect:VibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibe.vibrate(effect)
        }else{
            vibe.vibrate(500)
        }



        alarmCloseBtn.setOnClickListener {

            dialog.dismiss()
        }



        return view
    }



}
