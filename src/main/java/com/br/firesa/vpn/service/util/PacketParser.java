package com.br.firesa.vpn.service.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class PacketParser {

	// Extrair o endereço IP de destino
    public static InetAddress getDestinationAddress(byte[] ipPacket) throws Exception {
        byte[] destinationIP = new byte[4];
        System.arraycopy(ipPacket, 16, destinationIP, 0, 4);
        return InetAddress.getByAddress(destinationIP);
    }

    // Extrair o protocolo (TCP, UDP, ICMP)
    public static int getProtocol(byte[] ipPacket) {
        return ipPacket[9] & 0xFF;
    }

    // Extrair a porta de destino (para TCP e UDP)
    public static int getDestinationPort(byte[] ipPacket) {
        // Cabeçalho IP é de 20 bytes, então a porta começa no byte 22
        int portOffset = 20;

        // Extrair os dois bytes da porta de destino (22 e 23)
        int destinationPort = ((ipPacket[portOffset + 2] & 0xFF) << 8) | (ipPacket[portOffset + 3] & 0xFF);
        return destinationPort;
    }

    // Encaminhamento de pacotes TCP
    public static byte[] forwardTcpPacketToInternet(byte[] packet) throws Exception {
        InetAddress destinationAddress = getDestinationAddress(packet);
        int destinationPort = getDestinationPort(packet); // Obter a porta de destino

        try (Socket socket = new Socket(destinationAddress, destinationPort);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {

            out.write(packet);
            out.flush();

            byte[] response = new byte[4096];
            int bytesRead = in.read(response);
            return Arrays.copyOf(response, bytesRead);
        }
    }


    // Encaminhamento de pacotes UDP
    public static byte[] forwardUdpPacketToInternet(byte[] packet) throws Exception {
        InetAddress destinationAddress = getDestinationAddress(packet);
        int destinationPort = getDestinationPort(packet); // Obter a porta de destino

        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket udpPacket = new DatagramPacket(packet, packet.length, destinationAddress, destinationPort);
            socket.send(udpPacket);

            byte[] response = new byte[4096];
            DatagramPacket responsePacket = new DatagramPacket(response, response.length);
            socket.receive(responsePacket);

            return responsePacket.getData();
        }
    }

    // Encaminhamento de pacotes ICMP (ping)
    public static byte[] forwardIcmpPacketToInternet(byte[] packet) throws Exception {
        InetAddress destinationAddress = getDestinationAddress(packet);

        try (DatagramSocket socket = new DatagramSocket()) {
            // Specify a port number (port 0 for ICMP)
            DatagramPacket icmpPacket = new DatagramPacket(packet, packet.length, destinationAddress, 0); 
            socket.send(icmpPacket);

            byte[] response = new byte[4096];
            DatagramPacket responsePacket = new DatagramPacket(response, response.length);
            socket.receive(responsePacket);

            return responsePacket.getData();
        }
    }
}
