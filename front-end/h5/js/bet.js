$(function() {
    bet.initPage();
});

bet = {
    ajax: new HttpService(),
    pageNo: 1,
    pageSize: 10,
    loading: true,
    being: false,
    fidata: {},
    initPage: function() {
        bet.loadList(bet.fidata);
        $('.i-category').on('click', function() {
            var c = '<div class="categorys"><a href="basketball.html" class="primary"><i class="iconfont icon-tiyu-lanqiu f26"></i><p>篮球</p></a><a href="football.html" class="warning"><i class="iconfont icon-tiqiu f26"></i><p>足球</p></a><a href="javascript:void(0);" class="normal"><i class="iconfont icon-21466 f26"></i><p>彩票</p></a><a href="index.html" class="danger"><i class="iconfont icon-shouye f26"></i><p>首页</p></a></div>';
            layer.open({
                content: c,
                skin: 'footer'
            });
        });
        public.filtrate({}, function(data) {
            if (data.rs != 'reset') {
                var alltimes = data.time_stand.split(' - ');
                delete data.time_stand;
                data.beginTime = alltimes[0] ? alltimes[0].replace(/\//g, '-') : "";
                data.endTime = alltimes[1] ? alltimes[1].replace(/\//g, '-') : "";
            }
            // delete data.fitype;
            delete data.rs;
            bet.pageNo = 1;
            bet.pageSize = 10;
            bet.loading = true;
            bet.being = false;
            bet.fidata = data;
            bet.loadList(data);
        });
        $('.list_container').scroll(function() {
            var scrollTop = $(this).scrollTop();
            var objHeight = $(this).height();
            var scrollHeight = $('.list_box').height();
            if (scrollTop > 100) {
                $('.back_to_top').fadeIn();
            } else {
                $('.back_to_top').fadeOut();
            }
            if (scrollHeight <= scrollTop + objHeight) {
                if (bet.loading) {
                    if (bet.being) {
                        return;
                    }
                    bet.being = true;
                    bet.pageNo++;
                    bet.loadList(bet.fidata);
                } else {
                    if (bet.pageNo == 1 && $('#baseline').length == 0 && $('#nodata').length == 0) {
                        $('.list_box').append('<fieldset id="baseline" class="baseline"><legend>没有啦</legend></fieldset>');
                    }
                }
            }
        });

        var backButton = $('.back_to_top');
        $('.back_to_top').on('click', function() {
            $('.list_container').animate({
                scrollTop: 0
            }, 800);
        });
    },
    loadList: function(option) {
        var param = $.extend({}, {
            pageNo: bet.pageNo,
            pageSize: bet.pageSize
        }, option);
        this.ajax.post('/member/single-note', param, function(data) {
            // console.log(data);
            if (param.pageNo == 1) {
                $('.list_box').html('');
            }
            if (data.code == '2018') {
                if (data.total == 0) {
                    $('.list_box').html('<img id="nodata" src="img/nodata.png" class="nodatas">');
                } else {
                    $.each(data.result, function(i, item) {
                        // 解析下注类型
                        var btypeName = '<div class="bprimary w-l-b">滚球</div>',
                            btypeIcon = '';
                        switch (item.betType) {
                            case 'RFT':
                                btypeName = '';
                                btypeIcon = 'icon-xiaoyuan-';
                                break;
                            case 'REFT':
                                // btypeName = '滚球';
                                btypeIcon = 'icon-xiaoyuan-';
                                break;
                            case 'RBK':
                                btypeName = '';
                                btypeIcon = 'icon-lanqiu';
                                break;
                            case 'REBK':
                                // btypeName = '滚球';
                                btypeIcon = 'icon-lanqiu';
                                break;
                            default:
                                btypeName = '';
                                btypeIcon = '';
                        };
                        // 解析注单选项
                        var orderOption = '';
                        var strArr = ['大', '小', '单大', '单小', '单', '双'];
                        var st = item.occasion ? item.occasion : '';
                        if (item.iorType == '大' || item.iorType == '小') {
                            st += '大小'
                        } else if (item.iorType == '单大' || item.iorType == '单小') {
                            st += '积分大小'
                        } else if (item.iorType == '单' || item.iorType == '双') {
                            st = '单双'
                        } else {
                            st += item.iorType;
                        }
                        var alltype = item.iorType;
                        if (strArr.indexOf(alltype) > -1) {
                            if (alltype == '单大') {
                                alltype = '大';
                            }
                            if (alltype == '单小') {
                                alltype = '小';
                            }
                        } else {
                            alltype = '';
                        }
                        var bs = '';
                        if (item.iorType == '让球' || item.iorType == '让分') {
                            if (item.bet == item.strong) {
                                bs = '(让方)';
                            } else {
                                bs = '(受让方)';
                            }
                            if (item.bet == 'N') {
                                bs = '';
                            }
                        }
                        var startStr = '<span style="color:red;">' + (item.bet == 'H' ? '主场' : (item.bet == 'C' ? '客场' : '和局')) + bs + '</span>' + ' - ';
                        if (item.iorType == '大' || item.iorType == '小' || item.iorType == '单' || item.iorType == '双') {
                            startStr = '';
                        }
                        orderOption = startStr + st + ' [ ' + alltype + (item.iorRatio ? item.iorRatio : "") + '&nbsp;<span style="color:red;">@' + item.ratio + '</span> ]';
                        // 解析注单类型
                        var state = '';
                        switch (item.state) {
                            case '0':
                                state = '未结算';
                                break;
                            case '1':
                                state = '已结算';
                                break;
                            case '2':
                                state = '待结算';
                                break;
                            case '-1':
                                state = '被取消';
                                break;
                            case '-2':
                                state = '下注中';
                                break;
                            default:
                                state = '*';
                        };
                        // 盈利
                        dealMoney = item.dealMoney ? (item.dealMoney + "点") : '*';
                        // 注单输赢与否
                        winLose = '', bgcolor = '';
                        switch (item.winLose) {
                            case '1':
                                winLose = '<div class="profit"><label class="biaoqian bq_state bprimary">赢</label><span class="primary">' + dealMoney + '</span></div>';
                                bgcolor = 'br-green';
                                break;
                            case '0':
                                winLose = '<div class="profit"><label class="biaoqian bq_state bfirsthand">平</label><span>' + dealMoney + '</span></div>';
                                bgcolor = 'br-red';
                                break;
                            case '-1':
                                winLose = '<div class="profit"><label class="biaoqian bq_state bdanger">输</label><span class="danger">' + dealMoney + '</span></div>';
                                break;
                            default:
                                winLose = '';
                        }

                        $('.list_box').append('<div class="each_box ' + bgcolor + '">' +
                            '<i class="type_icon iconfont ' + btypeIcon + ' f26"></i>' + btypeName +
                            '<div class="con_part">' +
                            '<p><span class="legends">' + item.league + '</span></p>' +
                            '<p class="font-bold">' + item.teamh + ' VS ' + item.teamc + '</p>' +
                            '<p>注单号: ' + item.number + '</p>' +
                            '<p>下注时间: ' + item.betTime + '</p>' +
                            '<p class="normal">下注额度: ' + item.money + '点</p>' +
                            '<p><span class="vertival-middle">比赛结果：' + (item.score ? item.score : "*") + '</span><label class="biaoqian bq_type">' + state + '</label></p>' +
                            '<p>注单选项: ' + orderOption + '</p>' +
                            '</div>' + winLose + '</div>');
                    });
                    bet.being = false;
                    if (data.result.length < param.pageSize) {
                        bet.loading = false;
                        if (param.pageNo != 1 && $('#baseline').length == 0 && $('#nodata').length == 0) {
                            $('.list_box').append('<fieldset id="baseline" class="baseline"><legend>没有啦</legend></fieldset>');
                        }
                    }
                }
            } else {
                bet.being = false;
                layer.open({
                    content: '数据加载失败，请重试',
                    skin: 'msg',
                    time: 2
                });
            }
        }, function(e) {
            bet.being = true;
            layer.open({
                content: '网络正在开小差',
                skin: 'msg',
                time: 2
            });
        });
    },
};
