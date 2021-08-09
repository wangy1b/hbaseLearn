# Hbase on Docker

拉取镜像，建立container

```shell
docker search hbase
# 拉取镜像
docker pull harisekhon/hbase
# 运行hbase
docker run -d -p 2181:2181 -p 8080:8080 -p 8085:8085 -p 9090:9090 -p 9095:9095 -p 16000:16000 -p 16010:16010 -p 16201:16201 -p 16301:16301  -p 16030:16030 -p 16020:16020 --name myhbase harisekhon/hbase
# 进入hbase container 
docker exec -it myhbase /bin/bash 
```

## hbase 管理页面

```
http://ip地址:16010/
```

## HBase数据模型

在 HBase 中，数据模型同样是由表组成的，各个表中又包含数据行和列，在这些表中存储了 HBase 数据。在本节中，我们将介绍 HBase 数据模型中的一些术语。

- 表（Table）

  HBase 会将数据组织进一张张的表里面，一个 HBase 表由多行组成。

- 行（Row）

  HBase 中的一行包含一个行键和一个或多个与其相关的值的列。在存储行时，行按字母顺序排序。出于这个原因，行键的设计非常重要。目标是以相关行相互靠近的方式存储数据。常用的行键模式是网站域。如果你的行键是域名，则你可能应该将它们存储在相反的位置（org.apache.www，org.apache.mail，org.apache.jira）。这样，表中的所有 Apache 域都彼此靠近，而不是根据子域的第一个字母分布。

- 列（Column）

  HBase 中的列由一个列族和一个列限定符组成，它们由`:`（冒号）字符分隔。

- 列族（Column Family）

  出于性能原因，列族在物理上共同存在一组列和它们的值。在 HBase 中每个列族都有一组存储属性，例如其值是否应缓存在内存中，数据如何压缩或其行编码是如何编码的等等。表中的每一行都有相同的列族，但给定的行可能不会在给定的列族中存储任何内容。

  列族一旦确定后，就不能轻易修改，因为它会影响到 HBase 真实的物理存储结构，但是列族中的列标识(Column Qualifier)以及其对应的值可以动态增删。 

  列簇，HBase中的每个列都归属于某个列簇，列簇是表的schema的一部分，必须在使用表之前定义。划分columns family的原则如下：

  - 是否具有相似的数据格式；
  - 是否具有相似的访问类型。

- 列限定符（Column Qualifier）

  列限定符被添加到列族中，以提供给定数据段的索引。鉴于列族的`content`，列限定符可能是`content:html`，而另一个可能是`content:pdf`。虽然列族在创建表时是固定的，但列限定符是可变的，并且在行之间可能差别很大。

- 单元格（Cell）

  单元格是行、列族和列限定符的组合，并且包含值和时间戳，它表示值的版本。

- 时间戳（Timestamp）

  时间戳与每个值一起编写，并且是给定版本的值的标识符。默认情况下，时间戳表示写入数据时 RegionServer 上的时间，但可以在将数据放入单元格时指定不同的时间戳值。

- Rowkey

  用来表示唯一一行记录的主键，HBase的数据是按照row key的字典顺序进行全局排列的。访问HBase中的行只有3种方式

  - 通过单个row key访问
  - 通过row key的正则访问
  - 全表扫描

  由于HBase通过rowkey对数据进行检索，而rowkey由于长度限制的因素不能将很多查询条件拼接在rowkey中，因此HBase无法像关系数据库那样根据多种条件对数据进行筛选。一般地，HBase需建立二级索引来满足根据复杂条件查询数据的需求。

  Rowkey设计时需要遵循三大原则：

  - 唯一性原则：rowkey需要保证唯一性，不存在重复的情况。在画像中一般使用用户id作为rowkey。
  - 长度原则：rowkey的长度一般为10-100bytes。
  - 散列原则：rowkey的散列分布有利于数据均衡分布在每个RegionServer，可实现负载均衡。

  通常来说，RowKey 只能针对条件中含有其首字段的查询给予令人满意的性能支持，在 查询其他字段时，表现就差强人意了，在极端情况下某些字段的查询性能可能会退化为全表 扫描的水平，这是因为字段在 RowKey 中的地位是不等价的，它们在 RowKey 中的排位决 定了它们被检索时的性能表现，排序越靠前的字段在查询中越具有优势，特别是首位字段 具有特别的先发优势，如果查询中包含首位字段，检索时就可以通过首位字段的值确定 RowKey 的前缀部分，从而大幅度地收窄检索区间，如果不包含则只能在全体数据的 RowKey 上逐一查找，由此可以想见两者在性能上的差距。

## HBase的Shell命令

### 简介

#### HBase表的操作

- **create:** 创建一个表。
- **list:** 列出HBase的所有表。
- **disable:** 禁用表。
- **is_disabled:** 验证表是否被禁用。
- **enable:** 启用一个表。
- **is_enabled:** 验证表是否已启用。
- **describe:** 提供了一个表的描述。
- **alter:** 改变一个表。
- **exists:** 验证表是否存在。
- **drop:** 从HBase中删除表。
- **drop_all:** 丢弃在命令中给出匹配“regex”的表。

#### HBase表中数据的操作

- **put:** 把指定列在指定的行中单元格的值在一个特定的表。
- **get:** 取行或单元格的内容。
- **delete:** 删除表中的单元格值。
- **deleteall:** 删除给定行的所有单元格。
- **scan:** 扫描并返回表数据。
- **count:** 计数并返回表中的行的数目。
- **truncate:** 禁用，删除和重新创建一个指定的表。

~~~ shell
COMMAND GROUPS:
  Group name: general
  Commands: processlist, status, table_help, version, whoami

  Group name: ddl
  Commands: alter, alter_async, alter_status, clone_table_schema, create, describe, disable, disable_all, drop, drop_all, enable, enable_all, exists, get_table, is_disabled, is_enabled, list, list_regions, locate_region, show_filters

  Group name: namespace
  Commands: alter_namespace, create_namespace, describe_namespace, drop_namespace, list_namespace, list_namespace_tables

  Group name: dml
  Commands: append, count, delete, deleteall, get, get_counter, get_splits, incr, put, scan, truncate, truncate_preserve

  Group name: tools
  Commands: assign, balance_switch, balancer, balancer_enabled, catalogjanitor_enabled, catalogjanitor_run, catalogjanitor_switch, cleaner_chore_enabled, cleaner_chore_run, cleaner_chore_switch, clear_block_cache, clear_compaction_queues, clear_deadservers, close_region, compact, compact_rs, compaction_state, flush, is_in_maintenance_mode, list_deadservers, major_compact, merge_region, move, normalize, normalizer_enabled, normalizer_switch, split, splitormerge_enabled, splitormerge_switch, stop_master, stop_regionserver, trace, unassign, wal_roll, zk_dump

  Group name: replication
  Commands: add_peer, append_peer_exclude_namespaces, append_peer_exclude_tableCFs, append_peer_namespaces, append_peer_tableCFs, disable_peer, disable_table_replication, enable_peer, enable_table_replication, get_peer_config, list_peer_configs, list_peers, list_replicated_tables, remove_peer, remove_peer_exclude_namespaces, remove_peer_exclude_tableCFs, remove_peer_namespaces, remove_peer_tableCFs, set_peer_bandwidth, set_peer_exclude_namespaces, set_peer_exclude_tableCFs, set_peer_namespaces, set_peer_replicate_all, set_peer_serial, set_peer_tableCFs, show_peer_tableCFs, update_peer_config

  Group name: snapshots
  Commands: clone_snapshot, delete_all_snapshot, delete_snapshot, delete_table_snapshots, list_snapshots, list_table_snapshots, restore_snapshot, snapshot

  Group name: configuration
  Commands: update_all_config, update_config

  Group name: quotas
  Commands: list_quota_snapshots, list_quota_table_sizes, list_quotas, list_snapshot_sizes, set_quota

  Group name: security
  Commands: grant, list_security_capabilities, revoke, user_permission

  Group name: procedures
  Commands: list_locks, list_procedures

  Group name: visibility labels
  Commands: add_labels, clear_auths, get_auths, list_labels, set_auths, set_visibility

  Group name: rsgroup
  Commands: add_rsgroup, balance_rsgroup, get_rsgroup, get_server_rsgroup, get_table_rsgroup, list_rsgroups, move_namespaces_rsgroup, move_servers_namespaces_rsgroup, move_servers_rsgroup, move_servers_tables_rsgroup, move_tables_rsgroup, remove_rsgroup, remove_servers_rsgroup
~~~

### 展开

#### general

Commands: processlist, status, table_help, version, whoami

help

~~~ shell
hbase(main):031:0> help 'status'
Show cluster status. Can be 'summary', 'simple', 'detailed', or 'replication'. The
default is 'summary'. Examples:

  hbase> status
  hbase> status 'simple'
  hbase> status 'summary'
  hbase> status 'detailed'
  hbase> status 'replication'
  hbase> status 'replication', 'source'
  hbase> status 'replication', 'sink'
~~~

~~~ shell
hbase(main):033:0> status
1 active master, 0 backup masters, 1 servers, 0 dead, 8.0000 average load
Took 0.1593 seconds
~~~

#### ddl

Commands: alter, alter_async, alter_status, clone_table_schema, create, describe, disable, disable_all, drop, drop_all, enable, enable_all, exists, get_table, is_disabled, is_enabled, list, list_regions, locate_region, show_filters

help

~~~ shell
hbase(main):035:0> help 'ddl'
Command: alter
Alter a table. Tables can be altered without disabling them first.
Altering enabled tables has caused problems
in the past, so use caution and test it before using in production.

You can use the alter command to add,
modify or delete column families or change table configuration options.
Column families work in a similar way as the 'create' command. The column family
specification can either be a name string, or a dictionary with the NAME attribute.
Dictionaries are described in the output of the 'help' command, with no arguments.

For example, to change or add the 'f1' column family in table 't1' from
current value to keep a maximum of 5 cell VERSIONS, do:

  hbase> alter 't1', NAME => 'f1', VERSIONS => 5

You can operate on several column families:

  hbase> alter 't1', 'f1', {NAME => 'f2', IN_MEMORY => true}, {NAME => 'f3', VERSIONS => 5}

To delete the 'f1' column family in table 'ns1:t1', use one of:

  hbase> alter 'ns1:t1', NAME => 'f1', METHOD => 'delete'
  hbase> alter 'ns1:t1', 'delete' => 'f1'

You can also change table-scope attributes like MAX_FILESIZE, READONLY,
MEMSTORE_FLUSHSIZE, NORMALIZATION_ENABLED, NORMALIZER_TARGET_REGION_COUNT,
NORMALIZER_TARGET_REGION_SIZE(MB), DURABILITY, etc. These can be put at the end;
for example, to change the max size of a region to 128MB, do:

  hbase> alter 't1', MAX_FILESIZE => '134217728'

