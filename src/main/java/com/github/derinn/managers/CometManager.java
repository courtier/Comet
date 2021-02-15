package com.github.derinn.managers;

import com.github.derinn.account.Account;
import com.github.derinn.account.AccountHandler;
import com.github.derinn.proxies.ProxyManager;
import com.github.derinn.sniper.Sniper;
import com.github.derinn.util.Logger;
import com.github.derinn.util.StopProgram;
import com.github.derinn.util.time.MojangPinger;
import com.github.derinn.util.time.TimeAPI;

import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class CometManager implements Runnable{

    //each name sniped will be one of this class

    private final String desiredName;
    private final int id;
    private final long snipeDelayRaw;
    private final int accounts;

    HashMap<Instant, Boolean> responseCounterList = new HashMap<>();
    HashMap<Instant, Instant> requestCounterList = new HashMap<>();
    HashMap<Account, HashMap<HttpResponse<String>, Instant>> allResponseList = new HashMap<>();
    private ExecutorService spamDelayService;
    private ScheduledExecutorService scheduledExecutorService;
    private Instant dropTime;
    private Account snipedAccount = null;
    private Instant snipedInstant = null;
    private boolean shutdown = false;
    private Instant unblockTime;

    public AtomicLong snipeDelay;

    public CometManager(int id, String desiredName, long snipeDelay, int accounts){
        this.desiredName = desiredName;
        this.id = id;
        this.snipeDelayRaw = snipeDelay;
        this.accounts = accounts;
    }

    @Override
    public void run(){

        if(!GlobalSettings.isTurboMode()){
            //dropTime = NamemcScraper.getDropTime(desiredName);
            dropTime = TimeAPI.getNXDropTime(desiredName);
        }else{
            dropTime = getUnblockTime();
        }
        if(dropTime == null){
            Logger.logError("Drop time returned null.");
            StopProgram.gracefulQuit();
            return;
        }

        long secondsToDrop = -Instant.now().minusMillis(dropTime.toEpochMilli()).getEpochSecond();
        Logger.logInfo("Name dropping in " + secondsToDrop + " seconds");

        Logger.logInfo("Loading accounts...");

        AccountHandler.loadAccounts(accounts);

        if(GlobalSettings.isUseProxy()){
           /* Logger.logInfo("Loading proxies...");
            ProxyManager.loadProxies(accounts);*/
            Logger.logInfo("Mapping proxies to accounts...");
            ProxyManager.mapProxiesToAccounts();
            Logger.logInfo("Proxies are locked and loaded.");
            System.out.println();
        }

        Logger.logInfo("Authenticating accounts 40 seconds before drop");

        System.out.println();

        checkTime();

        Logger.logDebug("Getting local time offset");
        GlobalSettings.setLocalClockOffset(TimeAPI.getOffset());
        Logger.logCustom("/b/[INFO] Local time offset: /c/" + GlobalSettings.getLocalClockOffset());

    }

    private void checkTime(){

        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        spamDelayService = Executors.newFixedThreadPool(1);

        snipeDelay = new AtomicLong(snipeDelayRaw);
        if(GlobalSettings.isWarmUp()){
            snipeDelay.getAndAdd(50);
        }

        if(GlobalSettings.isPingMojang()){
            ExecutorService pingService = Executors.newFixedThreadPool(1);
            Runnable pingMojang = () -> {
                long ping = MojangPinger.getPing();
                snipeDelay.getAndAdd(ping);
                Logger.logCustom("/b/[INFO] Ping to Mojang is: /c/" + ping);
                pingService.shutdownNow();
            };
            pingService.execute(pingMojang);
        }

        snipeDelay.getAndAdd(-(GlobalSettings.getLocalClockOffset()));

        Runnable checkSpam = () -> {

            for(; ; ){

                if(dropTime.toEpochMilli() - Instant.now().toEpochMilli() < snipeDelay.get()){

                    //spam
                    Sniper.handleSpam(id);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss.SSSSS");
                    Logger.logDebug("Sniper started  @ " + simpleDateFormat.format(Date.from(Instant.now())) + " + drop is @ " + simpleDateFormat.format(Date.from(dropTime)));
                    Logger.logDebug("Drop   @ " + dropTime.toEpochMilli());
                    Logger.logDebug("Start  @ " + Instant.now().toEpochMilli());
                    Logger.logDebug("Delay  : " + snipeDelayRaw);
                    spamDelayService.shutdownNow();
                    break;

                }

            }

        };

        AtomicBoolean hasAuthorizedAccounts = new AtomicBoolean(false);

        long startCheckingDrop = 8000 + snipeDelayRaw;
        //long authAccounts = 40 * 1000;
        AtomicLong authDelay = new AtomicLong(GlobalSettings.getAuthDelay() * 1000);
        if(authDelay.get() < 40){
            authDelay.set(4000);
        }

        Runnable calculateTime = () -> {

            long diff = dropTime.toEpochMilli() - Instant.now().toEpochMilli();

            if(diff < authDelay.get() && diff < startCheckingDrop){

                spamDelayService.execute(checkSpam);

            }

            if(!hasAuthorizedAccounts.get() && diff < authDelay.get()){

                //authorize
                //Logger.logInfo("Authorizing " + AccountHandler.accountList.size() + " accounts.");
                AccountHandler.authorizeAccounts();
                spamDelayService.execute(checkSpam);
                scheduledExecutorService.shutdownNow();

            }

        };

        scheduledExecutorService.scheduleAtFixedRate(calculateTime, 0, 1, TimeUnit.SECONDS);

    }

    public void endThread(){
        if(GlobalSettings.isSaveLogs()){
            Logger.logInfo("Saving logs");
            Logger.saveLogsToFile(desiredName);
            System.out.println();
        }
        Logger.logInfo("Ending Comet thread.");
        shutdown = true;
        if(GlobalSettings.getCometManagers().size() - 1 == 0){
            Logger.logInfo("Last thread is finished.");
            System.out.println();
            if(GlobalSettings.isRemoteMode() || GlobalSettings.isKillProcess()){
                StopProgram.gracefulQuit();
            }else{
                StopProgram.consensualQuit();
            }
        }else{
            GlobalSettings.getCometManagers().remove(this.id);
            Thread.currentThread().interrupt();
        }
    }

    public Instant getDropTime(){
        return dropTime;
    }

    public String getDesiredName(){
        return desiredName;
    }

    public HashMap<Instant, Boolean> getResponseCounterList(){
        return responseCounterList;
    }

    public HashMap<Instant, Instant> getRequestCounterList(){
        return requestCounterList;
    }

    public Account getSnipedAccount(){
        return snipedAccount;
    }

    public void setSnipedAccount(Account snipedAccount){
        this.snipedAccount = snipedAccount;
    }

    public Instant getSnipedInstant(){
        return snipedInstant;
    }

    public void setSnipedInstant(Instant snipedInstant){
        this.snipedInstant = snipedInstant;
    }

    public HashMap<Account, HashMap<HttpResponse<String>, Instant>> getAllResponseList(){
        return allResponseList;
    }

    public boolean isShutdown(){
        return !shutdown;
    }

    public Instant getUnblockTime(){
        return unblockTime;
    }

    public void setUnblockTime(Instant unblockTime){
        this.unblockTime = unblockTime;
    }

    public long getSnipeDelayRaw(){
        return snipeDelayRaw;
    }
}
