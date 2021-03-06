package com.maoding.financial.service.impl;

import com.maoding.attach.dto.FileDataDTO;
import com.maoding.commonModule.dto.*;
import com.maoding.commonModule.service.CopyRecordService;
import com.maoding.commonModule.service.RelationRecordService;
import com.maoding.companybill.dto.SaveCompanyBillDTO;
import com.maoding.companybill.service.CompanyBalanceService;
import com.maoding.companybill.service.CompanyBillService;
import com.maoding.core.base.dto.BaseDTO;
import com.maoding.core.base.dto.CorePageDTO;
import com.maoding.core.base.dto.CoreShowDTO;
import com.maoding.core.base.service.GenericService;
import com.maoding.core.bean.AjaxMessage;
import com.maoding.core.bean.ResponseBean;
import com.maoding.core.constant.*;
import com.maoding.core.util.DateUtils;
import com.maoding.core.util.StringUtil;
import com.maoding.core.util.StringUtils;
import com.maoding.dynamicForm.dao.DynamicFormDao;
import com.maoding.dynamicForm.dto.SaveDynamicAuditDTO;
import com.maoding.dynamicForm.entity.DynamicFormEntity;
import com.maoding.enterprise.dto.EnterpriseSearchQueryDTO;
import com.maoding.enterprise.service.EnterpriseService;
import com.maoding.exception.CustomException;
import com.maoding.financial.dao.ExpAuditDao;
import com.maoding.financial.dao.ExpDetailDao;
import com.maoding.financial.dao.ExpMainDao;
import com.maoding.financial.dao.LeaveDetailDao;
import com.maoding.financial.dto.*;
import com.maoding.financial.entity.ExpAuditEntity;
import com.maoding.financial.entity.ExpDetailEntity;
import com.maoding.financial.entity.ExpMainEntity;
import com.maoding.financial.service.ExpMainService;
import com.maoding.message.dto.SendMessageDTO;
import com.maoding.message.entity.MessageEntity;
import com.maoding.message.service.MessageService;
import com.maoding.mytask.service.MyTaskService;
import com.maoding.org.dao.CompanyDao;
import com.maoding.org.dao.CompanyUserDao;
import com.maoding.org.dto.CompanyRelationDTO;
import com.maoding.org.dto.CompanyUserTableDTO;
import com.maoding.org.entity.CompanyUserEntity;
import com.maoding.org.service.CompanyUserService;
import com.maoding.process.dto.ActivitiDTO;
import com.maoding.process.service.ProcessService;
import com.maoding.project.constDefine.EnterpriseServer;
import com.maoding.project.dao.ProjectSkyDriverDao;
import com.maoding.project.dto.NetFileDTO;
import com.maoding.project.dto.ProjectDTO;
import com.maoding.project.service.ProjectService;
import com.maoding.project.service.ProjectSkyDriverService;
import com.maoding.projectcost.dto.ProjectCostDataDTO;
import com.maoding.projectcost.service.ProjectCostService;
import com.maoding.role.service.PermissionService;
import com.maoding.statistic.dto.StatisticDetailQueryDTO;
import com.maoding.statistic.dto.StatisticDetailSummaryDTO;
import com.maoding.user.service.UserAttachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * 深圳市设计同道技术有限公司
 * 类    名 : ExpMainServiceImpl
 * 描    述 : 报销主表ServiceImpl
 * 作    者 : LY
 * 日    期 : 2016/7/26-16:07
 */

@Service("expMainService")
public class ExpMainServiceImpl extends GenericService<ExpMainEntity> implements ExpMainService {


    @Autowired
    private ExpMainDao expMainDao;

    @Autowired
    private ExpDetailDao expDetailDao;

    @Autowired
    private ExpAuditDao expAuditDao;

    @Autowired
    private CompanyUserService companyUserService;

    @Autowired
    private ProjectSkyDriverDao projectSkyDriverDao;

    @Autowired
    private ProjectSkyDriverService projectSkyDriverService;

    @Autowired
    private CompanyUserDao companyUserDao;

    @Autowired
    private MyTaskService myTaskService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CompanyBillService companyBillService;

    @Autowired
    private CompanyDao companyDao;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private CompanyBalanceService companyBalanceService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private RelationRecordService relationRecordService;

    @Autowired
    private CopyRecordService copyRecordService;

    @Autowired
    private UserAttachService userAttachService;

    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private LeaveDetailDao leaveDetailDao;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private EnterpriseServer enterpriseServer;

    @Autowired
    private ProjectCostService projectCostService;

    @Autowired
    private DynamicFormDao dynamicFormDao;

    /**
     * 方法描述：报销增加或者修改
     * 作   者：LY
     * 日   期：2016/7/26 17:35
     */
    public AjaxMessage saveOrUpdateExpMainAndDetail(ExpMainDTO dto, String userId, String companyId) throws Exception {
        AjaxMessage ajaxMessage = this.validateExpMainAndDetail(dto);
        if(ajaxMessage!=null){
            return ajaxMessage;
        }
//        CompanyUserEntity auditPerson = companyUserDao.selectById(dto.getAuditPerson());
//        if(auditPerson==null){
//            return AjaxMessage.failed("数据错误");
//        }
        CompanyUserEntity currentCompanyUser = companyUserDao.getCompanyUserByUserIdAndCompanyId(userId,companyId);
        if(currentCompanyUser==null){
            return AjaxMessage.failed("数据错误");
        }
        //保存收款方信息
        if (!StringUtils.isEmpty(dto.getEnterpriseName()) && StringUtils.isEmpty(dto.getEnterpriseId())){
            EnterpriseSearchQueryDTO query = new EnterpriseSearchQueryDTO();
            query.setCompanyId(companyId);
            query.setName(dto.getEnterpriseName());
            query.setSave(true);
            ResponseBean response = enterpriseService.getRemoteData(enterpriseServer.getQueryFull(),query);
            if (response != null && response.getData() != null) {
                Map<String,Object> data = response.getData();
                Map<String,Object> enterpriseDo = (Map<String,Object>) data.get("enterpriseDO");
                if (enterpriseDo != null) {
                    dto.setEnterpriseId((String)enterpriseDo.get("id"));
                }
            }
        }

        //是增加true还是修改false操作
        boolean flag = false;
        //保存报销主表
        ExpMainEntity entity = new ExpMainEntity();
        BaseDTO.copyFields(dto, entity);
        entity.setApproveStatus("0");
        if (StringUtil.isNullOrEmpty(dto.getId())) {//插入
            saveExpMain(entity,dto,userId,companyId);
            flag= true;
        }  else {//保存
            int result = 0;
            ExpMainEntity exp = expMainDao.selectById(dto.getId());
            if(exp!=null && ("2".equals(exp.getApproveStatus()) || "3".equals(exp.getApproveStatus()))){
                if(!StringUtil.isNullOrEmpty(dto.getTargetId()) && dto.getId().equals(dto.getTargetId())){
                    return AjaxMessage.failed("参数错误");
                }
                //判断是否退回后的编辑
                exp.setExpFlag(1);
                entity.setExpFlag(2);
                expMainDao.updateById(exp);
                //dto.setTargetId(null); //此处为了防止前端 更新的时候传递了targetId过来(前端生成)
                //新开一个新的报销单
                saveExpMain(entity,dto,userId,companyId);
                //复制原来的附件记录
                projectSkyDriverService.copyFileToNewObject(entity.getId(),dto.getId(),NetFileType.EXPENSE_ATTACH,dto.getDeleteAttachList());
                flag = true;
            }else {
                entity.set4Base(null, userId, null, new Date());
                //版本控制
                entity.setExpDate(DateUtils.getDate());
                result = expMainDao.updateById(entity);
                //保存报销明细表
                this.saveExpDetail(dto,entity.getId(),userId,currentCompanyUser.getId());
                if (result == 0) {
                    return new AjaxMessage().setCode("0").setInfo("保存失败").setData(dto);
                }
            }
        }
        //主表Id
        String id = entity.getId();
        //处理图片
        if(!CollectionUtils.isEmpty(dto.getDeleteAttachList())){
            projectSkyDriverService.deleteSysDrive(dto.getDeleteAttachList(),dto.getAccountId(),id);
        }
        //处理审核记录
        Integer myTaskType = this.getMyTaskType(entity);
        //处理抄送
        this.saveCopy(dto.getCcCompanyUserList(),currentCompanyUser.getId(),id,id);
        return new AjaxMessage().setCode("0").setInfo("保存成功").setData(dto);

    }

