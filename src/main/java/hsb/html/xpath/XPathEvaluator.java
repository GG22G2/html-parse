package hsb.html.xpath;


import hsb.html.dom.Node;

import java.util.List;


/**
 * @author code4crafter@gmail.com
 */
public interface XPathEvaluator {

    List<Node> findAll(Node element);

    Node findFirst(Node element);

    List<Node> findTag(Node element,String tagName);


    boolean hasAttribute();
}
