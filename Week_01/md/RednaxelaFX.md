作者：RednaxelaFX
链接：https://www.zhihu.com/question/27831730/answer/38266643
来源：知乎
著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。



**Java篇**

原先的问题讨论的是Java，这里先以Java为例来讨论。同时假设我们既有Java的源码也有对应的Java Class文件，只是想了解两者之间的映射关系。

**从Java源码到Java字节码**

Java的字节码本质上是Java AST通过后序遍历的线性序列化形式。几乎每个Java字节码opcode都有对应的Java语法结构。
只要熟悉Java的语法，能够在看到Java源码时想像出其解除语法糖之后的样子，然后对应的AST的样子，然后对这个AST后序遍历就能得到Java字节码。我在这里给过一个动画例子：[虚拟机随谈（一）：解释器，树遍历解释器，基于栈与基于寄存器，大杂烩](https://link.zhihu.com/?target=http%3A//rednaxelafx.iteye.com/blog/492667)
<- 请前读完这篇再继续向下读本回答。Java字节码就是一种“零地址指令”。

Java最主流的源码编译器，[javac](https://link.zhihu.com/?target=http%3A//docs.oracle.com/javase/7/docs/technotes/tools/windows/javac.html)，基本上不对代码做优化，只会做少量由Java语言规范要求或推荐的优化；也不做任何混淆，包括名字混淆或控制流混淆这些都不做。这使得javac生成的代码能很好的维持与原本的源码/AST之间的对应关系。换句话说就是javac生成的代码容易反编译。
Java Class文件含有丰富的符号信息。而且javac默认的编译参数会让编译器生成行号表，这些都有助于了解对应关系。

关于Java语法结构如何对应到Java字节码，在JVM规范里有相当好的例子：[Chapter 3. Compiling for the Java Virtual Machine](https://link.zhihu.com/?target=http%3A//docs.oracle.com/javase/specs/jvms/se7/html/jvms-3.html)
好好读完这章基本上就能手码字节码了。
记住一个要点就好：“运算”全部都在“操作数栈”（operand stack）上进行，每个运算的输入参数全部都在“操作数栈”上，运算完的结果也放到“操作数栈”顶。在多数Java语句之间“操作数栈”为空。

**从Java源码对应到Java字节码的例子**

题主之前说“从来不觉得阅读底层语言很容易，无论是汇编还是ByteCode还是IL”。我是觉得只要能耐心读点资料，Charles Nutter的[JVM Bytecodes for Dummies](https://link.zhihu.com/?target=http%3A//www.slideshare.net/CharlesNutter/javaone-2011-jvm-bytecode-for-dummies)，然后配合[The Java Virtual Machine Instruction Set](https://link.zhihu.com/?target=http%3A//docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html)，要理解Java字节码真的挺容易的。

口说无凭，举些简单的例子吧。把这些简单的例子组装起来，就可以得到完整方法的字节码了。

每个例子前半是Java代码，后面的注释是对应的Java字节码，每行一条指令。每条指令后面我还加了注释来表示执行完该指令后操作数栈的状态，就像[JVM规范的记法](https://link.zhihu.com/?target=http%3A//docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html%23jvms-6.4)一样，左边是栈底右边是栈顶，省略号表示不关心除栈顶附近几个值之外操作数栈上的值。

读取一个局部变量用<type>load系指令。

```java
local_var_0

//           // ...         ->
// iload_0   // ..., value0
```

<type>是类型前缀，有

- b: byte
- s: short
- c: char
- i: int
- l: long
- f: float
- d: double
- a: 引用类型

<type>load后面跟的参数是局部变量所在的位置（slot number）。其中对0到3的slot有特化的简短指令，例如iload_0。4和以上就用通用的load指令，例如iload 4。
存储一个局部变量用<type>store系指令。

```java
local_var_0 = ...

//            // ..., value0 ->
// istore_0   // ...
```

合并起来：

```java
local_var_1 = local_var_0;

//            // ...         ->
// iload_0    // ..., value0 ->
// istore_1   // ...
```

二元算术运算：

```java
... + ...

//            // ..., value1, value2 ->
// iadd       // ..., sum
```

结合读取局部变量：

```java
local_var_0 + local_var_1

//            // ...                 ->
// iload_0    // ..., value0         ->
// iload_1    // ..., value0, value1 ->
// iadd       // ..., sum
```

结合保存到局部变量：

```java
local_var_2 = local_var_0 + local_var_1;

//            // ...                 ->
// iload_0    // ..., value0         ->
// iload_1    // ..., value0, value1 ->
// iadd       // ..., sum            ->
// istore_2   // ...
```

连续加两次：

```java
local_var_3 = local_var_0 + local_var_1 + local_var_2

//            // ...                 ->
// iload_0    // ..., value0         ->
// iload_1    // ..., value0, value1 ->
// iadd       // ..., sum1           ->
// iload_2    // ..., sum1, value2   ->
// iadd       // ..., sum2           ->
// istore_3   // ...
```

返回结果：

```java
return ...;

//            // ..., value ->
// ireturn    // ...
```

 返回一个局部变量：

```java
return local_var_0;

//            // ...         ->
// iload_0    // ..., value0 ->
// ireturn    // ...
```

返回一个加法：

```java
return local_var_0 + local_var_0

//            // ...                 ->
// iload_0    // ..., value0         ->
// dup        // ..., value0, value0 ->
// iadd       // ..., sum            ->
// ireturn    // ...
```


<type>const_<val>、bipush、sipush、ldc这些指令都用于向操作数栈压入常量。例如：

```java
1    // iconst_1
true // iconst_1    // JVM的类型系统里，整型比int窄的类型都统一带符号扩展到int来表示
127  // bipush 127  // 能用一个字节表示的带符号整数常量
1234 // sipush 1234 // 能用两个字节表示的带符号整数常量
12.5 // ldc 12.5    // 较大的整型常量、float、double、字符串常量用ldc
```


创建一个对象，用空参数的构造器：

```java
new Object()

//                                           // ...           ->
// new java/lang/Object                      // ..., ref      ->
// dup                                       // ..., ref, ref ->
// invokespecial java/lang/Object.<init>()V  // ..., ref
```

关于这段字节码的解释，请用下面两个传送门：

- [实例构造器是不是静态方法？](https://link.zhihu.com/?target=http%3A//rednaxelafx.iteye.com/blog/652719)
- [答复: 不用构造方法也能创建对象](https://link.zhihu.com/?target=http%3A//rednaxelafx.iteye.com/blog/850082)

关键点在于：new指令只复制分配内存与默认初始化，包括设置对象的类型，将对象的Java字段都初始化到默认值；调用构造器来完成用户层面的初始化是后面跟着的一条invokespecial完成的。

使用this：

```java
this

//            // ...       ->
// aload_0    // ..., this
```

这涉及到Java字节码层面的“方法调用约定”（calling convention）：参数从哪里传出和传入，通过哪里返回。读读[这里](https://link.zhihu.com/?target=http%3A//docs.oracle.com/javase/specs/jvms/se7/html/jvms-3.html%23jvms-3.6)和[这里](https://link.zhihu.com/?target=http%3A//docs.oracle.com/javase/specs/jvms/se7/html/jvms-3.html%23jvms-3.7)就好了。
静态方法，方法参数会从局部变量区的第0～(n-1)个slot从左到右传入，假如有n个参数；
实例方法，方法参数会从局部变量区的第1～n个slot从左到右传入，假如有n个显式参数，第0个slot传入this的引用。所以在Java源码里使用this，到字节码里就是aload_0。

在被调用方看有传入的东西，必然都是在调用方显式传出的。传出的办法就是在invoke指令之前把参数压到操作数栈上。当然，“this”的引用也是这样传递的。

方法真正的局部变量分配在参数之后的slot里。常见的不做啥优化的Java编译器会按照源码里局部变量出现的顺序来分配slot；如果有局部变量的作用域仅在某些语句块里，那么在它离开作用域后后面新出现的局部变量可以复用前面离开了作用域的局部变量的slot。
这方面可以参考我以前写的一个演示稿的第82页：[Java 程序的编译，加载 和 执行](https://link.zhihu.com/?target=http%3A//www.valleytalk.org/2011/07/28/java-%E7%A8%8B%E5%BA%8F%E7%9A%84%E7%BC%96%E8%AF%91%EF%BC%8C%E5%8A%A0%E8%BD%BD-%E5%92%8C-%E6%89%A7%E8%A1%8C/)

继续举例。
调用一个静态方法：

```java
int local_var_2 = Math.max(local_var_0, local_var_1);

//                                       // ...                 ->
// iload_0                               // ..., value0         ->
// iload_1                               // ..., value0, value1 ->
// invokestatic java/lang/Math.max(II)I  // ..., result         ->
// istore_2                              // ...
```

调用一个公有实例方法：

```java
local_var_0.equals(local_var_1)

// aload_0   // 压入对象引用，作为被调用方法的“this”传递过去
// aload_1   // 压入参数
// invokevirtual java/lang/Object.equals(Ljava/lang/Object;)Z
```

Java字节码的方法调用使用“符号引用”（symbolic reference）来指定目标，非常容易理解，而不像native binary code那样用函数地址。

读取一个字段：

```java
this.x // 假设this是mydemo.Point类型，x字段是int类型

//                            // ...        ->
// aload_0                    // ..., ref   ->
// getfield mydemo.Point.x:I  // ..., value
```

写入一个字段：

```java
this.x = local_var_1 // 假设this是mydemo.Point类型，x字段是int类型

//                            // ...             ->
// aload_0                    // ..., ref        ->
// iload_1                    // ..., ref, value ->
// putfield mydemo.Point.x:I  // ...
```

循环的代码生成例子，我在[对C语义的for循环的基本代码生成模式](https://link.zhihu.com/?target=http%3A//rednaxelafx.iteye.com/blog/1961217)发过一个。这里就不写了。
其它控制流，例如条件分支与无条件分支，感觉都没啥特别需要说的…

异常处理…有人问到再说吧。

**从Java字节码到Java源码**

上面说的是从Java源码->Java字节码方向的对应关系，那么反过来呢？
反过来的过程也就是“反编译”。反编译Java既有现成的反编译器（[Procyon](https://link.zhihu.com/?target=https%3A//bitbucket.org/mstrobel/procyon/wiki/Java%20Decompiler)、[JD](https://link.zhihu.com/?target=http%3A//jd.benow.ca/)、[JAD](https://link.zhihu.com/?target=http%3A//varaneckas.com/jad/)之类，[这里](https://link.zhihu.com/?target=https%3A//developer.jboss.org/people/ozizka/blog/2014/05/06/java-decompilers-a-sad-situation-of)有更完整的列表），也有些现成的资料描述其做法，例如：

- 书：[Covert Java: Techniques for Decompiling, Patching, and Reverse Engineering: Alex Kalinovsky](https://link.zhihu.com/?target=http%3A//www.amazon.com/Covert-Java-Techniques-Decompiling-Engineering/dp/0672326388)
- 书：[Decompiling Java: Godfrey Nolan](https://link.zhihu.com/?target=http%3A//www.amazon.com/Decompiling-Java-Godfrey-Nolan/dp/1590592654)
- 老论文：[Java バイトコードをデコンパイルするための効果的なアルゴリズム](https://link.zhihu.com/?target=http%3A//openjit.org/publications/pro1999-06/decompiler-pro-199906.pdf)（An Effective Decompilation Algorithm for Java Bytecodes）

两本书里前一本靠谱一些，后一本过于简单不过入门读读可能还行。

论文是日文的不过写得还挺有趣，可读。它的特点是通过[dominator tree](https://link.zhihu.com/?target=http%3A//en.wikipedia.org/wiki/Dominator_(graph_theory))来恢复出Java层面的控制流结构。
它的背景是当时有个用Java写的研究性Java JIT编译器叫[OpenJIT](https://link.zhihu.com/?target=http%3A//openjit.org/)，先把Java字节码反编译为Java AST，然后再对AST应用传统的编译技术编译到机器码。
这种做法在90年代末的JIT挺常见，JRockit最初的JIT编译器也是用这个思路实现。但很快大家就发现干嘛一定要费力气先反编译Java字节码到AST再编译到机器码呢，直接把Java字节码转换为基于图的、有显式控制流和基本块的IR不就好了么。所以比较新的Java JIT编译器都不再做“反编译”这一步了。

这些比较老的资料从现在的角度看最大的问题是对JDK 1.4.2之后的javac对try...catch...finally生成的代码的处理不完善。由于较新的javac会把finally块复制到每个catch块的末尾，生成了冗余代码，在复原源码时需要识别出重复的代码并对做tail deduplication（尾去重）才行。以前老的编译方式则是用jsr/ret，应对方式不一样。

**从Java字节码对应到Java源码的例子**

首先，我们要掌握一些工具，帮助我们把二进制的Class文件转换（“反汇编”）为比较好读的文本形式。最常用的是JDK自带的[javap](https://link.zhihu.com/?target=http%3A//docs.oracle.com/javase/7/docs/technotes/tools/windows/javap.html)。要获取最详细的信息的话，用以下命令：

```text
javap -cp <your classpath> -c -s -p -l -verbose <full class name>
```

例如，要看java.lang.Object的Class文件的内容，可以执行：

```text
javap -c -s -p -l -verbose java.lang.Object
```

提取其中java.lang.Object.equals(Object)的部分出来：

```text
  public boolean equals(java.lang.Object);
    Signature: (Ljava/lang/Object;)Z
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=2, args_size=2
         0: aload_0       
         1: aload_1       
         2: if_acmpne     9
         5: iconst_1      
         6: goto          10
         9: iconst_0      
        10: ireturn       
      LineNumberTable:
        line 150: 0
      StackMapTable: number_of_entries = 2
           frame_type = 9 /* same */
           frame_type = 64 /* same_locals_1_stack_item */
          stack = [ int ]
```

（为了演示方便我删除了一些重复输出的属性表）
可以看到这里不但有Java字节码，还有丰富的元数据（metadata）描述这段代码。

让我们先从Java字节码的部分看起。在Class文件里，Java字节码位于方法的Code属性表里。

```text
0: aload_0
```

javap的这个显示格式，开头的数字就是bci（bytecode index，字节码偏移量）。bci是从该方法的字节码起始位置开始算的偏移量。后面跟的是字节码指令，以及可选的字节码参数。

如何把字节码转换回成Java代码呢？有些不错的算法可以机械地复原出Java AST。这个例子我们先用比较简单的思路人肉走一遍流程。
下面用一种新的记法来跟踪Java程序的局部变量与表达式临时值的状态，例如：

```text
[ 0: this, 1: x, 2: undefined | this, null ]
```

这个记法用方括号括住一个Java栈帧￼的状态。中间竖线是分隔符，左边是局部变量区，右边是操作数栈。局部变量区每个slot有标号，也就是slot number，这块可以随机访问；操作数栈的slot则没有标号，通常只能访问栈顶或栈顶附近的slot。
跟之前用的记法类似，操作数栈也是靠左边是栈底，靠右边是栈顶。
局部变量区里如果有slot尚未赋初始值的话，则标记为undefined。

让我们试着用这个记法来跟踪一下Object.equals(Object)的程序状态。
根据上文提到的Java calling convention，从该方法的signature（方法参数列表类型和返回值类型。[Method Signature](https://link.zhihu.com/?target=http%3A//docs.oracle.com/javase/specs/jls/se7/html/jls-8.html%23jls-8.4.2)是Java层面的叫法；在JVM层面叫做[Method Descriptor](https://link.zhihu.com/?target=http%3A//docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html%23jvms-4.3.3)）——(Object)boolean，或者用JVM内部表现方式 (Ljava/lang/Object;)Z——我们可以知道在进入该方法的时候局部变量区的头两个slot已经填充上了参数——实例方法的slot 0是this，slot 1是第一个显式参数。
局部变量区有多少个slot是传入的参数可以看javap输出的“args_size”属性，此例为2；局部变量区总共有多少个slot可以看“locals”属性，此例为2，跟args_size一样说明这个方法没有声明任何具名的局部变量；操作数栈最高的高度可以看“stack“属性，此例为2。
我们先不管具体的参数名，后面再说；先用arg0来指代“第一个参数”。

```text
                   // [ 0: this, 1: arg0 | ]
 0: aload_0        // [ 0: this, 1: arg0 | this ]
 1: aload_1        // [ 0: this, 1: arg0 | this, arg0 ]
 2: if_acmpne  9   // [ 0: this, 1: arg0 | ]           // if (this != arg0) goto bci_9
 5: iconst_1       // [ 0: this, 1: arg0 | 1 ]
 6: goto       10  // [ 0: this, 1: arg0 | 1 ]         // goto bci_10
 9: iconst_0       // [ 0: this, 1: arg0 | 0 ]
10: ireturn        // [ 0: this, 1: arg0 | phi(0, 1) ] // return phi(0, 1)
```

这要如何理解呢？

- 当指令使值从局部变量压到操作数栈的时候，我们只是记下栈的变化，其它什么都不用做。
- 当指令从操作数栈弹出值并且进行运算的时候，我们记下栈的变化并且记下运算的内容。
- 当指令是控制流（跳转）时，记录下跳转动作。
- 当指令是控制流交汇处（例如这里的bci 10的位置，既可以来自bci 6也可以来自bci 9），用“phi”函数来合并栈帧中对应位置的值的状态。这里例子里，phi(0, 1)表示这个slot既可能是0也可能是1，取决于前面来自哪条指令。
- 正统的做法应该把基本块（basic block）划分好并且构建出控制流图（CFG，control flow graph）。这个例子非常简单所以先偷个懒硬上。

其实上述过程就是一种“抽象解释”（[abstract interpretation](https://link.zhihu.com/?target=http%3A//en.wikipedia.org/wiki/Abstract_interpretation)）：我们实际上对字节码做了解释执行，只不过不以“运算出最终结果”为目的，而是以“提取出代码的某些特点”为目的。
之前有另外一个问题：[如何理解抽象解释（abstract interpretation）？ - 编程语言](http://www.zhihu.com/question/27789493)，这就是抽象解释的一个应用例子。
Wikipedia的[Decompiler](https://link.zhihu.com/?target=http%3A//en.wikipedia.org/wiki/Decompiler)词条也值得一读，了解一下大背景。

把上面记录下的代码整理出来，就是：

```java
if (this == arg0) {
  tmp0 = 1;
} else {
// bci_9:
  tmp0 = 0;
}
// bci_10:
return tmp0;
```

这里做了几项“整理”：

- 把if的判断条件“反过来”，跳转目标也“反过来。这是因为javac在为条件分支生成代码时，通常把then分支生成为fall through（直接执行下一条指令而不跳转），而把else分支生成为显式跳转。这样跳转的条件就正好跟源码相反。既然我们要从字节码恢复出源码，这里就得再反回去。
- 把操作数栈上出现了phi函数的slot在恢复出的源码里用临时变量tmp来代替。这样就可以知道到底哪个分支里应该取哪个值。

现在这个源码已经挺接近真正的源码。我们还需要做少许修正：

- 通过方法的signature，我们知道Object.equals(Object)boolean返回值是boolean类型的。前面提到了JVM字节码层面的类型系统boolean是提升到int来表示的，所以这里的1和0其实是true和false。
- if (compare) { true } else { false }，其实就是compare本身。只不过JVM字节码指令集没有返回boolean结果的比较指令，而只有带跳转的比较指令，所以生成出的代码略繁琐略奇葩。这样可以化简出tmp0 = this == arg0;
- 所有在我们的整理过程中添加的tmp变量在原本的源码里肯定不是有名字的局部变量，而是没有名字的临时值。在恢复源码时要尽量想办法消除掉。例如说return tmp0;就应该尽量替换成return ...，其中...是计算tmp0的表达式。

结合上述三点修正，我们可以得到：

```java
public boolean equals(Object arg0) {
  return this == arg0;
}
```

而这跟Object.equals(Object)boolean真正的源码几乎一样了：

```java
    public boolean equals(Object obj) {
        return (this == obj);
    }
```

如何？小试牛刀感觉还不错？

我们可以再试一个简单的算术运算例子。假如有下述字节码（及signature）：

```text
  public static java.lang.Object add3(int, int, int);
    Code:
      stack=2, locals=4, args_size=3
         0: iload_0       
         1: iload_1       
         2: iadd          
         3: istore_3      
         4: iload_3       
         5: iload_2       
         6: iadd          
         7: istore_3      
         8: iload_3       
         9: invokestatic  #2 // Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
        12: areturn
```

跟前面的例子一样，我们先根据方法的signature创建出初始的栈帧状态，然后再一条条指令抽象解释下去。
这是个静态方法，没有隐含参数this。根据args_size=3可知slot 0-2是传入的参数，locals=4所以有一个显式声明的局部变量，stack=2所以操作数栈最高高度为2。

```text
              // [ 0: arg0, 1: arg1, 2: arg2, 3: undefined | ]
 0: iload_0   // [ 0: arg0, 1: arg1, 2: arg2, 3: undefined | arg0 ]
 1: iload_1   // [ 0: arg0, 1: arg1, 2: arg2, 3: undefined | arg0, arg1 ]
 2: iadd      // [ 0: arg0, 1: arg1, 2: arg2, 3: undefined | tmp0 ] // tmp0 = arg0 + arg1
 3: istore_3  // [ 0: arg0, 1: arg1, 2: arg2, 3: loc3 | ]           // int loc3 = tmp0
 4: iload_3   // [ 0: arg0, 1: arg1, 2: arg2, 3: loc3 | loc3 ]
 5: iload_2   // [ 0: arg0, 1: arg1, 2: arg2, 3: loc3 | loc3, arg2 ]
 6: iadd      // [ 0: arg0, 1: arg1, 2: arg2, 3: loc3 | tmp1 ]      // tmp1 = loc3 + arg2
 7: istore_3  // [ 0: arg0, 1: arg1, 2: arg2, 3: loc3 | ]           // loc3 = tmp1
 8: iload_3   // [ 0: arg0, 1: arg1, 2: arg2, 3: loc3 | loc3 ]
 9: invokestatic  #2 // Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
              // [ 0: arg0, 1: arg1, 2: arg2, 3: loc3 | tmp2 ]      // tmp2 = Integer.valueOf(loc3)
12: areturn   // [ 0: arg0, 1: arg1, 2: arg2, 3: loc3 | ]           // return tmp2
```

这个抽象解释过程的原理跟上一例基本一样，跟踪压栈动作，记录弹栈和运算动作。
只有两点新做法值得留意：

- 显式声明的局部变量，在还没有进入作用域之前还没有值，记为undefined。当抽象解释到某个局部变量slot首次被赋值，也就是从undefined变为有意义的值的时候，把记录下的代码写成局部变量声明，类型就用赋值进来的值的类型。后面我们会看到局部变量的声明的类型有可能还要受后面代码的影响而需要调整，现在可以先不管。
- 每当从操作数栈弹出值，进行运算后要把结果压回到操作数栈上。为了方便记录，我们把运算用临时变量记着，并把临时变量压回到栈上。这样就不用把栈里的状态写得那么麻烦。

把记录下的代码整理出来，得到：

```java
tmp0 = arg0 + arg1
int loc3 = tmp0
tmp1 = loc3 + arg2
loc3 = tmp1
tmp2 = Integer.valueOf(loc3)
return tmp2
```

上一例也提到过，我们要尽量消除掉新添加的tmp临时变量，因为它们不是原本源码里存在的局部变量。修正后得到：

```java
public static Object add3(int arg0, int arg1, int arg2) {
  int loc3 = arg0 + arg1;
  loc3 = loc3 + arg2;
  return Integer.valueOf(loc3);
}
```

留意：包装类型的valueOf()方法可能是源码里显式调用的，也可能是编译器给自动装箱（autoboxing）生成代码时生成的。所以遇到Integer.valueOf(loc3)的话，反编译出loc3也正确，让编译器区做自动装箱。
整理出来的代码跟我原本写的源码一致：

```java
  public static Object add3(int x, int y, int z) {
    int result = x + y;
    result = result + z;
    return result;
  }
```

就差参数/局部变量名和行号了。

其次，我们要充分利用Java Class文件里包含的符号信息。

如果我们用的是debug build的JDK，那么javap得到的信息会更多。还是以java.lang.Object.equals(Object)为例，

```text
  public boolean equals(java.lang.Object);
    Signature: (Ljava/lang/Object;)Z
    flags: ACC_PUBLIC
    Code:
      stack=2, locals=2, args_size=2
         0: aload_0       
         1: aload_1       
         2: if_acmpne     9
         5: iconst_1      
         6: goto          10
         9: iconst_0      
        10: ireturn       
      LineNumberTable:
        line 150: 0
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
               0      11     0  this   Ljava/lang/Object;
               0      11     1   obj   Ljava/lang/Object;
      StackMapTable: number_of_entries = 2
           frame_type = 9 /* same */
           frame_type = 64 /* same_locals_1_stack_item */
          stack = [ int ]
```

Class文件里每个方法可以有许多元数据，里面可以包含丰富的符号信息。
其中有3个属性表含有非常重要的符号信息：

- LineNumberTable：行号表。顾名思义，它记录了 源码里的行号 -> 该行的代码的起始bci 的映射关系。javac默认会生成该属性表，也可以显式通过-g:lines参数指定生成。
- LocalVariableTable：局部变量表。它记录了 源码里的变量名和类型 -> 局部变量区的slot number以及作用域在什么bci范围内。javac默认不会生成该属性表，需要通过-g:vars或-g参数来指定生成。该属性表记录的类型是“擦除泛型”之后的类型。
- LocalVariableTypeTable：局部变量类型表。这是泛型方法才会有的属性表，用于记录擦除泛型前源码里声明的类型。javac默认也不会生成该属性表，跟上一个表一样要用参数指定。

这三个属性表通常被称为“调试符号信息”。事实上，Java的调试器就是通过它们来在某行下断点、读取局部变量的值并映射到源码的变量的。放几个传送门：
[为什么有时候调试代码的时候看不到变量的值。](https://link.zhihu.com/?target=http%3A//hllvm.group.iteye.com/group/topic/25798)
[LocalVariableTable有点迷糊](https://link.zhihu.com/?target=http%3A//hllvm.group.iteye.com/group/topic/25858)
[LocalVariableTable属性、LineNumberTable属性](https://link.zhihu.com/?target=http%3A//hllvm.group.iteye.com/group/topic/38505)
换句话说，如果没有LocalVariableTable，调试器就无法显示参数/局部变量的值（因为不知道某个名字的局部变量对应到第几个slot）；如果没有LineNumberTable，调试器就无法在某行上下断点（因为不知道行号与bci的对应关系）。
Oracle/Sun JDK的product build里，rt.jar里的Class文件都只有LineNumberTable而没有LocalVariableTable，所以只能下断点调试却不能显示参数/局部变量的值。
我是推荐用javac编译Java源码时总是传-g参数，保证所有调试符号信息都生成出来，以备不时之需。像Maven的Java compiler插件默认配置<debug>true</debug>，实际动作就是传-g参数给javac，如果想维持可调试性的话请不要把它配置为false。这些调试符号信息消耗不了多少空间，不会影响运行时性能，不要白不要——除非您的目的是想阻挠别人调试⋯

这个例子不是泛型方法所以没有LocalVariableTypeTable，只有LineNumberTable和LocalVariableTable。
LineNumberTable只有一项，说明这个方法只有一行有效的源码，第150行映射到bci [0, 11)这个半开区间。
LocalVariableTable有两项，正好描述的都是参数。它们的作用域都是bci [0, 11)这个半开区间；start和length描述的是 [start, start+length) 范围。它们的类型都是引用类型java.lang.Object。它们的名字，slot 0 -> this，slot 1 -> obj。
应用上这些符号信息，我们就可以把前面例子中反编译得到的：

```java
public boolean equals(Object arg0) {
  return this == arg0;
}
```

修正为：

```java
public boolean equals(Object obj) {
  return this == obj; // line 150
}
```

与原本的源码完美吻合。

终于铺垫了足够背景知识来回过头讲讲题主原本在[java.lang.NullPointerException为什么不设计成显示null对象的名字或类型？ - RednaxelaFX 的回答](http://www.zhihu.com/question/27824895/answer/38256110)下的疑问了。
假如一行源码有多个地方要解引用（dereference），每个地方都有可能抛出NullPointerException，但由此得到的stack trace的行号都是一样的，无法区分到底是哪个解引用出了问题。假如stack trace带上bci，问题就可以得到完美解决——前提是用户得能看懂bci对应到源码的什么位置。

于是让我们试一个例子。我先不说这是什么方法，只给出一小段字节码以及相关的调试符号信息：

```text
            44: aload_1       
            45: aload_0       
            46: getfield      #12                 // Field elementData:[Ljava/lang/Object;
            49: iload_2       
            50: aaload        
            51: invokevirtual #31                 // Method java/lang/Object.equals:(Ljava/lang/Object;)Z
            54: ifeq          59

          LineNumberTable:
            line 302: 44
            line 303: 57

          LocalVariableTable:
            Start  Length  Slot  Name   Signature
               36      29     2     i   I
                0      67     0  this   Ljava/util/ArrayList;
                0      67     1     o   Ljava/lang/Object;

          LocalVariableTypeTable:
            Start  Length  Slot  Name   Signature
                0      67     0  this   Ljava/util/ArrayList<TE;>;
```

从LineNumberTable可以知道，源码第302行对应到bci [44, 57)的半开区间。
从LocalVariableTable可以知道，在这段字节码的范围内每个slot到局部变量名的映射关系。
仅凭以上信息无法知道当前操作数栈的高度，不过这种上下文里通常我们可以不关心它的初始高度，暂时忽略就好。
然后让我们来抽象解释一下这段字节码：

```text
              // [ 0: this, 1: o, 2: i | ... ]
44: aload_1   // [ 0: this, 1: o, 2: i | ..., o ]
45: aload_0   // [ 0: this, 1: o, 2: i | ..., o, this ]
46: getfield      #12 // Field elementData:[Ljava/lang/Object;
              // [ 0: this, 1: o, 2: i | ..., o, tmp0 ] // tmp0 = this.elementData
49: iload_2   // [ 0: this, 1: o, 2: i | ..., o, tmp0, i ]
50: aaload    // [ 0: this, 1: o, 2: i | ..., o, tmp1 ] // tmp1 = tmp0[i]
51: invokevirtual #31 // Method java/lang/Object.equals:(Ljava/lang/Object;)Z
              // [ 0: this, 1: o, 2: i | ..., tmp2 ]    // tmp2 = o.equals(tmp1)
54: ifeq          59
              // [ 0: this, 1: o, 2: i | ... ]          // if (tmp2) goto bci_59
```

整理出来：

```text
tmp0 = this.elementData // bci 46
tmp1 = tmp0[i]          // bci 50
tmp2 = o.equals(tmp1)   // bci 51
if (tmp2) goto bci_59   // bci 54
```

可以很明显的看到这行代码有3处解引用，分别位于bci 46、50、51。当然，Java的实例方法的语义保证了此处this不会是null，所以能抛NPE的只能是bci 50和51两处。
消除掉临时变量恢复出源码，这行代码是：

```java
if (o.equals(this.elementData[i])) { // ...
```

实际源码在此：[http://hg.openjdk.java.net/jdk8/jdk8/jdk/file/tip/src/share/classes/java/util/ArrayList.java#l302](https://link.zhihu.com/?target=http%3A//hg.openjdk.java.net/jdk8/jdk8/jdk/file/tip/src/share/classes/java/util/ArrayList.java%23l302) 是 java.util.ArrayList.indexOf(Object)int 的其中一行。

假如有NullPointerException的stack trace带有bci，显示：

```text
java.lang.NullPointerException
        at java.util.ArrayList.indexOf(ArrayList.java:line 302, bci 51)
        ...
```

那么我们很容易就知道这里o是null，而不是elementData是null。

通常大家会写在一行上的代码都不会很多，很少会有复杂的控制流所以通常可以不管它，用这种简单的人肉分析法以及足以应付分析抛NPE时bci到源码的对应关系。

爽不？

实际的Java Decompiler是怎么做的，可以参考开源的[Procyon](https://link.zhihu.com/?target=https%3A//bitbucket.org/mstrobel/procyon/src/tip/Procyon.CompilerTools/src/main/java/com/strobel/decompiler/%3Fat%3Ddefault)的实现。

上面的讨论都是基于“要分析的字节码来自javac编译的Java源码”。如果不是javac或者ecj这俩主流编译器生成的，或者是经过了后期处理（各种优化和混淆过），那就没那么方便了，必须用更强力的办法来抵消掉一些优化或混淆带来的问题。

===================================================================

**.NET篇**

题主所说的IL多半说的是MSIL，而不是泛指“中间语言”吧？

.NET的纯托管程序里的MSIL（或称为CIL）的分析方法跟前面Java篇所说的类似。
不同的是，Java世界里大家主要用javac编译出Class文件，程序怎么被优化；而C#程序的发布版通常会用csc /o优化编译，代码与原本的源码的对应关系可能会受到影响，所以一般.NET反编译出来的源码相对原本的源码的差距，可能会比一般Java程序反编译出来的大一些。这只是一般论。

关于MSIL的知识，去看[ECMA-335](https://link.zhihu.com/?target=http%3A//www.ecma-international.org/publications/standards/Ecma-335.htm)规范自然好，另外也有不少现成的书可读：
[Inside Microsoft .NET IL Assembler](https://link.zhihu.com/?target=http%3A//book.douban.com/subject/1890118/)
[Expert .NET 2.0 IL Assembler](https://link.zhihu.com/?target=http%3A//book.douban.com/subject/2046202/)
[微软.NET程序的加密与解密](https://link.zhihu.com/?target=http%3A//book.douban.com/subject/3274597/) <- 看雪论坛的大大们写的书。开头有一章是介绍MSIL的。

研究MSIL的工具方面，ildasm（IL disassembler） 与 ilasm（IL assembler） 的组合完爆JDK的javap。前者能实现汇编-反汇编-汇编的roundtrip，使得实现学习MSIL非常顺手；而后者只能反汇编，不能再汇编成Class文件。

公平的说，Java也有许多第三方工具/库可以手写字节码。比较老的有例如[Jasmin](https://link.zhihu.com/?target=http%3A//jasmin.sourceforge.net/)，比较新的有例如[bitescript](https://link.zhihu.com/?target=https%3A//github.com/headius/bitescript)或[jitescript](https://link.zhihu.com/?target=https%3A//github.com/qmx/jitescript)，但它们有些很老了跟不上时代的步伐，而且全部都只能汇编而不能反汇编，无法达成roundtrip，总之就是略麻烦。
更新：2016年的现在OpenJDK项目里有jasm/jdis工具，终于可以跟.NET的iladm/ildasm一样roundtrip了，简直赞！请跳传送门：[是否有工具能够直接使用JVM字节码编写程序？ - RednaxelaFX 的回答](https://www.zhihu.com/question/50295665/answer/120401868)

反编译器方面.NET也有若干选择。以前很长一段时间[.NET Reflector](https://link.zhihu.com/?target=http%3A//www.red-gate.com/products/dotnet-development/reflector/)都是大家的不二之选，但自从它彻底商业化不再免费之后，大家又要寻找新的选择了。[ILSpy](https://link.zhihu.com/?target=http%3A//ilspy.net/)似乎是新的主流选择，免费开源；[JetBrains dotPeek](https://link.zhihu.com/?target=https%3A//www.jetbrains.com/decompiler/)免费不开源。