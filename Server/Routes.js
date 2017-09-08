const User=require("./Models/User");
const Device=require("./Models/Device");
const Data=require("./Models/Useful_Data");

module.exports= function(app,mongo){

app.get('/create',(req,res)=>{
  var d=new Data({
      usernext:1,
      devicenext:1,
      id:1
  });

  d.save((err,resp)=>{
    if(!err){
      console.log("Saved to Database");
      res.send("Data is saved");
    }
  });

});

app.get("/adduser",(req,res)=>{
  Data.find({id:1},(err,resp)=>{
    if(!err){

      var user=new User({
        email:req.query.email,
        password:req.query.password,
        name:req.query.name,
        devices:[],
        u_id:resp[0].usernext
      });

      user.save((err,resp)=>{
        if(!err){
          res.send("1");
          Data.update({id:1},{$set:{usernext:user.u_id+1}},(err,resp)=>{

          });

        }
        else {
          res.send("0");
          throw(err);
        }
      });



  }

  else {
    console.log(err);
  }
  });

});

app.get("/addevice",(req,res)=>{
  Data.find({id:1},(err,resp)=>{
    if(!err){
      var z={
        lat:0,
        lon:0
      };
        var x;
        x=resp[0].devicenext;
      var device=new Device({
        device_id:resp[0].devicenext,
        last_loc:z,
        user_id:req.query.userid,
        mac:req.query.mac,
        lost:0
      });


      device.save((err,resp)=>{
        if(!err){
          res.send("1");
          Data.update({id:1},{$set:{devicenext:device.device_id+1}},(err,resp)=>{

          });



        }

        else{
          res.send(err);
        }
      });
//Push device id to array
      User.update({u_id:req.query.userid},{$push:{
        devices:x
      }},(err)=>{
        if(!err)
        console.log("No error");
        else {
          console.log(err);
        }

      });


    }

    else {
      res.send("0");
    }

  });
});

app.get("/getdeviceinfo",(req,res)=>{
  Device.find({device_id:req.query.id},(err,resp)=>{
    if(!err){
      console.log(resp[0]);
      var k={
        device_id:resp[0].device_id,
        mac:resp[0].mac,
        last_loc:{
          lat:resp[0].last_loc.lat,
          lon:resp[0].last_loc.lon
        },
        user_id:resp[0].user_id,
        lost:0

      };

      res.json(k);
    }
  });
});

app.get("/login",(req,res)=>{
  User.find({email:req.query.email,password:req.query.password},(err,resp)=>{
    if(resp.length==0)
    res.send("0");
    else
    res.send(JSON.stringify(resp[0]));
  });
});


app.get("/device_lost",(req,res)=>{
  Device.update({mac:req.query.mac},{$set:{lost:1}},(err,resp)=>{
    if(!err){
      var location=JSON.parse(req.query.location);
      Device.update({mac:req.query.mac},{$set:{last_loc:location}},(err,resp)=>{
        if(!err)
        res.send("1");
        else {
          res.send("0");
        }
      });

    }
    else {
      res.send("0");
    }
  });
});



app.get("/device_found",(req,res)=>{
  Device.update({mac:req.query.mac},{$set:{lost:0}},(err,resp)=>{
    if(!err)
    res.send("1");
    else {
      res.send("0");
    }
  });
});
var istracker=function(n,i,req,res,arr,x)
{

    if(i==n){
      res.send(arr);
    }
    else{
      Device.find({mac:x[i]},(err,resp)=>{
        if(!err){
          if(resp.length==0){
          arr.push("0");
          istracker(n,i+1,req,res,arr,x);

        }
          else {

            arr.push("1");
            istracker(n,i+1,req,res,arr,x);
          }


        }
    });
}
}



app.get("/istrackerdevice",(req,res)=>{
  var x=JSON.parse(req.query.mac_addresses);
  var arr=[];

  istracker(x.length,0,req,res,arr,x);

});



app.get("/show",(req,res)=>{
  Data.find((err,resp)=>{
    if(!err)
    res.send(resp);
  });
});

app.get("/showuser",(req,res)=>{
  User.find((err,resp)=>{
    res.json(resp);
  });
});

app.get("/showdevice",(req,res)=>{
  Device.find((err,resp)=>{
    res.send(resp);
  });
});


app.get("/machine_learning",(req,res)=>{
  var z=[];
  z.push(-2.0434 *Math.pow(10,-5));
  z.push(-1.2300*Math.pow(10,-4));

  theta1=req.query.t1;
  theta2=req.query.t2;



  m=z[0]*theta1+z[1]*theta2;
  if(m>-0.0434)
  res.json(1);
  else {
    res.json(0);
  }


});


}
