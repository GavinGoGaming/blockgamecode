/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.network.encryption;

import com.google.common.primitives.Longs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.NetworkEncryptionException;

public class NetworkEncryptionUtils {
    private static final String AES = "AES";
    private static final int AES_KEY_LENGTH = 128;
    private static final String RSA = "RSA";
    private static final int RSA_KEY_LENGTH = 1024;
    private static final String ISO_8859_1 = "ISO_8859_1";
    private static final String SHA1 = "SHA-1";
    public static final String SHA256_WITH_RSA = "SHA256withRSA";
    public static final int SHA256_BITS = 256;
    private static final String RSA_PRIVATE_KEY_PREFIX = "-----BEGIN RSA PRIVATE KEY-----";
    private static final String RSA_PRIVATE_KEY_SUFFIX = "-----END RSA PRIVATE KEY-----";
    public static final String RSA_PUBLIC_KEY_PREFIX = "-----BEGIN RSA PUBLIC KEY-----";
    private static final String RSA_PUBLIC_KEY_SUFFIX = "-----END RSA PUBLIC KEY-----";
    public static final String LINEBREAK = "\n";
    public static final Base64.Encoder BASE64_ENCODER = Base64.getMimeEncoder(76, "\n".getBytes(StandardCharsets.UTF_8));
    public static final Codec<PublicKey> RSA_PUBLIC_KEY_CODEC = Codec.STRING.comapFlatMap(key -> {
        try {
            return DataResult.success(NetworkEncryptionUtils.decodeRsaPublicKeyPem(key));
        } catch (NetworkEncryptionException lv) {
            return DataResult.error(lv::getMessage);
        }
    }, NetworkEncryptionUtils::encodeRsaPublicKey);
    public static final Codec<PrivateKey> RSA_PRIVATE_KEY_CODEC = Codec.STRING.comapFlatMap(key -> {
        try {
            return DataResult.success(NetworkEncryptionUtils.decodeRsaPrivateKeyPem(key));
        } catch (NetworkEncryptionException lv) {
            return DataResult.error(lv::getMessage);
        }
    }, NetworkEncryptionUtils::encodeRsaPrivateKey);

