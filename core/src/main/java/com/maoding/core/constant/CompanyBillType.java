package com.maoding.core.constant;

public class CompanyBillType {

    //收款方向
    public static final Integer DIRECTION_PAYEE = 1;

    //付款方向
    public static final Integer DIRECTION_PAYER = 2;

    //合同回款
    public static final Integer FEE_TYPE_CONTRACT = 1;

    //技术审查费
    public static final Integer FEE_TYPE_TECHNICAL  = 2;

    //合作设计费
    public static final Integer FEE_TYPE_COOPERATIVE  = 3;

    //其他收支
    public static final Integer FEE_TYPE_OTHER  = 4;
    //其他收款（costType）
    public static final Integer FEE_TYPE_OTHER_PAID  = 5;//项目费用中的其他收款类型。但是在财务数据中，统一为其他费用（类型为：4）

    //报销
    public static final Integer FEE_TYPE_EXPENSE   = 5;

    //费用申请
    public static final Integer FEE_TYPE_EXP_APPLY  = 6;

    //固定支出
    public static final Integer FEE_TYPE_EXP_FIX  = 7;

    //其他业务收入
    public static final Integer FEE_TYPE_FIX_OTHER_INCOME  = 8;

    //自定义审批表申请
    public static final Integer FEE_TYPE_DYNAMIC  = 9;
}
