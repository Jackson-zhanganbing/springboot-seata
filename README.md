# springboot-seata

**分布式事务组件seata的使用demo，AT模式，集成nacos、springboot、springcloud数据库采用mysql**

----------

## 1. 服务端配置

### 1.1 Nacos-server

版本为nacos-server-2.0.2，demo采用本地单机部署方式，请参考 [Nacos 快速开始](https://nacos.io/zh-cn/docs/quick-start.html)

### 1.2 Seata-server

seata-server为release版本1.4.2，demo采用本地单机部署，从此处下载 [https://github.com/seata/seata/releases](https://github.com/seata/seata/releases)
并解压

#### 1.2.1 修改conf/registry.conf 配置

设置type、设置serverAddr为你的nacos节点地址。

**注意这里有一个坑，serverAddr不能带‘http://’前缀**

~~~java
registry {
  # file 、nacos 、eureka、redis、zk、consul、etcd3、sofa
  type = "nacos"
  loadBalance = "RandomLoadBalance"
  loadBalanceVirtualNodes = 10

  nacos {
    application = "seata-server"
    serverAddr = "127.0.0.1:8848"
    group = "SEATA_GROUP"
    namespace = ""
    cluster="default"
    username = "nacos"
    password = "nacos"
  }
}

config {
  # file、nacos 、apollo、zk、consul、etcd3
  type = "nacos"

  nacos {
    serverAddr = "127.0.0.1:8848"
    group = "SEATA_GROUP"
    namespace = ""
    username = "nacos"
    password = "nacos"
  }
}

~~~

#### 1.2.2 修改conf/nacos-config.txt 配置
```
transport.type=TCP
transport.server=NIO
transport.heartbeat=true
transport.enableClientBatchSendRequest=false
transport.threadFactory.bossThreadPrefix=NettyBoss
transport.threadFactory.workerThreadPrefix=NettyServerNIOWorker
transport.threadFactory.serverExecutorThreadPrefix=NettyServerBizHandler
transport.threadFactory.shareBossWorker=false
transport.threadFactory.clientSelectorThreadPrefix=NettyClientSelector
transport.threadFactory.clientSelectorThreadSize=1
transport.threadFactory.clientWorkerThreadPrefix=NettyClientWorkerThread
transport.threadFactory.bossThreadSize=1
transport.threadFactory.workerThreadSize=default
transport.shutdown.wait=3
#修改my_test_tx_group为自定义服务seata-group
service.vgroupMapping.order-service-group=default
service.default.grouplist=127.0.0.1:8091
service.enableDegrade=false
service.disableGlobalTransaction=false
client.rm.asyncCommitBufferLimit=10000
client.rm.lock.retryInterval=10
client.rm.lock.retryTimes=30
client.rm.lock.retryPolicyBranchRollbackOnConflict=true
client.rm.reportRetryCount=5
client.rm.tableMetaCheckEnable=false
client.rm.sqlParserType=druid
client.rm.reportSuccessEnable=false
client.rm.sagaBranchRegisterEnable=false
client.tm.commitRetryCount=5
client.tm.rollbackRetryCount=5
client.tm.defaultGlobalTransactionTimeout=60000
client.tm.degradeCheck=false
client.tm.degradeCheckAllowTimes=10
client.tm.degradeCheckPeriod=2000
store.mode=db
store.file.dir=file_store/data
store.file.maxBranchSessionSize=16384
store.file.maxGlobalSessionSize=512
store.file.fileWriteBufferCacheSize=16384
store.file.flushDiskMode=async
store.file.sessionReloadReadSize=100
store.db.datasource=druid
store.db.dbType=mysql
store.db.driverClassName=com.mysql.cj.jdbc.Driver
store.db.url=jdbc:mysql://127.0.0.1:3306/seata?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&useSSL=true
store.db.user=root
store.db.password=root
store.db.minConn=5
store.db.maxConn=30
store.db.globalTable=global_table
store.db.branchTable=branch_table
store.db.queryLimit=100
store.db.lockTable=lock_table
store.db.maxWait=5000
store.redis.host=127.0.0.1
store.redis.port=6379
store.redis.maxConn=10
store.redis.minConn=1
store.redis.database=0
store.redis.password=null
store.redis.queryLimit=100
server.recovery.committingRetryPeriod=1000
server.recovery.asynCommittingRetryPeriod=1000
server.recovery.rollbackingRetryPeriod=1000
server.recovery.timeoutRetryPeriod=1000
server.maxCommitRetryTimeout=-1
server.maxRollbackRetryTimeout=-1
server.rollbackRetryTimeoutUnlockEnable=false
client.undo.dataValidation=true
client.undo.logSerialization=jackson
client.undo.onlyCareUpdateColumns=true
server.undo.logSaveDays=7
server.undo.logDeletePeriod=86400000
client.undo.logTable=undo_log
client.log.exceptionRate=100
transport.serialization=seata
transport.compressor=none
metrics.enabled=false
metrics.registryType=compact
metrics.exporterList=prometheus
metrics.exporterPrometheusPort=9898
```

** 注意这里,高版本中应该是vgroupMapping 同时后面的如: order-service-group 不能定义为 order_service_group **

#### 1.3 启动seata-server

**分两步，如下**

~~~shell
# 初始化seata 的nacos配置
cd conf
sh nacos-config.sh

# 启动seata-server
cd bin
sh seata-server.sh
~~~

----------

## 2. 应用配置

### 2.1 数据库初始化

~~~SQL
-- 创建 order库及order表、undo_log表
CREATE DATABASE seata_order;

CREATE TABLE t_order (
                        `id` BIGINT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        `user_id` BIGINT(11) DEFAULT NULL COMMENT '用户id',
                        `product_id` BIGINT(11) DEFAULT NULL COMMENT '产品id',
                        `count` INT(11) DEFAULT NULL COMMENT '数量',
                        `money` DECIMAL(11,0) DEFAULT NULL COMMENT '金额',
                        `status` INT(1) DEFAULT NULL COMMENT '订单状态：0：创建中；1：已完结'
) ENGINE=INNODB DEFAULT CHARSET=utf8;

SELECT * FROM t_order;

DROP TABLE `undo_log`;

CREATE TABLE `undo_log` (
                           `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                           `branch_id` BIGINT(20) NOT NULL,
                           `xid` VARCHAR(100) NOT NULL,
                           `context` VARCHAR(128) NOT NULL,
                           `rollback_info` LONGBLOB NOT NULL,
                           `log_status` INT(11) NOT NULL,
                           `log_created` DATETIME NOT NULL,
                           `log_modified` DATETIME NOT NULL,
                           `ext` VARCHAR(100) DEFAULT NULL,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

-- 创建 storage库及storage表、undo_log表
CREATE DATABASE seata_storage;
CREATE TABLE t_storage (
                          `id` BIGINT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
                          `product_id` BIGINT(11) DEFAULT NULL COMMENT '产品id',
                          `total` INT(11) DEFAULT NULL COMMENT '总库存',
                          `used` INT(11) DEFAULT NULL COMMENT '已用库存',
                          `residue` INT(11) DEFAULT NULL COMMENT '剩余库存'
) ENGINE=INNODB  DEFAULT CHARSET=utf8;

-- 表示有100个库存
INSERT INTO seata_storage.t_storage(`id`, `product_id`, `total`, `used`, `residue`)
VALUES ('1', '1', '100', '0', '100');
SELECT * FROM t_storage;

CREATE TABLE `undo_log` (
                           `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                           `branch_id` BIGINT(20) NOT NULL,
                           `xid` VARCHAR(100) NOT NULL,
                           `context` VARCHAR(128) NOT NULL,
                           `rollback_info` LONGBLOB NOT NULL,
                           `log_status` INT(11) NOT NULL,
                           `log_created` DATETIME NOT NULL,
                           `log_modified` DATETIME NOT NULL,
                           `ext` VARCHAR(100) DEFAULT NULL,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=INNODB  DEFAULT CHARSET=utf8;
-- 创建 account库及account表、undo_log表
CREATE DATABASE seata_account;
CREATE TABLE t_account (
                          `id` BIGINT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'id',
                          `user_id` BIGINT(11) DEFAULT NULL COMMENT '用户id',
                          `total` DECIMAL(10,0) DEFAULT NULL COMMENT '总额度',
                          `used` DECIMAL(10,0) DEFAULT NULL COMMENT '已用余额',
                          `residue` DECIMAL(10,0) DEFAULT '0' COMMENT '剩余可用额度'
) ENGINE=INNODB  DEFAULT CHARSET=utf8;
-- 表示用户有1000块可以花
INSERT INTO seata_account.t_account(`id`, `user_id`, `total`, `used`, `residue`)  VALUES ('1', '1', '1000', '0', '1000');
SELECT * FROM t_account;

CREATE TABLE `undo_log` (
                           `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
                           `branch_id` BIGINT(20) NOT NULL,
                           `xid` VARCHAR(100) NOT NULL,
                           `context` VARCHAR(128) NOT NULL,
                           `rollback_info` LONGBLOB NOT NULL,
                           `log_status` INT(11) NOT NULL,
                           `log_created` DATETIME NOT NULL,
                           `log_modified` DATETIME NOT NULL,
                           `ext` VARCHAR(100) DEFAULT NULL,
                           PRIMARY KEY (`id`),
                           UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8;

~~~

----------

## 3. 测试

1. 不加分布式事务注解@GlobalTransactional，模拟账户服务问题，出现分布式事务问题（有订单，库存减少，但是没扣款）

http://127.0.0.1:8081/order/create?userId=1&productId=1&count=5&money=50

2. 加上分布式事务注解@GlobalTransactional，模拟下单成功、扣款失败，最终同时回滚（数据库无订单信息，库存和款都没有扣

http://127.0.0.1:8081/order/create?userId=1&productId=1&count=5&money=50





