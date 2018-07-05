$(function() {
    mine.initPage();
});

var mine = {
    ajax: new HttpService(),
    initPage: function() {
        if (sessionStorage.getItem('userinfo')) {
            var names = JSON.parse(sessionStorage.getItem('userinfo')).name;
            $('.account').text(names);
        }
        mine.refreshAccount();

        $('.refresh_sum').click(function() {
            $('.residual p').text('**** 点');
            mine.refreshAccount();
        });
        $('.exit-btn button').click(function() {
            mine.loginout();
        });
        $('.yqm_about').click(function() {
            layer.open({
                className: "yqm_tip",
                title: '我的邀请二维码',
                content: '<div class="y_tips">Tip：受邀会员每月下注流水总额的1%作为推广返利！</div><div id="qrcode"></div>',
                success: function() {
                    new QRCode(document.getElementById("qrcode"), {
                        text: public.basePath + "/register.html?icode=" + (JSON.parse(sessionStorage.getItem('userinfo')).name || ""),
                        width: 150,
                        height: 150,
                        colorDark: "#222222",
                        colorLight: "#ffffff",
                        correctLevel: QRCode.CorrectLevel.H
                    });
                },
            });
        });
    },
    refreshAccount: function() {
        this.ajax.get('/member/member-money', {}, function(data) {
            if (data.code == '2018') {
                $('.account').text(data.result.name);
                $('#localSum').text(data.result.sum + ' 点');
                $('#rebateSum').text(data.result.rebate + ' 点');
            }
        });
    },
    loginout: function() {
        this.ajax.post('/member/exit-member', {}, function(data) {
            if (data.code == '2018') {
                sessionStorage.setItem('userinfo', '');
                sessionStorage.setItem('toid', '');
                setTimeout(function() {
                    layer.closeAll();
                    window.location.href = 'index.html';
                }, 200);
            }
        });

    },
}
