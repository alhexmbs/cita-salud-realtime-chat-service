package hub

type Hub struct {
	// conjunto de clientes conectados
	Clients map[*Client]bool

	// canal para mensajes entrantes de los clientes a difundir
	Broadcast chan []byte

	// canal para registrar nuevos clientes
	Register chan *Client

	// canal para des-registrar clientes que se desconectan
	Unregister chan *Client
}

func NewHub() *Hub {
	return &Hub{
		Clients: 	make(map[*Client]bool),
		Broadcast:  make(chan []byte),
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
		case message := <-h.Broadcast:
			for client := range h.Clients {
				select {
				case client.Send <- message:
					// el mensaje fue enviado
				default:
					close(client.Send)
					delete(h.Clients, client)
				}
				
			}
		}
	}
}