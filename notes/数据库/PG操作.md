kubectl exec -it commsrvpg-58576e42-29d2-48b1-a53e-caa44f35b3e7-0  -n  opcs sh

export PGUSER=postgres

export PGPASSWORD=Cloud123

export PGHOST=localhost

export PGPORT=36789

export PGDATABASE=postgres

export LD_LIBRARY_PATH=/home/postgres/lib:/home/postgres/pgsql/lib:/usr/lib:.

export PATH=$PATH:/home/postgres/pgsql/bin:/home/postgres/bin

或者su - postgres

psql



\c db_b77a08ef40b74472aa1fd2756378e1ac

1、列举数据库：\l

2、选择数据库：\c 数据库名

3、查看该某个库中的所有表：\dt

4、切换数据库：\c interface

5、查看某个库中的某个表结构：\d 表名

6、查看某个库中某个表的记录：select * from apps limit 1;

7、显示字符集：\encoding

8、退出psgl：\q



select count(*) , datname, application_name  from pg_stat_activity group by 2,3 order by 1 desc  ; 



select count(*) , datname, application_name,state from pg_stat_activity where application_name = 'sus-jobmgt-ms' group by 2,3, 4 order by 1 desc  ;



select count(*) , datname, application_name,state from pg_stat_activity where application_name = 'sus-workflow-activiti' group by 2,3, 4 order by 1 desc  ;



select query from pg_stat_activity where datname ='db_b73df31901a949b4bff38bc6dd77d767' and state<>'idle' ;



 select query from pg_stat_activity where datname ='db_475a82af395b43b28e26917cc5cc867d' and state<>'idle' order by 1;



 select query from pg_stat_activity where application_name='sus-workflow-activiti' and state<>'idle';