    //保存报销明细表
    private void saveExpDetail(ExpMainDTO dto,String id,String userId,String currentCompanyUserId) throws Exception{
        //按照MainId先删除原来明细
        expDetailDao.deleteByMainId(id);
        ExpDetailEntity detailEntity = null;
        SaveRelationRecordDTO relation = new SaveRelationRecordDTO();
        relation.setAccountId(userId);
        relation.setTargetId(id);
        int seq = 1;
        for (ExpDetailDTO detailDTO : dto.getDetailList()) {
            detailEntity = new ExpDetailEntity();
            BaseDTO.copyFields(detailDTO, detailEntity);
            detailDTO.setId(StringUtil.buildUUID());
            detailEntity.setId(detailDTO.getId());
            detailEntity.setMainId(id);
            detailEntity.setSeq(seq++);
            if (detailEntity.getExpAllName() != null) {
                String[] allName = detailEntity.getExpAllName().split("-");
                detailEntity.setExpPName(allName[0]);
                detailEntity.setExpName(allName[1]);
            }
            detailEntity.set4Base(userId, userId, new Date(), new Date());
            expDetailDao.insert(detailEntity);

            if(detailDTO.getRelationRecord()!=null && !StringUtil.isNullOrEmpty(detailDTO.getRelationRecord().getRelationId())){
                detailDTO.getRelationRecord().setOperateRecordId(detailEntity.getId());
                relation.getRelationList().add(detailDTO.getRelationRecord());
            }
        }
        //处理关联项
        this.relationRecordService.saveRelationRecord(relation);
    }


    private AjaxMessage validateExpMainAndDetail(ExpMainDTO dto){
        List<ExpDetailDTO> detailList = dto.getDetailList();
        if (CollectionUtils.isEmpty(detailList)) {
            return new AjaxMessage().setCode("1").setInfo("报销明细不能为空");
        }
        if (!StringUtil.isNullOrEmpty(dto.getRemark()) && dto.getRemark().length() > 255) {
            return new AjaxMessage().setCode("1").setInfo("备注长度过长");
        }
        dto.setExpSumAmount(new BigDecimal("0"));
        for (ExpDetailDTO detailDTO : detailList) {
            if (detailDTO.getExpAmount() == null) {
                return new AjaxMessage().setCode("1").setInfo("报销金额不能为空");
            } else if (StringUtil.isNullOrEmpty(detailDTO.getExpType())) {
                return new AjaxMessage().setCode("1").setInfo("报销类别不能为空");
            } else if (StringUtil.isNullOrEmpty(detailDTO.getExpUse())) {
                return new AjaxMessage().setCode("1").setInfo("用途说明不能为空");
            }
            dto.setExpSumAmount(dto.getExpSumAmount().add(detailDTO.getExpAmount()));
        }
        return null;
    }


    @Override
    public AjaxMessage applyProjectCost(ApplyProjectCostDTO dto) throws Exception {
        String userId = dto.getAccountId();
        String companyId = dto.getCurrentCompanyId();
        CompanyUserEntity currentCompanyUser = companyUserDao.getCompanyUserByUserIdAndCompanyId(userId,companyId);
        if(currentCompanyUser==null){
            return AjaxMessage.failed("数据错误");
        }
        //是增加true还是修改false操作
        //保存报销主表
        ExpMainEntity entity = new ExpMainEntity();
        BaseDTO.copyFields(dto, entity);
        entity.setApproveStatus("0");
        entity.setType(ExpenseConst.TYPE_PROJECT_PAY_APPLY);//项目付款申请类型
        if (StringUtil.isNullOrEmpty(dto.getId())) {//插入
            this.saveExpMain(entity,dto,userId,companyId);
        }  else {//保存
            int result = 0;
            ExpMainEntity exp = expMainDao.selectById(dto.getId());
            if(exp!=null && "2".equals(exp.getApproveStatus())){
                if(!StringUtil.isNullOrEmpty(dto.getTargetId()) && dto.getId().equals(dto.getTargetId())){
                    return AjaxMessage.failed("参数错误");
                }
                //判断是否退回后的编辑
                exp.setExpFlag(1);
                entity.setExpFlag(2);
                expMainDao.updateById(exp);
                saveExpMain(entity,dto,userId,companyId);
            }else {
                entity.set4Base(null, userId, null, new Date());
                //版本控制
                entity.setExpDate(DateUtils.getDate());
                result = expMainDao.updateById(entity);
                if (result == 0) {
                    return new AjaxMessage().setCode("0").setInfo("保存失败").setData(dto);
                }
            }
        }
        return new AjaxMessage().setCode("0").setInfo("保存成功").setData(dto);
    }


    private void saveExpDetail(ApplyProjectCostDTO dto,String mainId) throws Exception {
        //按照MainId先删除原来明细
        String userId = dto.getAccountId();
        expDetailDao.deleteByMainId(mainId);
        ExpDetailEntity detailEntity = null;
        detailEntity = new ExpDetailEntity();
        detailEntity.setId(StringUtil.buildUUID());
        detailEntity.setProjectId(dto.getProjectId());
        detailEntity.setMainId(mainId);
        detailEntity.setSeq(1);
        if (dto.getApplyAmount() != null){
            BigDecimal expAmount = dto.getApplyAmount().multiply(new BigDecimal("10000"));//换成元
            detailEntity.setExpAmount(expAmount);
        }
        detailEntity.set4Base(userId, userId, new Date(), new Date());
        expDetailDao.insert(detailEntity);

        //处理关联项
        SaveRelationRecordDTO relation = new SaveRelationRecordDTO();
        relation.setAccountId(userId);
        relation.setTargetId(mainId);
        RelationTypeDTO relationTypeDTO = new RelationTypeDTO();
        relationTypeDTO.setRelationId(dto.getTargetId());
        relationTypeDTO.setOperateRecordId(detailEntity.getId());
        relationTypeDTO.setRecordType(CopyTargetType.PROJECT_COST_POINT_DETAIL);
        relation.getRelationList().add(relationTypeDTO);
        this.relationRecordService.saveRelationRecord(relation);
    }

    private ExpMainEntity saveExpMain(ExpMainEntity entity,ExpMainDTO dto,String userId,String companyId) throws Exception{
        String expNo = this.getExpNo(companyId);
        entity.setExpFlag(0);
        if(StringUtil.isNullOrEmpty(dto.getTargetId())){
            entity.setId(StringUtil.buildUUID());
        }else {
            entity.setId(dto.getTargetId());
        }
        dto.setExpNo(expNo);
        if(StringUtil.isNullOrEmpty(dto.getType())){
            entity.setType(ExpenseConst.TYPE_EXPENSE); //默认为报销费用
        }
        entity.setExpNo(expNo);
        entity.set4Base(userId, userId, new Date(), new Date());
        entity.setCompanyId(companyId);
        expMainDao.insert(entity);
        //保存明细
        this.saveExpDetail(dto,entity.getId(),userId,dto.getCurrentCompanyUserId());
        String targetType = null;
        if(ExpenseConst.TYPE_EXPENSE.equals(entity.getType())){
            targetType = ProcessTypeConst.PROCESS_TYPE_EXPENSE;
        }else {
            targetType= ProcessTypeConst.PROCESS_TYPE_COST_APPLY;
        }
        // 启动流程
        ActivitiDTO activitiDTO = new ActivitiDTO(entity.getId(),entity.getCompanyUserId(),companyId,userId,dto.getExpSumAmount(),targetType);
        activitiDTO.getParam().put("approveUser",dto.getAuditPerson());
        this.processService.startProcessInstance(activitiDTO);
        return entity;
    }

    private ExpMainEntity saveExpMain(ExpMainEntity entity,ApplyProjectCostDTO dto,String userId,String companyId) throws Exception{
        String expNo = this.getExpNo(companyId);
        entity.setExpFlag(0);
        if(StringUtil.isNullOrEmpty(dto.getType())){
            entity.setType(ExpenseConst.TYPE_EXPENSE); //默认为报销费用
        }
        entity.setExpNo(expNo);
        entity.set4Base(userId, userId, new Date(), new Date());
        entity.setCompanyId(companyId);
        entity.initEntity();
        expMainDao.insert(entity);

        String targetType = ProcessTypeConst.PROCESS_TYPE_PROJECT_PAY_APPLY;
        this.saveExpDetail(dto,entity.getId());
//        if(entity.getType()==1){
//            targetType = ProcessTypeConst.PROCESS_TYPE_EXPENSE;
//        }else {
//            targetType= ProcessTypeConst.PROCESS_TYPE_COST_APPLY;
//        }
        // 启动流程
        ActivitiDTO activitiDTO = new ActivitiDTO(entity.getId(),entity.getCompanyUserId(),companyId,userId,dto.getApplyAmount(),targetType);
        activitiDTO.getParam().put("approveUser",dto.getAuditPerson());
        this.processService.startProcessInstance(activitiDTO);
        return entity;
    }


