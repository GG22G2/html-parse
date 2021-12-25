package hsb.html;


import hsb.html.dom.Node;

import java.nio.charset.StandardCharsets;


public class HtmlNodeParse {
    //可以是单标签，也可以是双标签的元素,这个主要放这里便与查阅
    static final String[] NoMustSingleTag = new String[]{"colgroup"};
    //单标签元素 , 还有可省略闭合标签的元素
    static final String[] emptyTagNameStr = new String[]{"br", "img", "wbr", "col", "img", "area", "meta", "link", "base", "embed", "input", "keygen", "colgroup"};

    static final int[][] tagNameLengthIndex = new int[10][2];

    //已经过时的元素，这些元素直接丢弃就行
    static final String[] removeTag = new String[]{"frame"};

    static final byte[][] emptyTagName = new byte[emptyTagNameStr.length][];
    //用于字母大写转小写的表
    public static byte[] alphaTable = new byte[256];

    static {
        for (int i = 0; i < emptyTagNameStr.length; i++) {
            byte[] nameBytes = emptyTagNameStr[i].getBytes(StandardCharsets.UTF_8);
            emptyTagName[i] = nameBytes;
            tagNameLengthIndex[nameBytes.length][1] = i;
            tagNameLengthIndex[nameBytes.length + 1][0] = i + 1;
        }

        for (int i = 0; i < 256; i++) {
            alphaTable[i] = (byte) i;
        }

        for (int i = 65, j = 97; i <= 90; i++, j++) {
            alphaTable[i] = (byte) j;
        }

    }

    /**
     * 在我的6核12线程 i7-10750H上测试
     * 测试整个结构提取加parse ， 按照单线程为基准1   3线程为1.96    6线程2.1   12线程1.95
     * 如果把结构提取和解析两个过程拆开，
     * parse部分，按照单线程为基准1  2线程为1.79   3线程为2.35  6线程为2.79   12线程2.53
     * 通过vtune查看，好像3线程时，就基本把内存带宽跑满了  肯能是同时使用两个数组导致读取加倍了。
     *
     *
     * jsoup   1线程1   3线程2.4   6线程3.7
     * 1线程时，比jsoup快8倍  6线程比jsoup快4倍
     *
     * */
    public static Node parse(byte[] htmlBytes, int[] constructIndex) {

        Node root = new Node();
        root.name = "#root".getBytes(StandardCharsets.UTF_8);
        //默认最大栈不会大于256 , 大于的话再说吧
        Node[] stack = new Node[256];
        stack[0] = root;
        int stackTop = 0;

        //Node topNode = root;
        int[] constructPositions = new int[1];
        int start = 0;
        int constructPosition = 1;
        int textStart;
        boolean findTextEnd = false;

        int cLength = constructIndex[0];
        while (constructPosition < cLength) {
            int index = constructIndex[constructPosition++];
            byte c = htmlBytes[index];

            textStart = start;
            if (c == '<') {
                byte next = htmlBytes[index + 1];
                if (next == '!') {
                    //代表文档开始 或者frame开始
                    //或者是注解 <!--
                    byte one = htmlBytes[index + 2];
                    byte two = htmlBytes[index + 3];
                    if (one == two && one == '-') {
                        constructPosition = skipAnnotation(htmlBytes, constructIndex, constructPosition, cLength);
                    } else {
                        //获取<!DOCTYPE html>标签，是否有可能是内嵌文档，文档里嵌文档? 暂时先按照丢弃处理
                        constructPositions[0] = constructPosition;
                        Node node = readTag(htmlBytes, index + 2, constructIndex, constructPositions);
                        constructPosition = constructPositions[0];
                        findTextEnd = true;
                        start = constructIndex[constructPosition - 1] + 1;
                    }
                } else if (next == '/') {
                    /*
                      代表闭合标签开始
                      <div> </ div >
                      如果</ 后没有紧跟字母，那么  </ 和 >之间内容被当作注释处理
                      但现在不做考虑，认为不存在这么离谱的写法，遇到</就按照闭合标签处理
                      chrome会认为 </br />   </br/>    这两种写法等价于<br>
                      */
                    int tagNameEndIndex = constructIndex[constructPosition++];
                    if (htmlBytes[tagNameEndIndex] == '>') {
                        start = tagNameEndIndex + 1;
                        //这个if分支是为了处理  </br/> 这样的结构
                        if (htmlBytes[tagNameEndIndex - 1] == '/') {
                            tagNameEndIndex--;
                        }
                        tagNameEndIndex--;
                    } else {
                        start = constructIndex[constructPosition++] + 1;
                    }
                    int t = index + 2;
                    //String tagName = new String(htmlBytes, t, tagNameEndIndex - t + 1).toLowerCase();

                    //遇到闭合标签，因次应该更新dom结构树
                    findTextEnd = true;

                    int len = tagNameEndIndex - t + 1;

                    byte[] lowTagName = new byte[len];
                    UTF8ByteToLowerCase(htmlBytes, t, lowTagName, 0, len);
                    //stackTop = closeTag(stack, stackTop, htmlBytes, t, len);
                    stackTop = closeTag(stack, stackTop, lowTagName);
                } else if (next == '?') {
                    //chrome把 <?当作注释处理  <?  和  >  中间的内容都是注释
                    constructPosition = skipAnnotation2(htmlBytes, constructIndex, constructPosition, cLength);
                } else if ((next >= 'A' && next <= 'Z') || (next >= 'a' && next <= 'z')) {
                    constructPositions[0] = constructPosition;
                    //跳转到标签开始
                    Node node = readTag(htmlBytes, index + 1, constructIndex, constructPositions);
                    constructPosition = constructPositions[0];
                    findTextEnd = true;
                    //更新start位置
                    start = constructIndex[constructPosition - 1] + 1;
                    stackTop = addNode(stack, stackTop, node);

                    //todo 对于像是script之类的标签，其中的内容应该单独处理，如果脚本中遇到 <div 或 ”<div“ 的结构 ，就会导致解析出错
                }

                if (findTextEnd) {
                    //判断开始标签和 start之间是否有字符
                    if (index > textStart) { //当作文本标签处理
                        //  String text = new String(htmlBytes, start, index - start);
                        StringIndex stringIndex = new StringIndex(start, index - textStart);
                        Node textNode = new Node();
                        textNode.name = "text".getBytes(StandardCharsets.UTF_8);
                        textNode.text = stringIndex;
                        //todo 节点之间的内容   两个思路 ， 一种是当做文本节点处理
                        // 另一种是给node节点加上节点开始和节点结束索引， 后边实际需要获取文本时，根据前后节点的信息，推断文本快位置
                    }
                    findTextEnd = false;
                }
            } else {//找到下一个开始标签或闭合标签 < ，然后把中间的数据当作字符处理
                int strEnd;
                while (constructPosition < cLength) {
                    strEnd = constructIndex[constructPosition++];
                    if (htmlBytes[strEnd] == '<') {
                        break;
                    }
                }
                constructPosition--;

            }

        }
        return root;
    }


