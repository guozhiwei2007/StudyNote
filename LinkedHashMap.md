LinkedHashMap
LinkedHashMap
开始前请大家思考几个问题：

LinkedHashMap数据结构是怎么样的？难道仅仅是双向循环链表吗？如果是，那么get方法逻辑显然说不通
我们知道Lru算法底层实现就是LinkedHashMap，基于访问的排序，那么访问排序到底怎么实现的？
带着上述疑问，我们进入正文。

本篇我们分析一下HashMap的儿子LinkedHashMap的核心源码，提到LinkedHashMap做安卓的同学肯定会想到Lru(Least Recently Used)算法，Lru算法就是基于LinkedHashMap来实现的，明白了LinkedHashMap中基于访问排序逻辑Lru算法自然就明白了。

进入正题，源码基于android-23。

一、LinkedHashMap中成员变量
 1     /**
 2      * A dummy entry in the circular linked list of entries in the map.
 3      * The first real entry is header.nxt, and the last is header.prv.
 4      * If the map is empty, header.nxt == header && header.prv == header.
 5      */
 6     transient LinkedEntry<K, V> header;
 7 
 8     /**
 9      * True if access ordered, false if insertion ordered.
10      */
11     private final boolean accessOrder;
很简单，就两个成员变量。不过这里要明白LinkedHashMap是继承HashMap也就是HashMap中一些成员变量，方法LinkedHashMap中都是有的，父类的玩意就不提了，这里只说一下子类自己的。

header双向循环链表的头结点，看注释The first real entry is header.nxt, and the last is header.prv.翻译过来就是第一个加入链表的结点是header.nxt，最后被加入链表的是header.prv。

accessOrder控制链表的排序方式，如果是true那么链表节点是基于访问排序的，什么是访问排序？就是我们访问链表中某一节点的时候会将这个结点从链表中删除然后在放入链表的尾部，表示用户最近使用了这个结点，最近被“宠幸”了一次，那好，我把你放入链表尾部，链表删除是从头部删除的，插入数据是从尾部插入的，如果遇到一些情况要删除链表中节点数据，那么优先删除的是链表头部不经常使用的节点数据。如果为false则表示链表节点是基于插入排序的，理解起来很简单，就是平常的插入顺序了，先插入的在头部优先被删除。

二、LinkedHashMap构造方法
接下来看下构造方法，如下：

public LinkedHashMap() {
         init();
         accessOrder = false;
     }

     public LinkedHashMap(int initialCapacity) {
         this(initialCapacity, DEFAULT_LOAD_FACTOR);
     }

      public LinkedHashMap(int initialCapacity, float loadFactor) {
         this(initialCapacity, loadFactor, false);
     }
 　　
     public LinkedHashMap(
        　　int initialCapacity, float loadFactor, boolean accessOrder) {
    　　　super(initialCapacity, loadFactor);
    　　  init();
    　　  this.accessOrder = accessOrder;
　　  }
　　  @Override 
　　  void init() {  
　　　　　　header = new LinkedEntry<K, V>();  
　　　}
以上就是初始化的主要方法，大体上和HashMap差不多，不在细说，主要一点是默认的accessOrder值为false，也就是链表节点按照插入排序来排序的，当然我们也可以在初始化的时候指定accessOrder值，比如LruCache中LinkedHashMap初始化的时候accessOrder就指定为true。

三、LinkedHashMap中数据项LinkedEntry
LinkedHashMap中每个数据节点类型为LinkedEntry，LinkedEntry为HashMapEntry子类，我们直接看其源码：

 1     static class LinkedEntry<K, V> extends HashMapEntry<K, V> {
 2         LinkedEntry<K, V> nxt;
 3         LinkedEntry<K, V> prv;
 4 
 5         /** Create the header entry */
 6         LinkedEntry() {
 7             super(null, null, 0, null);
 8             nxt = prv = this;
 9         }
10 
11         /** Create a normal entry */
12         LinkedEntry(K key, V value, int hash, HashMapEntry<K, V> next,
13                     LinkedEntry<K, V> nxt, LinkedEntry<K, V> prv) {
14             super(key, value, hash, next);
15             this.nxt = nxt;
16             this.prv = prv;
17         }
18     }
LinkedEntry多了两个成员变量nxt与prv，分别只向后一个节点与前一个节点，这里暂且称呼为前向指针与后向指针，方便理解。