You can add a table coprocessor by setting a table coprocessor attribute:

  hbase> alter 't1',
    'coprocessor'=>'hdfs:///foo.jar|com.foo.FooRegionObserver|1001|arg1=1,arg2=2'

Since you can have multiple coprocessors configured for a table, a
sequence number will be automatically appended to the attribute name
to uniquely identify it.

The coprocessor attribute must match the pattern below in order for
the framework to understand how to load the coprocessor classes:

  [coprocessor jar file location] | class name | [priority] | [arguments]

You can also set configuration settings specific to this table or column family:

  hbase> alter 't1', CONFIGURATION => {'hbase.hregion.scan.loadColumnFamiliesOnDemand' => 'true'}
  hbase> alter 't1', {NAME => 'f2', CONFIGURATION => {'hbase.hstore.blockingStoreFiles' => '10'}}

You can also unset configuration settings specific to this table:

  hbase> alter 't1', METHOD => 'table_conf_unset', NAME => 'hbase.hregion.majorcompaction'

You can also remove a table-scope attribute:

  hbase> alter 't1', METHOD => 'table_att_unset', NAME => 'MAX_FILESIZE'

  hbase> alter 't1', METHOD => 'table_att_unset', NAME => 'coprocessor$1'

You can also set REGION_REPLICATION:

  hbase> alter 't1', {REGION_REPLICATION => 2}

There could be more than one alteration in one command:

  hbase> alter 't1', { NAME => 'f1', VERSIONS => 3 },
   { MAX_FILESIZE => '134217728' }, { METHOD => 'delete', NAME => 'f2' },
   OWNER => 'johndoe', METADATA => { 'mykey' => 'myvalue' }

Command: alter_async
Alter column family schema, does not wait for all regions to receive the
schema changes. Pass table name and a dictionary specifying new column
family schema. Dictionaries are described on the main help command output.
Dictionary must include name of column family to alter. For example,

To change or add the 'f1' column family in table 't1' from defaults
to instead keep a maximum of 5 cell VERSIONS, do:

  hbase> alter_async 't1', NAME => 'f1', VERSIONS => 5

To delete the 'f1' column family in table 'ns1:t1', do:

  hbase> alter_async 'ns1:t1', NAME => 'f1', METHOD => 'delete'

or a shorter version:

  hbase> alter_async 'ns1:t1', 'delete' => 'f1'

You can also change table-scope attributes like MAX_FILESIZE,
MEMSTORE_FLUSHSIZE, and READONLY.

For example, to change the max size of a family to 128MB, do:

  hbase> alter 't1', METHOD => 'table_att', MAX_FILESIZE => '134217728'

There could be more than one alteration in one command:

  hbase> alter 't1', {NAME => 'f1'}, {NAME => 'f2', METHOD => 'delete'}

To check if all the regions have been updated, use alter_status <table_name>

Command: alter_status
Get the status of the alter command. Indicates the number of regions of the
table that have received the updated schema
Pass table name.

hbase> alter_status 't1'
hbase> alter_status 'ns1:t1'

Command: clone_table_schema
          Create a new table by cloning the existent table schema.
          There're no copies of data involved.
          Just copy the table descriptor and split keys.

          Passing 'false' as the optional third parameter will
          not preserve split keys.
          Examples:
            hbase> clone_table_schema 'table_name', 'new_table_name'
            hbase> clone_table_schema 'table_name', 'new_table_name', false

Command: create
Creates a table. Pass a table name, and a set of column family
specifications (at least one), and, optionally, table configuration.
Column specification can be a simple string (name), or a dictionary
(dictionaries are described below in main help output), necessarily
including NAME attribute.
Examples:

Create a table with namespace=ns1 and table qualifier=t1
  hbase> create 'ns1:t1', {NAME => 'f1', VERSIONS => 5}

Create a table with namespace=default and table qualifier=t1
  hbase> create 't1', {NAME => 'f1'}, {NAME => 'f2'}, {NAME => 'f3'}
  hbase> # The above in shorthand would be the following:
  hbase> create 't1', 'f1', 'f2', 'f3'
  hbase> create 't1', {NAME => 'f1', VERSIONS => 1, TTL => 2592000, BLOCKCACHE => true}
  hbase> create 't1', {NAME => 'f1', CONFIGURATION => {'hbase.hstore.blockingStoreFiles' => '10'}}
  hbase> create 't1', {NAME => 'f1', IS_MOB => true, MOB_THRESHOLD => 1000000, MOB_COMPACT_PARTITION_POLICY => 'weekly'}

Table configuration options can be put at the end.
Examples:

  hbase> create 'ns1:t1', 'f1', SPLITS => ['10', '20', '30', '40']
  hbase> create 't1', 'f1', SPLITS => ['10', '20', '30', '40']
  hbase> create 't1', 'f1', SPLITS_FILE => 'splits.txt', OWNER => 'johndoe'
  hbase> create 't1', {NAME => 'f1', VERSIONS => 5}, METADATA => { 'mykey' => 'myvalue' }
  hbase> # Optionally pre-split the table into NUMREGIONS, using
  hbase> # SPLITALGO ("HexStringSplit", "UniformSplit" or classname)
  hbase> create 't1', 'f1', {NUMREGIONS => 15, SPLITALGO => 'HexStringSplit'}
  hbase> create 't1', 'f1', {NUMREGIONS => 15, SPLITALGO => 'HexStringSplit', REGION_REPLICATION => 2, CONFIGURATION => {'hbase.hregion.scan.loadColumnFamiliesOnDemand' => 'true'}}
  hbase> create 't1', {NAME => 'f1', DFS_REPLICATION => 1}

You can also keep around a reference to the created table:

  hbase> t1 = create 't1', 'f1'

Which gives you a reference to the table named 't1', on which you can then
call methods.

Command: describe
Describe the named table. For example:
  hbase> describe 't1'
  hbase> describe 'ns1:t1'

Alternatively, you can use the abbreviated 'desc' for the same thing.
  hbase> desc 't1'
  hbase> desc 'ns1:t1'

Command: disable
Start disable of named table:
  hbase> disable 't1'
  hbase> disable 'ns1:t1'

Command: disable_all
Disable all of tables matching the given regex:

hbase> disable_all 't.*'
hbase> disable_all 'ns:t.*'
hbase> disable_all 'ns:.*'

Command: drop
Drop the named table. Table must first be disabled:
  hbase> drop 't1'
  hbase> drop 'ns1:t1'

Command: drop_all
Drop all of the tables matching the given regex:

hbase> drop_all 't.*'
hbase> drop_all 'ns:t.*'
hbase> drop_all 'ns:.*'

Command: enable
Start enable of named table:
  hbase> enable 't1'
  hbase> enable 'ns1:t1'

Command: enable_all
Enable all of the tables matching the given regex:

hbase> enable_all 't.*'
hbase> enable_all 'ns:t.*'
hbase> enable_all 'ns:.*'

Command: exists
Does the named table exist?
  hbase> exists 't1'
  hbase> exists 'ns1:t1'

Command: get_table
Get the given table name and return it as an actual object to
be manipulated by the user. See table.help for more information
on how to use the table.
Eg.

  hbase> t1 = get_table 't1'
  hbase> t1 = get_table 'ns1:t1'

returns the table named 't1' as a table object. You can then do

  hbase> t1.help

which will then print the help for that table.

Command: is_disabled
Is named table disabled? For example:
  hbase> is_disabled 't1'
  hbase> is_disabled 'ns1:t1'

Command: is_enabled
Is named table enabled? For example:
  hbase> is_enabled 't1'
  hbase> is_enabled 'ns1:t1'

Command: list
List all user tables in hbase. Optional regular expression parameter could
be used to filter the output. Examples:

  hbase> list
  hbase> list 'abc.*'
  hbase> list 'ns:abc.*'
  hbase> list 'ns:.*'

Command: list_regions
        List all regions for a particular table as an array and also filter them by server name (optional) as prefix
        and maximum locality (optional). By default, it will return all the regions for the table with any locality.
        The command displays server name, region name, start key, end key, size of the region in MB, number of requests
        and the locality. The information can be projected out via an array as third parameter. By default all these information
        is displayed. Possible array values are SERVER_NAME, REGION_NAME, START_KEY, END_KEY, SIZE, REQ and LOCALITY. Values
        are not case sensitive. If you don't want to filter by server name, pass an empty hash / string as shown below.

        Examples:
        hbase> list_regions 'table_name'
        hbase> list_regions 'table_name', 'server_name'
        hbase> list_regions 'table_name', {SERVER_NAME => 'server_name', LOCALITY_THRESHOLD => 0.8}
        hbase> list_regions 'table_name', {SERVER_NAME => 'server_name', LOCALITY_THRESHOLD => 0.8}, ['SERVER_NAME']
        hbase> list_regions 'table_name', {}, ['SERVER_NAME', 'start_key']
        hbase> list_regions 'table_name', '', ['SERVER_NAME', 'start_key']


Command: locate_region
Locate the region given a table name and a row-key

  hbase> locate_region 'tableName', 'key0'

Command: show_filters
Show all the filters in hbase. Example:
  hbase> show_filters

  ColumnPrefixFilter
  TimestampsFilter
  PageFilter
  .....
  KeyOnlyFilter
~~~

##### create(创建)

~~~ shell
hbase(main):030:0> help 'create'
Creates a table. Pass a table name, and a set of column family
specifications (at least one), and, optionally, table configuration.
Column specification can be a simple string (name), or a dictionary
(dictionaries are described below in main help output), necessarily
including NAME attribute.
Examples:

Create a table with namespace=ns1 and table qualifier=t1
  hbase> create 'ns1:t1', {NAME => 'f1', VERSIONS => 5}

Create a table with namespace=default and table qualifier=t1
  hbase> create 't1', {NAME => 'f1'}, {NAME => 'f2'}, {NAME => 'f3'}
  hbase> # The above in shorthand would be the following:
  hbase> create 't1', 'f1', 'f2', 'f3'
  hbase> create 't1', {NAME => 'f1', VERSIONS => 1, TTL => 2592000, BLOCKCACHE => true}
  hbase> create 't1', {NAME => 'f1', CONFIGURATION => {'hbase.hstore.blockingStoreFiles' => '10'}}
  hbase> create 't1', {NAME => 'f1', IS_MOB => true, MOB_THRESHOLD => 1000000, MOB_COMPACT_PARTITION_POLICY => 'weekly'}

