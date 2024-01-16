package com.diva.batch.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSong is a Querydsl query type for Song
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSong extends EntityPathBase<Song> {

    private static final long serialVersionUID = 1596929595L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSong song = new QSong("song");

    public final StringPath albumUrl = createString("albumUrl");

    public final StringPath artist = createString("artist");

    public final QCategory category;

    public final EnumPath<com.diva.batch.enumstorage.Note> highestNote = createEnum("highestNote", com.diva.batch.enumstorage.Note.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.diva.batch.enumstorage.Note> lowestNote = createEnum("lowestNote", com.diva.batch.enumstorage.Note.class);

    public final StringPath mrUrl = createString("mrUrl");

    public final StringPath title = createString("title");

    public final NumberPath<Long> tjId = createNumber("tjId", Long.class);

    public final QYoutubeFile youtubeFile;

    public QSong(String variable) {
        this(Song.class, forVariable(variable), INITS);
    }

    public QSong(Path<? extends Song> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSong(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSong(PathMetadata metadata, PathInits inits) {
        this(Song.class, metadata, inits);
    }

    public QSong(Class<? extends Song> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new QCategory(forProperty("category")) : null;
        this.youtubeFile = inits.isInitialized("youtubeFile") ? new QYoutubeFile(forProperty("youtubeFile"), inits.get("youtubeFile")) : null;
    }

}

