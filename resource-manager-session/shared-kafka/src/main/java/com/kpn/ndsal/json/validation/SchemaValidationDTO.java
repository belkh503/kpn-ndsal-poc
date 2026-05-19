package com.kpn.ndsal.json.validation;

public class SchemaValidationDTO {
    private final String schemaLocation;

    public SchemaValidationDTO(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }
}
