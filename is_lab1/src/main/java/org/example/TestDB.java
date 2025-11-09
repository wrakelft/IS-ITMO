package org.example;

import org.example.model.Organization;
import org.example.model.Coordinates;
import org.example.model.Address;
import org.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class TestDB {
    public static void main(String[] args) {
        try {
            HibernateUtil hibernateUtil = new HibernateUtil();
            Session session = hibernateUtil.getSessionFactory().openSession();

            System.out.println("✅ База данных успешно инициализирована!");
            System.out.println("✅ Таблицы должны быть созданы автоматически");

            session.close();
            hibernateUtil.close();

        } catch (Exception e) {
            System.err.println("❌ Ошибка инициализации БД: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
