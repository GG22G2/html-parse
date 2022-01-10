package hsb;

import hsb.html.HtmlNodeParser;
import hsb.html.dom.Node;
import hsb.html.help.DocumentHelper;
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
                "C:\\Users\\h6706\\Desktop\\52pojie.html",
                "G:\\kaifa_environment\\code\\java\\html-parse\\src\\main\\resources\\html\\2.html",
                "C:\\Users\\h6706\\Desktop\\forum-5-1.html"
        };

        try {
            byte[] finalHtmlBytes = FileUtils.readFileToByteArray(new File(htmlFiles[5]));
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
            int[] constructIndex = HTMLConstructAnalysis.whiteSpaceStartAndEndIndex(finalHtmlBytes);
            long startNanos_16_36 = System.nanoTime();
            for (int i = 0; i < 1; i++) {
                long l = parser.tagNameByteToLong("a".getBytes(StandardCharsets.UTF_8));
                System.out.println(l);
                Node root = parser.parse(finalHtmlBytes, constructIndex);

                String baseUri = DocumentHelper.baseUri(root, "https://www.52pojie.cn/");

                List<String> alinks = DocumentHelper.alinks(root, baseUri);
                System.out.println(baseUri);
                for (String alink : alinks) {
                    System.out.println(alink);
                }


                ///  System.out.println(root);
                //   Node node = parse3.findFirst(root);
                //    Node table = parse5.findFirst(root);
                //    List<Node> tbodys = parse6.findAll(table);
                //System.out.println(tbodys.size());
            }
            long endNanos_16_38 = System.nanoTime();
            System.out.println((endNanos_16_38 - startNanos_16_36) / 1000000.0);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
