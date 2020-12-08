# MySQL索引

## 索引是什么

> Indexes are used to find rows with specific column values quickly. Without an index, MySQL must begin with the first row and then read through the entire table to find the relevant rows. The larger the table, the more this costs. If the table has an index for the columns in question, MySQL can quickly determine the position to seek to in the middle of the data file without having to look at all the data. This is much faster than reading every row sequentially.
>
> Most MySQL indexes (`PRIMARY KEY`, `UNIQUE`, `INDEX`, and `FULLTEXT`) are stored in [B-trees](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_b_tree). Exceptions: Indexes on spatial data types use R-trees; `MEMORY` tables also support [hash indexes](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_hash_index); `InnoDB` uses inverted lists for `FULLTEXT` indexes.

- 索引是帮助MySQL搞笑获取数据的数据结构
- 索引存储在文件系统中
- 索引的文件存储形式与存储引擎有关
- 索引文件的结构
  - hash
  - 二叉树
  - B树
  - B+树

## 索引的分类

- 聚集索引：键（主键/非空唯一键/6字节的row-id）和数据储存在一个文件中，如innodb
  - 注意，如果主键和 非空唯一键都存在，那么有数据的那个B+树，非叶子节点还是储存的主键信息，而非空唯一键会在另一个B+树上，通过这个数可以得到这个非空唯一键所对于的主键，然后再通过主键来获取值
    - 这个过程中如果没有进行回表的操作，即没有查存有数据的那个B+树，这种情况叫**索引覆盖**，如 select id from class where name = lala
- 非聚集索引：键和数据不存在一起：如myisam，Myisqm性能高，但是 不支持事务



注意：**MySQL会自动创建索引，它是给 唯一键 创建索引，主键是唯一且非空键**



联合索引，组合索引：索引存在多个列

如name 和 age 添加一个索引 （name, age）

**最左匹配原则**： 在包含多个列的查询过程中，会依靠先查第一个列，再查第二个列的顺序，这样才会使用联合索引。

where name = ?  and age = ? 走组合索引

where name = ? 走组合索引

where age = ? 不走组合索引

where age = ? and name = ? 走组合索引 （MySQL中的优化器会优化）

**索引下推** ICP

首先了解一下谓词下推： 

select t1.name, t2.name from t1 join t2 on t1.id = t2.id;

上述的语句有两种方法来运行：

1.先join，然后取值

2.先取需要的值，然后join

谓词下推的方式会使用第二种方式，这样在数据库表列多的情况下就有优势。



再看索引下推：关键就是减少拿的数据
以(name, age)这个联合索引为例：

当不适用索引下推时的做法：

- 先根据name列的值把所有的数据都拉取到server层，在server层对age进行过滤

使用索引下推的做法：

- 根据name， age两个字段把满足要求的数据拉取到server层，取出对应的数据 

using index condition 即用到了索引下推



**MRR**：multi range read

这个操作主要和回表有关：根据name查找得到的 id 是否是有序的，如果不用mrr，会用无序的id去回表查找，这是随机查找。

用了mrr，会将id排个序。