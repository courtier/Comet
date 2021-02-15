package com.github.derinn.benchmark;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class ThreadedRequest implements Callable<Boolean>{

    private final HttpClient client;
    private final HttpRequest request;
    private final CountDownLatch countDownLatch;
    private final int num;


    public ThreadedRequest(HttpClient client, HttpRequest request, CountDownLatch countDownLatch, int num){
        this.client = client;
        this.request = request;
        this.countDownLatch = countDownLatch;
        this.num = num;
    }

    @Override
    public Boolean call() throws Exception{

        for(int i = 0; i < num; i++){
            client.send(request, HttpResponse.BodyHandlers.ofString());
        }
        countDownLatch.countDown();

        return false;
    }
}
