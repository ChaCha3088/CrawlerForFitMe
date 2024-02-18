package com.diva.batch.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSongOld is a Querydsl query type for SongOld
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSongOld extends EntityPathBase<SongOld> {

    private static final long serialVersionUID = -1223093780L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSongOld songOld = new QSongOld("songOld");

    public final StringPath albumUrl = createString("albumUrl");

    public final StringPath artist = createString("artist");

    public final QCategory category;

    public final StringPath highestNote = createString("highestNote");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath lowestNote = createString("lowestNote");

    public final StringPath mrUrl = createString("mrUrl");

    public final StringPath title = createString("title");

    public final NumberPath<Long> tjId = createNumber("tjId", Long.class);

    public final QYoutubeFile youtubeFile;

    public QSongOld(String variable) {
        this(SongOld.class, forVariable(variable), INITS);
    }

    public QSongOld(Path<? extends SongOld> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSongOld(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSongOld(PathMetadata metadata, PathInits inits) {
        this(SongOld.class, metadata, inits);
    }

    public QSongOld(Class<? extends SongOld> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new QCategory(forProperty("category")) : null;
        this.youtubeFile = inits.isInitialized("youtubeFile") ? new QYoutubeFile(forProperty("youtubeFile"), inits.get("youtubeFile")) : null;
    }

}

