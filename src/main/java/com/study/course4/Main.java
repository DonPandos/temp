package com.study.course4;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        Security.addProvider(new BouncyCastleProvider());
        byte[] fileBytes = "Hello world".getBytes(StandardCharsets.UTF_8);
        byte[] hash = getHash("Hello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello world".getBytes(StandardCharsets.UTF_8));
        byte[] bytes = addHashToBytes(hash, fileBytes);
        SecretKey key = KeyGenerator.getInstance("AES").generateKey();
        byte[] encodedBytes = encodeByAES(bytes, key);

        File image = new File("src/main/resources/image.bmp");
        BufferedImage imageWithData = putDataToImage(image, bytes);
        File file = new File("hello.png");
        file.createNewFile();
        ImageIO.write(imageWithData, "png", file);
        getDataFromImage(file);
    }

    public static byte[] getHash(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("MD5");
        return digest.digest(bytes);
    }

    public static byte[] addHashToBytes(byte[] hash, byte[] bytes) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(hash);
        outputStream.write(bytes);

        return outputStream.toByteArray();
    }

    public static byte[] encodeByAES(byte[] bytes, SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(bytes);
    }

    public static BufferedImage putDataToImage(File image, byte[] data) throws IOException {
        data = Base64.getEncoder().encode(data);
        BufferedImage bufferedImage = ImageIO.read(image);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int[][] pixels = new int[width][height];
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                pixels[i][j] = bufferedImage.getRGB(i, j);
            }
        }
        int pointer = 0;
        boolean dataEnds = false;
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                if(pointer == data.length) dataEnds = true;
                BitSet pixelBS = BitSet.valueOf(ByteBuffer.allocate(4).putInt(pixels[i][j]).array());
                BitSet dataBS;
                if(!dataEnds) dataBS = BitSet.valueOf(new byte[] {data[pointer]});
                else {
                    dataBS = BitSet.valueOf("|".getBytes(StandardCharsets.UTF_8));
                }
                pointer++;
                pixelBS.set(0, dataBS.get(0));
                pixelBS.set(1, dataBS.get(1));
                pixelBS.set(8, dataBS.get(2));
                pixelBS.set(9, dataBS.get(3));
                pixelBS.set(16, dataBS.get(4));
                pixelBS.set(17, dataBS.get(5));
                pixelBS.set(24, dataBS.get(6));
                pixelBS.set(25, dataBS.get(7));

                bufferedImage.setRGB(i, j, fromByteArray(pixelBS.toByteArray()));
            }
        }
        return bufferedImage;
    }

    public static void getDataFromImage(File image) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(image);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int[][] pixels = new int[width][height];
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                pixels[i][j] = bufferedImage.getRGB(i, j);
            }
        }
        List<Byte> data = new ArrayList<>();
        BitSet dataBS = BitSet.valueOf("|".getBytes(StandardCharsets.UTF_8));
        for(int i = 0; i < width; i++) {
            for(int j = 0; j < height; j++) {
                BitSet pixelBS = BitSet.valueOf(ByteBuffer.allocate(4).putInt(pixels[i][j]).array());
                dataBS.set(0, pixelBS.get(0));
                dataBS.set(1, pixelBS.get(1));
                dataBS.set(2, pixelBS.get(8));
                dataBS.set(3, pixelBS.get(9));
                dataBS.set(4, pixelBS.get(16));
                dataBS.set(5, pixelBS.get(17));
                dataBS.set(6, pixelBS.get(24));
                dataBS.set(7, pixelBS.get(25));
                if(new String(dataBS.toByteArray(), StandardCharsets.UTF_8).equals("|")) {
                    Byte[] dataByteArray = new Byte[data.size()];
                    dataByteArray = data.toArray(dataByteArray);

                    return;
                }
                data.add(dataBS.toByteArray()[0]);
                bufferedImage.setRGB(i, j, fromByteArray(pixelBS.toByteArray()));
            }
        }
    }

    public static String byteToBinaryString(byte input) {
        return String.format("%8s", Integer.toBinaryString(input & 0xFF)).replace(' ', '0');
    }

    static int fromByteArray(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
    }
}


//                String binaryByteOfData;
//                if(!dataEnds) binaryByteOfData = byteToBinaryString(data[pointer]);
//                else binaryByteOfData = byteToBinaryString(" ".getBytes(StandardCharsets.UTF_8)[0]);
//                pointer++;
//                String binaryPixel = Integer.toBinaryString(pixels[i][j]);
//                StringBuilder binaryPixelStringBuilder = new StringBuilder(binaryPixel);
//                binaryPixelStringBuilder.setCharAt(6, binaryByteOfData.charAt(0));
//                binaryPixelStringBuilder.setCharAt(7, binaryByteOfData.charAt(1));
//                binaryPixelStringBuilder.setCharAt(14, binaryByteOfData.charAt(2));
//                binaryPixelStringBuilder.setCharAt(15, binaryByteOfData.charAt(3));
//                binaryPixelStringBuilder.setCharAt(22, binaryByteOfData.charAt(4));
//                binaryPixelStringBuilder.setCharAt(23, binaryByteOfData.charAt(5));
//                binaryPixelStringBuilder.setCharAt(30, binaryByteOfData.charAt(6));
//                binaryPixelStringBuilder.setCharAt(31, binaryByteOfData.charAt(7));
//                binaryPixel = binaryPixelStringBuilder.toString();
//                bufferedImage.setRGB(i, j, Integer.parseUnsignedInt(binaryPixel, 2));
//                System.out.println(Integer.parseUnsignedInt(binaryPixel, 2));