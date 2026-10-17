# 📹 WebRTC P2P Video Call & Live Chat App - Spring Boot

Este proyecto es una réplica minimalista, autocontenida y de alto rendimiento de un sistema de videoconferencias y mensajería en tiempo real inspirado en la arquitectura base de **Jitsi**. El objetivo principal es demostrar la viabilidad de utilizar **Spring Boot** como un motor de señalización corporativo y **WebRTC** para la comunicación directa (Peer-to-Peer) de vídeo, audio y texto entre navegadores web sin sobrecargar el servidor Java.

Prototipo diseñado, planificado y desarrollado como **Proyecto Final de Curso**.

---

## 🛠️ Arquitectura y Justificación Técnica

Para garantizar la estabilidad y mitigar el consumo de CPU y ancho de banda en el servidor, el sistema delega la transmisión masiva de flujos multimedia a los dispositivos cliente utilizando una arquitectura descentralizada:

1. **Spring Boot (Backend - Servidor de Señalización):** Utiliza **Spring WebSockets con protocolo STOMP** y soporte de **SockJS**. El backend actúa exclusivamente como un "espejo" (Matchmaker). Recibe los mensajes en un canal común dinámico empleando `@MessageMapping` y los redistribuye instantáneamente en formato JSON. El servidor no procesa ningún stream de vídeo.
2. **WebRTC (Frontend - Comunicación P2P):** API nativa del navegador (`RTCPeerConnection`). Los clientes utilizan el mismo canal del WebSocket de Spring para intercambiar descriptores de sesión (SDP `Offer`/`Answer`) y candidatos de red (`ICE Candidates`). Tras el acuerdo técnico, las cámaras se conectan de forma directa entre los navegadores.
3. **Dualidad de Datos (Vídeo + Texto):** El sistema aprovecha el modelo de datos de Spring Boot para discernir entre payloads técnicos (`OFFER`, `ANSWER`, `ICE`) y mensajes conversacionales de texto plano (`CHAT`), permitiendo que convivan la videollamada y un chat lateral síncrono.
4. **Portabilidad Absoluta:** Las dependencias de JavaScript (`sockjs.min.js` y `stomp.min.js`) están alojadas localmente en el servidor (`src/main/resources/static/js/`). Esto asegura inmunidad frente a caídas de CDNs externos, evita alertas de seguridad por *Mixed Content* en el navegador y permite compilar la app en un único archivo ejecutable `.jar`.

┌───────────────────────────────────────────────────────────────────────────┐
│                               [ SPRING BOOT ]                             │
│                      (WebSocket / STOMP Signaling Broker)                 │
└───────────────▲───────────────────────────────────────────▲───────────────┘
│                                           │
Mensajes de Control (WebRTC)                Mensajes de Control (WebRTC)
y Chat Escrito                              y Chat Escrito
│                                           │
┌───────▼───────┐                           ┌───────▼───────┐
│               │                           │               │
│   Cliente A   │◄─────────────────────────►│   Cliente B   │
│  (Navegador)  │   Canal Directo de Vídeo  │  (Navegador)  │
│               │      (WebRTC P2P Stream)  │               │
└───────────────┘                           └───────────────┘

---

## 📋 Requisitos del Entorno

*   **Java Development Kit (JDK):** Versión 17 o superior.
*   **Gestor de Proyectos:** Apache Maven 3.8+.
*   **Framework Principal:** Spring Boot 3.3.x (Web y WebSocket).
*   **Navegador Recomendado:** Google Chrome o Microsoft Edge actualizados.
*   **Hardware:** Cámara web y micrófono funcionales.

---

## 🚀 Guía de Uso y Ejecución de la Aplicación

Sigue estos pasos detallados para poner en marcha el sistema y realizar una simulación real de dos participantes:

### Paso 1: Arrancar el Servidor Backend
Puedes iniciar la aplicación desde tu entorno de desarrollo o compilarla para consola:

