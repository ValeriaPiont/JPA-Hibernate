package com.bobocode.dao;

import com.bobocode.exception.AccountDaoException;
import com.bobocode.model.Account;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class AccountDaoImpl implements AccountDao {
    private EntityManagerFactory emf;

    public AccountDaoImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void performWithoutReturning(Consumer<EntityManager> operation) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            operation.accept(entityManager);
            entityManager.getTransaction().commit();
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Error performing JPA operation. Transaction is rolled back", e);
        } finally {
            entityManager.close();
        }
    }

    public Account performWithReturning(Function<EntityManager, Account> operation) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            Account account = operation.apply(entityManager);
            entityManager.getTransaction().commit();
            return account;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Error performing JPA operation. Transaction is rolled back", e);
        } finally {
            entityManager.close();
        }
    }

    public List<Account> performWithListReturning(Function<EntityManager, List<Account>> operation) {
        EntityManager entityManager = emf.createEntityManager();
        entityManager.getTransaction().begin();
        try {
            List<Account> account = operation.apply(entityManager);
            entityManager.getTransaction().commit();
            return account;
        } catch (Exception e) {
            entityManager.getTransaction().rollback();
            throw new AccountDaoException("Error performing JPA operation. Transaction is rolled back", e);
        } finally {
            entityManager.close();
        }
    }

    public void save(Account account) {
        performWithoutReturning(em -> em.persist(account));
    }

    @Override
    public Account findById(Long id) {
        return performWithReturning(em -> em.find(Account.class, id));
    }

    @Override
    public Account findByEmail(String email) {
       return performWithReturning(em -> em.createQuery("select a from Account a where a.email = :email", Account.class)
                .setParameter("email", email)
                .getSingleResult());
    }

    @Override
    public List<Account> findAll() {
        return performWithListReturning(em ->
                em.createQuery("select a from Account a", Account.class).getResultList());
    }

    @Override
    public void update(Account account) {
        performWithoutReturning(em -> em.merge(account));
    }

    @Override
    public void remove(Account account) {
        performWithoutReturning(em -> {
            em.remove(em.merge(account));// put in pc with merge
        });
    }
}

