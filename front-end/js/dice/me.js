

var diceme = {
	ajax: new HttpService(),
	initPage: function (){
		this.ajax.get('/dice/member-info', {}, function(data) {
				if (data.code == '2018') {
					$('#myid').text(data.result.name);
					if(data.result.real_name != null){
						$('#realName').text(data.result.real_name);
					}else{
						$('#realName').text('匿名');
					}
					$('#yue').text(data.result.sum);
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
    diceme.initPage();
});



