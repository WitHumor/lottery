var regs = /^[+]{0,1}(\d+)$|^[+]{0,1}(\d+\.\d+)$/;
$(function() {
    $('#rechargebtb').bind('input propertychange', function() {
        var current = 0,
            btbrate = parseFloat($('.cny-btc-rate').text());
        if (!$('#rechargebtb').val() || !regs.test($('#rechargebtb').val())) {
            current = 0;
        } else {
            current = parseFloat($('#rechargebtb').val());
        }
        var btb_rmb = parseFloat((btbrate * current).toFixed(2)),
            charge_dis = parseFloat((btb_rmb * 0.01).toFixed(2));
        $('#btb-rmb').val(btb_rmb.toFixed(2) + ' 点');
        $('#discounts').val('188.00 点');
        $('#charge-dis').val(charge_dis.toFixed(2) + ' 点');
        $('#total-charge').val((btb_rmb + charge_dis + 188.00).toFixed(2) + ' 点');
        checks();
    }).blur(function() {
        checks();
    }).focus(function() {
        $('#rechargebtb').removeClass('deposit-m');
    });

    $('.btn-next').click(function() {
        var t = checks();
        if (t) {
            $('#firsts').hide();
            $('#nexts').show();
            $('#nexts .finish-div').html('<a class="btn-submit btn-finish">完成支付</a>');
        }
    });

    $('.paytype li[litype="BTB"]').click(function() {
        $('#firsts').show();
        $('#nexts').hide();
        $('#nexts .finish-div').html('');
    });

    // 全局设置
    ZeroClipboard.setDefaults({
        moviePath: '../../js/ZeroClipboard.swf'
    });
    new ZeroClipboard([document.getElementById("btn-copy-address"), document.getElementById("btn-copy-amount")]).on('complete', function(client, args) {
        // layer.tips('成功复制：'+$('#btn-copy-address').attr('data-clipboard-text'), '#btn-copy-address');
        layer.msg('内容已经复制，你可以使用Ctrl+V 粘贴！');
    });
});

function checks() {
    var btb = $('#rechargebtb').val();
    var rdata = {
        type: false
    };
    if (!btb) {
        rdata.value = '请输入充值金额';
    } else if (btb > 200) {
        rdata.value = '单笔充值最高金额 200 个数字货币';
    } else if (btb < 0.1) {
        rdata.value = '单笔充值最低金额 0.1 个数字货币';
    } else if (!regs.test(btb)) {
        rdata.value = '请输入正确的充值金额';
    } else {
        rdata.type = true;
    }

    if (rdata.type) {
        $('.btc-tip').text('').hide();
        $('.btn-next').removeClass('disabled');
    } else {
        $('.btc-tip').text(rdata.value).show();
        $('#rechargebtb').addClass('deposit-m');
        $('.btn-next').addClass('disabled');
    }
    return rdata.type;
}
