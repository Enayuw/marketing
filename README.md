## marketing 营销平台 数据收集整理
### 模块说明
- marketing-api 流失预警api服务，部署在k8环境。
- marketing-task 预警调度任务，流失预警核心调度任务。部署在调度平台。
- marketing-push-task 结果推送调度任务，流失预警结果文件推送到ftp。部署在调度平台。
- marketing-cs、marketing-utils 公共依赖。
- marketing-sync 文件同步
- marketing-check  数据入库、报警，校验等接口


注意：
   每次上线都需要将最新代码合并到master分支，为当前版本创建tag