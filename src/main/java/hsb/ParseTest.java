package hsb;

import hsb.html.HtmlNodeParse;
import hsb.html.dom.Node;
import hsb.html.help.HTMLConstructAnalysis;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;


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
            int[] constructIndex = HTMLConstructAnalysis.whiteSpaceStartAndEndIndex(finalHtmlBytes);
            for (int i = 0; i < 10000; i++) {
                long startNanos_16_36 = System.nanoTime();
                Node root = HtmlNodeParse.parse(finalHtmlBytes, constructIndex);
                long endNanos_16_38 = System.nanoTime();
                System.out.println((endNanos_16_38 - startNanos_16_36) / 1000000.0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
