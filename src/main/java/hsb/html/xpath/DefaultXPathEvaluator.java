package hsb.html.xpath;


import hsb.html.dom.Node;
import hsb.html.select.Collector;
import hsb.html.select.Evaluator;
import hsb.html.select.NodeTraversor;
import us.codecraft.xsoup.XElements;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


/**
 * @author code4crafter@gmail.com
 */
public class DefaultXPathEvaluator implements XPathEvaluator {

    private Evaluator evaluator;

    private ElementOperator elementOperator;

    public DefaultXPathEvaluator(Evaluator evaluator, ElementOperator elementOperator) {
        this.evaluator = evaluator;
        this.elementOperator = elementOperator;
    }

    @Override
    public List<Node> findAll(Node element) {
        List elements;
        if (evaluator != null) {
            elements = Collector.collect(evaluator, element);
        } else {
            elements = new ArrayList(1);
            elements.add(element);
        }
        return elements;
    }

    @Override
    public Node findFirst(Node element) {
        Node elements = null;
        if (evaluator != null) {
            elements = Collector.findFirst(evaluator, element);
        }
        return elements;
    }

    @Override
    public List<Node> findTag(Node element, String tagName) {
        Evaluator.Tag tagFilter = new Evaluator.Tag(tagName.getBytes(StandardCharsets.UTF_8));
        List result = new ArrayList();
        NodeTraversor.filterSingleEvaluator( element,tagFilter,result);
        return result;
    }

    @Override
    public boolean hasAttribute() {
        return elementOperator != null;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public String getAttribute() {
        if (elementOperator == null) {
            return null;
        }
        return elementOperator.toString();
    }

    public ElementOperator getElementOperator() {
        return elementOperator;
    }
}
