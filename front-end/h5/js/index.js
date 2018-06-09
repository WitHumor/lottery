$(function() {
    index.initPage();
});

var index = {
    initPage: function() {
        layui.use('carousel', function() {
            var carousel = layui.carousel;
            carousel.render({
                elem: '#banners',
                width: '100%',
                arrow: 'always',
                interval: '2000',
                height: '150px'
            });

            index.notice($('.notice-txt'));

            if (!sessionStorage.getItem('toid')) {
                $('.rightBtn').html('<button class="mR5 login-btn to_login_register" onclick="">登录</button><button class="register-btn to_login_register">注册</button>');

                $('.to_login_register').on('click', function() {
                    if ($(this).hasClass('login-btn')) {
                        window.location.href = 'login.html';
                    } else if ($(this).hasClass('register-btn')) {
                        window.location.href = 'register.html';
                    }
                });
            } else {
                $('.rightBtn').html('<i class="iconfont icon-xiaoxi2 f24_"></i>');
            }
        });
    },
    notice: function(ul) {
        var li = ul.find('li').eq(0).html();
        ul.append('<li>' + li + '</li>');
        var num = 0;
        setInterval(function() {
            num++;
            if (num == ul.find('li').length) {
                num = 1;
                ul.css({
                    marginTop: 0
                });
            }
            $('.notice-txt').animate({
                marginTop: -30 * num
            }, 400);
        }, 2000);
    },
};
