package com.egehankarakose.radaralert.UserProcess

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.egehankarakose.radaralert.R
import com.google.firebase.auth.FirebaseAuth

/**
 * A simple [Fragment] subclass.
 */
class ForgetPasswordFragment : DialogFragment() {

    lateinit var emailEditText : EditText
    lateinit var mContext : FragmentActivity


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var view = inflater.inflate(R.layout.fragment_forget_password, container, false)
        // Inflate the layout for this fragment

        emailEditText = view.findViewById(R.id.forgetPasswordEmailEditText)
        mContext = this!!.activity!!

        var cancelBtn = view.findViewById<Button>(R.id.cancelForgetPasswordButton)
        var sendBtn = view.findViewById<Button>(R.id.sendMailForgetPasswordButton)

        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        sendBtn.setOnClickListener {

            FirebaseAuth.getInstance().sendPasswordResetEmail(emailEditText.text.toString())
                .addOnCompleteListener{ task->

                    if(task.isSuccessful){

                        Toast.makeText(mContext,
                            R.string.mail_success, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }else{
                        Toast.makeText(mContext,
                            R.string.mail_fail, Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }

                }
        }



        return view
    }

}
