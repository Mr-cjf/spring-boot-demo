package top.cjf_rb.core.context.crypto.sm2;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.ECGenParameterSpec;

public class Sm2Encryptor {
    static {
        // 添加Bouncy Castle安全提供者
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     生成SM2密钥对

     @return 密钥对
     */
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", BouncyCastleProvider.PROVIDER_NAME);
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("sm2p256v1");
        keyGen.initialize(ecSpec, new SecureRandom());
        return keyGen.generateKeyPair();
    }

    /**
     使用SM2公钥加密数据

     @param publicKey 公钥
     @param data      待加密数据
     @return 加密后的数据
     */
    public static byte[] encrypt(PublicKey publicKey, byte[] data) throws Exception {
        // 使用ECIES算法进行SM2加密
        Cipher cipher = Cipher.getInstance("SM2", BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     使用SM2私钥解密数据

     @param privateKey    私钥
     @param encryptedData 加密的数据
     @return 解密后的数据
     */
    public static byte[] decrypt(PrivateKey privateKey, byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance("SM2", BouncyCastleProvider.PROVIDER_NAME);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }

    /**
     使用SM2私钥签名

     @param privateKey 私钥
     @param data       待签名数据
     @return 签名结果
     */
    public static byte[] sign(PrivateKey privateKey, byte[] data) throws Exception {
        Signature signature = Signature.getInstance("SM2Sign", BouncyCastleProvider.PROVIDER_NAME);
        signature.initSign(privateKey, new SecureRandom());
        signature.update(data);
        return signature.sign();
    }

    /**
     使用SM2公钥验证签名

     @param publicKey 公钥
     @param data      原始数据
     @param signature 签名数据
     @return 验证结果
     */
    public static boolean verify(PublicKey publicKey, byte[] data, byte[] signature) throws Exception {
        Signature sig = Signature.getInstance("SM2Sign", BouncyCastleProvider.PROVIDER_NAME);
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(signature);
    }
}
