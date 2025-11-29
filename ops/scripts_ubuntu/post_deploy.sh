#!/bin/bash

echo "Ejecutando post-deploy: Recargando Systemd, Iniciando y Verificando"

# Recargar la configuración de systemd
sudo systemctl daemon-reload

# Iniciar el servicio myapp
echo "Iniciando servicio myapp.service..."
sudo systemctl start myapp.service

# Verificar el estado
echo "Verificando estado del servicio..."
STATUS=$(systemctl is-active myapp.service)

if [ "$STATUS" == "active" ]; then
    echo "Éxito: myapp.service está activo y corriendo."
else
    echo "ERROR: myapp.service falló al iniciar. Estado: $STATUS"
    sudo journalctl -xeu myapp.service
    exit 1
fi

echo "Post-deploy finalizado."