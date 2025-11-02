package auth

import (
	"fmt"

	"github.com/alhexmbs/cita-salud-realtime-chat-service/config"
	"github.com/golang-jwt/jwt/v5"
)

type CustomClaims struct {
	UserID string `json:"sub"`
	Rol string `json:"rol"`
	jwt.RegisteredClaims
}

func ValidateToken(tokenString string) (*CustomClaims, error) {
	// Parsea el token
	// 'jwt.ParseWithClaims' toma el token, la struct de claims, y la función de validación
	token, err := jwt.ParseWithClaims(tokenString, &CustomClaims{}, func(token *jwt.Token) (interface{}, error) {
		// verifica el método de firma (algoritmo)
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("método de firma inesperado: %v", token.Header["alg"])
		}
		
		// devuelve el secreto (convertido a bytes)
		return []byte(config.AppConfig.JwtSecret), nil
	})

	if err != nil {
		return nil, fmt.Errorf("error al parsear el token: %v", err)
	}

	// si el token es válido, extrae los claims
	if claims, ok := token.Claims.(*CustomClaims); ok && token.Valid {
		return claims, nil
	}
	
	return nil, fmt.Errorf("token inválido")
}