"use strict"
const express = require("express");
const mongoose = require("mongoose");


var db = mongoose.connection.openUri('mongodb://127.0.0.1:27017/TrackerDatabase3');

mongoose.connection.once('connected',function(){
  console.log("Connected to database");
});

const app=express();

const port=process.env.PORT || 8080;

require("./Routes")(app,db);
app.listen(port);
