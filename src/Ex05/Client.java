package Ex05;

// Objetivo: Obter hora atual através da comunicação com o Servidor pelo protocolo UDP
// Localização do servidor recebida através da linha de comandos (Ex: java Client serverAddress serverUdpPort)

import java.io.IOException;
import java.net.*;

public class Client {
    public static final String TIME_REQUEST = "TIME";
    public static final int MAX_SIZE = 256;
    public static final int TIMEOUT = 10;

    public static void main(String[] args) {

        InetAddress serverAddr = null;
        int serverPort = -1;
        DatagramSocket socket = null;
        DatagramPacket packet = null;
        String response;

        // -> 1o - Testar sintaxe:
        if (args.length != 2) {
            System.out.println("Sintaxe: java Client serverAddress serverUdpPort");
            return;
        }

        try {
            // -> 2o - Preencher variaveis do IP e Porto do Servidor

            //a função getByName da classe InetAddress permite obter o endereço IP do servidor
            //quer este tenha sido passado como uma string no formato "192.168.1.70" como "servidorxxx.com"
            serverAddr = InetAddress.getByName(args[0]);
            serverPort = Integer.parseInt(args[1]);

            // -> 3o - criar DatagramSocket para enviar/receber datagramas
            // Ao não definir nenhum porto, o sistema atribui-nos um automaticamente
            socket = new DatagramSocket();

            // -> 4o - definir tempo máximo de espera por resposta do servidor
            // Isto faz com que ao usar o metodo receive(), este fique bloqueado à espera de resposta por X tempo
            socket.setSoTimeout(TIMEOUT * 1000);

            // -> 5o - criar DatagramPacket com a mensagem a enviar ao servidor
            // Neste caso a mensagem que queremos enviar é a constante TIME_REQUEST, "TIME"
            packet = new DatagramPacket(TIME_REQUEST.getBytes(), TIME_REQUEST.length(), serverAddr, serverPort);

            // -> 6o - enviar datagrama com o pedido de hora atual
            socket.send(packet);

            // -> 7o - criar DatagramPacket para receber a resposta do servidor
            // Porque precisamos de criar um novo DatagramPacket?
            // Porque o packet que usamos para enviar a mensagem ao servidor tinha a mensagem "TIME",
            // e por isso especificamos o tamanho da mensagem como sendo o tamanho dessa string (4).
            // Agora, para receber a resposta do servidor, precisamos de um DatagramPacket com um buffer
            // suficientemente grande para receber a resposta do servidor (neste caso 256)
            packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);

            // -> 8o - receber datagrama com a resposta do servidor
            socket.receive(packet); //metodo bloqueante ate receber o datagrama ou ate expirar o timeout

            // -> 9o - extrair a mensagem do datagrama recebido
            response = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Hora indicada pelo servidor: " + response);


        } catch (UnknownHostException e) {
            System.out.println("Destino desconhecido:\n\t" + e);
        } catch (NumberFormatException e) {
            System.out.println("O porto do servidor deve ser um inteiro positivo.");
        } catch (SocketTimeoutException e) {
            System.out.println("Nao foi recebida qualquer resposta:\n\t" + e);
        } catch (SocketException e) {
            System.out.println("Ocorreu um erro ao nivel do socket UDP:\n\t" + e);
        } catch (IOException e) {
            System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
        } finally {
            if (socket != null)
                socket.close();
        }


    }
}
