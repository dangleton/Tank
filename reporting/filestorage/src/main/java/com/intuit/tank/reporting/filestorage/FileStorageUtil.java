package com.intuit.tank.reporting.filestorage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class FileStorageUtil {

    private static final DateFormat DF = new SimpleDateFormat("yyyyMMddkkmmssS");

    public static final String getTpsFolderName(String jobId) {
        return "tps/" + jobId;
    }

    public static final String getTimingFolderName(String jobId) {
        return "timing/" + jobId;
    }

    public static final String getFileName(String instanceId, Date date) {
        date = date != null ? date : new Date();
        instanceId = instanceId != null ? instanceId : UUID.randomUUID().toString();
        return instanceId + "_" + DF.format(date) + ".xml.gz";
    }
}
