package hub

import (
	"log"
	"github.com/gorilla/websocket"
)

type Client struct {
	Hub *Hub
	Conn *websocket.Conn
	Send chan []byte
	UserID string
	Rol string
}

// se encarga de leer mensajes del WebSocket y pasarlos al Hub
func (c *Client) ReadPump() {
	defer func() {
		c.Hub.Unregister <- c
		c.Conn.Close()
	}()

	for {
		_, message, err := c.Conn.ReadMessage()
		if err != nil {
			log.Printf("Error al leer mensaje (cliente desconectado): %v", err)
			break
		}
		
		incoming := &IncomingMessage{
			Sender: 		c,
			MessageBytes: 	message,
		}

		c.Hub.Broadcast <- incoming
	}
}

// se encarga de tomar mensajes del canal 'Send' y escribirlos en el WebSocket
func (c *Client) WritePump() {
	// asegurar que la conexión se cierre al salir
	defer func() {
		c.Conn.Close()
	}()

	for message := range c.Send {
		err := c.Conn.WriteMessage(websocket.TextMessage, message)
		if err != nil {
			log.Printf("Error al escribir mensaje: %v", err)
			return
		}
	}
}