$(function() {
    login.initPage();
});

var login = {
    ajax: new HttpService(),
    initPage: function() {
        $('.icon-zhuye').click(function() {
            window.location.href = 'index.html';
        })
        $('.to_login').click(function() {
            login.userLogin();
        });
    },
    userLogin: function(flags) {
        var logName = $('#login_name').val(),
            logPass = $('#login_pass').val();
        if (public.checkinput('l')) {
            var reqData = {
                "name": logName,
                "password": hex_sha1(logPass)
                    // "password": logPass
            };
            this.ajax.post('/member/login-member', reqData, function(data) {
                if (data.code == '2018') {
                    $('.to_login').attr('disabled', 'disabled');
                    sessionStorage.setItem('userinfo', JSON.stringify(data.result));
                    sessionStorage.setItem('toid', data.result.token);
                    layer.open({
                        content: '登录成功',
                        skin: 'msg',
                        time: 2
                    });
                    setTimeout(function() {
                        layer.closeAll();
                        window.location.href = 'index.html';
                    }, 2000);
                } else {
                    layer.open({
                        content: '用户名或密码错误',
                        skin: 'msg',
                        time: 2
                    });
                }
            }, function(e) {
                layer.open({
                    content: '登录失败',
                    skin: 'msg',
                    time: 2
                });
                console.log(e);
            });
        };
    },
}
