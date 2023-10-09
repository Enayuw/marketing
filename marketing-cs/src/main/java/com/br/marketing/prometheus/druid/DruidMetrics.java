package com.br.marketing.prometheus.druid;

import org.apache.tomcat.jni.Local;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DruidMetrics {

    /**
     * Prefix used for all Druid metric names.
     */
    private static final String DRUID_METRIC_NAME_PREFIX = "druid";
    private static final String METRIC_CATEGORY = "pool";

    /**
     * DataSource
     */
    public static final String METRIC_NAME_INITIAL_SIZE = DRUID_METRIC_NAME_PREFIX + ".initial.size";
    public static final String METRIC_NAME_MIN_IDLE = DRUID_METRIC_NAME_PREFIX + ".min.idle";
    public static final String METRIC_NAME_MAX_ACTIVE = DRUID_METRIC_NAME_PREFIX + ".max.active";
    public static final String METRIC_NAME_MAX_WAIT = DRUID_METRIC_NAME_PREFIX + ".max.wait";
    public static final String METRIC_NAME_MAX_WAIT_THREAD_COUNT = DRUID_METRIC_NAME_PREFIX + ".max.wait.thread.count";
    public static final String METRIC_NAME_MAX_POOL_PREPARED_STATEMENT_PER_CONNECTION_SIZE = DRUID_METRIC_NAME_PREFIX + ".max.pool.prepared.statement.per.connection.size";
    public static final String METRIC_NAME_MAX_OPEN_PREPARED_STATEMENTS = DRUID_METRIC_NAME_PREFIX + ".max.open.prepared.statements";
    public static final String METRIC_NAME_LOGIN_TIMEOUT = DRUID_METRIC_NAME_PREFIX + ".login.timeout";
    public static final String METRIC_NAME_QUERY_TIMEOUT = DRUID_METRIC_NAME_PREFIX + ".query.timeout";
    public static final String METRIC_NAME_TRANSACTION_QUERY_TIMEOUT = DRUID_METRIC_NAME_PREFIX + ".transaction.query.timeout";
    public static final String METRIC_NAME_TRANSACTION_THRESHOLD_MILLIS = DRUID_METRIC_NAME_PREFIX + ".transaction.threshold.millis";
    public static final String METRIC_NAME_VALIDATION_QUERY_TIMEOUT = DRUID_METRIC_NAME_PREFIX + ".validation.query.timeout";
    public static final String METRIC_NAME_ACTIVE_COUNT = DRUID_METRIC_NAME_PREFIX + ".active.count";
    public static final String METRIC_NAME_ACTIVE_PEAK = DRUID_METRIC_NAME_PREFIX + ".active.peak";
    public static final String METRIC_NAME_POOLING_COUNT = DRUID_METRIC_NAME_PREFIX + ".pooling.count";
    public static final String METRIC_NAME_POOLING_PEAK = DRUID_METRIC_NAME_PREFIX + ".pooling.peak";
    public static final String METRIC_NAME_WAIT_THREAD_COUNT = DRUID_METRIC_NAME_PREFIX + ".wait.thread.count";
    public static final String METRIC_NAME_NOT_EMPTY_WAIT_COUNT = DRUID_METRIC_NAME_PREFIX + ".not.empty.wait.count";
    public static final String METRIC_NAME_NOT_EMPTY_WAIT_MILLIS = DRUID_METRIC_NAME_PREFIX + ".not.empty.wait.millis";
    public static final String METRIC_NAME_NOT_EMPTY_THREAD_COUNT = DRUID_METRIC_NAME_PREFIX + ".not.empty.thread.count";
    public static final String METRIC_NAME_LOGIC_CONNECT_COUNT = DRUID_METRIC_NAME_PREFIX + ".logic.connect.count";
    public static final String METRIC_NAME_LOGIC_CLOSE_COUNT = DRUID_METRIC_NAME_PREFIX + ".logic.close.count";
    public static final String METRIC_NAME_LOGIC_CONNECT_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + ".logic.connect.error.count";
    public static final String METRIC_NAME_PHYSICAL_CONNECT_COUNT = DRUID_METRIC_NAME_PREFIX + ".physical.connect.count";
    public static final String METRIC_NAME_PHYSICAL_CLOSE_COUNT = DRUID_METRIC_NAME_PREFIX + ".physical.close.count";
    public static final String METRIC_NAME_PHYSICAL_CONNECT_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + ".physical.connect.error.count";
    public static final String METRIC_NAME_EXECUTE_COUNT = DRUID_METRIC_NAME_PREFIX + ".execute.count";
    public static final String METRIC_NAME_EXECUTE_QUERY_COUNT = DRUID_METRIC_NAME_PREFIX + ".execute.query.count";
    public static final String METRIC_NAME_EXECUTE_UPDATE_COUNT = DRUID_METRIC_NAME_PREFIX + ".execute.update.count";
    public static final String METRIC_NAME_EXECUTE_BATCH_COUNT = DRUID_METRIC_NAME_PREFIX + ".execute.batch.count";
    public static final String METRIC_NAME_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + ".error.count";
    public static final String METRIC_NAME_COMMIT_COUNT = DRUID_METRIC_NAME_PREFIX + ".commit.count";
    public static final String METRIC_NAME_ROLLBACK_COUNT = DRUID_METRIC_NAME_PREFIX + ".rollback.count";
    public static final String METRIC_NAME_PSCACHE_ACCESS_COUNT = DRUID_METRIC_NAME_PREFIX + ".ps.cache.access.count";
    public static final String METRIC_NAME_PSCACHE_HIT_COUNT = DRUID_METRIC_NAME_PREFIX + ".ps.cache.hit.count";
    public static final String METRIC_NAME_PSCACHE_MISS_COUNT = DRUID_METRIC_NAME_PREFIX + ".ps.cache.miss.count";
    public static final String METRIC_NAME_PREPARED_STATEMENT_OPEN_COUNT = DRUID_METRIC_NAME_PREFIX + ".prepared.statement.open.count";
    public static final String METRIC_NAME_PREPARED_STATEMENT_CLOSED_COUNT = DRUID_METRIC_NAME_PREFIX + ".prepared.statement.closed.count";
    public static final String METRIC_NAME_RESULTSET_OPEN_COUNT = DRUID_METRIC_NAME_PREFIX + ".resultset.open.count";
    public static final String METRIC_NAME_RESULTSET_OPENING_COUNT = DRUID_METRIC_NAME_PREFIX + ".resultset.opening.count";
    public static final String METRIC_NAME_RESULTSET_OPENING_MAX = DRUID_METRIC_NAME_PREFIX + ".resultset.opening.max";
    public static final String METRIC_NAME_RESULTSET_CLOSE_COUNT = DRUID_METRIC_NAME_PREFIX + ".resultset.close.count";
    public static final String METRIC_NAME_RESULTSET_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + ".resultset.error.count";
    public static final String METRIC_NAME_RESULTSET_FETCH_ROW_COUNT = DRUID_METRIC_NAME_PREFIX + ".resultset.fetch.row.count";
    public static final String METRIC_NAME_START_TRANSACTION_COUNT = DRUID_METRIC_NAME_PREFIX + ".start.transaction.count";
    public static final String METRIC_NAME_TRANSACTION_COUNT = DRUID_METRIC_NAME_PREFIX + ".transaction.count";
    public static final String METRIC_NAME_CONNECTION_HOLD_TIME_MILLIS = DRUID_METRIC_NAME_PREFIX + ".connection.hold.time.millis";
    public static final String METRIC_NAME_CONNECTION_HOLD_TIME_MILLIS_MIN = DRUID_METRIC_NAME_PREFIX + ".connection.hold.time.millis.min";
    public static final String METRIC_NAME_CONNECTION_HOLD_TIME_MILLIS_MAX = DRUID_METRIC_NAME_PREFIX + ".connection.hold.time.millis.max";
    public static final String METRIC_NAME_REMOVE_ABANDONED_COUNT = DRUID_METRIC_NAME_PREFIX + ".remove.abandoned.count";
    public static final String METRIC_NAME_CLOB_OPEN_COUNT = DRUID_METRIC_NAME_PREFIX + ".clob.open.count";
    public static final String METRIC_NAME_BLOB_OPEN_COUNT = DRUID_METRIC_NAME_PREFIX + ".blob.open.count";

    public static final String METRIC_NAME_CONNECTION_ACTIVE_COUNT = DRUID_METRIC_NAME_PREFIX + ".connection.active.count";
    public static final String METRIC_NAME_CONNECTION_CONNECT_ALIVE_MILLIS = DRUID_METRIC_NAME_PREFIX + ".connection.connect.alive.millis";
    public static final String METRIC_NAME_CONNECTION_CONNECT_ALIVE_MILLIS_MIN = DRUID_METRIC_NAME_PREFIX + ".connection.connect.alive.millis.min";
    public static final String METRIC_NAME_CONNECTION_CONNECT_ALIVE_MILLIS_MAX = DRUID_METRIC_NAME_PREFIX + ".connection.connect.alive.millis.max";
    /**
     * connections
     */
    public static final String METRIC_NAME_CONNECTORS_CONNECT_MAX_TIME = DRUID_METRIC_NAME_PREFIX + ".connections.connect.max.time";
    public static final String METRIC_NAME_CONNECTORS_ALIVE_MAX_TIME = DRUID_METRIC_NAME_PREFIX + ".connections.alive.max.time";
    public static final String METRIC_NAME_CONNECTORS_ALIVE_MIN_TIME = DRUID_METRIC_NAME_PREFIX + ".connections.alive.min.time";
    public static final String METRIC_NAME_CONNECTORS_CONNECT_COUNT = DRUID_METRIC_NAME_PREFIX + ".connections.connect.count";
    public static final String METRIC_NAME_CONNECTORS_ACTIVE_COUNT = DRUID_METRIC_NAME_PREFIX + ".connections.active.count";
    public static final String METRIC_NAME_CONNECTORS_CLOSE_COUNT = DRUID_METRIC_NAME_PREFIX + ".connections.close.count";
    public static final String METRIC_NAME_CONNECTORS_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + ".connections.error.count";
    public static final String METRIC_NAME_CONNECTORS_CONNECT_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + ".connections.connect.error.count";
    public static final String METRIC_NAME_CONNECTORS_COMMIT_COUNT = DRUID_METRIC_NAME_PREFIX + ".connections.commit.count";
    public static final String METRIC_NAME_CONNECTORS_ROLLBACK_COUNT = DRUID_METRIC_NAME_PREFIX + ".connections.rollback.count";
    /**
     * statement
     */
    public static final String METRIC_NAME_STATEMENT_CREATE_COUNT = DRUID_METRIC_NAME_PREFIX + ".statement.create.count";
    public static final String METRIC_NAME_STATEMENT_PREPARE_COUNT = DRUID_METRIC_NAME_PREFIX + ".statement.prepare.count";
    public static final String METRIC_NAME_STATEMENT_PREPARE_CALL_COUNT = DRUID_METRIC_NAME_PREFIX + ".statement.prepare.call.count";
    public static final String METRIC_NAME_STATEMENT_CLOSE_COUNT = DRUID_METRIC_NAME_PREFIX + ".statement.close.count";
    public static final String METRIC_NAME_STATEMENT_RUNNING_COUNT = DRUID_METRIC_NAME_PREFIX + ".statement.running.count";
    public static final String METRIC_NAME_STATEMENT_CONCURRENT_MAX = DRUID_METRIC_NAME_PREFIX + ".statement.concurrent.max";
    public static final String METRIC_NAME_STATEMENT_EXECUTE_COUNT = DRUID_METRIC_NAME_PREFIX + ".statement.execute.count";
    public static final String METRIC_NAME_STATEMENT_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + ".statement.error.count";
    public static final String METRIC_NAME_STATEMENT_NANO_TOTAL = DRUID_METRIC_NAME_PREFIX + ".statement.nano.total";
    public static final String METRIC_NAME_STATEMENT_NANO_MAX = DRUID_METRIC_NAME_PREFIX + ".statement.nano.max";
    public static final String METRIC_NAME_STATEMENT_NANO_MIN = DRUID_METRIC_NAME_PREFIX + ".statement.nano.min";
    public static final String METRIC_NAME_STATEMENT_EXECUTE_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + ".statement.execute.error.count";
    public static final String METRIC_NAME_STATEMENT_EXECUTE_SUCCESS_COUNT = DRUID_METRIC_NAME_PREFIX + ".statement.execute.success.count";
    public static final String METRIC_NAME_STATEMENT_EXECUTE_UPDATE_COUNT = DRUID_METRIC_NAME_PREFIX + ".statement.execute.update.count";
    public static final String METRIC_NAME_STATEMENT_EXECUTE_QUERY_COUNT = DRUID_METRIC_NAME_PREFIX + ".statement.execute.query.count";
    public static final String METRIC_NAME_STATEMENT_EXECUTE_MILLIS_TOTAL = DRUID_METRIC_NAME_PREFIX + ".statement.execute.millis.total";
    /**
     * resultSet
     */
    public static final String METRIC_NAME_RESULTSET_CONNECT_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + ".connections.connect.error.count";
    public static final String METRIC_NAME_RESULTSET_COMMIT_COUNT = DRUID_METRIC_NAME_PREFIX + ".connections.commit.count";
    public static final String METRIC_NAME_RESULTSET_ROLLBACK_COUNT = DRUID_METRIC_NAME_PREFIX + ".connections.rollback.count";
    /**
     * Sql
     */
    public static final String METRIC_NAME_SQL_SKIP_COUNT = DRUID_METRIC_NAME_PREFIX + ".sql.skip.count";
    public static final String METRIC_NAME_SQL_EXECUTE_COUNT = DRUID_METRIC_NAME_PREFIX + ".sql.execute.count";
    public static final String METRIC_NAME_SQL_EXECUTE_SUCCESS_COUNT = DRUID_METRIC_NAME_PREFIX + ".sql.execute.success.count";
    public static final String METRIC_NAME_SQL_EXECUTE_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + ".sql.execute.error.count";
    public static final String METRIC_NAME_SQL_EXECUTE_MILLIS_TOTAL = DRUID_METRIC_NAME_PREFIX + ".sql.execute.millis.total";
    public static final String METRIC_NAME_SQL_EXECUTE_MILLIS_MAX = DRUID_METRIC_NAME_PREFIX + ".sql.execute.millis.max";
    public static final String METRIC_NAME_SQL_EXECUTE_BATCH_SIZE_TOTAL = DRUID_METRIC_NAME_PREFIX + ".sql.execute.batch.size.total";
    public static final String METRIC_NAME_SQL_EXECUTE_BATCH_SIZE_MAX = DRUID_METRIC_NAME_PREFIX + ".sql.execute.batch.size.max";
    public static final String METRIC_NAME_SQL_IN_TRANSACTION_COUNT = DRUID_METRIC_NAME_PREFIX + ".sql.in.transaction.count";
    public static final String METRIC_NAME_SQL_CONCURRENT_MAX = DRUID_METRIC_NAME_PREFIX + ".sql.concurrent.max";
    public static final String METRIC_NAME_SQL_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + ".sql.error.count";

    public static final String METRIC_NAME_SQL_SELECT_COUNT = DRUID_METRIC_NAME_PREFIX + ".sql.select.count";
    public static final String METRIC_NAME_SQL_UPDATE_COUNT = DRUID_METRIC_NAME_PREFIX + ".sql.update.count";
    public static final String METRIC_NAME_SQL_INSERT_COUNT = DRUID_METRIC_NAME_PREFIX + ".sql.insert.count";
    public static final String METRIC_NAME_SQL_DELETE_COUNT = DRUID_METRIC_NAME_PREFIX + ".sql.delete.count";

}