    public static SecretKey generateSecretKey() throws NetworkEncryptionException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
            keyGenerator.init(128);
            return keyGenerator.generateKey();
        } catch (Exception exception) {
            throw new NetworkEncryptionException(exception);
        }
    }

    public static KeyPair generateServerKeyPair() throws NetworkEncryptionException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
            keyPairGenerator.initialize(1024);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception exception) {
            throw new NetworkEncryptionException(exception);
        }
    }

    public static byte[] computeServerId(String baseServerId, PublicKey publicKey, SecretKey secretKey) throws NetworkEncryptionException {
        try {
            return NetworkEncryptionUtils.hash(baseServerId.getBytes(ISO_8859_1), secretKey.getEncoded(), publicKey.getEncoded());
        } catch (Exception exception) {
            throw new NetworkEncryptionException(exception);
        }
    }

    private static byte[] hash(byte[] ... bytes) throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance(SHA1);
        for (byte[] cs : bytes) {
            messageDigest.update(cs);
        }
        return messageDigest.digest();
    }

    private static <T extends Key> T decodePem(String key, String prefix, String suffix, KeyDecoder<T> decoder) throws NetworkEncryptionException {
        int i = key.indexOf(prefix);
        if (i != -1) {
            int j = key.indexOf(suffix, i += prefix.length());
            key = key.substring(i, j + 1);
        }
        try {
            return decoder.apply(Base64.getMimeDecoder().decode(key));
        } catch (IllegalArgumentException illegalArgumentException) {
            throw new NetworkEncryptionException(illegalArgumentException);
        }
    }

    public static PrivateKey decodeRsaPrivateKeyPem(String key) throws NetworkEncryptionException {
        return NetworkEncryptionUtils.decodePem(key, RSA_PRIVATE_KEY_PREFIX, RSA_PRIVATE_KEY_SUFFIX, NetworkEncryptionUtils::decodeEncodedRsaPrivateKey);
    }

    public static PublicKey decodeRsaPublicKeyPem(String key) throws NetworkEncryptionException {
        return NetworkEncryptionUtils.decodePem(key, RSA_PUBLIC_KEY_PREFIX, RSA_PUBLIC_KEY_SUFFIX, NetworkEncryptionUtils::decodeEncodedRsaPublicKey);
    }

    public static String encodeRsaPublicKey(PublicKey key) {
        if (!RSA.equals(key.getAlgorithm())) {
            throw new IllegalArgumentException("Public key must be RSA");
        }
        return "-----BEGIN RSA PUBLIC KEY-----\n" + BASE64_ENCODER.encodeToString(key.getEncoded()) + "\n-----END RSA PUBLIC KEY-----\n";
    }

    public static String encodeRsaPrivateKey(PrivateKey key) {
        if (!RSA.equals(key.getAlgorithm())) {
            throw new IllegalArgumentException("Private key must be RSA");
        }
        return "-----BEGIN RSA PRIVATE KEY-----\n" + BASE64_ENCODER.encodeToString(key.getEncoded()) + "\n-----END RSA PRIVATE KEY-----\n";
    }

    private static PrivateKey decodeEncodedRsaPrivateKey(byte[] key) throws NetworkEncryptionException {
        try {
            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA);
            return keyFactory.generatePrivate(encodedKeySpec);
        } catch (Exception exception) {
            throw new NetworkEncryptionException(exception);
        }
    }

    public static PublicKey decodeEncodedRsaPublicKey(byte[] key) throws NetworkEncryptionException {
        try {
            X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA);
            return keyFactory.generatePublic(encodedKeySpec);
        } catch (Exception exception) {
            throw new NetworkEncryptionException(exception);
        }
    }

    public static SecretKey decryptSecretKey(PrivateKey privateKey, byte[] encryptedSecretKey) throws NetworkEncryptionException {
        byte[] cs = NetworkEncryptionUtils.decrypt(privateKey, encryptedSecretKey);
        try {
            return new SecretKeySpec(cs, AES);
        } catch (Exception exception) {
            throw new NetworkEncryptionException(exception);
        }
    }

    public static byte[] encrypt(Key key, byte[] data) throws NetworkEncryptionException {
        return NetworkEncryptionUtils.crypt(1, key, data);
    }

    public static byte[] decrypt(Key key, byte[] data) throws NetworkEncryptionException {
        return NetworkEncryptionUtils.crypt(2, key, data);
    }

    private static byte[] crypt(int opMode, Key key, byte[] data) throws NetworkEncryptionException {
        try {
            return NetworkEncryptionUtils.createCipher(opMode, key.getAlgorithm(), key).doFinal(data);
        } catch (Exception exception) {
            throw new NetworkEncryptionException(exception);
        }
    }

    private static Cipher createCipher(int opMode, String algorithm, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(opMode, key);
        return cipher;
    }

    public static Cipher cipherFromKey(int opMode, Key key) throws NetworkEncryptionException {
        try {
            Cipher cipher = Cipher.getInstance("AES/CFB8/NoPadding");
            cipher.init(opMode, key, new IvParameterSpec(key.getEncoded()));
            return cipher;
        } catch (Exception exception) {
            throw new NetworkEncryptionException(exception);
        }
    }

    static interface KeyDecoder<T extends Key> {
        public T apply(byte[] var1) throws NetworkEncryptionException;
    }

    public record SignatureData(long salt, byte[] signature) {
        public static final SignatureData NONE = new SignatureData(0L, ByteArrays.EMPTY_ARRAY);

        public SignatureData(PacketByteBuf buf) {
            this(buf.readLong(), buf.readByteArray());
        }

        public boolean isSignaturePresent() {
            return this.signature.length > 0;
        }

        public static void write(PacketByteBuf buf, SignatureData signatureData) {
            buf.writeLong(signatureData.salt);
            buf.writeByteArray(signatureData.signature);
        }

        public byte[] getSalt() {
            return Longs.toByteArray(this.salt);
        }
    }

    public static class SecureRandomUtil {
        private static final SecureRandom SECURE_RANDOM = new SecureRandom();

        public static long nextLong() {
            return SECURE_RANDOM.nextLong();
        }
    }
}

