package hsb;

import hsb.html.HtmlNodeParse;
import hsb.html.dom.Node;
import hsb.html.help.HTMLConstructAnalysis;
import hsb.html.xpath.XPathEvaluator;
import hsb.html.xpath.XPathParser;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import us.codecraft.xsoup.Xsoup;


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
            byte[] finalHtmlBytes = FileUtils.readFileToByteArray(new File(htmlFiles[0]));
            finalHtmlBytes = new String(finalHtmlBytes, "gbk").getBytes(StandardCharsets.UTF_8);
            String shtml = new String(finalHtmlBytes);
            //   XPathEvaluator parse = XPathParser.parse("//*[@id=\"normalthread_919674\"]/tr/th/a[2]");

            //    XPathEvaluator parse1 = XPathParser.parse("//*[@id=\"normalthread_919674\"]");
            //   XPathEvaluator parse2 = XPathParser.parse("/tr/th/a[2]");
            int[] constructIndex = HTMLConstructAnalysis.whiteSpaceStartAndEndIndex(finalHtmlBytes);
            XPathEvaluator parse3 = XPathParser.parse("//a");
            for (int i = 0; i < 10000; i++) {


                long startNanos_16_36 = System.nanoTime();
                int[] constructIndex = HTMLConstructAnalysis.whiteSpaceStartAndEndIndex(finalHtmlBytes);
                Node root = HtmlNodeParse.parse(finalHtmlBytes, constructIndex);
                long endNanos_16_38 = System.nanoTime();
                System.out.println((endNanos_16_38 - startNanos_16_36) / 1000000.0);


//                long startNanos_59_47 = System.nanoTime();
//                compile.evaluate(Jsoup.parse(shtml));
//                long endNanos_59_49 = System.nanoTime();
//                System.out.println((endNanos_59_49 - startNanos_59_47) / 1000000.0);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
