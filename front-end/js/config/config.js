var config = {
    // sportsBetting: {
    navtype: [{
            "name": "滚球",
            "tabtype": "runball",
            "countType": "RB"
        }, {
            "name": "今日赛事",
            "tabtype": "today",
            "countType": "FT"
        },
        // {"name":"早盘","tabtype":"early","countType":"FU"},
    ],
    balltype: [{
            "name": "足球",
            "tabtype": "FT"
        }, {
            "name": "篮球美式足球",
            "tabtype": "BK"
        },
        // {"name":"网球","tabtype":"TN"},
        // {"name":"排球","tabtype":"VB"},
        // {"name":"羽毛球","tabtype":"BM"},
        // {"name":"乒乓球","tabtype":"TT"},
        // {"name":"棒球","tabtype":"BS"},
        // {"name":"其他","tabtype":"OP"},
    ],
    common: {
        type: 'POST',
        data: {
            uid: '41E1C90D347A90C6A60811350',
            langx: 'zh-cn',
            page_no: 0,
            mtype: 3,
            league_id: '',
            hot_game: ''
        }
    },
    //今日足球
    today_FT: {
        ttype: 'today_FT',
        url: 'https://www.ylg56789.com/app/member/FT_browse/body_var',
        data: {
            rtype: 'r'
        }
    },
    //今日篮球
    today_BK: {
        ttype: 'today_BK',
        url: 'https://www.ylg56789.com/app/member/BK_browse/body_var',
        data: {
            rtype: 'r_main'
        }
    },
    //滚球-足球
    runball_FT: {
        ttype: 'runball_FT',
        url: 'https://www.ylg56789.com/app/member/FT_browse/body_var',
        data: {
            rtype: 're'
        }
    },
    //滚球-篮球
    runball_BK: {
        ttype: 'runball_BK',
        url: 'https://www.ylg56789.com/app/member/BK_browse/body_var',
        data: {
            rtype: 're_main',
        }
    },
    anal: {
        gheads: ["_.GameHead = [", "];"], //列表数据解析字段
        gpages: ["_.t_page = ", "_.gamount"], //列表页数
        gptotals: ["_.gamount = ", ";"], //列表每页的数据条数
        gcounts: ["_.gameCount = ", ";"], //ball-type的各个类型的数据
        glists: ["g([", "]);"], //列表数据
    },
    html_FT: '<tr class="theads"><th>时间</th><th>赛事</th><th>独赢</th><th>全场 - 让球</th><th>全场 - 大小</th><th>单双</th><th>独赢</th><th>半场 - 让球</th><th>半场 - 大小</th></tr>',
    html_BK: '<tr class="theads"><th>时间</th><th>赛事</th><th>独赢</th><th>让分</th><th>大小</th><th colspan="2">球队积分：大小</th></tr>',
    dataless: '<tr><td colspan="9" style="color:#000;line-height:50px;">您选择的项目暂时没有赛事。请修改您的选项或迟些再返回。</td></tr>',

    corder: {
        type: 'GET',
        url: 'https://www.ylg56789.com/app/member/',
        ftbk: '_order',
        // td: 'm',
        // rb: 'rm',
        data: {
            uid: '41E1C90D347A90C6A60811350',
            odd_f_type: 'H',
            langx: 'zh-cn',
        }
    },
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
        }]
        // }
}
