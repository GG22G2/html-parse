package hsb.html.select;

import hsb.html.HtmlNodeParse;
import hsb.html.dom.Node;
import hsb.html.help.EvaluatorHelp;
import org.jsoup.helper.Validate;

import java.util.regex.Pattern;

import static org.jsoup.internal.Normalizer.lowerCase;
import static org.jsoup.internal.Normalizer.normalize;


/**
 * Evaluates that an element matches the selector.
 */
public abstract class Evaluator {
    protected Evaluator() {
    }

    /**
     * Test if the element meets the evaluator's requirements.
     *
     * @param root    Root of the matching subtree
     * @param element tested element
     * @return Returns <tt>true</tt> if the requirements are met or
     * <tt>false</tt> otherwise
     */
    public abstract boolean matches(Node root, Node element);

    /**
     * Evaluator for tag name
     */
    public static final class Tag extends Evaluator {
        private byte[] tagName;
        private long tagNameHash;

        public Tag(byte[] tagName) {
            this.tagName = tagName;
            tagNameHash = HtmlNodeParse.tagNameByteToLong(tagName);
        }

        @Override
        public boolean matches(Node root, Node element) {
            return element.nameHash == tagNameHash;
        }

        @Override
        public String toString() {
            return String.format("%s", tagName);
        }
    }


    /**
     * Evaluator for tag name that ends with
     */
    public static final class TagEndsWith extends Evaluator {
        private byte[] tagName;
        private long tagNameHash;

        public TagEndsWith(byte[] tagName) {
            this.tagName = tagName;
            tagNameHash = HtmlNodeParse.tagNameByteToLong(tagName);
        }

        @Override
        public boolean matches(Node root, Node element) {

            return EvaluatorHelp.endsWith(element.name, tagName);
        }

        @Override
        public String toString() {
            return String.format("%s", tagName);
        }
    }

    /**
     * Evaluator for element id
     */
    public static final class Id extends Evaluator {
        private byte[] id;

        public Id(byte[] id) {
            this.id = id;
        }

        @Override
        public boolean matches(Node root, Node element) {
            return EvaluatorHelp.equals(id, element.id);
        }

        @Override
        public String toString() {
            return String.format("#%s", id);
        }
    }


    public static final class IdContain extends Evaluator {
        private byte[] id;

        public IdContain(byte[] id) {
            this.id = id;
        }

        @Override
        public boolean matches(Node root, Node element) {
            if (element.id != null) {
                return EvaluatorHelp.contains(element.id, id);
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("#%s", id);
        }
    }

    /**
     * Evaluator for element class
     */
    public static final class Class extends Evaluator {
        private byte[] className;

        public Class(byte[] className) {
            this.className = className;
        }

