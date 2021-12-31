package hsb.html.select;

import hsb.html.dom.Node;
import org.jsoup.helper.Validate;

import java.util.Arrays;
import java.util.List;

import static hsb.html.select.NodeFilter.*;


/**
 * Depth-first node traversor. Use to iterate through all nodes under and including the specified root node.
 * <p>
 * This implementation does not use recursion, so a deep DOM does not risk blowing the stack.
 * </p>
 */
public class NodeTraversor {
    /**
     * Start a depth-first traverse of the root and all of its descendants.
     *
     * @param visitor Node visitor.
     * @param root    the root node point to traverse.
     */
    public static void traverse(NodeVisitor visitor, Node root) {
        Node node = root;
        int depth = 0;
        int t = 0;

        while (node != root || t++ == 0) {
            visitor.head(node, depth);
            if (node.size > 0) {
                node = node.children(0);
                depth++;
            } else {
                while ((node != root && node.parent.size <= node.siblingIndex + 1) && depth > 0) {
                    visitor.tail(node, depth);
                    node = node.parent;
                    depth--;
                }
                visitor.tail(node, depth);
                if (node == root)
                    break;
                node = node.parent.children[node.siblingIndex + 1];
            }
        }
    }

    /**
     * Start a depth-first traverse of all elements.
     *
     * @param visitor  Node visitor.
     * @param elements Elements to filter.
     */
    public static void traverse(NodeVisitor visitor, List<Node> elements) {
        Validate.notNull(visitor);
        Validate.notNull(elements);
        for (Node el : elements)
            traverse(visitor, el);
    }

    /**
     * Start a depth-first filtering of the root and all of its descendants.
     *
     * @param filter Node visitor.
     * @param root   the root node point to traverse.
     * @return The filter result of the root node, or {@link FilterResult#STOP}.
     * //
     */
    public static FilterResult filter(NodeFilter filter, Node root) {
        Node node = root;
        int depth = 0;

        while (node != null) {
            FilterResult result = filter.head(node, depth);
            if (result == FilterResult.STOP)
                return result;
            // Descend into child nodes:
            if (result == FilterResult.CONTINUE && node.size > 0) {
                node = node.children[0];
                ++depth;
                continue;
            }
            // No siblings, move upwards:
            while ((node != root && node.parent.size <= node.siblingIndex + 1) && depth > 0) {
                // 'tail' current node:
                if (result == FilterResult.CONTINUE || result == FilterResult.SKIP_CHILDREN) {
                    result = filter.tail(node, depth);
                    if (result == FilterResult.STOP)
                        return result;
                }
                Node prev = node; // In case we need to remove it below.
                node = node.parent;
                depth--;
                result = FilterResult.CONTINUE; // Parent was not pruned.
            }
            // 'tail' current node, then proceed with siblings:
            if (result == FilterResult.CONTINUE || result == FilterResult.SKIP_CHILDREN) {
                result = filter.tail(node, depth);
                if (result == FilterResult.STOP)
                    return result;
            }
            if (node == root)
                return result;
            Node prev = node; // In case we need to remove it below.
            node = node.parent.children[node.siblingIndex + 1];
        }
        // root == null?
        return FilterResult.CONTINUE;
    }

    /**
     * Start a depth-first filtering of all elements.
     *
     * @param filter   Node filter.
     * @param elements Elements to filter.
     */
    public static void filter(NodeFilter filter, List<Node> elements) {
        Validate.notNull(filter);
        Validate.notNull(elements);
        for (Node el : elements)
            if (filter(filter, el) == FilterResult.STOP)
                break;
    }

    //遍历元素，找某一类元素，比如a标签
    public static void filterSingleEvaluator(Node root, Evaluator evaluator, List<Node> elements) {
        int stackTop = 0;
        //todo 默认元素深度不会超过256，超过了一定报错
        Node[] stack = new Node[256];
        int[] stackPosition = new int[256];
        Node stackTopNode = root;
        stack[0] = root;
        while (true) {
            int index = stackPosition[stackTop];
            if (stackTopNode.size > index) {
                Node child = stackTopNode.children[index++];
                stackPosition[stackTop] = index;
                if (evaluator.matches(root,child)){
                    elements.add(child);
                }
                stack[++stackTop] = child;
                stackTopNode = child;
            } else {
                stackPosition[stackTop] = 0;
                if (stackTop==0){
                    break;
                }
                stackTopNode = stack[--stackTop];
            }
        }

    }
}
