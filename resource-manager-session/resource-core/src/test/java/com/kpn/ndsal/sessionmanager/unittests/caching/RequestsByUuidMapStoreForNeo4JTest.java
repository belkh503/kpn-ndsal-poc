package com.kpn.ndsal.sessionmanager.unittests.caching;

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
import com.kpn.ndsal.sessionmanager.caching.RequestsByUuidMapStoreForNeo4J;
import com.kpn.ndsal.sessionmanager.entity.InternalRequest;
import com.kpn.ndsal.sessionmanager.persistence.entity.RequestsByUuidEntity;
import com.kpn.ndsal.sessionmanager.persistence.repository.RequestsByUuidRepository;

import lombok.SneakyThrows;

@SpringBootTest(classes = { RequestsByUuidMapStoreForNeo4J.class })
class RequestsByUuidMapStoreForNeo4JTest {

    @MockitoBean
    private ObjectMapper objectMapper;

    @MockitoBean
    private RequestsByUuidRepository repository;

    @Autowired
    private RequestsByUuidMapStoreForNeo4J mapStoreForNeo4J;

    @SneakyThrows
    @Test
    void givenInternalRequest_whenSaved_thenSuccess() {
        // given
        var key = UUID.randomUUID();
        var internalRequest = mock(InternalRequest.class);
        var captor = ArgumentCaptor.forClass(RequestsByUuidEntity.class);

        // when
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repository.save(captor.capture())).thenReturn(mock(RequestsByUuidEntity.class));

        mapStoreForNeo4J.store(key, internalRequest);

        // then
        assertThat(captor.getValue().getPid()).isEqualTo(key);
        assertThat(captor.getValue().getPayload()).isEqualTo("{}");
        verify(repository, times(1)).save(any());
    }

    @SneakyThrows
    @Test
    void givenInternalRequest_whenSaveAll_thenSuccess() {
        // given
        var map = new HashMap<UUID, InternalRequest>() {{
            put(UUID.randomUUID(), mock(InternalRequest.class));
            put(UUID.randomUUID(), mock(InternalRequest.class));
        }};

        var captor = ArgumentCaptor.forClass(RequestsByUuidEntity.class);

        // when
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repository.save(captor.capture())).thenReturn(mock(RequestsByUuidEntity.class));

        mapStoreForNeo4J.storeAll(map);

        // then
        assertThat(captor.getAllValues().stream()
                .map(RequestsByUuidEntity::getPid)
                .toList())
                .filteredOn(map::containsKey)
                .hasSize(2);
        assertThat(captor.getAllValues().stream()
                .map(RequestsByUuidEntity::getPayload)
                .toList())
                .filteredOn(p -> p.equals("{}"))
                .hasSize(2);
        verify(repository, times(2)).save(any());
    }

    @Test
    void givenInternalRequest_whenDeleted_thenSuccess() {
        // given
        var key = UUID.randomUUID();
        var captor = ArgumentCaptor.forClass(UUID.class);

        // when
        doNothing().when(repository).deleteById(captor.capture());

        mapStoreForNeo4J.delete(key);

        // then
        assertThat(captor.getValue()).isEqualTo(key);
        verify(repository, times(1)).deleteById(any());
    }

    @Test
    void givenInternalRequest_whenDeleteAll_thenSuccess() {
        // given
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        // when
        doNothing().when(repository).deleteById(any());

        mapStoreForNeo4J.deleteAll(ids);

        // then
        verify(repository, times(ids.size())).deleteById(any());
    }

    @SneakyThrows
    @Test
    void givenInternalRequest_whenLoad_thenSuccess() {
        // given
        var key = UUID.randomUUID();
        var captor = ArgumentCaptor.forClass(UUID.class);

        // when
        when(repository.findByPid(captor.capture())).thenReturn(Optional.of(mock(RequestsByUuidEntity.class)));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(mock(InternalRequest.class));

        mapStoreForNeo4J.load(key);

        // then
        assertThat(captor.getValue()).isEqualTo(key);
        verify(repository, times(1)).findByPid(key);
    }

    @SneakyThrows
    @Test
    void givenInternalRequest_whenLoadNonExisting_thenSuccess() {
        // given
        var key = UUID.randomUUID();
        var captor = ArgumentCaptor.forClass(UUID.class);

        // when
        when(repository.findByPid(captor.capture())).thenReturn(Optional.empty());

        var internalRequest = mapStoreForNeo4J.load(key);

        // then
        assertThat(captor.getValue()).isEqualTo(key);
        assertThat(internalRequest).isNull();
        verify(repository, times(1)).findByPid(key);
        verify(objectMapper, never()).readValue(anyString(), any(TypeReference.class));
    }

    @SneakyThrows
    @Test
    void givenInternalRequest_whenLoadAll_thenSuccess() {
        // given
        var captor = ArgumentCaptor.forClass(UUID.class);
        var ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        // when
        when(repository.findByPid(captor.capture())).thenReturn(Optional.of(mock(RequestsByUuidEntity.class)));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(mock(InternalRequest.class));

        var result = mapStoreForNeo4J.loadAll(ids);

        // then
        verify(repository, times(ids.size())).findByPid(any());
        assertThat(result)
                .isNotNull()
                .hasSize(ids.size());
    }

    @SneakyThrows
    @Test
    void givenInternalRequest_whenLoadAllKeys_thenSuccess() {
        // when
        when(repository.findAll()).thenReturn(List.of(mock(RequestsByUuidEntity.class), mock(RequestsByUuidEntity.class)));

        var result = mapStoreForNeo4J.loadAllKeys();

        // then
        verify(repository, times(1)).findAll();
        assertThat(result)
                .isNotNull()
                .hasSize(2);
    }
}
