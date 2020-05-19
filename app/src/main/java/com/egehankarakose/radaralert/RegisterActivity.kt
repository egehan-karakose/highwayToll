package com.egehankarakose.radaralert

import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        registerButton.setOnClickListener {
            if(editTextMail.text.isNotEmpty() && editTextPassword.text.isNotEmpty() && editTextPassword2.text.isNotEmpty()){

                if(editTextPassword2.text.toString().equals(editTextPassword.text.toString())){

                    newUserRegister(editTextMail.text.toString(),editTextPassword.text.toString())

                }else{
                    Toast.makeText(this, R.string.match_fail,Toast.LENGTH_SHORT).show()

                }


            }else{
                Toast.makeText(this, R.string.fill_text,Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun newUserRegister(mail :String,password:String) {
        showProgressBar()
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail,password).addOnCompleteListener(object :OnCompleteListener<AuthResult>{
            override fun onComplete(p0: Task<AuthResult>) {

                if(p0.isSuccessful){

                    Toast.makeText(this@RegisterActivity,
                        R.string.register_success,Toast.LENGTH_SHORT).show()
                    mailCheck()
                    FirebaseAuth.getInstance().signOut()

                }else{
                    Toast.makeText(this@RegisterActivity,
                        R.string.register_fail,Toast.LENGTH_SHORT).show()
                }


            }
        })
        hideProgressBar()



    }

    private fun showProgressBar(){
        progressBar.visibility= View.VISIBLE

    }

    private fun hideProgressBar(){
        progressBar.visibility= View.INVISIBLE

    }

    private fun mailCheck() {
        var user = FirebaseAuth.getInstance().currentUser

        if(user != null){

            user.sendEmailVerification()
                .addOnCompleteListener(object: OnCompleteListener<Void>{
                    override fun onComplete(p0: Task<Void>) {
                        if(p0.isSuccessful) {

                            Toast.makeText(this@RegisterActivity,
                                R.string.mail_confirm, Toast.LENGTH_SHORT).show()
                        }else{

                            Toast.makeText(this@RegisterActivity,
                                R.string.mail_confirm_fail, Toast.LENGTH_SHORT).show()

                        }
                    }
                })

        }



    }

}
