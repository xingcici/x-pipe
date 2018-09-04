package com.ctrip.xpipe.redis.console.healthcheck.factory;

import com.ctrip.xpipe.api.foundation.FoundationService;
import com.ctrip.xpipe.api.proxy.ProxyEnabled;
import com.ctrip.xpipe.endpoint.DefaultEndPoint;
import com.ctrip.xpipe.endpoint.HostPort;
import com.ctrip.xpipe.redis.console.AbstractConsoleIntegrationTest;
import com.ctrip.xpipe.redis.console.healthcheck.RedisHealthCheckInstance;
import com.ctrip.xpipe.redis.console.healthcheck.config.ProxyEnabledHealthCheckConfig;
import com.ctrip.xpipe.redis.console.resources.DefaultMetaCache;
import com.ctrip.xpipe.redis.console.resources.MetaCache;
import com.ctrip.xpipe.redis.core.AbstractRedisTest;
import com.ctrip.xpipe.redis.core.entity.*;
import com.ctrip.xpipe.redis.core.protocal.cmd.AbstractRedisCommand;
import com.ctrip.xpipe.spring.AbstractProfile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * @author chen.zhu
 * <p>
 * Sep 04, 2018
 */
public class DefaultHealthCheckRedisInstanceFactoryTest extends AbstractConsoleIntegrationTest {

    @Autowired
    protected DefaultHealthCheckRedisInstanceFactory factory;

    @Autowired
    private DefaultHealthCheckEndpointFactory endpointFactory;

    private MetaCache metaCache;

    @Before
    public void beforeDefaultHealthCheckRedisInstanceFactoryTest() {
        metaCache = mock(DefaultMetaCache.class);
        endpointFactory.setMetaCache(metaCache);
    }

    @Test
    public void testCreate() {
        DcMeta dcMeta = new DcMeta().setId("dc");
        ClusterMeta clusterMeta = new ClusterMeta().setId("cluster").setParent(dcMeta);
        ShardMeta shardMeta = new ShardMeta().setParent(clusterMeta).setId("shard");
        RedisMeta redisMeta = new RedisMeta().setParent(shardMeta).setIp("localhost").setPort(randomPort());
        RedisHealthCheckInstance instance = factory.create(redisMeta);

        Assert.assertNotNull(instance.getEndpoint());
        Assert.assertNotNull(instance.getHealthCheckConfig());
        Assert.assertNotNull(instance.getHealthCheckContext());
        Assert.assertNotNull(instance.getHealthStatusManager());
        Assert.assertNotNull(instance.getRedisInstanceInfo());
        Assert.assertNotNull(instance.getRedisSession());

        Assert.assertEquals(instance.getEndpoint(), new DefaultEndPoint(redisMeta.getIp(), redisMeta.getPort()));
        Assert.assertTrue(instance.getLifecycleState().canStart());
    }

    @Test
    public void testCreateProxyEnabledInstance() {
        XpipeMeta meta = new XpipeMeta();
        DcMeta local = newDcMeta(FoundationService.DEFAULT.getDataCenter());
        meta.addDc(local);
        DcMeta target = newDcMeta("target");
        RedisMeta redisMeta = target.getClusters().get("cluster").getShards().get("shard").getRedises().get(0);
        meta.addDc(target);

        String routeInfo = "PROXYTCP://127.0.0.1:8008,PROXYTCP://127.0.0.1:8009";
        local.addRoute(new RouteMeta().setSrcDc(FoundationService.DEFAULT.getDataCenter())
                .setDstDc("target").setTag(Route.TAG_CONSOLE).setRouteInfo(routeInfo));

        when(metaCache.getRouteIfPossible(any())).thenReturn(local.getRoutes().get(0));
        when(metaCache.getXpipeMeta()).thenReturn(meta);

        logger.info("{}", metaCache.getXpipeMeta().toString());
        logger.info("{}", metaCache.getRouteIfPossible(new HostPort(redisMeta.getIp(), redisMeta.getPort())));


        RedisHealthCheckInstance instance = factory.create(redisMeta);

        Assert.assertTrue(instance.getEndpoint() instanceof ProxyEnabled);
        Assert.assertTrue(instance.getHealthCheckConfig() instanceof ProxyEnabledHealthCheckConfig);
        Assert.assertEquals(AbstractRedisCommand.PROXYED_REDIS_CONNECTION_COMMAND_TIME_OUT_MILLI,
                instance.getRedisSession().getCommandTimeOut());
    }

    protected DcMeta newDcMeta(String dcId) {
        DcMeta dcMeta = new DcMeta().setId(dcId);
        ClusterMeta clusterMeta = new ClusterMeta().setId("cluster").setParent(dcMeta);
        dcMeta.addCluster(clusterMeta);
        ShardMeta shardMeta = new ShardMeta().setParent(clusterMeta).setId("shard");
        clusterMeta.addShard(shardMeta);
        RedisMeta redisMeta = new RedisMeta().setParent(shardMeta).setIp("localhost").setPort(randomPort());
        shardMeta.addRedis(redisMeta);
        return dcMeta;
    }
}