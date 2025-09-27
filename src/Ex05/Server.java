package Ex05;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Calendar;

public class Server {

    public static final int MAX_SIZE = 256;
    public static final String TIME_REQUEST = "TIME";

    public static void main(String[] args) {
        int listeningPort;
        DatagramSocket socket = null;
        DatagramPacket packet;
        String receivedMsg, timeMsg;
        Calendar calendar;

        // -> 1o - Testar sintaxe:
        if (args.length != 1) {
            System.out.println("Sintaxe: java Servidor listeningPort");
            return;
        }

        try{
            // -> 2o - Criar DatagramSocket para enviar/receber datagramas
            // Aqui especificamos o porto que queremos usar
            // Ao contrário do cliente, no servidor queremos dizer que porto queremos usar especificamente
            // Se fosse automático, depois o cliente não saberia para que porto teria que enviar os datagramas
            listeningPort = Integer.parseInt(args[0]);
            socket = new DatagramSocket(listeningPort);

            System.out.println("UDP Time Server iniciado...");

            while (true) {
                // -> 3o - Esperar por datagramas usando o metodo receive() do socket
                // O metodo receive() é bloqueante, ou seja, o código ficará parado nesta linha até receber um datagrama
                // Não especificamos um tempo de timeout pois a ideia do servidor é ficar à espera infinitamente por mensagems
                //
                // NOTA2: reparem que aqui no servidor, ao contrário do que acontece no cliente, não estamos a especificar o
                //        IP e porto quando criamos o DatagramPacket. Isto porque, essa informação estará preenchida no
                //        packet quando este for recebido.
                //        Ou seja, quando recebermos o pacote este terá encapsulado o endereço IP e porto de onde veio a mensagem
                packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
                socket.receive(packet); //metodo bloqueante ate receber o datagrama

                // -> 4o - Processar pedido do cliente (se for pedido de hora atual, obter hora atual)
                receivedMsg = new String(packet.getData(), 0, packet.getLength());

                if (receivedMsg.equals(TIME_REQUEST)) {
                    System.out.println("Pedido de hora recebido...");
                    calendar = Calendar.getInstance();
                    timeMsg = String.format("%02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
                } else {
                    timeMsg = "INVALID REQUEST";
                }

                // -> 5o - Enviar resposta ao cliente com a hora atual
                packet = new DatagramPacket(timeMsg.getBytes(), timeMsg.length(),
                        packet.getAddress(), packet.getPort());
                socket.send(packet);
            }
        } catch (NumberFormatException e) {
            System.out.println("O porto de escuta deve ser um inteiro positivo.");
        } catch (SocketException e) {
            System.out.println("Ocorreu um erro ao nivel do socket UDP:\n\t" + e);
        } catch (IOException e) {
            System.out.println("Ocorreu um erro no acesso ao socket:\n\t" + e);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}