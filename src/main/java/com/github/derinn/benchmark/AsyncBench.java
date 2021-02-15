package com.github.derinn.benchmark;

import com.github.derinn.util.StopProgram;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

public class AsyncBench{

    public static void main(String[] args){

        HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();

        int reqs = 2000;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://httpstat.us/200"))
                .timeout(Duration.ofSeconds(1))
                .GET()
                .build();

        Instant before = Instant.now();

        for(int i = 0; i < reqs; i++){

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        }

        /*Instant beforeWarm = Instant.now();

        for(int i = 0; i < 3; i++){

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        }

        Instant afterWarm = Instant.now();

        System.out.println(afterWarm.toEpochMilli() - beforeWarm.toEpochMilli());*/

        /*Instant before = Instant.now();

        List<Callable<Boolean>> threads = Collections.synchronizedList(new ArrayList<>());

        int threadnum = reqs / 1000;

        CountDownLatch countDownLatch = new CountDownLatch(threadnum);

        for(int i = 0; i < threadnum; i++){

            ThreadedRequest threadedRequest = new ThreadedRequest(client, request, countDownLatch,1000);
            threads.add(threadedRequest);
            //client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        }

        ExecutorService executorService = Executors.newFixedThreadPool(threadnum);
        try{
            System.out.println("start");
            executorService.invokeAll(threads);
            countDownLatch.await();
        }catch(InterruptedException e){
            e.printStackTrace();
        }*/

        Instant after = Instant.now();

        long diff = after.toEpochMilli() - before.toEpochMilli();
        double reqsPerSec = ((double) reqs / diff) * 1000;
        System.out.println(reqs + " requests -> " + diff + "ms | R/s: " + reqsPerSec);

        StopProgram.gracefulQuit();

    }

}
