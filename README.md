# leetcode-tester
算法题本地测试框架，适用 Java 语言，适用大部分 leetcode 题目，运行版本 Java8。
也可以用于其他算法题，只需保证测试用例的输入格式与 leetcode 保持一致。

## 开始使用
### 导入
#### 使用构建工具，Maven / Gradle 等

[maven repository]
```xml
<dependency>
	<groupId>org.lengyu</groupId>
	<artifactId>leetcode-tester</artifactId>
	<version>1.0</version>
</dependency>
```

#### 不使用构建工具
下载 [release jar包](https://github.com/aa3615058/leetcode-tester/releases)，已包含全部依赖。或者自行下载源码使用。

### Hello-world

MAINCLASS.java
```java
import org.lengyu.algorithm.ProblemTester;
public class MAINCLASS {
    public static void main(String[] args) {
        // 类名和测试用例文件名，{}是通配符
        ProblemTester tester = new ProblemTester("Solution{}",
                "data\\Solution{}.txt");
        // 打开plus模式，影响输出，会增加方法名和运行时间
        tester.openPlusMode();
        // 测试题目 9999，类名Solution9999，测试用例文件data/Solution9999.txt
        tester.test("9999");
    }
}
```
Solution9999.java
```java
public class Solution9999 {
    //假设这是一道题目的题解
    public String hello(String name) {return "Hello " + name + "!";}
}
```
data\Solution9999.txt
```
"World"
"Jack"
"A"
```
运行输出

```shell
hello world!
hello Jack!
hello A!
```

### 样例工程
[aa3615058/leetcode-solution](https://github.com/aa3615058/leetcode-solution)

## 使用方法
### 题解类的写法
接受 ProblemTester 测试的题解类，需要至少有一个 public 方法。
如果是要求写一个类的题，则要把这个类写成 public 内部类，例如 [剑指 Offer 09. 用两个栈实现队列](https://leetcode.cn/problems/yong-liang-ge-zhan-shi-xian-dui-lie-lcof/)：

```java
public class Solution9 {
    public class CQueue {
        public CQueue() {
            
        }
        public void appendTail(int value) {
            
        }
        public int deleteHead() {
            
        }
    }
}
```

框架支持一次测试一道题的多个方法，但必须要用@org.lengyu.algorithm.Answer 注解要运行的方法，例如：

```java
import org.lengyu.algorithm.Answer;
public class SolutionSort {
    public int[] bubbleSort(int[] a) {
        //do sort
        return a;
    }    
    public int[] choiceSort(int[] a) {
        //do sort
        return a;
    }    
    @Answer
    public int[] insertSort(int[] a) {
        //do sort
        return a;
    }    
    @Answer
    public int[] mergeSort(int[] a) {
        //do sort
        return a;
    }    
    @Answer
    public int[] quickSort(int[] a) {
        //do sort
        return a;
    }
}
```
多个内部类同样需要用 @Answer 来注解要运行的类。

### 测试用例格式
测试用例格式与 leetcode 完全相同。注意测试用例文件的末尾至多包含一个空行。TreeNode 的输入格式为 带有必要Null的层序遍历，详见 https://support.leetcode-cn.com/hc/kb/article/1567641/

例如：
SolutionTest.java
```java
public void test(int i, double d, char c, String s, int[] arr, int[][] arr2, String[] words, ListNode head, TreeNode root) {
    return;
}
```

SolutionTest.txt
```
67
24.5
"c"
"hello world!"
[1,3,6,8]
[[1,2,5,6,7],[89,77,64,3,3]]
["blue","Maven","cover"]
[1,5,6,7,8,11,11,10,7,16]
[1,2,3,null,null,4,5]
16
12.333
"!"
"zzzzzz"
[1,12,3,123,12,3]
[[1,3],[2,2]]
["X", "word", "windows"]
[3,3,10,99,99,99]
[1,2,3]
```

以下是特殊测试用例：

| 类型      | 值         | 测试用例写法 |
| --------- | ---------- | ------------ |
| 任何      | null       | null         |
| int[]    | int [0]    | []           |
| List\<?\>   | 空 List    | []           |
| ListNode  | 空链表     | [] 或 null   |
| TreeNode  | 空树       | [] 或 null   |
| int\[]\[] | int \[0][0] | []           |
| int\[]\[] | int \[3][0] | [[],[],[]]   |
| String    | ""         | ""           |

### ProblemTester

ProblemTester 默认关闭 plusMode，输出是测试方法的返回值。

ProblemTester 内部使用 SolutionFactory 类来处理类名和测试用例文件名，修改 类名，测试用例文件名，占位符都要先获取 SolutionFactory 类对象。

ProblemTester 内部设定有超时机制，默认为 8 秒，可以通过 get/set timeout 方法修改。

### 不适用的题目

本框架目前无法处理特殊输入，特殊测试，特殊输出这三种题目。

特殊输入，方法参数类型不通用，或测试用例处理方法不通用，例如：

```
剑指Offer52. 两个链表的第一个节点
剑指Offer54. 复杂链表复制
剑指Offer68-I. 二叉搜索树的最近公共祖先
剑指Offer68-II. 二叉树的最近公共祖先
```

特殊测试，测试用例格式不通用，测试过程不通用，例如：

```
剑指Offer37. 序列化二叉树
```

特殊输出，输出参数类型没有 toString() 方法，例如：

```
剑指Offer36. 二叉搜索树转化为双向链表
```


### 支持的方法参数类型

8种基本类型及其包装类，String，ListNode，TreeNode

以上类型组成的 List，Map，支持嵌套类型；

以上类型（不含8种包装类）一维数组；

int,char,double,long,String 二维数组