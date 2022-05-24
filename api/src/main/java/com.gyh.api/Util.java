package com.gyh.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Util {
    private static final Random random = new Random();

    private Util() {
    }

    /**
     * 移除两个列表中所有相同的元素
     *
     * @param c1  一个列表
     * @param c2  另一个列表
     * @param <T> 元素的类型
     */
    public static <T> void removeAllSame(Collection<T> c1, Collection<T> c2) {
        c1.removeIf(c -> {
            if (c2.contains(c)) {
                c2.remove(c);
                return true;
            }
            return false;
        });
    }

    /**
     * 把4个byte字节转成一个4字节的int
     */
    public static int createInt(byte[] in, int start) {
        if (in.length < start + 4) {
            throw new IllegalArgumentException("输入数据不满足要求");
        }
        return ((0xFF & in[start]) << 24) |
                ((0xFF & in[start + 1]) << 16) |
                ((0xFF & in[start + 2]) << 8) |
                (0xFF & in[start + 3]);
    }

    public static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    /**
     * 将json格式化为map
     *
     * @param json
     * @return
     */
    public static Map<String, Object> getParameterMap(String json) {
        Map<String, Object> map = new HashMap<>();
        if (!isEmpty(json)) {
            try {
                map = new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * 按当前时间，按{@code yyyy-MM-dd HH:mm:ss}格式格式化一个时间字符串
     *
     * @return 格式化后的时间字符串
     */
    public static String createDate() {
        return createDate("yyyy-MM-dd HH:mm:ss");
    }

    public static String createDate(String pattern) {
        return createDate(pattern, System.currentTimeMillis());
    }

    public static String createDate(long time) {
        return createDate("yyyy-MM-dd HH:mm:ss", time);
    }

    public static String createDate(String pattern, long time) {
        return new SimpleDateFormat(pattern).format(time);
    }

    /**
     * 把格式化后的时间字符串解码成时间毫秒值
     *
     * @param time 格式化后的时间字符串
     * @return 时间毫秒值
     */
    public static Long encoderDate(String time) {
        return encoderDate("yyyy-MM-dd HH:mm:ss", time);
    }

    public static Long encoderDate(String pattern, String time) {
        try {
            return new SimpleDateFormat(pattern).parse(time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 用自己的方法判断{@code} element 是否在{@code} gather 里面出现过<p>
     *
     * @param fun     判断方法
     * @param gather  目标集合
     * @param element 要检查的集合
     * @param <T>     目标对象
     * @param <K>     检查对象
     * @return 如果 {@code} element 中的任何一个元素在{@code} gather 里面出现过就返回true 否则返回false
     */
    @SafeVarargs
    public static <T, K> boolean hasAny(BiFunction<T, K, Boolean> fun, List<T> gather, K... element) {
        for (T t : gather) {
            for (K k : element) {
                if (fun.apply(t, k)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T> boolean hasAny(T[] array, T t) {
        if (array == null || array.length == 0) {
            return false;
        }
        for (T index : array) {
            if (index.equals(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断一个（bean）实体类对象是否为空<p>
     * 判断为空的标准为：<P>
     * <ol>
     * <li>如果实体类的属性为{@link String}那么字符串长度为0或为null就认为为空</li>
     * <li>如果属性为{@link Collection}的子类那么集合的长度为0或为null就认为为空</li>
     * <li>如果属性不为上述的就为null才认为为空</li>
     * </ol>
     *
     * @param obj 一个实体类（bean）对象
     * @return true：如果该实体类的所有属性都为空，false：其中的任意一个属性不为空
     */
    public static boolean isEmpty(Object obj) {
        return isEmpty(obj, true);
    }

    /**
     * 判断一个（bean）实体类对象是否为空<p>
     * 非严格模式下判断为空的标准为：
     * 对象的属性是否为null<P>
     * 严格模式下判断为空的标准为：<P>
     * <ol>
     * <li>如果实体类的属性为{@link String}那么字符串长度为0或为null就认为为空</li>
     * <li>如果属性为{@link Collection}的子类那么集合的长度为0或为null就认为为空</li>
     * <li>如果属性不为上述的就为null才认为为空</li>
     * </ol>
     *
     * @param obj    一个实体类（bean）对象
     * @param strict 是否使用严格模式
     * @return true：如果该实体类的所有属性都为空，false：其中的任意一个属性不为空
     */
    public static boolean isEmpty(Object obj, boolean strict) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] proDescrtptors = beanInfo == null ? null : beanInfo.getPropertyDescriptors();
            if (proDescrtptors != null && proDescrtptors.length > 0) {
                for (PropertyDescriptor propDesc : proDescrtptors) {
                    Object o = propDesc.getReadMethod().invoke(obj);
                    if (o == null || o.equals(obj.getClass())) {
                        continue;
                    }
                    if (!strict) {
                        return false;
                    }
                    if (o instanceof String) {
                        if (!((String) o).isEmpty()) {
                            return false;
                        } else {
                            continue;
                        }
                    }
                    if (o instanceof Collection) {
                        if (!((Collection) o).isEmpty()) {
                            return false;
                        } else {
                            continue;
                        }
                    }
                    return false;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | IntrospectionException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 获取指定元素在指定列表中的排名情况
     *
     * @param objects    列表
     * @param tt         需要排名的元素
     * @param comparator 排名规则
     * @param <T>        列表元素类型
     * @return 指定元素的排名
     */
    public static <T> int getRank(List<T> objects, T tt, Comparator<? super T> comparator) {
        int rank = 1;
        for (T t : objects) {
            if (comparator.compare(t, tt) > 0) {
                rank++;
            }
        }
        return rank;
    }

    /**
     * 判断一个（bean）实体类对象的属性是否全为null<p>
     * 该方法没有对基本数据类型做处理
     *
     * @param obj 一个实体类（bean）对象
     * @return true：如果该实体类的所有属性都为空，false：其中的任意一个属性不为空
     */
    public static boolean isNull(Object obj) {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            PropertyDescriptor[] proDescrtptors = beanInfo == null ? null : beanInfo.getPropertyDescriptors();
            if (proDescrtptors != null && proDescrtptors.length > 0) {
                for (PropertyDescriptor propDesc : proDescrtptors) {
                    Object o = propDesc.getReadMethod().invoke(obj);
                    if (o == null) {
                        continue;
                    }
                    return false;
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | IntrospectionException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 用自定义的方法在{@code} collection 中查找特定的元素<p>
     * 该方法适用于集合内元素与条件元素不一致的情况
     *
     * @param collection 原始集合
     * @param consumer   自定义方法
     * @param <T>        集合元素类型
     * @return 满足自定义条件的元素，如果都不满足就放回null
     */
    public static <T> T find(Collection<T> collection, Function<T, Boolean> consumer) {
        for (T t : collection) {
            if (consumer.apply(t)) {
                return t;
            }
        }
        return null;
    }

    /**
     * 把一个数组转换成另一个类型的数组
     *
     * @param array    原数组
     * @param newType  要返回的新数组的类
     * @param consumer 转换方法
     * @param <T>      源数组的类型
     * @param <K>      新数组的类型
     * @return 一个转换后的新类型数组
     */
    public static <T, K> K[] convert(T[] array, Class<? extends K> newType, Function<T, K> consumer) {
        @SuppressWarnings("unchecked")
        K[] copy = (newType == Object[].class) ? (K[]) new Object[array.length]
                : (K[]) Array.newInstance(newType, array.length);
        for (int i = 0; i < array.length; i++) {
            copy[i] = consumer.apply(array[i]);
        }
        return copy;
    }

    /**
     * 驼峰命名转下划线命名
     *
     * @param para 驼峰命名的字符串
     * @return 下划线命名的字符串
     */
    public static String HumpToUnderline(String para) {
        StringBuilder sb = new StringBuilder(para);
        int temp = 0;//定位
        if (!para.contains("_")) {
            for (int i = 0; i < para.length(); i++) {
                if (Character.isUpperCase(para.charAt(i))) {
                    sb.insert(i + temp, "_");
                    temp += 1;
                }
            }
        }
        return sb.toString();
    }

    /**
     * 随机生成指定长度的数字验证码
     *
     * @param length 数字验证码长度
     * @return 数字验证码
     */
    public static String getRandomInt(int length) {
        int max = 1;
        for (int i = 0; i < length; i++) {
            max *= 10;
        }
        StringBuilder nextInt = new StringBuilder(String.valueOf(random.nextInt(max)));
        while (nextInt.length() < length) {
            nextInt.insert(0, '0');
        }
        return nextInt.toString();
    }

    /**
     * 构建数字验证码，补齐数字的长度（用0补齐），适用于一些自增的验证码
     * @param accessCode 验证码
     * @param length 想要的长度
     * @return 数字字符串验证码
     */
    public static String buildAccessCode(int accessCode, int length) {
        StringBuilder nextInt = new StringBuilder(String.valueOf(accessCode));
        while (nextInt.length() < length) {
            nextInt.insert(0, '0');
        }
        return nextInt.toString();
    }

    public static String buildcode(int accessCode, int length) {
        String code = buildAccessCode(accessCode, 4);
        return getRandomInt(10) + code + getRandomInt(10);
    }

}
