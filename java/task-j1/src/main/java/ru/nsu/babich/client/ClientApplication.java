package ru.nsu.babich.client;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ClientApplication {

    private static final String USAGE =
            "Usage: client <name> <host> <port> [--delay <seconds>] [--abort]\n"
            + "  --delay <seconds>  wait between sending the request and reading the response\n"
            + "  --abort            exit instead of reading the response";

    private static final byte TERMINATOR = 0x00;
    private static final int MAX_FIELD_SIZE = 1024 * 1024;

    public static void main(String[] args) {
        if (args.length < 3) {
            exitWithUsage();
        }

        String name = args[0];
        String host = args[1];
        int port = Integer.parseInt(args[2]);
        long delaySeconds = 0;
        boolean abort = false;

        for (int i = 3; i < args.length; i++) {
            switch (args[i]) {
                case "--delay" -> {
                    if (++i >= args.length) {
                        exitWithUsage();
                    }
                    delaySeconds = Long.parseLong(args[i]);
                }
                case "--abort" -> abort = true;
                default -> exitWithUsage();
            }
        }

        try {
            run(name, host, port, delaySeconds, abort);
        } catch (EOFException e) {
            System.err.println("Server closed the connection before sending the keys");
            System.exit(1);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(String name, String host, int port, long delaySeconds, boolean abort)
            throws IOException, InterruptedException {
        try (Socket socket = new Socket(host, port)) {
            OutputStream out = socket.getOutputStream();
            out.write(name.getBytes(StandardCharsets.US_ASCII));
            out.write(TERMINATOR);
            out.flush();

            if (delaySeconds > 0) {
                Thread.sleep(delaySeconds * 1000);
            }

            if (abort) {
                Runtime.getRuntime().halt(2);
            }

            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] privateKey = readField(in);
            readField(in);
            byte[] certificate = readField(in);

            Path keyFile = Path.of(name + ".key");
            Path certFile = Path.of(name + ".crt");
            Files.writeString(keyFile, toPem("PRIVATE KEY", privateKey));
            Files.writeString(certFile, toPem("CERTIFICATE", certificate));

            System.out.println("Saved " + keyFile + " and " + certFile);
        }
    }

    private static byte[] readField(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length < 0 || length > MAX_FIELD_SIZE) {
            throw new IOException("Malformed response: field length " + length);
        }
        byte[] data = new byte[length];
        in.readFully(data);
        return data;
    }

    private static String toPem(String type, byte[] der) {
        String body = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(der);
        return "-----BEGIN " + type + "-----\n" + body + "\n-----END " + type + "-----\n";
    }

    private static void exitWithUsage() {
        System.err.println(USAGE);
        System.exit(1);
    }
}
