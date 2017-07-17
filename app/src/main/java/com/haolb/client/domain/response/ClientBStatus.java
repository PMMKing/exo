package com.haolb.client.domain.response;

import java.io.Serializable;

/**
 * 
 *
 */
public class ClientBStatus implements Serializable {
    public static final String TAG = "bstatus";
    private static final long serialVersionUID = 1L;
    public int code;
    public String des;
    public boolean hasmessage;
}
