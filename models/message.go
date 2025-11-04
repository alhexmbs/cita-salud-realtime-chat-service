package models

import (
	"time"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type Message struct {
	// 'primitive.ObjectID' es el tipo de _id de MongoDB
	// 'bson:"_id,omitempty"' le dice a Go cómo llamarlo en MongoDB
	// 'json:"id"' le dice a Go cómo llamarlo en JSON
	ID			primitive.ObjectID	`bson:"_id,omitempty" json:"id"`
	ChatID		string				`bson:"chat_id" json:"chat_id"`
	UserID		string				`bson:"user_id" json:"user_id"`
	Rol			string				`bson:"rol" json:"rol"`
	Text		string 				`bson:"text" json:"text"`
	Timestamp	time.Time 			`bson:"timestamp" json:"timestamp"`
}