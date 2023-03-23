package megex.app.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


/**
 * Socket and ServerSocket factory for TLS-enabled sockets
 * with optionally-specified application protocol(s).
 */
public class TLSFactory {
    // Specify TLS version to use
    private static final String TLSVERSION = "TLSv1.2";
    // Application protocol list containing H2
    private static final String[] H2APPPROTO = new String[]{"h2"};

    /**
     * Gets a TLS client socket
     * @param server       identity of host to connect to
     * @param port         port of host to connect to
     * @param appProtocols list of application protocols (null if none)
     * @return connected/initialized socket
     * @throws Exception if connection/initialization fails
     */
    public static Socket getClientSocket(final String server, final int port, final
                                        String[] appProtocols) throws Exception {

        // Set VERY trusting trust manager.  NOT SECURE!!!!
        final SSLContext ctx = SSLContext.getInstance(TLSVERSION);
        ctx.init(null, new TrustManager[]{(TrustManager) new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
                                           String authType)
                    throws CertificateException {
            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
                                           String authType)
                    throws CertificateException {
            }
        }
        }        , null);
        // Create SSL socket factory and generate new, connected socket
        final SSLSocketFactory ssf = ctx.getSocketFactory();
        final SSLSocket s = (SSLSocket) ssf.createSocket(server, port);
        // Add any application protocols
        if (appProtocols != null) {
            final SSLParameters p = s.getSSLParameters();
            p.setApplicationProtocols(appProtocols);
            s.setSSLParameters(p);
        }
        // Execute TLS connection
        s.startHandshake();
        return s;
    }
    /**
     * Gets a TLS client socket for HTTP2
     *
     * @param server         identity of host to connect to
     * @param port         port of host to connect to

     *
     * @return connected/initialized socket
     *
     * @throws Exception if connection/initialization fails
     */
    public static Socket getClientSocket(final String server, final int port) throws
            Exception {
        return getClientSocket(server, port, H2APPPROTO);
    }

    /**
     * Create initialized listening socket
     *
     * @param port             port to listen on
     * @param keystorefile     name of key store file
     * @param keystorepassword password for key store file
     *
     * @return initialized server socket
     *
     * @throws Exception if unable to create socket
     */
    public static ServerSocket getServerListeningSocket(final int port, final String
            keystorefile,
                                                        final String keystorepassword) throws Exception {
        // Set the keystore and its password
        System.setProperty("javax.net.ssl.keyStorePassword", keystorepassword);
        System.setProperty("javax.net.ssl.keyStore", keystorefile);
        // Create a server-side SSLSocket
        SSLServerSocketFactory factory = (SSLServerSocketFactory)
                SSLServerSocketFactory.getDefault();
        SSLServerSocket servSocket = (SSLServerSocket)
                factory.createServerSocket(port);
        servSocket.setEnabledProtocols(new String[] { TLSVERSION });
        return servSocket;
    }
    /**
     * Block until connection then return new, connected socket
     *
     * @param servSocket   socket waiting on connections
     * @param appProtocols list of application protocols (null if none)
     *
     * @return connected socket
     *
     * @throws IOException if problem handling new connection
     */
    public static Socket getServerConnectedSocket(final ServerSocket servSocket,
                                                  final String[] appProtocols)
            throws IOException {
        SSLSocket socket = (SSLSocket) servSocket.accept();
        if (appProtocols != null) {
            // Get an SSLParameters object from the SSLSocket
            SSLParameters sslp = socket.getSSLParameters();
            // Populate SSLParameters with application protocols
            // As this is server side, put them in order of preference
            sslp.setApplicationProtocols(appProtocols);
            // Populate the SSLSocket object with application protocols
            socket.setSSLParameters(sslp);
        }
        // Make TLS handshake
        socket.startHandshake();
        return socket;
    }

    /**
     * Block until connection then return new, connected socket
     *
     * @param servSocket   socket waiting on connections

     *
     * @return connected socket
     *
     * @throws IOException if problem handling new connection
     */
    public static Socket getServerConnectedSocket(final ServerSocket servSocket)
            throws IOException {
        return getServerConnectedSocket(servSocket, H2APPPROTO);
    }
}

