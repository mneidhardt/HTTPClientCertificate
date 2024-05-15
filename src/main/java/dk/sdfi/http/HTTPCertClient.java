package dk.sdfi.http;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Kald til en SOAP service, i dette tilfælde Datafordelerens kvitteringsservice.
 * Jeg danner først en SSLContext med en Keymanager (med vores client certifikat)
 * og en Trustmanager (med certifikater for servere).
 * 
 * 
 */
public class HTTPCertClient {
	public java.net.http.HttpResponse<String> soapkald(String keystore,
														String keystorepwd,
														String URL,
														String soapaction,
														String soapenvelope,
														String certfile,
														String certpwd) throws NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException, KeyStoreException, UnrecoverableKeyException, KeyManagementException, InterruptedException {
		
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        clientStore.load(new FileInputStream(keystore), keystorepwd.toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientStore, keystorepwd.toCharArray());
        KeyManager[] kms = kmf.getKeyManagers();

        // Jeg bruger den medfølgende lib/security/cacerts. 
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(certfile), certpwd.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        TrustManager[] tms = tmf.getTrustManagers();

        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(kms, tms, new SecureRandom());
        
        // Nu er der dannet en SSLContext der skal bruges med nedenstående HttpClient.
        
        HttpClient.Builder builder = HttpClient.newBuilder().sslContext(sslContext);
        HttpClient client = builder.build();
        
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(soapenvelope))
                .uri(URI.create(URL))
                .setHeader("SOAPAction", soapaction)
                .setHeader("Content-Type", "text/xml")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return response;
	}
}