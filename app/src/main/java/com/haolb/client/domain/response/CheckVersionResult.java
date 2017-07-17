package com.haolb.client.domain.response;

import java.io.Serializable;

/**
 * 检查更新
 * @author zexu
 */
public class CheckVersionResult extends BaseResult {
    private static final long serialVersionUID = 1L;

    public CheckVersionData data;
    
    public static class CheckVersionData implements Serializable {
		private static final long serialVersionUID = 1L;
        public UpgradeInfo upgradeInfo;
    }

}