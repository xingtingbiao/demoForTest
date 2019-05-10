package com.xingtb;

import com.xingtb.annotation.ExcelTitle;

import java.util.HashMap;
import java.util.Map;

public class Person {
    @ExcelTitle(value = "姓名")
    private String name = "姓名";

    @ExcelTitle(value = "性别")
    private String sex = "性别";

    @ExcelTitle(value = "身高")
    private int height;

    @ExcelTitle(value = "体重")
    private float weight;

    @ExcelTitle(value = "兴趣")
    private String fun;

    @ExcelTitle(name = "工作")
    private String work;

    public Person() {
    }

    public Person(String name, String sex, int height, float weight, String fun, String work) {
        this.name = name;
        this.sex = sex;
        this.height = height;
        this.weight = weight;
        this.fun = fun;
        this.work = work;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getFun() {
        return fun;
    }

    public void setFun(String fun) {
        this.fun = fun;
    }

    public String getWork() {
        return work;
    }

    public void setWork(String work) {
        this.work = work;
    }

    public Map<String, String> getFieldsAlias() {
        return new HashMap<String, String>() {{
            put("name", "姓名");
            put("sex", "性别");
            put("height", "身高");
            put("weight", "体重");
            put("fun", "兴趣");
            put("work", "工作");
        }};
        // return new ArrayList<String>(){{add("姓名");add("性别");add("身高");add("体重");add("兴趣");add("工作");}};
    }
}
