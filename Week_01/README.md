# 学习笔记

## 作业1：Hello.java

分析字节码，包含基本数据类型、四则运算、对象引用、流程控制

``` java
1	/**
2	 * ClassName:Hello
3	 * Package:PACKAGE_NAME
4	 * Description:
5	 *  基本类型，四则运行，if 和 for，然后自己分析一下对应的字节码
6	 * @author:yellowrq
7	 * @date: 2020/10/19 16:30
8	 */
9	public class Hello {
10	
11	    public static void main(String[] args) {
12	        int i1 = 10;
13	        int i2 = 1;
14	        double d1 = 2.5;
15	        for (int i = 0; i < 5; i++) {
16	            i1 = i1 - 1;
17	            i2 = i2 * 2;
18	            if (i2 > i1) {
19	                d1 = i2 / d1;
20	            }
21	        }
22	    }
23	}

```

编译Hello类

```sh
$ javac Hello.java
$ javac -encoding utf-8 Hello.java
```



```sh
$ javap Hello.class
Compiled from "Hello.java"
public class Hello {
  public Hello();     
  public static void main(java.lang.String[]);
}
```



```
$ javap -c -v Hello.class
Classfile /E:/github/JAVA-000/Week_01/Hello.class
  Last modified 2020-10-19; size 409 bytes
  MD5 checksum dd4883503866540794f29fca9c6c6eb8
  Compiled from "Hello.java"
public class Hello
  minor version: 0							 // JVM版本号 52.0
  major version: 52			
  flags: ACC_PUBLIC, ACC_SUPER				 // 类的标示位
Constant pool:								 // 常量池
   #1 = Methodref          #5.#16         	 // java/lang/Object."<init>":()V
   #2 = Double             2.5d
   #4 = Class              #17               // Hello
   #5 = Class              #18               // java/lang/Object
   #6 = Utf8               <init>
   #7 = Utf8               ()V
   #8 = Utf8               Code
   #9 = Utf8               LineNumberTable
  #10 = Utf8               main
  #11 = Utf8               ([Ljava/lang/String;)V
  #12 = Utf8               StackMapTable
  #13 = Class              #19            	// "[Ljava/lang/String;"
  #14 = Utf8               SourceFile
  #15 = Utf8               Hello.java
  #16 = NameAndType        #6:#7         	// "<init>":()V
  #17 = Utf8               Hello
  #18 = Utf8               java/lang/Object
  #19 = Utf8               [Ljava/lang/String;
{
  public Hello();							  // 初始化无参构造器
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1		//无参构造的参数个是非0原因是在java中，对于非静态方法，this江北分配到局部变量表的0号槽位中
         0: aload_0							  //将第一个引用的本地变量推送至栈顶
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V   //调用
         4: return
      LineNumberTable:						  // 行号的表格（源代码行号与字节码行号映射关系）
        line 9: 0

  public static void main(java.lang.String[]);		
    descriptor: ([Ljava/lang/String;)V				// 方法描述符（行参类型[String=字符串数组、方法返回类型V[void]）
    flags: ACC_PUBLIC, ACC_STATIC					// 方法访问类型（ACC_PUBLIC=公共方法，ACC_STATIC=静态方法）
    Code:	
      stack=4, locals=6, args_size=1				// 操作栈的最大深度为4，本地变量表最大槽数为6,行参数为1
         0: bipush        10				//int常量数字10，入操作数栈，常量数大于1-5用b[类型]push+操作数，入操作数栈
         2: istore_1						//int常量数字10，出操作数栈，入本地变量表槽位1，完成：代表int i1 = 10
         3: iconst_1						//int常量数字1，将int型1推送至栈顶，放入操作数栈
         4: istore_2						//int常量数字1，出操作数栈，入本地变量表槽位2  完成：代表int i2 = 1
         5: ldc2_w        #2                // 将long或double型常量值从常量池中推送至栈顶（宽索引）double 2.5 引用常量池中常量编号为2的常量
         8: dstore_3                        //将double2.5，出操作数栈，入本地变量表槽位3 完成：double d1 = 2.5;
         9: iconst_0						//int常量数字0 入操作数栈 
        10: istore        5					//int常量数字5 入本地变量表槽位5 for循环的int i=0
        12: iload         5					//从本地变量表中读取槽位为5的整形加载操作数栈中
        14: iconst_5						//int常量数字5
        15: if_icmpge     42				//比较栈顶两int型数值大小，当结果超出范围跳转42  ge>=
        18: iload_1							//从本地变量表中读取槽位为1的整形加载操作数栈中 i1
        19: iconst_1						//定义一个常量1 入操作数栈
        20: isub							//做减法 将栈顶两int型数值相减并将结果压入栈顶  i1 - 1
        21: istore_1						//出操作数栈，入本地变量表为1的位置  i1 = i1 - 1;
        22: iload_2							//从本地变量表中读取槽位为2的整形加载操作数栈中 i2
        23: iconst_2						//定义一个常量2 入操作数栈
        24: imul							//相乘i2 * 2
        25: istore_2						//出操作数栈，入本地变量表为1的位置  i2 = i2 * 2;
        26: iload_2							//从本地变量表中读取槽位为2的整形加载操作数栈中 i2
        27: iload_1							//从本地变量表中读取槽位为1的整形加载操作数栈中 i1
        28: if_icmple     36				// if (i2 > i1) 否则跳36
        31: iload_2							//从本地变量表中读取槽位为2的数加载操作数栈中 i2
        32: i2d								//把int形转换成double -》  (double)i2
        33: dload_3							//从本地变量表中读取槽位为2的double加载操作数栈中 d1
        34: ddiv							//相除 i2 / d1;
        35: dstore_3						//出操作数栈，入本地变量表槽位3  d1 = i2 / d1
        36: iinc          5, 1				//将指定int型变量增加指定值（i++, i–-, i+=2等）本地变量表槽位5数值加1，代表for(i++)
        39: goto          12				//跳入第12行指令，即：（int i = 槽位5的数值）
        42: return
      LineNumberTable:									// 12=源代码第12行，0代表字节码中Code中的第一行 bipush        10
        line 12: 0
        line 13: 3
        line 14: 5
        line 15: 9
        line 16: 18
        line 17: 22
        line 18: 26
        line 19: 31
        line 15: 36
        line 23: 42
      StackMapTable: number_of_entries = 3              //在Jvm中的ClassLoad的验证阶段使用，被类型检测器使用
        frame_type = 255 /* full_frame */
          offset_delta = 12
          locals = [ class "[Ljava/lang/String;", int, int, double, int ]
          stack = []
        frame_type = 23 /* same */
        frame_type = 250 /* chop */
          offset_delta = 5
}
SourceFile: "Hello.java"


```





