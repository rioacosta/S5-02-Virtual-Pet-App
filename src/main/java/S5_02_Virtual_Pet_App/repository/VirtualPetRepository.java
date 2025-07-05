package S5_02_Virtual_Pet_App.repository;


import S5_02_Virtual_Pet_App.model.User;
import S5_02_Virtual_Pet_App.model.VirtualPet;
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
