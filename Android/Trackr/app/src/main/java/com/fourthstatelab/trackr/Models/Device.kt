package com.fourthstatelab.trackr.Models


/**
 * Created by sid on 9/2/17.
 */

data class Device(val deviceName: String,
                  val deviceAddress: String,
                  val customName: String,
                  var isLost: Boolean = false)
