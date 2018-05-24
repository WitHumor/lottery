/**

 @Name：layuiAdmin 公共业务
 @Author：贤心
 @Site：http://www.layui.com/admin/
 @License：LPPL

 */

layui.define(function(exports) {
    var $ = layui.$,
        layer = layui.layer,
        laytpl = layui.laytpl,
        setter = layui.setter,
        view = layui.view,
        admin = layui.admin

    //公共业务的逻辑处理可以写在此处，切换任何页面都会执行
    //……

    , common = {
        dtimeInit: function(ids) {
            layui.laydate.render({
                elem: ids,
                type: 'datetime',
                format: 'yyyy-MM-dd HH:mm',
                theme: 'grid',
                range: '至'
            });
        },
        getList: function(options) {
            if (!options.elem || !options.url || !options.cols) {
                layer.msg('列表参数有误', {
                    offset: '15px',
                    anim: 6,
                    time: 2000
                });
                return;
            }
            layui.table.render({
                id: 'idAllTable',
                elem: options.elem,
                url: setter.serviceUrl + options.url,
                method: options.type || 'post',
                where: options.param || {},
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
                    "token": layui.sessionData(setter.tableName)[setter.request.tokenName]
                },
                cellMinWidth: 80,
                loading: true,
                page: true,
                limits: [10, 30, 60, 90],
                cols: options.cols,
                done: function(res, curr, count) {
                    if (setter.response.statusCode['logout'].indexOf(res[setter.response.statusName]) > -1) {
                        layer.msg('登录超时，请重新登陆', {
                            offset: '15px',
                            time: 2000,
                            icon: 2
                        });
                        setTimeout(function() {
                            layui.view.exit();
                        }, 2000);
                        return;
                    }
                    typeof options.done === 'function' && options.done(res, curr, count);
                }
            });
        },
    }


    //退出
    admin.events.logout = function() {
        //执行退出接口
        admin.req({
            url: './json/user/logout.js',
            type: 'get',
            data: {},
            done: function(res) { //这里要说明一下：done 是只有 response 的 code 正常才会执行。而 succese 则是只要 http 为 200 就会执行

                //清空本地记录的 token，并跳转到登入页
                admin.exit();
            }
        });
    };


    //对外暴露的接口
    exports('common', common);
});
