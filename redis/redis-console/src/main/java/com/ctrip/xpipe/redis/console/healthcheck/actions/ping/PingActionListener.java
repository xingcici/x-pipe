package com.ctrip.xpipe.redis.console.healthcheck.actions.ping;

import com.ctrip.xpipe.redis.console.healthcheck.ActionContext;
import com.ctrip.xpipe.redis.console.healthcheck.HealthCheckAction;
import com.ctrip.xpipe.redis.console.healthcheck.HealthCheckActionListener;
import com.ctrip.xpipe.redis.console.healthcheck.RedisHealthCheckInstance;

/**
 * @author chen.zhu
 * <p>
 * Oct 11, 2018
 */
public interface PingActionListener extends HealthCheckActionListener<PingActionContext, HealthCheckAction<RedisHealthCheckInstance>> {

    @Override
    default boolean worksfor(ActionContext t) {
        return t instanceof PingActionContext;
    }
}
