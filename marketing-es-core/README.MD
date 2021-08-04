#贷前ES公共包
##说明
##索引模板
####template_request_marketing_history

###操作
通过索引模板动态添加字段
#####创建模板
curl -X PUT "http://test_user:user123456@192.168.162.222:9200/_template/template_request_marketing_history" -H "Content-Type:application/json" -d '{"order":0,"index_patterns":["request_marketing_history_*"],"settings":{"index":{"mapping":{"field_name_length":{"limit":"32"},"depth":{"limit":"2"}},"search":{"slowlog":{"level":"trace","threshold":{"fetch":{"warn":"2s","trace":"200ms","debug":"500ms","info":"1s"},"query":{"warn":"10s","trace":"1s","debug":"3s","info":"5s"}}},"idle":{"after":"10s"}},"refresh_interval":"5s","number_of_shards":"6","translog":{"flush_threshold_size":"512mb","durability":"request"},"merge":{"scheduler":{"max_thread_count":"1"}},"max_result_window":"10000","store":{"type":"hybridfs"}}},"mappings":{"_source":{"enabled":true},"dynamic":false,"properties":{"api_code":{"type":"keyword"},"product":{"type":"nested","properties":{"score":{"type":"double"},"code":{"type":"keyword","doc_values":false},"flag":{"type":"keyword"},"code_version":{"type":"keyword"},"version":{"type":"keyword","doc_values":false}}},"request_time":{"format":"yyyy-MM-dd","type":"date"},"batch_number":{"type":"keyword"},"swift_number":{"type":"keyword"},"cus_batch_number":{"type":"keyword"},"strategy_id":{"type":"keyword"},"id_card":{"type":"keyword","doc_values":false},"name":{"type":"keyword","doc_values":false},"cus_num":{"type":"keyword","doc_values":false},"cell":{"type":"keyword","doc_values":false},"version":{"type":"keyword"},"file_id":{"type":"keyword"},"reserve_field":{"type":"keyword"}}},"aliases":{}}'
#####查看模板
curl -X GET "http://test_user:user123456@192.168.162.222:9200/_template/template_request_marketing_history"

##预处理模板
####str_list_marketing_template
#####列表查询
{"script":{"lang":"mustache","source":"{\"size\":\"{{size}}{{^size}}0{{/size}}\",{{#searchAfter}}\"search_after\":{{#toJson}}searchAfter{{/toJson}},{{/searchAfter}}\"sort\":{{!以#开始、以/结束和{}连接以空格处理}}{ {{#code_version}}\"product.score\": {\"order\": \"desc\",\"nested\": {\"path\": \"product\",\"filter\": {\"term\": {\"product.code_version\":\"{{code_version}}\"}}}},{{/code_version}}\"swift_number\": \"desc\"},\"_source\":{{#toJson}}source{{/toJson}},\"query\":{\"bool\":{\"must\":[{{#batch_number}}{\"terms\":{\"batch_number\":{{#toJson}}batch_number{{/toJson}}}},{{/batch_number}}{{#code_version}}{\"nested\": {\"path\": \"product\",\"query\": {\"bool\": {\"must\": [{{#begin_score}}{\"range\":{\"product.score\":{\"gte\":{{begin_score}},\"lte\":{{end_score}}}}},{{/begin_score}}{\"term\":{\"product.code_version\":\"{{code_version}}\"}}]}}}},{{/code_version}}{\"term\":{\"api_code\":\"{{api_code}}\"}}{{!api_code必传}}]}}}"}}
###操作
采用mustache语言作为elasticsearch搜索请求的预处理模板
#####创建模板
curl -X PUT "http://test_user:user123456@192.168.162.222:9200/_scripts/str_list_marketing_template" -H "Content-Type:application/json" -d '{"script":{"lang":"mustache","source":"{\"size\":\"{{size}}{{^size}}0{{/size}}\",{{#searchAfter}}\"search_after\":{{#toJson}}searchAfter{{/toJson}},{{/searchAfter}}\"sort\":{{!以#开始、以/结束和{}连接以空格处理}}{ {{#code_version}}\"product.score\": {\"order\": \"desc\",\"nested\": {\"path\": \"product\",\"filter\": {\"term\": {\"product.code_version\":\"{{code_version}}\"}}}},{{/code_version}}\"swift_number\": \"desc\"},\"_source\":{{#toJson}}source{{/toJson}},\"query\":{\"bool\":{\"must\":[{{#file_id}}{\"terms\":{\"file_id\":{{#toJson}}file_id{{/toJson}}}},{{/file_id}}{{#code_version}}{\"nested\": {\"path\": \"product\",\"query\": {\"bool\": {\"must\": [{{#begin_score}}{\"range\":{\"product.score\":{\"gte\":{{begin_score}},\"lte\":{{end_score}}}}},{{/begin_score}}{\"term\":{\"product.code_version\":\"{{code_version}}\"}}]}}}},{{/code_version}}{\"term\":{\"api_code\":\"{{api_code}}\"}}{{!api_code必传}}]}}}"}}'
#####查看模板
curl -X GET "http://test_user:user123456@192.168.162.222:9200/_scripts/str_list_marketing_template"
#####查询语句
curl "http://test_user:user123456@192.168.162.222:9200/_render/template" -H "Content-Type:application/json" -d '{"id":"str_list_marketing_template","params":{"api_code":"7410480","batch_number":"[\"7410480_20210526210400_4077\"]","source":"[\"_id\"]","sort":"{\"swift_number\":\"desc\"}"}}'
#####查询结果
curl -X POST "http://test_user:user123456@192.168.162.222:9200/request_marketing_history_202105_03/_search/template" -H "Content-Type:application/json" -d '{"id":"str_list_marketing_template","params":{"size":5,"api_code":"7410480","batch_number":"[\"7410480_20210526210400_4077\"]","source":"[\"_id\",\"swift_number\"]","sort":"{\"swift_number\":\"desc\"}"}}'