var regs = /^[+]{0,1}(\d+)$|^[+]{0,1}(\d+\.\d+)$/;
$(function() {
    DE.initPage();
});

var DE = {
    ajax: new HttpService(),
    booltf: true,
    firstpay: false,
    initPage: function() {
        DE.currency();
        $('#rechargebtb').bind('input propertychange', function() {
            DE.allchange();
            DE.checks();
        }).blur(function() {
            DE.checks();
        }).focus(function() {
            $('#rechargebtb').removeClass('deposit-m');
        });

        $('.btn-next').click(function() {
            var t = DE.checks();
            if (t) {
                if ($('#total-charge').val() == 'NaN 点' || $('#total-charge').val() == '') {
                    layer.msg('充值金额有误，请确认', {
                        time: 2000
                    });
                } else {
                    if (DE.booltf) {
                        DE.booltf = false;
                        DE.currency('verify');
                    } else {
                        layer.msg('订单正在处理，请勿重复提交', {
                            time: 2000
                        });
                    }
                }
            }
        });

        $('.paytype li[litype="BTB"]').click(function() {
            $('#firsts').show();
            $('#nexts').hide();
            $('#nexts .finish-div').html('');
        });

        $('#coin-type').change(function() {
            DE.currency('sign');
        });

        // 全局设置
        ZeroClipboard.setDefaults({
            moviePath: '../../js/ZeroClipboard.swf'
        });
        new ZeroClipboard([document.getElementById("btn-copy-address"), document.getElementById("btn-copy-amount")]).on('complete', function(client, args) {
            // layer.tips('成功复制：'+$('#btn-copy-address').attr('data-clipboard-text'), '#btn-copy-address');
            layer.msg('内容已经复制，你可以使用Ctrl+V 粘贴！');
        });
    },
    checks: function() {
        var btb = $('#rechargebtb').val();
        var rdata = {
            type: false
        };
        if (!btb) {
            rdata.value = '请输入充值金额';
        } else if (btb > 200) {
            rdata.value = '单笔充值最高金额 200 个数字货币';
        } else if (btb < 0.1) {
            rdata.value = '单笔充值最低金额 0.1 个数字货币';
        } else if (!regs.test(btb)) {
            rdata.value = '请输入正确的充值金额';
        } else {
            rdata.type = true;
        }

        if (rdata.type) {
            $('.btc-tip').text('').hide();
            $('#rechargebtb').removeClass('deposit-m');
            $('.btn-next').removeClass('disabled');
        } else {
            $('.btc-tip').text(rdata.value).show();
            $('#rechargebtb').addClass('deposit-m');
            $('.btn-next').addClass('disabled');
        }
        return rdata.type;
    },
    currency: function(sign) {
        var ciontype = $('#coin-type').val(),
            cionname = $('#coin-type option:checked').text();
        $('.coin-name').text(cionname);
        if (sign == 'sign') {
            $('.cny-btc-rate').text('-');
            $('#btb-rmb').val('');
            $('#charge-dis').val('');
            $('#total-charge').val('');
        }
        // $.ajaxSettings.async = false;
        this.ajax.post('/member/money-exchange', {
            record: '0',
            currency: ciontype.toLowerCase()
        }, function(data) {
            console.log(data);
            if (data.code == '2018') {
                var real = data.result.exchange.replace(' CNY', '').replace(/,/g, '');
                if (!sign) {
                    $('.cny-btc-rate').text(real || '-');
                    if (data.result.total == '0') {
                        DE.firstpay = true;
                        $('.dis-before').append('<div class="form-group"><label class="col-2 control-label">首存特惠：</label><div class="col-7"><input id="discounts" type="text" class="form-control" readonly placeholder="首存即送 188 点"></div><div class="col-3 btnA btnRed"><a class="pointer-link"><span class="hongb">188</span>点</a></div></div>');
                    }
                } else {
                    if (sign == 'sign') {
                        $('.cny-btc-rate').text(real || '-');
                        DE.allchange(data.result.total);
                    } else {
                        if (real == $('.cny-btc-rate').text()) {
                            DE.createOrder();
                        } else {
                            layer.msg('汇率已发生变化，请重新提交', {
                                time: 2000
                            });
                            $('.cny-btc-rate').text(real || '-');
                            DE.allchange(data.result.total);
                        }
                        DE.booltf = true;
                    }
                }
            } else {
                layer.msg('汇率异常，请稍后再试', {
                    time: 2000,
                    icon: 2
                });
            }
        }, function(e) {
            layer.msg('网络错误', {
                time: 2000,
                icon: 2
            });
        });
        // $.ajaxSettings.async = true;
    },
    allchange: function(fi) {
        var current = 0,
            btbrate = parseFloat($('.cny-btc-rate').text());
        // if (!$('#rechargebtb').val() || !regs.test($('#rechargebtb').val())) {
        //     current = 0;
        // } else {
        current = parseFloat($('#rechargebtb').val());
        // }
        var btb_rmb = parseFloat((btbrate * current).toFixed(2)),
            charge_dis = parseFloat((btb_rmb * 0.01).toFixed(2));
        $('#btb-rmb').val(btb_rmb.toFixed(2) + ' 点');
        $('#discounts').val('188.00 点');
        $('#charge-dis').val(charge_dis.toFixed(2) + ' 点');
        $('#total-charge').val((btb_rmb + charge_dis + (fi == '0' ? 188.00 : 0)).toFixed(2) + ' 点');
    },
    createOrder: function() {
        var coinType = $('#coin-type').val(),
            coinName = $('#coin-type option:checked').text(),
            coinNum = $('#rechargebtb').val(),
            rmb = $('#btb-rmb').val().replace(' 点', ''),
            onSale = (parseFloat($('#charge-dis').val().replace(' 点', '')) + (DE.firstpay ? 188.00 : 0)) + '';
        this.ajax.post('/member/member-deposit', {
            money: rmb,
            discountsMoeny: onSale,
            currency: coinType,
            currencyCount: coinNum
        }, function(data) {
            if (data.code == '2018') {
                $('#can-dis').val(onSale + ' 点');
                $('#order-num').val(data.result);
                $('#re-amount').val(coinNum + ' ' + coinName);
                $('#pay-rmb').text(rmb + '点');
                $('#firsts').hide();
                $('#nexts').show();
                $('#nexts .finish-div').html('<a class="btn-submit btn-finish" onclick="DE.finishPay();">完成支付</a>');
            } else {
                layer.msg('订单提交失败', {
                    time: 2000,
                    icon: 2
                });
            }
        }, function(e) {
            layer.msg('网络错误', {
                time: 2000,
                icon: 2
            });
        });
    },
    finishPay: function() {
        var orderNum = $('#order-num').val();
        DE.ajax.post('/member/member-pay', {
            number: orderNum
        }, function(data) {
            if (data.code == '2018') {
                layer.msg('完成支付提交成功，请耐心等待工作人员处理', {
                    time: 2000,
                    icon: 1
                });
                setTimeout(function() {
                    window.location.href = 'tradingrecord.html?type=deposit';
                }, 2000);
            } else if(data.code == '1117') {
                layer.msg('您没有在规定时间内完成操作，可联系客服处理', {
                    time: 2000,
                    icon: 2
                });
            } else {
                layer.msg('完成支付提交失败', {
                    time: 2000,
                    icon: 1
                });
            }
        }, function(e) {
            layer.msg('网络错误', {
                time: 2000,
                icon: 2
            });
        });
    },
};
