var regs = /^[+]{0,1}(\d+)$|^[+]{0,1}(\d+\.\d+)$/;
$(function() {
    DW.initPage();
});

var DW = {
    ajax: new HttpService(),
    booltf: true,
    initPage: function() {
        DW.currency();
        $('.forminput').bind('input propertychange', function() {
            DW.allchanges($(this));
            DW.checks([$(this)]);
        }).blur(function() {
            DW.checks([$(this)]);
        }).focus(function() {
            $(this).removeClass('deposit-m');
        });
        $('#bitype').change(function() {
            DW.currency();
        });
        $('.btn-submit').click(function() {
            var bitname = $('#bitype option:checked').text(),
                btb_rmb = $('#btb-rmb').val();
            if (DW.checks([$('#rechargebtb'), $('#telphone'), $('#purseaddress'), $('#withpass')])) {
                if (btb_rmb == ('NaN 个' + bitname) || btb_rmb == '') {
                    layer.msg('取款金额有误，请确认', {
                        time: 2000
                    });
                } else {
                    if (DW.booltf) {
                        DW.booltf = false;
                        DW.currency('verify');
                    } else {
                        layer.msg('订单正在处理，请勿重复提交', {
                            time: 2000
                        });
                    }
                }
            }
        });
    },
    currency: function(sign) {
        var ciontype = $('#bitype').val(),
            cionname = $('#bitype option:checked').text();
        $('.bi-type').text(cionname);
        if (!sign) {
            $('.cny-rate').text('-');
            $('#btb-rmb').val('');
        }
        // $.ajaxSettings.async = false;
        this.ajax.post('/member/money-exchange', {
            record: '1',
            currency: ciontype.toLowerCase()
        }, function(data) {
            console.log(data);
            if (data.code == '2018') {
                var real = data.result.exchange.replace(' CNY', '').replace(/,/g, '');
                if (!sign) {
                    $('.cny-rate').text(real || '-');
                    DW.allchanges($('#rechargebtb'));
                } else {
                    if (real == $('.cny-rate').text()) {
                        DW.withdrawal();
                    } else {
                        layer.msg('汇率已发生变化，请重新提交', {
                            time: 2000
                        });
                        $('.cny-rate').text(real || '-');
                        DW.allchanges($('#rechargebtb'));
                    }
                    DW.booltf = true;
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
    withdrawal: function() {
        var param = {
            money: $('#btb-rmb').val().split(' 个')[0],
            currency: $('#bitype').val(),
            currencyCount: $('#rechargebtb').val(),
            phone: $('#telphone').val(),
            address: $('#purseaddress').val(),
            password: $('#withpass').val()
        };
        if ($('#beizhu').val()) {
            param.remark = $('#beizhu').val();
        }
        this.ajax.post('/member/member-withdrawn', param, function(data) {
            console.log(data);
            if (data.code == '2018') {
                layer.msg('成功生成取款订单', {
                    time: 2000,
                    icon: 1
                });
                setTimeout(function() {
                    window.location.href = 'tradingrecord.html';
                }, 2000);
            } else if (data.code == '1103') {
                layer.msg('余额不足', {
                    time: 2000,
                    icon: 2
                });
            } else if(data.code == '1108') {
                layer.msg('取款密码错误', {
                    time: 2000,
                    icon: 2
                });
            } else {
                layer.msg('取款订单生成失败', {
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
    allchanges: function(e) {
        if (e[0] == $('#rechargebtb')[0]) {
            var current = 0,
                btbrate = parseFloat($('.cny-rate').text());
            // if (!$('#rechargebtb').val() || !regs.test($('#rechargebtb').val())) {
            //     current = 0;
            // } else {
            current = parseFloat($('#rechargebtb').val());
            // }
            var btb_rmb = parseFloat((current / btbrate).toFixed(10));
            $('#btb-rmb').val(btb_rmb.toFixed(10) + ' 个' + $('#bitype option:checked').text());
        }
    },
    checks: function(checkArr) {
        var rtype = true,
            text = {
                "in_rec": "取款金额",
                "in_tel": "电话号码",
                "in_pur": "钱包地址",
                "in_wit": "取款密码"
            };
        $.each(checkArr, function(i, item) {
            var rdata = {
                    type: false
                },
                itemval = item.val();
            if (!itemval) {
                rdata.value = '请输入' + text[item.attr('inpt')];
            } else {
                if (item[0] == $('#rechargebtb')[0]) {
                    if (itemval < 100) {
                        rdata.value = '单笔取款最低金额 100 点';
                    } else if (!regs.test(itemval)) {
                        rdata.value = '请输入正确的' + text[item.attr('inpt')];
                    } else {
                        rdata.type = true;
                    }
                } else if (item[0] == $('#telphone')[0]) {
                    var reg = /0?(13|14|15|17|18)[0-9]{9}/;
                    if (!reg.test(itemval)) {
                        rdata.value = '请输入正确的' + text[item.attr('inpt')];
                    } else {
                        rdata.type = true;
                    }
                }
                // else if (item[0] == $('#withpass')[0]) {
                //     var reg = /^[0-9]{4}$/;
                //     if (!reg.test(itemval)) {
                //         rdata.value = '请输入正确的' + text[item.attr('inpt')];
                //     } else {
                //         rdata.type = true;
                //     }
                // }
                else {
                    rdata.type = true;
                }
            }
            if (rdata.type) {
                $('.btc-tip[inpt="' + item.attr('inpt') + '"]').text('').hide();
                item.removeClass('deposit-m');
            } else {
                rtype = false
                $('.btc-tip[inpt="' + item.attr('inpt') + '"]').text(rdata.value).show();
                item.addClass('deposit-m');
            }

        });

        return rtype;
    },
};
