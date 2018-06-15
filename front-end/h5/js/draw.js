$(function() {
    draw.initPage();
});

var draw = {
    ajax: new HttpService(),
    initPage: function() {
        public.currency('1', 'first');
        public.payInput('1');
        $('#countDian').next('i.clear_all').on('click', function() {
            $(this).siblings('input').val('');
            $(this).hide();
            public.aboutChange('1');
            public.checkinput({
                domlist: $('#countDian')
            });
        });
        $('#walletType').click(function() {
            public.getManyType({
              type: 'walletType',
              text: '选择钱包',
              length: 2
            }, function(data) {
                $('#walletType').val(data.text).attr('vals', data.value);
            })
        });
        $('#coinType').click(function() {
            public.getManyType({}, function(data) {
                $('#coinType').val(data.text).attr('vals', data.value);
                public.currency('1');
            })
        });
        $('i.icon-shuaxin2').click(function() {
            public.currency('1');
        });
        $('.to_submit').click(function() {
            if (public.checkinput()) {
                if ($('#biQuota').val() == '') {
                    layer.open({
                        content: '提现币额有误，请确认',
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
                        var param = {
                            currency: $('#coinType').attr('vals'),
                            money: $('#countDian').val(),
                            phone: $('#telephone').val(),
                            address: $('#walletAddress').val(),
                            password: hex_sha1($('#drawPassword').val()),
                            withdrawnType: $('#walletType').attr('vals')
                        };
                        if ($('#beizhu').val()) {
                            param.remark = $('#beizhu').val();
                        }
                        draw.ajax.post('/member/member-withdrawn', param, function(data) {
                            if (data.code == '2018') {
                                $('.to_submit').attr('disabled', 'disabled');
                                layer.open({
                                    content: '成功生成取款订单，请耐心等待工作人员处理',
                                    skin: 'msg',
                                    time: 2
                                });
                                setTimeout(function() {
                                    window.location.reload();
                                }, 1500);
                            } else if (data.code == '1103') {
                                layer.open({
                                    content: '余额不足',
                                    skin: 'msg',
                                    time: 2
                                });
                            } else if (data.code == '1108') {
                                layer.open({
                                    content: '取款密码错误',
                                    skin: 'msg',
                                    time: 2
                                });
                            } else {
                                layer.open({
                                    content: '取款订单生成失败',
                                    skin: 'msg',
                                    time: 2
                                });
                            }
                        });
                    },
                });
            }
        });
    },
};
