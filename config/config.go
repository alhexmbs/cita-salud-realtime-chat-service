package config

import (
	"log"
	"os"
	"github.com/joho/godotenv"
)


// config es una struct que contendrá toas las variables de entorno
type Config struct {
	JwtSecret string
}

// instancia global de la configuración
var AppConfig Config


// carga las variables de entorno desde .env
func LoadConfig() {
	// carga el archivo .env
	if err := godotenv.Load(); err != nil {
		log.Println("No se encontró el archivo .env, usando variables de entorno del sistema")
	}

	// lee la variable JWT_SECRET
	AppConfig.JwtSecret = os.Getenv("JWT_SECRET")
	if AppConfig.JwtSecret == "" {
		log.Fatal("FATAL: JWT_SECRET no está definida en las variables de entorno")
	}
}