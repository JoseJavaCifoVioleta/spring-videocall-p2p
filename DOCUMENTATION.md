# 📘 Documentación Técnica y Arquitectura del Sistema

Este documento detalla la arquitectura de software, los flujos de comunicación y las decisiones de diseño implementadas en el MVP de videollamadas Peer-to-Peer utilizando **Spring Boot** y **WebRTC**.

---

## 1. Patrón de Arquitectura: Servidor de Señalización (Signaling)

En un sistema de comunicación en tiempo real basado en WebRTC puro, los flujos de audio y vídeo no pasan por el servidor de Java. Pasar streams de vídeo por Spring Boot degradaría el rendimiento debido al alto consumo de CPU y ancho de banda. 

En su lugar, se ha implementado una arquitectura de **Servidor de Señalización**. Spring Boot actúa exclusivamente como un "intermediario de citas" (Matchmaker). Su única función es conectar a dos clientes para que estos intercambien sus datos de configuración de red y multimedia. Una vez realizada esta negociación, el servidor se retira y los clientes se comunican directamente (**Peer-to-Peer**).

### Componentes Tecnológicos:
*   **Spring WebSockets**: Canal de comunicación bidireccional permanente y de baja latencia entre el navegador y el servidor.
*   **STOMP (Simple Text Oriented Messaging Protocol)**: Protocolo de mensajería subyacente sobre WebSockets que define un patrón de Publicación/Suscripción (Pub/Sub) mediante destinos (`/app` y `/topic`).
*   **SockJS**: Capa de abstracción local que garantiza la conectividad incluso en entornos de red restrictivos o proxies que bloqueen WebSockets nativos.

---

## 2. Flujo de Comunicación (Diagrama de Secuencia)

El proceso de establecimiento de la conexión para la videollamada consta de tres fases críticas gestionadas a través del protocolo diseñado en el **Sprint 1**:

```text
 Cliente A (Navegador)               Spring Boot (Broker)              Cliente B (Navegador)
       │                                       │                                       │
       ├───────── 1. Conexión WebSocket ──────>│                                       │
       │          (Endpoint: /ws-signaling)    │                                       │
       │                                       │<──────── 1. Conexión WebSocket ───────┤
       │                                       │          (Endpoint: /ws-signaling)    │
       │                                       │                                       │
       ├───────── 2. Suscripción a Sala ──────>│                                       │
       │          (/topic/sala.sala1)          │                                       │
       │                                       │<──────── 2. Suscripción a Sala ───────┤
       │                                       │          (/topic/sala.sala1)          │
       │                                       │                                       │
       ├───────── 3. Envío SDP Offer ─────────>│                                       │
       │          (Ruta: /app/chat.sala1)      │───────── 4. Retransmisión SDP ───────>│
       │                                       │                                       │
       │                                       │<──────── 5. Envío SDP Answer ─────────┤
       │                                       │          (Ruta: /app/chat.sala1)      │
       │<──────── 6. Retransmisión SDP ────────┤                                       │
       │                                       │                                       │
       ├───────── 7. Intercambio ICE ─────────>│                                       │
       │                                       │───────── 8. Retransmisión ICE ───────>│
       │                                       │                                       │
       │███████████████████████████████████████████████████████████████████████████████│
       │               CONEXIÓN DIRECTA ESTABLECIDA (Peer-to-Peer)             │
       │                 Flujo de Vídeo/Audio fuera de Spring Boot             │
       └───────────────────────────────────────────────────────────────────────┘
```

### Explicación del Ciclo de Vida:
1. **Conexión e Inicialización**: Ambos clientes inicializan sus librerías locales (`sockjs.min.js` y `stomp.min.js`) y abren el canal físico.
2. **Suscripción (Handshake)**: Los clientes escuchan activamente el sub-canal dinámico de su sala (`/topic/sala.{roomId}`).
3. **Intercambio de Credenciales Multimedia (SDP)**: El iniciador envía un objeto JSON de tipo `OFFER` con los codecs soportados. El destinatario responde con un JSON de tipo `ANSWER`. El controlador `@MessageMapping` de Spring Boot actúa como un espejo distribuyendo los payloads.
4. **Conexión P2P**: Los navegadores procesan las señales e inician el streaming directo.

---

## 3. Estructura de Clases e Infraestructura de Código

El backend se ha estructurado bajo las mejores prácticas de modularidad de Spring:

*   **`WebSocketConfig.java`**: Configura el registro de endpoints. Restringe y securiza los orígenes de las peticiones (`setAllowedOriginPatterns("*")`) y define los prefijos de enrutamiento del broker de mensajes.
*   **`SignalingController.java`**: Controlador desacoplado de la lógica de red. Captura variables de ruta síncronas empleando `@DestinationVariable` para permitir infinitas salas en paralelo sin colisiones de datos.
*   **`ChatMessage.java`**: POJO (Plain Old Java Object) de transferencia de datos optimizado. Cuenta con un atributo mutante (`type`) diseñado estratégicamente para transportar texto plano en fases de prueba, descriptores de sesión (SDP) o candidatos ICE de red sin requerir refactorizaciones en el modelo del Backend.

---

## 4. Decisiones de Despliegue y Portabilidad

Para mitigar fallos de latencia por resolución DNS y bloqueos de seguridad perimetral de navegadores modernos (Políticas de CORS y scripts de terceros), se tomó la decisión arquitectónica de **alojar las dependencias JavaScript en el almacenamiento estático local** (`src/main/resources/static/js/`). 

Esto aporta tres ventajas críticas para la defensa del proyecto:
1. **Aislamiento de Red**: La aplicación no requiere conexión a internet externa para compilar o interactuar con el cliente Web.
2. **Cumplimiento de Seguridad**: Evita inyecciones de código de terceros y alertas de "Mixed Content" en el navegador.
3. **Portabilidad**: Facilita el empaquetado final en un único archivo ejecutable `.jar` autocontenido.
