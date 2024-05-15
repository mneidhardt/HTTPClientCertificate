package dk.sdfi.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Properties;

/**
 * Demonstrate connecting to a server secured with client-side SSL certificates.
 */
public class App {

	/* Forventer disse argumenter:
	 * 		Propertiesfilnavn,
	 * 		keystorefilnavn,
	 * 		keystorepassword,
	 * 		URL,
	 * 		soapaction,
	 * 		soapenvelope
	 * 
	 * Fx. java -jar <JARNAME> properties.txt blabla.p12 pasord https://hoohaa.dk soapaction soapenv
	 * 
	 * NB: Java version er minimum 11.
	 * 
	 *		Propertiesfilnavn	Fil med disse properties:successcode, certfilnavn, certfilpassword.
				CERTFILE			Fil med CA certifikater. Jeg bruger Javas default, lib/security/cacerts
				CERTPASSWORD		Password til CERTFILE. Default er changeit.
				SUCCESSCODE			Den HTTP status code fra servicen som betyder success, fx 200.

		    KEYSTOREFILNAVN		Fil med client certificate, i P12-format.
			KEYSTOREPASSWORD	Password til KEYSTOREFILEN.
			URL					URL til servicen. Den er https://<MILJØ>certupdate.datafordeler.dk/Receipts/DeliveryReceiptService.svc
								hvor <MILJØ> er TEST03-, TEST06- for hhv TEST03 og TEST06, og ingen ting for PROD.
			SOAPACTION			urn:gst:datafordeler:wsdl:1.0.0:#DeliveryReceiptGetStatusAndInfo
			SOAPENVELOPE		Dette er den XML der sendes til servicen. RCID og count (antal pakker) er indeholdt
								i denne XML.
	 */
	public static void main(String[] args) throws KeyManagementException, UnrecoverableKeyException, KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, InterruptedException {
		try {
			InputStream input = new FileInputStream(args[0]);
			Properties prop = new Properties();
			prop.load(input);
			input.close();
				
			final String keystore = args[1];
			final String keystorepwd = args[2];
			final String URL = args[3];
			final String soapaction = args[4];
			final String soapenvelope = args[5];
			final String certfile = prop.getProperty("CERTFILE");
			final String certpwd = prop.getProperty("CERTPASSWORD");
			final int successcode = Integer.parseInt(prop.getProperty("SUCCESSCODE"));

			HTTPCertClient hcl = new HTTPCertClient();
			java.net.http.HttpResponse<String> response = hcl.soapkald(keystore,
																		keystorepwd,
																		URL,
																		soapaction,
																		soapenvelope,
																		certfile,
																		certpwd);
			
			if (response.statusCode() == successcode) {
				System.out.println(response.body());
			} else {
				throw new Exception(response.statusCode() + "; " + response.body());
			}
		} catch (Exception e) {
			System.err.println("Error calling endpoint: " + e.getMessage());
			
		}
	}
}