package org.cysoft.carovignobot.common;

/**
 * Created by NS293854 on 13/05/2016.
 */
public class CyBssException extends Exception {
    public CyBssException(Exception e){
        super(e);
    }

    public CyBssException(String  msg){
        super(msg);
    }
}

