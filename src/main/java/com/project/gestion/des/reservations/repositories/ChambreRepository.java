package com.project.gestion.des.reservations.repositories;

import com.project.gestion.des.reservations.entities.Chambre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChambreRepository extends JpaRepository<Chambre, Long> {
    // Méthode héritée de JpaRepository suffisante pour findById
    // Pas besoin de méthodes supplémentaires pour le service actuel
}