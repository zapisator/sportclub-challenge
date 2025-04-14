package com.sportclub.challenge.infrastructure.config;

import com.sportclub.challenge.adapter.out.persistence.source.entity.SourceBranchJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.source.entity.SourceUserJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.source.mapper.SourceBranchPersistenceMapper;
import com.sportclub.challenge.adapter.out.persistence.source.mapper.SourceUserPersistenceMapper;
import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetBranchJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetUserJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.target.mapper.TargetBranchPersistenceMapper;
import com.sportclub.challenge.adapter.out.persistence.target.mapper.TargetUserPersistenceMapper;
import com.sportclub.challenge.adapter.out.persistence.target.repository.TargetBranchJpaRepository;
import com.sportclub.challenge.adapter.out.persistence.target.repository.TargetUserJpaRepository;
import com.sportclub.challenge.application.port.out.log.LoggingPort;
import com.sportclub.challenge.domain.model.branch.Branch;
import com.sportclub.challenge.domain.model.user.User;
import com.sportclub.challenge.infrastructure.batch.MigrationJobCompletionListener;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Objects;
import java.util.Optional;

@Configuration
public class BatchMigrationConfig {

    private static final int CHUNK_SIZE = 100;
    private final JobRepository jobRepository;
    private final LoggingPort logger;

    @Qualifier("targetEntityManagerFactory")
    private final EntityManagerFactory targetEntityManagerFactory;
    @Qualifier("targetTransactionManager")
    private final PlatformTransactionManager targetTransactionManager;
    private final TargetBranchJpaRepository targetBranchJpaRepository;
    private final TargetUserJpaRepository targetUserJpaRepository;
    private final TargetBranchPersistenceMapper targetBranchMapper;
    private final TargetUserPersistenceMapper targetUserMapper;
    @Qualifier("sourceEntityManagerFactory")
    private final EntityManagerFactory sourceEntityManagerFactory;
    private final SourceBranchPersistenceMapper sourceBranchMapper;
    private final SourceUserPersistenceMapper sourceUserMapper;
    private final MigrationJobCompletionListener jobCompletionNotificationListener;

    public BatchMigrationConfig(
            JobRepository jobRepository,
            LoggingPort logger,
            @Qualifier("targetEntityManagerFactory")
            EntityManagerFactory targetEntityManagerFactory,
            @Qualifier("targetTransactionManager")
            PlatformTransactionManager targetTransactionManager,
            TargetBranchJpaRepository targetBranchJpaRepository,
            TargetUserJpaRepository targetUserJpaRepository,
            TargetBranchPersistenceMapper targetBranchMapper,
            TargetUserPersistenceMapper targetUserMapper,
            @Qualifier("sourceEntityManagerFactory")
            EntityManagerFactory sourceEntityManagerFactory,
            SourceBranchPersistenceMapper sourceBranchMapper,
            SourceUserPersistenceMapper sourceUserMapper,
            MigrationJobCompletionListener jobCompletionNotificationListener
    ) {
        this.jobRepository = jobRepository;
        this.logger = logger;
        this.targetEntityManagerFactory = targetEntityManagerFactory;
        this.targetTransactionManager = targetTransactionManager;
        this.targetBranchJpaRepository = targetBranchJpaRepository;
        this.targetUserJpaRepository = targetUserJpaRepository;
        this.targetBranchMapper = targetBranchMapper;
        this.targetUserMapper = targetUserMapper;
        this.sourceEntityManagerFactory = sourceEntityManagerFactory;
        this.sourceBranchMapper = sourceBranchMapper;
        this.sourceUserMapper = sourceUserMapper;
        this.jobCompletionNotificationListener = jobCompletionNotificationListener;
    }

    @Bean
    public ItemReader<SourceBranchJpaEntity> sourceBranchReader() {
        return new JpaPagingItemReaderBuilder<SourceBranchJpaEntity>()
                .name("sourceBranchReader")
                .entityManagerFactory(sourceEntityManagerFactory)
                .queryString("SELECT b FROM SourceBranchJpaEntity b ORDER BY b.id ASC")
                .pageSize(CHUNK_SIZE)
                .saveState(false)
                .build();
    }

    @Bean
    public ItemProcessor<SourceBranchJpaEntity, TargetBranchJpaEntity> branchProcessor() {
        return sourceEntity -> {
            logger.trace("Processing Source Branch ID: {}", sourceEntity.getId());
            final Branch sourceBranch = sourceBranchMapper.toDomain(sourceEntity);
            final Optional<TargetBranchJpaEntity> targetEntityOpt = targetBranchJpaRepository
                    .findById(sourceBranch.id());

            if (targetEntityOpt.isEmpty()) {
                logger.debug("Branch ID '{}'. Action: CREATE.", sourceBranch.id());
                return targetBranchMapper.toEntity(sourceBranch);
            } else {
                final Branch targetBranch = targetBranchMapper.toDomain(targetEntityOpt.get());
                if (!areBranchesEqual(sourceBranch, targetBranch)) {
                    logger.debug("Branch ID '{}'. Action: UPDATE.", sourceBranch.id());
                    return targetBranchMapper.toEntity(sourceBranch);
                } else {
                    logger.trace("Branch ID '{}'. Action: IDENTICAL. Skipping write.", sourceBranch.id());
                    return null;
                }
            }
        };
    }

