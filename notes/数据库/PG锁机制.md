
一、概述

> pg默认的隔离基本是read commit，具体可参考  https://www.v2ex.com/t/185244

　　此文档主要对Postgresql 锁机制进行分析，在讲解的过程中结合实验，理解Postgresql的锁机制。



二、表级锁类型



　　表级锁类型分为八种，以下对各种表级锁类型进行简单介绍下, 锁的冲突模式可以参考3.1的图一:表级锁冲突模式。



2.1 Access share

　“Access share”锁模式只与“Access Exclusive” 锁模式冲突;

　　查询命令（Select command）将会在它查询的表上获取”Access Shared” 锁,一般地，任何一个对表上的只读查询操作都将获取这种类型的锁。



2.2 Row share

　“Row Share” 锁模式与”Exclusive’和”Access Exclusive”锁模式冲突;

　 ”Select for update”和”Select for share”命令将获得这种类型锁，并且所有被引用但没有 FOR UPDATE 的表上会加上”Access shared locks”锁。



2.3 Row exclusive

　“Row exclusive” 与 “Share,Shared roexclusive,Exclusive,Access exclusive”模式冲突;

　“Update,Delete,Insert”命令会在目标表上获得这种类型的锁，并且在其它被引用的表上加上”Access shared”锁,一般地，更改表数据的命令都将在这张表上获得”Row exclusive”锁。



2.4 Share update exclusive

　”Share update exclusive, Share,Share row ,exclusive,exclusive,Access exclusive”模式冲突，这种模式保护一张表不被并发的模式更改和VACUUM;

“Vacuum(without full), Analyze ”和 “Create index concurrently”命令会获得这种类型锁。



2.5 Share

　　与“Row exclusive,Shared update exclusive,Share row exclusive ,Exclusive,Access exclusive”锁模式冲突,这种模式保护一张表数据不被并发的更改;

　“Create index”命令会获得这种锁模式。



2.6 Share row exclusive

　与“Row exclusive,Share update exclusive,Shared,Shared row exclusive,Exclusive,Access Exclusive”锁模式冲突;

　　任何Postgresql 命令不会自动获得这种锁。



2.7 Exclusive

　　与” ROW SHARE, ROW EXCLUSIVE, SHARE UPDATE EXCLUSIVE, SHARE, SHARE ROW EXCLUSIVE, EXCLUSIVE, ACCESS EXCLUSIVE”模式冲突,这种索模式仅能与Access Share 模式并发,换句话说，只有读操作可以和持有”EXCLUSIVE”锁的事务并行；

　　任何Postgresql 命令不会自动获得这种类型的锁；



2.8 Access exclusive

　　与所有模式锁冲突(ACCESS SHARE, ROW SHARE, ROW EXCLUSIVE, SHARE UPDATE EXCLUSIVE, SHARE, SHARE ROW EXCLUSIVE, EXCLUSIVE, and ACCESS EXCLUSIVE),这种模式保证了当前只有一个事务访问这张表;

　“ALTER TABLE, DROP TABLE, TRUNCATE, REINDEX, CLUSTER, VACUUM FULL” 命令会获得这种类型锁，在Lock table 命令中，如果没有申明其它模式，它也是缺省模式。



三、表级锁冲突模式

3.1 Conflicting lock modes

image.png

备注： 上图是Postgresql 表级锁的各种冲突模式对照表，红色的‘X’表示冲突项, 在章节四中会对其中典型的锁模式进行模似演示。



3.2 锁类型对应的数据库操作。



锁类型	                           对应的数据库操作                                                                       互斥

Access share	                   select                                                                                         Access Exclusive

Row share	                   select for update, select for share

Row exclusive	                   update,delete,insert                                                                 与 “Share,Shared row exclusive,Exclusive,Access exclusive”模式冲突

Share update exclusive	   vacuum(without full),analyze,create index concurrently

Share	                           create index

Share row exclusive	   任何Postgresql命令不会自动获得这种锁                              

Exclusive	                           任何Postgresql命令不会自动获得这种类型的锁                           仅能与Access Share 模式并发

Access exclusive	           alter table,drop table,truncate,reindex,cluster,vacuum full       与所有模式锁冲突，保证了当前只有一个事务访问这张表





四、实验



在这一章节中将会对图一中比较典型的锁冲突进行模似演练，了解这些在Postgresql DBA的日常维护工作中很有帮助，同时也能减少人为故障的发生。



4.1 Access exclusive 锁与Access share锁冲突



　在日常维护中，　大家应该执行过’ALTER TABLE’更改表结构的DDL，例如加字段，更改字段数据类型等，

根据章节二的理论，在执行’ALTER TABLE’命令时将申请一个Access exclusive锁, 根据图一，大家知道Access exclusive 锁和所有的锁模式都冲突，那么，它将会’Select’命令冲突，因为Select 加的是Access share锁，那么真的会与‘SELECT‘命令冲突吗，接下来给大家演示下:



创建一张测试表 test_2 并插入测试数据

mydb=> create table test_2 (id integer,name varchar(32));

CREATE TABLE

1

2

mydb=> insert into test_2 values (1,'franc');

INSERT 0 1



mydb=> insert into test_2 values (2,'tan');

INSERT 0 1



mydb=> select * from test_2;



 id | name 

----+-------

  1 | franc

  2 | tan



(2 rows)





会话一 查询表数据 ( 这里获得Access Shared 锁)

mydb=> begin;

BEGIN



mydb=> select * from test_2 where id=1;



 id | name 

----+-------

  1 | franc



(1 row)



注意：这里begin开始事务，没有提交；



会话二 更改表结构 (这里申请 Access Exclusive锁 )

mydb=> alter table test_2 add column sex char(1);

1

发现，命令一直等侍，执行不下去;



会话三 查询状态

mydb=# select oid,relname from pg_class where relname='test_2';



  oid  | relname

-------+---------

 33802 | test_2



 mydb=# select locktype,database,relation,pid,mode from pg_locks where relation='33802';



 locktype | database | relation |  pid  |        mode        

----------+----------+----------+-------+---------------------

 relation |    16466 |    33802 | 18577 | AccessShareLock

 relation |    16466 |    33802 | 18654 | AccessExclusiveLock



 mydb=# select datname,procpid,usename,current_query from pg_stat_activity where procpid in (18577,18654);



 datname | procpid | usename |               current_query               

---------+---------+---------+--------------------------------------------

 mydb    |   18577 | skytf   | <IDLE> in transaction

 mydb    |   18654 | skytf   | alter table test_2 add column sex char(1);



(2 rows)



这里可以看出会话一(pid=18577) 获取的是 “AccessShareLock”锁,会话二(pid=18654 ) 获取的是 “AccessExclusiveLock”锁。



再次回到会话一，执行’end’结束事务后会发生什么结果

注意，此时会话二还处于等侍状态



mydb=> end;

COMMIT



回到会话二发现 原来处于等侍状态的’ALTER TABLE’命令执行成功

mydb=> alter table test_2 add column sex char(1);

ALTER TABLE



mydb=> \d test_2

            Table "skytf.test_2"



 Column |         Type          | Modifiers

--------+-----------------------+-----------

 id     | integer               |

 name   | character varying(32) |

 sex    | character(1)          |



 

回到会话三，锁已经释放

mydb=# select locktype,database,relation,pid,mode from pg_locks where relation='33802';



 locktype | database | relation | pid | mode

----------+----------+----------+-----+------

(0 rows)



mydb=# select datname,procpid,usename,client_addr,current_query from pg_stat_activity where procpid in (18577,18654);



 datname | procpid | usename | client_addr | current_query

---------+---------+---------+-------------+---------------

 mydb    |   18577 | skytf   |             | <IDLE>

 mydb    |   18654 | skytf   |             | <IDLE>



(2 rows)



实验说明： 这个实验说明了 ‘ALTER TABLE’命令与’SELECT’命令会产生冲突，证实了开始的结论，即”Access exclusive”锁模式与申请”Access shared”锁模式的’SELECT’命令相冲突。



4.2 Share 锁与 Row Exclusive 锁冲突



　　在数据库的维护过程中，创建索引也是经常做的工作，别小看创建索引，如果是一个很繁忙的系统，索引不一定能创建得上，可能会发生等侍, 严重时造成系统故障；根据章节二的理论，’Create Index’ 命令需要获取Share 锁模式。



　　根据图一，”Share” 锁和”Row Exclusive”锁冲突，下面来验证一下：



　　根据图三可以看出，share锁模式和多种锁模式冲突，有可能会问我，为什么单独讲share锁和Row Exclusive冲突呢？因为” Update,Delete,Insert”命令获取的是Row Exclusive 操作，而这种操作在生产过程中非常频繁；这个实验正是模似生产维护过程。



会话一， 向 test_2 上插入一条数据

mydb=> select * from test_2;



 id | name  | sex

----+-------+-----

  1 | franc |

  2 | tan   |



(2 rows)





mydb=> begin;

BEGIN

mydb=> insert into test_2 values (3,'fpzhou');

INSERT 0 1



mydb=>





说明： 这个Insert 操作放在一个事务里，注意此时事务尚未提交。



会话二，在表test_2上创建索引

mydb=> \d test_2;



            Table "skytf.test_2"



 Column |         Type          | Modifiers

--------+-----------------------+-----------

 id     | integer               |

 name   | character varying(32) |

 sex    | character(1)          |



mydb=> create unique index idx_test_2_id  on test_2 (id);



说明： 创建索引命令发生等侍



会话三，查询状态

mydb=# select locktype,database,relation,pid,mode from pg_locks where relation='33802';



 locktype | database | relation |  pid  |       mode      

