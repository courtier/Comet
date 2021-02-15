package com.github.derinn.proxies;

import com.github.derinn.util.Logger;

import java.io.IOException;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

public class ProxyPinger{

    public static long pingProxyDifference(){

        String url = "https://api.minecraftservices.com/minecraft/profile/name/";
        long averageDiff = 0;
        long counter = 0;

        for(Proxy proxy : ProxyManager.proxyList){

            HttpClient client;

            client = HttpClient.newBuilder()
                    .proxy(ProxySelector.of(proxy.getInetSocketAddress()))
                    .version(HttpClient.Version.HTTP_1_1).build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(5000))
                    .header("Proxy-Authorization", "Basic " + proxy.getAuth())
                    .GET()
                    .build();

            try{
                Instant before = Instant.now();
                client.send(request, HttpResponse.BodyHandlers.ofString());
                Instant after = Instant.now();
                long diff = after.toEpochMilli() - before.toEpochMilli();
                Logger.logDebug("Proxy responded in " + diff + " millis");
                counter++;
                averageDiff += diff;
                Logger.logDebug("Average response time is now: " + (averageDiff/counter));
            }catch(IOException | InterruptedException exception){
                Logger.logError("Error while testing proxy.");
            }

        }

        averageDiff = averageDiff/counter;
        Logger.logSuccess("Average response time is: " + averageDiff);
        Logger.logInfo("Testing ping without proxies.");

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1).build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(5000))
                .GET()
                .build();

        long averageNormalDiff = 0;
        long normalCounter = 0;

        try{
            for(int i = 0; i < counter; i++){
                Instant before = Instant.now();
                client.send(request, HttpResponse.BodyHandlers.ofString());
                Instant after = Instant.now();
                long diff = after.toEpochMilli() - before.toEpochMilli();
                Logger.logDebug("Responded in " + diff + " millis");
                normalCounter++;
                averageNormalDiff += diff;
                Logger.logDebug("Average response time is now: " + (averageNormalDiff/counter));
            }
        }catch(IOException | InterruptedException exception){
            Logger.logError("Error while testing without proxy.");
        }

        averageNormalDiff /= normalCounter;
        Logger.logSuccess("Average response time without proxies is: " + averageNormalDiff);

        System.out.println();

        return (averageDiff-averageNormalDiff);

    }

}
