package com.br.marketing.entity;

import lombok.Data;

import java.io.Serializable;

/**
* @Description:携程黑名单剔除量级记录
* @Author: Ethan.Kang
*/
@Data
public class XieChengBlackListDeleteNumber implements Serializable{
    private int cycPublicBlackListCount;
    private int robPublicBlackListCount ;
    private int cycBlackListZYCount ;
    private int cycBlackListBYCount ;
    private int robBlackListZYCount ;
    private int robBlackListBYCount;

}
