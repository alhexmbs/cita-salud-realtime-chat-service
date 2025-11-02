package main

import (
	"fmt"
	"log"
	"net/http"
	"github.com/alhexmbs/cita-salud-realtime-chat-service/config"
	"github.com/alhexmbs/cita-salud-realtime-chat-service/websocket"
	"github.com/alhexmbs/cita-salud-realtime-chat-service/hub"
	"github.com/alhexmbs/cita-salud-realtime-chat-service/db"
)

func handleHome(w http.ResponseWriter, r *http.Request) {
	fmt.Fprint(w, "El servidor vive!!")
}

func main() {
	config.LoadConfig()

	db.ConnectDB()

	// crea una instancia del hub
	hubInstance := hub.NewHub()

	// inicia el hub en una goroutine separada
	go hubInstance.Run()

	http.HandleFunc("/", handleHome)

	http.HandleFunc("/ws", func(w http.ResponseWriter, r *http.Request) {
		websocket.HandleConnection(hubInstance, w, r)
	})

	log.Println("Servidor iniciado en http://localhost:8083")

	if err := http.ListenAndServe(":8083", nil); err != nil {
		log.Fatalf("Error al iniciar el servidor: %v", err)
	}
}