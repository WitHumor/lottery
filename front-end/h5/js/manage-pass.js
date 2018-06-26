$(function() {
    MP.initPage();
});

var MP = {
    ajax: new HttpService(),
    initPage: function() {
        public.payInput();
        $('.pagenavi a').click(function() {
            if (!$(this).hasClass('active')) {
                $(this).addClass('active').parent('li').siblings().find('a').removeClass('active');
                $('#sliders input').val('').removeClass('deposit-m').attr('type', 'password');
                $('.clear_all').hide();
                $('.icon-yanjing').removeClass('icon-yanjing').addClass('icon-iconcloseeye');
                $('.tip_text').hide();
            }
            if ($('.pagenavi a.active').attr('state') == '1') {
                $('#sliders input').attr('maxlength', '12');
            } else {
                $('#sliders input').attr('maxlength', '4');
            }
        });
        $('.to_submit').click(function() {
            if (public.checkinput()) {
                MP.ajax.post('/member/update-password', {
                    oldPassword: hex_sha1($('#oldPassword').val()),
                    newPassword: hex_sha1($('#newPassword').val()),
                    state: $('.pagenavi a.active').attr('state')
                }, function(data) {
                    if (data.code == '2018') {
                        $('.to_submit').attr('disabled', 'disabled');
                        layer.open({
                            content: '密码修改成功',
                            skin: 'msg',
                            time: 2
                        });
                        setTimeout(function() {
                            window.location.reload();
                        }, 1500);
                    } else if (data.code == '1123') {
                        layer.open({
                            content: '新密码与原密码不能相同',
                            skin: 'msg',
                            time: 2
                        });
                    } else if (data.code == '1108') {
                        layer.open({
                            content: '原密码错误',
                            skin: 'msg',
                            time: 2
                        });
                    } else {
                        layer.open({
                            content: '密码修改失败',
                            skin: 'msg',
                            time: 2
                        });
                    }
                });
            }
        });

    },
};
