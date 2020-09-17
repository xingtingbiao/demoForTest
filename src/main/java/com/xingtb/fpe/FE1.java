package com.xingtb.fpe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class FE1 {
    /**
     * Instantiates a new FE1 instance.
     */
    public FE1() {
    }

    /**
     * A simple round function based on HMAC(SHA-256).
     */
    private static class FPEEncryptor {

        private static final String HMAC_ALGORITHM = "HmacSHA256";

        private byte[] macNT;

        private Mac macGenerator;

        private FPEEncryptor(byte[] key, BigInteger modulus, byte[] tweak) throws FPEException {
            try {
                this.macGenerator = Mac.getInstance(HMAC_ALGORITHM);
                this.macGenerator.init(new SecretKeySpec(key, HMAC_ALGORITHM));
            } catch (NoSuchAlgorithmException e) {
                // This should never happen as HMAC/SHA-2 are built in to the JVM.
                throw new FPEException(HMAC_ALGORITHM + " is not a valid MAC algorithm on this JVM", e);
            } catch (InvalidKeyException e) {
                // Outer class checks that key is more than 1 byte, so don't think this can happen, included for completeness though.
                throw new FPEException("The key passed was not valid for use with " + HMAC_ALGORITHM, e);
            }

            if (tweak == null || tweak.length == 0) {
                throw new IllegalArgumentException("tweak (IV) must be an array of length > 0");
            }

            byte[] encodedModulus = Utility.encode(modulus);

            if (encodedModulus.length > MAX_N_BYTES) {
                throw new IllegalArgumentException("Size of encoded n is too large for FPE encryption (was " + encodedModulus.length + " bytes, max permitted "
                        + MAX_N_BYTES + ")");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                baos.write(Utility.toBEBytes(encodedModulus.length));
                baos.write(encodedModulus);

                baos.write(Utility.toBEBytes(tweak.length));
                baos.write(tweak);

                // Flushing most likely a no-op, but best be sure.
                baos.flush();
            } catch (IOException e) {
                // Can't imagine why this would ever happen!
                throw new FPEException("Unable to write to byte array output stream!", e);
            }
            this.macNT = this.macGenerator.doFinal(baos.toByteArray());
        }

        BigInteger f(int roundNo, BigInteger r) throws FPEException {
            byte[] rBin = Utility.encode(r);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                baos.write(this.macNT);
                baos.write(Utility.toBEBytes(roundNo));

                baos.write(Utility.toBEBytes(rBin.length));
                baos.write(rBin);
            } catch (IOException e) {
                throw new FPEException("Unable to write to internal byte array, this should never happen so indicates a defect in the code", e);
            }

            byte[] x = this.macGenerator.doFinal(baos.toByteArray());
            byte[] positiveX = new byte[x.length + 1];
            System.arraycopy(x, 0, positiveX, 1, x.length);
            // First byte will always be 0 (default value) so BigInteger will always be positive.
            BigInteger ret = new BigInteger(positiveX);
            return ret;
        }
    }

    /**
     * Normally FPE is for SSNs, CC#s, etc.; so limit modulus to 128-bit numbers.
     */
    private static final int MAX_N_BYTES = 128 / 8;

    public BigInteger decrypt(final BigInteger modulus, final BigInteger ciphertext, final byte[] key, final byte[] tweak) throws FPEException {
        if (modulus == null) {
            throw new IllegalArgumentException("modulus must not be null.");
        }
        if (ciphertext == null) {
            throw new IllegalArgumentException("ciphertext must not be null.");
        }

        if (ciphertext.compareTo(modulus) >= 0) {
            throw new IllegalArgumentException("Cannot decrypt a number bigger than the modulus (otherwise this wouldn't be format preserving encryption");
        }

        FPEEncryptor encryptor = new FPEEncryptor(key, modulus, tweak);

        BigInteger[] factors = NumberTheory.factor(modulus);
        BigInteger firstFactor = factors[0];
        BigInteger secondFactor = factors[1];

        int rounds = getNumberOfRounds(firstFactor, secondFactor);

        /*
         * x starts as the ciphertext value and will be modified by several rounds of encryption in the rest of this method before arriving back at the
         * plaintext value, which is returned.
         */
        BigInteger x = ciphertext;

        /*
         * Apply the same algorithm repeatedly on x for the number of rounds given by getNumberOfRounds. Each round increases the security. Note that you must
         * use EXACTLY the same number of rounds to decrypt as you did to encrypt! As the round number is used in the calculation, we count down rather than up.
         */
        for (int round = rounds - 1; round >= 0; round--) {
            /*
             * Effectively reversing the calculation in encrypt for this.
             */
            BigInteger w = x.mod(firstFactor);
            BigInteger right = x.divide(firstFactor);

            BigInteger left = w.subtract(encryptor.f(round, right)).mod(firstFactor);
            x = secondFactor.multiply(left).add(right);
        }

        return x;
    }

    public BigInteger encrypt(final BigInteger modulus, final BigInteger plaintext, final byte[] key, final byte[] tweak) throws FPEException {
        if (modulus == null) {
            throw new IllegalArgumentException("modulus must not be null.");
        }
        if (plaintext == null) {
            throw new IllegalArgumentException("plaintext must not be null.");
        }

        if (plaintext.compareTo(modulus) >= 0) {
            throw new IllegalArgumentException("Cannot encrypt a number bigger than the modulus (otherwise this wouldn't be format preserving encryption");
        }

        FPEEncryptor encryptor = new FPEEncryptor(key, modulus, tweak);

        BigInteger[] factors = NumberTheory.factor(modulus);
        BigInteger firstFactor = factors[0];
        BigInteger secondFactor = factors[1];

        int rounds = getNumberOfRounds(firstFactor, secondFactor);

        /*
         * x starts as the plaintext value and will be modified by several rounds of encryption in the rest of this method before being returned.
         */
        BigInteger x = plaintext;

        /*
         * Apply the same algorithm repeatedly on x for the number of rounds given by getNumberOfRounds. Each round increases the security. Note that the
         * attribute and method names used align to the paper on FE1, not Java conventions on readability.
         */
        for (int round = 0; round != rounds; round++) {
            /*
             * Split the value of x in to left and right values (think splitting the binary in to two halves), around the second (smaller) factor
             */
            BigInteger left = x.divide(secondFactor);
            BigInteger right = x.mod(secondFactor);

            // Recalculate x as firstFactor * right + (left + F(round, right) % firstFactor)
            BigInteger w = left.add(encryptor.f(round, right)).mod(firstFactor);
            x = firstFactor.multiply(right).add(w);
        }

        return x;
    }


    private static int getNumberOfRounds(BigInteger a, BigInteger b) throws FPEException {
        if (a.compareTo(b) == -1) {
            throw new FPEException("FPE rounds: a < b");
        }
        return 3;
    }
}
