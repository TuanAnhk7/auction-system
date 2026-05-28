package auction.server.db.repository;

import auction.server.db.entity.AuctionEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Optional;

public class AuctionRepository {
    private final EntityManagerFactory emf;

    public AuctionRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public Optional<AuctionEntity> findById(String id) {
        EntityManager em = emf.createEntityManager();
        try {
            AuctionEntity auction = em.find(AuctionEntity.class, id);
            return Optional.ofNullable(auction);
        } finally {
            em.close();
        }
    }

    public void save(AuctionEntity auction) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (em.find(AuctionEntity.class, auction.getId()) != null) {
                em.merge(auction);
            } else {
                em.persist(auction);
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
            AuctionEntity auction = em.find(AuctionEntity.class, id);
            if (auction != null) {
                em.remove(auction);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<AuctionEntity> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createQuery("SELECT a FROM AuctionEntity a");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<AuctionEntity> findBySellerUsername(String sellerUsername) {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createQuery("SELECT a FROM AuctionEntity a WHERE a.sellerUsername = :seller");
            query.setParameter("seller", sellerUsername);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
