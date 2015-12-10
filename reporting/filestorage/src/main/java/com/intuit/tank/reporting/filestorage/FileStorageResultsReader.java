/**
 * Copyright 2011 Intuit Inc. All Rights Reserved
 */
package com.intuit.tank.reporting.filestorage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.intuit.tank.reporting.api.PagedTimingResults;
import com.intuit.tank.reporting.api.ResultsReader;
import com.intuit.tank.reporting.api.SerializerUtil;
import com.intuit.tank.reporting.api.TPSInfo;
import com.intuit.tank.reporting.api.TPSInfoContainer;
import com.intuit.tank.results.TankResult;
import com.intuit.tank.results.TankResultPackage;
import com.intuit.tank.storage.FileData;
import com.intuit.tank.storage.FileStorage;
import com.intuit.tank.storage.FileStorageFactory;

/**
 * DatabaseResultsReader
 * 
 * @author dangleton
 * 
 */
public class FileStorageResultsReader implements ResultsReader {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
            .getLogger(FileStorageResultsReader.class);

    private FileStorage fileStorage;

    public FileStorageResultsReader() {

    }

    /**
     * 
     * @throws IOException
     * @{inheritDoc
     */
    @Override
    public List<TankResult> getAllTimingResults(String jobId) {
        List<TankResult> ret = new ArrayList<TankResult>();
        try {
            for (FileData fd : fileStorage.listFileData(FileStorageUtil.getTimingFolderName(jobId))) {
                TankResultPackage pkg = SerializerUtil.unmarshall(fileStorage.readFileData(fd),
                        TankResultPackage.class);
                ret.addAll(pkg.getResults());
            }
        } catch (IOException e) {
            LOG.error("Error getting timing results: " + e, e);
        }
        return ret;
    }

    /**
     * 
     * @{inheritDoc
     */
    @Override
    public PagedTimingResults getPagedTimingResults(String jobId, Object nextToken) {
        return new PagedTimingResults(null, getAllTimingResults(jobId));
    }

    /**
     * 
     * @{inheritDoc
     */
    @Override
    public boolean hasTimingData(String jobId) {
        List<FileData> timingData = fileStorage.listFileData(FileStorageUtil.getTimingFolderName(jobId));
        return !timingData.isEmpty();
    }

    @Override
    public void deleteTimingForJob(String jobId, boolean asynch) {

        // may not want to do this
        // for (FileData fd : fileStorage.listFileData("timing/" + jobId)) {
        // fileStorage.delete(fd);
        // }

    }

    @Override
    public void config(HierarchicalConfiguration config) {
        String storage = config.getString("filestorage", "reporting-storage");
        LOG.info("Using reporting dir of " + storage);
        fileStorage = FileStorageFactory.getFileStorage(storage, true);
    }

    @Override
    public Map<Date, Map<String, TPSInfo>> getTpsMapForInstance(Date minDate, String jobId, String instanceId) {
        return getTpsMap(minDate, instanceId, jobId);
    }

    @Override
    public Map<Date, Map<String, TPSInfo>> getTpsMapForJob(Date minDate, String... jobId) {
        return getTpsMap(minDate, null, jobId);
    }

    private Map<Date, Map<String, TPSInfo>> getTpsMap(Date minDate, String instanceId, String... jobIds) {
        Map<Date, Map<String, TPSInfo>> ret = new HashMap<Date, Map<String, TPSInfo>>();
        try {
            if (jobIds != null && jobIds.length > 0) {
                for (String jobId : jobIds) {
                    List<FileData> tpsFiles = fileStorage.listFileData(FileStorageUtil.getTpsFolderName(jobId));
                    for (FileData fd : tpsFiles) {
                        if (instanceId == null || fd.getFileName().startsWith(instanceId)) {
                            TPSInfoContainer infoContainer = SerializerUtil.unmarshall(fileStorage.readFileData(fd),
                                    TPSInfoContainer.class);
                            for (TPSInfo info : infoContainer.getTpsInfos()) {
                                Date timestamp = info.getTimestamp();
                                String loggingKey = info.getKey();
                                if (minDate == null || timestamp.after(minDate)) {
                                    Map<String, TPSInfo> map = ret.get(timestamp);
                                    if (map == null) {
                                        map = new HashMap<String, TPSInfo>();
                                        ret.put(timestamp, map);
                                    }
                                    TPSInfo existing = map.get(loggingKey);
                                    if (existing != null) {
                                        info = existing.add(info);
                                    }
                                    map.put(loggingKey, info);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Error getting TPS map: " + e, e);
        }
        return ret;
    }

}
