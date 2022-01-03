package hsb.html.select;


import hsb.html.dom.Node;

import java.util.ArrayList;
import java.util.List;

import static hsb.html.select.NodeFilter.FilterResult.CONTINUE;


/**
 * Collects a list of elements that match the supplied criteria.
 *
 * @author Jonathan Hedley
 */
public class Collector {

    private Collector() {
    }

    /**
     * Build a list of elements, by visiting root and every descendant of root, and testing it against the evaluator.
     *
     * @param eval Evaluator to test elements against
     * @param root root of tree to descend
     * @return list of matches; empty if none
     */
    public static List<Node> collect(Evaluator eval, Node root) {
        List elements = new ArrayList();
        NodeTraversor.traverse(new Accumulator(root, elements, eval), root);
        return elements;
    }

    public static class ArrayFilter{
        private final Node root;
        private final List<Node> elements;
        private final Evaluator eval;

        public ArrayFilter(Node root, List<Node> elements, Evaluator eval) {
            this.root = root;
            this.elements = elements;
            this.eval = eval;
        }

        public void head(Node node, int depth) {
            if (eval.matches(root, node))
                elements.add(node);
        }

    }




    public static class Accumulator implements NodeVisitor {
        private final Node root;
        private final List<Node> elements;
        private final Evaluator eval;

       public Accumulator(Node root, List<Node> elements, Evaluator eval) {
            this.root = root;
            this.elements = elements;
            this.eval = eval;
        }

        public void head(Node node, int depth) {

            if (eval.matches(root, node))
                elements.add(node);

        }

        public void tail(Node node, int depth) {
            // void
        }
    }

    public static Node findFirst(Evaluator eval, Node root) {
        FirstFinder finder = new FirstFinder(root, eval);
        NodeTraversor.filter(finder, root);
        return finder.match;
    }

    public static class FirstFinder implements NodeFilter {
        private final Node root;
        private Node match = null;
        private final Evaluator eval;

        FirstFinder(Node root, Evaluator eval) {
            this.root = root;
            this.eval = eval;
        }

        @Override
        public FilterResult head(Node node, int depth) {

            if (eval.matches(root, node)) {
                match = node;
                return FilterResult.STOP;
            }

            return CONTINUE;
        }

        @Override
        public FilterResult tail(Node node, int depth) {
            return CONTINUE;
        }
    }

}
