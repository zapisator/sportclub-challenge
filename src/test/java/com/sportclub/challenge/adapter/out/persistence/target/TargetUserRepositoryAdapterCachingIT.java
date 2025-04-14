package com.sportclub.challenge.adapter.out.persistence.target;

import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetBranchJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.target.entity.TargetUserJpaEntity;
import com.sportclub.challenge.adapter.out.persistence.target.repository.TargetBranchJpaRepository;
import com.sportclub.challenge.adapter.out.persistence.target.repository.TargetUserJpaRepository;
import com.sportclub.challenge.application.port.out.persistence.target.TargetUserRepositoryPort;
import com.sportclub.challenge.domain.model.user.User;
import com.sportclub.challenge.domain.model.user.UserState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Integration Tests for Caching in TargetUserRepositoryAdapter")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = {
        "spring.datasource.source.url=jdbc:h2:mem:;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.target.properties.hibernate.hbm2ddl.auto=create-drop"
})
class TargetUserRepositoryAdapterCachingIT {
    //

    @Autowired
    private TargetUserRepositoryPort userRepositoryAdapter;
    @MockitoSpyBean
    private TargetUserJpaRepository targetUserJpaRepositorySpy;
    @MockitoSpyBean
    private TargetBranchJpaRepository targetBranchJpaRepositorySpy;
    @Autowired
    private CacheManager cacheManager;
    private final String TEST_DNI_1 = "11223344";
    private final String TEST_USER_ID_1 = "U_CACHE_TEST_1";
    private final String TEST_DNI_2 = "55667788";
    private final String TEST_USER_ID_2 = "U_CACHE_TEST_2";
    private final String BRANCH_ID = "B_CACHE";
    private final String CACHE_NAME_DNI = "usersByDni";
    private final String CACHE_NAME_ID = "usersById";

    @BeforeEach
    void setUp() {
        System.out.println("--- Running setUp for test ---");
        // 1. Очистка кэшей
        clearCache(CACHE_NAME_DNI);
        clearCache(CACHE_NAME_ID);

        // 2. ЯВНАЯ ОЧИСТКА БД ПЕРЕД ТЕСТОМ
        System.out.println("Explicitly deleting all users and branches before test setup...");
        // Важно: Порядок удаления - сначала зависимые (users), потом главные (branches)
        targetUserJpaRepositorySpy.deleteAllInBatch(); // Используем deleteAllInBatch для эффективности
        targetBranchJpaRepositorySpy.deleteAllInBatch();
        System.out.println("DB cleanup complete.");

        // 3. Проверка, что база действительно пуста
        assertThat(targetBranchJpaRepositorySpy.count()).as("Branch count before adding test data").isZero();
        assertThat(targetUserJpaRepositorySpy.count()).as("User count before adding test data").isZero();

        // 4. Подготовка и сохранение тестовых данных
        // Создаем и СОХРАНЯЕМ ВЕТКУ
        TargetBranchJpaEntity branchEntity = new TargetBranchJpaEntity(BRANCH_ID, "Cache Branch", "Addr", "City", null);
        targetBranchJpaRepositorySpy.save(branchEntity); // Можно использовать saveAndFlush, если нужно сразу записать
        System.out.println("Saved Branch Entity: " + branchEntity.getId() + ". Current branch count after save: " + targetBranchJpaRepositorySpy.count());

        // Проверка СРАЗУ ПОСЛЕ СОХРАНЕНИЯ ВЕТКИ
        assertThat(targetBranchJpaRepositorySpy.count()).as("Branch count after saving ONE branch").isEqualTo(1L); // Сравниваем с Long

        // Создаем и сохраняем пользователей
        TargetUserJpaEntity userEntity1 = new TargetUserJpaEntity(TEST_USER_ID_1, "Cache1", "Test1", "c1@test.com", "111", TEST_DNI_1, UserState.AUTHORIZED, branchEntity);
        targetUserJpaRepositorySpy.save(userEntity1);
        System.out.println("Saved User Entity 1: " + userEntity1.getId());

        TargetUserJpaEntity userEntity2 = new TargetUserJpaEntity(TEST_USER_ID_2, "Cache2", "Test2", "c2@test.com", "222", TEST_DNI_2, UserState.AUTHORIZED, branchEntity);
        targetUserJpaRepositorySpy.save(userEntity2);
        System.out.println("Saved User Entity 2: " + userEntity2.getId());

        // Финальная проверка в конце setUp
        assertThat(targetBranchJpaRepositorySpy.count()).as("Final branch count in setUp").isEqualTo(1L);
        assertThat(targetUserJpaRepositorySpy.count()).as("Final user count in setUp").isEqualTo(2L);

        // 5. Сброс счетчиков шпионов ПОСЛЕ подготовки данных
        Mockito.reset(targetUserJpaRepositorySpy, targetBranchJpaRepositorySpy);
        System.out.println("Spies reset after setup.");
        System.out.println("--- Finished setUp ---");
    }

