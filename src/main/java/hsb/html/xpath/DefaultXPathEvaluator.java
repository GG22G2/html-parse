package hsb.html.xpath;


import hsb.html.dom.Node;
import hsb.html.select.Collector;
import hsb.html.select.Evaluator;

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
    public XElements evaluate(Node element) {
        List elements;
        if (evaluator != null) {
            elements = Collector.collect(evaluator, element);
        }
        else {
            elements = new ArrayList(1);
            elements.add(element);
        }
        return new DefaultXElements(elements, elementOperator);
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
