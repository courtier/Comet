package com.github.derinn.sniper;

import com.github.derinn.account.AccountHandler;
import com.github.derinn.discord.WebhookTing;
import com.github.derinn.managers.CometManager;
import com.github.derinn.managers.GlobalSettings;
import com.github.derinn.util.Counter;
import com.github.derinn.util.Logger;
import com.github.derinn.util.StopProgram;
import com.github.derinn.util.file.SuccessfulSnipeFileThing;
import com.github.derinn.util.mojang.UpdateSkin;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Sniper{

    public static void handleSpam(int cometManagerID){

        int reqAmount = GlobalSettings.getReqsPerAccount();

        int totalReqs = AccountHandler.accountList.size() * reqAmount;
        CountDownLatch countDownLatch = new CountDownLatch(totalReqs);
        try{

            System.out.println();

            //ThreadFactory virtualThreadFactory = Thread.builder().virtual().factory();
            for(int i = 0; i < AccountHandler.accountList.size(); i++){

                //java 16 thing, do not uncomment
                /*Thread virtualThread = virtualThreadFactory.newThread(new SniperRunnable(i, reqAmount, AccountHandler.accountList.get(i), cometManagerID, countDownLatch));
                virtualThread.start();*/
                AtomicInteger atomicI = new AtomicInteger(i);
                AtomicInteger atomicReqs = new AtomicInteger(reqAmount);
                CompletableFuture.runAsync(() -> {
                    SniperRunnable sniperRunnable = new SniperRunnable(atomicReqs.get(), AccountHandler.accountList.get(atomicI.get()), cometManagerID, countDownLatch);
                    sniperRunnable.run();
                });
            }

            CometManager cometManager = GlobalSettings.getCometManagers().get(cometManagerID);

            long awaitTime = cometManager.getSnipeDelayRaw() + 8000;
            countDownLatch.await(awaitTime, TimeUnit.MILLISECONDS);

            System.out.println();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

            //cometManager.setDropTime(cometManager.getDropTime().plusMillis(GlobalSettings.getLocalClockOffset()));

            if(cometManager.getResponseCounterList().containsValue(true)){

                //Logger.logSuccess("Sniped " + cometManager.getDesiredName() + ". Time: " + simpleDateFormat.format(Date.from(cometManager.getResponseCounterList())));
                Date snipedDate = Date.from(cometManager.getSnipedInstant());
                Logger.logCustom("/g/[SUCCESS] Sniped " + cometManager.getDesiredName() + ". Time: /c/" + simpleDateFormat.format(snipedDate));

                if(cometManager.getSnipedAccount() != null){

                    SuccessfulSnipeFileThing.saveToFile(cometManager.getSnipedAccount(), cometManager.getDesiredName());
                    if(GlobalSettings.getChangeSkin() != null){
                        Logger.logInfo("Changing skin for account " + cometManager.getDesiredName() + ".");
                        UpdateSkin.updateSkin(cometManager.getSnipedAccount());
                    }

                    if(GlobalSettings.isSendWebhook() && WebhookTing.sendSnipeHook(cometManager.getDesiredName(), cometManager.getSnipedAccount(), cometManagerID)){
                        Logger.logSuccess("Sent webhook.");
                    }

                }

            }else{
                Logger.logError("Couldn't snipe " + cometManager.getDesiredName() + ".");
            }

            System.out.println();

            if(cometManager.getResponseCounterList() == null || cometManager.getResponseCounterList().keySet().isEmpty() || cometManager.getResponseCounterList().containsKey(null)){
                Logger.logError("Couldn't get any responses.");
                System.out.println();
                StopProgram.gracefulQuit();
            }

            SortedSet<Instant> results = new TreeSet<>(cometManager.getResponseCounterList().keySet());

            int totalResponses = cometManager.getResponseCounterList().size();
            long diff = results.last().toEpochMilli() - results.first().toEpochMilli();
            double responsesPerSec = ((double) totalResponses / diff) * 1000;

            Date firstReqDate = Date.from(results.first());
            Date lastReqDate = Date.from(results.last());

            Logger.logCustom("/b/[INFO] Drop: /c/" + simpleDateFormat.format(Date.from(cometManager.getDropTime())));
            Logger.logCustom("/b/[INFO] Delay: /y/" + cometManager.snipeDelay.get());
            Logger.logCustom("/b/[INFO] Time taken: /y/" + diff + "ms");
            if(cometManager.getSnipedAccount() != null){
                Logger.logCustom("/b/[INFO] Snipe: /c/" + simpleDateFormat.format(Date.from(cometManager.getSnipedInstant())));
            }
            Logger.logCustom("/b/[INFO] First response: /c/" + simpleDateFormat.format(firstReqDate) + "/b/ - Last response: /c/" + simpleDateFormat.format(lastReqDate));
            Logger.logCustom("/b/[INFO] Responses/second: /y/" + responsesPerSec + "/b/ - Total responses: /y/" + totalResponses);

            AtomicLong requestDiffs = new AtomicLong(0);

            for(Instant before : cometManager.getRequestCounterList().keySet()){
                if(before != null){
                    if(cometManager.getRequestCounterList().get(before) != null){
                        requestDiffs.getAndAdd(cometManager.getRequestCounterList().get(before).toEpochMilli() - before.toEpochMilli());
                    }
                }
            }

            int totalRequests = cometManager.getRequestCounterList().keySet().size() * reqAmount;
            double requestsPerSec = ((double) totalRequests / requestDiffs.get()) * 1000;
            Logger.logCustom("/b/[INFO] Requests/second: /y/" + requestsPerSec + "/b/ - Total requests: /y/" + totalRequests);

            SortedSet<Instant> requestBeforeResults = new TreeSet<>(cometManager.getRequestCounterList().keySet());
            SortedSet<Instant> requestAfterResults = new TreeSet<>(cometManager.getRequestCounterList().values());
            Instant earliest = requestBeforeResults.first();
            Instant latest = requestAfterResults.last();

            Logger.logCustom("/b/[INFO] First request: /c/" + simpleDateFormat.format(Date.from(earliest)) + "/b/ - Last request: /c/" + simpleDateFormat.format(Date.from(latest)));

            ArrayList<Integer> responses = new ArrayList<>();
            cometManager.getAllResponseList().keySet().forEach(account -> cometManager.getAllResponseList().get(account).keySet().forEach(response -> {
                if(response != null){
                    responses.add(response.statusCode());
                }
            }));

            Logger.logCustom(Counter.buildCounterMessage(responses));

            System.out.println();

            if(GlobalSettings.isRemoteMode() || GlobalSettings.isKillProcess()){
                cometManager.endThread();
            }

            Logger.logInput("See all responses (y/n or enter for yes) -> ");
            String choice = new Scanner(System.in).nextLine();
            if(choice.length() > 0){
                if(choice.startsWith("n")){
                    System.out.println();
                    cometManager.endThread();
                    return;
                }
            }

            System.out.println();

            cometManager.getAllResponseList().keySet().forEach(account -> cometManager.getAllResponseList().get(account).keySet().forEach(response -> {
                String message;
                int statusCode = response.statusCode();
                if(statusCode == 200){
                    message = "/g/[" + statusCode + "] Success response. /b/Account: /y/" + account.getName()
                            + " /b/-> Time: /y/" + simpleDateFormat.format(Date.from(cometManager.getAllResponseList().get(account).get(response)));
                }else{
                    String prefixColor = "/br/";
                    String errorMessage = "Nothing.";
                    String body = response.body().replaceAll("\n", "");
                    if(!body.contains("errorType")){
                        errorMessage = "Rate limited.";
                    }else{
                        if(body.contains("DUPLICATE")){
                            errorMessage = "Missed drop.";
                        }else if(body.contains("TooManyRequestsException")){
                            errorMessage = "Rate limited.";
                        }else if(body.contains("UnauthorizedOperationException")){
                            errorMessage = "Unauthorized.";
                        }else if(body.contains("CONSTRAINT")){
                            errorMessage = "Bad request.";
                        }
                    }
                    Logger.logDebug(body);
                    message = prefixColor + "[" + statusCode + "] Bad response. /b/Account: /y/" + account.getName()
                            + " /b/-> Error: /y/" + errorMessage
                            + " /b/-> Time: /y/" + simpleDateFormat.format(Date.from(cometManager.getAllResponseList().get(account).get(response)));
                }
                Logger.logCustom(message);

            }));

            System.out.println();

            cometManager.endThread();

        }catch(Exception ex){
            Logger.logError("Exception occurred while handling spam.", ex);
        }

    }

}
