package com.bobocode;

import com.bobocode.exception.QueryHelperException;
import com.bobocode.model.Account;
import com.bobocode.model.Gender;
import com.bobocode.util.EntityManagerUtil;
import com.bobocode.util.TestDataGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;


public class QueryHelperTest {
    private static EntityManagerFactory entityManagerFactory;
    private static EntityManagerUtil emUtil;
    private static QueryHelper queryHelper;

    @BeforeAll
    public static void setup() {
        entityManagerFactory = Persistence.createEntityManagerFactory("Account");
        emUtil = new EntityManagerUtil(entityManagerFactory);
        queryHelper = new QueryHelper(entityManagerFactory);
    }

    @AfterAll
    static void destroy() {
        entityManagerFactory.close();
    }

    Account generateAccount(){
        Account account = new Account();
        account.setFirstName("person.getFirstName()");
        account.setLastName("person.getLastName()");
        account.setEmail("person.getEmail()");
        account.setBirthday(LocalDate.of(
                2002,
                3,
                5));
        account.setGender(Gender.FEMALE);
        BigDecimal balance = BigDecimal.valueOf(new Random().nextInt(200_000),2);
        account.setBalance(balance);
        account.setCreationTime(LocalDateTime.now());
        return account;
    }

    @Test
    public void testQueryHelperReturnsResult() {
        Account account = saveRandomAccount();
        Long accountId = account.getId();

        Account foundAccount = queryHelper.readWithinTx(entityManager -> entityManager.find(Account.class, accountId));

        assertThat(foundAccount, notNullValue());
    }

    @Test
    public void testQueryHelperUsesReadOnly() {
        Account account = saveRandomAccount();
        Long accountId = account.getId();

        tryToUpdateFirstName(accountId);
        Account foundAccount = queryHelper.readWithinTx(entityManager -> entityManager.find(Account.class, accountId));

        assertThat(foundAccount.getFirstName(), not(equalTo("XXX")));
        assertThat(foundAccount.getFirstName(), equalTo(account.getFirstName()));
    }

    private Account saveRandomAccount() {
        Account account = generateAccount();
        emUtil.performWithinTx(entityManager -> entityManager.persist(account));
        return account;
    }

    private void tryToUpdateFirstName(Long accountId) {
        queryHelper.readWithinTx(entityManager -> {
            Account managedAccount = entityManager.find(Account.class, accountId);
            managedAccount.setFirstName("XXX");
            return managedAccount;
        });
    }


    @Test
    public void testQueryHelperThrowsException() {
        Account account = generateAccount();
        emUtil.performWithinTx(entityManager -> entityManager.persist(account));
        try {
            queryHelper.readWithinTx(entityManager -> {
                Account managedAccount = entityManager.find(Account.class, account.getId());
                throwException();
                return managedAccount;
            });
            fail("Exception should be thrown");
        } catch (Exception e) {
            assertThat(e.getClass(), equalTo(QueryHelperException.class));
            assertThat(e.getMessage(), containsString("Transaction is rolled back"));
        }
    }

    private void throwException() {
        throw new RuntimeException("Runtime error");
    }
}
