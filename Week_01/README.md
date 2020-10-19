# 学习笔记

Hello.java

``` java
public static void main(String[] args) {
    int i1 = 10;
    int i2 = 1;
    double d1 = 2.5;
    for (int i = 0; i < 5; i++) {
        i1 = i1 - 1;
        i2 = i2 * 2;
        if (i2 > i1) {
            d1 = i2 / d1;
        }
    }
}
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
      stack=1, locals=1, args_size=1
         0: aload_0							  //将第一个引用的本地变量推送至栈顶
         1: invokespecial #1                  // Method java/lang/Object."<init>":()V   //调用
         4: return
      LineNumberTable:						  // 行号的表格（源代码行号与字节码行号映射关系）
        line 9: 0

  public static void main(java.lang.String[]);		
    descriptor: ([Ljava/lang/String;)V				// 方法描述符（行参类型[String=字符串数组、方法返回类型V=空）
    flags: ACC_PUBLIC, ACC_STATIC					// 方法访问类型（ACC_PUBLIC=公共方法，ACC_STATIC=静态方法）
    Code:	
      stack=4, locals=6, args_size=1				// 栈的最大深度为4，本地变量表最大槽数为6,行参数为1
         0: bipush        10				// 常量数字10，入操作数栈，常量数大于1-5用b[类型]push+操作数，入操作数栈
         2: istore_1						//将栈顶int型数值存入第二个本地变量,入操作数栈
         3: iconst_1						//将int型1推送至栈顶
         4: istore_2
         5: ldc2_w        #2                  // double 2.5d
         8: dstore_3
         9: iconst_0
        10: istore        5
        12: iload         5
        14: iconst_5
        15: if_icmpge     42
        18: iload_1
        19: iconst_1
        20: isub
        21: istore_1
        22: iload_2
        23: iconst_2
        24: imul
        25: istore_2
        26: iload_2
        27: iload_1
        28: if_icmple     36
        31: iload_2
        32: i2d
        33: dload_3
        34: ddiv
        35: dstore_3
        36: iinc          5, 1
        39: goto          12
        42: return
      LineNumberTable:
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
      StackMapTable: number_of_entries = 3
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









---

、

1. （选做）、自己写一个简单的 Hello.java，里面需要涉及基本类型，四则运行，if 和 for，然后自己分析一下对应的字节码，有问题群里讨论。

1. （必做）、自定义一个 Classloader，加载一个 Hello.xlass 文件，执行 hello 方法，此文件内容是一个 Hello.class 文件所有字节（x=255-x）处理后的文件。文件群里提供。

1. （必做）、画一张图，展示 Xmx、Xms、Xmn、Meta、DirectMemory、Xss 这些内存参数的关系。

1. （选做）、检查一下自己维护的业务系统的 JVM 参数配置，用 jstat 和 jstack、jmap 查看一下详情，并且自己独立分析一下大概情况，思考有没有不合理的地方，如何改进。

 

# 作业一（选填）

#### 1、分析字节码，包含基本数据类型、四则运算、对象引用、流程控制

------

##### Java代码

```
package Week_01;

/**
 * Created by ipipman on 2020/10/17.
 *
 * @version V1.0
 * @Package Week_01
 * @Description: (用一句话描述该文件做什么)
 * @date 2020/10/17 12:27 下午
 */
public class Test {


    /**
     * 四则运算+流程控制
     *
     * @param args
     */
    public static void main(String[] args) {
        int num1 = 1;
        int num2 = 10;
        float num3;
        for (int i = 0; i < 3; i++) {
            num1 = num1 + 1;
            num2 = num2 - 1;
            num3 = num1 * num2;
            if (num3 > num2) {
                num3 = num3 / num2;
            }
        }
    }
}
```

------

##### 编译Java字节码

```
javac ./Week_01/Test.java
javap -c -v ./Week_01/Test.class 
```

------

##### Java字节码

```
Classfile /Users/ipipman/JAVA-000/Week_01/Test.class
  Last modified 2020-10-17; size 422 bytes
  MD5 checksum a49fe32db56789480c59ce59d9dc3643
  Compiled from "Test.java"
public class Week_01.Test
  minor version: 0                                
  major version: 52                              // JVM版本号
  flags: ACC_PUBLIC, ACC_SUPER                   // 类的标示位
