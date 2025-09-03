package com.br.marketing.service.rulecenter.impl.esquery;

import com.br.marketing.es.bean.MarketingHistory;
import com.br.marketing.es.bean.QueryBaseBean;

import java.util.List;
/**
 * ES查询结果包装类
 */
public class EsQueryResult {

    private boolean success;
    private List<MarketingHistory> marketingHistories;
    private String searchAfter;
    private Exception exception;
    private QueryBaseBean queryBaseBean;

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<MarketingHistory> getMarketingHistories() {
        return marketingHistories;
    }

    public void setMarketingHistories(List<MarketingHistory> marketingHistories) {
        this.marketingHistories = marketingHistories;
    }

    public String getSearchAfter() {
        return searchAfter;
    }

    public void setSearchAfter(String searchAfter) {
        this.searchAfter = searchAfter;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public QueryBaseBean getQueryBaseBean() {
        return queryBaseBean;
    }

    public void setQueryBaseBean(QueryBaseBean queryBaseBean) {
        this.queryBaseBean = queryBaseBean;
    }

}
