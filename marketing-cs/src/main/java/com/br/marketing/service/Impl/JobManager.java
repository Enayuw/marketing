package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.mapper.TransferActionFrontMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class JobManager {

    @Autowired
    TransferActionFrontMapper transferActionFrontMapper;

    /**
     * 获取推送记录
     *
     * @param apiCode
     * @param date
     * @param actionType
     * @return
     */
    public Result<TransferActionFront> getFrontData(String apiCode, String date, Integer actionType) {
        TransferActionFrontExample frontExample = new TransferActionFrontExample();
        frontExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(date)
                .andActionTypeEqualTo(actionType)
                .andIsDelEqualTo(1);

        List<TransferActionFront> transferActionFronts = transferActionFrontMapper.selectByExample(frontExample);

        if (transferActionFronts.size() > 1) {
            log.error(String.format("该推送日志当前有条 请检查apiCode:%s,data:%s,type:%s", apiCode, date, actionType));
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }
        if(transferActionFronts.size()<=0){
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(null);
        }

        TransferActionFront transferActionFront = transferActionFronts.get(0);

        if (new Integer(2).equals(transferActionFront.getStatus())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("今日作业已经执行结束");
        }

        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferActionFront);

    }

    public Long saveFrontData(String apiCode, String date, Integer actionType) {
        TransferActionFront front = new TransferActionFront();
        front.setApiCode(apiCode);
        front.setStatus(1);
        front.setActionType(actionType);
        front.setActionData(date);
        front.setCreateTime(new Date());
        front.setUpdateTime(new Date());
        transferActionFrontMapper.insertSelective(front);
        return front.getId();
    }

    public TransferActionFront saveFront(String apiCode, String date, Integer actionType) {
        TransferActionFront front = new TransferActionFront();
        front.setApiCode(apiCode);
        front.setStatus(1);
        front.setActionType(actionType);
        front.setActionData(date);
        front.setCreateTime(new Date());
        front.setUpdateTime(new Date());
        transferActionFrontMapper.insertSelective(front);
        return front;
    }

    public void updateFrontDataStatus(Long id, Integer status) {
        TransferActionFront front = new TransferActionFront();
        front.setId(id);
        front.setStatus(status);
        transferActionFrontMapper.updateByPrimaryKeySelective(front);
    }

    /**
     * 2023-11-18 16:00
     * 保存执行记录，赋值初始状态
     *
     * @return 执行记录主键
     */
    public Long saveFrontData(TransferActionFront front) {
        front.setStatus(1);
        front.setCreateTime(new Date());
        front.setIsDel(1);
        transferActionFrontMapper.insertSelective(front);
        return front.getId();
    }

    /**
     * 2023-11-18 16:00
     * 查询执行记录
     */
    public TransferActionFront getFrontData(String apiCode, String date
            , Integer actionType, String remark) {
        TransferActionFrontExample frontExample = new TransferActionFrontExample();
        TransferActionFrontExample.Criteria criteria = frontExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(date)
                .andIsDelEqualTo(1);
        if (remark != null) {
            criteria.andRemarkEqualTo(remark);
        }
        if (actionType != null) {
            criteria.andActionTypeEqualTo(actionType);
        }
        frontExample.setOrderByClause("create_time desc,update_time desc");
        List<TransferActionFront> transferActionFronts = transferActionFrontMapper.selectByExample(frontExample);
        if (transferActionFronts.size() == 1) {
            return transferActionFronts.get(0);
        } else if (transferActionFronts.size() > 1) {
            log.error("transferActionFront请检查apiCode:{},data:{},type:{}当前执行日志有{}条记录！"
                    , apiCode, date, actionType, transferActionFronts.size());
            return transferActionFronts.get(0);
        } else {
            return null;
        }
    }

    /**
     * 2023-11-18 15:58
     * 获取查询状态
     *
     * @return true 存在状态 {@code status}
     */
    public boolean getActionStatus(String apiCode, String date, Integer status, Integer actionType, String remark) {
        TransferActionFront frontData = getFrontData(apiCode, date, actionType, remark);
        return frontData != null && frontData.getStatus().equals(status);
    }


    /**
     * 2024-05-13 13:38
     * 根据条件更新
     */
    public int updateActionFrontInfo(String apiCode, String dateStr
            , Integer actionType, String remark, TransferActionFront front) {
        TransferActionFrontExample frontExample = new TransferActionFrontExample();
        TransferActionFrontExample.Criteria criteria = frontExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(dateStr)
                .andIsDelEqualTo(1);
        if (actionType != null) {
            criteria.andActionTypeEqualTo(actionType);
        }
        if (remark != null) {
            criteria.andRemarkEqualTo(remark);
        }
        return transferActionFrontMapper.updateByExampleSelective(front, frontExample);
    }


    public enum ActionTypeEnum {
        /**
         * 2024-05-16 14:40
         * 众邦上传录音文件执行类型
         */
        ZHONGBANG_PUSH_VOICE_FILE(3, "3710099", "7433800"),
        YIXIN_TRANSFER_PUSH_BAIYING(15, "3710012", "7412003"),
        YIXIN_BLACK_PUSH_BAIYING(16, "3710012", "7412003"),
        YIXIN_TRANSFER_PUSH_BIOCLOO(17, "3710012", "7412003"),
        YIXIN_BLACK_PUSH_BIOCLOO(18, "3710012", "7412003"),
        QIFU_TRIGGER_BRANCH_USER(2, "3710139", "7491635"),
        QIFU_DELETE_REACH_RECORD(1, "3710139"),

        WUBA_CHANGE_QUERY_BATCH(1, "3710155", "7491580"),
        WUBA_CHANGE_SUBMIT_DATA_BATCH(2, "3710155", "7491580"),
        WUBA_QUERY_CONVERSION_ZIP_RESULT(3, "3710155", "7491580"),
        WUBA_OLD_QUERY_CONVERSION_ZIP_RESULT(4, "3710155", "7491480"),
        SANLIULING_COLLECTION_DATA(1, "3700317", "7491777"),
        ZHONGBANG_AI_PUSH_VOICE_FILE(1, "3740001"),
        SUIYIJI_GETBLACK_PUSHTRANSFER(1, "3710222"),


        ;

        /**
         * 2024-05-16 11:38
         * 执行类型
         */
        private int actionType;
        /**
         * 2024-05-16 11:39
         * 使用该类型的集合
         */
        private String[] apiCodes;

        ActionTypeEnum(int actionType, String... apiCodes) {
            this.actionType = actionType;
            this.apiCodes = apiCodes;
        }

        public int getActionType() {
            return actionType;
        }

        public void setActionType(int actionType) {
            this.actionType = actionType;
        }

        public String[] getApiCodes() {
            return apiCodes;
        }

        public void setApiCodes(String[] apiCodes) {
            this.apiCodes = apiCodes;
        }
    }
}
