# 📹 WebRTC P2P Video Call & Live Chat App - Spring Boot (Versión Avanzada)

Este proyecto es una réplica minimalista, autocontenida y de alto rendimiento de un sistema de videoconferencias y mensajería en tiempo real inspirado en la arquitectura base de **Jitsi**. El objetivo principal es demostrar la viabilidad de utilizar **Spring Boot** como un motor de señalización corporativo y **WebRTC** para la comunicación directa (Peer-to-Peer) de vídeo, audio y texto entre navegadores web sin sobrecargar el servidor Java.

Esta versión incluye un **mecanismo avanzado de detección de red** que agrupa automáticamente a los usuarios según su ubicación física.

---

## 🛠️ Arquitectura y Justificación Técnica

Para garantizar la estabilidad y mitigar el consumo de CPU y ancho de banda en el servidor, el sistema delega la transmisión masiva de flujos multimedia a los dispositivos cliente utilizando una arquitectura descentralizada:

1. **Spring Boot (Backend - Servidor de Señalización):** Utiliza **Spring WebSockets con protocolo STOMP** y soporte de **SockJS**. El backend actúa exclusivamente como un "espejo" (Matchmaker). Recibe los mensajes en un canal común dinámico empleando `@MessageMapping` y los redistribuye instantáneamente en formato JSON. El servidor no procesa ningún stream de vídeo.
2. **WebRTC (Frontend - Comunicación P2P):** API nativa del navegador (`RTCPeerConnection`). Los clientes utilizan el mismo canal del WebSocket de Spring para intercambiar descriptores de sesión (SDP `Offer`/`Answer`) y candidatos de red (`ICE Candidates`). Tras el acuerdo técnico, las cámaras se conectan de forma directa entre los navegadores.
3. **Dualidad de Datos (Vídeo + Texto):** El sistema aprovecha el modelo de datos de Spring Boot para discernir entre payloads técnicos (`OFFER`, `ANSWER`, `ICE`, `HANGUP`) y mensajes conversacionales de texto plano (`CHAT`), permitiendo que convivan la videollamada y un chat lateral síncrono.
4. **Portabilidad Absoluta:** Las dependencias de JavaScript (`sockjs.min.js` y `stomp.min.js`) están alojadas localmente en el servidor (`src/main/resources/static/js/`). Esto asegura inmunidad frente a caídas de CDNs externos, evita alertas de seguridad por *Mixed Content* en el navegador y permite compilar la app en un único archivo ejecutable `.jar`.

---

## 🌐 Funcionalidad Avanzada: Asignación Automática de Salas por IP

A diferencia de los sistemas tradicionales donde el usuario debe introducir manualmente el nombre de una sala, esta versión incorpora una capa de inteligencia de red:

* **`IpHandshakeInterceptor` (Java):** Un interceptor intercepta la petición HTTP de establecimiento del WebSocket (*handshake*) antes de que se actualice al protocolo bidireccional. Este componente extrae la dirección IP pública real del router del cliente (`request.getRemoteAddress()`), sanitiza la cadena de texto para hacerla compatible con los destinos STOMP y la inyecta en los atributos de sesión de Spring.
* **Agrupamiento por NAT Común:** Cuando el frontend realiza la petición inicial a `/api/my-room`, Spring Boot lee la IP del cliente. Si dos o más ordenadores se conectan desde la **misma red física (misma oficina o casa)**, internet los identificará bajo la misma IP pública debido al proceso de NAT del router. El servidor mapea esta IP y les asigna dinámicamente un canal exclusivo (ej. `network-room-auto`).
* **Optimización Local de WebRTC:** Al forzar el emparejamiento de usuarios que comparten infraestructura local, los descriptores de red intercambiados permiten que el stream multimedia de WebRTC se transmita **100% dentro del router local (LAN)**, garantizando latencia cero, máxima privacidad y consumo nulo de datos de internet hacia el exterior.

