package com.bobocode.dao;

import com.bobocode.model.Photo;
import com.bobocode.model.PhotoComment;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Without auto-commit mode.
 */

public class PhotoDaoImpl implements PhotoDao {

    private EntityManagerFactory entityManagerFactory;

    public PhotoDaoImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public void save(Photo photo) {
        perform(entityManager -> entityManager.persist(photo));
    }

    @Override
    public Photo findById(long id) {
        return performWith(entityManager -> entityManager.createQuery("select p from Photo p where p.id = :id", Photo.class)
                .setParameter("id", id)
                .getSingleResult()
        );
    }

    @Override
    public void remove(Photo photo) {
          perform(entityManager -> entityManager.remove(entityManager.merge(photo)));
    }

    @Override
    public List<Photo> findAll() {
        return performWith(entityManager -> entityManager.createQuery("select p from Photo p", Photo.class)
                .getResultList()
        );
    }

    @Override
    public void addComment(long photoId, String comment) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            Photo photo = entityManager.find(Photo.class, photoId);
            PhotoComment photoComment = new PhotoComment();
            photoComment.setText(comment);
            photo.addComment(photoComment);
            entityManager.merge(photo);
            transaction.commit();
        }catch (Exception ex){
            transaction.rollback();
        }
    }

    public void perform(Consumer<EntityManager> operation) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            operation.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
        } finally {
            entityManager.close();
        }
    }

    public  <T>  T performWith(Function<EntityManager, T> entityManagerFunction) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            T result = entityManagerFunction.apply(entityManager);
            entityManager.getTransaction().commit();
            return result;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new RuntimeException("Error. Transaction was rolled back");
        } finally {
            entityManager.close();
        }
    }

}
