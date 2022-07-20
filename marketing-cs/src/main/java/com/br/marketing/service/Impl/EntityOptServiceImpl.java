package com.br.marketing.service.Impl;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.enums.TableCodeEnum;
import com.br.marketing.entity.EntityOptLog;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.EntityOptLogMapper;
import com.br.marketing.service.EntityOptService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class EntityOptServiceImpl implements EntityOptService {

    @Resource
    EntityOptLogMapper entityOptLogMapper;

    public enum EnumLogCRUD {
        Create(1), Update(2);
        public int Value;

        private EnumLogCRUD(int value) {
            this.Value = value;
        }
    }

    @Override
    public <T> void saveOpt(T entity, MarketingUserDetail user, TableCodeEnum tableCodeEnum) {
        Field id = null;
        try {
            id = entity.getClass().getDeclaredField("id");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        id.setAccessible(true);
        String s = null;
        try {
            s = String.valueOf(id.get(entity));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        EntityOptLog log = new EntityOptLog();
        log.setSourceId(s);
        log.setSourceObj(tableCodeEnum.getTableName());
        log.setSourceEntity(tableCodeEnum.getTableEntity());
        log.setContent(JSON.toJSONString(entity));
        log.setOptType(EnumLogCRUD.Create.Value);
        log.setDesc("");
        log.setOptUserId(user.getId().toString());
        log.setOptUserName(user.getUserName());
        log.setCreateTime(new Date());
        entityOptLogMapper.insertSelective(log);

    }

    @Override
    public <T> void updateOpt(T newEntity, T oleEntity, MarketingUserDetail user, TableCodeEnum tableCodeEnum) {
//        Class c = newEntity.getClass();
//        List<Field> fields =getOpenFields(c);
//        Date date = new Date();
//        try {
//            List<LogEntityChange> optLogList=new ArrayList<>();
//            for (Field f : fields) {
//                f.setAccessible(true);
//                Object newValue = f.get(newObj) == null ? null : f.get(newObj);
//                Object oldValue = null;
//                //修改情況下，值相等。不记录
//                if (oldObj != null) {
//                    oldValue = f.get(oldObj) == null ? null : f.get(oldObj);
//                    //都是null值不记录
//                    if (isNullCase(newValue)) {
//                        continue;
//                    }
//                    //两者皆不为空，判断具体值
//                    if (newValue != null && oldValue != null ) {
//                        if( newValue.equals(oldValue))
//                        {
//                            continue;
//                        }
//                    }
//                    //一个为空，一个不为空需要记录
//                }
//                //新增的情况下不记录空值字段
//                if(logType== EnumLogCRUD.Create&&isNullCase(newValue))
//                {
//                    continue;
//                }
//
//                LogEntityChange lec = new LogEntityChange();
//                lec.setNewValue(newValue==null?null:newValue.toString());
//                lec.setLogCRUD(logType.Value);
//                lec.setInsertTime(date);
//                IOptUser iOptUser=optContext.getOptUser();
//                if(iOptUser.getUserDataID()!=null){
//                    lec.setOperatorUserID(Long.valueOf(iOptUser.getUserDataID()));
//                }else  if(iOptUser.getUserID()!=null) {
//                    lec.setOperatorUserID(iOptUser.getUserID());
//                }
//                if(iOptUser.getRoleID()!=null) {
//                    lec.setOperatorRoleID(iOptUser.getRoleID());
//                }
//                if(iOptUser.getRoleDeptID()!=null) {
//                    lec.setOperatorRoleDeptID(iOptUser.getRoleDeptID());
//                }
//                lec.setField(f.getName());
//                lec.setTypeName(c.getName());
//                lec.setObjectID(objID.toString());
//                if(optContext.getOriDesc()!=null)
//                {
//                    lec.setOptDesc(optContext.getOriDesc()+"-"+optDesc);
//                }else
//                {
//                    lec.setOptDesc("-"+optDesc);
//                }
//
//                lec.setGUID(optContext.getUUID());
//                if (oldObj != null) {
//                    lec.setOldValue(oldValue==null?null:oldValue.toString());
//                }
//                optLogList.add(lec);
//            }
//            if(!optLogList.isEmpty()) {
//                logMapper.insertBatch(optLogList);
//            }
//        }catch (Exception ee)
//        {
//            logger.error("记录实体变更日志发生异常",ee);
//            throw  new RuntimeException("记录实体变更日志发生异常");
//        }
    }


    private  static List<Field> getOpenFields(Class cl){
        List<Field> fields=new ArrayList<>();
        //添加类自己定义的所有field 。public，protect,private
        fields.addAll(Arrays.asList(cl.getDeclaredFields()));
        Class superClass=cl.getSuperclass();
        while (superClass !=null && !"java.lang.object".equals(superClass.getName().toLowerCase()) )
        {
            //添加父类public,protect
            fields.addAll(Arrays.asList(superClass.getFields()));
            superClass=superClass.getSuperclass();
        }
        return fields;
    }
}
