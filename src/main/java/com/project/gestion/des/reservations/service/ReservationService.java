package com.project.gestion.des.reservations.service;

import com.project.gestion.des.reservations.entities.Reservation;
import com.project.gestion.des.reservations.entities.Client;
import com.project.gestion.des.reservations.entities.Chambre;
import com.project.gestion.des.reservations.repositories.ReservationRepository;
import com.project.gestion.des.reservations.repositories.ClientRepository;
import com.project.gestion.des.reservations.repositories.ChambreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ChambreRepository chambreRepository;

    // Créer une réservation (nécessite le rôle USER)
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public Reservation creerReservation(
            // Informations du client
            String nomClient,
            String prenomClient,
            String emailClient,
            String telephoneClient,

            // Informations de la chambre
            String typeChambre,
            BigDecimal prixChambre,


            // Informations de réservation
            LocalDate dateDebut,
            LocalDate dateFin,
            String preferences) {

        // Créer un nouveau client
        Client client = new Client();
        client.setNom(nomClient);
        client.setPrenom(prenomClient);
        client.setEmail(emailClient);
        client.setTelephone(telephoneClient);
        client = clientRepository.save(client);

        // Créer une nouvelle chambre
        Chambre chambre = new Chambre();
        chambre.setType(typeChambre);
        chambre.setPrix(prixChambre);
        chambre.setDisponible(true);
        chambre = chambreRepository.save(chambre);

        // Vérifier la disponibilité de la chambre
        boolean isChambreDisponible = estChambreDisponible(chambre.getId(), dateDebut, dateFin);
        if (!isChambreDisponible) {
            throw new RuntimeException("La chambre n'est pas disponible pour ces dates");
        }

        // Créer la réservation
        Reservation reservation = new Reservation();
        reservation.setClient(client);
        reservation.setChambre(chambre);
        reservation.setDateDebut(dateDebut);
        reservation.setDateFin(dateFin);
        reservation.setPreferences(preferences);

        return reservationRepository.save(reservation);
    }

    // Méthode privée pour vérifier la disponibilité de la chambre
    private boolean estChambreDisponible(Long chambreId, LocalDate dateDebut, LocalDate dateFin) {
        return reservationRepository.findConflictingReservations(dateDebut, dateFin)
                .stream()
                .noneMatch(r -> r.getChambre().getId().equals(chambreId));
    }

    // Consulter une réservation (pas besoin de rôle)
    @Transactional(readOnly = true)
    public Optional<Reservation> consulterReservation(Long reservationId) {
        return reservationRepository.findById(reservationId);
    }

    @Transactional
    @PreAuthorize("hasRole('USER')")
    public Reservation modifierReservation(Long reservationId, Long nouvelleReservationId) {
        // Rechercher la réservation existante
        Reservation reservationExistante = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation originale non trouvée"));

        // Rechercher la nouvelle réservation
        Reservation nouvelleReservation = reservationRepository.findById(nouvelleReservationId)
                .orElseThrow(() -> new RuntimeException("Nouvelle réservation non trouvée"));

        // Vérifier la disponibilité de la chambre pour les nouvelles dates
        boolean isChambreDisponible = estChambreDisponible(
                nouvelleReservation.getChambre().getId(),
                nouvelleReservation.getDateDebut(),
                nouvelleReservation.getDateFin()
        );

        if (!isChambreDisponible) {
            throw new RuntimeException("La chambre n'est pas disponible pour ces dates");
        }

        // Mise à jour des informations de la réservation existante avec celles de la nouvelle réservation
        reservationExistante.setChambre(nouvelleReservation.getChambre());
        reservationExistante.setClient(nouvelleReservation.getClient());
        reservationExistante.setDateDebut(nouvelleReservation.getDateDebut());
        reservationExistante.setDateFin(nouvelleReservation.getDateFin());
        reservationExistante.setPreferences(nouvelleReservation.getPreferences());

        // Supprimer la nouvelle réservation, si elle est temporaire
        reservationRepository.deleteById(nouvelleReservationId);

        // Sauvegarder les modifications sur la réservation existante
        return reservationRepository.save(reservationExistante);
    }


    // Supprimer une réservation (nécessite le rôle ADMIN ou USER)
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public void supprimerReservation(Long reservationId) {
        // Vérifier si la réservation existe
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        reservationRepository.delete(reservation);
    }
}
