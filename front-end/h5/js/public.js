
var public = {
  notice: function(ul) {
        var li = ul.find('li').eq(0).html();
        ul.append('<li>' + li + '</li>');
        var num = 0;
        setInterval(function() {
            num++;
            if (num == ul.find('li').length) {
                num = 1;
                ul.css({
                    marginTop: 0
                });
            }
            $('.notice-txt').animate({
                marginTop: -30 * num
            }, 400);
        }, 2000);
    },
};