<type>const_<val>、bipush、sipush、ldc这些指令都用于向操作数栈压入常量。例如：

```java
1    // iconst_1
true // iconst_1    // JVM的类型系统里，整型比int窄的类型都统一带符号扩展到int来表示
127  // bipush 127  // 能用一个字节表示的带符号整数常量 (-128~127)
1234 // sipush 1234 // 能用两个字节表示的带符号整数常量 (-32768~32767)
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

new 指令只是创建对象，但没有调用构造函数。

dup 指令用于复制栈顶的值。

invokespecial 指令用来调用某些特殊方法的, 当然这里调用的是构造函数。

我们都知道 new 是Java编程语言中的一个关键字， 但其实在字节码中，也有一个指令叫做 new 。 当我们创建类的实例时, 编译器会生成类似下面这样的操作码：
当你同时看到 new, dup 和 invokespecial 指令在一起时，那么一定是在创建类的实例对象！

由于构造函数调用不会返回值，所以如果没有dup指令, 在对象上调用方法并初始化之后，操作数栈就会是空的，在初始化之后就会出问题, 接下来的代码就无法
对其进行处理。  



---

、

1. （选做）、自己写一个简单的 Hello.java，里面需要涉及基本类型，四则运行，if 和 for，然后自己分析一下对应的字节码，有问题群里讨论。

1. （必做）、自定义一个 Classloader，加载一个 Hello.xlass 文件，执行 hello 方法，此文件内容是一个 Hello.class 文件所有字节（x=255-x）处理后的文件。文件群里提供。

1. （必做）、画一张图，展示 Xmx、Xms、Xmn、Meta、DirectMemory、Xss 这些内存参数的关系。

1. （选做）、检查一下自己维护的业务系统的 JVM 参数配置，用 jstat 和 jstack、jmap 查看一下详情，并且自己独立分析一下大概情况，思考有没有不合理的地方，如何改进。

#### 
