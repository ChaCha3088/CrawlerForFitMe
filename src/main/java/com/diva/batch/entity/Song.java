package com.diva.batch.entity;

import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Song extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "song_id")
    private Long id;

    @NotBlank
    @Column(name = "song_title", length = 50)
    private String title;

    @NotBlank
    @Column(name = "artist", length = 50)
    private String artist;

//    @NotBlank
    @Column(name = "cover_img", length = 200)
    private String coverImg;

//    @NotBlank
    @Column(name = "lyrics", length = 10000)
    private String lyric;

//    @NotBlank
    @Column(name = "mr_url", length = 300)
    private String mrUrl;

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "song_range_id")
    private SongRange songRange;

    @Builder
    protected Song(String title, String artist, String mrUrl, SongRange songRange) {
        this.title = title;
        this.artist = artist;
        this.mrUrl = mrUrl;
        this.songRange = songRange;
        if (songRange != null) {
            songRange.setSong(this);
        }
    }
}
