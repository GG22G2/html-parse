package hsb.html.help;

import hsb.html.dom.Node;
import hsb.html.xpath.XPathEvaluator;
import hsb.html.xpath.XPathParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 胡帅博
 * @date 2022/1/10 13:27
 */
public class DocumentHelper {


    /**
     * 获取所有a标签中的连接，
     * 如果是相对路径的，则转换成绝对路径
     */
    public static List<String> alinks(Node node, String baseUri) {
        XPathEvaluator aLinksSelector = XPathParser.parse("//a");
        List<Node> links = aLinksSelector.findAll(node);
        List<String> all = new ArrayList(links.size());

        try {
            URL base = new URL(baseUri);
            for (Node alink : links) {
                String href = alink.attr("href");
                href = StringUtil.resolve(base, href).toExternalForm();
                all.add(href);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return all;
    }

    //todo 待优化
    public static List<String> alinks(Node node, String baseUri, Pattern regex) {
        XPathEvaluator aLinksSelector = XPathParser.parse("a");
        List<Node> links = aLinksSelector.findAll(node);

        List<String> all = new ArrayList<String>(links.size());

        try {
            URL base = new URL(baseUri);
            for (Node alink : links) {
                String href = alink.attr("href").trim();
                try {
                    if (!href.toLowerCase(Locale.ROOT).startsWith("javascript:")) {
                        href = StringUtil.resolve(base, href).toExternalForm();
                    }else {
                        href = "";
                    }
                } catch (Exception e) {
                    href = "";
                }
                Matcher matcher = regex.matcher(href);
                if (matcher.find()) {
                    all.add(matcher.group(0));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return all;
    }


    //根据当前页面的url 和  base标签中的内容确定baseUri
    public static String baseUri(Node node, String location) {
        XPathEvaluator headSelector = XPathParser.parse("//head");
        XPathEvaluator baseSelector = XPathParser.parse("/base");

        Node headNode = headSelector.findFirst(node);
        Node base = baseSelector.findFirst(headNode);
        String baseUri = null;
        if (base != null) {
            String href = base.attr("href");
            if (href != null && href.length() > 0) {
                baseUri = StringUtil.resolve(location, href);
            }
            return baseUri;
        }
        return location;
    }


}
