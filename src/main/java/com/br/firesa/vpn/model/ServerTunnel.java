package com.br.firesa.vpn.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.spec.SecretKeySpec;

import com.br.firesa.vpn.security.keygen.CryptoUtil;

import one.papachi.tuntap4j.NetworkDevice;
import one.papachi.tuntap4j.TapDevice;

public class ServerTunnel {

	private PrivateKey serverPrivateKey;
	private PublicKey clientPublicKey;
	private SecretKeySpec aesKey;
	private NetworkDevice networkDevice; // Pode ser TapDevice ou TunDevice
	private ExecutorService threadPool;

	private Socket clientSocket; // Usaremos sockets padrão para comunicação

	public ServerTunnel(PrivateKey serverPrivateKey, PublicKey userPublicKey) throws Exception {
		this.serverPrivateKey = serverPrivateKey;
		this.clientPublicKey = userPublicKey;
		this.threadPool = Executors.newFixedThreadPool(2); // Para lidar com leitura/escrita
		this.aesKey = CryptoUtil.generateSharedSecret(this.clientPublicKey, this.serverPrivateKey);
	}

	// Inicia o servidor VPN
	public void start() throws Exception {
		// Gera a chave AES compartilhada usando ECDH
		System.out.println("AESKEY DO SERVIDOR: " + Base64.getEncoder().encodeToString(this.aesKey.getEncoded()));

		String deviceName = "tap0"; // Você pode alterar o nome conforme necessário
	    networkDevice = new TapDevice(deviceName);
	    try {
	        networkDevice.open();
	        networkDevice.setStatus(true);
	        networkDevice.setIPAddress("10.8.0.1", "255.255.255.0"); // Configura o IP da interface
	        System.out.println("Interface TAP iniciada como '" + deviceName + "'.");
	    } catch (IOException e) {
	        System.err.println("Erro ao configurar o dispositivo TAP: " + e.getMessage());
	        e.printStackTrace();
	        throw e;
	    }

		System.out.println("Interface TAP iniciada como '" + deviceName + "'.");

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
				readFromNetworkDevice();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		threadPool.execute(this::readFromClient);
	}

	// Lê pacotes da interface TAP e envia para o cliente
    private void readFromNetworkDevice() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(networkDevice.getMTU() + 14); // +14 para cabeçalho Ethernet
        try (OutputStream out = clientSocket.getOutputStream()) {
            while (true) {
                buffer.clear();
                int bytesRead = networkDevice.read(buffer);
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
            System.err.println("Erro ao ler da interface TAP: " + e.getMessage());
        }
    }

    // Lê pacotes do cliente e escreve na interface TAP
    private void readFromClient() {
        try (InputStream in = clientSocket.getInputStream()) {
            byte[] buffer = new byte[2048]; // Tamanho adequado para quadros Ethernet
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                // Descriptografa o pacote recebido
                byte[] encryptedPacket = new byte[bytesRead];
                System.arraycopy(buffer, 0, encryptedPacket, 0, bytesRead);

                byte[] packet = CryptoUtil.decryptData(encryptedPacket, aesKey);
                // Escreve o pacote na interface TAP
                ByteBuffer packetBuffer = ByteBuffer.wrap(packet);
                networkDevice.write(packetBuffer);
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
        if (networkDevice != null) {
            networkDevice.close();
        }
        threadPool.shutdownNow();
        System.out.println("Conexão VPN encerrada.");
    }
}
