package com.github.derinn.account;

import java.util.ArrayList;

public class Account{

    private final String name;
    private final String password;
    private final ArrayList<String> secQuestions = new ArrayList<>();
    private String bearer, uuid;

    Account(String name, String password){
        this.name = name;
        this.password = password;
    }

    public ArrayList<String> getSecQuestions(){
        return secQuestions;
    }

    public String getName(){
        return name;
    }

    public String getPassword(){
        return password;
    }

    public String getBearer(){
        return bearer;
    }

    void setBearer(String bearer){
        this.bearer = bearer;
    }

    public String getUuid(){
        return uuid;
    }

    void setUuid(String uuid){
        this.uuid = uuid;
    }

}
