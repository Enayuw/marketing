package com.br.marketing.service.rulecenter.impl.esquery;

import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.entity.ErrorMark;

import java.util.List;

/**
 * ES查询参数类
 * 封装所有查询参数和状态，确保线程安全
 */
public class EsQueryParams {
    
    // 查询参数
    private CustomerInfoPushMain customerInfoPushMain;
    private String part;
    private List<String> numList;
    private List<Long> fileIds;
    private Integer pageSize;
    private Integer totalPage;
    private Boolean isPerOrTop;
    private Object labelObject;
    private Boolean markWithEsFlag;
    
    // 状态变量
    private ErrorMark errorMark = new ErrorMark();
    private int startPageIndex = 1;
    private String searchAfterStr = "";
    
    // 构造函数
    public EsQueryParams(CustomerInfoPushMain customerInfoPushMain,
                        String part,
                        List<String> numList,
                        List<Long> fileIds,
                        Integer pageSize,
                        Integer totalPage,
                        Boolean isPerOrTop,
                        Object labelObject,
                        Boolean markWithEsFlag) {
        this.customerInfoPushMain = customerInfoPushMain;
        this.part = part;
        this.numList = numList;
        this.fileIds = fileIds;
        this.pageSize = pageSize;
        this.totalPage = totalPage;
        this.isPerOrTop = isPerOrTop;
        this.labelObject = labelObject;
        this.markWithEsFlag = markWithEsFlag;
    }
    
    // Getter和Setter方法
    public CustomerInfoPushMain getCustomerInfoPushMain() {
        return customerInfoPushMain;
    }
    
    public void setCustomerInfoPushMain(CustomerInfoPushMain customerInfoPushMain) {
        this.customerInfoPushMain = customerInfoPushMain;
    }
    
    public String getPart() {
        return part;
    }
    
    public void setPart(String part) {
        this.part = part;
    }
    
    public List<String> getNumList() {
        return numList;
    }
    
    public void setNumList(List<String> numList) {
        this.numList = numList;
    }
    
    public List<Long> getFileIds() {
        return fileIds;
    }
    
    public void setFileIds(List<Long> fileIds) {
        this.fileIds = fileIds;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    
    public Integer getTotalPage() {
        return totalPage;
    }
    
    public void setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
    }
    
    public Boolean getIsPerOrTop() {
        return isPerOrTop;
    }
    
    public void setIsPerOrTop(Boolean isPerOrTop) {
        this.isPerOrTop = isPerOrTop;
    }
    
    public Object getLabelObject() {
        return labelObject;
    }
    
    public void setLabelObject(Object labelObject) {
        this.labelObject = labelObject;
    }
    
    public Boolean getMarkWithEsFlag() {
        return markWithEsFlag;
    }
    
    public void setMarkWithEsFlag(Boolean markWithEsFlag) {
        this.markWithEsFlag = markWithEsFlag;
    }
    
    // 状态变量的getter和setter
    public ErrorMark getErrorMark() {
        return errorMark;
    }
    
    public void setErrorMark(ErrorMark errorMark) {
        this.errorMark = errorMark;
    }
    
    public int getStartPageIndex() {
        return startPageIndex;
    }
    
    public void setStartPageIndex(int startPageIndex) {
        this.startPageIndex = startPageIndex;
    }
    
    public String getSearchAfterStr() {
        return searchAfterStr;
    }
    
    public void setSearchAfterStr(String searchAfterStr) {
        this.searchAfterStr = searchAfterStr;
    }
}
