$(function() {
    TR.initPage();
});

var TR = {
    ajax: new HttpService(),
    cz: {
        pageNo: 1,
        pageSize: 10,
        loading: true,
        being: false,
        fidata: {},
    }, //充值0
    tx: {
        pageNo: 1,
        pageSize: 10,
        loading: true,
        being: false,
        fidata: {},
    }, //提现1

    initPage: function() {
        // var touchslider = new TouchSlider('sliders', {
        //     duration: 100,
        //     direction: 0,
        //     autoplay: false,
        //     fullsize: true
        // });
        // touchslider.on('before', function(m, n) {
        //     var as = $('.pagenavi a');
        //     as[m].className = '';
        //     as[n].className = 'active';
        // });
        TR.loadList(TR.tx.fidata, '1');
        // TR.loadList(TR.cz.fidata, '0');
        var mySwiper = new Swiper('#mySwiperes', {
            autoHeight: true,
            spaceBetween: 20,
            observer: true,
            observeParents: true,
            on: {
                slideChangeTransitionEnd: function() {
                    var aIndex = this.activeIndex;
                    var tIndex = $('.pagenavi a.active').parent('li').index();
                    if (aIndex == tIndex) return;
                    $('.pagenavi a').removeClass('active');
                    $($('.pagenavi > li')[aIndex]).find('a').addClass('active');
                    $($('.filtrate .fi_container')[aIndex]).addClass('activate').siblings('.fi_container').removeClass('activate');
                    $('.bottom_btn_reset_submit .btn_reset').click();
                    mySwiper.updateSize();
                },
            },
        });
        $('.pagenavi a').click(function() {
            if ($(this).hasClass('active')) {
                $('.bottom_btn_reset_submit .btn_reset').click();
                mySwiper.updateSize();
                return;
            } else {
                var thisindex = $(this).parent('li').index();
                mySwiper.slideTo(thisindex, 100, true);
            }
            // var thisindex = $(this).parent('li').index();
            // $(this).addClass('active').parent('li').siblings('li').find('a').removeClass('active');
            // $($('.filtrate .fi_container')[aIndex]).addClass('activate').siblings('.fi_container').removeClass('activate');
            // $('.bottom_btn_reset_submit .btn_reset').click();
            // mySwiper.updateSize();
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
            if ($('.pagenavi a.active').attr('atype') == '1') {
                TR.tx = {
                    pageNo: 1,
                    pageSize: 10,
                    loading: true,
                    being: false,
                    fidata: data
                };
            } else {
                TR.cz = {
                    pageNo: 1,
                    pageSize: 10,
                    loading: true,
                    being: false,
                    fidata: data
                };
            }
            TR.loadList(data);
        });
        $('.list_container').scroll(function() {
            var atype = $('.pagenavi a.active').attr('atype');
            var scrollTop = $(this).scrollTop();
            var objHeight = $(this).height();
            var scrollHeight = $('.list_box[atype="' + atype + '"]').height();
            if (scrollTop > 100) {
                $('.back_to_top').fadeIn();
            } else {
                $('.back_to_top').fadeOut();
            }
            if (scrollHeight <= scrollTop + objHeight) {
                var curs = atype == '1' ? TR.tx : TR.cz;
                if (curs.loading) {
                    if (curs.being) {
                        return;
                    }
                    curs.being = true;
                    curs.pageNo++;
                    TR.loadList(curs.fidata);
                } else {
                    if (curs.pageNo == 1 && $('.list_box[atype="' + atype + '"] .baseline').length == 0 && $('.list_box[atype="' + atype + '"] .nodatas').length == 0) {
                        $('.list_box[atype="' + atype + '"]').append('<fieldset class="baseline"><legend>没有啦</legend></fieldset>');
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

        $('.list_box').on('click', '.showMore', function() {
            if ($(this).hasClass('notclick')) {
                return;
            }
            var content = $(this).attr('con');
            if (content) {
                layer.open({
                    content: content,
                    skin: 'footer'
                });
            }
        });
    },
    loadList: function(option, records) {
        var atype = records || $('.pagenavi a.active').attr('atype');
        var curs = atype == '1' ? TR.tx : TR.cz;
        var param = $.extend({}, {
            pageNo: curs.pageNo,
            pageSize: curs.pageSize,
            record: atype
        }, option);
        this.ajax.post('/member/member-record', param, function(data) {
            // console.log(data);
            if (param.pageNo == 1) {
                $('.list_box[atype="' + atype + '"]').html('');
            }
            if (data.code == '2018') {
                if (data.total == 0) {
                    $('.list_box[atype="' + atype + '"]').html('<img src="img/nodata.png" class="nodatas">');
                } else {
                    $.each(data.result, function(i, item) {
                        var html = '';
                        if (atype == '1') {
                            var withdrawnType = '*';
                            switch (item.withdrawnType) {
                                case '0':
                                    withdrawnType = '本地账户';
                                    break;
                                case '1':
                                    withdrawnType = '返利账户';
                                    break;
                                default:
                                    withdrawnType = '*';
                            };
                            var state = '*';
                            switch (item.state) {
                                case '0':
                                    state = '等待处理';
                                    break;
                                case '1':
                                    state = '审核中';
                                    break;
                                case '2':
                                    state = '支付完成';
                                    break;
                                case '-1':
                                    state = '已取消';
                                    break;
                                case '-2':
                                    state = '审批拒绝';
                                    break;
                                default:
                                    state = '*';
                            }
                            html = '<div class="each_box"><img class="order_type" src="img/bq.png">' +
                                '<div class="order_type">' + state + '</div>' +
                                '<div class="timers">' +
                                '<label>' + item.time + '</label>' +
                                '<label class="fr f12">' + withdrawnType + '</label>' +
                                '</div>' +
                                '<div class="con_part">' +
                                '<p><i class="iconfont icon-dingdanhao f12 mR5"></i>' +
                                '<label>' + item.number + '</label>' +
                                '</p>' +
                                '<p><i class="iconfont icon-jine1 f12 mR5"></i>' +
                                '<label>' + item.money + ' 点</label>' +
                                '</p>' +
                                '<p class="mB5"><i class="iconfont icon-qianbaodizhi f12 mR5"></i>' +
                                '<label>' + item.moneyAddress + '</label>' +
                                '</p>' +
                                '<div class="clear mores">' +
                                '<a href="javascript:void(0);" con="' + (item.remark ? item.remark : '暂无') + '" class="showMore">相关备注</a>' +
                                '<a href="javascript:void(0);" con="' + (item.resultRemark ? item.resultRemark : '暂无') + '" class="showMore">审核意见</a></div></div></div>';
                        } else {
                            var types = '*';
                            $.each(jsons.coin, function(j, jtem) {
                                if (jtem.value == item.type) {
                                    types = jtem.name;
                                    return false;
                                }
                            });
                            var states = '*';
                            var surepay = '';
                            switch (item.state) {
                                case '0':
                                    states = '等待支付';
                                    surepay = '<a href="javascript:void(0);" onclick="TR.surePay(this, \'' + item.number + '\');">确认支付</a>';
                                    break;
                                case '1':
                                    states = '支付成功';
                                    break;
                                case '-1':
                                    states = '审核中';
                                    break;
                                case '2':
                                    states = '充值成功';
                                    break;
                                case '-2':
                                    states = '充值失败';
                                    break;
                                default:
                                    states = '*';
                            };
                            html = '<div class="each_box"><img class="order_type" src="img/bq.png">' +
                                '<div class="order_type">' + states + '</div>' +
                                '<div class="timers">' +
                                '<label>' + item.time + '</label>' +
                                '<label class="fr f12">' + types + '</label>' +
                                '</div>' +
                                '<div class="con_part">' +
                                '<p><i class="iconfont icon-dingdanhao f12 mR5"></i>' +
                                '<label>' + item.number + '</label>' +
                                '</p>' +
                                '<p><i class="iconfont icon-jine1 f12 mR5"></i>' +
                                '<label>' + item.money + ' 点</label>' +
                                '</p>' +
                                '<p class="mB5"><i class="iconfont icon-icontag f12 mR5"></i>' +
                                '<label>' + item.discounts + ' 点</label>' +
                                '</p>' +
                                '<div class="clear mores">' + surepay +
                                '<a href="javascript:void(0);" con="' + (item.resultRemark ? item.resultRemark : '暂无') + '" class="showMore" style="' + (surepay ? '' : 'width: 100%;border-right: none;') + '" disabled>审核意见</a></div></div></div>';
                        }
                        $('.list_box[atype="' + atype + '"]').append(html);
                    });
                    curs.being = false;
                    if (data.result.length < param.pageSize) {
                        curs.loading = false;
                        if (param.pageNo != 1 && $('.list_box[atype="' + atype + '"] .baseline').length == 0 && $('.list_box[atype="' + atype + '"] .nodatas').length == 0) {
                            $('.list_box[atype="' + atype + '"]').append('<fieldset class="baseline"><legend>没有啦</legend></fieldset>');
                        }
                    }
                }
            } else {
                curs.being = false;
                layer.open({
                    content: '数据加载失败，请重试',
                    skin: 'msg',
                    time: 2
                });
            }
        }, function(e) {
            curs.being = true;
            layer.open({
                content: '网络正在开小差',
                skin: 'msg',
                time: 2
            });
        });
    },
    surePay: function(e, oId) {
        if ($(e).hasClass('notclick')) {
            return;
        }
        this.ajax.post('/member/member-pay', {
            number: oId
        }, function(data) {
            if (data.code == '2018') {
                $('.list_box[atype="0"] .mores a').addClass('notclick');
                layer.open({
                    content: '确认支付提交成功，请耐心等待工作人员处理',
                    skin: 'msg',
                    time: 2
                });
                setTimeout(function() {
                    TR.cz.pageNo = '1';
                    TR.loadList(TR.cz.fidata);
                    // $('.list_box[atype="0"] .mores a').removeClass('notclick');
                }, 1500);
            } else if (data.code == '1117') {
                $('.list_box[atype="0"] .mores a').addClass('notclick');
                layer.open({
                    content: '您没有在规定时间内完成操作，订单已失效',
                    skin: 'msg',
                    time: 2
                });
                setTimeout(function() {
                    TR.cz.pageNo = '1';
                    TR.loadList(TR.cz.fidata);
                    // $('.list_box[atype="0"] .mores a').removeClass('notclick');
                }, 1500);
            } else {
                layer.open({
                    content: '确认支付提交失败',
                    skin: 'msg',
                    time: 2
                });
            }
        });
    },
};
