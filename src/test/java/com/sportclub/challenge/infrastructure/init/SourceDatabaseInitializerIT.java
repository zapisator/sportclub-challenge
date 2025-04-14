package com.sportclub.challenge.infrastructure.init;

import com.sportclub.challenge.adapter.out.persistence.source.entity.SourceBranchJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.source.entity.SourceUserJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.source.repository.SourceBranchJpaRepository;
import com.sportclub.challenge.adapter.out.persistence.source.repository.SourceUserJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles({"init-source-db", "test"})
@TestPropertySource(properties = {
        "spring.batch.job.enabled=false",
        "spring.jpa.source.hibernate.ddl-auto=create-drop",
        "spring.jpa.target.hibernate.ddl-auto=create-drop",
        "spring.datasource.source.url=jdbc:h2:mem:;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
})
@DisplayName("Integration Test for SourceDatabaseInitializer")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SourceDatabaseInitializerIT {

    @Autowired
    @Qualifier("sourceBranchJpaRepository")
    private SourceBranchJpaRepository sourceBranchRepo;

    @Autowired
    @Qualifier("sourceUserJpaRepository")
    private SourceUserJpaRepository sourceUserRepo;

    private static final int EXPECTED_BRANCH_COUNT = 100;
    private static final int EXPECTED_USER_COUNT = 100;
    private static final String FIRST_BRANCH_ID = "SB001";
    private static final String FIRST_USER_ID = "SU0001";
    private static final String FIRST_USER_DNI = "20000001";
    private static final String BRANCH_BASE_NAME = "Source Branch ";
    private static final String USER_FNAME_BASE = "SourceFName ";
    private static final String USER_LNAME_BASE = "SourceLName ";
    private static final String ADDRESS_SUFFIX = " Source St.";
    private static final String CITY_BASE_NAME = "City ";
    private static final String EMAIL_DOMAIN = "@example.com";

    @Test
    @DisplayName("should populate source database with branches and users when init-source-db profile is active")
    void shouldInitializeSourceDatabase_whenProfileIsActive() {
        final long actualBranchCount = sourceBranchRepo.count();
        final long actualUserCount = sourceUserRepo.count();

        assertThat(actualBranchCount)
                .as("Check source branch count after initialization")
                .isEqualTo(EXPECTED_BRANCH_COUNT);
        assertThat(actualUserCount)
                .as("Check source user count after initialization")
                .isEqualTo(EXPECTED_USER_COUNT);

        final Optional<SourceBranchJpaEntity> firstBranchOpt = sourceBranchRepo.findById(FIRST_BRANCH_ID);
        assertThat(firstBranchOpt)
                .as("Check if first source branch (ID: %s) exists", FIRST_BRANCH_ID)
                .isPresent();

        firstBranchOpt.ifPresent(branch -> {
            assertThat(branch.getName()).as("First branch name").isEqualTo(BRANCH_BASE_NAME + 1);
            assertThat(branch.getAddress()).as("First branch address").isEqualTo(1 + ADDRESS_SUFFIX);
            assertThat(branch.getCity()).as("First branch city").isEqualTo(CITY_BASE_NAME + (1 % 10));
        });

        final Optional<SourceUserJpaEntity> firstUserOpt = sourceUserRepo.findById(FIRST_USER_ID);
        assertThat(firstUserOpt)
                .as("Check if first source user (ID: %s) exists", FIRST_USER_ID)
                .isPresent();

        firstUserOpt.ifPresent(user -> {
            assertThat(user.getDni()).as("First user DNI").isEqualTo(FIRST_USER_DNI);
            assertThat(user.getFirstName()).as("First user first name").isEqualTo(USER_FNAME_BASE + 1);
            assertThat(user.getLastName()).as("First user last name").isEqualTo(USER_LNAME_BASE + 1);
            assertThat(user.getEmail()).as("First user email").isEqualTo("su.1" + EMAIL_DOMAIN);
            assertThat(user.getState()).as("First user state").isNotNull();
            assertThat(user.getBranch()).as("First user branch link").isNotNull();
            assertThat(user.getBranch().getId()).as("First user linked branch ID").isEqualTo(FIRST_BRANCH_ID);
        });

        final Optional<SourceUserJpaEntity> userByDniOpt = sourceUserRepo.findAll().stream()
                .filter(u -> FIRST_USER_DNI.equals(u.getDni()))
                .findFirst();

        assertThat(userByDniOpt)
                .as("Check if user with DNI %s exists in source DB", FIRST_USER_DNI)
                .isPresent();
        userByDniOpt.ifPresent(user -> {
            assertThat(user.getId()).isEqualTo(FIRST_USER_ID);
        });
    }
}