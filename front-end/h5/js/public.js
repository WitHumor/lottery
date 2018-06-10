var ServerUrl = 'http://www.xrp-candy.com/springBoot';
// var ServerUrl = 'http://192.168.43.20:8080';
// var ServerUrl = 'http://172.20.10.2:8080';
var BasePath = 'http://ylg51888.com';
// var layer = '';
// layui.use('layer', function() {
//     var $ = layui.jquery;
//     layer = layui.layer;
// });
var HttpService = function() {
    this.MAX_VALUE = 100000;
    var TYPE = {
        "post": "POST",
        "get": "GET"
    };

    var _ajax = function(type, url, data, succ, failed, bSend) {
        var userinfo = sessionStorage.getItem("userinfo");
        var toid = null;
        var index = '';
        if (userinfo) {
            toid = JSON.parse(userinfo).token
        }

        $.ajax({
            headers: {
                "content-type": "application/x-www-form-urlencoded",
                "token": toid,
                "accept": "application/json, text/javascript, */*; q=0.01"
            },
            type: type,
            url: ServerUrl + url,
            dataType: "json",
            contentType: "application/x-www-form-urlencoded",
            data: data,
            beforeSend: function() {
                if (typeof(bSend) == "function") {
                    bSend();
                } else {
                    index = layer.open({
                        type: 2,
                        shade: 'background-color: rgba(0,0,0,.3)'
                    });
                }
            },
            success: function(data) {
                var codeArr = ['1109', '1114', '1121', '1122'];
                if (codeArr.indexOf(data.code) > -1) {
                    if ($('.mine').length > 0) {
                        setTimeout(function() {
                            $('.mine').remove();
                        }, 500);
                    }
                    if (data.code == '1109' || data.code == '1114') {
                        layer.open({
                            content: '登录超时，请重新登陆',
                            skin: 'msg',
                            time: 2
                        });
                    } else if (data.code == '1121') {
                        layer.open({
                            content: '您的账号已在其他地方登陆，请重新登录',
                            skin: 'msg',
                            time: 2
                        });
                    } else if (data.code == '1122') {
                        layer.open({
                            content: '同网络下只能有一个账户活跃，您已被迫下线',
                            skin: 'msg',
                            time: 2
                        });
                    }
                    sessionStorage.setItem('userinfo', '');
                    sessionStorage.setItem('toid', '');
                    setTimeout(function() {
                        window.location.href = 'home.html';
                    }, 2000);
                    return;
                }

                if (typeof(succ) == "function") {
                    return succ(data);
                } else {
                    console.log("the method is no a function!");
                }
            },
            error: function(error) {
                if (typeof(failed) == "function") {
                    failed(error);
                } else {
                    layer.open({
                        content: '网络正在开小差',
                        skin: 'msg',
                        time: 2
                    });
                }
            },
            complete: function() {
                layer.close(index);
            }
        });
    };
    this.get = function(url, data, succ, failed, bSend) {
        return _ajax(TYPE.get, url, data, succ, failed, bSend);
    }
    this.post = function(url, data, succ, failed, bSend) {
        return _ajax(TYPE.post, url, data, succ, failed, bSend);
    }
}

var public = {
    ajax: new HttpService(),
    getParamFromUrl: function(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    },
    //返回url中的参数对应值
    getParameter: function(key) {
        var str,
            result = '',
            pageUrl = window.location.href,
            parameter = pageUrl.substring(pageUrl.indexOf('?') + 1, pageUrl.length);
        parameter = parameter.split('&');
        for (i in parameter) {
            var str = parameter[i].split('=');
            if (str[0] == key) {
                result = str[1];
                break;
            }
        }
        return result;
    },
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

        $('.icon-kefu').on('click', function() {
            $('#nb_icon_wrap').click();
        });

        $('.total_box input').bind("input propotychange", function() {
            if ($(this).val()) {
                $(this).siblings('i.clear_all').show();
            } else {
                $(this).siblings('i.clear_all').hide();
            }
        });

        $('.total_box i.clear_all').on('click', function() {
            $(this).siblings('input').val('');
            $(this).hide();
        });

        $('.total_box i.eyes').on('click', function() {
            if ($(this).hasClass('icon-yanjing')) {
                $(this).removeClass('icon-yanjing').addClass('icon-iconcloseeye');
                $(this).siblings('input').attr('type', 'password');
            } else {
                $(this).removeClass('icon-iconcloseeye').addClass('icon-yanjing');
                $(this).siblings('input').attr('type', 'text');
            }
        });
    },
    checkinput: function(type) {
        var reg1 = /^[a-zA-Z][a-zA-Z0-9]{5,9}$/,
            reg2 = /^[a-zA-Z][a-zA-Z0-9]{5,11}$/,
            reg3 = /^[0-9]{4}$/,
            reBool = true;
        $.each($('.total_box input[need]'), function(i, item) {
            var ivalue = $(item).val(),
                itype = $(item).attr('itype'),
                istr = $(item).attr('istr'),
                tips = $(item).siblings('.total_tip').find('.tip_text');
            if (!ivalue) {
                tips.text(istr + '必须填写').removeClass('smile').addClass('cry').show();
                $(item).addClass('deposit-m');
                reBool = false;
                return;
            }
            if (itype == 'account') {
                if (!reg1.test(ivalue)) {
                    tips.text(istr + '由6~10个字母和数字组合的字符，以字母开头').removeClass('smile').addClass('cry').show();
                    $(item).addClass('deposit-m');
                    reBool = false;
                    return;
                }
                if (type == 'r') {
                    public.ajax.get('/member/verify-name', {
                        name: ivalue
                    }, function(data) {
                        if (data.code == '2018') {

                        } else {
                            tips.text(istr + '账号已存在，请重新填写').removeClass('smile').addClass('cry').show();
                            $(item).addClass('deposit-m');
                            reBool = false;
                            return;
                        }
                    }, function(e) {
                        console.log(e);
                        layer.msg('用户名验证失败', {
                            time: 2000,
                            icon: 2
                        });
                    });
                }
            }
            if (itype.indexOf('password') > -1) {
                if (!reg2.test(ivalue)) {
                    tips.text(istr + '由6~12个字母和数字组合的字符，以字母开头').removeClass('smile').addClass('cry').show();
                    $(item).addClass('deposit-m');
                    reBool = false;
                    return;
                }
            }
            if (itype.indexOf('withpass') > -1) {
                if (!reg3.test(ivalue)) {
                    tips.text(istr + '由4位纯数字组成').removeClass('smile').addClass('cry').show();
                    $(item).addClass('deposit-m');
                    reBool = false;
                    return;
                }
            }
            if (type == 'w' && itype.indexOf('cipher') > -1) {
                var before = $(item).parents('.input_box').prev('.input_box').find('input').val();
                if (ivalue == before) {
                    tips.text('旧密码与新密码不能相同').removeClass('smile').addClass('cry').show();
                    $(item).addClass('deposit-m');
                    reBool = false;
                    return;
                }
            }
            if (itype.indexOf('surepass') > -1) {
                var before = $(item).parents('.input_box').prev('.input_box').find('input').val();
                if (ivalue == before) {
                    tips.text('两次密码输入不一致，请确认').removeClass('smile').addClass('cry').show();
                    $(item).addClass('deposit-m');
                    reBool = false;
                    return;
                }
            }
            tips.removeClass('cry').addClass('smile').hide();
            $(item).removeClass('deposit-m');
        });
        return reBool;
    },
};

$(function() {
    public.init();
});
