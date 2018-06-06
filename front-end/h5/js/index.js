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
            public.notice($('.notice-txt'));
        });
    },
};
