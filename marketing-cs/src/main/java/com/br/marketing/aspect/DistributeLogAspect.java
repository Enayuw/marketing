package com.br.marketing.aspect;

import IceInternal.Ex;
import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.annoation.DistributeLog;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.exception.KnowException;
import com.br.marketing.dto.DataDistributeBase;
import com.br.marketing.dto.DataDistributeLogBase;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.DataDistributeDetailLogMapper;
import com.br.marketing.mapper.RetryMainLogMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Aspect
@Order(-990) // 异常处理之内
@Component
public class DistributeLogAspect {
    private static final Logger log = LoggerFactory.getLogger(RetryAspect.class);

    @Resource
    DataDistributeDetailLogMapper dataDistributeDetailLogMapper;

    @Autowired
    RedisChgService redisChgService;

    private final static List soleTypes;

    static {
        soleTypes = new ArrayList();
        soleTypes.add(1);
        soleTypes.add(2);
    }

    @Around("@annotation(com.br.marketing.common.annoation.DistributeLog)")
    public Object retry(ProceedingJoinPoint jp) throws Throwable {
        final Object[] args = jp.getArgs();
        Object arg = args[0];
        //region check
        if (!(arg instanceof DataDistributeLogBase)) {
            return jp.proceed();
        }
        DataDistributeLogBase logBase = (DataDistributeLogBase) arg;
        List<DataDistributeDetailLog> detailLogList = logBase.getDetailLogList();
        if (detailLogList.size() <= 0) {
            return jp.proceed();
        }
        List<? extends DataDistributeBase> data = logBase.getData();
        Method sMethod = ((MethodSignature) jp.getSignature()).getMethod();
        DistributeLog distributeLog = sMethod.getAnnotation(DistributeLog.class);
        DistributeTypeEnum distributeTypeEnum = distributeLog.distributeType();
        if(logBase.getIsSole() && !soleTypes.contains(logBase.getSoleField())){
            return jp.proceed();
        }
        //endregion
        DataDistributeDetailLog dataDistributeDetailLog = detailLogList.get(0);
        boolean isRecord = dataDistributeDetailLog.getId() !=null&&dataDistributeDetailLog.getId()>0;
        //去重数据
        ArrayList<Object> soleDatas = new ArrayList<>();
        //去重日志
        ArrayList<Object> soleDataLogs = new ArrayList<>();
        if(!isRecord){
            for (DataDistributeDetailLog log : detailLogList) {

                if(logBase.getIsSole()) {
                    //region 去重处理
                    String key = RedisKeyConstant.dributeDataSloeLock;
                    switch (logBase.getSoleField()) {
                        // 1-apiCode,custNum
                        case 1:
                            key = key.concat(String.format(":%d:%d:%s:%s", log.getDistributeType()
                                    , logBase.getSoleDay(), log.getApiCode(), log.getCustNum()));
                            // 2-apiCode,cell
                        case 2:
                            key = key.concat(String.format(":%d:%d:%s:%s", log.getDistributeType()
                                    , logBase.getSoleDay(), log.getApiCode(), log.getCell()));
                    }
                    try {
                        UUID uuid = UUID.randomUUID();
                        redisChgService.lock(key, uuid.toString());
                        //region 去重判断
                        DataDistributeDetailLogExample logExample = new DataDistributeDetailLogExample();
                        logExample.setOrderByClause(" id limit 1 ");
                        DataDistributeDetailLogExample.Criteria criteria = logExample.createCriteria().andApiCodeEqualTo(log.getApiCode())
                                .andDistributeTypeEqualTo(distributeLog.distributeType().getValue());
                        if (logBase.getSoleDay() != null && logBase.getSoleDay() > 0) {
                            if (logBase.getSoleDay() == 1) {
                                String day = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                criteria.andDistributeDateEqualTo(day);
                            } else {
                                String day = LocalDate.now().minusDays(logBase.getSoleDay() - 1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                criteria.andDistributeDateGreaterThanOrEqualTo(day);
                            }
                        }
                        if (logBase.getSoleField() == 1) {
                            criteria.andCustNumEqualTo(log.getCustNum());
                        } else if (logBase.getSoleField() == 2) {
                            criteria.andCellEqualTo(log.getCell());
                        }
                        List<DataDistributeDetailLog> dataDistributeDetailLogs = dataDistributeDetailLogMapper.selectByExample(logExample);
                        if (dataDistributeDetailLogs.size() > 0) {
                            soleDatas.add(data.stream().filter(t -> t.getId().equals(log.getSourceId())).findFirst().get());
                            soleDataLogs.add(log);
                            redisChgService.unlock(key, uuid.toString());
                            continue;
                        } else {
                            dataDistributeDetailLogMapper.insertSelective(log);
                        }
                        redisChgService.unlock(key, uuid.toString());
                        //endregion
                    } catch (Exception ex) {
                        continue;
                    }
                    //endregion
                }else{
                    dataDistributeDetailLogMapper.insertSelective(log);
                }
            }
        }
        if(logBase.getIsSole()){
            ((DataDistributeLogBase)args[0]).getData().removeAll(soleDatas);
            ((DataDistributeLogBase)args[0]).setDetailLogList(detailLogList);
            ((DataDistributeLogBase)args[0]).getDetailLogList().removeAll(soleDataLogs);
        }
        Object proceed = jp.proceed();
        //region 结果处理
        if (proceed instanceof Result) {
            Result res = (Result) proceed;
            DataDistributeDetailLog updateEntity = new DataDistributeDetailLog();

            if (ResultCode.SUCCESS.getValue().equals(res.getCode())) {
                List<Long> logIds = detailLogList.stream().map(DataDistributeDetailLog::getId).collect(Collectors.toList());
                DataDistributeDetailLogExample upExample = new DataDistributeDetailLogExample();
                upExample.createCriteria().andIdIn(logIds);
                updateEntity.setpStatus(2);
                dataDistributeDetailLogMapper.updateByExample(updateEntity,upExample);
            }else if (ResultCode.FAIL.getValue().equals(res.getCode())){
                List<Long> logIds = detailLogList.stream().map(DataDistributeDetailLog::getId).collect(Collectors.toList());
                DataDistributeDetailLogExample upExample = new DataDistributeDetailLogExample();
                upExample.createCriteria().andIdIn(logIds);
                updateEntity.setpStatus(3);
                dataDistributeDetailLogMapper.updateByExample(updateEntity,upExample);
            }
            return res;
        }
        //endregion
        return proceed;
    }
}
