#贷前ES公共包
##说明
##索引模板
####template_request_approval_history

###操作
通过索引模板动态添加字段
#####创建模板
curl -X PUT "http://test_user:user123456@192.168.162.222:9200/_template/template_request_marketing_history" -H "Content-Type:application/json" -d '{"order":0,"index_patterns":["request_marketing_history_*"],"settings":{"index":{"mapping":{"field_name_length":{"limit":"32"},"depth":{"limit":"2"}},"search":{"slowlog":{"level":"trace","threshold":{"fetch":{"warn":"2s","trace":"200ms","debug":"500ms","info":"1s"},"query":{"warn":"10s","trace":"1s","debug":"3s","info":"5s"}}},"idle":{"after":"10s"}},"refresh_interval":"5s","number_of_shards":"6","translog":{"flush_threshold_size":"512mb","durability":"request"},"merge":{"scheduler":{"max_thread_count":"1"}},"max_result_window":"10000","store":{"type":"hybridfs"}}},"mappings":{"_source":{"enabled":true},"dynamic":false,"properties":{"request_time":{"format":"yyyy-MM-dd","type":"date"},"batch_number":{"type":"keyword"},"swift_number":{"type":"keyword"},"cus_num":{"type":"keyword","doc_values":false},"strategy_id":{"type":"keyword"},"version":{"type":"keyword"},"api_code":{"type":"keyword"}}},"aliases":{}}'
#####查看模板
curl -X GET "http://test_user:user123456@192.168.162.222:9200/_template/template_request_marketing_history"
#####删除模板
curl -X DELETE "http://test_user:user123456@192.168.162.222:9200/_template/template_request_marketing_history"

##预处理模板
####str_sn_approval_template
#####根据流水号查询传值字段返回
{"script":{"lang":"mustache","source":"{\"_source\":{{#toJson}}source{{/toJson}},\"query\":{\"bool\":{\"must\":[{\"term\":{\"swift_number\":\"{{swift_number}}\"}}{{#status}},{\"term\":{\"status\":\"{{status}}\"}}{{/status}}]}}}"}}
####str_list_approval_template
#####列表查询
{"script":{"lang":"mustache","source":"{\"from\":\"{{from}}{{^from}}0{{/from}}\",\"size\":\"{{size}}{{^size}}0{{/size}}\",\"sort\":{{#toJson}}sort{{/toJson}},\"_source\":{{#toJson}}source{{/toJson}},\"query\":{\"bool\":{\"must\":[{{#swift_number}}{\"terms\":{\"swift_number\":{{#toJson}}swift_number{{/toJson}}}},{{/swift_number}}{{#name}}{\"bool\":{\"should\":[{\"multi_match\":{\"query\" :\"{{name}}\",\"fields\":[\"id\",\"cell\",\"name\"]}},{\"term\":{\"cell\" :\"[\\\"{{name}}\\\"]\"}}]}},{{/name}}{{#status}}{\"term\":{\"status\":{{status}}}},{{/status}}{{#cus_num}}{\"term\":{\"cus_num\":\"{{cus_num}}\"}},{{/cus_num}}{{#final_decision}}{\"terms\":{\"final_decision\":{{#toJson}}final_decision{{/toJson}}}},{{/final_decision}}{{#batch_number}}{\"wildcard\":{\"batch_number\":\"*{{batch_number}}*\"}},{{/batch_number}}{{#request_type_str}}{\"wildcard\":{\"strategy_id\":\"{{request_type_str}}*\"}},{{/request_type_str}}{{#request_type_mcp}}{\"wildcard\":{\"conf_id\":\"{{request_type_mcp}}*\"}},{{/request_type_mcp}}{{#query_type}}{\"terms\":{\"query_type\":{{#toJson}}query_type{{/toJson}}}},{{/query_type}}{{#start_begin_time}}{\"range\":{\"start_time\":{\"gte\":\"{{start_begin_time}}\",\"lte\":\"{{start_end_time}}\"}}},{{/start_begin_time}}{{#owner_user}}{\"terms\":{\"owner_user\":{{#toJson}}owner_user{{/toJson}}}},{{/owner_user}}{{#prePageSwiftNumber}}{\"range\":{\"swift_number\":{\"gt\":\"{{prePageSwiftNumber}}\"}}},{{/prePageSwiftNumber}}{{#nextPageSwiftNumber}}{\"range\":{\"swift_number\":{\"lt\":\"{{nextPageSwiftNumber}}\"}}},{{/nextPageSwiftNumber}}{\"term\":{\"api_code\":\"{{api_code}}\"}}{{!api_code必传}}],\"must_not\":[{{#final_decision_not}}{\"terms\":{\"final_decision\":{{#toJson}}final_decision_not{{/toJson}}}},{{/final_decision_not}}{\"term\":{\"status\":4}}]}}}"}}
###操作
采用mustache语言作为elasticsearch搜索请求的预处理模板
#####创建模板
curl -X PUT "http://test_user:user123456@192.168.162.222:9200/_scripts/str_approval_template_test" -H "Content-Type:application/json" -d '{"script":{"lang":"mustache","source":"{\"_source\": {{#toJson}}source{{/toJson}},\"query\":{\"bool\":{\"filter\":[{\"term\" : { \"swift_number\":\"{{swift_number}}\"}} ]}} }"}}'
#####查看模板
curl -X GET "http://test_user:user123456@192.168.162.222:9200/_scripts/str_approval_template_test"
#####查询语句
curl "http://test_user:user123456@192.168.162.222:9200/_render/template" -H "Content-Type:application/json" -d '{"id":"str_approval_template_test","params":{"swift_number":"4c82eeff84234fd1b2a43ac27bd933e2","source":["_id","swift_number","name","cell"]}}'
#####查询结果
curl -X POST "http://test_user:user123456@192.168.162.222:9200/request_approval_history_2020*/_search/template" -H "Content-Type:application/json" -d '{"id":"str_approval_template_test","params":{"swift_number":"4c82eeff84234fd1b2a43ac27bd933e2","source":["_id","swift_number","name","cell"]}}'
#####删除模板
curl -X DELETE "http://test_user:user123456@192.168.162.222:9200/_scripts/str_approval_template_test"