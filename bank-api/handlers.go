package main

import (
	"encoding/json"
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
	w.WriteHeader(http.StatusOK)
	w.Write([]byte("bank-api is up"))
}

func accountsHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(mockAccounts)
}

func accountHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}
	// path: /accounts/{id}
	id := strings.TrimPrefix(r.URL.Path, "/accounts/")
	for _, acct := range mockAccounts {
		if acct.ID == id {
			w.Header().Set("Content-Type", "application/json")
			json.NewEncoder(w).Encode(acct)
			return
		}
	}
	http.Error(w, "account not found", http.StatusNotFound)
}

func transactionHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "method not allowed", http.StatusMethodNotAllowed)
		return
	}

	var req TransactionRequest
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		http.Error(w, "invalid JSON", http.StatusBadRequest)
		return
	}

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

	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(resp)
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
