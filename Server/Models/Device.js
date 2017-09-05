var mongoose = require('mongoose');
require('mongoose-double')(mongoose);

var Schema = mongoose.Schema;

var SchemaTypes = mongoose.Schema.Types;

var schema=new Schema({
  device_id:{type:Number,required:true,unique:true},
  mac:{type:String,required:true,unique:true},
  last_loc:{
    lat:{type:SchemaTypes.Double,required:true},
    lon:{type:SchemaTypes.Double,required:true}
  },
  user_id:{type:SchemaTypes.Double,required:true},
  lost:{type:Number,required:true}
});

module.exports=mongoose.model('Device',schema);
