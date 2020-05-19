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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

/**
 * A simple [Fragment] subclass.
 */
class VerificationAgainFragment : DialogFragment() {

    lateinit var emailEditText : EditText
    lateinit var passwordEditText : EditText
    lateinit var mContext : FragmentActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_verification_again, container, false)

        emailEditText = view.findViewById(R.id.verificationMailEditText)
        passwordEditText = view.findViewById(R.id.verificationPassEditText)
        mContext = this!!.activity!!

        val btnCancel = view.findViewById<Button>(R.id.cancelVerificationMailButton)
        btnCancel.setOnClickListener {
            dialog.dismiss()

        }
        val btnSend = view.findViewById<Button>(R.id.sendVerificationMailButton)
        btnSend.setOnClickListener {

            if(emailEditText.text.isNotEmpty() && passwordEditText.text.isNotEmpty()){

                loginAndSendVerificationMail(emailEditText.text.toString(),passwordEditText.text.toString())

            }else{
                Toast.makeText(mContext,"Boş alanları doldurunuz!!",Toast.LENGTH_SHORT).show()
            }


        }


        return view
    }

    private fun loginAndSendVerificationMail(mail: String, password: String) {

        var credential = EmailAuthProvider.getCredential(mail,password)
        FirebaseAuth.getInstance().signInWithCredential(credential)

            .addOnCompleteListener{task ->
                if(task.isSuccessful){
                    sendVerificationMailAgain()
                    dialog.dismiss()

                }else{

                    Toast.makeText(mContext,"Gönderilemedi!!",Toast.LENGTH_SHORT).show()
                }
            }

    }


    private fun sendVerificationMailAgain() {
        var user = FirebaseAuth.getInstance().currentUser

        if(user != null){

            user.sendEmailVerification()
                .addOnCompleteListener(object: OnCompleteListener<Void> {
                    override fun onComplete(p0: Task<Void>) {
                        if(p0.isSuccessful) {

                            Toast.makeText(mContext,"Mail kutunuzu kontrol edip maili onaylayın", Toast.LENGTH_SHORT).show()
                        }else{

                            Toast.makeText(mContext,"Mail gönderilirirken sorun oluştu!!", Toast.LENGTH_SHORT).show()

                        }
                    }
                })

        }



    }

}
