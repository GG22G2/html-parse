package hsb;

import hsb.html.HtmlNodeParser;
import hsb.html.dom.Node;
import hsb.html.help.HTMLConstructAnalysis;
import hsb.html.xpath.XPathEvaluator;
import hsb.html.xpath.XPathParser;
import org.apache.commons.io.FileUtils;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class ParseTest {
    public static void main(String[] args) {


        String[] htmlFiles = new String[]{
                "C:\\Users\\h6706\\Desktop\\az0vn-uwu6s.html",
                "C:\\Users\\h6706\\Desktop\\123.html",
                "C:\\Users\\h6706\\Desktop\\1234.html",
                "C:\\Users\\h6706\\Desktop\\333.html",
                "C:\\Users\\h6706\\Desktop\\1234.html",
                "C:\\Users\\h6706\\Desktop\\az0vn-uwu6s.html"
        };

        try {
            byte[] finalHtmlBytes = FileUtils.readFileToByteArray(new File(htmlFiles[4]));
            finalHtmlBytes = new String(finalHtmlBytes, "gbk").getBytes(StandardCharsets.UTF_8);
            String shtml = new String(finalHtmlBytes);
            XPathEvaluator parse = XPathParser.parse("//tbody[@id=\"normalthread_919674\"]/tr/th/a[2]");

            XPathEvaluator parse1 = XPathParser.parse("//tbody[@id=\"normalthread_919674\"]");
            XPathEvaluator parse2 = XPathParser.parse("/tr/th/a[2]");


            HtmlNodeParser parser = new HtmlNodeParser();
            XPathEvaluator parse3 = XPathParser.parse("//a");
            XPathEvaluator parse4 = XPathParser.parse("//*[@id=\"threadlisttableid\"]/tbody[100]/tr/th/a[2]");
            XPathEvaluator parse5 = XPathParser.parse("//*[@id=\"threadlisttableid\"]");
            XPathEvaluator parse6 = XPathParser.parse("/tbody/tr/th/a[2]");

            long startNanos_16_36 = System.nanoTime();
            for (int i = 0; i < 10000; i++) {

                int[] constructIndex = HTMLConstructAnalysis.whiteSpaceStartAndEndIndex(finalHtmlBytes);
                Node root = parser.parse(finalHtmlBytes, constructIndex);
                //   Node node = parse3.findFirst(root);
                Node table = parse5.findFirst(root);
                List<Node> tbodys = parse6.findAll(table);

                //System.out.println(tbodys.size());
            }
            long endNanos_16_38 = System.nanoTime();
            System.out.println((endNanos_16_38 - startNanos_16_36) / 1000000.0);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
