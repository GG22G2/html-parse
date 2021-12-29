package hsb.html.xpath;


import hsb.html.dom.Node;
import hsb.html.help.EvaluatorHelp;
import hsb.html.select.Evaluator;

/**
 * Evaluators in Xsoup.
 *
 * @author code4crafter@gmail.com
 */
public abstract class XEvaluators {

    public static class HasAnyAttribute extends Evaluator {

        @Override
        public boolean matches(Node root, Node element) {
            if (element.id != null || element.allClass != null || element.hrefStart > 0) {
                return true;
            }
           // element.attributeKIndex

            return false;
        }
    }

    public static class IsNthOfType extends Evaluator.CssNthEvaluator {
        public IsNthOfType(int a, int b) {
            super(a, b);
        }

        protected int calculatePosition(Node root, Node element) {
            int pos = 0;
            Node[] family = element.parent.children;
            int size = element.parent.size;
            for (int i = 0; i < size; i++) {
                if (EvaluatorHelp.equals(family[i].name, element.name)) pos++;
                if (family[i] == element) break;
            }
            return pos;
        }

        @Override
        protected String getPseudoClass() {
            return "nth-of-type";
        }
    }
}
