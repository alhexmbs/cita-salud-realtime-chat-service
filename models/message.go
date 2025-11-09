package models

import (
	"time"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

// estructura de un mensaje

type Message struct {
	ID        primitive.ObjectID `bson:"_id,omitempty" json:"id"`
	ChatID    string             `bson:"chat_id" json:"chat_id"`
	SenderID  string             `bson:"sender_id" json:"sender_id"`
	Text      string             `bson:"text" json:"text"`
	Timestamp time.Time          `bson:"timestamp" json:"timestamp"`
	SenderRol string             `bson:"sender_rol,omitempty" json:"sender_rol,omitempty"`
}