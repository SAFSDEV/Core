/**
 ** Copyright (C) SAS Institute, All rights reserved.
 ** General Public License: http://www.opensource.org/licenses/gpl-license.php
 **/
package org.safs;

import java.awt.MouseInfo;
import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import org.safs.text.FileUtilities;
import org.w3c.tools.codec.Base64Decoder;
import org.w3c.tools.codec.Base64Encoder;


/**
 *
<pre>
Usage:
java org.safs.RSA -gen
java org.safs.RSA -gen -out outputfile
java org.safs.RSA -encrypt -data data/file -key publickey/file
java org.safs.RSA -encrypt -data data/file -key publickey/file -out outputfile
java org.safs.RSA -decrypt -data data/file -key privatekey/file
java org.safs.RSA -decrypt -data data/file -key privatekey/file -out outputfile
</pre>
 * History:<br>
 *
 *  <br>   Mar 31, 2014    (Lei Wang) Initial release.
 */
public class RSA {
	public static final String CLASS_NAME = RSA.class.getName();
	/**The default encoding used to interpret plain-text, it is 'UTF-8'*/
	public static final String UTF8_CHARSET = "UTF-8";
	/**The default length of generated key, it is 1024*/
	public static final int ENGRYPT_KEY_LENGHT = 1024;//512 Minimum

	/**The algorithm of encryption, it is 'RSA'*/
	public static final String KEY_ALGORITHM = "RSA";
	/**The algorithm of signature, it is 'MD5withRSA'*/
	public static final String SIGNATURE_ALGORITHM = "MD5withRSA";
	/**The map key for storing a public key in the map, it is '__RSAPublicKey__'*/
	public static final String MAPKEY_FOR_PUBLICKEY = "__RSAPublicKey__";
	/**The map key for storing a private key in the map, it is '__RSAPrivateKey__'*/
	public static final String MAPKEY_FOR_PRIVATEKEY = "__RSAPrivateKey__";
	public static final String SAFS_ENCRYPTED_STRING_PREFIX = "__SAFS_ENCRYPTED_STRING__";

	public static final String PARAM_TEST 	= "-test";

	public static final String PARAM_GENERATE_KEY 	= "-gen";
	public static final String PARAM_OUT_FILE 		= "-out";

	public static final String PARAM_ENCRYPT 		= "-encrypt";
	public static final String PARAM_DECRYPTE 		= "-decrypt";
	public static final String PARAM_DATA 			= "-data";
	public static final String PARAM_KEY 			= "-key";

