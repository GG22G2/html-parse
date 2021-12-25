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

        int threadCount = 6;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        long startNanos_34_31 = System.nanoTime();
        for (int i = 0; i < threadCount; i++) {
            byte[] htmlBytes = null;
            try {
                htmlBytes = FileUtils.readFileToByteArray(new File(htmlFiles[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }

            byte[] finalHtmlBytes = htmlBytes;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 5000; i++) {

                     //   int[] constructIndex = HTMLConstructAnalysis.whiteSpaceStartAndEndIndex(finalHtmlBytes);
                      //  Node root = HtmlNodeParse.parse(finalHtmlBytes, constructIndex);

                        String s = new String(finalHtmlBytes);
                        Document parse = Jsoup.parse(s);

                    }
                    countDownLatch.countDown();
                }
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endNanos_34_33 = System.nanoTime();
        System.out.println((endNanos_34_33 - startNanos_34_31) / (threadCount * 5000) / 1000000.0);
    }


}
