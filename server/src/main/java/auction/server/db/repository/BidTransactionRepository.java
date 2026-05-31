package auction.server.db.repository;

import auction.server.db.entity.BidTransactionEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import java.util.List;

public class BidTransactionRepository {
    private final EntityManagerFactory emf;

    public BidTransactionRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void save(BidTransactionEntity bid) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (em.find(BidTransactionEntity.class, bid.getId()) != null) {
                em.merge(bid);
            } else {
                em.persist(bid);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<BidTransactionEntity> findByAuctionId(String auctionId) {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createQuery("SELECT b FROM BidTransactionEntity b WHERE b.auctionId = :auctionId");
            query.setParameter("auctionId", auctionId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void deleteByAuctionId(String auctionId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM BidTransactionEntity b WHERE b.auctionId = :auctionId")
                    .setParameter("auctionId", auctionId)
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<BidTransactionEntity> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            Query query = em.createQuery("SELECT b FROM BidTransactionEntity b");
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}
