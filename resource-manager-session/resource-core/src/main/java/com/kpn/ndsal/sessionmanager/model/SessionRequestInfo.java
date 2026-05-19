package com.kpn.ndsal.sessionmanager.model;

import com.kpn.ndsal.sessionmanager.entity.TripletEntity;
import lombok.Value;

import jakarta.validation.constraints.NotNull;

@Value
public class SessionRequestInfo {
    @NotNull TripletEntity triplet;
    @NotNull int sessionsWanted;
    @NotNull int maximumAllowedSessions;
}
