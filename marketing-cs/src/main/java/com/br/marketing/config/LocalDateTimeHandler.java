package com.br.marketing.config;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.time.LocalDateTime;

@MappedJdbcTypes(JdbcType.TIMESTAMP)
@MappedTypes(LocalDateTime.class)
public class LocalDateTimeHandler extends BaseTypeHandler<LocalDateTime> {
    @Override public void setNonNullParameter(PreparedStatement ps, int i, LocalDateTime p, JdbcType t) throws SQLException {
        ps.setTimestamp(i, Timestamp.valueOf(p));
    }
    @Override public LocalDateTime getNullableResult(ResultSet rs, String c) throws SQLException {
        Timestamp ts = rs.getTimestamp(c);
        return ts == null ? null : ts.toLocalDateTime();
    }
    @Override public LocalDateTime getNullableResult(ResultSet rs, int i) throws SQLException {
        Timestamp ts = rs.getTimestamp(i);
        return ts == null ? null : ts.toLocalDateTime();
    }
    @Override public LocalDateTime getNullableResult(CallableStatement cs, int i) throws SQLException {
        Timestamp ts = cs.getTimestamp(i);
        return ts == null ? null : ts.toLocalDateTime();
    }
}
