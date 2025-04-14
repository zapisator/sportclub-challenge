package com.sportclub.challenge.adapter.out.persistence.target;

import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetBranchJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetUserJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.target.mapper.TargetBranchPersistenceMapperImpl;
import com.sportclub.challenge.adapter.out.persistence.target.mapper.TargetUserPersistenceMapperImpl;
import com.sportclub.challenge.adapter.out.persistence.target.repository.TargetBranchJpaRepository;
import com.sportclub.challenge.adapter.out.persistence.target.repository.TargetUserJpaRepository;
import com.sportclub.challenge.application.port.out.persistence.target.TargetBranchRepositoryPort;
import com.sportclub.challenge.application.port.out.persistence.target.TargetUserRepositoryPort;
import com.sportclub.challenge.domain.model.branch.Branch;
import com.sportclub.challenge.domain.model.user.User;
import com.sportclub.challenge.domain.model.user.UserState;
import com.sportclub.challenge.infrastructure.config.DatasourceConfig;
import com.sportclub.challenge.infrastructure.config.TargetJpaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        DatasourceConfig.class,
        TargetJpaConfig.class,
        TargetUserRepositoryAdapter.class,
        TargetBranchRepositoryAdapter.class,
        TargetUserPersistenceMapperImpl.class,
        TargetBranchPersistenceMapperImpl.class
})
@DisplayName("Integration Tests for TargetUserRepositoryAdapter using @DataJpaTest")
class TargetUserRepositoryAdapterDataJpaIT {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TargetUserRepositoryPort targetUserRepositoryAdapter;
    @Autowired
    private TargetBranchRepositoryPort targetBranchRepositoryAdapter;
    @Autowired
    private TargetUserJpaRepository targetUserJpaRepository;
    @Autowired
    private TargetBranchJpaRepository targetBranchJpaRepository;
    private Branch savedBranchDomain;

