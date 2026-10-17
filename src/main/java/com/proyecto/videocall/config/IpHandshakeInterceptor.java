package com.proyecto.videocall.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class IpHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        // Obtenemos la IP pública del cliente que realiza la petición
        String clientIp = request.getRemoteAddress().getAddress().getHostAddress();

        // Reemplazamos puntos o caracteres conflictivos para evitar fallos en las rutas de STOMP
        clientIp = clientIp.replace(".", "-").replace(":", "-");

        // Almacenamos la IP en los atributos de la sesión del WebSocket
        attributes.put("clientIp", clientIp);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No se requiere lógica adicional después del handshake
    }
}
