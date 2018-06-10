

var share = {
	ajax: new HttpService(),
	initPage: function (){
			var names = JSON.parse(sessionStorage.getItem('userinfo')).name;
			var shareurl = window.location.protocol+"//"+window.location.hostname+"/home.html?icode=" + names;
            var h = '<div class="icodes">' + names + '</div><div class="netaddr"><input id="copyurl" value="'+shareurl+ '" style="width:200px;"/><span class="tooltip"><button id="btn-copy-qrcode" onclick="share.copycode();"  onmouseout="share.outFunc()"><span class="tooltiptext" id="myTooltip">Copy to clipboard</span>复 制</button></span></div><div id="qrcode"></div><div class="qrcode-tip">Tip：受邀会员每月下注流水总额的1%作为推广返利</div>';
			$('#shareSection').html(h);
			new QRCode(document.getElementById("qrcode"), {
				text:  shareurl,
				width: 128,
				height: 128,
				colorDark: "#000000",
				colorLight: "#ffffff",
				correctLevel: QRCode.CorrectLevel.H
			});
	

	},

	copycode:function (){
		var copyText = document.getElementById("copyurl");
		 copyText.select();
		document.execCommand("copy");
		var tooltip = document.getElementById("myTooltip");
		 tooltip.innerHTML = "复制成功";
		  $('#myTooltip').css("visibility", "visible").css("opacity","1");
	},
	outFunc:function () {
	  var tooltip = document.getElementById("myTooltip");
	  tooltip.innerHTML = "";
	  $('#myTooltip').css("visibility", "hidden");
	}
};

$(function() {
    share.initPage();
});



