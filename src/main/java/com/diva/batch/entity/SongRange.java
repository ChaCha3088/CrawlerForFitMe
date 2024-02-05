package com.diva.batch.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SongRange extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "song_range_id")
    private Long id;

    @Column(name = "highest_note", length = 10)
    private String highestNote;

    @Column(name = "highest_midi")
    private Integer highestMidi;

    @OneToOne(mappedBy = "songRange")
    private Song song;

    @Builder
    protected SongRange(String highestNote, Integer highestMidi) {
        this.highestNote = highestNote;
        this.highestMidi = highestMidi;
    }

    //== 연관관계 메서드 ==//
    public void setSong(Song song) {
        this.song = song;
    }
}