    private String getExpNo(String companyId){
        Map<String, Object> map = new HashMap<>();
        map.put("companyId", companyId);
        String expNo = expMainDao.getMaxExpNo(map);
        String yyMMdd = DateUtils.date2Str(DateUtils.yyyyMMdd);
        if ("1001".equals(expNo)) {
            //自动生成一个
            expNo = yyMMdd + "0001";
        } else if (yyMMdd.equals(expNo.substring(0, expNo.length() - 4))) {

        } else {
            expNo = yyMMdd + "0001";
        }
        return expNo;
    }

    private Integer getMyTaskType(ExpMainEntity entity) {
        if (entity.getType() == ExpenseConst.TYPE_COST_APPLY) {
            return SystemParameters.COST_AUDIT;
        }
        return SystemParameters.EXP_AUDIT;
    }


    /**
     * 方法描述：得到当前公司和当前组织下面人员
     * 作   者：LY
     * 日   期：2016/8/3 17:17
     *
     * @param companyId 公司Id  orgId 组织Id
     */
    public List<CompanyUserTableDTO> getUserList(String companyId, String orgId) throws Exception {
        Map<String, Object> mapParams = new HashMap<String, Object>();
        mapParams.put("companyId", companyId);
        if (!companyId.equals(orgId)) {
            mapParams.put("orgId", orgId);
        }
        return companyUserService.getUserByOrgId(mapParams);
    }


    public int getExpMainPageCount(Map<String, Object> param) {
        return expMainDao.getExpMainPageCount(param);
    }


    /**
     * 方法描述：撤回报销
     * 作   者：LY
     * 日   期：2016/7/29 11:01
     *
     * @param id--报销单id type--状态(3撤回)
     */
    public int recallExpMain(String id, String versionNum, String type) throws Exception {
        ExpMainEntity entity = this.expMainDao.selectById(id);
        entity.setId(id);
        entity.setApproveStatus(type);
        if (versionNum != null) {
            entity.setVersionNum(Integer.parseInt(versionNum));
        }
        ExpAuditEntity auditEntity = new ExpAuditEntity();
        auditEntity.setMainId(id);
        auditEntity.setApproveStatus(type);
        if (versionNum != null) {
            auditEntity.setVersionNum(Integer.parseInt(versionNum));
        }
        expAuditDao.updateByMainId(auditEntity);
        deleteMyTask(id, this.getMyTaskType(entity));
        return expMainDao.updateById(entity);
    }


    private void deleteMyTask(String targetId, Integer taskType) {
        setMyTaskStatus(targetId, taskType, null, true);
    }

    private void setMyTaskStatus(String targetId, Integer taskType, String param2, boolean isDeleted) {
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("taskType", taskType);
        param.put("targetId", targetId);
        param.put("param2", param2);
        if (isDeleted) {
            param.put("param4", "1");
        } else {
            param.put("status", "1");
        }
        myTaskService.updateStatesByTargetId(param);
    }

//    /**
//     * 方法描述：退回报销
//     * 作   者：LY
//     * 日   期：2016/7/29 11:01
//     *
//     * @param dto -- mainId--报销单id  approveStatus--状态(2.退回) auditMessage审批意见
//     */
//    public int recallExpMain(ExpAuditDTO dto) throws Exception {
//        ExpAuditEntity auditEntity = new ExpAuditEntity();
//        auditEntity.setMainId(dto.getMainId());
//        auditEntity.setApproveStatus(dto.getApproveStatus());
//        auditEntity.setAuditMessage(dto.getAuditMessage());
//        expAuditDao.updateByMainId(auditEntity);
//        ExpMainEntity entity = this.expMainDao.selectById(dto.getMainId());
//        entity.setApproveStatus(dto.getApproveStatus());
//        //任务设置状态
//        setMyTaskStatus(dto.getMainId(), this.getMyTaskType(entity), "2", false);
//        //发送消息
//        this.sendMessageForType20(dto.getMainId(),dto.getAppOrgId(), entity.getCompanyUserId(), entity.getType(), dto.getAuditPerson(), "2");//推送拒绝的消息
//        return expMainDao.updateById(entity);
//    }

//    private void sendMessageForType20(String mainId, String companyId, String companyUserId, Integer type, String accountId, String approveStatus) throws Exception {
//        CompanyUserEntity companyUserEntity = this.companyUserDao.selectById(companyUserId);
//        if (companyUserEntity != null) {
//            MessageEntity messageEntity = new MessageEntity();
//            messageEntity.setTargetId(mainId);
//            if (type == 1 && "1".equals(approveStatus)) {
//                messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_22);
//            }
//            if (type == 1 && "2".equals(approveStatus)) {
//                messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_20);
//            }
//            if (type == 1 && "3".equals(approveStatus)) {
//                messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_221);
//            }
//            if ( "1".equals(approveStatus)) {
//                messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_224);
//            }
//            if ( "2".equals(approveStatus)) {
//                messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_223);
//            }
//            if ( "3".equals(approveStatus)) {
//                messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_225);
//            }
//            messageEntity.setCompanyId(companyUserEntity.getCompanyId());
//            messageEntity.setUserId(companyUserEntity.getUserId());
//            messageEntity.setSendCompanyId(companyId);
//            messageEntity.setCreateBy(accountId);
//            this.messageService.sendMessage(messageEntity);
//        }
//    }

    /**
     * 方法描述：报销详情
     * 作   者：LY
     * 日   期：2016/7/29 10:53
     */
    public ExpMainDTO getExpMainPageDetail(String id) throws Exception {
        ExpMainDTO dto = new ExpMainDTO();
        ExpMainEntity entity = expMainDao.selectById(id);

        BaseDTO.copyFields(entity, dto);
        ExpAuditDTO auditDto = expAuditDao.selectAuditPersonByMainId(id);
        dto.setAuditPerson(auditDto.getAuditPerson());
        dto.setAuditPersonName(auditDto.getAuditPersonName());
        List<ExpDetailDTO> list = expDetailDao.selectDetailDTOByMainId(id);
        dto.setDetailList(list);
        Map<String, Object> map = new HashMap<>();
        map.put("targetId", id);
        map.put("type", NetFileType.EXPENSE_ATTACH);
        List<NetFileDTO> expAttachEntityList = projectSkyDriverService.getNetFileByParam(map);
        dto.setExpAttachDTOList(BaseDTO.copyFields(expAttachEntityList, ExpAttachDTO.class));
        return dto;
    }

