HashMap简介
HashMap
开始前请大家思考几个问题：

hashmap中hash碰撞产生的根本原因是什么？难道仅仅是大家所说的hash值相同吗？hash值不同就不会产生hash碰撞？
位于同一链表数据的hash值有什么相同点？
发生hash碰撞的时候我们放入的新数据是位于链表头部还是尾部？
扩容的时候原数据到底是怎么重新放入新数组中的？难道还要挨个计算一下位置？如果不是，那么是通过什么方式来判断之前位于同一链表的数据是否还在同一链表？
好了，进入正文。

存储键值对我们首先想到HashMap，它的底层基于哈希表，采用数组存储数据，使用链表来解决哈希碰撞，它是线程不安全的，并且存储的key只能有一个为null，在安卓中如果数据量比较小（小于一千），建议使用SparseArray和ArrayMap，内存，查找性能方面会有提升，如果数据量比较大，几万，甚至几十万以上还是使用HashMap吧。本篇只详细分析HashMap的源码，SparseArray和ArrayMap不在本篇讨论范围内，后续会单独分析。

HashMap的理解，最最核心就是扩容那二十几行代码，可以说是HashMap的核心所在了，然而网上绝大部分博客只是一带而过，大体说了一下结论，让人十分失望，本篇将会彻底分析扩容机制，源码分析基于android-23。

好了，直入主题吧。

一、HashMap中成员变量
1 private static final int MINIMUM_CAPACITY = 4;//约定hashmap中最小容量，也可以是0，如果不为0，那么最小容量限制为4
2 private static final int MAXIMUM_CAPACITY = 1 << 30;约定hashmap中最大容量，为2的30次方    
3 static final float DEFAULT_LOAD_FACTOR = .75F;//扩容因子：主要用于扩容时机，后续会细讲  
4  
5 transient HashMapEntry<K, V>[] table;//盛放数据的table，数据每一项key不为null,每一项都是一个HashMapEntry对象  
6  
7 transient HashMapEntry<K, V> entryForNullKey;盛放key为null的数据项   
8  
9 transient int size;//hashmap中已经盛放数据的大小   
10   
11 private transient int threshold;//用来判断是否需要扩容，其值为DEFAULT_LOAD_FACTOR * hashmap的容量，当盛放数据达到hashmap的四分之三时，即需要考虑扩容了。
主要成员变量已经有所标注，后续分析的时候会再次提及，此处不做过多解释。

二、HashMap中数据项HashMapEntry
HashMap中每个数据项都是HashMapEntry对象，HashMapEntry是HashMap的内部类，我们先来看看其结构：

 1 static class HashMapEntry<K, V> implements Entry<K, V> {
 2         final K key;
 3         V value;
 4         final int hash;
 5         HashMapEntry<K, V> next;
 6 
 7         HashMapEntry(K key, V value, int hash, HashMapEntry<K, V> next) {
 8             this.key = key;
 9             this.value = value;
10             this.hash = hash;
11             this.next = next;
12         }
13 
14         public final K getKey() {
15             return key;
16         }
17 
18         public final V getValue() {
19             return value;
20         }
21 
22         public final V setValue(V value) {
23             V oldValue = this.value;
24             this.value = value;
25             return oldValue;
26         }
27 
28         @Override public final boolean equals(Object o) {
29             if (!(o instanceof Entry)) {
30                 return false;
31             }
32             Entry<?, ?> e = (Entry<?, ?>) o;
33             return Objects.equal(e.getKey(), key)
34                     && Objects.equal(e.getValue(), value);
35         }
36 
37         @Override public final int hashCode() {
38             return (key == null ? 0 : key.hashCode()) ^
39                     (value == null ? 0 : value.hashCode());
40         }
41 
42         @Override public final String toString() {
43             return key + "=" + value;
44         }
45     }
主要信息就是每一个数据项都包含了我们存储的key，value以及根据key算出来的hash值，next用于发生哈希碰撞的时候指向其下一个数据项。比较简单，同样不过多分析。

三、HashMap构造方法
HashMap构造方法有如下：

