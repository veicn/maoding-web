package com.maoding.activiti.dto;

/**
 * 深圳市卯丁技术有限公司
 *
 * @author : 张成亮
 * @date : 2018/7/30
 * @description :
 */
public class FlowSequenceDTO extends FlowElementDTO {
    /** 起始流程元素编号 */
    private String startFlowId;
    /** 终止流程元素编号 */
    private String endFlowId;
    /** 执行条件 */
    private String condition;

    public String getStartFlowId() {
        return startFlowId;
    }

    public void setStartFlowId(String startFlowId) {
        this.startFlowId = startFlowId;
    }

    public String getEndFlowId() {
        return endFlowId;
    }

    public void setEndFlowId(String endFlowId) {
        this.endFlowId = endFlowId;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
}
