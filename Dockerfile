# ---- Etapa 1: Compilador (Builder) ----
# Usamos la imagen oficial de Go (Alpine es ligera)
FROM golang:1.21-alpine AS builder

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos los archivos de dependencias primero (para caché)
COPY go.mod ./
COPY go.sum ./

# Descargamos las dependencias
RUN go mod download

# Copiamos todo el código fuente del proyecto
COPY . .

# Compilamos la aplicación para Linux, deshabilitando CGO
# Esto crea un binario estático que funciona perfectamente en Alpine
# El binario se guardará como '/app/main'
RUN CGO_ENABLED=0 GOOS=linux go build -o /app/main .

# ---- Etapa 2: Final (Producción) ----
# Usamos una imagen base mínima (Alpine)
FROM alpine:latest

# ¡IMPORTANTE! Para que Mongo Atlas (mongodb+srv://) funcione
# Necesitamos los certificados raíz de confianza
RUN apk add --no-cache ca-certificates

# Establecemos el directorio de trabajo
WORKDIR /root/

# Copiamos SOLO el binario compilado de la etapa 'builder'
COPY --from=builder /app/main .

# Exponemos el puerto que tu app Go está usando (8083)
EXPOSE 8083

# El comando que se ejecutará cuando el contenedor inicie
CMD ["./main"]