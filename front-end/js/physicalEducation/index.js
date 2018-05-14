var CDS = 3;
$(function() {
    init();
    getAllDatas(config.today_FT);
    // countDown();
});

var positions = {},
    fields = {},
    gCountObj = {},
    currentPage = 0,
    countdown = CDS,
    currentUrl = '';

function init() {
    setInterval(function() {
        $(".time > span").text(new Date().format("yyyy年MM月dd日 hh:mm:ss"));
    }, 1000);
    $.each(config.navtype, function(i, item) {
        $('#navType').append('<li><a href="javascript:void(0);" class="' + (item.name == "今日赛事" ? "active" : "") + '" tabType="' + item.tabtype + '" countType="' + item.countType + '">' + item.name + '</a></li>');
    });
    $.each(config.balltype, function(i, item) {
        $('#ballType').append((i == 0 ? '' : '<span> | </span>') + '<li><a href="javascript:void(0);" class="' + (i == 0 ? 'active' : '') + '" tabType="' + item.tabtype + '">' + item.name + '(<label>0</label>)</a></li>');
    });
    //两级tab的事件绑定
    $('#navType a, #ballType a').on('click', function() {
        $(this).addClass('active').parent('li').siblings().children('a').removeClass('active');
        getActiveTab();
    });
}

function getActiveTab() {
    var navtype = $('#navType a.active').attr('tabType');
    var balltype = $('#ballType a.active').attr('tabType');
    positions = {};
    fields = {};
    gCountObj = {};
    countdown = CDS;
    getAllDatas(config[navtype + "_" + balltype]);
}

function getAllDatas(reqData) {
    var datas = $.extend({}, config.common.data, reqData.data, {
        page_no: currentPage
    });
    currentUrl = reqData.url + '?uid=' + datas.uid + '&rtype=' + datas.rtype + '&langx=zh-cn&mtype=3&page_no=' + datas.page_no + '&league_id=&hot_game=';
    $.ajax({
        type: config.common.type,
        url: reqData.url,
        data: datas,
        success: function(data) {
            analysis(data);
        },
        error: function(error) {
            console.log(error);
        }
    });
}

function changepage(et) {
    currentPage = $(et).val() - 1;
    getActiveTab();
}


