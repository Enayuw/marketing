package com.br.marketing.service.Impl;

import com.br.marketing.service.IDataCleaningGeneralService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 数据清洗处理接口
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-05-24
 */
@Service
@Slf4j
public class DataCleaningGeneralServiceImpl implements IDataCleaningGeneralService {


    @Resource


    @Override
    public void isAction() {

    }

    @Override
    public void action() {

        // 查询满足处理条件的清洗任务

        // 解析配置的清洗规则

        // 读取文件并逐行处理

        // 打印处理正确和不正确的条数以及所在行

        // 根据清洗的文件类型（上传、转换等）
            // 参数封装
            // 调用推送方法

    }

    @Override
    public void pilotAction(Long id) {
        // 查询ID对应的清洗任务

        // 解析配置的清洗规则

        // 读取预存的数据并逐行处理

        // 打印处理正确和不正确的条数以及所在行

        // 根据清洗的文件类型（上传、转换等）
            // 参数封装
            // 调用推送方法
    }
}
