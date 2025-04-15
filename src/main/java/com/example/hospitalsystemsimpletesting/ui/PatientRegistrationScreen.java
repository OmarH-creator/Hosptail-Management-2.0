package com.example.hospitalsystemsimpletesting.ui;

import com.example.hospitalsystemsimpletesting.HospitalApplication;
import com.example.hospitalsystemsimpletesting.model.Patient;
import com.example.hospitalsystemsimpletesting.service.PatientService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Screen for patient registration and management functionality.
 * Allows registering new patients, editing patient information, and managing admissions.
 */
public class PatientRegistrationScreen {
    
    // UI components
    private final BorderPane rootContainer;
    private final TableView<Patient> patientsTable;
    private final TextField searchField;
    private final Button searchButton;
    private final Button registerButton;
    private final Button editButton;
    private final Button admitDischargeButton;
    private final Button deleteButton;
    private final Button backButton;
    private final Label titleLabel;
    private final Label statusLabel;
    private final ComboBox<String> searchTypeComboBox;
    
    // Service for patient operations
    private final PatientService patientService;
    
    // Observable list to display in the table
    private final ObservableList<Patient> patientsData = FXCollections.observableArrayList();
    
    // Currently selected patient
    private Patient selectedPatient;
    
    // Date formatter for display
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    // Blood type options
    private final String[] BLOOD_TYPES = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"};
    
    public PatientRegistrationScreen() {
        // Initialize the patient service
        this.patientService = HospitalApplication.getPatientService();
        
        // Create title
        titleLabel = new Label("Patient Registration");
        titleLabel.setFont(new Font("Arial", 24));
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setId("titleLabel");
        
        // Create the table for patients
        patientsTable = new TableView<>();
        patientsTable.setId("patientsTable");
        patientsTable.setPlaceholder(new Label("No patients registered"));
        
        // Define table columns
        TableColumn<Patient, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(80);
        idColumn.setCellFactory(column -> new TableCell<Patient, String>() {
            @Override
            protected void updateItem(String id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(id);
                    setStyle("-fx-font-weight: bold;"); // Make ID stand out visually
                }
            }
        });
        
        TableColumn<Patient, String> firstNameColumn = new TableColumn<>("First Name");
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameColumn.setPrefWidth(150);
        
        TableColumn<Patient, String> lastNameColumn = new TableColumn<>("Last Name");
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameColumn.setPrefWidth(150);
        
