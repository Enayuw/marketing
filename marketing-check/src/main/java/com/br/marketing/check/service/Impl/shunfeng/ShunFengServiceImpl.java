package com.br.marketing.check.service.Impl.shunfeng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.client.shunfeng.ShunfengClient;
import com.br.marketing.client.shunfeng.input.BussinessInfoReq;
import com.br.marketing.client.shunfeng.output.BussinesInfoReponse;
import com.br.marketing.client.shunfeng.utils.AESUtil;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.ShunfengCompanyData;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.shunfeng.ShunfengCompanyDataMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ShunFengServiceImpl implements ShunFengService {

    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private ShunfengCompanyDataMapper shunfengCompanyDataMapper;


    @Autowired
    RedisChgService redisChgService;

    @Autowired
    private ShunfengClient shunfengClient;


    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private PushInfoService pushInfoService;


    @Value("${api.shunfeng.aesKey:00}")
    private String aesKey;

    /**
     * 获取公司明细信息
     *
     * @param id
     * @return
     */
    @Override
    public void getCompanyDetail(Long id) {
        LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
        if (localFile == null) {
            return;
        }

        ThreadPoolExecutor pool = BrExecutors.getThreadPool(5, 5, 50);
        long start = System.currentTimeMillis();
        Long indexId = null;
        while (true) {
            //取公司名称数据
            List<ShunfengCompanyData> companyNameData = shunfengCompanyDataMapper.getCompanyNameList(id,
                    indexId);
            if (CollectionUtils.isEmpty(companyNameData)) {
                break;
            }
            indexId = companyNameData.get(companyNameData.size() - 1).getId();
            modifyCorePoolSize(pool);
            List<List<ShunfengCompanyData>> partition = Lists.partition(companyNameData, 500);
            partition.forEach((List<ShunfengCompanyData> companyData) -> {
                pool.submit(() -> {
                    try {
                        getCompanyInfo(companyData, localFile.getApiCode());
                    } catch (Exception ex) {
                        log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode(), "顺丰获取企业信息异常！"), ex);

                    }
                });
            });

        }
        // 关闭线程池
        pool.shutdown();
        try {
            while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (InterruptedException ex) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode(), "顺丰获取企业信息线程池停止异常！"), ex);
            Thread.currentThread().interrupt();
        }
        localFile.setPushEndTime(new Date());
        localFile.setPushStatus("2");
        localFileMapper.updateByPrimaryKeySelective(localFile);
        log.warn("顺丰获取企业信息运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }

    private void getCompanyInfo(List<ShunfengCompanyData> companyDataList, String apiCode) {
        List<BussinesInfoReponse> companyDetailList = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        companyDataList.forEach((ShunfengCompanyData company) -> {
            String token = getShunfengToken();
            if (StringUtils.isEmpty(token)) {
                return;
            }
            BussinessInfoReq infoReq = new BussinessInfoReq();
            infoReq.setCompany_name(company.getCompanyName());
            Result<BussinesInfoReponse> bussinessDetailInfo = shunfengClient.getBussinessDetailInfo(infoReq, token);
            // 更新结果
            if (ResultCode.SUCCESS.getValue().equals(bussinessDetailInfo.getCode())) {
                BussinesInfoReponse reponse = bussinessDetailInfo.getData();
                companyDetailList.add(reponse);
            } else {
                log.warn("顺丰获取企业信息接口失败，companyName={}", company.getCompanyName());
                ids.add(company.getId());
            }
        });
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        //构造上传,转化参数
        buildParam(apiCode, companyDetailList, marketingPreUserDTO);
        if (Objects.isNull(marketingPreUserDTO)) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode(), "顺丰获取企业信息组装上传数据异常！"));
            return;
        }
        //上传接口
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        pushInfoService.pushUploadByRetry(uploadDataDTO, null);
        //更新失败的数据，数据回溯
        if (!CollectionUtils.isEmpty(ids)) {
            shunfengCompanyDataMapper.updateDataStatus(ids, 3);
        }
    }

    private void buildParam(String apiCode, List<BussinesInfoReponse> companyDetailList, MarketingPreUserDTO marketingPreUserDTO) {
        List<MarketingPreUserDetailDTO> dataItems = new ArrayList<>();
        companyDetailList.forEach((BussinesInfoReponse reponse) -> {
            MarketingPreUserDetailDTO detailDTO = new MarketingPreUserDetailDTO();
            //mock联系方式
            if(marketingCommonConfig.getShunFengInterMock()){
                reponse.setContact_info("ybHca/NCwfC6pzUOs0w+6g==");
            }
            String cell = AESUtil.decrypt(reponse.getContact_info(), aesKey);
            List<String> moreCellList = reponse.getMore_contact();
            if (StringUtils.isEmpty(cell) && CollectionUtils.isEmpty(moreCellList)) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode(),
                        "顺丰获取企业信息cell,morecell都为空,companyName=".concat(reponse.getCompany_name())));
                return;
            }
            if (StringUtils.isNotEmpty(cell)) {
                detailDTO.setCell(cell);
            } else {
                detailDTO.setCell(AESUtil.decrypt(moreCellList.get(0), aesKey));
            }
            detailDTO.setCustNum(reponse.getCredit_code());
            detailDTO.setName(reponse.getLegal_person_name());
            JSONObject reserveField1 = new JSONObject();
            reserveField1.put("userType", 1001);
            reserveField1.put("company_type", reponse.getCompany_type());
            reserveField1.put("registered_address", AESUtil.decrypt(reponse.getRegistered_address(), aesKey));
            reserveField1.put("city", reponse.getCity());
            reserveField1.put("latitude", reponse.getLatitude());
            reserveField1.put("active_addr", AESUtil.decrypt(reponse.getActive_addr(), aesKey));
            reserveField1.put("shareholders_supervisors_info", reponse.getShareholders_supervisors_info());
            reserveField1.put("contact_verification_time", reponse.getContact_verification_time());
            reserveField1.put("province", reponse.getProvince());
            reserveField1.put("longitude", reponse.getLongitude());
            reserveField1.put("industry1", reponse.getIndustry1());
            reserveField1.put("establish_time", reponse.getEstablish_time());
            reserveField1.put("contact_verification_flag", reponse.getContact_verification_flag());
            reserveField1.put("business_status", reponse.getBusiness_status());
            reserveField1.put("address_verification_time", reponse.getAddress_verification_time());
            reserveField1.put("industry3", reponse.getIndustry3());
            reserveField1.put("industry2", reponse.getIndustry2());
            reserveField1.put("paid_in_capital", reponse.getPaid_in_capital());
            reserveField1.put("registered_capital", reponse.getRegistered_capital());
            //reserveField1.put("name", reponse.getLegal_person_name());
            reserveField1.put("company_name", reponse.getCompany_name());
            reserveField1.put("district", reponse.getDistrict());
            reserveField1.put("address_verification_flag", reponse.getAddress_verification_flag());
            reserveField1.put("aoi_type", reponse.getAoi_type());
            reserveField1.put("credit_code", reponse.getCredit_code());
            if (!CollectionUtils.isEmpty(moreCellList)) {
                for (int i = 0; i < moreCellList.size(); i++) {
                    reserveField1.put("cell".concat(String.valueOf(i + 1)), AESUtil.decrypt(moreCellList.get(i), aesKey));
                }
            }
            detailDTO.setReserveField1(reserveField1.toJSONString());
            dataItems.add(detailDTO);
        });
        marketingPreUserDTO.setRequestId(apiCode + System.currentTimeMillis() + UUID.randomUUID());
        marketingPreUserDTO.setDataItems(dataItems);
        marketingPreUserDTO.setTaskId(apiCode + "_" + LocalDate.now());
    }

    private String getShunfengToken() {
        String redisKey = RedisKeyConstant.SHUNFENG_GET_TOKEN_KEY;
        String redisKeyLock = RedisKeyConstant.SHUNFENG_GET_TOKEN_KEY_LOCK;
        String value = UUID.randomUUID().toString();
        String token = null;
        try {
            token = redisChgService.get(redisKey);
            if (StringUtils.isNotEmpty(token)) {
                return token;
            } else {
                redisChgService.lock(redisKeyLock, value);
                //获取锁成功，（多线程处理时）再查一遍
                token = redisChgService.get(redisKey);
                if (StringUtils.isNotEmpty(token)) {
                    return token;
                }
                //调用获取token接口
                while (Boolean.TRUE) {
                    Result<JSONObject> result = shunfengClient.getToken();
                    if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        token = result.getData().getString("accessToken");
                        Integer expire = result.getData().getInteger("expiresIn");
                        //写入redis，防止token时间过短
                        if (expire <= 3) {
                            try {
                                Thread.sleep(500);
                            } catch (Exception e) {
                                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode(), "顺丰获取token线程sleep异常!"), e);
                            }
                            continue;
                        }
                        redisChgService.setex(redisKey, token, expire - 3);
                        break;
                    } else {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode()
                                , "顺丰获取token调用异常,result= " + result.getMessage()));
                        break;
                    }
                }
                redisChgService.unlock(redisKeyLock, value);
            }
        } catch (Exception e) {
            redisChgService.unlock(redisKeyLock, value);
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUNFENG_SERVICEERROR.getCode(), "顺丰获取token程序异常!"), e);
        }
        return token;
    }

    private void modifyCorePoolSize(ThreadPoolExecutor pool) {
        Integer threadNum =
                marketingCommonConfig.getShunFengCompanyThreadNum();
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(pool, threadNum);
        log.warn("顺丰获取企业信息线程数core={}，max={}", pool.getCorePoolSize(), pool.getMaximumPoolSize());

    }
}

