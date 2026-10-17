package com.proyecto.videocall.controller;

import com.proyecto.videocall.model.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.Map;

@Controller
public class SignalingController {

    // Endpoint REST para que el frontend pregunte a qué sala por IP pertenece
    @GetMapping("/api/my-room")
    @ResponseBody
    public Map<String, String> getAutomaticRoom(SimpMessageHeaderAccessor headerAccessor) {
        // En conexiones REST directas o iniciales podemos recuperar atributos del contexto
        // Para desarrollo local devolvemos una sala por defecto, en producción devolverá la IP
        return Collections.singletonMap("roomId", "network-room-auto");
    }

    @MessageMapping("/chat.{roomId}")
    @SendTo("/topic/sala.{roomId}")
    public ChatMessage handleSignalingMessage(@DestinationVariable String roomId, ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        // Recuperamos la IP guardada por el interceptor si quisiéramos registrar logs de auditoría
        String clientIp = (String) headerAccessor.getSessionAttributes().get("clientIp");
        System.out.println("Mensaje procesado en sala [" + roomId + "] desde la IP: " + clientIp);

        return message;
    }
}
