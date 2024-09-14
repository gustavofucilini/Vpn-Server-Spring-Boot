package com.br.firesa.vpn.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.br.firesa.vpn.entity.User;
import com.br.firesa.vpn.model.ServerTunnel;
import com.br.firesa.vpn.security.keygen.CryptoUtil;

import java.net.ServerSocket;
import java.net.Socket;

@Service
public class VPNService {

    private ServerTunnel serverTunnel;
    private boolean isRunning;

    public VPNService() {
        this.isRunning = false;
    }

    // Método para iniciar a VPN de forma assíncrona
    @Async
    public void startVpnConnection(User user) {
        if (isRunning) {
            System.out.println("VPN já está em execução.");
            return;
        }

        try {
            System.out.println("Iniciando o servidor VPN...");

            // Configura o túnel do servidor com as chaves do usuário
            serverTunnel = new ServerTunnel(
                CryptoUtil.convertBytesToPrivateKey(user.getServerPrivateKey()),
                CryptoUtil.convertBytesToPublicKey(user.getUserPublicKey())
            );

            isRunning = true;
            int serverPort = 9090; // Porta do servidor VPN

            // Inicia o socket do servidor para aceitar conexões de clientes
            try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
                System.out.println("Aguardando conexão do cliente na porta " + serverPort);

                // Aceita a conexão do cliente
                Socket clientSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clientSocket.getInetAddress());

                // Define o socket do cliente no ServerTunnel
                serverTunnel.setClientSocket(clientSocket);

                // Inicia o ServerTunnel
                serverTunnel.start();

            } catch (Exception e) {
                e.printStackTrace();
                stopVpnConnection();
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Falha ao iniciar o servidor VPN.");
        }
    }

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

    public boolean isVpnRunning() {
        return isRunning;
    }
}
