package com.br.marketing.innerapi.bean;

import com.github.pagehelper.PageInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * 分页数据
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/8/30 17:52
 */
@Setter
@Getter
@NoArgsConstructor
public class PageResult implements Serializable {
    private int page;            // 当前页数
    private int total;            // 总页数
    private long records;        // 总记录数
    private List<?> rows;        // 每行显示的内容

    //分页数据进行封装到PageResult类，传给前端
    public static PageResult setPageResult(List<?> list, Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PageResult pageResult = new PageResult();
        pageResult.setPage(page);
        pageResult.setRows(list);
        pageResult.setTotal(pageList.getPages());
        pageResult.setRecords(pageList.getTotal());
        return pageResult;
    }
}
