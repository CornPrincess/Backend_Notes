# Trie

Trie 树的本质，就是利用字符串之间的公共前缀，将重复的前缀合并在一起。

如，我们现在有 how，hi，her，hello，so，see 这六个字符串，可以转化为如下的Trie 字典树

![TRIE](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/trie.png)

其中，根节点不包含任何信息。每个节点表示一个字符串中的字符，从根节点到红色节点的一条路径表示一个字符串（**注意：红色节点并不都是叶子节点**）。

如果我们要查找的是字符串“he”呢？我们还用上面同样的方法，从根节点开始，沿着某条路径来匹配，如图所示，绿色的路径，是字符串“he”匹配的路径。但是，路径的最后一个节点“e”并不是红色的。也就是说，“he”是某个字符串的前缀子串，但并不能完全匹配任何字符串。

即红色节点是该树存在该字符串的标志，其他字符串虽然也能匹配到，但它只是字符串的前缀而已。

## Trie的存储

![trie](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/JavaSE/trie2.png)

```java
public class Trie {
    private TrieNode root = new TrieNode('/');

    public void insert(char[] text) {
        TrieNode p = root;
        for (int i = 0; i < text.length; i++) {
            int index = text[i] - 'a';
            if (p.children[index] == null) {
                TrieNode newNode = new TrieNode(text[index]);
                p.children[index] = newNode;
            }
            p = p.children[index];
        }
        p.isEndingChar = true;
    }

    public boolean find(char[] patter) {
        TrieNode p = root;
        for (int i = 0; i < patter.length; i++) {
            int index = patter[i] - 'a';
            if (p.children[index] == null) {
                return false;
            }
            p = p.children[index];
        }
        return p.isEndingChar;
    }
}

class TrieNode {
    public char data;
    public TrieNode[] children = new TrieNode[26];
    public boolean isEndingChar = false;
    public TrieNode(char data) {
        this.data = data;
    }
}
```

构建Trie树的过程，需要扫描所有的字符串，时间复杂度为 O(n)

但是构建完成以后，在其中查找字符串的时间复杂度是 O(k)，k 表示要查找的字符串的长度。

## 性能

如果字符串中不仅包含小写字母，还包含大写字母、数字、甚至是中文，那需要的存储空间就更多了。所以，也就是说，在某些情况下，Trie 树不一定会节省存储空间。在重复的前缀并不多的情况下，Trie 树不但不能节省内存，还有可能会浪费更多的内存。

我们可以稍微牺牲一点查询的效率，将每个节点中的数组换成其他数据结构，来存储一个节点的子节点指针。用哪种数据结构呢？我们的选择其实有很多，比如有序数组、跳表、散列表、红黑树等。

也可以使用**缩点优化**来进行存储空间的优化

## Trie 树与散列表、红黑树的比较

在一组字符串中查找字符串，Trie 树实际上表现得并不好。

- 第一，字符串中包含的字符集不能太大。我们前面讲到，如果字符集太大，那存储空间可能就会浪费很多。即便可以优化，但也要付出牺牲查询、插入效率的代价。

- 第二，要求字符串的前缀重合比较多，不然空间消耗会变大很多。

- 第三，如果要用 Trie 树解决问题，那我们就要自己从零开始实现一个 Trie 树，还要保证没有 bug，这个在工程上是将简单问题复杂化，除非必须，一般不建议这样做。

- 第四，我们知道，通过指针串起来的数据块是不连续的，而 Trie 树中用到了指针，所以，对缓存并不友好，性能上会打个折扣。

综合这几点，针对在一组字符串中查找字符串的问题，我们在工程中，更倾向于用散列表或者红黑树。因为这两种数据结构，我们都不需要自己去实现，直接利用编程语言中提供的现成类库就行了。

实际上，**Trie 树只是不适合精确匹配查找**，这种问题更适合用散列表或者红黑树来解决。**Trie 树比较适合的是查找前缀匹配的字符串**，也就是类似搜索提示的场景。