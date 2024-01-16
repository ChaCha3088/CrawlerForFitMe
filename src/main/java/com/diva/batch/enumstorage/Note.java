package com.diva.batch.enumstorage;

import lombok.Getter;

@Getter
public enum Note {
    C1("C1")
    ;

    private final String note;

    private Note (String note) {
        this.note = note;
    }
}
