var ServerUrl = 'http://www.xrp-candy.com/springBoot'; //192.168.31.254  43.20  172.20.10.2:8080 http://www.xrp-candy.com/springBoot
var HttpService = function() {
    this.MAX_VALUE = 100000;
    var TYPE = {
        "post": "POST",
        "get": "GET"
    };

    var _ajax = function(type, url, data, succ, failed) {
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
                index = layer.load(0, {
                    shade: 0.1
                });
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
                        layer.msg('登录超时，请重新登陆', {
                            time: 2000,
                            icon: 2
                        });

                    } else if (data.code == '1121') {
                        layer.msg('您的账号已在其他地方登陆，请重新登录', {
                            time: 2000,
                            icon: 2
                        });
                    } else if (data.code == '1122') {
                        layer.msg('同网络下只能有一个账户活跃，您已被迫下线', {
                            time: 2000,
                            icon: 2
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
                    console.log("the method is no a function!");
                }
            },
            complete: function() {
                layer.close(index);
            }
        });
    };
    this.get = function(url, data, succ, failed) {
        return _ajax(TYPE.get, url, data, succ, failed);
    }
    this.post = function(url, data, succ, failed) {
        return _ajax(TYPE.post, url, data, succ, failed);
    }
}

var common = {
    ajax: new HttpService(),
    //表格设置
    tablePageSet: function() {
        return {
            pageNo: 1,
            pageSize: 10
        };
    },
    getParamFromUrl: function(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        ////console.log(window.location.search.substr(1));
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

    comesoon: function() {
        layer.msg('<span style="color: #F39B12;font-size: 18px;font-weight: bold;">攻城狮正在抓紧时间建设中...</span>', {
            icon: 7,
            time: 2000
        });
    },
    openlayer: function(types, flags) {
        var log = {
                tt: '<div class="lorcss">&nbsp;&nbsp;&nbsp;登&nbsp;录</div>',
                ar: ['350px', '320px'],
                co: '<div class="mbody">' +
                    '<div class="fgroup">' +
                    '<div class="input-group">' +
                    '<input id="loginname" type="text" placeholder="请输入账号" maxlength="10" itype="l_account" iStr="账号"/>' +
                    '<span class="icon-user"></span>' +
                    '</div>' +
                    '<label class="loginName-error tip vh"></label>' +
                    '</div>' +
                    '<div class="fgroup"><div class="input-group"><input id="loginpass" type="password" placeholder="请输入密码" maxlength="12" iType="l_password" iStr="登录密码"/><span class="icon-pass"></span></div><label class="loginPass-error tip vh"></label></div>' +
                    '<div class="fgroup"><div class="input-group"><input type="button" class="btn-submit" value="登录" onclick="common.login(\'' + flags + '\');"/></div></div>' +
                    '<div class="last-group f12"><a class="btn-forget-password fl hide">忘记密码？</a><span class="fr">没有账号？<a class="btn-sign-up" onclick="layer.closeAll();common.openlayer(\'R\');">在此注册</a></span></div></div>'
            },
            reg = {
                tt: '<div class="lorcss">&nbsp;&nbsp;&nbsp;注&nbsp;册</div>',
                ar: ['480px', '590px'],
                co: '<div class="mbody">' +
                    '<div class="fgroup">' +
                    '<div class="input-group">' +
                    '<input id="regname" type="text" placeholder="账号，6~10个字母和数字组合的字符，以字母开头" maxlength="10" iType="r_account" iStr="账号"/>' +
                    '<span class="icon-user"></span>' +
                    '</div>' +
                    '<label class="regName-error tip vh"></label>' +
                    '</div>' +
                    '<div class="fgroup"><div class="input-group"><input id="regpass" type="password" placeholder="密码，6~12个字母和数字组合的字符，区分大小写" maxlength="12" iType="r_password" iStr="密码"/><span class="icon-pass"></span></div><label class="regPass-error tip vh"></label></div>' +
                    '<div class="fgroup"><div class="input-group"><input id="verpass" type="password" placeholder="请确认密码" maxlength="12" iType="r_spassword" iStr="密码确认"/><span class="icon-pass"></span></div><label class="verPassVer-error tip vh"></label></div>' +
                    '<div class="fgroup"><div class="input-group"><input id="truename" type="text" placeholder="真实姓名，需与银行帐户名称相同，否则不能出款" iType="r_tname" iStr="真实姓名"/><span class="icon-tname"></span></div><label class="trueName-error tip vh"></label></div>' +
                    '<div class="fgroup"><div class="input-group"><input id="withpass" type="text" placeholder=" 4 位数字取款密码，提款认证必须，请务必记住" maxlength="4"  iType="r_wpassword" iStr="取款密码"/><span class="icon-wpass"></span></div><label class="withPass-error tip vh"></label></div>' +
                    '<div class="fgroup" style="margin-bottom: 15px;"><div class="input-group"><input id="yaoqingma" type="text" placeholder="代理邀请码，非必填" maxlength="10"  iType="r_yaoqingma" iStr="邀请码"/><span class="icon-yaoqingma"></span></div></div>' +
                    '<div class="fgroup f12" style="margin: 5px 0;color: gray;"><input type="checkbox" checked disabled style="vertical-align:middle;">&nbsp;我已届满合法博彩年龄﹐且同意各项开户条约。</div>' +
                    '<div class="fgroup"><div class="input-group"><input type="button" class="btn-submit" value="注册" onclick="common.register();"/></div></div>' +
                    '<div class="last-group f12"><a class="btn-forget-password fl hide">忘记密码？</a><span class="fr">已有账号？<a class="btn-sign-up" onclick="layer.closeAll();common.openlayer(\'L\');">在此登录</a></span></div></div>'
            }
        layer.open({
            type: 1,
            title: (types == 'L' ? log.tt : reg.tt),
            closeBtn: 1,
            move: false,
            area: (types == 'L' ? log.ar : reg.ar),
            content: (types == 'L' ? log.co : reg.co),
            success: function(layero, index) {
                $('#regpass').val('');$('#withpass').val('');
                var inputDeal = $('.input-group>input[type="text"],.input-group>input[type="password"]');
                // inputDeal.focus(function() {
                // });
                inputDeal.blur(function() {
                    common.checkinput([$(this)]);
                });
            }
        });
    },
    login: function(flags) {
        var logName = $('#loginname').val(),
            logPass = $('#loginpass').val();
        var thisArr = [$('#loginname'), $('#loginpass')];
        if (common.checkinput(thisArr)) {
            var reqData = {
                "name": logName,
                "password": logPass
            };
            this.ajax.post('/member/login-member', reqData, function(data) {
                if (data.code == '2018') {
                    sessionStorage.setItem('userinfo', JSON.stringify(data.result));
                    sessionStorage.setItem('toid', data.result.token);
                    layer.msg('登录成功', {
                        time: 2000,
                        icon: 1
                    });
                    setTimeout(function() {
                        layer.closeAll();
                        if (flags == 'bet') {
                            window.location.href = 'http://www.xrp-candy.com/dice/home.html';
                        } else {
                            window.location.reload();
                        }

                    }, 2000);
                } else {
                    layer.msg('用户名或密码错误', {
                        time: 2000,
                        icon: 2
                    });
                }
            }, function(e) {
                layer.msg('登录失败', {
                    time: 2000,
                    icon: 2
                });
                console.log(e);
            });
        };
    },

    register: function() {
        var cip = '';
        $.ajax({
            url: 'http://freegeoip.net/json/',
            async: false,
            success: function(data) {
                cip = data.ip;
            },
            type: 'GET',
            dataType: 'JSON'
        });
        var regname = $('#regname').val(),
            regpass = $('#regpass').val(),
            truename = $('#truename').val(),
            withpass = $('#withpass').val(),
            yaoqingma = $('#yaoqingma').val(),
            thisArr = [$('#regname'), $('#regpass'), $('#verpass'), $('#truename'), $('#withpass')];
        if (common.checkinput(thisArr)) {
            var reqData = {
                name: regname,
                password: regpass,
                real_name: truename,
                bank_password: withpass,
                address: cip
            };
            if (yaoqingma) {
                reqData.invitation_code = yaoqingma;
            }
            this.ajax.post('/member/add-member', reqData, function(data) {
                if (data.code == '2018') {
                    sessionStorage.setItem('userinfo', JSON.stringify(data.result));
                    sessionStorage.setItem('toid', data.result.token);
                    layer.msg('注册成功', {
                        time: 2000,
                        icon: 1
                    });
                    setTimeout(function() {
                        layer.closeAll();
                        window.location.reload();
                    }, 2000);
                } else {
                    layer.msg('注册失败', {
                        time: 2000,
                        icon: 2
                    });
                }
            }, function(e) {
                layer.msg('网络错误', {
                    time: 2000,
                    icon: 2
                });
                console.log(e);
            });

        };
    },

    checkinput: function(ithisArr) {
        var regOne = /^[a-zA-Z][a-zA-Z0-9]{5,9}$/,
            regTwo = /^[a-zA-Z][a-zA-Z0-9]{5,11}$/,
            regThree = /^[0-9]{4}$/,
            reBool = true;
        $.each(ithisArr, function(i, item) {
            var iValue = item.val(),
                iType = item.attr('iType'),
                iStr = item.attr('iStr'),
                tips = item.parent().siblings('label.tip');
            if (!iValue) {
                item.parent().siblings('label.tip').text(iStr + '必须填写').removeClass('vh');
                reBool = false;
                return;
            }
            if (iType == 'r_account') {
                if (!regOne.test(iValue)) {
                    tips.text('账号由6~10个字母和数字组合的字符，以字母开头').removeClass('vh');
                    // tips.text('填写的' + iStr + '不符合规则，请确认').removeClass('vh');
                    reBool = false;
                    return;
                } else {
                    common.ajax.get('/member/verify-name', {
                        name: iValue
                    }, function(data) {
                        if (data.code == '2018') {

                        } else {
                            tips.text('用户名已存在，请重新填写').removeClass('vh');
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
            if (iType == 'r_password') {
                if (!regTwo.test(iValue)) {
                    tips.text('密码由6~12个字母和数字组合的字符，以字母开头').removeClass('vh');
                    reBool = false;
                    return;
                }
            }
            if (iType == 'r_spassword') {
                var regpass = $('#regpass').val();
                if (regpass != iValue) {
                    tips.text('两次密码输入不一致，请确认').removeClass('vh');
                    reBool = false;
                    return;
                }
            }
            if (iType == 'r_wpassword') {
                if (!regThree.test(iValue)) {
                    tips.text('取款密码由4位纯数字组成').removeClass('vh');
                    reBool = false;
                    return;
                }
            }
            tips.text('').addClass('vh');
        });
        return reBool;
    },

    //初始化
    initPage: function() {
        //页面共同代码
        if ($('#thisisheader').length == 0) {
            var html = '';
            var html = '<button type="button" onclick="window.location.href=\'onlinedeposit.html\'">线上存款</button><button type="button" onclick="window.location.href=\'onlinedraw.html\'">线上取款</button>' + ($('#thisishome').length == 0 ? '<button type="button" onclick="window.location.href=\'home.html\'"><i class="iconfont icon-shouye"></i>&nbsp;首页</button>' : '') + '<i class="iconfont icon-yonghu" onclick="common.mine();"></i>';
            if (sessionStorage.getItem('toid')) {
                html = '<button type="button" onclick="window.location.href=\'onlinedeposit.html\'">线上存款</button><button type="button" onclick="window.location.href=\'onlinedraw.html\'">线上取款</button>' + ($('#thisishome').length == 0 ? '<button type="button" onclick="window.location.href=\'home.html\'"><i class="iconfont icon-shouye"></i>&nbsp;首页</button>' : '') + '<i class="iconfont icon-yonghu" onclick="common.mine();"></i>';
            } else {
                html = '<button type="button" onclick="common.openlayer(\'L\');">登录</button><button type="button" onclick="common.openlayer(\'R\');">注册</button>' + ($('#thisishome').length == 0 ? '<button type="button" onclick="window.location.href=\'home.html\'"><i class="iconfont icon-shouye"></i>&nbsp;首页</button>' : '');
            }
            $('body').prepend('<div id="thisisheader" class="header"><img src="../../img/homelogo.png" style="vertical-align: middle;" width="90" height="80"><div class="slogan"><span class="wel">永利高</span></div><i class="iconfont icon-icon_xinyongguiji"></i><div class="credit"><span>老品牌 值得信赖</span><div>精彩赛事，尽在永利高</div></div><div class="baseinfo fr">' + html + '</div></div>');
        }

        if ($('#thisisfooter').length == 0) {
            $('body').append('<div id="thisisfooter" class="footer"><div class="fl"><img src="../../img/2018sjb.png"></div><div class="fl footinfo"><div class="article-menu"><a href="footerinfo.html?type=aboutus">关于我們</a>｜ <a href="javascript:void(0)" onclick="$(\'.nb-icon-inner-wrap\').click();">联系我们</a>｜ <a href="javascript:void(0);" onclick="common.inviteCode();">合作代理</a>｜ <a href="footerinfo.html?type=diploma">博彩牌照</a>｜ <a href="footerinfo.html?type=desposit">存款帮助</a>｜ <a href="footerinfo.html?type=teller">取款帮助</a><!-- ｜ <a href="javascript:void(0)">常见问题</a> --></div><div class="fo-text">永利高所提供的产品和服务，是由菲律宾政府卡格扬河经济特区 First Cagayan leisure and Resort Corporation.<br> 授权和监管 我们将不余遗力的为您提供优质的服务和可靠的资金保障。</div><div class="footer-Infomation"><div class="copyright fl">Copyright © 永利高 Reserved&nbsp; <span class="footer-info"><!-- 邮箱：980127777@qq.com&nbsp;&nbsp;QQ：980127777 --></span></div></div></div><div class="Clear"></div></div>');
        }

        setInterval(function() {
            $(".time > span").text(new Date().format("yyyy年MM月dd日 hh:mm:ss"));
        }, 1000);
        $.each(config.navtype, function(i, item) {
            $('#navType').append('<li><a href="javascript:void(0);" class="' + (item.name == "今日赛事" ? "active" : "") + '" tabType="' + item.tabtype + '" countType="' + item.countType + '">' + item.name + '</a></li>');
        });

        var coinhtml = '';
        $.each(config.coin, function(i, item) {
            coinhtml += '<option value="' + item.value + '">' + item.name + '（' + item.value + '）</option>';
        });
        $('.all-coin').append(coinhtml);

        // 回到顶部
        $('body').prepend('<div id="return-to-top"><img src="../../img/float-top-white.png"/></div>');
        $(window).scroll(function() {
            if ($(window).scrollTop() >= 100) {
                $("#return-to-top").fadeIn(400); /* 当滑动到不小于 100px 时，回到顶部图标显示 */
            } else {
                $("#return-to-top").fadeOut(400); /* 当滑动到小于(页面被卷去的高度) 100px 时，回到顶部图标隐藏 */
            }
        });
        $("#return-to-top").click(function() {
            $("html,body").animate({
                scrollTop: 0
            }, 500);
        });

        // 在线咨询
        $('body').append('<script>var _hmt = _hmt || [];(function() {var hm = document.createElement("script");hm.src = "https://hm.baidu.com/hm.js?bdcca757f17f3439b840ebb0a44084a2";var s = document.getElementsByTagName("script")[0];s.parentNode.insertBefore(hm, s);})();</script>');

        if ($('.lotteryBetting').length > 0) {
            $('.lotteryBetting').click(function() {
                var userinfo = sessionStorage.getItem("userinfo");
                if (userinfo && JSON.parse(userinfo).token) {
                    // window.open("http://www.xrp-candy.com/dice/home.html");
                    window.location.href = 'http://www.xrp-candy.com/dice/home.html';
                } else {
                    layer.msg("请先登录", {
                        icon: 0,
                        time: 2000
                    });
                    setTimeout(function() {
                        common.openlayer('L', 'bet');
                    }, 2000);
                }
            });
        }

        if($('#thisishome').length == 0 && $('#thisisft').length == 0 && $('#thisisbk').length == 0) {
            $('body').append('<div class="hyperchannel"><li><a href="football.html"><i class="iconfont icon-xiaoyuan-"></i>&nbsp;足球</a></li><li><a href="basketball.html"><i class="iconfont icon-lanqiu"></i>&nbsp;篮球</a></li><li><a href="javascript:void(0);" class="lotteryBetting"><i class="iconfont icon-taiqiu"></i></i>&nbsp;彩票</a></li></div>');
        }
    },

    inviteCode: function() {
        $('.mine').remove();
        layer.closeAll();
        if (sessionStorage.getItem('toid') && sessionStorage.getItem('userinfo')) {
            var h = '<div style="letter-spacing: 5px;font-size: 20px;text-align: center;color: #071D32;">' + JSON.parse(sessionStorage.getItem('userinfo')).name + '</div><div style="margin-top: 10px;font-size: 12px; color: #FC6747;text-align: center;">Tip：受邀会员每月下注流水总额的1%作为推广返利</div>';
            layer.alert(h, {
                skin: 'layui-layer-molv',
                title: '您的代理邀请码',
                move: false,
                closeBtn: 0,
            });
        } else {
            sessionStorage.setItem('userinfo', '');
            sessionStorage.setItem('toid', '');
            common.openlayer('L');
        }

    },

    //取消订单
    cancelDeal: function() {
        $('.dealingTicket').html('<div class="dtitle">交易单</div><div class="noDeal"><div>点击赔率便可将<br>选项加到交易单里</div><!-- <div>赛事已经关闭</div><button>确定</button> --></div>');
    },

    loginout: function() {
        this.ajax.post('/member/exit-member', {}, function(data) {
            if (data.code == '2018') {
                sessionStorage.setItem('userinfo', '');
                sessionStorage.setItem('toid', '');
                // layer.msg('安全退出', {
                //     time: 2000,
                //     icon: 1
                // });
                setTimeout(function() {
                    layer.closeAll();
                    window.location.href = 'home.html';
                }, 500);
            }
        }, function(e) {
            layer.msg('网络错误', {
                time: 2000,
                icon: 2
            });
        });

    },

    mine: function() {
        $('body').prepend('<div class="mine"><div class="myinfos"><img src="../../img/icon_user_o_lg.png"><p>账户：<label id="myAccount" class="fwb">-</label></p><p>本地余额：<label id="remainSum" class="fwb">0.00</label> 点</p><p>返利余额：<label id="returnSum" class="fwb">0.00</label> 点</p><p><button class="refresh-credit" onclick="common.getANS();"><i class="iconfont icon-msnui-refresh-circle alignmid"></i> 刷新额度</button></p><div class="milist"><div onclick="window.location.href=\'tradingrecord.html\'">资金交易记录<i class="iconfont icon-more fr f20"></i></div><div onclick="window.location.href=\'managePass.html\'">密码管理<i class="iconfont icon-more fr f20"></i></div><div onclick="common.inviteCode();">获取代理邀请码<i class="iconfont icon-more fr f20"></i></div></div><a href="javascript:void(0);" onclick="common.loginout();"><i class="iconfont icon-tuichu alignmid"></i> 安全退出</a></div></div>');
        $('.mine').click(function(e) {
            var o = e.target;
            if ($(o).closest('.myinfos').length == 0) //不是特定区域
                $('.mine').remove();
        });
        common.getANS();
    },

    getANS: function() {
        this.ajax.get('/member/member-money', {}, function(data) {
            if (data.code == '2018') {
                $('#myAccount').text(data.result.name);
                $('#remainSum').text(data.result.sum);
                $('#returnSum').text(data.result.sum);
            }
            // else {
            //     layer.msg('数据获取失败', {
            //         time: 2000,
            //         icon: 2
            //     });
            // }
        }, function(e) {
            // layer.msg('网络错误', {
            //     time: 2000,
            //     icon: 2
            // });
            console.log(e);
        });
    },

    getPourList: function(obj) {
        this.ajax.post('/member/single-note', obj, function(data) {
            if (data.code == '2018') {
                if (data.result.length == 0) {
                    $('.bottomPourList').html('<div class="noDeal fwb">暂时还没有下注单</div>');
                } else {
                    var html = '<div class="alllist">';
                    var strArr = ['大', '小', '单大', '单小', '单', '双'];
                    $.each(data.result, function(i, item) {
                        var btype = '-';
                        switch (item.betType) {
                            case 'RFT':
                                btype = '足球';
                                break;
                            case 'REFT':
                                btype = '滚球-足球';
                                break;
                            case 'RBK':
                                btype = '篮球';
                                break;
                            case 'REBK':
                                btype = '滚球-篮球';
                                break;
                            default:
                                btype = '-';
                        };
                        var st = '';
                        if (item.iorType == '大' || item.iorType == '小') {
                            st = '大小'
                        } else if (item.iorType == '单大' || item.iorType == '单小') {
                            st = '积分大小'
                        } else if (item.iorType == '单' || item.iorType == '双') {
                            st = '单双'
                        } else {
                            st = item.iorType;
                        }
                        var alltype = item.iorType;
                        if (strArr.indexOf(alltype) > -1) {
                            if (alltype == '单大') {
                                alltype = '大';
                            }
                            if (alltype == '单小') {
                                alltype = '小';
                            }
                        } else {
                            alltype = '';
                        }
                        var th = '',
                            tc = '',
                            thisfront = '[ <span class="fwb">' + alltype + (item.iorRatio ? item.iorRatio : "") + '</span> ]',
                            chosedName = '';
                        switch (item.bet) {
                            case 'H':
                                th = thisfront;
                                chosedName = item.teamh;
                                break;
                            case 'C':
                                tc = thisfront;
                                chosedName = item.teamc;
                                break;
                            case 'N':
                                chosedName = '和局';
                                // thisfront = '';
                                break;
                        };
                        if(!alltype && !item.iorRatio) {
                            thisfront = '';
                        }
                        var bs = '';
                        if (item.iorType == '让球' || item.iorType == '让分') {
                            if (item.bet == item.strong) {
                                bs = '(让方) ';
                            } else {
                                bs = '(受让方) ';
                            }
                            if (item.bet == 'N') {
                                bs = '';
                            }
                        }

                        html += '<div class="tinfo commons"><div class="dleague"><span>' + btype + '</span></div><p>' + item.league + '</p><p>下注时间：<span>' + item.betTime + '</span></p><div class="chsteam commons c_red fwb">' + st + '</div><div class="chsteam commons"><span class="tName">' + item.teamh + ' <font class="radio">vs</font> ' + item.teamc + '</span></div><div class="chsteam commons"><label class="c_red">' + chosedName +  ' ' + bs + thisfront + '</label> @ <strong class="light" id="ioradio_id">' + (item.ratio || '-') + '</strong></div><p>下注金额：<span class="pourMoney">' + (item.money || '0') + '</span></p></div>';
                    });
                    html += '</div>';
                    $('.bottomPourList').html(html);
                }
            } else {
                $('.bottomPourList').html('<div class="noDeal fwb">暂时还没有下注单</div>');
            }
        }, function(e) {
            $('.bottomPourList').html('<div class="noDeal fwb">暂时还没有下注单</div>');
        });
    }
};

common.initPage();

/**
 * 时间对象的格式化;
 */
Date.prototype.format = function(format) {
    /*
     * eg:format="yyyy-MM-dd hh:mm:ss";
     */
    var o = {
        "M+": this.getMonth() + 1, // month
        "d+": this.getDate(), // day
        "h+": this.getHours(), // hour
        "m+": this.getMinutes(), // minute
        "s+": this.getSeconds(), // second
        "q+": Math.floor((this.getMonth() + 3) / 3), // quarter
        "S": this.getMilliseconds()
            // millisecond
    }

    if (/(y+)/.test(format)) {
        format = format.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }

    for (var k in o) {
        if (new RegExp("(" + k + ")").test(format)) {
            format = format.replace(RegExp.$1, RegExp.$1.length == 1 ? o[k] : ("00" + o[k]).substr(("" + o[k]).length));
        }
    }
    return format;
};
