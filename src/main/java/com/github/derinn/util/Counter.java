package com.github.derinn.util;

import java.util.ArrayList;
import java.util.HashMap;

public class Counter{

    public static String buildCounterMessage(ArrayList<Integer> responses){

        HashMap<Integer, Integer> counter = new HashMap<>();

        for(int res : responses){
            if(counter.containsKey(res)){
                counter.put(res, counter.get(res)+1);
            }else{
                counter.put(res, 1);
            }
        }

        StringBuilder builder = new StringBuilder();
        builder.append("/b/[INFO] ");

        for(int key : counter.keySet()){
            String appendation = "/b/" + key + ": /y/" + counter.get(key) + " /b/- ";
            builder.append(appendation);
        }

        return builder.toString();

    }

}
