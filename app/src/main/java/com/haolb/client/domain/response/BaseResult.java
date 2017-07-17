package com.haolb.client.domain.response;

import java.io.Serializable;

/**
 * 
 * @author zexu
 *
 */
public class BaseResult implements Serializable {
    public interface BaseData extends Serializable {
    }
    public static final String TAG = BaseResult.class.getSimpleName();

    private static final long serialVersionUID = 1L;


    public ClientBStatus bstatus;

}
