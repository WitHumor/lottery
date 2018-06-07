var public = {
    init: function() {
        if ($('#thisisindex').length > 0 || $('#thisismine').length > 0) {
            $('.vessel').append('<div class="navBottom">' +
                '<div class="items btn-click" htmls="index"><i class="iconfont icon-zhuye"></i><span class="icon-name">首页</span></div>' +
                '<div class="items"><a class="cd-bouncy-nav-trigger" href="javascript:void(0);"><i class="iconfont icon-jiahao"></i></a></div>' +
                '<div class="items btn-click" htmls="mine"><i class="iconfont icon-wode"></i><span class="icon-name">我的</span></div>' +
                '<div class="cd-bouncy-nav-modal">' +
                '<nav>' +
                '<ul class="cd-bouncy-nav">' +
                '<li><a class="cz" href="">充值</a></li>' +
                '<li><a class="tx" href="">提现</a></li>' +
                '</ul>' +
                '</nav>' +
                '<a href="javascript:void(0);" class="cd-close">Close modal</a>' +
                '</div>' +
                '</div>');
            if ($('#thisisindex').length > 0) {
                $('.navBottom .items[htmls="index"]').addClass('active');
            } else {
                $('.navBottom .items[htmls="mine"]').addClass('active');
            }
            $('.navBottom .btn-click').on('click', function() {
                if ($(this).hasClass('active')) {
                    window.location.reload();
                } else {
                    // $(this).addClass('ative').siblings().removeClass('active');
                    if ($(this).attr('htmls')) {
                        window.location.href = $(this).attr('htmls') + '.html';
                    }
                }
            });
        }
        // 在线咨询
        $('body').append('<script>var _hmt = _hmt || [];(function() {var hm = document.createElement("script");hm.src = "https://hm.baidu.com/hm.js?bdcca757f17f3439b840ebb0a44084a2";var s = document.getElementsByTagName("script")[0];s.parentNode.insertBefore(hm, s);})();</script>');
    },
};

$(function() {
    public.init();
});
