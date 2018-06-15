var regs = /^([1-9]\d*|0)(\.\d+)?$/; ///^[+]{0,1}(\d+)$|^[+]{0,1}(\d+\.\d+)$/
$(function() {
    DE.initPage();
});

var DE = {
    ajax: new HttpService(),
    // booltf: true,
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
                if ($('#total-charge').val() == '') {
                    layer.msg('充值金额有误，请确认', {
                        time: 2000
                    });
                } else {
                    // if (DE.booltf) {
                    //     DE.booltf = false;
                        DE.currency('verify');
                    // } else {
                    //     layer.msg('订单正在处理，请勿重复提交', {
                    //         time: 2000
                    //     });
                    // }
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

        $('.i-tate').click(function() {
            DE.currency('sign');
        });

        // 全局设置
        ZeroClipboard.setDefaults({
            moviePath: '../../js/ZeroClipboard.swf'
        });
        new ZeroClipboard([document.getElementById("btn-copy-address"), document.getElementById("btn-copy-order")]).on('complete', function(client, args) {
            // layer.tips('成功复制：'+$('#btn-copy-address').attr('data-clipboard-text'), '#btn-copy-address');
            layer.msg('内容已经复制，你可以使用Ctrl+V 粘贴！');
        });
    },
    checks: function() {
        var rechargebtb = $('#rechargebtb').val(),
            ciontype = $('#coin-type').val(),
            allCoin = {
                BTC: 0.003,
                EOS: 2,
                ETH: 0.03,
                XRP: 30,
                LTC: 0.2,
                QTUM: 2,
                AppleC: 100
            },
            rdata = {
                type: false
            };
        if (!rechargebtb) {
            rdata.value = '请输入充值币额';
        } else if (rechargebtb < allCoin[ciontype]) {
            rdata.value = $('#coin-type option:checked').text().split('（')[0] + ' 单笔充值最低币额为' + allCoin[ciontype] + '个';
        } else if (!regs.test(rechargebtb)) {
            rdata.value = '请输入正确的充值币额';
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
        $('.limit-tip span[ctype="' + ciontype + '"]').addClass('active').siblings('span').removeClass('active');
        $('.coin-name').text(cionname);
        if (sign == 'sign') {
            $('.cny-btc-rate').text('-');
            $('#btb-rmb').val('');
            $('#charge-dis').val('');
            $('#total-charge').val('');
        }
        if (ciontype == 'AppleC') {
            $('.cny-btc-rate').text(1);
            DE.toNext(sign,'1',(DE.firstpay ? '0' : ''));
            return;
        }
        this.ajax.post('/member/money-exchange', {
            record: '0',
            currency: ciontype.toLowerCase()
        }, function(data) {
            if (data.code == '2018') {
                var real = data.result.exchange.replace(' CNY', '').replace(/,/g, '');
                if (!sign) {
                    $('.cny-btc-rate').text(real || '-');
                    if (data.result.total == '0') {
                        DE.firstpay = true;
                        $('.dis-before').after('<div class="form-group"><label class="col-2 control-label">首存特惠：</label><div class="col-7"><input id="discounts" type="text" class="form-control" readonly placeholder="首存即送 188 点"></div><div class="col-3 btnA btnRed"><a class="pointer-link"><span class="hongb">188</span>点</a></div></div>');
                    }
                } else {
                    DE.toNext(sign, real, data.result.total);
                }
            } else {
                layer.msg('汇率异常，请刷新后再试', {
                    time: 2000,
                    icon: 2
                });
            }
        }, function(e) {
            // DE.booltf = true;
            layer.msg('网络错误', {
                time: 2000,
                icon: 2
            });
        });
    },
    toNext: function(sign, real, total) {
        if (sign == 'sign') {
            $('.cny-btc-rate').text(real || '-');
            DE.allchange(total);
            DE.checks();
        } else {
            // if (real == $('.cny-btc-rate').text()) {
            //     DE.createOrder();
            // } else {
            //     layer.msg('汇率已发生变化，请重新提交', {
            //         time: 2000
            //     });
            // }
            var index = layer.open({
                type: 1,
                title: false,
                closeBtn: false,
                area: '300px;',
                shade: 0.8,
                id: 'LAY_layuipro',
                btn: ['接受', '拒绝'],
                content: '<div style="padding: 30px; line-height: 22px; background-color: #393D49; color: #fff; font-weight: 300;">汇率实时变化可能会导致充值的点数有所变化，您是否接受？</div>',
                yes: function() {
                    layer.close(index);
                    DE.createOrder();
                },
                cancel: function() {
                    if (real != $('.cny-btc-rate').text()) {
                        $('.cny-btc-rate').text(real || '-');
                        DE.allchange(total);
                    }
                }
            });
            // DE.booltf = true;
        }
    },
    allchange: function(fi) {
        var current = parseFloat($('#rechargebtb').val()),
            btbrate = parseFloat($('.cny-btc-rate').text());
        $('#discounts').val('188.00 点');
        if (!regs.test(current) || isNaN(btbrate)) {
            $('#btb-rmb').val('');
            $('#charge-dis').val('');
            $('#total-charge').val('');
            return;
        }
        var btb_rmb = parseFloat((btbrate * current).toFixed(2)),
            charge_dis = parseFloat((btb_rmb * 0.01).toFixed(2));
        $('#btb-rmb').val(btb_rmb.toFixed(2) + ' 点');
        $('#charge-dis').val(charge_dis.toFixed(2) + ' 点');
        $('#total-charge').val((btb_rmb + charge_dis + (fi == '0' ? 188.00 : 0)).toFixed(2) + ' 点');
    },
    createOrder: function() {
        var coinType = $('#coin-type').val(),
            coinName = $('#coin-type option:checked').text(),
            coinNum = $('#rechargebtb').val();
            // rmb = $('#btb-rmb').val().replace(' 点', ''),
            // onSale = (parseFloat($('#charge-dis').val().replace(' 点', '')) + (DE.firstpay ? 188.00 : 0)) + '';
        this.ajax.post('/member/member-deposit', {
            currency: coinType,
            currencyCount: coinNum
        }, function(data) {
            if (data.code == '2018') {
                $('.btn-next').attr('disabled', 'disabled');
                $('#can-dis').val(data.result.discounts + ' 点');
                $('#order-num').val(data.result.number);
                $('#btn-copy-order').attr('data-clipboard-text', data.result);
                $('#re-amount').val(coinNum + ' ' + coinName);
                $('#pay-rmb').text(data.result.money + '点');
                $('#firsts').hide();
                $('#nexts').show();
                $('#nexts .finish-div').html('<button class="btn-submit btn-finish" onclick="DE.finishPay();">完成支付</button>');
                layer.alert('<span class="transfer2">转账时请务必在备注中填写您的订单号</span>，转账时请务必在备注中填写您的订单号，转账时请务必在备注中填写您的订单号；<br>重要的事情说三遍，<span class="transfer3">请务必记住！！！</span><br><span  class="transfer3">若由于您的错误操作导致了损失，本网站概不负责！', {
                    skin: 'layui-layer-molv',
                    title: '转账须知',
                    area: '370px;',
                    move: false,
                    scrollbar: false,
                    btn: ['我记下了'],
                    closeBtn: 0,
                });
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
            $('.btn-finish').attr('disabled', 'disabled');
            if (data.code == '2018') {
                layer.msg('完成支付提交成功，请耐心等待工作人员处理', {
                    time: 2000,
                    icon: 1
                });
                setTimeout(function() {
                    window.location.href = 'tradingrecord.html?type=deposit';
                }, 2000);
            } else if (data.code == '1117') {
                layer.msg('您没有在规定时间内完成操作，订单已失效', {
                    time: 2000,
                    icon: 0
                });
                setTimeout(function() {
                    $('.btn-finish').removeAttr('disabled');
                    window.location.reload();
                }, 2000);
            } else {
                $('.btn-finish').removeAttr('disabled');
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
