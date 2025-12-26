package ma.hibernate.dao;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        Transaction transaction = null;
        try (Session session = factory.openSession()) {
            transaction = session.beginTransaction();
            session.persist(phone);
            transaction.commit();
            return phone;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Can't create phone: " + phone.toString(), e);
        }
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        try (Session session = factory.openSession()) {
            HibernateCriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Phone> query = cb.createQuery(Phone.class);
            Root<Phone> root = query.from(Phone.class);
            List<Predicate> predicates = new ArrayList<>();
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                List<Predicate> separateFieldPredicates = new ArrayList<>();
                for (String valueElem : entry.getValue()) {
                    separateFieldPredicates.add(cb.equal(root.get(entry.getKey()), valueElem));
                }
                Predicate fieldPredicates =
                        cb.or(separateFieldPredicates.toArray(Predicate[]::new));
                predicates.add(fieldPredicates);
            }
            Predicate resultPredicate = cb.and(predicates.toArray(Predicate[]::new));
            query.where(resultPredicate);
            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Can't find all phones with params: "
                    + params.entrySet().stream()
                    .map(entry ->
                        entry.getKey() + " = { " + Arrays.toString(entry.getValue()) + " }")
                    .collect(Collectors.joining("; ")), e);
        }
    }
}
