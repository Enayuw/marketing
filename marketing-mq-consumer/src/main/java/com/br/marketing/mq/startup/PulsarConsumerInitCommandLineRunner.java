package com.br.marketing.mq.startup;

import javax.annotation.Resource;

import com.br.marketing.api.customer.upload.service.CustomerUploadDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import com.br.marketing.api.customer.transfer.service.CustomerTransferDataService;
import com.br.marketing.common.constants.PulsarSubscription;
import com.br.marketing.common.constants.PulsarTopic;
import com.br.marketing.service.IPushShuheDataService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.Impl.ConsumerService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PulsarConsumerInitCommandLineRunner implements CommandLineRunner {

    @Resource
    private ConsumerService consumerService;

    @Resource
    private PushRuleService pushRuleService;

    @Resource
    private IPushShuheDataService pushShuheDataService;

    @Resource
    private CustomerTransferDataService customerTransferDataService;

    @Resource
    private CustomerUploadDataService customerUploadDataService;


    /**
     * Callback used to run the bean.
     *
     * @param args incoming main method arguments
     * @throws Exception on error
     */
    @Override
    public void run(String... args) throws Exception {
        // 标准上传数据pulsar消费端
        consumerService.consumerPulsar(PulsarSubscription.upLoadSubscription, pushRuleService::consumerSyncInfo, 2, PulsarTopic.upLoadTopic);

        // 数禾上传数据pulsar消费端
        consumerService.consumerPulsar(PulsarSubscription.upLoadShSubscription, pushShuheDataService::consumerShUpload, 2, PulsarTopic.upLoadShTopic);

        // 标准转化数据pulsar消费端
        consumerService.consumerPulsar(PulsarSubscription.transferSubscription, pushRuleService::consumerTransferInfo, 2, PulsarTopic.transferTopic);

        // 数禾转化数据pulsar消费端
        consumerService.consumerPulsar(PulsarSubscription.transferShSubscription, pushShuheDataService::consumerShTransfer, 2,
            PulsarTopic.transferShTopic);

        // 定制客户转化数据pulsar消费端
        consumerService.consumerPulsar(PulsarSubscription.transferCustomSubscription, customerTransferDataService::consumerTransferPayData, 2,
            PulsarTopic.transferCustomTopic);

        // 定制客户上传数据pulsar消费端
        consumerService.consumerPulsar(PulsarSubscription.uploadCustomSubscription, customerUploadDataService::consumerUploadPayData, 2,
                                       PulsarTopic.uploadCustomTopic);
    }
}
