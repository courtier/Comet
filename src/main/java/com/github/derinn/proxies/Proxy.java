package com.github.derinn.proxies;

import java.net.InetSocketAddress;
import java.util.Base64;

public class Proxy{

    private final String ip;
    private final int port;
    private InetSocketAddress inetSocketAddress;
    private String auth = "";

    public Proxy(String ip, int port){
        this.ip = ip;
        this.port = port;
        setInet();
    }

    private void setInet(){
        inetSocketAddress = new InetSocketAddress(ip, port);
    }

    public void setAuth(String username, String password){
        auth = new String(Base64.getEncoder().encode((username+":"+password).getBytes()));
    }

    public String getAuth(){
        return auth;
    }

    public InetSocketAddress getInetSocketAddress(){
        return inetSocketAddress;
    }
}
