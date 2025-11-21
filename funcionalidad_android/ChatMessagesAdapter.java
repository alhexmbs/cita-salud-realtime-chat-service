package com.example.robles_farma.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robles_farma.R;
import com.example.robles_farma.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
package com.example.robles_farma.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.robles_farma.R;
import com.example.robles_farma.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_LOCATION_SENT = 3;
    private static final int VIEW_TYPE_LOCATION_RECEIVED = 4;

    private final List<ChatMessage> messages = new ArrayList<>();

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if ("location".equals(msg.getType())) {
            return msg.isSentByMe() ? VIEW_TYPE_LOCATION_SENT : VIEW_TYPE_LOCATION_RECEIVED;
        }
        return msg.isSentByMe() ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_LOCATION_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false); // Reutilizamos layout por simplicidad, idealmente uno propio
            return new LocationSentViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOCATION_RECEIVED) {
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new LocationReceivedViewHolder(view);
        } else if (viewType == VIEW_TYPE_SENT) {
            View view = inflater.inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        if (holder instanceof LocationSentViewHolder) {
            ((LocationSentViewHolder) holder).bind(message);
        } else if (holder instanceof LocationReceivedViewHolder) {
            ((LocationReceivedViewHolder) holder).bind(message);
        } else if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void setMessages(List<ChatMessage> newMessages) {
        messages.clear();
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    // ViewHolder para mensajes enviados (azul, derecha)
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText, tvMessageTime;

        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }

        public void bind(ChatMessage message) {
            tvMessageText.setText(message.getText());
            tvMessageTime.setText(formatTime(message.getTimestamp()));
        }
    }

    // ViewHolder para mensajes recibidos (blanco, izquierda)
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName, tvMessageText, tvMessageTime;

        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }

        public void bind(ChatMessage message) {
            tvSenderName.setText(message.getSenderName());
            tvMessageText.setText(message.getText());
            tvMessageTime.setText(formatTime(message.getTimestamp()));
        }
    }

    // ViewHolder para UBICACIÓN enviada
    static class LocationSentViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText, tvMessageTime;

        public LocationSentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }

        public void bind(ChatMessage message) {
            tvMessageText.setText("📍 " + message.getText());
            tvMessageText.setOnClickListener(v -> openMap(v.getContext(), message.getLatitude(), message.getLongitude()));
            tvMessageTime.setText(formatTime(message.getTimestamp()));
        }
    }

    // ViewHolder para UBICACIÓN recibida
    static class LocationReceivedViewHolder extends RecyclerView.ViewHolder {
        TextView tvSenderName, tvMessageText, tvMessageTime;

        public LocationReceivedViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
        }

        public void bind(ChatMessage message) {
            tvSenderName.setText(message.getSenderName());
            tvMessageText.setText("📍 " + message.getText());
            tvMessageText.setOnClickListener(v -> openMap(v.getContext(), message.getLatitude(), message.getLongitude()));
            tvMessageTime.setText(formatTime(message.getTimestamp()));
        }
    }

    private static void openMap(android.content.Context context, double lat, double lng) {
        try {
            android.net.Uri gmmIntentUri = android.net.Uri.parse("geo:" + lat + "," + lng + "?q=" + lat + "," + lng + "(Ubicación)");
            android.content.Intent mapIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            context.startActivity(mapIntent);
        } catch (Exception e) {
            android.widget.Toast.makeText(context, "Google Maps no instalado", android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private static String formatTime(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            Date date = inputFormat.parse(timestamp);
            return date != null ? outputFormat.format(date) : timestamp;
        } catch (Exception e) {
            // Si falla el parseo, mostrar solo la hora si está disponible
            if (timestamp.length() > 10) {
                return timestamp.substring(11, 16);
            }
            return timestamp;
        }
    }
}