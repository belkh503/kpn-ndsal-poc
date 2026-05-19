package com.kpn.ndsal.sessionmanager.model;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionReleaseRequestDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<UUID> uuids;
}
