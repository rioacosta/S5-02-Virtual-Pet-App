package S502VirtualPetApp.repository;


import S502VirtualPetApp.model.User;
import S502VirtualPetApp.model.VirtualBuddy;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VirtualBuddyRepository extends MongoRepository<VirtualBuddy, String> {
    List<VirtualBuddy> findByOwner(User owner);
    Optional<VirtualBuddy> findByIdAndOwner(String id, User owner);
    long countByOwner(User owner);
}
