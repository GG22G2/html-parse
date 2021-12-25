package hsb;

import hsb.html.help.HTMLConstructAnalysis;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class ConstructAnalysisTest {


    @Test
    public void valid() {
        System.out.println(1);
        try {
            String[] htmlFiles = new String[]{
                    "C:\\Users\\h6706\\Desktop\\az0vn-uwu6s.html"
                    , "C:\\Users\\h6706\\Desktop\\123.html"
                    , "G:\\kaifa_environment\\code\\java\\spiderManage\\src\\main\\resources\\static\\1.html"
            };
            for (String htmlFile : htmlFiles) {
                byte[] t = FileUtils.readFileToByteArray(new File(htmlFile));
                boolean match = validAnalysis(t);
                if (match) {
                    System.out.println("验证成功,文档: " + htmlFile);
                } else {
                    System.out.println("验证失败,文档: " + htmlFile);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean validAnalysis(byte[] htmlBytes) {
        int[] javaResult = HTMLConstructAnalysis.whiteSpaceStartAndEndIndex_Java(htmlBytes);
        int[] dllResult = HTMLConstructAnalysis.whiteSpaceStartAndEndIndex_Jni(htmlBytes);

        int l1 = javaResult[0];
        int l2 = dllResult[0];

        if (l1 != l2) {
            return false;
        }

        for (int i = 0; i < l1; i++) {
            if (javaResult[i] != dllResult[i]) {
                System.out.println(i);
                return false;
            }

        }

        return true;
    }
}
