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
    public static final String METRIC_NAME_INITIAL_SIZE = DRUID_METRIC_NAME_PREFIX + "_initial_size";
    public static final String METRIC_NAME_MIN_IDLE = DRUID_METRIC_NAME_PREFIX + "_min_idle";
    public static final String METRIC_NAME_MAX_ACTIVE = DRUID_METRIC_NAME_PREFIX + "_max_active";
    public static final String METRIC_NAME_MAX_WAIT = DRUID_METRIC_NAME_PREFIX + "_max_wait";
    public static final String METRIC_NAME_MAX_WAIT_THREAD_COUNT = DRUID_METRIC_NAME_PREFIX + "_max_wait_thread_count";
    public static final String METRIC_NAME_MAX_POOL_PREPARED_STATEMENT_PER_CONNECTION_SIZE = DRUID_METRIC_NAME_PREFIX + "_max_pool_prepared_statement_per_connection_size";
    public static final String METRIC_NAME_MAX_OPEN_PREPARED_STATEMENTS = DRUID_METRIC_NAME_PREFIX + "_max_open_prepared_statements";
    public static final String METRIC_NAME_LOGIN_TIMEOUT = DRUID_METRIC_NAME_PREFIX + "_login_timeout";
    public static final String METRIC_NAME_QUERY_TIMEOUT = DRUID_METRIC_NAME_PREFIX + "_query_timeout";
    public static final String METRIC_NAME_TRANSACTION_QUERY_TIMEOUT = DRUID_METRIC_NAME_PREFIX + "_transaction_query_timeout";
    public static final String METRIC_NAME_TRANSACTION_THRESHOLD_MILLIS = DRUID_METRIC_NAME_PREFIX + "_transaction_threshold_millis";
    public static final String METRIC_NAME_VALIDATION_QUERY_TIMEOUT = DRUID_METRIC_NAME_PREFIX + "_validation_query_timeout";
    public static final String METRIC_NAME_ACTIVE_COUNT = DRUID_METRIC_NAME_PREFIX + "_active_count";
    public static final String METRIC_NAME_ACTIVE_PEAK = DRUID_METRIC_NAME_PREFIX + "_active_peak";
    public static final String METRIC_NAME_POOLING_COUNT = DRUID_METRIC_NAME_PREFIX + "_pooling_count";
    public static final String METRIC_NAME_POOLING_PEAK = DRUID_METRIC_NAME_PREFIX + "_pooling_peak";
    public static final String METRIC_NAME_WAIT_THREAD_COUNT = DRUID_METRIC_NAME_PREFIX + "_wait_thread_count";
    public static final String METRIC_NAME_NOT_EMPTY_WAIT_COUNT = DRUID_METRIC_NAME_PREFIX + "_not_empty_wait_count";
    public static final String METRIC_NAME_NOT_EMPTY_WAIT_MILLIS = DRUID_METRIC_NAME_PREFIX + "_not_empty_wait_millis";
    public static final String METRIC_NAME_NOT_EMPTY_THREAD_COUNT = DRUID_METRIC_NAME_PREFIX + "_not_empty_thread_count";
    public static final String METRIC_NAME_LOGIC_CONNECT_COUNT = DRUID_METRIC_NAME_PREFIX + "_logic_connect_count";
    public static final String METRIC_NAME_LOGIC_CLOSE_COUNT = DRUID_METRIC_NAME_PREFIX + "_logic_close_count";
    public static final String METRIC_NAME_LOGIC_CONNECT_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + "_logic_connect_error_count";
    public static final String METRIC_NAME_PHYSICAL_CONNECT_COUNT = DRUID_METRIC_NAME_PREFIX + "_physical_connect_count";
    public static final String METRIC_NAME_PHYSICAL_CLOSE_COUNT = DRUID_METRIC_NAME_PREFIX + "_physical_close_count";
    public static final String METRIC_NAME_PHYSICAL_CONNECT_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + "_physical_connect_error_count";
    public static final String METRIC_NAME_EXECUTE_COUNT = DRUID_METRIC_NAME_PREFIX + "_execute_count";
    public static final String METRIC_NAME_EXECUTE_QUERY_COUNT = DRUID_METRIC_NAME_PREFIX + "_execute_query_count";
    public static final String METRIC_NAME_EXECUTE_UPDATE_COUNT = DRUID_METRIC_NAME_PREFIX + "_execute_update_count";
    public static final String METRIC_NAME_EXECUTE_BATCH_COUNT = DRUID_METRIC_NAME_PREFIX + "_execute_batch_count";
    public static final String METRIC_NAME_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + "_error_count";
    public static final String METRIC_NAME_COMMIT_COUNT = DRUID_METRIC_NAME_PREFIX + "_commit_count";
    public static final String METRIC_NAME_ROLLBACK_COUNT = DRUID_METRIC_NAME_PREFIX + "_rollback_count";
    public static final String METRIC_NAME_PSCACHE_ACCESS_COUNT = DRUID_METRIC_NAME_PREFIX + "_ps_cache_access_count";
    public static final String METRIC_NAME_PSCACHE_HIT_COUNT = DRUID_METRIC_NAME_PREFIX + "_ps_cache_hit_count";
    public static final String METRIC_NAME_PSCACHE_MISS_COUNT = DRUID_METRIC_NAME_PREFIX + "_ps_cache_miss_count";
    public static final String METRIC_NAME_PREPARED_STATEMENT_OPEN_COUNT = DRUID_METRIC_NAME_PREFIX + "_prepared_statement_open_count";
    public static final String METRIC_NAME_PREPARED_STATEMENT_CLOSED_COUNT = DRUID_METRIC_NAME_PREFIX + "_prepared_statement_closed_count";
    public static final String METRIC_NAME_RESULTSET_OPEN_COUNT = DRUID_METRIC_NAME_PREFIX + "_resultset_open_count";
    public static final String METRIC_NAME_RESULTSET_OPENING_COUNT = DRUID_METRIC_NAME_PREFIX + "_resultset_opening_count";
    public static final String METRIC_NAME_RESULTSET_OPENING_MAX = DRUID_METRIC_NAME_PREFIX + "_resultset_opening_max";
    public static final String METRIC_NAME_RESULTSET_CLOSE_COUNT = DRUID_METRIC_NAME_PREFIX + "_resultset_close_count";
    public static final String METRIC_NAME_RESULTSET_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + "_resultset_error_count";
    public static final String METRIC_NAME_RESULTSET_FETCH_ROW_COUNT = DRUID_METRIC_NAME_PREFIX + "_resultset_fetch_row_count";
    public static final String METRIC_NAME_START_TRANSACTION_COUNT = DRUID_METRIC_NAME_PREFIX + "_start_transaction_count";
    public static final String METRIC_NAME_TRANSACTION_COUNT = DRUID_METRIC_NAME_PREFIX + "_transaction_count";
    public static final String METRIC_NAME_CONNECTION_HOLD_TIME_MILLIS = DRUID_METRIC_NAME_PREFIX + "_connection_hold_time_millis";
    public static final String METRIC_NAME_CONNECTION_HOLD_TIME_MILLIS_MIN = DRUID_METRIC_NAME_PREFIX + "_connection_hold_time_millis_min";
    public static final String METRIC_NAME_CONNECTION_HOLD_TIME_MILLIS_MAX = DRUID_METRIC_NAME_PREFIX + "_connection_hold_time_millis_max";
    public static final String METRIC_NAME_REMOVE_ABANDONED_COUNT = DRUID_METRIC_NAME_PREFIX + "_remove_abandoned_count";
    public static final String METRIC_NAME_CLOB_OPEN_COUNT = DRUID_METRIC_NAME_PREFIX + "_clob_open_count";
    public static final String METRIC_NAME_BLOB_OPEN_COUNT = DRUID_METRIC_NAME_PREFIX + "_blob_open_count";

    public static final String METRIC_NAME_CONNECTION_ACTIVE_COUNT = DRUID_METRIC_NAME_PREFIX + "_connection_active_count";
    public static final String METRIC_NAME_CONNECTION_CONNECT_ALIVE_MILLIS = DRUID_METRIC_NAME_PREFIX + "_connection_connect_alive_millis";
    public static final String METRIC_NAME_CONNECTION_CONNECT_ALIVE_MILLIS_MIN = DRUID_METRIC_NAME_PREFIX + "_connection_connect_alive_millis_min";
    public static final String METRIC_NAME_CONNECTION_CONNECT_ALIVE_MILLIS_MAX = DRUID_METRIC_NAME_PREFIX + "_connection_connect_alive_millis_max";
    /**
     * connections
     */
    public static final String METRIC_NAME_CONNECTORS_CONNECT_MAX_TIME = DRUID_METRIC_NAME_PREFIX + "_connections_connect_max_time";
    public static final String METRIC_NAME_CONNECTORS_ALIVE_MAX_TIME = DRUID_METRIC_NAME_PREFIX + "_connections_alive_max_time";
    public static final String METRIC_NAME_CONNECTORS_ALIVE_MIN_TIME = DRUID_METRIC_NAME_PREFIX + "_connections_alive_min_time";
    public static final String METRIC_NAME_CONNECTORS_CONNECT_COUNT = DRUID_METRIC_NAME_PREFIX + "_connections_connect_count";
    public static final String METRIC_NAME_CONNECTORS_ACTIVE_COUNT = DRUID_METRIC_NAME_PREFIX + "_connections_active_count";
    public static final String METRIC_NAME_CONNECTORS_CLOSE_COUNT = DRUID_METRIC_NAME_PREFIX + "_connections_close_count";
    public static final String METRIC_NAME_CONNECTORS_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + "_connections_error_count";
    public static final String METRIC_NAME_CONNECTORS_CONNECT_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + "_connections_connect_error_count";
    public static final String METRIC_NAME_CONNECTORS_COMMIT_COUNT = DRUID_METRIC_NAME_PREFIX + "_connections_commit_count";
    public static final String METRIC_NAME_CONNECTORS_ROLLBACK_COUNT = DRUID_METRIC_NAME_PREFIX + "_connections_rollback_count";
    /**
     * statement
     */
    public static final String METRIC_NAME_STATEMENT_CREATE_COUNT = DRUID_METRIC_NAME_PREFIX + "_statement_create_count";
    public static final String METRIC_NAME_STATEMENT_PREPARE_COUNT = DRUID_METRIC_NAME_PREFIX + "_statement_prepare_count";
    public static final String METRIC_NAME_STATEMENT_PREPARE_CALL_COUNT = DRUID_METRIC_NAME_PREFIX + "_statement_prepare_call_count";
    public static final String METRIC_NAME_STATEMENT_CLOSE_COUNT = DRUID_METRIC_NAME_PREFIX + "_statement_close_count";
    public static final String METRIC_NAME_STATEMENT_RUNNING_COUNT = DRUID_METRIC_NAME_PREFIX + "_statement_running_count";
    public static final String METRIC_NAME_STATEMENT_CONCURRENT_MAX = DRUID_METRIC_NAME_PREFIX + "_statement_concurrent_max";
    public static final String METRIC_NAME_STATEMENT_EXECUTE_COUNT = DRUID_METRIC_NAME_PREFIX + "_statement_execute_count";
    public static final String METRIC_NAME_STATEMENT_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + "_statement_error_count";
    public static final String METRIC_NAME_STATEMENT_NANO_TOTAL = DRUID_METRIC_NAME_PREFIX + "_statement_nano_total";
    public static final String METRIC_NAME_STATEMENT_NANO_MAX = DRUID_METRIC_NAME_PREFIX + "_statement_nano_max";
    public static final String METRIC_NAME_STATEMENT_NANO_MIN = DRUID_METRIC_NAME_PREFIX + "_statement_nano_min";
    public static final String METRIC_NAME_STATEMENT_EXECUTE_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + "_statement_execute_error_count";
    public static final String METRIC_NAME_STATEMENT_EXECUTE_SUCCESS_COUNT = DRUID_METRIC_NAME_PREFIX + "_statement_execute_success_count";
    public static final String METRIC_NAME_STATEMENT_EXECUTE_UPDATE_COUNT = DRUID_METRIC_NAME_PREFIX + "_statement_execute_update_count";
    public static final String METRIC_NAME_STATEMENT_EXECUTE_QUERY_COUNT = DRUID_METRIC_NAME_PREFIX + "_statement_execute_query_count";
    public static final String METRIC_NAME_STATEMENT_EXECUTE_MILLIS_TOTAL = DRUID_METRIC_NAME_PREFIX + "_statement_execute_millis_total";
    /**
     * resultSet
     */
    public static final String METRIC_NAME_RESULTSET_CONNECT_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + "_connections_connect_error_count";
    public static final String METRIC_NAME_RESULTSET_COMMIT_COUNT = DRUID_METRIC_NAME_PREFIX + "_connections_commit_count";
    public static final String METRIC_NAME_RESULTSET_ROLLBACK_COUNT = DRUID_METRIC_NAME_PREFIX + "_connections_rollback_count";
    /**
     * Sql
     */
    public static final String METRIC_NAME_SQL_SKIP_COUNT = DRUID_METRIC_NAME_PREFIX + "_sql_skip_count";
    public static final String METRIC_NAME_SQL_EXECUTE_COUNT = DRUID_METRIC_NAME_PREFIX + "_sql_execute_count";
    public static final String METRIC_NAME_SQL_EXECUTE_SUCCESS_COUNT = DRUID_METRIC_NAME_PREFIX + "_sql_execute_success_count";
    public static final String METRIC_NAME_SQL_EXECUTE_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + "_sql_execute_error_count";
    public static final String METRIC_NAME_SQL_EXECUTE_MILLIS_TOTAL = DRUID_METRIC_NAME_PREFIX + "_sql_execute_millis_total";
    public static final String METRIC_NAME_SQL_EXECUTE_MILLIS_MAX = DRUID_METRIC_NAME_PREFIX + "_sql_execute_millis_max";
    public static final String METRIC_NAME_SQL_EXECUTE_BATCH_SIZE_TOTAL = DRUID_METRIC_NAME_PREFIX + "_sql_execute_batch_size_total";
    public static final String METRIC_NAME_SQL_EXECUTE_BATCH_SIZE_MAX = DRUID_METRIC_NAME_PREFIX + "_sql_execute_batch_size_max";
    public static final String METRIC_NAME_SQL_IN_TRANSACTION_COUNT = DRUID_METRIC_NAME_PREFIX + "_sql_in_transaction_count";
    public static final String METRIC_NAME_SQL_CONCURRENT_MAX = DRUID_METRIC_NAME_PREFIX + "_sql_concurrent_max";
    public static final String METRIC_NAME_SQL_ERROR_COUNT = DRUID_METRIC_NAME_PREFIX + "_sql_error_count";

    public static final String METRIC_NAME_SQL_SELECT_COUNT = DRUID_METRIC_NAME_PREFIX + "_sql_select_count";
    public static final String METRIC_NAME_SQL_UPDATE_COUNT = DRUID_METRIC_NAME_PREFIX + "_sql_update_count";
    public static final String METRIC_NAME_SQL_INSERT_COUNT = DRUID_METRIC_NAME_PREFIX + "_sql_insert_count";
    public static final String METRIC_NAME_SQL_DELETE_COUNT = DRUID_METRIC_NAME_PREFIX + "_sql_delete_count";

}
