'use strict';
/* sessionService */

//session storage api
var sessStorage = {
	put:function(key,value){
		var json = JSON.stringify(value);
		if(window.sessionStorage){
			window.sessionStorage.setItem(key,json);
		}else{
			$.cookie(key,json,{ expires: 100000});
		}
	},
	get :function(key){
		var value = "";
		if(window.sessionStorage){
			value = window.sessionStorage.getItem(key);
			
		}else{
			value = $.cookie(key);
		}
		if(value!=null&&value!='undefined'){
			return JSON.parse(value);
		}else{
			return null;	
		}
	},
	remove:function(key){
		if(window.sessionStorage){
			window.sessionStorage.removeItem(key);
		}else{
			$.cookie(key, null); 
		}
	},
	removeAll:function(){
		if(window.sessionStorage){
			window.sessionStorage.clear();
		}else{
			$.cookie('the_cookie', '', { expires: -1 });
		}
	}
}
var locStorage = {	
	put:function(key,value){
		var json = JSON.stringify(value);
		if(window.localStorage){
			window.localStorage.setItem(key,json);
		}else{
			$.cookie(key,json,{ expires: 100000});
		}
		
	},
	get:function(key){
		var value = "";
		if(window.localStorage){
			value = window.localStorage.getItem(key);
			
		}else{
			value = $.cookie(key);
		}
		if(value!=null&&value!='undefined'){
				return JSON.parse(value);
			}else{
				return null;	
			}
	},
	remove:function(key){
		if(window.localStorage){
			window.localStorage.removeItem(key);
		}else{
			$.cookie(key, null); 
		}
	},
	removeAll:function(){
		if(window.localStorage){
			window.localStorage.clear();
		}else{
			$.cookie('the_cookie', '', { expires: -1 });
		}		
	}
}
