# Guía del proyecto


## 1. Raiz del proyecto
- `go.mod`: Este es el archivo más importante para la gestión del proyecto. Se crea con el comando `go mod init <nombre-del-proyecto>`.

- `main.go`: Es el punto de arranque. Su única responsabilidad es "ensamblar" la aplicación:
    1. Cargar la configuración (`/config/`).
    2. Conectar a la base de datos (`/db/`).
    3. Crear e iniciar el Hub (`/hub/`).
    4. Definir la ruta HTTP (ej. `/ws`) y asignarle el manejador (`/websocket/`).
    5. Arrancar el servidor HTTP.

## 2. Configuración
- `/config/`: Este paquete leerá el archivo `.env` o las variables de entorno del sistema. Expondrá una estructura `(struct)` simple con las configuraciones (como `Config.Port` o `Config.JwtSecret`).

## 3. Autenticación
- `/auth/`
Este paquete es vital para la arquitectura de microservicios. Su trabajo no es crear tokens, sino validarlos. Tendrá una función como `ValidarToken(tokenString, claveSecreta) (*Claims, error)` que el manejador de WebSocket usará antes de permitir la conexión.

## 4. Base de datos
- `/db/`
Todo lo relacionado con MongoDB va aquí. Tendrá funciones para:
    - Conectar()
    - GuardarMensaje(mensaje)
    - ObtenerHistorialDeSala(salaID)

El `Hub` usará este paquete para persistir los mensajes.

## 5. El cerebro del chat
- `/hub/` 
Este es el componente más importante y el núcleo de la lógica de concurrencia de Go. Lo dividimos en dos archivos:
    - `client.go`: Define una `struct` (estructura) llamada `Client`. Representa a un usuario conectado. Contiene su conexión WebSocket `(*websocket.Conn)`, su `userID` (obtenido del token JWT) y un canal para enviarle mensajes.
    - `hub.go`: Define la `struct` `Hub`. Es el "controlador de tráfico" o la "sala de chat".
        - Mantiene un `map` de todos los clientes conectados.
        - Tiene canales (channels) para `register` (un cliente nuevo), `unregister` (un cliente se va) y `broadcast` (enviar un mensaje a todos).
        - **Esta es la parte que usa Goroutines y Canales.**

## 6. El portero
- `/websocket/` 
Este es el "controlador" HTTP. Es una simple función que:
    1. Recibe la petición HTTP (`http.ResponseWriter`, `*http.Request`).
    2. Extrae el token JWT (quizás de un query param como `?token=...`).
    3. Usa el paquete `/auth/` para validar el token y obtener el `userID`.
    4. Si es válido, "actualiza" la conexión de HTTP a WebSocket (usando `gorilla/websocket`).
    5. Crea un nuevo objeto `Client` (de `/hub/client.go`).
    6. Registra ese nuevo cliente en el `Hub`.

## 🚀 El flujo de una conexión
1. Un cliente (Vue o Kotlin) intenta conectarse a `ws://tu-api.com/ws?token=....`
2. El `/websocket/handler.go` recibe la petición.
3. Usa `/auth/` para validar el token.
4. Si es válido, crea un `Client` y lo pasa al canal `register` del `Hub`.
5. El `Hub` (corriendo en su propia Goroutine) recibe al cliente y lo añade a su `map` de clientes activos.
6. Ahora el `Client` está en dos bucles (en dos Goroutines separadas):
    - **Leer**: Escuchando mensajes del WebSocket del usuario.
    - **Escribir**: Escuchando mensajes que el `Hub` quiere enviarle.
7. Cuando el Client lee un mensaje de Vue, lo pasa al canal broadcast del Hub.
8. El Hub recibe el mensaje, usa /db/ para guardarlo en MongoDB, y luego lo reenvía a todos los demás clientes en su map.