package hub

import (
	"context"
	"encoding/json"
	"log"
	"time"

	"github.com/alhexmbs/cita-salud-realtime-chat-service/db"
	"github.com/alhexmbs/cita-salud-realtime-chat-service/models"

	"go.mongodb.org/mongo-driver/bson/primitive"
)

// wrapper que une el mensaje crudo con el cliente que lo envió
type IncomingMessage struct {
	Sender 			*Client
	MessageBytes 	[]byte
}

type Hub struct {
	// conjunto de clientes conectados
	Clients map[*Client]bool

	// canal para mensajes entrantes de los clientes a difundir
	Broadcast chan *IncomingMessage

	// canal para registrar nuevos clientes
	Register chan *Client

	// canal para des-registrar clientes que se desconectan
	Unregister chan *Client
}

func NewHub() *Hub {
	return &Hub{
		Clients: 	make(map[*Client]bool),
		Broadcast:  make(chan *IncomingMessage),
		Register:   make(chan *Client),
		Unregister: make(chan *Client),
	}
}

func (h *Hub) Run() {
	for {
		select {
		// un nuevo cliente se conecta
		case client := <-h.Register:
			h.Clients[client] = true
		
		// un cliente se ha desconectado
		case client := <-h.Unregister:
			if _, ok := h.Clients[client]; ok {
				close(client.Send)
				delete(h.Clients, client)
			}
		
		// un cliente ha enviado un mensaje
		case incoming := <-h.Broadcast:
			// decodificar el mensaje, enviado en JSON
			var msgData struct {
				Text string `json:"text"`
			}

			// intenta decodificar el mensaje
			if err := json.Unmarshal(incoming.MessageBytes, &msgData); err != nil {
				log.Printf("Error al decodificar el mensaje: %v", err)
				continue
			}

			// crea el objeto de la base de datos
			newMsg := models.Message{
				ID: 	  	primitive.NewObjectID(),
				Text:		msgData.Text,
				Timestamp:	time.Now(),
				UserID:		incoming.Sender.UserID,
				Rol:		incoming.Sender.Rol,
			}

			// guarda el mensaje en mongo, en la colección "messages"
			collection := db.DB.Collection("messages")
			_, err := collection.InsertOne(context.Background(), newMsg)
			if err != nil {
				log.Printf("Error al guardar el mensaje en la BD: %v", err)
			}

			// prepara el mensaje completo para enviar a los clientes
			fullMessageBytes, err := json.Marshal(newMsg)
			if err != nil {
				log.Printf("Error al codificar el mensaje completo: %v", err)
				continue
			}

			// difunde el mensaje a todos los clientes conectados
			for client := range h.Clients {
				select {
				case client.Send <- fullMessageBytes:
					// el mensaje fue enviado
				default:
					close(client.Send)
					delete(h.Clients, client)
				}
				
			}
		}
	}
}