每个数据节点结构类似如下图所示：

image

并且稍有经验就知道了LinkedHashMap中链表为双向循环链表，其数据结构如下图所示：

image

四、LinkedHashMap中put方法
我们会发现LinkedHashMap中并没有重写put方法，只是重写了addNewEntry方法，很好理解，HashMap与LinkedHashMap二者数据结构都不一样，肯定无法共用同一个put方法，这里LinkedHashMap重写了addNewEntry方法根据自己需要放入数据即可，至于hash值，index等父类已经帮我算好了，直接继承传递过来用就可以了，接下来我们分析LinkedHashMap中addNewEntry方法，源码如下:

1  @Override 
 2  void addNewEntry(K key, V value, int hash, int index) {
 3         LinkedEntry<K, V> header = this.header;
 4 
 5         // Remove eldest entry if instructed to do so.
 6         LinkedEntry<K, V> eldest = header.nxt;
 7         if (eldest != header && removeEldestEntry(eldest)) {
 8             remove(eldest.key);
 9         }
10 
11         // Create new entry, link it on to list, and put it into table
12         LinkedEntry<K, V> oldTail = header.prv;
13         LinkedEntry<K, V> newTail = new LinkedEntry<K,V>(
14                 key, value, hash, table[index], header, oldTail);
15         table[index] = oldTail.nxt = header.prv = newTail;
16  }
3行，获取链表的头结点header。

6行，获取链表中最先被加入的数据节点eldest，也就是最老的数据节点，位于队头。

7-9行，判断最老的数据节点eldest与header是否相等以及removeEldestEntry(eldest)方法是否返回true，如果二者均为true则删除最老的数据节点。

什么情况下eldest与header是否相等？很简单就是链表刚刚建立的时候啊，只有一个header节点，nxt与prv指针均指向自己。

removeEldestEntry(eldest)方法源码如下：

1     protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
2         return false;
3     }
看到了吧，超级简单，直接返回false，也就是默认是不会删除最老的节点的。

12行，获取oldTail，这里oldTail是链表中最后被插入的数据节点，也就是最新的数据，位于链表最尾部。

13行，创建一个新的数据节点newTail，看这名字就知道了位于链表尾部，翻译过来就是：新的尾巴。

新节点的nxt指针指向header，prv指针指向oldTail，也就是加入之前链表的尾部，13，14行执行完链表图示如下：红色部分为即将加入链表的节点。

image

15行，oldTail的nxt指针指向newTail，链表的头结点header的prv指针指向newTail节点，此时链表结构如图所示：

image

此时，新的数据节点(红色部分)就已经插入链表了，最新插入的数据位于链表尾部。
以上就是LinkedHashMap放入数据的核心逻辑，其实很简单，就是操作双向链表而已。
接下来，我们分析get方法，看看怎么实现访问排序的。

五、LinkedHashMap中get方法
再讲get方法之前我们稍微回顾一下addNewEntry方法的13-15行，这几行中有个table[index]没有提到，其实上面我只是将双向循环链表提取出来讲放入数据的逻辑，这样理解起来比较简单，而LinkedHashMap中隐藏了HashMap中的单向链表，全部展示出其数据结构如图所示：

image

是不是看着乱了很多，如果一开始我就抛出此图，估计很多就蒙圈了，除去红色的线其就是一个双向循环链表，为什么这时候要抛出这个图呢？大家想一下我们如果要get一个数据没有单向链表的话很自然从header节点开始挨个遍历整个链表就完了，和LinkedList算法就很像了，显然效率低下，这里有单向链表，我们只需要算出将要获取的数据在table数组的哪一行，只需要遍历那一行单向链表就完了，效率自然提升很多，这也是LinkedHashMap中存在单向链表的意义所在。

