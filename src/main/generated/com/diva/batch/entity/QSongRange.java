package com.diva.batch.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSongRange is a Querydsl query type for SongRange
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSongRange extends EntityPathBase<SongRange> {

    private static final long serialVersionUID = 1430372290L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSongRange songRange = new QSongRange("songRange");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final StringPath createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final NumberPath<Long> genre = createNumber("genre", Long.class);

    public final NumberPath<Integer> highestMidi = createNumber("highestMidi", Integer.class);

    public final StringPath highestNote = createString("highestNote");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final StringPath lastModifiedBy = _super.lastModifiedBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> lastModifiedDate = _super.lastModifiedDate;

    public final QSong song;

    public QSongRange(String variable) {
        this(SongRange.class, forVariable(variable), INITS);
    }

    public QSongRange(Path<? extends SongRange> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSongRange(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSongRange(PathMetadata metadata, PathInits inits) {
        this(SongRange.class, metadata, inits);
    }

    public QSongRange(Class<? extends SongRange> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.song = inits.isInitialized("song") ? new QSong(forProperty("song"), inits.get("song")) : null;
    }

}

