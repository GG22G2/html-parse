package hsb.html.select;


import hsb.html.dom.Node;
import hsb.html.help.EvaluatorHelp;

/**
 * Base structural evaluator.
 */
public abstract class StructuralEvaluator extends Evaluator {
    Evaluator evaluator;

    public static class Root extends Evaluator {
        public boolean matches(Node root, Node element) {
            return root == element;
        }
    }

    static class Has extends StructuralEvaluator {
        public Has(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        public boolean matches(Node root, Node element) {
            //getAllElements获取所有子元素，子孙元素，返回的集合
            //todo 待做
/*            for (Node e : element.getAllElements()) {
                if (e != element && evaluator.matches(element, e))
                    return true;
            }*/
            return false;
        }

        @Override
        public String toString() {
            return String.format(":has(%s)", evaluator);
        }
    }

    static class Not extends StructuralEvaluator {
        public Not(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        public boolean matches(Node root, Node node) {
            return !evaluator.matches(root, node);
        }

        @Override
        public String toString() {
            return String.format(":not%s", evaluator);
        }
    }

    public static class Parent extends StructuralEvaluator {
        public Parent(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        public boolean matches(Node root, Node element) {
            if (root == element)
                return false;

            Node parent = element.parent;
            while (true) {
                if (evaluator.matches(root, parent))
                    return true;
                if (parent == root)
                    break;
                parent = parent.parent;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format(":parent%s", evaluator);
        }
    }

    public static class ImmediateParent extends StructuralEvaluator {
        public ImmediateParent(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        public boolean matches(Node root, Node element) {
            if (root == element)
                return false;

            Node parent = element.parent;
            return parent != null && evaluator.matches(root, parent);
        }

        @Override
        public String toString() {
            return String.format(":ImmediateParent%s", evaluator);
        }
    }

    static class PreviousSibling extends StructuralEvaluator {
        public PreviousSibling(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        public boolean matches(Node root, Node element) {
            if (root == element)
                return false;

            Node el = element.preSiblingNode;
            while (el != null) {
                if (evaluator.matches(root, el))
                    return true;
                el = el.preSiblingNode;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format(":prev*%s", evaluator);
        }
    }

    static class ImmediatePreviousSibling extends StructuralEvaluator {
        public ImmediatePreviousSibling(Evaluator evaluator) {
            this.evaluator = evaluator;
        }

        public boolean matches(Node root, Node element) {
            if (root == element)
                return false;
            Node prev = element.preSiblingNode;
            return prev != null && evaluator.matches(root, prev);
        }

        @Override
        public String toString() {
            return String.format(":prev%s", evaluator);
        }
    }
}