    public static int addNode(Node[] stack, int stackTop, Node node) {
        //判断是不是空标签,如果是空标签当作闭合处理
        boolean isDiv = byteArrayIsEqual(node.name, "div".getBytes(StandardCharsets.UTF_8));
        if (isDiv || (!node.selfClose && !isEmptyTagName(node.name))) {
            stack[++stackTop] = node;
        } else {
            Node topNode = stack[stackTop];
            topNode.appendChild(node);
        }
        return stackTop;
    }

    /**
     * 标签闭合， 并且考虑标签不配对情况，
     * <p>
     * <p>
     * todo  name是标签名称， 因为一般标签的字节长度是 1 -  5 ，完全可以用一个long类型标签，这样比较的时候也能减少比较次数
     * todo  对是byte[] 映射到long类型的算法  目前想到的是字母只有低7位有效，通过或运算，左移7位，一个long类型可以包含9个字节的信息。一般标签都不会出现重复值
     */
    public static int closeTag(Node[] stack, int stackTop, byte[] name) {
        //因为div标签出现的概率很高，如果等于div就不用判断是不是空标签了
        boolean isDiv = byteArrayIsEqual(name, "div".getBytes(StandardCharsets.UTF_8));
        if (!isDiv && isEmptyTagName(name)) {  //空标签的闭合标签直接丢弃处理
            return stackTop;
        }

        Node node = stack[stackTop];
        boolean nameEq = byteArrayIsEqual(name, node.name);
        if (nameEq) {
            Node parent = stack[--stackTop];
            parent.appendChild(node);
            node.close = true;
        } else {
            System.out.println("标签不配对");
            /*
              对于可以省略闭合标签的情况，这里可以通过移动元素来解决
              把当前Node节点放入stack[--stackTop]中， 把Node中元素移入stack[--stackTop]中。
              但是移动时，并不一定是全移动，以 table  colgroup  col tb

              比如 table中包含colgroup ， 可能缺少闭合的colgroup标签，但chrome会在col结尾 插入 </colgroup>

              现在程序执行到这里可能是： colgroup中包含col 和tb , 那么应该把tb放入table中， col保留在colgroup中
              但这些都要根据具体的标签名称来判断，需要大量代码，
              */
            //todo 现在把所有可省略闭合标签等同于一定省略闭合标签来处理了，可能会导致局部的dom结构和浏览器解析出来的结构不一致
            Node parent = stack[--stackTop];
            parent.appendChild(node);

            stackTop = closeTag(stack, stackTop, name);
        }
        return stackTop;
    }


