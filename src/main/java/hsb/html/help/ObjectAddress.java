package hsb.html.help;

import jdk.internal.misc.Unsafe;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import java.lang.invoke.MethodHandles;

import java.lang.invoke.VarHandle;
import java.lang.management.ManagementFactory;
import java.util.Arrays;


/**
 * @author 胡帅博
 * @date 2021/12/24 17:23
 * <p>
 * java中默认是开启指针压缩的，指针压缩是值用4字节来保存地址信息,所以对象头中CLass位置用4字节记录，对象的地址也是四字节
 * 关闭指针压缩的：  -XX:-UseCompressedOops
 * <p>
 * 指针压缩的具体计算和ObjectAlignmentInBytes参数有关，ObjectAlignmentInBytes是对齐方式，默认是8字节对齐
 * 指针压缩后的地址是: 实际地址/8
 * 4字节地址的寻址空间是32G （4G * 8）
 * <p>
 * 举3个例子：
 * 如果添加jvm选项  -XX:ObjectAlignmentInBytes=32    32字节对齐
 * 指针压缩后的地址是: 实际地址/32
 * 4字节地址的寻址空间为144G (4G * 32)
 * <p>
 * 如果添加jvm选项  -XX:ObjectAlignmentInBytes=16    16字节对齐
 * 指针压缩后的地址是: 实际地址/16
 * 4字节地址的寻址空间为64G  (4G * 16)
 * <p>
 * 如果添加-XX:-UseCompressedOops参数，关闭了指针压缩,那么不管ObjectAlignmentInBytes设置的多少，保存的都是实际地址
 */
public class ObjectAddress {
    // private static VarHandle theUnsafe;
    public static Unsafe theUnsafe;

    //数组类型对象的 object[0] 所在位置和对象其实位置之间的偏移 ， 包括对象头，地址，数组长度
    private static int arrayObjectBase;

    // UseCompressedOops  是否开启指针压缩
    private static boolean compressedOopsEnabled;

    //narrowOopBase是通过addressOf(null)获取的 ,参考的jol-core，我也不知道啥意思
    private static long narrowOopBase;

    //根据 ObjectAlignmentInBytes参数计算
    private static int narrowOopShift;

    private static boolean inited = false;

    public static void main(String[] args) throws Exception {

        System.out.println(compressedOopsEnabled);
        System.out.println(narrowOopShift);
        System.out.println(arrayObjectBase);

        byte[][] bytes = new byte[10][];
        for (int i = 0; i < 10; i++) {
            bytes[i] = new byte[10];
        }
        for (int i = 0; i < 10; i++) {
            long address = addressOf(bytes[i]);
            System.out.println(address);
            System.out.println(Arrays.toString(bytes[i]));
        }

    }

    static {
        try {
            VarHandle theUnsafeVar = MethodHandles.privateLookupIn(jdk.internal.misc.Unsafe.class, MethodHandles.lookup())
                    .findStaticVarHandle(jdk.internal.misc.Unsafe.class, "theUnsafe", jdk.internal.misc.Unsafe.class);
            theUnsafe = (Unsafe) theUnsafeVar.get();
            arrayObjectBase = theUnsafe.arrayBaseOffset(byte[].class);

            compressedOopsEnabled = Boolean.valueOf(getString("UseCompressedOops"));
            Integer alignment = Integer.valueOf(getString("ObjectAlignmentInBytes"));
            narrowOopShift = Integer.numberOfTrailingZeros(alignment);  //因为ObjectAlignmentInBytes必须是2的幂次方，所以只包含1个1
            narrowOopBase = addressOf(null);
            inited = true;
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数组中指定偏移的数据，
     * @param address 地址
     * @param index 字节为单位的偏移量
     * @return 返回8字节内容
     *
     * */
    public static long getArrayData(long address,int index) {
        long longData = theUnsafe.getLong(address + arrayObjectBase + index);
        return longData;
    }
    /**
     * copy  jol-core中的实现
     */
    public static long addressOf(Object o) {
        if(!inited){
            return -1;
        }
        Object[] array = new Object[1];
        array[0] = o;
        long objectAddress;
        objectAddress = theUnsafe.getLong(array, arrayObjectBase);
        array[0] = null;
        return toNativeAddress(objectAddress);
    }


    private static long toNativeAddress(long address) {
        if (compressedOopsEnabled) {
            return narrowOopBase + (address << narrowOopShift);
        } else {
            return address;
        }
    }

    private static String getString(String key) throws Exception {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        ObjectName mbean = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
        CompositeDataSupport val = (CompositeDataSupport) server.invoke(mbean, "getVMOption", new Object[]{key}, new String[]{"java.lang.String"});
        return val.get("value").toString();
    }

    // 如果返回0，说明并不能用
    public static int getArrayObjectBase() {
        return arrayObjectBase;
    }

}
