package com.br.marketing.commonentity;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "分页数据")
public class PageResultReturn implements Serializable {
    @ApiModelProperty(value = "当前页")
    private int page;
    @ApiModelProperty(value = "总页数", position = 1)
    private int total;
    @ApiModelProperty(value = "总记录数", position = 2)
    private long records;
    @ApiModelProperty(value = "结果集", position = 3)
    private List<?> rows;

    //分页数据进行封装到PageResultReturn
    public static PageResultReturn setPageResult(List<?> list, Integer page) {
        PageInfo<?> pageList = new PageInfo<>(list);
        PageResultReturn pageResultReturn = new PageResultReturn();
        pageResultReturn.setPage(page);
        pageResultReturn.setRows(list);
        pageResultReturn.setTotal(pageList.getPages());
        pageResultReturn.setRecords(pageList.getTotal());
        return pageResultReturn;
    }
}
