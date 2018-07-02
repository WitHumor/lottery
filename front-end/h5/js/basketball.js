$(function() {
    BK.initPage();
});

var BK = {
    ajax: new HttpService(),
    currentPage: 0,
    positions: {},
    fields: {},
    gCountObj: {},
    countdown: 3,
    currentUrl: '',
    initPage: function() {
        this.initList();
        this.countDown();
        $('.run_today a').on('click', function() {
            $(this).addClass('active').siblings('a').removeClass('active');
            BK.currentPage = 0;
            $('.list_box').html('');
            BK.initAll();
        });
        $('.list_container').scroll(function() {
            if ($(this).scrollTop() > 100) {
                $('.back_to_top').fadeIn();
            } else {
                $('.back_to_top').fadeOut();
            }
        });
        var backButton = $('.back_to_top');
        $('.back_to_top').on('click', function() {
            $('.list_container').animate({
                scrollTop: 0
            }, 800);
        });
        $('.list_box').on('click', '.every_choice > p', function() {
            if (!sessionStorage.getItem('toid')) {
                layer.open({
                    content: '请登录再下注',
                    skin: 'msg',
                    time: 2
                });
                $('.every_choice > p').off('click');
                setTimeout(function() {
                    window.location.href = 'login.html?where=basketball';
                }, 1500);
                return;
            }
            var rate_label = $(this).children('label')[1];
            if ($(rate_label).text()) {
                var tType = $(this).index() == 0 ? 'H' : 'C',
                    fonts = $(this).attr('fonts'),
                    league = $(this).parents('.leng_child').siblings('p.league').children('label').text(), //联赛名称
                    teams = $(this).parents('.game_option').siblings('p.teams').children('span').text().split(' -vs- '),
                    indextext = $(this).parents('.every_choice').prev().text(), //选项名称
                    dataInfo = {
                        gid: $(this).parents('.leng_child').attr('gid'),
                        url: BK.currentUrl,
                        ratio: $(this).attr('vi'),
                        bet: tType,
                        betType: 'BK',
                        ratioData: parseFloat($(rate_label).text()).toFixed(2),
                    };
                if (fonts) {
                    dataInfo.iorRatio = fonts;
                }
                var show_team = (tType == "H" ? teams[0] : teams[1]);
                if (indextext.indexOf('大小') > -1) {
                    show_team = '';
                }
                if (indextext.indexOf('让分') > -1) {
                    if (fonts) {
                        show_team += '（让方）';
                    } else {
                        show_team += '（受让方）'
                    }
                    show_team += '<span class="danger">[&nbsp;' + $(this).parents('.every_choice').attr('vals') + '&nbsp;]</span>';
                } else {
                    if (fonts) {
                        var front_es = $(this).find('.fonts').text();
                        show_team += '&nbsp;<span class="danger">[&nbsp;' + front_es + '&nbsp;]</span>';
                    }
                }
                if (indextext.indexOf('球队积分') > -1) {
                    indextext = '球队积分 - 大小';
                }
                var styles = 'position:fixed; bottom:0; left:0; width: 100%; height: 100%; border:none;',
                    content = '<div class="layertitle"><i class="iconfont icon-close iconclose"></i><i class="iconfont icon-lanqiu_basketball4 f30 vertical-middle mR5"></i><span class="vertical-middle">下注单</span></div>' +
                    '<div class="thiscontent">' +
                    '<div class="order_s_cont">' +
                    '<label class="font-bold">联赛名称：</label>' +
                    '<label>' + league + '</label>' +
                    '</div>' +
                    '<div class="order_s_cont">' +
                    '<label class="font-bold">下注类型：</label>' +
                    '<label class="warning font-bold">' + indextext + '</label>' +
                    '</div>' +
                    '<div class="order_s_cont">' +
                    '<label class="font-bold">比赛队伍：</label>' +
                    '<label>' + teams[0] + ' <ab class="warning">-vs-</ab> ' + teams[1] + '</label>' +
                    '</div>' +
                    '<div class="order_s_cont">' +
                    '<label class="font-bold">下注选项：</label>' +
                    '<label>' + show_team + '</label>' +
                    '</div>' +
                    '<div class="order_s_cont">' +
                    '<label class="font-bold">下注赔率：</label>' +
                    '<label class="warning font-bold">@ <span id="ioradio_id">' + $(rate_label).text() + '</span></label>' +
                    '</div>' +
                    '<div class="order_s_cont">' +
                    '<input type="checkbox" checked class="vertical-middle">' +
                    '<label class="vertical-middle">自动接收较佳赔率</label>' +
                    '</div>' +
                    '<div class="order_s_cont">' +
                    '<label class="font-bold">交易金额：</label>' +
                    '<input id="moneyes" type="number" onkeyup="return BK.countWinGold()" maxlength="5">' +
                    '</div>' +
                    '<div class="order_s_cont">' +
                    '<label class="font-bold">可赢金额：</label>' +
                    '<label id="canwin">0.00</label>' +
                    '</div>' +
                    '<div class="order_s_cont">' +
                    '<label class="font-bold">单注最低：</label>' +
                    '<label>10</label>' +
                    '</div>' +
                    '<div class="order_s_cont">' +
                    '<label class="font-bold">单注最高：</label>' +
                    '<label>10000</label>' +
                    '</div>' +
                    '<div class="order_s_cont bottom_button text-right">' +
                    '<button class="order_cancel mR10">取消</button>' +
                    '<button class="order_sure">确定交易</button>' +
                    '</div></div>';
                var indexs = layer.open({
                    type: 1,
                    content: content,
                    anim: 'up',
                    style: styles,
                    success: function(e) {
                        $('.layertitle .iconclose, .order_cancel').click(function() {
                            layer.close(indexs);
                        });
                        $('.order_sure').click(function() {
                            BK.sureDeal(dataInfo, indexs);
                        });
                    },
                });
            }
        });
    },
    initAll: function(ls) {
        BK.positions = {};
        BK.fields = {};
        BK.gCountObj = {};
        BK.countdown = 3;
        BK.initList(ls);
    },
    initList: function(loadings) {
        var rtype = $('.run_today a.active').attr('rtype');
        BK.currentUrl = 'https://www.ylg56789.com/app/member/BK_browse/body_var?uid=' + jsons.together.uid + '&rtype=' + rtype + '&langx=zh-cn&mtype=3&page_no=' + BK.currentPage + '&league_id=&hot_game=';
        var indexlayer = '';
        $.ajax({
            type: 'POST',
            url: ServerUrl + '/member/load-url',
            data: {
                url: BK.currentUrl
            },
            dataType: 'text',
            beforeSend: function() {
                if (!loadings) {
                    indexlayer = layer.open({
                        type: 2,
                        content: '正在努力加载',
                        shade: 'background-color: rgba(0,0,0,.3)'
                    });
                }
            },
            success: function(data) {
                if (data != '1101') {
                    BK.analysis(data);
                    // console.log(data);
                } else {
                    if (!loadings) {
                        layer.open({
                            content: '数据加载失败',
                            skin: 'msg',
                            time: 2
                        });
                    }
                }
            },
            complete: function() {
                if (!loadings) {
                    layer.close(indexlayer);
                }
            }
        });
    },
    analysis: function(info) {
        for (v in jsons.anal) {
            BK.positions[v] = [];
            BK.searchSubStr(info, jsons.anal[v], v);
        }
        for (v in BK.positions) { //v 为属性名
            BK.fields[v] = [];
            if (BK.positions[v].length > 0) {
                $.each(BK.positions[v], function(i, item) {
                    var thisone;
                    if (v == "gcounts") {
                        thisone = info.substring(item[1], item[0] + jsons.anal[v][0].length).replace(new RegExp(/\'/g), '').split(',');
                    } else {
                        thisone = info.substring(item[1], item[0] + jsons.anal[v][0].length).replace(new RegExp(/\'|\s/g), '').split(',');
                    }
                    BK.fields[v].push(thisone);
                });
            }
        }
        if (BK.fields.gcounts[0].length > 0) {
            $.each(BK.fields.gcounts[0], function(i, item) {
                var pasArr = item.replace(new RegExp(/\s/g), '').split('|');
                BK.gCountObj[pasArr[0] + "_" + pasArr[1]] = pasArr[2];
            });
        }
        $('.run_today a[rtype="r_main"] label').text('今日赛事(' + BK.gCountObj.BK_FT + ')');
        $('.run_today a[rtype="re_main"] label').text('滚球(' + BK.gCountObj.BK_RB + ')');
        $('.pagings .firstp, .pagings .prevp, .pagings .nextp, .pagings .lastp').off();
        if (BK.fields.gpages[0][0] == '0' || BK.fields.gpages[0][0] == '1') {
            $('.pagings a').hide();
        } else {
            $('.pagings a').show();
            $('.pagings .firstp').on('click', function() {
                $('.run_today a.active').click();
            });
            if (BK.currentPage == 0) {
                $('.pagings .prevp').hide();
            } else {
                $('.pagings .prevp').show();
            }
            if ((BK.currentPage + 1) == BK.fields.gpages[0][0]) {
                $('.pagings .nextp').hide();
            } else {
                $('.pagings .nextp').show();
            }
            $('.pagings .prevp').on('click', function() {
                if (BK.currentPage == 0) return;
                else {
                    BK.currentPage--;
                    BK.initAll();
                }
            });
            $('.pagings .nextp').on('click', function() {
                if ((BK.currentPage + 1) == BK.fields.gpages[0][0]) return;
                else {
                    BK.currentPage++;
                    BK.initAll();
                }
            });
            $('.pagings .lastp').on('click', function() {
                BK.currentPage = parseInt(BK.fields.gpages[0][0]) - 1;
                console.log(BK.currentPage);
                BK.initAll();
            });
        }
        $('.pagings .curp').text(BK.currentPage + 1);
        $('.pagings .totalp').text((BK.fields.gpages[0][0] == '0' || BK.fields.gpages[0][0] == '1') ? '1' : BK.fields.gpages[0][0]);
        var html = '',
            leagues = '!PERCH';
        if (BK.fields.glists.length == 0) {
            html += '<img src="img/nodata.png" class="nodatas">';
            $('.pagings').addClass('vish');
        } else {
            $.each(BK.fields.glists, function(i, item) {
                // if (i == 0) {
                $('.pagings').removeClass('vish');
                // }
                if (item[BK.withdraws('league')] != leagues) {
                    html += '</div>';
                    leagues = item[BK.withdraws('league')];
                    html += '<div class="each_box"><p class="league"><i class="iconfont icon-liansai f30 vertical-middle"></i><label class="vertical-middle font-bold">' + leagues + '</label></p>';
                }
                var timeabout = '';
                if ($('.run_today a.active').attr('rtype') == 'r_main') {
                    timeabout = item[BK.withdraws('datetime')].replace("RunningBall", "<span class=\"danger\">滚球<span>").replace(/<br>/g, '&nbsp;');
                } else {
                    var knobble = ' - ';
                    switch (item[BK.withdraws('nowSession')]) {
                        case 'Q1':
                            knobble = "第一节";
                            break;
                        case 'Q2':
                            knobble = "第二节";
                            break;
                        case 'Q3':
                            knobble = "第三节";
                            break;
                        case 'Q4':
                            knobble = "第四节";
                            break;
                        case 'HT':
                            knobble = "半场";
                            break;
                        default:
                            knobble = " - ";
                    };
                    timeabout = '<label class="biaoqian">' + knobble + '</label><label class="warning mR10">' + (item[BK.withdraws('scoreH')] || 0) + '-' + (item[BK.withdraws('scoreC')] || 0) + '</label>';
                }
                html += '<div class="leng_child " gid="' + item[BK.withdraws('gid')] + '">' +
                    '<p class="teams">' +
                    '<i class="iconfont icon-shiliangzhinengduixiang- f16 vertical-middle"></i>' +
                    '<span class="vertical-middle">' + item[BK.withdraws('team_h')] + ' -vs- ' + item[BK.withdraws('team_c')] + '</span>' +
                    '</p>' +
                    '<div class="game_info">' + timeabout + '</div>' +
                    '<div class="game_option clear ">' +
                    '<div class="each_option">' +
                    '<p class="text-left mB5">全场独赢</p>' +
                    '<div class="every_choice">' +
                    '<p vi="ior_MH">' +
                    '<label><span class="danger">主队</span></label>' +
                    '<label class="normal font-bold">' + item[BK.withdraws('ior_MH')] + '</label>' +
                    '</p>' +
                    '<p vi="ior_MC">' +
                    '<label>客队</label>' +
                    '<label class="normal font-bold">' + item[BK.withdraws('ior_MC')] + '</label>' +
                    '</p>' +
                    '</div>' +
                    '</div>' +
                    '<div class="each_option">' +
                    '<p class="text-left mB5">全场让分</p>' +
                    '<div class="every_choice" vals="' + item[BK.withdraws('ratio')] + '">' +
                    '<p vi="ior_RH" fonts="' + (item[BK.withdraws('strong')] == 'H' ? 'ratio' : '') + '">' +
                    '<label><span class="danger">主队&nbsp;</span><span class="fonts">' + (item[BK.withdraws('strong')] == 'H' ? item[BK.withdraws('ratio')] : '') + '</span></label>' +
                    '<label class="normal font-bold">' + item[BK.withdraws('ior_RH')] + '</label>' +
                    '</p>' +
                    '<p vi="ior_RC" fonts="' + (item[BK.withdraws('strong')] == 'C' ? 'ratio' : '') + '">' +
                    '<label><span>客队&nbsp;</span><span class="fonts">' + (item[BK.withdraws('strong')] == 'C' ? item[BK.withdraws('ratio')] : '') + '</span></label>' +
                    '<label class="normal font-bold">' + item[BK.withdraws('ior_RC')] + '</label>' +
                    '</p>' +
                    '</div>' +
                    '</div>' +
                    '<div class="each_option">' +
                    '<p class="text-left mB5">全场积分大小</p>' +
                    '<div class="every_choice">' +
                    '<p vi="ior_OUC" fonts="ratio_o">' +
                    '<label class="fonts">' + (item[BK.withdraws('ratio_o')].replace("O", "大").replace("U", "小") || '大') + '</label>' +
                    '<label class="normal font-bold">' + item[BK.withdraws('ior_OUC')] + '</label>' +
                    '</p>' +
                    '<p vi="ior_OUH" fonts="ratio_u">' +
                    '<label class="fonts">' + (item[BK.withdraws('ratio_u')].replace("O", "大").replace("U", "小") || '小') + '</label>' +
                    '<label class="normal font-bold">' + item[BK.withdraws('ior_OUH')] + '</label>' +
                    '</p>' +
                    '</div>' +
                    '</div>' +
                    '<div class="each_option">' +
                    '<p class="text-left mB5">球队积分 - 大</p>' +
                    '<div class="every_choice">' +
                    '<p vi="ior_OUHO" fonts="ratio_ouho">' +
                    '<label><span class="danger">主队&nbsp;</span><span class="fonts">'+ item[BK.withdraws('ratio_ouho')].replace("O", "大").replace("U", "小") +'</span></label>' +
                    '<label class="normal font-bold">' + item[BK.withdraws('ior_OUHO')] + '</label>' +
                    '</p>' +
                    '<p vi="ior_OUCO" fonts="ratio_ouco">' +
                    '<label><span>客队&nbsp;</span><span class="fonts">'+ item[BK.withdraws('ratio_ouco')].replace("O", "大").replace("U", "小") +'</span></label>' +
                    '<label class="normal font-bold">' + item[BK.withdraws('ior_OUCO')] + '</label>' +
                    '</p>' +
                    '</div>' +
                    '</div>' +
                    '<div class="each_option">' +
                    '<p class="text-left mB5">球队积分 - 小</p>' +
                    '<div class="every_choice">' +
                    '<p vi="ior_OUHU" fonts="ratio_ouhu">' +
                    '<label><span class="danger">主队&nbsp;</span><span class="fonts">' + (item[BK.withdraws('ratio_ouhu')].replace("O", "大").replace("U", "小") || '大') + '</span></label>' +
                    '<label class="normal font-bold">' + item[BK.withdraws('ior_OUHU')] + '</label>' +
                    '</p>' +
                    '<p vi="ior_OUCU" fonts="ratio_oucu">' +
                    '<label><span>客队&nbsp;</span><span class="fonts">' + (item[BK.withdraws('ratio_oucu')].replace("O", "大").replace("U", "小") || '小') + '</span></label>' +
                    '<label class="normal font-bold">' + item[BK.withdraws('ior_OUCU')] + '</label>' +
                    '</p>' +
                    '</div>' +
                    '</div>' +
                    '</div></div>';
                if ((i + 1) == BK.fields.glists.length) {
                    html += '</div>';
                }
            });
        }
        $('.list_box').html(html);
    },
    withdraws: function(str) {
        return BK.fields.gheads[0].indexOf(str);
    },
    searchSubStr: function(str, strArray, keys) {
        var pos = str.indexOf(strArray[0]);
        while (pos > -1) {
            var endpos = str.indexOf(strArray[1], pos);
            BK.positions[keys].push([pos, endpos])
            pos = str.indexOf(strArray[0], pos + 1);
        }
    },
    countDown: function() {
        if (BK.countdown == 0) {
            BK.initAll('loadings');
        } else {
            BK.countdown--;
        }
        // $('.refreshBtn').text('刷新（' + BK.countdown + '）');
        setTimeout(function() {
            BK.countDown();
        }, 1000);
    },
    countWinGold: function() {
        if ($('#moneyes').val().length > 5) {
            $('#moneyes').val($('#moneyes').val().slice(0, 5));
        }
        $('#canwin').text(($('#moneyes').val() * $('#ioradio_id').text()).toFixed(2));
    },
    sureDeal: function(datastr, is) {
        var re = /^[1-9]\d*$/;
        var datajson = datastr; //.replace(/'/g,'"')
        var money = $('#moneyes').val();
        if (!money) {
            layer.open({
                content: '请输入下注金额！',
                skin: 'msg',
                time: 2
            });
            return;
        }
        if (!re.test(money)) {
            layer.open({
                content: '下注金额只能为正整数！',
                skin: 'msg',
                time: 2
            });
            return;
        }
        if (money < 10) {
            layer.open({
                content: '下注金额至少10点！',
                skin: 'msg',
                time: 2
            });
            return;
        }
        if (money > 10000) {
            layer.open({
                content: '下注金额至多10000点！',
                skin: 'msg',
                time: 2
            });
            return;
        }
        datajson.money = money;
        this.ajax.post('/member/bet-member', datajson, function(data) {
            if (data.code == '2018') {
                layer.open({
                    content: '下注成功！',
                    skin: 'msg',
                    time: 2
                });
                $('button').attr('disabled', 'disabled');
                setTimeout(function() {
                    layer.close(is);
                }, 1000);
            } else if (data.code == '1103') {
                layer.open({
                    content: '钱包余额不足',
                    skin: 'msg',
                    time: 2
                });
            }
            //
            else if (data.code == '1116') {
                layer.open({
                    content: '赛事已经关闭，请重新选择',
                    skin: 'msg',
                    time: 2
                });
                setTimeout(function() {
                    layer.close(is);
                }, 1000);
            } else if (data.code == '1106') {
                layer.open({
                    content: '赔率已更新，请重新下单',
                    skin: 'msg',
                    time: 2
                });
                setTimeout(function() {
                    layer.close(is);
                }, 1000);
            } else {
                layer.open({
                    content: '下注失败',
                    skin: 'msg',
                    time: 2
                });
            }
        });
    }
};
