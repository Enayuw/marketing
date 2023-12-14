package com.br.marketing.aspect;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.DataDistributeLogBase;
import com.br.marketing.dto.DataJoinLogDTO;
import com.br.marketing.entity.DataDistributeDetailLog;
import com.br.marketing.entity.DataDistributeDetailLogExample;
import com.br.marketing.mapper.DataDistributeDetailLogMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    /*private final static List soleTypes;

   // 1-apiCode,custNum
    private final static Integer soleTypeOne = new Integer(1);

    // 2-apiCode,cell
    private final static Integer soleTypeTwo = new Integer(2);

    // 3-apiCode,cell,status
    private final static Integer soleTypeThree = new Integer(3);

    static {
        soleTypes = new ArrayList();
        soleTypes.add(soleTypeOne);
        soleTypes.add(soleTypeTwo);
        soleTypes.add(soleTypeThree);
    }*/

    @Around("@annotation(com.br.marketing.common.annoation.DistributeLog)")
    public Object distribute(ProceedingJoinPoint jp) throws Throwable {
        final Object[] args = jp.getArgs();
        Object arg = args[0];
        //region check
        if (!(arg instanceof DataDistributeLogBase)) {
            return jp.proceed();
        }
        DataDistributeLogBase logBase = (DataDistributeLogBase) arg;
        List<DataJoinLogDTO> detailLogList = logBase.getDetailLogList();
        if (detailLogList.size() <= 0) {
            return jp.proceed();
        }
        if (logBase.getIsSole() && (!SoleFieldEnum.getValues().contains(logBase.getSoleField()))) {
            return jp.proceed();
        }
        List data = logBase.getData();
        HashMap<String, Object> dataMap = new HashMap<>();
        if(logBase.getIsSole()) {
            for (Object datum : data) {
                dataMap.put(DigestUtils.md5DigestAsHex(datum.toString().getBytes()) + datum.hashCode(), datum);
            }
        }
        //endregion
        DataJoinLogDTO dataDistributeDetailLog = detailLogList.get(0);
        boolean isRecord = dataDistributeDetailLog.getId() != null && dataDistributeDetailLog.getId() > 0;
        //去重数据
//        ArrayList<Object> soleDatas = new ArrayList<>();
//        //去重日志
//        ArrayList<Object> soleDataLogs = new ArrayList<>();
        if (!isRecord) {
            HashSet dataMd5Set = new HashSet();

            Iterator<DataJoinLogDTO> iterator = detailLogList.iterator();
            long start = System.currentTimeMillis();
            while (iterator.hasNext()) {
                long start2 = System.currentTimeMillis();
                DataJoinLogDTO logData = iterator.next();
                if (logBase.getIsSole()) {
                    //region 去重处理
                    String key = RedisKeyConstant.dributeDataSloeLock;
                    if (SoleFieldEnum.CUST_NUM_SOLE.getValue().equals(logBase.getSoleField())) {
                        key = key.concat(String.format(":%d:%d:%s:%s", logData.getDistributeType()
                                , logBase.getSoleDay(), logData.getApiCode(), logData.getCustNum()));
                    } else if (SoleFieldEnum.CELL_SOLE.getValue().equals(logBase.getSoleField())) {
                        key = key.concat(String.format(":%d:%d:%s:%s", logData.getDistributeType()
                                , logBase.getSoleDay(), logData.getApiCode(), logData.getCell()));
                    }else if (SoleFieldEnum.CELL_STATUS_SOLE.getValue().equals(logBase.getSoleField())) {
                        key = key.concat(String.format(":%d:%d:%s:%s:%s", logData.getDistributeType()
                                , logBase.getSoleDay(), logData.getApiCode(), logData.getCell(), logData.getStatus()));
                    } else if (SoleFieldEnum.CUST_NUM_STATUS_SOLE.getValue().equals(logBase.getSoleField())) {
                        key = key.concat(String.format(":%d:%d:%s:%s:%s"
                                , logData.getDistributeType()
                                , logBase.getSoleDay()
                                , logData.getApiCode()
                                , logData.getCustNum()
                                , logData.getStatus()));
                    }

                    try {
                        UUID uuid = UUID.randomUUID();
                        redisChgService.lock(key, uuid.toString());
                        //region 去重判断
                        // 数组内去重
                        if (!dataMd5Set.add(logData.getDataMd5())) {
                            Object o = dataMap.get(logData.getDataMd5() + logData.getDataCode());
                            if(o != null){
                                iterator.remove();
                                data.remove(o);
                                redisChgService.unlock(key, uuid.toString());
                                continue;
                            }
                        }
                        DataDistributeDetailLogExample logExample = new DataDistributeDetailLogExample();
                        logExample.setOrderByClause(" id limit 1 ");
                        DataDistributeDetailLogExample.Criteria criteria = logExample.createCriteria().andApiCodeEqualTo(logData.getApiCode())
                                .andDistributeTypeEqualTo(logData.getDistributeType());
                        if (logBase.getSoleDay() != null && logBase.getSoleDay() > 0) {
                            if (logBase.getSoleDay() == 1) {
                                String day = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                criteria.andDistributeDateEqualTo(day);
                            } else {
                                String day = LocalDate.now().minusDays(logBase.getSoleDay() - 1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                criteria.andDistributeDateGreaterThanOrEqualTo(day);
                            }
                        } else if (logBase.getSoleDay() != null && logBase.getSoleDay() == -1) {
                            //单条数据当前有效期内去重
                            if(StringUtils.isEmpty(logData.getExtend())){
                                criteria.andExtendIsNull();
                            }else{
                                criteria.andExtendEqualTo(logData.getExtend());
                            }
                        }
                        if (logBase.getSoleField().equals(SoleFieldEnum.CUST_NUM_SOLE.getValue())) {
                            criteria.andCustNumEqualTo(logData.getCustNum());
                        } else if (logBase.getSoleField().equals(SoleFieldEnum.CELL_SOLE.getValue())) {
                            criteria.andCellEqualTo(logData.getCell());
                        } else if (logBase.getSoleField().equals(SoleFieldEnum.CELL_STATUS_SOLE.getValue())) {
                            criteria.andCellEqualTo(logData.getCell());
                            criteria.andStatusEqualTo(logData.getStatus());
                        } else if (SoleFieldEnum.CUST_NUM_STATUS_SOLE.getValue().equals(logBase.getSoleField())) {
                            criteria.andCustNumEqualTo(logData.getCustNum());
                            criteria.andStatusEqualTo(logData.getStatus());
                        }
                        List<DataDistributeDetailLog> dataDistributeDetailLogs = dataDistributeDetailLogMapper.selectByExample(logExample);
                        if (dataDistributeDetailLogs.size() > 0) {
                            Object o = dataMap.get(logData.getDataMd5() + logData.getDataCode());
                            if(o != null){
                                iterator.remove();
                                data.remove(o);
                                redisChgService.unlock(key, uuid.toString());
                                continue;
                            }
                        } else {
                            DataDistributeDetailLog newLog = new DataDistributeDetailLog();
                            BeanUtils.copyProperties(logData, newLog);
                            newLog.setCreateTime(new Date());
                            dataDistributeDetailLogMapper.insertSelective(newLog);
                            logData.setId(newLog.getId());
                        }
                        redisChgService.unlock(key, uuid.toString());
                        //endregion
                    } catch (Exception ex) {
                        continue;
                    }
                    //endregion
                } else {
                    DataDistributeDetailLog newLog = new DataDistributeDetailLog();
                    BeanUtils.copyProperties(logData, newLog);
                    newLog.setCreateTime(new Date());
                    dataDistributeDetailLogMapper.insertSelective(newLog);
                    logData.setId(newLog.getId());
                }
                log.warn("推送客服转化去重一次的耗时："+(System.currentTimeMillis()-start2));
            }
            log.warn("推送客服转化切面去重耗时："+(System.currentTimeMillis()-start));
        }
//        if (logBase.getIsSole()) {
//            ((DataDistributeLogBase) args[0]).getData().removeAll(soleDatas);
//            ((DataDistributeLogBase) args[0]).setDetailLogList(detailLogList);
//            ((DataDistributeLogBase) args[0]).getDetailLogList().removeAll(soleDataLogs);
//        }

        long start1 = System.currentTimeMillis();
        Object proceed = jp.proceed();
        log.warn("推送客服转化推送耗时："+(System.currentTimeMillis()-start1));
        //region 结果处理
        if (proceed instanceof Result) {
            Result res = (Result) proceed;
            DataDistributeDetailLog updateEntity = new DataDistributeDetailLog();
            if (ResultCode.SUCCESS.getValue().equals(res.getCode())) {
                List<Long> logIds = detailLogList.stream().map(DataDistributeDetailLog::getId).collect(Collectors.toList());
                DataDistributeDetailLogExample upExample = new DataDistributeDetailLogExample();
                upExample.createCriteria().andIdIn(logIds);
                updateEntity.setSuccessDate(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                updateEntity.setpStatus(2);
                if (logIds != null && logIds.size() > 0) {
                    dataDistributeDetailLogMapper.updateByExampleSelective(updateEntity, upExample);
                }
            } else if (ResultCode.FAIL.getValue().equals(res.getCode())) {
                List<Long> logIds = detailLogList.stream().map(DataDistributeDetailLog::getId).collect(Collectors.toList());
                DataDistributeDetailLogExample upExample = new DataDistributeDetailLogExample();
                upExample.createCriteria().andIdIn(logIds);
                updateEntity.setpStatus(3);
                if (logIds != null && logIds.size() > 0) {
                    dataDistributeDetailLogMapper.updateByExampleSelective(updateEntity, upExample);
                }
            }
            return res;
        }
        //endregion
        return proceed;
    }
}