    /**
     * 方法描述：删除报销
     * 作   者：LY
     * 日   期：2016/7/29 10:53
     */
    public int deleteExpMain(String id, String versionNum) throws Exception {
        int i = recallExpMain(id, versionNum, "4");
        if (i > 0) {
            // 把相关的任务设置为无效
            this.myTaskService.ignoreMyTask(id);
            this.messageService.deleteMessage(id);
        }
        return i;
    }

//    /**
//     * 方法描述：同意报销
//     * 作   者：LY
//     * 日   期：2016/8/1 15:08
//     */
//    public int agreeExpMain(String id, String versionNum, String currentCompanyId, String currentUserId) throws Exception {
//        //处理我的任务
//        ExpMainEntity mainEntity = this.expMainDao.selectById(id);
//        if (mainEntity != null) {
//            Map<String, Object> param = new HashMap<String, Object>();
//            param.put("taskType", SystemParameters.EXP_AUDIT);
//            if (mainEntity.getType() == ExpenseConst.TYPE_COST_APPLY) {
//                param.put("taskType", SystemParameters.COST_AUDIT);
//            }
//            this.setMyTaskStatus(id, null, "1", false);
//            //给报销人推送消息
//            this.sendMessageForType20(id, currentCompanyId, mainEntity.getCompanyUserId(), mainEntity.getType(), currentUserId, "1");
//        }
//        mainEntity.setApproveStatus("1");
//        if (versionNum != null) {
//            mainEntity.setVersionNum(Integer.parseInt(versionNum));
//        }
//        ExpAuditEntity auditEntity = new ExpAuditEntity();
//        auditEntity.setMainId(id);
//        auditEntity.setApproveStatus("1");
//        if (versionNum != null) {
//            auditEntity.setVersionNum(Integer.parseInt(versionNum));
//        }
//        expAuditDao.updateByMainId(auditEntity);
//        return expMainDao.updateById(mainEntity);
//    }

//    /**
//     * 方法描述：同意报销并转移审批人
//     * 作   者：LY
//     * 日   期：2016/8/1 15:08
//     *
//     * @param id--报销单id auditPerson--新审批人  userId用户Id
//     */
//    public int agreeAndTransAuditPerExpMain(String id, String userId, String auditPerson, String versionNum, String currentCompanyId) throws Exception {
//        //版本控制，只为了方便
//        ExpMainEntity entityVersion = this.expMainDao.selectById(id);
//        if (entityVersion == null) {
//            return 0;
//        }
//        entityVersion.setApproveStatus("5");
//        entityVersion.setVersionNum(Integer.parseInt(versionNum));
//        int result = expMainDao.updateById(entityVersion);
//        if (result == 0) {
//            return result;
//        }
//
//        //根据报销单id查询最新审批记录id
//        String parentId = null;
//        ExpAuditEntity auditEntities = expAuditDao.getLastAuditByMainId(id);
//        if (auditEntities!=null) {
//            parentId = auditEntities.getId();
//        }
//
//        //把当前自己的审批记录改为同意并且is_new为N
//        ExpAuditEntity auditEntity = new ExpAuditEntity();
//        auditEntity.setMainId(id);
//        auditEntity.setApproveStatus("1");
//        expAuditDao.transAuditPer(auditEntity);
//
//        //插入新审批人审批记录
//        auditEntity = new ExpAuditEntity();
//        auditEntity.setId(StringUtil.buildUUID());
//        auditEntity.setIsNew("Y");
//        auditEntity.setMainId(id);
//        auditEntity.setParentId(parentId);
//        auditEntity.setApproveStatus("5");
//        auditEntity.setAuditPerson(auditPerson);
//        auditEntity.set4Base(userId, userId, new Date(), new Date());
//        expAuditDao.insert(auditEntity);
//
//        //推送消息
//        this.sendMessageForType20(id, currentCompanyId, auditPerson, entityVersion.getType(), userId, "3");//同意并转交
//        return result;
//    }

    /**
     * 财务拨款
     */
    @Override
    public int financialAllocation(String id, String currentCompanyUserId,String currentCompanyId, String accountId, Date d) throws Exception{

        //版本控制，只为了方便
        ExpMainEntity entity = this.expMainDao.selectById(id);
        if(entity == null){
            return 0;
        }
        //判断最低余额
        String amount = this.expMainDao.getTotalAmountByMainId(id);
        if(!companyBalanceService.isCanBeAllocate(entity.getCompanyId(),amount,DateUtils.formatDate(d==null?DateUtils.getDate():d))){
            //抛异常
            throw new CustomException("当前支出的金额不能大于账目余额与最低余额的差值");
        }
        entity.setApproveStatus("6");
        entity.setAllocationDate(new Date());
        entity.setAllocationUserId(currentCompanyUserId);
        entity.setUpdateBy(accountId);
        if (d != null) {
            entity.setAllocationDate(d);
        }
        int i= expMainDao.updateById(entity);
        //财务拨款
        if(i>0){
            i = this.financialAccount(entity);
        }
        if(i>0) {
            //推送抄送消息
            SendMessageDTO m = new SendMessageDTO();
            m.setTargetId(id);
            m.setCurrentCompanyId(currentCompanyId);
            m.setAccountId(accountId);
            int type = Integer.parseInt(entity.getType());
            if (type == 1) {//
                m.setMessageType(SystemParameters.MESSAGE_TYPE_236);
            }
            if (type == 2) {//提交
                m.setMessageType(SystemParameters.MESSAGE_TYPE_237);
            }
            //报销类别：1=报销申请，2=费用申请,3请假，4出差,5=项目费用申请,其他为自定义审批表
            if(type!=1 && type!=2 && type!=3 && type!=4 && type!=5){
                m.setMessageType(SystemParameters.MESSAGE_TYPE_253);
            }
            this.messageService.sendMessageForCopy(m);
        }
        return i;
    }

    @Override
    public int financialAllocation(String id, String currentCompanyUserId,String currentCompanyId, String accountId) throws Exception{
        return financialAllocation(id,currentCompanyUserId,currentCompanyId,accountId,null);
    }

    @Override
    public int financialRecallExpMain2(FinancialAllocationDTO dto) throws Exception {
        String userId = dto.getAccountId();
        //查询当前用户bean  company_user表
        CompanyUserEntity currentUser = this.companyUserDao.getCompanyUserByUserIdAndCompanyId(dto.getAccountId(),dto.getCurrentCompanyId());
        if(currentUser==null){
            return 0;
        }
        //ExpMainEntityMapper.xml  maoding_web_exp_main表
        ExpMainEntity main = this.expMainDao.selectById(dto.getId());
        if(main==null){
            return 0;
        }

        //获取要推送消息的人员的id（companyUserId）
        List<String> receiveMessageUserId = new ArrayList<>();
        receiveMessageUserId.add(main.getCompanyUserId());
        //1.找到要推送消息的人
       List<ExpAuditEntity> auditList =  expAuditDao.selectByMainId(dto.getId());
        for (ExpAuditEntity audit: auditList) {
            if (!receiveMessageUserId.contains(audit.getAuditPerson())){
                receiveMessageUserId.add(audit.getAuditPerson());
            }
        }


        //2.添加财务拒绝拨款的记录,保存报销审核表
        ExpAuditEntity auditEntity = new ExpAuditEntity();
        auditEntity.setId(StringUtil.buildUUID());
        auditEntity.setMainId(dto.getId());
        auditEntity.setAuditPerson(currentUser.getId());
        auditEntity.set4Base(userId, userId, new Date(), new Date());
        auditEntity.setIsNew("N");
        auditEntity.setApproveStatus("2");
        auditEntity.setAuditMessage(dto.getReason());
        auditEntity.setApproveDate(DateUtils.getDate());
        this.expAuditDao.insert(auditEntity);

        String mainId = dto.getId();//报销单ID
        String companyId = dto.getCurrentCompanyId();//公司ID

        for (String companyUserId : receiveMessageUserId){
            sendMessageForAudit(mainId,companyId,companyUserId,main.getType(),userId,null,"8");//报销单ID，公司ID，接受消息者ID，类型（报销or费用），当前用户ID，审核表ID，类型（处理类型）

        }
        main.setApproveStatus("7");//审批状态(0:待审核，1:同意，2，退回,3:撤回,4:删除,5.审批中）,6:财务已拨款',7：财务拒绝拨款

        return this.expMainDao.updateById(main);
    }
    @Override
    public int financialRecallExpMain(FinancialAllocationDTO dto) throws Exception {
        String userId = dto.getAccountId();
        CompanyUserEntity currentUser = this.companyUserDao.getCompanyUserByUserIdAndCompanyId(dto.getAccountId(),dto.getCurrentCompanyId());
        if(currentUser==null){
            return 0;
        }
        ExpMainEntity main = this.expMainDao.selectById(dto.getId());
        if(main==null){
            return 0;
        }

        //获取最新的那条记录
        ExpAuditEntity lastAudit = this.expAuditDao.getLastAuditByMainId(dto.getId());
        //首先增加一条审批记录
        //保存报销审核表
        ExpAuditEntity auditEntity = new ExpAuditEntity();
        auditEntity.setId(StringUtil.buildUUID());
        auditEntity.setMainId(dto.getId());
        auditEntity.setApproveStatus("0");
        auditEntity.setAuditPerson(currentUser.getId());
        auditEntity.set4Base(userId, userId, new Date(), new Date());
        auditEntity.setIsNew("N");
        auditEntity.setApproveStatus("2");
        auditEntity.setAuditMessage(dto.getReason());
        auditEntity.setApproveDate(DateUtils.getDate());
        this.expAuditDao.insert(auditEntity);
        //再增加一条给上一个审批人的记录
        if(lastAudit!=null){
            //把原有的记录给设置N
            lastAudit.setIsNew("N");
            this.expAuditDao.updateById(lastAudit);
            lastAudit.initEntity();//重新插入一条新的记录
            lastAudit.setIsNew("Y");
            lastAudit.setApproveStatus("0");
            lastAudit.setCreateDate(DateUtils.getDate(DateUtils.getDate().getTime()+100));
            this.expAuditDao.insert(lastAudit);
            //激活原有的任务
            Map<String,Object> map = new HashMap<>();
            map.put("targetId",dto.getId());
            map.put("status","0");
            map.put("ignoreStatus","1");
            map.put("handlerId",lastAudit.getAuditPerson());
            map.put("myTaskType",this.getMyTaskType(main));
            this.myTaskService.updateStatesByTargetId(map);
        }
        main.setApproveStatus("5");
        return this.expMainDao.updateById(main);
    }

