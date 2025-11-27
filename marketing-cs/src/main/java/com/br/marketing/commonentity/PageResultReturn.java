package com.br.marketing.commonentity;

import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "分页数据")
public class PageResultReturn<T> implements Serializable {

    @Schema(description = "当前页")
    private int current;
    @Schema(description = "总记录数")
    private long total;
    @Schema(description = "每页条数")
    private int size;
    @Schema(description = "结果集")
    private List<T> records;

    //分页数据进行封装到PageResult
    public static <T> PageResultReturn setPageResult(List<T> list, Integer page, Integer pageSize) {
        PageInfo<T> pageList = new PageInfo<>(list);
        PageResultReturn pageResultReturn = new PageResultReturn();
        pageResultReturn.setCurrent(page);
        pageResultReturn.setRecords(list);
        pageResultReturn.setTotal(pageList.getTotal());
        pageResultReturn.setSize(pageSize);
        return pageResultReturn;
    }

    public static <T> PageResultReturn setPageResult(List<T> list, Integer page, Integer pageSize, Long total) {
        PageInfo<T> pageList = new PageInfo<>(list);
        PageResultReturn pageResultReturn = new PageResultReturn();
        pageResultReturn.setCurrent(page);
        pageResultReturn.setRecords(list);
        pageResultReturn.setTotal(total);
        pageResultReturn.setSize(pageSize);
        return pageResultReturn;
    }

    //分页数据进行封装到PageResultReturn
    public static <T> PageResultReturn setPageResult(List<T> list, Integer page) {
        PageInfo<T> pageList = new PageInfo<>(list);
        PageResultReturn pageResultReturn = new PageResultReturn();
        pageResultReturn.setCurrent(page);
        pageResultReturn.setRecords(list);
        pageResultReturn.setTotal(pageList.getTotal());
        return pageResultReturn;
    }
}
