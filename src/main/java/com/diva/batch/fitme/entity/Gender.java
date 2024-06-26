package com.diva.batch.fitme.entity;

import com.diva.batch.fitme.exception.GenderBadRequestException;
import lombok.Getter;

@Getter
public enum Gender {
    MALE(0), FEMALE(1), UNISEX(2);

    private final int value;

    Gender(int value) {
        this.value = value;
    }

    public static Gender fromValue(int value) {
        for (Gender gender : Gender.values()) {
            if (gender.getValue() == value) {
                return gender;
            }
        }
        throw new GenderBadRequestException();
    }
}
