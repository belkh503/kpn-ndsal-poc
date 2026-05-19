package com.kpn.ndsal.sessionmanager.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public String errorMessage;
}