----------+----------+----------+-------+------------------

 relation |    16466 |    33802 | 18577 | RowExclusiveLock

 relation |    16466 |    33802 | 18654 | ShareLock



(2 rows)



mydb=# select datname,procpid,usename,client_addr,current_query from pg_stat_activity where procpid in (18577,18654);



 datname | procpid | usename | client_addr |    current_query                   



---------+---------+---------+-------------+------------------------------

 mydb    |   18577 | skytf   |             | <IDLE> in transaction

 mydb    |   18654 | skytf   |             | create unique index idx_test_2_id  on test_2 (id);





说明： 这里可以看出”Insert into”(procpid=18577) 命令获取”RowExclusiveLock”,而”Create Index”(procpid=18654)操作获取的是”Sharelock”, 并且创建索引操作发了等侍，因为这两种锁模式是冲突的。



回到会话一，提交事务,看看会发生什么

注意，此时创建索引的会话二仍处于等侍状态



mydb=> end;

COMMIT

1

2

回到会话二，发现创建索引命令成功,等侍消失

mydb=> create unique index idx_test_2_id  on test_2 (id);

CREATE INDEX

1

2

实验结论：

　1. 上述实验说明 “Create index “操作和”Insert”操作冲突;也就是 “Share”锁和”RowExclusive”锁冲突。

　2. 在生产库上应该避免在业务高峰期执行新建索引操作，因为如果在张大表上新建索引，消耗时间较长，在这个过程中会阻塞业务的DML操作。



4.3 SHARE UPDATE EXCLUSIVE 与自身冲突



　　 根据章节二，大家知道 VACUUM(Without full), Analyze 和 Create index (Concurently)操作会申请获得”Shared update Exclusive 锁”。根据图一，”Shared update Exclusive 锁”与本身也是会冲突的，下面实验验证一下:



会话一，分析表test_2

mydb=> select * from test_2;



 id |  name  | sex

----+--------+-----

  1 | franc  |

  2 | tan    |

  3 | fpzhou |



(3 rows)



mydb=>

mydb=>

mydb=> begin;

BEGIN

mydb=> analyze test_2;

ANALYZE      



注意： 表分析放在一个事务里，此时并没有提交；



会话二 对表 test_2 做 vacuum

mydb=> \d test_2;



            Table "skytf.test_2"



 Column |         Type          | Modifiers

--------+-----------------------+-----------

 id     | integer               |

 name   | character varying(32) |

 sex    | character(1)          |

Indexes:



    "idx_test_2_id" UNIQUE, btree (id)



mydb=> vacuum test_2;



注意： 当对表 test_2 执行 vacuum操作时，操作等侍，



会话三，观察系统哪里锁住了

[postgres@pg1 ~]$ psql -d mydb



psql (9.0beta3)



Type "help" for help.



mydb=# select datname,procpid,waiting,current_query from pg_stat_activity where waiting='t';



 datname | procpid | waiting | current_query 

---------+---------+---------+----------------

 mydb    |   20625 | t       | vacuum test_2;



(1 row)



这里说明会话 vacuum test_2 在等侍



mydb=# select oid,relname from pg_class where relname='test_2';



  oid  | relname

-------+---------

 33802 | test_2



(1 row)



mydb=# select locktype,database,relation,pid,mode from pg_locks where relation='33802';



 locktype | database | relation |  pid  |           mode          

----------+----------+----------+-------+--------------------------

 relation |    16466 |    33802 | 20625 | ShareUpdateExclusiveLock

 relation |    16466 |    33802 | 20553 | ShareUpdateExclusiveLock



 (2 rows)



说明: 这里可以看出 ‘Analyze’操作 (pid=20553) 和’Vacuum’操作 (pid=20625) 都是加的”ShareUpdateExclusiveLock”。



mydb=# select datname,procpid,waiting,current_query from pg_stat_activity where procpid in (20625,20553);



 datname | procpid | waiting |     current_query    

---------+---------+---------+-----------------------

 mydb    |   20553 | f       | <IDLE> in transaction

 mydb    |   20625 | t       | vacuum test_2;



(2 rows)



说明： 结束上面查询可以看出会话20625在等侍会话20553,也就是说”vacuum test_2” 被事务堵住了，



再次回到会话一，提交会话,注意此时会话二处于等侍姿态；

mydb=> end;



COMMIT

1

2

3

再次回到会话二，发现 vacuum命令执行下去了，等侍消失。

mydb=> vacuum test_2;



VACUUM



mydb=>  select datname,procpid,waiting,current_query from pg_stat_activity where waiting='t';



 datname | procpid | waiting | current_query

---------+---------+---------+---------------



(0 rows)



**实验结论:

1. Analyze 和 Vacuum 操作都会申请获得 “ShareUpdateExclusiveLock”。

2. ShareUpdateExclusiveLoc与ShareUpdateExclusiveLock是冲突的。**





参考

http://www.postgresql.org/docs/9.4/static/explicit-locking.html
