package com.kpn.ndsal.resourcemanager.adapter.out.persistence;

import static com.kpn.ndsal.resourcemanager.domain.LockRequest.LockType.EXCLUSIVE;
import static com.kpn.ndsal.resourcemanager.domain.LockRequest.LockType.SHARED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.kpn.ndsal.resourcemanager.domain.LockRequest;

@SpringBootTest
@ActiveProfiles("test")
class LockRequestRepositoryTestIT extends BaseNeo4jTestConfig {

    @Autowired
    private LockRequestRepository lockRequestRepository;

    @Autowired
    private LockObjectRepository lockObjectRepository;
    private final LockRequestMapper lockRequestMapper = new LockRequestMapperImpl();

    private static final Supplier<LockRequest> LOCKED_RESOURCES = () -> LockRequest.builder()
            .request("{}")
            .created(LocalDateTime.now())
            .domain("BCPE")
            .lockObjectEntities(List.of(
                    LockRequest.LockObject.builder()
                            .name("NODE1")
                            .type("ENE")
                            .lockType(SHARED)
                            .build(),
                    LockRequest.LockObject.builder()
                            .name("NODE2")
                            .type("ENE")
                            .lockType(EXCLUSIVE)
                            .build(),
                    LockRequest.LockObject.builder()
                            .name("NODE3")
                            .type("ENE")
                            .lockType(EXCLUSIVE)
                            .build()
            )).build();

    LockRequestEntity lockRequestEntity;

    @BeforeEach
    void setup() {
        cleanupNeo4jDb();

        var resources = lockRequestMapper.map(LOCKED_RESOURCES.get());
        lockRequestEntity = lockRequestRepository.save(resources);
    }

    @Test
    void test() {
        assertThat(lockRequestRepository.findAll()).asList().hasSize(1);
    }

    @Test
    void deleteLockedRequestEntity() {
        assertThat(lockRequestRepository.findAll()).asList().hasSize(1);
        lockRequestRepository.deleteLockRequestEntity(lockRequestEntity.getId());
        assertThat(lockObjectRepository.findAll()).asList().isEmpty();
        assertThat(lockRequestRepository.findAll()).asList().isEmpty();
    }

    @Override
    public LockRequestRepository getLockRequestRepository() {
        return this.lockRequestRepository;
    }
}
