package com.diva.batch.fitme.exception;

public class GenderBadRequestException extends BadRequestException {

    private static final String MESSAGE = "성별은 0(남자), 1(여자), 2(남녀 겸용)만 가능합니다.";

    public GenderBadRequestException() {
        super(MESSAGE);
    }
}
