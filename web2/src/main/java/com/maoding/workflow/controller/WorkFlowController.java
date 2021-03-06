package com.maoding.workflow.controller;

import com.maoding.activiti.dto.*;
import com.maoding.activiti.service.WorkflowService;
import com.maoding.core.base.controller.BaseController;
import com.maoding.core.bean.AjaxMessage;
import com.maoding.core.util.BeanUtils;
import com.maoding.dynamicForm.controller.DynamicFormController;
import com.maoding.dynamicForm.dto.FormQueryDTO;
import com.maoding.dynamicForm.service.DynamicFormService;
import com.maoding.process.dto.ProcessDefineEditDTO;
import com.maoding.process.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 深圳市卯丁技术有限公司
 *
 * @author : 张成亮
 * @date : 2018/7/31
 * @description : 工作流接口
 */
@Controller
@RequestMapping("/iWork/workflow")
public class WorkFlowController extends BaseController {
    /** 工作流服务接口 */
    @Autowired
    private WorkflowService workflowService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private DynamicFormService dynamicFormService;

    @Autowired
    private DynamicFormController dynamicFormController;

    /**
     * 描述       加载流程，准备进行编辑
     * 日期       2018/8/2
     * @author   张成亮
     * @param    prepareRequest 加载信息
     *              如果未指定accountId，currentCompanyId，则使用当前用户信息
     *              如果type为ProcessTypeConst.PROCESS_TYPE_CONDITION(3)，且startDigitCondition不为空
     *                  根据startDigitCondition创建多个分支
     *              否则，根据companyId,key,type生成流程编号，查找指定流程定义
     *                  如果找到流程定义，则加载此流程
     *                      加载流程时，srcProcessDefineId无效
     *                  否则，如果未找到流程定义，则创建新流程
     *                      创建流程时，如果指定了srcProcessDefineId时，则复制srcProcessDefineId流程
     *                          复制srcProcessDefineId流程时，不判断流程模板和新流程是否为相同类型
     *                      否则，如果未指定srcProcessDefineId
     *                          创建单分支，一个默认审批节点的流程
     *              调用此接口时，如果创建了一个流程，不会存储流程定义到数据库
     * @return  查找到的或创建的流程定义信息
     **/
    @RequestMapping("/prepareProcessDefine")
    @ResponseBody
    public AjaxMessage prepareProcessDefine(@RequestBody ProcessDetailPrepareDTO prepareRequest) throws Exception {
        updateCurrentUserInfo(prepareRequest);
        ProcessDefineDetailDTO deployment = processService.prepareProcessDefine(prepareRequest);
        return AjaxMessage.succeed("加载成功").setData(deployment);
    }

    /**
     * 描述       查询流程定义
     *              流程定义已使用动态表单代替
     * 日期       2018/8/2
     * @author   张成亮
     * @param    query 流程查询条件
     *              如果未指定accountId，currentCompanyId，则使用当前用户信息
     *              如果未指定key，查询所有流程定义，分组返回列表，组名为中文
     *              否则，如果指定key，仅查询此类型的流程定义，返回列表，
     * @return   流程列表或分组流程列表
     **/
    @RequestMapping("/listProcessDefine")
    @ResponseBody
    @Deprecated
    /** 使用iWork/dynamicForm/listForm代替 **/
    public AjaxMessage listProcessDefine(@RequestBody ProcessDefineQueryDTO query) throws Exception {
        FormQueryDTO formQuery = BeanUtils.createFrom(query,FormQueryDTO.class);
        formQuery.setFormId(query.getKey());
        return dynamicFormController.listForm(formQuery);
    }



    /**
     * 描述     添加审批
     *          由于原有审批管理的查询接口在此类内实现，因此保留一个接口，保持一致性
     *          但根据目前的设计，审批信息使用动态表单表来存储，因此在动态表单服务内进行具体实现，并在动态表单内也提供相同功能的接口
     * 日期     2018/9/14
     * @author  张成亮
     * @return  审批管理流程信息
     * @param   request 流程更改申请
     **/
    @RequestMapping(value = "/addProcess", method = RequestMethod.POST)
    @ResponseBody
    @Deprecated
    /** 使用iWork/dynamicForm/insertDynamicForm代替 **/
    public AjaxMessage addProcess(@RequestBody ProcessDefineEditDTO request) throws Exception {
        return dynamicFormController.insertDynamicForm(request);
    }


