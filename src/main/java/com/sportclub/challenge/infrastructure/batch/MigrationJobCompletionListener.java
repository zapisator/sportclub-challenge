package com.sportclub.challenge.infrastructure.batch;

import com.sportclub.challenge.application.port.out.log.LoggingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MigrationJobCompletionListener implements JobExecutionListener {

    private final CacheManager cacheManager;
    private final LoggingPort logger;
    private static final List<String> CACHE_NAMES_TO_CLEAR = Arrays.asList(
            "usersByDni",
            "usersById",
            "branchesById"
    );

    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.info(">>> Starting data migration job: {} (Instance ID: {}, Execution ID: {})",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobInstance().getInstanceId(),
                jobExecution.getId());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        long instanceId = jobExecution.getJobInstance().getInstanceId();
        long executionId = jobExecution.getId();
        BatchStatus status = jobExecution.getStatus();

        if (status == BatchStatus.COMPLETED) {
            logger.info("<<< Migration job '{}' (Instance: {}, Execution: {}) completed successfully. Clearing caches...",
                    jobName, instanceId, executionId);
            clearCaches(jobExecution);
        } else {
            logger.warn("<<< Migration job '{}' (Instance: {}, Execution: {}) " +
                            "finished with status: {}. Caches will NOT be cleared.",
                    jobName, instanceId, executionId, status
            );
            jobExecution.getAllFailureExceptions().forEach(
                    ex -> logger.error("Failure exception during job execution {}: ", executionId, ex)
            );
        }
    }

    private void clearCaches(JobExecution jobExecution) {
        logger.info("Attempting to clear specified caches after successful job execution ID: {}",
                jobExecution.getId()
        );
        CACHE_NAMES_TO_CLEAR.forEach(cacheName -> {
            try {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    logger.info("Successfully cleared cache: '{}'", cacheName);
                } else {
                    logger.warn("Cache named '{}' not found in CacheManager. Skipping clear.",
                            cacheName
                    );
                }
            } catch (Exception e) {
                logger.error("Failed to clear cache: '{}'. Job Execution ID: {}. Error: {}",
                        cacheName, jobExecution.getId(), e.getMessage(), e);
            }
        });
        logger.info("Cache clearing process finished for job execution ID: {}",
                jobExecution.getId()
        );
    }
}