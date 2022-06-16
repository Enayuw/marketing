import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.br.common.util.AESAlgorithmUtil;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.ScoreRuleConfig;
import com.br.marketing.entity.SyncConfig;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.mapper.ScoreRuleConfigMapper;
import com.br.marketing.mapper.SyncConfigMapper;
import com.br.marketing.service.Impl.ScoreRuleConfigServiceImpl;
import com.br.marketing.service.ScoreRuleConfigService;
import com.br.marketing.task.Scheduler;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by Bairong on 2019/8/19.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Scheduler.class)
@WebAppConfiguration
public class MarketingUserDBTest {
    @Autowired
    MarketingUserMapper marketingUserMapper;

    @Autowired
    MarketingTaskMapper marketingTaskMapper;
    @Autowired
    SyncConfigMapper loanSyncConfigMapper;

    @Test
    public void testQueryUser(){
        marketingUserMapper.createUserTable("b_marketing_user_4200333");
    }
    @Test
    public void testTask(){
        List<MarketingTask> list= marketingTaskMapper.queryBatchNum();
        JSONArray array = JSONArray.parseArray(JSON.toJSONString(list));
        System.out.println(array);

        Integer totalNum = marketingUserMapper.getTotalNum(list);
        System.out.println(totalNum);
    }
    @Test
    public void test(){
        SyncConfig loanSyncConfig=new SyncConfig();
        loanSyncConfig.setApiCode("7410105");
        loanSyncConfig.setSrcPath("/loanwarn/7410105/output/yyyy-MM-dd/");
        loanSyncConfig.setTargetPath("/yanfa_qa/spft_dz_get/7410105");
        loanSyncConfig.setSuffix(".zip,.success,.finish");
        loanSyncConfig.setCheckFinish(1);
        loanSyncConfig.setCheckSuccess(1);
        loanSyncConfig.setStatus(1);
        loanSyncConfig.setRemark("通用测试，客户目录》》内部目录");
        loanSyncConfig.setSrcSftpHost("10.100.123.65");
        loanSyncConfig.setSrcSftpPort(9999);
        loanSyncConfig.setSrcSftpUser("loan_warning_pre_qa");
        loanSyncConfig.setSrcSftpPwd(AESAlgorithmUtil.encrypt("ZXL3DkzZjtNsd0rO3T2w@_2020.11.05", Constants.SFTP_P_SECRET_KEY));
        loanSyncConfig.setTargetSftpHost("10.100.123.65");
        loanSyncConfig.setTargetSftpPort(9999);
        loanSyncConfig.setTargetSftpUser("loan_warning_pre");
        loanSyncConfig.setTargetSftpPwd(AESAlgorithmUtil.encrypt("5Ge89rr8TKvVEiHvmrSL@_2020.11.05",Constants.SFTP_P_SECRET_KEY));
        loanSyncConfigMapper.insertConfig(loanSyncConfig);
    }
    @Test
    public void testQuery(){
       List<SyncConfig> list= loanSyncConfigMapper.queryConfig("1");
       for(SyncConfig loanSyncConfig:list){
           String srcSftpPwd = AESAlgorithmUtil.decrypt(loanSyncConfig.getSrcSftpPwd(), Constants.SFTP_P_SECRET_KEY);
           String targetSftpPwd = AESAlgorithmUtil.decrypt(loanSyncConfig.getTargetSftpPwd(), Constants.SFTP_P_SECRET_KEY);
           System.out.println(srcSftpPwd+"------"+targetSftpPwd);
       }
    }

    @Resource
    ScoreRuleConfigMapper scoreRuleConfigMapper;

    @Test
    public void testQueryRule(){
        ScoreRuleConfig ruleConfig = scoreRuleConfigMapper.selectByPrimaryKey(360004L);
        System.out.println(JSON.toJSONString(ruleConfig));
    }

}
