var regs = /^[+]{0,1}(\d+)$|^[+]{0,1}(\d+\.\d+)$/;
$(function() {
    $('.forminput').bind('input propertychange', function() {
        if ($(this)[0] == $('#rechargebtb')[0]) {
            var current = 0,
                btbrate = parseFloat($('.cny-rate').text());
            if (!$('#rechargebtb').val() || !regs.test($('#rechargebtb').val())) {
                current = 0;
            } else {
                current = parseFloat($('#rechargebtb').val());
            }
            var btb_rmb = parseFloat((current / btbrate).toFixed(10));
            $('#btb-rmb').val(btb_rmb.toFixed(10) + ' 点');
            checks([$(this)]);
        }
    }).blur(function() {
        checks([$(this)]);
    }).focus(function() {
        $(this).removeClass('deposit-m');
    });
    $('.btn-submit').click(function() {
        var bitype = $('#bitype').val(),
            rechargebtb = $('#rechargebtb').val(),
            btb_rmb = $('#btb-rmb').val(),
            telphone = $('#telphone').val(),
            purseaddress = $('#purseaddress').val(),
            withpass = $('#withpass').val(),
            beizhu = $('#beizhu').val();
        if(checks([$('#rechargebtb'),$('#telphone'),$('#purseaddress'),$('#withpass')])) {
            console.log(true);
        } else {
            console.log(false);
        }
    });
});

function checks(checkArr) {
    var rtype = true,
        text = {
            "in_rec": "取款金额",
            "in_tel": "电话号码",
            "in_pur": "钱包地址",
            "in_wit": "取款密码"
        };
    $.each(checkArr, function(i, item) {
        var rdata = {
                type: false
            },
            itemval = item.val();
        if (!itemval) {
            rdata.value = '请输入' + text[item.attr('inpt')];
        } else {
            if (item[0] == $('#rechargebtb')[0]) {
                if (itemval < 1000) {
                    rdata.value = '单笔取款最低金额 1000 点';
                } else if (!regs.test(itemval)) {
                    rdata.value = '请输入正确的' + text[item.attr('inpt')];
                } else {
                    rdata.type = true;
                }
            } else if (item[0] == $('#telphone')[0]) {
                var reg = /0?(13|14|15|17|18)[0-9]{9}/;
                if (!reg.test(itemval)) {
                    rdata.value = '请输入正确的' + text[item.attr('inpt')];
                } else {
                    rdata.type = true;
                }
            } else if (item[0] == $('#withpass')[0]) {
                var reg = /^[0-9]{4}$/;
                if (!reg.test(itemval)) {
                    rdata.value = '请输入正确的' + text[item.attr('inpt')];
                } else {
                    rdata.type = true;
                }
            } else {
                rdata.type = true;
            }
        }
        if (rdata.type) {
            $('.btc-tip[inpt="' + item.attr('inpt') + '"]').text('').hide();
        } else {
            rtype = false
            $('.btc-tip[inpt="' + item.attr('inpt') + '"]').text(rdata.value).show();
            item.addClass('deposit-m');
        }

    });

    return rtype;
}
