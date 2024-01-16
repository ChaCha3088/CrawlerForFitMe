package com.diva.batch.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class YoutubeFile {
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "YOUTUBE_FILE_ID")
    private Long id;

    @Column(name = "YOUTUBE_FILE_URL")
    private String url;

    @OneToOne(mappedBy = "youtubeFile")
    private Song song;

    @Builder
    protected YoutubeFile(Long id, String url, Song song) {
        this.id = id;
        this.url = url;
        this.song = song;

        this.song.addYoutubeFile(this);
    }
}