Table configuration options can be put at the end.
Examples:

  hbase> create 'ns1:t1', 'f1', SPLITS => ['10', '20', '30', '40']
  hbase> create 't1', 'f1', SPLITS => ['10', '20', '30', '40']
  hbase> create 't1', 'f1', SPLITS_FILE => 'splits.txt', OWNER => 'johndoe'
  hbase> create 't1', {NAME => 'f1', VERSIONS => 5}, METADATA => { 'mykey' => 'myvalue' }
  hbase> # Optionally pre-split the table into NUMREGIONS, using
  hbase> # SPLITALGO ("HexStringSplit", "UniformSplit" or classname)
  hbase> create 't1', 'f1', {NUMREGIONS => 15, SPLITALGO => 'HexStringSplit'}
  hbase> create 't1', 'f1', {NUMREGIONS => 15, SPLITALGO => 'HexStringSplit', REGION_REPLICATION => 2, CONFIGURATION => {'hbase.hregion.scan.loadColumnFamiliesOnDemand' => 'true'}}
  hbase> create 't1', {NAME => 'f1', DFS_REPLICATION => 1}

You can also keep around a reference to the created table:

  hbase> t1 = create 't1', 'f1'

Which gives you a reference to the table named 't1', on which you can then
call methods.
~~~

 创建namespace

~~~
create_name 'ns1'
~~~

创建表

```
hbase(main):002:0> create 'ns1:users','user_id','address','info'
0 row(s) in 4.6300 seconds

=> Hbase::Table - users
```

##### disable

~~~ shell
Command: disable
Start disable of named table:
  hbase> disable 't1'
  hbase> disable 'ns1:t1'
~~~

##### drop

删除之前需要将表disable

~~~ shell
hbase(main):038:0* help 'drop'
Drop the named table. Table must first be disabled:
  hbase> drop 't1'
  hbase> drop 'ns1:t1'
~~~

#### dml

 Commands: append, count, delete, deleteall, get, get_counter, get_splits, incr, put, scan, truncate, truncate_preserve

~~~ shell
hbase(main):039:0> help 'dml'
Command: append
Appends a cell 'value' at specified table/row/column coordinates.

  hbase> append 't1', 'r1', 'c1', 'value', ATTRIBUTES=>{'mykey'=>'myvalue'}
  hbase> append 't1', 'r1', 'c1', 'value', {VISIBILITY=>'PRIVATE|SECRET'}

The same commands also can be run on a table reference. Suppose you had a reference
t to table 't1', the corresponding command would be:

  hbase> t.append 'r1', 'c1', 'value', ATTRIBUTES=>{'mykey'=>'myvalue'}
  hbase> t.append 'r1', 'c1', 'value', {VISIBILITY=>'PRIVATE|SECRET'}