    @Bean
    public ItemWriter<TargetBranchJpaEntity> targetBranchWriter() {
        final JpaItemWriter<TargetBranchJpaEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(targetEntityManagerFactory);
        return writer;
    }

    @Bean
    public Step migrateBranchesStep() {
        return new StepBuilder("migrateBranchesStep", jobRepository)
                .<SourceBranchJpaEntity, TargetBranchJpaEntity>chunk(CHUNK_SIZE, targetTransactionManager)
                .reader(sourceBranchReader())
                .processor(branchProcessor())
                .writer(targetBranchWriter())
                .build();
    }

    @Bean
    public ItemReader<SourceUserJpaEntity> sourceUserReader() {
        return new JpaPagingItemReaderBuilder<SourceUserJpaEntity>()
                .name("sourceUserReader")
                .entityManagerFactory(sourceEntityManagerFactory)
                .queryString("SELECT u FROM SourceUserJpaEntity u ORDER BY u.id ASC")
                .pageSize(CHUNK_SIZE)
                .saveState(false)
                .build();
    }

    @Bean
    public ItemProcessor<SourceUserJpaEntity, TargetUserJpaEntity> userProcessor() {
        return sourceEntity -> {
            logger.trace("Processing Source User ID: {}", sourceEntity.getId());
            final User sourceUser = sourceUserMapper.toDomain(sourceEntity);
            final String sourceBranchId = (sourceUser.branch() != null) ? sourceUser.branch().id() : null;
            if (sourceBranchId == null) {
                logger.warn("User ID '{}' (DNI: {}). Action: SKIP_NO_SOURCE_BRANCH_ID. Source branch or its ID is null.",
                        sourceUser.id(), sourceUser.dni());
                return null;
            }

            final Optional<TargetBranchJpaEntity> targetBranchEntityOpt = targetBranchJpaRepository.findById(sourceBranchId);
            if (targetBranchEntityOpt.isEmpty()) {
                logger.warn("User ID '{}' (DNI: {}). Action: SKIP_NO_TARGET_BRANCH. Target branch ID '{}' not found.",
                        sourceUser.id(), sourceUser.dni(), sourceBranchId);
                return null;
            }
            final Branch targetBranch = targetBranchMapper.toDomain(targetBranchEntityOpt.get());
            final User userToProcess = sourceUser.withBranch(targetBranch);
            final Optional<TargetUserJpaEntity> targetUserEntityOpt = targetUserJpaRepository
                    .findById(userToProcess.id());

            if (targetUserEntityOpt.isEmpty()) {
                logger.debug("User ID '{}' (DNI: {}). Action: CREATE.",
                        userToProcess.id(), userToProcess.dni()
                );
                return targetUserMapper.toEntity(userToProcess);
            } else {
                User targetUser = targetUserMapper.toDomain(targetUserEntityOpt.get());
                if (!areUsersEqual(userToProcess, targetUser)) {
                    logger.debug("User ID '{}' (DNI: {}). Action: UPDATE.",
                            userToProcess.id(), userToProcess.dni()
                    );
                    return targetUserMapper.toEntity(userToProcess);
                } else {
                    logger.trace("User ID '{}' (DNI: {}). Action: IDENTICAL. Skipping write.",
                            userToProcess.id(), userToProcess.dni()
                    );
                    return null;
                }
            }
        };
    }

    @Bean
    public ItemWriter<TargetUserJpaEntity> targetUserWriter() {
        final JpaItemWriter<TargetUserJpaEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(targetEntityManagerFactory);
        return writer;
    }

    @Bean
    public Step migrateUsersStep() {
        return new StepBuilder("migrateUsersStep", jobRepository)
                .<SourceUserJpaEntity, TargetUserJpaEntity>chunk(CHUNK_SIZE, targetTransactionManager)
                .reader(sourceUserReader())
                .processor(userProcessor())
                .writer(targetUserWriter())
                .build();
    }

    @Bean(name = "dataMigrationJob")
    public Job dataMigrationJob() {
        return new JobBuilder("dataMigrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobCompletionNotificationListener)
                .start(migrateBranchesStep())
                .next(migrateUsersStep())
                .build();
    }

    private boolean areBranchesEqual(Branch b1, Branch b2) {
        if (b1 == b2) return true;
        if (b1 == null || b2 == null) return false;
        return Objects.equals(b1.id(), b2.id()) &&
                Objects.equals(b1.name(), b2.name()) &&
                Objects.equals(b1.address(), b2.address()) &&
                Objects.equals(b1.city(), b2.city());
    }

    private boolean areUsersEqual(User u1, User u2) {
        if (u1 == u2) return true;
        if (u1 == null || u2 == null) return false;

        final String b1Id = (u1.branch() != null) ? u1.branch().id() : null;
        final String b2Id = (u2.branch() != null) ? u2.branch().id() : null;

        return Objects.equals(u1.id(), u2.id()) &&
                Objects.equals(u1.firstName(), u2.firstName()) &&
                Objects.equals(u1.lastName(), u2.lastName()) &&
                Objects.equals(u1.email(), u2.email()) &&
                Objects.equals(u1.phone(), u2.phone()) &&
                Objects.equals(u1.dni(), u2.dni()) &&
                Objects.equals(u1.state(), u2.state()) &&
                Objects.equals(b1Id, b2Id);
    }

}
