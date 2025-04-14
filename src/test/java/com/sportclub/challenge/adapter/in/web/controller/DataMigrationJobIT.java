package com.sportclub.challenge.adapter.in.web.controller;

import com.sportclub.challenge.adapter.out.persistence.source.entity.SourceBranchJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.source.entity.SourceUserJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.source.repository.SourceBranchJpaRepository;
import com.sportclub.challenge.adapter.out.persistence.source.repository.SourceUserJpaRepository;
import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetBranchJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetUserJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.target.repository.TargetBranchJpaRepository;
import com.sportclub.challenge.adapter.out.persistence.target.repository.TargetUserJpaRepository;
import com.sportclub.challenge.domain.model.user.UserState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@DisplayName("Integration Tests for DataMigrationJob")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DataMigrationJobIT {

    private static final String BRANCH_NEW_ID = "SB_NEW";
    private static final String BRANCH_UPD_ID = "SB_UPD";
    private static final String BRANCH_ID_ID = "SB_ID";
    private static final String BRANCH_OK_ID = "SB_OK";
    private static final String USER_NEW_ID = "SU_NEW";
    private static final String USER_UPD_ID = "SU_UPD";
    private static final String USER_ID_ID = "SU_ID";
    private static final String USER_OK_ID = "SU_OK";
    private static final String BRANCH_FOCUS_NEW_ID = "SB_FOCUS_NEW";
    private static final String USER_FOCUS_NEW_ID = "SU_FOCUS_NEW";
    private static final String BRANCH_MISSING_ID = "SB_MISSING";
    private static final String USER_ORPHAN_ID = "SU_ORPHAN";
    private static final String USER_NULL_BRANCH_ID = "SU_NULL_BRANCH";
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;
    @Autowired
    @Qualifier("dataMigrationJob")
    private Job dataMigrationJob;
    @Autowired
    private SourceBranchJpaRepository sourceBranchRepo;
    @Autowired
    private SourceUserJpaRepository sourceUserRepo;
    @Autowired
    private TargetBranchJpaRepository targetBranchRepo;
    @Autowired
    private TargetUserJpaRepository targetUserRepo;

    @BeforeEach
    void setUp() {
        this.jobLauncherTestUtils.setJob(dataMigrationJob);
        this.sourceUserRepo.deleteAllInBatch();
        this.sourceBranchRepo.deleteAllInBatch();
        this.targetUserRepo.deleteAllInBatch();
        this.targetBranchRepo.deleteAllInBatch();
    }

    @AfterEach
    void tearDown() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    private SourceBranchJpaEntity setupSourceBranch(String id, String name, String address, String city) {
        final SourceBranchJpaEntity entity = new SourceBranchJpaEntity(id, name, address, city, null);
        return sourceBranchRepo.saveAndFlush(entity);
    }

    private TargetBranchJpaEntity setupTargetBranch(String id, String name, String address, String city) {
        final TargetBranchJpaEntity entity = new TargetBranchJpaEntity(id, name, address, city, null);
        return targetBranchRepo.saveAndFlush(entity);
    }

    private SourceUserJpaEntity setupSourceUser(String id, String dni, UserState state, SourceBranchJpaEntity branch) {
        final SourceUserJpaEntity user = new SourceUserJpaEntity(id, "SrcFName_" + id, "SrcLName_" + id,
                id + "@source.test", "SrcPh_" + id, dni, state, branch
        );
        return sourceUserRepo.saveAndFlush(user);
    }

    private TargetUserJpaEntity setupTargetUser(String id, String dni, UserState state, TargetBranchJpaEntity branch) {
        final TargetUserJpaEntity user = new TargetUserJpaEntity(
                id, "TgtFName_" + id, "TgtLName_" + id,
                id + "@target.test", "TgtPh_" + id, dni, state, branch
        );
        return targetUserRepo.saveAndFlush(user);
    }

    private void assertTargetBranchCount(long expectedCount) {
        assertThat(targetBranchRepo.count())
                .as("Check target branch count")
                .isEqualTo(expectedCount);
    }

    private void assertTargetUserCount(long expectedCount) {
        assertThat(targetUserRepo.count())
                .as("Check target user count")
                .isEqualTo(expectedCount);
    }

    private void assertTargetBranchState(String id, String expectedName, String expectedAddress) {
        final Optional<TargetBranchJpaEntity> opt = targetBranchRepo.findById(id);
        assertThat(opt).as("Branch with ID '%s' should exist in target DB", id).isPresent();
        opt.ifPresent(branch -> {
            assertThat(branch.getName()).as("Branch '%s' name", id).isEqualTo(expectedName);
            assertThat(branch.getAddress()).as("Branch '%s' address", id).isEqualTo(expectedAddress);
        });
    }

    private void assertTargetUserState(String id, String expectedFirstName, UserState expectedState, String expectedBranchId) {
        final Optional<TargetUserJpaEntity> opt = targetUserRepo.findById(id);
        assertThat(opt).as("User with ID '%s' should exist in target DB", id).isPresent();
        opt.ifPresent(user -> {
            assertThat(user.getFirstName()).as("User '%s' first name", id).isEqualTo(expectedFirstName);
            assertThat(user.getState()).as("User '%s' state", id).isEqualTo(expectedState);
            assertThat(user.getBranch()).as("User '%s' branch link", id).isNotNull();
            assertThat(user.getBranch().getId()).as("User '%s' branch ID", id).isEqualTo(expectedBranchId);
        });
    }

    private void assertBranchListsMatchIgnoringUsers(List<TargetBranchJpaEntity> actual, List<TargetBranchJpaEntity> expected) {
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("users")
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    private void assertUserListsMatchIgnoringBranchUsers(List<TargetUserJpaEntity> actual, List<TargetUserJpaEntity> expected) {
        assertThat(actual)
                .usingRecursiveFieldByFieldElementComparatorIgnoringFields("branch.users")
                .containsExactlyInAnyOrderElementsOf(expected);
    }

    @Test
    @DisplayName("Scenario 1: Initial migration with empty target")
    void shouldMigrateDataToEmptyTarget() throws Exception {
        final SourceBranchJpaEntity sourceBranch1 = setupSourceBranch(
                "SB1", "Source Branch 1", "Addr 1", "City A"
        );
        setupSourceUser("SU1", "11111111", UserState.AUTHORIZED, sourceBranch1);

        assertTargetBranchCount(0);
        assertTargetUserCount(0);
        final JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertTargetBranchCount(1);
        assertTargetUserCount(1);
        assertTargetBranchState("SB1", "Source Branch 1", "Addr 1");
        assertTargetUserState("SU1", "SrcFName_SU1", UserState.AUTHORIZED, "SB1");
    }

    @Test
    @DisplayName("Scenario 2: Update existing data")
    void shouldUpdateExistingTargetData() throws Exception {
        final SourceBranchJpaEntity sourceBranchUpd = setupSourceBranch(
                BRANCH_UPD_ID, "Updated Branch Name", "New Addr", "City B"
        );
        setupSourceUser(USER_UPD_ID, "99999999", UserState.DENIED, sourceBranchUpd);

        final TargetBranchJpaEntity targetBranchOld = setupTargetBranch(
                BRANCH_UPD_ID, "Initial Branch Name", "Old Addr", "City A"
        );
        setupTargetUser(USER_UPD_ID, "99999999", UserState.AUTHORIZED, targetBranchOld);

        assertTargetBranchCount(1);
        assertTargetUserCount(1);
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertTargetBranchCount(1);
        assertTargetUserCount(1);
        assertTargetBranchState(
                BRANCH_UPD_ID, "Updated Branch Name", "New Addr"
        );
        assertTargetUserState(
                USER_UPD_ID, "SrcFName_" + USER_UPD_ID, UserState.DENIED, BRANCH_UPD_ID
        );
    }

    @Test
    @DisplayName("Scenario 3: Should skip update when data is identical")
    void shouldSkipIdenticalData() throws Exception {
        final String branchName = "Identical Branch";
        final String branchAddr = "Addr Id";
        final String branchCity = "City Id";
        final SourceBranchJpaEntity sourceBranchIdentical = setupSourceBranch(BRANCH_ID_ID, branchName, branchAddr, branchCity);
        final TargetBranchJpaEntity targetBranchIdentical = setupTargetBranch(BRANCH_ID_ID, branchName, branchAddr, branchCity);
        final String userDni = "55555555";
        final UserState userState = UserState.AUTHORIZED;
        final SourceUserJpaEntity sourceUserIdentical = setupSourceUser(USER_ID_ID, userDni, userState, sourceBranchIdentical);
        final String userFirstName = sourceUserIdentical.getFirstName();
        final String userLastName = sourceUserIdentical.getLastName();
        final String userEmail = sourceUserIdentical.getEmail();
        final String userPhone = sourceUserIdentical.getPhone();
        final TargetUserJpaEntity targetUserIdentical = new TargetUserJpaEntity(
                sourceUserIdentical.getId(),
                userFirstName,
                userLastName,
                userEmail,
                userPhone,
                userDni,
                userState,
                targetBranchIdentical
        );

        targetUserRepo.saveAndFlush(targetUserIdentical);
        final long initialTargetBranchCount = targetBranchRepo.count();
        final long initialTargetUserCount = targetUserRepo.count();
        final TargetBranchJpaEntity targetBranchBefore = targetBranchRepo.findById(BRANCH_ID_ID)
                .orElseThrow(() -> new AssertionError("Target branch " + BRANCH_ID_ID + " not found before job run"));
        final TargetUserJpaEntity targetUserBefore = targetUserRepo.findById(USER_ID_ID)
                .orElseThrow(() -> new AssertionError("Target user " + USER_ID_ID + " not found before job run"));

        assertThat(initialTargetBranchCount).isEqualTo(1);
        assertThat(initialTargetUserCount).isEqualTo(1);
        assertThat(targetUserBefore.getFirstName()).isEqualTo(userFirstName);
        assertThat(targetUserBefore.getEmail()).isEqualTo(userEmail);
        final JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertTargetBranchCount(initialTargetBranchCount);
        assertTargetUserCount(initialTargetUserCount);
        final Optional<TargetBranchJpaEntity> targetBranchAfterOpt = targetBranchRepo.findById(BRANCH_ID_ID);
        assertThat(targetBranchAfterOpt).as("Target branch %s should exist after job", BRANCH_ID_ID).isPresent();
        assertThat(targetBranchAfterOpt.get())
                .usingRecursiveComparison().ignoringFields("users")
                .as("Target branch %s data comparison", BRANCH_ID_ID)
                .isEqualTo(targetBranchBefore);
        final Optional<TargetUserJpaEntity> targetUserAfterOpt = targetUserRepo.findById(USER_ID_ID);
        assertThat(targetUserAfterOpt).as("Target user %s should exist after job", USER_ID_ID).isPresent();
        assertThat(targetUserAfterOpt.get())
                .usingRecursiveComparison().ignoringFields("branch.users")
                .as("Target user %s data comparison", USER_ID_ID)
                .isEqualTo(targetUserBefore);
        assertThat(targetUserAfterOpt.get().getFirstName()).isEqualTo(userFirstName);
        assertThat(targetUserAfterOpt.get().getEmail()).isEqualTo(userEmail);
        assertThat(targetUserAfterOpt.get().getState()).isEqualTo(userState);
    }

    @Test
    @DisplayName("Scenario 4: Should migrate user whose branch is created during the same migration")
    void shouldMigrateUserWhoseBranchIsCreatedDuringSameMigration() throws Exception {
        final SourceBranchJpaEntity sourceBranchOk = setupSourceBranch(BRANCH_OK_ID, "Branch OK", "Addr OK", "City OK");
        final TargetBranchJpaEntity targetBranchOk = setupTargetBranch(BRANCH_OK_ID, "Branch OK", "Addr OK", "City OK");
        final SourceBranchJpaEntity sourceBranchNew = setupSourceBranch(BRANCH_NEW_ID, "Branch New", "Addr New", "City New");
        setupSourceUser(USER_OK_ID, "11111111", UserState.AUTHORIZED, sourceBranchOk);
        setupSourceUser(USER_NEW_ID, "22222222", UserState.AUTHORIZED, sourceBranchNew);

        assertTargetBranchCount(1);
        assertTargetUserCount(0);
        final JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertTargetBranchCount(2);
        assertThat(targetBranchRepo.findById(BRANCH_OK_ID)).isPresent();
        assertTargetBranchState(BRANCH_NEW_ID, "Branch New", "Addr New");
        assertTargetUserCount(2);
        assertTargetUserState(USER_OK_ID, "SrcFName_" + USER_OK_ID, UserState.AUTHORIZED, BRANCH_OK_ID);
        assertTargetUserState(USER_NEW_ID, "SrcFName_" + USER_NEW_ID, UserState.AUTHORIZED, BRANCH_NEW_ID);
    }

    @Test
    @DisplayName("Scenario 5: Migration should be idempotent")
    void shouldBeIdempotent() throws Exception {
        final SourceBranchJpaEntity sourceBranchNew = setupSourceBranch(
                BRANCH_NEW_ID, "Branch New Created", "Addr New", "City New"
        );
        final SourceBranchJpaEntity sourceBranchUpd = setupSourceBranch(
                BRANCH_UPD_ID, "Branch Update Final", "Addr Upd New", "City Upd"
        );
        final SourceBranchJpaEntity sourceBranchIdentical = setupSourceBranch(
                BRANCH_ID_ID, "Branch Identical", "Addr Id", "City Id"
        );
        setupSourceUser(USER_NEW_ID, "333333", UserState.AUTHORIZED, sourceBranchNew);
        setupSourceUser(USER_UPD_ID, "111111", UserState.DENIED, sourceBranchUpd);
        setupSourceUser(USER_ID_ID, "222222", UserState.DENIED, sourceBranchIdentical);
        final TargetBranchJpaEntity targetBranchUpdInitial = setupTargetBranch(
                BRANCH_UPD_ID, "Branch Update Initial", "Addr Upd Old", "City Upd"
        );
        final TargetBranchJpaEntity targetBranchIdenticalInitial = setupTargetBranch(
                BRANCH_ID_ID, "Branch Identical", "Addr Id", "City Id"
        );
        setupTargetUser(USER_UPD_ID, "111111", UserState.AUTHORIZED, targetBranchUpdInitial);
        setupTargetUser(USER_ID_ID, "222222", UserState.DENIED, targetBranchIdenticalInitial);

        final JobExecution firstJobExecution = jobLauncherTestUtils.launchJob();
        assertThat(firstJobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        final long branchCountAfterFirstRun = targetBranchRepo.count();
        final long userCountAfterFirstRun = targetUserRepo.count();
        assertThat(branchCountAfterFirstRun).isEqualTo(3);
        assertThat(userCountAfterFirstRun).isEqualTo(3);
        final List<TargetBranchJpaEntity> branchesAfterFirstRun = targetBranchRepo.findAll();
        final List<TargetUserJpaEntity> usersAfterFirstRun = targetUserRepo.findAll();
        jobRepositoryTestUtils.removeJobExecutions();
        final JobExecution secondJobExecution = jobLauncherTestUtils.launchJob();
        assertThat(secondJobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertTargetBranchCount(branchCountAfterFirstRun);
        assertTargetUserCount(userCountAfterFirstRun);
        final List<TargetBranchJpaEntity> branchesAfterSecondRun = targetBranchRepo.findAll();
        final List<TargetUserJpaEntity> usersAfterSecondRun = targetUserRepo.findAll();
        assertBranchListsMatchIgnoringUsers(branchesAfterSecondRun, branchesAfterFirstRun);
        assertUserListsMatchIgnoringBranchUsers(usersAfterSecondRun, usersAfterFirstRun);
    }

    @Test
    @DisplayName("Scenario 6 (Focus): Should create new user linked to a new branch created in the same run")
    void shouldCreateNewUserAndItsNewBranch() throws Exception {
        final SourceBranchJpaEntity sourceBranchNew = setupSourceBranch(
                BRANCH_FOCUS_NEW_ID, "Focused New Branch", "Addr Focus New", "City Focus"
        );
        setupSourceUser(USER_FOCUS_NEW_ID, "77777777", UserState.AUTHORIZED, sourceBranchNew);

        assertTargetBranchCount(0);
        assertTargetUserCount(0);
        final JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertTargetBranchCount(1);
        assertTargetBranchState(
                BRANCH_FOCUS_NEW_ID, "Focused New Branch", "Addr Focus New"
        );
        assertTargetUserCount(1);
        assertTargetUserState(
                USER_FOCUS_NEW_ID, "SrcFName_" + USER_FOCUS_NEW_ID,
                UserState.AUTHORIZED, BRANCH_FOCUS_NEW_ID
        );
    }

    @DisplayName("Scenario 7: Should skip user whose target branch is missing")
    void shouldSkipUserWhenTargetBranchIsMissing() throws Exception {
        final SourceBranchJpaEntity sourceBranchOk = setupSourceBranch(
                BRANCH_OK_ID, "Branch OK", "Addr OK", "City OK"
        );
        setupSourceUser(USER_OK_ID, "11111111", UserState.AUTHORIZED, sourceBranchOk);
        final SourceBranchJpaEntity sourceBranchMissing = setupSourceBranch(
                BRANCH_MISSING_ID, "Missing Branch", "Addr Missing", "City Missing"
        );
        setupSourceUser(USER_ORPHAN_ID, "88888888", UserState.AUTHORIZED, sourceBranchMissing);

        assertTargetBranchCount(0);
        assertTargetUserCount(0);
        final JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertTargetBranchCount(1);
        assertTargetBranchState(BRANCH_OK_ID, "Branch OK", "Addr OK");
        assertThat(targetBranchRepo.findById(BRANCH_MISSING_ID)).isEmpty();
        assertTargetUserCount(1);
        assertTargetUserState(
                USER_OK_ID, "SrcFName_" + USER_OK_ID, UserState.AUTHORIZED, BRANCH_OK_ID
        );
        assertThat(targetUserRepo.findById(USER_ORPHAN_ID)).isEmpty();
    }

    @Test
    @DisplayName("Scenario 8: Should skip user when source user has null branch reference")
    void shouldSkipUserWhenSourceBranchIdIsNull() throws Exception {
        final SourceBranchJpaEntity sourceBranchOk = setupSourceBranch(BRANCH_OK_ID, "Branch OK", "Addr OK", "City OK");
        setupSourceUser(USER_OK_ID, "11111111", UserState.AUTHORIZED, sourceBranchOk);
        final SourceUserJpaEntity sourceUserNullBranch = new SourceUserJpaEntity(
                USER_NULL_BRANCH_ID,
                "SrcFName_" + USER_NULL_BRANCH_ID,
                "SrcLName_" + USER_NULL_BRANCH_ID,
                USER_NULL_BRANCH_ID + "@source.test",
                "SrcPh_" + USER_NULL_BRANCH_ID,
                "77777777",
                UserState.AUTHORIZED,
                null
        );
        sourceUserRepo.saveAndFlush(sourceUserNullBranch);

        assertTargetBranchCount(0);
        assertTargetUserCount(0);
        final JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertTargetBranchCount(1);
        assertTargetBranchState(BRANCH_OK_ID, "Branch OK", "Addr OK");
        assertTargetUserCount(1);
        assertTargetUserState
                (USER_OK_ID, "SrcFName_" + USER_OK_ID, UserState.AUTHORIZED, BRANCH_OK_ID
                );
        assertThat(targetUserRepo.findById(USER_NULL_BRANCH_ID)).isEmpty();
    }

    @Test
    @DisplayName("Scenario 9: Should complete successfully when source database is empty")
    void shouldCompleteSuccessfullyWhenSourceIsEmpty() throws Exception {
        final TargetBranchJpaEntity existingTargetBranch = setupTargetBranch("TB_EXIST", "Existing Target", "Addr Exist", "City Exist");
        final long initialTargetBranchCount = targetBranchRepo.count();
        final long initialTargetUserCount = targetUserRepo.count();

        final JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertTargetBranchCount(initialTargetBranchCount);
        assertTargetUserCount(initialTargetUserCount);
        assertThat(targetBranchRepo.findById("TB_EXIST")).isPresent();
        assertThat(targetBranchRepo.findById("TB_EXIST").get().getName())
                .isEqualTo("Existing Target");
    }

    @Test
    @DisplayName("Scenario 10: Should migrate only branches when source has no users")
    void shouldMigrateOnlyBranchesWhenSourceHasNoUsers() throws Exception {
        final SourceBranchJpaEntity sourceBranch1 = setupSourceBranch(
                "SB1", "Source Branch 1", "Addr 1", "City A"
        );
        final SourceBranchJpaEntity sourceBranch2 = setupSourceBranch(
                "SB2", "Source Branch 2", "Addr 2", "City B"
        );

        assertTargetBranchCount(0);
        assertTargetUserCount(0);
        final JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertTargetBranchCount(2);
        assertTargetBranchState("SB1", "Source Branch 1", "Addr 1");
        assertTargetBranchState("SB2", "Source Branch 2", "Addr 2");
        assertTargetUserCount(0);
    }

    @Test
    @DisplayName("Scenario 11: Should handle larger volume of data (e.g., 500 records)")
    void shouldHandleLargerVolume() throws Exception {
        final int recordCount = 500;
        final List<SourceBranchJpaEntity> sourceBranches = new ArrayList<>();

        for (int i = 1; i <= recordCount; i++) {
            sourceBranches.add(setupSourceBranch(
                    "SB_" + i, "Branch " + i, "Addr " + i, "City " + (i % 10)
            ));
        }
        for (int i = 1; i <= recordCount; i++) {
            setupSourceUser(
                    "SU_" + i, String.valueOf(10000000 + i), UserState.AUTHORIZED,
                    sourceBranches.get((i - 1) % recordCount)
            );
        }

        assertTargetBranchCount(0);
        assertTargetUserCount(0);
        final JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertTargetBranchCount(recordCount);
        assertTargetUserCount(recordCount);
        assertTargetBranchState("SB_1", "Branch 1", "Addr 1");
        assertTargetUserState("SU_" + recordCount, "SrcFName_SU_" + recordCount,
                UserState.AUTHORIZED, "SB_" + recordCount
        );
    }

}