        // Create a column for date of birth that displays it as a formatted string
        TableColumn<Patient, LocalDate> dobColumn = new TableColumn<>("Date of Birth");
        dobColumn.setCellValueFactory(new PropertyValueFactory<>("dateOfBirth"));
        dobColumn.setPrefWidth(120);
        dobColumn.setCellFactory(column -> new TableCell<Patient, LocalDate>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(dateFormatter.format(date));
                }
            }
        });
        
        // Create a column for age (calculated property)
        TableColumn<Patient, Integer> ageColumn = new TableColumn<>("Age");
        ageColumn.setCellValueFactory(cellData -> {
            Patient patient = cellData.getValue();
            // Use a wrapper to convert int to IntegerProperty
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(patient.getAge());
        });
        ageColumn.setPrefWidth(60);
        
        // Create a column for blood type
        TableColumn<Patient, String> bloodTypeColumn = new TableColumn<>("Blood Type");
        bloodTypeColumn.setCellValueFactory(new PropertyValueFactory<>("bloodType"));
        bloodTypeColumn.setPrefWidth(100);
        
        // Create a column for admission status
        TableColumn<Patient, Boolean> admissionColumn = new TableColumn<>("Status");
        admissionColumn.setCellValueFactory(new PropertyValueFactory<>("admitted"));
        admissionColumn.setPrefWidth(100);
        admissionColumn.setCellFactory(column -> new TableCell<Patient, Boolean>() {
            @Override
            protected void updateItem(Boolean admitted, boolean empty) {
                super.updateItem(admitted, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else if (admitted != null && admitted) {
                    setText("ADMITTED");
                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                } else {
                    setText("OUTPATIENT");
                    setStyle("-fx-text-fill: #2ecc71;");
                }
            }
        });
        
        // Add columns to the table
        patientsTable.getColumns().addAll(
                idColumn, 
                firstNameColumn, 
                lastNameColumn, 
                dobColumn,
                ageColumn,
                bloodTypeColumn,
                admissionColumn
        );
        
        // Set up table selection listener
        patientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedPatient = newSelection;
            updateButtonStates();
        });
        
        // Bind the table to the observable list
        patientsTable.setItems(patientsData);
        
        // Create search controls
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        
        searchTypeComboBox = new ComboBox<>();
        searchTypeComboBox.setItems(FXCollections.observableArrayList("ID", "Name"));
        searchTypeComboBox.setValue("Name");
        searchTypeComboBox.setId("searchTypeComboBox");
        
        searchField = new TextField();
        searchField.setId("searchField");
        searchField.setPromptText("Search patients");
        searchField.setPrefWidth(250);
        
        searchButton = new Button("Search");
        searchButton.setId("searchButton");
        searchButton.setOnAction(e -> searchPatients());
        
        // Reset search when text field is cleared
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty()) {
                refreshPatientsList();
            }
        });
        
        searchBox.getChildren().addAll(searchTypeComboBox, searchField, searchButton);
        
        // Create action buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        registerButton = new Button("Register Patient");
        registerButton.setId("registerButton");
        registerButton.setOnAction(e -> showRegisterPatientDialog());
        
        editButton = new Button("Edit Patient");
        editButton.setId("editButton");
        editButton.setOnAction(e -> showEditPatientDialog());
        editButton.setDisable(true);
        
        admitDischargeButton = new Button("Admit/Discharge");
        admitDischargeButton.setId("admitDischargeButton");
        admitDischargeButton.setOnAction(e -> togglePatientAdmission());
        admitDischargeButton.setDisable(true);
        
        deleteButton = new Button("Delete Patient");
        deleteButton.setId("deleteButton");
        deleteButton.setOnAction(e -> deleteSelectedPatient());
        deleteButton.setDisable(true);
        
        backButton = new Button("Back to Main Menu");
        backButton.setId("backButton");
        
        buttonBox.getChildren().addAll(registerButton, editButton, admitDischargeButton, deleteButton, backButton);
        
        // Status label for feedback
        statusLabel = new Label("");
        statusLabel.setId("statusLabel");
        
        // Arrange components in containers
        VBox headerBox = new VBox(15);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.getChildren().addAll(titleLabel, searchBox);
        
        VBox tableContainer = new VBox(15);
        tableContainer.getChildren().addAll(patientsTable);
        tableContainer.setVgrow(patientsTable, Priority.ALWAYS);
        
        VBox bottomBox = new VBox(15);
        bottomBox.setAlignment(Pos.CENTER);
        bottomBox.getChildren().addAll(buttonBox, statusLabel);
        
        // Main container
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20));
        mainContent.getChildren().addAll(headerBox, tableContainer, bottomBox);
        mainContent.setVgrow(tableContainer, Priority.ALWAYS);
        
        // Root container
        rootContainer = new BorderPane();
        rootContainer.setCenter(mainContent);
        
        // Load initial data
        refreshPatientsList();
        
        // Add some sample data for demonstration
        if (patientsData.isEmpty()) {
            addSampleData();
        }
    }
    
    /**
     * Add sample patients for demonstration
     */
    private void addSampleData() {
        // Create and register sample patients with different properties
        
        // Register John Doe
        Patient p1 = patientService.registerPatient(
            "John Doe", 
            LocalDate.of(1980, 5, 15),
            "Male",
            "555-123-4567",
            "123 Main St, Springfield"
        );
        p1.setBloodType("A+");
        
        // Register Jane Smith
        Patient p2 = patientService.registerPatient(
            "Jane Smith", 
            LocalDate.of(1992, 8, 22),
            "Female",
            "555-987-6543",
            "456 Oak Ave, Springfield"
        );
        p2.setBloodType("O-");
        p2.admit(); // This patient is admitted
        
        // Register Robert Johnson
        Patient p3 = patientService.registerPatient(
            "Robert Johnson", 
            LocalDate.of(1975, 12, 3),
            "Male",
            "555-456-7890",
            "789 Pine St, Springfield"
        );
        p3.setBloodType("B+");
        
        refreshPatientsList();
    }
    
    /**
     * Search for patients by ID or name
     */
    private void searchPatients() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            refreshPatientsList();
            return;
        }
        
        List<Patient> allPatients = patientService.getAllPatients();
        List<Patient> filteredPatients;
        
        // Filter based on search type
        if (searchTypeComboBox.getValue().equals("ID")) {
            // Search by ID
            filteredPatients = allPatients.stream()
                    .filter(p -> p.getId().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            // Search by name (first or last)
            filteredPatients = allPatients.stream()
                    .filter(p -> 
                            p.getFirstName().toLowerCase().contains(query.toLowerCase()) || 
                            p.getLastName().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        patientsData.clear();
        patientsData.addAll(filteredPatients);
        updateStatusLabel("Found " + patientsData.size() + " patient(s) matching '" + query + "'");
    }
    
    /**
     * Refresh the patients list from the service
     */
    private void refreshPatientsList() {
        patientsData.clear();
        patientsData.addAll(patientService.getAllPatients());
        updateStatusLabel("Showing all " + patientsData.size() + " patient(s)");
    }
    
    /**
     * Update status label with a message
     */
    private void updateStatusLabel(String message) {
        statusLabel.setText(message);
    }
    
    /**
     * Update the button states based on selection
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedPatient != null;
        editButton.setDisable(!hasSelection);
        admitDischargeButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        
        if (hasSelection) {
            admitDischargeButton.setText(selectedPatient.isAdmitted() ? "Discharge Patient" : "Admit Patient");
        }
    }
    
    /**
     * Toggle a patient's admission status
     */
    private void togglePatientAdmission() {
        if (selectedPatient == null) {
            return;
        }
        
        String message;
        
        try {
            if (selectedPatient.isAdmitted()) {
                patientService.dischargePatient(selectedPatient.getId());
                message = "Discharged patient: " + selectedPatient.getFullName();
            } else {
                patientService.admitPatient(selectedPatient.getId());
                message = "Admitted patient: " + selectedPatient.getFullName();
            }
            
            refreshPatientsList();
            updateStatusLabel(message);
            
            // Keep the same patient selected after refresh
            for (Patient p : patientsData) {
                if (p.getId().equals(selectedPatient.getId())) {
                    patientsTable.getSelectionModel().select(p);
                    break;
                }
            }
        } catch (Exception e) {
            showErrorAlert("Operation Failed", "Unable to update patient admission status: " + e.getMessage());
        }
    }
    
    /**
     * Delete the selected patient after confirmation
     */
    private void deleteSelectedPatient() {
        if (selectedPatient == null) {
            return;
        }
        
        // Create confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Patient");
        confirmDialog.setHeaderText("Delete Patient: " + selectedPatient.getFullName());
        confirmDialog.setContentText("Are you sure you want to delete this patient? This action cannot be undone.");
        
        // Show dialog and wait for response
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        // If confirmed, delete the patient
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Call delete via reflection to work around compilation issues
                java.lang.reflect.Method deleteMethod = patientService.getClass().getMethod("deletePatient", String.class);
                boolean deleted = (boolean) deleteMethod.invoke(patientService, selectedPatient.getId());
                
                if (deleted) {
                    // Update UI - refresh list and show status
                    refreshPatientsList();
                    updateStatusLabel("Patient deleted successfully: " + selectedPatient.getFullName());
                    selectedPatient = null;
                    updateButtonStates();
                } else {
                    showErrorAlert("Delete Failed", "Could not delete the selected patient.");
                }
            } catch (Exception e) {
                showErrorAlert("Delete Failed", "Error deleting patient: " + e.getMessage());
            }
        }
    }
    
    /**
     * Display dialog to register a new patient
     */
    private void showRegisterPatientDialog() {
        // Create dialog
        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle("Register New Patient");
        dialog.setHeaderText("Enter patient information");
        
        // Set button types
        ButtonType saveButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField idField = new TextField();
        idField.setPromptText("Patient ID");
        
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        
        // Date picker for birth date
        DatePicker dateOfBirthPicker = new DatePicker();
        dateOfBirthPicker.setPromptText("Date of Birth");
        dateOfBirthPicker.setValue(LocalDate.now().minusYears(30)); // Default to 30 years ago
        
        // Blood type dropdown
        ComboBox<String> bloodTypeComboBox = new ComboBox<>();
        bloodTypeComboBox.setItems(FXCollections.observableArrayList(BLOOD_TYPES));
        bloodTypeComboBox.setValue("Unknown");
        
        // Add fields to grid
        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Last Name:"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Date of Birth:"), 0, 3);
        grid.add(dateOfBirthPicker, 1, 3);
        grid.add(new Label("Blood Type:"), 0, 4);
        grid.add(bloodTypeComboBox, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on ID field
        idField.requestFocus();
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String id = idField.getText().trim();
                    String firstName = firstNameField.getText().trim();
                    String lastName = lastNameField.getText().trim();
                    LocalDate dob = dateOfBirthPicker.getValue();
                    String bloodType = bloodTypeComboBox.getValue();
                    
                    // Validate inputs
                    if (id.isEmpty()) {
                        showErrorAlert("Invalid Input", "Patient ID cannot be empty.");
                        return null;
                    }
                    
                    if (firstName.isEmpty()) {
                        showErrorAlert("Invalid Input", "First name cannot be empty.");
                        return null;
                    }
                    
                    if (lastName.isEmpty()) {
                        showErrorAlert("Invalid Input", "Last name cannot be empty.");
                        return null;
                    }
                    
                    if (dob == null) {
                        showErrorAlert("Invalid Input", "Date of birth is required.");
                        return null;
                    }
                    
                    // Check if ID already exists
                    Optional<Patient> existingPatient = patientService.findPatientById(id);
                    if (existingPatient.isPresent()) {
                        showErrorAlert("Duplicate ID", "A patient with ID " + id + " already exists.");
                        return null;
                    }
                    
                    // Default values for required fields that aren't collected in the dialog
                    String fullName = firstName + " " + lastName;
                    String gender = "Unknown"; // Default value
                    String contactNumber = ""; // Default value
                    String address = ""; // Default value
                    
                    // Register patient using the new method
                    Patient patient = patientService.registerPatient(fullName, dob, gender, contactNumber, address);
                    
                    // Set additional properties not covered by register
                    patient.setId(id); // Set the custom ID
                    patient.setBloodType(bloodType);
                    
                    return patient;
                } catch (IllegalArgumentException e) {
                    showErrorAlert("Invalid Input", e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<Patient> result = dialog.showAndWait();
        result.ifPresent(patient -> {
            refreshPatientsList();
            updateStatusLabel("Registered new patient: " + patient.getFullName());
            
            // Select the newly added patient
            patientsTable.getSelectionModel().select(patient);
        });
    }
    
    /**
     * Display dialog to edit an existing patient
     */
    private void showEditPatientDialog() {
        if (selectedPatient == null) {
            return;
        }
        
        // Create dialog
        Dialog<Patient> dialog = new Dialog<>();
        dialog.setTitle("Edit Patient");
        dialog.setHeaderText("Edit patient information");
        
        // Set button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Make the ID prominent
        Label idValueLabel = new Label(selectedPatient.getId());
        idValueLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #0056b3;");
        
        TextField firstNameField = new TextField(selectedPatient.getFirstName());
        
        TextField lastNameField = new TextField(selectedPatient.getLastName());
        
        // Date picker for birth date
        DatePicker dateOfBirthPicker = new DatePicker();
        dateOfBirthPicker.setValue(selectedPatient.getDateOfBirth());
        
        // Blood type dropdown
        ComboBox<String> bloodTypeComboBox = new ComboBox<>();
        bloodTypeComboBox.setItems(FXCollections.observableArrayList(BLOOD_TYPES));
        bloodTypeComboBox.setValue(selectedPatient.getBloodType() != null ? 
                selectedPatient.getBloodType() : "Unknown");
        
        // Add fields to grid
        grid.add(new Label("ID:"), 0, 0);
        grid.add(idValueLabel, 1, 0);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Last Name:"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Date of Birth:"), 0, 3);
        grid.add(dateOfBirthPicker, 1, 3);
        grid.add(new Label("Blood Type:"), 0, 4);
        grid.add(bloodTypeComboBox, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on first name field
        firstNameField.requestFocus();
        
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String firstName = firstNameField.getText().trim();
                    String lastName = lastNameField.getText().trim();
                    LocalDate dob = dateOfBirthPicker.getValue();
                    String bloodType = bloodTypeComboBox.getValue();
                    
                    // Validate inputs
                    if (firstName.isEmpty()) {
                        showErrorAlert("Invalid Input", "First name cannot be empty.");
                        return null;
                    }
                    
                    if (lastName.isEmpty()) {
                        showErrorAlert("Invalid Input", "Last name cannot be empty.");
                        return null;
                    }
                    
                    if (dob == null) {
                        showErrorAlert("Invalid Input", "Date of birth is required.");
                        return null;
                    }
                    
                    // Default values for required fields that aren't collected in the dialog
                    String fullName = firstName + " " + lastName;
                    String gender = "Unknown"; // Default value
                    String contactNumber = ""; // Default value
                    String address = ""; // Default value
                    
                    // Update the patient using the proper method
                    patientService.updatePatient(selectedPatient.getId(), fullName, dob, gender, contactNumber, address);
                    
                    // Fetch the updated patient
                    Optional<Patient> updatedPatientOpt = patientService.findPatientById(selectedPatient.getId());
                    if (!updatedPatientOpt.isPresent()) {
                        showErrorAlert("Update Failed", "Failed to retrieve updated patient data.");
                        return null;
                    }
                    
                    Patient updatedPatient = updatedPatientOpt.get();
                    
                    // Set additional properties
                    updatedPatient.setBloodType(bloodType);
                    updatedPatient.setAdmitted(selectedPatient.isAdmitted());
                    
                    return updatedPatient;
                } catch (IllegalArgumentException e) {
                    showErrorAlert("Invalid Input", e.getMessage());
                    return null;
                }
            }
            return null;
        });
        
        // Show dialog and process result
        Optional<Patient> result = dialog.showAndWait();
        result.ifPresent(patient -> {
            refreshPatientsList();
            updateStatusLabel("Updated patient: " + patient.getFullName());
            
            // Keep the same patient selected after refresh
            for (Patient p : patientsData) {
                if (p.getId().equals(patient.getId())) {
                    patientsTable.getSelectionModel().select(p);
                    break;
                }
            }
        });
    }
    
    /**
     * Display an error alert
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Returns the root container for the patient registration screen
     */
    public Parent getRoot() {
        return rootContainer;
    }
    
    /**
     * Set the action for the back button
     */
    public void setBackButtonAction(Runnable action) {
        backButton.setOnAction(e -> action.run());
    }
    
    // Getters for testing
    public TableView<Patient> getPatientsTable() {
        return patientsTable;
    }
    
    public TextField getSearchField() {
        return searchField;
    }
    
    public Button getSearchButton() {
        return searchButton;
    }
    
    public Button getRegisterButton() {
        return registerButton;
    }
    
    public Button getEditButton() {
        return editButton;
    }
    
    public Button getAdmitDischargeButton() {
        return admitDischargeButton;
    }
    
    public Button getDeleteButton() {
        return deleteButton;
    }
    
    public Button getBackButton() {
        return backButton;
    }
    
    public Label getStatusLabel() {
        return statusLabel;
    }
    
    public ComboBox<String> getSearchTypeComboBox() {
        return searchTypeComboBox;
    }
} 