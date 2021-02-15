package com.github.derinn.util.mojang;

import com.github.derinn.account.Account;

import java.util.concurrent.CountDownLatch;

public class SkinChangeRequester implements Runnable{

    private final Account account;
    private final CountDownLatch countDownLatch;

    public SkinChangeRequester(Account account, CountDownLatch countDownLatch){
        this.account = account;
        this.countDownLatch = countDownLatch;
    }

    public void run(){

        UpdateSkin.updateSkin(account);
        countDownLatch.countDown();

    }

}