    @AfterEach
    void tearDown() {
        // Оставляем пустым, т.к. @DirtiesContext(AFTER_CLASS) и hbm2ddl=create-drop
        // позаботятся об очистке после всех тестов класса.
        // Явная очистка здесь может быть избыточной и замедлить тесты.
        System.out.println("--- Finished test method, tearDown called (no explicit cleanup) ---");
    }

    // Вспомогательный метод для очистки кэша
    private void clearCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            System.out.println("Cache cleared: " + cacheName);
        } else {
            System.out.println("Cache not found for clearing: " + cacheName);
        }
    }

    @Test
    @DisplayName("findByDni should hit cache on second call with same DNI")
    void findByDni_shouldCacheResult() {
        System.out.println("First call to findByDni for DNI: " + TEST_DNI_1);
        final Optional<User> userOpt1 = userRepositoryAdapter.findByDni(TEST_DNI_1);

        assertThat(userOpt1).isPresent();
        assertThat(userOpt1.get().dni()).isEqualTo(TEST_DNI_1);
        verify(targetUserJpaRepositorySpy, times(1))
                .findByDni(eq(TEST_DNI_1));

        final Cache cache = cacheManager.getCache(CACHE_NAME_DNI);
        assertThat(cache).isNotNull();
        assertThat(cache.get(TEST_DNI_1)).isNotNull();
        assertThat(cache.get(TEST_DNI_1).get()).isInstanceOf(User.class);
        System.out.println("Cache state after first call for DNI " + TEST_DNI_1 + ": "
                + cache.get(TEST_DNI_1).get());

        System.out.println("Second call to findByDni for DNI: " + TEST_DNI_1);
        final Optional<User> userOpt2 = userRepositoryAdapter.findByDni(TEST_DNI_1);
        assertThat(userOpt2).isPresent();
        assertThat(userOpt2).usingRecursiveComparison().isEqualTo(userOpt1);
        verify(targetUserJpaRepositorySpy, times(1))
                .findByDni(eq(TEST_DNI_1));
        System.out.println("Verified repository findByDni called only once for DNI: "
                + TEST_DNI_1
        );
        System.out.println("Third call to findByDni for DNI: " + TEST_DNI_2);
        Optional<User> userOpt3 = userRepositoryAdapter.findByDni(TEST_DNI_2);
        assertThat(userOpt3).isPresent();
        assertThat(userOpt3.get().dni()).isEqualTo(TEST_DNI_2);
        verify(targetUserJpaRepositorySpy, times(1))
                .findByDni(eq(TEST_DNI_2));
        verify(targetUserJpaRepositorySpy, times(2))
                .findByDni(any(String.class));
        System.out.println("Verified repository findByDni called once for DNI: " + TEST_DNI_2);
    }

    @Test
    @DisplayName("findById should hit cache on second call with same ID")
    void findById_shouldCacheResult() {
        System.out.println("First call to findById for ID: " + TEST_USER_ID_1);
        final Optional<User> userOpt1 = userRepositoryAdapter.findById(TEST_USER_ID_1);

        assertThat(userOpt1).isPresent();
        assertThat(userOpt1.get().id()).isEqualTo(TEST_USER_ID_1);
        verify(targetUserJpaRepositorySpy, times(1))
                .findById(eq(TEST_USER_ID_1));

        final Cache cache = cacheManager.getCache(CACHE_NAME_ID);
        assertThat(cache).isNotNull();
        assertThat(cache.get(TEST_USER_ID_1)).isNotNull();
        assertThat(cache.get(TEST_USER_ID_1).get()).isInstanceOf(User.class);
        System.out.println("Cache state after first call for ID " + TEST_USER_ID_1 + ": "
                + cache.get(TEST_USER_ID_1).get()
        );

        System.out.println("Second call to findById for ID: " + TEST_USER_ID_1);
        final Optional<User> userOpt2 = userRepositoryAdapter.findById(TEST_USER_ID_1);

        assertThat(userOpt2).isPresent();
        assertThat(userOpt2).usingRecursiveComparison().isEqualTo(userOpt1);
        verify(targetUserJpaRepositorySpy, times(1))
                .findById(eq(TEST_USER_ID_1));
        System.out.println("Verified repository findById called only once for ID: "
                + TEST_USER_ID_1
        );
    }


    @Test
    @DisplayName("findByDni should miss cache after eviction")
    void findByDni_shouldMissCacheAfterEviction() {
        System.out.println("Populating cache for DNI: " + TEST_DNI_1);
        userRepositoryAdapter.findByDni(TEST_DNI_1);
        verify(targetUserJpaRepositorySpy, times(1))
                .findByDni(eq(TEST_DNI_1));
        final Cache cache = cacheManager.getCache(CACHE_NAME_DNI);
        assertThat(cache).isNotNull();
        assertThat(cache.get(TEST_DNI_1)).isNotNull();

        System.out.println("Evicting cache for DNI: " + TEST_DNI_1);
        cache.evict(TEST_DNI_1);
        assertThat(cache.get(TEST_DNI_1)).isNull();

        System.out.println("Calling findByDni again after eviction for DNI: " + TEST_DNI_1);
        final Optional<User> userOptAfterEvict = userRepositoryAdapter.findByDni(TEST_DNI_1);

        assertThat(userOptAfterEvict).isPresent();
        verify(targetUserJpaRepositorySpy, times(2))
                .findByDni(eq(TEST_DNI_1));
        System.out.println("Verified repository findByDni called again after eviction for DNI: "
                + TEST_DNI_1
        );
    }

    @Test
    @DisplayName("findByDni should miss cache after clear")
    void findByDni_shouldMissCacheAfterClear() {
        System.out.println("Populating cache for DNI: " + TEST_DNI_1);
        userRepositoryAdapter.findByDni(TEST_DNI_1);
        verify(targetUserJpaRepositorySpy, times(1))
                .findByDni(eq(TEST_DNI_1));
        Cache cache = cacheManager.getCache(CACHE_NAME_DNI);
        assertThat(cache).isNotNull();
        assertThat(cache.get(TEST_DNI_1)).isNotNull();

        System.out.println("Clearing cache: " + CACHE_NAME_DNI);
        cache.clear();
        assertThat(cache.get(TEST_DNI_1)).isNull();

        System.out.println("Calling findByDni again after clear for DNI: " + TEST_DNI_1);
        final Optional<User> userOptAfterClear = userRepositoryAdapter.findByDni(TEST_DNI_1);

        assertThat(userOptAfterClear).isPresent();
        verify(targetUserJpaRepositorySpy, times(2))
                .findByDni(eq(TEST_DNI_1));
        System.out.println("Verified repository findByDni called again after clear for DNI: "
                + TEST_DNI_1
        );
    }
}