package com.br.firesa.vpn.model;

import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.CompletableFuture;

import javax.crypto.spec.SecretKeySpec;

import com.br.firesa.vpn.security.keygen.CryptoUtil;
import com.br.firesa.vpn.service.util.PacketParser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServerTunnel {

	private PrivateKey serverPrivateKey;
	private PublicKey userPublicKey;
	private SecretKeySpec aesKey;

	private ServerSocket serverSocket;
	private Socket clientSocket;

	public ServerTunnel(PrivateKey serverPrivateKey, PublicKey userPublicKey) {
		this.serverPrivateKey = serverPrivateKey;
		this.userPublicKey = userPublicKey;
	}

	public void start(int port) throws Exception {
		this.aesKey = CryptoUtil.generateSharedSecret(userPublicKey, serverPrivateKey);
		serverSocket = new ServerSocket(port);
		System.out.println("Servidor VPN iniciado na porta " + port);

		clientSocket = serverSocket.accept();
		System.out.println("Cliente Conectado");
	}

	// Método principal para gerenciar o tráfego de rede
    public void handleTraffic() throws Exception {
        while (true) {
            byte[] encryptedPacket = receiveData();
            if (encryptedPacket != null) {
                // Descriptografar o pacote recebido
                byte[] packet = CryptoUtil.decryptData(encryptedPacket, aesKey);

                // Redirecionar o pacote para o destino correto (com a porta correta)
                byte[] responsePacket;
                int protocol = PacketParser.getProtocol(packet);

                if (protocol == 6) { // TCP
                    responsePacket = PacketParser.forwardTcpPacketToInternet(packet);
                } else if (protocol == 17) { // UDP
                    responsePacket = PacketParser.forwardUdpPacketToInternet(packet);
                } else {
                    System.out.println("Protocolo não suportado: " + protocol);
                    continue;
                }

                // Enviar a resposta criptografada de volta para o cliente
                sendData(responsePacket);
            }
        }
    }
    
 // Método principal para gerenciar o tráfego de rede de forma assíncrona
    public CompletableFuture<Void> handleTrafficAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                while (true) {
                    byte[] encryptedPacket = receiveData();
                    if (encryptedPacket != null) {
                        byte[] packet = CryptoUtil.decryptData(encryptedPacket, aesKey);
                        byte[] responsePacket;
                        int protocol = PacketParser.getProtocol(packet);

                        if (protocol == 6) { // TCP
                            responsePacket = PacketParser.forwardTcpPacketToInternet(packet);
                        } else if (protocol == 17) { // UDP
                            responsePacket = PacketParser.forwardUdpPacketToInternet(packet);
                        } else {
                            System.out.println("Protocolo não suportado: " + protocol);
                            continue;
                        }

                        sendData(responsePacket);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

	public void sendData(byte[] data) throws Exception {
		byte[] encriptedData = CryptoUtil.encryptData(data, aesKey);
		clientSocket.getOutputStream().write(encriptedData);
		clientSocket.getOutputStream().flush();
	}

	public byte[] receiveData() throws Exception {
		byte[] buffer = new byte[4096];
		int bytesRead = clientSocket.getInputStream().read(buffer);

		if (bytesRead != -1) {
			byte[] encryptedDataWithIv = new byte[bytesRead];
			System.arraycopy(buffer, 0, encryptedDataWithIv, 0, bytesRead);

			return CryptoUtil.decryptData(encryptedDataWithIv, aesKey);
		}

		return null;
	}

	public void close() throws Exception {
        clientSocket.close();
        serverSocket.close();
        System.out.println("Conexão encerrada.");
    }

}
