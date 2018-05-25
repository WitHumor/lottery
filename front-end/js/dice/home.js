var ServerUrl = 'http://localhost:8080'; //192.168.31.254
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
               if (typeof(succ) == "function") {
                    return succ(data);
                } else {
                    console.log("the method is no a function!");
                }
            },
            error: function(error) {
                if (typeof(failed) == "function") {
                    failed(error);
                } else {
                    console.log("the method is no a function!");
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



var dice = {
	ajax: new HttpService(),
	dicewait:0,
	account:0,
	bet_value:0,
	current_term:0,
	bet:0,
	bet_name:'无',
	select_num: function (number,name){
			this.ajax.get('/dice/account-info', {}, function(data) {
			if (data.code == '2018') {
				dice.account = data.result.account;
				$('#account').text(dice.account);
				$('#bet_value').html(dice.bet_value);
				$('#bet_name').text(name);
				dice.bet = number;
				dice.bet_name = name;
				layer.open({
				  type: 1,
				  title: false,
				  closeBtn: 0,
				  shadeClose: true,
				  area: '420px',
				  skin: 'bet-skin',
				  content: $('#bet'),
				  end: function(){
					if(dice.bet_value !=0){
						dice.account = dice.account + dice.bet_value;
						dice.bet_value = 0;
					}
				  }
				});
			} else {
				layer.msg('数据获取失败', {
					time: 2000,
					icon: 2
				});
			}
		}, function(e) {
			layer.msg('数据获取失败', {
				time: 2000,
				icon: 2
			});
			console.log(e);
		});	



	},
	exit_bet:function(){
		this.account = this.account + this.bet_value;
		this.bet_value = 0;
		layer.closeAll();
	},
	add_bet: function (number){
			if(this.account >= number){
				this.account = this.account - number;
				$('#account').html(this.account);
				this.bet_value = this.bet_value + number;
				$('#bet_value').html(this.bet_value);
			}else{
				 layer.msg('你的账户没有足够的余额，请先充值！', {
                    time: 2000,
                    icon: 2
                });
			}
		},

	reduce_bet:function (number){
			if(this.bet_value >= number){
				this.bet_value = this.bet_value - number;
				$('#bet_value').html(this.bet_value);
				this.account = this.account + number;
				$('#account').html(this.account);

				
			}else{
				 layer.msg('请先下注！', {
                    time: 2000,
                    icon: 2
                });
			}
		},

	save_bet:function (number){
		var index = layer.load();
		this.ajax.post('/dice/dice-bet', {term:dice.current_term,bet:dice.bet, bet_value:dice.bet_value}, 
			function(data) {
				if (data.code == '2018') {
					$('#show_number').html(dice.bet_name);
					$('#show_money').html(dice.bet_value);
					dice.bet_value = 0;
					layer.closeAll();
				} else {
					layer.msg('数据获取失败', {
						time: 2000,
						icon: 2
					});
				}

			
			}, function(e) {
				layer.msg('数据获取失败', {
					time: 2000,
					icon: 2
				});	
			
			
			});
	},
	shuoming: function(){
		layer.open({
		  type: 1,
		  title: false,
		  closeBtn: 0,
		  area: '420px',
		  skin: 'layui-layer-nobg', //没有背景色
		  shadeClose: true,
		  content: $('#shuoming')
		});
	},
	betHistory: function(page){
		this.ajax.get('/dice/bet-history', {"page":page}, function(data) {
				if (data.code == '2018') {
					var historyHtml = "<div id='historySection' style='width:100%'><section>" +
								'<div class="content">' + 
									'<ul class="betRecord-ul">' + 
										'<div class="lists">' ;
									for(var r in data.result.result){
										var obj = data.result.result[r];
										historyHtml = historyHtml + "<li>" + 
											'<div class="flex-cont flex-simple">' + 
											'<div class="flex-item s-word">' + 
											'<p class="p-money">第'+obj.term+'期</p>' + 
											'</div>' + 
											'<div class="flex-item s-word">' + 
											'<p class="s-tit">开奖号码：'+obj.result+'点, ' + (obj.result>3?"大":"小") + ", " + (obj.result%2==0?"双":"单")+'</p>' + 
											'<p class="s-desc">'+obj.bet_time_str+'</p>' + 
											'</div>' + 
											'<div class="flex-item s-word">' + 
											'<p class="s-tit">下注：200 双</p>' + 
											'<p class="s-desc">赢：180</p>' + 
											'</div></div></li> ';
											
									}

								historyHtml = historyHtml + 		'</div>' + 
									  '</ul>' + 
								"</div>" + 
								"</section></div>";
							layer.open({
							  type: 1,
							  title: false,
							  closeBtn: 0,
							  area: '460px',
							  shadeClose: true,
							  content: historyHtml
							});
				} else {
					layer.msg('数据获取失败', {
						time: 2000,
						icon: 2
					});
				}
			}, function(e) {
				layer.msg('数据获取失败', {
					time: 2000,
					icon: 2
				});
				console.log(e);
			});	

	},
	initPage: function (){
		if(dice.dicewait == 0){
			this.ajax.get('/dice/dice-draw', {}, function(data) {
				if (data.code == '2018') {
					dice.current_term = data.result.currentTerm;
					$('#kjResult').text("第"+data.result.lastTerm+"期:"+data.result.lastResult+"点, " + (data.result.lastResult>3?"大":"小") + ", " + (data.result.lastResult%2==0?"双":"单") );

					if(data.result.drawTime == 0){
						$('#second_show').html("<span id='miao'>抽奖中...</span>");
					}else{
						$('#second_show').html("<span id='miao'>"+data.result.drawTime +"</span>秒");
					}
					
					$("#result_dice").attr("class", 'dice dice_'+data.result.lastResult);
						
					dice.dicewait = data.result.drawTime;
					dice.bet_name = '无';
					dice.bet_value = 0;
					dice.bet = 0;
					$('#show_number').html(dice.bet_name);
					$('#show_money').html(dice.bet_value);
					setTimeout(function() {
						dice.initPage();
					},
					1000);
				} else {
					layer.msg('数据获取失败', {
						time: 2000,
						icon: 2
					});
				}
			}, function(e) {
				layer.msg('数据获取失败', {
					time: 2000,
					icon: 2
				});
				console.log(e);
			});	
		}else{
			dice.dicewait--;
			if(dice.dicewait == 0){
				$('#second_show').html("<span id='miao'>抽奖中...</span>");
			}else{
			
				$('#miao').html(dice.dicewait);
			}
			 
			setTimeout(function() {
				dice.initPage();
			},
			1000);
		}

	}
};

$(function() {
    dice.initPage();
});

var account = 1000;
var bet_value = 0;


