$(function() {
    layui.use('carousel', function() {
        var carousel = layui.carousel;
        carousel.render({
            elem: '#banners',
            width: '100%',
            arrow: 'always',
            interval: '2000',
            height: '150px'
        });

        notice($('.notice-txt'));

        function notice(ul) {
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
        }
    });
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
