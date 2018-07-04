$(function() {
    FT.initPage();
});

var FT = {
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
        $('.i-category').on('click', function() {
            var c = '<div class="categorys"><a href="basketball.html" class="primary"><i class="iconfont icon-tiyu-lanqiu f26"></i><p>篮球</p></a><a href="javascript:void(0);" class="normal"><i class="iconfont icon-21466 f26"></i><p>彩票</p></a><a href="bet.html" class="warning"><i class="iconfont icon-xiazhu- f26"></i><p>注单</p></a><a href="index.html" class="danger"><i class="iconfont icon-shouye f26"></i><p>首页</p></a></div>';
            layer.open({
                content: c,
                skin: 'footer'
            });
        });
        $('.run_today a').on('click', function() {
            $(this).addClass('active').siblings('a').removeClass('active');
            FT.currentPage = 0;
            $('.list_box').html('');
            FT.initAll();
        });
        // $('.list_box').on('click', '.league', function() {
        //     $(this).siblings('.leng_child').toggleClass('hide');
        // });
        // $('.list_box').on('click', '.teams, .game_info', function() {
        //     $(this).siblings('.game_option').toggleClass('hide');
        // });
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
                    window.location.href = 'login.html?where=football';
                }, 1500);
                return;
            }
            var rate_label = $(this).children('label')[1];
            if ($(rate_label).text()) {
                var tType = $(this).index() == 0 ? 'H' : ($(this).index() == 1 ? 'C' : 'N'),
                    fonts = $(this).attr('fonts'),
                    league = $(this).parents('.leng_child').siblings('p.league').children('label').text(), //联赛名称
                    teams = $(this).parents('.game_option').siblings('p.teams').children('span').text().split(' -vs- '),
                    indextext = $(this).parents('.every_choice').prev().text(), //选项名称
                    dataInfo = {
                        gid: $(this).parents('.leng_child').attr('gid'),
                        url: FT.currentUrl,
                        ratio: $(this).attr('vi'),
                        bet: tType,
                        betType: 'FT',
                        ratioData: parseFloat($(rate_label).text()).toFixed(2),
                    };
                if (fonts) {
                    dataInfo.iorRatio = fonts;
                }
                var show_team = (tType == "H" ? teams[0] : (tType == "C" ? teams[1] : '和局'));
                if (indextext.indexOf('大小') > -1 || indextext.indexOf('单双') > -1) {
                    show_team = '';
                }
                if (indextext.indexOf('让球') > -1) {
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
                // icon-lanqiu_basketball4
                var styles = 'position:fixed; bottom:0; left:0; width: 100%; height: 100%; border:none;',
                    content = '<div class="layertitle"><i class="iconfont icon-close iconclose"></i><i class="iconfont icon-zuqiu_soccer37 f30 vertical-middle mR5"></i><span class="vertical-middle">下注单</span></div>' +
                    '<div class="thiscontent">' +
                    '<div class="order_s_cont">' +
                    '<label class="font-bold">联赛名称：</label>' +
                    '<label>' + league + '</label>' +
                    '</div>' +
                    '<div class="order_s_cont">' +
                    '<label class="font-bold">下注类型：</label>' +
                    '<label class="warning font-bold">' + indextext + ($('.run_today a.active').attr('rtype') == "re" ? " - 滚球" : "") + '</label>' +
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
                    '<input id="moneyes" type="number" onkeyup="return FT.countWinGold()" maxlength="5">' +
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
                          FT.sureDeal(dataInfo,indexs);
                        });
                    },
                });
            }
        });
    },
    initAll: function(ls) {
        FT.positions = {};
        FT.fields = {};
        FT.gCountObj = {};
        FT.countdown = 3;
        FT.initList(ls);
    },
    initList: function(loadings) {
        var rtype = $('.run_today a.active').attr('rtype');
        FT.currentUrl = 'https://www.ylg56789.com/app/member/FT_browse/body_var?uid=' + jsons.together.uid + '&rtype=' + rtype + '&langx=zh-cn&mtype=3&page_no=' + FT.currentPage + '&league_id=&hot_game=';
        var indexlayer = '';
        $.ajax({
            type: 'POST',
            url: ServerUrl + '/member/load-url',
            data: {
                url: FT.currentUrl
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
                    FT.analysis(data);
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
            FT.positions[v] = [];
            FT.searchSubStr(info, jsons.anal[v], v);
        }
        for (v in FT.positions) { //v 为属性名
            FT.fields[v] = [];
            if (FT.positions[v].length > 0) {
                $.each(FT.positions[v], function(i, item) {
                    var thisone;
                    if (v == "gcounts") {
                        thisone = info.substring(item[1], item[0] + jsons.anal[v][0].length).replace(new RegExp(/\'/g), '').split(',');
                    } else {
                        thisone = info.substring(item[1], item[0] + jsons.anal[v][0].length).replace(new RegExp(/\'|\s/g), '').split(',');
                    }
                    FT.fields[v].push(thisone);
                });
            }
        }
        if (FT.fields.gcounts[0].length > 0) {
            $.each(FT.fields.gcounts[0], function(i, item) {
                var pasArr = item.replace(new RegExp(/\s/g), '').split('|');
                FT.gCountObj[pasArr[0] + "_" + pasArr[1]] = pasArr[2];
            });
        }
        $('.run_today a[rtype="r"] label').text('今日赛事(' + FT.gCountObj.FT_FT + ')');
        $('.run_today a[rtype="re"] label').text('滚球(' + FT.gCountObj.FT_RB + ')');
        $('.pagings .firstp, .pagings .prevp, .pagings .nextp, .pagings .lastp').off();
        if (FT.fields.gpages[0][0] == '0' || FT.fields.gpages[0][0] == '1') {
            $('.pagings a').hide();
        } else {
            $('.pagings a').show();
            $('.pagings .firstp').on('click', function() {
                $('.run_today a.active').click();
            });
            if (FT.currentPage == 0) {
                $('.pagings .prevp').hide();
            } else {
                $('.pagings .prevp').show();
            }
            if ((FT.currentPage + 1) == FT.fields.gpages[0][0]) {
                $('.pagings .nextp').hide();
            } else {
                $('.pagings .nextp').show();
            }
            $('.pagings .prevp').on('click', function() {
                if (FT.currentPage == 0) return;
                else {
                    FT.currentPage--;
                    FT.initAll();
                }
            });
            $('.pagings .nextp').on('click', function() {
                if ((FT.currentPage + 1) == FT.fields.gpages[0][0]) return;
                else {
                    FT.currentPage++;
                    FT.initAll();
                }
            });
            $('.pagings .lastp').on('click', function() {
                FT.currentPage = parseInt(FT.fields.gpages[0][0]) - 1;
                FT.initAll();
            });
        }
        $('.pagings .curp').text(FT.currentPage + 1);
        $('.pagings .totalp').text((FT.fields.gpages[0][0] == '0' || FT.fields.gpages[0][0] == '1') ? '1' : FT.fields.gpages[0][0]);
        var html = '',
            leagues = '!PERCH';
        if (FT.fields.glists.length == 0) {
            html += '<img src="img/nodata.png" class="nodatas">';
            $('.pagings').addClass('vish');
        } else {
            $.each(FT.fields.glists, function(i, item) {
                // if (i == 0) {
                $('.pagings').removeClass('vish');
                // }
                if (item[FT.withdraws('gid')] == '1855882' && item[FT.withdraws('league')] == '银靴赛(在澳洲)') {
                    if (FT.fields.glists.length == 1) {
                        html += '<img src="img/nodata.png" class="nodatas">';
                        $('.pagings').addClass('vish');
                        return false;
                    } else {
                        return true;
                    }
                }

                if (item[FT.withdraws('league')] != leagues) {
                    html += '</div>';
                    leagues = item[FT.withdraws('league')];
                    html += '<div class="each_box"><p class="league"><i class="iconfont icon-liansai f30 vertical-middle"></i><label class="vertical-middle font-bold">' + leagues + '</label></p>';
                }
                var timeabout = '';
                if ($('.run_today a.active').attr('rtype') == 'r') {
                    timeabout = '<label class="mL5 mR10">' + item[FT.withdraws('datetime')].replace(/<br>/g, '&nbsp;') + '</label>';
                } else {
                    if (item[FT.withdraws('retimeset')].indexOf('Start') > -1) {
                        // timeabout ='<label class="biaoqian"> - </label><label class="warning mR10">0-0</label>';
                        timeabout = '';
                    } else {
                        var strArr = item[FT.withdraws('retimeset')].split('^');
                        if (strArr[1].indexOf('半场') > -1) {
                            timeabout = '<label class="biaoqian">半场</label><label class="warning mR10">' + (item[FT.withdraws('score_h')] || 0) + '-' + (item[FT.withdraws('score_c')] || 0) + '</label>';
                        } else {
                            var hingf = item[FT.withdraws('timer')] < '46' ? '上半场' : '下半场';
                            timeabout = '<label class="biaoqian">' + hingf + '</label><label class="warning mR10">' + (item[FT.withdraws('score_h')] || 0) + '-' + (item[FT.withdraws('score_c')] || 0) + '</label><label class="mR10">' + strArr[1] + '\'</label>';
                        }
                    }
                }
                html += '<div class="leng_child " gid="' + item[FT.withdraws('gid')] + '">' +
                    '<p class="teams">' +
                    '<i class="iconfont icon-shiliangzhinengduixiang- f16 vertical-middle"></i>' +
                    '<span class="vertical-middle">' + item[FT.withdraws('team_h')] + ' -vs- ' + item[FT.withdraws('team_c')] + '</span>' +
                    '</p>' +
                    '<div class="game_info">' + timeabout + '</div>' +
                    '<div class="game_option clear ">' +
                    '<div class="each_option">' +
                    '<p class="text-left mB5">全场独赢</p>' +
                    '<div class="every_choice">' +
                    '<p vi="ior_MH">' +
                    '<label><span class="danger">主队</span></label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_MH')] + '</label>' +
                    '</p>' +
                    '<p vi="ior_MC">' +
                    '<label>客队</label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_MC')] + '</label>' +
                    '</p>' +
                    '<p vi="ior_MN">' +
                    '<label>和局</label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_MN')] + '</label>' +
                    '</p>' +
                    '</div>' +
                    '</div>' +
                    '<div class="each_option">' +
                    '<p class="text-left mB5">半场独赢</p>' +
                    '<div class="every_choice">' +
                    '<p vi="ior_HMH">' +
                    '<label class="danger"><span class="danger">主队</span></label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_HMH')] + '</label>' +
                    '</p>' +
                    '<p vi="ior_HMC">' +
                    '<label>客队</label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_HMC')] + '</label>' +
                    '</p>' +
                    '<p vi="ior_HMN">' +
                    '<label>和局</label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_HMN')] + '</label>' +
                    '</p>' +
                    '</div>' +
                    '</div>' +
                    '<div class="each_option">' +
                    '<p class="text-left mB5">全场让球</p>' +
                    '<div class="every_choice" vals="' + item[FT.withdraws('ratio')] + '">' +
                    '<p vi="ior_RH" fonts="' + (item[FT.withdraws('strong')] == 'H' ? 'ratio' : '') + '">' +
                    '<label><span class="danger">主队&nbsp;</span><span class="fonts">' + (item[FT.withdraws('strong')] == 'H' ? item[FT.withdraws('ratio')] : '') + '</span></label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_RH')] + '</label>' +
                    '</p>' +
                    '<p vi="ior_RC" fonts="' + (item[FT.withdraws('strong')] == 'C' ? 'ratio' : '') + '">' +
                    '<label><span>客队&nbsp;</span><span class="fonts">' + (item[FT.withdraws('strong')] == 'C' ? item[FT.withdraws('ratio')] : '') + '</span></label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_RC')] + '</label>' +
                    '</p>' +
                    '</div>' +
                    '</div>' +
                    '<div class="each_option">' +
                    '<p class="text-left mB5">半场让球</p>' +
                    '<div class="every_choice" vals="' + item[FT.withdraws('hratio')] + '">' +
                    '<p vi="ior_HRH" fonts="' + (item[FT.withdraws('hstrong')] == 'H' ? 'hratio' : '') + '">' +
                    '<label><span class="danger">主队&nbsp;</span><span class="fonts">' + (item[FT.withdraws('hstrong')] == 'H' ? item[FT.withdraws('hratio')] : '') + '</span></label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_HRH')] + '</label>' +
                    '</p>' +
                    '<p vi="ior_HRC" fonts="' + (item[FT.withdraws('hstrong')] == 'C' ? 'hratio' : '') + '">' +
                    '<label><span>客队&nbsp;</span><span class="fonts">' + (item[FT.withdraws('hstrong')] == 'C' ? item[FT.withdraws('hratio')] : '') + '</span></label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_HRC')] + '</label>' +
                    '</p>' +
                    '</div>' +
                    '</div>' +
                    '<div class="each_option">' +
                    '<p class="text-left mB5">全场大小</p>' +
                    '<div class="every_choice">' +
                    '<p vi="ior_OUC" fonts="ratio_o">' +
                    '<label class="fonts">' + (item[FT.withdraws('ratio_o')].replace("O", "大").replace("U", "小") || '大') + '</label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_OUC')] + '</label>' +
                    '</p>' +
                    '<p vi="ior_OUH" fonts="ratio_u">' +
                    '<label class="fonts">' + (item[FT.withdraws('ratio_u')].replace("O", "大").replace("U", "小") || '小') + '</label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_OUH')] + '</label>' +
                    '</p>' +
                    '</div>' +
                    '</div>' +
                    '<div class="each_option">' +
                    '<p class="text-left mB5">半场大小</p>' +
                    '<div class="every_choice">' +
                    '<p vi="ior_HOUC" fonts="hratio_o">' +
                    '<label class="fonts">' + (item[FT.withdraws('hratio_o')].replace("O", "大").replace("U", "小") || '大') + '</label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_HOUC')] + '</label>' +
                    '</p>' +
                    '<p vi="ior_HOUH" fonts="hratio_u">' +
                    '<label class="fonts">' + (item[FT.withdraws('hratio_u')].replace("O", "大").replace("U", "小") || '小') + '</label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_HOUH')] + '</label>' +
                    '</p>' +
                    '</div>' +
                    '</div>' +
                    '<div class="each_option">' +
                    '<p class="text-left mB5">单双</p>' +
                    '<div class="every_choice">' +
                    '<p vi="ior_EOO" fonts="str_odd">' +
                    '<label class="fonts">' + (item[FT.withdraws('str_odd')] || '单') + '</label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_EOO')] + '</label>' +
                    '</p>' +
                    '<p vi="ior_EOE" fonts="str_even">' +
                    '<label class="fonts">' + (item[FT.withdraws('str_even')] || '双') + '</label>' +
                    '<label class="normal font-bold">' + item[FT.withdraws('ior_EOE')] + '</label>' +
                    '</p></div></div></div></div>';
                if ((i + 1) == FT.fields.glists.length) {
                    html += '</div>';
                }
            });
        }
        $('.list_box').html(html);
    },
    withdraws: function(str) {
        return FT.fields.gheads[0].indexOf(str);
    },
    searchSubStr: function(str, strArray, keys) {
        var pos = str.indexOf(strArray[0]);
        while (pos > -1) {
            var endpos = str.indexOf(strArray[1], pos);
            FT.positions[keys].push([pos, endpos])
            pos = str.indexOf(strArray[0], pos + 1);
        }
    },
    countDown: function() {
        if (FT.countdown == 0) {
            FT.initAll('loadings');
        } else {
            FT.countdown--;
        }
        // $('.refreshBtn').text('刷新（' + FT.countdown + '）');
        setTimeout(function() {
            FT.countDown();
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
                }, 2000);
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
                }, 2000);
            } else if (data.code == '1106') {
                layer.open({
                    content: '赔率已更新，请重新下单',
                    skin: 'msg',
                    time: 2
                });
                setTimeout(function() {
                    layer.close(is);
                }, 2000);
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
