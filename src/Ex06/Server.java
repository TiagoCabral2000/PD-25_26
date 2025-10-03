package Ex06;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Server {
    public static final int MAX_SIZE = 4000;

    public static void main(String[] args) {
        File localDirectory;
        String requestedFileName, requestedCanonicalFilePath = null;
        FileInputStream requestedFileInputStream = null;
        DatagramSocket socket = null;
        int listeningPort;
        DatagramPacket packet;
        byte[] fileChunk = new byte[MAX_SIZE];
        int nbytes;

        // -> 1o - Testar sintaxe:
        if (args.length != 2) {
            System.out.println("Sintaxe: java Servidor listeningPort localRootDirectory");
            return;
        }

        // -> 2o - Popular variavel localDirectory
        localDirectory = new File(args[1].trim());

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
        if (!localDirectory.canRead()) {
            System.out.println("Sem permissoes de leitura na directoria " + localDirectory + "!");
            return;
        }

        try {
            // -> 4o - Criar DatagramSocket para enviar/receber datagramas
            listeningPort = Integer.parseInt(args[0]);
            socket = new DatagramSocket(listeningPort);
            System.out.println("Servidor a escuta na porta " + listeningPort + "...");

            while (true) {
                // -> 5o - criar DatagramPacket para receber pedido do cliente
                packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);

                // -> 6o - receber datagrama com o pedido do cliente
                socket.receive(packet); //metodo bloqueante ate receber o datagrama

                // -> 7o - processar pedido do cliente (obter nome do ficheiro pedido)
                requestedFileName = new String(packet.getData(), 0, packet.getLength()).trim();
                System.out.println("Pedido do cliente: " + requestedFileName);

                // -> 8o - validar pedido do cliente (verificar se o ficheiro existe e se é legível)
                requestedCanonicalFilePath = localDirectory.getCanonicalPath() + File.separator + requestedFileName;
                File requestedFile = new File(requestedCanonicalFilePath);

                // -> 9o - verificar se temos acesso ao ficheiro que o cliente pediu
                if (!requestedCanonicalFilePath.startsWith(localDirectory.getCanonicalPath() + File.separator)) {
                    System.out.println("Nao e' permitido aceder ao ficheiro " + requestedCanonicalFilePath + "!");
                    System.out.println("A directoria de base nao corresponde a " + localDirectory.getCanonicalPath() + "!");
                    continue;
                }

                // -> 10o - abrir o ficheiro para leitura (input)
                requestedFileInputStream = new FileInputStream(requestedCanonicalFilePath);
                System.out.println("Ficheiro " + requestedCanonicalFilePath + " aberto para leitura.");

                do
                { //Este ciclo do...while() irá ser executado enquanto houverem bytes para serem lidos do ficheiro, ou seja, enquanto não tivermos lido todo o ficheiro
                    // -> 11o - ler do ficheiro um bloco de dados
                    nbytes = requestedFileInputStream.read(fileChunk);

                    if (nbytes == -1) //ao tentar ler um chunk que já não existe, o nbytes será -1 logo estamos a meter a zero para parar o ciclo e enviar ao cliente a dizer que terminámos de enviar o ficheiro
                        nbytes = 0;

                    // -> 12o - atualizar o datagram com os dados que queremos enviar
                    packet.setData(fileChunk, 0, nbytes);
                    packet.setLength(nbytes);

                    // -> 13o - enviar o datagrama com o bloco de dados lido do ficheiro
                    socket.send(packet);
                } while (nbytes > 0);

                System.out.println("Transferencia concluida");

                // -> 14o - fechar o ficheiro que estávamos a ler
                requestedFileInputStream.close();
                requestedFileInputStream = null;
            }
        }catch(NumberFormatException e){
            System.out.println("O porto de escuta deve ser um inteiro positivo:\n\t" + e);
        } catch(SocketException e){
            System.out.println("Ocorreu uma excepcao ao nivel do socket UDP:\n\t" + e);
        } catch(FileNotFoundException e){   //Subclasse de IOException
            System.out.println("Ocorreu a excepcao {" + e + "} ao tentar abrir o ficheiro " + requestedCanonicalFilePath + "!");
        } catch(IOException e){
            System.out.println("Ocorreu a excepcao de E/S: \n\t" + e);
        } finally{
            if (socket != null) {
                socket.close();
            }
            if (requestedFileInputStream != null) {
                try {
                    requestedFileInputStream.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}

