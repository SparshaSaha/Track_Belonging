package com.fourthstatelab.trackr.Utils


import android.os.AsyncTask

import com.fourthstatelab.trackr.Models.Credential
import com.fourthstatelab.trackr.config
import com.google.gson.Gson
import com.squareup.okhttp.HttpUrl
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.RequestBody
import com.squareup.okhttp.Response

import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.TimeUnit


/**
 * Created by sid on 6/13/17.
 */

class HttpRequest {
    enum class Method {
        GET,
        POST
    }

    private var method: Method? = null
    private var url: String? = null
    private val params = HashMap<String, String>()
    private var authentication: String? = null
    private var shouldExecuteResponse = true
    private var client: OkHttpClient? = null

    constructor(url: String, method: Method) {
        this.url = config.SERVER_LINK + url
        this.method = method
        allRequests.add(this)
    }

    constructor() {
        allRequests.add(this)
    }

    fun setUrl(url: String): HttpRequest {
        this.url = config.SERVER_LINK + url
        return this
    }

    fun setMethod(method: Method): HttpRequest {
        this.method = method
        return this
    }

    internal fun stop() {
        shouldExecuteResponse = false
        if (client != null) client!!.cancel("Cancelled")
        allRequests.remove(this)
    }

    fun addParam(key: String, value: String): HttpRequest {
        params.put(key, value)
        return this
    }

    fun addAuthenticationHeader(reg: String, password: String): HttpRequest {
        val creds = Credential(reg, password)
        this.authentication = Gson().toJson(creds)
        return this
    }


    interface OnResponseListener {
        fun OnResponse(response: String?)
    }

    fun sendRequest(onResponseListener: OnResponseListener): HttpRequest {
        ApiExecuter(onResponseListener).execute()
        return this
    }

    private fun buildRequest(): Request {

        val builder = HttpUrl.parse(url!!).newBuilder()
        for ((key, value) in params) {
            builder.addQueryParameter(key, value)
        }
        val urlwithquery = builder.build()

        var requestBuilder: Request.Builder? = null
        if (method == Method.GET) {
            requestBuilder = Request.Builder()
                    .url(urlwithquery)
                    .get()
        } else {
            requestBuilder = Request.Builder()
                    .url(urlwithquery)
                    .post(RequestBody.create(null, ByteArray(0)))
        }
        if (authentication != null)
            requestBuilder!!.addHeader("Authorization", authentication!!)
        return requestBuilder!!.build()
    }

    private inner class ApiExecuter(internal var onResponseListener: OnResponseListener) : AsyncTask<Void, Void, String>() {
        override fun doInBackground(vararg voids: Void): String? {
            if (shouldExecuteResponse) {
                client = OkHttpClient()
                val request = buildRequest()
                var response: Response? = null
                try {
                    client!!.setConnectTimeout(60, TimeUnit.SECONDS)
                    client!!.setReadTimeout(60, TimeUnit.SECONDS)
                    client!!.setWriteTimeout(60, TimeUnit.SECONDS)
                    response = client!!.newCall(request).execute()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                if (response != null && response.isSuccessful) {
                    try {
                        return response.body().string()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                } else {
                    return null
                }
            }
            return null
        }

        override fun onPostExecute(s: String) {
            if (shouldExecuteResponse) {
                onResponseListener.OnResponse(s)
            }
            allRequests.remove(this@HttpRequest)
            super.onPostExecute(s)
        }
    }

    companion object {

        private val allRequests = ArrayList<HttpRequest>()
        fun stopAll() {
            for (request in allRequests) {
                request.stop()
            }
            allRequests.clear()
        }
    }

}
