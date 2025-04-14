package com.sportclub.challenge.infrastructure.batch;

import com.sportclub.challenge.application.exception.MigrationFailedException;
import com.sportclub.challenge.application.port.out.log.LoggingPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for DataMigrationService")
class DataMigrationServiceTest {

    @Mock
    private JobLauncher jobLauncher;
    @Mock
    private LoggingPort logger;
    @Mock
    private Job dataMigrationJob;
    @InjectMocks
    private DataMigrationService dataMigrationService;

    @Test
    @DisplayName("migrateData should throw MigrationFailedException when jobLauncher fails to run the job")
    void migrateData_shouldThrowMigrationFailedException_whenJobLauncherFails()
            throws
            JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException,
            JobRestartException
    {
        final JobParametersInvalidException launchException = new JobParametersInvalidException("Simulated invalid parameters");
        final String mockJobName = "testMigrationJob";

        when(dataMigrationJob.getName()).thenReturn(mockJobName);

        when(jobLauncher.run(eq(dataMigrationJob), any(JobParameters.class)))
                .thenThrow(launchException);

        assertThatThrownBy(() -> dataMigrationService.migrateData())
                .isInstanceOf(MigrationFailedException.class)
                .hasMessageContaining("Invalid parameters for migration job: "
                        + launchException.getMessage()
                )
                .hasCause(launchException);
        verify(logger)
                .info(contains("Attempting to launch data migration batch job"), eq(mockJobName));
        verify(jobLauncher).run(eq(dataMigrationJob), any(JobParameters.class));
        verify(logger).error(
                contains("Failed to launch data migration job: Invalid job parameters."),
                eq(launchException)
        );
        verify(logger, never()).info(contains("launched. Job Execution ID:"));
    }

}