	/**
	 * @param encodedPrivateKey	String the base64-encoded private key.
	 * @return PrivateKey the private key
	 * @throws Exception
	 */
	public static PrivateKey decodePrivateKey(String encodedPrivateKey) throws Exception{
		byte[] keybytes = Base64Decoder.decodeBase64Bytes(encodedPrivateKey);

		PKCS8EncodedKeySpec keyspec = new PKCS8EncodedKeySpec(keybytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		PrivateKey privateKey = keyFactory.generatePrivate(keyspec);

		return privateKey;
	}

	/**
	 * @param encodedPublicKey String the base64-encoded public key.
	 * @return PublicKey the public key
	 * @throws Exception
	 */
	public static PublicKey decodePublicKey(String encodedPublicKey) throws Exception{
		byte[] keybytes = Base64Decoder.decodeBase64Bytes(encodedPublicKey);

		X509EncodedKeySpec keyspec = new X509EncodedKeySpec(keybytes);
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		PublicKey publicKey = keyFactory.generatePublic(keyspec);

		return publicKey;
	}

	/**
	 * @param encodedKey	the base64-encoded key
	 * @param isPrivate		if true, then encodedKey is private; otherwise, the encodedKey is public
	 * @return	Key			the decoded key
	 * @throws Exception
	 */
	public static Key decodeEncodedKey(String encodedKey, boolean isPrivate) throws Exception{
		Key key = null;

		if(isPrivate){
			key = decodePrivateKey(encodedKey);
		}else{
			key = decodePublicKey(encodedKey);
		}

		return key;
	}

	/**
	 * Sign the data with private key, and base64-encode the signature.
	 * @param data byte[], the data to sign
	 * @param encodedPrivateKey String, the base64 encoded private key string
	 * @return String the base64-encoded signature.
	 * @throws Exception
	 */
	public static String sign(byte[] data, String encodedPrivateKey) throws Exception{
		PrivateKey privateKey = decodePrivateKey(encodedPrivateKey);

		//sign the data with private key
		Signature worker = Signature.getInstance(SIGNATURE_ALGORITHM);
		worker.initSign(privateKey);
		worker.update(data);

		byte[] signature = worker.sign();
		return Base64Encoder.encodeBase64Bytes(signature);
	}

	/**
	 * Use 'public key' to verify the data is signed (signed by 'private key') correclty.
	 * @param data byte[], the signed data (by 'private key')
	 * @param encodedPublicKey String, the base64-encoded public key.
	 * @param encodedSignature String, the base64-encoded signature.
	 * @return true if the data is signed by the right person (private key)
	 * @throws Exception
	 */
	public static boolean verifySignature(byte[] data, String encodedPublicKey, String encodedSignature) throws Exception{
		PublicKey publicKey = decodePublicKey(encodedPublicKey);

		//use 'public key' to verify the data is signed by the right person
		Signature worker = Signature.getInstance(SIGNATURE_ALGORITHM);
		worker.initVerify(publicKey);
		worker.update(data);

		byte[] signature = Base64Decoder.decodeBase64Bytes(encodedSignature);
		return worker.verify(signature);
	}

	/**====================================================
	 * ENCRYPT BY PRIVATE_KEY, DECRPT BY PUBLIC_KEY, user could sign the "encrypted data" before sending it out.
	 * ====================================================*/
	/**
	 * Encrypt the string data by private key, data is interpreted as UTF-8 encoding.
	 * @param data	String, the string data to encrypt, it will be interpreted as UTF-8 encoding
	 * @param encodedPrivateKey	String, the base64-encoded private key
	 * @return	String, the base64-encoded encrypted data
	 * @throws Exception
	 */
	public static String encryptByPrivateKey(String data, String encodedPrivateKey) throws Exception{
		return encryptByPrivateKey(data, encodedPrivateKey, UTF8_CHARSET);
	}
	/**
	 * Encrypt the string data by private key.
	 * @param data	String, the string data to encrypt
	 * @param encodedPrivateKey	String, the base64-encoded private key
	 * @param encoding String, the encoding used to interpret the string data
	 * @return	String, the base64-encoded encrypted string data
	 * @throws Exception
	 */
	public static String encryptByPrivateKey(String data, String encodedPrivateKey, String encoding) throws Exception{
		byte[] bytes = encryptByPrivateKey(data.getBytes(Charset.forName(encoding)), encodedPrivateKey);
		return Base64Encoder.encodeBase64Bytes(bytes);
	}
	/**
	 * @param data	byte[], the bytes data to encrypt
	 * @param encodedPrivateKey	String, the base64-encoded private key
	 * @return	byte[], the encrypted bytes data
	 * @throws Exception
	 */
	public static byte[] encryptByPrivateKey(byte[] data, String encodedPrivateKey) throws Exception{
		return encryptByKey(data, encodedPrivateKey, true);
	}
	/**
	 * Decrypt the base64-encoded enctyped string data by public key.
	 * @param data	String, the base64-encoded string data to decrypt, the decrypted bytes will be interpreted by UTF-8 encoding
	 * @param encodedPublicKey	String, the base64-encoded public key
	 * @return String the uts-8 encoded decrypted string
	 * @throws Exception
	 */
	public static String decryptByPublicKey(String data, String encodedPublicKey) throws Exception{
		return decryptByPublicKey(data, encodedPublicKey, UTF8_CHARSET);
	}
	/**
	 * Decrypt the base64-encoded enctyped string data by public key.
	 * @param data	String, the base64-encoded string data to decrypt.
	 * @param encodedPublicKey	String, the base64-encoded public key
	 * @param encoding String, the encoding used to interpret the decryped bytes.
	 * @return	String the decrypted string (encoded by parameter encoding)
	 * @throws Exception
	 */
	public static String decryptByPublicKey(String data, String encodedPublicKey, String encoding) throws Exception{
		byte[] bytes = decryptByPublicKey(Base64Decoder.decodeBase64Bytes(data), encodedPublicKey);
		return new String(bytes, Charset.forName(encoding));
	}
	/**
	 * Decrypt the bytes data by public key.
	 * @param data byte[], the bytes data to decrypt
	 * @param encodedPublicKey	String, the base64-encoded public key
	 * @return byte[], the decrypted bytes data
	 * @throws Exception
	 */
	public static byte[] decryptByPublicKey(byte[] data, String encodedPublicKey) throws Exception{
		return decryptByKey(data, encodedPublicKey, false);
	}
	/**========================================================================================================*/

	/**====================================================
	 * ENCRYPT BY PUBLIC_KEY, DECRPT BY PRIVATE_KEY
	 * ==================================================== */
	/**
	 * Encrypt the string data by public key, data is interpreted as UTF-8 encoding.
	 * @param data	String, the string data to encrypt, it will be interpreted as UTF-8 encoding
	 * @param encodedPublicKey	String, the base64-encoded public key
	 * @return	String, the base64-encoded encrypted data
	 * @throws Exception
	 */
	public static String encryptByPublicKey(String data, String encodedPublicKey) throws Exception{
		return encryptByPublicKey(data, encodedPublicKey, UTF8_CHARSET);
	}
	/**
	 * Encrypt the string data by public key.
	 * @param data	String, the string data to encrypt
	 * @param encodedPublicKey	String, the base64-encoded public key
	 * @param encoding String, the encoding used to interpret the string data
	 * @return	String, the base64-encoded encrypted string data
	 * @throws Exception
	 */
	public static String encryptByPublicKey(String data, String encodedPublicKey, String encoding) throws Exception{
		byte[] bytes = encryptByPublicKey(data.getBytes(Charset.forName(encoding)), encodedPublicKey);
		return Base64Encoder.encodeBase64Bytes(bytes);
	}
	/**
	 * Encrypt the bytes data by public key.
	 * @param data	byte[], the bytes data to encrypt
	 * @param encodedPublicKey	String, the base64-encoded public key
	 * @return	byte[], the encrypted bytes data
	 * @throws Exception
	 */
	public static byte[] encryptByPublicKey(byte[] data, String encodedPublicKey) throws Exception{
		return encryptByKey(data, encodedPublicKey, false);
	}
	/**
	 * Decrypt the base64-encoded enctyped string data by private key.
	 * @param data	String, the base64-encoded string data to decrypt, the decrypted bytes will be interpreted by UTF-8 encoding
	 * @param encodedPrivateKey	String, the base64-encoded private key
	 * @return	String, the decrypted string (encoded by UTF-8 encoding)
	 * @throws Exception
	 */
	public static String decryptByPrivateKey(String data, String encodedPrivateKey) throws Exception{
		return decryptByPrivateKey(data, encodedPrivateKey, UTF8_CHARSET);
	}
	/**
	 * Decrypt the base64-encoded enctyped string data by private key.
	 * @param data	String, the base64-encoded string data to decrypt.
	 * @param encodedPrivateKey	String, the base64-encoded private key
	 * @param encoding String, the encoding used to interpret the decrypted bytes
	 * @return	String, the decrypted string (encoded by paramter encoding)
	 * @throws Exception
	 */
	public static String decryptByPrivateKey(String data, String encodedPrivateKey, String encoding) throws Exception{
		byte[] bytes = decryptByPrivateKey(Base64Decoder.decodeBase64Bytes(data), encodedPrivateKey);
		return new String(bytes, Charset.forName(encoding));
	}
	/**
	 * Decrypt the bytes data by private key.
	 * @param data byte[], the bytes data to decrypt
	 * @param encodedPrivateKey String, the base64-encoded private key
	 * @return byte[], the decrypted bytes data
	 * @throws Exception
	 */
	public static byte[] decryptByPrivateKey(byte[] data, String encodedPrivateKey) throws Exception{
		return decryptByKey(data, encodedPrivateKey, true);
	}
	/**========================================================================================================*/

	/**
	 * Encrypt the 'data' by key (private or public).
	 * @param data	byte[], the bytes to encrypt.
	 * @param encodedKey	String, the key (base64-encoded) used to encrypt
	 * @param usePrivateKey	boolean, if true, the encodedKey is considered as private key; otherwise it is public key.
	 * @return	byte[], the "encrypted bytes"
	 * @throws Exception
	 */
	private static byte[] encryptByKey(byte[] data, String encodedKey, boolean usePrivateKey) throws Exception{
		Key key = decodeEncodedKey(encodedKey, usePrivateKey);

		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, key);

		return cipher.doFinal(data);
	}
	/**
	 * Decrypt the 'encrypted data' by key (private or public).
	 * @param data	byte[], the bytes to decrypt.
	 * @param encodedKey	String, the key (base64-encoded) used to decrypt
	 * @param usePrivateKey	boolean, if true, the encodedKey is considered as private key; otherwise it is public key.
	 * @return	byte[], the "decrypted bytes"
	 * @throws Exception
	 */
	private static byte[] decryptByKey(byte[] data, String encodedKey, boolean usePrivateKey) throws Exception{
		Key key = decodeEncodedKey(encodedKey, usePrivateKey);

		Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, key);
		//RSA has a limitation for the length of bytes to decrypt
		System.out.println("data length "+data.length);