    @BeforeEach
    void setUp() {

        targetUserJpaRepository.deleteAll();
        targetBranchJpaRepository.deleteAll();
        final TargetBranchJpaEntity branchEntity = new TargetBranchJpaEntity(
                "B99", "Test Branch", "1 Test St", "Test City", null
        );
        final TargetBranchJpaEntity savedBranchEntity = entityManager.persistAndFlush(branchEntity);


        savedBranchDomain = targetBranchRepositoryAdapter
                .findById(savedBranchEntity.getId())
                .orElseThrow();

        assertThat(savedBranchDomain).isNotNull();
        assertThat(targetBranchJpaRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("should save a new user and find it by ID")
    void saveAndFindById() {
        final User userToSave = User.builder()
                .id("U999")
                .firstName("Tester")
                .lastName("McTest")
                .email("test@example.com")
                .phone("5551234")
                .dni("999888777")
                .state(UserState.AUTHORIZED)
                .branch(savedBranchDomain)
                .build();
        final User savedUser = targetUserRepositoryAdapter.save(userToSave);

        assertAll(
                () -> assertThat(savedUser).isNotNull(),
                () -> assertThat(savedUser.id()).isEqualTo(userToSave.id()),
                () -> assertThat(savedUser.firstName()).isEqualTo(userToSave.firstName()),
                () -> assertThat(savedUser.dni()).isEqualTo(userToSave.dni()),
                () -> assertThat(savedUser.state()).isEqualTo(userToSave.state()),
                () -> assertThat(savedUser.branch()).isNotNull(),
                () -> assertThat(savedUser.branch().id()).isEqualTo(savedBranchDomain.id())
        );

        final TargetUserJpaEntity foundEntity = entityManager.find(TargetUserJpaEntity.class, savedUser.id());
        assertThat(foundEntity).isNotNull();
        assertThat(foundEntity.getEmail()).isEqualTo(userToSave.email());
        assertThat(foundEntity.getBranch().getId()).isEqualTo(savedBranchDomain.id());

        final Optional<User> foundUserOpt = targetUserRepositoryAdapter.findById(savedUser.id());

        assertAll(
                () -> assertThat(foundUserOpt).isPresent(),
                () -> assertThat(foundUserOpt.get().id()).isEqualTo(savedUser.id()),
                () -> assertThat(foundUserOpt.get().email()).isEqualTo(savedUser.email())
        );
    }

    @Test
    @DisplayName("should find user by DNI when user exists")
    void findByDni_whenExists() {
        final String dniToFind = "777666555";
        final User userToSave = User.builder()
                .id("U777")
                .firstName("Dni")
                .lastName("User")
                .email("dni@example.com")
                .phone("5551111")
                .dni(dniToFind)
                .state(UserState.AUTHORIZED)
                .branch(savedBranchDomain)
                .build();
        targetUserRepositoryAdapter.save(userToSave);

        final Optional<User> foundUserOpt = targetUserRepositoryAdapter.findByDni(dniToFind);

        assertAll(
                () -> assertThat(foundUserOpt).isPresent(),
                () -> assertThat(foundUserOpt.get().dni()).isEqualTo(dniToFind),
                () -> assertThat(foundUserOpt.get().id()).isEqualTo("U777")
        );
    }

    @Test
    @DisplayName("should return empty Optional when finding by DNI that does not exist")
    void findByDni_whenNotExists() {
        final String nonExistentDni = "000000000";

        final Optional<User> foundUserOpt = targetUserRepositoryAdapter.findByDni(nonExistentDni);

        assertThat(foundUserOpt).isEmpty();
    }

    @Test
    @DisplayName("should find all users with pagination and sorting")
    void findAll_withPagination() {

        targetUserRepositoryAdapter.save(User.builder()
                .id("U001")
                .firstName("Alice")
                .lastName("Smith")
                .email("a@a.com")
                .phone("111")
                .dni("111111")
                .state(UserState.AUTHORIZED)
                .branch(savedBranchDomain)
                .build());
        targetUserRepositoryAdapter.save(User.builder()
                .id("U002")
                .firstName("Bob")
                .lastName("Johnson")
                .email("b@b.com")
                .phone("222")
                .dni("222222")
                .state(UserState.DENIED)
                .branch(savedBranchDomain)
                .build());
        targetUserRepositoryAdapter.save(User.builder()
                .id("U003")
                .firstName("Charlie")
                .lastName("Brown")
                .email("c@c.com")
                .phone("333")
                .dni("333333")
                .state(UserState.AUTHORIZED)
                .branch(savedBranchDomain)
                .build()
        );

        final Pageable firstPage = PageRequest.of(0, 2, Sort.by("firstName"));
        final Page<User> userFirstPage = targetUserRepositoryAdapter.findAll(firstPage);

        assertAll(
                () -> assertThat(userFirstPage).isNotNull(),
                () -> assertThat(userFirstPage.getTotalElements()).isEqualTo(3),
                () -> assertThat(userFirstPage.getTotalPages()).isEqualTo(2),
                () -> assertThat(userFirstPage.getNumber()).isEqualTo(0),
                () -> assertThat(userFirstPage.getNumberOfElements()).isEqualTo(2),
                () -> assertThat(userFirstPage.getContent()).hasSize(2),
                () -> assertThat(userFirstPage.getContent().get(0).firstName()).isEqualTo("Alice"),
                () -> assertThat(userFirstPage.getContent().get(1).firstName()).isEqualTo("Bob")
        );

        final Pageable secondPage = PageRequest.of(1, 2, Sort.by("firstName"));
        final Page<User> userSecondPage = targetUserRepositoryAdapter.findAll(secondPage);

        assertAll(
                () -> assertThat(userSecondPage.getTotalElements()).isEqualTo(3),
                () -> assertThat(userSecondPage.getTotalPages()).isEqualTo(2),
                () -> assertThat(userSecondPage.getNumber()).isEqualTo(1),
                () -> assertThat(userSecondPage.getNumberOfElements()).isEqualTo(1),
                () -> assertThat(userSecondPage.getContent()).hasSize(1),
                () -> assertThat(userSecondPage.getContent().get(0).firstName()).isEqualTo("Charlie")
        );
    }
}