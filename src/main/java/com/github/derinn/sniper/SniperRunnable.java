package com.github.derinn.sniper;

import com.github.derinn.account.Account;
import com.github.derinn.managers.GlobalSettings;
import com.github.derinn.proxies.Proxy;
import com.github.derinn.util.Logger;

import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class SniperRunnable implements Runnable{

    private final int reqAmount;
    private final Account account;
    private final int cometManagerID;
    private final CountDownLatch countDownLatch;

    SniperRunnable(int reqAmount, Account account, int cometManagerID, CountDownLatch countDownLatch){
        this.reqAmount = reqAmount;
        this.account = account;
        this.cometManagerID = cometManagerID;
        this.countDownLatch = countDownLatch;
    }

    public void run(){

        try{

            String url;
            HttpRequest request;
            String bearerHeader = "Bearer " + account.getBearer();

            String encoded = "";

            HttpClient client;

            if(GlobalSettings.isUseProxy() && GlobalSettings.accountsProxies.get(account) != null){
                Proxy proxy = GlobalSettings.accountsProxies.get(account);
                client = HttpClient.newBuilder()
                        .proxy(ProxySelector.of(proxy.getInetSocketAddress()))
                        .version(HttpClient.Version.HTTP_1_1).build();
                encoded = proxy.getAuth();
            }else{
                client = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_1_1).build();
            }

            long timeout = 10000;

            url = "https://api.minecraftservices.com/minecraft/profile/name/" + GlobalSettings.getCometManagers().get(cometManagerID).getDesiredName();

            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeout))
                    .header("Content-Type", "application/json")
                    .header("Authorization", bearerHeader)
                    .header("Proxy-Authorization", "Basic " + encoded)
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .build();

            if(GlobalSettings.isWarmUp()){

                HttpRequest dummyReq = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofNanos(1))
                        .header("Content-Type", "application/json")
                        .header("Proxy-Authorization", "Basic " + encoded)
                        .POST(HttpRequest.BodyPublishers.ofString(""))
                        .build();

                for(int i = 0; i < 3; i++){

                    client.sendAsync(dummyReq, HttpResponse.BodyHandlers.ofString());

                }
            }

            try{

                Instant before = Instant.now();

                long cooldown = GlobalSettings.getSpeedCap();

                for(int i = 0; i < reqAmount; i++){

                    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                            .thenApplyAsync(response -> {

                                Instant now = Instant.now();

                                try{

                                    int statusCode = response.statusCode();

                                    //Instant now = Instant.now();
                                    Instant dropTime = GlobalSettings.getCometManagers().get(cometManagerID).getDropTime();
                                    long diff = dropTime.toEpochMilli() - now.toEpochMilli();
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");
                                    Date nowDate = Date.from(now);
                                    String message;
                                    if(statusCode == 200){
                                        message = "/g/[" + statusCode + "] Success response on name /y/" + GlobalSettings.getCometManagers().get(cometManagerID).getDesiredName() + " /g/with account /bb/" + account.getName() + ". /c/Time: /m/" + simpleDateFormat.format(nowDate) + "/rs/";
                                        Logger.logCustom(message);
                                        GlobalSettings.getCometManagers().get(cometManagerID).getResponseCounterList().put(now, true);
                                        GlobalSettings.getCometManagers().get(cometManagerID).setSnipedAccount(account);
                                        GlobalSettings.getCometManagers().get(cometManagerID).setSnipedInstant(now);
                                        Logger.logDebug("Sniped " + GlobalSettings.getCometManagers().get(cometManagerID).getDesiredName() + " with account " + account.getName() + " @ " + simpleDateFormat.format(nowDate) + " - Diff: " + diff);
                                        Logger.logDebug("Drop   @ " + dropTime.toEpochMilli());
                                        Logger.logDebug("Sniped @ " + now.toEpochMilli());
                                        Logger.logDebug("Drop and snipe difference: " + (diff));
                                    }else{
                                        String prefixColor = "/br/";
                                        GlobalSettings.getCometManagers().get(cometManagerID).getResponseCounterList().put(now, false);
                                        message = prefixColor + "[" + statusCode + "] Bad response. /c/Time: /m/" + simpleDateFormat.format(nowDate) + "/rs/";
                                        Logger.logCustom(message);
                                    }

                                    if(!GlobalSettings.getCometManagers().get(cometManagerID).getAllResponseList().containsKey(account)){
                                        GlobalSettings.getCometManagers().get(cometManagerID).getAllResponseList().put(account, new HashMap<>());
                                    }

                                    GlobalSettings.getCometManagers().get(cometManagerID).getAllResponseList().get(account).put(response, now);

                                }catch(Exception ex){
                                    Logger.logError("Exception occurred while receiving response.", ex);
                                }

                                countDownLatch.countDown();
                                return response;

                            });

                    Thread.sleep(cooldown);

                }

                GlobalSettings.getCometManagers().get(cometManagerID).getRequestCounterList().put(before, Instant.now());

            }catch(Exception ex){
                Logger.logError("Exception occurred.", ex);
            }

        }catch(Exception ex){
            Logger.logError("Exception occurred.", ex);
        }

    }

}