    private int financialAccount(ExpMainEntity entity) throws Exception{
        SaveCompanyBillDTO billDTO = new SaveCompanyBillDTO();
        billDTO.setCompanyId(entity.getCompanyId());
        billDTO.setFromCompanyId(entity.getCompanyId());
        if(entity.getType()==ExpenseConst.TYPE_EXPENSE){
            billDTO.setFeeType(CompanyBillType.FEE_TYPE_EXPENSE);
        }else {
            billDTO.setFeeType(CompanyBillType.FEE_TYPE_EXP_APPLY);
        }
        billDTO.setPayType(CompanyBillType.DIRECTION_PAYER);
        billDTO.setOperatorId(entity.getAllocationUserId());
        billDTO.setPaymentDate(DateUtils.formatDate(entity.getAllocationDate()));
        billDTO.setTargetId(entity.getId());
        return companyBillService.saveCompanyBill(billDTO);

        //通知抄送人
    }

    /**
     * 方法描述：报销详情与审批记录
     * 作   者：LY
     * 日   期：2016/8/2 14:13
     */
    public Map<String, Object> getExpMainDetail(String id) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        List<ExpDetailDTO> detailList = expDetailDao.selectDetailDTOByMainId(id);
        map.put("detailList", detailList);
        List<ExpMainDTO> auditList = new ArrayList<>();
        ExpMainDTO expMainDTO = expMainDao.selectByIdWithUserName(id);//guo
        expMainDTO.setApproveStatusName("发起申请");
        auditList.add(expMainDTO);
        List<ExpMainDTO> list = expAuditDao.selectAuditDetailByMainId(id);
        for (ExpMainDTO dto : list) {
            if (!StringUtil.isNullOrEmpty(expMainDTO.getAllocationDate())) {
                dto.setIsNew("N");
            }
            dto.setApproveStatusName(getApproveStatusName(dto.getApproveStatus(), dto.getIsNew()));
        }
        //报销拨款
        if (!StringUtil.isNullOrEmpty(expMainDTO.getAllocationDate())) {
            ExpMainDTO allocation = new ExpMainDTO();
            allocation.setId(expMainDTO.getId());
            allocation.setApproveStatusName("财务拨款");
            allocation.setUserName(expMainDTO.getAllocationUserName());
            allocation.setExpDate(expMainDTO.getAllocationDate());
            allocation.setCompanyName(companyDao.getCompanyName(expMainDTO.getAllocationUserCompanyId()));
            allocation.setIsNew("Y");
            list.add(allocation);
        }