*   **Desde el IDE (IntelliJ IDEA):** Abre la clase `src/main/java/com/proyecto/videocall/VideocallApplication.java`, haz clic en el triángulo verde (**Play**) junto al método `main` y selecciona *Run*.
*   **Desde la Terminal (Línea de comandos):** Sitúate en la raíz del proyecto y ejecuta:
    ```bash
    ./mvnw spring-boot:run
    ```
*   **Verificación:** Confirma en la consola de salida que aparece el mensaje de inicialización exitosa:  
    `Tomcat started on port 8080 (http) with context path ''`

### Paso 2: Conectar al Primer Participante (Usuario A)
1. Abre tu navegador web habitual (Chrome/Edge).
2. Entra en la dirección exacta del servidor local: **`http://localhost:8080/index.html`**
3. El navegador te solicitará inmediatamente **permisos para acceder a la cámara y al micrófono**. Concédelos.
4. Verás la interfaz oscura del ecosistema y tu propia imagen en el recuadro izquierdo **"Mi Cámara (Local)"**. En la cabecera se te asignará un nombre dinámico (ej: `User_40`).

### Paso 3: Conectar al Segundo Participante (Usuario B)
1. Abre una **nueva ventana en Modo Incógnito** (u otro navegador completamente diferente). Esto es obligatorio para que el servidor no comparta la misma sesión y le asigne un identificador de usuario único.
2. Accede a la misma dirección: **`http://localhost:8080/index.html`**
3. Otorga los permisos multimedia. Ahora tendrás dos ventanas independientes preparadas en la misma sala virtual (`sala1`).

### Paso 4: Probar el Chat de Texto Lateral
1. Escribe un mensaje en la caja de texto inferior derecha del *Usuario A* (ej: *"¡Hola! ¿Me escuchas?"*) y pulsa **Enviar** (o Enter).
2. Comprueba la ventana del *Usuario B*: el mensaje aparecerá reflejado al instante en su panel lateral indicando quién lo envía, demostrando la comunicación bidireccional por WebSocket.

### Paso 5: Iniciar la Videollamada P2P y Controles
1. En cualquiera de las dos ventanas, haz clic en el botón azul **"Iniciar Videollamada"**.
2. Los navegadores negociarán los codecs e IPs en segundo plano a través de Spring Boot. En un par de segundos, el cuadro **"Participante (Remoto)"** de ambas pantallas se activará mostrando el vídeo cruzado en tiempo real.
3. **Probando los Controles Multimedia:**
    *   **Silenciar Micro / Apagar Cámara:** Haz clic en los botones de control inferiores. Al pulsar *Apagar Cámara*, tu vídeo local se congelará y el participante remoto verá tu recuadro en negro de forma sincronizada, reduciendo a cero el envío de datos multimedia.
    *   **Colgar:** Al pulsar el botón rojo de *Colgar*, la conexión WebRTC y el flujo de hardware se detendrán de forma segura, notificando al otro navegador para que limpie su pantalla y liberando inmediatamente la memoria del servidor de Spring Boot.

---

## 🔄 Metodología de Desarrollo Ágil (Scrum)

El desarrollo del MVP se estructuró de manera rigurosa utilizando control de versiones Git (Conventional Commits) y marcos ágiles divididos en 3 Sprints integrados en el tablero de *GitHub Projects*:

*   **Sprint 1: Cimiento de Red e Intercambio de Texto:** Configuración del Message Broker, controladores REST/STOMP en Spring Boot y validación de la mensajería síncrona en red local.
*   **Sprint 2: Motor de Intercambio Técnico WebRTC:** Desarrollo del frontend nativo en JavaScript (`RTCPeerConnection`), captura e inyección del stream del dispositivo (`getUserMedia`) y enrutamiento del handshake técnico (`OFFER`/`ANSWER`).
*   **Sprint 3: Interfaz Jitsi Premium y Control de Tracks:** Integración del layout unificado (Panel lateral de Chat + Doble rejilla de vídeo), control de estados de hardware y destrucción limpia de conexiones multimedia.