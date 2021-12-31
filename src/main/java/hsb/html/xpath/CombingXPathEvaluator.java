package hsb.html.xpath;



import hsb.html.dom.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author code4crafter@gmail.com
 */
public class CombingXPathEvaluator implements XPathEvaluator {

    private List<XPathEvaluator> xPathEvaluators;

    public CombingXPathEvaluator(List<XPathEvaluator> xPathEvaluators) {
        this.xPathEvaluators = xPathEvaluators;
    }

    public CombingXPathEvaluator(XPathEvaluator... xPathEvaluators) {
        this.xPathEvaluators = Arrays.asList(xPathEvaluators);
    }

    @Override
    public List<Node> findAll(Node element) {
        List<Node> results = new ArrayList<Node>();
        for (XPathEvaluator xPathEvaluator : xPathEvaluators) {
            results.addAll(xPathEvaluator.findAll(element));
        }
        return results;
    }

    @Override
    public Node findFirst(Node element) {
        return null;
    }

    @Override
    public List<Node> findTag(Node element, String tagName) {
        return null;
    }

    @Override
    public boolean hasAttribute() {
        for (XPathEvaluator xPathEvaluator : xPathEvaluators) {
            if (xPathEvaluator.hasAttribute()) {
                return true;
            }
        }
        return false;
    }
}
