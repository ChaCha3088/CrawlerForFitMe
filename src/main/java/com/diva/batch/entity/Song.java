package com.diva.batch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class Song {
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "SONG_ID")
    private Long id;

    @NotNull
    @Column(unique = true, name = "TJ_ID")
    private Long tjId;

    @NotBlank
    private String title;

    @NotBlank
    private String artist;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "CATEGORY_ID")
    private Category category;

    @Builder
    protected Song(Long id, Long tjId, String title, String artist, Category category) {
        this.id = id;
        this.tjId = tjId;
        this.title = title;
        this.artist = artist;

        addCategory(category);
    }

    // == 연관관계 편의 메서드 == //
    public void addCategory(Category category) {
        this.category = category;

        this.category.addSong(this);
    }
}
