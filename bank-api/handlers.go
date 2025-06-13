package main

import (
	"encoding/json"
	"log"
	"math/rand"
	"net/http"
	"strings"
	"time"
)

type Account struct {
	ID       string  `json:"id"`
	Owner    string  `json:"owner"`
	Balance  float64 `json:"balance"`
	Currency string  `json:"currency"`
}

type TransactionRequest struct {
	FromAccount string  `json:"from_account"`
	ToAccount   string  `json:"to_account"`
	Amount      float64 `json:"amount"`
	Currency    string  `json:"currency"`
}

type TransactionResponse struct {
	TransactionID string `json:"transaction_id"`
	Status        string `json:"status"`
	Timestamp     string `json:"timestamp"`
}

var mockAccounts = []Account{
	{ID: "1001", Owner: "Alice", Balance: 5234.12, Currency: "AUD"},
	{ID: "1002", Owner: "Bob", Balance: 305.50, Currency: "AUD"},
	{ID: "1003", Owner: "Carol", Balance: 12990.00, Currency: "AUD"},
}

func healthHandler(w http.ResponseWriter, _ *http.Request) {
	log.Println("[DEBUG] /health endpoint hit")
	body := []byte("bank-api is up")
	w.WriteHeader(http.StatusOK)
	w.Write(body)
	if f, ok := w.(http.Flusher); ok {
		log.Println("[DEBUG] Flushing /health response to client")
		f.Flush()
	}
}

func accountsHandler(w http.ResponseWriter, r *http.Request) {
	log.Println("[DEBUG] /accounts endpoint hit")
	if r.Method != http.MethodGet {
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	data, err := json.Marshal(mockAccounts)
	if err != nil {
		http.Error(w, "failed to marshal response", http.StatusInternalServerError)
		return
	}
	log.Printf("[DEBUG] Marshaled accounts response: %s\n", string(data))
	w.WriteHeader(http.StatusOK)
	log.Printf("[DEBUG] Wrote OK Status")
	if _, err := w.Write(data); err != nil {
		http.Error(w, "failed to write response", http.StatusInternalServerError)
		return
	}

	log.Printf("[DEBUG] Wrote %d bytes with status 200 to client\n", len(data))
	if f, ok := w.(http.Flusher); ok {
		log.Println("[DEBUG] Flushing response to client")
		f.Flush()
	} else {
		log.Println("[DEBUG] Flusher interface not supported on ResponseWriter")
	}
}

func accountHandler(w http.ResponseWriter, r *http.Request) {
	log.Printf("[DEBUG] /accounts/{id} endpoint hit: path=%s\n", r.URL.Path)
	if r.Method != http.MethodGet {
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}
	// path: /accounts/{id}
	id := strings.TrimPrefix(r.URL.Path, "/accounts/")
	for _, acct := range mockAccounts {
		if acct.ID == id {
			log.Printf("[DEBUG] Found account: %+v\n", acct)
			w.Header().Set("Content-Type", "application/json")
			data, err := json.Marshal(acct)
			if err != nil {
				http.Error(w, "failed to marshal response", http.StatusInternalServerError)
				return
			}
			if _, err := w.Write(data); err != nil {
				http.Error(w, "failed to write response", http.StatusInternalServerError)
				return
			}
			log.Printf("[DEBUG] Wrote %d bytes with status 200 to client\n", len(data))
			// allow default chunked encoding, no manual Content-Length
			if f, ok := w.(http.Flusher); ok {
				log.Println("[DEBUG] Flushing response to client")
				f.Flush()
			} else {
				log.Println("[DEBUG] Flusher interface not supported on ResponseWriter")
			}
			return
		}
	}
	http.Error(w, "account not found", http.StatusNotFound)
}

func transactionHandler(w http.ResponseWriter, r *http.Request) {
	log.Println("[DEBUG] /transactions endpoint hit")
	if r.Method != http.MethodPost {
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req TransactionRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid JSON", http.StatusBadRequest)
		return
	}
	log.Printf("[DEBUG] Decoded transaction request: %+v\n", req)

	// Basic simulation: check accounts exist
	var fromExists, toExists bool
	for _, acct := range mockAccounts {
		if acct.ID == req.FromAccount {
			fromExists = true
		}
		if acct.ID == req.ToAccount {
			toExists = true
		}
	}
	if !fromExists || !toExists {
		http.Error(w, "one or both accounts not found", http.StatusBadRequest)
		return
	}

	// Simulate a transaction ID and timestamp
	txnID := randString(12)
	resp := TransactionResponse{
		TransactionID: txnID,
		Status:        "COMPLETED",
		Timestamp:     time.Now().Format(time.RFC3339),
	}
	log.Printf("[DEBUG] Responding with transaction: %+v\n", resp)

	w.Header().Set("Content-Type", "application/json")
	jsonBytes, err := json.Marshal(resp)
	if err != nil {
		http.Error(w, "failed to encode response", http.StatusInternalServerError)
		return
	}
	w.WriteHeader(http.StatusCreated)
	if _, err := w.Write(jsonBytes); err != nil {
		http.Error(w, "failed to write response", http.StatusInternalServerError)
		return
	}
	log.Printf("[DEBUG] Wrote %d bytes with status 201 to client\n", len(jsonBytes))
	if f, ok := w.(http.Flusher); ok {
		log.Println("[DEBUG] Flushing response to client")
		f.Flush()
	} else {
		log.Println("[DEBUG] Flusher interface not supported on ResponseWriter")
	}
}

// randString generates a random alphanumeric string of given length.
func randString(n int) string {
	const letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
	rand.Seed(time.Now().UnixNano())
	b := make([]byte, n)
	for i := range b {
		b[i] = letters[rand.Intn(len(letters))]
	}
	return string(b)
}
