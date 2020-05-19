package com.egehankarakose.radaralert

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {



    lateinit var mAuthStateListener : FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {

        setContentView(R.layout.activity_main)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        initAuthStateListener()

        mapButton.setOnClickListener {

            if(checkInternetConnection()){

                intent  = Intent(this, MapsActivity::class.java)
                startActivity(intent)

            }else{
                Toast.makeText(this,"Internete Bağlanın",Toast.LENGTH_SHORT).show()
            }

        }



        signOutButton.setOnClickListener {
            signOut()
        }



    }

    private  fun checkInternetConnection():Boolean{

        val cm = baseContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo

        var status= false

        if(networkInfo != null && networkInfo.isConnected ) {
            status =  true
        }

        return status

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.mainmenu,menu)
        return true

    }

    private fun initAuthStateListener(){

        mAuthStateListener= object :FirebaseAuth.AuthStateListener{
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var user = p0.currentUser
                if(user != null) {

                }
                else{

                    var intent = Intent(this@MainActivity,
                        LoginActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    finish()

                }

                }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.signOut ->{
                signOut()
                return true
            }


        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkUser(){

        var user = FirebaseAuth.getInstance().currentUser
        if(user == null) {

            var intent = Intent(this@MainActivity,
                LoginActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()


        }
    }


    private fun signOut(){

        FirebaseAuth.getInstance().signOut()
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener (mAuthStateListener)

    }

    override fun onStop() {
        super.onStop()
        if(mAuthStateListener != null){
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener)
        }


    }

    override fun onResume() {
        super.onResume()
        checkUser()

    }




}
