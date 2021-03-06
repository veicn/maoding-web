package com.maoding.project.dto;


import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;


/**
 * 深圳市卯丁技术有限公司
 * 日期: 2018/8/24
 * 类名: com.maoding.project.dto.ProjectQueryDTO
 * 作者: 张成亮
 * 描述:
 **/
public class ProjectQueryDTO extends DynamicQueryDTO {
    /** 立项时间 **/
    @JsonFormat(pattern="yyyy-MM-dd",timezone = "GMT+8")
    private Date projectCreateDateStart;

    @JsonFormat(pattern="yyyy-MM-dd",timezone = "GMT+8")
    private Date projectCreateDateEnd;

    /** 更改为projectCreateDateStart、projectCreateDateEnd **/
    @Deprecated
    private Date createDate;

    /** 项目状态 **/
    private String status;

    /** 项目类型 **/
    private String projectType;

    /** 甲方 **/
    private String partA;

    /** 乙方 **/
    private String partB;

    /** 合同签订 **/
    @JsonFormat(pattern="yyyy-MM-dd",timezone = "GMT+8")
    private String signDateStart;

    @JsonFormat(pattern="yyyy-MM-dd",timezone = "GMT+8")
    private String signDateEnd;

    /** 立项组织 */
    private String createCompany;

    /** 使用createCompany代替 **/
    @Deprecated
    private String companyId;

    /** 项目地点 **/
    private String address;

    /** 省/直辖市 **/
    private String province;

    /** 市/区 **/
    private String city;

    /** 区/县 **/
    private String county;

    /** 功能分类 **/
    private List<String> buildNameList;

    /** 合作组织名称 **/
    private String relationCompany;

    /** 经营负责人 **/
    private String busPersonInCharge;

    /** 经营助理 **/
    private String busPersonInChargeAssistant;

    /** 设计负责人 **/
    private String designPersonInCharge;

    /** 设计助理 **/
    private String designPersonInChargeAssistant;

    /** 任务负责人 **/
    private String taskLeader;

    /** 设计人员 **/
    private String designer;

    /** 校对人员 **/
    private String checker;

    /** 审核人员 **/
    private String auditor;

    /** 立项时间排序：0-正序，1-倒序 **/
    private Integer projectCreateDateOrder;

    /** 合同签订时间排序：0-正序，1-倒序 **/
    private Integer signDateOrder;

    /** 项目编号 */
    /** 目前没有定义此筛选条件 **/
    @Deprecated
    private String projectNo;

    /** 项目名称 */
    /** 目前没有定义此筛选条件 **/
    @Deprecated
    private String projectName;
    
    /** 我的项目还是项目总览：0-项目总览，1-我的项目 **/
    private Integer type;

    /** 要查询的费用类别 **/
    /** 取消 **/
    @Deprecated
    private Integer costType;

    /** 要查询的成员类别 **/
    /** 取消 **/
    @Deprecated
    private Integer memberType;

    /** 是否查询收付款实际到账 **/
    /** 取消 **/
    @Deprecated
    private Integer isDetail;

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public Integer getIsDetail() {
        return isDetail;
    }

    public void setIsDetail(Integer isDetail) {
        this.isDetail = isDetail;
    }

    public Integer getCostType() {
        return costType;
    }

    public void setCostType(Integer costType) {
        this.costType = costType;
    }

    public Integer getMemberType() {
        return memberType;
    }

    public void setMemberType(Integer memberType) {
        this.memberType = memberType;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getProjectCreateDateOrder() {
        return projectCreateDateOrder;
    }

    public void setProjectCreateDateOrder(Integer projectCreateDateOrder) {
        this.projectCreateDateOrder = projectCreateDateOrder;
    }

    public Integer getSignDateOrder() {
        return signDateOrder;
    }

    public void setSignDateOrder(Integer signDateOrder) {
        this.signDateOrder = signDateOrder;
    }

    public Date getProjectCreateDateStart() {
        return projectCreateDateStart;
    }

    public void setProjectCreateDateStart(Date projectCreateDateStart) {
        this.projectCreateDateStart = projectCreateDateStart;
    }

    public Date getProjectCreateDateEnd() {
        return projectCreateDateEnd;
    }

    public void setProjectCreateDateEnd(Date projectCreateDateEnd) {
        this.projectCreateDateEnd = projectCreateDateEnd;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPartA() {
        return partA;
    }

    public void setPartA(String partA) {
        this.partA = partA;
    }

    public String getPartB() {
        return partB;
    }

    public void setPartB(String partB) {
        this.partB = partB;
    }

    public String getSignDateStart() {
        return signDateStart;
    }

    public void setSignDateStart(String signDateStart) {
        this.signDateStart = signDateStart;
    }

    public String getSignDateEnd() {
        return signDateEnd;
    }

    public void setSignDateEnd(String signDateEnd) {
        this.signDateEnd = signDateEnd;
    }

    public String getCreateCompany() {
        return createCompany;
    }

    public void setCreateCompany(String createCompany) {
        this.createCompany = createCompany;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public List<String> getBuildNameList() {
        return buildNameList;
    }

    public void setBuildNameList(List<String> buildNameList) {
        this.buildNameList = buildNameList;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRelationCompany() {
        return relationCompany;
    }

    public void setRelationCompany(String relationCompany) {
        this.relationCompany = relationCompany;
    }

    public String getBusPersonInCharge() {
        return busPersonInCharge;
    }

    public void setBusPersonInCharge(String busPersonInCharge) {
        this.busPersonInCharge = busPersonInCharge;
    }

    public String getBusPersonInChargeAssistant() {
        return busPersonInChargeAssistant;
    }

    public void setBusPersonInChargeAssistant(String busPersonInChargeAssistant) {
        this.busPersonInChargeAssistant = busPersonInChargeAssistant;
    }

    public String getDesignPersonInCharge() {
        return designPersonInCharge;
    }

    public void setDesignPersonInCharge(String designPersonInCharge) {
        this.designPersonInCharge = designPersonInCharge;
    }

    public String getDesignPersonInChargeAssistant() {
        return designPersonInChargeAssistant;
    }

    public void setDesignPersonInChargeAssistant(String designPersonInChargeAssistant) {
        this.designPersonInChargeAssistant = designPersonInChargeAssistant;
    }

    public String getTaskLeader() {
        return taskLeader;
    }

    public void setTaskLeader(String taskLeader) {
        this.taskLeader = taskLeader;
    }

    public String getDesigner() {
        return designer;
    }

    public void setDesigner(String designer) {
        this.designer = designer;
    }

    public String getChecker() {
        return checker;
    }

    public void setChecker(String checker) {
        this.checker = checker;
    }

    public String getAuditor() {
        return auditor;
    }

    public void setAuditor(String auditor) {
        this.auditor = auditor;
    }

    public String getProjectNo() {
        return projectNo;
    }

    public void setProjectNo(String projectNo) {
        this.projectNo = projectNo;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public ProjectQueryDTO(){}
    public ProjectQueryDTO(String companyId, String projectNo, String projectName, Date createDate){
        setCompanyId(companyId);
        setProjectNo(projectNo);
        setProjectName(projectName);
        setCreateDate(createDate);
        setPageSize(1);
    }

}
