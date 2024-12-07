package com.project.gestion.des.reservations.controller;

import com.project.gestion.des.reservations.entities.Reservation;
import com.project.gestion.des.reservations.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    // Endpoint pour créer une réservation
    @PostMapping("/creer")
    public ResponseEntity<?> creerReservation(
            @RequestParam String nomClient,
            @RequestParam String prenomClient,
            @RequestParam String emailClient,
            @RequestParam String telephoneClient,
            @RequestParam String typeChambre,
            @RequestParam BigDecimal prixChambre,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) String preferences) {
        try {
            Reservation reservation = reservationService.creerReservation(
                    nomClient, prenomClient, emailClient, telephoneClient,
                    typeChambre, prixChambre, dateDebut, dateFin, preferences
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Endpoint pour consulter une réservation par ID
    @GetMapping("/{reservationId}")
    public ResponseEntity<?> consulterReservation(@PathVariable Long reservationId) {
        Optional<Reservation> reservation = reservationService.consulterReservation(reservationId);
        if (reservation.isPresent()) {
            return ResponseEntity.ok(reservation.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Réservation avec ID " + reservationId + " non trouvée");
        }
    }

    // Endpoint pour modifier une réservation
    @PutMapping("/modifier/{reservationId}")
    public ResponseEntity<?> modifierReservation(
            @PathVariable Long reservationId,
            @RequestParam Long nouvelleReservationId) {
        try {
            Reservation reservationModifiee = reservationService.modifierReservation(
                    reservationId, nouvelleReservationId
            );
            return ResponseEntity.ok(reservationModifiee);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Endpoint pour supprimer une réservation
    @DeleteMapping("/supprimer/{reservationId}")
    public ResponseEntity<?> supprimerReservation(@PathVariable Long reservationId) {
        try {
            reservationService.supprimerReservation(reservationId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
