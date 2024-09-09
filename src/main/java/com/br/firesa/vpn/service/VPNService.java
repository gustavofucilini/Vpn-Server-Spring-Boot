package com.br.firesa.vpn.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.br.firesa.vpn.entity.User;
import com.br.firesa.vpn.model.ServerTunnel;
import com.br.firesa.vpn.security.keygen.CryptoUtil;

@Service
public class VPNService {

    private ServerTunnel serverTunnel;
    private boolean isRunning;

    public VPNService() {
        this.isRunning = false;
    }

    // Método para iniciar a conexão VPN de forma assíncrona
    @Async
    public void startVpnConnection(User user) {
        if (isRunning) {
            System.out.println("VPN já está em execução.");
            return;
        }

        try {
            System.out.println("Iniciando o servidor VPN...");

            // Cria o túnel do servidor com as chaves criptográficas do usuário
            serverTunnel = new ServerTunnel(
                    CryptoUtil.convertBytesToPrivateKey(user.getServerPrivateKey()),
                    CryptoUtil.convertBytesToPublicKey(user.getUserPublicKey())
            );

            // Inicia o servidor VPN em uma porta especificada
            isRunning = true;
            int serverPort = 9090; // Porta do servidor VPN
            serverTunnel.start(serverPort);

            // Indica que a VPN está em execução

            // Executa a lógica de redirecionamento de tráfego em um thread separado
            new Thread(() -> {
                try {
                    serverTunnel.handleTraffic(); // Processa o tráfego de rede
                } catch (Exception e) {
                    e.printStackTrace();
                    stopVpnConnection(); // Para a VPN caso ocorra um erro
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Falha ao iniciar o servidor VPN.");
        }
    }

    // Método para parar a conexão VPN
    public void stopVpnConnection() {
        if (!isRunning) {
            System.out.println("VPN não está em execução.");
            return;
        }

        try {
            serverTunnel.close(); // Fecha as conexões do túnel VPN
            isRunning = false;
            System.out.println("Servidor VPN parado.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro ao parar o servidor VPN.");
        }
    }

    // Método opcional para verificar o status do servidor VPN
    public boolean isVpnRunning() {
        return isRunning;
    }
}