package com.ventrax.smartgn.support.domain.model.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "technicians")
public class Technician {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "technician_id")
    private UUID technicianId;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "accreditation_code", nullable = false, unique = true, length = 60)
    private String accreditationCode;

    @Column(nullable = false, length = 80)
    private String specialty = "Instalaciones Internas";

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false)
    private Boolean available = true;

    @Column(nullable = false)
    private Boolean active = true;

    public Technician() {}

    public Technician(String fullName, String accreditationCode, String specialty, String email, String phone) {
        this.fullName = fullName;
        this.accreditationCode = accreditationCode;
        this.specialty = (specialty != null && !specialty.isBlank()) ? specialty : "Instalaciones Internas";
        this.email = email;
        this.phone = phone;
        this.available = true;
        this.active = true;
    }

    public void updateDetails(String accreditationCode, String specialty, String email, String phone, Boolean available, Boolean active) {
        if (accreditationCode != null) this.accreditationCode = accreditationCode;
        if (specialty != null) this.specialty = specialty;
        if (email != null) this.email = email;
        if (phone != null) this.phone = phone;
        if (available != null) this.available = available;
        if (active != null) this.active = active;
    }

    public UUID getTechnicianId() { return technicianId; }
    public String getFullName() { return fullName; }
    public String getAccreditationCode() { return accreditationCode; }
    public String getSpecialty() { return specialty; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public Boolean getActive() { return active; }
}