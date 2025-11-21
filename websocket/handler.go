package websocket

import (
    "log"
    "net/http"

    "github.com/alhexmbs/cita-salud-realtime-chat-service/auth"
    "github.com/alhexmbs/cita-salud-realtime-chat-service/hub"
    "github.com/gorilla/websocket"
)

var upgrader = websocket.Upgrader{
    ReadBufferSize:  1024,
    WriteBufferSize: 1024,
    CheckOrigin: func(r *http.Request) bool {
        return true
    },
}

func HandleConnection(hubInstance *hub.Hub, w http.ResponseWriter, r *http.Request) {

    // extraer y validar el token
    tokenString := r.URL.Query().Get("token")
    if tokenString == "" {
        log.Println("Rechazado. Falta el token papu")
        http.Error(w, "Falta el token papito", http.StatusUnauthorized)
        return
    }

    claims, err := auth.ValidateToken(tokenString)
    if err != nil {
        log.Printf("Rechazado: Token inválido (%v)", err)
        http.Error(w, "Token inválido", http.StatusUnauthorized)
        return
    }

    // actualizar a WebSocket
    conn, err := upgrader.Upgrade(w, r, nil)
    if err != nil {
        log.Println("Error al actualizar a WebSocket:", err)
        return
    }

    // ---------------------------------------------------------
    // LÓGICA PARA DECIDIR EL ID (EL CAMBIO ESTÁ AQUÍ)
    // ---------------------------------------------------------
    
    // por defecto el UserID normal que es para pacientes
    chatUserID := claims.UserID 

    // si es médico y trae el OID -> id_personal_especialidad, ese es el ID que uso
    if claims.Rol == "personal_medico" && claims.OID != "" {
        chatUserID = claims.OID
    }
    // ---------------------------------------------------------

    client := &hub.Client{
        Hub:    hubInstance,
        Conn:   conn,
        Send:   make(chan []byte, 256),
        UserID: chatUserID, // usamos la variable calculada
        Rol:    claims.Rol,
    }

    // registra un nuevo cliente en el hub
    client.Hub.Register <- client

    log.Printf("Cliente conectado (ID_Chat: %s, Rol: %s, ID_Real: %s)", client.UserID, client.Rol, claims.UserID)

    go client.WritePump()
    go client.ReadPump()
}