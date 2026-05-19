package com.kpn.ndsal.resourcemanager.application.configuration;

import com.kpn.ndsal.json.validation.SchemaValidationDTO;
import com.kpn.ndsal.resourcemanager.model.AcquireLockRequestDto;
import com.kpn.ndsal.resourcemanager.model.DeleteLockRequestDto;
import com.kpn.ndsal.resourcemanager.model.GetLockStatusRequestDto;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto;
import com.kpn.ndsal.sessionmanager.model.SessionReleaseRequestDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * Merged configuration class for JSON schema validation covering both resource-manager
 * and session-manager DTOs.
 */
@Configuration
public class JsonSchemaValidationConfig {

    private static final String ACQUIRE_LOCK_REQUEST_SCHEMA_LOCATION = "com/kpn/ndsal/resourcemanager/json_schemas/acquire_lock_request_dto.json";
    private static final String DELETE_LOCK_REQUEST_SCHEMA_LOCATION = "com/kpn/ndsal/resourcemanager/json_schemas/delete_lock_request_dto.json";
    private static final String GET_LOCK_STATUS_REQUEST_SCHEMA_LOCATION = "com/kpn/ndsal/resourcemanager/json_schemas/get_lock_status_request_dto.json";
    private static final String SESSION_RELEASE_REQUEST_SCHEMA_LOCATION = "com/kpn/ndsal/sessionmanager/json_schemas/session_release_request_dto.json";
    private static final String SESSION_ACQUIRE_REQUEST_SCHEMA_LOCATION = "com/kpn/ndsal/sessionmanager/json_schemas/session_acquire_request_dto.json";

    @Bean
    public Map<Class<?>, SchemaValidationDTO> schemaValidationDTOMap() {
        return Map.of(
                AcquireLockRequestDto.class, new SchemaValidationDTO(ACQUIRE_LOCK_REQUEST_SCHEMA_LOCATION),
                DeleteLockRequestDto.class, new SchemaValidationDTO(DELETE_LOCK_REQUEST_SCHEMA_LOCATION),
                GetLockStatusRequestDto.class, new SchemaValidationDTO(GET_LOCK_STATUS_REQUEST_SCHEMA_LOCATION),
                SessionReleaseRequestDto.class, new SchemaValidationDTO(SESSION_RELEASE_REQUEST_SCHEMA_LOCATION),
                SessionAcquireRequestDto.class, new SchemaValidationDTO(SESSION_ACQUIRE_REQUEST_SCHEMA_LOCATION)
        );
    }
}