Command: count
Count the number of rows in a table.  Return value is the number of rows.
This operation may take a LONG time (Run '$HADOOP_HOME/bin/hadoop jar
hbase.jar rowcount' to run a counting mapreduce job). Current count is shown
every 1000 rows by default. Count interval may be optionally specified. Scan
caching is enabled on count scans by default. Default cache size is 10 rows.
If your rows are small in size, you may want to increase this
parameter. Examples:

 hbase> count 'ns1:t1'
 hbase> count 't1'
 hbase> count 't1', INTERVAL => 100000
 hbase> count 't1', CACHE => 1000
 hbase> count 't1', INTERVAL => 10, CACHE => 1000
 hbase> count 't1', FILTER => "
    (QualifierFilter (>=, 'binary:xyz')) AND (TimestampsFilter ( 123, 456))"
 hbase> count 't1', COLUMNS => ['c1', 'c2'], STARTROW => 'abc', STOPROW => 'xyz'

The same commands also can be run on a table reference. Suppose you had a reference
t to table 't1', the corresponding commands would be:

 hbase> t.count
 hbase> t.count INTERVAL => 100000
 hbase> t.count CACHE => 1000
 hbase> t.count INTERVAL => 10, CACHE => 1000
 hbase> t.count FILTER => "
    (QualifierFilter (>=, 'binary:xyz')) AND (TimestampsFilter ( 123, 456))"
 hbase> t.count COLUMNS => ['c1', 'c2'], STARTROW => 'abc', STOPROW => 'xyz'

Command: delete
Put a delete cell value at specified table/row/column and optionally
timestamp coordinates.  Deletes must match the deleted cell's
coordinates exactly.  When scanning, a delete cell suppresses older
versions. To delete a cell from  't1' at row 'r1' under column 'c1'
marked with the time 'ts1', do:

  hbase> delete 'ns1:t1', 'r1', 'c1', ts1
  hbase> delete 't1', 'r1', 'c1', ts1
  hbase> delete 't1', 'r1', 'c1', ts1, {VISIBILITY=>'PRIVATE|SECRET'}

The same command can also be run on a table reference. Suppose you had a reference
t to table 't1', the corresponding command would be:

  hbase> t.delete 'r1', 'c1',  ts1
  hbase> t.delete 'r1', 'c1',  ts1, {VISIBILITY=>'PRIVATE|SECRET'}

Command: deleteall
Delete all cells in a given row; pass a table name, row, and optionally
a column and timestamp. Deleteall also support deleting a row range using a
row key prefix. Examples:

  hbase> deleteall 'ns1:t1', 'r1'
  hbase> deleteall 't1', 'r1'
  hbase> deleteall 't1', 'r1', 'c1'
  hbase> deleteall 't1', 'r1', 'c1', ts1
  hbase> deleteall 't1', 'r1', 'c1', ts1, {VISIBILITY=>'PRIVATE|SECRET'}

ROWPREFIXFILTER can be used to delete row ranges
  hbase> deleteall 't1', {ROWPREFIXFILTER => 'prefix'}
  hbase> deleteall 't1', {ROWPREFIXFILTER => 'prefix'}, 'c1'        //delete certain column family in the row ranges
  hbase> deleteall 't1', {ROWPREFIXFILTER => 'prefix'}, 'c1', ts1
  hbase> deleteall 't1', {ROWPREFIXFILTER => 'prefix'}, 'c1', ts1, {VISIBILITY=>'PRIVATE|SECRET'}

CACHE can be used to specify how many deletes batched to be sent to server at one time, default is 100
  hbase> deleteall 't1', {ROWPREFIXFILTER => 'prefix', CACHE => 100}


The same commands also can be run on a table reference. Suppose you had a reference
t to table 't1', the corresponding command would be:

  hbase> t.deleteall 'r1', 'c1', ts1, {VISIBILITY=>'PRIVATE|SECRET'}
  hbase> t.deleteall {ROWPREFIXFILTER => 'prefix', CACHE => 100}, 'c1', ts1, {VISIBILITY=>'PRIVATE|SECRET'}

Command: get
Get row or cell contents; pass table name, row, and optionally
a dictionary of column(s), timestamp, timerange and versions. Examples:

  hbase> get 'ns1:t1', 'r1'
  hbase> get 't1', 'r1'
  hbase> get 't1', 'r1', {TIMERANGE => [ts1, ts2]}
  hbase> get 't1', 'r1', {COLUMN => 'c1'}
  hbase> get 't1', 'r1', {COLUMN => ['c1', 'c2', 'c3']}
  hbase> get 't1', 'r1', {COLUMN => 'c1', TIMESTAMP => ts1}
  hbase> get 't1', 'r1', {COLUMN => 'c1', TIMERANGE => [ts1, ts2], VERSIONS => 4}
  hbase> get 't1', 'r1', {COLUMN => 'c1', TIMESTAMP => ts1, VERSIONS => 4}
  hbase> get 't1', 'r1', {FILTER => "ValueFilter(=, 'binary:abc')"}
  hbase> get 't1', 'r1', 'c1'
  hbase> get 't1', 'r1', 'c1', 'c2'
  hbase> get 't1', 'r1', ['c1', 'c2']
  hbase> get 't1', 'r1', {COLUMN => 'c1', ATTRIBUTES => {'mykey'=>'myvalue'}}
  hbase> get 't1', 'r1', {COLUMN => 'c1', AUTHORIZATIONS => ['PRIVATE','SECRET']}
  hbase> get 't1', 'r1', {CONSISTENCY => 'TIMELINE'}
  hbase> get 't1', 'r1', {CONSISTENCY => 'TIMELINE', REGION_REPLICA_ID => 1}

Besides the default 'toStringBinary' format, 'get' also supports custom formatting by
column.  A user can define a FORMATTER by adding it to the column name in the get
specification.  The FORMATTER can be stipulated:

 1. either as a org.apache.hadoop.hbase.util.Bytes method name (e.g, toInt, toString)
 2. or as a custom class followed by method name: e.g. 'c(MyFormatterClass).format'.

Example formatting cf:qualifier1 and cf:qualifier2 both as Integers:
  hbase> get 't1', 'r1' {COLUMN => ['cf:qualifier1:toInt',
    'cf:qualifier2:c(org.apache.hadoop.hbase.util.Bytes).toInt'] }

Note that you can specify a FORMATTER by column only (cf:qualifier). You can set a
formatter for all columns (including, all key parts) using the "FORMATTER"
and "FORMATTER_CLASS" options. The default "FORMATTER_CLASS" is
"org.apache.hadoop.hbase.util.Bytes".

  hbase> get 't1', 'r1', {FORMATTER => 'toString'}
  hbase> get 't1', 'r1', {FORMATTER_CLASS => 'org.apache.hadoop.hbase.util.Bytes', FORMATTER => 'toString'}

The same commands also can be run on a reference to a table (obtained via get_table or
create_table). Suppose you had a reference t to table 't1', the corresponding commands
would be:

  hbase> t.get 'r1'
  hbase> t.get 'r1', {TIMERANGE => [ts1, ts2]}
  hbase> t.get 'r1', {COLUMN => 'c1'}
  hbase> t.get 'r1', {COLUMN => ['c1', 'c2', 'c3']}
  hbase> t.get 'r1', {COLUMN => 'c1', TIMESTAMP => ts1}
  hbase> t.get 'r1', {COLUMN => 'c1', TIMERANGE => [ts1, ts2], VERSIONS => 4}
  hbase> t.get 'r1', {COLUMN => 'c1', TIMESTAMP => ts1, VERSIONS => 4}
  hbase> t.get 'r1', {FILTER => "ValueFilter(=, 'binary:abc')"}
  hbase> t.get 'r1', 'c1'
  hbase> t.get 'r1', 'c1', 'c2'
  hbase> t.get 'r1', ['c1', 'c2']
  hbase> t.get 'r1', {CONSISTENCY => 'TIMELINE'}
  hbase> t.get 'r1', {CONSISTENCY => 'TIMELINE', REGION_REPLICA_ID => 1}

Command: get_counter
Return a counter cell value at specified table/row/column coordinates.
A counter cell should be managed with atomic increment functions on HBase
and the data should be binary encoded (as long value). Example:

  hbase> get_counter 'ns1:t1', 'r1', 'c1'
  hbase> get_counter 't1', 'r1', 'c1'

The same commands also can be run on a table reference. Suppose you had a reference
t to table 't1', the corresponding command would be:

  hbase> t.get_counter 'r1', 'c1'

Command: get_splits
Get the splits of the named table:
  hbase> get_splits 't1'
  hbase> get_splits 'ns1:t1'

The same commands also can be run on a table reference. Suppose you had a reference
t to table 't1', the corresponding command would be:

  hbase> t.get_splits

Command: incr
Increments a cell 'value' at specified table/row/column coordinates.
To increment a cell value in table 'ns1:t1' or 't1' at row 'r1' under column
'c1' by 1 (can be omitted) or 10 do:

  hbase> incr 'ns1:t1', 'r1', 'c1'
  hbase> incr 't1', 'r1', 'c1'
  hbase> incr 't1', 'r1', 'c1', 1
  hbase> incr 't1', 'r1', 'c1', 10
  hbase> incr 't1', 'r1', 'c1', 10, {ATTRIBUTES=>{'mykey'=>'myvalue'}}
  hbase> incr 't1', 'r1', 'c1', {ATTRIBUTES=>{'mykey'=>'myvalue'}}
  hbase> incr 't1', 'r1', 'c1', 10, {VISIBILITY=>'PRIVATE|SECRET'}

The same commands also can be run on a table reference. Suppose you had a reference
t to table 't1', the corresponding command would be:

  hbase> t.incr 'r1', 'c1'
  hbase> t.incr 'r1', 'c1', 1
  hbase> t.incr 'r1', 'c1', 10, {ATTRIBUTES=>{'mykey'=>'myvalue'}}
  hbase> t.incr 'r1', 'c1', 10, {VISIBILITY=>'PRIVATE|SECRET'}

Command: put
Put a cell 'value' at specified table/row/column and optionally
timestamp coordinates.  To put a cell value into table 'ns1:t1' or 't1'
at row 'r1' under column 'c1' marked with the time 'ts1', do:

  hbase> put 'ns1:t1', 'r1', 'c1', 'value'
  hbase> put 't1', 'r1', 'c1', 'value'
  hbase> put 't1', 'r1', 'c1', 'value', ts1
  hbase> put 't1', 'r1', 'c1', 'value', {ATTRIBUTES=>{'mykey'=>'myvalue'}}
  hbase> put 't1', 'r1', 'c1', 'value', ts1, {ATTRIBUTES=>{'mykey'=>'myvalue'}}
  hbase> put 't1', 'r1', 'c1', 'value', ts1, {VISIBILITY=>'PRIVATE|SECRET'}

The same commands also can be run on a table reference. Suppose you had a reference
t to table 't1', the corresponding command would be:

  hbase> t.put 'r1', 'c1', 'value', ts1, {ATTRIBUTES=>{'mykey'=>'myvalue'}}

Command: scan
Scan a table; pass table name and optionally a dictionary of scanner
specifications.  Scanner specifications may include one or more of:
TIMERANGE, FILTER, LIMIT, STARTROW, STOPROW, ROWPREFIXFILTER, TIMESTAMP,
MAXLENGTH or COLUMNS, CACHE or RAW, VERSIONS, ALL_METRICS or METRICS

If no columns are specified, all columns will be scanned.
To scan all members of a column family, leave the qualifier empty as in
'col_family'.

The filter can be specified in two ways:
1. Using a filterString - more information on this is available in the
Filter Language document attached to the HBASE-4176 JIRA
2. Using the entire package name of the filter.

If you wish to see metrics regarding the execution of the scan, the
ALL_METRICS boolean should be set to true. Alternatively, if you would
prefer to see only a subset of the metrics, the METRICS array can be
defined to include the names of only the metrics you care about.

Some examples:

  hbase> scan 'hbase:meta'
  hbase> scan 'hbase:meta', {COLUMNS => 'info:regioninfo'}
  hbase> scan 'ns1:t1', {COLUMNS => ['c1', 'c2'], LIMIT => 10, STARTROW => 'xyz'}
  hbase> scan 't1', {COLUMNS => ['c1', 'c2'], LIMIT => 10, STARTROW => 'xyz'}
  hbase> scan 't1', {COLUMNS => 'c1', TIMERANGE => [1303668804000, 1303668904000]}
  hbase> scan 't1', {REVERSED => true}
  hbase> scan 't1', {ALL_METRICS => true}
  hbase> scan 't1', {METRICS => ['RPC_RETRIES', 'ROWS_FILTERED']}
  hbase> scan 't1', {ROWPREFIXFILTER => 'row2', FILTER => "
    (QualifierFilter (>=, 'binary:xyz')) AND (TimestampsFilter ( 123, 456))"}
  hbase> scan 't1', {FILTER =>
    org.apache.hadoop.hbase.filter.ColumnPaginationFilter.new(1, 0)}
  hbase> scan 't1', {CONSISTENCY => 'TIMELINE'}
For setting the Operation Attributes
  hbase> scan 't1', { COLUMNS => ['c1', 'c2'], ATTRIBUTES => {'mykey' => 'myvalue'}}
  hbase> scan 't1', { COLUMNS => ['c1', 'c2'], AUTHORIZATIONS => ['PRIVATE','SECRET']}
For experts, there is an additional option -- CACHE_BLOCKS -- which
switches block caching for the scanner on (true) or off (false).  By
default it is enabled.  Examples:

  hbase> scan 't1', {COLUMNS => ['c1', 'c2'], CACHE_BLOCKS => false}

Also for experts, there is an advanced option -- RAW -- which instructs the
scanner to return all cells (including delete markers and uncollected deleted
cells). This option cannot be combined with requesting specific COLUMNS.
Disabled by default.  Example:

  hbase> scan 't1', {RAW => true, VERSIONS => 10}

Besides the default 'toStringBinary' format, 'scan' supports custom formatting
by column.  A user can define a FORMATTER by adding it to the column name in
the scan specification.  The FORMATTER can be stipulated:

 1. either as a org.apache.hadoop.hbase.util.Bytes method name (e.g, toInt, toString)
 2. or as a custom class followed by method name: e.g. 'c(MyFormatterClass).format'.

Example formatting cf:qualifier1 and cf:qualifier2 both as Integers:
  hbase> scan 't1', {COLUMNS => ['cf:qualifier1:toInt',
    'cf:qualifier2:c(org.apache.hadoop.hbase.util.Bytes).toInt'] }

Note that you can specify a FORMATTER by column only (cf:qualifier). You can set a
formatter for all columns (including, all key parts) using the "FORMATTER"
and "FORMATTER_CLASS" options. The default "FORMATTER_CLASS" is
"org.apache.hadoop.hbase.util.Bytes".

  hbase> scan 't1', {FORMATTER => 'toString'}
  hbase> scan 't1', {FORMATTER_CLASS => 'org.apache.hadoop.hbase.util.Bytes', FORMATTER => 'toString'}

Scan can also be used directly from a table, by first getting a reference to a
table, like such:

  hbase> t = get_table 't'
  hbase> t.scan

Note in the above situation, you can still provide all the filtering, columns,
options, etc as described above.


Command: truncate
  Disables, drops and recreates the specified table.

Command: truncate_preserve
  Disables, drops and recreates the specified table while still maintaing the previous region boundaries.
~~~

测试数据

```shell
create 'user','info','ship';

put 'user', '524382618264914241', 'info:name', 'zhangsan'
put 'user', '524382618264914241', 'info:age',30
put 'user', '524382618264914241', 'info:height',168
put 'user', '524382618264914241', 'info:weight',168
put 'user', '524382618264914241', 'info:phone','13212321424'
put 'user', '524382618264914241', 'ship:addr','beijing'
put 'user', '524382618264914241', 'ship:email','sina@sina.com'
put 'user', '524382618264914241', 'ship:salary',3000

put 'user', '224382618261914241', 'info:name', 'lisi'
put 'user', '224382618261914241', 'info:age',24
put 'user', '224382618261914241', 'info:height',158
put 'user', '224382618261914241', 'info:weight',128
put 'user', '224382618261914241', 'info:phone','13213921424'
put 'user', '224382618261914241', 'ship:addr','chengdu'
put 'user', '224382618261914241', 'ship:email','qq@sina.com'
put 'user', '224382618261914241', 'ship:salary',5000

put 'user', '673782618261019142', 'info:name', 'zhaoliu'
put 'user', '673782618261019142', 'info:age',19
put 'user', '673782618261019142', 'info:height',178
put 'user', '673782618261019142', 'info:weight',188
put 'user', '673782618261019142', 'info:phone','17713921424'
put 'user', '673782618261019142', 'ship:addr','shenzhen'
put 'user', '673782618261019142', 'ship:email','126@sina.com'
put 'user', '673782618261019142', 'ship:salary',8000

put 'user', '813782218261011172', 'info:name', 'wangmazi'
put 'user', '813782218261011172', 'info:age',19
put 'user', '813782218261011172', 'info:height',158
put 'user', '813782218261011172', 'info:weight',118
put 'user', '813782218261011172', 'info:phone','12713921424'
put 'user', '813782218261011172', 'ship:addr','xian'
put 'user', '813782218261011172', 'ship:email','139@sina.com'
put 'user', '813782218261011172', 'ship:salary',10000

put 'user', '510824118261011172', 'info:name', 'yangyang'
put 'user', '510824118261011172', 'info:age',18
put 'user', '510824118261011172', 'info:height',188
put 'user', '510824118261011172', 'info:weight',138
put 'user', '510824118261011172', 'info:phone','18013921626'
put 'user', '510824118261011172', 'ship:addr','shanghai'
put 'user', '510824118261011172', 'ship:email','199@sina.com'
put 'user', '510824118261011172', 'ship:salary',50000
```

##### Get（读取）

```shell
hbase(main):041:0* help 'get'
Get row or cell contents; pass table name, row, and optionally
a dictionary of column(s), timestamp, timerange and versions. Examples:

  hbase> get 'ns1:t1', 'r1'
  hbase> get 't1', 'r1'
  hbase> get 't1', 'r1', {TIMERANGE => [ts1, ts2]}
  hbase> get 't1', 'r1', {COLUMN => 'c1'}
  hbase> get 't1', 'r1', {COLUMN => ['c1', 'c2', 'c3']}
  hbase> get 't1', 'r1', {COLUMN => 'c1', TIMESTAMP => ts1}
  hbase> get 't1', 'r1', {COLUMN => 'c1', TIMERANGE => [ts1, ts2], VERSIONS => 4}
  hbase> get 't1', 'r1', {COLUMN => 'c1', TIMESTAMP => ts1, VERSIONS => 4}
  hbase> get 't1', 'r1', {FILTER => "ValueFilter(=, 'binary:abc')"}
  hbase> get 't1', 'r1', 'c1'
  hbase> get 't1', 'r1', 'c1', 'c2'
  hbase> get 't1', 'r1', ['c1', 'c2']
  hbase> get 't1', 'r1', {COLUMN => 'c1', ATTRIBUTES => {'mykey'=>'myvalue'}}
  hbase> get 't1', 'r1', {COLUMN => 'c1', AUTHORIZATIONS => ['PRIVATE','SECRET']}
  hbase> get 't1', 'r1', {CONSISTENCY => 'TIMELINE'}
  hbase> get 't1', 'r1', {CONSISTENCY => 'TIMELINE', REGION_REPLICA_ID => 1}

Besides the default 'toStringBinary' format, 'get' also supports custom formatting by
column.  A user can define a FORMATTER by adding it to the column name in the get
specification.  The FORMATTER can be stipulated:

 1. either as a org.apache.hadoop.hbase.util.Bytes method name (e.g, toInt, toString)
 2. or as a custom class followed by method name: e.g. 'c(MyFormatterClass).format'.

Example formatting cf:qualifier1 and cf:qualifier2 both as Integers:
  hbase> get 't1', 'r1' {COLUMN => ['cf:qualifier1:toInt',
    'cf:qualifier2:c(org.apache.hadoop.hbase.util.Bytes).toInt'] }

Note that you can specify a FORMATTER by column only (cf:qualifier). You can set a
formatter for all columns (including, all key parts) using the "FORMATTER"
and "FORMATTER_CLASS" options. The default "FORMATTER_CLASS" is
"org.apache.hadoop.hbase.util.Bytes".

  hbase> get 't1', 'r1', {FORMATTER => 'toString'}
  hbase> get 't1', 'r1', {FORMATTER_CLASS => 'org.apache.hadoop.hbase.util.Bytes', FORMATTER => 'toString'}

The same commands also can be run on a reference to a table (obtained via get_table or
create_table). Suppose you had a reference t to table 't1', the corresponding commands
would be:

  hbase> t.get 'r1'
  hbase> t.get 'r1', {TIMERANGE => [ts1, ts2]}
  hbase> t.get 'r1', {COLUMN => 'c1'}
  hbase> t.get 'r1', {COLUMN => ['c1', 'c2', 'c3']}
  hbase> t.get 'r1', {COLUMN => 'c1', TIMESTAMP => ts1}
  hbase> t.get 'r1', {COLUMN => 'c1', TIMERANGE => [ts1, ts2], VERSIONS => 4}
  hbase> t.get 'r1', {COLUMN => 'c1', TIMESTAMP => ts1, VERSIONS => 4}
  hbase> t.get 'r1', {FILTER => "ValueFilter(=, 'binary:abc')"}
  hbase> t.get 'r1', 'c1'
  hbase> t.get 'r1', 'c1', 'c2'
  hbase> t.get 'r1', ['c1', 'c2']
  hbase> t.get 'r1', {CONSISTENCY => 'TIMELINE'}
  hbase> t.get 'r1', {CONSISTENCY => 'TIMELINE', REGION_REPLICA_ID => 1}
```

取得一个id的所有数据

```
hbase(main):003:0> get 'ns1:users','xiaoming'
COLUMN                               CELL                                                                                                      
 info:birthday                       timestamp=1594003730408, value=1987-06-17                                                                 
1 row(s) in 0.0710 seconds
```

获取单元格数据的版本数据

```
hbase(main):006:0> get 'ns1:users','xiaoming',{COLUMN=>'info:age',VERSIONS=>1}
COLUMN                               CELL                                                                                                      
 info:age                            timestamp=1594003806409, value=                                                                           
1 row(s) in 0.0040 seconds
```

##### Put（写 or 更新）

~~~shell
hbase(main):042:0> help 'put'
Put a cell 'value' at specified table/row/column and optionally
timestamp coordinates.  To put a cell value into table 'ns1:t1' or 't1'
at row 'r1' under column 'c1' marked with the time 'ts1', do:

  hbase> put 'ns1:t1', 'r1', 'c1', 'value'
  hbase> put 't1', 'r1', 'c1', 'value'
  hbase> put 't1', 'r1', 'c1', 'value', ts1
  hbase> put 't1', 'r1', 'c1', 'value', {ATTRIBUTES=>{'mykey'=>'myvalue'}}
  hbase> put 't1', 'r1', 'c1', 'value', ts1, {ATTRIBUTES=>{'mykey'=>'myvalue'}}
  hbase> put 't1', 'r1', 'c1', 'value', ts1, {VISIBILITY=>'PRIVATE|SECRET'}

The same commands also can be run on a table reference. Suppose you had a reference
t to table 't1', the corresponding command would be:

  hbase> t.put 'r1', 'c1', 'value', ts1, {ATTRIBUTES=>{'mykey'=>'myvalue'}}
~~~

写

```
hbase(main):002:0> put 'ns1:users','xiaoming','info:birthday','1987-06-17'
0 row(s) in 0.1910 seconds
```

  更新

~~~ shell
hbase(main):004:0> put 'ns1:users','xiaoming','info:age' ,''
0 row(s) in 0.0150 seconds

hbase(main):005:0> get 'ns1:users','xiaoming','info:age'
COLUMN                               CELL                                                                                                      
 info:age                            timestamp=1594003806409, value=                                                                           
1 row(s) in 0.0170 seconds
~~~

##### Scan（扫描）

~~~shell
hbase(main):043:0> help 'scan'
Scan a table; pass table name and optionally a dictionary of scanner
specifications.  Scanner specifications may include one or more of:
TIMERANGE, FILTER, LIMIT, STARTROW, STOPROW, ROWPREFIXFILTER, TIMESTAMP,
MAXLENGTH or COLUMNS, CACHE or RAW, VERSIONS, ALL_METRICS or METRICS

If no columns are specified, all columns will be scanned.
To scan all members of a column family, leave the qualifier empty as in
'col_family'.

The filter can be specified in two ways:
1. Using a filterString - more information on this is available in the
Filter Language document attached to the HBASE-4176 JIRA
2. Using the entire package name of the filter.

If you wish to see metrics regarding the execution of the scan, the
ALL_METRICS boolean should be set to true. Alternatively, if you would
prefer to see only a subset of the metrics, the METRICS array can be
defined to include the names of only the metrics you care about.

Some examples:

  hbase> scan 'hbase:meta'
  hbase> scan 'hbase:meta', {COLUMNS => 'info:regioninfo'}
  hbase> scan 'ns1:t1', {COLUMNS => ['c1', 'c2'], LIMIT => 10, STARTROW => 'xyz'}
  hbase> scan 't1', {COLUMNS => ['c1', 'c2'], LIMIT => 10, STARTROW => 'xyz'}
  hbase> scan 't1', {COLUMNS => 'c1', TIMERANGE => [1303668804000, 1303668904000]}
  hbase> scan 't1', {REVERSED => true}
  hbase> scan 't1', {ALL_METRICS => true}
  hbase> scan 't1', {METRICS => ['RPC_RETRIES', 'ROWS_FILTERED']}
  hbase> scan 't1', {ROWPREFIXFILTER => 'row2', FILTER => "
    (QualifierFilter (>=, 'binary:xyz')) AND (TimestampsFilter ( 123, 456))"}
  hbase> scan 't1', {FILTER =>
    org.apache.hadoop.hbase.filter.ColumnPaginationFilter.new(1, 0)}
  hbase> scan 't1', {CONSISTENCY => 'TIMELINE'}
For setting the Operation Attributes
  hbase> scan 't1', { COLUMNS => ['c1', 'c2'], ATTRIBUTES => {'mykey' => 'myvalue'}}
  hbase> scan 't1', { COLUMNS => ['c1', 'c2'], AUTHORIZATIONS => ['PRIVATE','SECRET']}
For experts, there is an additional option -- CACHE_BLOCKS -- which
switches block caching for the scanner on (true) or off (false).  By
default it is enabled.  Examples:

  hbase> scan 't1', {COLUMNS => ['c1', 'c2'], CACHE_BLOCKS => false}

Also for experts, there is an advanced option -- RAW -- which instructs the
scanner to return all cells (including delete markers and uncollected deleted
cells). This option cannot be combined with requesting specific COLUMNS.
Disabled by default.  Example:

  hbase> scan 't1', {RAW => true, VERSIONS => 10}

Besides the default 'toStringBinary' format, 'scan' supports custom formatting
by column.  A user can define a FORMATTER by adding it to the column name in
the scan specification.  The FORMATTER can be stipulated:

 1. either as a org.apache.hadoop.hbase.util.Bytes method name (e.g, toInt, toString)
 2. or as a custom class followed by method name: e.g. 'c(MyFormatterClass).format'.

Example formatting cf:qualifier1 and cf:qualifier2 both as Integers:
  hbase> scan 't1', {COLUMNS => ['cf:qualifier1:toInt',
    'cf:qualifier2:c(org.apache.hadoop.hbase.util.Bytes).toInt'] }

Note that you can specify a FORMATTER by column only (cf:qualifier). You can set a
formatter for all columns (including, all key parts) using the "FORMATTER"
and "FORMATTER_CLASS" options. The default "FORMATTER_CLASS" is
"org.apache.hadoop.hbase.util.Bytes".

  hbase> scan 't1', {FORMATTER => 'toString'}
  hbase> scan 't1', {FORMATTER_CLASS => 'org.apache.hadoop.hbase.util.Bytes', FORMATTER => 'toString'}

Scan can also be used directly from a table, by first getting a reference to a
table, like such:

  hbase> t = get_table 't'
  hbase> t.scan

Note in the above situation, you can still provide all the filtering, columns,
options, etc as described above.
~~~

扫描

```
hbase(main):007:0> scan 'ns1:users'
ROW                                  COLUMN+CELL                                                                                               
 xiaoming                            column=info:age, timestamp=1594003806409, value=                                                          
 xiaoming                            column=info:birthday, timestamp=1594003730408, value=1987-06-17                                           
1 row(s) in 0.0340 seconds
```

~~~ 
hbase(main):004:0> scan "u_analysis_prod:customer",{LIMIT => 1}
ROW                                          COLUMN+CELL                                                                                                                    
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:age, timestamp=1628030071439, value=\x00\x00\x00'                                                                    
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:age_group, timestamp=1628030071439, value=36-40YearsOld                                                              
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:alimop_survey, timestamp=1628187573464, value=\x00                                                                   
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:birthday, timestamp=1628030071439, value=1982-05-03                                                                  
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:birthday_month, timestamp=1628030071439, value=\x00\x00\x00\x05                                                      
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:birthday_month_day, timestamp=1628030071439, value=05-03                                                             
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:ce_selected, timestamp=1628187573464, value=\x00                                                                     
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:company, timestamp=1628030071439, value=CCO                                                                          
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:constellation, timestamp=1628030071439, value=Taurus                                                                 
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:elemod_survey, timestamp=1628187573464, value=\x00                                                                   
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:email, timestamp=1628030071439, value=7229505@qq.com                                                                 
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:email_desens, timestamp=1628030071439, value=***9505@qq.com                                                          
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:gender, timestamp=1628030071439, value=Female                                                                        
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:is_app, timestamp=1628030071439, value=\x00\x00\x00\x00                                                              
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:is_notification, timestamp=1628030071439, value=\x00\x00\x00\x00                                                     
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:language, timestamp=1628030071439, value=CHS                                                                         
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:level, timestamp=1628030071439, value=Green                                                                          
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:level_change, timestamp=1628030071439, value=LevelUnchanged                                                          
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:maxCountModSku2021DragonBoat, timestamp=1626220175772, value=                                                        
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:member_id, timestamp=1628030071439, value=1-1W4MBAUF                                                                 
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:member_uuid, timestamp=1628030071439, value=0000002a6e644fe9ad8bd3871a41ba91                                         
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:modSku20210601, timestamp=1624975403591, value=                                                                      
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:modSku20210717, timestamp=1627949176934, value=                                                                      
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:mod_survey, timestamp=1628187573464, value=\x00                                                                      
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:mop_survey, timestamp=1628187573464, value=\x00                                                                      
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:name, timestamp=1628030071439, value=\xE8\xA2\x81 \xE5\xAA\x9B                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:name_desens, timestamp=1628030071439, value=\xE8\xA2\x81**                                                           
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:phone_num, timestamp=1628030071439, value=18604412277                                                                
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:phone_num_desens, timestamp=1628030071439, value=186****2277                                                         
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:register_city, timestamp=1628030071439, value=changchun                                                              
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:register_date, timestamp=1628030071439, value=2018-10-24 16:00:00.0                                                  
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:register_province, timestamp=1628030071439, value=JiLin                                                              
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:register_region, timestamp=1628030071439, value=North China                                                          
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:register_source, timestamp=1628030071439, value=Others                                                               
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:register_store, timestamp=1628030071439, value=1-1H4BEN11                                                            
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:register_year, timestamp=1628030071439, value=\x00\x00\x03\xF7                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:star_group, timestamp=1628030071439, value=Below5                                                                    
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:star_num, timestamp=1628030071439, value=\x00\x00\x00\x00\x00\x00\x00\x00                                            
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:status, timestamp=1628030071439, value=Active                                                                        
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:storeSku20210601, timestamp=1624975403591, value=                                                                    
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:storeSku20210717, timestamp=1627949176934, value=                                                                    
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:storeSku2021DragonBoat, timestamp=1626220175772, value=                                                              
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:storeZongziSku2021DragonBoat, timestamp=1626220175772, value=                                                        
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:survey_risk_control, timestamp=1628188265811, value=\x00                                                             
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:update_time, timestamp=1628030071439, value=\x00\x00\x00\x00_\x94o\xE6                                               
 0000002a6e644fe9ad8bd3871a41ba91            column=cf:zongziStoreOrderCount2021DragonBoat, timestamp=1626220175772, value=\x00\x00\x00\x00                                 
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_1883, timestamp=1626197662779, value=1883                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_1888, timestamp=1626199370015, value=1888                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_1897, timestamp=1626200650988, value=1897                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_1910, timestamp=1626202424490, value=1910                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_1918, timestamp=1626204053285, value=1918                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_2082, timestamp=1626223089088, value=2082                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_2084, timestamp=1626224901820, value=2084                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_2093, timestamp=1626227511862, value=2093                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_3774, timestamp=1626244619427, value=3774                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_3776, timestamp=1626245131463, value=3776                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_3781, timestamp=1626247216591, value=3781                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_3996, timestamp=1626250541629, value=3996                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_3997, timestamp=1626251014385, value=3997                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_3998, timestamp=1626251883938, value=3998                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_4000, timestamp=1626251999982, value=4000                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_4006, timestamp=1626252162406, value=4006                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_4007, timestamp=1626252299843, value=4007                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_4008, timestamp=1626253187456, value=4008                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_4025, timestamp=1626253278375, value=4025                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_5319, timestamp=1626262952688, value=5319                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_6284, timestamp=1628030684909, value=6284                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_6285, timestamp=1628030980035, value=6285                                                                       
 0000002a6e644fe9ad8bd3871a41ba91            column=cf_:tag_6915, timestamp=1626292901077, value=6915   
~~~

##### Delete（删除）

~~~ shell
hbase(main):049:0* help 'delete'
Put a delete cell value at specified table/row/column and optionally
timestamp coordinates.  Deletes must match the deleted cell's
coordinates exactly.  When scanning, a delete cell suppresses older
versions. To delete a cell from  't1' at row 'r1' under column 'c1'
marked with the time 'ts1', do:

  hbase> delete 'ns1:t1', 'r1', 'c1', ts1
  hbase> delete 't1', 'r1', 'c1', ts1
  hbase> delete 't1', 'r1', 'c1', ts1, {VISIBILITY=>'PRIVATE|SECRET'}

The same command can also be run on a table reference. Suppose you had a reference
t to table 't1', the corresponding command would be:

  hbase> t.delete 'r1', 'c1',  ts1
  hbase> t.delete 'r1', 'c1',  ts1, {VISIBILITY=>'PRIVATE|SECRET'}
~~~

删除xiaoming值的'info:age'字段：

```
hbase(main):008:0> delete 'ns1:users','xiaoming','info:age'
0 row(s) in 0.0340 seconds

hbase(main):009:0> get 'ns1:users','xiaoming'
COLUMN                               CELL                                                                                                      
 info:birthday                       timestamp=1594003730408, value=1987-06-17                                                                 
1 row(s) in 0.0110 seconds
```

删除整行

```
hbase(main):010:0> deleteall 'ns1:users','xiaoming'
0 row(s) in 0.0170 seconds
```

统计表的行数

```
hbase(main):011:0> count 'ns1:users'
0 row(s) in 0.0260 seconds
```

清空表：

```
hbase(main):012:0> truncate 'ns1:users'
Truncating 'users' table (it may take a while):
 - Disabling table...
 - Truncating table...
0 row(s) in 4.2520 seconds
```

#### 版本

~~~ shell
create 'sut_test',{NAME =>'info' ,VERSIONS =>3}

put 'sut_test','1001','info:name','zhangsan'
put 'sut_test','1001','info:age','18'

#再插入两条年龄数据
put 'sut_test','1001','info:age','19'
put 'sut_test','1001','info:age','20'

#scan只会查询最新的版本数据

#获取age的所有版本
get 'sut_test','1001',{COLUMN =>'info:age' ,VERSIONS =>3}

#获取某个时间戳版本
get 'sut_test','1001',{COLUMN =>'info:age' ,TIMESTAMP =>1582204581662}
~~~



~~~ shell
hbase(main):061:0> get 'sut_test','1001',{COLUMN =>'info:age' ,VERSIONS =>3}
COLUMN                                CELL                                                                                                         
 info:age                             timestamp=1582204582448, value=20                                                                            
 info:age                             timestamp=1582204581662, value=19                                                                            
 info:age                             timestamp=1582204508964, value=18                                                                            
1 row(s) in 0.0130 seconds

hbase(main):062:0> get 'sut_test','1001',{COLUMN =>'info:age' ,VERSIONS =>2}
COLUMN                                CELL                                                                                                         
 info:age                             timestamp=1582204582448, value=20                                                                            
 info:age                             timestamp=1582204581662, value=19                                                                            
1 row(s) in 0.0050 seconds

hbase(main):063:0> get 'sut_test','1001',{COLUMN =>'info:age' ,VERSIONS =>1}
COLUMN                                CELL                                                                                                         
 info:age                             timestamp=1582204582448, value=20                                                                            
1 row(s) in 0.0030 seconds
~~~

## HBase Rowkey设计

​        一条数据的唯一标识就是 rowkey，那么这条数据存储于哪个分区，取决于 rowkey 处于哪个一个预分区的区间内，设计 rowkey 的主要目的 ，就是让数据均匀的分布于所有的 region 中，在一定程度上防止数据倾斜。接下来我们就谈一谈 rowkey 常用的设计方案。

#### 1. rowkey 长度原则

　　Rowkey 是一个二进制码流，Rowkey 的长度被很多开发者建议说设计在 10~100 个字节，不过建议是越短越好，不要超过 16 个字节，存为byte[]字节数组，**一般设计成定长的**。

　　原因如下：

　　　　1、数据的持久化文件 HFile 中是按照 KeyValue 存储的，如果 Rowkey 过长比如 100 个字 节，1000 万列数据光 Rowkey 就要占用 100*1000 万=10 亿个字节，将近 1G 数据，这会极大 影响 HFile 的存储效率；

　　　　2、MemStore 将缓存部分数据到内存，如果 Rowkey 字段过长内存的有效利用率会降低， 系统将无法缓存更多的数据，这会降低检索效率。因此 Rowkey 的字节长度越短越好。

　　　　3、目前操作系统是都是 64 位系统，内存 8 字节对齐。控制在 16 个字节，8 字节的整数 倍利用操作系统的最佳特性。

#### 2. rowkey 散列原则

　　如果 Rowkey 是按时间戳的方式递增，不要将时间放在二进制码的前面，建议将 Rowkey 的高位作为散列字段，由程序循环生成，低位放时间字段，这样将提高数据均衡分布在每个 Regionserver 实现负载均衡的几率。如果没有散列字段，首字段直接是时间信息将产生所有 新数据都在一个 RegionServer 上堆积的热点现象，这样在做数据检索的时候负载将会集中 在个别 RegionServer，降低查询效率。

​      row key是按照**字典序**存储，因此，设计row key时，要充分利用这个排序特点，将经常一起读取的数据存储到一块，将最近可能会被访问的数据放在一块。

举个例子：如果最近写入HBase表中的数据是最可能被访问的，可以考虑将时间戳作为row key的一部分，由于是字典序排序，所以可以使用Long.MAX_VALUE - timestamp作为row key，这样能保证新写入的数据在读取时可以被快速命中。

#### 3. rowkey 唯一原则

　　必须在设计上保证其唯一性。rowkey 是按照字典顺序排序存储的，因此，设计 rowkey 的时候，要充分利用这个排序的特点，将经常读取的数据存储到一块，将最近可能会被访问 的数据放到一块。

## HBase Hotspotting 数据热点

　　HBase 中的行是按照 rowkey 的字典顺序排序的，这种设计优化了 scan 操作，可以将相 关的行以及会被一起读取的行存取在临近位置，便于 scan。然而糟糕的 rowkey 设计是热点 的源头。 热点发生在大量的 client 直接访问集群的一个或极少数个节点（访问可能是读， 写或者其他操作）。大量访问会使热点 region 所在的单个机器超出自身承受能力，引起性能 下降甚至 region 不可用，这也会影响同一个 RegionServer 上的其他 region，由于主机无法服 务其他 region 的请求。 设计良好的数据访问模式以使集群被充分，均衡的利用。 为了避免写热点，设计 rowkey 使得不同行在同一个 region，但是在更多数据情况下，数据 应该被写入集群的多个 region，而不是一个。

**防止数据热点的有效措施：**

#### 1.加盐

　　这里所说的加盐不是密码学中的加盐，而是在 rowkey 的前面增加随机数，具体就是给 rowkey 分配一个随机前缀以使得它和之前的 rowkey 的开头不同。分配的前缀种类数量应该 和你想使用数据分散到不同的 region 的数量一致。加盐之后的 rowkey 就会根据随机生成的 前缀分散到各个 region 上，以避免热点。

例：生成随机数、**hash**、散列值

```
比如：
原本 rowKey 为 1001 的，SHA1 后变成：dd01903921ea24941c26a48f2cec24e0bb0e8cc7 
原本 rowKey 为 3001 的，SHA1 后变成：49042c54de64a1e9bf0b33e00245660ef92dc7bd 
原本 rowKey 为 5001 的，SHA1 后变成：7b61dec07e02c188790670af43e717f0f46e8913
在做此操作之前，一般我们会选择从数据集中抽取样本，来决定什么样的 rowKey 来 Hash
后作为每个分区的临界值。
```

#### 2.哈希

　　哈希会使同一行永远用一个前缀加盐。哈希也可以使负载分散到整个集群，但是读却是 可以预测的。使用确定的哈希可以让客户端重构完整的 rowkey，可以使用 get 操作准确获取 某一个行数据

#### 3.反转

　　第三种防止热点的方法是反转固定长度或者数字格式的 rowkey。这样可以使得 rowkey 中经常改变的部分（最没有意义的部分）放在前面。这样可以有效的随机 rowkey，但是牺牲了 rowkey 的有序性。

　　反转 rowkey 的例子以手机号为 rowkey，可以将手机号反转后的字符串作为 rowkey，这 样的就避免了以手机号那样比较固定开头导致热点问题

例： 字符串反转

```
20170524000001 转成 10000042507102
20170524000002 转成 20000042507102
```

这样也可以在一定程度上散列逐步 put 进来的数据。

例： 字符串拼接

```
20170524000001_a12e
20170524000001_93i7
```

#### 4.时间戳反转

　　一个常见的数据处理问题是快速获取数据的最近版本，使用反转的时间戳作为 rowkey 的一部分对这个问题十分有用，可以用 Long.Max_Value - timestamp 追加到 key 的末尾，例 如 [key][reverse_timestamp] , [key] 的最新值可以通过 scan [key]获得[key]的第一条记录，因 为 HBase 中 rowkey 是有序的，第一条记录是最后录入的数据。比如需要保存一个用户的操 作记录，按照操作时间倒序排序，在设计 rowkey 的时候，可以这样设计 [userId 反转][Long.Max_Value - timestamp]，在查询用户的所有操作记录数据的时候，直接指 定 反 转 后 的 userId ， startRow 是 [userId 反 转 ][000000000000],stopRow 是 [userId 反 转][Long.Max_Value - timestamp]

　　如果需要查询某段时间的操作记录，startRow 是[user 反转][Long.Max_Value - 起始时间]， stopRow 是[userId 反转][Long.Max_Value - 结束时间]

#### 5.预分区

##### 1)什么是预分区？

HBase表在刚刚被创建时，只有1个分区（region），当一个region过大（达到hbase.hregion.max.filesize属性中定义的阈值，默认10GB）时，

表将会进行split，分裂为2个分区。表在进行split的时候，会耗费大量的资源，频繁的分区对HBase的性能有巨大的影响。

HBase提供了预分区功能，即用户可以在创建表的时候对表按照一定的规则分区。

##### 2)预分区的目的是什么？

减少由于region split带来的资源消耗。从而提高HBase的性能。

##### 3)如何预分区？

通过HBase shell来创建。命令样例如下：

~~~ shell
create 't1', 'f1', SPLITS => ['10', '20', '30', '40']

create 't1', {NAME =>'f1', TTL => 180}, SPLITS => ['10', '20', '30', '40']

create 't1', {NAME =>'f1', TTL => 180}, {NAME => 'f2', TTL => 240}, SPLITS => ['10', '20', '30', '40']
~~~

## HBase API

具体见项目HBaseUtils

### 过滤器（Filter）

　　基础API中的查询操作在面对大量数据的时候是非常苍白的，这里Hbase提供了高级的查询方法：Filter。Filter可以根据簇、列、版本等更多的条件来对数据进行过滤，基于Hbase本身提供的三维有序（主键有序、列有序、版本有序），这些Filter可以高效的完成查询过滤的任务。带有Filter条件的RPC查询请求会把Filter分发到各个RegionServer，是一个服务器端（Server-side）的过滤器，这样也可以降低网络传输的压力。

　　要完成一个过滤的操作，至少需要两个参数。**一个是抽象的操作符**，Hbase提供了枚举类型的变量来表示这些抽象的操作符：LESS/LESS_OR_EQUAL/EQUAL/NOT_EUQAL等；**另外一个就是具体的比较器（Comparator）**，代表具体的比较逻辑，如果可以提高字节级的比较、字符串级的比较等。有了这两个参数，我们就可以清晰的定义筛选的条件，过滤数据。

#### 1.**抽象操作符（比较运算符）**

> LESS <
>
> LESS_OR_EQUAL <=
>
> EQUAL =
>
> NOT_EQUAL <>
>
> GREATER_OR_EQUAL >=
>
> GREATER >
>
> NO_OP 排除所有

#### 2.**比较器（指定比较机制）**

> BinaryComparator 按字节索引顺序比较指定字节数组，采用 Bytes.compareTo(byte[])
>
> BinaryPrefixComparator 跟前面相同，只是比较左端的数据是否相同
>
> NullComparator 判断给定的是否为空
>
> BitComparator 按位比较
>
> RegexStringComparator 提供一个正则的比较器，仅支持 EQUAL 和非 EQUAL
>
> SubstringComparator 判断提供的子串是否出现在 value 中

#### 3.过滤器

- 行键过滤器 RowFilter
- 列簇过滤器 FamilyFilter
- 列过滤器 QualifierFilter
- 值过滤器 ValueFilter
- 时间戳过滤器 TimestampsFilter
- 单列值过滤器 SingleColumnValueFilter
- 单列值排除器 SingleColumnValueExcludeFilter 
- 前缀过滤器 PrefixFilter----针对行键
- 列前缀过滤器 ColumnPrefixFilter
- 分页过滤器 PageFilter

## Hive集成

注意：HBase 与 Hive 的集成在版本中兼容问题。

环境准备

因为我们后续可能会在操作 Hive 的同时对 HBase 也会产生影响，所以 Hive 需要持有操作HBase 的 Jar，那么接下来拷贝 Hive 所依赖的 Jar 包（或者使用软连接的形式）。

```
$ export HBASE_HOME=/opt/modules/hbase-1.2.6
$ export HIVE_HOME=/opt/modules/hive-2.3.3

$ ln -s $HBASE_HOME/lib/hbase-common-1.2.6.jar             $HIVE_HOME/lib/hbase-common-1.2.6.jar
$ ln -s $HBASE_HOME/lib/hbase-server-1.2.6.jar             $HIVE_HOME/lib/hbase-server-1.2.6.jar
$ ln -s $HBASE_HOME/lib/hbase-client-1.2.6.jar             $HIVE_HOME/lib/hbase-client-1.2.6.jar
$ ln -s $HBASE_HOME/lib/hbase-protocol-1.2.6.jar           $HIVE_HOME/lib/hbase-protocol-1.2.6.jar
$ ln -s $HBASE_HOME/lib/hbase-it-1.2.6.jar                 $HIVE_HOME/lib/hbase-it-1.2.6.jar
$ ln -s $HBASE_HOME/lib/htrace-core-3.1.0-incubating.jar   $HIVE_HOME/lib/htrace-core-3.1.0-incubating.jar
$ ln -s $HBASE_HOME/lib/hbase-hadoop2-compat-1.2.6.jar     $HIVE_HOME/lib/hbase-hadoop2-compat-1.2.6.jar
$ ln -s $HBASE_HOME/lib/hbase-hadoop-compat-1.2.6.jar      $HIVE_HOME/lib/hbase-hadoop-compat-1.2.6.jar
```

同时在 **hive-site.xml** 中修改 **zookeeper** 的属性，如下：

```
<property>
<name>hive.zookeeper.quorum</name>
<value>node21,node22,node23</value>
<description>The list of ZooKeeper servers to talk to. This is only needed for read/write locks.</description>
</property>
<property>
<name>hive.zookeeper.client.port</name>
<value>2181</value>
<description>The port of ZooKeeper servers to talk to. This is only needed for read/write locks.</description>
</property>
```

### 2.1. 案例一

目标：建立 Hive 表，关联 HBase 表，插入数据到 Hive 表的同时能够影响 HBase 表。分步实现：

(1) 在 **Hive** 中创建表同时关联 **HBase**

```HQ
CREATE TABLE hive_hbase_emp_table( 
empno int,
ename string, 
job string,
mgr int,
hiredate string, 
sal double, 
comm double,
deptno int)
STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler' WITH SERDEPROPERTIES ("hbase.columns.mapping" =
":key,info:ename,info:job,info:mgr,info:hiredate,info:sal,info:comm,info:deptno")
TBLPROPERTIES ("hbase.table.name" = "hbase_emp_table");
```

尖叫提示：完成之后，可以分别进入 Hive 和 HBase 查看，都生成了对应的表

(2) 在 **Hive** 中创建临时中间表，用于 **load** 文件中的数据

尖叫提示：不能将数据直接 load 进 Hive 所关联 HBase 的那张表中

```
CREATE TABLE emp(
empno int,
ename string, 
job string,
mgr int,
hiredate string, 
sal double, 
comm double, 
deptno int)
row format delimited fields terminated by '\t';
```

(3) 向 **Hive** 中间表中 **load** 数据

```
hive> load data local inpath '/opt/data/emp.txt' into table emp;
```

(4) 通过 **insert** 命令将中间表中的数据导入到 **Hive** 关联 **HBase** 的那张表中

```
hive> insert into table hive_hbase_emp_table select * from emp;
```

(5) 查看 **Hive** 以及关联的 **HBase** 表中是否已经成功的同步插入了数据

Hive：

```
hive> select * from hive_hbase_emp_table;
```

HBase：

```
hbase> scan ‘hbase_emp_table’
```

### 2.2. 案例二

目标：在 HBase 中已经存储了某一张表 hbase_emp_table，然后在 Hive 中创建一个外部表来，

关联 HBase 中的 hbase_emp_table 这张表，使之可以借助 Hive 来分析 HBase 这张表中的数据。

注：该案例 2 紧跟案例 1 的脚步，所以完成此案例前，请先完成案例 1。分步实现：

(1) 在 **Hive** 中创建外部表

```
CREATE EXTERNAL TABLE relevance_hbase_emp(
empno int,
ename string,
job string,
mgr int,
hiredate string,
sal double,
comm double,
deptno int) 
STORED BY
'org.apache.hadoop.hive.hbase.HBaseStorageHandler' WITH SERDEPROPERTIES ("hbase.columns.mapping" =
":key,info:ename,info:job,info:mgr,info:hiredate,info:sal,info:comm,info:deptno")
TBLPROPERTIES ("hbase.table.name" = "hbase_emp_table");
```

其他建表例子：

~~~ hql
CREATE EXTERNAL TABLE `dw_dv1.hbase_wechat_customer_prod`(
  `wechat_uuid` string COMMENT 'from deserializer', 
  `uuid` string COMMENT 'from deserializer', 
  `unionid` string COMMENT 'from deserializer', 
  `openid_wsg` string COMMENT 'from deserializer', 
  `appid_wsg` string COMMENT 'from deserializer', 
  `openid_oachina` string COMMENT 'from deserializer', 
  `appid_oachina` string COMMENT 'from deserializer', 
  `openid_oajzh` string COMMENT 'from deserializer', 
  `appid_oajzh` string COMMENT 'from deserializer', 
  `openid_miniapp` string COMMENT 'from deserializer', 
  `appid_miniapp` string COMMENT 'from deserializer', 
  `bind_flag_miniapp` string COMMENT 'from deserializer', 
  `bind_time_miniapp` string COMMENT 'from deserializer', 
  `nickname` string COMMENT 'from deserializer', 
  `language` string COMMENT 'from deserializer', 
  `sex` string COMMENT 'from deserializer', 
  `country` string COMMENT 'from deserializer', 
  `province` string COMMENT 'from deserializer', 
  `city` string COMMENT 'from deserializer', 
  `subscribe_time` string COMMENT 'from deserializer')
ROW FORMAT SERDE 
  'org.apache.hadoop.hive.hbase.HBaseSerDe' 
STORED BY 
  'org.apache.hadoop.hive.hbase.HBaseStorageHandler' 
WITH SERDEPROPERTIES ( 
  'hbase.columns.mapping'=':key,cf:member_uuid,cf:unionID,cf:openid_wsg,cf:appid_wsg,cf:openid_oachina,cf:appid_oachina,cf:openid_oajzh,cf:appid_oajzh,cf:openid_miniapp,cf:appid_miniapp,cf:bind_flag_miniapp,cf:bind_time_miniapp,cf:nickname,cf:language,cf:sex,cf:country,cf:province,cf:city,cf:subscribe_time', 
  'serialization.format'='1')
TBLPROPERTIES (
  'hbase.table.name'='u_analysis:wechat_customer', 
  'last_modified_by'='User_RS', 
  'last_modified_time'='1627245923', 
  'transient_lastDdlTime'='1627245923')
;
~~~

(2) 关联后就可以使用 **Hive** 函数进行一些分析操作了

```
hive (default)> select * from relevance_hbase_emp;
```

## HBase与Sqoop集成

### **1.** 概念

Sqoop supports additional import targets beyond HDFS and Hive. Sqoop can also import records into a table in HBase.

之前我们已经学习过如何使用 Sqoop 在 Hadoop 集群和关系型数据库中进行数据的导入导出

工作，接下来我们学习一下利用 Sqoop 在 HBase 和 RDBMS 中进行数据的转储。

相关参数：

| 参数                       | 描述                                                         |
| -------------------------- | ------------------------------------------------------------ |
| --column-family <family>   | Sets the target column family for the import设置导入的目标列族。 |
| --hbase-create-table       | If specified, create missing HBase tables是否自动创建不存在的 HBase 表（这就意味着，不需要手动提前在 HBase 中先建立表） |
| --hbase-row-key <col>      | Specifies which input column to use as the row key.In case, if input table contains composite key, then <col> must be in the form of acomma-separated list of composite key attributes.mysql 中哪一列的值作为 HBase 的 rowkey，如果rowkey 是个组合键，则以逗号分隔。（注：避免 rowkey 的重复） |
| --hbase-table <table-name> | Specifies an HBase table to use as the target instead of HDFS.指定数据将要导入到 HBase 中的哪张表中。 |
| --hbase-bulkload           | Enables bulk loading.是否允许 bulk 形式的导入。              |

### **2.** 案例一

目标：将 RDBMS 中的数据抽取到 HBase 中分步实现：

(1) 配置 **sqoop-env.sh**，添加如下内容：

```
export HBASE_HOME=/opt/module/hbase-1.2.6
```

(2) 在 **Mysql** 中新建一个数据库 **db_library**，一张表 **book**

```
CREATE DATABASE db_library; 
CREATE TABLE db_library.book(
id int(4) PRIMARY KEY NOT NULL AUTO_INCREMENT,
name VARCHAR(255) NOT NULL,
price VARCHAR(255) NOT NULL);
```

(3) 向表中插入一些数据

```
INSERT INTO db_library.book (name, price) VALUES('Lie Sporting', '30'); 
INSERT INTO db_library.book (name, price) VALUES('Pride & Prejudice', '70');
INSERT INTO db_library.book (name, price) VALUES('Fall of Giants', '50');
```

(4) 执行 **Sqoop** 导入数据的操作

```
$ bin/sqoop import \
--connect jdbc:mysql://node21:3306/db_library \
--username root \
--password 123456 \
--table book \
--columns "id,name,price" \
--column-family "info" \
--hbase-create-table \
--hbase-row-key "id" \
--hbase-table "hbase_book" \
--num-mappers 1 \
--split-by id
```

提示：sqoop1.4.7 只支持 HBase1.0.1 之前的版本的自动创建 HBase 表的功能

解决方案：手动创建 HBase 表

```
hbase> create 'hbase_book','info'
```

(5) 在 **HBase** 中 **scan** 这张表得到如下内容

```
hbase> scan ‘hbase_book’
```

思考：尝试使用复合键作为导入数据时的 rowkey。

## BulkLoad 

### 原理

批量装载特性采用 MapReduce 任务，将表数据输出为HBase的内部数据格式，然后可以将产生的存储文件直接装载到运行的集群中。批量装载比简单使用 HBase API 消耗更少的CPU和网络资源。

#### 1.加载数据到HBase的三种方法：

- 通过MR job，使用TableOutputFormat加载到表中。（效率较低）
  核心的原理还是使用htable的put方法，不过由于使用了mapreduce分布式提交到hbase，速度比单线程效率高出许多。
- 通过客户端API，写入表中。（效率较低）
- 通过Bulk load 运行MR job将数据输出为hbase内部格式，再加载数据到集群。（使用更少的CPU和网络资源）

#### 2.Bulk load两步走

- 生成HFile

  - 命令行

    ~~~ shell
    # 生成hfile
    hadoop jar hbase-version.jar importtsv -Dimporttsv.columns=HBASE_ROW_KEY,c1,c2 -Dimporttsv.bulk.output=tmp
    hbase_table hdfs_file
    # 导入hbase
    hadoop jar hbase-version.jar completebulkload /user/hadoop/tmp/cf hbase_table
    ~~~

  - 代码

    通过MR job，使用HFileOutputFormat2，生成StoreFiles。
    每一个输出的HFile 文件，都在一个单独的region内，所以需要使用TotalOrderPartitioner 进行分区。
    保证map任务的输出为相互不交叉的主键空间范围，也就是对应hbase中region里的主键范围。

- 完成文件加载，将数据导入hbase集群中。
  完成数据加载有两种方式：

  - 命令行---completebulkload 

  ~~~ shell
  hadoop jar hbase-VERSION.jar completebulkload [-c /path/to/hbase/config/hbase-site.xml] /user/todd/myoutput mytable
  ~~~

  - 命令行---LoadIncrementalHFiles

  ~~~ shell
  hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles HDFS_Dir_Path HBase_table_name
  ~~~

  - 代码---通过HFileOutputFormat2 类编写 MapReduce 程序来生成 HFile 。
    案例1：https://github.com/jrkinley/hbase-bulk-import-example
    案例2：https://www.cnblogs.com/smartloli/p/9501887.html

  - 代码---Spark bulk load：Using thin record bulk load.
    案例1：（Hbase官网文档）Chapter 110 Bulk Load



### 测试

使用mr生成的HFile，加载的时候出现问题：

具体见项目中 GenerateHFileForBulkLoad 

代码测试，遇到报错

~~~ shell
2021-08-09 15:19:33,957 ERROR [main] tool.LoadIncrementalHFiles: -------------------------------------------------
Bulk load aborted with some files not yet loaded:
-------------------------------------------------
  file:/C:/Users/32006/Desktop/test/hbase/output/info/d3303af77b7c4ca1a9b2652bc8d515fd

Exception in thread "main" java.io.IOException: Retry attempted 20 times without completing, bailing out
	at org.apache.hadoop.hbase.tool.LoadIncrementalHFiles.performBulkLoad(LoadIncrementalHFiles.java:444)
	at org.apache.hadoop.hbase.tool.LoadIncrementalHFiles.doBulkLoad(LoadIncrementalHFiles.java:367)
	at org.apache.hadoop.hbase.tool.LoadIncrementalHFiles.doBulkLoad(LoadIncrementalHFiles.java:281)
	at com.wyb.GenerateHFileForBulkLoad.main(GenerateHFileForBulkLoad.java:108)
~~~

猜测应该是本地的hbase在docker里，考虑到Bulkload实质是将产生的存储文件直接装载到运行的集群中，idea 运行的程序应该不能完成数据的移动到docker，毕竟就开了几个端口

拿到docker里面用命令行执行一下：

~~~ shell
# 同步本地文件到docker
docker cp C:\Users\32006\Desktop\test\hbase\output myhbase:/
# 进入docker 查看文件
ls /output/
_SUCCESS  info
# 执行 LoadIncrementalHFiles 命令
hbase org.apache.hadoop.hbase.mapreduce.LoadIncrementalHFiles /output bulkload
# 无报错，查看一下hbase下对应的表 bulkload
hbase(main):021:0> scan 'bulkload'
ROW                             COLUMN+CELL
 1                              column=info:www.baidu.com, timestamp=1628493572491, value=baidu
 2                              column=info:www.huawei.com, timestamp=1628493572491, value=huawei
 3                              column=info:www.jd.com, timestamp=1628493572491, value=jd
 4                              column=info:www.taobao.com, timestamp=1628493572491, value=taobao
 5                              column=info:www.alibaba.com, timestamp=1628493572491, value=alibaba
5 row(s)
Took 0.5542 seconds

# docker 环境没有hadoop 就不测试completebulkload 
~~~

