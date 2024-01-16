package com.diva.batch.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QYoutubeFile is a Querydsl query type for YoutubeFile
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QYoutubeFile extends EntityPathBase<YoutubeFile> {

    private static final long serialVersionUID = 438080345L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QYoutubeFile youtubeFile = new QYoutubeFile("youtubeFile");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QSong song;

    public final StringPath url = createString("url");

    public QYoutubeFile(String variable) {
        this(YoutubeFile.class, forVariable(variable), INITS);
    }

    public QYoutubeFile(Path<? extends YoutubeFile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QYoutubeFile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QYoutubeFile(PathMetadata metadata, PathInits inits) {
        this(YoutubeFile.class, metadata, inits);
    }

    public QYoutubeFile(Class<? extends YoutubeFile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.song = inits.isInitialized("song") ? new QSong(forProperty("song"), inits.get("song")) : null;
    }

}

