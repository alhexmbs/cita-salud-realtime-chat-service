package websocket

import (
	"log"
	"net/http"
	"github.com/gorilla/websocket"
	"github.com/alhexmbs/cita-salud-realtime-chat-service/hub"
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
	conn, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Println("Error al actualizar a WebSocket:", err)
		return
	}

	client := &hub.Client{
		Hub: hubInstance,
		Conn: conn,
		Send: make(chan []byte, 256),
	}

	// registra un nuevo cliente en el hub
	client.Hub.Register <- client

	go client.WritePump()
	go client.ReadPump()
}