function analysis(datas) {
    for (v in config.anal) {
        positions[v] = [];
        searchSubStr(datas, config.anal[v], v);
    }
    for (v in positions) { //v 为属性名
        fields[v] = [];
        if (positions[v].length > 0) {
            $.each(positions[v], function(i, item) {
                var thisone;
                if (v == "gcounts") {
                    thisone = datas.substring(item[1], item[0] + config.anal[v][0].length).replace(new RegExp(/\'/g), '').split(',');
                } else {
                    thisone = datas.substring(item[1], item[0] + config.anal[v][0].length).replace(new RegExp(/\'|\s/g), '').split(',');
                }
                fields[v].push(thisone);
            });
        }
    }
    // console.log(positions);
    // console.log(fields);
    // console.log(fields.glists);
    if (fields.gcounts[0].length > 0) {
        $.each(fields.gcounts[0], function(i, item) {
            var pasArr = item.replace(new RegExp(/\s/g), '').split('|');
            gCountObj[pasArr[0] + "_" + pasArr[1]] = pasArr[2];
        });
    }
    var ctype = $('#navType a.active').attr('countType');
    for (let i = 0; i < $('#ballType a').length; i++) {
        var ttype = $($('#ballType a')[i]).attr('tabType');
        $($('#ballType a')[i]).find('label').text(gCountObj[ttype + "_" + ctype]);
    }
    var leagues = '!x',
        select_option = '';
    if (fields.gpages == "0" || fields.gpages == "1") {
        select_option += '<select onchange="changepage(this)" disabled><option value="1">1</option></select>';
    } else {
        select_option += '<select onchange="changepage(this)">';
        for (let x = 0; x < fields.gpages; x++) {

            select_option += '<option value="' + (x + 1) + '" ' + (currentPage == x ? "selected" : "") + '>' + (x + 1) + '</option>';
        }
        select_option += '</select>';
    }
    var html = '<div class="listcontainer"><div class="listss listtitle"><span>' + $('#navType a.active').text() + '：' + $('#ballType a.active').text().split('(')[0] + '</span></div>' +
        '<div class="listss listinfo"><span> ' + (currentPage + 1) + '/' + (fields.gpages == "0" ? "1" : fields.gpages) + ' 页&nbsp;&nbsp;</span>' + select_option + '<button class="refreshBtn" onclick="getActiveTab();">刷新（' + countdown + '）</button></div><table>';
    // if ($('#ballType a.active').attr('tabType') == 'FT') {
    //     html += config.html_FT;
    // } else if ($('#ballType a.active').attr('tabType') == 'BK') {
    //     html += config.html_BK;
    // }
    html += config['html_' + $('#ballType a.active').attr('tabType')];
    if (fields.glists.length == 0) {
        html += config.dataless;
    } else {
        // if()
        $.each(fields.glists, function(i, item) {
            if ($('#ballType a.active').attr('tabType') == 'FT') {
                if (item[withdraws('gid')] == '1855882' && item[withdraws('league')] == '银靴赛(在澳洲)') {
                    if (fields.glists.length == 1) {
                        html += config.dataless;
                        return false;
                    } else {
                        return true;
                    }
                }
                if (item[withdraws('league')] != leagues) {
                    leagues = item[withdraws('league')];
                    html += '<tr class="everytitle"><td colspan="9">&nbsp;&nbsp;' + item[withdraws('league')] + '</td></tr>';
                }
                html += '<tr gid="' + item[withdraws('gid')] + '" tmtype="H">' +
                    '<td rowspan="3">' + item[withdraws('datetime')] + '</td>' +
                    '<td class="coal" rowspan="3">' + item[withdraws('team_h')] + '<br>' + item[withdraws('team_c')] + '<br>和局</td>' +
                    '<td><a href="javascript:void(0);" class="canclick" vi="ior_MH">' + item[withdraws('ior_MH')] + '</a></td>' +
                    '<td class="t_right">' + (item[withdraws('strong')] == 'H' ? item[withdraws('ratio')] + "&nbsp;" : '') + '<a href="javascript:void(0);" class="canclick" vi="ior_RH">' + item[withdraws('ior_RH')] + '</a></td>' +
                    '<td class="t_right">' + item[withdraws('ratio_o')].replace("O", "大").replace("U", "小") + '&nbsp;<a href="javascript:void(0);" class="canclick" vi="ior_OUC">' + item[withdraws('ior_OUC')] + '</a></td>' +
                    '<td>' + item[withdraws('str_odd')] + '&nbsp;<a href="javascript:void(0);" class="canclick odd_even" vi="ior_EOO">' + item[withdraws('ior_EOO')] + '</a></td>' +
                    '<td class="bgF0F1B5"><a href="javascript:void(0);" class="canclick halfs" vi="ior_HMH">' + item[withdraws('ior_HMH')] + '</a></td>' +
                    '<td class="t_right bgF0F1B5">' + (item[withdraws('hstrong')] == 'H' ? item[withdraws('hratio')] + "&nbsp;" : '') + '<a href="javascript:void(0);" class="canclick halfs" vi="ior_HRH">' + item[withdraws('ior_HRH')] + '</a></td>' +
                    '<td class="t_right bgF0F1B5">' + item[withdraws('hratio_o')].replace("O", "大").replace("U", "小") + '&nbsp;<a href="javascript:void(0);" class="canclick halfs" vi="ior_HOUC">' + item[withdraws('ior_HOUC')] + '</a></td>' +
                    '</tr>';
                html += '<tr gid="' + item[withdraws('gid')] + '" tmtype="C"><td class="hide"></td><td class="hide"></td><td><a href="javascript:void(0);" class="canclick" vi="ior_MC">' + item[withdraws('ior_MC')] + '</a></td>' +
                    '<td class="t_right">' + (item[withdraws('strong')] == 'C' ? item[withdraws('ratio')] + "&nbsp;" : '') + '<a href="javascript:void(0);" class="canclick" vi="ior_RC">' + item[withdraws('ior_RC')] + '</a></td>' +
                    '<td class="t_right">' + item[withdraws('ratio_u')].replace("O", "大").replace("U", "小") + '&nbsp;<a href="javascript:void(0);" class="canclick" vi="ior_OUH">' + item[withdraws('ior_OUH')] + '</a></td>' +
                    '<td>' + item[withdraws('str_even')] + '&nbsp;<a href="javascript:void(0);" class="canclick odd_even" vi="ior_EOE">' + item[withdraws('ior_EOE')] + '</a></td>' +
                    '<td class="bgF0F1B5"><a href="javascript:void(0);" class="canclick halfs" vi="ior_HMC">' + item[withdraws('ior_HMC')] + '</a></td>' +
                    '<td class="t_right bgF0F1B5">' + (item[withdraws('hstrong')] == 'C' ? item[withdraws('hratio')] + "&nbsp;" : '') + '<a href="javascript:void(0);" class="canclick halfs" vi="ior_HRC">' + item[withdraws('ior_HRC')] + '</a></td>' +
                    '<td class="t_right bgF0F1B5">' + item[withdraws('hratio_u')].replace("O", "大").replace("U", "小") + '&nbsp;<a href="javascript:void(0);" class="canclick halfs" vi="ior_HOUH">' + item[withdraws('ior_HOUH')] + '</a></td>' +
                    '</tr>';
                html += '<tr gid="' + item[withdraws('gid')] + '" tmtype="N"><td class="hide"></td><td class="hide"></td><td><a href="javascript:void(0);" class="canclick" vi="ior_MN">' + item[withdraws('ior_MN')] + '</a></td>' +
                    '<td colspan="3"></td><td class="hide"></td><td class="hide"></td>' +
                    '<td class="bgF0F1B5"><a href="javascript:void(0);" class="canclick halfs" vi="ior_HMN">' + item[withdraws('ior_HMN')] + '</a></td>' +
                    '<td class="bgF0F1B5" colspan="2"></td><td class="hide"></td>' +
                    '</tr>';
                var finfo = JSON.stringify({
                    gid: item[withdraws('gid')],
                    league: item[withdraws('league')],
                    team_h: item[withdraws('team_h')],
                    team_c: item[withdraws('team_c')],
                });
                html += '<tr fgid="f' + item[withdraws('gid')] + '" class="hide" finfo="' + finfo.replace(/"/g, '\'') + '"></tr>';
            } else if ($('#ballType a.active').attr('tabType') == 'BK') {
                if (item[withdraws('league')] != leagues) {
                    leagues = item[withdraws('league')];
                    html += '<tr class="everytitle"><td colspan="7">&nbsp;&nbsp;' + item[withdraws('league')] + '</td></tr>';
                }
                html += '<tr gid="' + item[withdraws('gid')] + '">' +
                    '<td rowspan="2">' + item[withdraws('datetime')].replace("RunningBall", "<span class=\"c_red\">滚球<span>") + '</td>' +
                    '<td class="coal" rowspan="2">' + item[withdraws('team_h')] + '<br>' + item[withdraws('team_c')] + '</td>' +
                    '<td><a href="javascript:void(0);" class="canclick" vi="ior_MH">' + item[withdraws('ior_MH')] + '</a></td>' +
                    '<td class="t_right">' + (item[withdraws('strong')] == 'H' ? item[withdraws('ratio')] + "&nbsp;" : '') + '<a href="javascript:void(0);" class="canclick" vi="ior_RH">' + item[withdraws('ior_RH')] + '</a></td>' +
                    '<td>' + item[withdraws('ratio_o')].replace("O", "大").replace("U", "小") + '&nbsp;<a href="javascript:void(0);" class="canclick" vi="ior_OUC">' + item[withdraws('ior_OUC')] + '</a></td>' +
                    '<td class="t_right bgFCF9E7">' + item[withdraws('ratio_ouho')].replace("O", "<label class=\"c_24B335\">大</label>").replace("U", "<label class=\"c_B48438\">小</label>") + '&nbsp;<a href="javascript:void(0);" class="canclick" vi="ior_OUHO">' + item[withdraws('ior_OUHO')] + '</a></td>' +
                    '<td class="t_right bgFCF9E7">' + item[withdraws('ratio_ouhu')].replace("O", "<label class=\"c_24B335\">大</label>").replace("U", "<label class=\"c_B48438\">小</label>") + '&nbsp;<a href="javascript:void(0);" class="canclick" vi="ior_OUHU">' + item[withdraws('ior_OUHU')] + '</a></td>' +
                    '</tr>';
                html += '<tr gid="' + item[withdraws('gid')] + '"><td><a href="javascript:void(0);" class="canclick" vi="ior_MC">' + item[withdraws('ior_MC')] + '</a></td>' +
                    '<td class="t_right">' + (item[withdraws('strong')] == 'C' ? item[withdraws('ratio')] + "&nbsp;" : '') + '<a href="javascript:void(0);" class="canclick" vi="ior_RC">' + item[withdraws('ior_RC')] + '</a></td>' +
                    '<td>' + item[withdraws('ratio_u')].replace("O", "大").replace("U", "小") + '&nbsp;<a href="javascript:void(0);" class="canclick" vi="ior_OUH">' + item[withdraws('ior_OUH')] + '</a></td>' +
                    '<td class="t_right bgECE3C4">' + item[withdraws('ratio_ouco')].replace("O", "<label class=\"c_24B335\">大</label>").replace("U", "<label class=\"c_B48438\">小</label>") + '&nbsp;<a href="javascript:void(0);" class="canclick" vi="ior_OUCO">' + item[withdraws('ior_OUCO')] + '</a></td>' +
                    '<td class="t_right bgECE3C4">' + item[withdraws('ratio_oucu')].replace("O", "<label class=\"c_24B335\">大</label>").replace("U", "<label class=\"c_B48438\">小</label>") + '&nbsp;<a href="javascript:void(0);" class="canclick" vi="ior_OUCU">' + item[withdraws('ior_OUCU')] + '</a></td>' +
                    '</tr>';
                var finfo = JSON.stringify({
                    gid: item[withdraws('gid')],
                    league: item[withdraws('league')],
                    team_h: item[withdraws('team_h')],
                    team_c: item[withdraws('team_c')],
                });
                html += '<tr fgid="f' + item[withdraws('gid')] + '" class="hide" finfo="' + finfo.replace(/"/g, '\'') + '"></tr>';
            }
        });
    };
    html += '</table></div>';
    $('#datalists').html(html);
    $('.canclick').on('click', function() {
        var me = $(this);
        tgid = me.parents('tr').attr('gid'),
            tmtype = me.parents('tr').attr('tmtype'),
            finfo = JSON.parse(me.parents('tr').siblings('tr[fgid="f' + tgid + '"]').attr('finfo').replace(/'/g, '"')),
            navtype = $('#navType a.active').attr('tabType'),
            balltype = $('#ballType a.active').attr('tabType');
        console.log(tgid, navtype, balltype, finfo);
        var indextext = $('.theads th').eq(me.parents('tr').children('td').index(me.parents('td'))).text();
        // if(me.hasClass('halfs')) {
        //     getData.gid = finfo.hgid;
        //     getData.strong = finfo.hstrong;
        // } else {
        //     getData.gid;
        //     getData.strong = finfo.strong;
        // }
        // if(navtype == 'runball') {
        //     nt = 'R';
        //     rt = '_rm';
        // }
        // if(me.hasClass('odd_even')) {
        //     getData.rtype = nt + (tmtype=='H' ? 'ODD' : 'EVEN');
        // }
        var dsinfo = JSON.stringify({
            gid: finfo.gid,
            url: currentUrl,
            ratio: me.attr('vi'),
            ratioData: parseFloat(me.text()).toFixed(2),
        }).replace(/"/g, '\'');
        var html = '<div class="dtitle">交易单</div>' +
            '<div class="dleague"><span>篮球美式足球</span></div>' +
            '<div class="tinfo commons">' +
            '<p>' + finfo.league + '</p><p>' + indextext + '</p>' +
            '<p><span class="tName">' + finfo.team_h + ' <font class="radio">vs</font> ' + finfo.team_c +
            '</span></p></div>' +
            '<div class="chsteam commons"><label class="c_red">' + (tmtype == "H" ? finfo.team_h : finfo.team_c) + '</label> @ <strong class="light" id="ioradio_id">' + parseFloat(me.text()).toFixed(2) + '</strong></div><div class="fwb commons">' +
            '<input type="checkbox" checked/> 自动接收较佳赔率</div>' +
            '<div class="tranDetail commons"><p>交易金额：' +
            '<input id="money" type="text" onkeypress="checkKey()" onkeyup="return countWinGold()" maxlength="10"/></p><p>可赢金额：<span id="canwin">0</span></p><p>单注最低：10</p><p>单注最高：10000000</p></div>' +
            '<div><button class="cancelDeal" onclick="cancelDeal()">取消</button>' +
            '<button class="sureDeal" onclick="sureDeal(' + dsinfo + ')">确定交易</button></div>';

        $('.dealingTicket').html(html);
    });
}

function searchSubStr(str, strArray, keys) {
    var pos = str.indexOf(strArray[0]);
    while (pos > -1) {
        var endpos = str.indexOf(strArray[1], pos);
        positions[keys].push([pos, endpos])
        pos = str.indexOf(strArray[0], pos + 1);
    }
}

function checkKey() {
    var re = /^[0-9]+$/ ;
    var money = $('#money').val();
    if(!money) {return;}
    if(!re.test(money)) {
        layer.msg("请输入数字！", {
            time: 2000
        });
        // var rate = $('#ioradio_id').text();
        // $('#canwin').text((money*rate).toFixed(2));
        // return true;
    }
}

function countWinGold() {
    // var money = $('#money').val();
    $('#canwin').text(($('#money').val()*$('#ioradio_id').text()).toFixed(2));
}

function cancelDeal() {
    $('.dealingTicket').html('<div class="dtitle">交易单</div><div class="noDeal"><div>点击赔率便可将<br>选项加到交易单里</div><!-- <div>赛事已经关闭</div><button>确定</button> --></div>');
}

function sureDeal(datastr) {
    console.log(datastr);
    var datajson = datastr; //.replace(/'/g,'"')
    var money = $('#money').val();
    if (!money) {
        // alert('请输入下注金额！');
        layer.msg("请输入下注金额！", {
            time: 2000
        });
        return;
    }
    datajson.money = money;

    $.ajax({
        type: 'get',
        url: ServerUrl + 'member/bet-member',
        data: datajson,
        success: function(data) {
            var datas = JSON.parse(data);
            console.log(datas.result);
            if(datas.result == '2018') {
                layer.msg("下注成功！", {
                    icon: 1,
                    time: 2000
                });
                setTimeout(function() {
                    cancelDeal();
                },2000);
            } else {

            }
        },
        error: function(e) {
            console.log(e);
        }
    });
}

function withdraws(str) {
    return fields.gheads[0].indexOf(str);
}

function countDown() {
    if (countdown == 0) {
        getActiveTab();
    } else {
        countdown--;
    }
    $('.refreshBtn').text('刷新（' + countdown + '）');
    setTimeout(function() {
        countDown();
    }, 1000);
    // setInterval(function() {countDown();}, 1000);
}
