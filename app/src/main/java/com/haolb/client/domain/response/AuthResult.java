package com.haolb.client.domain.response;

import java.io.Serializable;

public class AuthResult extends BaseResult {
    private static final long serialVersionUID = 1L;
    public AuthData data;
    public static class AuthData  implements Serializable {
        private static final long serialVersionUID = 1L;
        public String id;
    }

}
