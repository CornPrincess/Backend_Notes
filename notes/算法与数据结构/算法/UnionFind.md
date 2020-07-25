## Dynamic connectivity

Given a set of N objects:

- Union command: connect two objects
- Find/connected query: is there a path connecting the two objects?

![dynamic connectivity](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/dynamicConnectivity.png)

We assume "is connected to" is an equivalence relation:

- Reflexive: *p* is connected to *p*.
- Symmetric: if *p* is connected to *q*, then *q* is connected to *p*. 
- ransitive: if *p* is connected to *q* and *q* is connected to *r*, then *p* is connected to *r*.

**Connected components**: Maximal set of objects that are mutually connected.

![connected components](https://blog-1300663127.cos.ap-shanghai.myqcloud.com/BackEnd_Notes/conncetedcomponents.png)

## Quick-find [eager approach]

```java
public class QuickFind {
    private int[] id;

    public QuickFind(int N) {
        id = new int[N];
        for (int i = 0; i < N; i++) {
            id[i] = i;
        }
    }

    public boolean connected(int p, int q) {
        return id[p] == id[q];
    }

    public void union(int p, int q) {
        int pid = id[p];
        int qid = id[q];
        for (int i = 0; i < id.length; i++) {
            if (id[i] == id[pid]) {
                id[i] = qid;
            }
        }
    }
}
```

Union is too expensive: It takes N<sup>2</sup> array accesses to process a sequence of N unuion commands on N objects. 



## Quick-union [lazy approach]

Lazy approach: 

