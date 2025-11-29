#!/bin/bash

echo "Actualizando sistema e instalando dependencias"
sudo apt update -y
sudo apt install -y openjdk-21-jre-headless authbind

echo "Creando usuario y grupo de despliegue"
sudo groupadd deployers || echo "Grupo deployers ya existe."

if id "git_deploy" &>/dev/null; then
    echo "Usuario git_deploy ya existe."
else
    sudo useradd -m -s /bin/bash git_deploy
    echo "Creando directorio SSH para git_deploy..."
    sudo mkdir -p /home/git_deploy/.ssh
    sudo chown git_deploy:deployers /home/git_deploy/.ssh
    sudo chmod 700 /home/git_deploy/.ssh
fi

sudo usermod -aG deployers git_deploy

echo "Creando directorio de la aplicación y asignando permisos"
APP_DIR="/opt/apps/backend"
sudo mkdir -p $APP_DIR

sudo chown -R git_deploy:deployers $APP_DIR
sudo chmod -R 770 $APP_DIR

echo "Configurando authbind para permitir puerto 80"
PORT_CONFIG="/etc/authbind/byport/80"
sudo touch $PORT_CONFIG

sudo chown git_deploy:deployers $PORT_CONFIG
sudo chmod 770 $PORT_CONFIG

echo "Copiando y habilitando servicio Systemd"

echo "--------------------------------------"
echo "CONFIGURACIÓN DE SERVIDOR COMPLETA."
echo "Ahora puede copiar 'myapp.service' a /etc/systemd/system/ y ejecutar el CI/CD."
