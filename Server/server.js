"use strict"
const express = require("express");
const mongoose = require("mongoose");


var db = mongoose.connection.openUri('mongodb://heroku_d2cs28g3:hmsbs2a4sf8252piktq4438o1e@ds127894.mlab.com:27894/heroku_d2cs28g3');
//mongodb://heroku_d2cs28g3:hmsbs2a4sf8252piktq4438o1e@ds127894.mlab.com:27894/heroku_d2cs28g3
//'mongodb://127.0.0.1:27017/TrackerDatabase3'
mongoose.connection.once('connected',function(){
  console.log("Connected to database");
});

const app=express();

const port=process.env.PORT || 8080;

require("./Routes")(app,db);
app.listen(port);
