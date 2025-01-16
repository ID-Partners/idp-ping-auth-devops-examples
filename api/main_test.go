package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gorilla/mux"
)

// TestPaymentHandler
func TestPaymentHandler(t *testing.T) {
	// Define a sample request payload
	requestPayload := BIANPaymentRequest{
		DebtorAccount:   "12345678",
		CreditorAccount: "87654321",
		Amount:          150.75,
		Currency:        "USD",
		PaymentDetails:  "Invoice Payment",
	}
	payloadBytes, _ := json.Marshal(requestPayload)

	// Create a new HTTP request
	req, err := http.NewRequest("POST", "/payment", bytes.NewBuffer(payloadBytes))
	if err != nil {
		t.Fatalf("Could not create request: %v", err)
	}

	// Set the header to indicate JSON content
	req.Header.Set("Content-Type", "application/json")

	// Create a ResponseRecorder to capture the response
	rr := httptest.NewRecorder()

	// Create a router and register the handler
	router := mux.NewRouter()
	router.HandleFunc("/payment", PaymentHandler).Methods("POST")

	// Serve the HTTP request
	router.ServeHTTP(rr, req)

	// Assert the response code
	if status := rr.Code; status != http.StatusOK {
		t.Errorf("Expected status code %v, got %v", http.StatusOK, status)
	}

	// Assert the response body contains expected data
	var response map[string]interface{}
	json.Unmarshal(rr.Body.Bytes(), &response)

	if response["status"] != "Payment Processed" {
		t.Errorf("Expected status 'Payment Processed', got '%v'", response["status"])
	}
	if response["debtorAccount"] != requestPayload.DebtorAccount {
		t.Errorf("Expected debtor account '%v', got '%v'", requestPayload.DebtorAccount, response["debtorAccount"])
	}
}

// TestIntentsHandler
func TestIntentsHandler(t *testing.T) {
	// Define a sample request payload
	requestPayload := Intent{
		TransactionID: "txn123",
		Key:           "status",
		Value:         "pending",
	}
	payloadBytes, _ := json.Marshal(requestPayload)

	// Create a new HTTP request
	req, err := http.NewRequest("POST", "/intents", bytes.NewBuffer(payloadBytes))
	if err != nil {
		t.Fatalf("Could not create request: %v", err)
	}

	// Set the header to indicate JSON content
	req.Header.Set("Content-Type", "application/json")

	// Create a ResponseRecorder to capture the response
	rr := httptest.NewRecorder()

	// Create a router and register the handler
	router := mux.NewRouter()
	router.HandleFunc("/intents", IntentsHandler).Methods("POST")

	// Serve the HTTP request
	router.ServeHTTP(rr, req)

	// Assert the response code
	if status := rr.Code; status != http.StatusOK {
		t.Errorf("Expected status code %v, got %v", http.StatusOK, status)
	}

	// Assert the response body contains expected data
	var response map[string]interface{}
	json.Unmarshal(rr.Body.Bytes(), &response)

	if response["status"] != "Intent Processed" {
		t.Errorf("Expected status 'Intent Processed', got '%v'", response["status"])
	}
	if response["key"] != requestPayload.Key {
		t.Errorf("Expected key '%v', got '%v'", requestPayload.Key, response["key"])
	}
}
