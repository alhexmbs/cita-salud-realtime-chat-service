package main

import (
	"fmt"
	"log"
	"net/http"
)

func handleHome(w http.ResponseWriter, r *http.Request) {
	fmt.Fprint(w, "El servidor vive!!")
}

func main() {
	http.HandleFunc("/", handleHome)

	log.Println("Servidor iniciado en http://localhost:8083")

	if err := http.ListenAndServe(":8083", nil); err != nil {
		log.Fatalf("Error al iniciar el servidor: %v", err)
	}
}