1 public HashMap()
2 public HashMap(int capacity) 
3 public HashMap(int capacity, float loadFactor)
4 public HashMap(Map<? extends K, ? extends V> map)
构造方法有如上4种方式，我们平时最常用的就是第一种方式，直接初始化然后不停往里面仍数据就可以了，第二种初始化的时候可以指定容量大小，第三，四中估计大部分人没用过，第三种除了指定容量大小我们还可以指定扩容因子，不过我们还是不要动扩容因子为好，指定为0.75是时间和空间的权衡，平时我们使用就用默认的0.75就可以了。

我们看一下HashMap()这种构造方式：
1     public HashMap() {
2         table = (HashMapEntry<K, V>[]) EMPTY_TABLE;
3         threshold = -1; // Forces first put invocation to replace EMPTY_TABLE
4     }
太简单了，就是初始化table为空的数组EMPTY_TABLE，这个EMPTY_TABLE的初始化容量可不为0，源码如下：

private static final Entry[] EMPTY_TABLE
            = new HashMapEntry[MINIMUM_CAPACITY >>> 1];
看到了吧，初始化容量为MINIMUM_CAPACITY的一半，也就是2。
此外threshold初始化的时候置为-1。

接下来我们在看下HashMap(int capacity) 这种构造方式：

1     public HashMap(int capacity) {
 2         if (capacity < 0) {
 3             throw new IllegalArgumentException("Capacity: " + capacity);
 4         }
 5 
 6         if (capacity == 0) {
 7             @SuppressWarnings("unchecked")
 8             HashMapEntry<K, V>[] tab = (HashMapEntry<K, V>[]) EMPTY_TABLE;
 9             table = tab;
10             threshold = -1; // Forces first put() to replace EMPTY_TABLE
11             return;
12         }
13 
14         if (capacity < MINIMUM_CAPACITY) {
15             capacity = MINIMUM_CAPACITY;
16         } else if (capacity > MAXIMUM_CAPACITY) {
17             capacity = MAXIMUM_CAPACITY;
18         } else {
19             capacity = Collections.roundUpToPowerOfTwo(capacity);
20         }
21         makeTable(capacity);
22     }
23 
24 
25     private HashMapEntry<K, V>[] makeTable(int newCapacity) {
26         @SuppressWarnings("unchecked") HashMapEntry<K, V>[] newTable
27                 = (HashMapEntry<K, V>[]) new HashMapEntry[newCapacity];
28         table = newTable;
29         threshold = (newCapacity >> 1) + (newCapacity >> 2); // 3/4 capacity
30         return newTable;
31     }
6-12行逻辑大体就是我们调用hashmap()空参数的构造函数初始化一样。
14-17行，就是对我们设置的容量capacity进行检查了，如果小于MINIMUM_CAPACITY那么就重置为MINIMUM_CAPACITY，如果大于MAXIMUM_CAPACITY则重置为MAXIMUM_CAPACITY。
19行，Collections.roundUpToPowerOfTwo这个方法就是找出一个2^n的数，使其不小于给出的数字，并且最近接给出的数字。
比如：
Collections.roundUpToPowerOfTwo(3)返回4，2的2次方.
Collections.roundUpToPowerOfTwo(4)返回4，2的2次方.
Collections.roundUpToPowerOfTwo(100)返回128，2的7次方.

明白了吧？也就是说返回的数肯定是2的几次方，也就是说hashmap的容量肯定是2的几次方形式，这里很重要，一定要记住，后续分析的时候还会用到。

接下来就是调用makeTable了。
26，27就是根据给定的容量创建newTable数组。
28行，成员变量table指向新创建的newTable数组。
29行，计算threshold的值，也就是我们指定的容量的四分之三了。
好了，以上就是构造方法逻辑，其余两种方法可自行查看一下，比较核心的就是19行代码，对capacity数据的转换，约束hashmap容量大小肯定为2的n次方。

