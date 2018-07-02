var ServerUrl = 'http://wap.ylg51888.com/springBoot';
// var ServerUrl = 'http://192.168.43.20:8080';
// var ServerUrl = 'http://172.20.10.2:8080';
var BasePath = 'http://www.ylg51888.com';
// var layer = '';
// layui.use('layer', function() {
//     var $ = layui.jquery;
//     layer = layui.layer;
// });

var jsons = {
    coin: [{
        name: '比特币',
        value: 'BTC',
    }, {
        name: '柚子币',
        value: 'EOS',
    }, {
        name: '以太币',
        value: 'ETH',
    }, {
        name: '瑞波币',
        value: 'XRP',
    }, {
        name: '莱特币',
        value: 'LTC',
    }, {
        name: '量子币',
        value: 'QTUM',
    }, {
        name: '苹果币',
        value: 'AppleC',
    }],
    walletType: [{
        name: '本地钱包',
        value: '0'
    }, {
        name: '返利钱包',
        value: '1'
    }],
    coinLimit: {
        BTC: 0.003,
        EOS: 2,
        ETH: 0.03,
        XRP: 30,
        LTC: 0.2,
        QTUM: 2,
        AppleC: 100
    },
    // 数据解析
    anal: {
        gheads: ["_.GameHead = [", "];"], //列表数据解析字段
        gpages: ["_.t_page = ", "_.gamount"], //列表页数
        gptotals: ["_.gamount = ", ";"], //列表每页的数据条数
        gcounts: ["_.gameCount = ", ";"], //ball-type的各个类型的数据
        glists: ["g([", "]);"], //列表数据
    },
    // 数据解析请求参数
    together: {
        uid: '41E1C90D347A90C6A60811350',
        langx: 'zh-cn',
        page_no: 0,
        mtype: 3,
        league_id: '',
        hot_game: ''
    },
};
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
                        content: '正在努力加载',
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
                        window.location.href = 'index.html';
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
    firstpay: false,
    regs1: /^([1-9]\d*|0)(\.\d+)?$/, //充值币额验证正则
    regs2: /^[+]{0,1}(\d+)$|^[+]{0,1}(\d+\.\d+)$/, //提现点数验证正则
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
    browserRedirect: function() {
        var sUserAgent = navigator.userAgent.toLowerCase();
        var bIsIpad = sUserAgent.match(/ipad/i) == "ipad";
        var bIsIphoneOs = sUserAgent.match(/iphone os/i) == "iphone os";
        var bIsMidp = sUserAgent.match(/midp/i) == "midp";
        var bIsUc7 = sUserAgent.match(/rv:1.2.3.4/i) == "rv:1.2.3.4";
        var bIsUc = sUserAgent.match(/ucweb/i) == "ucweb";
        var bIsAndroid = sUserAgent.match(/android/i) == "android";
        var bIsCE = sUserAgent.match(/windows ce/i) == "windows ce";
        var bIsWM = sUserAgent.match(/windows mobile/i) == "windows mobile";
        // if (bIsIpad || bIsIphoneOs || bIsMidp || bIsUc7 || bIsUc || bIsAndroid || bIsCE || bIsWM) {
        //     console.log('H5',window.location.href)
        //     if (window.location.href != 'http://localhost/h5/') {
        //         $('body').append('<div id="browser-redirect"><img src="img/apple.png"></div>');
        //         window.location.href = "http://localhost/h5";
        //         // window.location.href = "http://wap.ylg51888.com";
        //     }
        // } else {
        //     console.log('pc',window.location.href);
        //     if (window.location.href != 'http://localhost/html/physicalEducation/home.html') {
        //         $('body').append('<div id="browser-redirect"><img src="img/apple.png"></div>');
        //         window.location.href = 'http://localhost/html/physicalEducation/home.html';
        //         // window.location.href = "http://www.ylg51888.com";
        //     }
        // }
    },
    init: function() {
        public.browserRedirect();
        if ($('#thisisindex').length > 0 || $('#thisismine').length > 0) {
            $('.vessel').append('<div class="navBottom">' +
                '<div class="items btn-click" htmls="index"><i class="iconfont icon-zhuye"></i><span class="icon-name">首页</span></div>' +
                '<div class="items"><a class="cd-bouncy-nav-trigger" href="javascript:void(0);"><i class="iconfont icon-jiahao"></i></a></div>' +
                '<div class="items btn-click" htmls="mine"><i class="iconfont icon-wode"></i><span class="icon-name">我的</span></div>' +
                '<div class="cd-bouncy-nav-modal">' +
                '<nav>' +
                '<ul class="cd-bouncy-nav">' +
                '<li><a class="cz validate" href="javascript:void(0);" htmls="recharge">充值</a></li>' +
                '<li><a class="tx validate" href="javascript:void(0);" htmls="draw">提现</a></li>' +
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
                if ($(this).attr('htmls') == 'mine' && !sessionStorage.getItem('toid')) {
                    window.location.href = 'login.html?where=' + $(this).attr('htmls');
                    return;
                }
                if ($(this).hasClass('active')) {
                    window.location.reload();
                } else {
                    // $(this).addClass('ative').siblings().removeClass('active');
                    if ($(this).attr('htmls')) {
                        window.location.href = $(this).attr('htmls') + '.html';
                    }
                }
            });
            $('.validate').click(function() {
                if (!sessionStorage.getItem('toid')) {
                    window.location.href = 'login.html?where=' + $(this).attr('htmls');
                } else {
                    window.location.href = $(this).attr('htmls') + '.html';
                }
            });
        }
        // 在线咨询
        $('body').append('<script>var _hmt = _hmt || [];(function() {var hm = document.createElement("script");hm.src = "https://hm.baidu.com/hm.js?bdcca757f17f3439b840ebb0a44084a2";var s = document.getElementsByTagName("script")[0];s.parentNode.insertBefore(hm, s);})();</script>');

        $('#backs').click(function() {
            if (document.referrer.indexOf('login.html') > -1) {
                history.go(-2);
            } else {
                history.back(-1);
            }
        });
        $('.icon-kefu').on('click', function() {
            $('#nb_icon_wrap').click();
        });

        $('.input_box input').bind("input propotychange", function() {
            if ($(this).val()) {
                $(this).siblings('i.clear_all').show();
            } else {
                $(this).siblings('i.clear_all').hide();
            }
        });

        $('.input_box i.clear_all').on('click', function() {
            $(this).siblings('input').val('');
            $(this).hide();
        });

        $('.input_box i.eyes').on('click', function() {
            if ($(this).hasClass('icon-yanjing')) {
                $(this).removeClass('icon-yanjing').addClass('icon-iconcloseeye');
                $(this).siblings('input').attr('type', 'password');
            } else {
                $(this).removeClass('icon-iconcloseeye').addClass('icon-yanjing');
                $(this).siblings('input').attr('type', 'text');
            }
        });
    },
    checkinput: function(obj) {
        var o = $.extend({}, obj);
        var inputArr = o.domlist ? [o.domlist] : $('.input_box input[need]');
        var reg1 = /^[a-zA-Z][a-zA-Z0-9]{5,9}$/, //用户名验证正则
            reg2 = /^[a-zA-Z][a-zA-Z0-9]{5,11}$/, //登录密码验证正则
            reg3 = /^[0-9]{4}$/, //取款密码验证正则
            reg4 = /0?(13|14|15|17|18)[0-9]{9}/, //电话号码验证正则
            reBool = true;
        $.each(inputArr, function(i, item) {
            var ivalue = $(item).val(),
                itype = $(item).attr('itype'),
                istr = $(item).attr('istr'),
                tips = $(item).parents('.input_div').siblings('.total_tip').find('.tip_text');
            if (!ivalue) {
                tips.text(istr + '必须填写').removeClass('smile').addClass('cry').show();
                $(item).addClass('deposit-m');
                reBool = false;
                return;
            }
            if (itype == 'new-pass-cipher') {
                var state = $('.pagenavi a.active').attr('state');
                if (state == '1') {
                    itype = 'password-cipher';
                } else if (state == '0') {
                    itype = 'withpass-cipher';
                }
            }
            if (itype == 'account') {
                if (!reg1.test(ivalue)) {
                    tips.text(istr + '由6~10个字母和数字组合的字符，以字母开头').removeClass('smile').addClass('cry').show();
                    $(item).addClass('deposit-m');
                    reBool = false;
                    return;
                }
                if (o.type == 'r') {
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
            if (itype.indexOf('telephone') > -1) {
                if (!reg4.test(ivalue)) {
                    tips.text(istr + '格式有误').removeClass('smile').addClass('cry').show();
                    $(item).addClass('deposit-m');
                    reBool = false;
                    return;
                }
            }
            if (itype.indexOf('cipher') > -1) {
                var before = $(item).parents('.input_box').prev('.input_box').find('input').val();
                if (ivalue == before) {
                    tips.text('原密码与新密码不能相同').removeClass('smile').addClass('cry').show();
                    $(item).addClass('deposit-m');
                    reBool = false;
                    return;
                }
            }
            if (itype.indexOf('sure-pass') > -1) {
                var before = $(item).parents('.input_box').prev('.input_box').find('input').val();
                if (ivalue != before) {
                    tips.text('两次密码输入不一致，请确认').removeClass('smile').addClass('cry').show();
                    $(item).addClass('deposit-m');
                    reBool = false;
                    return;
                }
            }
            if (itype == 'paw') {
                var payAmount = $('#payAmount').val(),
                    cointype = $('#coinType');
                if (payAmount < jsons.coinLimit[cointype.attr('vals')]) {
                    tips.text(cointype.val().split('（')[0] + ' 单笔充值最低币额为' + jsons.coinLimit[cointype.attr('vals')] + '个').removeClass('smile').addClass('cry').show();
                    $(item).addClass('deposit-m');
                    reBool = false;
                    return;
                }
                if (!(public.regs1).test(payAmount)) {
                    tips.text('请输入正确的' + istr).removeClass('smile').addClass('cry').show();
                    $(item).addClass('deposit-m');
                    reBool = false;
                    return;
                }
            }
            if (itype == 'wap') {
                var countDian = $('#countDian').val();
                if (countDian < 100) {
                    tips.text('单笔提现最低金额 100 点').removeClass('smile').addClass('cry').show();
                    $(item).addClass('deposit-m');
                    reBool = false;
                    return;
                }
                if (!(public.regs2).test(countDian)) {
                    tips.text('请输入正确的' + istr).removeClass('smile').addClass('cry').show();
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

    payInput: function(types) {
        $('.input_box input[need]').bind('input propertychange', function() {
            if ($(this)[0].id == 'payAmount' || $(this)[0].id == 'countDian') {
                public.aboutChange(types);
            }
            public.checkinput({
                domlist: $(this)
            });
        }).blur(function() {
            public.checkinput({
                domlist: $(this)
            });
        }).focus(function() {
            $(this).removeClass('deposit-m');
        });
    },

    aboutChange: function(t) {
        var current = parseFloat((t == '0') ? $('#payAmount').val() : $('#countDian').val()),
            rate = parseFloat($('#coinRate').attr('vals')),
            reg = t == '0' ? public.regs1 : public.regs2;
        if (!(reg.test(current)) || isNaN(rate)) {
            if (t == '0') {
                $('#payCount').val('');
                $('#payDiscount').val('');
                $('#toAccount').val('');
            } else {
                $('#biQuota').val('');
            }
        } else {
            if (t == '0') {
                var coin_dian = parseFloat((rate * current).toFixed(2)),
                    pay_discount = parseFloat((coin_dian * 0.01).toFixed(2));
                $('#payCount').val(coin_dian.toFixed(2) + ' 点');
                $('#payDiscount').val(pay_discount.toFixed(2) + ' 点');
                $('#toAccount').val((coin_dian + pay_discount + (public.firstpay ? 188.00 : 0)).toFixed(2) + ' 点');
            } else {
                var dian_coin = parseFloat((current / rate).toFixed(3));
                $('#biQuota').val(dian_coin.toFixed(3) + ' 个' + $('#coinType').val());
            }

        }
    },

    getManyType: function(obj, callback) {
        var o = $.extend({}, {
            type: 'coin',
            text: '选择币种',
            length: 4
        }, obj);
        var ulheight = 170,
            layerheight = 200;
        switch (o.length) {
            case 1:
                ulheight = 45;
                layerheight = 75;
                break;
            case 2:
                ulheight = 90;
                layerheight = 120;
                break;
            case 3:
                ulheight = 130;
                layerheight = 160;
                break;
            default:
                ulheight = 170;
                layerheight = 200;
        };
        var lcontent = '<ul style="height: ' + ulheight + 'px;">';
        $.each(jsons[o.type], function(i, item) {
            if (o.type = 'coin') {
                lcontent += '<li val="' + item.value + '">' + item.name + '（' + item.value + '）</li>';
            } else {
                lcontent += '<li val="' + item.value + '">' + item.name + '</li>';
            }
        });
        lcontent += '</ul>';
        var content = '<div class="Clear bottom_choose"><label class="cl">取消</label><label>' + o.text + '</label><label class="sure normal">完成</label></div><div class="li_list">' + lcontent + '</div>';
        var index = layer.open({
            type: 1,
            content: content,
            shadeClose: false,
            anim: 'up',
            /*170-4-200 130-3-160  90-2-120 45-1-75*/
            style: 'position:fixed; bottom:0; left:0; width: 100%; height: ' + layerheight + 'px; padding:5px 0; border:none;',
            success: function(e) {
                console.log();
                $('body').css({
                    'position': 'fixed',
                    'top': '-' + $(document).scrollTop() + 'px'
                });
                $('.li_list li').on('click', function() {
                    $(this).addClass('chosed').siblings('li').removeClass('chosed');
                });
                $('.bottom_choose .cl').on('click', function() {
                    $('body').css({
                        'position': 'relative',
                        'top': '0px'
                    });
                    layer.close(index);
                });
                $('.bottom_choose .sure').on('click', function() {
                    if ($('.li_list li.chosed').length == 0) {
                        layer.open({
                            content: '您还未选择',
                            skin: 'msg',
                            time: 2
                        });
                    } else {
                        var data = {
                            text: $('.li_list li.chosed').text(),
                            value: $('.li_list li.chosed').attr('val')
                        };
                        if (typeof(callback) == "function") {
                            callback(data);
                            $('.bottom_choose .cl').click();
                        } else {
                            layer.open({
                                content: '回调函数有误',
                                skin: 'msg',
                                time: 2
                            });
                        }
                    }
                });
            }
        });
    },
    currency: function(type, first) {
        var ciontype = $('#coinType').attr('vals'),
            coinname = $('#coinType').val().split('（')[0];
        if (type == '0') {
            $('#payQuota').val('');
            $('#payCount').val('');
            $('#payDiscount').val('');
            $('#toAccount').val('');
            $('#payQuota').val('最小充值限额为 ' + jsons.coinLimit[ciontype] + ' 个');
        } else {
            $('#biQuota').val('');
        }
        $('#coinRate').val('').attr('vals', '');
        if (ciontype == 'AppleC') {
            $('#coinRate').val('1 ' + ciontype + ' = 1 点').attr('vals', '1');
            return;
        }
        this.ajax.post('/member/money-exchange', {
            record: type,
            currency: ciontype.toLowerCase()
        }, function(data) {
            if (data.code == '2018') {
                var real = data.result.exchange.replace(' CNY', '').replace(/,/g, '');
                $('#coinRate').val('1 ' + ciontype + ' = ' + (real || '') + ' 点').attr('vals', (real || ''));
                if (type == '0' && data.result.total == '0') {
                    public.firstpay = true;
                    $('#payDiscount').parents('.input_box').before('<div class="input_box">' +
                        '<i class="iconfont icon-discount i_text begin_icon primary">首冲特惠</i>' +
                        '<input type="text" id="firstPay" class="plr" readonly disabled placeholder="首冲送188点" value="188 点" /></div>');
                }
                if (!first) {
                    public.aboutChange(type);
                    public.checkinput({
                        domlist: (type == '0') ? $('#payAmount') : $('#countDian')
                    });
                }
            } else {
                layer.open({
                    content: '汇率异常，请刷新后再试',
                    skin: 'msg',
                    time: 2
                });
            }
        });
    },

    filtrate: function(option, callback) {
        var opts = $.extend({}, option);
        // var contain = $('.filtrate .fi_container')[opts.type];
        // if (contain) {
        //     $(contain).addClass('activate').siblings('.fi_container').removeClass('activate');
        // }
        $('.timeSection').mobiscroll().range({
            theme: 'mobiscroll',
            lang: 'zh',
            display: 'bottom',
            mode: 'rangeBasic',
            // dateFormat: 'yy-mm-dd',
            controls: ['calendar', 'time'],
            defaultValue: [new Date(), new Date()],
            max: new Date()
        });
        $(".filtrate_btn").click(function() {
            $(".filtrate").animate({
                right: '0'
            }, 150);
            setTimeout(function() {
                $('.filtrate_modal').show();
            }, 150);
            $('.filtrate_modal').on('click', function() {
                $(".filtrate").animate({
                    right: '-101%'
                }, 150);
                $('.filtrate_modal').hide();
            });
        });

        $('.i_inputs i.icon-cha').on('click', function() {
            $(this).siblings('input').val('');
        });
        $('.filtrate .options label').on('click', function() {
            if (!$(this).hasClass('active')) {
                $(this).addClass('active').siblings('label').removeClass('active');
            } else {
                $(this).removeClass('active')
            }
        });
        $('.bottom_btn_reset_submit .btn_reset').on('click', function() {
            var some_container = $('.filtrate .fi_container.activate');
            some_container.find('input').val('');
            some_container.find('.options label').removeClass('active');
            var data = {
                // fitype: opts.type,
                rs: 'reset'
            };
            setTimeout(function() {
                $('.filtrate_modal').click();
            }, 50);
            if (typeof(callback) == "function") {
                return callback(data);
            } else {
                console.log("the method is no a function!");
            }
        });
        $('.bottom_btn_reset_submit .btn_submit').on('click', function() {
            var data = {
                // fitype: opts.type,
                rs: 'submit'
            };
            $.each($('.filtrate .fi_container.activate *[want]'), function(i, item) {
                var _this = $(item);
                if (_this.hasClass('ipts')) {
                    data[_this.attr('want')] = _this.val();
                } else if (_this.hasClass('options')) {
                    data[_this.attr('want')] = _this.find('label.active').attr('value') || "";
                } else {
                    data[_this.attr('want')] = _this.text();
                }
            });
            setTimeout(function() {
                $('.filtrate_modal').click();
            }, 50);
            if (typeof(callback) == "function") {
                return callback(data);
            } else {
                console.log("the method is no a function!");
            }
        });
    }
};

$(function() {
    public.init();
});
