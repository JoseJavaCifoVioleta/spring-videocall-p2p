package com.proyecto.videocall.controller;

import com.proyecto.videocall.model.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class SignalingController {

    // Escucha los mensajes enviados a /app/chat.{roomId}
    @MessageMapping("/chat.{roomId}")
    // Retransmite el mensaje automáticamente a todos los suscritos a /topic/sala.{roomId}
    @SendTo("/topic/sala.{roomId}")
    public ChatMessage handleSignalingMessage(@DestinationVariable String roomId, ChatMessage message) {
        // En un entorno real aquí podríamos guardar logs o validar al usuario.
        // Por ahora, actuamos como un espejo: lo que entra de un usuario se difunde a la sala.
        return message;
    }
}
