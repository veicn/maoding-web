/**
 * 审批操作-审批意见
 * Created by wrb on 2018/9/6.
 */
;(function ($, window, document, undefined) {

    "use strict";
    var pluginName = "m_approval_operational_comments",
        defaults = {
            isDialog:true,
            doType:1,//1=同意,2=退回,3=撤销
            dataInfo:null,
            saveCallBack:null
        };

    // The actual plugin constructor
    function Plugin(element, options) {
        this.element = element;

        this.settings = $.extend({}, defaults, options);
        this._defaults = defaults;
        this._name = pluginName;
        this.init();
    }

    // Avoid Plugin.prototype conflicts
    $.extend(Plugin.prototype, {
        init: function () {
            var that = this;


            if(that.settings.doType==3){//撤销，不需要填写意见
                that.confirm(restApi.url_repealApprove);
                return false;
            }
            if(that.settings.doType==1 && that.settings.dataInfo.processFlag!='1'){//同意，固定流程，条件流程不需要填写意见
                that.agree();
                return false;
            }

            that.renderDialog(function () {
                that.settings.dataInfo.doType = that.settings.doType;
                var html = template('m_approval/m_approval_operational_comments', that.settings.dataInfo);
                $(that.element).html(html);
                that.bindActionClick();
            });

        }
        //初始化数据,生成html
        ,renderDialog:function (callBack) {

            var that = this;
            if(that.settings.isDialog){//以弹窗编辑
                S_dialog.dialog({
                    title: that.settings.title||'确定审批',
                    contentEle: 'dialogOBox',
                    lock: 3,
                    width: '400',
                    height:'140',
                    url: rootPath+'/assets/module/m_common/m_dialog.html'

                },function(d){//加载html后触发
                    that.element = 'div[id="content:'+d.id+'"] .dialogOBox';
                    if(callBack!=null)
                        callBack();

                });
            }else{//不以弹窗编辑
                if(callBack!=null)
                    callBack();
            }
        }
        //同意
        ,agree:function (auditPerson) {
            var that = this;
            var options = {};
            options.classId = that.element;
            options.postData = {
                id:that.settings.dataInfo.id,
                auditMessage:$(that.element).find('textarea[name="auditMessage"]').val(),
                auditPerson:auditPerson
            };
            options.url=restApi.url_agreeExpMain;

            m_ajax.postJson(options,function (response) {

                if(response.code=='0'){

                    S_toastr.success('操作成功');
                    S_dialog.close($(that.element));

                    if(that.settings.saveCallBack)
                        that.settings.saveCallBack();

                }else {
                    S_dialog.error(response.info);
                }

            });
        }
        //退回
        ,confirm:function (url) {
            var that = this;
            var options = {};
            options.classId = that.element;
            options.postData = {
                id:that.settings.dataInfo.id,
                auditMessage:$(that.element).find('textarea[name="auditMessage"]').val()
            };
            options.url = url;

            m_ajax.postJson(options,function (response) {

                if(response.code=='0'){

                    S_toastr.success('操作成功');
                    S_dialog.close($(that.element));

                    if(that.settings.saveCallBack)
                        that.settings.saveCallBack();

                }else {
                    S_dialog.error(response.info);
                }

            });
        }
        ,bindActionClick:function () {
            var that = this;
            $(that.element).find('button[data-action]').off('click').on('click',function () {
                var $this = $(this),dataAction = $this.attr('data-action');

                switch(dataAction){

                    case 'agreeAndDone'://同意
                        that.agree();
                        break;
                    case 'agreeAndToNext'://同意

                        var options = {};
                        options.url = restApi.url_getOrgTree;
                        options.isASingleSelectUser = 2;
                        options.isOkSave = false;
                        options.cancelText = '关闭';
                        options.selectUserCallback = function (data,event) {

                            S_dialog.confirm('确定同意并转交['+data.userName+']审批？', function () {
                                that.agree(data.companyUserId);
                                S_dialog.close($(event));
                            }, function () {
                                S_dialog.close($(event));
                            });

                        };
                        $('body').m_orgByTree(options);
                        break;

                    case 'confirm'://确定

                        if(that.settings.doType==1){
                            that.agree();
                        }else if(that.settings.doType==2){
                            that.confirm(restApi.url_recallExpMain);
                        }else if(that.settings.doType==3){
                            that.confirm(restApi.url_repealApprove);
                        }
                        break;
                }

            });
        }


    });

    $.fn[pluginName] = function (options) {
        return this.each(function () {
            // if (!$.data(this, "plugin_" + pluginName)) {
            $.data(this, "plugin_" +
                pluginName, new Plugin(this, options));
            // }
        });
    };

})(jQuery, window, document);