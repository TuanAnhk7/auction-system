package auction.server.db.repository;

import auction.server.db.entity.ItemEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Optional;

public class ItemRepository {
    private final EntityManagerFactory emf;

    public ItemRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Optional<ItemEntity> findById(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            ItemEntity item = em.find(ItemEntity.class, id);
            return Optional.ofNullable(item);
        } finally {
            em.close();
        }
    }

    public void save(ItemEntity item) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (em.find(ItemEntity.class, item.getId()) != null) {
                em.merge(item);
            } else {
                em.persist(item);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            ItemEntity item = em.find(ItemEntity.class, id);
            if (item != null) {
                em.remove(item);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<ItemEntity> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createQuery("SELECT i FROM ItemEntity i");
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
