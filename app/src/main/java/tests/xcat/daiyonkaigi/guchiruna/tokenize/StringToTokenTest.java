package tests.xcat.daiyonkaigi.guchiruna.tokenize;


import junit.framework.TestCase;


import net.reduls.sanmoku.Morpheme;

import java.util.List;

import xcat.daiyonkaigi.guchiruna.tokenize.StringToToken;

public class StringToTokenTest extends TestCase {
    public void testTokenize() throws Exception {
        List<Morpheme> list = StringToToken.tokenize("冷やし中華");
        for(Morpheme token : list){
            //TODO テスト書く
            //System.out.println(token.getAllFeatures().toString());
        }
    }
}