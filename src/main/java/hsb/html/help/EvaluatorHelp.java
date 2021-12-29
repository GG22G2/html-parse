package hsb.html.help;

import hsb.html.dom.Node;
import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.List;

/**
 * @author 胡帅博
 * @date 2021/12/29 13:16
 */
public class EvaluatorHelp {


    public static boolean endsWith(byte[] b1, byte[] b2) {
        return startsWith(b1, b1.length - b2.length, b2);

    }

    public static boolean startsWith(byte[] b1, byte[] b2) {
        return startsWith(b1, 0, b2);

    }


    public static boolean startsWith(byte[] b1, int toffset, byte[] b2) {
        int po = 0;
        int pc = b2.length;
        int to = toffset;
        while (po < pc) {
            if (b1[to++] != b2[po++]) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(byte[] b1, byte[] b2) {
        if (b1==null||b2==null) return false;
        if (b1.length != b2.length) {
            return false;
        }
        int po = 0;
        int pc = b1.length;
        while (po < pc) {
            if (b1[po] != b2[po++]) {
                return false;
            }
        }
        return true;
    }


    public static boolean contains(byte[] b1, byte[] b2) {

        int pc = b2.length;
        int matchCount = b1.length - b2.length + 1;
        for (int i = 0; i < matchCount; i++) {
            int po = 0;
            int to = i;
            while (po < pc) {
                if (b1[to++] != b2[po++]) {
                    return false;
                }
            }
            return true;
        }

        return true;
    }


    public static int siblingIndex(Node root, Node element) {
        if (element != root) {
            Node parent = element.parent;
            Node[] children = parent.children;
            int length = parent.size;
            for (int i = 0; i < length; i++) {
                if (children[i] == element) {
                    return i;
                }
            }
        }
        return 0;
    }


    public static int previousElementSibling(Node root, Node element) {
        if (element != root) {
            Node parent = element.parent;
            Node[] children = parent.children;
            int length = parent.size;
            for (int i = 0; i < length; i++) {
                if (children[i] == element) {
                    return i;
                }
            }
        }
        return 0;
    }



}