四、HashMap中put方法分析
接下来就是HashMap核心所在了，我们一点点分析，先看下put方法源码：

 1     @Override 
 2     public V put(K key, V value) {
 3         if (key == null) {
 4             return putValueForNullKey(value);
 5         }
 6         int hash = Collections.secondaryHash(key);
 7         HashMapEntry<K, V>[] tab = table;
 8         int index = hash & (tab.length - 1);
 9         for (HashMapEntry<K, V> e = tab[index]; e != null; e = e.next) {
10             if (e.hash == hash && key.equals(e.key)) {
11                 preModify(e);
12                 V oldValue = e.value;
13                 e.value = value;
14                 return oldValue;
15             }
16         }
17         // No entry for (non-null) key is present; create one
18         modCount++;
19         if (size++ > threshold) {
20             tab = doubleCapacity();
21             index = hash & (tab.length - 1);
22         }
23         addNewEntry(key, value, hash, index);
24         return null;
25     }
3-5行，如果我们放入的数据key为null，那么执行4行代码逻辑并且直接返回，putValueForNullKey源码如下：

 1     private V putValueForNullKey(V value) {
 2         HashMapEntry<K, V> entry = entryForNullKey;
 3         if (entry == null) {
 4             addNewEntryForNullKey(value);
 5             size++;
 6             modCount++;
 7             return null;
 8         } else {
 9             preModify(entry);
10             V oldValue = entry.value;
11             entry.value = value;
12             return oldValue;
13         }
14     }
大体逻辑很简单，就是对成员变量entryForNullKey操作，其就是HashMapEntry对象实例，3-8行如果entry为null，则代表之前没有放入过key为null的数据，则只需要创建即可。8-12行表示之前放入锅key为null的数据，那么只需要将value替换为新的value即可，这里说明HashMap只会有一个数据的key为null，重复放入只会将value替换为最新value.好了，这里就只是简单分析一下。

回到put方法，如果我们放入的key不为null，则继续向下执行：
6行，根据key计算二次哈希值，源码如下：

 1     public static int secondaryHash(Object key) {
 2         return secondaryHash(key.hashCode());
 3     }
 4 
 5     private static int secondaryHash(int h) {
 6         // Spread bits to regularize both segment and index locations,
 7         // using variant of single-word Wang/Jenkins hash.
 8         h += (h <<  15) ^ 0xffffcd7d;
 9         h ^= (h >>> 10);
10         h += (h <<   3);
11         h ^= (h >>>  6);
12         h += (h <<   2) + (h << 14);
13         return h ^ (h >>> 16);
14     }
就是将key的hashCode方法返回的值传入secondaryHash(int h) 再次计算一次返回一个值，这里最重要的一点就是我们传入的key必须有hashCode()方法并且每次返回的值一样，如果HashMap Key的哈希值在存储键值对后发生改变，Map可能再也查找不到这个Entry了，所以HashMap中key我们需要使用不可变对象，比如经常使用的String，Integer对象，其HashCode()方法分别如下：

 1     @Override 
 2     public int hashCode() {//String中HashCode()方法
 3         int hash = hashCode;
 4         if (hash == 0) {
 5             if (count == 0) {
 6                 return 0;
 7             }
 8             for (int i = 0; i < count; ++i) {
 9                 hash = 31 * hash + charAt(i);
10             }
11             hashCode = hash;
12         }
13         return hash;
14     }
15 
16     @Override
17     public int hashCode() {//Integer中HashCode()方法
18         return value;
19     }
回到put方法，则继续向下执行：
7行定义局部变量tab指向全局变量table数组。
8行，计算放入的数据在tab中的位置，计算方式为key的hash值按位与tab的长度减1，这样确保了计算出的index不会超出数组角标，比如：
key的hash值为11111111111111111111111111111111，tab容量为8，则tab.length-1为7，其数组角标范围为0~7。

为什么要进行二次哈希值得计算呢？

比如我们放入三个数据，key的HashCode值分别为:31，63，95。tab容量为8
如果不进行二次哈希值计算索引index，也就是key.hashcode() & (tab.length - 1)，计算如下：

