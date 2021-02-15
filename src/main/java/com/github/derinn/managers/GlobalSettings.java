package com.github.derinn.managers;

import com.github.derinn.account.Account;
import com.github.derinn.proxies.Proxy;

import java.util.HashMap;

public class GlobalSettings{

    public static final HashMap<Integer, CometManager> cometManagers = new HashMap<>();

    public static final HashMap<Account, Proxy> accountsProxies = new HashMap<>();

    private static boolean debugMode, autoMode, warmUp,
            sendWebhook, pingMojang, turboMode,
            remoteMode, saveLogs, killProcess,
            useNamemcProxy, useProxy;

    private static int slowAuthDelay, authDelay;

    private static long localClockOffset, speedCap;
    private static int reqsPerAccount;

    private static String changeSkin, webhookThumbnail;

    public static String getChangeSkin(){
        return changeSkin;
    }

    public static void setChangeSkin(String changeSkin){
        GlobalSettings.changeSkin = changeSkin;
    }

    public static boolean isDebugMode(){
        return debugMode;
    }

    public static void setDebugMode(boolean debugMode){
        GlobalSettings.debugMode = debugMode;
    }

    public static boolean isAutoMode(){
        return autoMode;
    }

    public static void setAutoMode(boolean autoMode){
        GlobalSettings.autoMode = autoMode;
    }

    public static long getLocalClockOffset(){
        return localClockOffset;
    }

    public static void setLocalClockOffset(long localClockOffset){
        GlobalSettings.localClockOffset = localClockOffset;
    }

    public static HashMap<Integer, CometManager> getCometManagers(){
        return cometManagers;
    }

    public static void setReqsPerAccount(int reqsPerAccount){
        GlobalSettings.reqsPerAccount = reqsPerAccount;
    }

    public static int getReqsPerAccount(){
        return reqsPerAccount;
    }

    public static void setWarmUp(boolean warmUp){
        GlobalSettings.warmUp = warmUp;
    }

    public static boolean isWarmUp(){
        return warmUp;
    }

    public static void setSendWebhook(boolean sendWebhook){
        GlobalSettings.sendWebhook = sendWebhook;
    }

    public static boolean isSendWebhook(){
        return sendWebhook;
    }

    public static boolean isPingMojang(){
        return pingMojang;
    }

    public static void setPingMojang(boolean pingMojang){
        GlobalSettings.pingMojang = pingMojang;
    }

    public static int getSlowAuthDelay(){
        return slowAuthDelay;
    }

    public static void setSlowAuthDelay(int slowAuthDelay){
        GlobalSettings.slowAuthDelay = slowAuthDelay;
    }

    public static int getAuthDelay(){
        return authDelay;
    }

    public static void setAuthDelay(int authDelay){
        GlobalSettings.authDelay = authDelay;
    }

    public static void setWebhookThumbnail(String webhookThumbnail){
        GlobalSettings.webhookThumbnail = webhookThumbnail;
    }

    public static String getWebhookThumbnail(){
        return webhookThumbnail;
    }

    public static boolean isTurboMode(){
        return turboMode;
    }

    public static void setTurboMode(boolean turboMode){
        GlobalSettings.turboMode = turboMode;
    }

    public static void setRemoteMode(boolean remoteMode){
        GlobalSettings.remoteMode = remoteMode;
    }

    public static boolean isRemoteMode(){
        return remoteMode;
    }

    public static void setKillProcess(boolean killProcess){
        GlobalSettings.killProcess = killProcess;
    }

    public static void setSaveLogs(boolean saveLogs){
        GlobalSettings.saveLogs = saveLogs;
    }

    public static boolean isKillProcess(){
        return killProcess;
    }

    public static boolean isSaveLogs(){
        return saveLogs;
    }

    public static boolean isUseNamemcProxy(){
        return useNamemcProxy;
    }

    public static void setUseNamemcProxy(boolean useNamemcProxy){
        GlobalSettings.useNamemcProxy = useNamemcProxy;
    }

    public static long getSpeedCap(){
        return speedCap;
    }

    public static void setSpeedCap(long speedCap){
        GlobalSettings.speedCap = speedCap;
    }

    public static void setUseProxy(boolean useProxy){
        GlobalSettings.useProxy = useProxy;
    }

    public static boolean isUseProxy(){
        return useProxy;
    }
}