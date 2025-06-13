package main

import (
	"log"
	"net/http"
)

func main() {
	mux := http.NewServeMux()
	mux.HandleFunc("/", healthHandler)
	mux.HandleFunc("/accounts", accountsHandler)        // GET list or POST transactions is separate
	mux.HandleFunc("/accounts/", accountHandler)        // GET single account
	mux.HandleFunc("/transactions", transactionHandler) // POST new transaction

	addr := ":8445"
	certFile := "/app/certs/gateway.crt"
	keyFile := "/app/certs/gateway.key"
	log.Printf("bank-api listening on %s (TLS)\n", addr)
	if err := http.ListenAndServeTLS(addr, certFile, keyFile, mux); err != nil {
		log.Fatalf("server failed: %v", err)
	}
}