31=00011111 & 00000111 = 0111 = 7
63=00111111 & 00000111 = 0111 = 7
95=01011111 & 00000111 = 0111 = 7

进行二次哈希值后再计算索引index，也就是源码中secondaryHash(key.hashCode())& (tab.length - 1)，计算如下：

31=00011111 =>secondaryHash=> 00011110 & 00000111= 0110 = 6
63=00111111 ==secondaryHash=> 00111100 & 00000111= 0100 = 4
95=01011111 ==secondaryHash=> 01011010 & 00000111= 0010 = 2

如上不经过二次哈希值计算最终计算出的index值均为7，也就是我们放入数组中都处于同一位置。而经过二次哈希值计算之后再计算index值分别为6，4，2也就是在数组中处于了三个不同的位置，这样就达到了更加散列的效果。但是即使经过二次哈希值计算也不能保证计算出的index值都不相同，这里只是尽可能的散列化，不能保证避免哈希碰撞。

回到put方法，继续向下分析：
我们知道HashMap存储数据结构如下：

image

简单说就是我们放入一个数据的时候会先根据数据项的key计算出其在table数组中的索引，如果索引位置已经有元素了，那么则与已经放入的数据形成链表的关系，相信稍有经验的都明白，这里只是稍微提一下。

9-16行的for循环逻辑就是挨个遍历所在数组行链表中每一个数据项，然后将每个数据项的hash值和key与将要放入的key及其hash值比较，如果二者均相等则表明HashMap中已经存在此数据。
12-14行就是将对应数据项的value值替换为新的value值，并将之前value返回。
如果整个for循环都没有找到则表明HashMap中没有将要存储的数据项，继续向下执行。
19行，判断是否需要扩容，threshold上面说过值为table容量的四分之三，size记录我们HashMap中存入数据的大小，我们放入数据时如果超过容量的四分之三那么就需要扩容了。
20行，如果需要扩容那么调用doubleCapacity()方法进行扩容（后续会仔细分析扩容机制），扩容完此方法会返回扩容后的数组。
21行，由于数组已经扩容，容量发生了变化，所以这里需要重新计算一下将要放入数据的index索引。
23行调用addNewEntry方法将数据放入数组中。addNewEntry源码如下：

1     void addNewEntry(K key, V value, int hash, int index) {
2         table[index] = new HashMapEntry<K, V>(key, value, hash, table[index]);
3     }
这里就是根据我们传入的key,value,hash值新建HashMapEntry数据节点，此数据节点的next指向原table[index]，最后将新数据节点赋值给table[index]，这里说的有点蒙圈，用图来解释一下，又要展示我强大的画图能力了：

image

这里通过阅读源码可以发现新添加的数据项是放在链表头部的，而不是直接放在尾部。
好了，以上就是put方法主要逻辑了，不再做其余分析，下面我们着重看一下HashMap的扩容机制。

五、HashMap中扩容机制分析
好了，如果你看到这里那么清理一下大脑吧，下面的有点烧脑了。
废话少说，直接看扩容方法源码：

 1 private HashMapEntry<K, V>[] doubleCapacity() {
 2         HashMapEntry<K, V>[] oldTable = table;
 3         int oldCapacity = oldTable.length;
 4         if (oldCapacity == MAXIMUM_CAPACITY) {
 5             return oldTable;
 6         }
 7         int newCapacity = oldCapacity * 2;
 8         HashMapEntry<K, V>[] newTable = makeTable(newCapacity);
 9         if (size == 0) {
10             return newTable;
11         }
12         for (int j = 0; j < oldCapacity; j++) {
13             /*
14              * Rehash the bucket using the minimum number of field writes.
15              * This is the most subtle and delicate code in the class.
16              */
17             HashMapEntry<K, V> e = oldTable[j];
18             if (e == null) {
19                 continue;
20             }
21             int highBit = e.hash & oldCapacity;
22             HashMapEntry<K, V> broken = null;
23             newTable[j | highBit] = e;
24             for (HashMapEntry<K, V> n = e.next; n != null; e = n, n = n.next) {
25                 int nextHighBit = n.hash & oldCapacity;
26                 if (nextHighBit != highBit) {
27                     if (broken == null)
28                         newTable[j | nextHighBit] = n;
29                     else
30                         broken.next = n;
31                     broken = e;
32                     highBit = nextHighBit;
33                 }
34             }
35             if (broken != null)
36                 broken.next = null;
37         }
38         return newTable;
39     }
2-6行oldTable，oldCapacity记录原来数组，数组长度以及检查原数组长度是否已经达到MAXIMUM_CAPACITY，如果已经达到最大长度，那么不好意思了，直接返回原数组了，老子无法给你扩容了，都那么长了，还扩什么容，自己继续在原数组玩吧，管你哈希碰撞导致链表多长我都不管了。

