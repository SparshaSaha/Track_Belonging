var mongoose = require('mongoose');

var Schema = mongoose.Schema;
 var schema=new Schema({
   usernext:{type:Number},
   devicenext:{type:Number},
   id:{type:Number}
 });

 module.exports=mongoose.model('Data',schema);
