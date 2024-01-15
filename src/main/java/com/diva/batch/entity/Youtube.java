package com.diva.batch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@NoArgsConstructor(access = PROTECTED)
public class Youtube {
    @Id @GeneratedValue(strategy = IDENTITY)
    @Column(name = "YOUTUBE_ID")
    private Long id;

    @Column(unique = true, name = "YOUTUBE_URL")
    private String url;

    @OneToOne(mappedBy = "youtube")
    private Song song;
}
