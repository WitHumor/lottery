$(function() {
    PR.initPage();
});

PR = {
    ajax: new HttpService(),
    pageNo: 1,
    pageSize: 10,
    loading: true,
    being: false,
    initPage: function() {
        PR.loadList();
        $('.rightBtn').click(function() {
            layer.open({
                content: '<div style="color: #F7636D;"><div>推广返利当月月底结算</div><div>受邀会员每月下注流水总额的1%作为推广返利！</div></div>',
                skin: 'footer',
            });
        });
        $('.acontainer').scroll(function() {
            var scrollTop = $(this).scrollTop();
            var objHeight = $(this).height();
            var scrollHeight = $('.list_box').height();
            if (scrollTop > 100) {
                $('.back_to_top').fadeIn();
            } else {
                $('.back_to_top').fadeOut();
            }
            if (scrollHeight <= scrollTop + objHeight) {
                if (PR.loading) {
                    if (PR.being) {
                        return;
                    }
                    PR.being = true;
                    PR.pageNo++;
                    PR.loadList();
                } else {
                    if (PR.pageNo == 1 && $('#baseline').length == 0 && $('#nodata').length == 0) {
                        $('.list_box').append('<fieldset id="baseline" class="baseline"><legend>没有啦</legend></fieldset>');
                    }
                }
            }
        });
        var backButton = $('.back_to_top');
        $('.back_to_top').on('click', function() {
            $('.acontainer').animate({
                scrollTop: 0
            }, 800);
        });
    },
    loadList: function() {
        var param = {
            pageNo: PR.pageNo,
            pageSize: PR.pageSize
        };
        this.ajax.post('/member/generalize-rebate', param, function(data) {
            // console.log(data);
            if (param.pageNo == 1) {
                $('.list_box').html('');
            }
            if (data.code == '2018') {
                if (data.total == 0) {
                    $('.list_box').html('<img id="nodata" src="img/nodata.png" class="nodatas">');
                } else {
                    $.each(data.result, function(i, item) {
                        var times = item.time.split('年');
                        $('.list_box').append('<div class="each_box">' +
                            '<div class="each_time">' +
                            '<p>' + times[0] + '年</p>' +
                            '<p>' + times[1] + '</p>' +
                            '</div>' +
                            '<p><span class="biaoqian">会员名</span>' +
                            '<label class="font-bold primary">' + item.name + '</label>' +
                            '</p>' +
                            '<p><span class="biaoqian">流水总量</span>' +
                            '<label class="font-bold normal">' + item.betMoney + ' 点</label>' +
                            '</p>' +
                            '<p><span class="biaoqian bdanger">返利</span>' +
                            '<label class="font-bold danger">' + item.rebate + ' 点</label>' +
                            '</p>' +
                            '</div>');
                    });
                    PR.being = false;
                    if (data.result.length < param.pageSize) {
                        PR.loading = false;
                        if (param.pageNo != 1 && $('#baseline').length == 0 && $('#nodata').length == 0) {
                            $('.list_box').append('<fieldset id="baseline" class="baseline"><legend>没有啦</legend></fieldset>');
                        }
                    }
                }
            } else {
                PR.being = false;
                layer.open({
                    content: '数据加载失败，请重试',
                    skin: 'msg',
                    time: 2
                });
            }
        }, function(e) {
            PR.being = false;
            layer.open({
                content: '网络正在开小差',
                skin: 'msg',
                time: 2
            });
        });
    },
};
