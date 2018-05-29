

var dice = {
	ajax: new HttpService(),
	dicewait:0,
	account:0,
	bet_value:0,
	current_term:0,
	bet:0,
	bet_name:'',
	danshuang:1.9,
	number:5,
	daxiao:1.9,
	dan : "7",
	shuang : "8",
	xiao : "9",
	da : "10",
	select_num: function (number,name){
			this.ajax.get('/dice/account-info', {}, function(data) {
			if (data.code == '2018') {
				dice.account = data.result.account;
				$('#account').text(dice.account);
				$('#bet_name').text(name);
				dice.bet = number;
				dice.bet_name = name;
				layer.open({
				  type: 1,
				  title: false,
				  closeBtn: 0,
				  shadeClose: false,
				  area: '100%',
				  skin: 'bet-skin',
				  content: $('#bet')
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
		$('#bet_value').html(this.bet_value);
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
                    icon: 2,
					end: function(){ 
						window.location.href='../onlinedeposit.html'
					  }
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
	
	update_bet:function(diceBet,diceBetValue){
		$('#betValue' + diceBet).text(diceBetValue);
		$('#bet-mask' + diceBet).show();
		$('#betValueDiv' + diceBet).animate({
			bottom: "10"
		  }, 600, function() {
			// Animation complete.
		  });
	},
	save_bet:function (number){
		if(dice.bet_value > 0){
			var index = layer.load();
			this.ajax.post('/dice/dice-bet', {term:dice.current_term,bet:dice.bet, bet_value:dice.bet_value}, 
				function(data) {
					if (data.code == '1103'){
						layer.msg('您的余额不足，请先充值！', {
							time: 2000,
							icon: 2
						});
					}else{
						if (data.code == '2018') {
						
							dice.update_bet(dice.bet,dice.bet_value);
							dice.bet=0;
							dice.bet_name = '';
							dice.bet_value = 0;
							$('#bet_value').html(dice.bet_value);
							layer.closeAll();
						} else {
							layer.msg('数据获取失败', {
								time: 2000,
								icon: 2
							});
						}

					}
				}, function(e) {
					layer.msg('数据获取失败', {
						time: 2000,
						icon: 2
					});	
				});
		}else{
			layer.msg('您还没有下注！', {
						time: 2000,
						icon: 2
					});
		}
	},
	shuoming: function(){
		layer.open({
		  type: 1,
		  shade: false,
		  title: false,
		  area: '100%',
		  skin: 'layui-layer-nobg', //没有背景色
		  content: $('#shuoming')
		});
	},
	
	diceAnimate:function(){
		var dice = $("#result_dice");
		var num = Math.floor(Math.random()*6+1);//生成随机数1-6
		dice.attr("class","dice");//清除上次动画后的点数
        dice.css('cursor','default');
        // 骰子动画
        dice.animate({left: '+2px'}, 100,function(){
            dice.addClass("dice_t");
        }).delay(200).animate({top:'-2px'},100,function(){
            dice.removeClass("dice_t").addClass("dice_s");
        }).delay(200).animate({opacity: 'show'},600,function(){
            dice.removeClass("dice_s").addClass("dice_e");
        }).delay(100).animate({left:'-2px',top:'2px'},100,function(){
           
        });
	},

	initPage: function (isInit){
		if(dice.dicewait == 0){
			this.ajax.get('/dice/dice-draw', {"init":isInit}, function(data) {
				if (data.code == '2018') {
					dice.current_term = data.result.currentTerm;
					var resultMsg = "第"+data.result.lastTerm+"期:"+data.result.lastResult+"点, " + (data.result.lastResult>3?"大":"小") + ", " + (data.result.lastResult%2==0?"双":"单");
					$('#kjResult').text(resultMsg);

					if(data.result.drawTime == 0){
						layer.closeAll();
						$('#second_show').html("<span id='miao'>抽奖中...</span>");
					}else{
						$('#second_show').html("<span id='miao'>"+data.result.drawTime +"</span>秒");
						$("#result_dice").attr("class", 'dice dice_'+data.result.lastResult);
						if(!isInit){
							$(".bet-mask").hide();
							$(".bet-value-num").text("0");
							$('.bet-info' ).removeAttr( "style");
							var betkeys  = Object.keys(data.result.bet);
							if(betkeys.length > 0){
								var winMsg = "";
								betkeys.forEach(function(key) {
									if(key == "1" || key == "2" || key == "3" || key == "4" || key == "5" || key == "6"){
										if(data.result.bet[key] > 0){
											winMsg = winMsg + "下注" +　key + "赢："+data.result.bet[key] + "<br/>";
										}else{
											winMsg = winMsg + "下注" +　key + "没有中奖<br/>";
										}
									}else if(key == dice.dan){
										if(data.result.bet[key] > 0){
											winMsg = winMsg + "下注单赢："+data.result.bet[key]+ "<br/>";
										}else{
											winMsg = winMsg + "下注单没有中奖<br/>";
										}
									}else if(key == dice.shuang){
										if(data.result.bet[key] > 0){
											winMsg = winMsg + "下注双赢："+data.result.bet[key]+ "<br/>";
										}else{
											winMsg = winMsg + "下注双没有中奖<br/>";
										}
									}else if(key == dice.da){
										if(data.result.bet[key] > 0){
											winMsg = winMsg + "下注大赢："+data.result.bet[key]+ "<br/>";
										}else{
											winMsg = winMsg + "下注大没有中奖<br/>";
										}
									}else if(key == dice.xiao){
										if(data.result.bet[key] > 0){
											winMsg = winMsg + "下注小赢："+data.result.bet[key]+ "<br/>";
										}else{
											winMsg = winMsg + "下注小没有中奖<br/>";
										}
									}

									layer.msg(resultMsg + "<br/> " + winMsg, {
										time: 4000,
										icon: 6
									});
								});
								
							}

						}else{
							Object.keys(data.result.bet).forEach(function(key) {
								dice.update_bet(key,data.result.bet[key]);
							});
							
						}
					}
					dice.dicewait = data.result.drawTime;
					dice.bet_name = '';
					dice.bet_value = 0;
					dice.bet = 0;
					$('#show_number').html(dice.bet_name);
					$('#show_money').html(dice.bet_value);
					setTimeout(function() {
						dice.initPage(false);
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
				layer.closeAll();
				dice.diceAnimate();
				$('#second_show').html("<span id='miao'>抽奖中...</span>");
			}else{
			
				$('#miao').html(dice.dicewait);
			}
			 
			setTimeout(function() {
				dice.initPage(false);
			},
			1000);
		}

	}
};

$(function() {
    dice.initPage(true);
});