        Map<String, Object> param = new HashMap<>();
        param.put("targetId", id);
        param.put("type", NetFileType.EXPENSE_ATTACH);
        List<NetFileDTO> expAttachEntityList = projectSkyDriverService.getNetFileByParam(param);
        map.put("expAttachEntityList", BaseDTO.copyFields(expAttachEntityList, ExpAttachDTO.class));
        auditList.addAll(list);
        map.put("auditList", auditList);
        map.put("expNo", expMainDTO.getExpNo());
        map.put("versionNum", expMainDTO.getVersionNum());
        map.put("totalExpAmount", detailList.get(0).getTotalExpAmount());
        return map;
    }

    /**
     * 方法描述：状态码得到状态名称
     * 作   者：LY
     * 日   期：2016/8/2 15:42
     *
     * @param approveStatus 状态码
     * @return 审批状态(0:待审核，1:同意，2，退回, 3:撤回, 4:删除, 5.审批中）
     */
    private String getApproveStatusName(String approveStatus, String isNew) {
        if ("0".equals(approveStatus)) {
            return "待审核";
        } else if ("1".equals(approveStatus)) {
            if ("Y".equals(isNew)) {
                return "已同意(完成)";
            } else {
                return "已同意";
            }
        } else if ("2".equals(approveStatus)) {
            return "退回";
        } else if ("3".equals(approveStatus)) {
            return "撤回";
        } else if ("4".equals(approveStatus)) {
            return "删除";
        } else if ("5".equals(approveStatus)) {
            return "审批中";
        }
        return null;
    }


    /**
     * 方法描述：报销汇总数量
     * 作   者：LY
     * 日   期：2016/7/28 16:34
     *
     * @param param 查询条件
     */
    public int getExpMainPageForSummaryCount(Map<String, Object> param) {
        return expMainDao.getExpMainPageForSummaryCount(param);
    }


    //====================================新接口2.0=====================================================

    /**
     * 方法描述：我的报销列表
     * 作   者：LY
     * 日   期：2016/7/28 16:34
     *
     * @param param 查询条件
     */
    @Override
    public List<ExpMainDTO> getExpMainListPage(Map<String, Object> param) {
        if (null != param.get("pageIndex")) {
            int page = (Integer) param.get("pageIndex");
            int pageSize = (Integer) param.get("pageSize");
            param.put("startPage", page * pageSize);
            param.put("endPage", pageSize);
        }
        String approveStatus = (String) param.get("approveStatus");
        if ("1".equals(approveStatus)) {//此处查询已完成的和已拨款的
            param.remove("approveStatus");
            param.put("defaultApproveStatus", "('1','6')");
        }

        List<ExpMainDTO> list = expMainDao.getExpMainPage(param);
        return list;
    }

    @Override
    public int getExpMainListPageCount(Map<String, Object> param) {
        return expMainDao.getExpMainPageCount(param);
    }


    /**
     * 方法描述：报销汇总List
     * 作   者：LY
     * 日   期：2016/7/28 16:34
     *
     * @param param 查询条件
     */
    @Override
    public List<ExpMainDTO> getExpMainListPageForSummary(Map<String, Object> param) {
        if (null != param.get("pageIndex")) {
            int page = (Integer) param.get("pageIndex");
            int pageSize = (Integer) param.get("pageSize");
            param.put("startPage", page * pageSize);
            param.put("endPage", pageSize);
        }

        List<ExpMainDTO> list = expMainDao.getExpMainPageForSummary(param);
        boolean isFinance = this.permissionService.isFinancial((String)param.get("currentCompanyId"),(String)param.get("accountId"));
        for(ExpMainDTO dto:list){
            if(isFinance && "1".equals(dto.getApproveStatus())){
                dto.getRole().setFinancialAllocation(1);
                dto.getRole().setFinancialRecall(1);
            }
        }
        return list;
    }

    /**
     * 方法描述：报销汇总数量
     * 作   者：LY
     * 日   期：2016/7/28 16:34
     *
     * @param param 查询条件
     */
    @Override
    public int getExpMainListPageForSummaryCount(Map<String, Object> param) {
        return expMainDao.getExpMainPageForSummaryCount(param);
    }

    /**
     * 方法描述：报销汇总金额
     * 作   者：ZCL
     * 日   期：2016/7/28 16:34
     *
     * @param param 查询条件
     */
    @Override
    public ExpSummaryDTO getExpMainSummary(Map<String, Object> param) {
        ExpSummaryDTO summary = expMainDao.getExpMainSummary(param);
        List<CompanyRelationDTO> filterCompanyList = expMainDao.listExpFilterCompany(param);
        if (summary != null) {
            summary.setCompanyList(filterCompanyList);
        }
        return summary;
    }

    /**
     * 方法描述：获取最大组织expNo + 1
     * 作   者：ZhujieChen
     * 日   期：2016/12/22
     */
    public Map<String, Object> getMaxExpNo(Map<String, Object> param) throws Exception {

        boolean flag = true;
        while (flag) {
            int num = new Random().nextInt(999) + 1 + 1000;
            String expNo = DateUtils.date2Str(DateUtils.yyyymmddhhmmss);
            expNo = expNo + num;
            param.put("expNo", expNo);
            List<ExpMainEntity> list = expMainDao.selectByParam(param);
            if (list.size() > 0) {
                flag = true;
            } else {
                flag = false;
            }
        }
        param.put("maxExpNo", param.get("expNo"));
        return param;
    }

    /**
     * 方法描述：项目收支明细-台账模块获取费用申请，报销申请
     * 作   者：DongLiu
     * 日   期：2017/12/6 18:36
     */
    @Override
    public StatisticDetailSummaryDTO getExpenditureCount(StatisticDetailQueryDTO param) {
        return expMainDao.getExpenditureCount(param);
    }

    @Override
    public List<LeaveDetailDTO> getLeaveDetailList(LeaveDetailQueryDTO queryDTO) {
        return expMainDao.getLeaveDetailList(queryDTO);
    }

    @Override
    public Integer getLeaveDetailCount(LeaveDetailQueryDTO queryDTO) {
        return expMainDao.getLeaveDetailCount(queryDTO);
    }

    /**
     * 请假任务详情
     */
    @Override
    public Map<String, Object> getLeaveDetail(LeaveDetailQueryDTO queryDTO) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<ExpMainDTO> expMainDTOS = new ArrayList<ExpMainDTO>();
        ExpMainDTO expMainDTO = new ExpMainDTO();
        expMainDTO = expMainDao.selectByIdWithUserName(queryDTO.getId());
        expMainDTO.setApproveStatusName("发起申请");
        expMainDTOS.add(expMainDTO);
        List<ExpAuditEntity> expAuditEntities = expAuditDao.selectDetailByMainId(queryDTO.getId());
        for (ExpAuditEntity entity : expAuditEntities) {
            expMainDTO = new ExpMainDTO();
            if ("N".equals(entity.getIsNew())) {
                expMainDTO.setApproveStatusName("审核通过");
            } else if ("Y".equals(entity.getIsNew())) {
                expMainDTO.setApproveStatusName("审核完成");
            }
            expMainDTO.setUserName(entity.getAuditPerson());
            expMainDTO.setExpDate(entity.getApproveDate());
            expMainDTOS.add(expMainDTO);
        }
        map.put("expAuditEntities", expMainDTOS);
        map.put("leaveDetail",getLeaveDetail(queryDTO.getId()));

        return map;
    }


    private LeaveDTO getLeaveDetail(String id) throws Exception {
        LeaveDTO result = leaveDetailDao.getLeaveById(id);
        if(result==null){
            return new LeaveDTO();
        }
        Map<String,Object> param = new HashMap<>();
        param.put("id",id);
        List<AuditDTO> auditList = expAuditDao.selectAuditByMainId(param);
        result.setAuditList(auditList);
        if(!CollectionUtils.isEmpty(auditList)){
            if("2".equals(result.getApproveStatus())){//如果是退回，把退回原因提取到最外层，以便前端展示
                result.setCallbackReason(auditList.get(auditList.size()-1).getAuditMessage());
            }
            //获取审核人
            AuditDTO audit = auditList.get(0);
            result.setAuditPersonName(audit.getUserName());
            result.setAuditPerson(audit.getCompanyUserId());

            //copy一份最后审批人到到你跟前审批人上
            AuditDTO currentAuditPerson = new AuditDTO();
            BaseDTO.copyFields(auditList.get(auditList.size()-1),currentAuditPerson);
            result.setCurrentAuditPerson(currentAuditPerson);
        }
        //发起申请的记录
        AuditDTO applyDTO = new AuditDTO();
        applyDTO.setUserName(result.getUserName());
        applyDTO.setCompanyUserId(result.getCompanyUserId());
        applyDTO.setApproveDate(result.getExpDate());
        if("3".equals(result.getApproveStatus())){
            applyDTO.setApproveStatus(result.getApproveStatus());
        }else {
            applyDTO.setApproveStatus(null);
        }
        applyDTO.setApproveStatusName("发起申请");
        applyDTO.setFileFullPath(userAttachService.getHeadImgNotFullPath(result.getAccountId()));
        auditList.add(0,applyDTO);

        param.clear();
        param.put("targetId", id);
        param.put("type", NetFileType.EXPENSE_ATTACH);
        List<FileDataDTO> expAttachList = this.projectSkyDriverService.getAttachDataList(param);
        result.setAttachList(expAttachList);

        //获取抄送人
        result.setCcCompanyUserList(copyRecordService.getCopyRecode(new QueryCopyRecordDTO(id)));
        //返回流程标识，给前端控制是否要给审批人，以及按钮显示的控制
        Map<String,Object> processData = processService.getCurrentTaskUser(new AuditEditDTO(id,null,null),auditList,result.getLeaveTime());
        result.setProcessFlag(processData.get("processFlag"));
        result.setProcessType(processData.get("processType"));
        result.setConditionList(processData.get("conditionList"));

        return result;
    }


    @Override
    public void saveCopy(List<String> ccCompanyUserList,String sendCompanyUserId,String targetId,String operateRecordId) throws Exception{
        //保存抄送人
        SaveCopyRecordDTO copyDTO = new SaveCopyRecordDTO();
        copyDTO.setCompanyUserList(ccCompanyUserList);
        //当前人为发送人，在数据库中的字段为sendCompanyUserId
        copyDTO.setSendCompanyUserId(sendCompanyUserId);
        copyDTO.setOperateRecordId(operateRecordId);
        copyDTO.setTargetId(targetId);
        if(targetId.equals(operateRecordId)){
            copyDTO.setRecordType(CopyTargetType.EXP_MAIN);
        }else {
            copyDTO.setRecordType(CopyTargetType.EXP_AUDIT);
        }
        copyDTO.setRecordType(CopyTargetType.EXP_AUDIT);
        this.copyRecordService.saveCopyRecode(copyDTO);

    }

    @Override
    public Map<String, Object> getAuditInfoByRelationId(String relationId,String companyUserId) throws Exception {
        ExpMainDTO expMain = this.expMainDao.getExpMainByRelationId(relationId);
        List<AuditDTO> list = null;
        Map<String, Object> result = new HashMap<>();
        if(expMain!=null){
            list =  getAuditList(expMain.getId(),null);
            // result.putAll(processService.getCurrentTaskUser(new AuditEditDTO(expMain.getId(),null,null),list,(expMain.getExpSumAmount()).toString()));
            if(("0".equals(expMain.getApproveStatus()) || "5".equals(expMain.getApproveStatus()))
                    && !CollectionUtils.isEmpty(list)){
                AuditDTO auditDTO = list.get(list.size()-1);
                if(companyUserId!=null && companyUserId.equals(auditDTO.getCompanyUserId())){
                    result.put("auditFlag","1");//处于审批的状态
                    result.put("mainId",expMain.getId());
                }
            }
            processService.getCurrentTaskUser(new AuditEditDTO(expMain.getId(),null,null),list,(expMain.getExpSumAmount()).toString());
            result.put("approveStatus",expMain.getApproveStatus());
        }
        result.put("auditList",list);
        return result;
    }


    public List<AuditDTO> getAuditList(String id, ExpMainEntity expMainEntity) throws Exception {
        Map<String,Object> param = new HashMap<>();
        param.put("id",id);
        List<AuditDTO> auditList = expAuditDao.selectAuditByMainId(param);
//        for (AuditDTO dto : auditList) {
//            dto.setApproveStatusName(getApproveStatusName(dto.getApproveStatus(), dto.getIsNew()));
//        }
        ExpMainDataDTO expMainDTO = expMainDao.selectByIdWithUserNameMap(param);
        AuditDTO applyDTO = new AuditDTO();
        BaseDTO.copyFields(expMainDTO,applyDTO);
        if("3".equals(expMainDTO.getApproveStatus())){
            applyDTO.setApproveStatus(expMainDTO.getApproveStatus());
        }else {
            applyDTO.setApproveStatus(null);
        }
        applyDTO.setApproveStatusName("发起申请");
        applyDTO.setFileFullPath(userAttachService.getHeadImgNotFullPath(expMainDTO.getAccountId()));
        applyDTO.setApproveDate(expMainDTO.getExpDate());
        auditList.add(0,applyDTO);
        if(expMainEntity!=null){//费用申请，不需要一下内容
            //报销拨款
            if (!StringUtil.isNullOrEmpty(expMainEntity.getAllocationDate())) {
                ExpMainDataDTO expAllocationDataDTO = expMainDao.selectAllocationUser(param);
                AuditDTO  expAllocationDTO = new AuditDTO();
                BaseDTO.copyFields(expAllocationDataDTO,expAllocationDTO);
                expAllocationDTO.setId(expMainDTO.getId());
                expAllocationDTO.setApproveStatus("6");//财务已拨款
                // expAllocationDTO.setApproveStatusName(getApproveStatusName(expAllocationDTO.getApproveStatus(), "Y"));
                expAllocationDTO.setIsNew("Y");
                expAllocationDTO.setApproveDate(expMainEntity.getAllocationDate());
                //头像
                CompanyUserEntity user = companyUserDao.selectById(expMainEntity.getAllocationUserId());
                if(user!=null){
                    expAllocationDTO.setFileFullPath(userAttachService.getHeadImgNotFullPath(user.getUserId()));
                }
                auditList.add(expAllocationDTO);
            }else if("1".equals(expMainEntity.getApproveStatus())){
                AuditDTO expAllocationDTO = new AuditDTO();
                expAllocationDTO.setId(expMainEntity.getId());
                expAllocationDTO.setApproveStatus("7");//等待财务拨款;
                // expAllocationDTO.setApproveStatusName("等待财务拨款");
                auditList.add(expAllocationDTO);
            }
        }
        return auditList;
    }

    public void sendMessageForAudit(String mainId, String companyId, String companyUserId,String type,String accountId,String auditId,String approveStatus) throws Exception {
        CompanyUserEntity companyUserEntity = this.companyUserDao.selectById(companyUserId);
        if (companyUserEntity != null) {
            MessageEntity messageEntity = new MessageEntity();
            messageEntity.setTargetId(mainId);
            //报销
            if(ExpenseConst.TYPE_EXPENSE.equals(type)){
                if( "0".equals(approveStatus)){//提交
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_19);
                }
                if( "1".equals(approveStatus)){//同意
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_22);
                }
                if( "2".equals(approveStatus)){//拒绝
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_20);
                }
                if( "3".equals(approveStatus)){//同意并转交
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_221);
                }
                if( "6".equals(approveStatus)){//拨款
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_234);
                }
                //抄送
                if( "7".equals(approveStatus)){//
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_236);
                }
                //财务审批不通过（报销）
                if( "8".equals(approveStatus)){
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_247);
                }
            }else if(ExpenseConst.TYPE_COST_APPLY.equals(type)) {
                //费用
                if ( "0".equals(approveStatus)) {//提交
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_222);
                }
                if ( "1".equals(approveStatus)) {//同意
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_224);
                }
                if ( "2".equals(approveStatus)) {//拒绝
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_223);
                }
                if ( "3".equals(approveStatus)) {
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_225);
                }
                if ( "6".equals(approveStatus)) {
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_235);//拨款
                }
                if ( "7".equals(approveStatus)) {//提交
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_237);
                }
                //财务审批不通过（费用）
                if( "8".equals(approveStatus)){
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_248);
                }
            }else if(ExpenseConst.TYPE_LEAVE.equals(type)) {
                //请假
                if ( "0".equals(approveStatus)) {//提交
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_226);
                }
                if ( "1".equals(approveStatus)) {//同意
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_228);
                }
                if ( "2".equals(approveStatus)) {//拒绝
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_227);
                }
                if ( "3".equals(approveStatus)) {//同意并转交
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_229);
                }
                if ( "7".equals(approveStatus)) {//提交
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_238);
                }
            }else if (ExpenseConst.TYPE_ON_BUSINESS.equals(type)){
                //出差
                if( "0".equals(approveStatus)){//提交
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_230);
                }
                if( "1".equals(approveStatus)){//同意
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_232);
                }
                if( "2".equals(approveStatus)){//拒绝
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_231);
                }
                if( "3".equals(approveStatus)){//同意并转交
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_233);
                }
                if( "7".equals(approveStatus)){//提交
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_239);
                }
            }else if (ExpenseConst.TYPE_PROJECT_PAY_APPLY.equals(type)) {
                //付款申请
                if ( "0".equals(approveStatus)) {//提交
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_243);
                }
                if ( "1".equals(approveStatus)) {//同意
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_244);
                }
                if ( "2".equals(approveStatus)) {//拒绝
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_245);
                }
                if ( "3".equals(approveStatus)) {//同意并转交
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_246);
                }
            }else{
            //其他申请（自定义申请）
                if ( "0".equals(approveStatus)) {//提交
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_249);
                }
                if ( "2".equals(approveStatus)) {//拒绝
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_250);
                }
                if ( "1".equals(approveStatus)) {//同意
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_251);
                }
                if ( "3".equals(approveStatus)) {//同意并转交
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_252);
                }
                if ( "7".equals(approveStatus)) {//财务已拨款(抄送)
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_253);
                }
                if ( "5".equals(approveStatus)) {//财务审批未通过
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_254);
                }
                if ( "6".equals(approveStatus)) {//财务已拨款
                    messageEntity.setMessageType(SystemParameters.MESSAGE_TYPE_255);
                }
            }

            //把最新的审批记录的id关联上
            messageEntity.setParam1(auditId);
            messageEntity.setCompanyId(companyUserEntity.getCompanyId());
            messageEntity.setUserId(companyUserEntity.getUserId());
            messageEntity.setSendCompanyId(companyId);
            messageEntity.setCreateBy(accountId);
            this.messageService.sendMessage(messageEntity);
        }
    }

    /**
     * 作者：FYT
     * 日期：2018/9/4
     * @param dto
     * @return
     * @throws Exception
     */
    @Override
    public CorePageDTO<AuditCommonDTO> getAuditDataForWeb(QueryAuditDTO dto) throws Exception {
        CorePageDTO<AuditCommonDTO> page = new CorePageDTO<>();
        List<AuditCommonDTO> list = new ArrayList<>();
        setQuery(dto);
        list = expMainDao.getAuditDataForWeb(dto);
        int count = expMainDao.getAuditDataForWebCount(dto);
        page.setData(list);
        page.setPageIndex(dto.getPageIndex());
        page.setPageSize(dto.getPageSize());
        page.setTotal(count);
        return page;
    }

    /**
     * 方法描述：根据companyId查询所有有效项目(我要报销 选择项目下拉框 )app
     * 作   者：LY
     * 日   期：2016/7/27 17:39
     */
    @Override
    public List<ProjectDTO> getProjectListWS(String companyId, String accountId) {
        return this.getProjectListWS(companyId,accountId,"0");
    }

    @Override
    public List<ProjectDTO> getProjectListWS(String companyId, String userId, String type) {
        CompanyUserEntity userEntity = companyUserDao.getCompanyUserByUserIdAndCompanyId(userId,companyId);
        if(userEntity==null){
            return new ArrayList<>();
        }
        ProjectDTO query = new ProjectDTO();
        query.setCompanyId(companyId);
        query.setCurrentCompanyUserId(userEntity.getId());
        if("1".equals(type)){
            query.setType(type);
        }
        return projectService.getProjectListByCompanyId(query);
    }

    @Override
    public List<AuditDataDTO> getPassAuditData(QueryAuditDTO query) throws Exception {
        query.setCompanyUserId(query.getCurrentCompanyUserId());
        //忽略 退回的记录
        query.setType("4");
        query.setExpType("4");
//       return expMainDao.getAuditDataForWeb(query);
       return getAuditDataDTO(query);
    }


    /**
     * type: (type=1:我提交的，type=2：待审核的,type=3:我已经审核的 type=4:我提交并审批完成的,type=5:我提交的正处于审核中的,type=6:我提交未审核的,7:抄送列表，8：我提交并已退回，9：我提交并已撤销)
     *
     * expTypes（1：报销数据，2：费用申请数据，3：请假数据，4：出差数据，5：项目费用申请数据），如果是查询多个：比如报销+费用申请，传递的参数为 "1,2",用逗号隔开
     */
    @Override
    public List<AuditDataDTO> getAuditDataDTO(QueryAuditDTO query) throws Exception {
        String type = query.getType();
        query.setCompanyId(query.getCurrentCompanyId());
        query.setIgnoreRecall("1");//type=4的，也不包含已经退回的
        CompanyUserEntity user = this.companyUserDao.getCompanyUserByUserIdAndCompanyId(query.getAccountId(),query.getAppOrgId());
        if(user==null){
            return new ArrayList<>();
        }

        if(isMySubmitAudit(type)){
            query.setCompanyUserId(user.getId());
        }
        if ("2".equals(type) || "3".equals(type)){
            query.setAuditPerson(user.getId());
        }
        if ("7".equals(type)){//抄送
            query.setCcCompanyUserId(user.getId());
        }
        List<AuditDataDTO> list = expMainDao.getAuditData(query);
        if (isMySubmitAudit(type)){
            for(AuditDataDTO dto:list){
                if(1==dto.getType() || 2==dto.getType()){
                    List<ExpDetailEntity> detailList = expDetailDao.selectByMainId(dto.getId());
                    dto.setCostList(BaseDTO.copyFields(detailList,CostDetailDTO.class));
                }
            }
        }
        for(AuditDataDTO audit:list){
            if(audit.getType()==5){
                ProjectCostDataDTO cost = projectCostService.getProjectCostByMainId(audit.getId());
                if(cost!=null){
                    audit.setToCompanyName(cost.getRelationCompanyName());
                    audit.setProjectName(cost.getProjectName());
                    audit.setProjectFeeTypeName(cost.getTypeName());
                    audit.setAmount(cost.getDetailFee());
                }
            }
        }
        return list;
    }


    private boolean isMySubmitAudit(String  type){
        if(StringUtil.isNullOrEmpty(type) || "1".equals(type)  || "4".equals(type)
                || "5".equals(type) || "6".equals(type) || "8".equals(type) || "9".equals(type)){
            return true;
        }
        return false;
    }


    public Map<String, Object> getAuditDetailForExp(QueryAuditDTO query) throws Exception {
        Map<String, Object> result = this.getAuditDetailForExp2(query.getId());
        if(result!=null && result.containsKey("auditList")){
            List<AuditDTO> auditList = (List<AuditDTO>)result.get("auditList");
            for(AuditDTO audit:auditList){
                if(query.getAppOrgId().equals(audit.getCompanyId())){
                    audit.setCompanyName("");
                }
            }
        }
        if(result!=null && result.containsKey("exp")){
            Map<String,String> exp = ((Map<String,String>)result.get("exp"));
            if(exp!=null){
                if(exp.containsKey("auditCompanyId") && query.getAppOrgId().equals(exp.get("auditCompanyId"))){
                    exp.put("auditCompanyName","");
                }
            }
        }
        return result;
    }


    public Map<String,Object> getAuditDetailForExp2(String id) throws Exception{
        CompanyUserEntity companyUserEntity = null;
        Map<String, Object> map = new HashMap<>();
        ExpMainEntity expMainEntity = expMainDao.selectById(id);
        if (null == expMainEntity) {
            return map;
        }
        Map<String, String> exp = new HashMap<>();
        companyUserEntity = companyUserDao.selectById(expMainEntity.getCompanyUserId());
        if(companyUserEntity!=null){
            exp.put("submitter", companyUserEntity.getUserName());
        }
        exp.put("expFlag", expMainEntity.getExpFlag() + "");
        exp.put("submittime", DateUtils.formatTimeSlash(expMainEntity.getCreateDate()));
        exp.put("remark", expMainEntity.getRemark());
        exp.put("approveStatus", expMainEntity.getApproveStatus());
        exp.put("versionNum", expMainEntity.getVersionNum() + "");
        exp.put("expNo", expMainEntity.getExpNo());
        exp.put("companyUserId",expMainEntity.getCompanyUserId());

        ExpAuditEntity recallAudit = this.expAuditDao.selectLastRecallAudit(id);
        if (recallAudit!=null) {
            exp.put("sendBackReason", recallAudit.getAuditMessage());
        } else {
            exp.put("sendBackReason", "");
        }

        //获取详情信息
        List<ExpDetailDTO> detailList = expDetailDao.selectDetailDTOByMainId(id);
        for(ExpDetailDTO detail:detailList){
            detail.setExpName(detail.getExpTypeParentName());
            detail.setExpAllName(detail.getExpTypeName());
            detail.setRelationRecordData( this.relationRecordService.getRelationList(new QueryRelationRecordDTO(id,detail.getId())));
        }

        //获取审批历史记录
        List<AuditDTO> auditList = this.getAuditList(id,expMainEntity);
        //获取当前审批人信息
        ExpMainDataDTO currentAuditPerson = new ExpMainDataDTO();
        BaseDTO.copyFields(auditList.get(auditList.size()-1),currentAuditPerson);
        //获取附件
        Map<String, Object> param = new HashMap<>();
        param.put("targetId", id);
        param.put("type", NetFileType.EXPENSE_ATTACH);
        List<FileDataDTO> attachList = this.projectSkyDriverService.getAttachDataList(param);

        //返回数据
        map.put("exp", exp);
        map.put("currentAuditPerson", currentAuditPerson);
        map.put("auditList", auditList);
        map.put("attachList", attachList);
        if(!CollectionUtils.isEmpty(detailList)){
            map.put("totalExpAmount", detailList.get(0).getTotalExpAmount());//每条记录中都记录了总金额
        }else {
            map.put("totalExpAmount", 0);
        }
        //填充外部组织名称
        if (!StringUtil.isNullOrEmpty(expMainEntity.getEnterpriseId())) {
            map.put("enterpriseName",enterpriseService.getEnterpriseName(expMainEntity.getEnterpriseId()));
            map.put("enterpriseId",expMainEntity.getEnterpriseId());
        }
        map.put("detailList", detailList);
        //查询抄送人
        map.put("ccCompanyUserList",copyRecordService.getCopyRecode(new QueryCopyRecordDTO(id)));
        //返回流程标识，给前端控制是否要给审批人，以及按钮显示的控制

        map.putAll(processService.getCurrentTaskUser(new AuditEditDTO(id,null,null),auditList,(detailList.get(0).getTotalExpAmount()).toString()));
        return map;
    }

    /**
     * 我申请的  详情
     * @param dto
     * @return
     */
    @Override
    public List<ExpMainDTO> getAuditDataDetail(QueryAuditDTO dto) {
        return expMainDao.getAuditDataDetail(dto);
    }

    @Override
    public void saveExpMain(ExpMainEntity entity, AuditBaseDTO dto) throws Exception {

        if(!StringUtils.isEmpty(dto.getId())){//如果是编辑，则要把原有的记录的expFlag设置为1，以免下次再可编辑
            ExpMainEntity exp = new ExpMainEntity();
            exp.setId(dto.getId());
            exp.setExpFlag(1);
            this.expMainDao.updateById(exp);
        }
        String userId = dto.getAccountId();
        String companyId = dto.getCurrentCompanyId();
        String type = null;
        Object conditionFieldValue = null;
        if(dto instanceof SaveDynamicAuditDTO){
            type = ((SaveDynamicAuditDTO) dto).getType();
            conditionFieldValue = dto.getConditionFieldValue();
        }

        if(StringUtil.isNullOrEmpty(type)){
            entity.setType(ExpenseConst.TYPE_EXPENSE); //默认为报销费用
        }else {
            entity.setType(type);
        }
        entity.setCompanyUserId(dto.getCurrentCompanyUserId());
        entity.setExpFlag(0);
        entity.setExpNo(this.getExpNo(companyId));
        entity.set4Base(userId, userId, new Date(), new Date());
        entity.setCompanyId(dto.getCurrentCompanyId());
        entity.setExpDate(DateUtils.getDate());
        entity.setApproveStatus("0");//默认为待审批状态
        if(StringUtils.isEmpty(entity.getId())){
            entity.initEntity();//如果id不为null，不用initEntity，因为在调用该接口的地方已经设置了id
        }

        if(StringUtils.isNotEmpty(dto.getId())){//如果是重新编辑
            projectSkyDriverService.copyFileToNewObject(entity.getId(),dto.getId(),NetFileType.EXPENSE_ATTACH,dto.getDeleteAttachList());
        }
        expMainDao.insert(entity);
        String targetType = type;//动态模板中的 targetType 就是 expMain中的type （每创建一个模板，就当做是一种类型）
        // 启动流程
        ActivitiDTO activitiDTO = new ActivitiDTO(entity.getId(),entity.getCompanyUserId(),companyId,userId,conditionFieldValue,targetType);
        activitiDTO.getParam().put("approveUser",dto.getAuditPerson());
        this.processService.startProcessInstance(activitiDTO);
    }


    @Override
    public AuditCommonDTO getAuditDataById(String id) {
        QueryAuditDTO queryAuditDTO = new QueryAuditDTO();
        queryAuditDTO.setMainId(id);
        List<AuditCommonDTO> list = this.expMainDao.getAuditDataForWeb(queryAuditDTO);
        if(!CollectionUtils.isEmpty(list)){
            return list.get(0);
        }
        return new AuditCommonDTO();
    }

    @Override
    public List<CoreShowDTO> listAuditTypeName(QueryAuditDTO query) throws Exception {
        setQuery(query);
        return this.expMainDao.listAuditTypeName(query);
    }


    private void setQuery(QueryAuditDTO dto){
        //页面审批管理：我申请的（type=1）
        if ("1".equals(dto.getType())){
            dto.setCompanyUserId(dto.getCurrentCompanyUserId());
        }
        //页面审批管理：待我审批（type=2）
        if ("2".equals(dto.getType())){
            dto.setAuditPerson(dto.getCurrentCompanyUserId());
        }
        //页面审批管理：我已审批（type=3）
        if("3".equals(dto.getType())){
            dto.setAuditPerson(dto.getCurrentCompanyUserId());
        }
        //页面审批管理：抄送我的（type=4）
        if("4".equals(dto.getType())){
            dto.setCcCompanyUserId(dto.getCurrentCompanyUserId());
        }
    }
}

