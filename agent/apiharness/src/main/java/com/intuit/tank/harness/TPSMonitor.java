package com.intuit.tank.harness;

/*
 * #%L
 * Intuit Tank Agent (apiharness)
 * %%
 * Copyright (C) 2011 - 2015 Intuit Inc.
 * %%
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * #L%
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import com.intuit.tank.http.BaseRequest;
import com.intuit.tank.reporting.api.TPSInfo;
import com.intuit.tank.reporting.api.TPSInfoContainer;
import com.intuit.tank.vm.settings.TimeUtil;

public class TPSMonitor {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(TPSMonitor.class);

    private int period;

    private List<Counter> counters;
    private List<TPSInfo> infoList = new ArrayList<TPSInfo>();
    private Date minDate;

    public TPSMonitor(int period) {
        super();
        this.counters = Collections.synchronizedList(new ArrayList<TPSMonitor.Counter>());
        this.period = period;
    }

    public boolean isEnabled() {
        return period > 0;
    }

    /**
     * @return the tpsMap
     */
    public TPSInfoContainer getTPSInfo() {
        TPSInfoContainer ret = null;
        if (isEnabled()) {
            List<TPSInfo> retList = new ArrayList<TPSInfo>();
            long now = TimeUtil.normalizeToPeriod(period, new Date()).getTime();
            long min = now;
            long max = 0;
            Map<Long, Map<String, Integer>> tpsMap = new ConcurrentHashMap<Long, Map<String, Integer>>();
            List<Counter> toRemove = new ArrayList<TPSMonitor.Counter>();
            List<Counter> subList = new ArrayList<TPSMonitor.Counter>(counters);
            for (Counter counter : subList) {
                long entryTime = counter.time;
                if (now > (entryTime + (period * 2000))) {
                    min = Math.min(min, entryTime);
                    max = Math.max(max, entryTime);
                    Map<String, Integer> map = tpsMap.get(counter.time);
                    if (map == null) {
                        map = new HashMap<String, Integer>();
                        tpsMap.put(counter.time, map);
                    }
                    Integer integer = map.get(counter.key);
                    if (integer == null) {
                        integer = new Integer(0);
                        map.put(counter.key, integer);
                    }
                    map.put(counter.key, integer + 1);
                    toRemove.add(counter);
                }
            }
            if (!toRemove.isEmpty()) {
                LOG.info("about to remove " + toRemove.size() + " counters from list resulted in with " + counters.size() + " left");
                boolean removed = counters.removeAll(toRemove);
                LOG.info("removed " + toRemove.size() + " counters from list resulted in " + removed + " have " + counters.size() + " left");
            }
            for (Entry<Long, Map<String, Integer>> entry : tpsMap.entrySet()) {
                for (Entry<String, Integer> valueEntry : entry.getValue().entrySet()) {
                    TPSInfo info = new TPSInfo(new Date(entry.getKey()), valueEntry.getKey(), valueEntry.getValue()
                            .intValue(), period);
                    infoList.add(info);
                    retList.add(info);
                }
            }
            if (max == 0) { // no data yet
                ret = new TPSInfoContainer(new Date(min), new Date(min), period, retList);
            } else {
                if (minDate == null) {
                    minDate = new Date(min);
                }
                ret = new TPSInfoContainer(new Date(min), new Date(max), period, retList);
            }
        }
        LOG.info("returning Container with " + ret.getTpsInfos().size() + " infos and min of " + ret.getMinTime() + " and max of " + ret.getMaxTime());
        return ret;
    }

    public void addToMap(final String loggingKey, final BaseRequest req) {
        if (isEnabled()) {
            // LOG.info("Adding request " + req.getTimeStamp());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (req != null && req.getTimeStamp() != null) {
                        Date targetDate = TimeUtil.normalizeToPeriod(period, req.getTimeStamp());
                        // LOG.info("Adding normalized " + targetDate);
                        counters.add(new Counter(loggingKey, targetDate));
                    }

                }
            }).start();
        }
    }

    private static class Counter {
        private String key;
        private long time;

        public Counter(String key, Date date) {
            super();
            this.key = key;
            this.time = date.getTime();
        }

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

    }

}
