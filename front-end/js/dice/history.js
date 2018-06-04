

var dicehistory = {
	ajax: new HttpService(),
	initPage: function (page){
		this.ajax.get('/dice/bet-draw-history', {"page":page}, function(data) {
				if (data.code == '2018') {
					var historyHtml = "<section>" +
								'<div class="content history">' + 
									'<ul class="betRecord-ul">' + 
										'<div class="lists">' ;
									for(var r in data.result.result){
										var obj = data.result.result[r];
										historyHtml = historyHtml + "<li>" + 
											'<div class="flex-simple">' + 
												'<div>' + 
													'<div class="term">第'+obj.current_term+'期</div>' + 
													'<div class="bet-result">开奖号码：'+obj.result+'点, ' + (obj.result>3?"大":"小") + ", " + (obj.result%2==0?"双":"单")+'</div>' + 
												'</div>' + 
												
													'<div class="detail term-time">' + 
													'<div>开奖时间：'+obj.end_time_str+'</div>'+
													'<div>共'+obj.bet_total+'人下注，中奖'+obj.win_total+'!</div>';
													if(obj.diceBetDTOs.length > 0){
														for(var betD in obj.diceBetDTOs){
															historyHtml = historyHtml + '<div>下注 '+obj.diceBetDTOs[betD].bet_name+'：'+obj.diceBetDTOs[betD].bet_value+'， 返回：'+obj.diceBetDTOs[betD].win_money+'</div>';
														}
													}else{
														historyHtml = historyHtml + '<div style="font-weight:bold;">本期您没有下注！</div>';
													}
												historyHtml = historyHtml + '</div>'+
											 '</div>'+
											'</li> ';
											
									}

								historyHtml = historyHtml + 		'</div>' + 
									  '</ul>' + 
									'<div class="page" style=" padding:10px;margin-bottom: 30px;">'+
									'<div><span class="rows">共 '+data.result.total+' 条记录</span>';
									if(1 == data.result.current_page){
										historyHtml = historyHtml + '<span class="next"><<</span>';
									}else{
										historyHtml = historyHtml + '<a class="next" href="javascript:void(0);" onclick="dicehistory.initPage('+(data.result.current_page - 1)+');"><<</a>';
									}
									var begin = 1;
									var end = data.result.pages;

									if(data.result.current_page > 7){
										begin = data.result.current_page -5;
									}

									if(begin + 9 < data.result.pages){
										end = begin + 9;
									}

									for(var i=begin;i<=end;i++){
										if(i==data.result.current_page){
											historyHtml = historyHtml + '<span class="current">'+i+'</span>';
										}else{
											historyHtml = historyHtml + '<a class="num" href="javascript:void(0);" onclick="dicehistory.initPage('+i+');">'+i+'</a>'
										}	
									}
									if(data.result.pages == data.result.current_page){
										historyHtml = historyHtml + '<span class="next">>></span>';
									}else{
										historyHtml = historyHtml + '<a class="next" href="javascript:void(0);" onclick="dicehistory.initPage('+(data.result.current_page + 1)+');">>></a>';
									}
									'</div>'
								"</div>" + 
								"</section>";

							$('#historySection').html(historyHtml);
							
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


	}
};

$(function() {
    dicehistory.initPage(1);
});



