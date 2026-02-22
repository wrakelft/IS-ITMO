package ru.itmo.util;

import org.hibernate.JDBCException;
import org.hibernate.Session;
import java.sql.Connection;
import java.sql.SQLException;

public class TxIsolationUtil {
    private TxIsolationUtil() {}

    public static void setSerializable(Session session) {
        session.doWork(conn -> conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE));
    }

    public static boolean isSerializationFailure(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            if (cur instanceof JDBCException jdbcEx) {
                SQLException sql = jdbcEx.getSQLException();
                if (sql != null && "40001".equals(sql.getSQLState())) return true;
            }
            if (cur instanceof SQLException sql) {
                if ("40001".equals(sql.getSQLState())) return true;
            }
            cur = cur.getCause();
        }
        return false;
    }
}
