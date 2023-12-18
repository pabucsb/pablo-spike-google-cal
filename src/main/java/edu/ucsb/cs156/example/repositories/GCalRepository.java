package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.GCal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
//import com.google.api.services.calendar.model.Event;

@Repository
public interface GCalRepository extends CrudRepository<GCal, Long> {
    //Iterable<GCal> findByEmail(String driverEmail);
}
