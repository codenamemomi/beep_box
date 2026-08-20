package com.beepbox.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class ItemDto {

    private Long id;

    @NotBlank(message = "Item name is required")
    @Pattern(regexp = "^[a-zA-Z0-9\\-_]+$", message = "Item name can only contain letters, numbers, hyphen '-', and underscore '_'")
    private String name;

    @NotNull(message = "Item weight is required")
    @Positive(message = "Item weight must be positive")
    private Double weight;

    @NotBlank(message = "Item code is required")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Item code can only contain uppercase letters, numbers, and underscore '_'")
    private String code;

    public ItemDto() {
    }

    public ItemDto(String name, Double weight, String code) {
        this.name = name;
        this.weight = weight;
        this.code = code;
    }

    public ItemDto(Long id, String name, Double weight, String code) {
        this.id = id;
        this.name = name;
        this.weight = weight;
        this.code = code;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