		return cipher.doFinal(data);
	}

	/**
	 * @param keyMap the map containing the base64-encoded private key.
	 * @return String, the base64-encoded private key.
	 * @throws Exception
	 * @see {@link #initKeys()}
	 */
	public static String getPrivateKey(Map<String, String> keyMap) throws Exception{
		String key = keyMap.get(MAPKEY_FOR_PRIVATEKEY);

		if(key==null) throw new Exception("Cannot get the private key from map");

		return key;
	}
	/**
	 * @param keyMap the map containing the base64-encoded public key.
	 * @return String, the base64-encoded public key.
	 * @throws Exception
	 * @see {@link #initKeys()}
	 */
	public static String getPublicKey(Map<String, String> keyMap) throws Exception{
		String key = keyMap.get(MAPKEY_FOR_PUBLICKEY);

		if(key==null) throw new Exception("Cannot get the public key from map");

		return key;
	}

	/**
	 * Generate key-pair, the public-key and private-key; then base64-encode them and put them into a map.
	 * @return	Map<String, String>, a map containing base64-encoded key pairs, <br>
	 *                               <{@link #MAPKEY_FOR_PRIVATEKEY}, encoded-private-key-string><br>
	 *                               <{@link #MAPKEY_FOR_PUBLICKEY}, encoded-public-key-string><br>
	 * @throws Exception
	 */
	public static Map<String, String> initKeys() throws Exception{
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		System.out.println("Please move your mouse to get seeds...");
		int pausetime = 50;
		Point mousePreviousLocation = MouseInfo.getPointerInfo().getLocation();
		Point mouseLocation = null;

		int seedsLength = 1024;
		byte[] seeds = new byte[seedsLength];
		byte[] coordinationBytes = null;
		int index = 0;
		DecimalFormat df = new DecimalFormat("%##.00");
		while(index<seedsLength){
			mouseLocation = MouseInfo.getPointerInfo().getLocation();
			if(!mouseLocation.equals(mousePreviousLocation)){
				coordinationBytes = getBytes(mouseLocation.x);
				System.arraycopy(coordinationBytes, 0, seeds, index, coordinationBytes.length);
				index += coordinationBytes.length;

				coordinationBytes = getBytes(mouseLocation.y);
				System.arraycopy(coordinationBytes, 0, seeds, index, coordinationBytes.length);
				index += coordinationBytes.length;

				mousePreviousLocation = mouseLocation;
				System.out.println("Getting seeds: "+df.format((float)index/(float)seedsLength)+" finished.");
			}else{
				try {Thread.sleep(pausetime);}catch(Exception e){}
			}
		}

		SecureRandom random = new SecureRandom(seeds);
		keyPairGen.initialize(ENGRYPT_KEY_LENGHT, random);
//		keyPairGen.initialize(ENGRYPT_KEY_LENGHT);

		KeyPair keyPair = keyPairGen.generateKeyPair();

		Map<String, String> keyMap = new HashMap<String, String>();

		keyMap.put(MAPKEY_FOR_PRIVATEKEY, Base64Encoder.encodeBase64Bytes(keyPair.getPrivate().getEncoded()));
		keyMap.put(MAPKEY_FOR_PUBLICKEY, Base64Encoder.encodeBase64Bytes(keyPair.getPublic().getEncoded()));

		return keyMap;
	}

	/**
	 * Get the 4 bytes of an integer.
	 * @param coordination	int, the
	 * @return	byte[], an array of byte for an integer
	 */
	private static byte[] getBytes(int coordination){
		byte[] bytes = new byte[4];
		for(int i=0;i<4;i++){
			bytes[i] = (byte) (coordination & 0XFF);
			coordination = coordination>>8;
		}
		return bytes;
	}

	public static void main(String[] args) throws Exception{
		Map<String, String> keyMap = null;
		String pubkey = null;
		String prikey = null;

		String arg = null;
		boolean test = false;
		boolean generateKey = false;
		boolean encrypt = false;
		boolean decrypt = false;
		/* The file to contain the base64-encoded-generated-keys, or encrypted-string, or decrypted-string*/
		String outputFile = null;
		/* The data to encrypt or to decrypt*/
		String data = null;
		/* The key used to encrypt data or to decrypt data*/
		String key = null;

		String testfile = null;

		for(int i=0;i<args.length;i++){
			arg = args[i];
			if(PARAM_TEST.equals(arg)){
				test = true;
				if((i+1)<args.length && !args[i+1].startsWith("-")){
					testfile = args[++i];
				}
			}else if(PARAM_GENERATE_KEY.equals(arg)){
				generateKey = true;
			}else if(PARAM_ENCRYPT.equals(arg)){
				encrypt = true;
			}else if(PARAM_DECRYPTE.equals(arg)){
				decrypt = true;
			}else if(PARAM_OUT_FILE.equals(arg)){
				if(++i<args.length && !args[i].startsWith("-")){
					outputFile = args[i];
				}else{
					System.err.println("Error: miss outputfile. Usage: "+PARAM_OUT_FILE+" outputFile");
					return;
				}
			}else if(PARAM_DATA.equals(arg)){
				if(++i<args.length && !args[i].startsWith("-")){
					data = args[i];
				}else{
					System.err.println("Error: miss data. Usage: "+PARAM_DATA+" data");
					return;
				}
			}else if(PARAM_KEY.equals(arg)){
				if(++i<args.length && !args[i].startsWith("-")){
					key = args[i];
				}else{
					System.err.println("Error: miss key. Usage: "+PARAM_KEY+" key");
					return;
				}
			}else{
				System.out.println("Warning: Unkonwn parameter: '"+arg+"'");
			}
		}

		if(test || generateKey){
			keyMap = RSA.initKeys();
			pubkey = RSA.getPublicKey(keyMap);
			prikey = RSA.getPrivateKey(keyMap);

			StringBuffer sb = new StringBuffer();
			sb.append(MAPKEY_FOR_PUBLICKEY+"\n");
			sb.append(pubkey+"\n");
			sb.append("\n");
			sb.append(MAPKEY_FOR_PRIVATEKEY+"\n");
			sb.append(prikey+"\n");
			if(generateKey && outputFile!=null){
				//We don't really need to write/read the "encoded-key" with UTF-8 encoding.
				FileUtilities.writeStringToUTF8File(outputFile, sb.toString());
				System.out.println("Key Pairs have been saved to file '"+outputFile+"'");
			}else{
				System.out.println(sb.toString());

				if(test){
					String text = "YouPassword123dfaderewafaDAEGEAEDGEWRVSfsdf598";
//					byte[] bytes = text.getBytes();
					byte[] bytes = text.getBytes(Charset.forName("UTF-8"));
					testEncyptByPublicKeyAndDecryptByPrivateKey(bytes, pubkey, prikey);
					testEncyptByPrivateKeyAndDecryptByPublicKey(bytes, pubkey, prikey);

					if(testfile!=null) readRemote(testfile);
				}
			}
		}else if(encrypt || decrypt){
			StringBuffer sb = new StringBuffer();

			try{
				data = FileUtilities.readStringFromUTF8File(data);
			}catch(Exception e){
				System.out.println("Info: data is provided directly");
			}
			try{
				key = FileUtilities.readStringFromUTF8File(key);
			}catch(Exception e){
				System.out.println("Info: key is provided directly");
			}

			if(encrypt){
				String encryptedString = RSA.encryptByPublicKey(data, key);
				sb.append(encryptedString);
			}else if(decrypt){
				String decryptedString = RSA.decryptByPrivateKey(data, key);
				sb.append(decryptedString);
			}

			if(outputFile!=null){
				FileUtilities.writeStringToUTF8File(outputFile, sb.toString());
				System.out.println("Encrypted/Decrypted string have been saved to file '"+outputFile+"'");
			}else{
				System.out.println("Encrypted/Decrypted string:");
				System.out.println(sb.toString());
			}
		}else{
			System.out.println(getUsage());
			return;
		}

	}

	public static String getUsage(){
		StringBuffer usage = new StringBuffer();

		usage.append("Usage:\n");
		usage.append("java "+CLASS_NAME+" "+PARAM_GENERATE_KEY+"\n");
		usage.append("java "+CLASS_NAME+" "+PARAM_GENERATE_KEY+" "+PARAM_OUT_FILE+" outputfile\n");
		usage.append("java "+CLASS_NAME+" "+PARAM_ENCRYPT+" "+PARAM_DATA+" data/file "+PARAM_KEY+" publickey/file\n");
		usage.append("java "+CLASS_NAME+" "+PARAM_ENCRYPT+" "+PARAM_DATA+" data/file "+PARAM_KEY+" publickey/file "+PARAM_OUT_FILE+" outputfile\n");
		usage.append("java "+CLASS_NAME+" "+PARAM_DECRYPTE+" "+PARAM_DATA+" data/file "+PARAM_KEY+" privatekey/file\n");
		usage.append("java "+CLASS_NAME+" "+PARAM_DECRYPTE+" "+PARAM_DATA+" data/file "+PARAM_KEY+" privatekey/file "+PARAM_OUT_FILE+" outputfile\n");

		return usage.toString();
	}

	public static void testEncyptByPublicKeyAndDecryptByPrivateKey(byte[] data, String pubkey, String prikey) throws Exception{
		byte[] encryptedData = RSA.encryptByPublicKey(data, pubkey);



		byte[] decryptedData = RSA.decryptByPrivateKey(encryptedData, prikey);
//		String t = Base64Encoder.encodeBase64Bytes(encryptedData);
//		byte[] decryptedData = RSA.decryptByPrivateKey(Base64Decoder.decodeBase64Bytes(t), prikey);

		String originalStr = new String(data);
		String encryptedStr = new String(encryptedData);
		String decryptedStr = new String(decryptedData);

		System.out.println("originalStr="+originalStr);
		System.out.println("encryptedStr="+encryptedStr);
		System.out.println("decryptedStr="+decryptedStr);

		if(originalStr.equals(decryptedStr)){
			System.out.print("Success");
		}else{
			System.err.print("Fail");
		}
	}

	public static void testEncyptByPrivateKeyAndDecryptByPublicKey(byte[] data, String pubkey, String prikey) throws Exception{

		byte[] encryptedData = RSA.encryptByPrivateKey(data, prikey);

		byte[] decryptedData = RSA.decryptByPublicKey(encryptedData, pubkey);

		String originalStr = new String(data);
		String encryptedStr = new String(encryptedData);
		String decryptedStr = new String(decryptedData);

		System.out.println("originalStr="+originalStr);
		System.out.println("encryptedStr="+encryptedStr);
		System.out.println("decryptedStr="+decryptedStr);

		if(originalStr.equals(decryptedStr)){
			System.out.print("Success");
		}else{
			System.err.print("Fail");
		}

		//Signature
		String sign = RSA.sign(encryptedData, prikey);
		System.out.println("Signature: \n"+sign);

		if(RSA.verifySignature(encryptedData, pubkey, sign)){
			System.out.println("The encryptedData has correct signature.");
		}else{
			System.err.println("The encryptedData does not have correct signature.");
		}

		//Anybody can decrypt the "private-key-encrypted data" with a public-key
		RSA.decryptByPublicKey(encryptedData, pubkey);

		encryptedStr = new String(encryptedData);
		decryptedStr = new String(decryptedData);
		System.out.println("originalStr="+originalStr);
		System.out.println("signed encryptedStr ="+encryptedStr);
		System.out.println("signed decryptedStr="+decryptedStr);

		if(originalStr.equals(decryptedStr)){
			System.out.print("Success");
		}else{
			System.err.print("Fail");
		}
	}

	public static void readRemote(String filename) throws FileNotFoundException, IOException{
		String pass = FileUtilities.readStringFromUTF8File(filename);
		System.out.println(pass);
	}
}