package com.lolsearcher.databatch.constant;

public class BatchConstants {

    public static final String CHAMP_STATS_JOB_NAME = "champion-stats-job";
    public static final String CHAMP_STATS_CREATING_STEP_NAME = "new-champion-stats-step";
    public static final String CHAMP_STATS_UPLOADING_STEP_NAME = "merge-champion-stats-step";
    public static final String CHAMP_STATS_CREATING_STEP_READER_NAME = "new-champion-stats-step-reader";
    public static final String CHAMP_STATS_UPLOADING_STEP_READER_NAME = "new-champion-stats-step-reader";
    public static final int MATCH_CHUNK_SIZE = 1000;
    public static final int CHAMP_STATS_CHUNK_SIZE = 100;
    public static final long INTERVAL_BATCH_TIME = 24*60*60*1000; /* 1 DAYS */
}
