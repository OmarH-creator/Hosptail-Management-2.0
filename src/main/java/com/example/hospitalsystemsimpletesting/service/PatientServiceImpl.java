package com.example.hospitalsystemsimpletesting.service;

import com.example.hospitalsystemsimpletesting.model.Patient;

import java.time.LocalDate;
import java.util.*;

/**
 * Implementation of the PatientService interface
 */
public class PatientServiceImpl implements PatientService {
    
    private final Map<String, Patient> patientsById = new HashMap<>();
    private final DataPersistenceService persistenceService;
    
    /**
     * Constructor without persistence - for testing only
     */
    public PatientServiceImpl() {
        this.persistenceService = null; // No persistence
    }
    
    /**
     * Constructor with persistence
     * @param persistenceService The data persistence service to use
     */
    public PatientServiceImpl(DataPersistenceService persistenceService) {
        this.persistenceService = persistenceService;
        loadPatients();
    }
    
    /**
     * Load patients from persistence
     */
    private void loadPatients() {
        if (persistenceService != null) {
            List<Patient> patients = persistenceService.loadPatients();
            for (Patient patient : patients) {
                patientsById.put(patient.getId(), patient);
            }
        }
    }
    
    /**
     * Save patients to persistence
     */
    private void savePatients() {
        if (persistenceService != null) {
            persistenceService.savePatients(new ArrayList<>(patientsById.values()));
        }
    }
    
    @Override
    public Patient registerPatient(String name, LocalDate dateOfBirth, String gender, String contactNumber, String address) {
        String patientId = "P" + (100 + patientsById.size());
        Patient patient = new Patient(patientId, name, dateOfBirth, gender, contactNumber, address);
        patientsById.put(patientId, patient);
        savePatients();
        return patient;
    }
    
    @Override
    public Optional<Patient> findPatientById(String patientId) {
        return Optional.ofNullable(patientsById.get(patientId));
    }
    
    @Override
    public List<Patient> findPatientsByName(String namePart) {
        List<Patient> results = new ArrayList<>();
        String lowerNamePart = namePart.toLowerCase();
        
        for (Patient patient : patientsById.values()) {
            if (patient.getName().toLowerCase().contains(lowerNamePart)) {
                results.add(patient);
            }
        }
        
        return results;
    }
    
    @Override
    public List<Patient> getAllPatients() {
        return new ArrayList<>(patientsById.values());
    }
    
    @Override
    public void updatePatient(String patientId, String name, LocalDate dateOfBirth, String gender, String contactNumber, String address) {
        Patient patient = patientsById.get(patientId);
        if (patient != null) {
            patient.setName(name);
            patient.setDateOfBirth(dateOfBirth);
            patient.setGender(gender);
            patient.setContactNumber(contactNumber);
            patient.setAddress(address);
            savePatients();
        } else {
            throw new IllegalArgumentException("Patient not found: " + patientId);
        }
    }
    
    @Override
    public void admitPatient(String patientId) {
        Patient patient = patientsById.get(patientId);
        if (patient != null) {
            patient.admit();
            savePatients();
        } else {
            throw new IllegalArgumentException("Patient not found: " + patientId);
        }
    }
    
    @Override
    public void dischargePatient(String patientId) {
        Patient patient = patientsById.get(patientId);
        if (patient != null) {
            patient.discharge();
            savePatients();
        } else {
            throw new IllegalArgumentException("Patient not found: " + patientId);
        }
    }
    
    @Override
    public List<Patient> getAdmittedPatients() {
        List<Patient> admitted = new ArrayList<>();
        for (Patient patient : patientsById.values()) {
            if (patient.isAdmitted()) {
                admitted.add(patient);
            }
        }
        return admitted;
    }
    
    @Override
    public boolean deletePatient(String patientId) {
        if (patientId == null || patientId.trim().isEmpty()) {
            return false;
        }
        
        Patient removedPatient = patientsById.remove(patientId);
        if (removedPatient != null) {
            savePatients(); // Update persistence
            
            // Clean up references to this patient in other files would normally happen here
            // But we'll simplify for now to avoid dependencies
            System.out.println("Patient deleted: " + patientId);
            
            return true;
        }
        
        return false;
    }
} 