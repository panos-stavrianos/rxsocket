package gr.osnet.rxsocket;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void unpackTest() {

        String data = "l+U93kFYeO4YbfAH/0CyG3y6wnzNT935URzNAElKJ5h7gjxPjF99+EQrVsdGX+qIeIipc3lUVI7nmfSMOElLblybDSxO90ms/Q19bIzNTwTXX4yACyRtT8HgsSVk174wQW3qj9r/f+I2xPzj7dgaskK+WOLbqX6QvAp1wGaJKOzBlIx1LnjXftyt2qb8MvUEnCeHtxME6ZCvfcDkX/UkN2/COjf/YPFo048utM/q9+2EUqgdtIAh6CJNOvWZUl9uoWPdDekM25mf8F+u7yxfaEa46xGZEQ4WhPxy1Epj11TwlXeofuNczitsDQqj5igvEk2pQLUm8LkRvhGburFGJQ==";

        // AES.INSTANCE.unpack(data, "1234");
        //System.err.println(Base64.decodeBase64(data.getBytes()));
        System.err.println(AES.INSTANCE.unpack(data, "1234"));

        byte[] a = AES.INSTANCE.decrypt(data.getBytes(), "1234");
        String b = AES.INSTANCE.decompress(a);
        try {
            System.err.println(URLDecoder.decode(b, "UTF-8"));
            System.err.println(URLDecoder.decode(b, "ISO-8859-1"));
            System.err.println(URLDecoder.decode(b, "US-ASCII"));
            System.err.println(URLDecoder.decode(b, "UTF-16BE"));
            System.err.println(URLDecoder.decode(b, "UTF-16LE"));
            System.err.println(URLDecoder.decode(b, "UTF-16"));
            System.err.println(URLDecoder.decode(b, "ISO-2022-KR"));
            System.err.println(URLDecoder.decode(b, "x-MacGreek"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assertEquals(4, 2 + 2);

    }

    @Test
    public void packTest() {


        // AES.INSTANCE.unpack(data, "1234");
        //System.err.println(Base64.decodeBase64(data.getBytes()));
      /*  val compressed = compress(data)
        val encrypted = if (pre_shared_key != null)
            encrypt(compressed, pre_shared_key)
        else
            compressed
        return encrypted*/
        byte[] data = "ασφ ξν ον σδφΔΦΓ ΔΓ ΦΓΔ'ΔΦΑΆΆΆ ΑΆ ΑΣΓ ´ΑΣ ΓΑΓ ΆΣΓΓΑ ΑΦΣ ΑΦΣ ΑΆΓΆΓΆ Γ'ασφ ξν ον σδφΔΦΓ ΔΓ ΦΓΔ'ΔΦΑΆΆΆ ΑΆ ΑΣΓ ´ΑΣ ΓΑΓ ΆΣΓΓΑ ΑΦΣ ΑΦΣ ΑΆΓΆΓΆ Γ'ασφ ξν ον σδφΔΦΓ ΔΓ ΦΓΔ'ΔΦΑΆΆΆ ΑΆ ΑΣΓ ´ΑΣ ΓΑΓ ΆΣΓΓΑ ΑΦΣ ΑΦΣ ΑΆΓΆΓΆ Γ'ασφ ξν ον σδφΔΦΓ ΔΓ ΦΓΔ'ΔΦΑΆΆΆ ΑΆ ΑΣΓ ´ΑΣ ΓΑΓ ΆΣΓΓΑ ΑΦΣ ΑΦΣ ΑΆΓΆΓΆ Γ'ασφ ξν ον σδφΔΦΓ ΔΓ ΦΓΔ'ΔΦΑΆΆΆ ΑΆ ΑΣΓ ´ΑΣ ΓΑΓ ΆΣΓΓΑ ΑΦΣ ΑΦΣ ΑΆΓΆΓΆ Γ'ασφ ξν ον σδφΔΦΓ ΔΓ ΦΓΔ'ΔΦΑΆΆΆ ΑΆ ΑΣΓ ´ΑΣ ΓΑΓ ΆΣΓΓΑ ΑΦΣ ΑΦΣ ΑΆΓΆΓΆ Γ'ασφ ξν ον σδφΔΦΓ ΔΓ ΦΓΔ'ΔΦΑΆΆΆ ΑΆ ΑΣΓ ´ΑΣ ΓΑΓ ΆΣΓΓΑ ΑΦΣ ΑΦΣ ΑΆΓΆΓΆ Γ'ασφ ξν ον σδφΔΦΓ ΔΓ ΦΓΔ'ΔΦΑΆΆΆ ΑΆ ΑΣΓ ´ΑΣ ΓΑΓ ΆΣΓΓΑ ΑΦΣ ΑΦΣ ΑΆΓΆΓΆ Γ'".getBytes();


        byte[] a = AES.INSTANCE.compress(data);
        byte[] b = AES.INSTANCE.encrypt(a, "1234");


        byte[] a1 = AES.INSTANCE.decrypt(b, "1234");
        String b2 = AES.INSTANCE.decompress(a1);
        try {
            System.err.println(URLDecoder.decode(b2, "UTF-8"));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        assertEquals(4, 2 + 2);

    }

}