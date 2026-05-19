package com.kpn.ndsal.resourcemanager;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

@ToString
@Builder
class LockRequest {

    @JsonProperty("domain")
    @NotBlank(message = "Should be not blank")
    public String domainName;

    @NotNull
    @NotEmpty
    public List<LockGroup> lockGroups;

}

@ToString
@Builder
class LockGroup {

    @NotNull
    @NotEmpty
    public List<LockObject> lockObjects;

}

@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
class LockObject {

    @NotBlank(message = "Should be not blank")
    public String type;

    @NotBlank(message = "Should be not blank")
    public String id;

    @Null(message = "Optional value")
    public Boolean force;

    public LockObject(String type, String id) {
        this.type = type;
        this.id = id;
    }
}
