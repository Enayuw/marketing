package com.br.marketing.check.service;

import com.br.marketing.entity.PushCustomerDetail;
import com.br.marketing.entity.ScorePushCustomerConfig;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.vo.scorepushcustomer.ScoreSortJsonVO;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public interface PushCallBackService {
    void pushCustomer(StraHisFile straHisFile, List<ScoreSortJsonVO> vos, AtomicInteger error, ScorePushCustomerConfig scorePushCustomerConfig);

    default String getScoreSortByDb(Integer dbNumber, PushCustomerDetail detail) {
        switch (dbNumber) {
            case 0:
                return detail.getScoreSort1();
            case 1:
                return detail.getScoreSort2();
            case 2:
                return detail.getScoreSort3();
            case 3:
                return detail.getScoreSort4();
            default:
                return "";
        }
    }

    default void waitThreadPool(ThreadPoolExecutor executor) throws InterruptedException {
        executor.shutdown();
        while (true) {
            if (executor.isTerminated()) {
                break;
            }
            try {
                Thread.sleep(6000);
            } catch (Exception e) {
                throw e;
            }
        }
    }
}
