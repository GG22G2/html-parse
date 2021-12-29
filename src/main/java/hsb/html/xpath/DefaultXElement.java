package hsb.html.xpath;


import hsb.html.dom.Node;


/**
 * XPath result.
 *
 * @author code4crafter@gmail.com
 */
public class DefaultXElement implements XElement {

    private Node element;

    private ElementOperator elementOperator;

    public DefaultXElement(Node element, ElementOperator elementOperator) {
        this.element = element;
        this.elementOperator = elementOperator;
    }

    @Override
    public String get() {
        return get(elementOperator);
    }

    protected String get(ElementOperator elementOperator) {
        if (elementOperator == null) {
            return element.toString();
        }
        else {
            return elementOperator.operate(element);
        }
    }

    public String toString() {
        return get();
    }

    @Override
    public Node getElement() {
        return element;
    }
}
