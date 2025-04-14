package com.sportclub.challenge.infrastructure.init;

import com.sportclub.challenge.adapter.out.persistence.source.entity.SourceBranchJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.source.entity.SourceUserJpaEntity;
import com.sportclub.challenge.application.port.out.log.LoggingPort;
import com.sportclub.challenge.domain.model.user.UserState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile("init-source-db")
@RequiredArgsConstructor
public class SourceDatabaseInitializer implements CommandLineRunner {

    private static final int BRANCH_COUNT = 100;
    private static final int USER_COUNT = 100;
    private static final String BRANCH_ID_PREFIX = "SB";
    private static final String USER_ID_PREFIX = "SU";
    private static final String BRANCH_ID_FORMAT = "%03d";
    private static final String USER_ID_FORMAT = "%04d";
    private static final long DNI_BASE = 20000000L;
    private static final long PHONE_BASE = 555000000L;
    private static final String EMAIL_DOMAIN = "@example.com";
    private static final String CITY_BASE_NAME = "City ";
    private static final String BRANCH_BASE_NAME = "Source Branch ";
    private static final String USER_FNAME_BASE = "SourceFName ";
    private static final String USER_LNAME_BASE = "SourceLName ";
    private static final String ADDRESS_SUFFIX = " Source St.";

    @PersistenceContext(unitName = "sourcePersistenceUnit")
    private EntityManager entityManager;
    private final LoggingPort logger;

    @Override
    @Transactional("sourceTransactionManager")
    public void run(String... args) throws Exception {
        logger.info("Source Database Initializer starting (profile 'init-source-db' is active)...");

        try {
//            if (dataExists(entityManager)) {
//                return;
//            }

            logger.info("Initializing source database with {} branches and {} users...", BRANCH_COUNT, USER_COUNT);

            final List<SourceBranchJpaEntity> createdBranches = createAndPersistBranches(entityManager);
            createAndPersistUsers(entityManager, createdBranches);

            logger.info("Source Database Initializer finished successfully.");

        } catch (Exception e) {
            logger.error("Source Database Initialization failed!", e);
            throw e;
        }
//        finally {
//            if (entityManager.isOpen()) {
//                entityManager.close();
//            }
//        }
    }

//    private boolean dataExists(EntityManager em) {
//        final long existingBranches = countEntities(em, SourceBranchJpaEntity.class);
//        final long existingUsers = countEntities(em, SourceUserJpaEntity.class);
//        if (existingBranches > 0 || existingUsers > 0) {
//            logger.warn("Source database already contains data ({} branches, {} users). " +
//                    "Skipping initialization.", existingBranches, existingUsers
//            );
//            return true;
//        }
//        return false;
//    }

    private long countEntities(EntityManager entityManager, Class<?> entityClass) {
        final String entityName = entityManager.getMetamodel().entity(entityClass).getName();
        return entityManager.createQuery("SELECT COUNT(e) FROM " + entityName + " e", Long.class)
                .getSingleResult();
    }

    private List<SourceBranchJpaEntity> createAndPersistBranches(EntityManager entityManager) {
        logger.debug("Creating {} branches...", BRANCH_COUNT);
        final List<SourceBranchJpaEntity> branches = new ArrayList<>(BRANCH_COUNT);
        for (int i = 1; i <= BRANCH_COUNT; i++) {
            final SourceBranchJpaEntity branchEntity = buildBranchEntity(i);
            try {
                entityManager.persist(branchEntity);
                branches.add(branchEntity);
                if (i % 10 == 0) {
                    logger.trace("Persisted branch {}/{}", i, BRANCH_COUNT);
                }
            } catch (Exception e) {
                logger.error(
                        "Failed to persist branch {} (ID: {}): {}",
                        i, branchEntity.getId(), e.getMessage(), e
                );
                throw e;
            }
        }
        logger.info("Successfully created {} branches.", branches.size());
        return branches;
    }

    private void createAndPersistUsers(EntityManager em, List<SourceBranchJpaEntity> branches) {
        logger.debug("Creating {} users...", USER_COUNT);
        int usersCreated = 0;
        if (branches.isEmpty()) {
            logger.warn("No branches provided to create users. Skipping user creation.");
            return;
        }
        for (int i = 1; i <= USER_COUNT; i++) {
            final SourceBranchJpaEntity assignedBranchEntity = branches.get((i - 1) % branches.size());
            final SourceUserJpaEntity userEntity = buildUserEntity(i, assignedBranchEntity);
            try {
                em.persist(userEntity);
                usersCreated++;
                if (i % 10 == 0) {
                    logger.trace("Persisted user {}/{}", i, USER_COUNT);
                }
            } catch (Exception e) {
                logger.error("Failed to persist user {} (ID: {}, DNI: {}): {}",
                        i, userEntity.getId(), userEntity.getDni(), e.getMessage(), e)
                ;
                throw e;
            }
        }
        logger.info("Successfully created {} users.", usersCreated);
    }

    private SourceBranchJpaEntity buildBranchEntity(int index) {
        String branchId = BRANCH_ID_PREFIX + String.format(BRANCH_ID_FORMAT, index);
        return new SourceBranchJpaEntity(
                branchId,
                BRANCH_BASE_NAME + index,
                index + ADDRESS_SUFFIX,
                CITY_BASE_NAME + (index % 10),
                null
        );
    }

    private SourceUserJpaEntity buildUserEntity(int index, SourceBranchJpaEntity assignedBranch) {
        String userId = USER_ID_PREFIX + String.format(USER_ID_FORMAT, index);
        String dni = String.valueOf(DNI_BASE + index);
        UserState state = getRandomUserState();
        return new SourceUserJpaEntity(
                userId,
                USER_FNAME_BASE + index,
                USER_LNAME_BASE + index,
                USER_ID_PREFIX.toLowerCase() + "." + index + EMAIL_DOMAIN,
                String.valueOf(PHONE_BASE + index),
                dni,
                state,
                assignedBranch
        );
    }

    private UserState getRandomUserState() {
        // ~80% AUTHORIZED, ~20% DENIED
        return ThreadLocalRandom.current().nextInt(10) < 8
                ? UserState.AUTHORIZED
                : UserState.DENIED;
    }
}