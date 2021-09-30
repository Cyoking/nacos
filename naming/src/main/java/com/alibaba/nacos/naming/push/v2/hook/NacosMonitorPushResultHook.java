/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.naming.push.v2.hook;

import com.alibaba.nacos.metrics.manager.MetricsManager;
import com.alibaba.nacos.metrics.manager.NamingMetricsConstant;
import com.alibaba.nacos.naming.monitor.MetricsMonitor;
import com.alibaba.nacos.naming.monitor.NamingTpsMonitor;
import com.alibaba.nacos.naming.pojo.Subscriber;

/**
 * Nacos naming monitor push result hook.
 *
 * @author xiweng.yy
 */
public class NacosMonitorPushResultHook implements PushResultHook {
    
    @Override
    public void pushSuccess(PushResult result) {
        MetricsMonitor.incrementPush();
        MetricsMonitor.incrementPushCost(result.getAllCost());
        MetricsMonitor.compareAndSetMaxPushCost(result.getAllCost());
        if (isRpc(result.getSubscriber())) {
            MetricsManager.counter(NamingMetricsConstant.NACOS_SERVER_PUSH_COUNT,
                    NamingMetricsConstant.MODULE, NamingMetricsConstant.NAMING,
                    NamingMetricsConstant.TYPE, NamingMetricsConstant.GRPC,
                    NamingMetricsConstant.SUCCESS, NamingMetricsConstant.TRUE).increment();
            MetricsMonitor.setServerPushCost(result.getAllCost(), "grpc", "true");
            NamingTpsMonitor.rpcPushSuccess(result.getSubscribeClientId(), result.getSubscriber().getIp());
        } else {
            MetricsManager.counter(NamingMetricsConstant.NACOS_SERVER_PUSH_COUNT,
                    NamingMetricsConstant.MODULE, NamingMetricsConstant.NAMING,
                    NamingMetricsConstant.TYPE, NamingMetricsConstant.UDP,
                    NamingMetricsConstant.SUCCESS, NamingMetricsConstant.TRUE).increment();
            MetricsMonitor.setServerPushCost(result.getAllCost(), "udp", "true");
            NamingTpsMonitor.udpPushSuccess(result.getSubscribeClientId(), result.getSubscriber().getIp());
        }
    }
    
    @Override
    public void pushFailed(PushResult result) {
        MetricsMonitor.incrementFailPush();
        if (isRpc(result.getSubscriber())) {
            MetricsManager.counter(NamingMetricsConstant.NACOS_SERVER_PUSH_COUNT,
                    NamingMetricsConstant.MODULE, NamingMetricsConstant.NAMING,
                    NamingMetricsConstant.TYPE, NamingMetricsConstant.GRPC,
                    NamingMetricsConstant.SUCCESS, NamingMetricsConstant.FALSE).increment();
            MetricsMonitor.setServerPushCost(result.getAllCost(), "grpc", "false");
            NamingTpsMonitor.rpcPushFail(result.getSubscribeClientId(), result.getSubscriber().getIp());
        } else {
            MetricsManager.counter(NamingMetricsConstant.NACOS_SERVER_PUSH_COUNT,
                    NamingMetricsConstant.MODULE, NamingMetricsConstant.NAMING,
                    NamingMetricsConstant.TYPE, NamingMetricsConstant.UDP,
                    NamingMetricsConstant.SUCCESS, NamingMetricsConstant.FALSE).increment();
            MetricsMonitor.setServerPushCost(result.getAllCost(), "udp", "false");
            NamingTpsMonitor.udpPushFail(result.getSubscribeClientId(), result.getSubscriber().getIp());
        }
    }
    
    private boolean isRpc(Subscriber subscriber) {
        return subscriber.getPort() <= 0;
    }
}