7行，定义newCapacity也就是新数组长度为原数组长度的2倍。

8行，就是执行makeTable()逻辑，创建新的数组newTable了，至于makeTable方法上面说过，就不再分析了。

9-11行，检查size是否为0，如果为0那么表明HashMap中没有存储过数据，不用执行下面的数据拷贝逻辑了，直接返回newTable就可以了。

12-37行，这可就是HashMap整个类的精华所在了，这几行代码看不懂这个类你就没有真正理解，看懂了其余扫一下就明白了。

假设原HashMap如图：
image

12行很简单就是遍历原数组中每个位置的数据，也可以说每个链表的头数据。

13-16行，风骚的注释：直白翻译就是下面这几行代码是这个类中最风骚的几行代码。

17-20行代码，就是检查取出的数组中每个数据项是否为null，为null则表明此行没有数据，继续循环就可以了。

在继续向下讲请大家思考一个问题：HashMap中同一个链表中每一个数据项的哈希值有什么相同点？比如原数组大小是8，那么同一链表中每一个数据项的哈希值有什么相同点？

思考。。。。。

这里直接说了：能在同一链表说明计算出来的index值相同，在看计算公式为int index = hash & (tab.length - 1)，这里在扩容之前tab.length-1的值是相同的，比如数组长度为8，那么tab.length - 1的二进制表示为00000111，不同hash值计算出的index又相同，那么这里同一链表中每一个数据项的hash值得最后三位一定相同，只有这样计算出的index值才相同，如下：

image

如上图 两个hash值不同的数据项，经过运算后得出index均为2，原因就是虽然整体的hash值不同，但是最后三位均为010，所以计算出index值是相同的（此处假设数组长度为8）。
进而得出结论：如果HashMap中数组长度为2的n次方，那么同一链表中不同数据项的hash值的最后n位一定相同。
好了，到这里第一个难点通过，我们继续分析doubleCapacity()方法。

21行，int highBit = e.hash & oldCapacity计算出highBit位，翻译过来就是高位，这他妈又是什么玩意？仔细看计算方式与的oldCapacity，而不是oldCapacity-1，所以这里取得是数据项hash值得第n+1位(hashmap数组长度为2的n次方)是0还是1，这里一定要知道HashMap数组长度一定为2的n次方，二进制形式就是第n+1位为1其余为均为0。这里先记住这个highBit是哪一位，后面会用到。

22行，定义一个broken，知道有这么个玩意，后面也会用到。

23行，newTable[j | highBit] = e，将我们从原数组取出的数据项放入新数组中，也就是数据的拷贝了，注意这里e是每一个链表的头部，也就是处于数组中的数据。链表其余数据是通过24行for循环挨个遍历再放入新数组中的。但是这里有个疑问原数组放入数据是按照hash & (tab.length - 1)计算其在数组中位置的，这里怎么成了j | highBit这样计算了呢？这里真是卡住我了，一开始我是怎么想怎么想不通，但是我觉得二者之间一定有什么关系，不可能用两个完全不相关的算法来计算同一数据项在数组中的位置，绝不可能，一定有内在联系，我查啊查，算啊算，在经过如下计算我终于想明白了：hash & (tab.length - 1)与j | highBit这二者逻辑是完全相同的，TMD，算法逻辑竟然是相同的。

