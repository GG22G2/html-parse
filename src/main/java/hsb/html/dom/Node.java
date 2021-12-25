package hsb.html.dom;


import hsb.html.StringIndex;


public class Node {

    Node[] children = new Node[4];
    Node[] textChildren = new Node[5];

    //标签名称
    public byte[] name;

    //一般都通过id 或者 class找元素，所以把这两个单独拿出来
    public byte[] id;
    //计算id的hash值，这个值要保证在这篇文档中，不同的id数组对应的idHash一定不同，每个文档一个字节数组到id的映射空间，暂时还没想出来咋做，所在暂时不用idHash
    public long idHash = 0;

    //原始的 class内存
    public byte[] allClass;

    //class可能包含很多个子块，分割后保存
    public byte[][] classList;

    //文本节点的数据
    public StringIndex text;

    //是否自闭合 只有在解析标签是遇到/> 这个值才可能是true ，之所以不是一定,是因为如<div id = fee/>，这个"fee/"都被当作id的值,chrome也是这么处理的
    //但实际上是否被当作自闭合，好像有严格限制，比如div就不允许自闭合，<div/>也会被认为是开始标签
    public boolean selfClose = false;
    //代表标签闭合，用在程序中标记一个node节点处理完成
    public boolean close = false;

    //Object[] attributes;

    int cap1 = 4;
    int cap2 = 5;
    int size1 = 0;
    int size2 = 0;

    public Node() {

    }

    public void appendChild(Node node) {
        if (size1 >= cap1) {
            ChildExpandCapacity(cap1 << 1);
        }
        children[size1++] = node;
    }

    public void appendTextChild(Node node) {
        if (size2 >= cap2) {
            textChildExpandCapacity(cap1 + 1);
        }
        textChildren[size2++] = node;
    }

    //只包含元素节点，非空节点或者文本节点都不包含
    public Node children(int index) {
        return children[index];
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
            n = textChildren[index];
        } else { //奇数
            n = children[index];
        }
        return n;
    }


    public void ChildExpandCapacity(int newCap) {
        Node[] n = new Node[newCap];
        System.arraycopy(children, 0, n, 0, children.length);
        children = n;
        cap1 = newCap;
    }

    public void textChildExpandCapacity(int newCap) {
        Node[] n = new Node[newCap];
        System.arraycopy(textChildren, 0, n, 0, textChildren.length);
        textChildren = n;
        cap2 = newCap;
    }


    @Override
    public String toString() {
        return "Node{" +
                "name='" + new String(name)  + '\'' +
                ", id=" + (id != null ? new String(id) : null) +
                ", allClass=" + (allClass != null ? new String(allClass) : "") +
                '}';
    }
}