        @Override
        public boolean matches(Node root, Node element) {

            for (byte[] bytes : element.classList) {
                if (EvaluatorHelp.equals(className, bytes)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format(".%s", className);
        }

    }

    /**
     * Evaluator for attribute name matching
     */
    public static final class Attribute extends Evaluator {
        private byte[] key;
        private int attributeType = -1; // 0 id ,1 class ,2 href

        public Attribute(int attributeType) {
            this.attributeType = attributeType;
        }

        //只匹配id class href
        @Override
        public boolean matches(Node root, Node element) {
            if (attributeType == 0) {
                return element.id != null;
            } else if (attributeType == 1) {
                return element.allClass != null;
            } else if (attributeType == 2) {
                return element.hrefStart != -1;
            }
            return false;
        }

        @Override
        public String toString() {
            return String.format("[%s]", key);
        }

    }

    /**
     * Evaluator for attribute name prefix matching
     * todo 不能用
     */
    public static final class AttributeStarting extends Evaluator {
        private String keyPrefix;

        public AttributeStarting(String keyPrefix) {
            Validate.notEmpty(keyPrefix);
            this.keyPrefix = lowerCase(keyPrefix);
        }

        @Override
        public boolean matches(Node root, Node element) {
            return false;
        }

        @Override
        public String toString() {
            return String.format("[^%s]", keyPrefix);
        }

    }

    /**
     * Evaluator for attribute name/value matching
     * <p>
     * todo 不能用
     */
    public static final class AttributeWithValue extends AttributeKeyPair {
        public AttributeWithValue(String key, String value) {
            super(key, value);
        }

        @Override
        public boolean matches(Node root, Node element) {
            return false;
        }

        @Override
        public String toString() {
            return String.format("[%s=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name != value matching
     * todo 不能用
     */
    public static final class AttributeWithValueNot extends AttributeKeyPair {
        public AttributeWithValueNot(String key, String value) {
            super(key, value);
        }

        @Override
        public boolean matches(Node root, Node element) {
            return false;
        }

        @Override
        public String toString() {
            return String.format("[%s!=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name/value matching (value prefix)
     * todo 不能用
     */
    public static final class AttributeWithValueStarting extends AttributeKeyPair {
        public AttributeWithValueStarting(String key, String value) {
            super(key, value, false);
        }

        @Override
        public boolean matches(Node root, Node element) {
            return false; // value is lower case already
        }

        @Override
        public String toString() {
            return String.format("[%s^=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name/value matching (value ending)
     * todo 不能用
     */
    public static final class AttributeWithValueEnding extends AttributeKeyPair {
        public AttributeWithValueEnding(String key, String value) {
            super(key, value, false);
        }

        @Override
        public boolean matches(Node root, Node element) {
            return false; // value is lower case
        }

        @Override
        public String toString() {
            return String.format("[%s$=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name/value matching (value containing)
     * todo 不能用
     */
    public static final class AttributeWithValueContaining extends AttributeKeyPair {
        public AttributeWithValueContaining(String key, String value) {
            super(key, value);
        }

        @Override
        public boolean matches(Node root, Node element) {
            return false; // value is lower case
        }

        @Override
        public String toString() {
            return String.format("[%s*=%s]", key, value);
        }

    }

    /**
     * Evaluator for attribute name/value matching (value regex matching)
     * <p>
     * todo 不能用
     */
    public static final class AttributeWithValueMatching extends Evaluator {
        String key;
        Pattern pattern;

        public AttributeWithValueMatching(String key, Pattern pattern) {
            this.key = normalize(key);
            this.pattern = pattern;
        }

        @Override
        public boolean matches(Node root, Node element) {
            return false;//element.hasAttr(key) && pattern.matcher(element.attr(key)).find();
        }

        @Override
        public String toString() {
            return String.format("[%s~=%s]", key, pattern.toString());
        }

    }

    /**
     * Abstract evaluator for attribute name/value matching
     */
    public abstract static class AttributeKeyPair extends Evaluator {
        String key;
        String value;

        public AttributeKeyPair(String key, String value) {
            this(key, value, true);
        }

        public AttributeKeyPair(String key, String value, boolean trimValue) {
            Validate.notEmpty(key);
            Validate.notEmpty(value);

            this.key = normalize(key);
            boolean isStringLiteral = value.startsWith("'") && value.endsWith("'")
                    || value.startsWith("\"") && value.endsWith("\"");
            if (isStringLiteral) {
                value = value.substring(1, value.length() - 1);
            }

            this.value = trimValue ? normalize(value) : normalize(value, isStringLiteral);
        }
    }

    /**
     * Evaluator for any / all element matching
     */
    public static final class AllElements extends Evaluator {

        @Override
        public boolean matches(Node root, Node element) {
            return true;
        }

        @Override
        public String toString() {
            return "*";
        }
    }

    /**
     * Evaluator for matching by sibling index number (e {@literal <} idx)
     */
    public static final class IndexLessThan extends IndexEvaluator {
        public IndexLessThan(int index) {
            super(index);
        }

        @Override
        public boolean matches(Node root, Node element) {
            return root != element && EvaluatorHelp.siblingIndex(root, element) < index;
        }

        @Override
        public String toString() {
            return String.format(":lt(%d)", index);
        }

    }

    /**
     * Evaluator for matching by sibling index number (e {@literal >} idx)
     */
    public static final class IndexGreaterThan extends IndexEvaluator {
        public IndexGreaterThan(int index) {
            super(index);
        }

        @Override
        public boolean matches(Node root, Node element) {
            return EvaluatorHelp.siblingIndex(root, element) > index;
        }

        @Override
        public String toString() {
            return String.format(":gt(%d)", index);
        }

    }

    /**
     * Evaluator for matching by sibling index number (e = idx)
     */
    public static final class IndexEquals extends IndexEvaluator {
        public IndexEquals(int index) {
            super(index);
        }

        @Override
        public boolean matches(Node root, Node element) {
            return EvaluatorHelp.siblingIndex(root, element) == index;
        }

        @Override
        public String toString() {
            return String.format(":eq(%d)", index);
        }

    }

    /**
     * Evaluator for matching the last sibling (css :last-child)
     */
    public static final class IsLastChild extends Evaluator {
        @Override
        public boolean matches(Node root, Node element) {
            final Node p = element.parent;
            return p != null && EvaluatorHelp.siblingIndex(root, element) == p.size - 1;
        }

        @Override
        public String toString() {
            return ":last-child";
        }
    }

    public static final class IsFirstOfType extends IsNthOfType {
        public IsFirstOfType() {
            super(0, 1);
        }

        @Override
        public String toString() {
            return ":first-of-type";
        }
    }

    public static final class IsLastOfType extends IsNthLastOfType {
        public IsLastOfType() {
            super(0, 1);
        }

        @Override
        public String toString() {
            return ":last-of-type";
        }
    }


    public static abstract class CssNthEvaluator extends Evaluator {
        protected final int a, b;

        public CssNthEvaluator(int a, int b) {
            this.a = a;
            this.b = b;
        }

        public CssNthEvaluator(int b) {
            this(0, b);
        }

        @Override
        public boolean matches(Node root, Node element) {
            final Node p = element.parent;
            if (p == null) return false;

            final int pos = calculatePosition(root, element);
            if (a == 0) return pos == b;

            return (pos - b) * a >= 0 && (pos - b) % a == 0;
        }

        @Override
        public String toString() {
            if (a == 0)
                return String.format(":%s(%d)", getPseudoClass(), b);
            if (b == 0)
                return String.format(":%s(%dn)", getPseudoClass(), a);
            return String.format(":%s(%dn%+d)", getPseudoClass(), a, b);
        }

        protected abstract String getPseudoClass();

        protected abstract int calculatePosition(Node root, Node element);
    }


    /**
     * css-compatible Evaluator for :eq (css :nth-child)
     *
     * @see IndexEquals
     */
    public static final class IsNthChild extends CssNthEvaluator {

        public IsNthChild(int a, int b) {
            super(a, b);
        }

        protected int calculatePosition(Node root, Node element) {
            return EvaluatorHelp.siblingIndex(root, element) + 1;
        }


        protected String getPseudoClass() {
            return "nth-child";
        }
    }

    /**
     * css pseudo class :nth-last-child)
     *
     * @see IndexEquals
     */
    public static final class IsNthLastChild extends CssNthEvaluator {
        public IsNthLastChild(int a, int b) {
            super(a, b);
        }

        @Override
        protected int calculatePosition(Node root, Node element) {
            return element.parent.size - EvaluatorHelp.siblingIndex(root, element);
        }

        @Override
        protected String getPseudoClass() {
            return "nth-last-child";
        }
    }

    /**
     * css pseudo class nth-of-type
     */
    public static class IsNthOfType extends CssNthEvaluator {
        public IsNthOfType(int a, int b) {
            super(a, b);
        }

        protected int calculatePosition(Node root, Node element) {
            int pos = 0;
            Node[] family = element.parent.children;
            int size = element.parent.size;

            for (int i = 0; i < size; i++) {
                Node el = family[i];
                if (EvaluatorHelp.equals(el.name, element.name)) pos++;
                if (el == element) break;
                ;
            }
            return pos;
        }

        @Override
        protected String getPseudoClass() {
            return "nth-of-type";
        }
    }

    public static class IsNthLastOfType extends CssNthEvaluator {

        public IsNthLastOfType(int a, int b) {
            super(a, b);
        }

        @Override
        protected int calculatePosition(Node root, Node element) {
            int pos = 0;
            Node[] family = element.parent.children;
            int size = element.parent.size;
            for (int i = EvaluatorHelp.siblingIndex(root, element); i < size; i++) {
                Node el = family[i];
                if (EvaluatorHelp.equals(el.name, element.name)) pos++;
            }
            return pos;
        }

        @Override
        protected String getPseudoClass() {
            return "nth-last-of-type";
        }
    }

    /**
     * Evaluator for matching the first sibling (css :first-child)
     */
    public static final class IsFirstChild extends Evaluator {
        @Override
        public boolean matches(Node root, Node element) {
            final Node p = element.parent;
            return p != null && EvaluatorHelp.siblingIndex(root, element) == 0;
        }

        @Override
        public String toString() {
            return ":first-child";
        }
    }

    /**
     * css3 pseudo-class :root
     *
     * @see <a href="http://www.w3.org/TR/selectors/#root-pseudo">:root selector</a>
     */
    public static final class IsRoot extends Evaluator {
        @Override
        public boolean matches(Node root, Node element) {
            return element == root;
        }

        @Override
        public String toString() {
            return ":root";
        }
    }

    public static final class IsOnlyChild extends Evaluator {
        @Override
        public boolean matches(Node root, Node element) {
            final Node p = element.parent;
            return p != null && p.size == 1;
        }

        @Override
        public String toString() {
            return ":only-child";
        }
    }

    public static final class IsOnlyOfType extends Evaluator {
        @Override
        public boolean matches(Node root, Node element) {
            final Node p = element.parent;
            if (p == null) return false;
            int pos = 0;
            Node[] family = p.children;
            int size = p.size;

            for (int i = 0; i < size; i++) {
                Node el = family[i];
                if (EvaluatorHelp.equals(el.name, element.name)) pos++;
            }
            return pos == 1;
        }

        @Override
        public String toString() {
            return ":only-of-type";
        }
    }

    //todo 这个的实现和原来不太一样，最好不要用力
    public static final class IsEmpty extends Evaluator {
        @Override
        public boolean matches(Node root, Node element) {

            if (element.size > 0 || (element.closeStartIndex - element.openEndIndex > 0)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return ":empty";
        }
    }

    /**
     * Abstract evaluator for sibling index matching
     *
     * @author ant
     */
    public abstract static class IndexEvaluator extends Evaluator {
        int index;

        public IndexEvaluator(int index) {
            this.index = index;
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) text
     * 获取节点中文本，子文本，以及子元素，子子元素中的文本
     * <p>
     * todo 这个待做，
     */
    public static final class ContainsText extends Evaluator {
        private String searchText;

        public ContainsText(String searchText) {
            this.searchText = lowerCase(searchText);
        }

        @Override
        public boolean matches(Node root, Node element) {
            return false;
            //   lowerCase(element.text()).contains(searchText);
        }

        @Override
        public String toString() {
            return String.format(":contains(%s)", searchText);
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) data
     * todo 这个不能用
     */
    public static final class ContainsData extends Evaluator {
        private String searchText;

        public ContainsData(String searchText) {
            this.searchText = lowerCase(searchText);
        }

        @Override
        public boolean matches(Node root, Node element) {
            return false;
            //lowerCase(element.data()).contains(searchText);
        }

        @Override
        public String toString() {
            return String.format(":containsData(%s)", searchText);
        }
    }

    /**
     * Evaluator for matching Element's own text
     * <p>
     * 获取自己的文本，不包括子元素的
     * todo 待做
     */
    public static final class ContainsOwnText extends Evaluator {
        private String searchText;

        public ContainsOwnText(String searchText) {
            this.searchText = lowerCase(searchText);
        }

        @Override
        public boolean matches(Node root, Node element) {
            return false;//lowerCase(element.ownText()).contains(searchText);
        }

        @Override
        public String toString() {
            return String.format(":containsOwn(%s)", searchText);
        }
    }

    /**
     * Evaluator for matching Element (and its descendants) text with regex
     * todo 代做
     */
    public static final class Matches extends Evaluator {
        private Pattern pattern;

        public Matches(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean matches(Node root, Node element) {

          /*  Matcher m = pattern.matcher(element.text());
            return m.find();*/
            return false;
        }

        @Override
        public String toString() {
            return String.format(":matches(%s)", pattern);
        }
    }

    /**
     * Evaluator for matching Element's own text with regex
     * todo 待做
     */
    public static final class MatchesOwn extends Evaluator {
        private Pattern pattern;

        public MatchesOwn(Pattern pattern) {
            this.pattern = pattern;
        }

        @Override
        public boolean matches(Node root, Node element) {
    /*        Matcher m = pattern.matcher(element.ownText());
            return m.find();*/
            return false;
        }

        @Override
        public String toString() {
            return String.format(":matchesOwn(%s)", pattern);
        }
    }

    //todo 匹配文本节点 ,这个好像做不了
    public static final class MatchText extends Evaluator {

        @Override
        public boolean matches(Node root, Node element) {
 /*           if (element instanceof PseudoTextElement)
                return true;*/

/*            List<TextNode> textNodes = element.textNodes();
            for (TextNode textNode : textNodes) {
                PseudoTextElement pel = new PseudoTextElement(
                        org.jsoup.parser.Tag.valueOf(element.tagName()), element.baseUri(), element.attributes());
                textNode.replaceWith(pel);
                pel.appendChild(textNode);
            }*/
            return false;
        }

        @Override
        public String toString() {
            return ":matchText";
        }
    }
}
