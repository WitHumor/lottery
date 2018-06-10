$(function() {
    register.initPage();
});

var register = {
    ajax: new HttpService(),
    icode: public.getParameter('icode'),
    initPage: function() {
        if (register.icode) {
            $('.leftBtn').html('');
            $('.about-tip').html('');
            $('.ctitle span').text('永利高注册');
            $('#invitation_code').val(register.icode).attr('readonly', 'readonly');
        } else {
            $('.leftBtn').html('<i class="iconfont icon-zhuye f24_"></i>');
            $('.about_tip').html('<span class="fr">已有账号？<a href="login.html" style="color: #FF1A1A;">在此登录</a></span>');
        }

        $('.icon-zhuye').click(function() {
            window.location.href = 'index.html';
        })
        $('.to_register').click(function() {
            register.userRegister();
        });
    },
    userRegister: function(flags) {
        var regname = $('#reg_name').val(),
            regpass = $('#reg_pass').val(),
            truename = $('#true_name').val(),
            withpass = $('#with_pass').val(),
            invitationcode = $('#invitation_code').val();
        if (public.checkinput('r')) {
            var reqData = {
                name: regname,
                password: hex_sha1(regpass),
                real_name: truename,
                bank_password: hex_sha1(withpass),
            };
            if (invitationcode) {
                reqData.invitation_code = invitationcode;
            }
            this.ajax.post('/member/add-member', reqData, function(data) {
                if (data.code == '2018') {
                    $('.to_register').attr('disabled', 'disabled');
                    sessionStorage.setItem('userinfo', JSON.stringify(data.result));
                    sessionStorage.setItem('toid', data.result.token);
                    layer.open({
                        content: '注册成功',
                        skin: 'msg',
                        time: 2
                    });
                    setTimeout(function() {
                        layer.closeAll();
                        if (register.icode) {
                            layer.open({
                                type: 1,
                                content: '<div class="modales"><p>恭喜您，您已成为永利高的会员，为了让您有更好的用户体验，请尽量使用电脑访问以下地址进行娱乐</p><p class="netaddr"><input id="copy-text" value="'+ BasePath +'" readonly /><button>复 制</button></p></div>',
                                shadeClose: false,
                                anim: 'up',
                                style: 'position:fixed; bottom:10%; left:15px; right:15px; padding:10px 0; border:none;border-radius:10px;',
                                success: function(elem) {
                                    $('.netaddr button').click(function() {
                                        var ctxt = document.getElementById("copy-text");
                                        ctxt.select();
                                        document.execCommand("Copy");
                                        layer.open({
                                            content: '已复制，可贴粘',
                                            skin: 'msg',
                                            time: 2
                                        });
                                    });
                                }
                            });
                        } else {
                            window.location.href = 'index.html';
                        }

                    }, 2000);
                } else {
                    layer.open({
                        content: '注册失败',
                        skin: 'msg',
                        time: 2
                    });
                }
            }, function(e) {
                layer.open({
                    content: '网络正在开小差',
                    skin: 'msg',
                    time: 2
                });
                console.log(e);
            });
        };
    },
}
