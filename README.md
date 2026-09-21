# Laboratorio 8 — ExpresoFast Parte IV: Consola de Operación Logística

**Curso:** IF0009 - Desarrollo de Software IV
**Ciclo:** II-2026
**Estudiante:** Daniela Tames Vega
**Carné:** c5k177


## Requisitos de Entorno

- Java 21
- Maven (incluido como wrapper mvnw)
- Microsoft SQL Server 
- Navegador web moderno 
- Servidor local para el front-end (Live Server)


## Instrucciones de Ejecución

### Backend

1. Desde la carpeta backend/, ejecutá: .\mvnw.cmd spring-boot:run
2. El backend queda disponible en `http://localhost:8080`.

### Frontend

1. Abrí la carpeta frontend/ con la extensión Live Server de VS Code
2. Iniciá sesión con cualquiera de los usuarios de prueba.

## Usuarios de Prueba

| Usuario | Contraseña |
|---|---|---|
| admin | admin123 | 
| operador1 | oper123 | 
| conductor1 | cond123 |

## Funcionalidad por Rol

- ADMIN: ve la bitácora de auditorí, el indicador de vehículos activos,y el botón "Ver Bitácora" en cada envío.
- OPERADOR: ve el tablero de envíos y sus acciones de estado, sin acceso a la bitácora.
- CONDUCTOR: ve únicamente sus envíos y el botón para marcar como entregado.

