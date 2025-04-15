package com.example.hospitalsystemsimpletesting;

import com.example.hospitalsystemsimpletesting.service.*;
import com.example.hospitalsystemsimpletesting.service.impl.CSVDataPersistenceService;
import com.example.hospitalsystemsimpletesting.service.AppointmentServiceImpl;
import com.example.hospitalsystemsimpletesting.service.MedicalRecordServiceImpl;
import com.example.hospitalsystemsimpletesting.service.PatientServiceImpl;
import com.example.hospitalsystemsimpletesting.ui.LoginScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application class for the Hospital Management System.
 * Acts as the entry point for the JavaFX application.
 */
public class HospitalApplication extends Application {
    // Static role variable - visible to all classes
    public static String CURRENT_USER_ROLE = "NONE";
    
    // Shared services for data persistence
    private static PatientService patientService;
    private static DataPersistenceService dataPersistenceService;
    private static MedicalRecordService medicalRecordService;
    private static AppointmentService appointmentService;
    
    // Initialize services
    static {
        // Create services with circular dependency resolution
        PatientServiceImpl tempPatientService = new PatientServiceImpl();
        dataPersistenceService = new CSVDataPersistenceService(tempPatientService);
        patientService = new PatientServiceImpl(dataPersistenceService);
        
        // Update the reference in the persistence service
        ((CSVDataPersistenceService) dataPersistenceService).setPatientService(patientService);
        
        // Initialize appointment service
        appointmentService = new AppointmentServiceImpl(patientService, dataPersistenceService);
        
        // Initialize medical record service with proper persistence
        medicalRecordService = new MedicalRecordServiceImpl(patientService, appointmentService, dataPersistenceService);
    }

    @Override
    public void start(Stage stage) {
        try {
            // Start with login screen instead of main menu
            LoginScreen loginScreen = new LoginScreen();
            Scene scene = new Scene(loginScreen.getRoot(), 600, 450);
            scene.getStylesheets().add(getClass().getResource("/styles/application.css").toExternalForm());
            
            stage.setTitle("Hospital Management System - Login");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the shared patient service instance
     */
    public static PatientService getPatientService() {
        return patientService;
    }
    
    /**
     * Get the shared data persistence service instance
     */
    public static DataPersistenceService getDataPersistenceService() {
        return dataPersistenceService;
    }
    
    /**
     * Get the shared medical record service instance
     */
    public static MedicalRecordService getMedicalRecordService() {
        return medicalRecordService;
    }
    
    /**
     * Get the shared appointment service instance
     */
    public static AppointmentService getAppointmentService() {
        return appointmentService;
    }

    /**
     * Main method to launch the application.
     */
    public static void main(String[] args) {
        launch(args);
    }
} 