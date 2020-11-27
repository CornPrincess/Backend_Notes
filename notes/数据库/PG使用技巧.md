PostgreSQL的功能非常强大，但是要把PostgreSQL用好，开发人员是非常关键的。



下面列列举一些常见的设计约束请大家遵守:

对于频繁更新的表，建议建表时指定表的fillfactor=85，每页预留15%的空间给HOT更新使用


create table test123(id int, info text) with(fillfactor=85);

有大量历史数据删除需求的业务，表按时间分区，删除时不要使用DELETE操作，而是DROP或者TRUNCATE对应的表

设计时应选择合适的数据类型，能用数字的坚决不用字符串,使用好的数据类型，可以使用数据库的索引，操作符，函数，提高数据的查询效率。

不推荐使用前模糊查询例如like %xxxx, 不能使用索引

应该对频繁访问的大表（通常指超过8GB的表，或者超过1000万记录的表）进行分区，从而提升查询的效率、更新的效率、备份与恢复的效率、建索引的效率等等

创建索引时加CONCURRENTLY关键字，就可以并行创建，不会堵塞DML操作，否则会堵塞DML操作: create index CONCURRENTLY idx on tbl(id);

防止长连接，占用过多的relcache, syscache。
当系统中有很多张表时，元数据会比较庞大，例如1万张表可能有上百MB的元数据，如果一个长连接的会话，访问到了所有的对象，则可能会长期占用这些syscache和relcache。
建议遇到这种情况时，定期释放长连接，重新建立连接，例如每个小时释放一次长连接

缩短事务操作时间, 推荐使用小事务, 避免引起表膨胀. 不要在事务中做应用层复杂计算,

判断表是否有记录不要用count, 用select 1 from tbl where xxx limit 1; 

避免频繁创建和删除临时表，以减少系统表资源的消耗，因为创建临时表会产生元数据，频繁创建，元数据可能会出现碎片

不要使用offset limit来翻页, 效率很低; 改进方法是, 在表中设计一个有序字段进行分页.

不建议对宽表频繁的更新，原因是PG目前的引擎是多版本的，更新后会产生新的版本，如果对宽表的某几个少量的字段频繁更新，其实是存在写放大的。
建议将此类宽表的不更新或更新不频繁的列与频繁更新的列拆分成两张表，通过PK进行关联。
查询是通过PK关联查询出结果即可。

OLTP系统不要频繁的使用聚合操作，聚合操作消耗较大的CPU与IO资源。例如实时的COUNT操作，如果并发很高，可能导致CPU资源撑爆。
对于实时性要求不高的场景，可以使用定期操作COUNT，并将COUNT数据缓存在缓存系统中的方式

避免在 where 子句中使用!=或<>操作符，否则将引擎放弃使用索引而进行全表扫描。

对于经常变更，或者新增，删除记录的表，应该尽量加快这种表的统计信息采样频率，获得较实时的采样，输出较好的执行计划。
例如
当垃圾达到表的千分之五时，自动触发垃圾回收。
当数据变化达到表的百分之一时，自动触发统计信息的采集。 create table t21(id int, info text) with (autovacuum_enabled=on, toast.autovacuum_enabled=on, autovacuum_vacuum_scale_factor=0.005, toast.autovacuum_vacuum_scale_factor=0.005, autovacuum_analyze_scale_factor=0.01, autovacuum_vacuum_cost_delay=0, toast.autovacuum_vacuum_cost_delay=0);

PG优化器可以动态调整JOIN的顺序，获取更好的执行计划，也可以使用 , 条件是 : 必须使用显示的JOIN，其次将join_collapse_limit设置为1 .

b-tree索引优化，不建议对频繁访问的数据上使用非常离散的数据，例如UUID作为索引，索引页会频繁的分裂，重锁，重IO和CPU开销都比较高。
如何降低频繁更新索引字段的索引页IO，设置fillfactor为一个合适的值，默认90已经适合大部分场景
