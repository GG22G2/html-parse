# html-parse
html解析

因为做爬虫，一直用Jsoup做HTML解析，学习其源码也有了很多收获。因为Jsoup的解析是非常完善的，
如果只是从html中提取一点内容，不会修改dom结构，那么很多jsoup中的逻辑都可以省略掉。所以想实验下，是否能够加快解析过程。  
我参考simdjson的解析思路，先构建结构索引，然后再解析。构建结构索引部分我使用java和C++分别实现，C++的实现参考了simdjson中的思路。目前测试结果，java的实现因为没法用到simd指令，基本达不到加速效果。C++的实现可以提供不错的加速效果

>目前只是测试是否能达到加速效果，目前通过结合xsoup和jsoup提供xpath语法的元素提取

---
##解析过程

#### 1.构建结构索引，对三种类型字符标记
1. 四种<标签： __<字母__  __<!__  __<?__  __</__
2. HTML中有特殊含义的字母    __=__   __>__  __"__  __'__
3. 认为(\t \n \r \f \o 空格)都是空格，然后标记空格两端字符。比如 a bcdefghijk   mn ，就会标记a b k f的位置  

#### 2.根据结构索引，遍历html，生成node节点
  只能处理utf8的字节数组，假设html保存在数组a,结构索引保存再数组b。那这个阶段就是遍历b数组，根据b中值去读取a，类似a[b[i]]。
这一部分参考了jsoup的方式，根据读取到的字符走不同的逻辑，比如读取到<,下一个字符只会是? / ! 字母。这个阶段的处理还有许多问题，
比如对script,area等文本标签，还没有处理。

#### 3.将node节点构建成dom树
dom树的构建很简单，就是把解析出的节点，按照深度，插入dom中对应位置。不会自己去修复一些节点，比如chrome再解析table部分时:
原始内容:
```aidl
<table>
    <col align="left" />
    <col align="left" />
    <col align="right" />
</table>
```
chrome解析出的结果:
```aidl
<table>
    <colgroup>
        <col align="left">
        <col align="left">
        <col align="right">
    </colgroup>
</table>
```
遇得没有闭合的标签，会当作闭合处理，chrome有时是这么做的，有的节点结构却不这么做。总之可能和chrome解析出来的有差异。

运行，jvm参数
```aidl
--add-modules jdk.incubator.foreign
--enable-native-access=ALL-UNNAMED
--add-opens java.base/jdk.internal.misc=ALL-UNNAMED
--add-opens jdk.incubator.foreign/jdk.internal.foreign=ALL-UNNAMED
-XX:-UseCompressedOops
```