    public static boolean isEmptyTagName(byte[] name) {
        int[] index = HtmlNodeParse.tagNameLengthIndex[name.length];
        int tagStart = index[0];
        int tagEnd = index[1];
        //判断是不是空标签,如果是空标签当作闭合处理
        for (int i = tagStart; i <= tagEnd; i++) {
            boolean eq = byteArrayIsEqual(emptyTagName[i], name);
            if (eq) {
                return true;
            }
        }
        return false;
    }


    public static boolean isEmptyTagName(byte[] name, int start, int length) {
        //emptyTagName中字节数组是有序的，可以根据长度跳过一部分
        int[] index = HtmlNodeParse.tagNameLengthIndex[length];
        int tagStart = index[0];
        int tagEnd = index[1];

        //判断是不是空标签,如果是空标签当作闭合处理
        for (int i = tagStart; i <= tagEnd; i++) {
            boolean eq = byteArrayIsEqual(name, start, length, emptyTagName[i]);
            if (eq) {
                return true;
            }
        }
        return false;
    }

    /**
     * 参考解析 ：
     * 例子1  <div id =/"fe\\\""<a// abc=1 'cd='1 a' = afe > &nbsp;1 < 3  </div>
     * chrome解析结果：div有五个属性和值，分别是:
     * 0: id    /"fe\\\""<a//
     * 1: abc   1
     * 2: 'cd   '1 a'
     * 3: =
     * 4: afe
     * <p>
     *
     * <p>
     * <p>
     * 例子2  <div id =‘fea bv’ sd = "1" > &nbsp;1 < 3  </div>
     * <p>
     * 0: id    fea
     * 1: bv’
     * 2: sd    "1"
     * <p>
     * 例子3  <div  'cd='' > 4  </div>
     * chrome解析结果:
     * 'cd     ''
     * <p>
     */
    // start标签开始 第一个字母所在位置
    public static Node readTag(byte[] htmlBytes, int start, int[] constructIndex, int[] constructPositions) {
        Node node = new Node();
        int constructPosition = constructPositions[0];
        int index = constructIndex[constructPosition++];

        //当作结构字符处理的双引号和单引号数量,主要用于文本标签  比如 script  area等标签内配对引号
        int doubleQuoteCount = 0;
        int singleQuoteCount = 0;

        byte[] id = null;
        byte[] allClass = null;
        byte c = htmlBytes[index];
        boolean findEnd = false;
        int slashIsStr = 0;  //0代表不是字符   1代表是字符

        byte[] nameBytes;

        if (c == '>') {
            findEnd = true;
            if ('/' == htmlBytes[index - 1]) {
                index--;
            }
            nameBytes = new byte[index - start];
            UTF8ByteToLowerCase(htmlBytes, start, nameBytes, 0, nameBytes.length);
            //start指向>结束后第一个位置
            start = index + 3;
        } else {
            nameBytes = new byte[index - start + 1];
            UTF8ByteToLowerCase(htmlBytes, start, nameBytes, 0, nameBytes.length);
        }

        node.name = nameBytes;

        //读取等号 判断等号左侧是否的id  或者class ，否则的话先不解析直接跳过

        while (!findEnd) {
            index = constructIndex[constructPosition++];
            c = htmlBytes[index];

            if (c != '>') {
                //index1 字符串左侧   index2 字符串右侧
                int index1 = index;
                int index2 = constructIndex[constructPosition++];
                boolean needValue = false;
                //判断 index2指向字母还是=
                if (htmlBytes[index2] == '=') {
                    index = index2;
                    index2 = index2 - 1;
                } else if (htmlBytes[index2] == '>') {
                    //这个属性只有名称 没有值  可以丢弃
                    index = index2;  //index 指向 >
                    findEnd = true;
                    break;
                } else {
                    index = constructIndex[constructPosition++];
                    if (htmlBytes[index] != '=') {
                        //这个属性没有等号 =  ，所以index指向的是下一个属性的开始，或者闭合标签
                        constructPosition--;
                        continue;
                    }
                }
                int KeyLength = index2 - index1 + 1;

                //如果是 id 或者 class属性
                if (id == null && KeyLength == 2) { //长度是2  非常可能是id
                    byte c1 = htmlBytes[index1];
                    byte c2 = htmlBytes[index2];
                    int idh = ((c1 | 0x20) << 8) | (c2 | 0x20);
                    // 0x4944L=是 小写id 的16进制ascii拼接
                    if (idh == 0x6964) {
                        needValue = true;
                    }

                    //System.arraycopy();
                } else if (allClass == null && KeyLength == 5) { //验证是不是class
                    byte c1 = htmlBytes[index1];
                    byte c2 = htmlBytes[index1 + 1];
                    byte c3 = htmlBytes[index1 + 2];
                    byte c4 = htmlBytes[index1 + 3];
                    byte c5 = htmlBytes[index2];
                    long classH = ((c1 | 0x20L) << 32) | ((c2 | 0x20L) << 24) | ((c3 | 0x20L) << 16) | ((c4 | 0x20L) << 8) | (c5 | 0x20L);
                    // 0x636c617373L是 小写class 的16进制ascii拼接
                    if (classH == 0x636c617373L) {
                        needValue = true;
                    }
                }


                //index指向=位置  ，  constructIndex[constructPosition] 现在指向=后第一个特征位置  可能是引号 可能是> 可能是字母
                //判断等号右侧是不是 " ' 或者紧跟数字
                byte eqNext = htmlBytes[index + 1];
                int attrValueStart;
                int attrValueEnd;
                if (isWhiteSpace(eqNext)) {
                    //等号后下一个位置是空格，那么constructIndex中下一个位置一定是属性值开始位置的索引 或者 >
                    attrValueStart = constructIndex[constructPosition];
                } else {
                    //等号后下一个位置一定是属性值开始位置 或者 >
                    attrValueStart = index + 1;
                }
                eqNext = htmlBytes[attrValueStart];
                if (eqNext == '\"') { //只要是引号开始，就必须有对应的引号才算结束
                    //以 双引号开始的字符串
                    constructPosition = consumeDoubleQuoteStr(htmlBytes, constructIndex, constructPosition + 1);
                    attrValueEnd = constructIndex[constructPosition++];
                } else if (eqNext == '\'') {
                    //单引号开始的字符串
                    constructPosition = consumeSingleQuoteStr(htmlBytes, constructIndex, constructPosition + 1);
                    attrValueEnd = constructIndex[constructPosition++];
                } else if (eqNext == '>') {
                    //标签结束，等号后没有值，所以这个属性也可以丢弃了
                    index = attrValueStart;
                    findEnd = true;
                    continue;
                } else {
                    //字符串没有引号，找下一个空格，代表字符串结束
                    int[] rs = consumeUnQuoteStr(htmlBytes, constructIndex, constructPosition);
                    slashIsStr = rs[0];
                    attrValueEnd = rs[1];
                    constructPosition = rs[2];
                }

                //  String value = new String(htmlBytes, attrValueStart, attrValueEnd - attrValueStart + 1);
                if (needValue) {
                    int valueLength = attrValueEnd - attrValueStart + 1;
                    byte[] v = new byte[valueLength];
                    System.arraycopy(htmlBytes, attrValueStart, v, 0, valueLength);

                    if (KeyLength == 2) {
                        node.id = v;
                    } else {
                        node.allClass = v;
                    }
                }


            } else {

                break;
            }

        }

        //标签结束  如果有 / 则按自闭和处理
        if (slashIsStr == 0 && htmlBytes[index - 1] == '/') {
            //开始标签 自闭合
            node.selfClose = true;
        }
        constructPositions[0] = constructPosition;
        return node;
    }


