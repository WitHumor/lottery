
$(function() {
    TD.initPage();
    $('.filtrate .czjl').hide();
    $('#withdrawStatus').html(TD.txjl.select);
    TD.loadList(TD.txjl.cols, {
        record: '1'
    });
});

var TD = {
    txjl: {
        select: '<option value="" selected>全部</option><option value="0">等待处理</option><option value="1">等待支付</option><option value="2">支付完成</option><option value="-1">已取消</option><option value="-2">审批拒绝</option>',
        cols: [
            [{
                    field: 'time',
                    title: '申请时间',
                    templet: function(d) {
                        // console.log(d);
                    }
                }, {
                    field: 'number',
                    title: '提现单号',
                }, {
                    field: 'bankNumber',
                    title: '钱包地址',
                }, {
                    field: 'money',
                    title: '提现金额',
                }, {
                    field: 'state',
                    title: '状态',
                }
                // , {
                //     field: 'operate',
                //     title: '操作',
                // }
                , {
                    field: 'remark',
                    title: '备注',
                }
            ]
        ],

    },
    czjl: {
        select: '<option value="" selected>全部</option><option value="1">等待</option><option value="0">成功</option><option value="-1">失败</option>',
        cols: [
            [{
                field: 'time',
                title: '申请时间',
            }, {
                field: 'number',
                title: '充值单号',
            }, {
                field: 'money',
                title: '充值金额',
            }, {
                field: 'type',
                title: '充值类型',
            }, {
                field: 'state',
                title: '状态',
            }]
        ],
    },
    initPage: function() {
        laydate.render({
            elem: '#allTime',
            type: 'datetime',
            format: 'yyyy-MM-dd HH:mm',
            theme: 'grid',
            range: '至'
        });
        $('.recordtype li').on('click', function() {
            $(this).addClass('active').siblings().removeClass('active');
            var litype = $(this).attr('litype');
            $('#allTime').val('');
            if (litype == '1') {
                $('.filtrate .czjl').hide();
                $('#withdrawStatus').html(TD.txjl.select);
                TD.loadList(TD.txjl.cols, {
                    record: '1'
                });
            } else {
                $('.filtrate .czjl').show();
                $('#withdrawStatus').html(TD.czjl.select);
                TD.loadList(TD.czjl.cols, {
                    record: '0'
                });
            }
        });
        $('#all-search').on('click', function() {
            var litype = $('.recordtype li.active').attr('litype'),
                allTime = $('#allTime').val().split(' 至 '),
                withdrawType = $('#withdrawType').val(),
                withdrawStatus = $('#withdrawStatus').val();
            var param = {
                record: litype,
                beginTime: allTime[0] || "",
                endTime: allTime[1] || "",
                state: withdrawStatus
            };
            if (litype == '0') {
                param.type = withdrawType;
            }
            var cols = litype == '1' ? TD.txjl.cols : TD.czjl.cols;
            TD.loadList(cols, param);
        });
    },
    loadList: function(cols, param) {
        layui.use('table', function() {
            var table = layui.table;
            table.render({
                elem: '#deal-list',
                url: ServerUrl + '/member/member-record',
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
                    "token": sessionStorage.getItem('token')
                },
                limits: [5, 10, 30, 60],
                loading: true,
                page: true, //开启分页
                // data: data,
                cols: cols,
                done: function(res, curr, count) {
                    console.log(res);
                }
            });
        });
    }
}
