package com.test;

import com.xingtb.Person;
import com.xingtb.annotation.ExcelTitle;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class TestForMysql {
    Lock lock = new ReentrantLock();

    @Test
    public void connect() throws ClassNotFoundException, SQLException {
        String JDBC_DRIVER = "com.mysql.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost:3306/test?serverTimezone=GMT%2B8";
        String USER = "admin";
        String PASS = "admin";
        Class.forName(JDBC_DRIVER);
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery("select * from test_tab");
        while (rs.next()) {
            String name = rs.getString("name");
            System.out.println(name);
        }
    }

    @Test
    public void testReflect() throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Person person = new Person("xingtb", "man", 175, 70.0F, "sport", "IT");
        testReflect(person);
    }

    private void testReflect(Object o) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class<?> c = o.getClass();
        Field[] fields = c.getDeclaredFields();
        Method method = c.getMethod("getFieldsAlias");
        Object o1 = c.newInstance();
        Map<String, String> map = (Map<String, String>) method.invoke(o1);
        for (Field f : fields) {
            f.setAccessible(true);
            ExcelTitle annotation = f.getAnnotation(ExcelTitle.class);
            System.out.println(f.getName() + "---->" + f.get(o) + "--->" + map.get(f.getName()) + "  annotation-value-->" + annotation.value() + "  annotation-name-->" + annotation.name());
        }
        System.out.println(map);
    }

    @Test
    public void testExcelTitle() {
        Field[] fields = Person.class.getDeclaredFields();
        for (Field f : fields) {
            f.setAccessible(true);
            ExcelTitle title = f.getAnnotation(ExcelTitle.class);
            System.out.println("value: " + title.value() + "  name: " + title.name());
        }
    }

    @Test
    public void testForByte() {
        Byte b = 0;
        System.out.println(b);
    }

    @Test
    public void testForMath() {
        float f = 2.1f;
        int rint = Math.round(f);
        int ceil = (int) Math.ceil(f);
        // List<Integer> integers = new ArrayList<Integer>();
        List<Float> percent = new ArrayList<Float>(){{add(10.0f); add(30.0f); add(50.4f); add(60.0f); add(75.0f);}};
        List<Integer> totalCount = new ArrayList<Integer>(){{add(100); add(200); add(300); add(400); add(450);}};
        System.out.println(rint);
        System.out.println(ceil);
        System.out.println((int) Math.ceil(Collections.max(percent)/10) * 10);
        // System.out.println(Collections.max(integers));
        System.out.println((int) Math.ceil((float)Collections.max(totalCount)/100) * 100);
    }

    @Test
    public void name() {
        // InputStream inputStream = new FileInputStream()
        SecureRandom random = new SecureRandom();
        BigInteger integer = new BigInteger(32, random);
        String s = integer.toString();
        System.out.println(s);
    }

    @Test
    public void testFineBI() throws ClassNotFoundException, SQLException, InterruptedException {
        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://192.168.2.233:3306/vip";
        String USER = "root";
        String PASS = "Jsb123456";
        Class.forName(JDBC_DRIVER);
        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
        Statement statement = conn.createStatement();
//        ResultSet rs = statement.executeQuery("select * from test_finebi");
//        while (rs.next()) {
//            String name = rs.getString("num");
//            System.out.println(name);
//        }
        for (int i = 0; i < 100; i++) {
            statement.execute("insert into test_finebi(num) values(" + i + ")");
            Thread.sleep(1000);
        }
    }
}