    public static boolean isAlpha(byte next) {
        return (next >= 'A' && next <= 'Z') || (next >= 'a' && next <= 'z');
    }

    public static boolean isWhiteSpace(byte what) {
        return what == '\0' || what == '\t' || what == '\n' || what == '\f' || what == '\r' || what == ' ';
    }

    public static boolean byteArrayIsEqual(byte[] b1, byte[] b2) {
        if (b1.length != b2.length) {
            return false;
        }
        for (int i = 0; i < b1.length; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean byteArrayIsEqual(byte[] b1, int start, int length, byte[] b2) {
        if (length != b2.length) {
            return false;
        }
        for (int i = start, j = 0; j < length; i++, j++) {
            if (b1[i] != b2[j]) {
                return false;
            }
        }
        return true;
    }

    public static int consumeDoubleQuoteStr(byte[] htmlBytes, int[] constructIndex, int constructPosition) {
        if (htmlBytes[constructIndex[constructPosition++]] == '\"') {
            return constructPosition - 1;
        }
        while (htmlBytes[constructIndex[constructPosition++]] != '\"') {
        }
        return constructPosition - 1;
    }

    /**
     * 一些极端例子，没有对应的闭合引号，会一直遍历到文档结束，constructPosition有可能超出constructIndex长度。
     */
    public static int consumeSingleQuoteStr(byte[] htmlBytes, int[] constructIndex, int constructPosition) {
        if (htmlBytes[constructIndex[constructPosition++]] == '\'') {
            return constructPosition - 1;
        }
        while (htmlBytes[constructIndex[constructPosition++]] != '\'') ;
        return constructPosition - 1;
    }

    //没有用引号包裹的字符串 ，要么是遇到空格代表结束 ，要么是>
    // 返回值低32位代表位置索引，constructIndex[constructPosition]是结束位置  高32位代表 /需不需要当作自闭和处理
    public static int[] consumeUnQuoteStr(byte[] htmlBytes, int[] constructIndex, int constructPosition) {
        int[] result = new int[3];
        do {
            int index = constructIndex[constructPosition++];
            byte c = htmlBytes[index];
            if (c != '>') {
                if (isWhiteSpace(htmlBytes[index + 1])) {
                    result[1] = index;
                    result[2] = constructPosition;
                    return result;
                }
            } else {
                //  long slashIsStr = htmlBytes[index - 1] == '/' ? 0x100000000L : 0;
                result[0] = htmlBytes[index - 1] == '/' ? 1 : 0;
                result[1] = index - 1;
                result[2] = constructPosition - 1;
                return result;
            }

        } while (true);

    }

    //跳过注释  <!--    -->
    public static int skipAnnotation(byte[] htmlBytes, int[] constructIndex, int constructPosition, final int clength) {
        while (constructPosition < clength) {
            int index = constructIndex[constructPosition++];
            if (htmlBytes[index] == '>') {
                byte one = htmlBytes[index - 2];
                byte two = htmlBytes[index - 1];
                if (one == two && one == '-') {
                    return constructPosition;
                }
            }
        }
        return constructPosition;
    }


    //跳过注释  <?    >
    public static int skipAnnotation2(byte[] htmlBytes, int[] constructIndex, int constructPosition, final int clength) {
        while (constructPosition < clength) {
            int index = constructIndex[constructPosition++];
            if (htmlBytes[index] == '>') {
                return constructPosition;
            }
        }
        return constructPosition;
    }

    //utf8字符转小写, 使用查表方法,并且将小写后内容左hash， 也即是左移和或运算， 生成long返回
    public static long UTF8ByteCharToLowerCaseHash(byte[] name, int start, int length) {
        byte[] table = alphaTable;
        length = Math.min(9, length);
        long hash = table[name[start]];
        int end = start + length;
        for (int i = start + 1; i < end; i++) {
            hash = ((hash << 7) | table[name[i]]);
        }
        return hash;
    }


    //  todo 现在调用改方法的都是为了把开始标签的名称 或者结束标签的名称转小写，
    //  还有个思路是调用UTF8ByteCharToLowerCaseHash方法，用一个long类型代表标签名称，这样后边比较操作就会快很多
    //utf8字符转小写, 使用查表方法
    public static void UTF8ByteToLowerCase(byte[] src, int srcPos, byte[] dst, int dstPos, int length) {
        byte[] table = alphaTable;
        int loopLength = dstPos + length;
        for (int i = srcPos, j = dstPos; j < loopLength; i++, j++) {
            dst[j] = table[src[i]];
        }
    }

    public static long tagNameByteToLong(byte[] name) {
        return tagNameByteToLong(name, 0, name.length);
    }


    /**
     * name是标签名称， 因为一般标签名称的字节长度是 1 -  5 ，完全可以用一个long类型标签，这样比较的时候也能减少比较次数
     * 现在主要是byte[] 映射到long类型的算法不太成熟，  目前想到的是字母只有低7位有效，通过或运算，左移7位，一个long类型可以包含9个字节的信息。一般不会出错
     */
    public static long tagNameByteToLong(byte[] name, int start, int length) {
        length = Math.min(length, 9);
        long hash = name[start];
        int end = start + length;
        for (int i = start + 1; i < end; i++) {
            hash = (hash << 7) | name[i];
        }
        return -1;
    }


}
