package com.fourthstatelab.trackr.Activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

import com.fourthstatelab.trackr.R
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View

import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

import com.fourthstatelab.trackr.Data
import com.fourthstatelab.trackr.Models.Device
import com.fourthstatelab.trackr.Models.User
import com.fourthstatelab.trackr.Utils.HttpRequest

import com.fourthstatelab.trackr.Utils.Preference
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class Login : AppCompatActivity() {

    lateinit var emailView: TextView
    lateinit var passwordView: TextView
    lateinit var toggle_showPassword: ImageView
    var isPasswordShown: Boolean = false
    lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initLayout()

        /*LocationService.getLocation(applicationContext,object : LocationService.LocationChangedListener {
            override fun onLocationChanged(location: Location) {
                Toast.makeText(applicationContext,Gson().toJson(location),Toast.LENGTH_SHORT).show()
                Log.d("LOCATION UPDATE",Gson().toJson(location))
            }
        })*/

    }

    private fun initLayout() {
        emailView = findViewById(R.id.tv_login_email) as TextView
        passwordView = findViewById(R.id.tv_login_password) as TextView
        toggle_showPassword = findViewById(R.id.toggle_pass_View) as ImageView
        loginButton = findViewById(R.id.button_login) as Button
        toggle_showPassword.setOnClickListener({
            togglePassword()
        })

        loginButton.setOnClickListener({
            signin()
        })

    }

    fun signin(){
        val progressDialog = ProgressDialog(this@Login)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Signing in")
        progressDialog.show()
        HttpRequest("/login",HttpRequest.Method.GET)
                .addParam("email",emailView.text.toString())
                .addParam("password",passwordView.text.toString())
                .sendRequest(object : HttpRequest.OnResponseListener {
                    override fun OnResponse(response: String?) {
                        if(response!=null) {
                            Log.d("HTTP LOGIN",response)
                            if(response=="0") {
                                Toast.makeText(applicationContext, "Invalid Credential", Toast.LENGTH_LONG).show()
                            }
                            else{
                                try {
                                    Data.user = Gson().fromJson(response, object : TypeToken<User>() {}.type)
                                    if (readDataSuccessful()) {
                                        startActivity(Intent(this@Login, Dashboard::class.java))
                                    } else {
                                        Data.myDevices = ArrayList()
                                        val intent = Intent(this@Login, AddDevice::class.java)
                                        intent.putExtra("intentAction","fromSignin")
                                        startActivity(intent)
                                    }
                                }
                                catch(exception : Exception){
                                    Log.e("HTTP LOGIN","GSON PARSING ERROR")
                                }
                            }
                        }
                        else{
                            Toast.makeText(applicationContext, "No Internet Connection", Toast.LENGTH_LONG).show()
                        }
                        progressDialog.cancel()
                    }
                })
    }

    fun onSignupClick(view : View){
        startActivity(Intent(this@Login,Signup::class.java))
    }

    fun readDataSuccessful(): Boolean {
        val json: String = Preference.get(applicationContext, Preference.MY_DEVICES, "")
        return if (!json.isEmpty()) {
            Data.myDevices = Gson().fromJson(json, object : TypeToken<ArrayList<Device>>() {}.type)
            true
        } else {
            false
        }
    }

    private fun isEmailCorrect() {

    }

    private fun isPasswordCorrect() {

    }

    fun togglePassword() {
        if (isPasswordShown) {
            passwordView.transformationMethod = PasswordTransformationMethod()
            toggle_showPassword.setImageResource(R.drawable.ic_view_password)
            isPasswordShown = false
            passwordView.selectionEnd
        } else {
            passwordView.transformationMethod = null
            toggle_showPassword.setImageResource(R.drawable.ic_unview_password)
            isPasswordShown = true
            passwordView.selectionEnd
        }
    }

}
