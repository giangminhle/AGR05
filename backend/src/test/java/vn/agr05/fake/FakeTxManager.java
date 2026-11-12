package vn.agr05.fake;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

/** Bộ quản lý transaction giả: chỉ đếm số lần Commit, Rollback. */
public class FakeTxManager extends AbstractPlatformTransactionManager {

    public int soCommit;
    public int soRollback;

    public TransactionTemplate template() {
        return new TransactionTemplate(this);
    }

    @Override
    protected Object doGetTransaction() {
        return new Object();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        // không làm gì
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        soCommit++;
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        soRollback++;
    }
}
