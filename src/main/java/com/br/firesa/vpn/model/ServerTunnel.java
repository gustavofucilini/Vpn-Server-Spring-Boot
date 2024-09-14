package com.br.firesa.vpn.model;

import one.papachi.tuntap4j.TunDevice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.spec.SecretKeySpec;

import com.br.firesa.vpn.security.keygen.CryptoUtil;

public class ServerTunnel {

    private PrivateKey serverPrivateKey;
    private PublicKey userPublicKey;
    private SecretKeySpec aesKey;
    private TunDevice tunDevice;
    private ExecutorService threadPool;

    private Socket clientSocket; // Usaremos sockets padrão para comunicação

    public ServerTunnel(PrivateKey serverPrivateKey, PublicKey userPublicKey) {
        this.serverPrivateKey = serverPrivateKey;
        this.userPublicKey = userPublicKey;
        this.threadPool = Executors.newFixedThreadPool(2); // Para lidar com leitura/escrita
    }

    // Inicia o servidor VPN
    public void start() throws Exception {
        // Gera a chave AES compartilhada usando ECDH
        this.aesKey = CryptoUtil.generateSharedSecret(userPublicKey, serverPrivateKey);

        // Inicializa a interface TUN
        tunDevice = new TunDevice("tun0");
        tunDevice.open();
        tunDevice.setStatus(true);
        tunDevice.setIPAddress("10.8.0.1", "255.255.255.0"); // Configura o IP da interface

        System.out.println("Interface TUN iniciada como 'tun0'.");

        // Aguarda a conexão do cliente (implemente o socket do servidor em outro lugar)
        // Aqui, assumimos que o socket do cliente já está conectado
        if (clientSocket != null) {
            System.out.println("Cliente conectado.");
            handleTraffic();
        } else {
            System.out.println("Nenhum cliente se conectou.");
        }
    }

    // Define o socket do cliente (pode ser chamado pelo VPNService)
    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    // Método para lidar com o tráfego entre a interface TUN e o cliente
    public void handleTraffic() {
        // Inicia as threads para leitura e escrita
        threadPool.execute(() -> {
			try {
				readFromTun();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
        threadPool.execute(this::readFromClient);
    }

    // Lê pacotes da interface TUN e envia para o cliente
    private void readFromTun() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(tunDevice.getMTU());
        try (OutputStream out = clientSocket.getOutputStream()) {
            while (true) {
                buffer.clear();
                int bytesRead = tunDevice.read(buffer);
                if (bytesRead > 0) {
                    buffer.flip();
                    byte[] packet = new byte[bytesRead];
                    buffer.get(packet);

                    // Criptografa o pacote antes de enviar
                    byte[] encryptedPacket = CryptoUtil.encryptData(packet, aesKey);

                    // Envia o pacote criptografado para o cliente
                    out.write(encryptedPacket);
                    out.flush();
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao ler da interface TUN: " + e.getMessage());
        }
    }

    // Lê pacotes do cliente e escreve na interface TUN
    private void readFromClient() {
        try (InputStream in = clientSocket.getInputStream()) {
            byte[] buffer = new byte[2048]; // Tamanho adequado para pacotes IP
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                // Descriptografa o pacote recebido
                byte[] encryptedPacket = new byte[bytesRead];
                System.arraycopy(buffer, 0, encryptedPacket, 0, bytesRead);

                byte[] packet = CryptoUtil.decryptData(encryptedPacket, aesKey);

                // Escreve o pacote na interface TUN
                ByteBuffer packetBuffer = ByteBuffer.wrap(packet);
                tunDevice.write(packetBuffer);
            }
        } catch (Exception e) {
            System.err.println("Erro ao ler do cliente: " + e.getMessage());
        }
    }

    // Fecha conexões e recursos
    public void close() throws Exception {
        if (clientSocket != null && !clientSocket.isClosed()) {
            clientSocket.close();
        }
        if (tunDevice != null) {
            tunDevice.close();
        }
        threadPool.shutdownNow();
        System.out.println("Conexão VPN encerrada.");
    }
}
