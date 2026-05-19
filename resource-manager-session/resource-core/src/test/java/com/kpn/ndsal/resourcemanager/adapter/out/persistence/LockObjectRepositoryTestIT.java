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
class LockObjectRepositoryTestIT extends BaseNeo4jTestConfig {

    @Autowired
    private LockObjectRepository lockObjectRepository;

    @Autowired
    private LockRequestRepository lockRequestRepository;

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
                            .type("EAS")
                            .lockType(EXCLUSIVE)
                            .build(),
                    LockRequest.LockObject.builder()
                            .name("NODE3")
                            .type("EVA")
                            .lockType(EXCLUSIVE)
                            .build()
            )).build();

    @BeforeEach
    void setup() {
        cleanupNeo4jDb();
    }

    @Test
    void findByDomainAndLockObjectEntitiesNameIn_happy() {

        var resources = lockRequestMapper.map(LOCKED_RESOURCES.get());
        lockRequestRepository.save(resources);
        List<String> lockedEntitiesNames = LOCKED_RESOURCES.get().getLockObjectEntities().stream()
                .map(LockRequest.LockObject::getName).toList();
        var lockedObjects = lockObjectRepository.findByDomainAndLockObjectEntitiesNameIn("BCPE", lockedEntitiesNames);
        assertThat(lockedObjects).asList().isNotEmpty();
        assertThat(lockedObjects).asList().hasSize(3);
    }

    @Override
    public LockRequestRepository getLockRequestRepository() {
        return this.lockRequestRepository;
    }
}
