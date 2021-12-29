package hsb.html.dom;

import java.util.Arrays;

public class Node {

    public Node parent;

    public Node[] children = new Node[4];
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

    public int siblingIndex;
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

    public void appendChild(Node node) {
        if (size >= cap) {
            ChildExpandCapacity(cap << 1);
        }
        children[size++] = node;
    }

/*    public void appendTextChild(Node node) {
        if (size2 >= cap2) {
            textChildExpandCapacity(cap1 + 1);
        }
        textChildren[size2++] = node;
    }*/

    //只包含元素节点，非空节点或者文本节点都不包含
    public Node children(int index) {
        if (index < size) {
            return children[index];
        }
        return null;
    }

    /**
     * 包含文本节点
     * 结构如下： 两个文本节点之间是一个元素节点
     * 文本
     * 元素
     * 文本
     * 元素
     * 文本
     * <p>
     * 文本节点包括  空内容  字符串   或者是注释，   html实际把注释当作一个单独节点  ，但是为了方便，注释和文本合二为一了
     */
    public Node childNodes(int index) {
        Node n;
        int a = index & 0x1;
        index = index >> 1;
        if (a == 0) { //偶数
            //   n = textChildren[index];
            //todo 如果是chrome，这里代表文本标签
            n = null;
        } else { //奇数
            n = children[index];
        }
        return n;
    }


    public void ChildExpandCapacity(int newCap) {
        Node[] n = new Node[newCap];
        System.arraycopy(children, 0, n, 0, children.length);
        children = n;
        cap = newCap;
    }

/*    public void textChildExpandCapacity(int newCap) {
        Node[] n = new Node[newCap];
        System.arraycopy(textChildren, 0, n, 0, textChildren.length);
        textChildren = n;
        cap2 = newCap;
    }*/


    @Override
    public String toString() {
        return "Node{" +
                ", name=" + (name != null ? new String(name) : "") +
                ", id=" + (id != null ? new String(id) : "") +
                ", allClass=" + (allClass != null ? new String(allClass) : "") +
                "openStartIndex=" + openStartIndex +
                ", openEndIndex=" + openEndIndex +
                ", closeStartIndex=" + closeStartIndex +
                ", closeEndIndex=" + closeEndIndex +
                '}';
    }
}
