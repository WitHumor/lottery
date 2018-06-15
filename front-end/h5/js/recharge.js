$(function() {
    recharge.initPage();
})

var recharge = {
    ajax: new HttpService(),
    initPage: function() {
        // 全局设置
        // ZeroClipboard.setDefaults({
        //     moviePath: 'js/ZeroClipboard.swf'
        // });
        // new ZeroClipboard([document.getElementById("btn-copy-address"), document.getElementById("btn-copy-order")],{
        //   moviePath: 'js/ZeroClipboard.swf'
        // }).on('complete', function(client, args) {
        //     layer.open({
        //         content: '内容已经复制！',
        //         skin: 'msg',
        //         time: 2
        //     });
        // });
        $('.btn-copy').click(function() {
            var ids = $(this).attr('target');
            var ctxt = document.getElementById(ids);
            ctxt.select();
            document.execCommand("Copy");
            layer.open({
                content: '已复制，可贴粘',
                skin: 'msg',
                time: 2
            });
        });
        public.currency('0', 'first');
        public.payInput('0');
        $('#payAmount').next('i.clear_all').on('click', function() {
            $(this).siblings('input').val('');
            $(this).hide();
            public.aboutChange('0');
            public.checkinput({domlist: $('#payAmount')});
        });
        $('#coinType').click(function() {
            public.getManyType({}, function(data) {
                $('#coinType').val(data.text).attr('vals', data.value);
                public.currency('0');
            })
        });

        $('.to_next').click(function() {
            if (public.checkinput()) {
                if ($('#toAccount').val() == '') {
                    layer.open({
                        content: '充值金额有误，请确认',
                        skin: 'msg',
                        time: 2
                    });
                    return;
                }
                var index = layer.open({
                    className: 'layer_edit',
                    btn: ['接受', '拒绝'],
                    content: '汇率实时变化可能会导致充值的点数有所变化，您是否接受？',
                    yes: function() {
                        layer.close(index);
                        var coinType = $('#coinType').attr('vals'),
                            coinName = $('#coinType').val(),
                            coinNum = $('#payAmount').val();
                        recharge.ajax.post('/member/member-deposit', {
                            currency: coinType,
                            currencyCount: coinNum
                        }, function(data) {
                            if (data.code == '2018') {
                                $('.step1').html('<i class="iconfont icon-selected f20"></i>');
                                $('.stepl1, .step2').removeClass('bfirsthand').addClass('bnormal');
                                var res = data.result;
                                $('#orderNum').val(res.number);
                                $('#btn-copy-order').attr('data-clipboard-text', data.result);
                                $('#getDiscounts').val(res.discounts + ' 点');
                                $('#payMoney').val(coinNum + ' ' + coinName + ' = ' + res.money + ' 点');
                                $('#firsts').hide();
                                $('#nexts').show();
                                $('#nexts .finish-div').html('<button class="to_finish bdanger" onclick="recharge.finishPay();">完 成 支 付</button>');
                                var indexs = layer.open({
                                    className: 'best_imp_tip',
                                    content: '<div class="content_tip">转账须知</div><div class="content_content"><span class="transfer">转账时请务必在备注中填写您的订单号</span>，转账时请务必在备注中填写您的订单号，转账时请务必在备注中填写您的订单号；<br>重要的事情说三遍，<span class="transfers">请务必记住！！！</span><br><span class="transfers">若由于您的错误操作导致了损失，本网站概不负责！</span></div><div class="bottom_btns"><button>我记下了</button></div>',
                                    shadeClose: false,
                                    success: function(e) {
                                        $('.bottom_btns button').click(function() {
                                            layer.close(indexs);
                                        })
                                    }
                                });
                            } else {
                                layer.open({
                                    content: '订单提交失败',
                                    skin: 'msg',
                                    time: 2
                                });
                            }
                        });
                    },
                });
            }
        });
        $('i.icon-shuaxin2').click(function() {
            public.currency('0');
        });
    },
    finishPay: function() {
        var orderNum = $('#orderNum').val();
        this.ajax.post('/member/member-pay', {
            number: orderNum
        }, function(data) {
            if (data.code == '2018') {
                $('.step2').html('<i class="iconfont icon-selected f20"></i>');
                $('.to_finish').attr('disabled', 'disabled');
                layer.open({
                    content: '完成支付提交成功，请耐心等待工作人员处理',
                    skin: 'msg',
                    time: 2
                });
                setTimeout(function() {
                    // window.location.href = 'tradingrecord.html?type=deposit';
                    window.location.reload();
                }, 1500);
            } else if (data.code == '1117') {
                layer.open({
                    content: '您没有在规定时间内完成操作，订单已失效',
                    skin: 'msg',
                    time: 2
                });
                setTimeout(function() {
                    window.location.reload();
                }, 1500);
            } else {
                layer.open({
                    content: '完成支付提交失败',
                    skin: 'msg',
                    time: 2
                });
            }
        });
    },
}
