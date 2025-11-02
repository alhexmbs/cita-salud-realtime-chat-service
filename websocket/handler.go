package websocket

import (
	"log"
	"net/http"

	"github.com/alhexmbs/cita-salud-realtime-chat-service/auth"
	"github.com/alhexmbs/cita-salud-realtime-chat-service/hub"
	"github.com/gorilla/websocket"
)

var upgrader = websocket.Upgrader{
	// tamaño de los buffers de lectura y escritura
	ReadBufferSize: 1024,
	WriteBufferSize: 1024,

	//permitir o denegar conexiones desde otros dominios
	CheckOrigin: func(r *http.Request) bool {
		return true // por ahora permitimos todas las conexiones
	},
}

func HandleConnection(hubInstance *hub.Hub, w http.ResponseWriter, r *http.Request) {

	// extraer el token de la query
	tokenString := r.URL.Query().Get("token")
	if tokenString == "" {
		log.Println("Rechazado. Falta el token papu")
		http.Error(w, "Falta el token papito", http.StatusUnauthorized)
	}

	// validar el token
	claims, err := auth.ValidateToken(tokenString)
	if err != nil {
		log.Printf("Rechazado: Token inválido (%v)", err)
		http.Error(w, "Token inválido", http.StatusUnauthorized)
		return
	}

	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Println("Error al actualizar a WebSocket:", err)
		return
	}

	client := &hub.Client{
		Hub: hubInstance,
		Conn: conn,
		Send: make(chan []byte, 256),
		UserID: claims.UserID,
		Rol: claims.Rol,
	}

	// registra un nuevo cliente en el hub
	client.Hub.Register <- client

	log.Printf("Cliente conectado (Usuario: %s, Rol: %s)", client.UserID, client.Rol)

	go client.WritePump()
	go client.ReadPump()
}
