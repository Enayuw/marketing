package com.br.marketing.service.Impl;

import java.lang.reflect.Field;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.enums.TableCodeEnum;
import com.br.marketing.context.OptUser;
import com.br.marketing.entity.EntityOptLog;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.EntityOptLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Service
@Slf4j
public class EntityOptServiceImpl{

    @Autowired
    OptUser optUser;

    private static HashMap<String,TableCodeEnum> tableHm;

    @Resource
    EntityOptLogMapper entityOptLogMapper;

    public enum EnumLogCRUD {
        Create(1), Update(2);
        public int Value;

        private EnumLogCRUD(int value) {
            this.Value = value;
        }
    }

    @PostConstruct
    void init(){
        tableHm = new HashMap<>();
        for (TableCodeEnum value : TableCodeEnum.values()) {
            tableHm.put(value.getTableEntity(),value);
        }
    }

    private <T> void saveOpt(Long id,T entity, MarketingUserDetail user, TableCodeEnum tableCodeEnum) {
        EntityOptLog log = new EntityOptLog();
        log.setSourceId(id.toString());
        log.setSourceObj(tableCodeEnum.getTableName());
        log.setSourceEntity(tableCodeEnum.getTableEntity());
        log.setContent(JSON.toJSONString(entity));
        log.setOptType(EnumLogCRUD.Create.Value);
        log.setOptUserId(user.getId().toString());
        log.setOptUserName(user.getUserName());
        log.setCreateTime(new Date());
        entityOptLogMapper.insertSelective(log);

    }

    private <T> void updateOpt(Long id,T newEntity, T oldEntity, MarketingUserDetail user, TableCodeEnum tableCodeEnum) {
        Class c = newEntity.getClass();
        List<Field> fields = getOpenFields(c);
        Date date = new Date();
            StringBuilder info = new StringBuilder();
            for (Field f : fields) {
                f.setAccessible(true);
                Object newValue = null;
                try {
                    newValue = f.get(newEntity) == null ? null : f.get(newEntity);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                Object oldValue = null;
                //修改情況下，值相等。不记录
                if (oldEntity != null) {
                    try {
                        oldValue = f.get(oldEntity) == null ? null : f.get(oldEntity);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    //新值为null不记录
                    if (newValue == null) {
                        continue;
                    }
                    //两者皆不为空，判断具体值
                    if (newValue != null && oldValue != null) {
                        if (!newValue.equals(oldValue)) {
                            info.append(String.format("【%s】=【%s】->【%s】\r\n"
                                    , f.getName()
                                    , oldValue.toString()
                                    , newValue.toString()));
                        }
                    }

                    //一个为空，一个不为空需要记录
                    if (newValue != null && oldValue == null) {
                        info.append(String.format("【%s】=【null】->【%s】\r\n"
                                , f.getName()
                                , newValue.toString()));
                    }
                }
            }
            EntityOptLog log = new EntityOptLog();
            log.setSourceId(id.toString());
            log.setSourceObj(tableCodeEnum.getTableName());
            log.setSourceEntity(tableCodeEnum.getTableEntity());
            log.setContent(info.toString());
            log.setOptType(EnumLogCRUD.Update.Value);
            log.setOptUserId(user.getId().toString());
            log.setOptUserName(user.getUserName());
            log.setCreateTime(new Date());
            entityOptLogMapper.insertSelective(log);
    }


    /**
     * 操作日志 oldENtity 为null 记录为新建日志 否则是修改日志
     * @param id 当前操作对象的id
     * @param newEntity 修改后的对象
     * @param oldEntity 修改前的对象
     * @param <T>
     */
    public <T> void writeOptLog(Long id,T newEntity, T oldEntity){
        String simpleName = newEntity.getClass().getSimpleName();
        TableCodeEnum tableCodeEnum = tableHm.get(simpleName);
        if(tableCodeEnum == null){
            return;
        }
        if(oldEntity == null){
            saveOpt(id,newEntity,optUser.getUserDetail(),tableCodeEnum);
        }else{
            updateOpt(id,newEntity,oldEntity,optUser.getUserDetail(),tableCodeEnum);
        }
    }

    /**
     * 操作日志 oldENtity 为null 记录为新建日志 否则是修改日志
     * @param id 当前操作对象的id
     * @param newEntity 修改后的对象
     * @param oldEntity 修改前的对象
     * @param marketingUserDetail 操作人信息
     * @param <T>
     */
    public <T> void writeOptLog(Long id,T newEntity, T oldEntity,MarketingUserDetail marketingUserDetail){
        String simpleName = newEntity.getClass().getSimpleName();
        TableCodeEnum tableCodeEnum = tableHm.get(simpleName);
        if(tableCodeEnum == null){
            return;
        }
        if(oldEntity == null){
            saveOpt(id,newEntity,marketingUserDetail,tableCodeEnum);
        }else{
            updateOpt(id,newEntity,oldEntity,marketingUserDetail,tableCodeEnum);
        }
    }

    private static List<Field> getOpenFields(Class cl) {
        List<Field> fields = new ArrayList<>();
        //添加类自己定义的所有field 。public，protect,private
        fields.addAll(Arrays.asList(cl.getDeclaredFields()));
        Class superClass = cl.getSuperclass();
        while (superClass != null && !"java.lang.object".equals(superClass.getName().toLowerCase())) {
            //添加父类public,protect
            fields.addAll(Arrays.asList(superClass.getFields()));
            superClass = superClass.getSuperclass();
        }
        return fields;
    }
}
