package hsb.html.xpath;


import hsb.html.dom.Node;


/**
 * @author code4crafter@gmail.com
 */
public interface XPathEvaluator {

    XElements evaluate(Node element);

    boolean hasAttribute();
}
