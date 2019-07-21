package com.yk.bean;

/**
 * 附带权限枚举类
 */
public class User {

    private int id;
    private String name;
    private String authType;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

//    public enum AuthType{
//        ALARM("alarm"), TOPICWORD("topicword"), ALL("all"),VOICE("voice");
//
//        String authType;
//        AuthType(String authType) {
//            this.authType = authType;
//        }
//        public String getAuthType(){
//            return this.authType;
//        }
//    }



}
