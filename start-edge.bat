@echo off
title FabricHair Edge Node (Servidor Hibrido Offline)
echo ====================================================
echo INICIANDO FABRICHAIR EDGE SERVER (MODO OFFLINE)
echo ====================================================
echo.
echo Para essa bolha logistica funcionar, o Docker Desktop
echo precisa estar rodando neste computador.
echo.
echo Iniciando o Banco de Dados (Postgres) e o ERP (Spring)...
docker-compose up -d --build
echo.
echo ====================================================
echo O Sistema esta rodando isolado localmente! 
echo Acesse no navegador deste computador ou celulares na filial:
echo http://localhost:8080
echo.
echo Quando fechar esta janela, a bolha continua rodando.
echo Para desligar os servicos fisicamente, digite:
echo docker-compose down
echo ====================================================
pause
