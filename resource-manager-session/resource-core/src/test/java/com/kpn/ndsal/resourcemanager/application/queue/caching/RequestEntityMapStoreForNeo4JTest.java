package com.kpn.ndsal.resourcemanager.application.queue.caching;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueDatabaseEntity;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntityRepository;

import lombok.SneakyThrows;

@SpringBootTest(classes = {RequestEntityMapStoreForNeo4J.class})
class RequestEntityMapStoreForNeo4JTest {

    @MockitoBean
    private ObjectMapper objectMapper;

    @MockitoBean
    private RequestQueueEntityRepository repository;

    @Autowired
    private RequestEntityMapStoreForNeo4J mapStoreForNeo4J;

    @SneakyThrows
    @Test
    void givenRequestQueueEntity_whenSaved_thenSuccess() {
        var key = UUID.randomUUID();
        var internalRequest = mock(RequestQueueEntity.class);
        var captor = ArgumentCaptor.forClass(RequestQueueDatabaseEntity.class);

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repository.save(captor.capture())).thenReturn(mock(RequestQueueDatabaseEntity.class));

        mapStoreForNeo4J.store(key, internalRequest);

        assertThat(captor.getValue().getPid()).isEqualTo(key);
        assertThat(captor.getValue().getPayload()).isEqualTo("{}");
        verify(repository, times(1)).save(any());
    }

    @SneakyThrows
    @Test
    void givenRequestQueueEntity_whenSaveAll_thenSuccess() {

        var map = new HashMap<UUID, RequestQueueEntity>() {{
            put(UUID.randomUUID(), mock(RequestQueueEntity.class));
            put(UUID.randomUUID(), mock(RequestQueueEntity.class));
        }};

        var captor = ArgumentCaptor.forClass(RequestQueueDatabaseEntity.class);

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repository.save(captor.capture())).thenReturn(mock(RequestQueueDatabaseEntity.class));

        mapStoreForNeo4J.storeAll(map);

        assertThat(captor.getAllValues().stream()
                .map(RequestQueueDatabaseEntity::getPid)
                .toList())
                .filteredOn(map::containsKey)
                .hasSize(2);
        assertThat(captor.getAllValues().stream()
                .map(RequestQueueDatabaseEntity::getPayload)
                .toList())
                .filteredOn(p -> p.equals("{}"))
                .hasSize(2);
        verify(repository, times(2)).save(any());
    }

    @Test
    void givenRequestQueueEntity_whenDeleted_thenSuccess() {

        var key = UUID.randomUUID();
        var captor = ArgumentCaptor.forClass(UUID.class);

        doNothing().when(repository).deleteById(captor.capture());

        mapStoreForNeo4J.delete(key);

        assertThat(captor.getValue()).isEqualTo(key);
        verify(repository, times(1)).deleteById(any());
    }

    @Test
    void givenRequestQueueEntity_whenDeleteAll_thenSuccess() {

        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        doNothing().when(repository).deleteById(any());

        mapStoreForNeo4J.deleteAll(ids);

        verify(repository, times(ids.size())).deleteById(any());
    }

    @SneakyThrows
    @Test
    void givenRequestQueueEntity_whenLoad_thenSuccess() {

        var key = UUID.randomUUID();
        var captor = ArgumentCaptor.forClass(UUID.class);

        when(repository.findByPid(captor.capture())).thenReturn(Optional.of(mock(RequestQueueDatabaseEntity.class)));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(mock(RequestQueueEntity.class));

        mapStoreForNeo4J.load(key);

        assertThat(captor.getValue()).isEqualTo(key);
        verify(repository, times(1)).findByPid(key);
    }

    @SneakyThrows
    @Test
    void givenRequestQueueEntity_whenLoadNonExisting_thenSuccess() {

        var key = UUID.randomUUID();
        var captor = ArgumentCaptor.forClass(UUID.class);

        when(repository.findByPid(captor.capture())).thenReturn(Optional.empty());

        var internalRequest = mapStoreForNeo4J.load(key);

        assertThat(captor.getValue()).isEqualTo(key);
        assertThat(internalRequest).isNull();
        verify(repository, times(1)).findByPid(key);
        verify(objectMapper, never()).readValue(anyString(), any(TypeReference.class));
    }

    @SneakyThrows
    @Test
    void givenRequestQueueEntity_whenLoadAll_thenSuccess() {

        var captor = ArgumentCaptor.forClass(UUID.class);
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        when(repository.findByPid(captor.capture())).thenReturn(Optional.of(mock(RequestQueueDatabaseEntity.class)));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(mock(RequestQueueEntity.class));

        var result = mapStoreForNeo4J.loadAll(ids);

        verify(repository, times(ids.size())).findByPid(any());
        assertThat(result)
                .isNotNull()
                .hasSize(ids.size());
    }

    @SneakyThrows
    @Test
    void givenRequestQueueEntity_whenLoadAllKeys_thenSuccess() {

        when(repository.findAll()).thenReturn(List.of(mock(RequestQueueDatabaseEntity.class), mock(RequestQueueDatabaseEntity.class)));

        var result = mapStoreForNeo4J.loadAllKeys();

        verify(repository, times(1)).findAll();
        assertThat(result)
                .isNotNull()
                .hasSize(2);
    }

}

