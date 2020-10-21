# JVM 内部原理（六）— Java 字节码基础之一

## 介绍

版本：Java SE 7

### 为什么需要了解 Java 字节码？

无论你是一名 Java 开发者、架构师、CxO 还是智能手机的普通用户，Java 字节码都在你面前，它是 Java 虚拟机的基础。

总监、管理者和非技术人员可以放轻松点：他们所要知道的就是开发团队在正在进行下一版的开发，Java 字节码默默的在 JVM 平台上运行。

**简单地说**，Java 字节码是 Java 代码（如，class 文件）的中间表现形式，它在 JVM 内部执行，那么为什么你需要关心它？因为如果没有 Java 字节码，Java 程序就无法运行，因为它定义了 Java 开发者编写代码的方式。

**从技术角度看**，JVM 在运行时将 Java 字节码以 JIT 的编译方式将它们转换成原生代码。如果没有 Java 字节码在背后运行，JVM 就无法进行编译并映射到原生代码上。

很多 IT 的专业技术人员可能没有时间去学习汇编程序或者机器码，可以将 Java 字节码看成是某种与底层代码相似的代码。但当出问题的时候，理解 JVM 的基本运行原理对解决问题非常有帮助。

在本篇文章中，你会知道如何阅读与编写 JVM 字节码，更好的理解运行时的工作原理，以及结构某些关键库的能力。

本篇文章会包括一下话题：

- 如何获得字节码列表
- 如何阅读字节码
- 语言结构是如何被编译器映射的：局部变量，方法调用，条件逻辑
- ASM 简介
- 字节码在其他 JVM 语言（如，Groovy 和 Kotlin）中是如何工作的

## 目录

- 为什么需要了解 Java 字节码？
- 第一部分：Java 字节码简介
  - 基础
  - 基本特性
  - JVM 栈模型
    - 方法体里面是什么？
    - 局部栈详解
    - 局部变量详解
    - 流程控制
    - 算术运算及转换
    - new & &
    - 方法调用及参数传递
- 第二部分：ASM
  - ASM 与工具
- 第三部分：Javassist
- 总结

## Java 字节码简介

Java 字节码是 JVM 里指令运行的形式。Java 程序员通常不需要知道 Java 字节码是如何工作的。不过了解平台底层的细节可以让我们成为更好的程序员（我们都想成为更好的程序员，难道不是吗？）

理解字节码以及 Java 编译器是如何生成字节码所带来的帮助，与 C 或 C++ 程序员具有汇编语言的知识一样。

了解字节码对于编写程序工具和程序分析至关重要，应用程序可以根据不同的领域修改字节码，调整应用程序的行为。性能分析工具，mocking 框架，AOP，要想编写这些工具，程序员就需要透彻理解 Java 字节码。

### 基础

让我们用一个非常基础的例子来带大家了解 Java 字节码是如何运行的。看看这个简单的表达式，1 + 2，用逆波兰式表示为 1 2 + 。这里使用逆波兰式标记有什么好处呢？因为这种表达式可以很容易的用栈来计算：

在执行完 “add” 指令后，结果 3 处于栈顶位置。

