

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



