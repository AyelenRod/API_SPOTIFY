#!/bin/bash
echo "Ejecutando pre-deploy: Deteniendo servicio y limpiando artefacto antiguo"

# Detener el servicio si está corriendo
sudo systemctl stop myapp.service || true

# Eliminar el Fat JAR anterior
JAR_PATH="/opt/apps/backend/mi-api2.jar"
if [ -f "$JAR_PATH" ]; then
    echo "Eliminando $JAR_PATH antiguo..."
    sudo rm -f "$JAR_PATH"
else
    echo "El artefacto antiguo no existe en $JAR_PATH. Continuando..."
fi

echo "Pre-deploy finalizado."