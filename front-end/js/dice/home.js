

var dice = {
	ajax: new HttpService(),
	dicewait:0,
	account:0,
	bet_value:0,
	current_term:0,
	bet:0,
	bet_name:'无',
	danshuang:1.9,
	number:5,
	daxiao:1.9,
	dan : 7,
	shuang : 8,
	xiao : 9,
	da : 10,
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
				  shadeClose: false,
				  area: '100%',
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
			if(dice.dicewait == 0){
				dice.diceAnimate();
			}
           
        });
	},

	initPage: function (popup){
		if(dice.dicewait == 0){
			this.ajax.get('/dice/dice-draw', {}, function(data) {
				if (data.code == '2018') {
					dice.current_term = data.result.currentTerm;
					var resultMsg = "第"+data.result.lastTerm+"期:"+data.result.lastResult+"点, " + (data.result.lastResult>3?"大":"小") + ", " + (data.result.lastResult%2==0?"双":"单");
					$('#kjResult').text(resultMsg);

					if(data.result.drawTime == 0){
						$('#second_show').html("<span id='miao'>抽奖中...</span>");
					}else{
						$('#second_show').html("<span id='miao'>"+data.result.drawTime +"</span>秒");
						$("#result_dice").attr("class", 'dice dice_'+data.result.lastResult);
						if(popup){
							var win_rate = 0;
							if(data.result.lastResult == 1){
								if(dice.bet == 1){
									win_rate = dice.number;
								}else if(dice.bet == dice.dan){
									win_rate = dice.danshuang;
								}else if(dice.bet == dice.xiao){
									win_rate = dice.daxiao;
								}
							}else if(data.result.lastResult == 2){
								if(dice.bet == 2){
									win_rate = dice.number;
								}else if(dice.bet == dice.shuang){
									win_rate = dice.danshuang;
								}else if(dice.bet == dice.xiao){
									win_rate = dice.daxiao;
								}
							}else if(data.result.lastResult == 3){
								if(dice.bet == 3){
									win_rate = dice.number;
								}else if(dice.bet == dice.dan){
									win_rate = dice.danshuang;
								}else if(dice.bet == dice.xiao){
									win_rate = dice.daxiao;
								}
							}else if(data.result.lastResult == 4){
								if(dice.bet == 4){
									win_rate = dice.number;
								}else if(dice.bet == dice.shuang){
									win_rate = dice.danshuang;
								}else if(dice.bet == dice.da){
									win_rate = dice.daxiao;
								}
							}else if(data.result.lastResult == 5){
								if(dice.bet == 5){
									win_rate = dice.number;
								}else if(dice.bet == dice.dan){
									win_rate = dice.danshuang;
								}else if(dice.bet == dice.da){
									win_rate = dice.daxiao;
								}
							}else if(data.result.lastResult == 6){
								if(dice.bet == 6){
									win_rate = dace.number;
								}else if(dice.bet == dice.shuang){
									win_rate = dace.danshuang;
								}else if(dice.bet == dice.da){
									win_rate = dace.daxiao;
								}
							}
							
							if(win_rate > 0){
								layer.msg(resultMsg + "<br/> 恭喜您，您中奖：" + ((win_rate-1)*dice.bet_value), {
									time: 2000,
									icon: 6
								});
							}else if(win_rate == 0){
								layer.msg(resultMsg + "<br/> 这期您没有投注哦！", {
									time: 2000,
									icon: 6
								});
							}else{
								layer.msg(resultMsg + "<br/> 这期您没有中奖，再接再厉！", {
									time: 2000,
									icon: 5
								});
							}
						}
					}
					dice.dicewait = data.result.drawTime;
					dice.bet_name = '无';
					dice.bet_value = 0;
					dice.bet = 0;
					$('#show_number').html(dice.bet_name);
					$('#show_money').html(dice.bet_value);
					setTimeout(function() {
						dice.initPage(true);
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
				dice.diceAnimate();
				$('#second_show').html("<span id='miao'>抽奖中...</span>");
			}else{
			
				$('#miao').html(dice.dicewait);
			}
			 
			setTimeout(function() {
				dice.initPage(true);
			},
			1000);
		}

	}
};

$(function() {
    dice.initPage(false);
});



