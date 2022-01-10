package hsb.html.dom;

import hsb.html.help.EvaluatorHelp;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Node {

    public byte[] rawHtml;

    public Node parent;
    public Node preSiblingNode;
    public Node nextSiblingNode;
    public Node firstChild = null;

    // public Node[] children = null;// new Node[4];

    //Node[] textChildren = new Node[5];

    //使用四个变量记录节点在原文中定位，方便提取字符串等操作. <div  id ='a' class='f' >
    public int openStartIndex;
    public int openEndIndex;
    // </div> < 和 >的位置，对于自闭和标签，这两个值都等于openEndIndex
    public int closeStartIndex;
    public int closeEndIndex;

    //结构索引位置,方便二次解析,如果需要解析属性，可以用这两个值从结构索引中快速
    public int copenStartIndex;
    public int copenEndIndex;


    //标签名称
    public byte[] name;

    public long nameHash = 0;

    //一般都通过id 或者 class找元素，所以把这两个属性的值单独保存
    public byte[] id;

    //原始的 class内存
    public byte[] allClass;

    //class可能包含很多个子块，分割后保存
    public byte[][] classList;

    //如果有链接属性的话，这里标记位置
    public int hrefStart = -1;
    public int hrefEnd = -1;

    //是否自闭合 只有在解析标签是遇到/> 这个值才可能是true ，之所以不是一定,是因为如<div id = fee/>，这个"fee/"都被当作id的值,chrome也是这么处理的
    //但实际上是否被当作自闭合，好像有严格限制，比如div就不允许自闭合，<div/>也会被认为是开始标签
    public boolean selfClose = false;

    //代表标签闭合，用在程序中标记一个node节点处理完成
    public boolean close = false;

    public int siblingIndex;

    //todo 除了id class href之外的属性如何处理
    //属性的名字，除了id class href，其他属性很少被用来做xpath选择器的元素。所以都放这里
    public long[] attributeK;
    public int attributeKIndex;
    //属性值中存位置索引比如 title="I 我是标题!" ，attributeV[0]=I位置,attributeV[1]=!位置
    public int[] attributeV;
    public int attributeVIndex;

    int cap = 4;
    public int size = 0;

    public Node() {

    }

//    public void appendChild(Node node) {
//        if (size >= cap) {
//            ChildExpandCapacity(cap << 1);
//        }
//        children[size++] = node;
//    }

    //只包含元素节点，非空节点或者文本节点都不包含
//    public Node children(int index) {
//        if (index < size) {
//            return children[index];
//        }
//        return null;
//    }


    /**
     * 全部文本，不包括子元素中的
     * <div>
     *     1
     *     <span>2</span>
     *     3
     *     <span>4</span>
     *     5
     * </div>
     * <p>
     * 返回 135
     */
    public String ownText() {
        if (rawHtml != null && closeStartIndex > openEndIndex) {
            int byteLength = 0;
            int le = (size + 1) * 2;
            int strIndex[] = new int[le];


            Node child = firstChild;
//            for (int i = 1; i < (le - 1); i += 2) {
//                strIndex[i] = child.openStartIndex;
//                strIndex[i + 1] = child.closeEndIndex;
//                child = child.nextSiblingNode;
//            }

            int nextStart = openEndIndex + 1;
            int len;
            for (int i = 0, j = 0; i < size; i++) {
                int start = nextStart;
                int end = child.openStartIndex - 1;
                nextStart = child.closeEndIndex + 1;
                len = end - start + 1;
                len = len > 0 ? len : 0;

                strIndex[j++] = start;
                strIndex[j++] = len;
                byteLength += len;
                child = child.nextSiblingNode;
            }
            len = closeStartIndex - nextStart;
            len = len > 0 ? len : 0;
            strIndex[le - 2] = nextStart;
            strIndex[le - 1] = len;
            byteLength += len;

            byte[] strBytes = new byte[byteLength];
            int position = 0;
            for (int i = 0; i < strIndex.length; i += 2) {
                int start = strIndex[i];
                len = strIndex[i + 1];
                System.arraycopy(rawHtml, start, strBytes, position, len);
                position += len;
            }
            //utf8编码
            return new String(strBytes);
        }
        return "";
    }

    /**
     * 同ownText()中注释例子:
     * <p>
     * 返回 ["1","3","5"];
     */
    public List<String> ownTextNodes() {
        if (rawHtml != null && closeStartIndex > openEndIndex) {
            //utf8编码
            ArrayList<String> strs = new ArrayList<String>(size + 1);
            Node child = firstChild;
            int nextStart = openEndIndex + 1;
            int len;
            for (int i = 0; i < size; i++) {
                int start = nextStart;
                int end = child.openStartIndex - 1;
                nextStart = child.closeEndIndex + 1;
                len = end - start + 1;
                len = len > 0 ? len : 0;
                if (len == 0) {
                    strs.add("");
                } else {
                    strs.add(new String(rawHtml, start, len));
                }
                child = child.nextSiblingNode;
            }
            len = closeStartIndex - nextStart + 1;
            len = len > 0 ? len : 0;
            if (len == 0) {
                strs.add("");
            } else {
                strs.add(new String(rawHtml, nextStart, len));
            }
            return strs;
        }
        return new ArrayList<>(0);
    }


    /**
     * <div>
     *     1
     *     <span>2</span>
     *     3
     *     <span>4</span>
     *     5
     * </div>
     * <p>
     * 返回 12345
     */
    public String text() {
        //todo 待做
        return "";
    }

    /**
     * 同ownText()中注释例子:
     * <p>
     * 返回 ["1","2","3","4","5"];
     */
    public String textNodes() {
        //todo 待做
        return "";
    }

    public String html() {
        if (rawHtml != null) {
            //utf8编码
            return new String(rawHtml, openStartIndex, closeEndIndex - openStartIndex + 1);
        }
        return "";
    }

//    public void ChildExpandCapacity(int newCap) {
//        Node[] n = new Node[newCap];
//        System.arraycopy(children, 0, n, 0, children.length);
//        children = n;
//        cap = newCap;
//    }


    public String attr(String key) {
        if (key.equals("href")) {
            if (hrefStart > 0 && hrefEnd > hrefStart) {
                return new String(rawHtml, hrefStart, hrefEnd - hrefStart + 1);
            }
        }else {
            byte[] bytes = key.getBytes(StandardCharsets.UTF_8);

            //获取当前标签的位置，从中解析属性
            for (int i = openStartIndex + name.length + 1; i < openEndIndex; i++) {

            }

        }

        return "";
    }

    @Override
    public String toString() {
        return "Node{" +
                ", name=" + (name != null ? new String(name) : "") +
                ", id=" + (id != null ? new String(id) : "") +
                ", allClass=" + (allClass != null ? new String(allClass) : "") +
                ", openStartIndex=" + openStartIndex +
                ", openEndIndex=" + openEndIndex +
                ", closeStartIndex=" + closeStartIndex +
                ", closeEndIndex=" + closeEndIndex +
                '}';
    }
}
