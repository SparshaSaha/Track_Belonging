package com.fourthstatelab.trackr.Activities

import android.app.ProgressDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.fourthstatelab.trackr.Models.Credential

import com.fourthstatelab.trackr.R
import com.fourthstatelab.trackr.Utils.HttpRequest
import com.fourthstatelab.trackr.Utils.Preference

class Signup : AppCompatActivity() {

    lateinit var emailView: TextView
    lateinit var nameView : TextView
    lateinit var passwordView: TextView
    lateinit var toggle_showPassword: ImageView
    var isPasswordShown: Boolean = false
    lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        initLayout()
    }

    private fun initLayout() {
        emailView = findViewById(R.id.tv_login_email) as TextView
        passwordView = findViewById(R.id.tv_login_password) as TextView
        nameView = findViewById(R.id.tv_login_name) as TextView
        toggle_showPassword = findViewById(R.id.toggle_pass_View) as ImageView
        loginButton = findViewById(R.id.button_login) as Button
        toggle_showPassword.setOnClickListener({
            togglePassword()
        })

        loginButton.setOnClickListener({
                signup()
        })
    }

    fun signup(){
        val progressDialog = ProgressDialog(this@Signup)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Signing up")
        progressDialog.show()

        val  creds = Credential(emailView.text.toString(),passwordView.text.toString())
        HttpRequest("/adduser", HttpRequest.Method.GET)
                .addParam("email",emailView.text.toString())
                .addParam("password",passwordView.text.toString())
                .addParam("name",nameView.text.toString())
                .sendRequest(object : HttpRequest.OnResponseListener {
                    override fun OnResponse(response: String?) {
                        if (response != null) {
                            if (response=="1") {
                                Preference.put(applicationContext, Preference.CREDS, creds)
                                Toast.makeText(applicationContext, "Sign up Successful", Toast.LENGTH_LONG).show()
                                finish()
                            }
                            else {
                                Toast.makeText(applicationContext, "Sign up failed", Toast.LENGTH_LONG).show()
                            }
                        }
                        else{
                            Toast.makeText(applicationContext, "No Internet Connection", Toast.LENGTH_LONG).show()
                        }
                        progressDialog.cancel()
                    }

                })
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
