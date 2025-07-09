package S502VirtualPetApp.repository;


import S502VirtualPetApp.model.User;
import S502VirtualPetApp.model.VirtualPet;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VirtualPetRepository extends MongoRepository<VirtualPet, String> {
    List<VirtualPet> findByOwner(User owner);
    Optional<VirtualPet> findByIdAndOwner(String id, User owner);
    long countByOwner(User owner);
}
