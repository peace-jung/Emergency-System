package com.dexafree.materiallistviewexample;

/**
 * Created by peace-win10 on 2017-05-30.
 */

public class Person {
    private String userID;
    private String name;
    private String age;
    private String sex;
    private String phone;
    private String address;
    //private String disease;

    public String getUserID() {
        return userID;
    }
    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }
    public void setAge(String age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /*public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }*/

    /*public String getDisease() {
        return disease;
    }
    public void setDisease(String disease) {
        this.disease = disease;
    }*/

    @Override
    public String toString() {
        return "Person [name=" + name + ", age=" + age + ", sex=" + sex + ", phone=" + phone/* + ", address=" + address + ", disease=" + disease  */+ "]";
    }
}