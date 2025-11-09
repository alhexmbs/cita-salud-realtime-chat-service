# ---- Etapa 1: Compilador (Builder) ----
# CAMBIO: Usamos Go 1.25 para cumplir con el go.mod
FROM golang:1.25-alpine AS builder

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
RUN CGO_ENABLED=0 GOOS=linux go build -o /app/main .

# ---- Etapa 2: Final (Producción) ----
# Usamos una imagen base mínima (Alpine)
FROM alpine:latest

# ¡IMPORTANTE! Para que Mongo Atlas (mongodb+srv://) funcione
RUN apk add --no-cache ca-certificates

# Establecemos el directorio de trabajo
WORKDIR /root/

# Copiamos SOLO el binario compilado de la etapa 'builder'
COPY --from=builder /app/main .

# Exponemos el puerto que tu app Go está usando (8083)
EXPOSE 8083

# El comando que se ejecutará cuando el contenedor inicie
CMD ["./main"]