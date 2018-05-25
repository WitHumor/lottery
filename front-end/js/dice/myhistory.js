

var dicemyhistory = {
	ajax: new HttpService(),
	initPage: function (page){
		this.ajax.get('/dice/bet-history', {"page":page}, function(data) {
				if (data.code == '2018') {
					var historyHtml = "<section>" +
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
											'<div class="flex-item s-word" style="text-align:right;">' + 
											'<p class="s-tit">下注：'+obj.bet_value+', '+obj.bet_name+'</p>' + 
											'<p class="s-desc">赢：'+(obj.win_money>0?obj.win_money-obj.bet_value:0)+'</p>' + 
											'</div></div></li> ';
											
									}

								historyHtml = historyHtml + 		'</div>' + 
									  '</ul>' + 
									'<div class="page" style=" padding:10px;margin-bottom: 30px;">'+
									'<div><span class="rows">共 '+data.result.total+' 条记录</span>';
									if(1 == data.result.current_page){
										historyHtml = historyHtml + '<span class="next"><<</span>';
									}else{
										historyHtml = historyHtml + '<a class="next" href="javascript:void(0);" onclick="dicemyhistory.betHistory('+(data.result.current_page - 1)+');"><<</a>';
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
											historyHtml = historyHtml + '<a class="num" href="javascript:void(0);" onclick="dicemyhistory.initPage('+i+');">'+i+'</a>'
										}	
									}
									if(data.result.pages == data.result.current_page){
										historyHtml = historyHtml + '<span class="next">>></span>';
									}else{
										historyHtml = historyHtml + '<a class="next" href="javascript:void(0);" onclick="dicemyhistory.initPage('+(data.result.current_page + 1)+');">>></a>';
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
    dicemyhistory.initPage(1);
});



