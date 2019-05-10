package com.test;

import com.xingtb.Person;
import com.xingtb.Tom;
import com.xingtb.annotation.ExcelTitle;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;

public class MyTest {
    @Test
    public void testPattern() {
        Pattern pattern = Pattern.compile("^[%|a-zA-Z0-9_-]+$");
        boolean b = pattern.matcher("demo_consumer_group").matches();
        System.out.println(b);
    }

    @Test
    public void testPattern02() {
        String s = " fl.。dj\n,das";
        String s1 = s.replaceAll("[.。,\\s]", "");
        System.out.println(s1);
    }

    @Test
    public void testAnnotation() {
        Class<Tom> tc = Tom.class;
        Field[] fields = tc.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            ExcelTitle annotation = f.getAnnotation(ExcelTitle.class);
            System.out.println(f.getName() + "---->" + "  annotation-value-->" + annotation.value() + "  annotation-name-->" + annotation.name());
        }
    }

    @Test
    public void BigDec() {
        Integer i = 0;
        BigDecimal value = BigDecimal.valueOf(i);
        System.out.println(value);
    }

    @Test
    public void test001() {
        String s = "哈,啊，哈,,  da".replaceAll(" ", "").replaceAll("[,，]+", "|");
        System.out.println(s);
        StringBuilder builder = new StringBuilder();
        List<Integer> list = new ArrayList<Integer>() {{
            add(1);
            add(2);
        }};
        list.forEach(x -> builder.append(String.valueOf(x)).append("|"));
        String substring = builder.substring(0, builder.length() - 1);
        System.out.println(substring);
    }

    @Test
    public void test002() {
        Float F = 1654456456466465464.566645645645645464644646464f;
        System.out.println(F);
        String f = "%1$s%2$s";
        System.out.println(String.format(f, "ab", "cd"));
    }

    @Test
    public void testBase64() {
        String s = Base64.getEncoder().encodeToString("工程报表-2018-12.xlsx".getBytes());
        System.out.println(s);
    }

    @Test
    public void testThreadPoolExecutor() {
        // new ThreadPoolExecutor();
    }
}
