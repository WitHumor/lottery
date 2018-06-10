//var ServerUrl = 'http://localhost:8080'; //192.168.31.254
var ServerUrl =  window.location.protocol +'//'+window.location.hostname+'/springBoot'; //192.168.31.254
var BaseUrl =  window.location.protocol +'//'+window.location.hostname; //192.168.31.254
var HttpService = function() {
    this.MAX_VALUE = 100000;
    var TYPE = {
        "post": "POST",
        "get": "GET"
    };

    var _ajax = function(type, url, data, succ, failed) {
        var userinfo = sessionStorage.getItem("userinfo");
        var toid = null;
        if (userinfo) {
            toid = JSON.parse(userinfo).token
        }

        $.ajax({
            headers: {
                "content-type": "application/x-www-form-urlencoded",
                "token": toid,
                "accept": "application/json, text/javascript, */*; q=0.01"
            },
            type: type,
            url: ServerUrl + url,
            dataType: "json",
            contentType: "application/x-www-form-urlencoded",
            data: data,
            success: function(data) {
			   if(data.code == '1114' || data.code == '1109' || data.code == '1102'){
					   layer.msg('请重新登录！', {
						time: 2000,
						icon: 2,
						end: function(){ 
							window.location.href=BaseUrl+'/home.html'
						  }
					});
			   }else{
				   if (typeof(succ) == "function") {
						return succ(data);
					} else {
						console.log("the method is no a function!");
					}
			   }
            },
            error: function(error) {
				if(data.code == '1114' || data.code == '1109' || data.code == '1102'){
					   layer.msg('请重新登录！', {
						time: 2000,
						icon: 2,
						end: function(){ 
							window.location.href=BaseUrl+'/home.html'
						  }
						});
			   }else{
					if (typeof(failed) == "function") {
						failed(error);
					} else {
						console.log("the method is no a function!");
					}
				}
            }
        });
    };
    this.get = function(url, data, succ, failed) {
        return _ajax(TYPE.get, url, data, succ, failed);
    }
    this.post = function(url, data, succ, failed) {
        return _ajax(TYPE.post, url, data, succ, failed);
    }
}

