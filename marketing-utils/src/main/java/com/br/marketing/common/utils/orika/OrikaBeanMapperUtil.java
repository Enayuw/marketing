package com.br.marketing.common.utils.orika;

import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.metadata.ClassMapBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class OrikaBeanMapperUtil {
    private static final MapperFacade MAPPER_FACADE;

    static {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().useAutoMapping(true).mapNulls(true).build();
        MAPPER_FACADE = mapperFactory.getMapperFacade();
    }

    public static <S, D> void map(S from, D to) {
        MAPPER_FACADE.map(from, to);
    }

    public static <S, D> D map(S from, Class<D> clazz) {
        return MAPPER_FACADE.map(from, clazz);
    }

    public static <S, D> D map(S from, Class<S> source, Class<D> target, List<MappingItem> itemList) {
        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
        ClassMapBuilder<S, D> classMapBuilder = mapperFactory.classMap(source, target);
        for(MappingItem item : itemList){
            if(StringUtils.isEmpty(item.getFrom()) || StringUtils.isEmpty(item.getTo())){
                continue;
            }
            classMapBuilder.field(item.getFrom(), item.getTo());
        }
        classMapBuilder.byDefault().register();

        MapperFacade mapperFacade = mapperFactory.getMapperFacade();
        D to = mapperFacade.map(from, target);
        return to;
    }

    public static MapperFacade getMapperFacade() {
        return MAPPER_FACADE;
    }

    public static <S, D> List<D> mapAsList(Iterable<S> source, Class<D> destinationClass) {
        return MAPPER_FACADE.mapAsList(source, destinationClass);
    }
}