接下来咱们推导分析一下：

j | highBit
= j | (e.hash & oldCapacity) 第一步
= (e.hash & (oldCapacity-1)) | (e.hash & oldCapacity) 第二步
= e.hash & ( (oldCapacity-1) | oldCapacity) 第三步
= e.hash & (newCapacity- 1) 第四步

从开始到第一步很简单了，highBit的计算方式就是e.hash & oldCapacity这里只是替换回来。

第一步到第二步，j怎么就成了e.hash & (oldCapacity-1)呢？还记得index的计算方式吗？就是e.hash & (oldCapacity-1)，那就是说j就是index了，在看看j是什么？j就是从0开始到oldCapacity的值，这里我们想一下啊，e就是通过oldTable[j]获取的，我们想想put方法怎么放入的呢，不就是oldTable[e.hash & (oldCapacity-1)] = e吗，想到了什么？想到了什么？对，通过j获取的元素e，这个j就是e.hash & (oldCapacity-1)，所以这里可以替换的。

第二步到第三步就是数学方面的，记住就可以了。

第三步到第四步怎么来的呢？也就是(oldCapacity-1) | oldCapacity与newCapacity- 1相等，还记得上面说的HashMap数组容量一定是2的n次方吗？并且newCapacity = oldCapacity * 2 。

oldCapacity为2的n次方，也就是n+1位为1，其余都为0，oldCapacity-1也就是0到n位为1其余都为0，二者或运算后0到n+1为1其余位0。

newCapacity= oldCapacity * 2 也就是n+2位为1其余位0，newCapacity- 1也就是0到n+1位为1其余为0.

所以(oldCapacity-1) | oldCapacity与newCapacity- 1相等。
到此，我们就证明了j | highBit = e.hash & (newCapacity- 1)
其计算数据项在新数组中位置与原数组的计算逻辑是一样的，只不过十分巧妙的运用了位运算，好了，想明白这里恭喜你通过了第二个难点，我们继续向下分析。

24-34行就是遍历链表中数据项了，把他们挨个放入新数组中，这里思考一个问题？在原数组中同一链表的数据项在新数组中还处于同一链表吗？如果不是那么是什么决定它们不在同一链表了？

在上面分析的时候我们得出一个结论：如果HashMap中数组长度为2的n次方，那么同一链表中不同数据项的hash值的最后n位一定相同。

扩容后数组容量为原来的2倍了，根据index的计算方式e.hash & (newCapacity- 1)每个数据项的hash值是不变的，但是长度变了，所以同一链表中不同数据项在新数组中不一定还处于同一链表，那么具体是什么决定在新数组中二者在不在同一链表呢？

原数组长度为2的n次方，新数组长度扩容后为原数组2倍也就是2的n+1次方，原数组中同一链表中不同数据项的hash值的最后n位一定相同，所以新数组同一链表中不同数据项的hash值的最后n+1位一定相同。如果上面讲的你真的理解了，这里就不难理解，不过多解释。

在原数组中同一链表的数据项已经确保了hash值最后n位相同，按照计算方式新数组中处于同一链表的数据项需要确保hash值最后n+1位相同即可，既然原链表中的数据项最后n位已经相同了，在新数组中是否处于同一链表那么只需要比较同链表数据项hash值的第n+1位即可，如果相同则表明在新数组中依然处于同一链表，如果不同那么就处于不同链表了，上面的高位highBit就是取的是每一数据项的第n+1位，后面比较也只是比较每个数据项的highBit是否相同。

好了，这里我认为解释的已经很清楚了，这里你要是明白了，恭喜你，HashMap中最难理解的部分你已经完全掌握了。

至于24-34行具体逻辑我就不一一分析了，静下心来，自己试着分析，难度不大。

六、总结
好了，到这里本篇就要结束了，咦？这不就分析了一个put方法外加扩容机制吗？这就完了？是的，我想说的就这些，这部分是最难理解的，至于其余自己看看都能理解的差不多了。

最关键是一定要理解扩容机制，那几行最难理解的代码设计的真是巧妙