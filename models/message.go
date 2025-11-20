package models

import (
	"time"
	"go.mongodb.org/mongo-driver/bson/primitive"
)

type LocationData struct {
	Latitude  float64 `bson:"latitude" json:"latitude"`
	Longitude float64 `bson:"longitude" json:"longitude"`
}

type Message struct {
	ID			primitive.ObjectID	`bson:"_id,omitempty" json:"id"`
	ChatID		string				`bson:"chat_id" json:"chat_id"`
	SenderID	string				`bson:"sender_id" json:"sender_id"`
	Text     	string				`bson:"text,omitempty" json:"text,omitempty"`
	Timestamp	time.Time			`bson:"timestamp" json:"timestamp"`
	SenderRol	string				`bson:"sender_rol,omitempty" json:"sender_rol,omitempty"`

	Type		string				`bson:"type" json:"type"`
	Location	*LocationData		`bson:"location,omitempty" json:"location,omitempty"`
}
