// JavaScript Document
//13883420270 123456
var HttpService = function(){
	this.MAX_VALUE = 100000;
	var TYPE = {
		"post":"POST",
		"get":"GET"
	};

	var ServerUrl = "http://101.201.102.136/zzbapi/";
  var PicUrl = "http://101.201.102.136/";

    // var ServerUrl = "https://szcg.qiantongyun.cn/api/";
    // var PicUrl = "https://szcg.qiantongyun.cn/";
	var _ajax = function(type,url,data,succ,failed){
		// console.log("url:"+url);
		var userInfo = JSON.parse(sessionStorage.getItem("userInfo"));
		var token = null;
		if(userInfo){
            token = userInfo.token
		}

		$.ajax({
            "headers": {
                "content-type": "application/x-www-form-urlencoded",
				"admintoken":token
            },
			type: type,
			url: ServerUrl+url,
			dataType: "json",
			//contentType: "application/json",
			contentType: "application/x-www-form-urlencoded",
			data: data ,
			success: function(data) {
				// console.log(data);
				if(typeof(succ)=="function"){
					return succ(data);
				}else{
					console.log("the method is no a function!");
				}
			},
			error: function(error) {
//				Loading.hide();
				if(typeof(failed)=="function"){
					failed(error);
				}else{
					console.log("the method is no a function!");
				}
			}
		});
	}
	var _upload = function(type,url,data,succ,failed){
		$.ajax({
          url: PicUrl+url ,
          type: type,
          data: data,
          async: true,
          cache: false,
          contentType: false,
          processData: false,
          success: function(data) {
			//console.log(data);
			if(typeof(succ)=="function"){
				return succ(data);
			}else{
				console.log("the method is no a function!");
			}

		  },
		  error: function(error) {
			if(typeof(failed)=="function"){
				failed(error);
			}else{
				console.log("the method is no a function!");
			}
		 }
     	});
	}
	this.upload = function(url,data,succ,failed){
		return _upload(TYPE.post,url,data,succ,failed)
	}
	this.get = function(url,data,succ,failed){
		return _ajax(TYPE.get,url,data,succ,failed);
	}
	this.post = function(url,data,succ,failed){
		return _ajax(TYPE.post,url,data,succ,failed);
	}
}

