$(function() {
    TD.initPage();
    var type = common.getParameter('type');
    $('.recordtype li').removeClass('active');
    if (type == 'deposit') {
        $('.recordtype li[litype=0]').addClass('active');
        $('.filtrate .czjl').show();
        $('#withdrawStatus').html(TD.czjl.select);
        TD.loadList(TD.czjl.cols, {
            record: '0'
        }, '0');
    } else {
        $('.recordtype li[litype=1]').addClass('active');
        $('.filtrate .czjl').hide();
        $('#withdrawStatus').html(TD.txjl.select);
        TD.loadList(TD.txjl.cols, {
            record: '1'
        }, '1');
    }

});

var TD = {
    ajax: new HttpService(),
    txjl: {
        select: '<option value="" selected>全部</option><option value="0">等待处理</option><option value="1">审核中</option><option value="2">支付完成</option><!-- <option value="-1">已取消</option> --><option value="-2">审批拒绝</option>',
        cols: [
            [{
                field: 'time',
                title: '申请时间',
            }, {
                field: 'number',
                title: '提现单号',
            }, {
                field: 'moneyAddress',
                title: '钱包地址',
            }, {
                field: 'money',
                title: '提现金额(点)',
            }, {
                field: 'state',
                title: '状态',
                templet: function(data) {
                    var st = '';
                    switch (data.state) {
                        case '0':
                            st = '等待处理';
                            break;
                        case '1':
                            st = '审核中';
                            break;
                        case '2':
                            st = '支付完成';
                            break;
                        case '-1':
                            st = '已取消';
                            break;
                        case '-2':
                            st = '审批拒绝';
                            break;
                        default:
                            st = '-';
                    }
                    return st;
                }
            }, {
                field: 'remark',
                title: '备注',
            }, {
                field: 'resultRemark',
                title: '审核意见',
            }]
        ],

    },
    czjl: {
        select: '<option value="" selected>全部</option><option value="0">等待支付</option><option value="1">支付成功</option><option value="-1">审核中</option><option value="2">充值成功</option><option value="-2">充值失败</option>',
        cols: [
            [{
                field: 'time',
                title: '申请时间',
            }, {
                field: 'number',
                title: '充值单号',
            }, {
                field: 'money',
                title: '充值金额(点)',
            }, {
                field: 'discounts',
                title: '优惠金额(点)',
            }, {
                field: 'type',
                title: '充值类型',
                templet: function(data) {
                    var types = '-';
                    $.each(config.coin, function(i, item) {
                        if (item.value == data.type) {
                            types = item.name;
                            return false;
                        }
                    });
                    return types;
                }
            }, {
                field: 'state',
                title: '状态',
                minWidth: '100',
                templet: function(data) {
                    var st = '';
                    switch (data.state) {
                        case '0':
                            st = '等待支付';
                            break;
                        case '1':
                            st = '支付成功';
                            break;
                        case '-1':
                            st = '审核中';
                            break;
                        case '2':
                            st = '充值成功';
                            break;
                        case '-2':
                            st = '充值失败';
                            break;
                        default:
                            st = '-';
                    }
                    return st;
                }
            }, {
                field: 'resultRemark',
                title: '审核意见',
            }, {
                title: '操作',
                fixed: 'right',
                width: 100,
                align: 'center',
                templet: function(data) {
                    var html = '<a class="layui-btn layui-btn-xs layui-btn-disabled" style="width: 70px;">已支付</a>';
                    if (data.state == '0') {
                        html = '<a class="layui-btn layui-btn-xs" onclick="TD.surePay(\'' + data.number + '\');" style="width: 70px;">确认支付</a>';
                    } else if (data.state == '-1') {
                        html = '<a class="layui-btn layui-btn-xs layui-btn-disabled" style="width: 70px;">审核中</a>';
                    } else if (data.state == '2') {
                        html = '<a class="layui-btn layui-btn-xs layui-btn-disabled" style="width: 70px;">充值成功</a>';
                    } else if (data.state == '-2') {
                        html = '<a class="layui-btn layui-btn-xs layui-btn-disabled" style="width: 70px;">充值失败</a>';
                    }
                    return html;
                }
            }]
        ],
    },
    xzjl: {
        select: '<option value="" selected>全部</option><option value="0">未结算</option><option value="1">已结算</option><option value="2">待结算</option>',
        types: '<option value="" selected>所有类型</option><option value="RFT">足球</option><option value="REFT">滚球-足球</option><option value="RBK">篮球</option><option value="REBK">滚球-篮球</option>',
        cols: [
            [{
                field: 'betTime',
                title: '下注时间',
            }, {
                field: 'number',
                title: '下注单号',
            }, {
                field: 'league',
                title: '联赛名称',
            }, {
                field: 'teamh',
                title: '主场队伍',
            }, {
                field: 'teamc',
                title: '客场队伍',
            }, {
                field: 'money',
                width: 110,
                minWidth: 110,
                align: 'center',
                title: '下注金额(点)',
            }, {
                field: 'betType',
                title: '比赛类型',
                width: 95,
                minWidth: 95,
                align: 'center',
                templet: function(data) {
                    var st = '';
                    switch (data.betType) {
                        case 'RFT':
                            st = '足球';
                            break;
                        case 'REFT':
                            st = '滚球-足球';
                            break;
                        case 'RBK':
                            st = '篮球';
                            break;
                        case 'REBK':
                            st = '滚球-篮球';
                            break;
                        default:
                            st = '-';
                    }
                    return st;
                }
            }, {
                title: '注单选项',
                minWidth: 280,
                templet: function(data) {
                    var strArr = ['大','小','单大','单小','单','双'];
                    var st = '';
                    if (data.iorType == '大' || data.iorType == '小') {
                        st = '大小'
                    } else if (data.iorType == '单大' || data.iorType == '单小') {
                        st = '积分大小'
                    } else if (data.iorType == '单' || data.iorType == '双') {
                        st = '单双'
                    } else {
                        st = data.iorType;
                    }
                    var alltype = data.iorType;
                    if(strArr.indexOf(alltype) > -1) {
                        if(alltype == '单大') {
                            alltype = '大';
                        }
                        if(alltype == '单小') {
                            alltype = '小';
                        }
                    } else {
                        alltype = '';
                    }
                    var bs = '';
                    if (data.iorType == '让球' || data.iorType == '让分') {
                        if (data.bet == data.strong) {
                            bs = '(让方)';
                        } else {
                            bs = '(受让方)';
                        }
                        if (data.bet == 'N') {
                            bs = '';
                        }
                    }
                    var startStr = '<span style="color:red;">' + (data.bet == 'H' ? '主场' : (data.bet == 'C' ? '客场' : '和局')) + bs + '</span>' + ' - ';
                    if (data.iorType == '大' || data.iorType == '小') {
                        startStr = '';
                    }
                    return startStr + st + ' [ ' + alltype + (data.iorRatio ? data.iorRatio : "") + '&nbsp;<span style="color:red;">@' + data.ratio + '</span> ]';
                }
            }, {
                field: 'state',
                title: '类型',
                width: 85,
                minWidth: 85,
                align: 'center',
                fixed: 'right',
                templet: function(data) {
                    var st = '';
                    switch (data.state) {
                        case '0':
                            st = '未结算';
                            break;
                        case '1':
                            st = '已结算';
                            break;
                        case '2':
                            st = '待结算';
                            break;
                        default:
                            st = '-';
                    }
                    return st;
                }
            }, {
                field: 'dealMoney',
                title: '盈亏',
                fixed: 'right',
                width: 100,
                minWidth: 100,
                align: 'center',
                templet: function(data) {
                    return data.dealMoney == null ? ' - ' : data.dealMoney;
                }
            }, {
                field: 'winLose',
                title: '状态',
                width: 60,
                minWidth: 60,
                align: 'center',
                fixed: 'right',
                templet: function(data) {
                    var reData = '-';
                    switch (data.winLose) {
                        case '1':
                            reData = '<span style="color:green;" class="fwb">赢</span>';
                            break;
                        case '0':
                            reData = '<span style="color:orange;" class="fwb">平</span>';
                            break;
                        case '-1':
                            reData = '<span style="color:red;" class="fwb">输</span>';
                            break;
                        default:
                            reData = '-';
                    }
                    return reData;
                }
            }]
        ],
    },
    initPage: function() {

        laydate.render({
            elem: '#allTime',
            type: 'datetime',
            format: 'yyyy-MM-dd HH:mm',
            // theme: '#D69C13',
            theme: 'grid',
            range: '至'
        });
        $('.recordtype li').on('click', function() {
            $(this).addClass('active').siblings().removeClass('active');
            var litype = $(this).attr('litype');
            $('#allTime').val('');
            if (litype == '1') {
                $('.filtrate .czjl').hide();
                $('.filtrate .xzjl').hide();
                $('#withdrawStatus').html(TD.txjl.select);
                TD.loadList(TD.txjl.cols, {
                    record: '1'
                }, '1');
            } else if (litype == '0') {
                $('.filtrate .czjl').show();
                $('.filtrate .xzjl').hide();
                var coinhtml = '<option value="" selected>所有类型</option>';
                $.each(config.coin, function(i, item) {
                    coinhtml += '<option value="' + item.value + '">' + item.name + '（' + item.value + '）</option>';
                });
                $('#withdrawType').html(coinhtml);
                $('#withdrawStatus').html(TD.czjl.select);
                TD.loadList(TD.czjl.cols, {
                    record: '0'
                }, '0');
            } else if (litype == '2') {
                $('.filtrate .czjl').show();
                $('.filtrate .xzjl').show();
                $('#withdrawStatus').html(TD.xzjl.select);
                $('#withdrawType').html(TD.xzjl.types);
                TD.loadList(TD.xzjl.cols, {}, '2');
            }
        });
        $('#all-search').on('click', function() {
            var litype = $('.recordtype li.active').attr('litype'),
                allTime = $('#allTime').val().split(' 至 '),
                withdrawType = $('#withdrawType').val(),
                withdrawStatus = $('#withdrawStatus').val(),
                keyword = $('#keyword').val();

            var cols = '',
                param = {
                    beginTime: allTime[0] || "",
                    endTime: allTime[1] || "",
                    state: withdrawStatus
                };
            if (litype == '0') {
                cols = TD.czjl.cols;
                param.record = litype;
                param.type = withdrawType;
            } else if (litype == '1') {
                cols = TD.txjl.cols;
                param.record = litype;
            } else if (litype == '2') {
                cols = TD.xzjl.cols;
                param.keyword = keyword;
                param.betType = withdrawType;
            }
            TD.loadList(cols, param, litype);
        });
    },
    loadList: function(cols, param, litype) {
        var url = ServerUrl;
        if (litype == '0' || litype == '1') {
            url += '/member/member-record';
        } else if (litype == '2') {
            url += '/member/single-note';
        }
        layui.use('table', function() {
            var table = layui.table;
            table.render({
                elem: '#deal-list',
                url: url,
                method: 'post',
                where: param,
                request: {
                    pageName: 'pageNo', //页码的参数名称，默认：page
                    limitName: 'pageSize' //每页数据量的参数名，默认：limit
                },
                response: {
                    statusName: 'code', //数据状态的字段名称，默认：code
                    statusCode: "2018", //成功的状态码，默认：0
                    countName: 'total', //数据总数的字段名称，默认：count
                    dataName: 'result', //数据列表的字段名称，默认：data
                },
                headers: {
                    "content-type": "application/x-www-form-urlencoded",
                    "token": sessionStorage.getItem('toid')
                },
                limits: [5, 10, 30, 60],
                loading: true,
                page: true, //开启分页
                // data: data,
                cols: cols,
                done: function(res, curr, count) {
                    if (res.code == '1109' || res.code == '1114') {
                        layer.msg('登录超时，请重新登陆', {
                            time: 2000,
                            icon: 2
                        });
                        sessionStorage.setItem('userinfo', '');
                        sessionStorage.setItem('toid', '');
                        setTimeout(function() {
                            window.location.href = 'home.html';
                        }, 2000);
                    }
                }
            });
        });
    },

    surePay: function(oId) {
        TD.ajax.post('/member/member-pay', {
            number: oId
        }, function(data) {
            if (data.code == '2018') {
                layer.msg('确认支付提交成功，请耐心等待工作人员处理', {
                    time: 2000,
                    icon: 1
                });
                setTimeout(function() {
                    $('#all-search').click();
                }, 2000);
            } else if (data.code == '1117') {
                layer.msg('您没有在规定时间内完成操作，订单已失效', {
                    time: 2000,
                    icon: 0
                });
                setTimeout(function() {
                    $('#all-search').click();
                }, 2000);
            } else {
                layer.msg('确认支付提交失败', {
                    time: 2000,
                    icon: 1
                });
            }
        }, function(e) {
            layer.msg('网络错误', {
                time: 2000,
                icon: 2
            });
        });
    },
}
