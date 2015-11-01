package xcat.daiyonkaigi.guchiruna.tokenize;


import net.reduls.sanmoku.Morpheme;
import net.reduls.sanmoku.Tagger;

import java.util.List;


/**
 * 文字列をトークン化し、kuromojiライブラリを実行するクラスです。
 */
public class StringToToken {
    public static List<Morpheme> tokenize(String sentence) {
        return Tagger.parse(sentence);
    }
}
