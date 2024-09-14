package com.br.firesa.vpn.service.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class PacketParser {

    // Extrai o endereço IP de destino
    public static InetAddress getDestinationAddress(byte[] ipPacket) throws Exception {
        byte[] destinationIP = new byte[4];
        System.arraycopy(ipPacket, 16, destinationIP, 0, 4);
        return InetAddress.getByAddress(destinationIP);
    }

    // Extrai o protocolo (TCP, UDP, ICMP)
    public static int getProtocol(byte[] ipPacket) {
        return ipPacket[9] & 0xFF;
    }

    // Extrai o cabeçalho IP (tamanho variável devido a opções)
    public static int getIpHeaderLength(byte[] ipPacket) {
        return (ipPacket[0] & 0x0F) * 4;
    }

    // Encaminha o pacote IP para a Internet
    public static void forwardIpPacketToInternet(byte[] packet) throws Exception {
        InetAddress destinationAddress = getDestinationAddress(packet);
        int protocol = getProtocol(packet);

        switch (protocol) {
            case 6: // TCP
                forwardTcpPacket(packet, destinationAddress);
                break;
            case 17: // UDP
                forwardUdpPacket(packet, destinationAddress);
                break;
            case 1: // ICMP
                forwardIcmpPacket(packet, destinationAddress);
                break;
            default:
                System.err.println("Protocolo não suportado: " + protocol);
                break;
        }
    }

    private static void forwardTcpPacket(byte[] packet, InetAddress destinationAddress) throws Exception {
        int ipHeaderLength = getIpHeaderLength(packet);
        int tcpHeaderOffset = ipHeaderLength;

        // Obtém a porta de destino
        int destinationPort = ((packet[tcpHeaderOffset + 2] & 0xFF) << 8) | (packet[tcpHeaderOffset + 3] & 0xFF);

        try (Socket socket = new Socket(destinationAddress, destinationPort)) {
            socket.getOutputStream().write(packet, tcpHeaderOffset, packet.length - tcpHeaderOffset);
            socket.getOutputStream().flush();

            // Você pode ler a resposta se necessário
        }
    }

    private static void forwardUdpPacket(byte[] packet, InetAddress destinationAddress) throws Exception {
        int ipHeaderLength = getIpHeaderLength(packet);
        int udpHeaderOffset = ipHeaderLength;

        // Obtém a porta de destino
        int destinationPort = ((packet[udpHeaderOffset + 2] & 0xFF) << 8) | (packet[udpHeaderOffset + 3] & 0xFF);

        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket udpPacket = new DatagramPacket(packet, udpHeaderOffset, packet.length - udpHeaderOffset, destinationAddress, destinationPort);
            socket.send(udpPacket);

            // Você pode receber a resposta se necessário
        }
    }

    private static void forwardIcmpPacket(byte[] packet, InetAddress destinationAddress) throws Exception {
        try (DatagramSocket socket = new DatagramSocket()) {
            DatagramPacket icmpPacket = new DatagramPacket(packet, packet.length, destinationAddress, 0);
            socket.send(icmpPacket);

            // Você pode receber a resposta se necessário
        }
    }
}
