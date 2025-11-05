package db

import (
	"context"
	"log"
	"time"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
)

var DB *mongo.Database

// uri local por ahora. luego la cambio (/config)
const uri = "mongodb://localhost:27017"

func ConnectDB() {
	// definición del contexto con timeout
	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	// creacion del cliente
	client, err := mongo.Connect(ctx, options.Client().ApplyURI(uri))
	if err != nil {
		log.Fatal("Error al conectar a MongoDB:", err)
	}

	// haciendo ping para verificar la conexion
	err = client.Ping(ctx, nil)
	if err != nil {
		log.Fatal("Error al hacer ping a mongo: ", err)
	}

	log.Println("Conexión exitosa a mongo xd")

	DB = client.Database("cita-salud-chat")
}