接下来我们分析get源码：

1  @Override 
 2  public V get(Object key) {
 3         /*
 4          * This method is overridden to eliminate the need for a polymorphic
 5          * invocation in superclass at the expense of code duplication.
 6          */
 7         if (key == null) {
 8             HashMapEntry<K, V> e = entryForNullKey;
 9             if (e == null)
10                 return null;
11             if (accessOrder)
12                 makeTail((LinkedEntry<K, V>) e);
13             return e.value;
14         }
15 
16         int hash = Collections.secondaryHash(key);
17         HashMapEntry<K, V>[] tab = table;
18         for (HashMapEntry<K, V> e = tab[hash & (tab.length - 1)];
19                 e != null; e = e.next) {
20             K eKey = e.key;
21             if (eKey == key || (e.hash == hash && key.equals(eKey))) {
22                 if (accessOrder)
23                     makeTail((LinkedEntry<K, V>) e);
24                 return e.value;
25             }
26         }
27         return null;
28     }
7-14行我们不做详细分析，就是获取key的null的情况，比较简单，自己看看就行了。

16行，计算key的二次hash值，上一篇分析HashMap的时候已经分析过，不在分析。

17行，获取table数组。

18-26行，就是遍历key所在行的单向链表，21行如果链表中有此数据则执行24行逻辑返回对应数据，如果循环整个所在行单向链表都没有那么执行27行逻辑返回null，表明链表中没有我们要获取的数据。

22-23行，核心所在，我们说LinkedHashMap可以控制数据是插入排序还是访问排序，这里get方法显示就是对数据的访问，如果我们设accessOrder为true,表明我们想让LinkedHashMap数据基于访问排序，则执行makeTail方法。

接下来我们看下makeTail都做了什么。

六、LinkedHashMap中访问排序的实现
直接分析makeTail方法源码：

 1  private void makeTail(LinkedEntry<K, V> e) {
 2         // Unlink e
 3         e.prv.nxt = e.nxt;
 4         e.nxt.prv = e.prv;
 5 
 6         // Relink e as tail
 7         LinkedEntry<K, V> header = this.header;
 8         LinkedEntry<K, V> oldTail = header.prv;
 9         e.nxt = header;
10         e.prv = oldTail;
11         oldTail.nxt = header.prv = e;
12         modCount++;
13     }
又是对链表的操作，而且还是双向链表，很多同学估计一看就发愁了，静下心来，其实没那么难。

假设原链表如图所示：
image

此时访问数据①,我们看下makeTail是如何处理数据①的。

3行，将e所在节点的prv指针指向的节点的nxt指针指向e所在节点的nxt指针指向的节点，真是拗口。

4行，同理。

其实3，4行逻辑就是将e所在节点从链表中断开，执行完3，4行逻辑，图示如下：
image

主要信息图中已经体现，不在过多解释。

7,8行分别获取header与oldTail节点。

9行，将e所在节点的nxt指针指向header节点。

10行，将e所在节点的prv指针指向oldTail节点。

9，10行执行完，数据结构图示如下：

image

11行，oldTail所在节点的nxt指针指向e，header所在节点的prv指针指向e，11行完图示如下：

image

这样e所在节点就插入了链表的尾部，成为最新的数据。

makeTail方法就是将我们访问的数据通过调整指针的指向来将访问的节点调整到队列的尾部，成为最新的数据。是不是很简单？

七、总结
到此我想讲的就都完了，本篇希望你掌握LinkedHashMap的数据结构，记住有个单向链表啊，不仅仅是双向链表，否则get方法的逻辑你是看不懂的。
此外，掌握访问排序到底怎么实现的，其实很简单，就是对双向链表的操作。

好了，本篇到此结束，希望对你有用，真好！！！！！！。