package auction.server.db.repository;

import auction.server.db.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import java.util.List;

public class UserRepository {
    private final EntityManagerFactory emf;

    public UserRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public UserEntity findByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(UserEntity.class, username);
        } finally {
            em.close();
        }
    }

    public void save(UserEntity user) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (em.find(UserEntity.class, user.getUsername()) != null) {
                em.merge(user);
            } else {
                em.persist(user);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void delete(String username) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            UserEntity user = em.find(UserEntity.class, username);
            if (user != null) {
                em.remove(user);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<UserEntity> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createQuery("SELECT u FROM UserEntity u");
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
