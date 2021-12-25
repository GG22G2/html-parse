package hsb.html.help;

import jdk.incubator.foreign.*;
import jdk.internal.foreign.MemoryAddressImpl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.ByteOrder;


/**
 * @author 胡帅博
 * @date 2021/12/20 16:03
 */
public class HTMLConstructAnalysis {

    private static final String dllPath = "D:\\kaifa_environment\\code\\C\\html-parse-c\\x64\\Release\\html-parse-c.dll";

    private static MethodHandle analysisHTMLConstruct;
    private static MethodHandle aligned_free;
    private static boolean nativeMethod = false;

    static {

        try {
            System.load(dllPath);
            analysisHTMLConstruct = CLinker.getInstance().downcallHandle(
                    SymbolLookup.loaderLookup().lookup("analysisHTMLConstruct").get(),
                    MethodType.methodType(void.class, MemoryAddress.class, int.class, MemoryAddress.class),
                    FunctionDescriptor.ofVoid(CLinker.C_POINTER, CLinker.C_INT, CLinker.C_POINTER));
            aligned_free = CLinker.getInstance().downcallHandle(
                    SymbolLookup.loaderLookup().lookup("aligned_free").get(),
                    MethodType.methodType(void.class, MemoryAddress.class),
                    FunctionDescriptor.ofVoid(CLinker.C_POINTER));
            nativeMethod = true;
        } catch (Exception e) {
            Exception exception = new Exception("没有dll,使用java方式代替");
            exception.addSuppressed(exception);
            exception.printStackTrace();
        }
    }


    public static int[] whiteSpaceStartAndEndIndex(byte[] bytes) {
        if (nativeMethod) {
            return whiteSpaceStartAndEndIndex_Jni(bytes);
        }
        return whiteSpaceStartAndEndIndex_Java(bytes);
    }


    /**
     * 获取byte[]的地址，数据地址
     * <p>
     * 正常应该是，创建MemorySegment，然后把byte[]中的值拷贝进去，但是传递给C函数的只是这个段中数据的地址
     * 所有理论上,获取到byte[]中byte[0]的地址，也是可以直接传递给C函数的,但是这里不能保证地址一定是对的,最好能验证一下
     */
    public static MemoryAddress getParamsByteArrayAddress(byte[] bytes, ResourceScope scope) {
        long address = ObjectAddress.addressOf(bytes);
        MemoryAddress memoryAddress;
        if (address == -1) {
            MemorySegment htmlSegment = MemorySegment.allocateNative(bytes.length, scope);
            for (int i = 0; i < bytes.length; i++) {
                MemoryAccess.setByteAtOffset(htmlSegment, i, bytes[i]);
            }
            memoryAddress = htmlSegment.address();
        } else {
            memoryAddress = new MemoryAddressImpl(null, address + ObjectAddress.getArrayObjectBase());
        }
        return memoryAddress;
    }

    public static MemoryAddress getReturnIntArrayAddress(int[] arr, ResourceScope scope) {
        long address = ObjectAddress.addressOf(arr);
        MemoryAddress memoryAddress;
        if (address == -1) {
            MemorySegment htmlSegment = MemorySegment.allocateNative(4 * arr.length, scope);
            memoryAddress = htmlSegment.address();
        } else {
            memoryAddress = new MemoryAddressImpl(null, address + ObjectAddress.getArrayObjectBase());
        }
        return memoryAddress;
    }


    public static int[] whiteSpaceStartAndEndIndex_Jni(byte[] htmlBytes) {
        ResourceScope scope = ResourceScope.newConfinedScope();
        int[] result = new int[htmlBytes.length / 3];
        MemoryAddress htmlBytesAddress = getParamsByteArrayAddress(htmlBytes, scope);
        MemoryAddress resultIntsAddress = getReturnIntArrayAddress(result, scope);

        result[0] = -1;
        try {
            analysisHTMLConstruct.invokeExact(htmlBytesAddress, htmlBytes.length, resultIntsAddress);

            if (result[0] == -1) { //需要从内存段中取结果
                int intPointLength = MemoryAccess.getInt(resultIntsAddress.asSegment(4, scope), ByteOrder.nativeOrder());
                MemorySegment memorySegment1 = resultIntsAddress.asSegment(intPointLength * 4, scope);
                result = memorySegment1.toIntArray();
                aligned_free.invoke(resultIntsAddress);  //返回的数组是 dll中创建的，所以需要释放一下
            }
            scope.close();

            return result;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return new int[]{0};
    }

    public static int[] whiteSpaceStartAndEndIndex_Java(byte[] bytes) {
        //一帮构建结果是原长度的  1/5，这里使用bytes.length / 3一般不会有问题.
        int[] tempPosition = new int[bytes.length / 3];
        int continuousWhiteSpace = 0;
        int start = 0;

        int positionIndex = 1;
        for (int j = 0; j < bytes.length; j++) {
            byte b = bytes[j];
            if (b == '\0' || b == '\t' || b == '\n' || b == '\f' || b == '\r' || b == ' ') {
                if (continuousWhiteSpace == 0) {
                    start = j;
                }
                continuousWhiteSpace++;
            } else if (continuousWhiteSpace > 0) {
                continuousWhiteSpace = 0;
                if (positionIndex == 1) {
                    if (start > 0) {
                        tempPosition[positionIndex++] = start - 1;
                    }
                } else if (tempPosition[positionIndex - 1] != start - 1) {
                    tempPosition[positionIndex++] = start - 1;
                }

                tempPosition[positionIndex++] = j;
            }


            if (b == '=' || b == '\"' || b == '\'' || b == '>') {
                if (tempPosition[positionIndex - 1] < j)
                    tempPosition[positionIndex++] = j;
            }

            if (b == '<') {
                if (positionIndex == 1 || (tempPosition[positionIndex - 1] < j)) {
                    byte nextC = bytes[j + 1];
                    boolean f1 = nextC == '!' || nextC == '?' || nextC == '/';
                    boolean f2 = (nextC >= 'a' && nextC <= 'z') || (nextC >= 'A' && nextC <= 'Z');
                    if (f1 || f2) {
                        tempPosition[positionIndex++] = j;
                    }
                }

            }

        }

        //数组中第一个位置代表数组长度
        tempPosition[0] = positionIndex;
        return tempPosition;
    }


}
