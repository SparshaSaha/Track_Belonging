var mongoose = require('mongoose');
var Schema = mongoose.Schema;

var schema=new Schema({
  u_id:{type:Number,required:true,unique:true},
  name:{type:String,required:true},
  email:{type:String, required:true, unique:true},
  password:{type:String, required:true},
  devices:[ {type:Number} ]
});

module.exports=mongoose.model('User',schema);
