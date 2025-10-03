package Ex06;

// Objetivo: Obter ficheiro através da comunicação com o Servidor pelo protocolo UDP
// Localização do servidor recebida através da linha de comandos (Ex: java Client serverAddress serverUdpPort)

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

public class Client {
    public static final int MAX_SIZE = 4000; //Enunciado: defina um tamanho máximo para os blocos transferidos (por exemplo,MAX_DATA = 4000 bytes)
    public static final int TIMEOUT = 5; //segundos

    public static void main(String[] args) {
        File localDirectory;
        String fileName, localFilePath = null;
        InetAddress serverAddr;
        int serverPort;
        DatagramSocket socket = null;
        DatagramPacket packet;
        FileOutputStream localFileOutputStream = null;
        int contador = 0;


        // -> 1o - Testar sintaxe:
        if (args.length != 4) {
            System.out.println("Sintaxe: java Client serverAddress serverUdpPort fileToGet localDirectory");
            return;
        }

        // -> 2o - Popular variaveis fileName e localDirectory
        fileName = args[2].trim();
        localDirectory = new File(args[3].trim());

        // -> 3o - Validar directoria:
            // 1º Se a directoria existe
            // 2º se é uma directoria
            // 3º se temos permissões para escrita

        if (!localDirectory.exists()) {
            System.out.println("A directoria " + localDirectory + " nao existe!");
            return;
        }
        if (!localDirectory.isDirectory()) {
            System.out.println("O caminho " + localDirectory + " nao se refere a uma directoria!");
            return;
        }
        if (!localDirectory.canWrite()) {
            System.out.println("Sem permissoes de escrita na directoria " + localDirectory);
            return;
        }

        try {
            try {
                // -> 4o - Criar objeto que irá ser usado para escrever (output) o ficheiro que vamos receber do servidor. Apenas o vamos abrir, ainda não escrevemos nada nele

                localFilePath = localDirectory.getCanonicalPath() + File.separator + fileName;
                localFileOutputStream = new FileOutputStream(localFilePath);

                System.out.println("Ficheiro " + localFilePath + " criado.");
            } catch (IOException e) {
                if (localFilePath == null) {
                    System.out.println("Ocorreu a excepcao {" + e + "} ao obter o caminho canonico para o ficheiro local!");
                } else {
                    System.out.println("Ocorreu a excepcao {" + e + "} ao tentar criar o ficheiro " + localFilePath + "!");
                }
                return;
            }

            try {
                // -> 5o - Preencher variaveis do IP e Porto do Servidor
                serverAddr = InetAddress.getByName(args[0]);
                serverPort = Integer.parseInt(args[1]);

                // -> 6o - Criar DatagramSocket para enviar/receber datagramas
                socket = new DatagramSocket();

                // -> 7o - Definir tempo máximo de espera por resposta do servidor
                socket.setSoTimeout(TIMEOUT * 1000);

                // -> 8o - Criar DatagramPacket com a mensagem a enviar ao servidor
                packet = new DatagramPacket(fileName.getBytes(), fileName.length(), serverAddr, serverPort);

                // -> 9o - Enviar datagrama com o pedido do ficheiro
                socket.send(packet);

                do{ // ciclo para receber os blocos do ficheiro
                    // -> 10o - Criar DatagramPacket para receber a resposta do servidor
                    packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);

                    // -> 11o - Receber datagrama com a resposta do servidor
                    socket.receive(packet); //metodo bloqueante ate receber o datagrama ou ate expirar o timeout

                    // -> 12o - Extrair a mensagem do datagrama recebido
                    if (packet.getPort() == serverPort && packet.getAddress().equals(serverAddr)) { // validar que o datagrama recebido é do servidor que pretendemos
                        localFileOutputStream.write(packet.getData(), 0, packet.getLength()); // escrever os dados recebidos para dentro no nosso ficheiro
                        contador++;
                    }
                }while(packet.getLength() > 0);

                System.out.println("Transferencia concluida (numero de blocos: " + contador + ")");

            } catch (UnknownHostException e) {
                System.out.println("Destino desconhecido:\n\t" + e);
            } catch (NumberFormatException e) {
                System.out.println("O porto do servidor deve ser um inteiro positivo:\n\t" + e);
            } catch (SocketTimeoutException e) {
                System.out.println("Nao foi recebida qualquer bloco adicional, podendo a transferencia estar incompleta:\n\t" + e);
            } catch (SocketException e) {
                System.out.println("Ocorreu um erro ao nivel do socket UDP:\n\t" + e);
            } catch (IOException e) {
                System.out.println("Ocorreu um erro no acesso ao socket ou ao ficheiro local " + localFilePath + ":\n\t" + e);
            }
        }
        finally { //liberar recursos (socket e output stream) - desnecessario com try-with-resouces
            if (socket != null) {
                socket.close();
            }
            if (localFileOutputStream != null) {
                try {
                    localFileOutputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }
}
