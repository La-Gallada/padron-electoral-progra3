# Padron Electoral Java

Sistema de consulta del padrón electoral desarrollado en Java con arquitectura por capas.

## Características
- Consulta de personas por cédula desde PADRON.txt
- Enriquecimiento de dirección usando distelec.txt
- Servidor TCP (protocolo GET|cedula|JSON/XML)
- Servidor HTTP (/padron)
- Respuestas en JSON y XML

## Arquitectura
- entidades
- dto
- datos
- logica
- presentacion.tcp
- presentacion.http
- util
