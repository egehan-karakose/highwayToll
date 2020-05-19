package com.egehankarakose.radaralert

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.egehankarakose.radaralert.UserProcess.ForgetPasswordFragment
import com.egehankarakose.radaralert.UserProcess.VerificationAgainFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {

    lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        initMyAuthStateListener()


        textViewRegister.setOnClickListener {
            intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        buttonLogIn.setOnClickListener {

            if(editTextLoginMail.text.isNotEmpty() && editTextLoginPass.text.isNotEmpty()){

                showProgressBarLogin()
                FirebaseAuth.getInstance().signInWithEmailAndPassword(editTextLoginMail.text.toString(),editTextLoginPass.text.toString()).addOnCompleteListener(object:OnCompleteListener<AuthResult>{
                    override fun onComplete(p0: Task<AuthResult>) {

                        if (p0.isSuccessful){
                            //Toast.makeText(this@LoginActivity,"Giriş Başarılı",Toast.LENGTH_SHORT).show()
                            if(!p0.result!!.user?.isEmailVerified!!){
                                FirebaseAuth.getInstance().signOut()
                            }



                        }else
                        {
                            Toast.makeText(this@LoginActivity,
                                R.string.login_fail,Toast.LENGTH_SHORT).show()

                        }
                        hideProgressBarLogin()

                    }

                })
            }else{
                Toast.makeText(this, R.string.complete, Toast.LENGTH_SHORT).show()
            }
        }

        verificationMailAgainButton.setOnClickListener {
            var showDialog = VerificationAgainFragment()
            showDialog.show(supportFragmentManager,"showDialog")
        }

        forgetPasswordButton.setOnClickListener {
            var dialogSendNewPassword =
                ForgetPasswordFragment()
            dialogSendNewPassword.show(supportFragmentManager,"showDialogPass")
        }

        }






    private fun showProgressBarLogin(){
        progressBarLogin.visibility= View.VISIBLE

    }

    private fun hideProgressBarLogin(){
        progressBarLogin.visibility= View.INVISIBLE

    }


    private fun initMyAuthStateListener(){

        mAuthStateListener= object :FirebaseAuth.AuthStateListener{
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var user = p0.currentUser
                if(user!=null){
                    if(user.isEmailVerified){
                        Toast.makeText(this@LoginActivity,
                            R.string.login_success,Toast.LENGTH_SHORT).show()
                        var intent = Intent(this@LoginActivity,
                            MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    }else{

                        Toast.makeText(this@LoginActivity,
                            R.string.mail_check,Toast.LENGTH_SHORT).show()

                    }



                }}
        }

    }


    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener)
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener)
    }
}
