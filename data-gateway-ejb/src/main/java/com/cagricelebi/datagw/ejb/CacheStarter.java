package com.cagricelebi.datagw.ejb;

import com.cagricelebi.datagw.lib.log.Logger;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import org.infinispan.Cache;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.CacheContainer;

@Singleton
@Startup
public class CacheStarter {

    @Inject
    Logger logger;

    @Resource(lookup = "java:/infinispan/DGW-CACHE")
    private CacheContainer dgwCache;
    @Resource(lookup = "java:/infinispan/DGW-CACHE/incomingIpCache")
    private Cache<Long, List<Long>> incomingIpCache;

    @PostConstruct
    private void init() {
        if (incomingIpCache.getStatus() == ComponentStatus.TERMINATED || incomingIpCache.getStatus() == ComponentStatus.FAILED) {
            incomingIpCache.start();
        }
    }

}
