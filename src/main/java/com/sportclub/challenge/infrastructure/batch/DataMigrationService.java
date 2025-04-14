package com.sportclub.challenge.infrastructure.batch;

import com.sportclub.challenge.application.exception.MigrationFailedException;
import com.sportclub.challenge.application.port.in.MigrateDataUseCase;
import com.sportclub.challenge.application.port.out.log.LoggingPort;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class DataMigrationService implements MigrateDataUseCase {

    private final JobLauncher jobLauncher;
    private final LoggingPort logger;
    private final Job dataMigrationJob;

    public DataMigrationService(
            JobLauncher jobLauncher, LoggingPort loggingPort,
            @Qualifier("dataMigrationJob") Job job
    ) {
        this.jobLauncher = jobLauncher;
        logger = loggingPort;
        dataMigrationJob = job;
    }

    @Override
    public void migrateData() {
        logger.info("Attempting to launch data migration batch job '{}'...", dataMigrationJob.getName());

        JobParameters jobParameters = null;
        try {
            jobParameters = new JobParametersBuilder()
                    .addLong("run.timestamp", System.currentTimeMillis(), true)
                    .toJobParameters();
            JobExecution jobExecution = jobLauncher.run(dataMigrationJob, jobParameters);

            logger.info("Data migration batch job '{}' launched. Job Execution ID: {}, Job Parameters: [{}], Initial Status: {}",
                    dataMigrationJob.getName(),
                    jobExecution.getId(),
                    jobParameters,
                    jobExecution.getStatus());

        } catch (JobExecutionAlreadyRunningException e) {
            logger.error("Failed to launch data migration job: Job execution is already running.", e);
            throw new MigrationFailedException("Migration job is already running: " + e.getMessage(), e);
        } catch (JobRestartException e) {
            logger.error("Failed to launch data migration job: Job restart is not allowed or failed.", e);
            throw new MigrationFailedException("Migration job restart failed: " + e.getMessage(), e);
        } catch (JobInstanceAlreadyCompleteException e) {
            logger.warn("Failed to launch data migration job: Job instance is already complete for parameters: {}. Exception: {}",
                    jobParameters,
                    e.getMessage());
            throw new MigrationFailedException("Migration job instance already complete: " + e.getMessage(), e);
        } catch (JobParametersInvalidException e) {
            logger.error("Failed to launch data migration job: Invalid job parameters.", e);
            throw new MigrationFailedException("Invalid parameters for migration job: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred while launching the data migration batch job!", e);
            throw new MigrationFailedException("Failed to launch data migration job due to an unexpected error: " + e.getMessage(), e);
        }
    }
}