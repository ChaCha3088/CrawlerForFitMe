package com.diva.batch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.*;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class Category {
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "CATEGORY_ID")
    private Long id;

    @NotBlank
    @Column(unique = true, name = "CATEGORY_NAME")
    private String name;

    @NotBlank
    @Column(name = "CRAWLING_URL")
    private String crawlingUrl;

    @NotNull
    @OneToMany(mappedBy = "category")
    private List<Song> songs = new ArrayList<>();

    @Builder
    protected Category(Long id, String name, String crawlingUrl) {
        this.id = id;
        this.name = name;
        this.crawlingUrl = crawlingUrl;
    }

    // == 연관관계 편의 메서드 == //
    public void addSong(Song song) {
        this.songs.add(song);
    }
}
