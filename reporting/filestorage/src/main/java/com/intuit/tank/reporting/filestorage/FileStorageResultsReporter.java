/**
 * Copyright 2011 Intuit Inc. All Rights Reserved
 */
package com.intuit.tank.reporting.filestorage;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration.HierarchicalConfiguration;

import com.intuit.tank.reporting.api.ResultsReporter;
import com.intuit.tank.reporting.api.SerializerUtil;
import com.intuit.tank.reporting.api.TPSInfoContainer;
import com.intuit.tank.results.TankResult;
import com.intuit.tank.results.TankResultPackage;
import com.intuit.tank.storage.FileData;
import com.intuit.tank.storage.FileStorage;
import com.intuit.tank.storage.FileStorageFactory;

/**
 * DatabaseResultsReporter
 * 
 * @author dangleton
 * 
 */
public class FileStorageResultsReporter implements ResultsReporter {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
            .getLogger(FileStorageResultsReporter.class);

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(50), Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    private FileStorage fileStorage;

    

    public FileStorageResultsReporter() {
    }

    /**
     * @{inheritDoc
     */
    @Override
    public void sendTpsResults(final String jobId, final String instanceId, final TPSInfoContainer container,
            boolean async) {
        LOG.info("Writing tps results...");
        Runnable task = new Runnable() {
            public void run() {
                try {
                    String fileName = FileStorageUtil.getFileName(instanceId, container.getMinTime());
                    FileData fd = new FileData(FileStorageUtil.getTpsFolderName(jobId), fileName);
                    fileStorage.storeFileData(fd, SerializerUtil.marshall(container));
                } catch (Exception t) {
                    LOG.error("Error adding results: " + t.getMessage(), t);
                    throw new RuntimeException(t);
                }
            }
        };
        if (async) {
            EXECUTOR.execute(task);
        } else {
            task.run();
        }

    }

    /**
     * @{inheritDoc
     */
    @Override
    public void sendTimingResults(final String jobId, final String instanceId, final List<TankResult> results,
            boolean async) {
        LOG.info("Writing timing results...");
        if (!results.isEmpty()) {
            Runnable task = new Runnable() {
                public void run() {
                    try {
                        String fileName = FileStorageUtil.getFileName(instanceId, results.get(0).getTimeStamp());
                        FileData fd = new FileData(FileStorageUtil.getTimingFolderName(jobId), fileName);
                        fileStorage.storeFileData(fd, SerializerUtil.marshall(new TankResultPackage(jobId, instanceId, results)));
                    } catch (Exception t) {
                        LOG.error("Error adding results: " + t.getMessage(), t);
                        throw new RuntimeException(t);
                    }
                }
            };
            if (async) {
                EXECUTOR.execute(task);
            } else {
                task.run();
            }
        }
    }

    @Override
    public void config(HierarchicalConfiguration config) {
        String storage = config.getString("filestorage", "reporting-storage");
        LOG.info("Using reporting dir of " + storage);
        fileStorage = FileStorageFactory.getFileStorage(storage, true);
    }

}