    /**
     * 描述       创建或修改一个流程，并保存到数据库
     *           调用此接口时，会存储流程定义到数据库
     * 日期       2018/7/31
     * @author   张成亮
     * @param    editRequest 更改信息
     *              如果未指定accountId，currentCompanyId，则使用当前用户信息
     *              根据companyId,key,type生成流程编号
     *              如果type为ProcessTypeConst.PROCESS_TYPE_CONDITION(3)
     *                  使用taskListMap中defaultFlow之外的分组名，作为条件节点信息
     *                  使用key值作为条件节点变量名
     *              否则，如果type不为ProcessTypeConst.PROCESS_TYPE_CONDITION(3)
     *                  taskListMap中defaultFlow之外的分组无效
     *              存储流程定义到数据库时
     *                 不会判断数据库内是否已经存在同编号流程，如果存在，会生成新版本
     *                 不会根据type值更改组、人员等设置
     *              taskListMap需要包含所有节点信息，无论是否曾被修改
     * @return   保存后的流程定义
     **/
    @RequestMapping("/changeProcessDefine")
    @ResponseBody
    public AjaxMessage changeProcessDefine(@RequestBody ProcessDefineDetailEditDTO editRequest) throws Exception {
        updateCurrentUserInfo(editRequest);
        ProcessDefineDetailDTO deployment = processService.changeProcessDefine(editRequest);
        return AjaxMessage.succeed("修改成功").setData(deployment);
    }

    /**
     * 描述       删除流程
     * 日期       2018/7/31
     * @author   张成亮
     * @param    deleteRequest 更改信息
     *              如果未指定accountId，currentCompanyId，则使用当前用户信息
     *              如果未指定id，则根据currentCompanyId,key生成部分流程编号，只要编号符合这部分的流程都将被删除
     **/
    @RequestMapping("/deleteProcessDefine")
    @ResponseBody
    public AjaxMessage deleteProcessDefine(@RequestBody ProcessDefineQueryDTO deleteRequest) throws Exception {
        updateCurrentUserInfo(deleteRequest);
        processService.deleteProcessDefine(deleteRequest);
        return AjaxMessage.succeed("删除成功");
    }

    /**
     * 描述     查询流程用到的用户
     * 日期     2018/8/3
     * @author   张成亮
     * @param    query 查询条件
     *              如果未指定accountId，currentCompanyId，则使用当前用户信息
     *              如果指定了idList，id无效
     *              如果同时指定了多个条件，各条件之间是“与”的关系
     * @return   符合条件的用户列表
     **/
    @RequestMapping("/listUser")
    @ResponseBody
    public AjaxMessage listUser(@RequestBody UserQueryDTO query) throws Exception {
        updateCurrentUserInfo(query);
        //List<UserDTO> result = workflowService.listUser(query);
        return AjaxMessage.succeed("查询成功").setData(null);
    }

    /**
     * 描述     查询流程用到的群组
     * 日期     2018/8/3
     * @author   张成亮
     * @param    query 查询条件
     *              如果未指定accountId，currentCompanyId，则使用当前用户信息
     *              如果指定了idList，id无效
     *              如果同时指定了多个条件，各条件之间是“与”的关系
     * @return   符合条件的群组列表
     **/
    @RequestMapping("/listGroup")
    @ResponseBody
    public AjaxMessage listGroup(@RequestBody GroupQueryDTO query) throws Exception {
        updateCurrentUserInfo(query);
        List<FlowGroupDTO> result = workflowService.listGroup(query);
        return AjaxMessage.succeed("查询成功").setData(result);
    }

    /**
     * 描述       启动流程，id应设置为流程编号
     * 日期       2018/7/31
     * @author   张成亮
     **/
    @RequestMapping("/startProcess")
    @ResponseBody
    public AjaxMessage startProcess(@RequestBody WorkActionDTO startRequest) throws Exception {
        updateCurrentUserInfo(startRequest);
        WorkTaskDTO workTask = workflowService.startProcess(startRequest);
        return AjaxMessage.succeed("删除成功").setData(workTask);
    }

    /**
     * 描述       完成执行任务，id应设置为执行任务编号
     * 日期       2018/7/31
     * @author   张成亮
     **/
    @RequestMapping("/completeWorkTask")
    @ResponseBody
    public AjaxMessage completeWorkTask(@RequestBody WorkActionDTO completeRequest) throws Exception {
        updateCurrentUserInfo(completeRequest);
        workflowService.completeWorkTask(completeRequest);
        return AjaxMessage.succeed("完成成功");
    }

    @ModelAttribute
    public void before() {
        this.currentUserId = this.getFromSession("userId", String.class);
        this.currentCompanyId = this.getFromSession("companyId", String.class);
        this.currentCompanyUserId = this.getFromSession("companyUserId", String.class);
    }
}
