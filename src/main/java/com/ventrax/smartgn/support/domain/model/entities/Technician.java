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

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 30)
    private String phone;

    @Column(nullable = false)
    private Boolean available = true;

    @Column(nullable = false)
    private Boolean active = true;

    public Technician() {}

    public Technician(String fullName, String accreditationCode, String email, String phone) {
        this.fullName = fullName;
        this.accreditationCode = accreditationCode;
        this.email = email;
        this.phone = phone;
        this.available = true;
        this.active = true;
    }

    public UUID getTechnicianId() { return technicianId; }
    public String getFullName() { return fullName; }
    public String getAccreditationCode() { return accreditationCode; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public Boolean getActive() { return active; }
}