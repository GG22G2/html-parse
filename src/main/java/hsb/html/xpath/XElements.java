package hsb.html.xpath;

import hsb.html.dom.Node;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * @author code4crafter@gmail.com
 */
public interface XElements {

    String get();

    List<String> list();

    List<Node> getElements();
}
