package main

import (
	"encoding/json"
	"fmt"
	"log"
	"math/rand"
	"net/http"

	"github.com/gorilla/mux"
)

// BIAN Payment API Request
type BIANPaymentRequest struct {
	DebtorAccount   string  `json:"debtorAccount"`
	CreditorAccount string  `json:"creditorAccount"`
	Amount          float64 `json:"amount"`
	Currency        string  `json:"currency"`
	PaymentDetails  string  `json:"paymentDetails"`
}

type Intent struct {
	TransactionID string `json:"transactionId"`
	Key           string `json:"key"`
	Value         string `json:"value"`
}

func IntentsHandler(w http.ResponseWriter, r *http.Request) {
	// Parse the BIAN payment request payload
	var intent Intent
	err := json.NewDecoder(r.Body).Decode(&intent)
	if err != nil {
		http.Error(w, "Invalid Intent Request", http.StatusBadRequest)
		return
	}

	// Here, you can assume the RAR validation and authorization were already handled by Ping Authorize.
	// Process payment (mock payment processing)
	response := map[string]interface{}{
		"status":        "Intent Processed",
		"transactionId": intent.TransactionID,
		"key":           intent.Key,
		"value":         intent.Value,
	}

	// Return a success response
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(response)
}

// Payment Handler
func PaymentHandler(w http.ResponseWriter, r *http.Request) {
	// Parse the BIAN payment request payload
	var paymentReq BIANPaymentRequest
	err := json.NewDecoder(r.Body).Decode(&paymentReq)
	if err != nil {
		http.Error(w, "Invalid Payment Request", http.StatusBadRequest)
		return
	}

	fmt.Println("Payment Request Received: ", paymentReq)

	// Here, you can assume the RAR validation and authorization were already handled by Ping Authorize.
	// Process payment (mock payment processing)
	response := map[string]interface{}{
		"status":          "Payment Processed",
		"transactionId":   rand.Intn(1000000),
		"debtorAccount":   paymentReq.DebtorAccount,
		"creditorAccount": paymentReq.CreditorAccount,
		"amount":          paymentReq.Amount,
		"currency":        paymentReq.Currency,
	}

	// Return a success response
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(response)
}

func CDRHandler(w http.ResponseWriter, r *http.Request) {
	// Pull out the path parameters using mux.Vars
	vars := mux.Vars(r)
	brand := vars["brand"]
	resource := vars["resource"]

	// Here you can do any logic you need based on the brand and resource,
	// such as querying a database or another API.
	// For this example, we'll just return some mock data.
	response := map[string]interface{}{
		"status":   "CDR Data Retrieved",
		"brand":    brand,
		"resource": resource,
		"data": map[string]interface{}{
			// Sample data structure, adapt as needed
			"accountNumber": "12345678",
			"balance":       1000.00,
			"currency":      "AUD",
			"description":   "Mock CDR data for demonstration",
		},
	}

	// Set JSON header and status, then encode response
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	json.NewEncoder(w).Encode(response)
}

func main() {
	r := mux.NewRouter()

	// Define the Payment route
	r.HandleFunc("/payment", PaymentHandler).Methods("POST")
	r.HandleFunc("/intents", IntentsHandler).Methods("POST")
	r.HandleFunc("/cdr/{brand}/{resource}", CDRHandler).Methods("POST")

	fmt.Println("Server running on port 8081")
	log.Fatal(http.ListenAndServe(":8081", r))
}
