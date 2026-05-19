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

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpn.ndsal.sessionmanager.caching.SessionByTripletMapStoreForNeo4J;
import com.kpn.ndsal.sessionmanager.entity.SessionEntity;
import com.kpn.ndsal.sessionmanager.persistence.entity.SessionByTripletEntity;
import com.kpn.ndsal.sessionmanager.persistence.repository.SessionByTripletRepository;

import lombok.SneakyThrows;

@SpringBootTest(classes = { SessionByTripletMapStoreForNeo4J.class })
class SessionByTripletMapStoreForNeo4JTest {

    @MockitoBean
    private ObjectMapper objectMapper;

    @MockitoBean
    private SessionByTripletRepository repository;

    @Autowired
    private SessionByTripletMapStoreForNeo4J mapStoreForNeo4J;

    @SneakyThrows
    @Test
    void givenSessionEntity_whenSaved_thenSuccess() {
        // given
        var sessionEntity = mock(SessionEntity.class);
        var captor = ArgumentCaptor.forClass(SessionByTripletEntity.class);

        // when
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repository.save(captor.capture())).thenReturn(mock(SessionByTripletEntity.class));

        mapStoreForNeo4J.store("test", sessionEntity);

        // then;
        assertThat(captor.getValue().getPid()).isEqualTo("test");
        assertThat(captor.getValue().getPayload()).isEqualTo("{}");
        verify(repository, times(1)).save(any());
    }

    @SneakyThrows
    @Test
    void givenSessionEntity_whenSaveAll_thenSuccess() {
        // given
        var map = new HashMap<String, SessionEntity>() {{
            put("test1", mock(SessionEntity.class));
            put("test2", mock(SessionEntity.class));
        }};

        var captor = ArgumentCaptor.forClass(SessionByTripletEntity.class);

        // when
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(repository.save(captor.capture())).thenReturn(mock(SessionByTripletEntity.class));

        mapStoreForNeo4J.storeAll(map);

        // then;
        assertThat(captor.getAllValues().stream()
                .map(SessionByTripletEntity::getPid)
                .toList())
                .filteredOn(map::containsKey)
                .hasSize(2);
        assertThat(captor.getAllValues().stream()
                .map(SessionByTripletEntity::getPayload)
                .toList())
                .filteredOn(p -> p.equals("{}"))
                .hasSize(2);
        verify(repository, times(2)).save(any());
    }

    @Test
    void givenSessionEntity_whenDeleted_thenSuccess() {
        // given
        var captor = ArgumentCaptor.forClass(String.class);

        // when
        doNothing().when(repository).deleteById(captor.capture());

        mapStoreForNeo4J.delete("test");

        // then;
        assertThat(captor.getValue()).isEqualTo("test");
        verify(repository, times(1)).deleteById(any());
    }

    @Test
    void givenSessionEntity_whenDeleteAll_thenSuccess() {
        // given
        var ids = List.of("test1", "test2");

        // when
        doNothing().when(repository).deleteById(any());

        mapStoreForNeo4J.deleteAll(ids);

        // then
        verify(repository, times(ids.size())).deleteById(any());
    }

    @SneakyThrows
    @Test
    void givenSessionEntity_whenLoad_thenSuccess() {
        // given
        var captor = ArgumentCaptor.forClass(String.class);

        // when
        when(repository.findByPid(captor.capture())).thenReturn(Optional.of(mock(SessionByTripletEntity.class)));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(mock(SessionEntity.class));

        mapStoreForNeo4J.load("test");

        // then;
        assertThat(captor.getValue()).isEqualTo("test");
        verify(repository, times(1)).findByPid("test");
    }

    @SneakyThrows
    @Test
    void givenSessionEntity_whenLoadNonExisting_thenSuccess() {
        // given
        var captor = ArgumentCaptor.forClass(String.class);

        // when
        when(repository.findByPid(captor.capture())).thenReturn(Optional.empty());

        var sessionEntity = mapStoreForNeo4J.load("test");

        // then;
        assertThat(captor.getValue()).isEqualTo("test");
        assertThat(sessionEntity).isNull();
        verify(repository, times(1)).findByPid("test");
        verify(objectMapper, never()).readValue(anyString(), any(TypeReference.class));
    }

    @SneakyThrows
    @Test
    void givenSessionEntity_whenLoadAll_thenSuccess() {
        // given
        var captor = ArgumentCaptor.forClass(String.class);
        var ids = List.of("test1", "test2");

        // when
        when(repository.findByPid(captor.capture())).thenReturn(Optional.of(mock(SessionByTripletEntity.class)));
        when(objectMapper.readValue(anyString(), any(TypeReference.class))).thenReturn(mock(SessionEntity.class));

        var result = mapStoreForNeo4J.loadAll(ids);

        // then;
        verify(repository, times(ids.size())).findByPid(any());
        assertThat(result)
                .isNotNull()
                .hasSize(ids.size());
    }

    @SneakyThrows
    @Test
    void givenSessionEntity_whenLoadAllKeys_thenSuccess() {
        // when
        when(repository.findAll()).thenReturn(List.of(mock(SessionByTripletEntity.class), mock(SessionByTripletEntity.class)));

        var result = mapStoreForNeo4J.loadAllKeys();

        // then;
        verify(repository, times(1)).findAll();
        assertThat(result)
                .isNotNull()
                .hasSize(2);
    }

}
