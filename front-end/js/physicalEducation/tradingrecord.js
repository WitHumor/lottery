var datas1 = [{
    "applyTime": 10000,
    "orderNumber": "user-0",
    "bankCard": "女",
    "money": "城市-0",
    "status": "签名-0",
    "operate": 255,
    "remark": 24,
}, {
    "applyTime": 10001,
    "orderNumber": "user-0",
    "bankCard": "女",
    "money": "城市-0",
    "status": "签名-0",
    "operate": 255,
    "remark": 24,
}, {
    "applyTime": 10002,
    "orderNumber": "user-0",
    "bankCard": "女",
    "money": "城市-0",
    "status": "签名-0",
    "operate": 255,
    "remark": 24,
}, {
    "applyTime": 10003,
    "orderNumber": "user-0",
    "bankCard": "女",
    "money": "城市-0",
    "status": "签名-0",
    "operate": 255,
    "remark": 24,
}, {
    "applyTime": 10004,
    "orderNumber": "user-0",
    "bankCard": "女",
    "money": "城市-0",
    "status": "签名-0",
    "operate": 255,
    "remark": 24,
}, {
    "applyTime": 10005,
    "orderNumber": "user-0",
    "bankCard": "女",
    "money": "城市-0",
    "status": "签名-0",
    "operate": 255,
    "remark": 24,
}, {
    "applyTime": 10006,
    "orderNumber": "user-0",
    "bankCard": "女",
    "money": "城市-0",
    "status": "签名-0",
    "operate": 255,
    "remark": 24,
}, {
    "applyTime": 10006,
    "orderNumber": "user-0",
    "bankCard": "女",
    "money": "城市-0",
    "status": "签名-0",
    "operate": 255,
    "remark": 24,
}, {
    "applyTime": 10007,
    "orderNumber": "user-0",
    "bankCard": "女",
    "money": "城市-0",
    "status": "签名-0",
    "operate": 255,
    "remark": 24,
}, {
    "applyTime": 10008,
    "orderNumber": "user-0",
    "bankCard": "女",
    "money": "城市-0",
    "status": "签名-0",
    "operate": 255,
    "remark": 24,
}];

var datas2 = [{
    "applyTime": 10000,
    "orderNumber": "user-0",
    "money": "城市-0",
    "status": "签名-0",
    "type": 255,
}, {
    "applyTime": 10001,
    "orderNumber": "user-0",
    "money": "城市-0",
    "status": "签名-0",
    "type": 255,
}, {
    "applyTime": 10002,
    "orderNumber": "user-0",
    "money": "城市-0",
    "status": "签名-0",
    "type": 255,
}, {
    "applyTime": 10003,
    "orderNumber": "user-0",
    "money": "城市-0",
    "status": "签名-0",
    "type": 255,
}, {
    "applyTime": 10004,
    "orderNumber": "user-0",
    "money": "城市-0",
    "status": "签名-0",
    "type": 255,
}, {
    "applyTime": 10005,
    "orderNumber": "user-0",
    "money": "城市-0",
    "status": "签名-0",
    "type": 255,
}, {
    "applyTime": 10006,
    "orderNumber": "user-0",
    "money": "城市-0",
    "status": "签名-0",
    "type": 255,
}, {
    "applyTime": 10006,
    "orderNumber": "user-0",
    "money": "城市-0",
    "status": "签名-0",
    "type": 255,
}, {
    "applyTime": 10007,
    "orderNumber": "user-0",
    "money": "城市-0",
    "status": "签名-0",
    "type": 255,
}, {
    "applyTime": 10008,
    "orderNumber": "user-0",
    "money": "城市-0",
    "status": "签名-0",
    "type": 255,
}];

$(function() {
    TD.initPage();
    $('.filtrate .czjl').hide();
    $('#withdrawStatus').html(config.czjl.select);
    TD.loadList(config.txjl.cols, datas1, {record: '1'});
});

var TD = {
    initPage: function() {
        laydate.render({
            elem: '#startTime',
            type: 'datetime',
            format: 'yyyy-MM-dd HH:mm',
            theme: 'grid',
            range: true
        });
        // laydate.render({
        //     elem: '#endTime',
        //     type: 'datetime',
        //     format: 'yyyy-MM-dd HH:mm',
        //     theme: 'grid'
        // });
        $('.recordtype li').on('click', function() {
            $(this).addClass('active').siblings().removeClass('active');
            var litype = $(this).attr('litype');
            $('#startTime').val('');
            $('#endTime').val('');
            if (litype == '1') {
                $('.filtrate .czjl').hide();
                $('#withdrawStatus').html(config.txjl.select);
                TD.loadList(config.txjl.cols, datas1, {record: '1'});
            } else {
                $('.filtrate .czjl').show();
                $('#withdrawStatus').html(config.czjl.select);
                TD.loadList(config.czjl.cols, datas2, {record: '0'});
            }
        });
        $('#all-search').on('click', function() {
          var litype = $('.recordtype li.active').attr('litype'),
              startTime = $('#startTime').val(),
              endTime = $('#endTime').val(),
              withdrawType = $('#withdrawType').val(),
              withdrawStatus = $('#withdrawStatus').val();
          var param = {
            record: litype,
            beginTime: startTime,
            endTime: endTime,
            state: withdrawStatus
          };
          if(litype == '0') {
            param.type = withdrawType;
          }
          var cols = litype == '1' ? config.txjl.cols : config.czjl.cols;
          var data = litype == '1' ? datas1 : datas2;
          TD.loadList(cols, data, param);
        });
    },
    loadList: function(cols, data, param) {
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
                    statusCode: 2018, //成功的状态码，默认：0
                    //msgName: 'hint', //状态信息的字段名称，默认：msg
                    countName: 'total', //数据总数的字段名称，默认：count
                    dataName: 'rows', //数据列表的字段名称，默认：data
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
                done: function(res, curr, count){
                    console.log(res);
                }
            });
        });
    }
}
