package com.diva.batch.querydsl.itemreader;

import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.batch.item.database.orm.JpaQueryProvider;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.util.ClassUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class QuerydslPagingItemReader<T> extends AbstractPagingItemReader<T> {
    protected EntityManagerFactory entityManagerFactory;
    protected EntityManager entityManager;
    protected final Map<String, Object> jpaPropertyMap = new HashMap();
    private String queryString;
    private JpaQueryProvider queryProvider;
    private Map<String, Object> parameterValues;
    protected Function<JPAQueryFactory, JPAQuery<T>> queryFunction;
    protected boolean transacted = true;

    protected QuerydslPagingItemReader() {
        setName(ClassUtils.getShortName(QuerydslPagingItemReader.class));
    }

    public QuerydslPagingItemReader(EntityManagerFactory entityManagerFactory, int pageSize, Function<JPAQueryFactory, JPAQuery<T>> queryFunction) {
        this();

        this.entityManagerFactory = entityManagerFactory;
        this.queryFunction = queryFunction;

        setPageSize(pageSize);
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public void setParameterValues(Map<String, Object> parameterValues) {
        this.parameterValues = parameterValues;
    }

    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setQueryProvider(JpaQueryProvider queryProvider) {
        this.queryProvider = queryProvider;
    }

    @Override
    protected void doOpen() throws Exception {
        super.doOpen();

        this.entityManager = this.entityManagerFactory.createEntityManager(this.jpaPropertyMap);
        if (this.entityManager == null) {
            throw new DataAccessResourceFailureException("Unable to obtain an EntityManager");
        } else {
            if (this.queryProvider != null) {
                this.queryProvider.setEntityManager(this.entityManager);
            }

        }
    }

    @Override
    protected void doReadPage() {
        EntityTransaction tx = getTxOrNull();

        JPAQuery<T> query = createQuery()
                .offset(this.getPage() * this.getPageSize())
                .limit(this.getPageSize());

        initResults();

        fetchQuery(query, tx);
    }

    protected EntityTransaction getTxOrNull() {
        if (transacted) {
            EntityTransaction tx = entityManager.getTransaction();
            tx.begin();

            entityManager.flush();
            entityManager.clear();
            return tx;
        }

        return null;
    }

    protected JPAQuery<T> createQuery() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFunction.apply(queryFactory);
    }

    protected void initResults() {
        if (this.results == null) {
            this.results = new CopyOnWriteArrayList<>();
        } else {
            this.results.clear();
        }
    }

    protected void fetchQuery(JPQLQuery<T> query, EntityTransaction tx) {
        if (transacted) {
            results.addAll(query.fetch());
            if(tx != null) {
                tx.commit();
            }
        } else {
            List<T> queryResult = query.fetch();
            for (T entity : queryResult) {
                entityManager.detach(entity);
                results.add(entity);
            }
        }
    }

    @Override
    protected void jumpToItem(int itemIndex) {
    }

    @Override
    protected void doClose() throws Exception {
        this.entityManager.close();
        super.doClose();
    }
}