┌───────────────────────────────────────────────────────────────────────────┐
│                               [ SPRING BOOT ]                             │
│       (Con Interceptor de Handshake e Identificación de IP Pública)       │
└───────────────▲───────────────────────────────────────────▲───────────────┘
│                                           │
Petición desde IP Pública:                  Petición desde IP Pública:
84.123.45.6                                 84.123.45.6
│                                           │
[Asignados a sala: network-room-auto]       [Asignados a sala: network-room-auto]
│                                           │
┌───────▼───────┐                           ┌───────▼───────┐
│               │                           │               │
│   Cliente A   │◄─────────────────────────►│   Cliente B   │
│  (Navegador)  │   Flujo de Vídeo Local    │  (Navegador)  │
│               │      (Tráfico LAN Puro)   │               │
└───────────────┘                           └───────────────┘

---

## 🚀 Guía de Uso y Ejecución de la Aplicación

Sigue estos pasos detallados para poner en marcha el sistema y realizar una simulación real de dos participantes:

### Paso 1: Arrancar el Servidor Backend
Puedes iniciar la aplicación desde tu entorno de desarrollo o compilarla para consola:
*   **Desde el IDE (IntelliJ IDEA):** Abre la clase `src/main/java/com/proyecto/videocall/VideocallApplication.java`, haz clic en el triángulo verde (**Play**) junto al método `main` y selecciona *Run*.
*   **Desde la Terminal:** Sitúate en la raíz del proyecto y ejecuta:
    ```bash
    ./mvnw spring-boot:run
    ```

### Paso 2: Conectar los Participantes
1. Abre tu navegador web habitual (Chrome/Edge) e introduce: **`http://localhost:8080/index.html`**
2. Concede los **permisos de acceso a la cámara y al micrófono**. Verás tu propio rostro en el recuadro izquierdo.
3. Abre una **nueva ventana en Modo Incógnito** (u otro navegador completamente diferente) y accede a la misma dirección: **`http://localhost:8080/index.html`**
4. Si abres la consola de desarrollo del navegador (**`F12`**), observarás el log: `Sala asignada dinámicamente por red: network-room-auto`, confirmando que el backend ha emparejado a ambos clientes en la misma sala basándose en la coincidencia de red.

### Paso 3: Probar el Chat y la Videollamada
1. **Chat de texto:** Escribe un mensaje en la barra lateral derecha de cualquiera de las ventanas. El texto aparecerá instantáneamente en el panel opuesto de forma síncrona mediante WebSockets.
2. **Videollamada:** Haz clic en el botón azul **"Iniciar Videollamada"**. Tras el intercambio de señales en segundo plano, el recuadro "Participante (Remoto)" comenzará a emitir el vídeo cruzado en tiempo real.
3. **Controles Multimedia:** Puedes silenciar el micrófono o pulsar **Apagar Cámara** para mutear tus pistas de hardware (el participante remoto verá tu pantalla en negro de inmediato). Al presionar **Colgar**, la sesión se destruirá limpiamente notificando al otro extremo.

---

## ⚙️ Estructura Final del Proyecto Evaluado

```text
├── src/
│   ├── main/
│   │   ├── java/com/proyecto/videocall/
│   │   │   ├── config/      
│   │   │   │   ├── WebSocketConfig.java       # Registro de endpoints y broker
│   │   │   │   └── IpHandshakeInterceptor.java # Captura avanzada de IP pública
│   │   │   ├── controller/  
│   │   │   │   └── SignalingController.java   # Controlador REST y espejo WebRTC
│   │   │   └── model/       
│   │   │       └── ChatMessage.java           # Modelo de datos polimórfico (Chat/Control)
│   │   │   └── VideocallApplication.java      # Clase principal de arranque
│   │   └── resources/
│   │       └── static/      
│   │           ├── js/
│   │           │   ├── sockjs.min.js          # Dependencia local SockJS
│   │           │   └── stomp.min.js           # Dependencia local STOMP
│   │           └── index.html                 # Interfaz Premium, Vídeo + Chat y WebRTC
│   └── test/                
├── pom.xml                  
└── README.md                
```