![image](http://note.youdao.com/yws/api/personal/file/WEBfb7c6ddf836494f6e9684dd7d4972b00?method=download&shareKey=98cb87a782509d8963deb45d83d6af93)

Java 字节码的计算模型是一个面向栈的编程语言。以上例子用 Java 字节码指令表示是一样的，唯一的不同是操作码有一些特定的语法：

![image](http://note.youdao.com/yws/api/personal/file/WEB7bf3137758955b0cb7752efc380c3f5e?method=download&shareKey=19f2439212f623bdb084214782b8be11)

操作码 **iconst_1** 和 **iconst_2** 将常量 1 和 2 分别进行入栈操作。指令 **iadd** 对两个整数进行求和操作，并将结果入栈到栈顶。

### 基本特性

就如名字里暗示的那样，**Java 字节码** 包括一个字节的指令，所以操作码有 256 种可能。真实的指令比允许的数量略少，大概使用的操作码有 200 个，有些操作码是为调试器（debugger）操作保留的。

指令是由一个类型前缀和操作名组成。例如，“i” 前缀表示 “integer”（整形），因此 iadd 指令表示求和操作是针对整数的。

根据指令的性质，我们可以将它们分为四类：

- **栈操作指令，包括本地变量的迭代**
- **流程控制指令**
- **对象操作，包括方法调用**
- **算术和类型转换**

也有指令是为一些特别的任务使用的，比如同步和抛出异常。

#### javap

为了得到编译好的类文件的指令列表，可以使用 javap 工具，这个标准的 Java 类文件反编译器是与 JDK 一起发布的。

让我们用一个应用程序（移动平均值计算器）作为示例：

```
public class Main {
    public static void main(String[] args){
        MovingAverage app = new MovingAverage();
    }
}
```

在类文件被编译后，为了得到以上字节码列表可以执行一下命令：

```
javap -c Main
```

结果如下：

```
Compiled from "Main.java"
public class algo.Main {
  public algo.Main();
	Code:
   	0: aload_0       
   	1: invokespecial #1       	// Method java/lang/Object."":()V
   	4: return        

  public static void main(java.lang.String[]);
	Code:
   	0: new           #2       	// class algo/MovingAverage
   	3: dup           
   	4: invokespecial #3         	// Method algo/MovingAverage."":()V
   	7: astore_1      
   	8: return        
}
```

可以发现有默认的构造器和一个主方法。Java 程序员可能都知道，如果没有为类指定任何构造器，仍然会有一个默认的构造器，但是可能并没有意识到它到底在哪。对，就在这里！这个默认的构造器就存在于被编译好的类中，所以它是 Java 编译器生成的。

构造器体是空的，但仍然会生成一些指令。为什么呢？每个构造器都会调用 super() ，对吗？这并不是自然而然生成的，这也是字节码指令生成缺省构造器的原因。基本上这就是 super() 的调用。

主方法创建了 MovingAverage 类的一个实例，然后返回。

可能你已经注意到有些指令引用 #1、#2、#3 这些数字参数。这些都是指向常量池的引用。那么我们如何找到这些常量？又如何查看列表中的常量池呢？可以通过使用带 **-verbose** 参数的 javap 对类进行反编译：

```
$ javap -c -verbose HelloWorld
```

以下打印出的部分有些地方比较有趣：

```
Classfile /Users/anton/work-src/demox/out/production/demox/algo/Main.class
  Last modified Nov 20, 2012; size 446 bytes
  MD5 checksum ae15693cf1a16a702075e468b8aaba74
  Compiled from "Main.java"
public class algo.Main
  SourceFile: "Main.java"
  minor version: 0
  major version: 51
  flags: ACC_PUBLIC, ACC_SUPER
Constant pool:
   #1 = Methodref          #5.#21         //  java/lang/Object."":()V
   #2 = Class              #22            //  algo/MovingAverage
   #3 = Methodref          #2.#21         //  algo/MovingAverage."":()V
   #4 = Class              #23            //  algo/Main
   #5 = Class              #24            //  java/lang/Object
```

这里有关于类的很多信息：它是何时编译的，MD5 校验值是什么？它是由哪个 *.java 文件编译而成的，它遵从 Java 的版本是什么，等等。

我们也可以看到访问标识（accessor flags）：ACC_PUBLIC 和 ACC_SUPER 。ACC_PUBLIC 标识从直观上比较容易理解：我们类是公有的，因此访问标识表明它是公有的。但 ACC_SUPER 有什么作用呢？ACC_SUPER 的引入是为了解决通过 invokespecial 指令调用 super 方法的问题。可以将它理解成 Java 1.0 的一个缺陷补丁，只有通过这样它才能正确找到 super 类方法。从 Java 1.1 开始，编译器始终会在字节码中生成 ACC_SUPER 访问标识。

也可以在常量池找到所表示的常量定义：

```
 #1 = Methodref          #5.#21         //java/lang/Object."":()V
```

常量的定义是可组成的，也就是说常量也可以由引用到相同表的其他常量组成。

当使用 javap -verbose 参数时，也可以发现其他的一些细节。方法可以输出更多信息：

```
public static void main(java.lang.String[]);
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=2, args_size=1
```

访问标识也会在方法中生成，同时也可以看到一个方法执行所需要的栈深度是多少，接收多少参数，以及本地变量表需要为本地变量保留多少个参数。

### JVM 栈模型

为了更详细的理解字节码，我们对字节码的执行模型有概念。JVM 是一个基于堆栈模式的虚拟机。每个线程都有一个 JVM 栈用来存储栈桢信息。每次方法被调用时都有桢被创建。桢内包括操作数栈，本地变量列表，以及当前类当前方法的运行时常量池的引用。这些都可以在开始的反编译的 Main 类中看到。

本地变量数组也被称为本地变量列表，它包括方法的参数，同时也用来保持本地变量的值。本地变量列表的大小是在编译时决定的，取决于数字和本地变量的大小和方法的参数。

![image](http://note.youdao.com/yws/api/personal/file/WEBee999df3ac863de8a4c11e33bb3f86ca?method=download&shareKey=70a712582aa689115bdc1d0066b451bd)

操作数栈是一个后进先出（LIFO）栈，用来对值进行入栈和出栈的操作。它的大小也是在编译时决定的。有些操作码指令将值入栈到操作数栈；有些进行出栈操作，对它们进行计算，并将结果入栈。操作数栈也用来接收方法的返回值。

在调试工具中，我们可以进行逐桢回退，但字段的状态并不会回退到之前状态。

![image](http://note.youdao.com/yws/api/personal/file/WEBaed1bc0f9d1821eee721b89a863f42db?method=download&shareKey=12042d3eeb2d7ebd6c28543e8db550c3)

#### 方法体里面是什么？

在查看 HelloWorld 例子中的字节码列表时，可能会想知道，每条指令前的数字表示什么？为什么数字之间的间隔不相等？

```
0: new           #2       // class algo/MovingAverage
3: dup           
4: invokespecial #3       // Method algo/MovingAverage."":()V
7: astore_1      
8: return
```

原因：有些操作码有参数需要占用字节码列表空间。例如，new 占用了列表中的三个位置：一个位置是留给它自己的，另外两个是留给输入参数的。因此，下一个指令 *dup* 处于下标索引 3 的位置。

以下如图所示，我们将方法体看成一个数组：

![image](http://note.youdao.com/yws/api/personal/file/WEB51c55a6a22025c0aa7139e95ef07ee41?method=download&shareKey=6119b03a838037c3b9f9370e6227e1ca)

每条指令都有自己的十六进制表示形式，我们可以得到方法体以十六进制字符串来表示如下：

![image](http://note.youdao.com/yws/api/personal/file/WEB55d94d2545917163b0672f750f67bfd5?method=download&shareKey=bf6c0e2c6f82dade1f8624bb4f85b0ca)

用十六进制编辑器打开类文件可以找到一下字符串：

![image](http://note.youdao.com/yws/api/personal/file/WEB229b5b929634c0bbabc39c05818a9b4c?method=download&shareKey=4bcfc47c17fd1782879a78fbc888febd)

也可以通过十六进制编辑器来修改字节码，尽管这么做比较易错。除此之外还有一些更简单的方式，可以使用字节码操作工具比如 ASM 或 Javassist 。

目前还和这个知识点没有太大关系，不过现在你已经知道这些数字的来源是什么。

#### 局部栈详解

操作栈的方式有多种多样。我们已经提到过一些基本栈操作指令：对值进行入栈或出栈操作。swap 指令可以将栈顶的两个值进行交换。

这里有些对栈内值进行操作的指令的示例。有些基本指令：dup 和 pop 。dup 指令将栈顶的值重复并再次入栈。pop 指令移除栈顶的值。

也有一些更复杂的指令如：**swap**、**dup_x1** 和 **dup2_x1** 。swap 指令和它名称预示的一样，将栈顶的两个值进行交换，如 A 和 B 交换位置；**dup_x1** 将栈顶处的值复制并插入到栈的底部（如 5）。**dup2_x1** 将栈顶处的两个值复制并插入到栈的底部（如 6）。

![image](http://note.youdao.com/yws/api/personal/file/WEB5c4b383a16f795e5fe8642b1fa5fba43?method=download&shareKey=d07db2af5b1780469eb55691f3eabbbf)

**dup_x1** 和 **dup2_x1** 指令看上去有点难懂 - 为什么会有人需要这种行为 - 复制栈顶的值并插入到栈底部？这里有一些更实际的例子：如何交换两个 double 类型的值？这里的问题是 double 类型需要占用栈中的两个位置，这也就意味着如果我们有两个 double 值，那么在栈中就会占四个位置。为了交换两个 double 值我们可能会想到使用 **swap** 指令，但问题是它只能操作一个字的指令，也就是说它无法操作 double ，指令 **swap2** 也不存在。替代方案可以使用 **dup2_x2** 指令复制栈顶的两个值，并将它们插入到栈底，然后我们可以使用 **pop2** 指令。这样，就能成功交换两个 double 值。

![image](http://note.youdao.com/yws/api/personal/file/WEB7311eb6eda5f7b51fa5bae2dad5b3183?method=download&shareKey=96a6cd933128f39b3e948d666b866197)

#### 局部变量详解

栈是用来执行的，本地变量是用来存储中间结果的，直接与栈发生交互。

现在让我们在之前的示例中增加一些代码：

```
public static void main(String[] args) {
  MovingAverage ma = new MovingAverage();

  int num1 = 1;
  int num2 = 2;

  ma.submit(num1);
  ma.submit(num2);

  double avg = ma.getAvg();
}
```

我为 MovingAverage 类提供两个值，并让他计算当前值的平均值。得到的 bytecode 如下：

```
Code:
   	0: new           #2          // class algo/MovingAverage
   	3: dup          
   	4: invokespecial #3          // Method algo/MovingAverage."":()V
   	7: astore_1     

   	8: iconst_1     
   	9: istore_2   

  	10: iconst_2     
  	11: istore_3     

  	12: aload_1      
  	13: iload_2      
  	14: i2d          
  	15: invokevirtual #4         // Method algo/MovingAverage.submit:(D)V

  	18: aload_1      
  	19: iload_3      
  	20: i2d          
  	21: invokevirtual #4         // Method algo/MovingAverage.submit:(D)V

  	24: aload_1      
  	25: invokevirtual #5         // Method algo/MovingAverage.getAvg:()D
  	28: dstore    	4
	LocalVariableTable:
  	Start  Length  Slot  Name   Signature
         	0  	31 	0  args   [Ljava/lang/String;
         	8  	23 	1  ma     Lalgo/MovingAverage;
        	10  	21 	2  num1   I
        	12  	19 	3  num2   I
        	30   	1 	4  avg    D
```

在创建好 MovingAverage 类型的本地变量后将值存储到本地变量 **ma** 中，用 **astore_1** 指令：1 是 **ma** 在本地变量表（LocalVariableTable）中的序号位置。

接着，指令 **iconst_1** 和 **iconst_2** 用来加载常量 1 和 2 将其入栈，然后通过 **istore_2** 和 **istore_3** 指令将它们存入 LocalVariableTable 的 2 和 3 的位置。

注意调用类 store 的指令实际上是进行出栈操作，这也是为什么为了再次使用变量值的时候，我们需要再次将其载入栈中。例如，在上述列表中，在调用 submit 方法之前，我们需要将参数的值再次载入栈中：

```
12: aload_1
13: iload_2
14: i2d
15: invokevirtual #4 // Method algo/MovingAverage.submit:(D)V
```

在调用 getAvg() 方法后返回的结果会入栈并存再次入本地变量中，使用 dstore 指令是因为目标变量的类型是 double 。

```
24: aload_1
25: invokevirtual #5 // Method algo/MovingAverage.getAvg:()D
28: dstore 4
```

更有趣的事情是本地变量列表（LocalVariableTable）第一个位置是由方法参数所占的。在我们当前的示例中，它是一个静态方法，在表中没有 this 的引用指向 0 位置。但是，对于非静态方法，this 会指向 0 位置。

![image](http://note.youdao.com/yws/api/personal/file/WEB5bfb2ed8f1c5b419f95c1474ef4c4139?method=download&shareKey=72236093602d56ce67b8f01d0c479515)

将这部分放在一边，一旦你想为本地变量赋值，这也意味着你想用相应的指令将其存储起来（**store**），例如，**astore_1** 。store 指令总是对栈顶的值进行出栈操作。相应的 **load** 指令会将值从本地变量列表取出并写入栈中，不过这个值不会从本地变量删除。

#### 流程控制

流程控制指令会根据不同情况组织执行顺序。If-Then-Else，三元操作码，各种循环，甚至各种错误处理操作码（opcodes）也属于 **Java 字节码** 流程控制。现在这些概念都变成了 jumps 和 gotos 。

现在我们对示例做一些更改，让它可以处理任意数目的数字传入到 MovingAverage 类的 submit 方法中：

```
MovingAverage ma = new MovingAverage();
for (int number : numbers) {
    ma.submit(number);
}
```

假设变量 numbers 是同一个类的静态字段。与在 numbers 上循环迭代对应的字节码如下：

```
0: new #2 // class algo/MovingAverage
3: dup
4: invokespecial #3 // Method algo/MovingAverage."":()V
7: astore_1
8: getstatic #4 // Field numbers:[I
11: astore_2
12: aload_2
13: arraylength
14: istore_3
15: iconst_0
16: istore 4
18: iload 4
20: iload_3
21: if_icmpge 43
24: aload_2
25: iload 4
27: iaload
28: istore 5
30: aload_1
31: iload 5
      33: i2d          
      34: invokevirtual #5       // Method algo/MovingAverage.submit:(D)V
      37: iinc      	4, 1
      40: goto      	18
      43: return
	LocalVariableTable:
  	Start  Length  Slot  Name   Signature
         30       7 	5  number I
         12      31 	2  arr$   [I
         15      28 	3  len$   I
         18      25 	4  i$     I
          0      49 	0  args   [Ljava/lang/String;
          8      41 	1  ma     Lalgo/MovingAverage;
         48   	1 	2  avg    D
```

在 8 和 16 位置的指令是用来组织循环控制的。可以看到在本地变量列表（ *LocalVariableTable* ）中有三个变量，它们没有在源代码中体现： *arr$* 、 *len$* 、 *i$* ，这些都是循环变量。变量 *arr$* 存储的 numbers 字段，循环的长度 *len$* 来自于数组长度指令 *arraylength* 。循环计数器， *i$* 在每个循环后用 *iinc* 指令增加。

循环体的第一个指令是用来比较循环计数器与数组长度的：

```
18: iload 4
20: iload_3
21: if_icmpge 43
```

我们载入 *i$* 和 *len$* 到栈中并调用 *if_icmpge* 来比较值的大小。 *if_icmpge* 指令的意思是如果一个值大于或等于另外一个值，在本例中就是如果 *i$* 大于或等于 *len$* ，那么执行会从被标记为 43 的语句执行。如果没有满足条件，则循环继续执行下一个迭代。

在循环结束时，循环计数器增加 1 循环跳回到循环条件开始的位置再次校验：

```
37: iinc          4, 1       // increment i$
40: goto      	18         // jump back to the beginning of the loop
```

#### 算术运算及转换

正如所见的那样，在 **Java 字节码**中，有一系列的指令可以进行算术运算。事实上，有很大一部分的指令集是用来表示算术运算的。有针对于各种整型、长整型、双精度、浮点数的加、减、乘、除、取负指令。除此之外，还有很多指令用来在不同类型间进行转换。

##### 算术操作码及其类型

类型转换发生在比如当我们想将整型值（integer）赋值到长整型（long）变量时。

![image](http://note.youdao.com/yws/api/personal/file/WEB99a1d9c620f84b12af7774784e4fc7f8?method=download&shareKey=7bb90fdcb6a30be788385857a639b15c)

##### Type conversion opcodes

在我们的例子中，整型值作为参数传入实际接收双精度的 submit() 方法，可以看到在方法真实调用之前，会应用到类型转换操作码：

![image](http://note.youdao.com/yws/api/personal/file/WEBaa1a371515912c2eabf63740965287c8?method=download&shareKey=b9f5db4a8df37da43a0c3911a4b4253c)

```
31: iload     	5	
33: i2d          
34: invokevirtual #5     // Method algo/MovingAverage.submit:(D)V
```

这表示我们将本地变量值以 integer 类型进行入栈操作，然后用 i2d 指令将其转换成 double 从而可以将其作为参数传入。

唯一不要求值在栈中的指令就是增量指令，**iinc**，它可以直接操作本地变量表（LocalVariableTable）上的值。其他所有的操作都是使用栈的。

#### new & <init> & <clinit>

在 Java 中有关键字 new ，在字节码指令中也有 new 的指令。当我们创建 MovingAverage 类实例时：

```
MovingAverage ma = new MovingAverage();
```

编译器生成一系列如下形式的操作码：

```
0: new #2 // class algo/MovingAverage
3: dup
4: invokespecial #3 // Method algo/MovingAverage."":()V
```

当你看到 **new**、**dup** 和 **invokespecial** 指令时，这时通常就代表着类实例的创建！

你可能会问，为什么是三条指令而不是一条？new 指令创建对象，但它并没有调用构造器，不过会调用 invokespecial 指令：它调用了一个特别的方法，它其实是构造器。因为构造器调用不返回值，在对象调用这个方法后，对象会被初始化，但此时栈是空的，在对象初始化之后，我们无法做任何事情。这正是为什么我们需要提前在堆栈中复制引用，在构造器返回后可以将对象实例赋值到本地变量或字段。因此，下一条指令通常是以下指令中的一条：

- **astore {N}** 或 **astore_{N}** – 给本地变量赋值，{N} 是变量在本地变量表的位置。
- **putfield** – 为实例字段赋值
- **putstatic** – 为静态变量赋值

在调用构造器之前，有另外一个类似的方法在此之前被调用。它是这个类的静态初始器。类的静态初始器并不是直接被调用的，而是由以下指令触发：new、getstatic、putstatic 或 invokestatic 。也就是说，如果你创建了类的一个实例，访问一个静态字段或调用一个静态方法，静态的初始器会被触发。

事实上，想要触发静态初始器的方式有很多，参见 [The Java® Language Specification - Java SE 7 Edition](http://docs.oracle.com/javase/specs/jvms/se7/html/)

#### 方法调用及参数传递

在类实例化的内容中，我们简单介绍了方法的调用：通过 invokespecial 指令调用的方法会调用构造器。但是，还有一些指令也用作于方法调用：

- **invokestatic** 正如名称所示，它调用类的静态方法。这里它是方法调用最快的指令。
- **invokespecial** 如我们知道的那样，指令用来调用构造器。但它也用来调用同一类的私有方法，以及父类可访问的方法。
- **invokevirtual** 用来调用公有，受保护的以及包私有方法，如果方法的目标对象是具体类型。
- **invokeinterface** 用来调用属于接口的方法。

那么 *invokevirtual* 和 *invokeinterface* 的区别是什么呢？

这确实是个好问题。为什么我们同时需要 *invokevirtual* 和 *invokeinterface* ，为什么不在所有地方使用 *invokevirtual* ？接口方法也还是公有方法啊！好，这都是为了方法调用的优化。首先，方法被解析，然后调用它。例如，有了 *invokestatic* 我们知道具体那个方法被调用了：它是静态的，只属于一个类。有了 *invokespecial* 我们的可选项是一个有限的列表，更容易选择解析策略，意味着运行时能更快找到需要的方法。

*invokevirtual* 和 *invokeinterface* 的区别并不是那么明显。我们对两个指令的区别提供一个非常简单的解释。试想类定义包括一个方法定义的列表，所有的方法都是按位置进行编号的。这里有个例子：类 A 有方法 method1 和 method2 以及一个子类 B ，子类 B 继承了 method1 覆写了 method2，并声明了方法 method3 。注意到 method1 和 method2 在类 A 和类 B 中处于同一索引下标位置。

```
class A
    1: method1
    2: method2

class B extends A
    1: method1
    2: method2
    3: method3
```

这意味着如果运行时想要调用方法 method2 ，它始终会在位置 2 被找到。现在，解释 *invokevirtual* 和 *invokeinterface* 之前，让类 B 扩展接口 X 定义一个新的方法 methodX ：

```
class B extends A implements X
    1: method1
    2: method2
    3: method3
    4: methodX
```

新方法在下标 4 的位置而且看上去和 method3 没有两样。但是，如果有另外一个类 C ，也实现了接口，但是和 A 和 B 的结构不太一样：

```
class C implements  X 
    1: methodC
    2: methodX
```

接口方法的位置和类 B 中的位置不太一样，这也是为什么 *invokeinterface* 在运行时更加严格，也就是说它在方法解析过程中要比 *invokeinterface* 做更少的推断假设。

## 参考

参考来源:

[The Java® Language Specification - Java SE 7 Edition](http://docs.oracle.com/javase/specs/jls/se7/html/index.html)

[The Java® Language Specification - Chapter 6. The Java Virtual Machine Instruction Set](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html)

[2015.01 A Java Programmer’s Guide to Byte Code](http://www.beyondjava.net/blog/java-programmers-guide-java-byte-code/)

[2012.11 Mastering Java Bytecode at the Core of the JVM](http://zeroturnaround.com/rebellabs/rebel-labs-report-mastering-java-bytecode-at-the-core-of-the-jvm/)

[2011.01 Java Bytecode Fundamentals](http://arhipov.blogspot.jp/2011/01/java-bytecode-fundamentals.html)

[2001.07 Java bytecode: Understanding bytecode makes you a better programmer](http://www.ibm.com/developerworks/ibm/library/it-haggar_bytecode/)

[Wiki: Java bytecode](https://en.wikipedia.org/wiki/Java_bytecode)

[Wiki: Java bytecode instruction listings](https://en.wikipedia.org/wiki/Java_bytecode_instruction_listings)