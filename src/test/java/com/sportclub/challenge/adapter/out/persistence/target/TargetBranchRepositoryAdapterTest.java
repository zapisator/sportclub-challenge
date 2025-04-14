package com.sportclub.challenge.adapter.out.persistence.target;

import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetBranchJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.target.mapper.TargetBranchPersistenceMapperImpl;
import com.sportclub.challenge.adapter.out.persistence.target.repository.TargetBranchJpaRepository;
import com.sportclub.challenge.adapter.out.persistence.target.repository.TargetUserJpaRepository;
import com.sportclub.challenge.application.port.out.persistence.target.TargetBranchRepositoryPort;
import com.sportclub.challenge.domain.model.branch.Branch;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        DatasourceConfig.class,
        TargetJpaConfig.class,
        TargetBranchRepositoryAdapter.class,
        TargetBranchPersistenceMapperImpl.class
})
@DisplayName("Integration Tests for TargetBranchRepositoryAdapter using @DataJpaTest")
class TargetBranchRepositoryAdapterTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private TargetBranchRepositoryPort targetBranchRepositoryAdapter;
    @Autowired
    private TargetBranchJpaRepository targetBranchJpaRepository;
    @Autowired
    private TargetUserJpaRepository targetUserJpaRepository;

    @BeforeEach
    void setUp() {
        targetUserJpaRepository.deleteAllInBatch();
        targetBranchJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("should save a new branch and find it by ID")
    void saveAndFindById() {
        final Branch branchToSave = new Branch("B001", "Central Branch", "123 Main St", "Capital City");
        final Branch savedBranch = targetBranchRepositoryAdapter.save(branchToSave);

        assertAll(
                () -> assertThat(savedBranch).isNotNull(),
                () -> assertThat(savedBranch.id()).isEqualTo(branchToSave.id()),
                () -> assertThat(savedBranch.name()).isEqualTo(branchToSave.name()),
                () -> assertThat(savedBranch.address()).isEqualTo(branchToSave.address()),
                () -> assertThat(savedBranch.city()).isEqualTo(branchToSave.city())
        );
        final TargetBranchJpaEntity foundEntity = entityManager
                .find(TargetBranchJpaEntity.class, savedBranch.id());
        assertThat(foundEntity).isNotNull();
        assertThat(foundEntity.getName()).isEqualTo(branchToSave.name());
        final Optional<Branch> foundBranchOpt = targetBranchRepositoryAdapter
                .findById(savedBranch.id());
        assertAll(
                () -> assertThat(foundBranchOpt).isPresent(),
                () -> assertThat(foundBranchOpt.get()).usingRecursiveComparison()
                        .isEqualTo(savedBranch)
        );
    }

    @Test
    @DisplayName("should return empty Optional when finding by ID that does not exist")
    void findById_whenNotExists() {
        final Optional<Branch> foundBranchOpt = targetBranchRepositoryAdapter
                .findById("NON-EXISTENT-ID");

        assertThat(foundBranchOpt).isEmpty();
    }

    @Test
    @DisplayName("should find all branches")
    void findAll() {
        final Branch branch1 = new Branch(
                "B1", "Branch A", "Addr A", "City X"
        );
        final Branch branch2 = new Branch(
                "B2", "Branch B", "Addr B", "City Y"
        );

        targetBranchRepositoryAdapter.save(branch1);
        targetBranchRepositoryAdapter.save(branch2);
        final List<Branch> allBranches = targetBranchRepositoryAdapter.findAll();

        assertThat(allBranches)
                .isNotNull()
                .hasSize(2)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactlyInAnyOrder(branch1, branch2);
    }

    @Test
    @DisplayName("should find all branches as a map with ID as key")
    void findAllAsMap() {
        final Branch branch1 = new Branch(
                "B1", "Branch A", "Addr A", "City X"
        );
        final Branch branch2 = new Branch(
                "B2", "Branch B", "Addr B", "City Y"
        );

        targetBranchRepositoryAdapter.save(branch1);
        targetBranchRepositoryAdapter.save(branch2);

        final Map<String, Branch> branchMap = targetBranchRepositoryAdapter.findAllAsMap();

        assertThat(branchMap)
                .isNotNull()
                .hasSize(2)
                .containsKeys("B1", "B2");
        assertThat(branchMap.get("B1")).usingRecursiveComparison().isEqualTo(branch1);
        assertThat(branchMap.get("B2")).usingRecursiveComparison().isEqualTo(branch2);
    }

    @Test
    @DisplayName("should update an existing branch")
    void save_shouldUpdateExistingBranch() {
        final Branch initialBranch = targetBranchRepositoryAdapter.save(new Branch("B_UPDATE", "Initial Name", "Initial Addr", "Initial City"));
        assertThat(targetBranchJpaRepository.count()).isEqualTo(1);

        final Branch updatedBranchData = new Branch(
                initialBranch.id(), "Updated Name", "Updated Addr", "Updated City"
        );
        final Branch savedUpdatedBranch = targetBranchRepositoryAdapter.save(updatedBranchData);

        assertThat(savedUpdatedBranch).isNotNull();
        assertThat(savedUpdatedBranch.id()).isEqualTo(initialBranch.id());
        assertThat(savedUpdatedBranch.name()).isEqualTo("Updated Name");
        assertThat(savedUpdatedBranch.address()).isEqualTo("Updated Addr");
        assertThat(targetBranchJpaRepository.count()).isEqualTo(1);
        final Optional<TargetBranchJpaEntity> foundEntityOpt = targetBranchJpaRepository
                .findById(initialBranch.id());
        assertThat(foundEntityOpt).isPresent();
        assertThat(foundEntityOpt.get().getName()).isEqualTo("Updated Name");
    }
}