Constant pool:                                   // 常量池
   #1 = Methodref          #3.#14                // java/lang/Object."<init>":()V
   #2 = Class              #15                   // Week_01/Test
   #3 = Class              #16                   // java/lang/Object
   #4 = Utf8               <init>
   #5 = Utf8               ()V
   #6 = Utf8               Code
   #7 = Utf8               LineNumberTable
   #8 = Utf8               main
   #9 = Utf8               ([Ljava/lang/String;)V
  #10 = Utf8               StackMapTable
  #11 = Class              #17                   // "[Ljava/lang/String;"
  #12 = Utf8               SourceFile
  #13 = Utf8               Test.java
  #14 = NameAndType        #4:#5                 // "<init>":()V
  #15 = Utf8               Week_01/Test
  #16 = Utf8               java/lang/Object
  #17 = Utf8               [Ljava/lang/String;
{
  public Week_01.Test();                         // 初始化构造器
    descriptor: ()V
    flags: ACC_PUBLIC
    Code:
      stack=1, locals=1, args_size=1
         0: aload_0
         1: invokespecial #1                    // Method java/lang/Object."<init>":()V
         4: return
      LineNumberTable:				            // 行号的表格（源代码行号与字节码行号映射关系）
        line 11: 0

  public static void main(java.lang.String[]);
  descriptor: ([Ljava/lang/String;)V           // 方法描述符（行参类型[String=字符串数组、方法返回类型V=空）
    flags: ACC_PUBLIC, ACC_STATIC              // 方法访问类型（ACC_PUBLIC=公共方法，ACC_STATIC=静态方法）
    Code:
      stack=2, locals=5, args_size=1           // stack=2代表栈的最大深度为2，locals=5代表本地变量表最大槽数为5，args_size=1代表行参数为1
         0: iconst_1                           // 常量数字1，入操作数栈
         1: istore_1                           // 常量数字1，出操作数栈，入本地变量表LocalVariableTable槽位1，代表完成：int num1 = 1
         2: bipush        10                   // 常量数字10，入操作数栈，常量数大于1-5用b[类型]push+操作数，入操作数栈
         4: istore_2                           // 常量数字10，出操作数栈，入本地变量表槽位2，完成：代表int num2 = 10
         5: iconst_0                           // 常量数字0，入操作数栈
         6: istore        4                    // 常量数字0，出操作数栈，入本地变量表槽位4，完成for循环中初始化的：代表int i = 0
         8: iload         4                    // 局部表量表槽位4（int i = 0）出栈，入操作数栈
        10: iconst_3                           // 常量数字3，入操作数栈
        11: if_icmpge     45                   // 比较数值，如果满足条件跳入45行指令return（跳出方法），代表完成： for 中的(i < 3) 时
        14: iload_1                            // 局部标量表槽位1（int num1=1）出栈，入操作数栈
        15: iconst_1                           // 常量数字1，入操作数栈
        16: iadd                               // 操作数栈相加计算，代表：（int num1 = 1）+ （常量数子1）
        17: istore_1                           // 将计算结果1+ 1 = 2从操作数栈，入本地变量表槽位1，代表完成：num1 = num1 + 1�
        18: iload_2                            // 局部变量表槽位2（int num2 = 10）出栈，入操作数栈
        19: iconst_1                           // 常量数字1，入操作数栈
        20: isub                               // 操作数栈相减计算，代表：（int num2 = 10）- (常量数字1) 
        21: istore_2                           // 将计算结果10 - 1 = 9从操作数栈，如本地变量表槽位2，代表完成：num2 = num2 - 1
        22: iload_1                            // 局部变量表槽位1出栈，入操作数栈，int num1 = 2
        23: iload_2                            // 局部变量表槽位2出栈，入操作数栈，int num2 = 9
        24: imul                               // 操作数栈相除计算，代表：（int num2 = 9） / (int num1 = 2)
        25: i2f                                // 操作数栈类型转换，i=int类型，2代表to，f代表float类型，值=4.5
        26: fstore_3                           // 操作数栈计算结果4.5出栈，入本地变量表槽位3，代表完成：int num3 = num2 / num1
        27: fload_3                            // 本地变量表槽位3出栈，入操作数栈
        28: iload_2                            // 本地变量表槽位2出栈，入操作数栈
        29: i2f                                // 操作数栈int类型转换float
        30: fcmpl                              // 操作数栈float类型比较
        31: ifle          39                   // 如果不满足if (num3 > num2)条件，跳如39行指令for(i++）
        34: fload_3                            //  本地变量表槽位3出栈，入操作数栈
        35: iload_2                            // 本地变量表槽位2出栈，入操作数栈
        36: i2f                                // 操作数栈int类型转换float
        37: fdiv                               // 操作数栈浮点数除法，代表num2 / num3
        38: fstore_3                           // 操作数栈计算结果num2 / num3出栈，入本地变量表槽位3
        39: iinc          4, 1                 // 本地变量表槽位4数值加1，代表for(i++)
        42: goto          8                    //  跳入第8行指令，即：（int i = 槽位4的数值）
        45: return                             // 方法返回
      LineNumberTable:                         // 行号的表格（源代码行号与字节码行号映射关系）
        line 20: 0                             // 20=源代码第20行，0代表字节码中Code中的第一行iconst_1
        line 21: 2
        line 23: 5
        line 24: 14
        line 25: 18
        line 26: 22
        line 27: 27
        line 28: 34
        line 23: 39
        line 31: 45
      StackMapTable: number_of_entries = 3     //在Jvm中的ClassLoad的验证阶段使用，被类型检测器使用
        frame_type = 255 /* full_frame */
          offset_delta = 8
          locals = [ class "[Ljava/lang/String;", int, int, top, int ]
          stack = []
        frame_type = 255 /* full_frame */
          offset_delta = 30
          locals = [ class "[Ljava/lang/String;", int, int, float, int ]
          stack = []
        frame_type = 249 /* chop */
          offset_delta = 5
}
SourceFile: "Test.java"
```





- 



